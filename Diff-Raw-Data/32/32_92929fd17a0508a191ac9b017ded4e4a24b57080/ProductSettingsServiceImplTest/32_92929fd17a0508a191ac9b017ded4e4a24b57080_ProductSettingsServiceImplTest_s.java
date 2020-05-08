 package com.discount.services.impl;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.discount.model.Product;
 import com.discount.model.ProductSettings;
 import com.discount.services.ProductService;
 import com.discount.services.ProductSettingsService;
 
 public class ProductSettingsServiceImplTest {
 	ApplicationContext appContext = new ClassPathXmlApplicationContext(
 			"spring/config/BeanLocations.xml");
 
 	@Test
 	public void testSave() {
 		ProductSettingsService productSettingsService = (ProductSettingsService) appContext
 				.getBean("productSettingsService");
 
 		ProductSettings expectedProductSettings = getProductSettings();
 
 		productSettingsService.save(expectedProductSettings);
 
 		ProductSettings actualProductSettings = productSettingsService
 				.findByPropertyName(expectedProductSettings.getPropertyName());
 
 		Assert.assertEquals(expectedProductSettings, actualProductSettings);
 
 		productSettingsService.delete(actualProductSettings);
 	}
 
 	@Test
 	public void testUpdate() {
 		ProductSettingsService productSettingsService = (ProductSettingsService) appContext
 				.getBean("productSettingsService");
 
 		ProductSettings expectedProductSettings = getProductSettings();
 
 		productSettingsService.save(expectedProductSettings);
 
 		ProductSettings actualProductSettings = productSettingsService
 				.findByPropertyName(expectedProductSettings.getPropertyName());
 
 		actualProductSettings.setPropertyValue("4GHz");
 
 		productSettingsService.update(actualProductSettings);
 
 		ProductSettings updatedProductSettings = productSettingsService
 				.findById(actualProductSettings.getId());
 
 		Assert.assertEquals(actualProductSettings, updatedProductSettings);
 
 		productSettingsService.delete(updatedProductSettings);
 	}
 
 	private ProductSettings getProductSettings() {
 		ProductSettings productSettings = new ProductSettings();
 
 		ProductService productService = (ProductService) appContext
 				.getBean("productService");
 
 		Product initProduct = productService.findById(new Integer(1));
 		productSettings.setPropertyName("CPU");
 		productSettings.setPropertyValue("2GHz");
		productSettings.setProduct(initProduct);
 
 		return productSettings;
 	}
 
 }
