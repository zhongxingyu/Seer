 /**
  * 
  */
 package org.melati.admin.test;
 
 import java.util.ArrayList;
 
 import junit.framework.AssertionFailedError;
 
 import net.sourceforge.jwebunit.html.Cell;
 import net.sourceforge.jwebunit.html.Row;
 import net.sourceforge.jwebunit.html.Table;
 
 import org.melati.JettyWebTestCase;
 import org.melati.util.HTMLUtils;
 
 /**
  * 
  *  
  * @author timp
  * @since 2008/01/01
  */
 public class AdminJettyWebTest extends JettyWebTestCase {
    private String dbName = "admintest";
   
   /**
    * 
    */
   public AdminJettyWebTest() {
     super();
   }
   /**
    * @param name
    */
   public AdminJettyWebTest(String name) {
     super(name);
   }
   /**
    * If you don't know by now.
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
     startServer(8080);
   }
   /**
    * There appears to be a problem with gargoyle javascript such that 
    * we cannot begin at the Main page as this throws NullPointerException, 
    * The work around is to begin at the server top and then got the Main page.
    * 
    * {@inheritDoc}
    * @see org.melati.JettyWebTestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
     beginAt("/");
     
   }
 
   // Test Page calls
   /**
    * 
    */
   public void testBadUrl() {
     gotoPage("/Admin/" + dbName + "/Unknown");
     assertTextPresent("Melati Error Template");
   }
   /**
    * 
    */
   public void testAdminMain() {
     gotoPage("/Admin/" + dbName + "/Main");
     assertTextPresent("You need a frames enabled browser to use the Admin Suite");
   }
   /**
    * 
    */
   public void testAdminTop() {
     gotoPage("/Admin/" + dbName + "/Top");
     assertTextPresent("Melati Database Admin Suite - Options for Admintest database");
   }
   /**
    * 
    */
   public void testAdminTopWithTable() {
     gotoPage("/Admin/" + dbName +  "/user/Top");
     assertTextPresent("Melati Database Admin Suite - Options for Admintest database");
   }
   /**
    * 
    */
   public void testAdminTopWithTableAndTroid() {
     gotoPage("/Admin/" + dbName +  "/user/0/Top");
     assertTextPresent("Melati Database Admin Suite - Options for Admintest database");
   }
   
   /**
    * 
    */
   public void testUpload() {
     gotoPage("/Admin/" + dbName + "/user/Upload?field=tim");
     assertTextPresent("File to upload:");
   }
   
   /**
    * Test that an AdminSpecialised object has its special templet included in edit.
    */
   public void testAdminSpecialised() { 
     loginAsAdministrator();
     gotoPage("/Admin/admintest/uploadedfile/Main");
     gotoRootWindow();
     gotoFrame("admin_top");
     setWorkingForm("gotoform");
     selectOption("table","Uploaded File");
     assertFormPresent("gotoform");
     submit();
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_navigation");
     clickLink("add");
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
     
     setTextField("field_filename","test.txt");
     clickLinkWithText("Upload a new file");
     gotoWindow("filename");
     setTextField("file","/dist/melati/melati/src/main/java/org/melati/admin/static/file.gif");
     
     submit();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
     submit();
     gotoPage("/Admin/admintest/uploadedfile/0/Edit");
     assertTextPresent("Hi");    
   }
   
   /**
    * Test that an AdminSpecialised object invokes its own handler.
    */
   public void testAdminSpecialisedHandler() { 
     gotoPage("/Admin/admintest/specialised/Main");
     gotoAddRecord("Specialised");
     setTextField("field_name", "test");
     submit();
     assertTextPresent("Done");
     gotoPage("/Admin/admintest/specialised/0/NotAnAdminMethod");
     assertTextPresent("Hi, I'm Special."); 
     // Hmm, is this intended behaviour?
     gotoPage("/Admin/admintest/specialised/0/Edit");
     assertTextPresent("Hi, I'm Special.");        
   }
   
   /**
    * Test that if there is no primary select column on the table 
    * no primary criteria are displayed. 
    */
   public void testNoPrimarySelect() { 
     gotoPage("/Admin/admintest/user/PrimarySelect");
     assertTablePresent("primarySelectTable");
     gotoPage("/Admin/admintest/uploadedfile/PrimarySelect");
     assertTableNotPresent("primarySelectTable");
     gotoPage("/Admin/admintest/user/PrimarySelect?field_name=");
     assertTablePresent("primarySelectTable");
 
     // Relies upon an object already being created in previous test
     gotoPage("/Admin/admintest/specialised/0/PrimarySelect");
     assertTableNotPresent("primarySelectTable");
     
   }
   
 
   /**
    *  Getting the coverage is proof enough.
    */
   public void testDescendingOrder() { 
     gotoPage("/Admin/admintest/user/Selection?target=admin_record&returnTarget=admin_record&field_id=0&field_order-1=1&field_order-1-toggle=true");
     gotoPage("/Admin/admintest/user/Selection?target=admin_record&returnTarget=admin_record&field_id=0&field_order-1=1&field_order-1-toggle=true");
     gotoPage("/Admin/admintest/uploadedfile/Selection?target=admin_record&returnTarget=admin_record&field_order-1=70&field_order-1-toggle=true");
     gotoPage("/Admin/admintest/uploadedfile/Selection?target=admin_record&returnTarget=admin_record&field_order-1=70&field_order-1-toggle=true");
   }
   /**
    * 
    */
   public void testAdminBottom() {
     gotoPage("/Admin/" + dbName + "/user/Bottom");
     // Hmmm Should assert something, coverage is the thing
   }
   /**
    * 
    */
   public void testAdminRight() {
     gotoPage("/Admin/" + dbName + "/user/0/Right");
     // Hmmm Should assert something, coverage is the thing
   }
   /**
    * 
    */
   public void testAdminPrimarySelect() {
     gotoPage("/Admin/" + dbName + "/user/PrimarySelect");
     assertTextPresent("Full name");
   }
   /**
    * 
    */
   public void testAdminSelection() {
     gotoPage("/Admin/" + dbName + "/user/Selection?target=&returnTarget=");
     assertTextPresent("Full name");
     assertTextPresent("Melati guest user");
     clickLinkWithText("Full name");
     Table s = getTable("selectionTable");
     ArrayList rows = s.getRows();
     for (int i = 0; i< rows.size(); i++) { 
       ArrayList cells = ((Row)rows.get(i)).getCells();
       for (int j = 0; j< cells.size(); j++) { 
         String value = ((Cell)cells.get(j)).getValue();
         if(i == 2 && j == 2)
           assertEquals("_guest_", value);
         if(i == 3 && j == 2)
           assertEquals("_administrator_", value);
       }
     }
     clickLinkWithText("Full name");
     s = getTable("selectionTable");
     rows = s.getRows();
     for (int i = 0; i< rows.size(); i++) { 
       ArrayList cells = ((Row)rows.get(i)).getCells();
       for (int j = 0; j< cells.size(); j++) { 
         String value = ((Cell)cells.get(j)).getValue();
         if(i == 2 && j == 2)
           assertEquals("_administrator_", value);
         if(i == 3 && j == 2)
           assertEquals("_guest_", value);
       }
     }
     clickLinkWithText("Full name");
     s = getTable("selectionTable");
     rows = s.getRows();
     for (int i = 0; i< rows.size(); i++) { 
       ArrayList cells = ((Row)rows.get(i)).getCells();
       for (int j = 0; j< cells.size(); j++) { 
         String value = ((Cell)cells.get(j)).getValue();
         if(i == 2 && j == 2)
           assertEquals("_guest_", value);
         if(i == 3 && j == 2)
           assertEquals("_administrator_", value);
       }
     }
     clickLinkWithText("Full name");
     s = getTable("selectionTable");
     rows = s.getRows();
     for (int i = 0; i< rows.size(); i++) { 
       ArrayList cells = ((Row)rows.get(i)).getCells();
       for (int j = 0; j< cells.size(); j++) { 
         String value = ((Cell)cells.get(j)).getValue();
         if(i == 2 && j == 2)
           assertEquals("_administrator_", value);
         if(i == 3 && j == 2)
           assertEquals("_guest_", value);
       }
     }
   }
   /**
    * Can we get to page three and back.
    */
   public void testSelectionPaging() { 
     gotoPage("/Admin/" + dbName + "/columninfo/Selection?target=&returnTarget=");
     clickLinkWithText(">");
     clickLinkWithText(">");
     clickLinkWithText(">");
     assertTextPresent("Records 61 to 80 of 90");
     clickLinkWithText("<");
     clickLinkWithText("<");
     clickLinkWithText("<");
     assertTextPresent("Records 1 to 20 of 90");
   }
   /**
    * 
    */
   public void testAdminEditHeader() {
     gotoPage("/Admin/" + dbName + "/parent/0/EditHeader");
     clickLinkWithText("Child");
     gotoWindow("admin_edit_parent_0");
     assertElementPresent("selection");
     
   }
   /**
    * 
    */
   public void testAdminEdit() {
     gotoPage("/Admin/" + dbName + "/user/0/Edit");
     assertTextPresent("Full name");
     assertTextPresent("_guest_");
     clickLinkWithText("More ...");
     assertLinkNotPresentWithText("More ...");
   }
   
   /**
    * 
    */
   public void testAdminEditField() { 
     gotoPage("/Admin/admintest/markup/Main");
     gotoAddRecord("Markup");
     assertEquals("&Aacute;",HTMLUtils.entityFor("\u00C1".charAt(0),false, null, false));
     //char it = 193;
     //System.err.println("\u00C1".charAt(0));
     //System.err.println(new Integer("\u00C1".charAt(0)));
     //System.err.println(new Integer("?".charAt(0)));
     //System.err.println("Acirc=" +new Integer("Â".charAt(0)));
     //System.err.println("");
 
     setTextField("field_text", "\u00C1");
     submit();
     assertTextPresent("Done");
     gotoPage("/Admin/admintest/markup/0/Edit");
     assertEquals("\u00C1",getFormFieldValue("field_text"));
     assertTextPresent("\u00C1");
   }
   /**
    * @param fieldName
    * @return value of named field
    */
   public String getFormFieldValue(String fieldName) { 
     try { 
       return getTester().getElementAttributByXPath(
             "//input[@name='" + fieldName + "']", "value");
     } catch (AssertionFailedError e) { 
       System.out.println("Form element not present:" + fieldName);
       System.out.println(getTester().getPageSource());
       throw e;             
     }
     
   }
   /**
    * @param fieldName
    * @return value of named field
    */
   public String getFormTextareaValue(String fieldName) { 
     return getTester().getElementTextByXPath(
             "//textarea[@name='" + fieldName + "']");
     
   }
 
   /**
    * Test that login is required.
    */
   public void testAdminEditAdministrator() {
     gotoPage("/Admin/" + dbName + "/user/1/Edit");
     assertTextPresent("You need to log in.");
     setTextField("field_login", "_administrator55_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME_not");
     checkCheckbox("rememberme");
     submit("action");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
     assertTextPresent("Full name");
     assertTextPresent("_administrator_");
 
     // Note that logging out has no effect if rememberme was chosen
     gotoPage("/Logout/" + dbName + "");
     gotoPage("/Admin/" + dbName + "/user/1/Edit");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     assertTextFieldEquals("field_email", "");
     setTextField("field_email", "test@test.com");
     submit("action");
     assertTextFieldEquals("field_email", "test@test.com");
     setScriptingEnabled(false);
     submit("action");
     assertTextPresent("Updated a User Record");
     assertTextPresent("Done");
   }
   /**
    * Test that we are challenged to get into the everything db.
    */
   public void testEverythingIsProtected() {
     gotoPage("/Admin/everything/Main");
     assertTextPresent("You need to log in");
     
   }
   /**
    * 
    */
   public void testAdminTree() {
     gotoPage("/Admin/" + dbName + "/user/0/Record");
     gotoFrame("admin_edit_header");
     assertTextPresent("_guest_");
     assertTextPresent("[ Group membership ]");
     clickLink("recordTree");
     gotoRootWindow();
     gotoFrame("admin_edit_user_0");
     assertTextPresent("Melati guest user tree");
     clickLinkWithText("Melati guest user");
   }
   /**
    * 
    */
   public void testAdminTreeNoScript() {
     gotoPage("/Admin/" + dbName + "/user/0/Tree");
     assertTextPresent("Melati guest user tree");
   }
   /**
    * 
    */
   public void testAdminTableTree() {
     gotoPage("/Admin/" + dbName + "/user/Table");
     gotoFrame("admin_navigation");
     clickLink("tableTree");
     gotoRootWindow();
     gotoFrame("admin_selection");
     assertTextPresent("User table tree");
     assertLinkPresentWithText("Melati guest user");
     assertLinkPresentWithText("Melati database administrator");
   }
   /**
    * 
    */
   public void testAdminSelectionWindow() {
     gotoPage("/Admin/" + dbName + "/user/SelectionWindow?returnfield=field_user");
     assertTextPresent("Select a User");
   }
   /**
    * 
    */
   public void testAdminSelectionWindowPrimarySelect() {
     gotoPage("/Admin/" + dbName + "/user/SelectionWindowPrimarySelect?returnfield=field_user");
     assertTextPresent("Full name");
   }
   /**
    * 
    */
   public void testAdminSelectionWindowSelection() {
     gotoPage("/Admin/" + dbName + "/user/SelectionWindowSelection?returnfield=field_user");
     assertTextPresent("Records 1 to 2 of 2");
   }
   /**
    * 
    */
   public void testAdminPopup() {
     gotoPage("/Admin/" + dbName + "/user/PopUp");
     assertTextPresent("Search User Table");
   }
   /**
    * 
    */
   public void testAdminDSD() {
     gotoPage("/Admin/" + dbName + "/DSD");
     assertTextPresent("Generated for _guest_");
     assertTextPresent("package org.melati.admin.test;");
   }
   /**
    * Move to login
    */
   public void testLoginWithContinuation() {
     gotoPage("/Login/" + dbName + "?continuationURL=" + contextUrl("/index.html"));
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
     assertTextPresent("Hello World!");
   }
   
   /**
    * Test setting the defaults.
    */
   public void testSetupStory() { 
     loginAsAdministrator();
     gotoFrame("admin_top");
     setScriptingEnabled(false);
     clickLinkWithText("Setup");
     gotoRootWindow();
     gotoFrame("admin_bottom");
     assertTextPresent("Done");
     //deleteRecord("setting","org.melati.admin.Admin.ScreenStylesheetURL", 0);
     //deleteRecord("setting","org.melati.admin.Admin.PrimaryDisplayTable", 1);
     //deleteRecord("setting","org.melati.admin.Admin.HomepageURL", 2);
     //gotoPage("/Admin/" + dbName + "/setting/Main");
     //gotoFrame("admin_bottom");
     //gotoFrame("admin_left");
     //gotoFrame("admin_selection");
     //assertTextPresent("No records found");
   }
 
   
   /**
    * Search for a set of records.
    */
   public void testSearchAndGoto() {
     setScriptingEnabled(false);
     gotoPage("/Admin/" + dbName + "/Main");
     gotoRootWindow();
     gotoFrame("admin_top");
     setWorkingForm("gotoform");
     selectOption("table","Column");
     assertFormPresent("gotoform");
     submit();
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_navigation");
     clickLink("search");
     gotoWindow("admin_search");
     setTextField("field_displayname", "Id");
     selectOption("field_order-1","Id");
     selectOption("field_order-2","Owning table");
     submit();
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_selection");
     assertTextPresent("Records 1 to 15 of 15");
     //String page = getPageSource();
     //System.err.println(page);
   }
   /**
    * User story.
    */
   public void testCreateTableStory() { 
     setScriptingEnabled(false);
     loginAsAdministrator();
     gotoAddRecord("Table");
     setTextField("field_name", "test");
     setTextField("field_displayname", "Test");
     setTextField("field_description", "A Test table");
     setTextField("field_displayorder", "0");
     selectOption("field_category","Normal");
     submit();
     assertTextPresent("Done");
     String tableTroid = getElementAttributByXPath(
         "//input[@name='" + "troid" + "']", "value");
     gotoPage("/Admin/" + dbName + "/tableinfo/" + tableTroid + "/Main"); 
     System.err.println("Found created table troid " + tableTroid);
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
     gotoFrame("admin_edit_header");
     clickLinkWithText("Column");
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");   
     gotoFrame("admin_edit_tableinfo_" + tableTroid);
     clickLink("create_columninfo");
     setTextField("field_name", "test");
     setTextField("field_description", "A Test column");
     setTextField("field_displayorder", "0");
     checkCheckbox("field_usercreateable");
     checkCheckbox("field_indexed");
     // We want to duplicate
     //checkCheckbox("field_unique");
     setTextField("field_displayname", "Test");
     checkCheckbox("field_nullable");
     setTextField("field_size", "20");
     setTextField("field_width", "20");
     setTextField("field_height", "1");
     setTextField("field_precision", "1");
     setTextField("field_scale", "1");
     submit();
     assertTextPresent("Done");
     String columnTroid = getElementAttributByXPath(
         "//input[@name='" + "troid" + "']", "value");
     System.err.println("Found created troid " + columnTroid);
     gotoAddRecord("Test");
     setTextField("field_test", "test");
     submit();
     assertTextPresent("Done");
     String recordTroid = getElementAttributByXPath(
         "//input[@name='" + "troid" + "']", "value");
     
     clickLink("continue");
     gotoPage("/Admin/" + dbName + "/test/" + recordTroid + "/Main"); 
     gotoFrame("admin_bottom");    
     gotoFrame("admin_record");
     gotoFrame("admin_edit_test_" + recordTroid);
     submit("action","Duplicate");
     assertTextPresent("Done");
     //String href = getElementAttributByXPath(
     //    "//a[@id='" + "continue" + "']", "href");
     //System.err.println("Continue:" + href);
     clickLink("continue");
     
     // Records will be sorted by id
     deleteRecord("test", "test", new Integer(recordTroid).intValue());
     deleteRecord("test", "test", new Integer(recordTroid).intValue() + 1);
     
     gotoPage("/Admin/" + dbName + "/columninfo/" + columnTroid + "/Main"); 
     gotoFrame("admin_bottom");    
     gotoFrame("admin_record");
     gotoFrame("admin_edit_columninfo_" + columnTroid);
     submit("action","Delete");
     assertTextPresent("Done");
 
     gotoPage("/Admin/" + dbName + "/tableinfo/" + tableTroid + "/Main"); 
     gotoFrame("admin_bottom");    
     gotoFrame("admin_record");
     gotoFrame("admin_edit_tableinfo_" + tableTroid);
     submit("action","Delete");
     clickLink("edit_columninfo_" + (new Integer(columnTroid).intValue() -1));
     submit("action","Delete");
     assertTextPresent("Done");
     clickLink("continue");
     assertTextPresent("Done");
     clickLink("continue");
     assertTextPresent("Melati Database Admin Suite - Admintest database");
   }
   private void gotoAddRecord(String table) {
     gotoRootWindow();
     gotoFrame("admin_top");
     selectOption("table",table);
     assertFormPresent("gotoform");
     setWorkingForm("gotoform");
     submit();
     gotoRootWindow();
     gotoFrame("admin_bottom");
     //System.err.println(getTester().getDialog().getPageURL());
     
     gotoFrame("admin_left");
     gotoFrame("admin_navigation");
     clickLink("add");
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
   }
   /**
    * Start and end at top window.
    * @param tableName 
    * @param uniqueKeyValue
    */
   private void deleteRecord(String tableName, String uniqueKeyValue, int troid) {
     gotoPage("/Admin/" + dbName + "/" + tableName + "/Main");
     gotoFrame("admin_bottom");
     gotoFrame("admin_left");
     gotoFrame("admin_selection");
     clickLinkWithText(uniqueKeyValue);
     gotoRootWindow();
     gotoFrame("admin_bottom");
     gotoFrame("admin_record");
     gotoFrame("admin_edit_" + tableName + "_" + troid);
     clickButton("delete");
     gotoPage("/Admin/" + dbName + "/Main");
   }
   /**
    * Returns us to top frame.
    */
   private void loginAsAdministrator() {
     gotoPage("/Admin/" + dbName + "/Main");
     gotoFrame("admin_top");
     clickButton("login");
     setTextField("field_login", "_administrator_");
     setTextField("field_password", "FIXME");
     checkCheckbox("rememberme");
     submit("action");
   }
 }
