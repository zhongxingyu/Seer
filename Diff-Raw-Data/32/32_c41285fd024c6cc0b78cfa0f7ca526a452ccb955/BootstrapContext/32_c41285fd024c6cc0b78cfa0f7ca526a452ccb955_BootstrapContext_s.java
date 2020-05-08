 package ch.cmbntr.modulizer.bootstrap;
 
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 public interface BootstrapContext extends Map<String, String> {
 
   public static final String CONFIG_NAME = "/bootstrap-config.xml";
 
   public static final String CONFIG_KEY_LOGGING = "modulizer.logging";
 
   public static final String CONFIG_KEY_APP_ID = "modulizer.bootstrap.app.id";
 
   public static final String CONFIG_KEY_APP_DIR = "modulizer.bootstrap.app.dir";
 
   public static final String CONFIG_KEY_APP_DIR_SLOT = "modulizer.bootstrap.app.dir.slot";
 
   public static final String CONFIG_KEY_BASE_DIR = "modulizer.bootstrap.base.dir";
 
  public static final String CONFIG_KEY_GC_DELAY = "modulizer.bootstrap.gc.delay-ms";
 
   public static final String CONFIG_KEY_SECURITY_SKIP = "modulizer.bootstrap.security.skip";
 
   public static final String CONFIG_KEY_LAUNCH_PLUGINS = "modulizer.bootstrap.launch.plugins";
 
   public static final String CONFIG_KEY_PREPARE_PLUGINS = "modulizer.bootstrap.prepare.plugins";
 
   public static final String CONFIG_KEY_MAIN_MODULE = "modulizer.jboss-modules.main";
 
   public static final String CONFIG_KEY_BUNDLE_ID = "modulizer.filetree.bundle.id";
 
   public static final String CONFIG_KEY_BUNDLE_REF = "modulizer.filetree.bundle.ref";
 
   public static final String CONFIG_KEY_BUNDLE_URI = "modulizer.filetree.bundle.uri";
 
   public static final String CONFIG_KEY_IGNORE_EXISTING = "modulizer.filetree.ignore-existing";
 
   public static final String CONFIG_KEY_FILETREE_CLEANUP = "modulizer.filetree.cleanup";
 
   public static final String DEFAULT_KEY_BUNDLE_REF = "refs/heads/master";
 
   public static final String DEFAULT_KEY_BUNDLE_URI = "/filetree.dat";
 
   public static final long DEFAULT_GC_DELAY_MS = 8000L;
 
   public static final AtomicReference<String[]> ARGS = new AtomicReference<String[]>();
 
   public static final AtomicReference<BootstrapContext> CURRENT = new AtomicReference<BootstrapContext>();
 
 }
