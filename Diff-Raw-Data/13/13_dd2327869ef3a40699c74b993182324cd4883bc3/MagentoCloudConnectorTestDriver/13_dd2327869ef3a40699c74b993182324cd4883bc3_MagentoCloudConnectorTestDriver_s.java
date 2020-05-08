 /**
  * Mule Magento Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.magento;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import org.mule.module.magento.api.MagentoException;
 import org.mule.module.magento.api.catalog.model.MediaMimeType;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 
 /**
  * Integration test of the {@link MagentoCloudConnector}
  */
 @SuppressWarnings("serial")
 public class MagentoCloudConnectorTestDriver
 {
     private static final int EXISTENT_PRODUCT_ID = 11;
     /**A category that is supposed to exist, as a workaround to the create category magento bug*/
     private static final Integer ROOT_CATEGORY_ID = 3;
     private static final Integer CATEGORY_ID_1 = 4;
     private static final Integer CATEGORY_ID_2 = 5;
     private static final Integer CATEGORY_ID_3 = 6;
     
     private static final String ORDER_ID = "100000001";
     private MagentoCloudConnector connector;
 
     @Before
     public void setup() throws Exception
     {
         connector = new MagentoCloudConnector();
         //it looks like http://<host>/index.php/api/v2_soap
         connector.setAddress(System.getenv("magentoHost"));
         connector.setPassword(System.getenv("magentoPassword"));
         connector.setUsername(System.getenv("magentoUsername"));
         System.out.println(connector.getAddress());
         System.out.println(connector.getPassword());
         System.out.println(connector.getUsername());
         connector.initialise();
     }
 
     @Test
     public void listOrders() throws Exception
     {
         assertNotNull(connector.listOrders(null));
     }
 
     /**
      * Tests listing order invoices
      */
     @Test
     public void listOrdersInvoices() throws Exception
     {
         assertNotNull(connector.listOrdersInvoices(null));
     }
 
     /**
      * Tests getting information of an existent order.
      * The order {@link #ORDER_ID} must exist 
      */
     @Test
     public void getOrder() throws Exception
     {
         Map<String, Object> orderInfo = connector.getOrder(ORDER_ID);
         assertNotNull(orderInfo);
         System.out.println(ToStringBuilder.reflectionToString(orderInfo));
     }
 
     /**
      * Tests adding a comment to an existent order
      */
     @Test
     public void addOrderComment() throws Exception
     {
         connector.addOrderComment(ORDER_ID, "status", "A comment", false);
     }
 
     @Test
     public void getShipmentCarriers() throws Exception
     {
         assertFalse(connector.getOrderShipmentCarriers(ORDER_ID).isEmpty());
     }
 
     /**
      * Tests getting an order that does not exists
      */
     @Test(expected = MagentoException.class)
     public void getOrderInexistent()
     {
         connector.getOrder("899966");
     }
 
     @Test
     public void updateStockItem() throws Exception
     {
         connector.updateStockItem(String.valueOf(EXISTENT_PRODUCT_ID), new HashMap<String, Object>()
         {
             {
                 put("manage_stock", "0");
                 put("use_config_manage_stock", "0");
             }
         });
     }
 
     /**
      * Tests that filtering expressions are interpreted properly the the webservice
      * when passing numeric arguments
      */
     @Test
     public void getWithNumericFilter() throws Exception
     {
         assertEquals(connector.listOrders("").size(), connector.listOrders("gt(subtotal, 800)").size()
                                                       + connector.listOrders("lteq(subtotal, 800)").size());
     }
 
     /**
      * Tests that filtering expressions are interpreted properly the the webservice
      * when passing string arguments
      */
     @Test
     public void getWithStringFilter() throws Exception
     {
         assertEquals(connector.listOrders("").size(), 
                      connector.listOrders("eq(customer_firstname, 'John')").size() 
                          + connector.listOrders("neq(customer_firstname, 'John')").size());
     }
 
     /**
      * Test that a user can be created an deleted, and that such operations
      * impact on the customer listing
      */
     @Test
     public void createCustomer() throws Exception
     {
         final String email = "johndoe@mycia.com";
         final String firstname = "John";
         final String lastname = "Doe";
         assertEquals(0, countCustomers(email, firstname, lastname));
         
         int customerId = connector.createCustomer(new HashMap<String, Object>()
         {
             {
                 put("email", email);
                 put("firstname", firstname);
                 put("lastname", lastname);
                 put("password", "123456");
                 put("group_id", "1");
             }
         });
         try
         {
             assertEquals(1, countCustomers(email, firstname, lastname));
             assertEquals(firstname, 
                 connector.getCustomer(customerId, Arrays.asList("firstname")).get("firstname"));
         }
         finally
         {
             connector.deleteCustomer(customerId);
             assertEquals(0, countCustomers(email, firstname, lastname));
         }
     }
     
     /**
      * Test that a customer can be updated 
      */
     @Test
     public void updateCustomer() throws Exception
     {
         int customerId = connector.createCustomer(new HashMap<String, Object>()
         {
             {
                 put("email", "johndoe@mycia.com");
                 put("firstname", "John");
                 put("lastname", "Doe");
                 put("password", "123456");
                 put("group_id", "1");
             }
         });
         try
         {
             connector.updateCustomer(customerId, new HashMap<String, Object>()
             {
                 {
                     put("firstname", "Tom");
                 }
             });
             assertEquals("Tom", 
                 connector.getCustomer(customerId, Arrays.asList("firstname")).get("firstname"));
         }
         finally
         {
             connector.deleteCustomer(customerId);
         }
     }
 
     /**Counts customers that have the given email, firstname and lastname*/
     private int countCustomers(String email, String firstname, String lastname)
     {
         return connector.listCustomers(
             "eq(firstname,'" + firstname + "'), eq(lastname, '" + lastname + "'), eq(email, '" + email + "')").size();
     }
     
     /**Tests that customer groups can be listed*/
     @Test
     public void listCustomerGroups() throws Exception
     {
         assertFalse(connector.listCustomerGroups().isEmpty());
     }
 
     /**
      * Tests that the XXXCurrentStore SOAP methods can be used like a getter - they
      * are not very well documented
      * 
      * @throws Exception
      */
     @Test
     public void storeView() throws Exception
     {
         assertEquals(connector.getCatalogCurrentStoreView(), connector.getCatalogCurrentStoreView());
     }
     
     /**
      * Tests that search by SKU works
      * This test assumes that there exists a product with id {@link #EXISTENT_PRODUCT_ID}
      */
    @Test
     public void getExistentProductBySku() throws Exception
     {
         Map<String, Object> product = getExistentProductWithDescriptions();
        Map<String, Object> product2 = connector.getProduct(null, null, (String) product.get("sku"), null,
             Arrays.asList("description"), Collections.<String> emptyList());
         assertEquals(product.get("id"), product2.get("id"));
     }
     
     /**
     * Tests that search by ID works
      * This test assumes that there exists a product with id {@link #EXISTENT_PRODUCT_ID}
      */
     @Test
     public void getExistentProductByIdOrSku() throws Exception
     {
         Map<String, Object> product = getExistentProductWithDescriptions();
        Map<String, Object> product2 = connector.getProduct(null, (String) product.get("sku"), null, null,
             Arrays.asList("description"), Collections.<String> emptyList());
         assertEquals(product.get("id"), product2.get("id"));
     }
 
     /**
      * This test assumes that there exists a product with id {@link #EXISTENT_PRODUCT_ID}
      */
     @Test
     public void getAndUpdateExistentProduct() throws Exception
     {
         final String description = "A great wood kitchen table";
         final String shortDescription = "Best Product ever!";
         updateDescriptions(description, shortDescription);
         Map<String, Object> product = getExistentProductWithDescriptions();
         assertEquals(description, product.get("description"));
         assertEquals(shortDescription, product.get("short_description"));
         
         final Object description2 = "An acceptable kitchen table";
         final Object shortDescription2 = "A good product";
         updateDescriptions(description2, shortDescription2);
         product = getExistentProductWithDescriptions();
         assertEquals(description2, product.get("description"));
         assertEquals(shortDescription2, product.get("short_description"));
     }
     
     private void updateDescriptions(final Object description2, final Object shortDescription2)
     {
         connector.updateProduct(null, null, String.valueOf(EXISTENT_PRODUCT_ID), new HashMap<String, Object>()
         {
             {
                 put("description", description2);
                 put("short_description", shortDescription2);
             }
         }, null);
     }
 
     private Map<String, Object> getExistentProductWithDescriptions()
     {
         return connector.getProduct(EXISTENT_PRODUCT_ID, null, null, null, Arrays.asList("sku",
             "description", "short_description"), null);
     }
     
     
     /**
      * Test that products can be created, linked and deleted
      */
     @Test
     public void createAndLinkProduct() throws Exception
     {
         Integer productId = null;
         Integer productId2 = null;
         try
         {
             productId = connector.createProduct("simple", 4, "FOOO457", null, null);
             productId2 = connector.createProduct("simple", 4, "AOOO986", null, null);
             connector.addProductLink("related", productId, null, null, productId2.toString(), null);
         }
         finally
         {
             if (productId != null)
             {
                 connector.deleteProduct(productId, null, null);
             }
             if (productId2 != null)
             {
                 connector.deleteProduct(productId2, null, null);
             }
         }
     }
     
     /**
      * Test that product inventory can be set retrieved
      */
     @Test
     public void productInventory() throws Exception
     {
         Integer productId = null;
         productId = connector.createProduct("simple", 4, "X8960",
             Collections.singletonMap("stock_data",
             (Object) new HashMap<String, Object>()
             {
                 {
                     put("qty", "10");
                     put("is_in_stock", true);
                 }
             }), null);
         try
         {
             List<Map<String, Object>> stockItems = connector.listStockItems(Arrays.asList("X8960"));
             assertEquals(1, stockItems.size());
             assertEquals("10.0000", stockItems.get(0).get("qty"));
             assertEquals("1", stockItems.get(0).get("is_in_stock"));
         }
         finally
         {
             if (productId != null)
             {
                 connector.deleteProduct(productId, null, null);
             }
         }
     }
 
     /**
      * Test that product can be listed, and their special prices retrieved and
      * updated. It assumes that a product with sku 986320 exists
      */
     @Test
     public void specialPrices() throws Exception
     {
         Integer productId = null;
         try
         {
             int originalProductsCount = connector.listProducts(null, null).size();
             productId = connector.createProduct("simple", 4, "AK4596", null, null);
             assertEquals(originalProductsCount + 1, connector.listProducts(null, null).size());
             connector.updateProductSpecialPrice(null, null, productId.toString(), "6953.6", "2011-30-01", null, null);
             Map<String, Object> productSpecialPrice = connector.getProductSpecialPrice(productId, null, null,
                 null);
             assertNotNull(productSpecialPrice);
             System.out.println("Special price:" + productSpecialPrice);
         }
         finally
         {
             if (productId != null)
             {
                 connector.deleteProduct(productId, null, null);
             }
         }
     }
 
     /** Test that images can be uploaded, fetched and deleted */
     @Test
     public void createMedia() throws Exception
     {
         Integer productId = null;
         String fileName = null;
         try
         {
             productId = connector.createProduct("simple", 4, "W875651", null, null);
             int originalMediaCount = connector.listProductAttributeMedia(productId, null, null, null).size();
 
             fileName = connector.createProductAttributeMedia(productId, null, null, null, null,
                 new ClassPathResource("img.gif").getInputStream(), MediaMimeType.GIF, "img.gif");
             assertNotNull(connector.getProductAttributeMedia(productId, null, null, fileName, null));
 
             assertEquals(originalMediaCount + 1, connector.listProductAttributeMedia(productId, null, null,
                 null).size());
         }
         finally
         {
             if (productId != null)
             {
                 if (fileName != null)
                 {
                     connector.deleteProductAttributeMedia(productId, null, null, fileName);
                 }
                 connector.deleteProduct(productId, null, null);
             }
         }
     }
 
     @Test 
     public void createCategory() throws Exception
     {
         Integer categoryId = null;
         try
         {
             categoryId = connector.createCategory(1, new HashMap<String, Object>()
             {
                 {
                     put("name", "Hardware");
                     put("is_active", true);
                     put("level", 1);
                     put("available_sort_by", false);
                     put("default_sort_by", false);
                     put("include_in_menu", 1);
                     put("meta_title", "Hardware and similar stuff");
                 }
             }, null);
         }
         finally
         {
             if (categoryId != null)
             {
                 connector.deleteCategory(categoryId);
             }
         }
     }
     
     /**
      * Tests that can list countries and regions
      */
     @Test
     public void directory() throws Exception
     {
         List<Map<String, Object>> countries = connector.listDirectoryCountries();
         assertFalse(countries.isEmpty());
         assertEquals("Andorra", countries.get(0).get("name"));
         assertEquals("United Arab Emirates", countries.get(1).get("name"));
         assertFalse(connector.listDirectoryRegions("US").isEmpty());
     }
     
     /**
      * Test that category attributes can be fetched 
      * 
      * This test assumes there exist a category with id {@link #CATEGORY_ID_1},
      * name subCategory1, active
      * and description "This a subcategory!"
      * 
      * @throws Exception
      */
     @Test
     public void getCategory() throws Exception
     {
         Map<String, Object> attributes = connector.getCategory(CATEGORY_ID_1, null, Arrays.asList("name", "is_active",
             "description"));
         assertEquals(attributes.get("name"), "SubCategory1");
         assertEquals(1, attributes.get("is_active"));
         assertEquals(attributes.get("description"), "This a subcategory!");
     }
     
     /**
      * Test that the category tree can be fetched. This tests assumes the existence
      * of a root category with id {@link #ROOT_CATEGORY_ID}, and that the category structure is the following:
      * <pre>
      * Root:{@value #ROOT_CATEGORY_ID} +- SubCategory1:{@value #CATEGORY_ID_1}
      *                                 |
      *                                 +-SubCategory3:{@value #CATEGORY_ID_3}
      *                                 |
      *                                 +-SubCategory2:{@value #CATEGORY_ID_2}   
      * </pre>
      */
     @Test
     public void getCategoryTree() throws Exception
     {
         Map<String, Object> categoryTree = connector.getCategoryTree(ROOT_CATEGORY_ID.toString(), null);
         assertEquals(ROOT_CATEGORY_ID, categoryTree.get("category_id"));
         assertEquals(CATEGORY_ID_1, getChildren(categoryTree, 0).get("category_id"));
         assertEquals(CATEGORY_ID_3, getChildren(categoryTree, 1).get("category_id"));
         assertEquals(CATEGORY_ID_2, getChildren(categoryTree, 2).get("category_id"));
     }
     
     /**
      * Gets the nth children of a category
      * @param categoryTree
      * @param pos
      * @return
      */
     @SuppressWarnings("unchecked")
     private Map<String, Object> getChildren(Map<String, Object> categoryTree, int pos)
     {
         return ((List<Map<String, Object>>) categoryTree.get("children")).get(pos);
     }
     
     @Test
     public void move() throws Exception
     {
         assertEquals(ROOT_CATEGORY_ID.toString(), connector.getCategory(CATEGORY_ID_2, null, Arrays.asList("parent_id")).get("parent_id"));
         
         connector.moveCategory(CATEGORY_ID_2, CATEGORY_ID_3, null);
         assertEquals(CATEGORY_ID_3.toString(), connector.getCategory(CATEGORY_ID_2, null, Arrays.asList("parent_id")).get("parent_id"));
         
         connector.moveCategory(CATEGORY_ID_2, ROOT_CATEGORY_ID, null);
         assertEquals(ROOT_CATEGORY_ID.toString(), connector.getCategory(CATEGORY_ID_2, null, Arrays.asList("parent_id")).get("parent_id"));
     }
     
 }
