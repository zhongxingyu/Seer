 /**
  * JDBCVelocityContext.java
  *
  * @author Created by Omnicore CodeGuide
  */
 
 package edu.sc.seis.sod.database.waveform;
 
 import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
 import edu.sc.seis.sod.CookieJarResult;
 import edu.sc.seis.sod.EventChannelPair;
 import java.io.Serializable;
 import java.sql.SQLException;
 import org.apache.velocity.context.AbstractContext;
 import org.apache.velocity.context.Context;
 
 public class JDBCVelocityContext extends AbstractContext {
 
     public JDBCVelocityContext(EventChannelPair ecp, JDBCEventChannelCookieJar jdbcCookieJar, Context context) {
         super(context);
         this.pairId = ecp.getPairId();
         this.jdbcCookieJar = jdbcCookieJar;
     }
 
     public Object internalGet(String name) {
         try {
             CookieJarResult cookie = jdbcCookieJar.get(pairId, name);
             if (cookie == null) { return null; }
             if (cookie.getValueString() != null) {return cookie.getValueString();}
             if (cookie.getValueObject() != null) {return cookie.getValueObject();}
             return new Double(cookie.getValueDouble());
         } catch (SQLException e) {
             GlobalExceptionHandler.handle("Problem getting value for name="+name, e);
             return null;
         }
     }
 
     /** The interface requires Object value, but we really want it to be
      * limited to String Double and Serializable. */
     public Object internalPut(String name, Object value) {
         try {
             if (value instanceof String) {
                 jdbcCookieJar.put(pairId, name, (String)value);
             } else if (value instanceof Double) {
                 jdbcCookieJar.put(pairId, name, ((Double)value).doubleValue());
             }  else if (value instanceof Serializable) {
                 jdbcCookieJar.put(pairId, name, (Serializable)value);
             } else {
                 throw new IllegalArgumentException("value must be a String or a Double or a Serializable: "+value.getClass().getName());
             }
         } catch (SQLException e) {
            GlobalExceptionHandler.handle("Problem putting value for name="+name, e);
         }
         return null;
     }
 
 
     public boolean internalContainsKey(Object name) {
         try {
             return jdbcCookieJar.containsKey(pairId, (String)name);
         } catch (SQLException e) {
             GlobalExceptionHandler.handle("Problem checking for pair="+pairId+" key="+name, e);
             return false;
         }
     }
 
 
     public Object[] internalGetKeys() {
         try {
             return jdbcCookieJar.getKeys(pairId);
         } catch (SQLException e) {
             GlobalExceptionHandler.handle("Problem getting keys for pair="+pairId, e);
             return new String[0];
         }
     }
 
 
     public Object internalRemove(Object name) {
         try {
             return jdbcCookieJar.remove(pairId, (String)name);
         } catch (SQLException e) {
             GlobalExceptionHandler.handle("Problem checking for pair="+pairId+" key="+name, e);
             return null;
         }
     }
 
     int pairId;
 
     JDBCEventChannelCookieJar jdbcCookieJar;
 
 }
 
