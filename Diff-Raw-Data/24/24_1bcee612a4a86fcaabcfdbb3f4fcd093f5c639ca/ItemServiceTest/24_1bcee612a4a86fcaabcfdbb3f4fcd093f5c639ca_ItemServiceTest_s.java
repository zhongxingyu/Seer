 package com.euroit.militaryshop.service;
 
 import com.euroit.common.exception.ItemAlreadyExistsException;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * @author Roman Tuchin
  */
 @ContextConfiguration(locations = "classpath:integration-tests-context.xml")
 public class ItemServiceTest extends AbstractJUnit4SpringContextTests {
 
     @Autowired
     ItemService itemService;
 
     @Autowired
     ProductService productService;
 
     private final LocalServiceTestHelper helper =
             new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                     .setDefaultHighRepJobPolicyUnappliedJobPercentage(0.01f));
 
     private static final String PRODUCT_FULL_NAME = "productFullName";
     private static final String PRODUCT_SHORT_NAME = "productShortName";
     private long PREDEFINED_PRODUCT_ID;
     private static final String ITEM_SHORT_NAME = "itemShortName";
     private static final long ITEM_ID1 = 2;
     private static final long ITEM_ID2 = 3;
     private static final long MATERIAL_ID = 11;
     private static final long COLOR_ID = 12;
     private static final long SIZE_ID = 13;
     private static final long SIZE_ID_2 = 14;
     private static final long COLOR_ID_2 = 15;
     private static final long PRODUCT_ID_2 = 4;
 
     @Before
     public void setUp() {
         helper.setUp();
 
         createProduct();
         List<ProductDto> allProducts = productService.getAllProducts();
         PREDEFINED_PRODUCT_ID = allProducts.get(0).getId();
     }
 
     @Test
     public void testCreateOrSaveItem1() {
         ProductDto productDto = productService.findProductById(PREDEFINED_PRODUCT_ID);
         assertEquals(0, productDto.getItems().size());
 
         createOrSaveItem(0, 0, 0, 0, PREDEFINED_PRODUCT_ID);
 
         productDto = productService.findProductById(PREDEFINED_PRODUCT_ID);
         assertEquals(1, productDto.getItems().size());
 
         assertEquals(0, productDto.getItems().get(0).getColorId());
         assertEquals(0, productDto.getItems().get(0).getMaterialId());
         assertEquals(0, productDto.getItems().get(0).getSizeId());
         assertEquals(ITEM_SHORT_NAME, productDto.getItems().get(0).getShortName());
     }
 
     @Test
     public void testCreateOrSaveItem2() {
         createOrSaveItem(0, 0, 0, 0, PREDEFINED_PRODUCT_ID);
         createOrSaveItem(0, 0, 0, 0, PREDEFINED_PRODUCT_ID);
         createOrSaveItem(0, 0, 0, 0, PREDEFINED_PRODUCT_ID);
 
         ProductDto productDto = productService.findProductById(PREDEFINED_PRODUCT_ID);
         assertEquals(3, productDto.getItems().size());
     }
 
     @Test
     public void testCreateOrSaveItem3() {
         createOrSaveItem(ITEM_ID1, 0, 0, 0, PREDEFINED_PRODUCT_ID);
         createOrSaveItem(ITEM_ID2, 0, 0, 0, PREDEFINED_PRODUCT_ID);
         createOrSaveItem(ITEM_ID2, 0, 0, 0, PREDEFINED_PRODUCT_ID);
 
         ProductDto productDto = productService.findProductById(PREDEFINED_PRODUCT_ID);
         assertEquals(2, productDto.getItems().size());
     }
 
     @Test
     public void testCreateOrSaveItem4() {
         ProductDto productDto = productService.findProductById(PREDEFINED_PRODUCT_ID);
         assertEquals(0, productDto.getItems().size());
 
         createOrSaveItem(0, MATERIAL_ID, COLOR_ID, SIZE_ID, PREDEFINED_PRODUCT_ID);
 
         productDto = productService.findProductById(PREDEFINED_PRODUCT_ID);
         assertEquals(1, productDto.getItems().size());
 
         assertEquals(COLOR_ID, productDto.getItems().get(0).getColorId());
         assertEquals(MATERIAL_ID, productDto.getItems().get(0).getMaterialId());
         assertEquals(SIZE_ID, productDto.getItems().get(0).getSizeId());
     }
 
     private void createProduct() {
         ProductDto productDto = new ProductDto();
         productDto.setShortName(PRODUCT_SHORT_NAME);
         productDto.setFullName(PRODUCT_FULL_NAME);
         productService.createOrSaveProduct(productDto);
     }
 
     private void createOrSaveItem(long itemId, long materialId, long colorId, long sizeId, long predefinedProductId) {
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setId(itemId);
         dto.setProductId(predefinedProductId);
         dto.setShortName(ITEM_SHORT_NAME);
         dto.setMaterialId(materialId);
         dto.setColorId(colorId);
         dto.setSizeId(sizeId);
         itemService.createOrSaveItem(dto);
     }
 
     @Test(expected = ItemAlreadyExistsException.class)
     public void testCheckItemWithProperties1() throws ItemAlreadyExistsException {
         createOrSaveItem(0, MATERIAL_ID, COLOR_ID, SIZE_ID, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(MATERIAL_ID);
         dto.setColorId(COLOR_ID);
         dto.setSizeId(SIZE_ID);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test
     public void testCheckItemWithProperties2() throws ItemAlreadyExistsException {
         createOrSaveItem(0, MATERIAL_ID, COLOR_ID, SIZE_ID, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(MATERIAL_ID);
         dto.setColorId(COLOR_ID);
         dto.setSizeId(SIZE_ID_2);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test(expected = ItemAlreadyExistsException.class)
     public void testCheckItemWithProperties3() throws ItemAlreadyExistsException {
         createOrSaveItem(0, 0, COLOR_ID, SIZE_ID, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(0);
         dto.setColorId(COLOR_ID);
         dto.setSizeId(SIZE_ID);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test
     public void testCheckItemWithProperties4() throws ItemAlreadyExistsException {
         createOrSaveItem(0, 0, COLOR_ID, SIZE_ID, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(0);
         dto.setColorId(COLOR_ID);
         dto.setSizeId(SIZE_ID_2);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test(expected = ItemAlreadyExistsException.class)
     public void testCheckItemWithProperties5() throws ItemAlreadyExistsException {
         createOrSaveItem(0, MATERIAL_ID, 0, SIZE_ID, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(MATERIAL_ID);
         dto.setColorId(0);
         dto.setSizeId(SIZE_ID);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test
     public void testCheckItemWithProperties6() throws ItemAlreadyExistsException {
         createOrSaveItem(0, MATERIAL_ID, 0, SIZE_ID, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(MATERIAL_ID);
         dto.setColorId(0);
         dto.setSizeId(SIZE_ID_2);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test(expected = ItemAlreadyExistsException.class)
     public void testCheckItemWithProperties7() throws ItemAlreadyExistsException {
         createOrSaveItem(0, MATERIAL_ID, COLOR_ID, 0, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(MATERIAL_ID);
         dto.setColorId(COLOR_ID);
         dto.setSizeId(0);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test
     public void testCheckItemWithProperties8() throws ItemAlreadyExistsException {
         createOrSaveItem(0, MATERIAL_ID, COLOR_ID, 0, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(MATERIAL_ID);
         dto.setColorId(COLOR_ID_2);
         dto.setSizeId(0);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test(expected = ItemAlreadyExistsException.class)
     public void testCheckItemWithProperties9() throws ItemAlreadyExistsException {
         createOrSaveItem(0, 0, 0, 0, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(0);
         dto.setColorId(0);
         dto.setSizeId(0);
         dto.setProductId(PREDEFINED_PRODUCT_ID);
 
         itemService.checkItemWithProperties(dto);
     }
 
     @Test
     public void testCheckItemWithProperties10() throws ItemAlreadyExistsException {
         createOrSaveItem(0, 0, 0, 0, PREDEFINED_PRODUCT_ID);
 
         MilitaryShopItemDto dto = new MilitaryShopItemDto();
         dto.setMaterialId(0);
         dto.setColorId(0);
         dto.setSizeId(0);
         dto.setProductId(PRODUCT_ID_2);
 
         itemService.checkItemWithProperties(dto);
     }
 }
