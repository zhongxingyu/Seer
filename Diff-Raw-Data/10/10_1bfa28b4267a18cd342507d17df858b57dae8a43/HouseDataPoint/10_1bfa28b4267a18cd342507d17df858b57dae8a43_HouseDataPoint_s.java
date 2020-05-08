 package cpsc310.server;
 
 import java.util.HashMap;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 /*
  * A single data point value representing a house
  */
 @Entity
 public class HouseDataPoint {
 
 	// Program Created entries:
 	// Composite of (TO_CIVIC_NUMBER + " " + STREET_NAME) if duplicates exist
 	// take entry with highest CURRENT_LAND_VALUE
 	@Id
 	private String houseID;
 	// null until Specifed on house edit to currently logged in google account
 	private String owner;
 	// 0 until User Specifed on house edit
 	private int price;
 	private boolean isSelling;
 	private double latitude;
 	private double longitude;
 
 	// From File entries:
 	// TO_CIVIC_NUMBER: Always in file, if not do not add
 	private int civicNumber;
 	// STREET_NAME: Always in file, if not do not add
 	private String streetName;
 	// PROPERTY_POSTAL_CODE: If missing add when user updates house
 	private String postalCode;
 	// CURRENT_LAND_VALUE: Always in file, if not do not add
 	private int currentLandValue;
 	// CURRENT_IMPROVEMENT_VALUE: Should always be in file, but optional
 	private int currentImprovementValue;
 	// ASSESSMENT_YEAR: Should always be in file, but optional
 	private int assessmentYear;
 	// PREVIOUS_LAND_VALUE: Optional
 	private int previousLandValue;
 	// PREVIOUS_IMPROVEMENT_VALUE: Optional
 	private int previousImporvementValue;
 	// YEAR_BUILT: Optional
 	private int yearBuilt;
 	// BIG_IMPROVEMENT_YEAR: Optional
 	private int bigImprovementYear;
 
 	/**
 	 * Constructor
 	 * 
 	 * @pre in houseRow <TO_CIVIC_NUMBER> and <STREET_NAME> and
 	 *      <CURRENT_LAND_VALUE> != null;
 	 * @post true;
 	 * @param houseRow
 	 *            - the HashMap containing the information for a house
 	 */
 	public HouseDataPoint(HashMap<String, String> houseRow) {
 		// Variables to be set by house data
 		civicNumber = convertNumberToInt(houseRow.get("TO_CIVIC_NUMBER"));
 		streetName = houseRow.get("STREET_NAME");
 		postalCode = houseRow.get("PROPERTY_POSTAL_CODE");
 		currentLandValue = convertNumberToInt(houseRow
 				.get("CURRENT_LAND_VALUE"));
 		currentImprovementValue = convertNumberToInt(houseRow
 				.get("CURRENT_IMPROVEMENT_VALUE"));
 		assessmentYear = convertNumberToInt(houseRow.get("ASSESSMENT_YEAR"));
 		previousLandValue = convertNumberToInt(houseRow
 				.get("PREVIOUS_LAND_VALUE"));
 		previousImporvementValue = convertNumberToInt(houseRow
 				.get("PREVIOUS_IMPROVEMENT_VALUE"));
 		yearBuilt = convertNumberToInt(houseRow.get("YEAR_BUILT"));
 		bigImprovementYear = convertNumberToInt(houseRow
 				.get("BIG_IMPROVEMENT_YEAR"));
 
 		houseID = civicNumber + " " + streetName;
 
 		// User specified data
 		owner = "";
 		isSelling = false;
 		price = 0;
 		latitude = -91;
 		longitude = -180;
 	}
 
 	// getters
 	public String getHouseID() {
 		return houseID;
 	}
 
 	public int getCivicNumber() {
 		return civicNumber;
 	}
 
 	public String streetName() {
 		return streetName;
 	}
 
 	public String getPostalCode() {
 		return postalCode;
 	}
 
 	public int getCurrentLandValue() {
 		return currentLandValue;
 	}
 
 	public int getCurrentImprovementValue() {
 		return currentImprovementValue;
 	}
 
 	public int getAssessmentYear() {
 		return assessmentYear;
 	}
 
 	public int getPreviousLandValue() {
 		return previousLandValue;
 	}
 
 	public int getPreviousImporvementValue() {
 		return previousImporvementValue;
 	}
 
 	public int getYearBuilt() {
 		return yearBuilt;
 	}
 
 	public int getBigImprovementYear() {
 		return bigImprovementYear;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public boolean getIsSelling() {
 		return isSelling;
 	}
 
 	public int getPrice() {
 		return price;
 	}
 
 	public double getLatitude() {
 		return latitude;
 	}
 
 	public double getLongitude() {
 		return longitude;
 	}
 
 	// setters
 	public void setOwner(String newOwner) {
 		owner = newOwner;
 	}
 
 	public void setIsSelling(boolean sell) {
 		isSelling = sell;
 	}
 
 	public void setPrice(int salePrice) {
 		price = salePrice;
 	}
 
 	public void setLatLng(double lat, double lng) {
 		latitude = lat;
 		longitude = lng;
 	}
 
 	/**
 	 * Helper method
 	 * 
 	 * @pre a string with only numeric values or null is passed
 	 * @post a integer is returned, -1 is return upon a null value
 	 * @param number
 	 *            - a string that represents a number
 	 */
 	private int convertNumberToInt(String number) {
 		String currentNumer = number;
 		if (!currentNumer.equals("")) {
 			//clean up entries
			number = currentNumer.replaceAll("\"", "");
			number = currentNumer.replaceAll("\\.\\d$", "");
 			return Integer.parseInt(number);
 		}
 		return -1;
 	}
 }
