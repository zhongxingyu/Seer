 package org.unbunt.ella.lang.sql;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.unbunt.ella.compiler.ParserHelper;
 import org.unbunt.ella.compiler.support.RawParamedSQL;
 import org.unbunt.ella.compiler.support.RawSQL;
 import org.unbunt.ella.engine.context.Context;
 import org.unbunt.ella.engine.corelang.*;
 import static org.unbunt.ella.engine.corelang.ObjUtils.ensureType;
 import org.unbunt.ella.exception.*;
 import org.unbunt.ella.lang.*;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Date; // NOTE: it is essential to have this import before the java.sql.* import
 import java.sql.*;
 import static java.lang.String.format;
 
 /**
  * Represents an EllaScript object wrapping an SQL statement.
  */
 public class Stmt extends AbstractObj {
     protected static final Log logger = LogFactory.getLog(Stmt.class);
     protected static final boolean trace = logger.isTraceEnabled();
 
     protected static final int OBJECT_ID = ProtoRegistry.generateObjectID();
 
     protected RawSQL rawStatement;
     protected RawParamedSQL rawParamedStatement = null;
     protected Statement statement = null;
     protected PreparedStatement preparedStatement = null;
     protected Connection connection;
 
     protected boolean paramed = false;
     protected Obj[] params = null;
     protected Map<String, Obj> namedParams = null;
 
     protected boolean keepResources;
 
     /**
      * Indicates if this Stmt object's resources are to be managed by an external entity.
      */
     protected boolean managedExternal;
 
     protected boolean initialized = false;
 
     /**
      * Creates a new Stmt.
      *
      * @param rawStatement the SQL statement to wrap.
      * @param connection the connection associated with the SQL statement.
      * @param managedExternal a flag indicating if this object's resources are to be managed from an external entity.
      */
     public Stmt(RawSQL rawStatement, Connection connection, boolean managedExternal) {
         this.rawStatement = rawStatement;
         this.connection = connection;
         this.managedExternal = managedExternal;
         this.keepResources = managedExternal;
     }
 
     protected void enterManagedMode() {
         if (managedExternal) {
             return;
         }
         keepResources = true;
     }
 
     protected void leaveManagedMode() throws SQLException {
         if (managedExternal) {
             return;
         }
         keepResources = false;
         close();
     }
 
     /**
      * Tells this object that any resources aquired and held open can now be released.
      *
      * @throws SQLException if a database error occurs.
      */
     public void leaveExternalManagedMode() throws SQLException {
         managedExternal = false;
         keepResources = false;
         close();
     }
 
     protected boolean execute() throws SQLException {
         return execute(false);
     }
 
     protected boolean execute(boolean scrollable) throws SQLException {
         if (paramed) {
             initPrepared(scrollable);
             addParams();
             logger.info(getParamedQuery());
             boolean isResult;
             Date t1 = new Date();
             isResult = preparedStatement.execute();
             Date t2 = new Date();
             logger.info(format("query took %.3f seconds", 1d * (t2.getTime() - t1.getTime()) / 1000));
             return isResult;
         }
         else {
             init(scrollable);
             logger.info(rawStatement.getStatement());
             boolean isResult;
             Date t1 = new Date();
             isResult = statement.execute(rawStatement.getStatement());
             Date t2 = new Date();
             logger.info(format("query took %.3f seconds", 1d * (t2.getTime() - t1.getTime()) / 1000));
             return isResult;
         }
     }
 
     protected ResultSet query() throws SQLException {
         if (paramed) {
             initPrepared(false);
             addParams();
             return preparedStatement.executeQuery();
         }
         else {
             init(false);
             return statement.executeQuery(rawStatement.getStatement());
         }
     }
 
     protected ResultSet retrieveKeys() throws SQLException {
         if (paramed) {
             initPreparedForKeys();
             addParams();
             preparedStatement.executeUpdate();
             return preparedStatement.getGeneratedKeys();
         }
         else {
             init(false);
             statement.executeUpdate(rawStatement.getStatement(), Statement.RETURN_GENERATED_KEYS);
             return statement.getGeneratedKeys();
         }
     }
 
     protected void addBatch(Obj[] params) throws SQLException {
         setParams(params);
         addParams();
         preparedStatement.addBatch();
     }
 
     protected void addNamedBatch(Obj namedParams) throws SQLException {
         setNamedParams(namedParams);
         addParams();
         preparedStatement.addBatch();
     }
 
     protected void execBatch() throws SQLException {
         preparedStatement.executeBatch();
     }
 
     protected void init(boolean scrollable) throws SQLException {
         if (initialized) {
             if (keepResources) {
                 return;
             }
             else {
                 throw new EllaRuntimeException("Illegal state");
             }
         }
 
         if (scrollable) {
             // create statement downgrading result set features as nessassary
             try {
                 statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             } catch (SQLFeatureNotSupportedException e) {
                 try {
                     statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                 } catch (SQLFeatureNotSupportedException e2) {
                     statement = connection.createStatement();
                 }
             }
         }
         else {
             statement = connection.createStatement();
         }
 
         initialized = true;
     }
 
     protected void initPrepared(boolean scrollable) throws SQLException {
         if (initialized) {
             if (keepResources) {
                 return;
             }
             else {
                 throw new EllaRuntimeException("Illegal state");
             }
         }
 
         String sql = getParamedQuery();
         if (scrollable) {
             try {
                 preparedStatement = connection.prepareStatement(sql,
                                                                 ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                 ResultSet.CONCUR_UPDATABLE);
             } catch (SQLFeatureNotSupportedException e) {
                 try {
                     preparedStatement = connection.prepareStatement(sql,
                                                                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                                     ResultSet.CONCUR_READ_ONLY);
                 } catch (SQLFeatureNotSupportedException e2) {
                     preparedStatement = connection.prepareStatement(sql);
                 }
             }
         }
         else {
             preparedStatement = connection.prepareStatement(sql);
         }
 
         initialized = true;
         paramed = true;
     }
 
     protected void initPreparedForKeys() throws SQLException {
         if (initialized) {
             if (keepResources) {
                 return;
             }
             else {
                 throw new EllaRuntimeException("Illegal state");
             }
         }
 
         preparedStatement = connection.prepareStatement(getParamedQuery(), Statement.RETURN_GENERATED_KEYS);
 
         initialized = true;
     }
 
     protected String getParamedQuery() {
         return rawParamedStatement != null ? rawParamedStatement.getStatement() : rawStatement.getStatement();
     }
 
     protected void addParams() throws SQLException {
         if (namedParams != null) {
             Map<String, List<Integer>> paramIndices = rawParamedStatement.getParameters();
             for (Map.Entry<String, Obj> entry : namedParams.entrySet()) {
                 List<Integer> indices = paramIndices.get(entry.getKey());
                 for (Integer index : indices) {
                     setParam(index, entry.getValue());
                 }
             }
         }
         else {
             int nparams = params.length;
             for (int i = 0; i < nparams; i++) {
                 setParam(i + 1, params[i]);
             }
         }
     }
 
     /**
      * Sets the given parameter on the current prepared statement.
      * Make use of type hint on null values.
      *
      * TODO: The mapping needs some work. Also we will possibly need a way of letting the user specify the type
      *       explicitly.
      *
      * @param idx index of the parameter to set
      * @param wrappedParam value of the parameter to set in it's wrapped form, will be unwrapped to it's host object
      * @throws SQLException possible exception thrown by the underlying statement object
      */
     protected void setParam(int idx, Obj wrappedParam) throws SQLException {
         Object param = wrappedParam.toJavaObject();
         Class<?> typeHint;
         if (param != null
             || !(wrappedParam instanceof Null)
             || (typeHint = ((Null) wrappedParam).getTypeHint()) == null) {
             if (param instanceof java.util.Date) {
                 // the oracle jdbc driver throws an exception when passing a java.util.Date object to setObject()...
                 // so we take care of this here.
                 preparedStatement.setTimestamp(idx, new Timestamp(((java.util.Date) param).getTime()));
             }
             else {
                 preparedStatement.setObject(idx, param);
             }
             return;
         }
 
         int type;
         if (typeHint.isAssignableFrom(Byte.class))
             type = Types.TINYINT;
         else if (typeHint.isAssignableFrom(Short.class))
             type = Types.SMALLINT;
         else if (typeHint.isAssignableFrom(Integer.class))
             type = Types.INTEGER;
         else if (typeHint.isAssignableFrom(Long.class))
             type = Types.BIGINT;
         else if (typeHint.isAssignableFrom(Float.class))
             type = Types.REAL;
         else if (typeHint.isAssignableFrom(Double.class))
             type = Types.DOUBLE;
         else if (typeHint.isAssignableFrom(BigDecimal.class))
             type = Types.DECIMAL;
         else if (typeHint.isAssignableFrom(Boolean.class))
             type = Types.BIT;
         else if (typeHint.isAssignableFrom(String.class))
             type = Types.VARCHAR;
         else if (typeHint.isAssignableFrom(Date.class))
             type = Types.TIMESTAMP;
         else if (typeHint.isAssignableFrom(Time.class))
             type = Types.TIME;
         else if (typeHint.isAssignableFrom(Timestamp.class))
             type = Types.TIMESTAMP;
         else {
             preparedStatement.setObject(idx, param);
             return;
         }
 
         preparedStatement.setNull(idx, type);
     }
 
     protected void setRawParamedStmt(RawParamedSQL stmt) {
         rawParamedStatement = stmt;
     }
 
     protected void setParams(Obj[] params) {
         if (initialized) {
             if (!paramed) {
                 throw new EllaRuntimeException("Illegal state");
             }
             else if (namedParams != null) {
                 throw new EllaRuntimeException("Illegal state: Statement requires named parameters.");
             }
         }
         this.params = params;
         paramed = true;
     }
 
     protected void setNamedParams(Obj namedParams) {
         if (initialized) {
             if (!paramed) {
                 throw new EllaRuntimeException("Illegal state");
             }
             else if (params != null) {
                 throw new EllaRuntimeException("Illegal state: Statement requires positional parameters.");
             }
         }
         Map<String, List<Integer>> knownParams = rawParamedStatement.getParameters();
         Map<String, Obj> result = new HashMap<String, Obj>();
         for (Map.Entry<Obj, Obj> entry : namedParams.getSlots().entrySet()) {
             Str param = ensureType(Str.class, entry.getKey());
             String paramName = param.value;
             if (!knownParams.containsKey(paramName)) {
                 throw new EllaRuntimeException("Invalid named parameter: " + paramName);
             }
             result.put(paramName, entry.getValue());
         }
         this.namedParams = result;
         paramed = true;
     }
 
     protected void parseParams() {
         if (rawParamedStatement != null) {
             if (keepResources) {
                 return;
             }
             else {
                 throw new EllaRuntimeException("Illegal state");
             }
         }
 
         RawParamedSQL paramedStmt;
         try {
             paramedStmt = ParserHelper.parseParamedSQLLiteral(rawStatement);
         } catch (GenericParseException e) {
             throw new EllaRuntimeException("Failed to parse SQL statement: " +
                                                 rawStatement.getStatement(), e);
         }
         setRawParamedStmt(paramedStmt);
     }
 
     protected void close() throws SQLException {
         if (keepResources) {
             return;
         }
 
         try {
             if (statement != null) {
                 statement.close();
             }
 
             if (preparedStatement != null) {
                 preparedStatement.close();
             }
         } finally {
             reset();
         }
     }
 
     protected void reset() {
         statement = null;
         preparedStatement = null;
         rawParamedStatement = null;
         paramed = false;
         params = null;
         namedParams = null;
         keepResources = false;
         initialized = false;
     }
 
     protected Statement getStatement() {
         return statement != null ? statement : preparedStatement;
     }
 
     protected void setConnection(Connection connection) {
         if (this.initialized) {
             throw new EllaRuntimeException("Cannot set connection. Statement is already initialized.");
         }
         this.connection = connection;
     }
 
     @Override
     public int getObjectID() {
         return OBJECT_ID;
     }
 
     /**
      * Registers this EllaScript object within the given execution context.
      *
      * @param ctx the execution context to register this object in.
      */
     public static void regiserInContext(Context ctx) {
         StmtProto.registerInContext(ctx);
         ctx.registerProto(OBJECT_ID, StmtProto.OBJECT_ID);
     }
 
     @Override
     public Object toJavaObject() {
         return statement != null ? statement : preparedStatement != null ? preparedStatement : null;
     }
 
     /**
      * Represents the implicit parent object for Stmt objects.
      */
     public static class StmtProto extends AbstractObj {
         public static final int OBJECT_ID = ProtoRegistry.generateObjectID();
 
         protected static final NativeCall nativeDo = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 try {
                    boolean hasResult = thiz.execute(true);
                     if (hasResult) {
                         ResultSet rs = thiz.getStatement().getResultSet(); // NOTE: rs closed implicitly with statement
                         engine.notifyResultSet(rs);
                     }
                     else {
                         int updateCount = thiz.getStatement().getUpdateCount();
                         engine.notifyUpdateCount(updateCount);
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Query failed: " + e.getMessage(), e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeExec = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 try {
                     thiz.execute();
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Query failed: " + e.getMessage(), e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeEach = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Obj closure = args[0];
                 Null _null = engine.getObjNull();
                 try {
                     ResultSet rs = thiz.query();
                     ResSet resSet = new ResSet(rs);
                     while (rs.next()) {
                         try {
                             engine.invokeInLoop(closure, _null, resSet);
                         } catch (LoopContinueException e) {
                             continue;
                         } catch (LoopBreakException e) {
                             break;
                         }
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Query failed: " + e.getMessage(), e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
 
                 return null;
             }
         };
 
         protected static final NativeCall nativeWithResult = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Obj closure = args[0];
                 Null _null = engine.getObjNull();
                 try {
                     ResultSet rs = thiz.query();
                     ResSet resSet = new ResSet(rs);
                     engine.invoke(closure, _null, resSet);
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Query failed: " + e.getMessage(), e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
 
                 return null;
             }
         };
 
         // NOTE: This has to be improved. Result should be wrapped into special object.
         protected static final NativeCall nativeFirst = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Null _null = engine.getObjNull();
                 Context ctx = engine.getContext();
                 try {
                     ResultSet rs = thiz.query();
                     if (rs.next()) {
                         ResultSetMetaData meta = rs.getMetaData();
                         int ncols = meta.getColumnCount();
                         Obj result = new PlainObj();
                         for (int i = 1; i <= ncols; i++) {
                             result.setSlot(ctx,
                                            // XXX: Should name really be intern()ed?
                                            new Str(meta.getColumnLabel(i)).intern(),
                                            NativeWrapper.wrap(ctx, rs.getObject(i)));
                         }
                         return result;
                     }
                     else {
                         return _null;
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Query failed: " + e.getMessage(), e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
             }
         };
 
         protected static final NativeCall nativeEachKey = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Obj closure = args[0];
                 Obj _null = engine.getObjNull();
 
                 try {
                     ResultSet rs = thiz.retrieveKeys();
                     ResSet resSet = new ResSet(rs);
                     while (rs.next()) {
                         try {
                             engine.invokeInLoop(closure, _null, resSet);
                         } catch (LoopContinueException e) {
                             continue;
                         } catch (LoopBreakException e) {
                             break;
                         }
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Query failed: " + e.getMessage(), e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
 
                 return null;
             }
         };
 
         protected static final NativeCall nativeKey = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
 
                 try {
                     ResultSet rs = thiz.retrieveKeys();
                     if (rs.next()) {
                         return NativeWrapper.wrap(engine.getContext(), rs.getObject(1));
                     }
                     else {
                         return engine.getObjNull();
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Query failed: " + e.getMessage(), e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
             }
         };
 
         protected static final NativeCall nativeWith = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 thiz.setParams(args);
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeWithParams = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Lst params = ensureType(Lst.class, args[0]);
                 List<Obj> paramsList = params.getValue();
                 thiz.setParams(paramsList.toArray(new Obj[paramsList.size()]));
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeWithNamed = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Obj params = args[0];
 
                 thiz.parseParams();
                 thiz.setNamedParams(params);
 
                 return thiz;
             }
         };
 
         // TODO: How to handle update counts???
         protected static final NativeCall nativeBatch = new NativeBatchCall() {
             public Obj batchCall(Engine engine, Obj context, Obj closure, int batchSize)
                     throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
 
                 try {
                     thiz.initPrepared(false);
                     ParamBatch batch = new ParamBatch(thiz, batchSize);
                     engine.invoke(closure, engine.getObjNull(), batch);
                     engine.invokeSlot(batch, Str.SYM_finish);
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Batch execution failed: " + e, e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
 
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeBatchNamed = new NativeBatchCall() {
             public Obj batchCall(Engine engine, Obj context, Obj closure, int batchSize)
                     throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
 
                 try {
                     thiz.parseParams();
                     thiz.initPrepared(false);
                     NamedParamBatch batch = new NamedParamBatch(thiz, batchSize);
                     engine.invoke(closure, engine.getObjNull(), batch);
                     batch.finish();
                 } catch (SQLException e) {
                     throw new EllaRuntimeException("Batch execution failed " + e, e);
                 } finally {
                     try {
                         thiz.close();
                     } catch (SQLException ignored) {
                     }
                 }
 
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeWithPrepared = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Obj closure = args[0];
 
                 try {
                     thiz.enterManagedMode();
                     engine.invoke(closure, engine.getObjNull(), thiz);
                 } finally {
                     try {
                         thiz.leaveManagedMode();
                     } catch (SQLException ignored) {
                     }
                 }
 
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeAssociateConnection = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) {
                 Stmt thiz = ensureType(Stmt.class, context);
                 Conn conn = ensureType(Conn.class, args[0]);
                 thiz.setConnection(conn.getConnection());
                 return thiz;
             }
         };
 
         protected static final NativeCall nativeGetQueryString = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 Stmt thiz = ensureType(Stmt.class, context);
                 return new Str(thiz.rawStatement.getStatement());
             }
         };
 
         protected StmtProto() {
             slots.put(Str.SYM_do, nativeDo);
             slots.put(Str.SYM_exec, nativeExec);
             slots.put(Str.SYM_each, nativeEach);
             slots.put(Str.SYM_first, nativeFirst);
             slots.put(Str.SYM_eachKey, nativeEachKey);
             slots.put(Str.SYM_key, nativeKey);
             slots.put(Str.SYM_with, nativeWith);
             slots.put(Str.SYM_withParams, nativeWithParams);
             slots.put(Str.SYM_withNamed, nativeWithNamed);
             slots.put(Str.SYM_batch, nativeBatch);
             slots.put(Str.SYM_batchNamed, nativeBatchNamed);
             slots.put(Str.SYM_withPrepared, nativeWithPrepared);
             slots.put(Str.SYM_withResult, nativeWithResult);
             slots.put(Str.SYM_associateConnection, nativeAssociateConnection);
             slots.put(Str.SYM_getQueryString, nativeGetQueryString);
         }
 
         @Override
         public int getObjectID() {
             return OBJECT_ID;
         }
 
         /**
          * Registers this EllaScript object within the given execution context.
          *
          * @param ctx the execution context to register this object in.
          */
         public static void registerInContext(Context ctx) {
             Base.registerInContext(ctx);
             ctx.registerProto(OBJECT_ID, Base.OBJECT_ID);
             ctx.registerObject(new StmtProto());
         }
     }
 
     protected static class ParamBatch extends AbstractObj {
         protected Stmt stmt;
         protected int batchSize;
         protected int currentBatchSize = 0;
         protected int executedStatements = 0;
 
         protected boolean addBatch(Obj[] params) throws SQLException {
             this.stmt.addBatch(params);
             if (++this.currentBatchSize % this.batchSize == 0) {
                 this.stmt.execBatch();
                 this.executedStatements += this.currentBatchSize;
                 this.currentBatchSize = 0;
                 return true;
             }
             return false;
         }
 
         protected boolean finish() throws SQLException {
             if (currentBatchSize == 0) {
                 return false;
             }
 
             stmt.execBatch();
             this.executedStatements += this.currentBatchSize;
             this.currentBatchSize = 0;
             return true;
         }
 
         protected static NativeCall nativeAdd = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 ParamBatch thiz = ensureType(ParamBatch.class, context);
                 try {
                     if (thiz.addBatch(args)) {
                         Obj execCallback = thiz.getSlot(engine.getContext(), Str.SYM_onAfterExecute);
                         if (execCallback != null) {
                             engine.invoke(execCallback, engine.getObjNull(), new NNum(thiz.executedStatements));
                         }
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException(e);
                 }
                 return thiz;
             }
         };
 
         protected static NativeCall nativeAddParams = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) {
                 ParamBatch thiz = ensureType(ParamBatch.class, context);
                 Lst params = ensureType(Lst.class, args[0]);
                 List<Obj> paramsList = params.getValue();
                 try {
                     if (thiz.addBatch(paramsList.toArray(new Obj[paramsList.size()]))) {
                         Obj execCallback = thiz.getSlot(engine.getContext(), Str.SYM_onAfterExecute);
                         if (execCallback != null) {
                             engine.invoke(execCallback, engine.getObjNull(), new NNum(thiz.executedStatements));
                         }
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException(e);
                 }
                 return thiz;
             }
         };
 
         protected static NativeCall nativeFinish = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 ParamBatch thiz = ensureType(ParamBatch.class, context);
                 try {
                     if (thiz.finish()) {
                         Obj execCallback = thiz.getSlot(engine.getContext(), Str.SYM_onAfterExecute);
                         if (execCallback != null) {
                             engine.invoke(execCallback, engine.getObjNull(), new NNum(thiz.executedStatements));
                         }
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException(e);
                 }
                 return thiz;
             }
         };
 
         public ParamBatch(Stmt stmt, int batchSize) {
             this.stmt = stmt;
             this.batchSize = batchSize;
             slots.put(Str.SYM_add, nativeAdd);
             slots.put(Str.SYM_addParams, nativeAddParams);
             slots.put(Str.SYM_finish, nativeFinish);
         }
     }
 
     protected static class NamedParamBatch extends ParamBatch {
         protected static NativeCall nativeAdd = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj... args) throws ClosureTerminatedException {
                 ParamBatch thiz = ensureType(ParamBatch.class, context);
                 try {
                     thiz.stmt.addNamedBatch(args[0]);
                     if (++thiz.currentBatchSize % thiz.batchSize == 0) {
                         thiz.stmt.execBatch();
                         thiz.currentBatchSize = 0;
                     }
                 } catch (SQLException e) {
                     throw new EllaRuntimeException(e);
                 }
                 return thiz;
             }
         };
 
         public NamedParamBatch(Stmt stmt, int batchSize) {
             super(stmt, batchSize);
             slots.put(Str.SYM_add, nativeAdd);
             slots.put(Str.SYM_addParams, nativeAdd);
         }
     }
 
 }
