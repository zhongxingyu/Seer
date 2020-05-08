 package com.omartech.tdg.model;
 
import java.sql.Date;
 import java.util.List;
 import java.util.Map;
 
 public class Product {
 
 	/**
 	 * basic
 	 */
 	private long id;
 	private String name;
 	private String nameInChinese;
 	private String description;
 	private String descriptionInChinese;
 	private int isChinese;//0:no,1:yes
 	private String basicParams;
 	private Map<String,String> basicParamsMap;
 	private Map<String,String> basicParamsMapInEnglish;
 	private int hasChildren;//0:no, 1: yes
 	
 	/**
 	 * price
 	 */
 	private float retailPrice;//零售价
 	private float promotionPrice;//推广价
 	private Date promotionTime;//推广时间
 	private float wholePrice;//批发价
 	private int minimumQuantity;//最低批发数量
 	private int maximumAcceptQuantity;//最大单笔数量
 	private int coinage;//货币
 	
 	/**
 	 * store
 	 */
 	private int availableQuantity;//现库存
 	private int safeStock;//安全库存
 	private int active;//0:false;1:true
 	
 	private float netWeight;//净重
 	private float grossWeight;//毛重
 	private String sizeWithPackage;//包装大小
 	
 	/**
 	 * pictures
 	 */
 	private String mainImage;//主图
 	private List<String> otherImages;//附图-用于逻辑
 	private String subImages;//附图-用于存储
 	
 	/**
 	 * forigin keys
 	 */
 	private int brandId;//brand
 	private int sellerId;//seller
 	private int productTypeId;//产品类型id
 	private int categoryId;//大类别
 	
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getNameInChinese() {
 		return nameInChinese;
 	}
 	public void setNameInChinese(String nameInChinese) {
 		this.nameInChinese = nameInChinese;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getDescriptionInChinese() {
 		return descriptionInChinese;
 	}
 	public void setDescriptionInChinese(String descriptionInChinese) {
 		this.descriptionInChinese = descriptionInChinese;
 	}
 	public int getIsChinese() {
 		return isChinese;
 	}
 	public void setIsChinese(int isChinese) {
 		this.isChinese = isChinese;
 	}
 	public float getRetailPrice() {
 		return retailPrice;
 	}
 	public void setRetailPrice(float retailPrice) {
 		this.retailPrice = retailPrice;
 	}
 	public float getPromotionPrice() {
 		return promotionPrice;
 	}
 	public void setPromotionPrice(float promotionPrice) {
 		this.promotionPrice = promotionPrice;
 	}
 	public Date getPromotionTime() {
 		return promotionTime;
 	}
 	public void setPromotionTime(Date promotionTime) {
 		this.promotionTime = promotionTime;
 	}
 	public float getWholePrice() {
 		return wholePrice;
 	}
 	public void setWholePrice(float wholePrice) {
 		this.wholePrice = wholePrice;
 	}
 	public int getMinimumQuantity() {
 		return minimumQuantity;
 	}
 	public void setMinimumQuantity(int minimumQuantity) {
 		this.minimumQuantity = minimumQuantity;
 	}
 	public int getMaximumAcceptQuantity() {
 		return maximumAcceptQuantity;
 	}
 	public void setMaximumAcceptQuantity(int maximumAcceptQuantity) {
 		this.maximumAcceptQuantity = maximumAcceptQuantity;
 	}
 	public int getAvailableQuantity() {
 		return availableQuantity;
 	}
 	public void setAvailableQuantity(int availableQuantity) {
 		this.availableQuantity = availableQuantity;
 	}
 	public int getSafeStock() {
 		return safeStock;
 	}
 	public void setSafeStock(int safeStock) {
 		this.safeStock = safeStock;
 	}
 	public int getActive() {
 		return active;
 	}
 	public void setActive(int active) {
 		this.active = active;
 	}
 	public String getSubImages() {
 		return subImages;
 	}
 	public void setSubImages(String subImages) {
 		this.subImages = subImages;
 	}
 	public float getNetWeight() {
 		return netWeight;
 	}
 	public void setNetWeight(float netWeight) {
 		this.netWeight = netWeight;
 	}
 	public float getGrossWeight() {
 		return grossWeight;
 	}
 	public void setGrossWeight(float grossWeight) {
 		this.grossWeight = grossWeight;
 	}
 	public String getSizeWithPackage() {
 		return sizeWithPackage;
 	}
 	public void setSizeWithPackage(String sizeWithPackage) {
 		this.sizeWithPackage = sizeWithPackage;
 	}
 	public String getMainImage() {
 		return mainImage;
 	}
 	public void setMainImage(String mainImage) {
 		this.mainImage = mainImage;
 	}
 	public List<String> getOtherImages() {
 		return otherImages;
 	}
 	public void setOtherImages(List<String> otherImages) {
 		this.otherImages = otherImages;
 	}
 	public int getBrandId() {
 		return brandId;
 	}
 	public void setBrandId(int brandId) {
 		this.brandId = brandId;
 	}
 	public int getSellerId() {
 		return sellerId;
 	}
 	public void setSellerId(int sellerId) {
 		this.sellerId = sellerId;
 	}
 	public int getProductTypeId() {
 		return productTypeId;
 	}
 	public void setProductTypeId(int productTypeId) {
 		this.productTypeId = productTypeId;
 	}
 	public int getCoinage() {
 		return coinage;
 	}
 	public void setCoinage(int coinage) {
 		this.coinage = coinage;
 	}
 	public int getCategoryId() {
 		return categoryId;
 	}
 	public void setCategoryId(int categoryId) {
 		this.categoryId = categoryId;
 	}
 	public String getBasicParams() {
 		return basicParams;
 	}
 	public void setBasicParams(String basicParams) {
 		this.basicParams = basicParams;
 	}
 	public Map<String, String> getBasicParamsMap() {
 		return basicParamsMap;
 	}
 	public void setBasicParamsMap(Map<String, String> basicParamsMap) {
 		this.basicParamsMap = basicParamsMap;
 	}
 	public Map<String, String> getBasicParamsMapInEnglish() {
 		return basicParamsMapInEnglish;
 	}
 	public void setBasicParamsMapInEnglish(
 			Map<String, String> basicParamsMapInEnglish) {
 		this.basicParamsMapInEnglish = basicParamsMapInEnglish;
 	}
 	public int getHasChildren() {
 		return hasChildren;
 	}
 	public void setHasChildren(int hasChildren) {
 		this.hasChildren = hasChildren;
 	}
 }
