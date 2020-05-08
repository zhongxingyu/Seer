 //
 // JOOReports - The Open Source Java/OpenOffice Report Engine
 // Copyright (C) 2004-2006 - Mirko Nasato <mirko@artofsolving.com>
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // http://www.gnu.org/copyleft/lesser.html
 //
 package net.sf.jooreports.templates;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.jooreports.templates.DocumentTemplate.ContentWrapper;
 import freemarker.template.Configuration;
 import freemarker.template.TemplateException;
 
 public class TemplateTest extends AbstractTemplateTest {
 
     public void testZipped() throws IOException, DocumentTemplateException {
         File templateFile = getTestFile("hello-template.odt");
         Map model = new HashMap();
         model.put("name", "Mirko");
         String actual = processTemplate(templateFile, model);
         assertEquals("output content", "Hello Mirko!", actual);
     }
 
     public void testUnzipped() throws IOException, DocumentTemplateException {
         File templateDir = getTestFile("hello-template");
         Map model = new HashMap();
         model.put("name", "Mirko");
         String actual = processTemplate(templateDir, model);
         assertEquals("output content", "Hello Mirko!", actual);
     }
 
     public void testXmlSpecialChars() throws IOException, DocumentTemplateException {
         File templateFile = getTestFile("hello-template.odt");
         Map model = new HashMap();
         model.put("name", "You&Me");
         String actual = processTemplate(templateFile, model);
         assertEquals("output content", "Hello You&Me!", actual);
     }
 
     /**
      * Header/footer are in styles.xml
      * 
      * @throws IOException
      * @throws TemplateException
      */
     public void testStylesDotXml() throws IOException, DocumentTemplateException {
         File templateFile = getTestFile("header-template.odt");
         Map model = new HashMap();
         model.put("name", "Mirko");
         try {
             processTemplate(templateFile, model);
             fail("undetected invalid reference to 'title' in styles.xml");
         } catch (DocumentTemplateException expected) { }
     }
 
     public void testUtf8() throws Exception {
         File templateFile = getTestFile("hebrew-template.odt");
         Map model = new HashMap();
         model.put("Text", "שלום");
         String actual = processTemplate(templateFile, model);
         assertEquals("output content", "A Hebrew Template\n\nטקסט עברי: שלום", actual);        
     }
 
     public void testCustomFreemarkerConfiguration() throws Exception {
     	File templateFile = getTestFile("number-template.odt");
     	DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
     	Configuration freemarkerConfiguration = documentTemplateFactory.getFreemarkerConfiguration();
     	freemarkerConfiguration.setNumberFormat("#,##0.00");
         Map model = new HashMap();
         model.put("number", new Integer(1000));
         DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
         File openDocumentFile = createTempFile("odt");
         template.createDocument(model, new FileOutputStream(openDocumentFile));
         assertFileCreated(openDocumentFile);
         String content = extractTextContent(openDocumentFile);        
         assertEquals("output content", "Number: 1,000.00", content);   
     }
 
     public void testLineBreaks() throws Exception {
         File templateFile = getTestFile("multiline-template.odt");
         Map model = new HashMap();
         model.put("text", "First line\nSecond line\nThird line");
         String content = processTemplate(templateFile, model);
         String expected = "Multiline Text Test\n"
             +"\n"
             +"First line\n"
             +"Second line\n" 
             +"Third line";
         assertEquals("output content", expected, content);        
     }
 
     public void testCustomContentWrapper() throws Exception {
         File templateFile = getTestFile("multiline-template.odt");
         Map model = new HashMap();
         model.put("text", "First line\nSecond line\nThird line");
         File openDocumentFile = createTempFile(".odt");
         DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
         DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
         // do not escape newline as line-break
         template.setContentWrapper(new ContentWrapper() {
 			public String wrapContent(String content) {
 				return "[#ftl]\n"
 					+ "[#escape any as any?xml]\n"
 					+ content
 					+ "[/#escape]";
 			}
         });
         template.createDocument(model, new FileOutputStream(openDocumentFile));
         assertFileCreated(openDocumentFile);
         String content = extractTextContent(openDocumentFile);        
         String expected = "Multiline Text Test\n"
             +"\n"
             +"First line Second line Third line";
         assertEquals("output content", expected, content);
     }
 }
