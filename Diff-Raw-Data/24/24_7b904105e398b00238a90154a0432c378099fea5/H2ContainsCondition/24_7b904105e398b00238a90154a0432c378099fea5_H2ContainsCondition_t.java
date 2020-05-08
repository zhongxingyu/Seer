 package com.flexive.core.search.cmis.impl.sql.H2;
 
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.search.cmis.impl.CmisSqlQuery;
 import com.flexive.core.search.cmis.impl.ResultScore;
 import com.flexive.core.search.cmis.impl.sql.ColumnIndex;
 import com.flexive.core.search.cmis.impl.sql.SelectedTableVisitor;
 import com.flexive.core.search.cmis.impl.sql.SqlMapperFactory;
 import com.flexive.core.search.cmis.impl.sql.generic.mapper.condition.GenericContainsCondition;
 import com.flexive.core.search.cmis.model.ContainsCondition;
 import com.flexive.shared.FxFormatUtils;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * H2 fulltext search implementation. Currently this is a dummy implementation that does not
  * utilize fulltext indices, and always returns "1.0" when the fulltext SCORE() is selected.
  *
  * @author Daniel Lichtenberger, UCS
  * @version $Rev$
  */
 public class H2ContainsCondition extends GenericContainsCondition {
     private static final H2ContainsCondition INSTANCE = new H2ContainsCondition();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String selectColumn(SqlMapperFactory sqlMapperFactory, CmisSqlQuery query, ResultScore column, long languageId, boolean xpath, boolean includeResultAlias, ColumnIndex index) {
         // check if we have a fulltext query
         findContainsCondition(query);
         // since H2 doesn't support scoring, we always return 1.0 and don't select from the result
         index.increment();
         return "1.0";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object decodeResultValue(SqlMapperFactory factory, ResultSet rs, ResultScore column, long languageId) throws SQLException {
         return 1.0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getConditionSql(SqlMapperFactory sqlMapperFactory, ContainsCondition condition, SelectedTableVisitor joinedTables) {
         final String alias = joinedTables.getTableAlias(condition.getTableReference());
         return "(SELECT DISTINCT " + joinedTables.getSelectForSingleTable(condition.getTableReference()) + " FROM "
                 + DatabaseConst.TBL_CONTENT_DATA_FT + " " + alias + " "
                 + "WHERE value LIKE "
                + FxFormatUtils.escapeForSql("%" + condition.getExpression().toUpperCase() + "%")
                 + sqlMapperFactory.getSqlDialect().limitSubquery()
                 + ")";
 
     }
 
     public static H2ContainsCondition getInstance() {
         return INSTANCE;
     }
 }
