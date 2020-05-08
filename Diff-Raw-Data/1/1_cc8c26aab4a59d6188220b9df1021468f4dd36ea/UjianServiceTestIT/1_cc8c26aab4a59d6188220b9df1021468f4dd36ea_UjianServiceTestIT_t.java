 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.school.onlinetest.service.impl;
 
 import com.artivisi.school.onlinetest.domain.Ujian;
 import com.artivisi.school.onlinetest.service.BelajarRestfulService;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author LILY
  */
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath*:com/artivisi/school/onlinetest/**/applicationContext.xml"})
 public class UjianServiceTestIT {
     @Autowired private BelajarRestfulService belajarRestfulService;
     
     @Test
     public void testFindAll(){
         Page<Ujian>hasil = belajarRestfulService.findAllUjians(new PageRequest(0, 10));
         assertEquals(new Integer(1), new Integer(hasil.getNumberOfElements()));
     }
 }
