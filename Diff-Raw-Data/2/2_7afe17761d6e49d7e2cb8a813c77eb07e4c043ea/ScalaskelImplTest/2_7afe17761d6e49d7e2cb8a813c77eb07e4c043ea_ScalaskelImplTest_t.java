 package org.fred.codestory.service;
 
 import org.fred.codestory.model.scalaskel.Change;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.List;
 
 public class ScalaskelImplTest {
 
 
     @Test
     public void testFoundBestChange() {
         // given
         ScalaskelImpl scalaskel = new ScalaskelImpl();
 
         // when
         Change moneyChange = scalaskel.foundBestChange(100, null);
 
         // then
         Assert.assertEquals("Incorrect Baz", 4, moneyChange.getBaz());
        Assert.assertEquals("Incorrect Qix", 1, moneyChange.getQix());
         Assert.assertEquals("Incorrect Bar", 0, moneyChange.getBar());
         Assert.assertEquals("Incorrect Foo", 5, moneyChange.getFoo());
     }
 
     @Test
     public void searchAllPossibilities() {
         // given
         ScalaskelImpl scalaskel = new ScalaskelImpl();
 
         // when
         List<Change> allChanges = scalaskel.searchAllChange(100);
 
         // then
         Assert.assertEquals(allChanges.size(), 75);
     }
 }
