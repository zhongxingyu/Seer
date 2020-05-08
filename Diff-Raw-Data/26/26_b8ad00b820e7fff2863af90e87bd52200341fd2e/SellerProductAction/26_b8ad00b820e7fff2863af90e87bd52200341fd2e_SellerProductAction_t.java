 package com.omartech.tdg.action.seller;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.mapper.ShopSettingMapper;
 import com.omartech.tdg.model.Brand;
 import com.omartech.tdg.model.Item;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Product;
 import com.omartech.tdg.model.ProductLine;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.model.ShopSetting;
 import com.omartech.tdg.service.BrandService;
 import com.omartech.tdg.service.ItemService;
 import com.omartech.tdg.service.ProductLineService;
 import com.omartech.tdg.service.ProductParameterService;
 import com.omartech.tdg.service.ProductService;
 import com.omartech.tdg.utils.JsonMessage;
import com.omartech.tdg.utils.OrderStatus;
 import com.omartech.tdg.utils.ProductStatus;
 import com.omartech.tdg.utils.TimeFormat;
 
 @Controller
 @RequestMapping(value="/seller/product/")
 public class SellerProductAction {
 
 	@Autowired
 	private ProductLineService productLineService;
 	@Autowired
 	private ProductService productService;
 	@Autowired
 	private BrandService brandService;
 	@Autowired
 	private ShopSettingMapper shopSettingMapper;
 	@Autowired
 	private ProductParameterService productParameterService;
 	
 	@Autowired
 	private ItemService itemService;
 	
 //	private Logger logger = Logger.getLogger(SellerProductAction.class);
 	
 	@RequestMapping("/edit")
 	public ModelAndView editProduct(@RequestParam int id, HttpSession session){
 		Seller seller = (Seller)session.getAttribute("seller");
 		int sellerId = seller.getId();
 		Product product = productService.getProductById(id);
 		List<Brand> brands = brandService.getBrandListBySellerId(sellerId);
 		return new ModelAndView("/seller/product/product-edit").addObject("product", product).addObject("brands", brands);
 	}
 	@RequestMapping("/quickedit")
 	public ModelAndView quickEditProduct(@RequestParam int id, HttpSession session){
 		Seller seller = (Seller)session.getAttribute("seller");
 		int sellerId = seller.getId();
 		Product product = productService.getProductById(id);
 		List<Brand> brands = brandService.getBrandListBySellerId(sellerId);
 		return new ModelAndView("/seller/product/product-quickedit").addObject("product", product).addObject("brands", brands);
 	}
 	@ResponseBody
 	@RequestMapping("delete")
 	public JsonMessage deleteProduct(@RequestParam int id){
 		productService.deleteProduct(id);
 		return new JsonMessage();
 	}
 	@ResponseBody
 	@RequestMapping(value="/quickupdate", method=RequestMethod.POST)
 	public JsonMessage quickUpdateProduct(@RequestParam int id,
 			@RequestParam(value="sku", required=false) String sku,
 			@RequestParam(value="mainImg", required=false) String mainImg,
 			@RequestParam(value="subImgs", required=false) String subImgs,
 			@RequestParam(value="retailPrice", required=false) Float retailPrice,
 			@RequestParam(value="promotionPrice", required=false) Float promotionPrice,
 			@RequestParam(value="promotionTime", required=false) String promotionTime,
 			@RequestParam(value="promotionEnd", required=false) String promotionEnd,
 			@RequestParam(value="wholePrice", required=false) Float wholePrice,
 			@RequestParam(value="iss", required=false) Integer iss,
 			@RequestParam(value="ifee", required=false) Integer ifee,
 			@RequestParam(value="idays", required=false) Integer idays,
 			@RequestParam(value="minimumQuantity", required=false) Integer minimumQuantity,
 			@RequestParam(value="maximumAcceptQuantity", required=false) Integer maximumAcceptQuantity,
 			@RequestParam(value="availableQuantity", required=false) Integer availableQuantity,
 			@RequestParam(value="safeStock", required=false) Integer safeStock,
 			@RequestParam(value = "netWeight", required = false) Float netWeight,
 			@RequestParam(value = "grossWeight", required = false) Float grossWeight,
 			@RequestParam(value = "sizeWithPackage", required = false) String sizeWithPackage,
 			@RequestParam(value="brandId", required = false, defaultValue = "0") Integer brandId
 			){
 		Product product = productService.getProductById(id);
 		if(sku!=null){
 			product.setSku(sku);
 		}
 		product.setMainImage(mainImg);
 		product.setSubImages(subImgs);
 		product.setRetailPrice(retailPrice);
 		product.setPromotionPrice(promotionPrice);
 		if(promotionTime !=null && promotionEnd != null){
 			product.setPromotionTime(TimeFormat.StringToDate(promotionTime));
 			product.setPromotionEnd(TimeFormat.StringToDate(promotionEnd));
 		}
 		product.setWholePrice(wholePrice);
 		product.setInternationalShippingService(iss);
 		if(iss == 1){
 			product.setInternationalShippingFee(ifee);
 			product.setInternationalPromiseDays(idays);
 		}
 		product.setMinimumQuantity(minimumQuantity);
 		product.setMaximumAcceptQuantity(maximumAcceptQuantity);
 		product.setAvailableQuantity(availableQuantity);
 		product.setSafeStock(safeStock);
 		product.setNetWeight(netWeight);
 		product.setGrossWeight(grossWeight);
 		product.setSizeWithPackage(sizeWithPackage);
 		product.setBrandId(brandId);
 		
 		productService.quickUpdateProduct(product);
 		
 		return new JsonMessage(true, "success updated");
 	}
 	@ResponseBody
 	@RequestMapping(value = "/update", method=RequestMethod.POST)
 	public JsonMessage updateProduct(
 			@RequestParam int id,
 			/**
 			 * 不会变更状态的属性
 			 */
 			@RequestParam(value="sku", required=false) String sku,
 			@RequestParam(value="mainImg", required=false) String mainImg,
 			@RequestParam(value="subImgs", required=false) String subImgs,
 			@RequestParam(value="retailPrice", required=false) Float retailPrice,
 			@RequestParam(value="promotionPrice", required=false) Float promotionPrice,
 			@RequestParam(value="promotionTime", required=false) String promotionTime,
 			@RequestParam(value="promotionEnd", required=false) String promotionEnd,
 			@RequestParam(value="wholePrice", required=false) Float wholePrice,
 			@RequestParam(value="hasChildren", required=false) Integer hasChildren,
 			@RequestParam(value="iss", required=false) Integer iss,
 			@RequestParam(value="ifee", required=false) Integer ifee,
 			@RequestParam(value="idays", required=false) Integer idays,
 			@RequestParam(value="minimumQuantity", required=false) Integer minimumQuantity,
 			@RequestParam(value="maximumAcceptQuantity", required=false) Integer maximumAcceptQuantity,
 			@RequestParam(value="availableQuantity", required=false) Integer availableQuantity,
 			@RequestParam(value="safeStock", required=false) Integer safeStock,
 			@RequestParam(value = "netWeight", required = false) Float netWeight,
 			@RequestParam(value = "grossWeight", required = false) Float grossWeight,
 			@RequestParam(value = "sizeWithPackage", required = false) String sizeWithPackage,
 			@RequestParam(value="brandId", required = false, defaultValue = "0") Integer brandId,
 			/**
 			 * 会变更状态的属性
 			 */
 			@RequestParam(value="name", required=false) String name,
 			@RequestParam(value="description", required=false) String description,
 			
 			
 			HttpServletRequest request,
 			HttpSession session
 			){
 		Product product = productService.getProductById(id);
 		if(sku!=null){
 			product.setSku(sku);
 		}
 		product.setMainImage(mainImg);
 		product.setSubImages(subImgs);
 		product.setRetailPrice(retailPrice);
 		product.setPromotionPrice(promotionPrice);
 		if(promotionTime !=null && promotionEnd != null){
 			product.setPromotionTime(TimeFormat.StringToDate(promotionTime));
 			product.setPromotionEnd(TimeFormat.StringToDate(promotionEnd));
 		}
 		product.setWholePrice(wholePrice);
 		product.setInternationalShippingService(iss);
 		if(iss == 1){
 			product.setInternationalShippingFee(ifee);
 			product.setInternationalPromiseDays(idays);
 		}
 		product.setMinimumQuantity(minimumQuantity);
 		product.setMaximumAcceptQuantity(maximumAcceptQuantity);
 		product.setAvailableQuantity(availableQuantity);
 		product.setSafeStock(safeStock);
 		product.setNetWeight(netWeight);
 		product.setGrossWeight(grossWeight);
 		product.setSizeWithPackage(sizeWithPackage);
 		product.setBrandId(brandId);
 		
 		/**
 		 * 会导致状态变更的属性
 		 */
 		product.setName(name);
 		product.setDescription(description);
 		
 		/**
 		 * 重要变更，如hasChildren
 		 */
 		int oldHasChildren = product.getHasChildren();
 		if(oldHasChildren != hasChildren){
 			if(hasChildren == 0){
 				itemService.deleteItemByProductId(id);
 				itemService.insertItemAsProduct(product);
 				
 			}else if(hasChildren == 1){
 				itemService.deleteItemByProductId(id);
 				product.setSku("");
 			}else{
 				return new JsonMessage(false, "wrong hasChildren");
 			}
 			product.setHasChildren(hasChildren);
 		}
 
 		productService.slowUpdateProduct(product);
 		
 		return new JsonMessage(true, "success updated");
 	}
 	
 	
 	//通向产品分类选择页面，选择category
 	@RequestMapping(value="category-select")
 	public ModelAndView categorySelect(Locale locale){
 		List<ProductLine> productLines = productLineService.getProductLinesByParentId(0);
 		return new ModelAndView("/seller/product/category-select").addObject("productLines", productLines).addObject("locale", locale);
 	}
 	//提交category，开始进入产品详细页面
 	@RequestMapping(value="productadd")
 	public ModelAndView selectCategory(
 			@RequestParam(value="productLine") int productLine,
 			@RequestParam(value="categoryId") int categoryId,
 			@RequestParam(value="nodeId", required=false) Integer nodeId,
 			HttpSession session
 			){
 		Seller seller = (Seller) session.getAttribute("seller");
 		int sellerId = seller.getId();
 		//获取对应的销售属性
 		List<Brand> brands = brandService.getBrandListBySellerId(sellerId);
 		return new ModelAndView("/seller/product/product-add")
 			.addObject("productLine", productLine)
 			.addObject("categoryId", categoryId)
 			.addObject("nodeId", nodeId)
 			.addObject("brands", brands);
 	}
 	@ResponseBody
 	@RequestMapping(value="addproduct", method=RequestMethod.POST)
 	public JsonMessage addProduct(
 			@RequestParam String name,
 			@RequestParam(value="sku", required=false) String sku,
 			@RequestParam int categoryId,
 			@RequestParam(value="nodeId", required=false) Integer nodeId,
 			@RequestParam int productLine,
 			@RequestParam int iss,
 			@RequestParam(value="ifee", required=false) Integer ifee,
 			@RequestParam(value="idays", required=false) Integer idays,
 			@RequestParam int hasChildren,//0:no,1:yes
 			@RequestParam String mainImg,
 			@RequestParam String subImgs,
 			@RequestParam float retailPrice,
 			@RequestParam(value="promotionPrice", required=false) Float promotionPrice,
 			@RequestParam(value="promotionTime", required=false) String promotionTime,
 			@RequestParam(value="promotionEnd", required=false) String promotionEnd,
 			@RequestParam(value="wholePrice", required=false) Float wholePrice,
 			@RequestParam(value="minimumQuantity", required=false, defaultValue="0") Integer minimumQuantity,
 			@RequestParam(value="maximumAcceptQuantity", required=false, defaultValue = "0") Integer maximumAcceptQuantity,
 			@RequestParam int availableQuantity,
 			@RequestParam int safeStock,
 			@RequestParam(value = "netWeight", required = false) Float netWeight,
 			@RequestParam(value = "grossWeight", required = false) Float grossWeight,
 			@RequestParam(value = "sizeWithPackage", required = false) String sizeWithPackage,
 			@RequestParam(value="brandId", required = false, defaultValue="0") Integer brandId,
 			@RequestParam String description,
 			HttpServletRequest request,
 			HttpSession session,
 			Locale locale
 			){
 		JsonMessage message = new JsonMessage();
 		Seller seller = (Seller) session.getAttribute("seller");
 		int pId = 0;
 		int internationalShippingService = iss;
 		int internationalShippingFee = 0;
 		int internationalPromiseDays = 0;
 		if(internationalShippingService == 1){
 			internationalShippingFee = ifee;
 			internationalPromiseDays = idays;
 		}
 		int status = ProductStatus.InProductCreation;
 		int sellerId = seller.getId();
 		ShopSetting shopSetting = shopSettingMapper.getShopSettingBySellerId(sellerId);
 		if(shopSetting == null){
 			message.setFlag(false);
 			if(locale.equals("zh_CN")){
 				message.setObject("请先设置店铺中的默认货币");
 			}else{
 				message.setObject("Please init the coinage of shopsetting.");
 			}
 			return message;
 		}
 		int defaultCoinage = shopSetting.getDefaultCoinage();//设定货币
 		int productTypeId = categoryId;
 		
 		if(nodeId != null){
 			productTypeId = nodeId;
 		}
 		
 		Date proBegin = TimeFormat.StringToDate(promotionTime);
 		Date proEnd = TimeFormat.StringToDate(promotionEnd);
 		
 		int countryCode = shopSetting.getShippingCountry();
 		
 		if(promotionPrice == null){
 			promotionPrice = 0f;
 		}
 		if(wholePrice == null){
 			wholePrice = 0f;
 			minimumQuantity = 0;
 		}
 		if(maximumAcceptQuantity == null){
 			maximumAcceptQuantity = 0;
 		}
 		
 		Product product = new Product(pId, sku, name,  description,
 				 hasChildren,  status,  internationalShippingService,
 				 internationalShippingFee,  internationalPromiseDays,
 				 retailPrice,  promotionPrice, proBegin,
 				 proEnd,  wholePrice,  minimumQuantity,
 				 maximumAcceptQuantity,  defaultCoinage,  availableQuantity,
 				 safeStock,  netWeight,  grossWeight,
 				 sizeWithPackage,  mainImg,  subImgs,
 				 brandId,  sellerId,  productTypeId,  productLine, countryCode);
 		
 		int productId = productService.insertProduct(product);
 		
 		message.setFlag(true);
 		message.setObject(productId);
 		return message;
 	}
 	
 	
 	
 	
 	
 	@RequestMapping(value="list")
 	public ModelAndView list(@RequestParam(value="pageNo", defaultValue= "0", required = false) int pageNo, 
 			@RequestParam(value="pageSize", defaultValue = "10", required = false) int pageSize,
 			HttpSession session){
 		Page page = new Page(pageNo,pageSize);
 		Seller seller = (Seller) session.getAttribute("seller");
 		
 		List<Product> products = productService.getProductListByPageAndSeller(page, seller.getId());
 		
 		return new ModelAndView("/seller/product/product-list").addObject("products", products).addObject("pageNo", pageNo);
 	}
 	
 	@RequestMapping(value="itemadd")
 	public ModelAndView itemAdd(
 			@RequestParam int productId
 			){
 		Product product = productService.getProductById(productId);
 		
 		return new ModelAndView("/seller/product/item-add")
 				.addObject("product", product);
 	}
 	@RequestMapping(value="addItem")
 	public String addItem(
 			@RequestParam int productId,
 			@RequestParam(value="sku", required=false) String sku,
 			@RequestParam(value="mainImg", required=false) String mainImg,
 			@RequestParam(value="params", required=false) String params,
 			@RequestParam float retailPrice,
 			@RequestParam(value="promotionPrice", required=false, defaultValue="0") Float promotionPrice,
 			@RequestParam(value="promotionTime", required=false) String promotionTime,
 			@RequestParam(value="promotionEnd", required=false) String promotionEnd,
 			@RequestParam(value="wholePrice", required=false, defaultValue="0") Float wholePrice,
 			@RequestParam(value="minimumQuantity", required=false, defaultValue="0") Integer minimumQuantity,
 			@RequestParam(value="maximumAcceptQuantity", required=false, defaultValue="0") Integer maximumAcceptQuantity,
 			@RequestParam Integer availableQuantity,
 			@RequestParam Integer safeStock,
 			HttpSession session
 			){
 		Seller seller = (Seller) session.getAttribute("seller");
 		int sellerId = seller.getId();
 		if(params.length()>1){
 			Item item = new Item();
 			Product product = productService.getProductById(productId);
 			item.setSku(sku);
 			item.setImage(mainImg);
 			item.setName(product.getName());
 			item.setNameInChinese(product.getNameInChinese());
 			item.setAvailableQuantity(availableQuantity);
 			item.setMaximumAcceptQuantity(maximumAcceptQuantity);
 			item.setMinimumQuantity(minimumQuantity);
 			if(promotionPrice != null){
 				if(promotionTime == null || promotionEnd == null){
 					Date begin = product.getPromotionTime();
 					Date end = product.getPromotionEnd();
 					if(begin !=null && end !=null){
 						item.setPromotionPrice(promotionPrice);
 						item.setPromotionTime(begin);
 						item.setPromotionEnd(end);
 					}
 				}else{
 					item.setPromotionPrice(promotionPrice);
 					item.setPromotionTime(TimeFormat.StringToDate(promotionTime));
 					item.setPromotionEnd(TimeFormat.StringToDate(promotionEnd));
 				}
 			}
 			item.setInternationalShippingService(product.getInternationalShippingService());
 			item.setInternationalPromiseDays(product.getInternationalPromiseDays());
 			item.setInternationalShippingFee(product.getInternationalShippingFee());
 			item.setRetailPrice(retailPrice);
 			item.setSafeStock(safeStock);
 			item.setWholePrice(wholePrice);
 			item.setFeatureJson(params);
 			item.setCoinage(product.getCoinage());
 			item.setProductId(productId);
 			item.setCategoryId(product.getProductTypeId());
 			item.setProductLineId(product.getProductLine());
 			item.setSellerId(sellerId);
 			itemService.insertItem(item);
 		}
 		return "redirect:/seller/product/list";
 	}
 	@RequestMapping("/changestatus")
 	public String changeProductStatus(@RequestParam int productId, @RequestParam int status){
		if(status == ProductStatus.Sellable){
			//开始销售时，若无子产品，跳转到错误页面
			Product product = productService.getProductById(productId);
			int hasChild = product.getHasChildren();
			List<Item> items = product.getItems();
			if(hasChild == 1){
				if(items == null || items.size() == 0){
					return "redirect:/seller/error/productHasNoChildren";
				}
 			}
 		}
 		productService.updateProductStatus(productId, status);
 		return "redirect:/seller/product/list";
 	}
 	
 	public ProductService getProductService() {
 		return productService;
 	}
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 	public ShopSettingMapper getShopSettingMapper() {
 		return shopSettingMapper;
 	}
 	public void setShopSettingMapper(ShopSettingMapper shopSettingMapper) {
 		this.shopSettingMapper = shopSettingMapper;
 	}
 	public ProductLineService getProductLineService() {
 		return productLineService;
 	}
 	public void setProductLineService(ProductLineService productLineService) {
 		this.productLineService = productLineService;
 	}
 	public ItemService getItemService() {
 		return itemService;
 	}
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 	public ProductParameterService getProductParameterService() {
 		return productParameterService;
 	}
 	public void setProductParameterService(
 			ProductParameterService productParameterService) {
 		this.productParameterService = productParameterService;
 	}
 	public BrandService getBrandService() {
 		return brandService;
 	}
 	public void setBrandService(BrandService brandService) {
 		this.brandService = brandService;
 	}
 }
