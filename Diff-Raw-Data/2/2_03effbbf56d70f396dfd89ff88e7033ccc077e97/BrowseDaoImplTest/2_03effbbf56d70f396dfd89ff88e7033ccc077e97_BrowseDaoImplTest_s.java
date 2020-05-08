 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.entities.dao;
 
 import com.team33.entities.Genre;
 import com.team33.entities.ScreenRating;
 import com.team33.entities.VideoInfo;
 import com.team33.services.exception.DataAccessException;
 import java.util.List;
 import org.junit.After;
 import org.junit.AfterClass;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Caleb
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/test/dao/dao-test.xml"})
 @TransactionConfiguration(transactionManager = "transactionManager")
 @Transactional
 public class BrowseDaoImplTest {
     
     @Autowired
     private BrowseDaoImpl browseDao;
     
     public BrowseDaoImplTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
     
     public void setBrowseDao(BrowseDaoImpl browseDao) {
         this.browseDao = browseDao;
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_ScreenRating_InvalidRating() {
         // Invalid Screen rating
         ScreenRating testRating = new ScreenRating();
         testRating.setId(9999);
         List<VideoInfo> videos = this.browseDao.searchVideos(testRating);
         
         assertNotNull(videos);
         assertEquals(videos.size(), 0);
         System.out.println("testSearchVideos_ScreenRating_InvalidRating() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_ScreenRating_NullRating() {
         // Null Screen Rating
         try {
             List<VideoInfo> videos = this.browseDao.searchVideos((ScreenRating) null);
             fail("Exception was not thrown");
         } catch (DataAccessException e) {
         }
         System.out.println("testSearchVideos_ScreenRating_NullRating() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_ScreenRating_ValidRating() {
         ScreenRating testRating = new ScreenRating();
         // Valid Screen rating
         testRating.setId(0);
         List<VideoInfo> videos = this.browseDao.searchVideos(testRating);
         
         assertNotNull(videos);
         assertTrue(videos.size() > 0);
 
         // TODO Need to check if correct videos appear
         System.out.println("testSearchVideos_ScreenRating_ValidRating() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_String_NullName() {
         try {
             List<VideoInfo> videos = this.browseDao.searchVideos((String) null);
             fail("Exception was not thrown");
         } catch (DataAccessException e) {
         }
         System.out.println("testSearchVideos_String_NullName() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_String_BlankName() {
         // Blank video title
         List<VideoInfo> videos = this.browseDao.searchVideos("");
         assertNotNull(videos);
         assertEquals(videos.size(), 0);
         System.out.println("testSearchVideos_String_BlankName() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_String_InvalidName() {
         // Invalid video title
        List<VideoInfo> videos = this.browseDao.searchVideos("No Going to Find Something");
         assertNotNull(videos);
         assertEquals(videos.size(), 0);
         System.out.println("testSearchVideos_String_InvalidName() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_String_ValidSingleName() {
         // Valid video title
         List<VideoInfo> videos = this.browseDao.searchVideos("Single Title");
         assertNotNull(videos);
         assertEquals(videos.size(), 1);
         VideoInfo testVideoInfo = videos.get(0);
         
         assertNotNull(testVideoInfo);
         System.out.println("testSearchVideos_String_ValidSingleName() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_String_ValidMultipleName() {
         // TODO test for video retrieved
         List<VideoInfo> videos = this.browseDao.searchVideos("Multiple Title");
         assertNotNull(videos);
         assertEquals(videos.size(), 2);
         assertNotNull(videos.get(0));
         assertNotNull(videos.get(1));
 
         // TODO test for videos retrieved
         System.out.println("testSearchVideos_String_ValidMultipleName() passed");
     }
 
     @Test
     @Rollback(true)
     public void testDisplayAllVideoContent_NotNull() {
         assertNotNull(this.browseDao.displayAllVideoContent());
         System.out.println("testDisplayAllVideoContent_NotNull() passed");
     }
     
     @Test
     @Rollback(true)
     public void testDisplayAllVideoContent_NotNegSize() {
         assertTrue(this.browseDao.displayAllVideoContent().size() >= 0);
         System.out.println("testDisplayAllVideoContent_NotNegSize() passed");
     }
     
     @Test
     @Rollback(true)
     public void testDisplayVideoDetails_NegId() {
         try {
             this.browseDao.displayVideoDetails(-1);
             fail("Exception was not thrown");
         } catch (DataAccessException e) {
         }
         System.out.println("testDisplayVideoDetails_NegId() passed");
     }
     
     @Test
     @Rollback(true)
     public void testDisplayVideoDetails_InvalidId() {
         try {
             this.browseDao.displayVideoDetails(9999);
             fail("Exception was not thrown");
         } catch (DataAccessException e) {
         }
         System.out.println("testDisplayVideoDetails_InvalidId() passed");
     }
     
     @Test
     @Rollback(true)
     public void testDisplayVideoDetails_ValidId() {
         VideoInfo testInfo = null;
         try {
             testInfo = this.browseDao.displayVideoDetails(0);
         } catch (DataAccessException e) {
             fail("Exception was thrown");
         }
         assertNotNull(testInfo);
         assertEquals(testInfo.getId().intValue(), 0);
         
         // TODO more checks needed
         System.out.println("testDisplayVideoDetails_ValidId() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_Genre_NullGenre() {
         try {
             List<VideoInfo> videos = this.browseDao.searchVideos((Genre) null);
             fail("Exception was not thrown");
         } catch (DataAccessException e) {
         }
         System.out.println("testSearchVideos_Genre_NullGenre() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_Genre_InvalidGenre() {
         Genre g = new Genre(-1, null);
         List<VideoInfo> videos = this.browseDao.searchVideos(g);
         assertNotNull(videos);
         assertEquals(videos.size(), 0);
         System.out.println("testSearchVideos_Genre_InvalidGenre() passed");
     }
     
     @Test
     @Rollback(true)
     public void testSearchVideos_Genre_ValidGenre() {
         Genre g = new Genre(0, "Comedy");
         List<VideoInfo> videos = this.browseDao.searchVideos(g);
         assertNotNull(videos);
         assertTrue(videos.size() > 0);
         VideoInfo testVideoInfo = videos.get(0);
         assertNotNull(testVideoInfo);
         assertEquals(testVideoInfo.getGenreid(), g.getId().intValue());
         // TODO test for videos retrieved
         System.out.println("testSearchVideos_Genre_ValidGenre() passed");
     }    
 }
