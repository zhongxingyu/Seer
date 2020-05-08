 package com.rightmove.es.service;
 
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static junit.framework.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"classpath:/springContext-test.xml",
                         "classpath:/applicationContext.xml"})
 public class RegionServiceTest {
 
     @Autowired
     private RegionService regionService;
 
     @Test
     public void loadAndIndexRegions() throws Exception{
         regionService.loadAndIndexRegions();
 
         assertEquals("Expect 10 regions to be indexed", 10l, regionService.listAll());
     }
 }
