 package com.shopservice;
 
 import org.junit.Assert;
 import org.junit.Test;
 
import javax.xml.bind.JAXBException;
 import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: neuser50
  * Date: 26.08.13
  * Time: 13:08
  * To change this template use File | Settings | File Templates.
  */
 public class PriceListServiceTest extends Assert {
 
     @Test
     public void testGetExistPriceList() throws Exception {
         PriceListService priceListService = new PriceListService();
         File priceList = priceListService.getPriceList("client1", PriceListType.price);
 
         assertNotNull(priceList);
     }
 
     @Test
     public void testGetNotExistPriceList() throws Exception {
         PriceListService priceListService = new PriceListService();
         File priceList = priceListService.getPriceList("client2", PriceListType.price);
 
         assertNull(priceList);
     }
 
    @Test
    public void testRefresh() throws JAXBException, FileNotFoundException, SQLException {
        PriceListService priceListService = new PriceListService();
        priceListService.refreshPriceList("client1",PriceListType.price);
    }

 }
