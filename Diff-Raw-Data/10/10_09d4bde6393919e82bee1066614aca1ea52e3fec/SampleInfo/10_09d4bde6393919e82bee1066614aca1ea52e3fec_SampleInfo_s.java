 package org.gwaspi.model;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import org.gwaspi.constants.cDBSamples;
 
 @Entity
 @Table(name = "sampleInfo")
 @NamedQueries({
 	@NamedQuery(
 		name = "sampleInfo_listByStudyId",
 		query = "SELECT s FROM SampleInfo s WHERE s.poolId = :studyId"),
 	@NamedQuery(
 		name = "sampleInfo_listBySampleIdFamilyIdStudyId",
 		query = "SELECT s FROM SampleInfo s WHERE s.sampleId = :sampleId AND s.familyId = :familyId AND s.poolId = :studyId"),
 	@NamedQuery(
 		name = "sampleInfo_deleteByStudyId",
 		query = "DELETE FROM SampleInfo s WHERE s.poolId = :studyId"),
 })
 public class SampleInfo implements Comparable<SampleInfo>, Serializable {
 
 	public static enum Sex {
 		UNKNOWN,
 		MALE,
 		FEMALE;
 
 		/**
 		 * Parse by the PLINK standard
 		 */
 		public static Sex parse(String sexStr) {
 
 			try {
 				return Sex.values()[Integer.parseInt(sexStr)];
 			} catch (NumberFormatException ex) {
 				throw new ParseException(sexStr + " is not a valid sex value", ex);
 			}
 		}
 
 		/**
 		 * Encode with the PLINK standard,
 		 * optionally using the alternative long-encoding
 		 */
 		public static int toNumber(Sex sex) {
 			return sex.ordinal();
 		}
 	}
 
 	/**
 	 * PLINK standards:
 	 *
 	 * default:
 	 *   -9 missing
 	 *   0 missing
 	 *   1 unaffected
 	 *   2 affected
 	 *
 	 * alternative:
 	 *   -9 missing
 	 *   0 unaffected
 	 *   1 affected
 	 */
 	public static enum Affection {
 		/** Undefined / Missing */
 		UNKNOWN,
 		/** Control */
 		UNAFFECTED,
 		/** Case */
 		AFFECTED;
 
 		/** Parse by the PLINK standard */
 		public static Affection parse(String affectionStr) {
 			return parse(affectionStr, false);
 		}
 
 		/**
 		 * Parse by the PLINK standard,
 		 * optionally using the alternative encoding.
 		 * @param altEnc use the alternative encoding (@{see #Affection})
 		 */
 		public static Affection parse(String affectionStr, boolean altEnc) {
 
 			Affection result = Affection.UNKNOWN;
 
 			try {
 				int affectionInt = Integer.parseInt(affectionStr);
 
 				if (altEnc) {
 					switch (affectionInt) {
 						case 0:
 							result = UNAFFECTED;
 							break;
 						case 1:
 							result = AFFECTED;
 							break;
 						case -9:
 							result = UNKNOWN;
 							break;
 						default:
 							throw new ParseException(affectionInt + " is not a valid affection value");
 					}
 				} else {
 					// default
 					switch (affectionInt) {
 						case 0:
 							result = UNKNOWN;
 							break;
 						case 1:
 							result = UNAFFECTED;
 							break;
 						case 2:
 							result = AFFECTED;
 							break;
 						case -9:
 							result = UNKNOWN;
 							break;
 						default:
 							throw new ParseException(affectionInt + " is not a valid affection value (alternative standard)");
 					}
 				}
 			} catch (NumberFormatException ex) {
 				throw new ParseException("\"" + affectionStr + "\" is not a valid affection value", ex);
 			}
 
 			return result;
 		}
 
 		/**
 		 * Encode with the PLINK standard,
 		 * optionally using the alternative long-encoding
 		 */
 		public static int toNumber(Affection affection) {
 			return toNumber(affection, false);
 		}
 
 		/**
 		 * Encode with the PLINK standard.
 		 * @param altEnc use the alternative encoding (@{see #Affection})
 		 */
 		public static int toNumber(Affection affection, boolean altEnc) {
 
 			int result = -9;
 
 			if (altEnc) {
 				switch (affection) {
 					case UNKNOWN:
 						result = -9;
 						break;
 					case AFFECTED:
 						result = 0;
 						break;
 					case UNAFFECTED:
 						result = 1;
 						break;
 					default:
 						throw new ParseException(affection + " is not a valid affection value; can not be converted to a number");
 				}
 			} else {
 				// default
 				switch (affection) {
 					case UNKNOWN:
 						result = 0;
 						break;
 					case AFFECTED:
 						result = 1;
 						break;
 					case UNAFFECTED:
 						result = 2;
 						break;
 					default:
 						throw new ParseException(affection + " is not a valid affection value; can not be converted to a number (alternative standard)");
 				}
 			}
 
 			return result;
 		}
 	}
 
 	private SampleKey key;
 	private int orderId;
 	private String fatherId;
 	private String motherId;
 	private Sex sex;
 	private Affection affection;
 	private String category;
 	private String disease;
 	private String population;
 	private int age;
 	private String filter;
 	private Integer poolId;
 	private int approved;
 	private int status;
 
 	public SampleInfo() {
 
 		this.orderId = Integer.MIN_VALUE;
 		this.fatherId = "0";
 		this.motherId = "0";
 		this.sex = Sex.UNKNOWN;
 		this.affection = Affection.UNKNOWN;
 		this.category = "0";
 		this.disease = "0";
 		this.population = "0";
 		this.age = 0;
 		this.filter = "";
 		this.poolId = null;
 		this.approved = 0;
 		this.status = 0;
 		this.key = new SampleKey("0", "0");
 	}
 
 	public SampleInfo(String sampleId) {
 		this();
 
 		this.key = new SampleKey(sampleId, "0");
 	}
 
 	public SampleInfo(
 			String sampleId,
 			String familyId,
 			Sex sex,
 			Affection affection)
 	{
 		this.orderId = Integer.MIN_VALUE;
 		this.fatherId = "";
 		this.motherId = "";
 		this.sex = sex;
 		this.affection = affection;
 		this.category = "";
 		this.disease = "";
 		this.population = "";
 		this.age = Integer.MIN_VALUE;
 		this.filter = "";
 		this.poolId = null;
 		this.approved = 0;
 		this.status = 0;
 		this.key = new SampleKey(sampleId, familyId);
 	}
 
 	public SampleInfo(
 			String sampleId,
 			String familyId,
 			String fatherId,
 			String motherId,
 			Sex sex,
 			Affection affection,
 			String category,
 			String disease,
 			String population,
 			int age)
 	{
 		this.orderId = Integer.MIN_VALUE;
 		this.fatherId = fatherId;
 		this.motherId = motherId;
 		this.sex = sex;
 		this.affection = affection;
 		this.category = category;
 		this.disease = disease;
 		this.population = population;
 		this.age = age;
 		this.filter = "";
 		this.poolId = null;
 		this.approved = 0;
 		this.status = 0;
 		this.key = new SampleKey(sampleId, familyId);
 	}
 
 	public SampleInfo(
 			int orderId,
 			String sampleId,
 			String familyId,
 			String fatherId,
 			String motherId,
 			Sex sex,
 			Affection affection,
 			String category,
 			String disease,
 			String population,
 			int age,
 			String filter,
 			Integer poolId,
 			int approved,
 			int status)
 	{
 		this.orderId = orderId;
 		this.fatherId = fatherId;
 		this.motherId = motherId;
 		this.sex = sex;
 		this.affection = affection;
 		this.category = category;
 		this.disease = disease;
 		this.population = population;
 		this.age = age;
 		this.filter = filter;
 		this.poolId = poolId;
 		this.approved = approved;
 		this.status = status;
 		this.key = new SampleKey(sampleId, familyId);
 	}
 
 	@Override
 	public int compareTo(SampleInfo other) {
 		return hashCode() - other.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object other) {
 
 		boolean equal = false;
 
 		if (other instanceof SampleInfo) {
 			SampleInfo otherSampleInfo = (SampleInfo) other;
 			equal = getFamilyId().equals(otherSampleInfo.getFamilyId());
 			equal = equal && getSampleId().equals(otherSampleInfo.getSampleId());
 		}
 
 		return equal;
 	}
 
 	@Override
 	public int hashCode() {

		int hash = 7;
		hash = 31 * hash + (this.sampleId != null ? this.sampleId.hashCode() : 0);
		hash = 31 * hash + (this.familyId != null ? this.familyId.hashCode() : 0);
 		return hash;
 	}
 
 	/**
 	 * Returns a unique identifier for this specific sample.
 	 */
 	@Transient
 	public SampleKey getKey() {
 		return key;
 	}
 
 	@Column(
 		name       = "orderId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getOrderId() {
 		return orderId;
 	}
 
 	protected void setOrderId(int orderId) {
 		this.orderId = orderId;
 	}
 
 	/**
 	 * Returns the sample-id, which is unique in combination with the family-id.
 	 * @return the sample-id
 	 */
 	@Id
 	@Column(
 		name       = "sampleId",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getSampleId() {
 		return getKey().getSampleId();
 	}
 
 	/**
 	 * Sets the unique study identification number.
 	 * @param id the study-id to set
 	 */
 	public void setSampleId(String sampleId) {
 		this.key = new SampleKey(sampleId, getFamilyId());
 	}
 
 	/**
 	 * Returns the family-id, which is unique in combination with the sample-id.
 	 * @return the family-id
 	 */
 	@Id
 	@Column(
 		name       = "familyId",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getFamilyId() {
 		return getKey().getFamilyId();
 	}
 
 	protected void setFamilyId(String familyId) {
 		this.key = new SampleKey(getSampleId(), familyId);
 	}
 
 	@Column(
 		name       = "fatherId",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getFatherId() {
 		return fatherId;
 	}
 
 	protected void setFatherId(String fatherId) {
 		this.fatherId = fatherId;
 	}
 
 	@Column(
 		name       = "motherId",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getMotherId() {
 		return motherId;
 	}
 
 	protected void setMotherId(String motherId) {
 		this.motherId = motherId;
 	}
 
 	@Column(
 		name       = "sex",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public Sex getSex() {
 		return sex;
 	}
 
 	protected void setSex(Sex sex) {
 		this.sex = sex;
 	}
 
 	/** @deprecated */
 	@Transient
 	public String getSexStr() {
 
 		if (getSex() != null) {
 			return String.valueOf(getSex().ordinal());
 		} else {
 			return "0";
 		}
 	}
 
 	@Column(
 		name       = "affection",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public Affection getAffection() {
 		return affection;
 	}
 
 	protected void setAffection(Affection affection) {
 		this.affection = affection;
 	}
 
 	/** @deprecated */
 	@Transient
 	public String getAffectionStr() {
 
 		if (getAffection() != null) {
 			return String.valueOf(getAffection().ordinal());
 		} else {
 			return "0";
 		}
 	}
 
 	@Column(
 		name       = "category",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getCategory() {
 		return category;
 	}
 
 	protected void setCategory(String category) {
 		this.category = category;
 	}
 
 	@Column(
 		name       = "disease",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getDisease() {
 		return disease;
 	}
 
 	protected void setDisease(String disease) {
 		this.disease = disease;
 	}
 
 	@Column(
 		name       = "population",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getPopulation() {
 		return population;
 	}
 
 	protected void setPopulation(String population) {
 		this.population = population;
 	}
 
 	@Column(
 		name       = "age",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getAge() {
 		return age;
 	}
 
 	protected void setAge(int age) {
 		this.age = age;
 	}
 
 	@Column(
 		name       = "filter",
 		length     = 255,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getFilter() {
 		return filter;
 	}
 
 	protected void setFilter(String filter) {
 		this.filter = filter;
 	}
 
 	@Id
 	@Column(
 		name       = "poolId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public Integer getPoolId() {
 		return poolId;
 	}
 
 	public void setPoolId(Integer poolId) {
 		this.poolId = poolId;
 	}
 
 	@Column(
 		name       = "approved",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getApproved() {
 		return approved;
 	}
 
 	protected void setApproved(int approved) {
 		this.approved = approved;
 	}
 
 	@Column(
 		name       = "status",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getStatus() {
 		return status;
 	}
 
 	protected void setStatus(int status) {
 		this.status = status;
 	}
 
 	/**
 	 * @deprecated
 	 * @param fieldName see org.gwaspi.constants.cDBSamples#f_*
 	 * @return String, Integer, Boolean or null
 	 */
 	public Object getField(String fieldName) {
 
 		if (fieldName.equals(cDBSamples.f_ID)) {
 			return getOrderId();
 		} else if (fieldName.equals(cDBSamples.f_SAMPLE_ID)) {
 			return getSampleId();
 		} else if (fieldName.equals(cDBSamples.f_FAMILY_ID)) {
 			return getFamilyId();
 		} else if (fieldName.equals(cDBSamples.f_FATHER_ID)) {
 			return getFatherId();
 		} else if (fieldName.equals(cDBSamples.f_MOTHER_ID)) {
 			return getMotherId();
 		} else if (fieldName.equals(cDBSamples.f_SEX)) {
 			return getSexStr();
 		} else if (fieldName.equals(cDBSamples.f_AFFECTION)) {
 			return getAffectionStr();
 		} else if (fieldName.equals(cDBSamples.f_CATEGORY)) {
 			return getCategory();
 		} else if (fieldName.equals(cDBSamples.f_DISEASE)) {
 			return getDisease();
 		} else if (fieldName.equals(cDBSamples.f_POPULATION)) {
 			return getPopulation();
 		} else if (fieldName.equals(cDBSamples.f_AGE)) {
 			return getAge();
 		} else if (fieldName.equals(cDBSamples.f_FILTER)) {
 			return getFilter();
 		} else if (fieldName.equals(cDBSamples.f_POOL_ID)) {
 			return getPoolId();
 		} else if (fieldName.equals(cDBSamples.f_APPROVED)) {
 			return getApproved();
 		} else if (fieldName.equals(cDBSamples.f_STATUS_ID_FK)) {
 			return getStatus();
 		} else {
 			return null;
 		}
 	}
 }
