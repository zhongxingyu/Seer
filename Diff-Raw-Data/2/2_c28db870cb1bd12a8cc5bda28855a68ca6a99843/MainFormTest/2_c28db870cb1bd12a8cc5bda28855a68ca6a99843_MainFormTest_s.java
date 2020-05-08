 package net.codjo.security.gui.user;
 import java.awt.Component;
 import java.io.File;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import net.codjo.gui.toolkit.HelpButton;
 import net.codjo.security.common.message.Role;
 import net.codjo.security.common.message.SecurityEngineConfiguration.UserManagement;
 import net.codjo.security.common.message.User;
 import net.codjo.test.common.LogString;
 import net.codjo.test.common.PathUtil;
 import org.uispec4j.ListBox;
 import org.uispec4j.Panel;
 import org.uispec4j.TabGroup;
 import org.uispec4j.Trigger;
 import org.uispec4j.UISpecTestCase;
 import org.uispec4j.Window;
 import org.uispec4j.interception.BasicHandler;
 import org.uispec4j.interception.FileChooserHandler;
 import org.uispec4j.interception.WindowHandler;
 import org.uispec4j.interception.WindowInterceptor;
 import org.uispec4j.utils.ComponentUtils;
 
 import static net.codjo.security.common.message.SecurityEngineConfiguration.adsConfiguration;
 import static net.codjo.security.common.message.SecurityEngineConfiguration.defaultConfiguration;
 import static net.codjo.util.file.FileUtil.loadContent;
 
 public class MainFormTest extends UISpecTestCase {
     private MainForm mainForm;
 
 
     public void test_validate() throws Exception {
         initWithNoUser();
 
         LogString log = new LogString();
         mainForm.setGuiResultHandler(new GuiResultHandlerMock(log));
 
         getMainPanel().getButton("Annuler").click();
         log.assertContent("handleCancel()");
         log.clear();
 
         WindowInterceptor
               .init(getMainPanel().getButton("Valider").triggerClick())
               .process(BasicHandler.init()
                              .assertContainsText("Voulez-vous vraiment enregistrer le modle ?")
                              .triggerButtonClick("OK"))
               .run();
         log.assertContent("handleValidate()");
         log.clear();
     }
 
 
     public void test_exportCsv() throws Exception {
         String user1 = "smith";
         String user2 = "dupont";
         String role1 = "utilisateur";
         String role2 = "guest";
         String role3 = "administrator";
         mainForm = new MainForm(defaultConfiguration(),
                                 new ModelBuilder()
                                       .addRoleToUser(role1, user1)
                                       .addRoleToUser(role1, user2)
                                       .addRoleToUser(role2, user2)
                                       .addRoleToUser(role3, user1).get());
 
         File exportedFile = File.createTempFile("exportTest", ".csv");
 
         WindowInterceptor.init(
               getUserPanel().getButton("Export CSV").triggerClick())
               .process(FileChooserHandler.init()
                              .select(exportedFile))
               .run();
 
        assertEquals(loadContent(resource("export.csv")),
                      removeWindowsSpecificCarriageReturn(loadContent(exportedFile)));
     }
 
 
     private static String removeWindowsSpecificCarriageReturn(String currentContent) {
         return currentContent.replaceAll("\r", "");
     }
 
 
     public void test_userTab_filled() throws Exception {
         initWithTwoUsers("smith", "dupont");
 
         assertTrue(getUserList().contentEquals(new String[]{"dupont", "smith"}));
     }
 
 
     public void test_roleTab_filled() throws Exception {
         mainForm = new MainForm(defaultConfiguration(),
                                 new ModelBuilder()
                                       .addRole("simple")
                                       .addRoleComposite("composite").get());
 
         assertTrue(getMainPanel().getListBox("compositeList").contentEquals(new String[]{"composite"}));
     }
 
 
     public void test_undo_usingKeyStroke() throws Exception {
         initWithTwoUsers("smith", "dupont");
 
         mainForm.getGuiManager().addRoleToUser(new Role("admin"), new User("smith"));
         getUserList().select("smith");
 
         assertTrue(getUserPanel().getListBox("AssignedRoles").contentEquals(new String[]{"admin"}));
 
         pressKey(KeyStroke.getKeyStroke("control Z"));
 
         assertTrue(getUserPanel().getListBox("AssignedRoles").isEmpty());
     }
 
 
     public void test_editComposite_Assigned() throws Exception {
         mainForm = new MainForm(defaultConfiguration(),
                                 new ModelBuilder()
                                       .addRoleComposite("compositeA")
                                       .addRoleToUser("compositeA", "smith").get());
 
         getUserList().select("smith");
         getUserPanel().getListBox("AssignedRoles").select("compositeA");
         getUserPanel().getButton("editRightCompositeButton").click();
 
         assertTrue(getTabbedPane().selectedTabEquals("Rles"));
 
         assertTrue(getRolePanel().getListBox("compositeList").selectionEquals("compositeA"));
     }
 
 
     public void test_editComposite_unassigned() throws Exception {
         mainForm = new MainForm(defaultConfiguration(),
                                 new ModelBuilder()
                                       .addUser("smith")
                                       .addRoleComposite("compositeA", "simpleA").get());
 
         getUserList().select("smith");
         getUserPanel().getListBox("UnassignedRoles").select("compositeA");
         getUserPanel().getButton("editLeftCompositeButton").click();
 
         assertTrue(getTabbedPane().selectedTabEquals("Rles"));
 
         assertTrue(getRolePanel().getListBox("compositeList").selectionEquals("compositeA"));
     }
 
 
     public void test_editComposite_buttonState() throws Exception {
         mainForm = new MainForm(defaultConfiguration(),
                                 new ModelBuilder()
                                       .addRoleComposite("compositeA", "simpleA")
                                       .addRoleComposite("compositeB")
                                       .addRoleToUser("compositeB", "smith").get());
 
         getUserList().select("smith");
 
         assertTrue(getUserPanel().getListBox("UnassignedRoles")
                          .contentEquals(new String[]{"compositeA",
                                                      "simpleA"}));
 
         assertFalse(getUserPanel().getButton("editLeftCompositeButton").isEnabled());
         assertFalse(getUserPanel().getButton("editRightCompositeButton").isEnabled());
 
         getUserPanel().getListBox("UnassignedRoles").select("simpleA");
         assertFalse(getUserPanel().getButton("editLeftCompositeButton").isEnabled());
 
         getUserPanel().getListBox("UnassignedRoles").select("compositeA");
         assertTrue(getUserPanel().getButton("editLeftCompositeButton").isEnabled());
 
         getUserPanel().getListBox("AssignedRoles").select("compositeB");
         assertTrue(getUserPanel().getButton("editRightCompositeButton").isEnabled());
     }
 
 
     public void test_switchUserTab() throws Exception {
         mainForm = new MainForm(adsConfiguration("http://my.jnlp"),
                                 new ModelBuilder()
                                       .addRoleComposite("compositeA")
                                       .addRoleToUser("compositeA", "smith").get());
 
         assertEquals("startAdsBvGuiButton", getUserPanel().getButton().getName());
     }
 
 
     public void test_help_adsConfiguration() throws Exception {
         mainForm = new MainForm(adsConfiguration("http://my.jnlp"), new ModelBuilder().get());
 
         assertHelpUrl(
               "http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security+ads");
     }
 
 
     public void test_help_defaultConfiguration() throws Exception {
         mainForm = new MainForm(defaultConfiguration(), new ModelBuilder().get());
 
         assertHelpUrl(
               "http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security");
     }
 
 
     public void test_keepDirectoryInFileChooser() throws Exception {
         mainForm = new MainForm(defaultConfiguration(), new ModelBuilder().get());
 
         final File exportedFile = new File(PathUtil.findTargetDirectory(MainFormTest.class),
                                            "exportTest.csv");
 
         WindowInterceptor.init(
               getMainPanel().getButton("Export").triggerClick())
               .process(new WindowHandler() {
                   @Override
                   public Trigger process(Window window) throws Exception {
                       Trigger trigger = FileChooserHandler.init().select(exportedFile).process(window);
                       assertFileChooser(exportedFile.getParentFile(), exportedFile).process(window);
                       return trigger;
                   }
               })
               .run();
 
         WindowInterceptor.init(
               getMainPanel().getButton("Import").triggerClick())
               .process(assertFileChooser(exportedFile.getParentFile(), null))
               .run();
 
         WindowInterceptor.init(
               getUserPanel().getButton("export CSV").triggerClick())
               .process(assertFileChooser(exportedFile.getParentFile(), null))
               .run();
 
         WindowInterceptor.init(
               getRolePanel().getButton("export CSV").triggerClick())
               .process(assertFileChooser(exportedFile.getParentFile(), null))
               .run();
     }
 
 
     private File getSelectedFile(Window window) {
         Component[] components = window.getSwingComponents(JFileChooser.class);
         assertEquals(1, components.length);
         JFileChooser fileChooser = (JFileChooser)components[0];
         return fileChooser.getSelectedFile();
     }
 
 
     private WindowHandler assertFileChooser(final File expectedDir, final File expectedSelectedFile) {
         return new WindowHandler() {
             @Override
             public Trigger process(final Window window) throws Exception {
                 FileChooserHandler.init()
                       .assertCurrentDirEquals(expectedDir)
                       .cancelSelection();
                 ComponentUtils.close(window);
                 assertEquals(expectedSelectedFile, getSelectedFile(window));
                 return Trigger.DO_NOTHING;
             }
         };
     }
 
 
     private void assertHelpUrl(String expectedUrl) {
         String url = ((HelpButton)getMainPanel().getButton("help").getAwtComponent()).getHelpUrl();
         assertEquals(expectedUrl, url);
     }
 
 
     public void test_consistencyBetweenGuiAndAllowedConfiguration() throws Exception {
         for (UserManagement userManagement : UserManagement.values()) {
             assertThatGuiExistsFor(userManagement);
         }
     }
 
 
     private void assertThatGuiExistsFor(UserManagement type) {
         try {
             MainForm.instanciateUserTabFor(type);
         }
         catch (UnsupportedOperationException ex) {
             fail("La stratgie de gestion utilisateur " + type + " ne possde pas d'IHM associ."
                  + " Merci de construire l'IHM UserTabFor" + type.getId());
         }
     }
 
 
     private void initWithNoUser() {
         mainForm = new MainForm(defaultConfiguration(), new ModelBuilder().get());
     }
 
 
     private void initWithTwoUsers(String user1, String user2) {
         mainForm = new MainForm(defaultConfiguration(),
                                 new ModelBuilder()
                                       .addUser(user1)
                                       .addUser(user2).get());
     }
 
 
     private ListBox getUserList() {
         return getMainPanel().getListBox("userList");
     }
 
 
     private Panel getRolePanel() {
         TabGroup group = getTabbedPane();
         group.selectTab("Rles");
         return group.getSelectedTab();
     }
 
 
     private Panel getUserPanel() {
         TabGroup group = getTabbedPane();
         group.selectTab("Utilisateurs");
         return group.getSelectedTab();
     }
 
 
     private TabGroup getTabbedPane() {
         return getMainPanel().getTabGroup();
     }
 
 
     private Panel getMainPanel() {
         return new Panel(mainForm.getMainPanel());
     }
 
 
     private void pressKey(KeyStroke keyStroke) {
         JPanel mainPanel = (JPanel)getMainPanel().getAwtComponent();
         Object actionId = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(keyStroke);
         mainPanel.getActionMap().get(actionId).actionPerformed(null);
     }
 
 
     private static File resource(String name) {
         return new File(MainFormTest.class.getResource(name).getFile());
     }
 }
