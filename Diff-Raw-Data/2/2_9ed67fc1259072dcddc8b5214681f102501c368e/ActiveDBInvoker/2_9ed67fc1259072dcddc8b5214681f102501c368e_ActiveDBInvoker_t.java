 /*
  * ActiveDBExecuter.java
  *
  * Created on August 30, 2013, 12:43 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.osiris3.activedb;
 
 import com.rameses.osiris3.persistence.EntityManager;
 import com.rameses.osiris3.sql.SqlExecutor;
 import com.rameses.osiris3.sql.SqlQuery;
 import java.util.Map;
 
 /**
  *
  * @author Elmo
  */
 public class ActiveDBInvoker {
     
     private String schemaName;
     private EntityManager em;
     
     public ActiveDBInvoker(String schemaName, EntityManager em) {
         this.em = em;
         this.schemaName = schemaName;
     }
     
     public Object invokeMethod(String methodName, Object[] args) {
         try {
             String n = schemaName;
             String subSchema = "";
             
             Map m = null;
             if( args!=null ) {
                 if( args.length > 0 ) {
                     m = (Map)args[0];        
                 }
                 //used for subschema for entity managers. 
                 if(args.length>1) {
                     subSchema = ":"+args[1];
                 }
             }
 
             if(methodName.equals("create")) {
                 return em.create(n+subSchema, m);
             }
             else if(methodName.equals("update")) {
                 return em.update(n+subSchema, m);
             }
             else if(methodName.equals("read")) {
                 return em.update(n+subSchema, m);
             }
             else if(methodName.equals("delete")) {
                 em.delete(n+subSchema, m);
                 return null;
             }
             else if(methodName.startsWith("get") || methodName.startsWith("findAll")) {
                 SqlQuery sq = em.getSqlContext().createNamedQuery( n+":"+methodName );    
                 if(m!=null) {
                     sq.setVars(m).setParameters(m);
                     if(m.containsKey("_start")) {
                         int s = Integer.parseInt(m.get("_start")+"");
                         sq.setFirstResult( s );
                     }
                     if(m.containsKey("_limit")) {
                         int l = Integer.parseInt(m.get("_limit")+"");
                         sq.setMaxResults( l );
                     }
                 }
                 return sq.getResultList();
             }
            else if(methodName.startsWith("find")) {
                 SqlQuery sq = em.getSqlContext().createNamedQuery( n+":"+methodName );    
                 if(m!=null) {
                     sq.setVars(m).setParameters(m);
                 }
                 return sq.getSingleResult();
             }
             else {
                 SqlExecutor sqe = em.getSqlContext().createNamedExecutor( n+":"+methodName );    
                 if(m!=null) {
                     sqe.setVars(m).setParameters(m);
                 }
                 return sqe.execute();
             }
         } catch(Exception ex) {
             throw new RuntimeException(ex.getMessage(), ex);
         }
     }
     
 }
