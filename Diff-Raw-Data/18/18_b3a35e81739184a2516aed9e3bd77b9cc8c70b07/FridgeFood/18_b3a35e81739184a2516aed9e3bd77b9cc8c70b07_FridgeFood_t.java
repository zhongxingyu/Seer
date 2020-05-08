 package com.openfridge;
 
 import java.net.URLDecoder;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import android.os.Bundle;
 
 /**
  * Encapsulates the data associated with an item of food, including description
  * and expiry date.
  * 
  * @author Tom, Jesse
  * 
  */
 public class FridgeFood implements Cloneable {
 	private GregorianCalendar creationDateTime;
 	private String description;
 	private GregorianCalendar expirationDate;
 	private int id = -1;
 	private GregorianCalendar lastUpdateDateTime;
 	private int userId = -1;
 
 	public FridgeFood() {
 		this("", "", "", "", "", "");
 	}
 
 	public FridgeFood(String descriptionString, String expirationDateString,
 			String userIdString) {
 		this(descriptionString, expirationDateString, "", "", "", userIdString);
 	}
 
 	public FridgeFood(String descriptionString, String expirationDateString,
 			String creationDateTimeString, String lastUpdateDateTimeString,
 			String idString, String userIdString) {
 
 		setDescription(URLDecoder.decode(descriptionString));
 		creationDateTime = defaultDate(creationDateTimeString, new GregorianCalendar());
 		lastUpdateDateTime = defaultDate(lastUpdateDateTimeString, new GregorianCalendar());
 		GregorianCalendar weekLater = getCreationDateTime();
 		weekLater.roll(Calendar.DAY_OF_YEAR, 7);
 		expirationDate = defaultDate(expirationDateString, weekLater);
 		setId(idString);
 		try {
 			userId = Integer.parseInt(userIdString.trim());
 		} catch (NumberFormatException e) {
 			userId = Integer.parseInt(DataClient.getInstance().getUID());
 		}
 	}
 
 	private GregorianCalendar defaultDate (String dateString, GregorianCalendar defaultDateValue) {
 		GregorianCalendar ans = parseXMLDateTime(dateString);
 		if (ans==null) {
 			return defaultDateValue;
 		} else {
 			return ans;
 		}
 	}
 	/**
 	 * Parse a string containing date and time information from the XML format
 	 * to a GregorianCalendar object representation
 	 * 
 	 * @param xmlDateTime
 	 *            an XML string with Date and Time information in the format
 	 *            yyyy-mm-ddThh:mm:ssZ.
 	 * @return An object representation of the given Date and Time string
 	 */
 	private GregorianCalendar parseXMLDateTime(String xmlDateTime) {
 		String dateTimeString = xmlDateTime.replace('-', ' ').replace('T', ' ')
 				.replace('Z', ' ').replace(':', ' ');
 		Scanner s = new Scanner(dateTimeString);
 
 		try {
 			int year = s.nextInt();
 			int month = s.nextInt();
 			int day = s.nextInt();
 
 			// Return if only the date is included
 			if (!s.hasNext())
 				return new GregorianCalendar(year, month, day);
 			int hour = s.nextInt();
 			int minute = s.nextInt();
 			int second = s.nextInt();
 
 			return new GregorianCalendar(year, month, day, hour, minute, second);
 		} catch (IllegalStateException e) {
 			return null;
 		} catch (NoSuchElementException e) {
 			return null;
 		}
 	}
 	
 	public GregorianCalendar getCreationDateTime() {
 		return (GregorianCalendar) creationDateTime.clone();
 	}
 
 	public String getCreationDateTimeString() {
 		return creationDateTime.toString();
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public GregorianCalendar getExpirationDate() {
 		return (GregorianCalendar) expirationDate.clone();
 	}
 
 	public String getExpirationDateString() {
 		return expirationDate.get(Calendar.YEAR) + "-"
 				+ expirationDate.get(Calendar.MONTH) + "-"
 				+ expirationDate.get(Calendar.DAY_OF_MONTH);
 	}
 
 	public int getExpirationYear() {
 		return expirationDate.get(Calendar.YEAR);
 	}
 
 	public int getExpirationMonth() {
 		return expirationDate.get(Calendar.MONTH);
 	}
 
 	public int getExpirationDay() {
 		return expirationDate.get(Calendar.DAY_OF_MONTH);
 	}
 
 	public int getId() {
		if (id==-1) {
			throw new RuntimeException("Invalid ID!");
		}
 		return id;
 	}
 
 	public GregorianCalendar getLastUpdateDateTime() {
 		return (GregorianCalendar) lastUpdateDateTime.clone();
 	}
 
 	public String getLastUpdateDateTimeString() {
 		return lastUpdateDateTime.toString();
 	}
 
 	public int getUserId() {
		if (userId==-1) {
			throw new RuntimeException("Invalid userID!");
		}

 		return userId;
 	}
 
 	public String toString() {
 		return description;
 	}
 
 	/**
 	 * Get a short string summary of this food item, including its description
 	 * and expiration date.
 	 * 
 	 * @return A string containing a summary of this food record
 	 */
 	public String getSummary() {
 		return description + "\nExpires: " + expirationDate.get(Calendar.YEAR)
 				+ "-" + expirationDate.get(Calendar.MONTH) + "-"
 				+ expirationDate.get(Calendar.DAY_OF_MONTH) + "\n";
 	}
 
 	public Object clone() {
 		try {
 			FridgeFood o = (FridgeFood) super.clone();
 			o.creationDateTime = null;
 			o.lastUpdateDateTime = null;
 
 			if (creationDateTime != null) {
 				o.creationDateTime = (GregorianCalendar) creationDateTime
 						.clone();
 			}
 
 			if (lastUpdateDateTime != null) {
 				o.lastUpdateDateTime = (GregorianCalendar) lastUpdateDateTime
 						.clone();
 			}
 
 			o.expirationDate = (GregorianCalendar) expirationDate.clone();
 
 			return o;
 		} catch (CloneNotSupportedException e) {
 			// Should never happen
 			return null;
 		}
 	}
 
 	public static Bundle bundleFood(FridgeFood f) {
 		Bundle b = new Bundle();
 		b.putString("foodDescription", f.getDescription());
 		b.putString("expirationDate", f.getExpirationDateString());
 		b.putString("userId", Integer.toString(f.getUserId()));
 		return b;
 	}
 
 	private static String getBundledFoodDescription(Bundle b) {
 		return b.getString("foodDescription");
 	}
 
 	private static String getBundledExpirationDateString(Bundle b) {
 		return b.getString("expirationDate");
 	}
 
 	private static String getBundledUserIdString(Bundle b) {
 		return b.getString("userId");
 	}
 
 	/**
 	 * Return a newly created FridgeFood item, initialized with the data from
 	 * the Bundle, if it exists, otherwise default.
 	 */
 	public static FridgeFood getFoodFromBundle(Bundle b) {
 		if (b != null && b.containsKey("foodDescription")
 				&& b.containsKey("expirationDate") && b.containsKey("userId")) {
 			return new FridgeFood(getBundledFoodDescription(b),
 					getBundledExpirationDateString(b),
 					getBundledUserIdString(b));
 		} else {
 			return new FridgeFood();
 		}
 	}
 
 	public void setDescription(String description) {
 		markAsUpdated();
 		this.description = description.trim();
 	}
 
 	public void setExpirationDate(String expirationDate) {
 		setExpirationDate(parseXMLDateTime(expirationDate));
 		
 	}
 
 	public void setExpirationDate(GregorianCalendar expirationDate) {
 		if (expirationDate!=null) {
 			markAsUpdated();
 			this.expirationDate = expirationDate;
 		}
 	}
 
 	public void setId(String id) {
 		try {
 			setId(Integer.parseInt(id.trim()));
 		} catch (NumberFormatException e) {
 		}
 	}
 
 	public void setId(int id) {
 		markAsUpdated();
 		this.id = id;
 	}
 	
 	private void markAsUpdated() {
 		lastUpdateDateTime = new GregorianCalendar();
 	}
 	
 }
