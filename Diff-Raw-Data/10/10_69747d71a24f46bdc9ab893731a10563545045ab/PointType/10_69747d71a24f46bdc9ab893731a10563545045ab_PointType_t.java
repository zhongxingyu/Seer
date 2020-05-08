 package com.fixedd.AndroidTrimet.schemas.TypeDefs;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 /**
  * <p>Java class for PointType complex type.
  * 
  * <p>The following schema fragment specifies the expected content contained within this class.
  * 
  * <pre>
  * &lt;complexType name="PointType">
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element name="pos" type="{http://maps.trimet.org/maps/model/xml}GeoPointType"/>
  *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
  *       &lt;/sequence>
  *       &lt;attribute name="areaKey" type="{http://www.w3.org/2001/XMLSchema}string" />
  *       &lt;attribute name="areaValue" type="{http://www.w3.org/2001/XMLSchema}string" />
  *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 public class PointType implements Parcelable {
 
 	protected GeoPointType	mPos;
 	protected String		mDescription	= "";
 	protected String		mAreaKey		= "";
 	protected String		mAreaValue		= "";
 	protected String		mId				= "";
 
 	/**
 	 * Create a new, empty copy of this object.
 	 */
 	public PointType() {}
 
 	/**
 	 * Gets the value of the pos property.
 	 * 
 	 * @return
 	 *     possible object is
 	 *     {@link GeoPointType }
 	 *     
 	 */
 	public GeoPointType getPos() {
 		return mPos;
 	}
 
 	/**
 	 * Sets the value of the pos property.
 	 * 
 	 * @param value
 	 *     allowed object is
 	 *     {@link GeoPointType }
 	 *     
 	 */
 	public void setPos(GeoPointType value) {
 		mPos = value;
 	}
 
 	/**
 	 * Gets the value of the description property.
 	 * 
 	 * @return
 	 *     possible object is
 	 *     {@link String }
 	 *     
 	 */
 	public String getDescription() {
 		return mDescription;
 	}
 
 	/**
 	 * Sets the value of the description property.
 	 * 
 	 * @param value
 	 *     allowed object is
 	 *     {@link String }
 	 *     
 	 */
 	public void setDescription(String value) {
 		mDescription = value;
 	}
	
	public void appendDescription(String value) {
		mDescription += value;
	}
	
 
 	/**
 	 * Gets the value of the areaKey property.
 	 * 
 	 * @return
 	 *     possible object is
 	 *     {@link String }
 	 *     
 	 */
 	public String getAreaKey() {
 		return mAreaKey;
 	}
 
 	/**
 	 * Sets the value of the areaKey property.
 	 * 
 	 * @param value
 	 *     allowed object is
 	 *     {@link String }
 	 *     
 	 */
 	public void setAreaKey(String value) {
 		mAreaKey = value;
 	}
 
 	/**
 	 * Gets the value of the areaValue property.
 	 * 
 	 * @return
 	 *     possible object is
 	 *     {@link String }
 	 *     
 	 */
 	public String getAreaValue() {
 		return mAreaValue;
 	}
 
 	/**
 	 * Sets the value of the areaValue property.
 	 * 
 	 * @param value
 	 *     allowed object is
 	 *     {@link String }
 	 *     
 	 */
 	public void setAreaValue(String value) {
 		mAreaValue = value;
 	}
 
 	/**
 	 * Gets the value of the id property.
 	 * 
 	 * @return
 	 *     possible object is
 	 *     {@link String }
 	 *     
 	 */
 	public String getId() {
 		return mId;
 	}
 
 	/**
 	 * Sets the value of the id property.
 	 * 
 	 * @param value
 	 *     allowed object is
 	 *     {@link String }
 	 *     
 	 */
 	public void setId(String value) {
 		mId = value;
 	}
 
 
 	// **********************************************
 	//  for implementing Parcelable
 	// **********************************************
 
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		if (mPos != null) {
 			dest.writeInt(1);
 			dest.writeParcelable(mPos, flags);
 		} else
 			dest.writeInt(0);
 		dest.writeString(mDescription);
 		dest.writeString(mAreaKey    );
 		dest.writeString(mAreaValue  );
 		dest.writeString(mId         );
 	}
 
 	public static final Parcelable.Creator<PointType> CREATOR = new Parcelable.Creator<PointType>() {
 		public PointType createFromParcel(Parcel in) {
 			return new PointType(in);
 		}
 
 		public PointType[] newArray(int size) {
 			return new PointType[size];
 		}
 	};
 
	protected PointType(Parcel dest) {
		if (dest.readInt() == 1) mPos = (GeoPointType) dest.readParcelable(getClass().getClassLoader());
 		mDescription = dest.readString();
 		mAreaKey     = dest.readString();
 		mAreaValue   = dest.readString();
 		mId          = dest.readString();
 	}
 }
