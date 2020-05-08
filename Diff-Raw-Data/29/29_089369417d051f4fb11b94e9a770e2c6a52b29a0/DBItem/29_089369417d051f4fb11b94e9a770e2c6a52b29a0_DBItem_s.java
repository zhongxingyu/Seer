 package edu.gatech.oad.rocket.findmythings.server.db.model;
 
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.Unindex;
 import edu.gatech.oad.rocket.findmythings.server.model.Category;
 import edu.gatech.oad.rocket.findmythings.server.model.Type;
 import edu.gatech.oad.rocket.findmythings.server.util.Searchable;
 import edu.gatech.oad.rocket.findmythings.server.util.SearchableHelper;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Database entity representing a Lost/Found Item.
  *
  * User: zw
  */
 @Entity @Index
 public class DBItem implements Searchable {
 
 	@Id Long iID;
 
 	/**
 	 * Basic string descriptors.
 	 */
 	private String name = "";
 	@Unindex private String location = "";
 	private String description = "";
 	private String submittingUser = "";
 
 	private final Set<String> searchTokens = new HashSet<>();
 
 	private final Date submittedDate = new Date();
 
 	/**
 	 * The type of an Item. Defines which list it goes on.
 	 */
 	private Type type = Type.FOUND;
 
 	/**
 	 * The category of an Item. Used for filtering.
 	 */
 	@Unindex private Category category = Category.MISC;
 
 	/**
 	 * The user-defined date.
 	 */
 	@Unindex private Date date = new Date();
 
 	/**
 	 * The user-defined reward (used only for Lost Type).
 	 */
 	@Unindex private int reward;
 
 
 	protected DBItem() {}
 
 	public DBItem(String name, String description) {
 		this();
 		setName(name);
 		setDescription(description);
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	// Setters
 
 	/**
 	 * Sets the name of the Item, but only
 	 * if it's not null
 	 * @param name A non-null string
 	 */
 	public void setName(String name) {
 		this.name = (name != null) ? name.trim() : "";
 		SearchableHelper.updateSearchTokens(this);
 	}
 
 	/**
 	 * Sets the description of the Item, but only
 	 * if it's not null
 	 * @param description A non-null string
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 		SearchableHelper.updateSearchTokens(this);
 	}
 
 	/**
 	 * Sets the location of the Item, but only
 	 * if it's not null.
 	 * @param location A non-null string
 	 */
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	/**
 	 * Sets the type of an Item
 	 * @param type An enumerated Type value.
 	 */
 	public void setType(Type type) {
 		this.type = type;
 	}
 
 	/**
 	 * Sets the category of an Item
 	 * @param category An enumerated Category value.
 	 */
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	/**
 	 * Sets the date of an Item
 	 * @param date A date object
 	 */
 	public void setDate(Date date) {
 		this.date = (date != null) ? date : new Date();
 	}
 
 	/**
 	 * Sets the reward of a Lost Item
 	 * @param reward An integer primitive.
 	 */
 	public void setReward(int reward) {
 		this.reward = reward;
 	}
 
 	/**
 	 * @param submittingUser the submittingUser to set
 	 */
 	public void setSubmittingUser(String submittingUser) {
 		this.submittingUser = submittingUser;
 	}
 
 	// Getters
 
 	/**
 	 * The name value of an Item. Read-only.
 	 * @return A user-input string.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * The short description value of an Item.
 	 * @return A user-input string. Editable.
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Returns the type of the item, i.e., which list it is displayed on.
 	 * @return A Type enumerated value.
 	 */
 	public Type getType() {
 		return type;
 	}
 
 	/**
 	 * Returns the category of the item.
 	 * @return A Category enumerated value.
 	 */
 	public Category getCategory() {
 		return category;
 	}
 
 	/**
 	 * A user-input date for the Item.
 	 * @return A date object.
 	 */
 	public Date getDate() {
 		return date;
 	}
 
 	/**
 	 * A monetary compensation for a lost Item.
 	 * @return An integer representing the user-input reward
 	 * of the receiving item.
 	 */
 	public int getReward() {
 		return reward;
 	}
 
 	/**
 	 * The location of an item, like an address or coordinates.
 	 * @return A string representing the user-input location
 	 * of the receiving item.
 	 */
 	public String getLocation() {
 		return location;
 	}
 
 	/**
 	 * @return the submittingUser
 	 */
 	public String getSubmittingUser() {
 		return submittingUser;
 	}
 
 	public Date getSubmittedDate() {
 		return submittedDate;
 	}
 
 	public String getSearchableContent() {
		return getName() + " " + getCategory().searchableValue() + " " + Integer.toString(getReward()) + " " + getDescription();
 	}
 
 	public Set<String> getSearchTokens() {
 		return searchTokens;
 	}
 
 }
