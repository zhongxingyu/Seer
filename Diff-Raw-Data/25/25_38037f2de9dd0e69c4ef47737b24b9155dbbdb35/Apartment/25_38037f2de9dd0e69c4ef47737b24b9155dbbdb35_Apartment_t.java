 package com.hansson.rento.entities;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import com.hansson.rento.utils.HtmlUtil;
 
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
	private Double mRooms = 0.0;
 	@Column(name = "MSIZE")
	private Integer mSize = 0;
 	@Column(name = "MRENT")
	private Integer mRent = 0;
 	// This identifier should be unique to be able to decide if the apartment is a new one or if it was already stored in the database.
 	@Column(name = "MIDENTIFIER", length = 512)
 	private String mIdentifier;
 	@Column(name = "MLANDLORD", length = 64)
 	private String mLandlord;
 	@Column(name = "MADDED")
 	private Date mAdded;
 	@Column(name = "MSTUDENT")
 	private Boolean mStudent;
 
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
 		mArea = HtmlUtil.textToHtml(area);
 	}
 
 	public void setCity(String city) {
 		mCity = HtmlUtil.textToHtml(city);
 	}
 
 	public void setAddress(String address) {
 		mAddress = HtmlUtil.textToHtml(address);
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
 	
 	public Boolean getStudent() {
 		return mStudent;
 	}
 
 	public void setStudent(Boolean student) {
 		mStudent = student;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((mAddress == null) ? 0 : mAddress.hashCode());
 		result = prime * result + ((mArea == null) ? 0 : mArea.hashCode());
 		result = prime * result + ((mCity == null) ? 0 : mCity.hashCode());
 		result = prime * result + ((mIdentifier == null) ? 0 : mIdentifier.hashCode());
 		result = prime * result + ((mLandlord == null) ? 0 : mLandlord.hashCode());
 		result = prime * result + ((mRent == null) ? 0 : mRent.hashCode());
 		result = prime * result + ((mRooms == null) ? 0 : mRooms.hashCode());
 		result = prime * result + ((mSize == null) ? 0 : mSize.hashCode());
 		result = prime * result + ((mStudent == null) ? 0 : mStudent.hashCode());
 		result = prime * result + ((mUrl == null) ? 0 : mUrl.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Apartment other = (Apartment) obj;
 		if (mAddress == null) {
 			if (other.mAddress != null)
 				return false;
 		} else if (!mAddress.equals(other.mAddress))
 			return false;
 		if (mArea == null) {
 			if (other.mArea != null)
 				return false;
 		} else if (!mArea.equals(other.mArea))
 			return false;
 		if (mCity == null) {
 			if (other.mCity != null)
 				return false;
 		} else if (!mCity.equals(other.mCity))
 			return false;
 		if (mIdentifier == null) {
 			if (other.mIdentifier != null)
 				return false;
 		} else if (!mIdentifier.equals(other.mIdentifier))
 			return false;
 		if (mLandlord == null) {
 			if (other.mLandlord != null)
 				return false;
 		} else if (!mLandlord.equals(other.mLandlord))
 			return false;
 		if (mRent == null) {
 			if (other.mRent != null)
 				return false;
 		} else if (!mRent.equals(other.mRent))
 			return false;
 		if (mRooms == null) {
 			if (other.mRooms != null)
 				return false;
 		} else if (!mRooms.equals(other.mRooms))
 			return false;
 		if (mSize == null) {
 			if (other.mSize != null)
 				return false;
 		} else if (!mSize.equals(other.mSize))
 			return false;
 		if (mStudent == null) {
 			if (other.mStudent != null)
 				return false;
 		} else if (!mStudent.equals(other.mStudent))
 			return false;
 		if (mUrl == null) {
 			if (other.mUrl != null)
 				return false;
 		} else if (!mUrl.equals(other.mUrl))
 			return false;
 		return true;
 	}
 
 }
