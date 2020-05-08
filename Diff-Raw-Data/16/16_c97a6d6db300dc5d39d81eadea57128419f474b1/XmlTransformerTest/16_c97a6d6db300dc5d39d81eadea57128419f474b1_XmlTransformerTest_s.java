 /*
  * SoCoFo - Another source code formatter
  * Copyright (C) 2013  Dirk Strauss
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.googlecode.socofo.core.impl.xml;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotNull;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.io.File;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Guice;
 import org.testng.annotations.Test;
 
 import com.googlecode.socofo.core.api.FileDestination;
 import com.googlecode.socofo.core.api.StreamRoot;
 import com.googlecode.socofo.core.exceptions.LoadingException;
 import com.googlecode.socofo.core.exceptions.TranslationException;
 import com.googlecode.socofo.core.impl.TestInjectionPlan;
 import com.googlecode.socofo.rules.api.v1.RuleSet;
 import com.googlecode.socofo.rules.api.v1.RulesLoader;
 import com.googlecode.socofo.rules.api.v1.XmlFormatRules;
 import com.googlecode.socofo.rules.api.v1.XmlFormatRulesUpdater;
 
 /**
  * class to test the xml transformer.
  * 
  * @author dstrauss
  * @version 1.0
  * 
  */
 @Guice(modules = { TestInjectionPlan.class })
 @Test(singleThreaded = true, groups = "xml")
 public class XmlTransformerTest {
     /**
      * a logger.
      */
     private static final transient Logger LOG = LoggerFactory.getLogger(XmlTransformerTest.class.getName());
     /**
      * MDC name for the testcase.
      */
     private static final String MDCPARAM = "testCaseName";
     /**
      * the transformer.
      */
     @Inject
     private XmlTransformer to;
     /**
      * The rules loader.
      */
     @Inject
     private RulesLoader rulesLoader;
     /**
      * the xml transformation rules.
      */
     XmlFormatRules formatRules;
     /**
      * The stream root.
      */
     @Inject
     private StreamRoot sr;
     /**
      * the destination to write a result to.
      */
     @Inject
     private FileDestination dest;
     
     /**
      * Actions before any test method.
      * 
      * @throws java.lang.Exception
      *             if an error occurred.
      */
     @BeforeMethod
     public final void onMethod() throws Exception {
         formatRules = rulesLoader.loadFormatRules(getClass().getResourceAsStream("/xmlconfig.xml"));
         assertNotNull(formatRules);
         LOG.info("Rules are {}", formatRules);
         to.setRules(new RuleSet() {
             
             /**
              * The svuid.
              */
             private static final long serialVersionUID = 1L;
             
             @Override
             public XmlFormatRules getXmlFormatRules() {
                 return formatRules;
             }
         });
         dest.setFile(new File("target/sample1.result.xml"));
     }
     
     /**
      * Actions to perform after a test method run.
      */
     @AfterMethod
     public static final void afterMethod() {
         MDC.remove(MDCPARAM);
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.core.impl.xml.XmlTransformer#setRules(com.googlecode.socofo.rules.api.v1.RuleSet)}
      * .
      */
     @Test(enabled = false)
     public final void testLoadRules() {
         to.setRules(new RuleSet() {
             
             /**
              * 
              */
             private static final long serialVersionUID = 1L;
             
             @Override
             public XmlFormatRules getXmlFormatRules() {
                 return formatRules;
             }
         });
     }
     
     /**
      * Test method for throwing a IAE when using null.
      */
     @Test(expectedExceptions = IllegalArgumentException.class)
     public final void testLoadRulesNull() {
         to.setRules(null);
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.core.impl.xml.XmlTransformer#parseContent(java.lang.String)} .
      */
     @Test
     public final void testParseContent() {
         MDC.put(MDCPARAM, "testParseContent");
         to.parseContent(null);
         to.parseContent("");
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.core.impl.xml.XmlTransformer#performTranslation()} .
      */
     @Test
     public final void testPerformTranslation() {
         MDC.put(MDCPARAM, "testPerformTranslation");
         assertNotNull(sr);
         try {
             sr.loadStream(getClass().getResourceAsStream("/sample1.xml"), "utf-8");
         } catch (final LoadingException e) {
             fail(e.getLocalizedMessage());
         }
         final String xmlSample = sr.getContent();
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (final TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         final String result = to.getResult();
         assertNotNull(result);
         assertTrue(result.length() > 0);
         LOG.info("Result is\n" + result);
         dest.writeContent(result, "utf-8");
     }
     
     /**
      * Test run for sample2.xml.
      */
     @Test
     public final void testPerformTranslation2() {
         MDC.put(MDCPARAM, "testPerformTranslation2");
         assertNotNull(sr);
         try {
             sr.loadStream(getClass().getResourceAsStream("/sample2.xml"), "utf-8");
         } catch (final LoadingException e) {
             fail(e.getLocalizedMessage());
         }
         final String xmlSample = sr.getContent();
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (final TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         final String result = to.getResult();
         assertNotNull(result);
         assertTrue(result.length() > 0);
         LOG.info("Result is\n" + result);
         dest.writeContent(result, "utf-8");
     }
     
     /**
      * Simple translation test.
      * 
      * @throws TranslationException
      *             if an error occurred.
      */
     @Test
     public final void testTranslation1() throws TranslationException {
         MDC.put(MDCPARAM, "testTranslation1");
         final String xmlSample = "<a><b/></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         to.performTranslation();
         final String result = to.getResult();
         assertNotNull(result);
         LOG.info("rules are {}", to.getRuleSet());
         MDC.remove("testCaseName");
         assertEquals(result, "<a>\n  <b/>\n</a>\n");
     }
     
     /**
      * Simple test.
      * 
      * @throws TranslationException
      *             if an error occurred.
      */
     @Test
     public final void testTranslation2() throws TranslationException {
         MDC.put(MDCPARAM, "testTranslation2");
         final String xmlSample = "<a><b></b></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         to.performTranslation();
         final String result = to.getResult();
         assertNotNull(result);
         assertEquals(result, "<a>\n  <b></b>\n</a>\n");
     }
     
     /**
      * XML transform test.
      */
     @Test
     public final void testTranslation3() {
         MDC.put(MDCPARAM, "testTranslation3");
         final String xmlSample = "<a><b>text</b></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (final TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         final String result = to.getResult();
         assertNotNull(result);
         assertEquals(result, "<a>\n  <b>text</b>\n</a>\n");
     }
     
     @Test
     public final void testTranslation4() {
         MDC.put(MDCPARAM, "testTranslation4");
         final String xmlSample = "<a><b a=\"13\"/></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (final TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         final String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <b\n    a=\"13\"\n  />\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation5() {
         MDC.put(MDCPARAM, "testTranslation5");
         String xmlSample = "<a><b a=\"13\">test</b></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <b\n    a=\"13\"\n  >test</b>\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation6() {
         String xmlSample = "<a><b>this is a long text</b></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <b>this is a long text</b>\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation7() {
         String xmlSample = "<a><b><!-- this is a long text --></b></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <b>\n    <!--this is a long text-->\n  </b>\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation8() {
         String xmlSample = "<a><!-- this is a long text --></a>";
         assertNotNull(xmlSample);
         XmlFormatRules rs = to.getRuleSet();
         XmlFormatRulesUpdater updater = (XmlFormatRulesUpdater) rs;
         updater.getCommentsRulesUpdater().setBreakAfterBegin(true);
         updater.getCommentsRulesUpdater().setBreakBeforeEnd(true);
         updater.getCommentsRulesUpdater().setIndentComment(false);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <!--\nthis is a long text\n  -->\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation9() {
         String xmlSample = "<a><!-- this is a long text --></a>";
         assertNotNull(xmlSample);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <!--this is a long text-->\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation10() {
         String xmlSample = "<a><!-- this is a long text --></a>";
         assertNotNull(xmlSample);
         XmlFormatRules rs = to.getRuleSet();
         XmlFormatRulesUpdater updater = (XmlFormatRulesUpdater) rs;
         updater.getCommentsRulesUpdater().setBreakAfterBegin(true);
         updater.getCommentsRulesUpdater().setBreakBeforeEnd(true);
         updater.getCommentsRulesUpdater().setIndentComment(true);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <!--\n    this is a long text\n  -->\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation11() {
         String xmlSample = "<a>\n  <b>hallo</b>  \n</a>\n";
         assertNotNull(xmlSample);
         XmlFormatRules rs = to.getRuleSet();
         XmlFormatRulesUpdater updater = (XmlFormatRulesUpdater) rs;
         updater.getCommentsRulesUpdater().setBreakAfterBegin(true);
         updater.getCommentsRulesUpdater().setBreakBeforeEnd(true);
         updater.getCommentsRulesUpdater().setIndentComment(true);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
         assertEquals("<a>\n  <b>hallo</b>\n</a>\n", result);
     }
     
     @Test
     public final void testTranslation12() {
         String xmlSample =
             "<?xml version=\"1.0\"?><a xsi:ns=\"bla bla\">\n  <b>\n  <txt>Hallo, Welt</txt>\n  </b>  \n</a>\n";
         assertNotNull(xmlSample);
         XmlFormatRules rs = to.getRuleSet();
         XmlFormatRulesUpdater updater = (XmlFormatRulesUpdater) rs;
         updater.getCommentsRulesUpdater().setBreakAfterBegin(true);
         updater.getCommentsRulesUpdater().setBreakBeforeEnd(true);
         updater.getCommentsRulesUpdater().setIndentComment(true);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
        } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
        String result = to.getResult();
         assertNotNull(result);
        assertEquals("<?xml\n" + "  version=\"1.0\"\n" + "?><a\n" + "  xsi:ns=\"bla bla\"\n" + ">\n" + "  <b>\n"
            + "    <txt>Hallo, Welt</txt>\n" + "  </b>\n" + "</a>\n", result);
     }
     
     @Test
     public final void testTranslation13() {
         String xmlSample =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                 + "<tns:xml-format xmlns:tns=\"http://www.ds2/ns/socofo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ds2/ns/socofo http://www.ds2/ns/socofo \">\n"
                 + "  <commonAttributes>\n" + "    <maxLineWidth>80</maxLineWidth>\n"
                 + "    <indentSequence>  </indentSequence>\n" + "    <tabSize>4</tabSize>\n"
                 + "  </commonAttributes></tns:xml-format>";
         assertNotNull(xmlSample);
         XmlFormatRules rs = to.getRuleSet();
         XmlFormatRulesUpdater updater = (XmlFormatRulesUpdater) rs;
         updater.getCommentsRulesUpdater().setBreakAfterBegin(true);
         updater.getCommentsRulesUpdater().setBreakBeforeEnd(true);
         updater.getCommentsRulesUpdater().setIndentComment(true);
         to.parseContent(xmlSample);
         try {
             to.performTranslation();
         } catch (TranslationException e) {
             fail(e.getLocalizedMessage());
         }
         String result = to.getResult();
         assertNotNull(result);
        assertEquals("<?xml\n" + "  version=\"1.0\"\n" + "  encoding=\"UTF-8\"\n" + "?><tns:xml-format\n"
             + "  xmlns:tns=\"http://www.ds2/ns/socofo\"\n"
             + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
             + "  xsi:schemaLocation=\"http://www.ds2/ns/socofo http://www.ds2/ns/socofo \">\n"
             + "  <commonAttributes>\n" + "    <maxLineWidth>80</maxLineWidth>\n"
             + "    <indentSequence></indentSequence>\n" + "    <tabSize>4</tabSize>\n" + "  </commonAttributes>\n"
            + "</tns:xml-format>\n", result);
     }
 }
