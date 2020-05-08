 package org.iucn.sis.shared.api.models.fields;
 
 import java.util.Date;
 
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 
 public class RedListCriteriaField extends ProxyField {
 	
 	public static final String IS_MANUAL_KEY = "isManual";
 	public static final String CRIT_VERSION_KEY = "critVersion";
 	public static final String MANUAL_CATEGORY_KEY = "manualCategory";
 	public static final String MANUAL_CRITERIA_KEY = "manualCriteria";
 	public static final String GENERATED_CATEGORY_KEY = "autoCategory";
 	public static final String GENERATED_CRITERIA_KEY = "autoCriteria";
 	public static final String RLHISTORY_TEXT_KEY = "rlHistoryText";
 	public static final String POSSIBLY_EXTINCT_KEY = "possiblyExtinct";
 	public static final String POSSIBLY_EXTINCT_CANDIDATE_KEY = "possiblyExtinctCandidate";
 	public static final String DATE_LAST_SEEN_KEY = "dateLastSeen";
 	public static final String CATEGORY_TEXT_KEY = "categoryText";
 	public static final String DATA_DEFICIENT_KEY = "dataDeficientReason";
 	
 	public static String CANONICAL_NAME = CanonicalNames.RedListCriteria;
 	public static String RL_HISTORY_TEXT = "rlHistoryText";
 	
 	public RedListCriteriaField(Field proxy) {
 		super(proxy == null ? new Field() : proxy);
 	}
 	
 	public void setManual(Boolean isManual) {
 		/*
 		 * TODO: if manual, do we remove auto-gen data? also, 
 		 * conversely, if not manual, do we remove all manual 
 		 * data? 
 		 */
 		setBooleanPrimitiveField(IS_MANUAL_KEY, isManual, null);
 	}
 	
 	/*
 	 * This is false by default
 	 */
 	public boolean isManual() {
 		return getBooleanPrimitiveField(IS_MANUAL_KEY, false);
 	}
 	
 	public void setCriteriaVersion(Integer version) {
		setForeignKeyPrimitiveField(CRIT_VERSION_KEY, version);
 	}
 	
 	public Integer getCriteriaVersion() {
 		Integer value = getForeignKeyPrimitiveField(CRIT_VERSION_KEY);
 		if (value == null)
 			value = 0;
 		return value;
 	}
 	
 	public void setManualCategory(String value) {
 		setStringPrimitiveField(MANUAL_CATEGORY_KEY, value);
 	}
 	
 	public String getManualCategory() {
 		return getStringPrimitiveField(MANUAL_CATEGORY_KEY);
 	}
 	
 	public void setManualCriteria(String value) {
 		setStringPrimitiveField(MANUAL_CRITERIA_KEY, value);
 	}
 	
 	public String getManualCriteria() {
 		return getStringPrimitiveField(MANUAL_CRITERIA_KEY);
 	}
 	
 	public void setGeneratedCategory(String value) {
 		setStringPrimitiveField(GENERATED_CATEGORY_KEY, value);
 	}
 	
 	public String getGeneratedCategory() {
 		return getStringPrimitiveField(GENERATED_CATEGORY_KEY);
 	}
 	
 	public void setGeneratedCriteria(String value) {
 		setStringPrimitiveField(GENERATED_CRITERIA_KEY, value);
 	}
 	
 	public String getGeneratedCriteria() {
 		return getStringPrimitiveField(GENERATED_CRITERIA_KEY);
 	}
 	
 	public void setRLHistoryText(String value) {
 		setStringPrimitiveField(RLHISTORY_TEXT_KEY, value);
 	}
 	
 	public String getRLHistoryText() {
 		return getStringPrimitiveField(RLHISTORY_TEXT_KEY);
 	}
 	
 	public void setPossiblyExtinct(Boolean value) {
 		setBooleanPrimitiveField(POSSIBLY_EXTINCT_KEY, value, false);
 	}
 	
 	public Boolean isPossiblyExtinct() {
 		return getBooleanPrimitiveField(POSSIBLY_EXTINCT_KEY, false);
 	}
 	
 	public void setPossiblyExtinctCandidate(Boolean value) {
 		setBooleanPrimitiveField(POSSIBLY_EXTINCT_CANDIDATE_KEY, value, false);
 	}
 	
 	public Boolean isPossiblyExtinctCandidate() {
 		return getBooleanPrimitiveField(POSSIBLY_EXTINCT_CANDIDATE_KEY, false);
 	}
 	
 	public void setDateLastSeen(Date value) {
 		setDatePrimitiveField(DATE_LAST_SEEN_KEY, value);
 	}
 	
 	public Date getDateLastSeen() {
 		return getDatePrimitiveField(DATE_LAST_SEEN_KEY);
 	}
 	
 	public void setCategoryText(String value) {
 		setStringPrimitiveField(CATEGORY_TEXT_KEY, value);
 	}
 	
 	public String getCategoryText() {
 		return getStringPrimitiveField(CATEGORY_TEXT_KEY);
 	}
 	
 	public void setDataDeficient(String value) {
 		setStringPrimitiveField(DATA_DEFICIENT_KEY, value);
 	}
 	
 	public String getDataDeficient() {
 		return getStringPrimitiveField(DATA_DEFICIENT_KEY);
 	}
 
 }
