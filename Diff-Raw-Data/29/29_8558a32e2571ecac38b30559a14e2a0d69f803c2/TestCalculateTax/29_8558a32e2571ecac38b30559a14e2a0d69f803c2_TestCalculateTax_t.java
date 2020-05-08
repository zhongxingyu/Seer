 package test;
 import main.Item;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Created by IntelliJ IDEA.
  * User: rajashre
  * Date: 7/7/11
  * Time: 3:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TestCalculateTax {
     @Test
     public void costOfOneBookWithPriceTenIsTen() throws Exception
     {
         assertEquals(10.0,calculateTotalCostOfItem("book",1,10.0));
     }
 
     @Test
     public void costOfBookWithPriceFifteenIsFifteen() throws Exception
     {
         Assert.assertEquals(15.0,calculateTotalCostOfItem("book",1,15.0) );
     }
 
     @Test
     public void costOfTwoBooksWithPriceFifteenIsThirty() throws Exception
     {
         Assert.assertEquals(30.0, calculateTotalCostOfItem("book", 2, 15.0));
     }
 
     @Test
     public void costOfBookWithPriceTwelvePointFortyNineIsThirty() throws Exception
     {
         Assert.assertEquals(12.49, calculateTotalCostOfItem("book",1,12.49));
     }
 
     @Test
     public void costOfChocolateWithPricePointEightyFiveIsPointEightyFive() throws Exception
     {
         assertEquals(0.85,calculateTotalCostOfItem("chocolate",1,0.85));
     }
 
     @Test
     public void costOfMusicCdWithPriceFourteenPointNinetyNineIsSixteenPointFortyNine() throws Exception
     {
         assertEquals(16.49,calculateTotalCostOfItem("music_CD",1,14.99));
     }
 
    @Test
    public void costOfPerfumeWithPriceEighteenPointNinetyNineIsTwentyPointEightNine() throws Exception
    {
        assertEquals(20.89,calculateTotalCostOfItem("perfume",1,18.99));

    }
 
     @Test
     public void costOfHeadachePillsWithPriceNinePointSeventyFiveIsNinePointSeventyFive() throws Exception
     {
         assertEquals(9.75,calculateTotalCostOfItem("headache_pills",1,9.75));
 
     }
 
     @Test
     public void costOfImportedChocolateBarWithPriceTenIsTenPointFive() throws Exception
     {
         assertEquals(10.50,calculateTotalCostOfItem("imported_chocolate_bar",1,10.00));
 
     }
 
     @Test
     public void costOfImportedMusicCDWithPriceHundredIsHundredAndFifteen() throws Exception
     {
         assertEquals(115.0,calculateTotalCostOfItem("imported_musicCD",1,100.00));
 
     }
 
     @Test
     public void costOfTwoImportedMusicCDsWithPriceHundredIsTwuHundredAndThirty() throws Exception
     {
         assertEquals(230.0,calculateTotalCostOfItem("imported_musicCD",2,100.00));
 
     }
 
     @Test
     public void costOfTwoImportedBooksWithPriceHundredIsTwuHundredAndTen() throws Exception
     {
         assertEquals(210.0,calculateTotalCostOfItem("imported_books",2,100.00));
 
     }
 
     private double calculateTotalCostOfItem(String book, int quantity, double price)
     {
         return new Item().calculateCostAfterTax(book, quantity, price);
     }
 }
