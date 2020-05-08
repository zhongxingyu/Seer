 package me.deal.shared;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /*
  * The deal object which contains information about deals from Yipit.
  * See http://yipit.com/about/api/documentation/#deals for reference.
  */
 
 /*
  *  IMPORTANT NOTE: we may or may not use this object.  If we decide it's better
  *  for the client to parse the Yipit return values, then there is no need to use
  *  this class; for now, to make it easy on the client, we'll use it.
  */
 
 @SuppressWarnings("serial")
 public class Deal implements Serializable {
 	
 	private Integer yipitDealID;
 	private String addDate; // The date the deal was created
 	private String endDate; // The date when the deal offer ends
 	private Double discountPercentage; // Number between 0 and 100
 	private Double price; // How much the deal costs
 	private Double value; // How much the deal retails for
 	private String title; // The title of the deal
 	private String subtitle; // Yipit's assigned title
 	private String yipitWebUrl; // The web url of the deal
 	private String yipitMobileUrl; // The mobile url of the deal
 	private String bigImageUrl; // The url of the deal's big image
 	private String smallImageUrl; // The url of the deal's small image
 	private Location businessAddress; // The address of the business
 	private String businessPhoneNumber; // The phone number of the businesses (containing no '-' or '(', ')')
 	private Boolean paid; // Is this deal part of the Yipit affiliates program?
 	private ArrayList<Category> tags; // The categories that this deal falls under
 	private BusinessInfo dealBusinessInfo;
 	
 	public Deal() {
 	}
 	
 	public Deal(
 		final Integer yipitDealID,
 		final String addDate,
 		final String endDate,
 		final Double discountPercentage,
 		final Double price,
 		final Double value,
 		final String title,
 		final String subtitle,
 		final String yipitWebUrl,
 		final String yipitMobileUrl,
 		final String bigImageUrl,
 		final String smallImageUrl,
 		final Location businessAddress,
 		final String businessPhoneNumber,
 		final Boolean paid,
 		final ArrayList<Category> tags,
 		final BusinessInfo dealBusinessInfo) {
 			this.yipitDealID = yipitDealID;
 			this.addDate = addDate;
 			this.endDate = endDate;
 			this.discountPercentage = discountPercentage;
 			this.price = price;
 			this.value = value;
 			this.title = title;
 			this.subtitle = subtitle;
 			this.yipitWebUrl = yipitWebUrl;
 			this.yipitMobileUrl = yipitMobileUrl;
 			this.bigImageUrl = bigImageUrl;
 			this.smallImageUrl = smallImageUrl;
 			this.businessAddress = businessAddress;
 			this.businessPhoneNumber = businessPhoneNumber;
 			this.paid = paid;
 			this.tags = tags;
 			this.dealBusinessInfo= dealBusinessInfo;
 	}
 	
 	public Integer getYipitDealID() {
 		return this.yipitDealID;
 	}
 	
 	public void setYipitDealID(final Integer dealID) {
 		yipitDealID = dealID;
 	}
 	
 	public String getAddDate() {
 		return this.addDate;
 	}
 	
 	public void setAddDate(final String addDate) {
 		this.addDate = addDate;
 	}
 	
 	public String getEndDate() {
 		return this.endDate;
 	}
 	
 	public void setEndDate(final String endDate) {
 		this.endDate = endDate;
 	}
 	public Double getDiscountPercentage() {
 		return this.discountPercentage;
 	}
 	
 	public void setDiscountPercentage(final Double discountPercentage) {
 		this.discountPercentage = discountPercentage;
 	}
 	
 	public Double getPrice() {
 		return this.price;
 	}
 	
 	public void setPrice(final Double price) {
 		this.price = price;
 	}
 	
 	public Double getValue() {
 		return this.value;
 	}
 	
 	public void setValue(final Double value) {
 		this.value = value;
 	}
 	
 	public String getTitle() {
 		return this.title;
 	}
 	
 	public void setTitle(final String title) {
 		this.title = title;
 	}
 	
 	public String getSubtitle() {
 		return this.subtitle;
 	}
 	
 	public void setSubtitle(final String subtitle) {
 		this.subtitle = subtitle;
 	}
 	
 	public String getYipitWebUrl() {
 		return this.yipitWebUrl;
 	}
 	
 	public void setYipitWebUrl(final String yipitWebUrl) {
 		this.yipitWebUrl = yipitWebUrl;
 	}
 	
 	public String getYipitMobileUrl() {
 		return this.yipitMobileUrl;
 	}
 	
 	public void setYipitMobileUrl(final String yipitMobileUrl) {
 		this.yipitMobileUrl = yipitMobileUrl;
 	}
 	
 	public String getBigImageUrl() {
 		return this.bigImageUrl;
 	}
 	
 	public void setBigImageUrl(final String bigImageUrl) {
 		this.bigImageUrl = bigImageUrl;
 	}
 	
 	public String getSmallImageUrl() {
 		return this.smallImageUrl;
 	}
 	
 	public void setSmallImageUrl(final String smallImageUrl) {
 		this.smallImageUrl = smallImageUrl;
 	}
 	
 	public Location getBusinessAddress() {
 		return this.businessAddress;
 	}
 	
 	public void setBusinessAddress(final Location businessAddress) {
 		this.businessAddress = businessAddress;
 	}
 	
 	public String getBusinessPhoneNumber() {
 		return this.businessPhoneNumber;
 	}
 	
 	public void setBusinessPhoneNumber(final String businessPhoneNumber) {
 		this.businessPhoneNumber = businessPhoneNumber;
 	}
 
 	public Boolean getPaid() {
 		return this.paid;
 	}
 	
 	public void setPaid(final Boolean paid) {
 		this.paid = paid;
 	}
 	
 	public ArrayList<Category> getTags() {
 		return this.tags;
 	}
 	
 	public void addTag(final Category tag) {
 		if(!tags.contains(tag))
 			tags.add(tag);
 	}
 
 	public void setDealBusinessInfo(BusinessInfo dealBusinessInfo) {
 		this.dealBusinessInfo = dealBusinessInfo;
 	}
 
 	public BusinessInfo getDealBusinessInfo() {
 		return dealBusinessInfo;
 	}
 }
