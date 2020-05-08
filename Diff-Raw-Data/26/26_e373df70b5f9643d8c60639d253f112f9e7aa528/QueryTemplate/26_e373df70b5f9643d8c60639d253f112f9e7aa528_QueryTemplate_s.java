 package jewas.persistence;
 
 import jewas.persistence.exception.CannotGetJDBCConnectionException;
 import jewas.persistence.exception.DataAccessException;
 import jewas.persistence.rowMapper.LongRowMapper;
 import jewas.persistence.rowMapper.RowMapper;
 import jewas.persistence.rowMapper.StringRowMapper;
import jewas.persistence.util.JDBCUtils;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author fcamblor
  */
 public class QueryTemplate<T> {
 
     private RowMapper<T> rowMapper;
     private DataSource datasource;
     private Map<String, Query> queries;
 
     public QueryTemplate(DataSource datasource, RowMapper<T> rowMapper) {
         this.rowMapper = rowMapper;
         this.datasource = datasource;
         this.queries = new HashMap<String,Query>();
     }
 
     static interface InConnectionCallback {
         public void doInConnection(Connection connection);
     }
 
     private static abstract class OpenPreparedStatementInConnection implements InConnectionCallback{
 
         private Query query;
         private QueryExecutionContext context;
 
         public OpenPreparedStatementInConnection(Query query, QueryExecutionContext executionContext){
             this.query = query;
             this.context = executionContext;
         }
 
         @Override
         public void doInConnection(Connection connection) {
            PreparedStatement ps = null;
            try {
                ps = createPreparedStatement(query, connection, context);

                 doWithPreparedStatement(ps);

             } catch (SQLException ex) {
                 throw new DataAccessException("Exception while executing prepared statement", ex);
            } finally {
                JDBCUtils.closeStatementIfNecessary(ps);
                JDBCUtils.closeConnectionIfNecessary(connection);
             }
         }
 
         protected PreparedStatement createPreparedStatement(Query query, Connection c,
                                                             QueryExecutionContext context) throws SQLException{
             return query.preparedStatementForContext(context, c);
         }
 
         public abstract void doWithPreparedStatement(PreparedStatement ps) throws SQLException;
     }
 
     private static class ExecuteUpdateQueryWithPreparedStatement extends OpenPreparedStatementInConnection {
 
         int rowsUpdated = 0;
 
         public ExecuteUpdateQueryWithPreparedStatement(Query query, QueryExecutionContext executionContext){
             super(query, executionContext);
         }
 
         @Override
         public void doWithPreparedStatement(PreparedStatement ps) throws SQLException {
             rowsUpdated = ps.executeUpdate();
         }
 
         public int rowsUpdated(){
             return this.rowsUpdated;
         }
     }
 
     public QueryTemplate<T> addQuery(String queryName, Query q){
         queries.put(queryName, q);
         return this;
     }
 
     // Syntaxic sugar for addQuery(String,Query)
     public QueryTemplate<T> addQuery(String queryName, String sql){
         return addQuery(queryName, new Query(sql));
     }
 
     protected Query query(String name){
         if(queries.containsKey(name)){
             return queries.get(name);
         } else {
             StringBuilder message = new StringBuilder("Unknown query <").append(name).append("> !")
                     .append(String.format("%n")).append(String.format("Available queries : %s"));
             for(Query q : queries.values()){
                 message.append(String.format("%s %n", q));
             }
 
             throw new IllegalArgumentException(message.toString());
         }
     }
 
     public void execute(final InConnectionCallback action) throws DataAccessException {
        Connection connection = null;
        try {
            connection = datasource.getConnection();
         } catch (SQLException e) {
             throw new CannotGetJDBCConnectionException("Cannot get JDBC Connection", e);
         }

        try {
            action.doInConnection(connection);
        } finally {
            JDBCUtils.closeConnectionIfNecessary(connection);
        }
     }
 
     public int executeUpdate(String queryName, QueryExecutionContext executionContext) throws DataAccessException {
         ExecuteUpdateQueryWithPreparedStatement callback = new ExecuteUpdateQueryWithPreparedStatement(
                 query(queryName), executionContext);
         execute(callback);
         return callback.rowsUpdated();
     }
 
     private <U> void select(final List<U> objects, final String queryName,
                         final QueryExecutionContext executionContext, final RowMapper<U> rowMapper) {
         // TODO: check @NonNull of objects with a specialized framework like CheckerFramework or Intellij implem ?
 
         execute(new OpenPreparedStatementInConnection(query(queryName), executionContext) {
             @Override
             public void doWithPreparedStatement(PreparedStatement ps) throws SQLException {
                 ResultSet rs = ps.executeQuery();
                 while (rs.next()) {
                     objects.add(rowMapper.processRow(rs));
                 }
             }
         });
     }
 
     private <U> U selectObject(String queryName, QueryExecutionContext executionContext,
                                RowMapper<U> rowMapper) {
         List<U> unaryList = new ArrayList<U>(1);
         select(unaryList, queryName, executionContext, rowMapper);
 
         // TODO: assert unaryList.size()<=1
 
         if(unaryList.size() == 0){
             return null;
         } else {
             return unaryList.iterator().next();
         }
     }
 
     public void select(final List<T> objects, final String queryName, final QueryExecutionContext executionContext) {
         select(objects, queryName, executionContext, rowMapper);
     }
 
     public T selectObject(String queryName, QueryExecutionContext executionContext) {
         return selectObject(queryName, executionContext, rowMapper);
     }
 
     public Long selectLong(String queryName, QueryExecutionContext executionContext) {
         return selectObject(queryName, executionContext, new LongRowMapper());
     }
 
     public String selectString(String queryName, QueryExecutionContext executionContext) {
         return selectObject(queryName, executionContext, new StringRowMapper());
     }
 
     public void insert(final String queryName, final QueryExecutionContext executionContext){
         insert(queryName, executionContext, null);
     }
 
     /**
      * Insert a row in the database
      * @param queryName The name of the insert sql query
      * @param executionContext Current Querycontext
      * @param generatedKeyColumns List of column names which will be auto-filled by the database
      * (auto increment / sequence fields).
      * IMPORTANT NOTE : The key columns ordering must be the same as the calling of auto increment / sequence
      * execution ordering !
      * For example, if we call "insert into azerty (id1, id2) values (seq.nextval, seq.nextval) the generatedKeyColumns
      * must be filled with ["id1", "id2"]
      * @return A Map having every generated column values. It is in the form [column name => generated value ]
      */
     public Map<String,String> insert(final String queryName, final QueryExecutionContext executionContext, final String... generatedKeyColumns){
         final Map<String, String> genKeys = new HashMap<String, String>();
 
         execute(new OpenPreparedStatementInConnection(query(queryName), executionContext) {
             @Override
             protected PreparedStatement createPreparedStatement(Query query, Connection c,
                                                                 QueryExecutionContext context) throws SQLException{
                 if(generatedKeyColumns==null || generatedKeyColumns.length==0){
                     return super.createPreparedStatement(query, c, context);
                 }else{
                     return query.preparedStatementForContext(context, c, generatedKeyColumns);
                 }
             }
 
             @Override
             public void doWithPreparedStatement(PreparedStatement ps) throws SQLException {
                 ps.executeUpdate();
 
                 // Retrieving generated data
                 ResultSet genKeysResultSet = ps.getGeneratedKeys();
                 if(genKeysResultSet.next()){
                     for(int i=0; i<generatedKeyColumns.length; i++){
                         genKeys.put(generatedKeyColumns[i], genKeysResultSet.getString(i+1));
                     }
                 }
             }
         });
 
         return genKeys;
     }
 
     public int update(final String queryName, final QueryExecutionContext executionContext){
         return executeUpdate(queryName, executionContext);
     }
 
     public int delete(final String queryName, final QueryExecutionContext executionContext){
         return executeUpdate(queryName, executionContext);
     }
 }
