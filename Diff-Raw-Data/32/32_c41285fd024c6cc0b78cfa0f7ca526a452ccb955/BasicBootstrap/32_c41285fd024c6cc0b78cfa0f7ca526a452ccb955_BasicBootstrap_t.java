 package ch.cmbntr.modulizer.bootstrap.impl;
 
 import static ch.cmbntr.modulizer.bootstrap.util.ModulizerIO.closeQuietly;
 import static ch.cmbntr.modulizer.bootstrap.util.ModulizerIO.mkdir;
 import static ch.cmbntr.modulizer.bootstrap.util.ModulizerLog.initLogging;
 import static java.lang.Boolean.parseBoolean;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.management.ManagementFactory;
 import java.util.InvalidPropertiesFormatException;
 import java.util.UUID;
 import java.util.concurrent.Future;
 
 import ch.cmbntr.modulizer.bootstrap.Bootstrap;
 import ch.cmbntr.modulizer.bootstrap.BootstrapContext;
 import ch.cmbntr.modulizer.bootstrap.Launch;
 import ch.cmbntr.modulizer.bootstrap.Prepare;
 import ch.cmbntr.modulizer.bootstrap.util.Resources;
 import ch.cmbntr.modulizer.bootstrap.util.Resources.Pool;
 
 public class BasicBootstrap extends AbstractOperation implements Bootstrap {
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void run() {
     final Pool handle = Resources.getPoolHandle();
     try {
       establishContext();
       performSecuritySettings();
       initializeLogging();
      verboseLoading();
 
       final Future<ClassLoader> prepareLoader = preparePluginLoader(handle);
       final Future<ClassLoader> launchLoader = launchPluginLoader(handle);
 
       prepare(prepareLoader);
       launch(launchLoader);
       scheduleGC();
     } finally {
       clearContext();
       Resources.dispose(handle);
     }
   }
 
   private void performSecuritySettings() {
     try {
       if (!parseBoolean(lookupContext(BootstrapContext.CONFIG_KEY_SECURITY_SKIP))) {
         log("applying security settings");
         System.setSecurityManager(null);
       }
     } catch (final SecurityException e) {
       warn("failed to apply security settings: %s", e);
     }
   }
 
   private void establishContext() {
     final InputStream config = BasicBootstrap.class.getResourceAsStream(BootstrapContext.CONFIG_NAME);
     if (config == null) {
       warn("config not found: %s", BootstrapContext.CONFIG_NAME);
     }
     try {
       final PropertiesContext ctx = PropertiesContext.empty().loadFromXML(config).addSystemProperties();
       ctx.put(BootstrapContext.CONFIG_KEY_APP_ID, sanitizeAppId(ctx));
       ctx.put(BootstrapContext.CONFIG_KEY_APP_DIR, sanitizeAppDir(ctx));
       BootstrapContext.CURRENT.set(ctx);
 
     } catch (final InvalidPropertiesFormatException e) {
       throw new RuntimeException(e);
     } catch (final IOException e) {
       throw new RuntimeException(e);
     } finally {
       closeQuietly(config);
     }
   }
 
   private void initializeLogging() {
     String loggingConfig = lookupContext(BootstrapContext.CONFIG_KEY_LOGGING);
     if ("app.dir".equals(loggingConfig)) {
       loggingConfig = "file:" + lookupContext(BootstrapContext.CONFIG_KEY_APP_DIR) + "/bootstrap.log";
     }
     initLogging(loggingConfig);
   }
 
  private void verboseLoading() {
    final String val = lookupContext(BootstrapContext.CONFIG_KEY_VERBOSE_LOADING_MILLIS);
    final long verboseLoadingMillis = val == null ? -1L : Long.parseLong(val);
    verboseClassloading(verboseLoadingMillis);
  }

  private static void verboseClassloading(final long durationMillis) {
    if (durationMillis > 0L) {
      ManagementFactory.getClassLoadingMXBean().setVerbose(true);
      Resources.delay(durationMillis, new Runnable() {
        @Override
        public void run() {
          ManagementFactory.getClassLoadingMXBean().setVerbose(false);
        }
      });
    }
  }

   private String sanitizeAppId(final PropertiesContext ctx) {
     final String given = ctx.get(BootstrapContext.CONFIG_KEY_APP_ID);
     return given == null ? "unnamed-" + UUID.randomUUID() : given.trim();
   }
 
   private String sanitizeAppDir(final PropertiesContext ctx) throws IOException {
     final String given = ctx.get(BootstrapContext.CONFIG_KEY_APP_DIR);
     return ensureAppDir(given == null ? determineDefaultAppDir(ctx) : new File(given));
   }
 
   private File determineBootstrapBaseDir(final PropertiesContext ctx) {
     final String baseDir = ctx.get(BootstrapContext.CONFIG_KEY_BASE_DIR);
     return new File(baseDir == null ? System.getProperty("java.io.tmpdir") : baseDir);
   }
 
   private File determineDefaultAppDir(final PropertiesContext ctx) {
     final String appId = ctx.get(BootstrapContext.CONFIG_KEY_APP_ID);
     final File appDir = new File(determineBootstrapBaseDir(ctx), appId);
     final String slot = ctx.get(BootstrapContext.CONFIG_KEY_APP_DIR_SLOT);
     return slot == null ? appDir : new File(appDir, slot.trim());
   }
 
   private String ensureAppDir(final File appDir) throws IOException {
     mkdir(appDir);
     return appDir.getCanonicalFile().getAbsolutePath();
   }
 
   private Future<ClassLoader> preparePluginLoader(final Pool handle) {
     return pluginLoaderViaSpecKey(handle, BootstrapContext.CONFIG_KEY_PREPARE_PLUGINS);
   }
 
   private void prepare(final Future<ClassLoader> loader) {
     invokePluginOperations(Prepare.class, loader);
   }
 
   private Future<ClassLoader> launchPluginLoader(final Pool handle) {
     return pluginLoaderViaSpecKey(handle, BootstrapContext.CONFIG_KEY_LAUNCH_PLUGINS);
   }
 
   private void launch(final Future<ClassLoader> loader) {
     invokePluginOperations(Launch.class, loader);
   }
 
   private void scheduleGC() {
     final String val = lookupContext(BootstrapContext.CONFIG_KEY_GC_DELAY);
     final long delay = val == null ? BootstrapContext.DEFAULT_GC_DELAY_MS : Long.parseLong(val);
     delayedGC(delay);
   }
 
   private static void delayedGC(final long delay) {
    if (delay >= 0L) {
       Resources.delay(delay, new Runnable() {
         @Override
         public void run() {
           ManagementFactory.getMemoryMXBean().gc();
         }
       });
     }
   }
 
   private void clearContext() {
     final BootstrapContext ctxt = BootstrapContext.CURRENT.getAndSet(null);
     log("final context: %s", ctxt);
   }
 
 }
