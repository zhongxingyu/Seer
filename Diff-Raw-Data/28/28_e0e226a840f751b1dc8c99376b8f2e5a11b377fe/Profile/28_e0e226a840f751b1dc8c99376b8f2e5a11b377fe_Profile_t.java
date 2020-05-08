 package com.genability.client.types;
 
 import java.io.Serializable;
 import java.util.List;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
 /**
  * Represents a Usage Profile.
  * 
  */
 
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class Profile implements Serializable {
 
 	public static final String REST_TYPE = "UsageProfile";
 	
 	/**
      * 
      */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * private member variable for ProfileId.
 	 */
 	private String profileId;
 
 	/**
 	 * private member variable for ProfileName.
 	 */
 	private String profileName;
 
 	/**
 	 * Private member variable for accountId
 	 */
 	private String accountId;
 	
 	/**
 	 * Private member variable for providerOrgId
 	 */
 	private String providerOrgId;
 	
 	/**
 	 * private member variable for Description.
 	 */
 	private String description;
 
 	/**
 	 * private member variable for SourceId.
 	 */
 	private String sourceId;
 
 	/**
 	 * private member to hold list of <code>ReadingDataSummary</code>
 	 */
 	private List<ReadingDataSummary> readingDataSummaries;
 	
 	/**
 	 * private member to hold list of <code>ReadingData</code>
 	 */
 	private List<ReadingData> readingData;
 	
 	/**
 	 * Private member indicating if this profiles interval and other pre-calculated/cached
 	 * data is current.
 	 */
 	private Integer dataStatus;
 	
 	private Boolean isDefault;
 	
 	/**
 	 * private member for type of service of this profile
 	 */	
 	private String serviceTypes;
 	
 	/**
 	 * This is the default constructor.
 	 */
 	public Profile() {
 		// no-op
 
 	} // end of constructor
 
 	/**
 	 * This is the constructor that sets the primary key information on
 	 * construction. In other words, it sets the property.
 	 */
 	public Profile(String profileId) {
 		this.profileId = profileId;
 
 	} // end of constructor
 
 	/**
 	 * Access method for ProfileId.
 	 */
 	public String getProfileId() {
 		return profileId;
 	}
 
 	/**
 	 * Mutator for ProfileId.
 	 */
 	public void setProfileId(String profileId) {
 		this.profileId = profileId;
 	}
 
 	/**
 	 * Access method for ProfileName.
 	 */
 	public String getProfileName() {
 		return profileName;
 	}
 
 	/**
 	 * Mutator for ProfileName.
 	 */
 	public void setProfileName(String profileName) {
 		this.profileName = profileName;
 	}
 
 	/**
 	 * Access method for Description.
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Mutator for Description.
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * Access method for SourceId.
 	 */
 	public String getSourceId() {
 		return sourceId;
 	}
 
 	/**
 	 * Mutator for SourceId.
 	 */
 	public void setSourceId(String sourceId) {
 		this.sourceId = sourceId;
 	}
 
 	/**
 	 * @return the readingDataSummaries
 	 */
 	public List<ReadingDataSummary> getReadingDataSummaries() {
 		return readingDataSummaries;
 	}
 
 	/**
 	 * @param readingDataSummaries
 	 *            the readingDataSummaries to set
 	 */
 	public void setReadingDataSummaries(List<ReadingDataSummary> readingDataSummaries) {
 		this.readingDataSummaries = readingDataSummaries;
 	}
 
 	/**
 	 * Access method for ReadingData.
 	 */
 	public List<ReadingData> getReadingData() {
 		return readingData;
 	}
 
 	/**
 	 * Mutator method for ReadingData.
 	 */
 	public void setReadingData(List<ReadingData> readingData) {
 		this.readingData = readingData;
 	}
 	
 	/**
 	 * @return the accountId
 	 */
 	public String getAccountId() {
 		return accountId;
 	}
 
 	/**
 	 * @param accountId
 	 *            the accountId to set
 	 */
 	public void setAccountId(String accountId) {
 		this.accountId = accountId;
 	}
 
 	/**
 	 * This is the Genability ID of the provider of this account.
 	 * 
 	 * @return
 	 */
 	@JsonIgnore
 	public String getProviderOrgId() {
 		return providerOrgId;
 	}
 
 	/**
 	 * Use this to set the Genability ID of the provider of this account. This
 	 * field is maintained (or at least validated) by server side code so that
 	 * it cannot be populated erroneously by a different provider.
 	 * 
 	 * @param providerOrgId
 	 */
 	public void setProviderOrgId(String providerOrgId) {
 		this.providerOrgId = providerOrgId;
 	}
 	
 	
 	/**
 	 * This holds the status of the cached/pre-calculated interval data.
 	 * @return
 	 */
 	@JsonIgnore
 	public Integer getDataStatus() {
 		return dataStatus;
 	}
 
 	@JsonIgnore
 	public void setDataStatus(Integer dataStatus) {
 		this.dataStatus = dataStatus;
 	}
 
 	@JsonIgnore
 	public Boolean getIsDefault() {
 	    return isDefault;
     }
 
 	public void setIsDefault(Boolean isDefault) {
 	    this.isDefault = isDefault;
     }
 	
 	@JsonIgnore
 	public String getServiceTypes() {
     	return serviceTypes;
     }
 
 	public void setServiceTypes(String serviceTypes) {
     	this.serviceTypes = serviceTypes;
     }
 
 	/**
 	 * Pass in a quantityUnit value and this will tell you whether there is 
 	 * 1 or more readings for it.
 	 * 
 	 * @param quantityUnit
 	 * @return true if has readings, false otherwise
 	 */
 	public boolean hasReadingsFor(String quantityUnit) {
 		
 		if(quantityUnit == null || quantityUnit.isEmpty()) return false;
 		if(this.readingDataSummaries == null) return false;
 			
 		for(ReadingDataSummary summary : this.getReadingDataSummaries()) {
 			if(quantityUnit.equals(summary.getQuantityUnit()) 
 					&& summary.getNumberOfReadings() != null
 					&& summary.getNumberOfReadings() > 0) 
 				return true;
 		}
 		return false;
 		
 	} // end of hasReadingsFor
 	
 	/**
 	 * Implementation of the compareTo.
 	 * 
 	 * @param anotherClass
 	 * @return
 	 * @throws ClassCastException
 	 */
 	public int compareTo(Object anotherClass) throws ClassCastException {
 
 		if (!(anotherClass instanceof Profile)) {
 			throw new ClassCastException("Object is not Profile expected.");
 		}
 		return this.profileId.compareTo(((Profile) anotherClass).getProfileId());
 
 	}
 	
 	
 	/**
 	 * Helper method to make a copy of the profile. Note it doesn't
 	 * make a copy of the readings or reading summaries.
 	 * 
 	 * @param anotherProfile to copy data into
 	 * @return void
 	 */
 	public void copy(Profile toCopyToProfile) {
 		
 		toCopyToProfile.setProfileId(this.profileId);
 		toCopyToProfile.setProfileName(this.profileName);
 		toCopyToProfile.setAccountId(this.accountId);
 		toCopyToProfile.setProviderOrgId(this.providerOrgId);
 		toCopyToProfile.setDescription(this.description);
 		toCopyToProfile.setSourceId(this.sourceId );
 		toCopyToProfile.setIsDefault(this.isDefault);
 	}
 
 } // end of class Profile
 
