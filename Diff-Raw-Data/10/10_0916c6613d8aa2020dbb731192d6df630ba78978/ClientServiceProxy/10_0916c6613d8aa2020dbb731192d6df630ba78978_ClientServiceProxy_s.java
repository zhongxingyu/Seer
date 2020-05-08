 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.phoundation.blackboard9.client;
 
 import java.lang.reflect.Method;
 import java.net.SocketTimeoutException;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import com.phoundation.blackboard9.client.exception.BlackboardTimeOutException;
 
 /**
  *
  * @author chris
  */
 public class ClientServiceProxy implements java.lang.reflect.InvocationHandler {
 
     private static Log log = LogFactory.getLog(ClientServiceProxy.class);
     private Class objClass;
     private Object obj;
     private ClientWrapper bbClient;
 
     public static Object newInstance(
             Class objClass,
             Object obj,
             ClientWrapper client) {
         log.debug("CREATING NEW PROXY FOR : " + objClass.getCanonicalName());
         return java.lang.reflect.Proxy.newProxyInstance(
                 objClass.getClassLoader(),
                 new Class[]{objClass},
                 new ClientServiceProxy(objClass, obj, client));
     }
 
     private ClientServiceProxy(
             Class objClass,
             Object obj,
             ClientWrapper client) {
         this.objClass = objClass;
         this.obj = obj;
         this.bbClient = client;
     }
 
     @Override
     public Object invoke(Object proxy, Method m, Object[] args)
             throws Throwable {
         log.debug("INVOKING : " + m.getName() + " : ON : " + objClass.getCanonicalName());
         
 
         Object result = null;
         int attempt = 0;
         int maxAttempts = 3;
         Throwable thrown = null;
 
         boolean success = true;
 
         do {
             attempt++;
             if (SessionHandler.getPassword().equals("nosession")) {
                 bbClient.loginTool();
             }
             log.debug("SESSION : " + SessionHandler.getPassword());
             try {
                 result = m.invoke(obj, args);
             } catch (Throwable e) {
                 log.error(e.getMessage() +" :: ATTEMPT - "+attempt);
                 thrown = new Exception(objClass.getCanonicalName() + "." + m.getName() + " : failed", e);
 
                 while (e != null) {
                    if (e instanceof SOAPFaultException) {
                        handleSOAPFault((SOAPFaultException) e);
                         break;
                     }
 
                     if (e instanceof SocketTimeoutException) {
                         throw new BlackboardTimeOutException((SocketTimeoutException) e);
                     }
                     e = e.getCause();
                 }
                 success = false;
             }
             
         } while (success != true && attempt <= maxAttempts);
 
         if (result == null) {
             throw new RuntimeException(thrown);
         } else {
             return result;
         }
     }
     private String[] initSessionCodes = new String[]{"WSFW001", "WSFW004", "WSFW010", "WSFW012"};
     private String[] throwExceptionCodes = new String[]{
         "Context.WS003", "WSFW002", "WSFW003", "WSFW005", "WSFW006", "WSFW007",
         "WSFW008", "WSFW009", "WSFW011", "WSFW013", "WSFW014", "WSFW015",};
     Pattern faultPattern = Pattern.compile("\\[(.*)\\](.*)");
 
    private void handleSOAPFault(SOAPFaultException ex) throws Exception {
         SOAPFault fault = ex.getFault();
         String faultString = fault.getFaultString();
         log.error(faultString, ex);
         Matcher faultMatcher = faultPattern.matcher(faultString);
 
         if (faultMatcher.matches() && Arrays.binarySearch(throwExceptionCodes, faultMatcher.group(1)) < 0) {
             log.debug("FAULT = "+faultMatcher.group(0)+" -- Resetting session");
             SessionHandler.reset();
         } else {
             throw new RuntimeException(ex);
         }
 
     }
 }
