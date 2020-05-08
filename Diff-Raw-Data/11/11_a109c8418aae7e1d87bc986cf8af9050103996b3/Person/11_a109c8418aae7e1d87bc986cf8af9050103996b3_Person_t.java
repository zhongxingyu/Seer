 package com.orangeleap.tangerine.domain;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.orangeleap.tangerine.domain.communication.AbstractCommunicatorEntity;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.util.StringConstants;
 
 public class Person extends AbstractCommunicatorEntity {
 
     private static final long serialVersionUID = 1L;
 
     public static final String INDIVIDUAL = "individual";
     public static final String ORGANIZATION = "organization";
     public static final String FORMAL_SALUTATION = "formalSalutation";
     public static final String INFORMAL_SALUTATION = "informalSalutation";
     public static final String HEAD_OF_HOUSEHOLD_SALUTATION = "headOfHouseholdSalutation";
     public static final String ORGANIZATION_ELIGIBILITY = "organization.eligibility";
     public static final String DONOR_PROFILES = "donorProfiles";
     public static final String ORGANIZATION_MINIMUM_GIFT_MATCH = "organization.minimumGiftMatch";
     public static final String ORGANIZATION_MAXIMUM_GIFT_MATCH = "organization.maximumGiftMatch";
 
     private Site site;
     private String constituentType = INDIVIDUAL;
     private String title;
     private String firstName;
     private String middleName;
     private String lastName;
     private String suffix;
     private String recognitionName;
     private String organizationName;
     private String legalName;
     private String ncaisCode;
     private String maritalStatus = "Unknown";
     private String preferredPhoneType;
     private String loginId;
     private String accountNumber;
     
     private List<Gift> gifts;
     private List<RecurringGift> recurringGifts;
     private List<Pledge> pledges;
     
     public Person() { }
 
     public Person(Long id, Site site) { 
         this();
         this.id = id;
         this.site = site;
     }
     
     @Override
     public String toString() {
         return getDisplayValue();
     }
 
     @Override
     // TODO: remove this overridden method when this class is renamed to "Constituent"
     public String getType() {
         return "person";
     }
 
     public String getDisplayValue() {
         if (isOrganization()) {
             return organizationName;
         } else {
             return createName(true);
         }
     }
 
     public String getFirstLast() {
         StringBuilder sb = new StringBuilder();
         if (isOrganization()) {
             sb.append(organizationName);
         } 
         else {
             if (firstName != null) {
                 sb.append(firstName).append(" ");
             }
             if (lastName != null) {
                 sb.append(lastName);
             }
         }
         return sb.toString();
     }
 
     public String getFullName() {
         if (isOrganization()) {
             return organizationName;
         } 
         else {
             return createName(false);
         }
     }
 
     public String createName(boolean lastFirst) {
         StringBuilder sb = new StringBuilder();
         if (lastFirst) {
             sb.append(lastName == null ? "" : lastName).append(", ");
         }
         sb.append(firstName == null ? "" : firstName);
         if (middleName != null && middleName.length() > 0) {
             sb.append(" ").append(middleName);
         }
         if (!lastFirst) {
             sb.append(" ").append(lastName == null ? "" : lastName);
         }
         if (suffix != null && suffix.length() > 0) {
             sb.append(", ").append(suffix);
         }
         return sb.toString();
     }
 
     public String getEntityName() {
         return "person";
     }
 
     public Site getSite() {
         return site;
     }
 
     public void setSite(Site site) {
         this.site = site;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getMiddleName() {
         return middleName;
     }
 
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getPreferredPhoneType() {
         return preferredPhoneType;
     }
 
     public void setPreferredPhoneType(String preferredPhoneType) {
         this.preferredPhoneType = preferredPhoneType;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getSuffix() {
         return suffix;
     }
 
     public void setSuffix(String suffix) {
         this.suffix = suffix;
     }
 
     public String getMaritalStatus() {
         return maritalStatus;
     }
 
     public void setMaritalStatus(String maritalStatus) {
         this.maritalStatus = maritalStatus;
     }
 
     public String getOrganizationName() {
         return organizationName;
     }
 
     public void setOrganizationName(String organizationName) {
         this.organizationName = organizationName;
     }
 
     public List<Gift> getGifts() {
         return gifts;
     }
 
     public void setGifts(List<Gift> gifts) {
         this.gifts = gifts;
     }
 
     public List<RecurringGift> getRecurringGifts() {
         return recurringGifts;
     }
 
     public void setRecurringGifts(List<RecurringGift> recurringGifts) {
         this.recurringGifts = recurringGifts;
     }
 
     public List<Pledge> getPledges() {
         return pledges;
     }
 
     public void setPledges(List<Pledge> pledges) {
         this.pledges = pledges;
     }
 
     public boolean isMajorDonor() {
         return isSpecificDonor("majorDonor");
     }
 
     public boolean isLapsedDonor() {
         return isSpecificDonor("lapsedDonor");
     }
 
     private boolean isSpecificDonor(String donorName) {
         boolean isSpecificDonor = false;
         String donorProfiles = getCustomFieldValue(DONOR_PROFILES);
         if (donorProfiles != null && donorProfiles.indexOf(donorName) > -1) {
             isSpecificDonor = true;
         }
         return isSpecificDonor;
     }
 
     public void setNcaisCode(String ncaisCode) {
         this.ncaisCode = ncaisCode;
     }
 
     public String getNcaisCode() {
         return ncaisCode;
     }
 
     public void setRecognitionName(String recognitionName) {
         this.recognitionName = recognitionName;
     }
 
     public String getRecognitionName() {
         return recognitionName;
     }
 
     public void setConstituentType(String constituentType) {
         this.constituentType = constituentType;
     }
 
     public String getConstituentType() {
         if (constituentType == null) {
             return INDIVIDUAL;
         }
         return constituentType;
     }
 
     public void setConstituentIndividualRoles(String constituentIndividualRoles) {
     	setCustomFieldValue("constituentIndividualRoles", constituentIndividualRoles);
     }
 
     public String getConstituentIndividualRoles() {
         String result = getCustomFieldValue("constituentIndividualRoles");
         if (result == null) return ""; else return result;
     }
 
     public void setConstituentOrganizationRoles(String constituentOrganizationRoles) {
     	setCustomFieldValue("constituentOrganizationRoles", constituentOrganizationRoles);
     }
 
     public String getConstituentOrganizationRoles() {
     	String result = getCustomFieldValue("constituentOrganizationRoles");
         if (result == null) return ""; else return result;
     }
 
     public void removeConstituentIndividualRoles(String role) {
    	String existingValue = getConstituentIndividualRoles();
     	
     	//
     	// if the custom field value does not contain the value
     	// we are removing simply return
     	if (existingValue.contains(role) == false) {
             return;
         }
     	
     	//
     	// if the existing value is equal to the value we are removing
     	// then set the field value to an empty string (remove it)
     	if (existingValue.contains(",") == false) {
     		if (existingValue.compareTo(role) == 0) {
                 setConstituentIndividualRoles("");
             }
     	} else {
     		//
     		// if the existing value is a comma separated list of values
     		// then find the value we are removing in the string and remove it
     		// then reset the field value
     		String[] values = existingValue.split(",");
     		
     		StringBuilder sb = new StringBuilder();
     		for (int i = 0; i < values.length; i++) {
     			if (values[i].equals(role) == false) {
                     sb.append(values[i]);
                 }
     			
     			if (i != (values.length - 1)) {
                     sb.append(",");
                 }
     		}
     		
     		setConstituentIndividualRoles(sb.toString());
     	}
     }
     
    public void addConstituentOrganizationRoles(String role) {
    	String existingValue = getConstituentOrganizationRoles();
        if (existingValue == null) {
            setConstituentOrganizationRoles(role);
        }
        else {
            setConstituentOrganizationRoles(existingValue + "," + role); 
        }
    	
    }
     public void addConstituentIndividualRoles(String role) {
     	String existingValue = getConstituentIndividualRoles();
         if (existingValue == null) {
             setConstituentIndividualRoles(role);
         }
         else {
             setConstituentIndividualRoles(existingValue + "," + role); 
         }
     }
     
     public void addConstituentRole(String constituentRole) {
         if (constituentRole == null || constituentRole.equals(ORGANIZATION) || constituentRole.equals(INDIVIDUAL)) {
             return;
         }
         if (isOrganization()) {
             setConstituentOrganizationRoles(addToList(getConstituentOrganizationRoles(), constituentRole));
         } 
         else {
             setConstituentIndividualRoles(addToList(getConstituentIndividualRoles(), constituentRole));
         }
     }
 
     private String addToList(String list, String s) {
         if (list == null) {
             list = "";
         }
         if (!list.contains(s)) {
             if (list.length() > 0) {
                 list += ",";
             }
             list += s;
         }
         return list;
     }
 
     public void setConstituentAttributes(String constituentAttributes) {
         // noop - read only field for Spring MVC
     }
 
     public String getConstituentAttributes() {
         String constituentAttributes;
         if (isOrganization()) {
             constituentAttributes = ORGANIZATION;
             String constituentOrganizationRoles = this.getConstituentOrganizationRoles();
             if (constituentOrganizationRoles != null && constituentOrganizationRoles.length() > 0) {
                 constituentAttributes = constituentAttributes + "," + constituentOrganizationRoles;
             }
         } 
         else {
             constituentAttributes = INDIVIDUAL;
             String constituentIndividualRoles = this.getConstituentIndividualRoles();
             if (constituentIndividualRoles != null && constituentIndividualRoles.length() > 0) {
                 constituentAttributes = constituentAttributes + "," + constituentIndividualRoles;
             }
         }
         return constituentAttributes;
     }
 
     public void setLegalName(String legalName) {
         this.legalName = legalName;
     }
 
     public String getLegalName() {
         return legalName;
     }
 
     public void setLoginId(String loginId) {
         this.loginId = loginId;
     }
 
     public String getLoginId() {
         return loginId;
     }
 
     public boolean isOrganization() {
         return ORGANIZATION.equals(getConstituentType());
     }
 
     public boolean isIndividual() {
         return INDIVIDUAL.equals(getConstituentType());
     }
 
     public String getAccountNumber() {
         return accountNumber;
     }
 
     public void setAccountNumber(String accountNumber) {
         this.accountNumber = accountNumber;
     }
     
     @Override
     public String getAuditShortDesc() {
     	return getFullName();
     }
 
 
     @Override
     public void setDefaults() {
         super.setDefaults();
         if (isOrganization()) {
             setDefaultCustomFieldValue(ORGANIZATION_ELIGIBILITY, StringConstants.UNKNOWN_LOWER_CASE);
         }
     }
 
     @Override
     public void prePersist() {
         super.prePersist();
         if (isOrganization() && StringUtils.isBlank(getLegalName())) {
             setLegalName(getOrganizationName());
         }
         if (StringUtils.isBlank(getRecognitionName())) {
             if (isIndividual()) { 
                 setRecognitionName(createName(false));
             }
             else if (isOrganization()) {
                 setRecognitionName(getOrganizationName());
             }
         }
         if (StringUtils.isBlank(getCustomFieldValue(FORMAL_SALUTATION))) {
             if (isOrganization()) {
                 setCustomFieldValue(FORMAL_SALUTATION, legalName);
             }
             else if (isIndividual()) {
                 StringBuilder sb = new StringBuilder();
                 if (StringUtils.isBlank(title) == false) {
                     sb.append(title).append(" ");
                 }
                 sb.append(getFirstLast());
                 setCustomFieldValue(FORMAL_SALUTATION, sb.toString());
             }
         }
         if (StringUtils.isBlank(getCustomFieldValue(INFORMAL_SALUTATION))) {
             if (isOrganization()) {
                 setCustomFieldValue(INFORMAL_SALUTATION, organizationName);
             }
             else if (isIndividual()) {
                 setCustomFieldValue(INFORMAL_SALUTATION, getFirstLast());
             }
         }
         if (isIndividual() && StringUtils.isBlank(getCustomFieldValue(HEAD_OF_HOUSEHOLD_SALUTATION))) {
             setCustomFieldValue(HEAD_OF_HOUSEHOLD_SALUTATION, getFirstLast());
         }
         setConstituentType(StringUtils.trimToEmpty(getConstituentType()).toLowerCase());
         setConstituentIndividualRoles(StringUtils.trimToEmpty(getConstituentIndividualRoles()).toLowerCase());
         setConstituentOrganizationRoles(StringUtils.trimToEmpty(getConstituentOrganizationRoles()).toLowerCase());
     }
 }
