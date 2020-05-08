 /**
  * @author Shoucong Chen
  * Feb 17, 2013
  */
 
 package edu.cmu.ebiz.task8.bean;
 
 public class SearchPlaceDetailBean implements Comparable<SearchPlaceDetailBean> {
 	private String name;
 	private String type;
 	private String phone;
 	private String address;
 	private double latitude;
 	private double longitude;
 	private double rating;
 	private String url;
 	private String reference;
 	private String website;
 	private int priceLevel;
 	private PlaceReviewBean[] reviews;
 	private String photoReference;
 	private String imgUrl;
 
 	public String getName() {
 		return name;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public String getPhone() {
 		return phone;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public double getLatitude() {
 		return latitude;
 	}
 
 	public double getLongitude() {
 		return longitude;
 	}
 
 	public double getRating() {
 		return rating;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public String getReference() {
 		return reference;
 	}
 
 	public String getWebsite() {
 		return website;
 	}
 
 	public int getPriceLevel() {
 		return priceLevel;
 	}
 
 	public PlaceReviewBean[] getReviews() {
 		return reviews;
 	}
 
 	public String getPhotoReference() {
 		return photoReference;
 	}
 
 	public String getImgUrl() {
 		return imgUrl;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 
 	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 
 	public void setRating(double rating) {
 		this.rating = rating;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public void setReference(String reference) {
 		this.reference = reference;
 	}
 
 	public void setWebsite(String website) {
 		this.website = website;
 	}
 
 	public void setPriceLevel(int priceLevel) {
 		this.priceLevel = priceLevel;
 	}
 
 	public void setReviews(PlaceReviewBean[] reviews) {
 		this.reviews = reviews;
 	}
 
 	public void setPhotoReference(String photoReference) {
 		this.photoReference = photoReference;
 	}
 
 	public void setImgUrl(String imgUrl) {
 		this.imgUrl = imgUrl;
 	}
 
 	@Override
 	public String toString() {
 		return "SearchPlaceDetailBean [longitude=" + longitude + ", latitude=" + latitude
 				+ ", address=" + address + ", url=" + url + ", name=" + name + ", phone=" + phone + ", website=" + website
 				+ ", priceLevel=" + priceLevel + ", rating=" + rating
 				+ ", types=" + type + ", reference=" + reference
 				+ ", reviews=" + reviews[0].toString() + "]";
 	}
 	
 	@Override
 	public int compareTo(SearchPlaceDetailBean arg0) {
 		return (int)(arg0.getRating() * 10 - rating * 10);
 		
 	}
 }
