 package com.omartech.tdg.action.seller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.mapper.BrandMapper;
 import com.omartech.tdg.mapper.ShopSettingMapper;
 import com.omartech.tdg.model.Brand;
 import com.omartech.tdg.model.Item;
 import com.omartech.tdg.model.ItemProperty;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Product;
 import com.omartech.tdg.model.ProductCategory;
 import com.omartech.tdg.model.SaleProperty;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.model.ShopSetting;
 import com.omartech.tdg.service.CategoryService;
 import com.omartech.tdg.service.ItemPropertyService;
 import com.omartech.tdg.service.ItemService;
 import com.omartech.tdg.service.ProductService;
 import com.omartech.tdg.service.SalePropertyService;
 import com.omartech.tdg.utils.TimeFormat;
 
 @Controller
 @RequestMapping(value="/seller/product/")
 public class SellerProductAction {
 
 	@Autowired
 	private CategoryService categoryService;
 	@Autowired
 	private ProductService productService;
 	@Autowired
 	private BrandMapper brandMapper;
 	@Autowired
 	private ShopSettingMapper shopSettingMapper;
 	@Autowired
 	private ItemPropertyService itemPropertyService;
 	@Autowired
 	private SalePropertyService salePropertyService;
 	
 	@Autowired
 	private ItemService itemService;
 	
 	private Logger logger = Logger.getLogger(SellerProductAction.class);
 	
 	//通向产品分类选择页面，选择category
 	@RequestMapping(value="category-select")
 	public ModelAndView categorySelect(){
 		List<ProductCategory> categories = categoryService.getProductCategories();
 		logger.info("categories list size is :"+categories.size());
 		return new ModelAndView("/seller/product/category-select").addObject("categories", categories);
 	}
 	//提交category，开始进入产品详细页面
 	@RequestMapping(value="productadd")
 	public ModelAndView selectCategory(
 			@RequestParam(value="cid") int categoryId){
 		//获取对应的销售属性
 		List<Brand> brands = brandMapper.getBrandList();
 		ItemProperty itemProperty = itemPropertyService.getItemPropertyByCategoryId(categoryId);
 		int hasSkuItem = 1;
 		SaleProperty saleProperty = salePropertyService.getSalePropertyByCategoryId(categoryId);
 		if(saleProperty == null || saleProperty.getSubProperties() == null || saleProperty.getSubProperties().size() == 0){
 			hasSkuItem = 0;
 		}
 		return new ModelAndView("/seller/product/product-add")
 			.addObject("categoryId", categoryId)
 			.addObject("brands", brands)
 			.addObject("itemProperties", itemProperty)
 			.addObject("hasSkuItem", hasSkuItem);
 	}
 	@RequestMapping(value="addproduct", method=RequestMethod.POST)
 	public String addProduct(
 			@RequestParam String name,
			@RequestParam(value="sku", required=false, defaultValue = "0") int sku,
 			@RequestParam int categoryId,
 			@RequestParam float retailPrice,
 			@RequestParam float promotionPrice,
 			@RequestParam String promotionTime,
 			@RequestParam float wholePrice,
 			@RequestParam int minimumQuantity,
 			@RequestParam int maximumAcceptQuantity,
 			@RequestParam int availableQuantity,
 			@RequestParam int safeStock,
 			@RequestParam(value = "netWeight", required = false) float netWeight,
 			@RequestParam(value = "grossWeight", required = false) float grossWeight,
 			@RequestParam(value = "sizeWithPackage", required = false) String sizeWithPackage,
 			@RequestParam(value="brandId", required = false, defaultValue = "0") int brandId,
 			@RequestParam String description,
 			@RequestParam String mainImg,
 			@RequestParam String subImgs,
 			@RequestParam String params,
 			@RequestParam int hasChildren,//0:no,1:yes
 			HttpServletRequest request,
 			HttpSession session
 			){
 		Seller seller = (Seller) session.getAttribute("seller");
 		int sellerId = seller.getId();
 		ShopSetting shopSetting = shopSettingMapper.getShopSettingBySellerId(sellerId);
 		if(shopSetting == null){
 			return "redirect:/seller/shopsetting/show";
 		}
 		int defaultCoinage = shopSetting.getDefaultCoinage();//设定货币
 		ProductCategory category  = categoryService.findRootCategory(categoryId);
 		int categoryRootId = category.getRoot();//设定大分类
 		Product product = new Product();
		product.setId(sku);//用id暂存sku
 		product.setName(name);
 		product.setMainImage(mainImg);
 		product.setSubImages(subImgs);
 		product.setRetailPrice(retailPrice);
 		product.setPromotionPrice(promotionPrice);
 		product.setPromotionTime(TimeFormat.StringToDate(promotionTime));
 		product.setWholePrice(wholePrice);
 		product.setMinimumQuantity(minimumQuantity);
 		product.setMaximumAcceptQuantity(maximumAcceptQuantity);
 		product.setAvailableQuantity(availableQuantity);
 		product.setSafeStock(safeStock);
 		product.setNetWeight(netWeight);
 		product.setGrossWeight(grossWeight);
 		product.setSizeWithPackage(sizeWithPackage);
 		product.setBrandId(brandId);
 		product.setDescription(description);
 		product.setProductTypeId(categoryId);
 		product.setHasChildren(hasChildren);
 		product.setBasicParams(params);
 		product.setSellerId(sellerId);
 		product.setCoinage(defaultCoinage);
 		product.setCategoryId(categoryRootId);
 		productService.insertProduct(product);
 		return "redirect:/seller/product/list";
 	}
 	
 	
 	@RequestMapping(value="list")
 	public ModelAndView list(@RequestParam(value="pageNo", defaultValue= "0", required = false) int pageNo, @RequestParam(value="pageSize", defaultValue = "10", required = false) int pageSize){
 		Page page = new Page(pageNo,pageSize);
 		
 		List<Product> products = productService.getProductListByPage(page);
 		
 		return new ModelAndView("/seller/product/product-list").addObject("products", products).addObject("pageNo", pageNo);
 	}
 	
 	@RequestMapping(value="subcategory")
 	@ResponseBody
 	public List<ProductCategory> getSubCategoriesByCid(@RequestParam(value="cid") int cid){
 		List<ProductCategory> subCategories = categoryService.getSubProductCategories(cid);
 		return subCategories;
 	}
 	@RequestMapping(value="itemadd")
 	public ModelAndView itemAdd(
 			@RequestParam int productId
 			){
 		Product product = productService.getProductById(productId);
 		int categoryId = product.getProductTypeId();
 		SaleProperty saleProperty = salePropertyService.getSalePropertyByCategoryId(categoryId);
 		return new ModelAndView("/seller/product/item-add").addObject("saleProperty", saleProperty)
 				.addObject("cid", categoryId).addObject("product", product);
 	}
 	@RequestMapping(value="addMultiItem")
 	public String addMultiItem(
 			@RequestParam int productId,
 			@RequestParam String params,
 			@RequestParam float retailPrice,
 			@RequestParam float promotionPrice,
 			@RequestParam String promotionTime,
 			@RequestParam float wholePrice,
 			@RequestParam int minimumQuantity,
 			@RequestParam int maximumAcceptQuantity,
 			@RequestParam int availableQuantity,
 			@RequestParam int safeStock
 			){
 		if(params.length()>1){
 			Item item = new Item();
 			String tmps[] = params.split("\\|"); 
 			for(String tmp : tmps){
 				System.out.println(tmp);
 				item.setAvailableQuantity(availableQuantity);
 				item.setMaximumAcceptQuantity(maximumAcceptQuantity);
 				item.setMinimumQuantity(minimumQuantity);
 				item.setPromotionPrice(promotionPrice);
 				item.setPromotionTime(TimeFormat.StringToDate(promotionTime));
 				item.setRetailPrice(retailPrice);
 				item.setSafeStock(safeStock);
 				item.setWholePrice(wholePrice);
 				item.setFeatureJson(tmp);
 				item.setProductId(productId);
 				itemService.insertItem(item);
 			}
 		}
 		return "redirect:/seller/product/list";
 	}
 	@RequestMapping(value="addItem")
 	public String addItem(
 			@RequestParam int productId,
 			@RequestParam(value="sku", required=false, defaultValue="0") int sku,
 			@RequestParam String params,
 			@RequestParam float retailPrice,
 			@RequestParam float promotionPrice,
 			@RequestParam String promotionTime,
 			@RequestParam float wholePrice,
 			@RequestParam int minimumQuantity,
 			@RequestParam int maximumAcceptQuantity,
 			@RequestParam int availableQuantity,
 			@RequestParam int safeStock,
 			HttpSession session
 			){
 		Seller seller = (Seller) session.getAttribute("seller");
 		int sellerId = seller.getId();
 		if(params.length()>1){
 			Item item = new Item();
 			Product product = productService.getProductById(productId);
 			System.out.println(params);
 			item.setSku(sku);
 			item.setName(product.getName());
 			item.setNameInChinese(product.getNameInChinese());
 			item.setAvailableQuantity(availableQuantity);
 			item.setMaximumAcceptQuantity(maximumAcceptQuantity);
 			item.setMinimumQuantity(minimumQuantity);
 			item.setPromotionPrice(promotionPrice);
 			item.setPromotionTime(TimeFormat.StringToDate(promotionTime));
 			item.setRetailPrice(retailPrice);
 			item.setSafeStock(safeStock);
 			item.setWholePrice(wholePrice);
 			item.setFeatureJson(params);
 			item.setCoinage(product.getCoinage());
 			item.setProductId(productId);
 			item.setCategoryId(product.getProductTypeId());
 			item.setSellerId(sellerId);
 			itemService.insertItem(item);
 		}
 		return "redirect:/seller/product/list";
 	}
 	
 	
 	public CategoryService getCategoryService() {
 		return categoryService;
 	}
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 	public ProductService getProductService() {
 		return productService;
 	}
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 	public BrandMapper getBrandMapper() {
 		return brandMapper;
 	}
 	public void setBrandMapper(BrandMapper brandMapper) {
 		this.brandMapper = brandMapper;
 	}
 	public ShopSettingMapper getShopSettingMapper() {
 		return shopSettingMapper;
 	}
 	public void setShopSettingMapper(ShopSettingMapper shopSettingMapper) {
 		this.shopSettingMapper = shopSettingMapper;
 	}
 	public ItemPropertyService getItemPropertyService() {
 		return itemPropertyService;
 	}
 	public void setItemPropertyService(ItemPropertyService itemPropertyService) {
 		this.itemPropertyService = itemPropertyService;
 	}
 	public SalePropertyService getSalePropertyService() {
 		return salePropertyService;
 	}
 	public void setSalePropertyService(SalePropertyService salePropertyService) {
 		this.salePropertyService = salePropertyService;
 	}
 }
