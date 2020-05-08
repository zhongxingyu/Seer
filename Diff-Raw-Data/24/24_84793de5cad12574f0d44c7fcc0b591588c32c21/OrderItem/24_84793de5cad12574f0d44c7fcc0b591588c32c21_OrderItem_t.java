 package com.omartech.tdg.model;
 
 
 public class OrderItem {
 	
 	private int id;
 	private int itemId;
 	private String sku;
 	private int productId;
 	private String name;//货物名
 	private String nameInChinese;//中文
 	private String desc;//用于描述子产品
 	private String featureJson;
 	private int coinage;
 	private int sellerId;
 	private int num;
 	private float discount;//返利比率
 	private float price;//原价
 	private float priceRMB;//原价
	private float internationalShippingFee;//国际运输费--单项，没有乘num
	private float ifeeRMB;//运费的rmb --单项，没有乘num
 	private float discountFee;//实际返利金额
 	private float discountFeeRMB;//实际返利金额
 	private float finalPrice;//物品成交价
 	private float finalPriceRMB;//物品成交价
 	private float sumPrice; //当前项价格 = 成交价 * num
 	private float sumPriceRMB; //当前人民币价格 = 成交价 * num
 	private int orderId;
 	private int availableQuantity;
 	private int internationalShippingService;// 0:no, 1:yes
 
 	private float internationalPromiseDays;//承诺日期
 	private int countryCode;
 	private String shippingCountry;
 	private int productLineId;//大类别
 	private int categoryId;//小类别
 	
 	public OrderItem(){
 		
 	}
 	
 	public OrderItem(Item item){
 		this.itemId = item.getId();
 		this.sku = item.getSku();
 		this.productId = item.getProductId();
 		this.name = item.getName();
 		this.nameInChinese = item.getNameInChinese();
 		this.desc = item.getFeatureJson();
 		String json = item.getFeatureJson();
 		if(json !=null ){
 			json = json.replaceAll("color", "颜色").replaceAll("size", "尺寸");
 		}
 		this.featureJson = json;
 		this.coinage = item.getCoinage();
 		this.sellerId = item.getSellerId();
 		this.setInternationalShippingService(item.getInternationalShippingService());
 		if(item.getInternationalShippingService() == 1){
 			this.setInternationalPromiseDays(item.getInternationalPromiseDays());
 			this.setInternationalShippingFee(item.getInternationalShippingFee());
 			this.ifeeRMB = Coinage.compute(coinage, item.getInternationalShippingFee());
 		}
 		this.countryCode = item.getCountryCode();
 		this.availableQuantity = item.getAvailableQuantity();
 		this.categoryId = item.getCategoryId();
 		this.productLineId = item.getProductLineId();
 	}
 	
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public int getItemId() {
 		return itemId;
 	}
 	public void setItemId(int itemId) {
 		this.itemId = itemId;
 	}
 	public String getSku() {
 		return sku;
 	}
 
 	public void setSku(String sku) {
 		this.sku = sku;
 	}
 
 	public int getProductId() {
 		return productId;
 	}
 	public void setProductId(int productId) {
 		this.productId = productId;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public int getCoinage() {
 		return coinage;
 	}
 	public void setCoinage(int coinage) {
 		this.coinage = coinage;
 	}
 	public int getSellerId() {
 		return sellerId;
 	}
 	public void setSellerId(int sellerId) {
 		this.sellerId = sellerId;
 	}
 	public int getNum() {
 		return num;
 	}
 	public void setNum(int num) {
 		this.num = num;
 	}
 	public float getPrice() {
 		return price;
 	}
 	public void setPrice(float price) {
 		this.price = price;
 	}
 	public int getOrderId() {
 		return orderId;
 	}
 	public void setOrderId(int orderId) {
 		this.orderId = orderId;
 	}
 	public String getDesc() {
 		return desc;
 	}
 	public void setDesc(String desc) {
 		this.desc = desc;
 	}
 
 	public String getNameInChinese() {
 		return nameInChinese;
 	}
 
 	public void setNameInChinese(String nameInChinese) {
 		this.nameInChinese = nameInChinese;
 	}
 
 
 	public int getInternationalShippingService() {
 		return internationalShippingService;
 	}
 
 	public void setInternationalShippingService(int internationalShippingService) {
 		this.internationalShippingService = internationalShippingService;
 	}
 
 	public float getInternationalShippingFee() {
 		return internationalShippingFee;
 	}
 
 	public void setInternationalShippingFee(float internationalShippingFee) {
 		this.internationalShippingFee = internationalShippingFee;
 	}
 
 	public float getInternationalPromiseDays() {
 		return internationalPromiseDays;
 	}
 
 	public void setInternationalPromiseDays(float internationalPromiseDays) {
 		this.internationalPromiseDays = internationalPromiseDays;
 	}
 
 	public int getCountryCode() {
 		return countryCode;
 	}
 
 	public void setCountryCode(int countryCode) {
 		this.countryCode = countryCode;
 	}
 
 	public float getPriceRMB() {
 		return priceRMB;
 	}
 
 	public void setPriceRMB(float priceRMB) {
 		this.priceRMB = priceRMB;
 	}
 
 	public float getIfeeRMB() {
 		return ifeeRMB;
 	}
 
 	public void setIfeeRMB(float ifeeRMB) {
 		this.ifeeRMB = ifeeRMB;
 	}
 
 	public String getShippingCountry() {
 		return shippingCountry;
 	}
 
 	public void setShippingCountry(String shippingCountry) {
 		this.shippingCountry = shippingCountry;
 	}
 
 	public String getFeatureJson() {
 		return featureJson;
 	}
 
 	public void setFeatureJson(String featureJson) {
 		this.featureJson = featureJson;
 	}
 
 	public int getAvailableQuantity() {
 		return availableQuantity;
 	}
 
 	public void setAvailableQuantity(int availableQuantity) {
 		this.availableQuantity = availableQuantity;
 	}
 
 	public float getDiscount() {
 		return discount;
 	}
 
 	public void setDiscount(float discount) {
 		this.discount = discount;
 	}
 
 	public float getDiscountFee() {
 		return discountFee;
 	}
 
 	public void setDiscountFee(float discountFee) {
 		this.discountFee = discountFee;
 	}
 
 	public float getDiscountFeeRMB() {
 		return discountFeeRMB;
 	}
 
 	public void setDiscountFeeRMB(float discountFeeRMB) {
 		this.discountFeeRMB = discountFeeRMB;
 	}
 
 	public float getFinalPrice() {
 		return finalPrice;
 	}
 
 	public void setFinalPrice(float finalPrice) {
 		this.finalPrice = finalPrice;
 	}
 
 	public float getFinalPriceRMB() {
 		return finalPriceRMB;
 	}
 
 	public void setFinalPriceRMB(float finalPriceRMB) {
 		this.finalPriceRMB = finalPriceRMB;
 	}
 
 	public float getSumPrice() {
 		return sumPrice;
 	}
 
 	public void setSumPrice(float sumPrice) {
 		this.sumPrice = sumPrice;
 	}
 
 	public float getSumPriceRMB() {
 		return sumPriceRMB;
 	}
 
 	public void setSumPriceRMB(float sumPriceRMB) {
 		this.sumPriceRMB = sumPriceRMB;
 	}
 
 	public int getProductLineId() {
 		return productLineId;
 	}
 
 	public void setProductLineId(int productLineId) {
 		this.productLineId = productLineId;
 	}
 
 	public int getCategoryId() {
 		return categoryId;
 	}
 
 	public void setCategoryId(int categoryId) {
 		this.categoryId = categoryId;
 	}
 	
 }
