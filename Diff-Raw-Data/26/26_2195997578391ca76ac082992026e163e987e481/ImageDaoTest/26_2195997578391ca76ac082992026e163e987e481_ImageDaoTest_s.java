 package com.codimage.image;
 
 import com.codimage.config.AppConfig;
 import com.codimage.test.FixtureFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.AnnotationConfigContextLoader;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 /**
  * Tests for {@link ImageJdbcDao}
  *
  * @author Ruslan Khmelyuk
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = {AppConfig.class}, loader = AnnotationConfigContextLoader.class)
 public class ImageDaoTest {
 
     @Autowired private ImageDao imageDao;
 
     @Before
     public void setUp() {
         imageDao.deleteAll();
     }
 
     @Test
     public void findRandomImageReturnsImage() {
         final Image image = FixtureFactory.createImage();
         imageDao.create(image);
 
         final Image foundImage = imageDao.findRandomImage();
         assertThat(foundImage, not(nullValue()));
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void findRandomImageReturnsRandomImage() {
         imageDao.create(FixtureFactory.createImage());
         imageDao.create(FixtureFactory.createImage());
         imageDao.create(FixtureFactory.createImage());
         imageDao.create(FixtureFactory.createImage());
         imageDao.create(FixtureFactory.createImage());
 
         final Image returnedImage1 = imageDao.findRandomImage();
         final Image returnedImage2 = imageDao.findRandomImage();
         final Image returnedImage3 = imageDao.findRandomImage();
 
         assertThat(returnedImage1, not(allOf(equalTo(returnedImage2), equalTo(returnedImage3))));
     }
 
     // -- findNextImage()
 
     @Test
     public void findNextImageReturnsTheNextImage() {
         final Image image1 = FixtureFactory.createImage();
         final Image image2 = FixtureFactory.createImage();
         final Image image3 = FixtureFactory.createImage();
 
         imageDao.create(image1);
         imageDao.create(image2);
         imageDao.create(image3);
 
         final Image nextImage = imageDao.findNextImage(image1.getId());
 
         assertThat(nextImage.getId(), is(image2.getId()));
     }
 
     @Test
     public void findNextImageForLastImageReturnsTheFirstImage() {
         final Image image1 = FixtureFactory.createImage();
         final Image image2 = FixtureFactory.createImage();
         final Image image3 = FixtureFactory.createImage();
 
         imageDao.create(image1);
         imageDao.create(image2);
         imageDao.create(image3);
 
         final Image nextImage = imageDao.findNextImage(image3.getId());
 
         assertThat(nextImage.getId(), is(image1.getId()));
     }
 
     // -- findPrevImage()
 
     @Test
     public void findPrevImageReturnsThePrevImage() {
         final Image image1 = FixtureFactory.createImage();
         final Image image2 = FixtureFactory.createImage();
         final Image image3 = FixtureFactory.createImage();
 
         imageDao.create(image1);
         imageDao.create(image2);
         imageDao.create(image3);
 
        final Image nextImage = imageDao.findPrevImage(image2.getId());
 
        assertThat(nextImage.getId(), is(image1.getId()));
     }
 
     @Test
     public void findPrevImageForFirstImageReturnsTheLastImage() {
         final Image image1 = FixtureFactory.createImage();
         final Image image2 = FixtureFactory.createImage();
         final Image image3 = FixtureFactory.createImage();
 
         imageDao.create(image1);
         imageDao.create(image2);
         imageDao.create(image3);
 
        final Image nextImage = imageDao.findPrevImage(image1.getId());
 
        assertThat(nextImage.getId(), is(image3.getId()));
     }
 }
