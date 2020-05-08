 package edu.ucsb.cs56.S13.lab04.BrandonNewman;
 
 /**
 StreetAddress is a class that represents a street address. It has
 two private variables: a String streetName, which represents the 
 name of a street, and an int streetNumber, which represents the
 specific location on street an address is located. 
 
 @author Brandon Newman
 @version 2013/05/02 for lab04, cs56, s13
 */
 
 public class StreetAddress {
 
	private String streetName;
	private int streetNumber;
 
 	/**
 	no-arg Constructor for objects of class StreetAddress
 	*/
 	public StreetAddress() {}
 
 	/**
 	Two-arg constructor for objects of class StreetAddress
 	@param streetName street name
 	@param streetNumber street number
 	*/
 	public StreetAddress(String streetName, int streetNumber)
 	{
 
 		this.streetName = streetName;
 		this.streetNumber = streetNumber;
 	}
 
 	/**
 	sets the street name
 	@param streetName the new street name
 	*/
 	public void setStreetName(String streetName) 
 	{
 		this.streetName = streetName;
 	}
 
 	/**
 	sets the street number
 	@param streetNumber the new street number
 	*/
 	public void setStreetNumber(int streetNumber)
 	{
 		this.streetNumber = streetNumber;
 	}
 
 	/**
 	gets the street name
 	@return street name
 	*/
 	public String getStreetName()
 	{
 		return this.streetName;
 	}	
 
 	/**
 	gets the street number
 	@return street number
 	*/
 	public int getStreetNumber()
 	{
 		return this.streetNumber;
 	}
 
 	/**
 	formats the street address as a String such as:
 	"785 Camino Del Sur"
 
 	@return formatted string representing an address
 	*/
 	public String toString()
 	{
 		String fullStreet = this.streetNumber + " " + this.streetName;
 		return fullStreet;
 	}
 
 	/**
 	checks to see if two StreetAddress objects are meaningfully equal
 	@return true or false
 	*/
 	public boolean equals(Object o) {
 
 	if (o == null)
 	    return false;
 	if (!(o instanceof StreetAddress))
 	    return false;
 
 	StreetAddress s = (StreetAddress) o;
 
 	if (s.getStreetNumber() == this.streetNumber && s.getStreetName() == this.streetName)
 		return true; //true though	
 
 	return false; // @@@ STUB
 	
     }
 
     /**
     Main method testing the two arg Constructor and ToString()
     */
     public static void main(String [] args) 
     {
     	StreetAddress one = new StreetAddress("Hank Street", 443);
     	System.out.println(one);
     }
 
 }
 
 
 	
