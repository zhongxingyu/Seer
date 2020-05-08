 package com.euroit.militaryshop.catalog;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 import com.euroit.militaryshop.adapter.MediaAdapter;
 import com.euroit.militaryshop.catalog.impl.CatalogExporterImpl;
 import com.euroit.militaryshop.catalog.impl.CatalogImporterImpl;
 import com.euroit.militaryshop.dto.CategoryDto;
 import com.euroit.militaryshop.dto.DictionaryEntryDto;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.enums.DictionaryName;
 import com.euroit.militaryshop.service.CategoryService;
 import com.euroit.militaryshop.service.DictionaryEntryService;
 import com.euroit.militaryshop.service.ItemService;
 import com.euroit.militaryshop.service.ProductService;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.google.appengine.api.files.FileService;
 import com.google.appengine.api.files.FileServiceFactory;
 import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalFileServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 
 /**
  * 
  * @author roman.tuchin
  *
  */
 @ContextConfiguration(locations = "classpath:integration-tests-context.xml")
 public class CatalogExportImportIT extends AbstractJUnit4SpringContextTests {
 	
 	private final LocalServiceTestHelper helper = 
 			new LocalServiceTestHelper(new LocalFileServiceTestConfig(),
 			new LocalDatastoreServiceTestConfig()
             	.setDefaultHighRepJobPolicyUnappliedJobPercentage(0.01f));
 	
 	private static final String COLOR_RED = "red";
 
 	private static final long CATEGORY_ID = 2;
 
 	private static final Logger LOG = LoggerFactory.getLogger(CatalogExportImportIT.class);
 
 	private static final long PRODUCT_ID = 10;
 	private static final String PRODUCT_FULL_NAME = "Full name";
 	private static final String PRODUCT_SHORT_NAME = "Short name";
 
 	private static final String PRODUCT_BIG_IMAGE_NAME = "bigImageName";
 	private static final String PRODUCT_SMALL_IMAGE_NAME = "smallImageName";
 	private static final String PRODUCT_INTERNAL_CATALOG_CODE = "SLFK_1234";
 
 	private static final String ITEM_SHORT_NAME = "itemShortName";
 
 	private static final long COLOR_RED_ID = 3;
 	private static final long MATERIAL_LEATHER_ID = 4;
 	private static final long SIZE_XL_ID = 5;
 	
 	private static final String MATERIAL_LEATHER = "Кожа";
 	private static final String SIZE_XL = "XL";
 
 	private static final float PRODUCT_PRICE = 1000;
 	
 	
 	private CatalogExporterImpl catalogExporter = new CatalogExporterImpl();
 	private CatalogImporterImpl catalogImporter = new CatalogImporterImpl();
 	
 	private DictionaryEntryService dictionaryEntryServiceMocked = mock(DictionaryEntryService.class);
 	private CategoryService categoryServiceMocked = mock(CategoryService.class);
 	private ProductService productServiceMocked = mock(ProductService.class);
 	private MediaAdapter mediaAdapterMocked = mock(MediaAdapter.class);
 	private FileService fileService = FileServiceFactory.getFileService();
 	
 	@Autowired
 	private DictionaryEntryService dictionaryEntryService;
 	@Autowired
 	private CategoryService categoryService;
 	@Autowired
 	private ProductService productService;
 	@Autowired
 	private MediaAdapter mediaAdapter;
 	@Autowired
 	private ItemService itemService;
 	
 	
 	
 	@Before
 	public void setUp() {
 		catalogExporter.setDictionaryEntryService(dictionaryEntryServiceMocked);
 		catalogExporter.setCategoryService(categoryServiceMocked);
 		catalogExporter.setProductService(productServiceMocked);
 		catalogExporter.setMediaAdapter(mediaAdapterMocked);
 		catalogExporter.setFileService(fileService);
 		
 		catalogImporter.setFileService(fileService);
 		catalogImporter.setCategoryService(categoryService);
 		catalogImporter.setDictionaryEntryService(dictionaryEntryService);
 		catalogImporter.setProductService(productService);
 		catalogImporter.setMediaAdapter(mediaAdapter);
 		catalogImporter.setItemService(itemService);
 		
 		helper.setUp();
 	}
 	
 	@After
 	public void tearDown() {
 		helper.tearDown();
 	}
 	
 	@Test
 	public void exportTest() throws IOException {
 		List<DictionaryEntryDto> fakeDictEntries = new ArrayList<>();
 		DictionaryEntryDto dictionaryEntry = new DictionaryEntryDto();
 		dictionaryEntry.setId(COLOR_RED_ID);
 		dictionaryEntry.setDictionaryName(DictionaryName.COLOR);
 		dictionaryEntry.setValue(COLOR_RED);
 		fakeDictEntries.add(dictionaryEntry);
 		
 		dictionaryEntry = new DictionaryEntryDto();
 		dictionaryEntry.setId(MATERIAL_LEATHER_ID);
 		dictionaryEntry.setDictionaryName(DictionaryName.MATERIAL);
 		dictionaryEntry.setValue(MATERIAL_LEATHER);
 		fakeDictEntries.add(dictionaryEntry);
 		
 		dictionaryEntry = new DictionaryEntryDto();
 		dictionaryEntry.setId(SIZE_XL_ID);
 		dictionaryEntry.setDictionaryName(DictionaryName.SIZE);
 		dictionaryEntry.setValue(SIZE_XL);
 		fakeDictEntries.add(dictionaryEntry);
 		
 		when(dictionaryEntryServiceMocked.getAllEntries()).thenReturn(fakeDictEntries);
 		
 		List<CategoryDto> categoryList = new ArrayList<CategoryDto>();
 		CategoryDto category = new CategoryDto("tshirts", "T-Shirts");
 		category.setId(CATEGORY_ID);
 		categoryList.add(category);
 		when(categoryServiceMocked.getAllCategories()).thenReturn(categoryList);
 		
 		List<ProductDto> productList = new ArrayList<ProductDto>();
 		ProductDto product = new ProductDto(PRODUCT_ID, PRODUCT_FULL_NAME, PRODUCT_SHORT_NAME);
 		
 		List<Long> categoryIds = new ArrayList<>();
 		categoryIds.add(CATEGORY_ID);
 		product.setCategoryIds(categoryIds);
 		
 		product.setBigImageName(PRODUCT_BIG_IMAGE_NAME);
 		product.setSmallImageName(PRODUCT_SMALL_IMAGE_NAME);
 		product.setInternalCatalogCode(PRODUCT_INTERNAL_CATALOG_CODE);
 		product.setVisible(true);
 		product.setPrice(PRODUCT_PRICE);
 		
 		List<MilitaryShopItemDto> items = new ArrayList<>();
 		MilitaryShopItemDto item = new MilitaryShopItemDto();
 		item.setShortName(ITEM_SHORT_NAME);
 		item.setColorId(COLOR_RED_ID);
 		item.setMaterialId(MATERIAL_LEATHER_ID);
 		item.setSizeId(SIZE_XL_ID);
 		items.add(item);
 		product.setItems(items);
 		productList.add(product);
 		
 		when(productServiceMocked.getAllProducts()).thenReturn(productList);
		when(productServiceMocked.getProductForAdminCard(PRODUCT_ID)).thenReturn(product);
 		
 		String exportedFileBokey = catalogExporter.exportAsJson();
 		
 		catalogImporter.importToDb(exportedFileBokey);
 		
 		final List<ProductDto> allProducts = productService.getAllProducts();
 		assertEquals(1, allProducts.size());
 		
 		ProductDto firstProduct = productService.getProductForAdminCard(allProducts.get(0).getId());
 		assertEquals(PRODUCT_FULL_NAME, firstProduct.getFullName());
 		assertEquals(PRODUCT_SHORT_NAME, firstProduct.getShortName());
 		assertEquals(PRODUCT_INTERNAL_CATALOG_CODE, firstProduct.getInternalCatalogCode());
 		assertEquals(1, firstProduct.getCategoryIds().size());
 		assertEquals(PRODUCT_PRICE, firstProduct.getPrice(), 0.01);
 		assertEquals(true, firstProduct.isVisible());
 	}
 }
