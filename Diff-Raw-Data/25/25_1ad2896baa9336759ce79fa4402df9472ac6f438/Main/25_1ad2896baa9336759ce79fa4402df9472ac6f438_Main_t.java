 package com.sonatype.shjgit.standalone;
 
 import com.sonatype.shjgit.core.ServerFactory;
 import org.apache.shiro.cache.DefaultCacheManager;
 import org.apache.shiro.mgt.DefaultSecurityManager;
 import org.apache.shiro.mgt.SecurityManager;
 import org.apache.shiro.realm.SimpleAccountRealm;
 import org.apache.sshd.SshServer;
 
 /**
  * Main entry point
  *
 * // TODO: This class should either be made more generic, or be considered a sample.
 *
  * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
  */
 public class Main {
     private static final String CONFIG_DIR = System.getProperty("user.dir");
 
     public static void main( String... args ) throws Exception {
 
         final SshServer server = new ServerFactory().createDefaultServer(
                 CONFIG_DIR, 2222, createSecurityManager());
         
         server.start();
 
         Runtime.getRuntime().addShutdownHook( new Thread() {
             @Override
             public void run() {
                 try {
                     server.stop();
                 } catch (InterruptedException ignore) {
                 }
             }
         } );
     }
 
     private static SecurityManager createSecurityManager() {
         SimpleAccountRealm realm = new SimpleAccountRealm();
 
         realm.init();
 
         DefaultSecurityManager securityManager = new DefaultSecurityManager( realm );
 
         securityManager.setCacheManager( new DefaultCacheManager() );
 
         realm.addAccount( System.getProperty( "user.name" ), "test", "shjgit" );
 
         return securityManager;
     }
 }
