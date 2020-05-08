 package de.eonas.website.vote;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration()
 public class DemoDataTest  {
     @Autowired
     Dao dao;
 
     @Test
     public void checkPreload() {
         // die postconstructmethode triggert den test
     }
 }
