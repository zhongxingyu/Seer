 package com.celements.photo.unpack;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.celements.photo.container.ImageLibStrings;
 import com.celements.photo.utilities.AddAttachmentToDoc;
 import com.celements.photo.utilities.Unzip;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 public class UnpackComponentTest extends AbstractBridgedComponentTestCase {
   private UnpackComponent upc;
   private XWiki xwiki;
 
   @Before
   public void setUp_UnpackComponentTest() throws Exception {
     upc = new UnpackComponent();
     upc.inject_context = getContext();
     xwiki = createMock(XWiki.class);
     getContext().setWiki(xwiki);
   }
 
   @Test
   public void testUnzipFileToAttachment_parameterPromotion() throws XWikiException, IOException {
     String filename = "file.zip";
     String imgName = "file 1.png";
     String cleanImgName = "file1.png";
     DocumentReference zipSrcDocRef = new DocumentReference(getContext().getDatabase(),
         "AttSpace", "ZipDoc");
     DocumentReference imgDestDocRef = new DocumentReference(getContext().getDatabase(),
         "AttSpace", "AttDoc");
     XWikiDocument srcDoc = new XWikiDocument(zipSrcDocRef);
     XWikiDocument destDoc = new XWikiDocument(zipSrcDocRef);
     XWikiAttachment att = createMock(XWikiAttachment.class);
     expect(att.getFilename()).andReturn(filename).anyTimes();
     List<XWikiAttachment> atts = new ArrayList<XWikiAttachment>();
     expect(att.getMimeType(same(getContext()))).andReturn(ImageLibStrings.MIME_ZIP);
     expect(att.getContentInputStream(same(getContext()))).andReturn(createMock(
         InputStream.class));
     expect(att.clone()).andReturn(att).anyTimes();
     atts.add(att);
     srcDoc.setAttachmentList(atts);
     Unzip unzip = createMock(Unzip.class);
     upc.inject_unzip = unzip;
     InputStream in = this.getClass().getClassLoader().getResourceAsStream(
         "test.zip");
     byte[] inArray = IOUtils.toByteArray(in);
     ByteArrayOutputStream bais = new ByteArrayOutputStream();
     bais.write(inArray);
     expect(unzip.getFile(eq(imgName), (InputStream)anyObject())).andReturn(bais);
     AddAttachmentToDoc addAtt = createMock(AddAttachmentToDoc.class);
     upc.inject_addAttachmentToDoc = addAtt;
     XWikiAttachment newAtt = new XWikiAttachment();
     newAtt.setFilename(cleanImgName);
     newAtt.setFilesize(123);
     newAtt.setDoc(destDoc);
     expect(addAtt.addAtachment(same(destDoc), /*eq(inArray)*/(byte[])anyObject(), eq(cleanImgName), same(getContext())
         )).andReturn(newAtt);
     expect(xwiki.clearName(eq(imgName), eq(false), eq(true), same(getContext()))
         ).andReturn(cleanImgName);
     expect(xwiki.getDocument(same(zipSrcDocRef), same(getContext()))).andReturn(srcDoc);
     expect(xwiki.getDocument(same(imgDestDocRef), same(getContext()))).andReturn(destDoc);
     replay(addAtt, att, unzip, xwiki);
     String resultImgName = upc.unzipFileToAttachment(zipSrcDocRef, filename, imgName, 
         imgDestDocRef);
     verify(addAtt, att, unzip, xwiki);
     assertEquals(cleanImgName, resultImgName);
   }
   
   @Test
   public void testIsZipFile_false() {
     assertFalse(upc.isZipFile(null));
     XWikiAttachment att = createMock(XWikiAttachment.class);
     expect(att.getMimeType(same(getContext()))).andReturn("123").anyTimes();
     replay(att);
     assertFalse(upc.isZipFile(att));
     verify(att);
   }
 
   @Test
   public void testIsZipFile_true() {
     XWikiAttachment att = createMock(XWikiAttachment.class);
     expect(att.getMimeType(same(getContext()))).andReturn(ImageLibStrings.MIME_ZIP);
     replay(att);
     assertTrue(upc.isZipFile(att));
     verify(att);
   }
 
   @Test
   public void testIsImgFile_false() {
    assertFalse(upc.isImgFile(null));
     assertFalse(upc.isImgFile(""));
     assertFalse(upc.isImgFile("123.jzpg"));
     assertFalse(upc.isImgFile("jpg"));
     assertFalse(upc.isImgFile("1.png.txt"));
   }
 
   @Test
   public void testIsImgFile_true() {
     assertTrue(upc.isImgFile("123.png"));
     assertTrue(upc.isImgFile("123.txt.jpg"));
   }
 }
