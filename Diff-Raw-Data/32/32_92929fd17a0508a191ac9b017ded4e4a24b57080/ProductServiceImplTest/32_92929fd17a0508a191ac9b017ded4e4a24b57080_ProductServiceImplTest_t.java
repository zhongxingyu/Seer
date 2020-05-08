 package com.discount.services.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.discount.model.Client;
 import com.discount.model.Product;
 import com.discount.model.ProductCategory;
 import com.discount.model.ProductSettings;
 import com.discount.services.ClientService;
 import com.discount.services.ProductCategoryService;
 import com.discount.services.ProductService;
 
 public class ProductServiceImplTest {
 	ApplicationContext appContext = new ClassPathXmlApplicationContext(
 			"spring/config/BeanLocations.xml");
 
 	@Test
 	public void testSave() {
 		ProductService productService = (ProductService) appContext
 				.getBean("productService");
 		Product expectedProduct = getProduct();
 
 		productService.save(expectedProduct);
 
 		Product actualProduct = productService.findByName(expectedProduct
 				.getName());
 
 		Client client = actualProduct.getClient();
 		ProductCategory productCategory = actualProduct.getProductCategory();
 		List<ProductSettings> settings = actualProduct.getSettings();
 		// System.out.println(settings.get(0).getPropertyName());
 		// Assert.assertEquals(expectedProduct, actualProduct);
 		productService.delete(expectedProduct);
 	}
 
 	@Test
 	public void testUpdate() {
 		ProductService productService = (ProductService) appContext
 				.getBean("productService");
 		Product expectedProduct = getProduct();
 
 		productService.save(expectedProduct);
 
 		Product actualProduct = productService.findByName(expectedProduct
 				.getName());
 
 		actualProduct.setName("Updated name");
 
 		productService.update(actualProduct);
 
 		Product updatedProduct = productService.findById(actualProduct.getId());
 
 		productService.delete(updatedProduct);
 	}
 
 	private Product getProduct() {
 		Product product = new Product();
 
 		ClientService clientService = (ClientService) appContext
 				.getBean("clientService");
 		ProductCategoryService productCategoryService = (ProductCategoryService) appContext
 				.getBean("productCategoryService");
 		ProductCategory productCategory = productCategoryService
 				.findByName("init category");
 
 		Client initClient = clientService.findByName("init client");
 
 		ProductSettings cpuSettings = new ProductSettings();
 		cpuSettings.setPropertyName("CPU");
 		cpuSettings.setPropertyValue("1000GHz");
		cpuSettings.setProduct(product);
 
 		List<ProductSettings> settings = new ArrayList<ProductSettings>();
 		settings.add(cpuSettings);
 
 		product.setClient(initClient);
 		product.setDiscount(new Integer(20));
 		product.setDiscountPrice(new Integer(80));
 		product.setInitialPrice(new Integer(100));
 		product.setName("test product");
 		product.setProductCategory(productCategory);
 		product.setSettings(settings);
 
 		product.setBeginShowDate(new Date());
 
 		return product;
 	}
 
 }
