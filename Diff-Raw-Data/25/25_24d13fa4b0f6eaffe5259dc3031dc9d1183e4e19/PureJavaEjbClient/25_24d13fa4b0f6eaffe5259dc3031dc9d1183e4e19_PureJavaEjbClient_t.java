 package ru.ttk.pure_java;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import java.util.Hashtable;
 
 /**
  *
  */
 public class PureJavaEjbClient {
 
     private static String[] JNDINAME = {
             "java:jboss/exported/building/building-ejb-0.5-SNAPSHOT/SimpleRemoteEJB!ru.ttk.test.interfaces.ISimpleRemoteEJB"
     };
 
     /*
     java:global/building/building-ejb-0.5-SNAPSHOT/SimpleRemoteEJB!ru.ttk.test.interfaces.ISimpleRemoteEJB
     java:app/building-ejb-0.5-SNAPSHOT/SimpleRemoteEJB!ru.ttk.test.interfaces.ISimpleRemoteEJB
     java:module/SimpleRemoteEJB!ru.ttk.test.interfaces.ISimpleRemoteEJB
     java:jboss/exported/building/building-ejb-0.5-SNAPSHOT/SimpleRemoteEJB!ru.ttk.test.interfaces.ISimpleRemoteEJB
     java:global/building/building-ejb-0.5-SNAPSHOT/SimpleRemoteEJB
     java:app/building-ejb-0.5-SNAPSHOT/SimpleRemoteEJB
     java:module/SimpleRemoteEJB
     */
 
     private Hashtable jndiProperties;
 
     public PureJavaEjbClient() {
         // setup 'base' jndi properties - no jboss-ejb-client.properties being picked up from classpath!
         jndiProperties = new Hashtable();
         jndiProperties.put("java.naming.factory.initial","org.jboss.naming.remote.client.InitialContextFactory");
         jndiProperties.put(InitialContext.PROVIDER_URL, "remote://localhost:4447");
         jndiProperties.put("jboss.naming.client.ejb.context", true);
 
         // needed for remote access - remember to run add-user.bat
        jndiProperties.put(Context.SECURITY_PRINCIPAL, "user.name@transtk.ru");
        jndiProperties.put(Context.SECURITY_CREDENTIALS, "password");
     }
 
     public void doLookups() {
         // the 'exported' namespace
         for (int i = 0; i < JNDINAME.length; i++) {
             lookup(JNDINAME[i]);
         }
         // This is an important property to set if you want to do EJB invocations via the remote-naming project
         jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
 
         // now with the ejb
         for (int i = 0; i < JNDINAME.length; i++) {
             lookup(JNDINAME[i]);
         }
     }
 
     private void lookup(String name) {
         System.out.println("Lookup name="+name);
 
         Context ctx = null;
         try {
             ctx = new InitialContext(jndiProperties);
             Object ref = ctx.lookup(name);
             System.out.println("...Successful");
         } catch (NamingException e) {
             System.out.println("...Failed");
             //System.out.println(e.getMessage());
             e.printStackTrace();
         } finally {
             if (ctx != null) {
                 try {
                     ctx.close();
                 } catch (NamingException e) {}
             }
         }
     }
 
     public static void main(String[] argv){
         PureJavaEjbClient client = new PureJavaEjbClient();
         client.doLookups();
 
         System.out.println("Done!");
     }
 }
