 /*
  * Copyright 2014 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.util.junit;
 
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.InitializationError;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 import sorcer.core.SorcerConstants;
 import sorcer.core.SorcerEnv;
 import sorcer.core.requestor.ServiceRequestor;
 import sorcer.launcher.Launcher;
 import sorcer.launcher.SorcerLauncher;
 import sorcer.resolver.Resolver;
 import sorcer.util.IOUtils;
 import sorcer.util.JavaSystemProperties;
 import sorcer.util.StringUtils;
 
 import java.io.File;
 import java.security.Policy;
 import java.util.Arrays;
 import java.util.concurrent.TimeoutException;
 
 import static sorcer.core.SorcerConstants.SORCER_HOME;
 
 /**
  * @author Rafał Krupiński
  */
 public class SorcerRunner extends BlockJUnit4ClassRunner {
     private Class<?> klass;
     private File home;
 
     /**
      * Creates a BlockJUnit4ClassRunner to run {@code klass}
      *
      * @throws org.junit.runners.model.InitializationError if the test class is malformed.
      */
     public SorcerRunner(Class<?> klass) throws InitializationError {
         super(klass);
         this.klass = klass;
         try {
             JavaSystemProperties.ensure(SORCER_HOME);
             home = new File(System.getProperty(SORCER_HOME));
         } catch (IllegalStateException ignore) {
             throw new InitializationError("sorcer.home property is required");
         }
     }
 
     @Override
     public void run(final RunNotifier notifier) {
         try {
             JavaSystemProperties.ensure("logback.configurationFile", new File(home, "configs/logback.groovy").getPath());
             JavaSystemProperties.ensure(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
             JavaSystemProperties.ensure("org.rioproject.resolver.jar", Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether"));
             JavaSystemProperties.ensure(SorcerConstants.SORCER_WEBSTER_INTERNAL, Boolean.TRUE.toString());
 
 
             String policyPath = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
             if (policyPath != null) {
                 File policy = new File(policyPath);
                 IOUtils.checkFileExistsAndIsReadable(policy);
             } else {
                 if (System.getSecurityManager() != null) {
                     notifier.fireTestFailure(new Failure(getDescription(), new IllegalStateException("SecurityManager set but no " + JavaSystemProperties.SECURITY_POLICY)));
                     return;
                 }
                 File policy = new File(home, "configs/sorcer.policy");
                 IOUtils.checkFileExistsAndIsReadable(policy);
                 System.setProperty(JavaSystemProperties.SECURITY_POLICY, policy.getPath());
                 Policy.getPolicy().refresh();
             }
             System.setSecurityManager(new SecurityManager());
 
             SLF4JBridgeHandler.removeHandlersForRootLogger();
             SLF4JBridgeHandler.install();
 
             SorcerEnv.debug = true;
             ExportCodebase exportCodebase = klass.getAnnotation(ExportCodebase.class);
             String[] codebase = exportCodebase != null ? exportCodebase.value() : null;
             if (codebase != null && codebase.length > 0) {
                 JavaSystemProperties.ensure(SorcerConstants.R_CODEBASE, StringUtils.join(codebase, ' '));
                 ServiceRequestor.prepareCodebase();
             }
 
             Launcher sorcerLauncher = null;
             String[] serviceConfigPaths = getServiceConfigPaths();
 
             if (serviceConfigPaths != null) {
                 if (serviceConfigPaths.length == 0) {
                     notifier.fireTestFailure(new Failure(getDescription(), new IllegalArgumentException("@SorcerService annotation without any configuration files")));
                     return;
                 }
 
                 try {
                     sorcerLauncher = startSorcer(serviceConfigPaths);
                 } catch (Exception e) {
                     notifier.fireTestFailure(new Failure(getDescription(), e));
                     return;
                 }
             }
 
             try {
                 Thread t = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         SorcerRunner.super.run(notifier);
                     }
                 });
                 t.start();
                 t.join(30000);
                 if (t.isAlive())
                     t.stop(new RuntimeException(new TimeoutException()));
             } catch (ThreadDeath x) {
                 notifier.fireTestFailure(new Failure(getDescription(), x));
             } catch (InterruptedException e) {
                 notifier.fireTestFailure(new Failure(getDescription(), e));
             } finally {
                 if (sorcerLauncher != null)
                     sorcerLauncher.stop();
             }
         } catch (RuntimeException x) {
             notifier.fireTestFailure(new Failure(getDescription(), x));
         }
     }
 
     private Launcher startSorcer(String[] serviceConfigPaths) throws Exception {
         Launcher launcher = new SorcerLauncher();
         launcher.setConfigs(Arrays.asList(serviceConfigPaths));
         launcher.setWaitMode(Launcher.WaitMode.start);
         launcher.setHome(home);
         File logDir = new File("/tmp/logs");
         logDir.mkdir();
         launcher.setLogDir(logDir);
        String rio = System.getProperty("RIO_HOME");
        if (rio != null)
            launcher.setRio(new File(rio));
         launcher.start();
 
         return launcher;
     }
 
     private String[] getServiceConfigPaths() {
         SorcerService annotation = klass.getAnnotation(SorcerService.class);
         return annotation != null ? annotation.value() : null;
     }
 }
