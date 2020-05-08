 package it.infn.chaos.mds.business;
 
 import java.util.Date;
 
 import it.infn.chaos.mds.RPCConstants;
 
 import org.bson.BasicBSONObject;
 import org.ref.common.business.*;
 import org.ref.common.exception.RefException;
 
 /**
  * @author bisegni
  */
 @DBTable(name = "dataset_attribute")
 public class DatasetAttribute extends BSONBusinessObject {
 	private static final long	serialVersionUID	= 745140933367435576L;
 	@DBPrimaryKey
 	@DBColumn(name = "id_attribute")
 	private Integer				attributeID			= null;
 	@DBPrimaryKey
 	@DBColumn(name = "version")
 	private Integer				version				= 1;
 	@DBPrimaryKey
 	@DBColumn(name = "id_device")
 	private Integer				deviceID			= null;
 	@DBPrimaryKey
 	@DBColumn(name = "id_dataset")
 	private Integer				datasetID			= null;
 	@DBColumn(name = "name")
 	private String				name				= null;
 	@DBColumn(name = "description")
 	private String				description			= null;
 	@DBColumn(name = "range_max")
 	private String				rangeMax			= null;
 	@DBColumn(name = "range_min")
 	private String				rangeMin			= null;
 	@DBColumn(name = "type")
 	private Integer				type				= null;
 	@DBColumn(name = "direction")
 	private Integer				direction			= null;
 	@DBColumn(name = "timestamp")
 	private Date				timestamp			= null;
 	@DBColumn(name = "attribute_check", maxDimension = 128)
 	private String				check				= null;
 	@DBColumn(name = "default_value", maxDimension = 128)
 	private String				defaultValue		= null;
 	@DBColumn(name = "tags_path", maxDimension = 128)
 	private String				tagsPath			= null;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ref.common.business.BusinessObject#equals(java.lang.Object)
 	 */
 	public boolean equals(Object attr) {
 		if (!(attr instanceof DatasetAttribute))
 			return false;
 		DatasetAttribute attrCasted = (DatasetAttribute) attr;
 		boolean result = attrCasted.getDeviceID() != null
 							&& getDeviceID() != null && attrCasted.getDeviceID().equals(getDeviceID());
 		result = result
 					&& attrCasted.getDatasetID() != null && getDatasetID() != null && attrCasted.getDatasetID().equals(getDatasetID());
 		result = result
 					&& attrCasted.getAttributeID() != null && getAttributeID() != null && attrCasted.getAttributeID().equals(getAttributeID());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ref.common.business.BusinessObject#hashCode()
 	 */
 	public int hashCode() {
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ref.common.business.BusinessObject#toString()
 	 */
 	public String toString() {
 		return getDescription() != null ? String.format("[%s]- %s", getName(), getDescription()) : String.format("[%s]", getName());
 	}
 
 	/**
 	 * @return the attributeID
 	 */
 	public Integer getAttributeID() {
 		return attributeID;
 	}
 
 	/**
 	 * @param attributeID
 	 *            the attributeID to set
 	 */
 	public void setAttributeID(Integer attributeID) {
 		this.attributeID = attributeID;
 	}
 
 	/**
 	 * @return the deviceID
 	 */
 	public Integer getDeviceID() {
 		return deviceID;
 	}
 
 	/**
 	 * @param deviceID
 	 *            the deviceID to set
 	 */
 	public void setDeviceID(Integer deviceID) {
 		this.deviceID = deviceID;
 	}
 
 	/**
 	 * @return the datasetID
 	 */
 	public Integer getDatasetID() {
 		return datasetID;
 	}
 
 	/**
 	 * @param datasetID
 	 *            the datasetID to set
 	 */
 	public void setDatasetID(Integer datasetID) {
 		this.datasetID = datasetID;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description
 	 *            the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the rangeMax
 	 */
 	public String getRangeMax() {
 		return rangeMax;
 	}
 
 	/**
 	 * @param rangeMax
 	 *            the rangeMax to set
 	 * @throws RefException
 	 */
 	public void setRangeMax(String rangeMax) throws RefException {
 		checkDefaultValueForType(rangeMax);
 		this.rangeMax = rangeMax;
 	}
 
 	/**
 	 * @return the rangeMin
 	 */
 	public String getRangeMin() {
 		return rangeMin;
 	}
 
 	/**
 	 * @param rangeMin
 	 *            the rangeMin to set
 	 * @throws RefException
 	 */
 	public void setRangeMin(String rangeMin) throws RefException {
 		checkDefaultValueForType(rangeMax);
 		this.rangeMin = rangeMin;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public Integer getType() {
 		return type;
 	}
 
 	/**
 	 * @param type
 	 *            the type to set
 	 */
 	public void setType(Integer type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the direction
 	 */
 	public Integer getDirection() {
 		return direction;
 	}
 
 	/**
 	 * @param direction
 	 *            the direction to set
 	 */
 	public void setDirection(Integer direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * @return the check
 	 */
 	public String getCheck() {
 		return check;
 	}
 
 	/**
 	 * @param check
 	 *            the check to set
 	 */
 	public void setCheck(String check) {
 		this.check = check;
 	}
 
 	/**
 	 * @return the timestamp
 	 */
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	/**
 	 * @param timestamp
 	 *            the timestamp to set
 	 */
 	public void setTimestamp(Date timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	/**
 	 * @return the version
 	 */
 	public Integer getVersion() {
 		return version;
 	}
 
 	/**
 	 * @param version
 	 *            the version to set
 	 */
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see it.infn.chaos.mds.business.BSONBusinessObject#fillFromBson(java.lang. Object)
 	 */
 	public void fillFromBson(Object bson) throws RefException {
 		if (!(bson instanceof BasicBSONObject))
 			return;
 
 		BasicBSONObject bsonAttributeDescription = (BasicBSONObject) bson;
 		if (bsonAttributeDescription.containsField(RPCConstants.DATASET_ATTRIBUTE_DESCRIPTION)) {
 			setDescription(bsonAttributeDescription.getString(RPCConstants.DATASET_ATTRIBUTE_DESCRIPTION));
 		}
 		if (bsonAttributeDescription.containsField(RPCConstants.DATASET_ATTRIBUTE_NAME)) {
 			setName(bsonAttributeDescription.getString(RPCConstants.DATASET_ATTRIBUTE_NAME));
 		}
 		if (bsonAttributeDescription.containsField(RPCConstants.DATASET_ATTRIBUTE_DIRECTION)) {
 			setDirection(bsonAttributeDescription.getInt(RPCConstants.DATASET_ATTRIBUTE_DIRECTION));
 		}
 		if (bsonAttributeDescription.containsField(RPCConstants.DATASET_ATTRIBUTE_TYPE)) {
 			setType(bsonAttributeDescription.getInt(RPCConstants.DATASET_ATTRIBUTE_TYPE));
 		}
 		if (bsonAttributeDescription.containsField(RPCConstants.DATASET_ATTRIBUTE_MAX_RANGE)) {
 			setRangeMax(bsonAttributeDescription.getString(RPCConstants.DATASET_ATTRIBUTE_MAX_RANGE));
 		}
 		if (bsonAttributeDescription.containsField(RPCConstants.DATASET_ATTRIBUTE_MIN_RANGE)) {
 			setRangeMin(bsonAttributeDescription.getString(RPCConstants.DATASET_ATTRIBUTE_MIN_RANGE));
 		}
 		if (bsonAttributeDescription.containsField(RPCConstants.DATASET_ATTRIBUTE_DEFAULT_VALUE)) {
 			setDefaultValue(bsonAttributeDescription.getString(RPCConstants.DATASET_ATTRIBUTE_DEFAULT_VALUE));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see it.infn.chaos.mds.business.BSONBusinessObject#toBson()
 	 */
 	@Override
 	public Object toBson() {
 		BasicBSONObject bsonAttributeDescription = new BasicBSONObject();
 		if (getName() != null) {
 			bsonAttributeDescription.append(RPCConstants.DATASET_ATTRIBUTE_NAME, getDescription());
 		}
 
 		if (getDescription() != null) {
 			bsonAttributeDescription.append(RPCConstants.DATASET_ATTRIBUTE_DESCRIPTION, getDescription());
 		}
 
 		if (getDirection() != null) {
 			bsonAttributeDescription.append(RPCConstants.DATASET_ATTRIBUTE_DIRECTION, getDirection());
 		}
 
 		if (getType() != null) {
 			bsonAttributeDescription.append(RPCConstants.DATASET_ATTRIBUTE_TYPE, getType());
 		}
 
 		if (getRangeMax() != null) {
 			bsonAttributeDescription.append(RPCConstants.DATASET_ATTRIBUTE_MAX_RANGE, getRangeMax());
 		}
 
 		if (getRangeMin() != null) {
 			bsonAttributeDescription.append(RPCConstants.DATASET_ATTRIBUTE_MIN_RANGE, getRangeMin());
 		}
 
 		if (getDefaultValue() != null) {
 			bsonAttributeDescription.append(RPCConstants.DATASET_ATTRIBUTE_DEFAULT_VALUE, getDefaultValue());
 		}
 		return bsonAttributeDescription;
 	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see it.infn.chaos.mds.business.BSONBusinessObject#toBsonHash()
	 */
	@Override
	public String toBsonHash() throws Throwable {
		BasicBSONObject bsonDesc = (BasicBSONObject) toBson();
		//remove the field taht dind't be considered for hashing compare operation
		bsonDesc.remove(RPCConstants.DATASET_ATTRIBUTE_MAX_RANGE);
		bsonDesc.remove(RPCConstants.DATASET_ATTRIBUTE_MIN_RANGE);
		bsonDesc.remove(RPCConstants.DATASET_ATTRIBUTE_DEFAULT_VALUE);
		return SHA1(bsonDesc.toString());
	}

 	/**
 	 * @return
 	 */
 	public String getNamedDirection() {
 		String result = "";
 		switch (getDirection()) {
 			case 0:
 				result = "Input";
 			break;
 			case 1:
 				result = "Output";
 			break;
 			case 2:
 				result = "Bidirectional";
 			break;
 		}
 		return result;
 	}
 
 	/**
 	 * CS_CMDM_ACTION_DESC_PAR_TYPE_INT32 = 0, CS_CMDM_ACTION_DESC_PAR_TYPE_INT64,
 	 * CS_CMDM_ACTION_DESC_PAR_TYPE_DOUBLE, CS_CMDM_ACTION_DESC_PAR_TYPE_STRING,
 	 * CS_CMDM_ACTION_DESC_PAR_TYPE_BYTEARRAY
 	 * 
 	 * @return
 	 */
 	public String getNamedType() {
 		String result = "";
 		switch (getType()) {
 			case 0:
 				result = "Int32";
 			break;
 			case 1:
 				result = "Int64";
 			break;
 			case 2:
 				result = "Double";
 			break;
 			case 3:
 				result = "String";
 			break;
 			case 4:
 				result = "ByteArray";
 			break;
 		}
 		return result;
 	}
 
 	/**
 	 * CS_CMDM_ACTION_DESC_PAR_TYPE_INT32 = 0, CS_CMDM_ACTION_DESC_PAR_TYPE_INT64,
 	 * CS_CMDM_ACTION_DESC_PAR_TYPE_DOUBLE, CS_CMDM_ACTION_DESC_PAR_TYPE_STRING,
 	 * CS_CMDM_ACTION_DESC_PAR_TYPE_BYTEARRAY
 	 * 
 	 * @return
 	 * @throws RefException
 	 */
 	public void checkDefaultValueForType(String strValueToCheck) throws RefException {
 		try {
 			if (strValueToCheck == null)
 				return;
 			switch (getType()) {
 				case 0:
 					Integer.parseInt(strValueToCheck);
 				break;
 				case 1:
 					Long.parseLong(strValueToCheck);
 				break;
 				case 2:
 					Double.parseDouble(strValueToCheck);
 				break;
 			}
 		} catch (Exception e) {
 			throw new RefException("The value can't be associated to the type");
 		}
 
 	}
 
 	/**
 	 * @return the defaultValue
 	 */
 	public String getDefaultValue() {
 		return defaultValue;
 	}
 
 	/**
 	 * @param defaultValue
 	 *            the defaultValue to set
 	 * @throws RefException
 	 */
 	public void setDefaultValue(String defaultValue) throws RefException {
 		checkDefaultValueForType(defaultValue);
 		if (defaultValue != null) {
 			int defv = Integer.parseInt(defaultValue);
 			int maxv = Integer.parseInt(getRangeMax());
 			int minv = Integer.parseInt(getRangeMin());
 			if (defv < minv) {
 				throw new RefException(String.format("The default value %s is lower than the minimum value %s", defaultValue, getRangeMin()));
 			}
 			if (defv > maxv) {
 				throw new RefException(String.format("The default value %s is greater than the maximum value %s", defaultValue, getRangeMax()));
 			}
 		}
 		this.defaultValue = defaultValue;
 
 	}
 
 	/**
 	 * @return the tagsPath
 	 */
 	public String getTagsPath() {
 		return tagsPath;
 	}
 
 	/**
 	 * @param tagsPath
 	 *            the tagsPath to set
 	 */
 	public void setTagsPath(String tagsPath) {
 		this.tagsPath = tagsPath;
 	}

 }
