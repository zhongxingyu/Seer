 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.entities.dao;
 
 import com.team33.entities.VideoInfo;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Caleb
  */
 public class AdminDaoImplTest {
 
     private AdminDao adminDao;
     
     public AdminDaoImplTest() {
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
     
     public void setAdminDao(AdminDao adminDao){
         this.adminDao = adminDao;
     }
 
     /**
      * Test of addVideoInfo method, of class AdminDaoImpl.
      */
     @Test
     @Transactional
     public void testAddVideoInfo() {
         System.out.println("addVideoInfo(VideoInfo) -> [DataAccessException]");
         
         try{
             this.adminDao.addVideoInfo(null);
             fail("Error was not thrown");
         }catch(DataAccessException e){
         }
         
         try{
             this.adminDao.addVideoInfo(new VideoInfo(-1));
             fail("Error was not thrown");
         }catch(DataAccessException e){
         }
         
         try{
             this.adminDao.addVideoInfo(new VideoInfo());
         }catch(DataAccessException e){
             fail("Error should not be thrown");
         }
     }
 
     /**
      * Test of getVideoInfo method, of class AdminDaoImpl.
      */
     @Test
     public void testGetVideoInfo() {
         System.out.println("getVideoInfo(Integer) -> [DataAccessException]");
         try{
             this.adminDao.getVideoInfo(-1);
             fail("Error was not thrown");
         }catch(DataAccessException e){
         }
         
         try{
             this.adminDao.getVideoInfo(9999);
             fail("Error was not thrown");
         }catch(DataAccessException e){
         }
         
         try{
             VideoInfo info = this.adminDao.getVideoInfo(0);
             assertNotNull(info);
             assertEquals(0, info.getId());
         }catch(DataAccessException e){
             fail("Error should not be thrown");
         }
     }
 
     /**
      * Test of removeVideoInfo method, of class AdminDaoImpl.
      */
     @Test
     public void testRemoveVideoInfo() {
         System.out.println("removeVideoInfo(Integer) -> [DataAccessException]");
         
         try{
             this.adminDao.removeVideoInfo(-1);
             fail("Error was not thrown");
         }catch(DataAccessException e){
         }
         
         try{
             this.adminDao.removeVideoInfo(9999);
             fail("Error was not thrown");
         }catch(DataAccessException e){
         }
         
         try{
             this.adminDao.removeVideoInfo(0);
         }catch(DataAccessException e){
             fail("Error should not be thrown");
         }
     }
 }
