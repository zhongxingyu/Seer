 package fr.soat.it;
 
 import org.junit.Test;
 
 import junit.framework.Assert;
 import fr.soat.bean.Product;
 import fr.soat.cassandra.ProductNoSqlRepository;
 
 /**
  * @author Mouhcine MOULOU
  *
  */
 public class ProductNoSqlRepositoryTestIt extends AbstractNoSqlRepositoryIntegrationTests{
 	
 
 	@Test
 	public void insertProductShouldBeOk() {
 		
 		// G
		ProductNoSqlRepository productNoSqlRepository = new ProductNoSqlRepository("TestCluster", "localhost:9160", "StockKS");
 		Product product = new Product("001", "keyboard", 4, 50.0);
 		
 		// W
 		productNoSqlRepository.insert(product);
 		
 		//T
 		Product product2 = productNoSqlRepository.getByRefCQL(product.getRef());
 		Assert.assertNotNull(product2);
 		Assert.assertEquals(product, product2);
 	}
 }
