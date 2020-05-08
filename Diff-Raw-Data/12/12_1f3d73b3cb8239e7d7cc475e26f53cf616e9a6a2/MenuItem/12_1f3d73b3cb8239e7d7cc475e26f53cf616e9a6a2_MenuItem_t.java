 package com.intuitive.yummy.models;
 
 import java.sql.Date;
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 //includes menu item information, includes their ingredients' information
 
 public class MenuItem implements Model {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final String modelName = "MenuItem";
 	private int id;
 	private int vendorID;
 	private String name;
 	private double price;
 	private String category;
 	private String description;
 	private boolean availability;
 	private String pictureURL;
 	private ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 	private Date dateCreated;
 	private Date dateLastModified;
 	private Boolean isDeleted;
 	
 	public void setID(int id) {
 		this.id = id;
 	}
 	public void setVendorID(int vendorID) {
 		this.vendorID = vendorID;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public void setPrice(double price) {
 		this.price = price;
 	}
 	public void setCategory(String category) {
 		this.category = category;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public void setAvailability(boolean availability) {
 		this.availability = availability;
 	}
 	public void setPictureURL(String pictureURL) {
 		this.pictureURL = pictureURL;
 	}
 	public void setIngredient(ArrayList<Ingredient> ingredients) {
 		this.ingredients = ingredients;
 	}
 	public void addIngredient(Ingredient ingredient) {
 		ingredients.add(ingredient);
 	}
 	public void removeIngredient(Ingredient ingredient) {
 		ingredients.remove(ingredient);
 	}
 	public int getID() {
 		return id;
 	}
 	public int getVendorID() {
 		return vendorID;
 	}
 	public String getName() {
 		return name;
 	}
 	public double getPrice() {
 		return price;
 	}
 	public String getCategory() {
 		return category;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public boolean getAvailability() {
 		return availability;
 	}
 	public String getPictureURL() {
 		return pictureURL;
 	}
 	public ArrayList<Ingredient> getIngredient() {
 		return ingredients;
 	}
 	
 	public MenuItem() {};
 	public MenuItem(int id, String name, double price, String category, String description, boolean availability, String pictureURL, ArrayList<Ingredient> ingredients) {
 		this.id = id;
 		this.name = name;
 		this.price = price;
 		this.category = category;
 		this.description = description;
 		this.availability = availability;
 		this.pictureURL = pictureURL;
 		this.ingredients = ingredients;
 	}
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 	@Override
 	public void writeToParcel(Parcel out, int flags) {
 		out.writeInt(id);
 		if(name == null)
 			out.writeInt(0);
 		else
 		{
 			out.writeInt(1);
 			out.writeString(name);
 		}
 		if(category == null)
 			out.writeInt(0);
 		else
 		{
 			out.writeInt(1);
 			out.writeString(category);
 		}
 		out.writeDouble(price);
 		if(description == null)
 			out.writeInt(0);
 		else
 		{
 			out.writeInt(1);
 			out.writeString(description);
 		}
 		if(availability)
 			out.writeInt(1);
 		else
 			out.writeInt(0);
 		if(pictureURL == null)
 			out.writeInt(0);
 		else
 		{
 			out.writeInt(1);
 			out.writeString(pictureURL);
 		}
 		if(dateCreated == null)
 			out.writeInt(0);
 		else{
 			out.writeInt(1);
 			out.writeString(dateCreated.toString());
 		}
 		if(dateLastModified == null)
 			out.writeInt(0);
 		else{
 			out.writeInt(1);
 			out.writeString(dateLastModified.toString());
 		}
 		if(isDeleted == null)
 			out.writeInt(0);
 		else{
 			out.writeInt(1);
			out.writeInt(isDeleted ? 1 : 0);
 		}
 	}
 	@Override
 	public Model createFromParcel(Parcel parcel) {
 		return new MenuItem(parcel);
 	}
 	
 	public MenuItem(Parcel parcel) {
 		id = parcel.readInt();
 		if(parcel.readInt() == 1)
 			name = parcel.readString();
 		if(parcel.readInt() == 1)
 			category = parcel.readString();
 		price = parcel.readDouble();
 		if(parcel.readInt() == 1)
 			description = parcel.readString();
 		if(parcel.readInt() == 1)
 			availability = true;
 		else
 			availability = false;
 		if(parcel.readInt() == 1)
 			pictureURL = parcel.readString();
 		if(parcel.readInt() == 1)
 			dateCreated = Date.valueOf(parcel.readString());
 		if(parcel.readInt() == 1)
 			dateLastModified = Date.valueOf(parcel.readString());
 		if(parcel.readInt() == 1)
 			isDeleted = parcel.readInt() == 1;
 	}
 	
 	public static final Parcelable.Creator<MenuItem> CREATOR = new Creator<MenuItem> () {
 		public MenuItem createFromParcel(Parcel source) {
 			return new MenuItem(source);
 		}
 		public MenuItem[] newArray(int size) {
 			return new MenuItem[size];
 		}
 	};
 	
 	@Override
 	public Model[] newArray(int size) {
 		return null;
 	}
 	@Override
 	public void parseJson(JSONObject json) {
 		try {
 			id = json.getInt("id");
 			name = json.getString("name");
 			category = json.getString("category");
 			price = json.getDouble("price");
 			description = json.getString("description");
			availability = json.getBoolean("available");
 			pictureURL = json.getString("picture_url");
 			vendorID = json.getInt("vendor_id");
 		} catch (JSONException e) {
 			Log.e("Yummy", "JSON object did not map to Vendor object.");
 			e.printStackTrace();
 		}
 	}
 	@Override
 	public String getModelName() {
 		return modelName;
 	}
 	
 }
