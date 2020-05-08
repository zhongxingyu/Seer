 package com.celements.photo.service;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.Map;
 
 import org.apache.velocity.VelocityContext;
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.celements.navigation.NavigationClasses;
 import com.celements.photo.container.ImageDimensions;
 import com.celements.web.classcollections.OldCoreClasses;
 import com.celements.web.plugin.cmd.AttachmentURLCommand;
 import com.celements.web.service.IWebUtilsService;
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
 
   @SuppressWarnings("unchecked")
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
     String imgAttURL = "/download/ContentAttachment/FileBaseDoc/myImg.png";
     expect(attURLCmdMock.getAttachmentURL(eq(attFullName), eq("download"), same(context))
         ).andReturn(imgAttURL).once();
     imageService.webUtilsService = createMock(IWebUtilsService.class);
     DocumentReference attDocRef = new DocumentReference(getContext().getDatabase(), 
         "ContentAttachment", "FileBaseDoc");
     IWebUtilsService webUtils = imageService.webUtilsService;
     expect(webUtils.resolveDocumentReference(
         eq("ContentAttachment.FileBaseDoc"))).andReturn(attDocRef).once();
     XWikiDocument attDoc = new XWikiDocument(new DocumentReference("a", "b", "c"));
     expect(xwiki.getDocument(eq(attDocRef), same(context))).andReturn(attDoc).once();
     expect(webUtils.resolveDocumentReference(
         eq("Classes.PhotoMetainfoClass"))).andReturn(attDocRef).once();
     xwiki.saveDocument(same(slideDoc), eq("add default image slide content"), eq(true),
         same(context));
     expectLastCall().once();
     expect(webUtils.getWikiRef((DocumentReference)anyObject())
         ).andReturn(attDocRef.getWikiReference()).anyTimes();
     expect(webUtils.getDefaultLanguage()).andReturn("de").anyTimes();
     expect(webUtils.getDefaultLanguage((String)anyObject())).andReturn("de").anyTimes();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("gallerySpace"), eq(""),
         same(context))).andReturn("de").anyTimes();
     VelocityContext vcontext = new VelocityContext();
     context.put("vcontext", vcontext);
     DocumentReference imgImportContentRef = new DocumentReference(context.getDatabase(),
         "Templates", "ImageSlideImportContent");
     expect(webUtils.renderInheritableDocument(eq(imgImportContentRef), 
         eq(context.getLanguage()), eq("de"))).andReturn("content").once();
    expect(xwiki.getWebPreference(eq("cel_centralfilebase"), same(getContext()))
        ).andReturn("");
    expect(xwiki.exists(eq(attDocRef), same(getContext()))).andReturn(true);
     replayDefault();
     replay(webUtils);
     assertTrue("Expecting successful adding slide", imageService.addSlideFromTemplate(
         galleryDocRef, "Slide", attFullName));
     String expectedImgURL = imgAttURL + "?celwidth=" + maxWidth + "&celheight=" 
         + maxHeight;
     verifyDefault();
     verify(webUtils);
     assertEquals(expectedImgURL, vcontext.get("imageURL"));
     assertEquals(attFullName, vcontext.get("attFullName"));
     assertEquals(0, ((Map<String, String>)vcontext.get("metaTagMap")).size());
   }
 
   @Test
   public void testGetFixedAspectURL_isSquare_tarSquare() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(300, 300), 1, 1);
     assertEquals("Expecting no params if image matches target aspect ratio.", "", params);
   }
 
   @Test
   public void testGetFixedAspectURL_isPortrait_tarSquare() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(300, 400), 1, 1);
     assertTrue("Expecting no crop on left. [" + params + "]", 
         params.indexOf("cropX=0") >= 0);
     assertTrue("Expecting full width. [" + params + "]", 
         params.indexOf("cropW=300") >= 0);
     assertTrue("Expecting cropped border on top. [" + params + "]", 
         params.indexOf("cropY=50") >= 0);
     assertTrue("Expecting height equals full width. [" + params + "]", 
         params.indexOf("cropH=300") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_isLandscape_tarSquare() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(400, 300), 1, 1);
     assertTrue("Expecting cropped border on left. [" + params + "]", 
         params.indexOf("cropX=50") >= 0);
     assertTrue("Expecting width equals full height. [" + params + "]", 
         params.indexOf("cropW=300") >= 0);
     assertTrue("Expecting no crop on top. [" + params + "]", 
         params.indexOf("cropY=0") >= 0);
     assertTrue("Expecting full height. [" + params + "]", 
         params.indexOf("cropH=300") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_is3to4_tar3to4() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(300, 400), 3, 4);
     assertEquals("Expecting no params if image matches target aspect ratio.", "", params);
   }
 
   @Test
   public void testGetFixedAspectURL_isSquare_tar3to4() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(300, 300), 3, 4);
     assertTrue("Expecting cropped border on left. [" + params + "]", 
         params.indexOf("cropX=37") >= 0);
     assertTrue("Expecting width equals .75 * height. [" + params + "]", 
         params.indexOf("cropW=225") >= 0);
     assertTrue("Expecting no crop on top. [" + params + "]", 
         params.indexOf("cropY=0") >= 0);
     assertTrue("Expecting full height. [" + params + "]", 
         params.indexOf("cropH=300") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_isLandscape_tar3to4() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(400, 300), 3, 4);
     assertTrue("Expecting cropped border on left. [" + params + "]", 
         params.indexOf("cropX=87") >= 0);
     assertTrue("Expecting width equals .75 * height. [" + params + "]", 
         params.indexOf("cropW=225") >= 0);
     assertTrue("Expecting no crop on top. [" + params + "]", 
         params.indexOf("cropY=0") >= 0);
     assertTrue("Expecting full height. [" + params + "]", 
         params.indexOf("cropH=300") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_isPortraitWide_tar3to4() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(350, 400), 3, 4);
     assertTrue("Expecting cropped border on left. [" + params + "]", 
         params.indexOf("cropX=25") >= 0);
     assertTrue("Expecting width equals .75 * height. [" + params + "]", 
         params.indexOf("cropW=300") >= 0);
     assertTrue("Expecting no crop on top. [" + params + "]", 
         params.indexOf("cropY=0") >= 0);
     assertTrue("Expecting full height. [" + params + "]", 
         params.indexOf("cropH=400") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_isPortraitSmall_tar3to4() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(240, 400), 3, 4);
     assertTrue("Expecting no crop on left. [" + params + "]", 
         params.indexOf("cropX=0") >= 0);
     assertTrue("Expecting full width. [" + params + "]", 
         params.indexOf("cropW=240") >= 0);
     assertTrue("Expecting cropped border on top. [" + params + "]", 
         params.indexOf("cropY=40") >= 0);
     assertTrue("Expecting width * 4/3. [" + params + "]", 
         params.indexOf("cropH=320") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_is4to3_tar4to3() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(400, 300), 4, 3);
     assertEquals("Expecting no params if image matches target aspect ratio.", "", params);
   }
 
   @Test
   public void testGetFixedAspectURL_isSquare_tar4to3() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(300, 300), 4, 3);
     assertTrue("Expecting no crop on left. [" + params + "]", 
         params.indexOf("cropX=0") >= 0);
     assertTrue("Expecting full width. [" + params + "]", 
         params.indexOf("cropW=300") >= 0);
     assertTrue("Expecting cropped border on top. [" + params + "]", 
         params.indexOf("cropY=37") >= 0);
     assertTrue("Expecting height equals .75 * width. [" + params + "]", 
         params.indexOf("cropH=225") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_isPortrait_tar4to3() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(300, 400), 4, 3);
     assertTrue("Expecting no crop on left. [" + params + "]", 
         params.indexOf("cropX=0") >= 0);
     assertTrue("Expecting full width. [" + params + "]", 
         params.indexOf("cropW=300") >= 0);
     assertTrue("Expecting cropped border on top. [" + params + "]", 
         params.indexOf("cropY=87") >= 0);
     assertTrue("Expecting height equals .75 * width. [" + params + "]", 
         params.indexOf("cropH=225") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_isLandscapeHigh_tar4to3() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(400, 350), 4, 3);
     assertTrue("Expecting no crop on left. [" + params + "]", 
         params.indexOf("cropX=0") >= 0);
     assertTrue("Expecting full width. [" + params + "]", 
         params.indexOf("cropW=400") >= 0);
     assertTrue("Expecting cropped border on top. [" + params + "]", 
         params.indexOf("cropY=25") >= 0);
     assertTrue("Expecting height equals .75 * width. [" + params + "]", 
         params.indexOf("cropH=300") >= 0);
   }
 
   @Test
   public void testGetFixedAspectURL_isLandscapeLow_tar4to3() {
     String params = imageService.getFixedAspectURL(new ImageDimensions(400, 240), 4, 3);
     assertTrue("Expecting cropped border on left. [" + params + "]", 
         params.indexOf("cropX=40") >= 0);
     assertTrue("Expecting width * 4/3. [" + params + "]", 
         params.indexOf("cropW=320") >= 0);
     assertTrue("Expecting no crop on top. [" + params + "]", 
         params.indexOf("cropY=0") >= 0);
     assertTrue("Expecting full height. [" + params + "]", 
         params.indexOf("cropH=240") >= 0);
   }
 }
