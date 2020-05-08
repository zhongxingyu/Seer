 package com.hansson.rento.entities;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "APARTMENTS")
 public class Apartment {
 
 	@Id
 	@Column(name = "ID", unique = true, nullable = false)
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Integer mId;
 	@Column(name = "MAREA", length = 64)
 	private String mArea;
 	@Column(name = "MCITY", length = 64)
 	private String mCity;
 	@Column(name = "MADDRESS", length = 64)
 	private String mAddress;
 	@Column(name = "MURL", length = 512)
 	private String mUrl;
 	@Column(name = "MROOMS")
 	private Double mRooms;
 	@Column(name = "MSIZE")
 	private Integer mSize;
 	@Column(name = "MRENT")
 	private Integer mRent;
 	// This identifier should be unique to be able to decide if the apartment is a new one or if it was already stored in the database.
 	@Column(name = "MIDENTIFIER", length = 512)
 	private String mIdentifier;
 	@Column(name = "MLANDLORD", length = 64)
 	private String mLandlord;
 	@Column(name = "MADDED")
 	private Date mAdded;
 
 	public Apartment() {
 	}
 
 	public Apartment(String landlord) {
 		mLandlord = landlord;
 		mAdded = new Date();
 	}
 
 	public Integer getId() {
 		return mId;
 	}
 
 	public String getArea() {
 		return mArea;
 	}
 
 	public String getCity() {
 		return mCity;
 	}
 
 	public String getAddress() {
 		return mAddress;
 	}
 
 	public String getUrl() {
 		return mUrl;
 	}
 
 	public Double getRooms() {
 		return mRooms;
 	}
 
 	public Integer getSize() {
 		return mSize;
 	}
 
 	public Integer getRent() {
 		return mRent;
 	}
 
 	public String getIdentifier() {
 		return mIdentifier;
 	}
 
 	public String getLandlord() {
 		return mLandlord;
 	}
 
 	public void setId(Integer id) {
 		mId = id;
 	}
 
 	public void setArea(String area) {
 		mArea = area;
 	}
 
 	public void setCity(String city) {
 		mCity = city;
 	}
 
 	public void setAddress(String address) {
 		mAddress = address;
 	}
 
 	public void setUrl(String url) {
 		mUrl = url;
 	}
 
 	public void setRooms(Double rooms) {
 		mRooms = rooms;
 	}
 
 	public void setSize(Integer size) {
 		mSize = size;
 	}
 
 	public void setRent(Integer rent) {
 		mRent = rent;
 	}
 
 	public void setIdentifier(String identifier) {
 		mIdentifier = identifier;
 	}
 
 	public void setLandlord(String landlord) {
 		mLandlord = landlord;
 	}
 
 	public Date getAdded() {
 		return mAdded;
 	}
 	
 	public void setAdded(Date added) {
 		mAdded = added;
 	}
 	
 	@Override
 	public String toString() {
 		return mLandlord + " - " + mIdentifier;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Apartment) {
 			Apartment that = (Apartment) obj;
			return this.mLandlord.equals(that.mLandlord) && this.mIdentifier.equals(that.mIdentifier);
 		} else {
 			return false;
 		}
 	}
 
 
 }
