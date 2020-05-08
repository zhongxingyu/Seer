 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
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
 
 package org.vesalainen.parsers.sql.dsql;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Index;
 import com.google.appengine.api.datastore.Index.IndexState;
 import com.google.appengine.api.datastore.Index.Property;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.PropertyProjection;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.datastore.TransactionOptions;
 import com.google.appengine.api.mail.MailService;
 import com.google.appengine.api.mail.MailService.Message;
 import com.google.appengine.api.mail.MailServiceFactory;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableSet;
 import java.util.Properties;
 import java.util.Set;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.MimeMessage;
 import org.vesalainen.parsers.sql.ColumnCondition;
 import org.vesalainen.parsers.sql.ColumnMetadata;
 import org.vesalainen.parsers.sql.ColumnReference;
 import org.vesalainen.parsers.sql.FetchResult;
 import org.vesalainen.parsers.sql.InsertStatement;
 import org.vesalainen.parsers.sql.JoinCondition;
 import org.vesalainen.parsers.sql.Relation;
 import org.vesalainen.parsers.sql.SQLConverter;
 import org.vesalainen.parsers.sql.Table;
 import org.vesalainen.parsers.sql.TableContext;
 import org.vesalainen.parsers.sql.TableMetadata;
 import org.vesalainen.parsers.sql.TruthValue;
 import org.vesalainen.parsers.sql.ValueComparisonCondition;
 
 /**
  * @author Timo Vesalainen
  */
 public class DatastoreEngine  implements DSProxyInterface
 {
     private static final int CHUNKSIZE = 500;
     private DatastoreService datastore;
     private Statistics statistics;
     private SQLConverter converter;
     private MailService mailService = MailServiceFactory.getMailService();
     private Session session = Session.getDefaultInstance(new Properties(), null);
 
     public DatastoreEngine(DatastoreService datastore)
     {
         this.datastore = datastore;
         if (statistics == null)
         {
             statistics = new Statistics(datastore);
         }
     }
 
     public void setConverter(SQLConverter converter)
     {
         this.converter = converter;
     }
     
     @Override
     public Collection<Entity> fetch(Table<Entity,Object> table)
     {
         String kind = table.getName();
         TableMetadata kindStats = statistics.getKind(kind);
         Query query = new Query(kind);
         List<ColumnCondition<Entity, Object>> locals = new ArrayList<>();
         for (ColumnCondition<Entity, Object> columnCondition : table.getAndConditions())
         {
             if (columnCondition instanceof ValueComparisonCondition)
             {
                 ValueComparisonCondition<Entity, Object> ccc = (ValueComparisonCondition) columnCondition;
                 handleValueComparisonCondition(ccc, query, locals, kindStats);
             }
             else
             {
                 locals.add(columnCondition);
             }
         }
         checkKeysOnlyAndProjection(query, table, false);
         return fetchAndFilter(query, locals);
     }
     
     @Override
     public Collection<Entity> fetch(TableContext<Entity,Object> tc, boolean update)
     {
         DSTable<Entity,Object> table = (DSTable) tc.getTable();
         String kind = table.getName();
         TableMetadata kindStats = statistics.getKind(kind);
         Query query = new Query(kind);
         DSTable ancestor = table.getAncestor();
         if (ancestor != null)
         {
             TableContext otherCtx = tc.getOther(ancestor);
             if (otherCtx.hasData())
             {
                 NavigableSet<Object> columnValues = otherCtx.getColumnValues(Entity.KEY_RESERVED_PROPERTY);
                 if (columnValues.size() == 1)
                 {
                     query.setAncestor((Key)columnValues.first());
                 }
             }
         }
         List<ColumnCondition<Entity, Object>> locals = new ArrayList<>();
         for (ColumnCondition<Entity, Object> columnCondition : table.getAndConditions())
         {
             if (columnCondition instanceof ParentOfCondition)
             {
                 ParentOfCondition poc = (ParentOfCondition) columnCondition;
                 handleParentOfCondition(tc, poc, query);
             }
             else
             {
                 if (columnCondition instanceof ValueComparisonCondition)
                 {
                     ValueComparisonCondition<Entity, Object> ccc = (ValueComparisonCondition) columnCondition;
                     handleValueComparisonCondition(ccc, query, locals, kindStats);
                 }
                 else
                 {
                     if (columnCondition instanceof JoinCondition)
                     {
                         JoinCondition<Entity, Object> jc = (JoinCondition) columnCondition;
                         boolean cont = handleJoinCondition(jc, tc, query, kindStats);
                         if (!cont)
                         {
                             return new ArrayList<>();
                         }
                     }
                     else
                     {
                         locals.add(columnCondition);
                     }
                 }
             }
         }
         checkKeysOnlyAndProjection(query, table, update);
         return fetchAndFilter(query, locals);
     }
     
     private Query.FilterOperator convertRelation(Relation relation)
     {
         switch (relation)
         {
             case EQ:
                 return Query.FilterOperator.EQUAL;
             case NE:
                 return Query.FilterOperator.NOT_EQUAL;
             case LE:
                 return Query.FilterOperator.LESS_THAN_OR_EQUAL;
             case LT:
                 return Query.FilterOperator.LESS_THAN;
             case GE:
                 return Query.FilterOperator.GREATER_THAN_OR_EQUAL;
             case GT:
                 return Query.FilterOperator.GREATER_THAN;
             default:
                 throw new IllegalArgumentException(relation+" not supported");
         }
     }
 
     @Override
     public void update(Collection<Entity> rows)
     {
         datastore.put(rows);
     }
 
     @Override
     public void delete(Collection<Entity> rows)
     {
         List<Key> keys = new ArrayList<>();
         for (Entity entity : rows)
         {
             keys.add(entity.getKey());
         }
         datastore.delete(keys);
     }
 
     @Override
     public void insert(InsertStatement insertStatement)
     {
         Table table = insertStatement.getTable();
         String kind = table.getName();
         FetchResult<Entity,Object> result = insertStatement.getFetchResult();
         for (int row=0;row<result.getRowCount();row++)
         {
             Entity entity = null;
             Key key = (Key) result.getValueAt(row, Entity.KEY_RESERVED_PROPERTY);
             if (key != null)
             {
                 if (!key.getKind().equals(kind))
                 {
                     insertStatement.throwException(key+" key <> tablename "+kind);
                 }
                 entity = new Entity(key);
             }
             else
             {
                 entity = new Entity(kind);
             }
             int keyIndex = result.getColumnIndex(Entity.KEY_RESERVED_PROPERTY);
             for (int col=0;col<result.getColumnCount();col++)
             {
                 if (col != keyIndex)
                 {
                     Object value = result.getValueAt(row, col);
                     if (value != null)
                     {
                         String columnName = result.getColumnName(col);
                         ColumnMetadata cm = statistics.getProperty(kind, columnName);
                         if (cm != null && !cm.isIndexed())
                         {
                             entity.setUnindexedProperty(columnName, value);
                         }
                         else
                         {
                             entity.setProperty(columnName, value);
                         }
                     }
                 }
             }
             datastore.put(entity);
         }
     }
 
     private void handleParentOfCondition(TableContext<Entity, Object> tc, ParentOfCondition poc, Query query)
     {
         ColumnReference otherCf = poc.getColumnReference2();
         Table otherTab = otherCf.getTable();
         TableContext otherCt = tc.getOther(otherTab);
         if (otherCt.hasData())
         {
             NavigableSet<Object> columnValues = otherCt.getColumnValues(Entity.KEY_RESERVED_PROPERTY);
             if (columnValues.size() == 1)
             {
                 query.setAncestor((Key)columnValues.first());
             }
         }
     }
 
     private void handleValueComparisonCondition(ValueComparisonCondition<Entity, Object> ccc, Query query, List<ColumnCondition<Entity, Object>> locals, TableMetadata kindStats)
     {
         String property = ccc.getColumn();
         if (kindStats != null)
         {
             ColumnMetadata propertyStats = kindStats.getColumnMetadata(property);
             if (propertyStats != null && propertyStats.isIndexed())
             {
                 query.addFilter(property, convertRelation(ccc.getRelation()), ccc.getValue());
                 return;
             }
         }
         locals.add(ccc);
     }
 
     private boolean handleJoinCondition(JoinCondition<Entity, Object> jc, TableContext tc, Query query, TableMetadata kindStats)
     {
         String property = jc.getColumn();
         if (kindStats != null)
         {
             ColumnMetadata propertyStats = kindStats.getColumnMetadata(property);
             if (Relation.EQ.equals(jc.getRelation()))
             {
                 ColumnReference cf = jc.getColumnReference2();
                 Table otherTable = cf.getTable();
                 TableContext otherCtx = tc.getOther(otherTable);
                 if (otherCtx.hasData())
                 {
                     NavigableSet<Object> columnValues = otherCtx.getColumnValues(cf.getColumn());
                     switch (columnValues.size())
                     {
                         case 0:
                             return false;
                         case 1:
                             if (propertyStats != null && propertyStats.isIndexed())
                             {
                                 query.addFilter(property, Query.FilterOperator.EQUAL, columnValues.first());
                             }
                             break;
                         default:
                             if (propertyStats != null && propertyStats.isIndexed())
                             {
                                 query.addFilter(property, Query.FilterOperator.GREATER_THAN_OR_EQUAL, columnValues.first());
                                 query.addFilter(property, Query.FilterOperator.LESS_THAN_OR_EQUAL, columnValues.last());
                             }
                             break;
                     }
                 }
             }
         }
         return true;
     }
 
     private Collection<Entity> fetchAndFilter(Query query, List<ColumnCondition<Entity, Object>> locals)
     {
         System.err.println(query);
         PreparedQuery prepared = datastore.prepare(query);
         List<Entity> list = prepared.asList(FetchOptions.Builder.withChunkSize(CHUNKSIZE));
         if (!locals.isEmpty())
         {
             List<Entity> flist = new ArrayList<>();
             for (Entity entity : list)
             {
                 boolean ok = true;
                 for (ColumnCondition cc : locals)
                 {
                     if (cc.matches(converter, entity) != TruthValue.TRUE)
                     {
                         ok = false;
                         break;
                     }
                 }
                 if (ok)
                 {
                     flist.add(entity);
                 }
             }
             System.err.println("filtered from "+list.size()+" to "+flist.size());
             return flist;
         }
         System.err.println("fetched "+list.size());
         return list;
     }
 
     @Override
     public void beginTransaction()
     {
         datastore.beginTransaction(TransactionOptions.Builder.withXG(true));
     }
 
     @Override
     public void commitTransaction()
     {
         datastore.getCurrentTransaction().commit();
     }
 
     @Override
     public void rollbackTransaction()
     {
         datastore.getCurrentTransaction().rollback();
     }
 
     @Override
     public void exit()
     {
         Transaction currentTransaction = datastore.getCurrentTransaction(null);
         if (currentTransaction != null && currentTransaction.isActive())
         {
             currentTransaction.rollback();
         }
     }
 
     public Statistics getStatistics()
     {
         return statistics;
     }
 
     @Override
     public Key createKey(Key parent, String kind, long id)
     {
         return KeyFactory.createKey(parent, kind, id);
     }
 
     @Override
     public Key createKey(Key parent, String kind, String name)
     {
         return KeyFactory.createKey(parent, kind, name);
     }
 
     @Override
     public Key createKey(String kind, long id)
     {
         return KeyFactory.createKey(kind, id);
     }
 
     @Override
     public Key createKey(String kind, String name)
     {
         return KeyFactory.createKey(kind, name);
     }
 
     @Override
     public String createKeyString(Key parent, String kind, long id)
     {
         return KeyFactory.createKeyString(parent, kind, id);
     }
 
     @Override
     public String createKeyString(Key parent, String kind, String name)
     {
         return KeyFactory.createKeyString(parent, kind, name);
     }
 
     @Override
     public String createKeyString(String kind, long id)
     {
         return KeyFactory.createKeyString(kind, id);
     }
 
     @Override
     public String createKeyString(String kind, String name)
     {
         return KeyFactory.createKeyString(kind, name);
     }
 
     @Override
     public String keyToString(Key key)
     {
         return KeyFactory.keyToString(key);
     }
 
     @Override
     public Key stringToKey(String encoded)
     {
         return KeyFactory.stringToKey(encoded);
     }
 
     private void checkKeysOnlyAndProjection(Query query, Table<Entity, Object> table, boolean update)
     {
         Set<String> minOutput = new HashSet<>();       // minimum set of output
         minOutput.addAll(table.getConditionColumns()); // all columns needed in conditions
         for (FilterPredicate fp : query.getFilterPredicates())
         {
             switch (fp.getOperator())
             {
                 case EQUAL:
                 case IN:
                     break;
                 default:
                     minOutput.remove(fp.getPropertyName());     // minus and-path filtered
                     break;
             }
         }
         minOutput.addAll(table.getSelectListColumns()); // plus all in select list
         if (
                 minOutput.size() == 1 &&
                 Entity.KEY_RESERVED_PROPERTY.equals(minOutput.iterator().next())
                 )
         {
             query.setKeysOnly();
             return;
         }
         if (!update)
         {
             // Only indexed properties can be projected.
             for (String property : minOutput)
             {
                 ColumnMetadata cm = statistics.getProperty(table.getName(), property);
                 if (cm == null || !cm.isIndexed())
                 {
                     return;
                 }
             }
             for (FilterPredicate fp : query.getFilterPredicates())
             {
                 switch (fp.getOperator())
                 {
                     case EQUAL:
                     case IN:
                         if (minOutput.contains(fp.getPropertyName()))
                         {
                             return;
                         }
                 }
             }
             if (minOutput.size() > 1)
             {
                 boolean ok1 = false;
                 Map<Index, IndexState> indexes = statistics.getIndexes();
                 for (Entry<Index, IndexState> entry : indexes.entrySet())
                 {
                     if (IndexState.SERVING.equals(entry.getValue()))
                     {
                         List<Property> properties = entry.getKey().getProperties();
                         if (minOutput.size() == properties.size())
                         {
                             boolean ok2 = true;
                             for (Property property : properties)
                             {
                                 if (!minOutput.contains(property.getName()))
                                 {
                                     ok2 = false;
                                     break;
                                 }
                             }
                             if (ok2)
                             {
                                 ok1 = true;
                                 break;
                             }
                         }
                     }
                 }
                 if (!ok1)
                 {
                     return;
                 }
             }
             for (String property : minOutput)
             {
                 query.addProjection(new PropertyProjection(property, null));
             }
         }
     }
 
     @Override
     public void send(Message message) throws IOException
     {
         mailService.send(message);
     }
 
     @Override
     public Session getSession()
     {
         return session;
     }
 
     @Override
     public void send(MimeMessage message) throws IOException
     {
         try
         {
             Transport.send(message);
         }
         catch (MessagingException ex)
         {
             throw new IOException(ex);
         }
     }
 
     @Override
     public Entity get(Key key) throws EntityNotFoundException
     {
         return datastore.get(key);
     }
 
     @Override
     public List<Entity> getAll(String kind)
     {
         Query query = new Query(kind);
         PreparedQuery prepared = datastore.prepare(query);
         return prepared.asList(FetchOptions.Builder.withChunkSize(CHUNKSIZE));
     }
 
     @Override
     public void update(Entity row)
     {
         datastore.put(row);
     }
 
     @Override
     public void delete(Entity row)
     {
         datastore.delete(row.getKey());
     }
 
 }
