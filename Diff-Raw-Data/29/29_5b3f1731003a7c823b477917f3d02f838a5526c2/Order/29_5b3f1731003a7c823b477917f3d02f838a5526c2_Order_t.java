 package com.omartech.tdg.model;
 
 
 import java.util.Date;
 import java.util.List;
 
 public class Order {
 
 	private int id;
 	
 	//order details
 	private List<OrderItem> orderItems;
 	private float orderPrice; //纯货物价格: 参与了平台的返利价格，不给商家看
 	private float transferPrice; //运费
 	private float discountFee;
 	private float price; //总价 = 货物价格 + 运费 - 折扣费
	private float priceRMB;
 	private float orderPriceRMB;//纯货物价格: 参与了平台的返利价格，不给商家看
 	private float transferPriceRMB;
 	private float discountFeeRMB;
 	
 	private float originPrice;//纯货物价格，给商家看
 	private float originPriceRMB;//纯货物价格，给商家看
 	
 	private float originTotal;//纯货物价格+运费，给商家看
 	private float originTotalRMB;//纯货物价格+运费,给商家看
 
 	
 	private int coinage;
 	private Date createAt;
 	//for seller
 	private Date sendAt;//发货日期
 	private String carrier;
 	private String trackingWeb;
 	private String trackingId;
 	private int cancelReason;//商家取消订单的原因
 	private String cancelComment;//商家取消订单的备注
 	private Date sendLogAt;//填写发货信息的日期（奇葩的想法)
 	
 	//for customer
 	private String name;
 	private String address;
 	private String city;
 	private String country;
 	private String postCode;
 
 	//for admin
 	private int customerId;
 	private int sellerId;
 	private String sellerName;
 	
 	//for order management
 	private int parentId;//default == null
 	private int hasChildren;//default = 0
 	private int orderStatus; //{OrderStatus}
 	
 	private int returnFlag;//用于标志退货剩余日期
 	private Date returnDate;
 	
 	public List<OrderItem> getOrderItems() {
 		return orderItems;
 	}
 	public void setOrderItems(List<OrderItem> orderItems) {
 		this.orderItems = orderItems;
 	}
 	public float getOrderPrice() {
 		return orderPrice;
 	}
 	public void setOrderPrice(float orderPrice) {
 		this.orderPrice = orderPrice;
 	}
 	public float getTransferPrice() {
 		return transferPrice;
 	}
 	public void setTransferPrice(float transferPrice) {
 		this.transferPrice = transferPrice;
 	}
 	public float getPrice() {
 		return price;
 	}
 	public void setPrice(float price) {
 		this.price = price;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	public String getCity() {
 		return city;
 	}
 	public void setCity(String city) {
 		this.city = city;
 	}
 	public String getCountry() {
 		return country;
 	}
 	public void setCountry(String country) {
 		this.country = country;
 	}
 	public String getPostCode() {
 		return postCode;
 	}
 	public void setPostCode(String postCode) {
 		this.postCode = postCode;
 	}
 	public int getCustomerId() {
 		return customerId;
 	}
 	public void setCustomerId(int customerId) {
 		this.customerId = customerId;
 	}
 	public int getSellerId() {
 		return sellerId;
 	}
 	public void setSellerId(int sellerId) {
 		this.sellerId = sellerId;
 	}
 	public int getOrderStatus() {
 		return orderStatus;
 	}
 	public void setOrderStatus(int orderStatus) {
 		this.orderStatus = orderStatus;
 	}
 	public Date getCreateAt() {
 		return createAt;
 	}
 	public void setCreateAt(Date createAt) {
 		this.createAt = createAt;
 	}
 	public String getSellerName() {
 		return sellerName;
 	}
 	public void setSellerName(String sellerName) {
 		this.sellerName = sellerName;
 	}
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public int getParentId() {
 		return parentId;
 	}
 	public void setParentId(int parentId) {
 		this.parentId = parentId;
 	}
 	public int getHasChildren() {
 		return hasChildren;
 	}
 	public void setHasChildren(int hasChildren) {
 		this.hasChildren = hasChildren;
 	}
 	public Date getSendAt() {
 		return sendAt;
 	}
 	public void setSendAt(Date sendAt) {
 		this.sendAt = sendAt;
 	}
 	public String getCarrier() {
 		return carrier;
 	}
 	public void setCarrier(String carrier) {
 		this.carrier = carrier;
 	}
 	public String getTrackingWeb() {
 		return trackingWeb;
 	}
 	public void setTrackingWeb(String trackingWeb) {
 		this.trackingWeb = trackingWeb;
 	}
 	public String getTrackingId() {
 		return trackingId;
 	}
 	public void setTrackingId(String trackingId) {
 		this.trackingId = trackingId;
 	}
 	public float getOrderPriceRMB() {
 		return orderPriceRMB;
 	}
 	public void setOrderPriceRMB(float orderPriceRMB) {
 		this.orderPriceRMB = orderPriceRMB;
 	}
 	public float getTransferPriceRMB() {
 		return transferPriceRMB;
 	}
 	public void setTransferPriceRMB(float transferPriceRMB) {
 		this.transferPriceRMB = transferPriceRMB;
 	}
 	public float getPriceRMB() {
 		return priceRMB;
 	}
 	public void setPriceRMB(float priceRMB) {
 		this.priceRMB = priceRMB;
 	}
 	public int getCoinage() {
 		return coinage;
 	}
 	public void setCoinage(int coinage) {
 		this.coinage = coinage;
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
 	public float getOriginPrice() {
 		return originPrice;
 	}
 	public void setOriginPrice(float originPrice) {
 		this.originPrice = originPrice;
 	}
 	public float getOriginPriceRMB() {
 		return originPriceRMB;
 	}
 	public void setOriginPriceRMB(float originPriceRMB) {
 		this.originPriceRMB = originPriceRMB;
 	}
 	public float getOriginTotal() {
 		return originTotal;
 	}
 	public void setOriginTotal(float originTotal) {
 		this.originTotal = originTotal;
 	}
 	public float getOriginTotalRMB() {
 		return originTotalRMB;
 	}
 	public void setOriginTotalRMB(float originTotalRMB) {
 		this.originTotalRMB = originTotalRMB;
 	}
 	public String getCancelComment() {
 		return cancelComment;
 	}
 	public void setCancelComment(String cancelComment) {
 		this.cancelComment = cancelComment;
 	}
 	public int getCancelReason() {
 		return cancelReason;
 	}
 	public void setCancelReason(int cancelReason) {
 		this.cancelReason = cancelReason;
 	}
 	public Date getSendLogAt() {
 		return sendLogAt;
 	}
 	public void setSendLogAt(Date sendLogAt) {
 		this.sendLogAt = sendLogAt;
 	}
 	public int getReturnFlag() {
 		return returnFlag;
 	}
 	public void setReturnFlag(int returnFlag) {
 		this.returnFlag = returnFlag;
 	}
 	public Date getReturnDate() {
 		return returnDate;
 	}
 	public void setReturnDate(Date returnDate) {
 		this.returnDate = returnDate;
 	}
 	
 }
