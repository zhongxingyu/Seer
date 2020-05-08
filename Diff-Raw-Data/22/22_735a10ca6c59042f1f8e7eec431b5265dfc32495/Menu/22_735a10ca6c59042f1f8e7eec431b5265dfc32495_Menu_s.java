 package com.res.model;
 
 import java.sql.Time;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.PrimaryKeyJoinColumn;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 @Entity
 @Table(name="menu", catalog="restaurant", uniqueConstraints={
 		@UniqueConstraint(columnNames="restaurantid"),
 		@UniqueConstraint(columnNames="foodcategoryid"),
 		@UniqueConstraint(columnNames="foodid")
 })
 public class Menu {
 	//TODO:cascade and hibernate relationships
 	private long menuId;
 	private long restaurantId;
 	private long foodCategoryId;
 	private long foodId;
 	private String menuNum;
 	private float small;
 	private float large;
 	private String lunchNum;
 	private float lunch;
 	private String comboNum;
 	private float combo;
 	private boolean spicy;
 	private boolean rice;
 	private boolean sauce;
 	private boolean noodle;
 	private int pieces;
 	private boolean appetizerCombo;
 	private Time createdDate;
 	private String createdBy;
 	private Time lastUpdatedDate;
 	private String lastUpdatedBy;
 	
 	private FoodCategory foodCategory;
 	private Food food;
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	@Column(name="menuid")
 	public long getMenuId() {
 		return menuId;
 	}
 	public void setMenuId(long menuId) {
 		this.menuId = menuId;
 	}
 	
 	@Column(name="restaurantid", unique=false, nullable=false, length=11)
 	public long getRestaurantId() {
 		return restaurantId;
 	}
 	public void setRestaurantId(long restaurantId) {
 		this.restaurantId = restaurantId;
 	}
 	
	@Column(name="foodcategoryid", unique=false, nullable=false, length=11)
 	public long getFoodCategoryId() {
 		return foodCategoryId;
 	}
 	public void setFoodCategoryId(long foodCategoryId) {
 		this.foodCategoryId = foodCategoryId;
 	}
 	
 	@Column(name="foodid", unique=false, nullable=false, length=11)
 	public long getFoodId() {
 		return foodId;
 	}
 	public void setFoodId(long foodId) {
 		this.foodId = foodId;
 	}
 	
 	@Column(name="menunum", unique=false, nullable=true, length=5)
 	public String getMenuNum() {
 		return menuNum;
 	}
 	public void setMenuNum(String menuNum) {
 		this.menuNum = menuNum;
 	}
 	
 	@Column(name="small", unique=false, nullable=true, length=5)
 	public float getSmall() {
 		return small;
 	}
 	public void setSmall(float small) {
 		this.small = small;
 	}
 	
 	@Column(name="large", unique=false, nullable=false, length=5)
 	public float getLarge() {
 		return large;
 	}
 	public void setLarge(float large) {
 		this.large = large;
 	}
 	
 	@Column(name="lunchnum", unique=false, nullable=true, length=5)
 	public String getLunchNum() {
 		return lunchNum;
 	}
 	public void setLunchNum(String lunchNum) {
 		this.lunchNum = lunchNum;
 	}
 	
 	@Column(name="lunch", unique=false, nullable=true, length=5)
 	public float getLunch() {
 		return lunch;
 	}
 	public void setLunch(float lunch) {
 		this.lunch = lunch;
 	}
 	
 	@Column(name="combonum", unique=false, nullable=true, length=5)
 	public String getComboNum() {
 		return comboNum;
 	}
 	public void setComboNum(String comboNum) {
 		this.comboNum = comboNum;
 	}
 	
 	@Column(name="combo", unique=false, nullable=true, length=5)
 	public float getCombo() {
 		return combo;
 	}
 	public void setCombo(float combo) {
 		this.combo = combo;
 	}
 	
 	@Column(name="spicy", unique=false, nullable=true, length=1)
 	public boolean isSpicy() {
 		return spicy;
 	}
 	public void setSpicy(boolean spicy) {
 		this.spicy = spicy;
 	}
 	
 	@Column(name="rice", unique=false, nullable=true, length=1)
 	public boolean hasRice() {
 		return rice;
 	}
 	public void setRice(boolean rice) {
 		this.rice = rice;
 	}
 	
 	@Column(name="sauce", unique=false, nullable=true, length=1)
 	public boolean hasSauce() {
 		return sauce;
 	}
 	public void setSauce(boolean sauce) {
 		this.sauce = sauce;
 	}
 	
 	@Column(name="noodle", unique=false, nullable=true, length=1)
 	public boolean hasNoodle() {
 		return noodle;
 	}
 	public void setNoodle(boolean noodle) {
 		this.noodle = noodle;
 	}
 	
 	@Column(name="pieces", unique=false, nullable=true, length=2)
 	public int getPieces() {
 		return pieces;
 	}
 	public void setPieces(int pieces) {
 		this.pieces = pieces;
 	}
 	
 	@Column(name="appetizercombo", unique=false, nullable=true, length=1)
 	public boolean isAppetizerCombo() {
 		return appetizerCombo;
 	}
 	public void setAppetizerCombo(boolean appetizerCombo) {
 		this.appetizerCombo = appetizerCombo;
 	}
 
 	@Column(name="createddate", unique=false, nullable=true, length=12)
 	public Time getCreatedDate() {
 		return createdDate;
 	}
 	public void setCreatedDate(Time createdDate) {
 		this.createdDate = createdDate;
 	}
 	
 	@Column(name="createdby", unique=false, nullable=true, length=20)
 	public String getCreatedBy() {
 		return createdBy;
 	}
 	public void setCreatedBy(String createdBy) {
 		this.createdBy = createdBy;
 	}
 	
 	@Column(name="lastupdateddate", unique=false, nullable=true, length=12)
 	public Time getLastUpdatedDate() {
 		return lastUpdatedDate;
 	}
 	public void setLastUpdatedDate(Time lastUpdatedDate) {
 		this.lastUpdatedDate = lastUpdatedDate;
 	}
 	
 	@Column(name="lastupdatedby", unique=false, nullable=true, length=12)
 	public String getLastUpdatedBy() {
 		return lastUpdatedBy;
 	}
 	public void setLastUpdatedBy(String lastUpdatedBy) {
 		this.lastUpdatedBy = lastUpdatedBy;
 	}
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="foodcategoryid", nullable=false)
 	public FoodCategory getFoodCategory() {
 		return foodCategory;
 	}
 	public void setFoodCategory(FoodCategory foodCategory) {
 		this.foodCategory = foodCategory;
 	}
 	
 	@OneToOne(fetch=FetchType.LAZY)
 	@PrimaryKeyJoinColumn
 	public Food getFood() {
 		return food;
 	}
 	public void setFood(Food food) {
 		this.food = food;
 	}
 	
 }
