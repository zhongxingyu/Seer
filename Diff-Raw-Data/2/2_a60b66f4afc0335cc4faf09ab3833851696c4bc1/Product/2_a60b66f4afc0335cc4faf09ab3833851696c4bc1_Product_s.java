 package com.cqlybest.common.bean;
 
 import java.util.Date;
 import java.util.Set;
 
 public class Product {
 
   private Integer id;
   private String name;// 产品名称
   private Integer days;// 行程天数
   private char daysUnit;// 行程天数单位 ：d/天，m/月，y/年
   private String description; // 产品介绍
   private Integer price; // 价格
   private String priceDescription; // 费用说明
   private Date effectiveDate; // 产品 生效日期
   private Date expiryDate; // 产品 失效日期
   private Date departureDate; // 准确的出发日期
   private String tripCharacteristic;// 行程特色
   private String serviceStandard;// 服务标准
   private String friendlyReminder;// 友情提示
   private String recommendedItem;// 推荐项目
   private boolean published; // 是否发布
   private boolean popular;// 是否热门
   private boolean recommend;// 是否推荐
   private boolean specialOffer;// 是否特价
   private Date lastUpdate;// 最后更新时间
 
   private Set<Integer> recommendedMonths;// 推荐月份
   private Set<Integer> crowds;// 适合人群
   private Set<Destination> destinations;// 目的地
   private Set<DictTraffic> traffics;// 交通方式
   private Set<DictProductType> types;// 产品类型
   private Set<DictProductGrade> grades;// 产品等级
   private Set<Keyword> keywords;// 关键词
   private Set<DepartureCity> departureCities;// 出发城市
   private Set<String> posters;// 海报图片
   private Set<String> photos;// 相册图片
 
   public Integer getId() {
     return id;
   }
 
   public void setId(Integer id) {
     this.id = id;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public Integer getDays() {
     return days;
   }
 
   public void setDays(Integer days) {
     this.days = days;
   }
 
   public char getDaysUnit() {
     return daysUnit;
   }
 
   public void setDaysUnit(char daysUnit) {
     this.daysUnit = daysUnit;
   }
 
   public String getDescription() {
     return description;
   }
 
   public void setDescription(String description) {
     this.description = description;
   }
 
   public Integer getPrice() {
     return price;
   }
 
   public void setPrice(Integer price) {
     this.price = price;
   }
 
   public String getPriceDescription() {
     return priceDescription;
   }
 
   public void setPriceDescription(String priceDescription) {
     this.priceDescription = priceDescription;
   }
 
   public Date getEffectiveDate() {
     return effectiveDate;
   }
 
   public void setEffectiveDate(Date effectiveDate) {
     this.effectiveDate = effectiveDate;
   }
 
   public Date getExpiryDate() {
     return expiryDate;
   }
 
   public void setExpiryDate(Date expiryDate) {
     this.expiryDate = expiryDate;
   }
 
   public Date getDepartureDate() {
     return departureDate;
   }
 
   public void setDepartureDate(Date departureDate) {
     this.departureDate = departureDate;
   }
 
   public String getTripCharacteristic() {
     return tripCharacteristic;
   }
 
   public void setTripCharacteristic(String tripCharacteristic) {
     this.tripCharacteristic = tripCharacteristic;
   }
 
   public String getServiceStandard() {
     return serviceStandard;
   }
 
   public void setServiceStandard(String serviceStandard) {
     this.serviceStandard = serviceStandard;
   }
 
   public String getFriendlyReminder() {
     return friendlyReminder;
   }
 
   public void setFriendlyReminder(String friendlyReminder) {
     this.friendlyReminder = friendlyReminder;
   }
 
   public String getRecommendedItem() {
     return recommendedItem;
   }
 
   public void setRecommendedItem(String recommendedItem) {
     this.recommendedItem = recommendedItem;
   }
 
   public boolean isPublished() {
     return published;
   }
 
   public void setPublished(boolean published) {
     this.published = published;
   }
 
   public boolean isPopular() {
     return popular;
   }
 
   public void setPopular(boolean popular) {
     this.popular = popular;
   }
 
   public boolean isRecommend() {
     return recommend;
   }
 
   public void setRecommend(boolean recommend) {
     this.recommend = recommend;
   }
 
   public boolean isSpecialOffer() {
     return specialOffer;
   }
 
  public void setSpecialffer(boolean specialOffer) {
     this.specialOffer = specialOffer;
   }
 
   public Date getLastUpdate() {
     return lastUpdate;
   }
 
   public void setLastUpdate(Date lastUpdate) {
     this.lastUpdate = lastUpdate;
   }
 
   public Set<Integer> getRecommendedMonths() {
     return recommendedMonths;
   }
 
   public void setRecommendedMonths(Set<Integer> recommendedMonths) {
     this.recommendedMonths = recommendedMonths;
   }
 
   public Set<Integer> getCrowds() {
     return crowds;
   }
 
   public void setCrowds(Set<Integer> crowds) {
     this.crowds = crowds;
   }
 
   public Set<Destination> getDestinations() {
     return destinations;
   }
 
   public void setDestinations(Set<Destination> destinations) {
     this.destinations = destinations;
   }
 
   public Set<DictTraffic> getTraffics() {
     return traffics;
   }
 
   public void setTraffics(Set<DictTraffic> traffics) {
     this.traffics = traffics;
   }
 
   public Set<DictProductType> getTypes() {
     return types;
   }
 
   public void setTypes(Set<DictProductType> types) {
     this.types = types;
   }
 
   public Set<DictProductGrade> getGrades() {
     return grades;
   }
 
   public void setGrades(Set<DictProductGrade> grades) {
     this.grades = grades;
   }
 
   public Set<Keyword> getKeywords() {
     return keywords;
   }
 
   public void setKeywords(Set<Keyword> keywords) {
     this.keywords = keywords;
   }
 
   public Set<DepartureCity> getDepartureCities() {
     return departureCities;
   }
 
   public void setDepartureCities(Set<DepartureCity> departureCities) {
     this.departureCities = departureCities;
   }
 
   public Set<String> getPosters() {
     return posters;
   }
 
   public void setPosters(Set<String> posters) {
     this.posters = posters;
   }
 
   public Set<String> getPhotos() {
     return photos;
   }
 
   public void setPhotos(Set<String> photos) {
     this.photos = photos;
   }
 
 }
