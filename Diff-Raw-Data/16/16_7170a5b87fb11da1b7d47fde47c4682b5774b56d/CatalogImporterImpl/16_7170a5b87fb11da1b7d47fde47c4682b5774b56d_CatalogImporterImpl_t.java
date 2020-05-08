 package com.euroit.militaryshop.catalog.impl;
 
 import com.euroit.militaryshop.adapter.MediaAdapter;
 import com.euroit.militaryshop.catalog.CatalogExporter;
 import com.euroit.militaryshop.catalog.CatalogImporter;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.enums.DictionaryName;
 import com.euroit.militaryshop.service.CategoryService;
 import com.euroit.militaryshop.service.DictionaryEntryService;
 import com.euroit.militaryshop.service.ItemService;
 import com.euroit.militaryshop.service.ProductService;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.files.AppEngineFile;
 import com.google.appengine.api.files.FileReadChannel;
 import com.google.appengine.api.files.FileService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.channels.Channels;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @SuppressWarnings("deprecation")
 public class CatalogImporterImpl implements CatalogImporter {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(CatalogImporterImpl.class);
 	
 	private DictionaryEntryService dictionaryEntryService;
 	private CategoryService categoryService;
 	private ProductService productService;
 	private FileService fileService;
 	private MediaAdapter mediaAdapter;
 	private ItemService itemService;
 	
 	private Map<Long, Long> oldNewDictionaryIdMap;
 	private Map<Long, Long> oldNewCategoryIdMap;
 	
 	@Override
 	public void importToDb(String catalogBlobKey) throws IOException {
 		if (!isCatalogEntitiesEmpty()) {
 			return;
 		}
 		
 		oldNewDictionaryIdMap = new HashMap<Long, Long>();
 		oldNewCategoryIdMap = new HashMap<Long, Long>();
 		
 		JsonNode rootNode = fetchJsonTreeFromBlobstore(catalogBlobKey)
 				.get(CatalogExporter.EXPORT_ROOT);
 		importDictionaries(rootNode.get(CatalogExporter.DICTIONARY_ELEM));
 		importCategories(rootNode.get(CatalogExporter.CATEGORY_ELEM));
 		importProducts(rootNode.get(CatalogExporter.PRODUCT_ELEM));
 	}
 
 	private void importDictionaries(JsonNode dictionariesNode) {
         LOG.info("Importing dictionaries");
 		for (JsonNode dictionaryNode : dictionariesNode) {
 			long oldId = dictionaryNode.get(CatalogExporter.DICTIONARY_ELEM_ID).asLong();
 			String elemName = dictionaryNode.get(CatalogExporter.DICTIONARY_ELEM_NAME).asText();
 			String elemValue = dictionaryNode.get(CatalogExporter.DICTIONARY_ELEM_VALUE).asText();
 			
 			long newId = 
 					dictionaryEntryService.createEntry(DictionaryName.valueOf(elemName), elemValue);
 			
 			oldNewDictionaryIdMap.put(oldId, newId);
 		}
 	}
 	
 	private void importCategories(JsonNode categoriesNode) {
         LOG.info("Importing categories");
 		for (JsonNode categoryNode : categoriesNode) {
 			long oldId = categoryNode.get(CatalogExporter.CATEGORY_ID).asLong();
 			String categoryCode = categoryNode.get(CatalogExporter.CATEGORY_CODE).asText();
 			String categoryDisplayName = categoryNode.get(CatalogExporter.CATEGORY_DISPLAY_NAME).asText();
 			
 			long newId = categoryService.createCategory(categoryDisplayName, categoryCode);
 			oldNewCategoryIdMap.put(oldId, newId);
 		}
 	}
 	
 	private void importProducts(JsonNode productsNode) throws IOException {
         LOG.info("Importing products");
 		for (JsonNode productNode : productsNode) {
 			ProductDto productDto = new ProductDto();
             String fullName = productNode.get(CatalogExporter.PRODUCT_FULL_NAME).asText();
             LOG.debug("Processing product: {}", fullName);
             productDto.setFullName(fullName);
 			productDto.setShortName(productNode.get(CatalogExporter.PRODUCT_SHORT_NAME).asText());
 			productDto.setInternalCatalogCode(
 					productNode.get(CatalogExporter.PRODUCT_INTERNAL_CATALOG_CODE).asText());
 			productDto.setCategoryIds(prepareCategoryIds(productNode.get(CatalogExporter.PRODUCT_CATEGORY_IDS)));
 			productDto.setPrice(productNode.get(CatalogExporter.PRODUCT_PRICE).floatValue());
 			productDto.setVisible(productNode.get(CatalogExporter.PRODUCT_VISIBLE).asBoolean());
 			
 			long productId = productService.createOrSave(productDto);
 			
 			//upload big image to blobstore
 
 			final JsonNode bigImageDataNode = productNode.get(CatalogExporter.PRODUCT_BIG_IMAGE_DATA);
 			
 			if (!bigImageDataNode.isNull()) {
				LOG.debug("Big image {} is going to be imported", productDto.getBigImageName());
 				mediaAdapter.uploadBase64EncodedMedia(bigImageDataNode.asText(), productDto.getBigImageName());
 			}
 			//upload small image to blobstore
 			final JsonNode smallImageDataNode = productNode.get(CatalogExporter.PRODUCT_SMALL_IMAGE_DATA);
 			
 			if (!smallImageDataNode.isNull()) {
				LOG.debug("Small image {} is going to be imported", productDto.getSmallImageName());
 				mediaAdapter.uploadBase64EncodedMedia(smallImageDataNode.asText(), productDto.getSmallImageName());
 			}
 			
 			//import of items
 			for (JsonNode productItemNode : productNode.get(CatalogExporter.PRODUCT_ITEMS)) {
 				MilitaryShopItemDto militaryShopItemDto = new MilitaryShopItemDto();
 				
 				militaryShopItemDto.setProductId(productId);
 				militaryShopItemDto.setShortName(productItemNode.get(CatalogExporter.ITEM_SHORT_NAME).asText());
 				
 				long newColorId = 
 						oldNewDictionaryIdMap.get(productItemNode.get(CatalogExporter.ITEM_COLOR_ID).asLong());
 				long newMaterialId = 
 						oldNewDictionaryIdMap.get(productItemNode.get(CatalogExporter.ITEM_MATERIAL_ID).asLong());
 				long newSizeId = 
 						oldNewDictionaryIdMap.get(productItemNode.get(CatalogExporter.ITEM_SIZE_ID).asLong());
 				
 				militaryShopItemDto.setColorId(newColorId);
 				militaryShopItemDto.setMaterialId(newMaterialId);
 				militaryShopItemDto.setSizeId(newSizeId);
 				
 				itemService.createOrSaveItem(militaryShopItemDto);
 			}
 		}
 	}
 
 	private List<Long> prepareCategoryIds(JsonNode categoryIdsNode) {
 		List<Long> categoryIds = new ArrayList<>();
 		LOG.debug("categoryIdsNode: {}", categoryIdsNode);
 		for (JsonNode categoryIdNode : categoryIdsNode) {
 			long newId = oldNewCategoryIdMap.get(categoryIdNode.asLong());
 			categoryIds.add(newId);
 		}
 		
 		return categoryIds;
 	}
 
 	private boolean isCatalogEntitiesEmpty() {
 		if (!dictionaryEntryService.hasNoEntries()) {
 			LOG.error("DictionaryEntry should be empty");
 			return false;
 		}
 		
 		if (!categoryService.hasNoEntries()) {
 			LOG.error("Category should be empty");
 			return false;
 		}
 		
 		if (!productService.hasNoEntries()) {
 			LOG.error("MilitaryShopProduct should be empty");
 			return false;
 		}
 		
 		return true;
 	}
 
 	public void setFileService(FileService fileService) {
 		this.fileService = fileService;
 	}
 
 	
 	public JsonNode fetchJsonTreeFromBlobstore(String catalogBlobKey) throws IOException {
 		
 		AppEngineFile appEngineFile = fileService.getBlobFile(new BlobKey(catalogBlobKey));
 		
 		JsonNode rootNode;
 		
 		try (FileReadChannel fileReadChannel = 
 				fileService.openReadChannel(appEngineFile, false);
 				InputStream inputStream = Channels.newInputStream(fileReadChannel)) {
 		
 			rootNode = new ObjectMapper().readTree(inputStream);
 		}
 		
 		return rootNode;
 	}
 
 	public void setDictionaryEntryService(
 			DictionaryEntryService dictionaryEntryService) {
 		this.dictionaryEntryService = dictionaryEntryService;
 	}
 
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 	public void setMediaAdapter(MediaAdapter mediaAdapter) {
 		this.mediaAdapter = mediaAdapter;
 	}
 
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 }
