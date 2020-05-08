 package com.omartech.tdg.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.omartech.tdg.mapper.CartMapper;
 import com.omartech.tdg.mapper.CountryMapper;
 import com.omartech.tdg.model.Cart;
 import com.omartech.tdg.model.Coinage;
 import com.omartech.tdg.model.Country;
 import com.omartech.tdg.model.Item;
 import com.omartech.tdg.model.OrderItem;
 import com.omartech.tdg.model.ProductLine;
 @Service
 public class CartService {
 
 	@Autowired
 	private ItemService itemService;
 	@Autowired
 	private CountryMapper countryMapper;
 	@Autowired
 	private ProductLineService productLineService;
 	
 	@Autowired
 	private CartMapper cartMapper;
 	
 	public void insert(Cart cart){
 		cartMapper.insert(cart);
 	}
 	public void deleteByCustomerIdAndItemId(int customerId, int itemId){
 		cartMapper.deleteByCustomerIdAndItemId(customerId, itemId);
 	}
 	public void deleteByCustomerId(int customerId){
 		cartMapper.deleteByCustomerId(customerId);
 	}
 	
 	public void deleteById(int id){
 		cartMapper.deleteById(id);
 	}
 	public void updateNumberByCustomerIdAndItemId(int customerId,int itemId, int number){
 		cartMapper.updateNumberByCustomerIdAndItemId(customerId, itemId, number);
 	}
 	
 	public List<Cart> getCartsByCustomerId(int customerId){
 		return cartMapper.getCartsByCustomerId(customerId);
 	}
 	
 	
 	public OrderItem createOrderItemFromItem(Item item, int count){
 		
 		OrderItem orderItem = new OrderItem(item);
 		orderItem.setNum(count);
 		
 		float price = itemService.getPriceByItemId(item.getId(), count);
 		orderItem.setPrice(price);
 		
 		float priceRMB = Coinage.compute(orderItem.getCoinage(), price);
 		orderItem.setPriceRMB(priceRMB);
 		
 		float ifee = item.getInternationalShippingFee();
 		float ifeeRMB = Coinage.compute(orderItem.getCoinage(), ifee);
 		orderItem.setIfeeRMB(ifeeRMB);
		orderItem.setInternationalShippingFee(ifee);
 		
 		int countryCode = item.getCountryCode();
 		Country country = countryMapper.getCountryById(countryCode);
 		String countryName = country.getNameInChinese();
 		orderItem.setShippingCountry(countryName);
 		
 		int lineId = item.getProductLineId();
 		ProductLine productLine = productLineService.getProductLineById(lineId);
 		float discount = productLine.getDiscount();
 		orderItem.setDiscount(discount);
 		
 		float discountFee = price * discount;
 		float discountFeeRMB = priceRMB * discount;
 		orderItem.setDiscountFee(discountFee);
 		orderItem.setDiscountFeeRMB(discountFeeRMB);
 		
 		float finalPrice = price - discountFee;
 		float finalPriceRMB = priceRMB - discountFeeRMB;
 		orderItem.setFinalPrice(finalPriceRMB);
 		orderItem.setFinalPriceRMB(finalPriceRMB);
 		
 		float sumPrice = finalPrice * count;
 		float sumPriceRMB = finalPriceRMB * count;
 		orderItem.setSumPrice(sumPrice);
 		orderItem.setSumPriceRMB(sumPriceRMB);
 		
 		
 		return orderItem;
 	}
 
 	
 	public ItemService getItemService() {
 		return itemService;
 	}
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 	public CountryMapper getCountryMapper() {
 		return countryMapper;
 	}
 	public void setCountryMapper(CountryMapper countryMapper) {
 		this.countryMapper = countryMapper;
 	}
 	public ProductLineService getProductLineService() {
 		return productLineService;
 	}
 	public void setProductLineService(ProductLineService productLineService) {
 		this.productLineService = productLineService;
 	}
 	
 	
 }
