 package org.iucn.sis.shared.api.models;
 
 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: 
  * License Type: Evaluation
  */
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.models.fields.RegionField;
 import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeElementCollection;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.lwxml.shared.utils.ArrayUtils;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 public class Assessment implements Serializable, AuthorizableObject, Referenceable {
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	public static final String ROOT_TAG = "assessment";
 	public static final int DELETED = -1;
 	public static final int ACTIVE = 0;
 	
 	protected boolean isHistorical;
 	protected int state;
 	
 	public int getState() {
 		return state;
 	}
 	public void setState(int state) {
 		this.state = state;
 	}
 	
 	public List<Integer> getRegionIDs() {
 		Field regionField = getField(CanonicalNames.RegionInformation);
 		List<Integer> regionIds = (List<Integer>) regionField.getKeyToPrimitiveFields().get(RegionField.PRIMITIVE_FIELD).getValue();
 		return regionIds;
 	}
 	
 	public void setId(int value) {
 		this.id = value;
 	}
 	
 	@Override
 	public int hashCode() {
 		return getId();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if( obj instanceof Assessment )
 			return ((Assessment) obj).getId() == getId();
 		else
 			return super.equals(obj);
 	}
 	
 	public String getCategoryAbbreviation() {
 		if( (Boolean)getPrimitiveValue(CanonicalNames.RedListCriteria, "isManual") )
 			return (String)getPrimitiveValue(CanonicalNames.RedListCriteria, "manualCategory");
 		else
 			return (String)getPrimitiveValue(CanonicalNames.RedListCriteria, "generatedCategory");
 	}
 	
 	public Set<Reference> getReferences(String canonicalName) {
 		Field field = getField(canonicalName);
 		return field == null ? null : getField(canonicalName).getReference();
 	}
 	
 	public boolean removeReference(Reference ref, String canonicalName) {
 		Field field = getField(canonicalName);
 		return field == null ? false : getField(canonicalName).getReference().remove(ref);
 	}
 	
 	/**
 	 * Checks the individual regions to see if there is an overlap.
 	 * @param other assessment to compare with
 	 * @return Integer representing the conflicting region, null if no conflict
 	 */
 	public Integer isRegionConflict(Assessment other) {
 		List<Integer> otherRegions = other.getRegionIDs();
 		for( Integer cur : getRegionIDs() )
 			if( otherRegions.contains(cur) )
 				return cur;
 		
 		return null;
 	}
 	
 	public static Assessment fromXML(NativeElement element) {
 		Assessment assessment = new Assessment();
 		assessment.setId(Integer.valueOf(element.getAttribute("id")));
 		assessment.setInternalId(element.getAttribute("internalID"));
 		assessment.setSource(element.getElementsByTagName("source").elementAt(0).getTextContent());
 		assessment.setSourceDate(element.getElementsByTagName("sourceDate").elementAt(0).getTextContent());
 
 		NativeNodeList taxonEl = element.getElementsByTagName(Taxon.ROOT_TAG);
 		assessment.setTaxon(Taxon.fromXMLminimal(taxonEl.elementAt(0)));
 		
 		assessment.setAssessmentType(AssessmentType.fromXML(element.getElementsByTagName(AssessmentType.ROOT_TAG).elementAt(0)));
 		
 		assessment.setEdit(new HashSet<Edit>());
 		NativeElementCollection edits = new NativeElementCollection(element.getElementsByTagName(Edit.ROOT_TAG));
 		for (NativeElement edit : edits) {
 			Edit cur = Edit.fromXML(edit);
 			if( cur.getAssessment() == null )
 				cur.setAssessment(new HashSet<Assessment>());
 			
 			cur.getAssessment().add(assessment);
 			assessment.getEdit().add(cur);
 		}
 		
 		assessment.setReference(new HashSet<Reference>());
 		NativeElementCollection references = new NativeElementCollection(element.getElementsByTagName(Reference.ROOT_TAG));
 		for (NativeElement reference : references) {
 			Reference cur = Reference.fromXML(reference);
 			if( cur.getAssessment() == null )
 				cur.setAssessment(new HashSet<Assessment>());
 			
 			cur.getAssessment().add(assessment);
 			assessment.getReference().add(cur);
 		}
 		
 		assessment.setField(new HashSet<Field>());
 		NativeElementCollection fields = new NativeElementCollection(element.getElementsByTagName(Field.ROOT_TAG));
 		for (NativeElement field : fields) {
 			Field cur = Field.fromXML(field);
 			
 			cur.setAssessment(assessment);
 			assessment.getField().add(cur);
 		}
 		
 		return assessment;
 		
 	}
 	
 	public static Assessment fromXML(NativeDocument ndoc) {
 		return fromXML(ndoc.getDocumentElement());
 	}
 	
 	@Override
 	public void addReferences(ArrayList<Reference> references,
 			GenericCallback<Object> callback) {
 		getReferences().addAll(references);
 		callback.onSuccess(this);
 	}
 	
 	@Override
 	public Set<Reference> getReferencesAsList() {
 		return getReferences();
 	}
 	
 	@Override
 	public void onReferenceChanged(GenericCallback<Object> callback) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void removeReferences(ArrayList<Reference> references,
 			GenericCallback<Object> callback) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public String getFullURI() {
 		return "resource/assessment/" + getAssessmentType().getName();
 	}
 
 	@Override
 	public String getProperty(String key) {
 		if ("region".equalsIgnoreCase(key))
 			return isGlobal() ? "global" : serializeRegionIDs(getRegionIDs());
 		else
 			return "";
 	}
 
 	
 	private String serializeRegionIDs(List<Integer> regionIDs) {
         ArrayUtils.insertionSort(regionIDs, new PortableAlphanumericComparator());
         StringBuilder str = new StringBuilder();
         for( Integer curID : regionIDs )
             str.append("," + curID);
        
         return str.substring(1);
     }
 	
 	public boolean setDateAssessed(Date dateAssessed) {
 		return setPrimitiveValue(dateAssessed, CanonicalNames.RedListAssessmentDate, "value", PrimitiveFieldFactory.DATE_PRIMITIVE);
 	}
 	
 	public Date getDateAssessed() {
 		return (Date)getPrimitiveValue(CanonicalNames.RedListAssessmentDate, "value");
 	}
 	
 	public boolean setPrimitiveValue(Object value, String canonicalName, String primitiveName, String primitiveType) {
 		boolean ret = false;
 		
 		Field field = getField(canonicalName);
 		if( field == null ) {
 			field = new Field(canonicalName, this);
 			field.setPrimitiveField(new HashSet<PrimitiveField>());
 		}
 
 		PrimitiveField prims = field.getKeyToPrimitiveFields().get(primitiveName);
 		if( prims != null ) {
 			prims.setValue(value);
 			ret = true;
 		} else {
 			PrimitiveField prim = PrimitiveFieldFactory.generatePrimitiveField(PrimitiveFieldFactory.DATE_PRIMITIVE);
 			prim.setField(field);
 			prim.setName(primitiveName);
 			
 			if( prim != null ) {
 				prim.setValue(value);
 				field.getPrimitiveField().add(prim);
 				ret = true;
 			}
 		}
 	
 		return ret;
 	}
 	
 	public Object getPrimitiveValue(String canonicalName, String primitiveName) {
 		PrimitiveField field = getPrimitiveField(canonicalName, primitiveName);
 		if (field != null)
 			return field.getValue();
 		return null;
 	}
 	
 	public PrimitiveField getPrimitiveField(String canonicalName, String primitiveName) {
 		Field field = getField(canonicalName);
 		if( field != null ) {
 			return field.getKeyToPrimitiveFields().get(primitiveName);
 		}		
 		return null;
 	}
 
 	public boolean isGlobal() {
 		return getRegionIDs().contains(Region.GLOBAL_ID);
 	}
 
 	public boolean isEndemic() {
 		PrimitiveField pf = getField(CanonicalNames.RegionInformation).getKeyToPrimitiveFields().get("endemic");
 		if (pf != null)
 			return (Boolean) pf.getValue();
 		return false;
 	}
 
 	public boolean isRegional() {
 		return !isGlobal();
 	}
 
 	
 	public boolean getIsHistorical() {
 		return isHistorical;
 	}
 	
 	public void setIsHistorical(Boolean historical) {
 		isHistorical = historical;
 	}
 
 	
 	protected String dateFinalized;
 	public String getDateFinalized() {
 		return dateFinalized;
 	}
 	public void setDateFinalized(String date) {
 		this.dateFinalized = date;
 	}
 
 	public boolean isPublished() {
 		return getAssessmentType().getId() == AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID;
 	}
 
 	public boolean isDraft() {
 		return getAssessmentType().getId() == AssessmentType.DRAFT_ASSESSMENT_STATUS_ID;
 	}
 
 	public String toXML() {
 		StringBuilder xml = new StringBuilder();
 		xml.append("<" + ROOT_TAG + " id=\"" + getId() + "\" internalID=\"" + getInternalId() + "\">");
 		xml.append("<source><![CDATA[" + getSource() + "]]></source>");
 		xml.append("<sourceDate><![CDATA[" + getSourceDate() + "]]></sourceDate>");
 		xml.append(getTaxon().toXMLMinimal());
 		xml.append(getAssessmentType().toXML());
 		
 		if (getEdit() != null) {
 			for (Edit edit : getEdit())
 				xml.append(edit.toXML());
 		}
 		
 		if (getReference() != null) {
 			for (Reference edit : getReference())
 				xml.append(edit.toXML());
 		}
 		
 		if (getField() != null) {
 			for (Field field : getField())
 				xml.append(field.toXML());
 		}
 		
 		xml.append("</" + ROOT_TAG + ">");
 		return xml.toString();		
 		
 	}
 	
 	public int getSpeciesID() {
 		return getTaxon().getId();
 	}
 
 	public String getType() {
 		return getAssessmentType().getName();
 	}
 
 	public Assessment deepCopy() {
 		
 		Assessment assessment = new Assessment();
 		assessment.setAssessmentType(getAssessmentType());
 		assessment.setDateFinalized(getDateFinalized());
 		assessment.setSource(getSource());
 		assessment.setSourceDate(getSourceDate());
 		assessment.setState(getState());
 		assessment.setTaxon(getTaxon());
 		
 		assessment.setField(new HashSet<Field>());
 		for (Field field : getField())
 			assessment.getField().add(field.deepCopy(false));
 		
 		assessment.setReference(new HashSet<Reference>());
 		for (Reference ref : getReference())
 			assessment.getReference().add(ref.deepCopy());	
 		
 		return assessment;
 
 	}
 
 //	public String getAssessmentID() {
 //		return getInternalId();
 //	}
 	
 	public void clearReferences() {
 		getReferences().clear();
 		
 		for( Field cur : getField() )
 			cur.getReference().clear();
 	}
 	
 	public void setRegions(Collection<Region> regions) {
 		setRegions(regions, false);
 	}
 	
 	public void setRegions(Collection<Region> regions, boolean endemic) {
 		final List<Integer> regionIDs = new ArrayList<Integer>();
 		for (Region region : regions)
 			regionIDs.add(region.getId());
 		
 		setField(new RegionField(endemic, regionIDs, this));
 	}
 	
 	public void setField(Field field) {
		final Field existing = getField(field.getName());
 		this.field.remove(existing);
 		
 		this.field.add(field);
 		this.keyToField.put(field.getName(), field);
 	}
 
 	public void setType(String type) {
 		setAssessmentType(AssessmentType.getAssessmentType(type));
 	}
 
 	public String getSpeciesName() {
 		return getTaxon().getFriendlyName();
 	}	
 	
 	public Set<Reference> getReferences() {
 		
 		return getReference();
 	}
 	
 	public String getUID() {
 		return getInternalId() + "_" + getAssessmentType().getName();
 	}
 	
 	protected Edit lastEdit;
 	public Edit getLastEdit() {
 		if (lastEdit == null) {
 			if (getEdit().size() > 1) {
 				List<Edit> edits = new ArrayList<Edit>();
 				for (Edit edit : getEdit()) {
 					edits.add(edit);
 				}
 				Collections.sort(edits);
 				lastEdit = edits.get(0);
 			} else if (getEdit().size() == 1) {
 				lastEdit = getEdit().iterator().next();
 			}			
 		}
 		return lastEdit;
 		
 	}
 	
 	public long getDateModified() {
 		long dateModified = 0;
 		Edit edit = getLastEdit();
 		if (edit != null) {
 			dateModified = edit.getCreatedDate().getTime();
 		}
 		return dateModified;
 	}
 	
 	public String getCategoryFuzzyResult() {
 		//FIXME
 		return null;
 	}
 	
 	public String getCategoryCriteria() {
 		//FIXME
 		return null;
 	}
 	
 	public String getCrCriteria() {
 		//FIXME
 		return null;
 	}
 	
 	public String getEnCriteria() {
 		//FIXME
 		return null;
 	}
 	
 	public String getVuCriteria() {
 		//FIXME
 		return null;
 	}
 	
 	private Map<String, Field> keyToField;
 	private void generateFields() {
 		keyToField = new HashMap<String, Field>();
 		for (Field field : getField()) {
 			keyToField.put(field.getName(), field);
 		}
 	}
 	
 	public Field getField(String fieldName) {
 		if (keyToField == null || keyToField.size() != getField().size()) {
 			generateFields();
 		}
 		return keyToField.get(fieldName);		
 	}
 
 	public boolean addReference(Reference ref, String fieldName) {
 		Field field = getField(fieldName);
 		if (field != null) {
 			field.getReference().add(ref);
 			return true;
 		}
 		return false;
 	}
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 
 	public Assessment() {
 		state = ACTIVE;
 	}
 
 	private int id;
 
 	private AssessmentType assessment_type;
 
 	private String source;
 
 	private String sourceDate;
 
 	private Taxon taxon;
 
 	private String internalId;
 
 	private java.util.Set<Edit> edit = new java.util.HashSet<Edit>();
 
 	private java.util.Set<Reference> reference = new java.util.HashSet<Reference>();
 
 	private java.util.Set<Field> field = new java.util.HashSet<Field>();
 
 	
 
 	public int getId() {
 		return id;
 	}
 
 	public int getORMID() {
 		return getId();
 	}
 
 	public void setSource(String value) {
 		this.source = value;
 	}
 
 	public String getSource() {
 		return source;
 	}
 
 	public void setSourceDate(String value) {
 		this.sourceDate = value;
 	}
 
 	public String getSourceDate() {
 		return sourceDate;
 	}
 
 	public void setInternalId(String value) {
 		this.internalId = value;
 	}
 
 	public String getInternalId() {
 		return internalId;
 	}
 
 	public void setAssessmentType(AssessmentType value) {
 		this.assessment_type = value;
 	}
 
 	public AssessmentType getAssessmentType() {
 		return assessment_type;
 	}
 
 	public void setTaxon(Taxon value) {
 		this.taxon = value;
 	}
 
 	public Taxon getTaxon() {
 		return taxon;
 	}
 
 	public void setEdit(java.util.Set<Edit> value) {
 		this.edit = value;
 	}
 
 	public java.util.Set<Edit> getEdit() {
 		return edit;
 	}
 
 	public void setReference(java.util.Set<Reference> value) {
 		this.reference = value;
 	}
 
 	public java.util.Set<Reference> getReference() {
 		return reference;
 	}
 
 	public void setField(java.util.Set<Field> value) {
 		this.field = value;
 	}
 
 	public java.util.Set<Field> getField() {
 		return field;
 	}
 
 	public String toString() {
 		return String.valueOf(getId());
 	}
 	
 	public void setCategoryCriteria(String criteriaString) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setCrCriteria(String criteriaStringCR) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setCategoryFuzzyResult(String string) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setCategoryAbbreviation(String abbreviatedCategory) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 
 }
