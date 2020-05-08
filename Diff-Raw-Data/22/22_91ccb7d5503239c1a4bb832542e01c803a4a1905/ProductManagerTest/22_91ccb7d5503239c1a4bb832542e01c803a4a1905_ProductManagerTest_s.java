 package com.orange.groupbuy.manager.test;
 
 import java.util.Date;
 import java.util.Random;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.orange.common.mongodb.MongoDBClient;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 
 public class ProductManagerTest {
 
 	static MongoDBClient mongoClient;
 	Random seed;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 		mongoClient = new MongoDBClient("localhost", "groupbuy", "", "");
 		seed = new Random();
 	}
 
 	@Test
 	public void testCreateProduct() {
 		
 		Product product = new Product();
		String city = "";
 		String loc = "http://t.dianping.com/deal/3671";
		String title = "35Ԫ!ֵ93-96ԪС˫ײ(19ͨ)";
 		String image = "http://t2.dpfile.com/tuan/20110714/3671_129551282160000000.jpg";
 		Date startDate = new Date();
 		Date endDate = new Date();
 		double price = 19.99;
 		double value = 299.99;
 		int bought = 150;
 		String siteId = "dianping";
		String siteName = "";
 		String siteURL = "http://www.dianping.com";
 		
 		product.setMandantoryFields(city, loc, image, title, startDate, endDate, price, value, bought, siteId, siteName, siteURL);
 		ProductManager.createProduct(mongoClient, product);
 	}
 }
