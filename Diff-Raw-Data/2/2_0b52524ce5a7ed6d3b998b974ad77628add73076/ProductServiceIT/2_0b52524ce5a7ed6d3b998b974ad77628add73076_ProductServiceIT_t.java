 package com.euroit.militaryshop.service;
 
 import com.euroit.eshop.exception.VisibleProductNotFoundException;
 import com.euroit.militaryshop.dto.DictionaryEntryDto;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.enums.DictionaryName;
 import com.euroit.militaryshop.service.impl.ProductServiceImpl;
 import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 import javax.persistence.NoResultException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import static org.junit.Assert.*;
 
 @ContextConfiguration(locations = "classpath:integration-tests-context.xml")
 public class ProductServiceIT extends AbstractJUnit4SpringContextTests {
 	public static final String DUMMY_CODE = "dummyOne";
 	
 	public static final String DUMMY_CATEGORY = "Dummy category";
 	
 	public static final String DUMMY_CODE_2 = "dummyOne2";
 	
 	public static final String DUMMY_CATEGORY_2 = "Dummy category2";
 
 	public static final String PRODUCT_INTERNAL_CODE = "KU-M65-NY-04";
 
 	public static final String PRODUCT_SHORT_NAME = "Рубашка";
 
 	public static final String PRODUCT_FULL_NAME = "Длинное описание рубашки";
 
 	private static final long FIRST_CREATED_ID = 2;
 
 	private static final String PRODUCT_FULL_NAME_NEW = "Новое описание";
 
 	private static final String PRODUCT_SHORT_NAME_NEW = "Новое короткое описание";
 
 	private static final String PRODUCT_INTERNAL_CODE_NEW = "Ноый Внутренний код";
 	
 	private final LocalServiceTestHelper helper = 
 			new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                     .setDefaultHighRepJobPolicyUnappliedJobPercentage(0.01f),
                     new LocalBlobstoreServiceTestConfig());
     private static final long PRODUCT_ID_1 = 1;
 
 	private static final long ITEM_ID = 10;
 
 	private static final Float PRODUCT_PRICE = 100f;
 	private static final Float PRODUCT_PRICE_NEW = 200f;
 	private static final Long CAT_1 = 5l;
 	private static final Long CAT_2 = 10l;
 
     private static final long DICT_ENTRY_ID = 1;
     private static final String DICT_ENTRY_VALUE = "dictEntryValue1";
     private static final long DICT_ENTRY_ID_2 = 2;
     private static final String DICT_ENTRY_VALUE_2 = "dictEntryValue2";
 
 	@Autowired
 	CategoryService categoryService;
 	
 	@Autowired
 	ProductService productService;
 
     @Autowired
     DictionaryEntryService dictionaryEntryService;
 
     @Autowired
     ItemService itemService;
 	
 	
 
     @Before
 	public void setUp() {
 		helper.setUp();
 		initDB();
 	}
 	
 	private void initDB() {
 		categoryService.createCategory(DUMMY_CATEGORY, DUMMY_CODE);
 	}
 
 	@After
 	public void tearDown() {
 		helper.tearDown();
 	}
 	
 	/**
 	 * Nonexistent product id
 	 */
 	public void testFindProductById1() {
 		assertNull(productService.getProductForAdminCard(1));
 	}
 	
 	/**
 	 * Real product id
 	 */
 	@Test
 	public void testFindProductById2() {
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
 		ProductDto product = productService.getProductForAdminCard(FIRST_CREATED_ID);
 		assertEquals(PRODUCT_FULL_NAME, product.getFullName());
 		assertEquals(PRODUCT_SHORT_NAME, product.getShortName());
 		assertEquals(PRODUCT_INTERNAL_CODE, product.getInternalCatalogCode());
 		assertEquals(PRODUCT_PRICE, product.getPrice());
 	}
 	
 	@Test
 	public void testFindProductById3() {
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, null);
 		
 	}
 		
 	@Test
 	public void testGetAllProducts1() {
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
 		List<ProductDto> productList = productService.getAllProducts();
 		assertEquals(2, productList.size());
 		
 		assertEquals(PRODUCT_FULL_NAME, productList.get(0).getFullName());
 		assertEquals(PRODUCT_SHORT_NAME, productList.get(0).getShortName());
 		assertEquals(PRODUCT_INTERNAL_CODE, productList.get(0).getInternalCatalogCode());
 		assertEquals(PRODUCT_PRICE, productList.get(0).getPrice());
 	}
 	
 	@Test
 	public void testCreateOrSaveProduct1() {
 		productService.createOrSave(null);
 		List<ProductDto> products = productService.getAllProducts();
 		assertEquals(0, products.size());
 	}
 	
 	/**
 	 * Should create a product
 	 */
 	@Test
 	public void testCreateOrSaveProduct2() {
 		ProductDto dto = new ProductDto();
 		dto.setFullName(PRODUCT_FULL_NAME);
 		dto.setShortName(PRODUCT_SHORT_NAME);
 		dto.setInternalCatalogCode(PRODUCT_INTERNAL_CODE);
 		dto.setPrice(PRODUCT_PRICE);
         dto.setVisible(true);
 		
 		productService.createOrSave(dto);
 		
 		List<ProductDto> products = productService.getAllProducts();
 		assertEquals(1, products.size());
 		
 		assertEquals(PRODUCT_FULL_NAME, products.get(0).getFullName());
 		assertEquals(PRODUCT_SHORT_NAME, products.get(0).getShortName());
 		assertEquals(PRODUCT_INTERNAL_CODE, products.get(0).getInternalCatalogCode());
 		assertEquals(PRODUCT_PRICE, products.get(0).getPrice());
         assertTrue(products.get(0).isVisible());
 	}
 	/**
 	 * Should save a product
 	 */
 	@Test
 	public void testCreateOrSaveProduct3() {
 		ProductDto dto = new ProductDto();
 		dto.setFullName(PRODUCT_FULL_NAME);
 		dto.setShortName(PRODUCT_SHORT_NAME);
 		dto.setInternalCatalogCode(PRODUCT_INTERNAL_CODE);
 		dto.setPrice(PRODUCT_PRICE);
 		
 		productService.createOrSave(dto);
 		
 		List<ProductDto> products = productService.getAllProducts();
 		assertEquals(1, products.size());
 		
 		dto = products.get(0);
 		
 		String smallImageName = dto.getSmallImageName();
 		String bigImageName = dto.getBigImageName();
 
 		dto.setFullName(PRODUCT_FULL_NAME_NEW);
 		dto.setInternalCatalogCode(PRODUCT_INTERNAL_CODE_NEW);
 		dto.setPrice(PRODUCT_PRICE_NEW);
 		dto.setShortName(PRODUCT_SHORT_NAME_NEW);
 		
 		productService.createOrSave(dto);
 		
 		products = productService.getAllProducts();
 		assertEquals(1, products.size());
 		
 		assertEquals(bigImageName, products.get(0).getBigImageName());
 		assertEquals(smallImageName, products.get(0).getSmallImageName());
 		assertEquals(PRODUCT_FULL_NAME_NEW, products.get(0).getFullName());
 		assertEquals(PRODUCT_SHORT_NAME_NEW, products.get(0).getShortName());
 		assertEquals(PRODUCT_INTERNAL_CODE_NEW, products.get(0).getInternalCatalogCode());
 		assertEquals(PRODUCT_PRICE_NEW, products.get(0).getPrice());
 	}
 	
 	/**
 	 * Should create product without categories 
 	 */
 	@Test
 	public void testCreateOrSaveProduct4() {
 		ProductDto dto = new ProductDto();
 		productService.createOrSave(dto);
 		
 		dto = productService.getProductForAdminCard(productService.getAllProducts().get(0).getId());
 		assertEquals(0, dto.getCategoryIds().size());
 	}
 	
 	/**
 	 * Should create product with categories 
 	 */
 	@Test
 	public void testCreateOrSaveProduct5() {
 		ProductDto dto = new ProductDto();
 		List<Long> categoryIds = new ArrayList<Long>();
 		categoryIds.add(CAT_1);
 		
 		dto.setCategoryIds(categoryIds);
 		productService.createOrSave(dto);
 		
 		dto = productService.getProductForAdminCard(productService.getAllProducts().get(0).getId());
 		assertEquals(1, dto.getCategoryIds().size());
 		assertEquals(CAT_1, dto.getCategoryIds().get(0));
 	}
 	
 	/**
 	 * Should change products categories to new 
 	 */
 	@Test
 	public void testCreateOrSaveProduct6() {
 		ProductDto dto = new ProductDto();
 		List<Long> categoryIds = new ArrayList<Long>();
 		categoryIds.add(CAT_1);
 		
 		dto.setCategoryIds(categoryIds);
 		productService.createOrSave(dto);
 		
 		dto = productService.getProductForAdminCard(productService.getAllProducts().get(0).getId());
 		
 		categoryIds.clear();
 		categoryIds.add(CAT_2);
 		dto.setCategoryIds(categoryIds);
 		
 		productService.createOrSave(dto);
 		
 		dto = productService.getProductForAdminCard(productService.getAllProducts().get(0).getId());
 		
 		assertEquals(1, dto.getCategoryIds().size());
 		assertEquals(CAT_2, dto.getCategoryIds().get(0));
 	}
 	
 	
 	/**
 	 * Should change products categories by adding one new category
 	 */
 	@Test
 	public void testCreateOrSaveProduct7() {
 		ProductDto dto = new ProductDto();
 		List<Long> categoryIds = new ArrayList<Long>();
 		categoryIds.add(CAT_1);
 		
 		dto.setCategoryIds(categoryIds);
 		productService.createOrSave(dto);
 		
 		dto = productService.getProductForAdminCard(productService.getAllProducts().get(0).getId());
 		
 		categoryIds.clear();
 		categoryIds.add(CAT_1);
 		categoryIds.add(CAT_2);
 		dto.setCategoryIds(categoryIds);
 		
 		productService.createOrSave(dto);
 		
 		dto = productService.getProductForAdminCard(productService.getAllProducts().get(0).getId());
 		
 		assertEquals(2, dto.getCategoryIds().size());
 		assertEquals(CAT_1, dto.getCategoryIds().get(0));
 		assertEquals(CAT_2, dto.getCategoryIds().get(1));
 	}
 
     @Test
 	public void testGetAllProductsByCategory1() {
 		categoryService.createCategory(DUMMY_CATEGORY_2, DUMMY_CODE_2);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE_2, PRODUCT_PRICE);
 		
 		List<ProductDto> productList = productService.getAllProductsByCategory(DUMMY_CODE);
 		assertEquals(2, productList.size());
 		
 		assertEquals(PRODUCT_FULL_NAME, productList.get(0).getFullName());
 		assertEquals(PRODUCT_SHORT_NAME, productList.get(0).getShortName());
 		assertEquals(PRODUCT_INTERNAL_CODE, productList.get(0).getInternalCatalogCode());
 		assertEquals(PRODUCT_PRICE, productList.get(0).getPrice());
 	}
 
     @Test
     public void testAddValueToSet1() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         @SuppressWarnings("rawtypes")
 		Class[] paramTypes = {Set.class, Long.TYPE, String.class};
         Method method = ProductServiceImpl.class.getDeclaredMethod("addValueToSet", paramTypes);
         method.setAccessible(true);
 
         Set<DictionaryEntryDto> valuesSet = new HashSet<DictionaryEntryDto>();
 
         method.invoke(new ProductServiceImpl(), valuesSet, 0, null);
 
         assertEquals(0, valuesSet.size());
     }
 
     @Test
     public void testAddValueToSet2() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         Method method = ProductServiceImpl.class.getDeclaredMethod("addValueToSet", Set.class, Long.TYPE, String.class);
         method.setAccessible(true);
 
         Set<DictionaryEntryDto> valuesSet = new HashSet<DictionaryEntryDto>();
         valuesSet.add(new DictionaryEntryDto(DICT_ENTRY_ID));
 
         method.invoke(new ProductServiceImpl(), valuesSet, DICT_ENTRY_ID, DICT_ENTRY_VALUE);
 
         assertEquals(1, valuesSet.size());
     }
 
     @Test
     public void testAddValueToSet3() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         dictionaryEntryService.createEntry(DictionaryName.COLOR, "ss");
         Method method = ProductServiceImpl.class.getDeclaredMethod("addValueToSet", Set.class, Long.TYPE, String.class);
         method.setAccessible(true);
 
         Set<DictionaryEntryDto> valuesSet = new HashSet<DictionaryEntryDto>();
         valuesSet.add(new DictionaryEntryDto(DICT_ENTRY_ID_2));
 
         ProductServiceImpl productService = new ProductServiceImpl();
 
         productService.setDictionaryEntryService(dictionaryEntryService);
 
         method.invoke(productService, valuesSet, DICT_ENTRY_ID, DICT_ENTRY_VALUE);
 
         assertEquals(2, valuesSet.size());
     }
 
     @Test
     public void testGetVisibleProduct() throws VisibleProductNotFoundException {
         ProductDto dto = new ProductDto();
         dto.setFullName(PRODUCT_FULL_NAME);
         dto.setShortName(PRODUCT_SHORT_NAME);
         dto.setInternalCatalogCode(PRODUCT_INTERNAL_CODE);
         dto.setPrice(PRODUCT_PRICE);
         dto.setVisible(true);
         productService.createOrSave(dto);
 
         long productId = productService.getAllProducts().get(0).getId();
         MilitaryShopItemDto item = new MilitaryShopItemDto();
         item.setProductId(productId);
         itemService.createOrSaveItem(item);
 
         dto = productService.getVisibleProduct(productId);
         assertEquals(0, dto.getPropertiesMap().get(DictionaryName.COLOR.name()).size());
         assertEquals(0, dto.getPropertiesMap().get(DictionaryName.MATERIAL.name()).size());
         assertEquals(0, dto.getPropertiesMap().get(DictionaryName.SIZE.name()).size());
     }
 
     @Test(expected = VisibleProductNotFoundException.class)
     public void testGetVisibleProduct2() throws VisibleProductNotFoundException {
         productService.getVisibleProduct(PRODUCT_ID_1);
     }
 
     @Test(expected = VisibleProductNotFoundException.class)
     public void testGetVisibleProduct3() throws VisibleProductNotFoundException {
         ProductDto dto = new ProductDto();
         dto.setFullName(PRODUCT_FULL_NAME);
         dto.setShortName(PRODUCT_SHORT_NAME);
         dto.setInternalCatalogCode(PRODUCT_INTERNAL_CODE);
         dto.setPrice(PRODUCT_PRICE);
         dto.setVisible(false);
         productService.createOrSave(dto);
 
         long productId = productService.getAllProducts().get(0).getId();
         productService.getVisibleProduct(productId);
     }
     
     @Test
     public void testGetVisibleProduct4() throws VisibleProductNotFoundException {
         ProductDto dto = new ProductDto();
         dto.setFullName(PRODUCT_FULL_NAME);
         dto.setShortName(PRODUCT_SHORT_NAME);
         dto.setInternalCatalogCode(PRODUCT_INTERNAL_CODE);
         dto.setPrice(PRODUCT_PRICE);
         dto.setVisible(true);
         productService.createOrSave(dto);
 
         long productId = productService.getAllProducts().get(0).getId();
         MilitaryShopItemDto item = new MilitaryShopItemDto();
         item.setProductId(productId);
        item.setBigImageName("RAThZA.jpg");
        item.setSmallImageName("RAThZA2.jpg");
         itemService.createOrSaveItem(item);
 
         dto = productService.getVisibleProduct(productId);
         assertEquals("[{\"itemId\":3,\"price\":100.0,\"productId\":2,\"colorId\":0,\"materialId\":0," +
                 "\"sizeId\":0,\"bigImageName\":\"RAThZA.jpg\"}]",
         		dto.getItemsJson());
     }
 
     @Test
     public void testHasItems1() {
         productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
         long productId = productService.getAllProducts().get(0).getId();
 
         assertFalse(productService.hasItems(productId));
     }
 
     @Test
     public void testHasItems2() {
         productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
         long productId = productService.getAllProducts().get(0).getId();
 
         MilitaryShopItemDto item = new MilitaryShopItemDto();
         item.setProductId(productId);
         itemService.createOrSaveItem(item);
 
         assertTrue(productService.hasItems(productId));
     }
 
     @Test
     public void testHasItems3() {
         assertFalse(productService.hasItems(0));
     }
 
     @Test
     public void testGetVisibleProducts1() {
         productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE,
                 PRODUCT_PRICE);
         assertEquals(0, productService.getVisibleProducts().size());
     }
 
     @Test
     public void testGetVisibleProducts2() {
         ProductDto productDto = new ProductDto();
         productDto.setVisible(true);
         productService.createOrSave(productDto);
 
         assertEquals(1, productService.getVisibleProducts().size());
     }
     
     @Test
     public void testGetVisibleProductsByCategory1() {
     	categoryService.createCategory(DUMMY_CATEGORY_2, DUMMY_CODE_2);
     	productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE, true);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE, true);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE, false);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE_2, PRODUCT_PRICE, true);
 		productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE_2, PRODUCT_PRICE, false);
 		
 		List<ProductDto> productList = productService.getVisibleProductsByCategory(DUMMY_CODE);
 		assertEquals(2, productList.size());
 		
 		assertEquals(PRODUCT_FULL_NAME, productList.get(0).getFullName());
 		assertEquals(PRODUCT_SHORT_NAME, productList.get(0).getShortName());
 		assertEquals(PRODUCT_INTERNAL_CODE, productList.get(0).getInternalCatalogCode());
 		assertEquals(PRODUCT_PRICE, productList.get(0).getPrice());
 		assertTrue(productList.get(0).isVisible());
     }
 
 	@Test
     public void testGetProductIdByCatalogCode1() {
     	productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
     	assertTrue(productService.getProductIdByCatalogCode(PRODUCT_INTERNAL_CODE) != 0);
     }
     
     @Test(expected = NoResultException.class)
     public void testGetProductIdByCatalogCode2() {
     	assertTrue(productService.getProductIdByCatalogCode(PRODUCT_INTERNAL_CODE) != 0);
     }
     
     @Test
     public void shouldNotUpdateDefaultItemForProduct() {
     	productService.updateDefaultItem(PRODUCT_ID_1, ITEM_ID);
     }
     
     @Test
     public void shouldUpdateDefaultItemForProduct() {
     	long productId = 
     			productService.createProduct(PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME, PRODUCT_INTERNAL_CODE, DUMMY_CODE, PRODUCT_PRICE);
     	
     	ProductDto productDto = productService.getProductForAdminCard(productId);
     	assertEquals(0, productDto.getDefaultItemId());
     	
     	productService.updateDefaultItem(productId, ITEM_ID);
     	
     	productDto = productService.getProductForAdminCard(productId);
     	assertEquals(ITEM_ID, productDto.getDefaultItemId());
     }
 }
