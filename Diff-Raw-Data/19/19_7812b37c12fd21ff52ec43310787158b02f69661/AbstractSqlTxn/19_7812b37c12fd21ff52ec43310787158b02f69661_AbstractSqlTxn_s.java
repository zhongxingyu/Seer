 /*
  * SQLQuery.java
  *
  * Created on July 21, 2010, 8:04 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.sql;
 
 import com.rameses.util.ExprUtil;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Base class for SqlQuery and SqlExecutor
  */
 public abstract class AbstractSqlTxn {
     
     protected SqlContext sqlContext;
     protected String statement;
     protected List<String> parameterNames = new ArrayList();
     protected List parameterValues;
     protected Connection connection;
     protected String origStatement;
     protected List origParamNames;
     protected ParameterHandler parameterHandler;
     
     protected Map vars;
     
     /***
      * By default, DataSource is passed by the SqlManager
      * however connection can be manually overridden by setting
      * setConnection.
      */
     AbstractSqlTxn(SqlContext sm, String statement, List paramNames) {
         this.origStatement = statement;
         this.origParamNames = paramNames;
         this.sqlContext = sm;
         clear();
     }
     
     //this resets the statements
     public final void clear() {
         this.statement = origStatement;
         parameterNames.clear();
         this.parameterNames.clear();
         if(origParamNames!=null) {
             for(Object o : origParamNames) {
                 this.parameterNames.add((String)o);
             }
         }
         allocate();
     }
     
      /** 
      * allocates the size and clears new values.
      */
     protected final void allocate() {
         this.parameterValues = new ArrayList();
         for(Object o : parameterNames) {
             this.parameterValues.add(null); 
         }
     }
     
     /** 
      * expands the values size but does not erase existing values
      */
    protected final void reallocate() {
        int diff = parameterNames.size() - parameterValues.size();
         for(int i=0; i<diff;i++) {
             parameterValues.add(null);
         }
     }
     
     /**
      * this is resusable code but does not return the specific transaction
      *
      */
     protected final void _setConnection(Connection connection) {
         this.connection = connection;
     }
     
     protected final void _setParameter( int idx, Object v ) {
         if(idx<=0)
             throw new RuntimeException("Index must be 1 or higher");
         
         int diff = idx - parameterValues.size();
         for(int i=0; i<diff;i++) {
             parameterValues.add(null);
         }
         parameterValues.set(idx-1, v);
     }
     
     protected final void _setParameter(String name, Object v ) {
         int idx = -1;
         for(int i=0; i<parameterNames.size();i++) {
             if(parameterNames.get(i).equals(name)) {
                 parameterValues.set(i, v);
                 return;
             }
         }
         throw new RuntimeException("Parameter " + name + " is not found");
     }
     
   
     
     
     /***
      * This method allows adding of parameters stored as one map.
      * It requires parameter names must exist. To do this,
      * your statement must have the $P{param} named parameter.
      */
     protected final void _setParameters( Map map ) {
         if(map==null) return;
         if(parameterNames==null)
             throw new RuntimeException("Parameter Names must not be null. Please indicate $P{paramName} in your statement");
         int sz = parameterNames.size();
         for(int i=0;i<sz;i++ ) {
             parameterValues.set(  i, map.get( parameterNames.get(i)  ));
         }
     }
     
     
     protected final void _setParameters( List params ) {
         if(params==null) return;
         if(parameterNames!=null && parameterNames.size()>0){
             if(parameterNames.size()!=params.size())
                 throw new RuntimeException("Parameter count does not match");
         }
         int sz = params.size();
         for(int i=0;i<sz;i++ ) {
             parameterValues.set(  i, params.get(i) );
         }
     }
     
     /**
      * used when setting variables to a statement
      */
     protected final void _setVars( Map map ) {
         this.vars = map;
         this.statement = ExprUtil.substituteValues( this.origStatement, map );
         //reparse the statement after parsing to update the parameter names
         this.statement = SqlUtil.parseStatement(statement, parameterNames);  
        reallocate();
     }
     
     /***
      * Apply the correct statments. can be overridden
      */
     protected final String getFixedSqlStatement() {
         return statement;
     }
     
     protected final void fillParameters( PreparedStatement ps ) throws Exception {
         int sz = parameterValues.size();
         for( int i=0; i<sz;i++) {
             String name = null;
             if(parameterNames!=null && parameterNames.size()>0) {
                 name = parameterNames.get(i);
             }
             int colIndex = i+1;
             Object value = parameterValues.get(i);
             parameterHandler.setParameter(ps,colIndex,value,name);
         }
     }
     
     public final void setParameterHandler(ParameterHandler parameterHandler) {
         this.parameterHandler = parameterHandler;
     }
     
     public final String getStatement() {
         return statement;
     }
 
     public final String getOriginalStatement() {
         return origStatement;
     }
 
     public final Map getVars() {
         return vars;
     }
 
     public final List<String> getParameterNames() {
         return parameterNames;
     }
 
     public final SqlContext getSqlContext() {
         return sqlContext;
     }
 
     public final List getParameterValues() {
         return parameterValues;
     }
     
    
     
 }
