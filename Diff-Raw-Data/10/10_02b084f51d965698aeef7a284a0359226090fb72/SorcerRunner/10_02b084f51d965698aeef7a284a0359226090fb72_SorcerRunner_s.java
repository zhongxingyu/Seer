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
 
 package sorcer.junit;
 
 import org.junit.Ignore;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 import sorcer.core.SorcerEnv;
 import sorcer.core.requestor.ServiceRequestor;
 import sorcer.launcher.Launcher;
 import sorcer.launcher.SorcerLauncher;
 import sorcer.resolver.Resolver;
 import sorcer.util.IOUtils;
 import sorcer.util.JavaSystemProperties;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.security.Policy;
 import java.util.Arrays;
 
 import static sorcer.core.SorcerConstants.SORCER_HOME;
 
 /**
  * @author Rafał Krupiński
  */
 public class SorcerRunner extends BlockJUnit4ClassRunner {
     private static File home;
     private static final Logger log;
 
     private SorcerServiceConfiguration[] configurations;
 
     static {
         String homePath = System.getProperty(SORCER_HOME);
         if (homePath != null)
             home = new File(homePath);
 
         JavaSystemProperties.ensure("logback.configurationFile", new File(home, "configs/logback.groovy").getPath());
         JavaSystemProperties.ensure(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
         JavaSystemProperties.ensure("org.rioproject.resolver.jar", Resolver.resolveAbsolute("org.rioproject.resolver:resolver-aether"));
         //JavaSystemProperties.ensure(SorcerConstants.SORCER_WEBSTER_INTERNAL, Boolean.TRUE.toString());
         //JavaSystemProperties.ensure(RMI_SERVER_CODEBASE, SorcerEnv.getWebsterUrl());
         log = LoggerFactory.getLogger(SorcerRunner.class);
     }
 
     /**
      * @throws org.junit.runners.model.InitializationError if the test class is malformed.
      */
     public SorcerRunner(Class<?> klass) throws InitializationError {
         super(klass);
         if (home == null)
             throw new InitializationError("sorcer.home property is required");
 
         String policyPath = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
         if (policyPath != null) {
             File policy = new File(policyPath);
             IOUtils.checkFileExistsAndIsReadable(policy);
         } else {
             if (System.getSecurityManager() != null)
                 throw new InitializationError("SecurityManager set but no " + JavaSystemProperties.SECURITY_POLICY);
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
             try {
                 JavaSystemProperties.ensure(JavaSystemProperties.RMI_SERVER_CODEBASE, Resolver.resolveCodeBase(SorcerEnv.getCodebaseRoot(), codebase));
             } catch (MalformedURLException e) {
                 throw new InitializationError(e);
             }
             ServiceRequestor.prepareCodebase();
         }
 
         SorcerServiceConfigurations sorcerServiceConfigurations = klass.getAnnotation(SorcerServiceConfigurations.class);
         SorcerServiceConfiguration sorcerServiceConfiguration = klass.getAnnotation(SorcerServiceConfiguration.class);
         if (sorcerServiceConfiguration != null && sorcerServiceConfigurations != null)
             throw new InitializationError("Both @SorcerServiceConfigurations and @SorcerServiceConfiguration is allowed");
 
         if (sorcerServiceConfigurations != null) {
             configurations = sorcerServiceConfigurations.value();
             if (configurations == null || configurations.length == 0)
                 throw new InitializationError("@SorcerServiceConfigurations annotation without any @SorcerServiceConfiguration entries");

         } else
             configurations = new SorcerServiceConfiguration[]{sorcerServiceConfiguration};
 
         for (SorcerServiceConfiguration configuration : configurations) {
             if (configuration.value() == null || configuration.value().length == 0)
                 throw new InitializationError("@SorcerServiceConfiguration annotation without any configuration files");
         }
     }
 
     @Override
     public void run(final RunNotifier notifier) {
         if (configurations == null) {
             super.run(notifier);
             return;
         }
 
         for (SorcerServiceConfiguration configuration : configurations) {
             Launcher sorcerLauncher = null;
             String[] configs = configuration.value();
             try {
                 sorcerLauncher = startSorcer(configs);
                 super.run(notifier);
             } catch (IOException e) {
                 notifier.fireTestFailure(new Failure(getDescription(), new Exception("Error while starting SORCER with configs: " + Arrays.toString(configs), e)));
             } finally {
                 if (sorcerLauncher != null)
                     sorcerLauncher.stop();
             }
         }
     }
 
     @Override
     protected void runChild(FrameworkMethod method, RunNotifier notifier) {
         if (method.getAnnotation(Ignore.class) == null)
             log.info("Testing {}", method.getMethod());
         super.runChild(method, notifier);
     }
 
     private Launcher startSorcer(String[] serviceConfigPaths) throws IOException {
         Launcher launcher = new SorcerLauncher();
         launcher.setConfigs(Arrays.asList(serviceConfigPaths));
         launcher.setWaitMode(Launcher.WaitMode.start);
         launcher.setHome(home);
         File logDir = new File("/tmp/logs");
         logDir.mkdir();
         launcher.setLogDir(logDir);
 
         log.info("Starting SORCER instance for test {}", getDescription());
         launcher.start();
 
         return launcher;
     }
 }
