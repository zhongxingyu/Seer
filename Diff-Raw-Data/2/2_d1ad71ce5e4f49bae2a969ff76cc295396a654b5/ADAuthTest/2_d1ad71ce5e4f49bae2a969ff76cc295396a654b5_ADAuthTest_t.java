 import javax.naming.*;
 import javax.naming.directory.*;
 import java.util.*;
 import java.io.*;
 
  public class ADAuthTest {
  
     public static void main(String [] args) {
 
             Hashtable env = new Hashtable(11);
             env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
             env.put(Context.PROVIDER_URL, "ldaps://" + "ymservices" + ":636");
             env.put(Context.SECURITY_AUTHENTICATION, "simple");
             env.put(Context.SECURITY_PROTOCOL, "ssl");
             env.put(Context.SECURITY_PRINCIPAL, "ymservices" + "\\" + "munroeb");
            env.put(Context.SECURITY_CREDENTIALS, "");
 
             try {
                 DirContext ctx = new InitialDirContext(env);
             } catch (NamingException e) {
                 System.out.println(e);
             }
         }
     }
 
