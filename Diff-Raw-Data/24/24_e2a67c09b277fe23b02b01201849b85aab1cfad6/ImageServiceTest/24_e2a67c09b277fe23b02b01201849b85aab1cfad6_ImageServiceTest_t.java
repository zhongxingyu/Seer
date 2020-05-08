 package com.celements.photo.service;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.celements.navigation.NavigationClasses;
 import com.celements.web.classcollections.OldCoreClasses;
 import com.celements.web.plugin.cmd.AttachmentURLCommand;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.user.api.XWikiRightService;
 
 public class ImageServiceTest extends AbstractBridgedComponentTestCase {
 
   private XWikiContext context;
   private ImageService imageService;
   private XWiki xwiki;
   private XWikiRightService rightServiceMock;
 
   @Before
   public void setUp_ImageServiceTest() throws Exception {
     context = getContext();
     xwiki = getWikiMock();
     rightServiceMock = createMockAndAddToDefault(XWikiRightService.class);
     expect(xwiki.getRightService()).andReturn(rightServiceMock).anyTimes();
     imageService = (ImageService) getComponentManager().lookup(IImageService.class);
   }
 
   @Test
   public void testGetPhotoAlbumNavObject() throws Exception {
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "galleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     BaseObject expectedPhotoAlbumNavObj = new BaseObject();
     expectedPhotoAlbumNavObj.setXClassReference(new NavigationClasses(
         ).getNavigationConfigClassRef(context.getDatabase()));
     galleryDoc.addXObject(expectedPhotoAlbumNavObj);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).once();
     replayDefault();
     BaseObject photoAlbumNavObj = imageService.getPhotoAlbumNavObject(galleryDocRef);
     assertNotNull(photoAlbumNavObj);
     assertSame(expectedPhotoAlbumNavObj, photoAlbumNavObj);
     verifyDefault();
   }
 
   @Test
   public void testGetPhotoAlbumNavObject_noObject() throws Exception {
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "noGalleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).once();
     replayDefault();
     try {
       imageService.getPhotoAlbumNavObject(galleryDocRef);
       fail("expecting NoGalleryDocumentException");
     } catch (NoGalleryDocumentException exp) {
       //expected
     }
     verifyDefault();
   }
 
   @Test
   public void testGetPhotoAlbumSpaceRef_noGalleryDoc() throws Exception {
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "noGalleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).once();
     replayDefault();
     try {
       imageService.getPhotoAlbumSpaceRef(galleryDocRef);
     } catch (NoGalleryDocumentException exp) {
       //expected
     }
     verifyDefault();
   }
 
   @Test
   public void testGetPhotoAlbumSpaceRef_galleryDoc() throws Exception {
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "galleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     BaseObject expectedPhotoAlbumNavObj = new BaseObject();
     expectedPhotoAlbumNavObj.setXClassReference(new NavigationClasses(
         ).getNavigationConfigClassRef(context.getDatabase()));
     String gallerySpaceName = "gallerySpace";
     expectedPhotoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
     galleryDoc.addXObject(expectedPhotoAlbumNavObj);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).once();
     replayDefault();
     SpaceReference expectedSpaceRef = new SpaceReference("gallerySpace",
         (WikiReference)galleryDocRef.getLastSpaceReference().getParent());
     assertEquals(expectedSpaceRef, imageService.getPhotoAlbumSpaceRef(galleryDocRef));
     verifyDefault();
   }
 
   @Test
   public void testCheckAddSlideRights_noObjects_noGalleryDoc() throws Exception {
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "noGalleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).once();
     replayDefault();
     assertFalse("Expecting no addSlide rights if document is no gallery document.",
         imageService.checkAddSlideRights(galleryDocRef));
     verifyDefault();
   }
 
   @Test
   public void testGetImageSlideTemplateRef_local() throws Exception {
     DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
         "ImageGalleryTemplates", "NewImageGallerySlide");
     expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(true).once();
     replayDefault();
     assertEquals(localTemplateRef, imageService.getImageSlideTemplateRef());
     verifyDefault();
   }
 
   @Test
   public void testGetImageSlideTemplateRef_central() throws Exception {
     DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
         "ImageGalleryTemplates", "NewImageGallerySlide");
     DocumentReference centralTemplateRef = new DocumentReference("celements2web",
         "ImageGalleryTemplates", "NewImageGallerySlide");
     expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(false).once();
     replayDefault();
     assertEquals(centralTemplateRef, imageService.getImageSlideTemplateRef());
     verifyDefault();
   }
 
   @Test
   public void testCheckAddSlideRights_yes() throws Exception {
     String editorUser = "XWiki.myEditor";
     context.setUser(editorUser);
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "galleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     BaseObject photoAlbumNavObj = new BaseObject();
     photoAlbumNavObj.setXClassReference(new NavigationClasses(
         ).getNavigationConfigClassRef(context.getDatabase()));
     String gallerySpaceName = "gallerySpace";
     photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
     galleryDoc.addXObject(photoAlbumNavObj);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).once();
     DocumentReference testSlideDocRef = new DocumentReference(context.getDatabase(),
         gallerySpaceName, "Testname1");
     XWikiDocument testSlideDocMock = createMockAndAddToDefault(XWikiDocument.class);
     expect(xwiki.getDocument(eq(testSlideDocRef), same(context))).andReturn(
         testSlideDocMock).once();
     expect(xwiki.exists(eq(testSlideDocRef), same(context))).andReturn(false).once();
     expect(testSlideDocMock.getLock(same(context))).andReturn(null).once();
     expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(editorUser),
         eq("xwikidb:gallerySpace.Testname1"), same(context))).andReturn(true).once();
     replayDefault();
     assertTrue("Expecting addSlide rights if 'edit' rights on space available",
         imageService.checkAddSlideRights(galleryDocRef));
     verifyDefault();
   }
 
   @Test
   public void testCheckAddSlideRights_no() throws Exception {
     String editorUser = "XWiki.myNoEditor";
     context.setUser(editorUser);
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "galleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     BaseObject photoAlbumNavObj = new BaseObject();
     photoAlbumNavObj.setXClassReference(new NavigationClasses(
         ).getNavigationConfigClassRef(context.getDatabase()));
     String gallerySpaceName = "gallerySpace";
     photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
     galleryDoc.addXObject(photoAlbumNavObj);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).once();
     DocumentReference testSlideDocRef = new DocumentReference(context.getDatabase(),
         gallerySpaceName, "Testname1");
     XWikiDocument testSlideDocMock = createMockAndAddToDefault(XWikiDocument.class);
     expect(xwiki.getDocument(eq(testSlideDocRef), same(context))).andReturn(
         testSlideDocMock).once();
     expect(xwiki.exists(eq(testSlideDocRef), same(context))).andReturn(false).once();
     expect(testSlideDocMock.getLock(same(context))).andReturn(null).once();
     expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(editorUser),
         eq("xwikidb:gallerySpace.Testname1"), same(context))).andReturn(false).once();
     replayDefault();
     assertFalse("Expecting no addSlide rights if no 'edit' rights on space available",
         imageService.checkAddSlideRights(galleryDocRef));
     verifyDefault();
   }
 
   @Test
   public void testAddSlideFromTemplate() throws Exception {
     String editorUser = "XWiki.myEditor";
     context.setUser(editorUser);
     AttachmentURLCommand attURLCmdMock = createMockAndAddToDefault(
         AttachmentURLCommand.class);
     imageService.attURLCmd = attURLCmdMock;
     DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(),
         "mySpace", "galleryDoc");
     XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
     BaseObject photoAlbumNavObj = new BaseObject();
     photoAlbumNavObj.setXClassReference(new NavigationClasses(
         ).getNavigationConfigClassRef(context.getDatabase()));
     String gallerySpaceName = "gallerySpace";
     photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
     galleryDoc.addXObject(photoAlbumNavObj);
     BaseObject photoAlbumObj = new BaseObject();
     photoAlbumObj.setXClassReference(new OldCoreClasses().getPhotoAlbumClassRef(
         context.getDatabase()));
     int maxWidth = 800;
     int maxHeight = 800;
     photoAlbumObj.setIntValue("height2", maxHeight);
     photoAlbumObj.setIntValue("photoWidth", maxWidth);
     galleryDoc.addXObject(photoAlbumObj);
     expect(xwiki.getDocument(eq(galleryDocRef), same(context))).andReturn(galleryDoc
         ).atLeastOnce();
     DocumentReference slideDocRef = new DocumentReference(context.getDatabase(),
         gallerySpaceName, "Slide1");
     XWikiDocument slideDocMock = createMockAndAddToDefault(XWikiDocument.class);
     expect(xwiki.getDocument(eq(slideDocRef), same(context))).andReturn(
         slideDocMock).once();
     expect(xwiki.exists(eq(slideDocRef), same(context))).andReturn(false).once();
     expect(slideDocMock.getLock(same(context))).andReturn(null).once();
     XWikiDocument slideDoc = new XWikiDocument(slideDocRef);
     expect(xwiki.getDocument(eq(slideDocRef), same(context))).andReturn(slideDoc).once();
     DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
         "ImageGalleryTemplates", "NewImageGallerySlide");
     expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(true).once();
     expect(xwiki.copyDocument(eq(localTemplateRef), eq(slideDocRef), eq(true),
         same(context))).andReturn(true).once();
     String attFullName = "ContentAttachment.FileBaseDoc;myImg.png";
     String imgAttURL = "/file/ContentAttachment/FileBaseDoc/myImg.png";
     expect(attURLCmdMock.getAttachmentURL(eq(attFullName), same(context))).andReturn(
         imgAttURL).once();
     xwiki.saveDocument(same(slideDoc), eq("add default image slide content"), eq(true),
         same(context));
     expectLastCall().once();
    expect(xwiki.getSpacePreference(eq("default_language"), eq("gallerySpace"), eq(""),
        same(context))).andReturn("de").anyTimes();
     replayDefault();
     assertTrue("Expecting successful adding slide", imageService.addSlideFromTemplate(
         galleryDocRef, "Slide", attFullName));
     assertTrue("expecting img tag in content", slideDoc.getContent().matches(
         "<img [^>]*/>"));
     String expectedImgURL = " src=\"" + imgAttURL + "?celwidth=" + maxWidth
         + "&celheight=" + maxHeight + "\"";
     assertTrue("expecting img src [" + expectedImgURL
         + "] with resizing to max dimensions from gallery doc but got ["
         + slideDoc.getContent() + "].", slideDoc.getContent().contains(expectedImgURL));
     verifyDefault();
   }
 
 }
