 package com.bing.biz.pdm.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.jtester.annotations.SpringApplicationContext;
 import org.jtester.annotations.SpringBeanByName;
 import org.junit.Test;
 
 import com.binq.biz.pdm.po.Product;
 import com.binq.biz.pdm.service.ProductService;
 import com.googlecode.coss.common.core.orm.mybatis.Page;
 import com.googlecode.coss.common.test.BaseJtesterCase;
 
 @SpringApplicationContext({ "/META-INF/spring/biz-service-test.xml" })
 public class ProductServiceTest extends BaseJtesterCase {
     @SpringBeanByName
     private ProductService productService;
 
     @Test
     //@DbFit(when = "wiki/storage/allot.wiki")
     public void testSaveProduct() {
         Product product = new Product();
        product.setId(1005L);
         product.setGmtCreate(new Date());
         product.setProductCode("A0005");
         product.setProductName("Test Product");
         product.setRetailPrice(100.3F);
         productService.save(product);
     }
 
     public void testPage() {
         Page<Product> page = productService.queryPage(1, 2, null);
         List<Product> productList = page.getResult();
         System.out.println(productList.size());
         want.number(productList.size()).isGreaterThan(0);
     }
 }
