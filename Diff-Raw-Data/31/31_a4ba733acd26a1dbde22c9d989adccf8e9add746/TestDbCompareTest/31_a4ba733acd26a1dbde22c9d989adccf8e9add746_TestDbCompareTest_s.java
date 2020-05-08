 package com.seitenbau.stu.database.util;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.Test;
 
 import com.seitenbau.stu.database.util.DbCompare;
 
 public class TestDbCompareTest
 {
   @Test
   public void test()
   {
    DbCompare.jetztNurDatum();
   }
   
   @Test
   public void testDateCompareEqual() throws Exception {
     DbCompare.DateCompareImpl sut = new DbCompare.DateCompareImpl();
     Date expectedDate = new Date();
     Date actualValue = expectedDate;
     assertEquals(0, sut.compare(expectedDate, actualValue));
   }
   
   @Test
   public void testDateCompareGreater() throws Exception {
       DbCompare.DateCompareImpl sut = new DbCompare.DateCompareImpl();
       Date expectedDate = new Date();
       Date actualValue = new Date(expectedDate.getTime() + 16000);
       assertEquals(1, sut.compare(expectedDate, actualValue));
   }
   
   @Test
   public void testDateCompareLower() throws Exception {
       DbCompare.DateCompareImpl sut = new DbCompare.DateCompareImpl();
       Date expectedDate = new Date();
       Date actualValue = new Date(expectedDate.getTime() - 16000);
       assertEquals(-1, sut.compare(expectedDate, actualValue));
   }
 }
