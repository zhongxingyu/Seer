 package com.omartech.tdg.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.omartech.tdg.mapper.BaseFeatureMapper;
 import com.omartech.tdg.mapper.ItemSubPropertyMapper;
 import com.omartech.tdg.mapper.ProductMapper;
 import com.omartech.tdg.model.BaseFeature;
 import com.omartech.tdg.model.Item;
 import com.omartech.tdg.model.ItemSubProperty;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Product;
 import com.omartech.tdg.utils.SystemDefaultSettings;
 
 @Service
 public class ProductService {
 	@Autowired
 	private ProductMapper productMapper;
 	
 	@Autowired
 	private CategoryService categoryService;
 	
 	@Autowired
 	private ItemSubPropertyMapper itemSubPropertyMapper;
 	
 	@Autowired
 	private BaseFeatureMapper baseFeatureMapper;
 	
 	@Autowired
 	private ItemService itemService;
 
 	public Product getProductById(long id){
 		Product product =  productMapper.getProductById(id);
 		int cid = product.getProductTypeId();
 		String subImages = product.getSubImages();
 		if(subImages.length()>1){
 			List<String> images = new ArrayList<String>();
 			String tmps[] = subImages.split(";");
 			for(String tmp : tmps){
 				images.add(tmp);
 			}
 			product.setOtherImages(images);
 		}
 		String basicParams = product.getBasicParams();
 		if(basicParams.length() > 1){
 			Map<String, String> params = new HashMap<String, String>();
 			Map<String, String> paramsInEnglish = new HashMap<String, String>();
 			String tmps[] = basicParams.split(";");
 			for(String pair : tmps){
 				String tmp[] = pair.split(":");
 				String pid =  tmp[0];
 				String vid = tmp[1];
 				String inputOrSelect = tmp[2];
 				System.out.println(pid +" :  " + vid +" : "+inputOrSelect);
 				ItemSubProperty property = itemSubPropertyMapper.getItemSubPropertyByPid(Integer.parseInt(pid), cid);
 				String name = property.getPname();
 				String nameInEnglish = property.getEnglish();
 				if(inputOrSelect.equals("1")){
 					BaseFeature baseFeature = baseFeatureMapper.getBaseFeatureById(Integer.parseInt(vid));
 					String value = baseFeature.getName();
 					String valueInEnglish = property.getEnglish();
 					params.put(name, value);
 					paramsInEnglish.put(nameInEnglish, valueInEnglish);
 				}else{
 					params.put(name, vid);
 					paramsInEnglish.put(name, vid);
 				}
 			}
 			product.setBasicParamsMap(params);
 			product.setBasicParamsMapInEnglish(paramsInEnglish);
 		}
 		return product;
 	}
 	
 	public List<Product> getProductListByPage(Page page){
 		return productMapper.getProductListByPage(page);
 	}
 	
 	public List<Product> getProductsInCategoryByPage(int categoryId, Page page){
 		return productMapper.getProductsInCategoryByPage(categoryId,page);
 	}
 	
 	@Transactional
 	public void insertProduct(Product product){
 		String mainImage = product.getMainImage();
 		if(mainImage == null || mainImage.equals("") || mainImage.equals("undefined")){
 			product.setMainImage(SystemDefaultSettings.DEFAULTPRODUCTIMAGE);
 		}
		long sku = product.getId();
 		long productId = productMapper.insertProduct(product);
 		int hasChildren = product.getHasChildren();
 		if(hasChildren==0){
 			Item item = new Item();
			if(sku == 0){
				item.setSku(product.getId());
			}else{
				item.setSku(sku);
			}
 			item.setWholePrice(product.getWholePrice());
 			item.setAvailableQuantity(product.getAvailableQuantity());
 			item.setCategoryId(product.getProductTypeId());
 			item.setImage(product.getMainImage());
 			item.setMaximumAcceptQuantity(product.getMaximumAcceptQuantity());
 			item.setMinimumQuantity(product.getMinimumQuantity());
 			item.setName(product.getName());
 			item.setNameInChinese(product.getNameInChinese());
 			item.setPromotionPrice(product.getPromotionPrice());
 			item.setPromotionTime(product.getPromotionTime());
 			item.setRetailPrice(product.getRetailPrice());
 			item.setSafeStock(product.getSafeStock());
 			item.setFeatureJson("");
 			item.setSellerId(product.getSellerId());
 			item.setProductId(productId);
 			itemService.insertItem(item);
 		}
 	}
 	
 	public ProductMapper getProductMapper() {
 		return productMapper;
 	}
 
 	public void setProductMapper(ProductMapper productMapper) {
 		this.productMapper = productMapper;
 	}
 
 	void updateProduct(Product product){
 		
 	}
 
 	public CategoryService getCategoryService() {
 		return categoryService;
 	}
 
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 
 	public ItemSubPropertyMapper getItemSubPropertyMapper() {
 		return itemSubPropertyMapper;
 	}
 
 	public void setItemSubPropertyMapper(ItemSubPropertyMapper itemSubPropertyMapper) {
 		this.itemSubPropertyMapper = itemSubPropertyMapper;
 	}
 
 	public BaseFeatureMapper getBaseFeatureMapper() {
 		return baseFeatureMapper;
 	}
 
 	public void setBaseFeatureMapper(BaseFeatureMapper baseFeatureMapper) {
 		this.baseFeatureMapper = baseFeatureMapper;
 	}
 
 	public ItemService getItemService() {
 		return itemService;
 	}
 
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 	
 }
