 package org.otherobjects.cms.tools;
 
 import junit.framework.TestCase;
 
 import org.otherobjects.cms.model.CmsImage;
 import org.otherobjects.cms.model.CmsImageSize;
 
 public class CmsImageToolTest extends TestCase
 {
     public void testGetOriginal()
     {
         CmsImage i1 = new CmsImage();
         i1.setOriginalWidth(200L);
         i1.setOriginalHeight(100L);
         i1.setDescription("A picture of something");
         CmsImageTool imageTool = new CmsImageTool();
         CmsImageSize is1 = imageTool.getOriginal(i1);
         assertEquals(i1.getOriginalWidth().intValue(), is1.getWidth());
         assertEquals(i1.getOriginalHeight().intValue(), is1.getHeight());
         assertEquals(i1.getDescription(), is1.getDescription());
     }
 
     public void testGetSize()
     {
 //        CmsImageTool imageTool = new CmsImageTool();
 //        OoResourceLoader ooResourceLoader = new OoResourceLoader();
 //        OtherObjectsConfigurator otherObjectsConfigurator = new OtherObjectsConfigurator();
 //        otherObjectsConfigurator.setProperty("site.public.data.path", ".");
 //        ooResourceLoader.setResourceLoader(new DefaultResourceLoader());
 //        ooResourceLoader.setOtherObjectsConfigurator(otherObjectsConfigurator);
 //        imageTool.setOoResourceLoader(ooResourceLoader);
 //        CmsImage i1 = new CmsImage();
 //        i1.setOriginalWidth(200L);
 //        i1.setOriginalHeight(100L);
 //        i1.setOriginalFileName("test.jpg");
 //        i1.setDescription("A picture of something");
 //        CmsImageSize is1 = imageTool.getSize(i1, 100, 50, null);
 //        assertEquals(100, is1.getWidth());
 //        assertEquals(50, is1.getHeight());
 //        assertEquals(i1.getDescription(), is1.getDescription());
     }
 }
