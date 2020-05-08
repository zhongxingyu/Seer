 package com.omartech.tdg.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.omartech.tdg.exception.OutOfStockException;
 import com.omartech.tdg.mapper.ItemMapper;
 import com.omartech.tdg.mapper.ShopSettingMapper;
 import com.omartech.tdg.model.Coinage;
 import com.omartech.tdg.model.Item;
 import com.omartech.tdg.model.Product;
 import com.omartech.tdg.model.ShopSetting;
 import com.omartech.tdg.service.seller.SellerAuthService;
 import com.omartech.tdg.utils.PricePair;
 import com.omartech.tdg.utils.ProductStatus;
 import com.omartech.tdg.utils.SystemDefaultSettings;
 
 @Service
 public class ItemService {
 	@Autowired
 	private ItemMapper itemMapper;
 
 	@Autowired
 	private ProductService productService;
 	
 	@Autowired
 	private ShopSettingMapper shopSettingMapper;
 	
 	@Autowired
 	private SellerAuthService sellerAuthService;
 	
 	@Autowired
 	private EmailService emailService;
 	
 	/**
 	 * 当更新的产品为无子产品商品时，需要对应更新其item
 	 */
 	public void updateSingleProductItem(Product product){
 		int productId = product.getId();
 		List<Item> items = getItemsByProductId(productId);
 		if(items.size()==1){
 			Item item = items.get(0);
 			item.setRetailPrice(product.getRetailPrice());
 			item.setPromotionPrice(product.getPromotionPrice());
 			item.setPromotionTime(product.getPromotionTime());
 			item.setPromotionEnd(product.getPromotionEnd());
 			item.setWholePrice(product.getWholePrice());
 			item.setMinimumQuantity(product.getMinimumQuantity());
 			item.setMaximumAcceptQuantity(product.getMaximumAcceptQuantity());
 			item.setAvailableQuantity(product.getAvailableQuantity());
 			item.setSafeStock(product.getSafeStock());
 			item.setImage(product.getMainImage());
 			item.setInternationalShippingService(product.getInternationalShippingService());
 			item.setInternationalShippingFee(product.getInternationalShippingFee());
 			item.setInternationalPromiseDays(product.getInternationalPromiseDays());
 			item.setSku(product.getSku());
 			item.setActive(product.getActive());
 			item.setSellable(product.getSellable());
 			simpleUpdateItem(item);
 		}
 	}
 	
 	/**
 	 * 删除该productId下的所有item
 	 * @param productId
 	 */
 	public void deleteItemByProductId(int productId){
 		List<Item> items = getItemsByProductId(productId);
 		for(Item item : items){
 			deleteItem(item.getId());
 		}
 	}
 	/**
 	 * 无子产品的商品自动插入一个自身的单品
 	 * @param product
 	 */
 	public void insertItemAsProduct(Product product){
 		Item item = new Item(product);
 		insertItem(item);
 	}
 	
 	/**
 	 * 减少该项对应的库存，若可购买量低于购买数，报错
 	 * @param itemId
 	 * @param count
 	 */
 	public void reduceStock(int itemId, int count){
 		Item item = getItemById(itemId);
		if(item == null){
			throw new OutOfStockException();
		}
 		int available = item.getAvailableQuantity();
 		int avail = available - count;
 		if(avail >= 0){
 			/**
 			 * 1.更新库存
 			 * 2.根据剩余库存判断情况
 			 * @param itemId
 			 * @param availablestock
 			 */
 			item.setAvailableQuantity(avail);
 			int productId = item.getProductId();
 			Product product = productService.getSimpleProductById(productId);
 			if(product.getHasChildren() == 0){
 				productService.updateStock(product, avail);
 			}
 			if(avail == 0){
 				item.setSellable(ProductStatus.Unsellable);
 			}
 			
 			int sellerId = item.getSellerId();
 			int safeStock = item.getSafeStock();//卖家自己的安全库存
 			if(avail <= SystemDefaultSettings.SystemSafeStock || avail <= safeStock){//低于系统安全库存or卖家自己库存，报警
 				item.setActive(Product.UnSafeStock);
 				emailService.sendEmailWhenNearlyOutofStock(sellerId, productId);
 			}
 			simpleUpdateItem(item);
 			if(avail == 0){//0库存，停售
 				//若该产品下所有子产品都不可售，则该商品停售
 				List<Item> items = getSellAbleItemsByProductId(productId);
 				boolean unsellableFlag = true;
 				for(Item tmp : items){
 					if(tmp.getId() == itemId){
 						continue;
 					}
 					if(tmp.getAvailableQuantity() > 0){
 						unsellableFlag = false;
 					}
 				}
 				if(unsellableFlag){
 					productService.updateProductSellable(productId, ProductStatus.Unsellable);
 					emailService.sendEmailWhenNearlyOutofStock(sellerId, productId);
 				}
 			}
 		}else{
 			throw new OutOfStockException();
 		}
 	}
 	
 	/**
 	 * 插入item，若该item与其他兄弟item同结构则返回true，否则返回false
 	 * @param item
 	 * @return
 	 */
 	@Transactional
 	public boolean insertItem(Item item) {
 		verifyStock(item);
 		item.setStatus(ProductStatus.OK);
 		int sellerId = item.getSellerId();
 		ShopSetting setting = shopSettingMapper.getShopSettingBySellerId(sellerId);
 		int countryCode = setting.getShippingCountry();
 		item.setCountryCode(countryCode);
 		boolean flag = compairWithBrothers(item, item.getProductId());
 		if(flag){
 			itemMapper.insertItem(item);
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	/**
 	 * 对比该item与其他兄弟item的featureJson结构是否一致
 	 * @param item
 	 * @param productId
 	 * @return
 	 */
 	private boolean compairWithBrothers(Item item, int productId){
 		List<Item> items = getItemsByProductIdAndStatus(productId, ProductStatus.OK);
 		String featureJson = item.getFeatureJson();
 		boolean flagColor = featureJson.contains("color");
 		boolean sizeColor = featureJson.contains("size");
 		if(items!=null && items.size() > 0){
 			for(Item tmp : items){
 				boolean colorTmp = tmp.getFeatureJson().contains("color");
 				if(flagColor ^ colorTmp){
 					return false;
 				}
 				boolean sizeTmp = tmp.getFeatureJson().contains("size");
 				if(sizeColor ^ sizeTmp){
 					return false;
 				}
 			}
 			return true;
 		}else{
 			return true;
 		}
 	}
 	
 	/**
 	 * 测试异或
 	 * @param args
 	 */
 	public  static void main(String[] args){
 		if((true ^ false)){
 			System.out.println("true & false");
 		}
 		if((false ^ false)){
 			System.out.println("false & false");
 		}
 		if((true ^ true)){
 			System.out.println("true & true");
 		}
 	}
 	
 	public float getPriceRMBByItemId(int id, int count){
 		float origin = getPriceByItemId(id, count);
 		Item item = getItemById(id);
 		if(item == null){
 			return 0;
 		}
 		int coinageId = item.getCoinage();
 		float result = Coinage.compute(coinageId, origin);//得到该产品对应的人民币价格
 		return result;
 	}
 	
 	public PricePair getPricePairByItemId(int id, int count){
 		float price = getPriceByItemId(id, count);
 		Item item = getItemById(id);
 		int coinageId = item.getCoinage();
 		float result = Coinage.compute(coinageId, price);//得到该产品对应的人民币价格
 		return new PricePair(price, result);
 	}
 	/**
 	 * 根据itemId和数量来返回对应的价格，是单价，不是乘上数量之后的价格
 	 * 返回原来的货币，美元、欧元等
 	 */
 	public float getPriceByItemId(int id, int count){
 		Item item = getItemById(id);
 		if(item == null){
 			return 0;
 		}
 		Date now = new Date(System.currentTimeMillis());
 		Date begin = item.getPromotionTime();
 		Date end = item.getPromotionEnd();
 		int min = item.getMinimumQuantity();
 		float retail = item.getRetailPrice();
 		float result = 0f;
 		float pifa = 0;
 		float pro = 0;
 		if(count >= min){//优先批发价，批发价只需要大于最低批发量即可
 			pifa = item.getWholePrice();
 		}
 		if(begin != null && end !=null){//如果在优惠期就用优惠价
 			boolean flag1 = !now.before(begin);
 			boolean flag2 = !end.before(now);
 			if(flag1 && flag2){// begin <= now <= end
 				pro = item.getPromotionPrice();
 			}
 		}
 		//先找pifa和pro中小的那个，如果为0，说明俩有一个是0；
 		//再找pifa和pro中最大的那个，若还为0，说明俩都是0；否则就返回这俩中不为0的那个与零售价的最低价
 		float tmp = min(pifa, pro);
 		if(tmp == 0){
 			float tmpp = max(pifa, pro);
 			if(tmpp == 0){
 				result = retail;
 			}else{
 				result =  min(retail, tmpp);
 			}
 		}else{
 			result = tmp;
 		}
 		return result;
 	}
 	public float min(float a, float b){
 		return a < b ? a : b;
 	}
 	public float max(float a, float b){
 		return a > b ? a : b;
 	}
 	public Item getItemBySku(String sku) {//for 卖家
 		Item item = itemMapper.getItemBySku(sku);
 		return item;
 	}
 	
 	public Item getItemById(int id){//for 系统
 		Item item = itemMapper.getItemById(id);
 		return item;
 	}
 	public List<Item> getItemsByProductId(int productId) {
 		List<Item> items = itemMapper.getItemsByProductIdAndStatus(productId, ProductStatus.OK);
 		return items;
 	}
 	public List<Item> getSellAbleItemsByProductId(int productId) {
 		List<Item> items = itemMapper.getSellAbleItemsByProductId(productId, ProductStatus.Sellable);
 		return items;
 	}
 	public List<Item> getItemsByProductIdAndStatus(int productId, int statusId){
 		List<Item> items = itemMapper.getItemsByProductIdAndStatus(productId, statusId);
 		return items;
 	}
 	
 	private void verifyStock(Item item){
 		if(item.getAvailableQuantity() > item.getSafeStock()){
 			item.setActive(Product.SafeStock);
 			item.setSellable(ProductStatus.Sellable);
 		}else if(item.getAvailableQuantity() == 0){
 			item.setSellable(ProductStatus.Unsellable);
 			item.setActive(Product.UnSafeStock);
 		}else{
 			item.setActive(Product.UnSafeStock);
 			item.setSellable(ProductStatus.Sellable);
 		}
 	}
 	
 	/**
 	 * 更新item，需要检验是否与兄弟item相同feature
 	 * @param item
 	 * @return
 	 */
 	public boolean updateItem(Item item){
 		boolean flag = compairWithBrothers(item, item.getProductId());
 		if(flag){
 			verifyStock(item);
 			itemMapper.updateItem(item);
 			return true;
 		}else{
 			return false;
 		}
 	}
 	/**
 	 * 不加验证，直接更新
 	 * @param item
 	 * @return
 	 */
 	public boolean simpleUpdateItem(Item item){
 		itemMapper.updateItem(item);
 		return true;
 	}
 	
 	/**
 	 * 改变item的状态为删除、恢复
 	 * @param itemId
 	 * @param statusId
 	 */
 	public void updateItemStatus(int itemId, int statusId){
 		Item item = getItemById(itemId);
 		item.setStatus(statusId);
 		simpleUpdateItem(item);
 	}
 	
 	public void deleteItem(int itemId){
 		updateItemStatus(itemId, ProductStatus.Deleted);
 	}
 	
 	public ItemMapper getItemMapper() {
 		return itemMapper;
 	}
 
 	public void setItemMapper(ItemMapper itemMapper) {
 		this.itemMapper = itemMapper;
 	}
 
 }
