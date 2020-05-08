 
 public class TravProf {
 
 	/**
 	 * @param args
 	 */
 	String travAgentID;
 	String firstName;
 	String lastName;
 	String address;
 	String phone;
 	float tripCost;
 	String travelType;
 	String paymentType;
 	MedCond medCondInfo;
 	
 	public static void main(String[] args) {
 		
 
 	}
 	
 	/* Constructor: TravProf
 	 * Variables:
 	 * 
 	 * 
 	 */
 	public TravProf(
 			String myTravAgentID,
 			String myFirstName,
 			String myLastName,
 			String myAddress,
 			String myPhone,
 			float myTripCost,
 			String myTravelType,
 			String myPaymentType,
 			MedCond myMedCond)
 	{
 
 		travAgentID = myTravAgentID;
 		firstName = myFirstName;
 		lastName = myLastName;
 		address = myAddress;
 		phone = myPhone;
 		tripCost = myTripCost;
 		travelType = myTravelType;
 		paymentType = myPaymentType;
 		
 	}
 	
 	
 	// Automatically generated Getters and Setters
 	public String getFirstName() {
 		return firstName;
 	}
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 	public String getLastName() {
 		return lastName;
 	}
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	public String getPhone() {
 		return phone;
 	}
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 	public float getTripCost() {
 		return tripCost;
 	}
 	public void setTripCost(float tripCost) {
 		this.tripCost = tripCost;
 	}
 	public String getTravelType() {
 		return travelType;
 	}
 	public void setTravelType(String travelType) {
 		this.travelType = travelType;
 	}
 	public String getPaymentType() {
 		return paymentType;
 	}
 	public void setPaymentType(String paymentType) {
 		this.paymentType = paymentType;
 	}
 	public MedCond getMedCondInfo() {
 		return medCondInfo;
 	}
 	public void setMedCondInfo(MedCond medCondInfo) {
 		this.medCondInfo = medCondInfo;
 	}
 	public void setTravAgentID(String travAgentID) {
 		this.travAgentID = travAgentID;
 	}
 
 	public String getTravAgentID() {
 
 		return travAgentID;
 	}
 
 }
