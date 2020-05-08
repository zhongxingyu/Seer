 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.dataset.domain.metamodel;
 
 import static edu.dfci.cccb.mev.dataset.domain.metamodel.MetamodelBackedValues.COLUMN_FIELD_NAME;
 import static edu.dfci.cccb.mev.dataset.domain.metamodel.MetamodelBackedValues.ROW_FIELD_NAME;
 import static edu.dfci.cccb.mev.dataset.domain.metamodel.MetamodelBackedValues.VALUE_FIELD_NAME;
 import static java.util.UUID.randomUUID;
 import static org.eobjects.metamodel.DataContextFactory.createJdbcDataContext;
 import static org.eobjects.metamodel.schema.ColumnType.DOUBLE;
 import static org.eobjects.metamodel.schema.ColumnType.VARCHAR;
 
import java.util.concurrent.atomic.AtomicBoolean;

 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.sql.DataSource;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import org.eobjects.metamodel.BatchUpdateScript;
 import org.eobjects.metamodel.UpdateCallback;
 import org.eobjects.metamodel.UpdateableDataContext;
 import org.eobjects.metamodel.schema.Table;
 
 import edu.dfci.cccb.mev.dataset.domain.contract.ValueStoreBuilder;
 import edu.dfci.cccb.mev.dataset.domain.contract.ValueStoreException;
 import edu.dfci.cccb.mev.dataset.domain.contract.Values;
 import edu.dfci.cccb.mev.dataset.domain.prototype.AbstractValueStoreBuilder;
 
 /**
  * @author levk
  * 
  */
 public class MetamodelBackedValueStoreBuilder extends AbstractValueStoreBuilder {
 
   private @Getter @Setter @Inject DataSource dataSource;
 
   private UpdateableDataContext context;
   private Table table;
  private final AtomicBoolean destroy = new AtomicBoolean (false);
 
   @PostConstruct
   private void initialize () {
    (context = createJdbcDataContext (dataSource)).executeUpdate (new BatchUpdateScript () {
 
       @Override
       public void run (UpdateCallback callback) {
         table = callback.createTable (context.getDefaultSchema (), randomUUID ().toString ())
                         .withColumn (ROW_FIELD_NAME).ofType (VARCHAR)
                         .withColumn (COLUMN_FIELD_NAME).ofType (VARCHAR)
                         .withColumn (VALUE_FIELD_NAME).ofType (DOUBLE)
                         .execute ();
        destroy.set (true);
       }
     });
   }
 
   /* (non-Javadoc)
    * @see
    * edu.dfci.cccb.mev.dataset.domain.contract.ValueStoreBuilder#add(double,
    * java.lang.String, java.lang.String) */
   @Override
   public ValueStoreBuilder add (final double value, final String row, final String column) throws ValueStoreException {
     context.executeUpdate (new BatchUpdateScript () {
 
       @Override
       public void run (UpdateCallback callback) {
         callback.insertInto (table)
                 .value (ROW_FIELD_NAME, row)
                 .value (COLUMN_FIELD_NAME, column)
                 .value (VALUE_FIELD_NAME, value)
                 .execute ();
       }
     });
     return this;
   }
 
   /* (non-Javadoc)
    * @see edu.dfci.cccb.mev.dataset.domain.contract.ValueStoreBuilder#build() */
   @Override
   public Values build () {
    destroy.set (false);
     return new MetamodelBackedValues (table, context);
   }

  /* (non-Javadoc)
   * @see java.lang.Object#finalize() */
  @Override
  protected void finalize () throws Throwable {
    if (destroy.getAndSet (false))
      context.executeUpdate (new BatchUpdateScript () {

        @Override
        public void run (UpdateCallback callback) {
          callback.dropTable (table);
        }
      });
  }
 }
