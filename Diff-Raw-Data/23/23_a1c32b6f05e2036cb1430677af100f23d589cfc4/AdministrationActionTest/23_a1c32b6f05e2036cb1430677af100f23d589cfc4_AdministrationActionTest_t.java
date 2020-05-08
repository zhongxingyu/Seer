 package net.codjo.administration.gui.plugin;
 import net.codjo.agent.AgentContainerMock;
import net.codjo.gui.toolkit.i18n.InternationalizationTestUtil;
 import net.codjo.mad.gui.framework.DefaultGuiContext;
 import net.codjo.security.common.api.UserMock;
 import net.codjo.test.common.LogString;
 import org.uispec4j.Trigger;
 import org.uispec4j.UISpecTestCase;
 import org.uispec4j.Window;
 import org.uispec4j.interception.WindowHandler;
 import org.uispec4j.interception.WindowInterceptor;
 
 public class AdministrationActionTest extends UISpecTestCase {
     private AdministrationAction administrationAction;
     private LogString log = new LogString();
 
 
     public void test_security_allowed() {
         DefaultGuiContext guiContext = new DefaultGuiContext();
         guiContext.setUser(new UserMock().mockIsAllowedTo(AdministrationFunctions.ADMINISTRATE_SERVER,
                                                           true));
         administrationAction = new AdministrationAction(guiContext, new AgentContainerMock(log));
 
         assertTrue(administrationAction.isEnabled());
 
         administrationAction.actionPerformed(null);
 
         log.assertContent(
               "acceptNewAgent(administration-gui-agent, AdministrationGuiAgent)"
               + ", administration-gui-agent.start()");
     }
 
 
     public void test_security_notAllowed() {
        InternationalizationTestUtil.initErrorDialogTranslationBackpack();

         DefaultGuiContext guiContext = new DefaultGuiContext();
         guiContext.setUser(new UserMock());
         administrationAction = new AdministrationAction(guiContext, new AgentContainerMock(log));
 
         assertFalse(administrationAction.isEnabled());
 
         WindowInterceptor
               .init(new Trigger() {
                   public void run() throws Exception {
                       administrationAction.actionPerformed(null);
                   }
               })
               .process(new WindowHandler() {
                   @Override
                   public Trigger process(Window window) throws Exception {
                       assertTrue(window.containsLabel("Erreur de scurit"));
                       return window.getButton("OK").triggerClick();
                   }
               })
               .run();
 
         log.assertContent("");
     }
 }
