 package org.nuxeo.newsml.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.InputStream;
 import java.io.Serializable;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
 import org.nuxeo.ecm.core.test.CoreFeature;
 import org.nuxeo.ecm.core.test.annotations.Granularity;
 import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
 import org.nuxeo.newsml.utils.NewsMLCodec;
 import org.nuxeo.runtime.test.runner.Deploy;
 import org.nuxeo.runtime.test.runner.Features;
 import org.nuxeo.runtime.test.runner.FeaturesRunner;
 import org.nuxeo.runtime.test.runner.RuntimeHarness;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSSerializer;
 
 import com.google.inject.Inject;
 
 @RunWith(FeaturesRunner.class)
 @Features(CoreFeature.class)
 @RepositoryConfig(repositoryName = "default", user = "Administrator", cleanup = Granularity.CLASS)
 @Deploy({ "org.nuxeo.ecm.platform.audio.core", // NXP-10070
         "org.nuxeo.ecm.platform.picture.core", // NXP-10070
         "org.nuxeo.ecm.platform.video.core", // NXP-10070
        "org.nuxeo.dam.jsf:OSGI-INF/dam-core-types-contrib.xml", // NXP-10070
         "org.nuxeo.newsml" })
 public class TestNewsMLCode {
 
     @Inject
     protected CoreSession coreSession;
 
     @Inject
     protected RuntimeHarness harness;
 
     protected NewsMLCodec codec;
 
     protected LSSerializer writer;
 
     @Before
     public void setupCodec() throws Exception {
         codec = new NewsMLCodec();
 
         DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
         DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
         writer = impl.createLSSerializer();
     }
 
     @Test
     public void testNoteToNewsML() throws Exception {
         DocumentModel note = coreSession.createDocumentModel("Note");
 
         // note contains HTML wrapping boilerplate:
         note.setPropertyValue("note:note", "<html>"
                 + "<head><title>Title Stuff</title></head>"
                 + "<body><p>Content stuff</p></body>" + "</html>");
         Document newsMLDom = codec.getDefaultNewMLDomDocument();
         assertNotNull(newsMLDom);
 
         codec.bodyToXML(note, "note:note", newsMLDom);
         String updatedNewsML = writer.writeToString(newsMLDom);
         assertTrue(updatedNewsML.contains("<body.content><p>Content stuff</p>"
                 + "</body.content>"));
 
         // let's try without the boilerplate:
         note.setPropertyValue("note:note", "<p>Content stuff.</p>");
         newsMLDom = codec.getDefaultNewMLDomDocument();
         codec.bodyToXML(note, "note:note", newsMLDom);
         updatedNewsML = writer.writeToString(newsMLDom);
         assertTrue(updatedNewsML.contains("<body.content><p>Content stuff.</p>"
                 + "</body.content>"));
 
         // HTML handling is tolerant
         note.setPropertyValue("note:note", "<p>Invalid content.</div>");
         newsMLDom = codec.getDefaultNewMLDomDocument();
         codec.bodyToXML(note, "note:note", newsMLDom);
         updatedNewsML = writer.writeToString(newsMLDom);
         assertTrue(updatedNewsML.contains("<body.content><p>Invalid content.</p>"
                 + "</body.content>"));
     }
 
     @Test
     public void testNewsMLToNote() throws Exception {
         DocumentModel note = coreSession.createDocumentModel("Note");
         Document newsMLDom = codec.getDefaultNewMLDomDocument();
         XPath xpath = XPathFactory.newInstance().newXPath();
         XPathExpression expr = xpath.compile("//body/body.content");
         // XXX: assume that there is only one ContentItem in the NewsML document
         Node bodyContentNode = (Node) expr.evaluate(newsMLDom,
                 XPathConstants.NODE);
         bodyContentNode.appendChild(newsMLDom.createTextNode("Some NewsML text"));
 
         // use the code to extract the body as the HTML payload of a note
         codec.bodyFromXML(note, "note:note", newsMLDom);
         assertTrue(note.getPropertyValue("note:note").toString().contains(
                 "Some NewsML text"));
     }
 
     @Test
     public void testAutomatedSynchronizationFromDocument() throws Exception {
         DocumentModel newsmlDoc = coreSession.createDocumentModel(
                 coreSession.getRootDocument().getPathAsString(),
                 "some-article", "NewsML");
 
         newsmlDoc.setPropertyValue("dc:title", "Some article");
         newsmlDoc.setPropertyValue("note:note",
                 "<html><p>Some content.</p></html>");
 
         assertNull(newsmlDoc.getPropertyValue("file:content"));
         newsmlDoc = coreSession.createDocument(newsmlDoc);
 
         assertNotNull(newsmlDoc.getPropertyValue("file:content"));
         Blob newsmlBlob = newsmlDoc.getProperty("file:content").getValue(
                 Blob.class);
         assertEquals("application/xml", newsmlBlob.getMimeType());
         assertEquals("some-article.xml", newsmlBlob.getFilename());
         assertTrue(newsmlBlob.getString().contains("<p>Some content.</p>"));
         assertTrue(newsmlBlob.getString().contains("Some article"));
     }
 
     @Test
     public void testAutomatedSynchronizationFromNewsml() throws Exception {
         DocumentModel newsmlDoc = coreSession.createDocumentModel(
                 coreSession.getRootDocument().getPathAsString(),
                 "some-article", "NewsML");
 
         InputStream is = getClass().getResourceAsStream("/newsml-test-file.xml");
         Blob blob = StreamingBlob.createFromStream(is).persist();
         blob.setFilename("newsml-test-file.xml");
         blob.setEncoding("utf-8");
         blob.setMimeType("application/xml");
         newsmlDoc.setPropertyValue("file:content", (Serializable) blob);
 
         newsmlDoc = coreSession.createDocument(newsmlDoc);
         assertNotNull(newsmlDoc.getPropertyValue("file:content"));
 
         assertNotNull(newsmlDoc.getPropertyValue("note:note"));
         assertTrue(newsmlDoc.getPropertyValue("note:note").toString().contains(
                 "<p>This is a paragraph.</p>"));
 
         assertNotNull(newsmlDoc.getPropertyValue("dc:title"));
         assertEquals("This is the Headline.",
                 newsmlDoc.getPropertyValue("dc:title"));
     }
 
 
     @Test
     public void testHTMLBodyExtraction() throws Exception {
         InputStream is = getClass().getResourceAsStream("/newsml-test-file.xml");
         String body = NewsMLCodec.extractHTMLBody(is);
         assertTrue(body.contains("<p>This is a paragraph.</p>"));
     }
 }
