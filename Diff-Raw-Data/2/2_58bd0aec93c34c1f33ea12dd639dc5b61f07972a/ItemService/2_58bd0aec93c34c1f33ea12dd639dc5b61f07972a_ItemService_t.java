 package com.omartech.tdg.service;
 
 import java.util.Calendar;
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
 import com.omartech.tdg.model.Seller;
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
 			updateItem(item);
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
 		int available = item.getAvailableQuantity();
 		int avail = available - count;
 		if(avail >= 0){
 			updateStock(itemId, avail);
 			if(avail <= SystemDefaultSettings.SystemSafeStock){
 				int productId = item.getProductId();
 				productService.updateProductSellable(productId, ProductStatus.Unsellable);
 				int sellerId = item.getSellerId();
 				emailService.sendEmailWhenNearlyOutofStock(sellerId, productId);
 			}
 		}else{
 			throw new OutOfStockException();
 		}
 		int safeStock = item.getSafeStock();
 		int sellerId = item.getSellerId();
 		if(available <= safeStock){
 			item.setActive(0);
 			Seller  seller = sellerAuthService.getSellerById(sellerId);
 			int productId = item.getProductId();
 			emailService.sendEmailWhenProductWillSoldOut(productId, seller);
 		}
 	}
 	/**
 	 * 更新库存
 	 * @param itemId
 	 * @param availablestock
 	 */
 	public void updateStock(int itemId, int availablestock){
 		itemMapper.updateStock(itemId, availablestock);
 	}
 	
 	@Transactional
 	public void insertItem(Item item) {
 		if(item.getAvailableQuantity() < item.getSafeStock()){
 			item.setActive(0);
 		}else{
 			item.setActive(1);
 		}
 		item.setStatus(ProductStatus.OK);
 		int sellerId = item.getSellerId();
 		ShopSetting setting = shopSettingMapper.getShopSettingBySellerId(sellerId);
 		int countryCode = setting.getShippingCountry();
 		item.setCountryCode(countryCode);
 		
 		itemMapper.insertItem(item);
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
 //		Date now = new Date(System.currentTimeMillis());
 //		Date 
 		Calendar today = Calendar.getInstance();
 		today.set(Calendar.HOUR_OF_DAY, 0);
 		today.set(Calendar.MINUTE, 0);
 		today.set(Calendar.MILLISECOND, 0);
 		today.set(Calendar.SECOND, 0);
 		Date now = today.getTime();
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
 	public List<Item> getItemsByProductIdAndStatus(int productId, int statusId){
 		List<Item> items = itemMapper.getItemsByProductIdAndStatus(productId, statusId);
 		return items;
 	}
 
 	public void updateItem(Item item){
 		itemMapper.updateItem(item);
 	}
 	
 	public void updateItemStatus(int itemId, int statusId){
 		Item item = getItemById(itemId);
 		item.setStatus(statusId);
 		updateItem(item);
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
