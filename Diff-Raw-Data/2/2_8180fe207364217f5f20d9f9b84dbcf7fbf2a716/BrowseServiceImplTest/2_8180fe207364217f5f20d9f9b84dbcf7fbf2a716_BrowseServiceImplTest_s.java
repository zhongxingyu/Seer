 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.services;
 
 import com.team33.entities.Genre;
 import com.team33.entities.ScreenRating;
 import com.team33.entities.VideoInfo;
 import com.team33.services.exception.DataAccessException;
 import java.util.List;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  *
  * @author Caleb
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/test/service/service-test.xml"})
 public class BrowseServiceImplTest {
     
     private BrowseServiceImpl browseServiceImpl;
     
     public BrowseServiceImplTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
         browseServiceImpl = new BrowseServiceImpl();
        browseServiceImpl.setBrowseDaoImpl(new BrowseDaoImplTestStub());
     }
     
     @After
     public void tearDown() {
     }
 
     public void testDisplayAllVideoContent(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.displayAllVideoContent();
         }catch(DataAccessException e){
             fail("DataAccessException should not be thrown");
         }
         assertNotNull(info);
         assertFalse(info.isEmpty());
     }
     
     public void testDisplayVideoDetails_NegId(){
         VideoInfo info = null;
         try{
             info = this.browseServiceImpl.displayVideoDetails(-1);
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testDisplayVideoDetails_InvalidId(){
         VideoInfo info = null;
         try{
             info = this.browseServiceImpl.displayVideoDetails(9999);
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testDisplayVideoDetails_ValidId(){
         VideoInfo info = null;
         try{
             info = this.browseServiceImpl.displayVideoDetails(0);
         }catch(DataAccessException e){
             fail("DataAccessException should not be thrown");
         }
         assertNotNull(info);
         assertEquals(info.getId().intValue(), 0);
     }
     
     public void testSearchVideos_Genre_NullGenre(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos((Genre)null);
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testSearchVideos_Genre_NegGenre(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos(new Genre(-1));
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testSearchVideos_Genre_InvalidGenre(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos(new Genre(9999));
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testSearchVideos_Genre_ValidGenre(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos(new Genre(0));
         }catch(DataAccessException e){
             fail("DataAccessException should not be thrown");
         }
     }
     
     public void testSearchVideos_ScreenRating_NullRating(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos((ScreenRating)null);
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testSearchVideos_ScreenRating_NegRating(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos(new ScreenRating(-1));
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testSearchVideos_ScreenRating_InvalidRating(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos(new ScreenRating(9999));
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testSearchVideos_ScreenRating_ValidRating(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos(new ScreenRating(0));
         }catch(DataAccessException e){
             fail("DataAccessException should not be thrown");
         }
     }
     
     public void testSearchVideos_String_NullTitle(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos((String)null);
             fail("DataAccessException should be thrown");
         }catch(DataAccessException e){
         }
     }
     
     public void testSearchVideos_String_BlankTitle(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos("");
         }catch(DataAccessException e){
             fail("DataAccessException should not be thrown");
         }
         assertNotNull(info);
         assertEquals(info.size(), 0);
     }
     
     public void testSearchVideos_String_InvalidTitle(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos("People");
         }catch(DataAccessException e){
             fail("DataAccessException should not be thrown");
         }
         assertNotNull(info);
         assertEquals(info.size(), 0);
     }
     
     public void testSearchVideos_String_ValidTitle(){
         List<VideoInfo> info = null;
         try{
             info = this.browseServiceImpl.searchVideos("Hello");
         }catch(DataAccessException e){
             fail("DataAccessException should not be thrown");
         }
         assertNotNull(info);
         assertTrue(info.size() > 0);
     }
 }
