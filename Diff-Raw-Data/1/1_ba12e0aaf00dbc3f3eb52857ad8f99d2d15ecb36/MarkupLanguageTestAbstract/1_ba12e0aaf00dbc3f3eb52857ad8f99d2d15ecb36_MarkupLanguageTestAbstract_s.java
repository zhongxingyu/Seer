 package org.melati.template.test;
 
 import java.io.IOException;
 
 import org.melati.Melati;
 import org.melati.MelatiConfig;
 import org.melati.PoemContext;
 import org.melati.poem.AccessPoemException;
 import org.melati.poem.AccessToken;
 import org.melati.poem.Capability;
 import org.melati.poem.Field;
 import org.melati.poem.test.Node;
 import org.melati.poem.test.PoemTestCase;
 import org.melati.template.AttributeMarkupLanguage;
 import org.melati.template.MarkupLanguage;
 import org.melati.template.NotFoundException;
 import org.melati.template.Template;
 import org.melati.template.TemplateContext;
 import org.melati.template.TemplateEngine;
 import org.melati.template.TemplateEngineException;
 import org.melati.template.webmacro.WebmacroTemplateEngine;
 import org.melati.util.MelatiException;
 import org.melati.util.MelatiStringWriter;
 
 import junit.framework.TestCase;
 
 
 /**
  * An abstract test which is run against most permutations of configuaration.
  * 
  * @author timp
  * @since 14-May-2006
  */
 abstract public class MarkupLanguageTestAbstract extends PoemTestCase {
 
   protected static MelatiConfig mc = null;
   protected static TemplateEngine templateEngine = null;
   protected static MarkupLanguage ml = null;
   protected static AttributeMarkupLanguage aml = null;
   protected static Melati m = null;
 
   /**
    * Constructor for PoemTest.
    * @param arg0
    */
   public MarkupLanguageTestAbstract(String arg0) {
     super(arg0);
    setDbName("poemtest");
   }
   /**
    * Constructor.
    */
   public MarkupLanguageTestAbstract() {
     super();
   }
   
 
  
   /**
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
     setDbName("poemtest");
     super.setUp();
     melatiConfig();
     templateEngine = mc.getTemplateEngine();
     if (templateEngine != null)
       templateEngine.init(mc);
     else fail();
     m = new Melati(mc, new MelatiStringWriter());
     m.setTemplateEngine(templateEngine);
     assertNotNull(m.getTemplateEngine());
     TemplateContext templateContext =
       templateEngine.getTemplateContext(m);
     m.setTemplateContext(templateContext);
   }
   
   protected void melatiConfig() throws MelatiException {
     mc = new MelatiConfig();
     if(mc.getTemplateEngine().getName() != "webmacro") {
       mc.setTemplateEngine(new WebmacroTemplateEngine());
     }
   }
   
   /**
    * Test method for rendered(Exception).
    * @throws Exception 
    * 
    * @see org.melati.template.HTMLAttributeMarkupLanguage#
    *      rendered(AccessPoemException)
    */
   public void testRenderedAccessPoemException() throws Exception {
     
     try {
       assertEquals("java.lang.Exception",aml.rendered(new Exception()));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
     try {
       AccessPoemException ape = new AccessPoemException(
           (AccessToken)getDb().getUserTable().guestUser(), new Capability("Cool"));
       assertTrue(ml.rendered(ape).indexOf(
           "org.melati.poem.AccessPoemException: " + 
           "You need the capability Cool but " + 
           "your access token _guest_ doesn&#39;t confer it") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
     try {
       AccessPoemException ape = new AccessPoemException();
       assertEquals("", aml.rendered(ape));
       System.err.println(m.getWriter().toString());
       assertTrue(m.getWriter().toString().indexOf("[Access denied to [UNRENDERABLE EXCEPTION!]") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
     try {
       AccessPoemException ape = new AccessPoemException(
           (AccessToken)getDb().getUserTable().guestUser(), new Capability("Cool"));
       assertEquals("", aml.rendered(ape));
       // NB Not at all sure how this value changed 
       //System.err.println(m.getWriter().toString());
       //assertTrue(m.getWriter().toString().indexOf("[Access denied to Melati guest user]") != -1);
       assertTrue(m.getWriter().toString().indexOf("[Access denied to _guest_]") != -1);
     } catch (Exception e) {
       System.err.println(m.getWriter().toString());
       e.printStackTrace();
       fail();
     }
 
   }
 
   /**
    * Test method for Constructor. 
    * 
    * @see org.melati.template.HTMLMarkupLanguage#
    *   HTMLMarkupLanguage(Melati, TempletLoader, PoemLocale)
    */
   public void testHTMLMarkupLanguageMelatiTempletLoaderPoemLocale() {
 
   }
 
   /**
    * Test method for Constructor 
    * @see org.melati.template.HTMLMarkupLanguage#
    *          HTMLMarkupLanguage(String, HTMLMarkupLanguage)
    */
   public void testHTMLMarkupLanguageStringHTMLMarkupLanguage() {
 
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
   public void testEscapedString() {
     try {
       // FIXME
       //assertEquals("&amp;&percent;&pound;", ml.rendered("&%£"));
       assertEquals("&amp;%£", ml.rendered("&%£"));
       assertEquals("&amp;%£", aml.rendered("&%£"));
     } catch (IOException e) {
       e.printStackTrace();
       fail();
     }
 
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
   public void testRenderedObject() {
     try {
       assertEquals("Fredd$", ml.rendered("Fredd$"));
     } catch (IOException e) {
       e.printStackTrace();
       fail();
     }
     try {
       // Note velocity seems to leave the line end on
       assertEquals("[1]", ml.rendered(new Integer("1")).trim());
     } catch (IOException e) {
       e.printStackTrace();
       fail();
     }
     
     try { 
       assertEquals("1", ml.getAttr().rendered(new Integer("1")));
     } catch (IOException e) {
       e.printStackTrace();
       fail();
     }
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
     
     try {
 
       Node persistent = (Node)getDb().getTable("node").newPersistent();
       persistent.setName("Mum");
       persistent.makePersistent();
       m.setPoemContext(new PoemContext());
       
       String renderedPersistent = ml.rendered(persistent);
       assertEquals("Mum", renderedPersistent);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
   }
 
   class Bomber {
     public Bomber() {}
     public String toString() {
       if (true == true) throw new RuntimeException("Bomber bombed.");
       return "Did not bomb";
     }
   }
   /**
    * Test method for rendered(String).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(String)
    */
   public void testRenderedString() {
     try {
       assertEquals("Fredd$", ml.rendered("Fredd$"));
     } catch (IOException e) {
       e.printStackTrace();
       fail();
     }
   }
 
   /**
    * Test method for rendered(String, int).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(String, int)
    */
   public void testRenderedStringInt() {
     try {
       assertEquals("Fre...", ml.rendered("Fredd$", 3));
     } catch (IOException e) {
       e.printStackTrace();
       fail();
     }
 
   }
 
   /**
    * Test method for rendered(Field).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Field)
    */
   public void testRenderedField() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_guest_", ml.rendered(userName));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
   /**
    * Test method for rendered(Field, int).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Field, int)
    */
   public void testRenderedFieldInt() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_guest_", ml.rendered(userName,3));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
   }
   /**
    * Test method for rendered(Field, int, int).
    * 
    * @see org.melati.template.MarkupLanguage#rendered(Field, int, int)
    */
   public void testRenderedFieldIntInt() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_gu...", ml.rendered(userName,3,3));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
 
   /**
    * Test method for renderedShort(Field).
    * 
    * @see org.melati.template.MarkupLanguage#renderedShort(Field)
    */
   public void testRenderedShort() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_guest_", ml.renderedShort(userName));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
   }
 
   /**
    * Test method for renderedMedium(Field).
    * 
    * @see org.melati.template.MarkupLanguage#renderedMedium(Field)
    */
   public void testRenderedMedium() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_guest_", ml.renderedMedium(userName));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
 
   /**
    * Test method for renderedLong(Field).
    * 
    * @see org.melati.template.MarkupLanguage#renderedLong(Field)
    */
   public void testRenderedLong() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_guest_", ml.renderedLong(userName));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
   }
 
   /**
    * Test method for renderedFull(Field).
    * 
    * @see org.melati.template.MarkupLanguage#renderedFull(Field)
    */
   public void testRenderedFull() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_guest_", ml.renderedFull(userName));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
 
   /**
    * Test method for renderedStart(Field).
    * 
    * @see org.melati.template.MarkupLanguage#renderedStart(Field)
    */
   public void testRenderedStart() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertEquals("_guest_", ml.renderedStart(userName));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
   }
 
   /**
    * Test method for input(Field).
    * 
    * @see org.melati.template.MarkupLanguage#input(Field)
    */
   public void testInputField() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertTrue(ml.input(userName).toLowerCase().indexOf("<input name=\"field_login\"") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
     /*
      * FIXME fails for hsqldb 
     Field owningTable = db.getColumnInfoTable().getColumnInfoObject(0).getField("tableinfo");
     try {
       assertTrue(ml.input(owningTable).toLowerCase().indexOf("<select name=") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
     */
   }
 
   /**
    * Test method for inputAs(Field, String).
    * 
    * @see org.melati.template.MarkupLanguage#inputAs(Field, String)
    */
   public void testInputAs() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertTrue(ml.inputAs(userName, "nonExistantTemplateName").toLowerCase().indexOf("<input name=\"field_login\"") != -1);
       fail();
     } catch (Exception e) {
       e = null;
     }
     try {
       assertTrue(ml.inputAs(userName, "org.melati.poem.StringPoemType").toLowerCase().indexOf("<input name=\"field_login\"") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
 
   /**
    * Test method for searchInput(Field, String).
    * 
    * @see org.melati.template.MarkupLanguage#searchInput(Field, String)
    */
   public void testSearchInput() {
     Field userName = getDb().getUserTable().getUserObject(0).getField("login");
     try {
       assertTrue(ml.searchInput(userName, "None").toLowerCase().indexOf("<input name=\"field_login\"") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
 
   /**
    * Test method for templet.
    * 
    * @see org.melati.template.MarkupLanguage#templet(String)
    */
   public void testTempletString() {
     try {
       Template t = m.getMarkupLanguage().templet(new Integer("1").getClass().getName());
       if(t != null) t = null;
     } catch (NotFoundException e) {
       // pass
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
     try {
       Template t = m.getMarkupLanguage().templet(new Object().getClass().getName());
       TemplateContext tc = m.getTemplateContext();
       tc.put("melati", m);
       tc.put("ml", ml);
       tc.put("object", new Object());
       t.write(m.getWriter(),tc, m.getTemplateEngine());
       // FIXME why is webmacro putting a line break at the front?
       assertTrue(m.getWriter().toString().trim().startsWith("[java.lang.Object@"));
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
     try {
       Template t = m.getMarkupLanguage().templet("select");
       TemplateContext tc = m.getTemplateContext();
       tc.put("melati", m);
       tc.put("ml", ml);
       Field nullable = getDb().getColumnInfoTable().
                            getColumnInfoObject(0).getField("nullable");
       tc.put("object", nullable);
       t.write(m.getWriter(),tc, m.getTemplateEngine());
       assertTrue(m.getWriter().toString().toLowerCase().indexOf("<select name=") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
   }
 
   /**
    * Test method for templet(Class).
    * 
    * @see org.melati.template.MarkupLanguage#templet(Class)
    */
   public void testTempletClass() {
     try {
       Template t = m.getMarkupLanguage().templet(new Integer("1").getClass());
       TemplateContext tc = m.getTemplateContext();
       tc.put("melati", m);
       tc.put("ml", ml);
       tc.put("object", new Integer("1"));
       t.write(m.getWriter(),tc, m.getTemplateEngine());
       // FIXME too much whitespace remaining
       assertEquals("[1]", m.getWriter().toString().trim());
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
   
   /**
    * Test method for templet.
    * 
    * @see org.melati.template.MarkupLanguage#templet(String, Class)
    */
   public void testTempletStringClass() {
     try {
       Template t = m.getMarkupLanguage().templet("unknown",new Integer("1").getClass());
       if(t != null) t = null;
       fail();
     } catch (NotFoundException e) {
       // Pass
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
     try {
       Template t = m.getMarkupLanguage().templet("error",new Integer("1").getClass());
       if(t != null) t = null;
       fail();
     } catch (NotFoundException e) {
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
     try {
       Template t = m.getMarkupLanguage().templet("error", new Exception().getClass());
       TemplateContext tc = m.getTemplateContext();
       tc.put("melati", m);
       tc.put("ml", ml);
       tc.put("object", new Integer("1"));
       t.write(m.getWriter(),tc, m.getTemplateEngine());
       if (m.getTemplateEngine().getName().equals("webmacro")) 
         // FIXME what is velocity doing
         if (m.getMarkupLanguage().getName().startsWith("html")) 
         fail();
     } catch (TemplateEngineException e) {
       // Pass - we should have passed in an exception as the object
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
     try {
       Template t = m.getMarkupLanguage().templet("error",new Exception().getClass());
       TemplateContext tc = m.getTemplateContext();
       tc.put("melati", m);
       tc.put("ml", ml);
       tc.put("object",new Exception("A message"));
       t.write(m.getWriter(),tc, m.getTemplateEngine());
       assertTrue(m.getWriter().toString().indexOf("A message") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
 
     try {
       Template t = m.getMarkupLanguage().templet("error",new AccessPoemException().getClass());
       TemplateContext tc = m.getTemplateContext();
       tc.put("melati", m);
       tc.put("ml", ml);
       tc.put("object", new AccessPoemException());
       t.write(m.getWriter(),tc, m.getTemplateEngine());
       assertTrue(m.getWriter().toString().indexOf("You need the capability") != -1);
     } catch (Exception e) {
       e.printStackTrace();
       fail();
     }
   }
 
 
 }
