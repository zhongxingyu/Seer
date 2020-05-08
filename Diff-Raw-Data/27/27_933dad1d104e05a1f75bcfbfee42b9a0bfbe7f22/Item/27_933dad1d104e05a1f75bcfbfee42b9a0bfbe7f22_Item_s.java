 package com.example.swp_ucd_2013_eule.model;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 import com.example.swp_ucd_2013_eule.R;
 
 public class Item extends Model {
 
 	public static String ITEM_TYPE_STANDARD = "standard";
 	public static String ITEM_TYPE_SPECIAL = "special";
 
 	public static String ITEM_CATEGORY_ANIMALS = "animals";
 	public static String ITEM_CATEGORY_PLANTS = "plants";
 	public static String ITEM_CATEGORY_CLOTHES = "clothes";
 
 	private String name;
 	private String description;
 	private String type;
 	private Integer price;
 	private String category;
 	private Integer amount;
 	private Integer level;
 	private Boolean moveable;
 	private Integer imageId;
 
 	// public enum ForestItemType {
 	// STANDARD, SPECIAL
 	// };
 
 	public Item() {
 
 	}
 
 	public Item(String type, int imageID, String name, String description) {
 		this.type = type;
 		imageId = imageID;
 		setName(name);
 		setDescription(description);
 	}
 
 	public Item(String type, int imageID, String name, String description,
 			boolean moveable) {
 		this.type = type;
 		imageId = imageID;
 		setName(name);
 		setDescription(description);
 		this.moveable = moveable;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public Integer getImageId() {
 		return imageId;
 	}
 
 	public void setImageId(Integer imageId) {
 		this.imageId = imageId;
 	}
 
 	public Bitmap getImage(Context ctx) {
 		Resources r = ctx.getResources();
 		return BitmapFactory.decodeResource(r, dbToResource(imageId));
 	}
 
 	public void setImage(int imageID) {
 		imageId = imageID;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
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
 
 	public boolean isStandardItem() {
		return type == ITEM_TYPE_STANDARD;
 	}
 
 	public boolean isSpecialItem() {
		return type == ITEM_TYPE_SPECIAL;
 	}
 
 	public Integer getAmount() {
 		return amount;
 	}
 
 	public void setAmount(Integer amount) {
 		this.amount = amount;
 	}
 
 	public void incAmount() {
 		this.amount++;
 	}
 
 	public Integer getLevel() {
 		return level;
 	}
 
 	public void setLevel(Integer level) {
 		this.level = level;
 	}
 
 	public boolean isMoveable() {
 		return moveable;
 	}
 
 	public Boolean getMoveable() {
 		return moveable;
 	}
 
 	public void setMoveable(Boolean moveable) {
 		this.moveable = moveable;
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	private static int dbToResource(int dbValue) {
 		/**
 		 * int imgTree = R.drawable.item_tree; int imgFir = R.drawable.item_fir;
 		 * int imgFrog = R.drawable.item_gordan; int imgBush =
 		 * R.drawable.item_bush; int imgPlants = R.drawable.item_plants; int
 		 * imgAnimals = R.drawable.item_animals; int imgClothes =
 		 * R.drawable.item_clothes;
 		 */
 
 		switch (dbValue) {
 		case (1):
 			return R.drawable.item_fir;
 		case (2):
 			return R.drawable.item_tree;
 		case (3):
 			return R.drawable.item_gordan;
 		case (4):
 			return R.drawable.item_bush;
 		case (5):
 			return R.drawable.item_plants;
 		case (6):
 			return R.drawable.item_animals;
 		case (7):
 			return R.drawable.item_clothes;
 		default:
 			return R.drawable.item_tree;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "Item [name=" + name + ", description=" + description
 				+ ", type=" + type + ", price=" + price + ", amount=" + amount
 				+ ", level=" + level + ", imageId=" + imageId + "]";
 	}
 
 }
