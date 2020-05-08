 package net.cyklotron.cms.docimport;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentFactory;
 import org.dom4j.Element;
 import org.hamcrest.Description;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.api.Action;
 import org.jmock.api.Invocation;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.objectledge.html.HTMLException;
 import org.objectledge.html.HTMLService;
 
 public class DocumentImportServiceTest
     extends MockObjectTestCase
 {
     private static final boolean NETWORKING_TESTS = true;
 
     public void testParsing()
         throws IOException
     {
         DocumentImportServiceImpl service = new DocumentImportServiceImpl(getHTMLService());
 
         TestImportSourceConfiguration config = new TestImportSourceConfiguration(true, true);
 
         Collection<DocumentData> documents = service.loadBatch(config, null, null,
             config.getDateFormat());
         assertEquals(10, documents.size());
         final Iterator<DocumentData> docIterator = documents.iterator();
         assertEquals(4, docIterator.next().getAttachments().size());
         assertEquals(3, docIterator.next().getAttachments().size());
         assertEquals(3, docIterator.next().getAttachments().size());
         assertEquals(0, docIterator.next().getAttachments().size());
     }
 
     public void testBatching()
         throws IOException
     {
         if(!NETWORKING_TESTS)
         {
             return;
         }
 
         DocumentImportServiceImpl service = new DocumentImportServiceImpl(getHTMLService());
 
         TestImportSourceConfiguration config = new TestImportSourceConfiguration(false, false);
         
         Calendar cal = new GregorianCalendar();
         cal.setTimeInMillis(0);
         cal.set(Calendar.YEAR, 2011);
         cal.set(Calendar.MONTH, Calendar.NOVEMBER);
         cal.set(Calendar.DAY_OF_MONTH, 1);
         Date start = cal.getTime();
         cal.set(Calendar.DAY_OF_MONTH, 15);
         Date end = cal.getTime();
         
         Collection<DocumentData> documents = service.importDocuments(config, start, end);
        assertEquals(21, documents.size());
     }
 
     public void testMockHTMLService()
         throws HTMLException
     {
         HTMLService htmlService = getHTMLService();
         Writer errorWriter = new StringWriter();
         Document doc = htmlService.textToDom4j("foo", errorWriter, "profile");
         Writer textWriter = new StringWriter();
         htmlService.dom4jToText(doc, textWriter, true);
         String text = textWriter.toString();
         assertEquals("foo", text);
     }
 
     /**
      * Download attachments into the source tree for test isolation.
      * 
      * @param args
      * @throws IOException
      */
     public static void main(String[] args)
         throws IOException
     {
         DocumentImportService service = new DocumentImportServiceImpl(getHTMLService());
         TestImportSourceConfiguration config = new TestImportSourceConfiguration(true, false);
         Collection<DocumentData> documents = service.importDocuments(config, null, null);
         for(DocumentData doc : documents)
         {
             for(AttachmentData att : doc.getAttachments())
             {
                 final String[] split = att.getOriginalURI().getPath().split("/");
                 File out = new File("src/test/resources/ngo/um/" + split[split.length - 1]);
                 FileOutputStream fos = new FileOutputStream(out);
                 fos.write(att.getContents());
                 fos.close();
             }
         }
     }
 
     private static HTMLService getHTMLService()
     {
         Mockery mockery = new Mockery();
 
         final HTMLService mockHtmlService = mockery.mock(HTMLService.class);
         mockery.checking(new Expectations()
             {
                 {
                     try
                     {
                         allowing(mockHtmlService).textToDom4j(with(any(String.class)),
                             with(any(Writer.class)), with(any(String.class)));
                         will(new TextToDom4j());
 
                         allowing(mockHtmlService).dom4jToText(with(any(Document.class)),
                             with(any(Writer.class)), with(any(Boolean.class)));
                         will(new Dom4jToText());
                     }
                     catch(HTMLException e)
                     {
                         //
                     }
                 }
 
                 class TextToDom4j
                     implements Action
                 {
                     @Override
                     public void describeTo(Description description)
                     {
                         // TODO implement
                     }
 
                     @Override
                     public Object invoke(Invocation invocation)
                         throws Throwable
                     {
                         DocumentFactory factory = DocumentFactory.getInstance();
                         org.dom4j.Document document = factory.createDocument();
                         Element html = document.addElement("HTML");
                         html.addElement("HEAD").addElement("TITLE");
                         Element body = html.addElement("BODY");
                         body.setText((String)invocation.getParameter(0));
                         return document;
                     }
                 }
 
                 class Dom4jToText
                     implements Action
                 {
                     @Override
                     public void describeTo(Description description)
                     {
                         // TODO implement
                     }
 
                     @Override
                     public Object invoke(Invocation invocation)
                         throws Throwable
                     {
                         Document doc = (Document)invocation.getParameter(0);
                         Writer w = (Writer)invocation.getParameter(1);
                         w.write(doc.selectSingleNode("/HTML/BODY").getText());
                         w.flush();
                         return null;
                     }
                 }
             });
         return mockHtmlService;
     }
 }
