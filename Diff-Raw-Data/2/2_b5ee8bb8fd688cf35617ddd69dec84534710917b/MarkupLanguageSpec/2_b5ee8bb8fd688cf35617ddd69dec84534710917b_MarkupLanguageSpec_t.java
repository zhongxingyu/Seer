 package org.melati.template.test;
 
 import java.util.Properties;
 
 import org.melati.Melati;
 import org.melati.MelatiConfig;
 import org.melati.PoemContext;
 import org.melati.poem.AccessPoemException;
 import org.melati.poem.BaseFieldAttributes;
 import org.melati.poem.Capability;
 import org.melati.poem.Column;
 import org.melati.poem.Field;
 import org.melati.poem.PoemThread;
 import org.melati.util.test.Node;
 import org.melati.util.test.TreeDatabase;
 import org.melati.template.AttributeMarkupLanguage;
 import org.melati.template.MarkupLanguage;
 import org.melati.template.TemplateEngine;
 import org.melati.template.TemplateEngineException;
 import org.melati.util.JSStaticTree;
 import org.melati.util.MelatiBugMelatiException;
 import org.melati.util.MelatiException;
 import org.melati.util.MelatiStringWriter;
 import org.melati.util.Tree;
 
 import junit.framework.TestCase;
 
 
 /**
  * An abstract test which is run against most permutations of configuaration.
  * 
  * @author timp
  * @since 14-May-2006
  */
 abstract public class MarkupLanguageSpec extends TreeTestCase {
 
   protected static MelatiConfig mc = null;
   protected static TemplateEngine templateEngine = null;
   protected static MarkupLanguage ml = null;
   protected static AttributeMarkupLanguage aml = null;
   protected static Melati m = null;
 
   /**
    * Constructor for PoemTest.
    * @param arg0
    */
   public MarkupLanguageSpec(String arg0) {
     super(arg0);
   }
   /**
    * Constructor.
    */
   public MarkupLanguageSpec() {
     super();
   }
   
 
  
   /**
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
     super.setUp();
     melatiConfig();
     templateEngine = mc.getTemplateEngine();
     //System.err.println("About to init " + templateEngine.getClass().getName());
     templateEngine.init(mc);
     m = new Melati(mc, new MelatiStringWriter());
     m.setTemplateEngine(templateEngine);
     m.setPoemContext(new PoemContext());
     assertNotNull(m.getTemplateEngine());
   }
   
   abstract protected void melatiConfig() throws MelatiException ;
   
   
   
   /**
    * Test method for rendered(Exception).
    * @throws Exception 
    * 
    * @see org.melati.template.HTMLAttributeMarkupLanguage#
    *      rendered(AccessPoemException)
    */
   public void testRenderedAccessPoemException() throws Exception {
     
     assertEquals("java.lang.Exception",aml.rendered(new Exception()));
    assertEquals("[java.lang.Exception]",ml.rendered(new Exception()));
 
     AccessPoemException ape = new AccessPoemException(
           getDb().getUserTable().guestUser(), new Capability("Cool"));
     assertTrue(ml.rendered(ape),ml.rendered(ape).indexOf("[Access denied to Melati guest user]") != -1);
     ape = new AccessPoemException();
     assertEquals("", aml.rendered(ape));
     //System.err.println(m.getWriter().toString());
     assertTrue(m.getWriter().toString().indexOf("[Access denied to [UNRENDERABLE EXCEPTION!]") != -1);
     ape = new AccessPoemException(
           getDb().getUserTable().guestUser(), new Capability("Cool"));
     assertEquals("", aml.rendered(ape));
       // NB Not at all sure how this value changed 
     System.err.println(m.getWriter().toString());
       //assertTrue(m.getWriter().toString().indexOf("[Access denied to Melati guest user]") != -1);
     assertTrue(m.getWriter().toString().indexOf("[Access denied to _guest_]") != -1);
 
   }
 
 
   /**
    * Test method for getAttr().
    * 
    * @see org.melati.template.HTMLMarkupLanguage#getAttr()
    */
   public void testGetAttr() {
     assertEquals(aml.getClass(), ml.getAttr().getClass());
   }
 
   /**
    * Test method for escaped(String).
    * 
    * @see org.melati.template.HTMLLikeMarkupLanguage#escaped(String)
    */
   public void testEscapedString() throws Exception {
 
   }
   
   public void testEntitySubstitution() throws Exception { 
     char pound[] = {163};
     assertEquals("&pound;", ml.rendered(new String(pound)));
     assertEquals("&pound;", aml.rendered(new String(pound)));
   }
 
   /**
    * Test method for escaped(Persistent).
    * 
    * @see org.melati.template.HTMLLikeMarkupLanguage#escaped(Persistent)
    */
   public void testEscapedPersistent() {
     assertEquals("Melati guest user",ml.escaped(getDb().getUserTable().getUserObject(0)));
   }
 
   /**
    * Test method for encoded.
    * 
    * @see org.melati.template.HTMLLikeMarkupLanguage#encoded(String)
    */
   public void testEncoded() {
     assertEquals("+", ml.encoded(" "));
     assertEquals("+", aml.encoded(" "));
     assertEquals("%26", ml.encoded("&"));
     assertEquals("%26", aml.encoded("&"));
   }
 
   /**
    * Test method for rendered(Object).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Object)
    */
   public void testRenderedObject() throws Exception {
     assertEquals("Fredd$", ml.rendered("Fredd$"));
     // Note velocity seems to leave the line end on
     assertEquals("[1]", ml.rendered(new Integer("1")).trim());
     
     assertEquals("1", ml.getAttr().rendered(new Integer("1")));
     try { 
       ml.getAttr().rendered(new Bomber());
       fail("Should have bombed");
     } catch (Exception e) {
       e = null;
     }
     
     try { 
       ml.rendered(new Bomber());
       fail("Should have bombed");
     } catch (Exception e) {
       e = null;
     }
     
     Node persistent = (Node)getDb().getTable("node").newPersistent();
     persistent.setName("Mum");
     persistent.makePersistent();
     m.setPoemContext(new PoemContext());
      
     String renderedPersistent = ml.rendered(persistent);
     //System.err.println(renderedPersistent);
     assertEquals("Mum", renderedPersistent);
 
   }
   /**
    * @see org.melati.template.MarkupLanguage#renderedMarkup
    */
   public void testRenderedMarkupString() throws Exception { 
     assertEquals("</a>",ml.renderedMarkup("</a>"));
     assertEquals("&lt;/a&gt;",ml.rendered("</a>"));
   }
   
   /**
    * Test that a syntax error in a templet is handled.
    */
   public void testSyntaxErrorInTemplet() throws Exception { 
     Object templated = new TemplatedWithSyntaxError();
     try { 
       ml.rendered(templated);
       fail("Should have bombed");
     } catch (TemplateEngineException e) { 
       e = null; // velocity template exists and bombs
     } catch (MelatiBugMelatiException e) {
       e = null; // velocity template bombs during creation from webmacro
     }
   }
   /**
    * Test that an evaluation error in a templet is handled.
    */
   public void testEvaluationErrorInTemplet() throws Exception { 
     Object templated = new TemplatedWithBombingToString();
     try { 
       ml.rendered(templated);
       fail("Should have bombed");
     } catch (TemplateEngineException e) { 
       e = null;
     }
   }
   
   /**
    * Test that a syntax error in a WM templet is handled by Velocity.
    */
   public void testSyntaxErrorInWMTemplet() throws Exception { 
     Object templated = new TemplatedWithWMSyntaxError();
     try { 
       ml.rendered(templated);
       fail("Should have bombed");
     } catch (TemplateEngineException e) { 
       e = null;
     }
   }
   
   /**
    * Test that we can find a template on the classpath.
    */
   public void testTemplateFoundOnClasspath() throws Exception { 
     Templated templated = new Templated();
     String rendered = ml.rendered(templated);
     assertEquals("Hi, this is from a template.", rendered);    
   }
   
   /**
    * Test that toString is used if no template found.
    * FIXME a template is always used, to this test is ill conceived
    */
   public void testUntemplatedObjectUsesToString() throws Exception { 
     assertEquals("[{}]", ml.rendered(new Properties()).trim());
   }
 
   /**
    * Test that special templets are found.
    */
   public void testSpecialTemplateFound() throws Exception { 
     Column<Integer> column = getDb().getGroupMembershipTable().getUserColumn();
     BaseFieldAttributes<Integer> fa = new BaseFieldAttributes<Integer>(column, column.getType());
     Field<Integer> field = new Field<Integer>(getDb().getUserTable().administratorUser().troid(), fa);
     Object adminUtil = m.getContextUtil("org.melati.admin.AdminUtils");
     assertTrue(adminUtil instanceof org.melati.admin.AdminUtils);
     assertTrue(ml.input(field).indexOf("add_rule(\"field_user\",") != -1);
   }
 
   /**
    * An object which throws an exception when its toString method is called.
    */
   class Bomber {
     /**
      * Constructor.
      */
     public Bomber() {}
     /** 
      * Throw exception.
      */
     public String toString() {
       throw new RuntimeException("Bomber bombed.");
     }
   }
   /**
    * Test method for rendered(String).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(String)
    */
   public void testRenderedString() throws Exception {
     assertEquals("Fredd$", ml.rendered("Fredd$"));
   }
 
   /**
    * Test NPE thrown.
    */
   public void testNull() throws Exception {
     try { 
       ml.rendered(null);
       fail("should have bombed");      
     } catch (NullPointerException e) { 
       e = null;
     }
     
   }
   /**
    * Test method for rendered(String, int).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(String, int)
    */
   public void testRenderedStringInt() throws Exception {
       assertEquals("Fre...", ml.rendered("Fredd$", 3));
   }
 
   /**
    * Test method for rendered(Field).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Field)
    */
   public void testRenderedField() throws Exception {
     Field<?> userName = getDb().getUserTable().getUserObject(0).getField("login");
     assertEquals("_guest_", ml.rendered(userName));
   }
   /**
    * Test method for rendered(Field, int).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Field, int)
    */
   public void testRenderedFieldInt() throws Exception {
     Field<?> userName = getDb().getUserTable().getUserObject(0).getField("login");
     assertEquals("_guest_", ml.rendered(userName,3));
   }
 
   /**
    * Test method for rendered(Field, int, int).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Field, int, int)
    */
   public void testRenderedFieldIntInt() throws Exception {
     Field<?> userName = getDb().getUserTable().getUserObject(0).getField("login");
     assertEquals("_gu...", ml.rendered(userName,3,3));
   }
 
 
   /**
    * Test method for renderedStart(Field).
    * 
    * @see org.melati.template.MarkupLanguage#renderedStart(Field)
    */
   public void testRenderedStart() throws Exception {
     Field<?> userName = getDb().getUserTable().getUserObject(0).getField("login");
     assertEquals("_guest_", ml.renderedStart(userName));
   }
 
   /**
    * Test method for input(Field).
    * 
    * @see org.melati.template.MarkupLanguage#input(Field)
    */
   public void testInputField() throws Exception {
     Field<?> userName = getDb().getUserTable().getUserObject(0).getField("login");
     assertTrue(ml.input(userName).toLowerCase().indexOf("<input name=\"field_login\"") != -1);
   }
   /**
    * Test method for input(Field) where field to be rendered as a dropdown.
    * 
    * @see org.melati.template.MarkupLanguage#input(Field)
    */
   public void testInputFieldSelection() throws Exception {
     //Selection dropdown of references    
     Field<?> group = getDb().getGroupMembershipTable().getGroupMembershipObject(0).getField("group");
     System.err.println(ml.input(group));
     assertTrue(ml.input(group).indexOf("name=\"field_group\"") != -1);
     assertTrue(ml.input(group).indexOf("id=\"field_group\"") != -1);
     assertTrue(ml.input(group).indexOf("<select") != -1);
   }
   /**
    * Test method for input(Field) where the renderinfo is selectionWindow.
    * 
    * @see org.melati.template.MarkupLanguage#input(Field)
    */
   public void testSelectionWindowField() throws Exception {
     Field<?> owningTable = getDb().getColumnInfoTable().getColumnInfoObject(0).getField("tableinfo");
     assertTrue(ml.input(owningTable).indexOf("<input type=\"text\" name=\"field_tableinfo_dummy\"") != -1);
     Field<?> user = getDb().getGroupMembershipTable().getGroupMembershipObject(0).getField("user");
     assertTrue(ml.input(user).indexOf("<input type=\"text\" name=\"field_user_dummy\"") != -1);
   }
 
   /**
    * Test access to password field.
    */
   public void testInputFieldForRestrictedField() throws Exception { 
     PoemThread.setAccessToken(getDb().getUserTable().guestUser());
     Field<?> password = getDb().getUserTable().getPasswordColumn().asEmptyField();
     assertTrue(ml.input(password).toLowerCase().indexOf("name=\"field_password\"") != -1);
     //System.err.println(ml.rendered(getDb().getUserTable().administratorUser()));
   }
   
   /**
    * Test method for inputAs(Field, String).
    * 
    * @see org.melati.template.MarkupLanguage#inputAs(Field, String)
    */
   public void testInputAs() throws Exception {
     Field<?> userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertTrue(ml.inputAs(userName, "nonExistantTemplateName").toLowerCase().indexOf("<input name=\"field_login\"") != -1);
       fail("Should have bombed");
     } catch (Exception e) {
       e = null;
     }
     assertTrue(ml.inputAs(userName, "org.melati.poem.StringPoemType").toLowerCase().indexOf("<input name=\"field_login\"") != -1);
   }
 
   /**
    * Test method for searchInput(Field, String).
    * 
    * @see org.melati.template.MarkupLanguage#searchInput(Field, String)
    */
   public void testSearchInput() throws Exception {
     Field<?> userName = getDb().getUserTable().getUserObject(0).getField("login");
     assertTrue(ml.searchInput(userName, "None").toLowerCase().indexOf("<input name=\"field_login\"") != -1);
   }
 
 
   /**
    * Test method for rendered(Treeable).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Object)
    */
   public void testRenderedTreeable() throws Exception {
     Node parent = (Node)((TreeDatabase)getDb()).getNodeTable().newPersistent();
     parent.setName("Mum");
     parent.makePersistent();
     Node kid1 = (Node)((TreeDatabase)getDb()).getNodeTable().newPersistent();
     kid1.setName("K1");
     kid1.setParent(parent);
     kid1.makePersistent();
     Node kid2 = (Node)((TreeDatabase)getDb()).getNodeTable().newPersistent();
     kid2.setName("K2");
     kid2.setParent(parent);
     kid2.makePersistent();
     Tree testTree = new Tree(parent);
     JSStaticTree tree = new JSStaticTree(testTree, "/melati-static/admin/static");
     m.setPoemContext(new PoemContext());
       
     String renderedTree = ml.rendered(tree);
     //System.err.println(":" + renderedTree + ":");
     assertTrue(renderedTree.indexOf("init") != -1);
    
   }
   
   
 }
