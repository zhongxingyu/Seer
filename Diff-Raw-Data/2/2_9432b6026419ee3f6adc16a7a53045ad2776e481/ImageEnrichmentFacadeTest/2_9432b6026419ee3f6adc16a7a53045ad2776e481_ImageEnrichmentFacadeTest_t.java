 package de.behrfriedapp.webshop.server.web;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author marcus
  */
 public class ImageEnrichmentFacadeTest {
 
 	private ImageEnrichmentFacade imageEnrichmentFacade;
 
 	@Before
 	public void setUp() throws Exception {
 		this.imageEnrichmentFacade = new ImageEnrichmentFacade(
 				new BingImageSearchUrlCreator(),
 				new DefaultHttpAccess(),
 				new BingImageSearchUrlExtractor(),
 				new MemoryImageDownloader()
 		);
 	}
 
 	@Test
 	public void testGetImageData() throws Exception {
		ImageDownloader.ImageData result = this.imageEnrichmentFacade.getImageData("Bier Durst");
 
 		Assert.assertNotNull(result);
 	}
 }
