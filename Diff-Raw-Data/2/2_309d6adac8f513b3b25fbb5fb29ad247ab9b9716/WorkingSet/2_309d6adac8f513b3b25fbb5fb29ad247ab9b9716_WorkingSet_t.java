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
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.workflow.WorkflowStatus;
 
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.portable.XMLWritingUtils;
 
 public class WorkingSet implements Serializable, AuthorizableObject {
 
 	private static final long serialVersionUID = 1L;
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	public static final String ROOT_TAG = "workingSet";
 
 	public List<Integer> getSpeciesIDs() {
 		List<Integer> ids = new ArrayList<Integer>();
 		for (Taxon taxon : this.taxon)
 			ids.add(taxon.getId());
 		return ids;
 	}
 
 	public void setId(int value) {
 		this.id = value;
 		this.generationID = value;
 	}
 
 	public String getFullURI() {
 		return "workingSet/" + getId();
 	}
 
 	public String getProperty(String key) {
 		if ("name".equalsIgnoreCase(key)) {
 			return getWorkingSetName();
 		} else
 			return "";
 	}
 
 	public List<Taxon> getSpecies() {
 		return new ArrayList<Taxon>(taxon);
 	}
 
 	// FIXME
 	public AssessmentFilter getFilter() {
 		AssessmentFilter filter = new AssessmentFilter();
 		filter.setAllPublished(getAssessmentTypes().contains(
 				AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_TYPE))
 				&& !isMostRecentPublished);
 		filter.setRecentPublished(getAssessmentTypes().contains(
 				AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_TYPE))
 				&& isMostRecentPublished);
 		filter.setDraft(getAssessmentTypes().contains(
 				AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_TYPE)));
 
 		if (getRegion().isEmpty() && getRelationship().getName().equals(Relationship.ALL))
 			filter.setAllRegions();
 		else if (!getRegion().isEmpty()) {
 			filter.getRegions().clear();
 			filter.getRegions().addAll(getRegion());
 		}
 		
 		filter.setRegionType(getRelationship().getName());
 
 		return filter;
 	}
 
 	
 	public void setFilter(AssessmentFilter filter) {
 		getRegion().clear();
 		getAssessmentTypes().clear();
 		isMostRecentPublished = new Boolean(false);
 		
 		if (filter.isAllRegions()) {
 			getRegion().clear();
 			setRelationship(Relationship.fromName(Relationship.ALL));
 		} else {
 			getRegion().addAll(filter.getRegions());
 			setRelationship(Relationship.fromName(filter.getRegionType()));
 		}
 		
 		if (filter.isRecentPublished()){
 			isMostRecentPublished = new Boolean(true);
 			getAssessmentTypes().add(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
 		} else if (filter.isAllPublished()){
 			getAssessmentTypes().add(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
 		}
 		
 		if (filter.isDraft())
 			getAssessmentTypes().add(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
 	}
 
 	public String getWorkingSetName() {
 		return getName();
 	}
 
 	public String getCreatorUsername() {
 		return getCreator().getUsername();
 	}
 
 	public String getWorkflowStatus() {
 		if (getWorkflow() == null)
 			return "draft";
 		return getWorkflow();
 	}
 
 	public String toXMLMinimal() {
 		return "<" + ROOT_TAG + " id=\"" + getId() + "\" date=\"" +
				getCreatedDate().getTime() + "\">" +
 				XMLWritingUtils.writeCDATATag("name", name) +
 				creator.toBasicXML("creator") + 
 				"</" + ROOT_TAG + ">";
 	}
 
 	public static WorkingSet fromXMLMinimal(NativeElement element) {
 		return fromXMLMinimal(new WorkingSet(), element);
 	}
 	
 	public static WorkingSet fromXMLMinimal(WorkingSet set, NativeElement element) {
 		NativeNodeList children = element.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			NativeNode node = children.item(i);
 			if ("creator".equals(node.getNodeName()))
 				set.setCreator(User.fromXML((NativeElement)node));
 			else if ("name".equals(node.getNodeName()))
 				set.setName(node.getTextContent());
 		}
 		
 		set.setId(Integer.valueOf(element.getAttribute("id")));
 		set.setCreatedDate(new Date(Long.valueOf(element.getAttribute("date"))));
 		
 		return set;
 	}
 
 	public static WorkingSet fromXML(NativeDocument ndoc) {
 		return fromXML(ndoc.getDocumentElement());
 	}
 
 	public static WorkingSet fromXML(NativeElement element) {
 		return fromXML(new WorkingSet(), element);
 	}
 	
 	public static WorkingSet fromXML(WorkingSet set, NativeElement element) {
 		fromXMLMinimal(set, element);
 		
 		final Set<Region> newRegions = new HashSet<Region>();
 		
 		final NativeNodeList nodes = element.getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			final NativeNode node = nodes.item(i);
 			final String nodeName = node.getNodeName(); 
 			
 			if ("workflow".equals(nodeName))
 				set.setWorkflow(node.getTextContent());		
 			else if ("description".equals(nodeName))
 				set.setDescription(node.getTextContent());
 			else if ("notes".equals(nodeName))
 				set.setNotes(node.getTextContent());
 			else if (Relationship.ROOT_TAG.equals(nodeName))
 				set.setRelationship(Relationship.fromXML((NativeElement)node));
 			else if ("mostRecentPublished".equals(nodeName))
 				set.setIsMostRecentPublished(Boolean.valueOf(node.getTextContent()));
 			else if (Region.ROOT_TAG.equals(nodeName))
 				newRegions.add(Region.fromXML((NativeElement)node));
 			else if (Taxon.ROOT_TAG.equals(nodeName))
 				set.getTaxon().add(Taxon.fromXMLminimal((NativeElement)node));
 			else if (User.ROOT_TAG.equals(nodeName)) {
 				set.getUsers().add(User.fromXML((NativeElement)node));
 			}
 			else if (AssessmentType.ROOT_TAG.equals(nodeName))
 				set.getAssessmentTypes().add(AssessmentType.fromXML((NativeElement)node));	
 		}
 		
 		if (set.getRegion().isEmpty()) {
 			set.getRegion().addAll(newRegions);
 		}
 		else {
 			final List<Region> toRemove = new ArrayList<Region>();
 			for (Region region : set.getRegion())
 				if (!newRegions.contains(region))
 					toRemove.add(region);
 			
 			for (Region region : toRemove)
 				set.getRegion().remove(region);
 		}
 		
 		return set;
 	}
 
 	public String toXML() {
 		StringBuilder xml = new StringBuilder();
 		xml.append("<" + ROOT_TAG + " id=\"" + getId() + "\" date=\""
 				+ getCreatedDate().getTime() + "\">");
 		xml.append(XMLWritingUtils.writeCDATATag("name", getName()));
 		xml.append(creator.toBasicXML("creator"));
 		if (workflow == null)
 			workflow = WorkflowStatus.DRAFT.toString();
 		xml.append("<workflow>" + workflow + "</workflow>");
 		xml.append("<description><![CDATA[" + getDescription() + "]]></description>");
 		if (getNotes() == null)
 			xml.append("<notes></notes>");
 		else
 			xml.append("<notes><![CDATA[" + getNotes() + "]]></notes>");
 		xml.append(getRelationship().toXML());
 		xml.append("<mostRecentPublished>" + getIsMostRecentPublished().toString() + "</mostRecentPublished>");
 
 		for (Region region : getRegion())
 			xml.append(region.toXML());
 
 		for (Taxon taxon : getTaxon())
 			xml.append(taxon.toXMLMinimal());
 
 		for (User user : getUsers())
 			xml.append(user.toXML());
 
 		for (AssessmentType at : getAssessmentTypes())
 			xml.append(at.toXML());
 
 		xml.append("</" + ROOT_TAG + ">");
 		
 		return xml.toString();
 	}
 
 	
 	public String getSpeciesIDsAsString() {
 		StringBuffer id = new StringBuffer();
 		for (Taxon taxon : this.taxon)
 			id.append(taxon.getId() + ",");
 		if (id.indexOf(",") >= 0)
 			return id.substring(0, id.length() - 1);
 		else
 			return "";
 	}
 
 	public Boolean getIsMostRecentPublished() {
 		return isMostRecentPublished;
 	}
 
 	public void setIsMostRecentPublished(Boolean isMostRecentPublished) {
 		this.isMostRecentPublished = isMostRecentPublished;
 	}
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 
 	public WorkingSet() {
 		generationID = new Date().getTime();
 	}
 
 	private int id;
 
 	private Relationship relationship;
 
 	private String name;
 
 	private String description;
 	
 	private String notes;
 
 	private Date createdDate;
 
 	private User creator;
 
 	private String workflow;
 
 	private Boolean isMostRecentPublished;
 
 	private java.util.Set<Taxon> taxon = new java.util.HashSet<Taxon>();
 
 	private java.util.Set<Region> region = new java.util.HashSet<Region>();
 
 	private java.util.Set<Edit> edit = new java.util.HashSet<Edit>();
 
 	private java.util.Set<User> users = new java.util.HashSet<User>();
 
 	private java.util.Set<AssessmentType> assessmentTypes = new java.util.HashSet<AssessmentType>();
 	
 	private long generationID;
 
 	public int getId() {
 		return id;
 	}
 
 	public int getORMID() {
 		return getId();
 	}
 
 	public void setName(String value) {
 		this.name = value;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setDescription(String value) {
 		this.description = value;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setCreatedDate(Date value) {
 		this.createdDate = value;
 	}
 
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 
 	
 	public Set<AssessmentType> getAssessmentTypes() {
 		return assessmentTypes;
 	}
 	
 	public void setAssessmentTypes(java.util.Set<AssessmentType> assessmentTypes) {
 		this.assessmentTypes = assessmentTypes;
 	}
 
 	public void setRelationship(Relationship value) {
 		this.relationship = value;
 	}
 
 	public Relationship getRelationship() {
 		return relationship;
 	}
 
 	public void setCreator(User value) {
 		this.creator = value;
 	}
 
 	public User getCreator() {
 		return creator;
 	}
 
 	public void setWorkflow(String value) {
 		this.workflow = value;
 	}
 
 	public String getWorkflow() {
 		return workflow;
 	}
 
 	public void setTaxon(java.util.Set<Taxon> value) {
 		this.taxon = value;
 	}
 
 	public java.util.Set<Taxon> getTaxon() {
 		return taxon;
 	}
 	
 	public Map<Integer, Taxon> getTaxaMap() {
 		Map<Integer, Taxon> map = new HashMap<Integer, Taxon>();
 		for (Taxon taxon : getTaxon()) {
 			map.put(Integer.valueOf(taxon.getId()), taxon);
 		}
 		return map;
 	}
 
 	public void setRegion(java.util.Set<Region> value) {
 		this.region = value;
 	}
 
 	public java.util.Set<Region> getRegion() {
 		return region;
 	}
 
 	public void setEdit(java.util.Set<Edit> value) {
 		this.edit = value;
 	}
 
 	public java.util.Set<Edit> getEdit() {
 		return edit;
 	}
 
 	public void setUsers(java.util.Set<User> value) {
 		this.users = value;
 	}
 
 	public java.util.Set<User> getUsers() {
 		return users;
 	}
 
 	public String toString() {
 		return "Working set #" + id + ": " + name + " (" + generationID + ")"; 
 	}
 	
 	public String getNotes() {
 		return notes;
 	}
 	
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (generationID ^ (generationID >>> 32));
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
 		WorkingSet other = (WorkingSet) obj;
 		if (generationID != other.generationID)
 			return false;
 		return true;
 	}
 
 }
