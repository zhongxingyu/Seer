 package net.codjo.administration.gui.plugin;
 import java.util.Arrays;
 import javax.swing.JDesktopPane;
import net.codjo.gui.toolkit.i18n.InternationalizationTestUtil;
import net.codjo.mad.gui.framework.DefaultGuiContext;
import net.codjo.test.common.LogString;
 import org.uispec4j.Trigger;
 import org.uispec4j.UISpecTestCase;
 import org.uispec4j.Window;
 import org.uispec4j.interception.WindowHandler;
 import org.uispec4j.interception.WindowInterceptor;
 
 public class DefaultHandlerTest extends UISpecTestCase {
     private DefaultHandler defaultHandler;
     private LogString log = new LogString();
     private AdministrationGuiMock gui = new AdministrationGuiMock(log);
 
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         defaultHandler = new DefaultHandler(new DefaultGuiContext(new JDesktopPane()), gui);
     }
 
 
     public void test_handleAgentBorn() throws Exception {
         assertFalse(gui.isVisible());
 
         defaultHandler.handleAgentBorn();
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 assertTrue(gui.isVisible());
             }
         });
     }
 
 
     public void test_handleAgentDead() throws Exception {
         gui.setVisible(true);
         assertTrue(gui.isVisible());
 
         defaultHandler.handleAgentDead();
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 assertFalse(gui.isVisible());
             }
         });
     }
 
 
     public void test_handleCommunicationError() throws Exception {
        InternationalizationTestUtil.initErrorDialogTranslationBackpack();
         WindowInterceptor
               .init(new Trigger() {
                   public void run() throws Exception {
                       defaultHandler.handleError("Error !!!");
                   }
               })
               .process(new WindowHandler() {
                   @Override
                   public Trigger process(Window window) throws Exception {
                       window.assertTitleEquals("Erreur");
                       window.containsLabel("Error !!!");
                       return window.getButton("OK").triggerClick();
                   }
               })
               .run();
     }
 
 
     public void test_handleActionStarted() throws Exception {
         defaultHandler.handleActionStarted();
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("lockGui()");
             }
         });
     }
 
 
     public void test_handlePluginsReceived() throws Exception {
         defaultHandler.handlePluginsReceived(Arrays.asList("security", "workflow"));
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("initPlugins(security, workflow), unlockGui()");
             }
         });
     }
 
 
     public void test_handlePluginStarted() throws Exception {
         defaultHandler.handlePluginStarted("workflow");
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("unlockGui()");
             }
         });
     }
 
 
     public void test_handlePluginStopped() throws Exception {
         defaultHandler.handlePluginStopped("security");
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("unlockGui()");
             }
         });
     }
 
 
     public void test_handleLogsReceived() throws Exception {
         defaultHandler.handleLogsReceived(Arrays.asList("server.log", "mad.log"));
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("initLogs(server.log, mad.log), unlockGui()");
             }
         });
     }
 
 
     public void test_handleLogRead() throws Exception {
         defaultHandler.handleLogRead("My log content");
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("showLog(My log content), unlockGui()");
             }
         });
     }
 
 
     public void test_handleSystemProperties() throws Exception {
         defaultHandler.handleSystemProperties("terre=sol");
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("setSystemProperties(terre=sol), unlockGui()");
             }
         });
     }
 
 
     public void test_handleSystemEnvironment() throws Exception {
         defaultHandler.handleSystemEnvironment("air=frais");
 
         assertUntil(new Assertion() {
             public void check() throws AssertionError {
                 log.assertContent("setSystemEnvironment(air=frais), unlockGui()");
             }
         });
     }
 
 
     private void assertUntil(Assertion assertion) {
         AssertionError exception;
         long begin = System.currentTimeMillis();
         do {
             try {
                 assertion.check();
                 return;
             }
             catch (AssertionError ex) {
                 exception = ex;
                 try {
                     Thread.sleep(50);
                 }
                 catch (InterruptedException e) {
                     ;
                 }
             }
         }
         while (System.currentTimeMillis() - begin < 1000);
 
         throw exception;
     }
 
 
     interface Assertion {
         void check() throws AssertionError;
     }
 
     private class AdministrationGuiMock extends AdministrationGui {
 
         private AdministrationGuiMock(LogString log) {
             super(new GuiAgentMock(log));
         }
 
 
         @Override
         public void initPlugins(String... plugins) {
            log.call("initPlugins", (Object[])plugins);
         }
 
 
         @Override
         public synchronized void lockGui() {
             log.call("lockGui");
         }
 
 
         @Override
         public synchronized void unlockGui() {
             log.call("unlockGui");
         }
 
 
         @Override
         public void initLogs(String... logs) {
            log.call("initLogs", (Object[])logs);
         }
 
 
         @Override
         public void showLog(String content) {
             log.call("showLog", content);
         }
 
 
         @Override
         public void setSystemProperties(String value) {
             log.call("setSystemProperties", value);
         }
 
 
         @Override
         public void setSystemEnvironment(String value) {
             log.call("setSystemEnvironment", value);
         }
     }
 }
