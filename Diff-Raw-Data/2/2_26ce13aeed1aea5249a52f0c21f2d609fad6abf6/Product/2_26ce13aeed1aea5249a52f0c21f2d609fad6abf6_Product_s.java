 /**
  *  Copyright 2011 Chris Barrett
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.cbarrett.scotchservices.lcbo.domain;
 
 import org.cbarrett.common.domain.DomainObject;
 import org.cbarrett.common.util.TimeFormats;
 import org.cbarrett.scotchservices.lcbo.domain.serializer.JsonDateTimeDeserializer;
 import org.cbarrett.scotchservices.lcbo.domain.serializer.JsonShortDateDeserializer;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.codehaus.jackson.map.annotate.JsonDeserialize;
 import org.joda.time.DateTime;
 
 public class Product implements DomainObject {
 	
 	public enum StockType {
 	    LCBO, VINTAGES 
 	}
 	public enum Sweetness {
 		XD, D, MD, M, MS, S
 	}
 	
 	private String id;
 	private Boolean is_dead;
 	private String name;
 	// "tags":"ardbeg corryvreckan spirits scotch whisky scotland united kingdom mchs bottle"
 	// tags are the search mechanism for the LCBO, and can be error prone as they are highly unstructured
 	private String tags;
 	private Boolean is_discontinued;
 	private int price_in_cents;
 	private int regular_price_in_cents;
 	private int limited_time_offer_savings_in_cents;
 	private DateTime limitedTimeOfferEndsOn;
 	private int bonus_reward_miles;
 	private DateTime bonusRewardMilesEndsOn;
 	private StockType stock_type;
 	private String primary_category;
 	private String secondary_category;
 	private String origin;
 	private String packaging;
 	private String package_unit_type;
 	private int package_unit_volume_in_milliliters;
 	private int total_package_units;
 	private int volume_in_milliliters;
 	private int alcohol_content;
 	private int price_per_liter_of_alcohol_in_cents;
 	private int price_per_liter_in_cents;
 	private int inventory_count;
 	private int inventory_volume_in_milliliters;
 	private int inventory_price_in_cents;
 	private String sugar_content;
 	private String producer_name;
 	private DateTime releasedOn;
 	private Boolean has_value_added_promotion;
 	private Boolean has_limited_time_offer;
 	private Boolean has_bonus_reward_miles;
 	private Boolean is_seasonal;
 	private Boolean is_vqa;
 	private Boolean is_kosher;
 	private String value_added_promotion_description;
 	private String description;
 	private String servingSuggestion;
 	private String tastingNote;
 	private DateTime updatedAt;
 	private String productNo;
 	
 	public String getId() {
 		return id;
 	}
 	public Boolean getIs_dead() {
 		return is_dead;
 	}
 	public String getName() {
 		return name;
 	}
 	public String getTags() {
 		return tags;
 	}
 	public Boolean getIs_discontinued() {
 		return is_discontinued;
 	}
 	public int getPrice_in_cents() {
 		return price_in_cents;
 	}
 	public int getRegular_price_in_cents() {
 		return regular_price_in_cents;
 	}
 	public int getLimited_time_offer_savings_in_cents() {
 		return limited_time_offer_savings_in_cents;
 	}
 	public DateTime getLimitedTimeOfferEndsOn() {
 		return limitedTimeOfferEndsOn;
 	}
 	public int getBonus_reward_miles() {
 		return bonus_reward_miles;
 	}
 	public DateTime getBonusRewardMilesEndsOn() {
 		return bonusRewardMilesEndsOn;
 	}
 	public StockType getStock_type() {
 		return stock_type;
 	}
 	public String getPrimary_category() {
 		return primary_category;
 	}
 	public String getSecondary_category() {
 		return secondary_category;
 	}
 	public String getOrigin() {
 		return origin;
 	}
 	public String getPackage() {
 		return packaging;
 	}
 	public String getPackage_unit_type() {
 		return package_unit_type;
 	}
 	public int getPackage_unit_volume_in_milliliters() {
 		return package_unit_volume_in_milliliters;
 	}
 	public int getTotal_package_units() {
 		return total_package_units;
 	}
 	public int getVolume_in_milliliters() {
 		return volume_in_milliliters;
 	}
 	public int getAlcohol_content() {
 		return alcohol_content;
 	}
 	public int getPrice_per_liter_of_alcohol_in_cents() {
 		return price_per_liter_of_alcohol_in_cents;
 	}
 	public int getPrice_per_liter_in_cents() {
 		return price_per_liter_in_cents;
 	}
 	public int getInventory_count() {
 		return inventory_count;
 	}
 	public int getInventory_volume_in_milliliters() {
 		return inventory_volume_in_milliliters;
 	}
 	public int getInventory_price_in_cents() {
 		return inventory_price_in_cents;
 	}
 	public String getSugar_content() {
 		return sugar_content;
 	}
 	public String getProducer_name() {
 		return producer_name;
 	}
 	public DateTime getReleasedOn() {
 		return releasedOn;
 	}
 	public Boolean getHas_value_added_promotion() {
 		return has_value_added_promotion;
 	}
 	public Boolean getHas_limited_time_offer() {
 		return has_limited_time_offer;
 	}
 	public Boolean getHas_bonus_reward_miles() {
 		return has_bonus_reward_miles;
 	}
 	public Boolean getIs_seasonal() {
 		return is_seasonal;
 	}
 	public Boolean getIs_vqa() {
 		return is_vqa;
 	}
 	public Boolean getIs_kosher() {
 		return is_kosher;
 	}
 	public String getValue_added_promotion_description() {
 		return value_added_promotion_description;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public String getServing_suggestion() {
 		return servingSuggestion;
 	}
 	public String getTasting_note() {
 		return tastingNote;
 	}
 	public DateTime getUpdatedAt() {
 		return updatedAt;
 	}
 	public String getProductNo() {
 		return productNo;
 	}
 	
 	public void setId(String id) {
 		this.id = id;
 	}
 	public void setIs_dead(Boolean isDead) {
 		is_dead = isDead;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public void setTags(String tags) {
 		this.tags = tags;
 	}
 	public void setIs_discontinued(Boolean isDiscontinued) {
 		is_discontinued = isDiscontinued;
 	}
 	public void setPrice_in_cents(int priceInCents) {
 		price_in_cents = priceInCents;
 	}
 	public void setRegular_price_in_cents(int regularPriceInCents) {
 		regular_price_in_cents = regularPriceInCents;
 	}
 	public void setLimited_time_offer_savings_in_cents(
 			int limitedTimeOfferSavingsInCents) {
 		limited_time_offer_savings_in_cents = limitedTimeOfferSavingsInCents;
 	}
 	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
 	@JsonProperty("limited_time_offer_ends_on")
 	public void setLimitedTimeOfferEndsOn(DateTime limitedTimeOfferEndsOn) {
 		this.limitedTimeOfferEndsOn = limitedTimeOfferEndsOn;
 	}
 	public void setBonus_reward_miles(int bonusRewardMiles) {
 		bonus_reward_miles = bonusRewardMiles;
 	}
 	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
 	@JsonProperty("bonus_reward_miles_ends_on")
 	public void setBonusRewardMilesEndsOn(DateTime bonusRewardMilesEndsOn) {
 		this.bonusRewardMilesEndsOn = bonusRewardMilesEndsOn;
 	}
 	public void setStock_type(StockType stockType) {
 		stock_type = stockType;
 	}
 	public void setPrimary_category(String primaryCategory) {
 		primary_category = primaryCategory;
 	}
 	public void setSecondary_category(String secondaryCategory) {
 		secondary_category = secondaryCategory;
 	}
 	public void setOrigin(String origin) {
 		this.origin = origin;
 	}
 	public void setPackage(String packaging) {
 		this.packaging = packaging;
 	}
 	public void setPackage_unit_type(String packageUnitType) {
 		package_unit_type = packageUnitType;
 	}
 	public void setPackage_unit_volume_in_milliliters(
 			int packageUnitVolumeInMilliliters) {
 		package_unit_volume_in_milliliters = packageUnitVolumeInMilliliters;
 	}
 	public void setTotal_package_units(int totalPackageUnits) {
 		total_package_units = totalPackageUnits;
 	}
 	public void setVolume_in_milliliters(int volumeInMilliliters) {
 		volume_in_milliliters = volumeInMilliliters;
 	}
 	public void setAlcohol_content(int alcoholContent) {
 		alcohol_content = alcoholContent;
 	}
 	public void setPrice_per_liter_of_alcohol_in_cents(
 			int pricePerLiterOfAlcoholInCents) {
 		price_per_liter_of_alcohol_in_cents = pricePerLiterOfAlcoholInCents;
 	}
 	public void setPrice_per_liter_in_cents(int pricePerLiterInCents) {
 		price_per_liter_in_cents = pricePerLiterInCents;
 	}
 	public void setInventory_count(int inventoryCount) {
 		inventory_count = inventoryCount;
 	}
 	public void setInventory_volume_in_milliliters(int inventoryVolumeInMilliliters) {
 		inventory_volume_in_milliliters = inventoryVolumeInMilliliters;
 	}
 	public void setInventory_price_in_cents(int inventoryPriceInCents) {
 		inventory_price_in_cents = inventoryPriceInCents;
 	}
 	public void setSugar_content(String sugarContent) {
 		sugar_content = sugarContent;
 	}
 	public void setProducer_name(String producerName) {
 		producer_name = producerName;
 	}
 	@JsonDeserialize(using = JsonShortDateDeserializer.class)
 	@JsonProperty("released_on")	
 	public void setReleasedOn(DateTime releasedOn) {
 		this.releasedOn = releasedOn;
 	}
 	public void setHas_value_added_promotion(Boolean hasValueAddedPromotion) {
 		has_value_added_promotion = hasValueAddedPromotion;
 	}
 	public void setHas_limited_time_offer(Boolean hasLimitedTimeOffer) {
 		has_limited_time_offer = hasLimitedTimeOffer;
 	}
 	public void setHas_bonus_reward_miles(Boolean hasBonusRewardMiles) {
 		has_bonus_reward_miles = hasBonusRewardMiles;
 	}
 	public void setIs_seasonal(Boolean isSeasonal) {
 		is_seasonal = isSeasonal;
 	}
 	public void setIs_vqa(Boolean isVqa) {
 		is_vqa = isVqa;
 	}
 	public void setIs_kosher(Boolean isKosher) {
 		is_kosher = isKosher;
 	}
 	public void setValue_added_promotion_description(
 			String valueAddedPromotionDescription) {
 		value_added_promotion_description = valueAddedPromotionDescription;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	@JsonProperty("serving_suggestion")
 	public void setServingSuggestion(String servingSuggestion) {
 		this.servingSuggestion = servingSuggestion;
 	}
 	@JsonProperty("tasting_note")
 	public void setTastingNote(String tastingNote) {
 		this.tastingNote = tastingNote;
 	}
 	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
 	@JsonProperty("updated_at")
 	public void setUpdatedAt(DateTime updatedAt) {
 		this.updatedAt = updatedAt;
 	}
 	@JsonProperty("product_no")
 	public void setProductNo(String productNo) {
 		this.productNo = productNo;
 	}
 	
 	/*
 	alcohol_content	 Alcohol content (Divide by 100 for decimal value)
 	bonus_reward_miles	 Number of bonus air miles
 	bonus_reward_miles_ends_on	 When bonus air miles are no longer valid
 	description	 Product description (not available for all products)
 	has_bonus_reward_miles	 True if the product has bonus air miles
 	has_limited_time_offer	 True if the product is on sale
 	has_value_added_promotion	 True if the product has a value added promotion
 	id	 The LCBO product ID / number
 	inventory_count	 Total units across all stores
 	inventory_price_in_cents	 Total retail price of all units across all stores
 	inventory_volume_in_milliliters	 Total volume of all units across all stores
 	is_dead	 When products are removed from the catalog they are marked as "dead"
 	is_discontinued	 True if the product has been marked as discontinued
 	is_kosher	 True if the product is designated as Kosher.
 	is_seasonal	 True if the product is designated as seasonal
 	is_vqa	 True if the product is designated as VQA
 	limited_time_offer_ends_on	 When the sale price is no longer valid
 	limited_time_offer_savings_in_cents	 Savings in cents if on sale
 	name	 Product name
 	origin	 Country of origin / manufacture
 	package	 Full package description
 	package_unit_type	 Package unit type (bottle, can, etc.)
 	package_unit_volume_in_milliliters	 The volume of one unit in the package
 	price_in_cents	 Current retail price in cents
 	price_per_liter_in_cents	 The beverage price per liter
 	price_per_liter_of_alcohol_in_cents	 The alcohol price per liter
 	primary_category	 Primary product stock category
 	producer_name	 Name of the company that produces the product
 	product_no	 The LCBO product ID / number [Deprecated]
 	regular_price_in_cents	 Regular retail price in cents
 	released_on	 Official release date (usually unspecified)
 	secondary_category	 Secondary LCBO product stock category (Not all products have one.)
 	serving_suggestion	 LCBO serving suggestion (not available for all products)
 	stock_type	 Either "LCBO" or "VINTAGES"
 	sugar_content	 The sugar content designation, can be a residual sugar number or designation such as extra-dry (XD), medium sweet (MS), etc.
 	tags	 A string of tags that reflect the product
 	tasting_note	 Professional tasting note (not available for all products)
 	total_package_units	 Number of units in a package
 	updated_at	 Time that the product information was updated
 	value_added_promotion_description	 Contents of the value added promotion offer if available
 	volume_in_milliliters	 Total volume of all units in package
 	*/
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		
 		result = prime * result + id.hashCode();
 		result = prime * result + name.hashCode();
 		result = prime * result + producer_name.hashCode();
 		result = prime * result + origin.hashCode();
 		result = prime * result + (is_dead ? 1 : 0);
 		result = prime * result + (is_discontinued ? 1 : 0);
 		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
 		
 		return result;
 	}
 	@Override
 	public boolean equals(Object otherObject) {
 		boolean result = false;
 
 		if (this == otherObject) {
 			result = true;
 		} else if (otherObject == null) {
 			result = false;
 		} else if (!(otherObject instanceof Product)) {
 			result = false;
 		} else {
 			Product otherProduct = (Product) otherObject;
 			result = (
 					  (id.equals(otherProduct.id))
 					  && (name.equals(otherProduct.name))
 					  && ((producer_name == null) ? otherProduct.producer_name == null : producer_name.equals(otherProduct.producer_name))
 					  && ((origin == null) ? otherProduct.origin == null : origin.equals(otherProduct.origin))
 					  && (is_dead == otherProduct.is_dead)
 					  && (is_discontinued == otherProduct.is_discontinued)
 					  && ((updatedAt == null) ? otherProduct.updatedAt == null : updatedAt.equals(otherProduct.updatedAt))
 					 );
 		}
 		return result;
 	}
 	@Override
 	public String toString() {
		StringBuilder sb = new StringBuilder(Dataset.class.getSimpleName());
 		sb.append("[id: " + id + ",");
 		sb.append("name: " + name + ",");
 		sb.append("producer_name: " + producer_name + ",");
 		sb.append("origin: " + origin + ",");
 		sb.append("primary_category: " + primary_category + ",");
 		sb.append("secondary_category: " + secondary_category + ",");
 		sb.append("is_dead: " + Boolean.valueOf(is_dead) + ",");
 		sb.append("is_discontinued: " + Boolean.valueOf(is_discontinued) + ",");
 		sb.append("stock_type: " + stock_type.toString() + ",");
 		sb.append("releasedOn: " + releasedOn.toString(TimeFormats.stdOutputFormat) + ",");
 		sb.append("updatedAt: " + updatedAt.toString(TimeFormats.stdOutputFormat) + "]");
 		return sb.toString();
 	}	
 }
