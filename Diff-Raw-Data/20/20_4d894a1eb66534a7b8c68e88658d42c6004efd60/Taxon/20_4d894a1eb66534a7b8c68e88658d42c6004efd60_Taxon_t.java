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
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.data.LongUtils;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 
 public class Taxon implements AuthorizableObject, Serializable, Referenceable {
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	private static final long serialVersionUID = -3917537005947441129L;
 	public static final String ROOT_TAG = "taxon";
 	public static final int DELETED = -1;
 	public static final int ACTIVE = 0;
 
 	public int state;
 
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	@Override
 	public void addReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
 		getReference().addAll(references);
 		callback.onSuccess(getReference());
 	}
 
 	@Override
 	public Set<Reference> getReferencesAsList() {
 		return getReference();
 	}
 
 	@Override
 	public void onReferenceChanged(GenericCallback<Object> callback) {
 
 	}
 
 	@Override
 	public void removeReferences(ArrayList<Reference> references, GenericCallback<Object> callback) {
 		callback.onSuccess(Boolean.valueOf(getReference().removeAll(references)));
 	}
 
 	public String getXMLofFootprintAndChildren() {
 		StringBuilder xml = new StringBuilder("<hierarchy>");
 		xml.append("<footprint>" + getIDFootprintAsString(0, "-") + "</footprint>");
 		xml.append("<options>");
 		for (Taxon child : getChildren())
 			xml.append("<option>" + child.getId() + "</option>");
 		xml.append("</options>");
 		xml.append("</hierarchy>");
 		return xml.toString();
 	}
 
 	public void setId(int value) {
 		this.id = value;
 	}
 
 	public String getFullURI() {
 		return "resource/taxon/" + getTaxonLevel().getLevel() + "/" + getName();
 	}
 
 	public String getKingdomName() {
 		return getFootprint()[0];
 	}
 
 	@Override
 	public String getProperty(String key) {
 		return "";
 	}
 
 	public String getDisplayableLevel() {
 		Debug.println("this is the taxon level " + this.getTaxonLevel().getLevel() + " and id " + this.getTaxonLevel().getId());
 		if (this.getTaxonLevel().getLevel() == TaxonLevel.INFRARANK) {
 			if (getInfratype().getName() == Infratype.SUBSPECIES_NAME) {
 				return "Subspecies";
 			} else if (getInfratype().getName() == Infratype.VARIETY_NAME) {
 				return "Variety";
 			}
 		}
 		return TaxonLevel.displayableLevel[this.getTaxonLevel().getLevel()];
 	}
 
 	public void setParentId(int parentID) {
 		Taxon parent = new Taxon();
 		parent.setId(parentID);
 		setParent(parent);
 	}
 
 	public int getParentId() {
 		if (parent != null)
 			return parent.getId();
 		return 0;
 	}
 
 	public String getParentName() {
 		if (parent == null)
 			return null;
 		return parent.getFriendlyName();
 	}
 
 	public void correctFullName() {
 		String fullName = generateFullName();
 		this.friendlyName = fullName;
 	}
 
 	public String generateFullName() {
 
 		String fullName = null;
 		
 		if (getTaxonLevel().getLevel() <= TaxonLevel.GENUS)
 			fullName = name;
 		else {
 			fullName = getParent().getFriendlyName();
 			if (infratype != null) {
 				if (infratype.getName().equals(Infratype.SUBSPECIES_NAME))
 					fullName += " ssp.";
 				else if (infratype.getName().equals(Infratype.VARIETY_NAME))
 					fullName += " var.";
 				
 				name = name.replace("ssp.", "").trim();
 				name = name.replace("var.", "").trim();
 			}
 			fullName += " " + name;
 			
 		}
 		return fullName.trim();
 	}
 
 	private String[] footprint;
 	private Integer[] footprintID;
 
 	protected Edit lastEdit;
 
 	public Edit getLastEdit() {
 		if (lastEdit == null) {
 			if (getEdits().size() > 1) {
 				List<Edit> edits = new ArrayList<Edit>();
 				edits.addAll(getEdits());
 				Collections.sort(edits);
 				lastEdit = edits.get(0);
 			} else if (getEdits().size() == 1) {
 				lastEdit = getEdits().iterator().next();
 			}
 		}
 		return lastEdit;
 
 	}
 
 	public void setStatus(String status) {
 		setTaxonStatus(TaxonStatus.fromCode(status));
 	}
 
 	public void setFootprint(String[] footprint) {
 		this.footprint = footprint;
 	}
 
 	// FIXME
 	public Integer[] getIDFootprint() {
 		if (this.footprintID == null) {
 			ArrayList<Integer> nodes = new ArrayList<Integer>();
 			nodes.add(this.getId());
 			Taxon node = this;
 			while (node.getParent() != null) {
 				node = node.getParent();
 				nodes.add(node.getId());
 			}
 			Collections.reverse(nodes);
 			footprintID = nodes.toArray(new Integer[nodes.size()]);
 		}
 		return footprintID;
 	}
 
 	public String[] getFootprint() {
 		if (this.footprint == null) {
 			ArrayList<String> nodes = new ArrayList<String>();
 			nodes.add(this.getName());
 			Taxon node = this;
 			while (node.getParent() != null) {
 				node = node.getParent();
 				nodes.add(node.getName());
 			}
 			Collections.reverse(nodes);
 			footprint = nodes.toArray(new String[nodes.size()]);
 		}
 		return footprint;
 	}
 
 	public String getFootprintCSV() {
 		return getFootprintAsString(0, ",");
 	}
 
 	public String getFootprintAsString() {
 		return getFootprintAsString(0);
 	}
 
 	public String getFootprintAsString(int startIndex) {
 		return getFootprintAsString(startIndex, " ");
 	}
 
 	public String getFootprintAsString(int startIndex, String separator) {
 		getFootprint();
 		String ret = "";
 
 		if (footprint.length <= startIndex)
 			return ret;
 
 		for (int i = startIndex; i < footprint.length - 1; i++)
 			ret += footprint[i] + separator;
 		ret += footprint[footprint.length - 1];
 
 		return ret;
 	}
 
 	public String getIDFootprintAsString(int startIndex, String separator) {
 		getIDFootprint();
 		String ret = "";
 
 		if (footprintID.length <= startIndex)
 			return ret;
 
 		for (int i = startIndex; i < footprintID.length - 1; i++)
 			ret += footprintID[i] + separator;
 		ret += footprintID[footprintID.length - 1];
 
 		return ret;
 	}
 
 	public static String getDisplayableLevel(int level) {
 		return getDisplayableLevel(level, -1);
 	}
 
 	public static String getDisplayableLevel(int level, int infraType) {
 		if (level == TaxonLevel.INFRARANK) {
 			if (infraType == Infratype.INFRARANK_TYPE_SUBSPECIES) {
 				return "Subspecies";
 			} else if (infraType == Infratype.INFRARANK_TYPE_VARIETY) {
 				return "Variety";
 			}
 		}
 		return TaxonLevel.displayableLevel[level];
 	}
 
 	public static int getDisplayableLevelCount() {
 		return TaxonLevel.displayableLevel.length;
 	}
 
 	public String getSequenceCode() {
 		return "0.0";
 	}
 
 	public boolean isDeprecated() {
 		return getTaxonStatus().getId() != TaxonStatus.getIdFromCode(TaxonStatus.STATUS_NEW) && getTaxonStatus().getId() != TaxonStatus.getIdFromCode(TaxonStatus.STATUS_ACCEPTED);
 	}
 
 	public int getLevel() {
 		return getTaxonLevel().getLevel();
 	}
 
 	public String getFullName() {
 		return getFriendlyName();
 	}
 
 	public int getParentID() {
 		return getParent().getId();
 	}
 
 	protected CommonName primaryCommonName;
 
 	public CommonName getPrimaryCommonName() {
 		if (primaryCommonName == null) {
 			for (CommonName name : getCommonNames()) {
 				if (name.isPrimary()) {
 					primaryCommonName = name;
 					break;
 				}
 			}
 			if (primaryCommonName == null && !getCommonNames().isEmpty())
 				primaryCommonName = getCommonNames().iterator().next();
 			
 		}
 		return primaryCommonName;
 	}
 
 
 
 	public String getChildrenCSV() {
 		StringBuilder builder = new StringBuilder();
 		for (Taxon child : getChildren()) {
 			builder.append(child.getId() + ",");
 		}
 		if (builder.length() > 0) {
 			return builder.substring(0, builder.length() - 1);
 		}
 		return "";
 	}
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 
 	public Taxon() {
 		working_set = new java.util.HashSet<WorkingSet>();
 		reference = new java.util.HashSet<Reference>();
 		children = new java.util.HashSet<Taxon>();
 		edits = new java.util.HashSet<Edit>();
 		notes = new java.util.HashSet<Notes>();
 		assessments = new java.util.HashSet<Assessment>();
 		synonyms = new java.util.HashSet<Synonym>();
 		commonNames = new java.util.HashSet<CommonName>();
 	}
 
 	private int id;
 
 	private TaxonLevel taxonLevel;
 
 	private String name;
 
 	private String friendlyName;
 
 	private boolean hybrid;
 
 	private String taxonomicAuthority;
 
 	private TaxonStatus taxonStatus;
 
 	private Taxon parent;
 
 	private java.util.Set<WorkingSet> working_set;
 
 	private java.util.Set<Reference> reference;
 
 	private java.util.Set<Taxon> children;
 
 	private java.util.Set<Edit> edits;
 
 	private java.util.Set<Notes> notes;
 
 	private java.util.Set<Assessment> assessments;
 
 	private java.util.Set<Synonym> synonyms;
 
 	private java.util.Set<CommonName> commonNames;
 
 	private Infratype infratype;
 	
 	private String generatedXML;
 
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
 
 	public void setFriendlyName(String value) {
 		this.friendlyName = value;
 	}
 
 	public String getFriendlyName() {
 		return friendlyName;
 	}
 
 	public void setHybrid(boolean value) {
 		this.hybrid = value;
 	}
 
 	public boolean getHybrid() {
 		return hybrid;
 	}
 
 	public void setTaxonomicAuthority(String value) {
 		this.taxonomicAuthority = value;
 	}
 
 	public String getTaxonomicAuthority() {
 		return taxonomicAuthority;
 	}
 
 	public void setWorking_set(java.util.Set<WorkingSet> value) {
 		this.working_set = value;
 	}
 
 	public java.util.Set<WorkingSet> getWorking_set() {
 		return working_set;
 	}
 
 	public void setReference(java.util.Set<Reference> value) {
 		this.reference = value;
 	}
 
 	public java.util.Set<Reference> getReference() {
 		return reference;
 	}
 
 	public void setTaxonLevel(TaxonLevel value) {
 		this.taxonLevel = value;
 	}
 
 	public TaxonLevel getTaxonLevel() {
 		return taxonLevel;
 	}
 
 	public void setChildren(java.util.Set<Taxon> value) {
 		this.children = value;
 	}
 
 	public java.util.Set<Taxon> getChildren() {
 		return children;
 	}
 
 	public void setTaxonStatus(TaxonStatus value) {
 		this.taxonStatus = value;
 	}
 
 	public String getStatusCode() {
 		return taxonStatus.getCode();
 	}
 
 	public TaxonStatus getTaxonStatus() {
 		return taxonStatus;
 	}
 
 	public void setEdits(java.util.Set<Edit> value) {
 		this.edits = value;
 	}
 
 	public java.util.Set<Edit> getEdits() {
 		return edits;
 	}
 
 	public void setNotes(java.util.Set<Notes> value) {
 		this.notes = value;
 	}
 
 	public java.util.Set<Notes> getNotes() {
 		return notes;
 	}
 
 	public void setParent(Taxon value) {
 		this.parent = value;
 	}
 
 	public Taxon getParent() {
 		return parent;
 	}
 
 	public void setAssessments(java.util.Set<Assessment> value) {
 		this.assessments = value;
 	}
 
 	public java.util.Set<Assessment> getAssessments() {
 		return assessments;
 	}
 
 	public void setSynonyms(java.util.Set<Synonym> value) {
 		this.synonyms = value;
 	}
 
 	public java.util.Set<Synonym> getSynonyms() {
 		return synonyms;
 	}
 
 	public void setCommonNames(java.util.Set<CommonName> value) {
 		this.commonNames = value;
 	}
 
 	public java.util.Set<CommonName> getCommonNames() {
 		return commonNames;
 	}
 
 	public void setInfratype(Infratype value) {
 		this.infratype = value;
 	}
 
 	public Infratype getInfratype() {
 		return infratype;
 	}
 
 	public String toString() {
 		return String.valueOf(getId());
 	}
 
 	public String toXMLMinimal() {
 		return toRelatedXML(ROOT_TAG);
 	}
 
 	/**
 	 * Parses the parent or child NativeElement
 	 * 
 	 * @param element
 	 *            -- start with either parent, or child, or is basic
 	 * @return
 	 */
 	public static Taxon fromXMLminimal(NativeElement element) {
 		Taxon taxon = new Taxon();
 		taxon.setId(Integer.parseInt(element.getAttribute("id")));
 		taxon.setName(element.getAttribute("name"));
 		taxon.setFriendlyName(element.getAttribute("fullname"));
 		return taxon;
 	}
 
 	public String toXML() {
 		return toXMLDetailed();
 	}
 
 	public String toXMLDetailed() {
 	
 	
 		// TRY TO GET THE PARENT NAME FROM THE SOURCE, IF APPLICABLE
 		StringBuilder xml = new StringBuilder();
 		xml.append("<" + ROOT_TAG + " id=\"" + getId() + "\" name=\"" + getName() + "\" hybrid=\""
 				+ getHybrid() + "\" level=\"" + getLevel() + "\" fullname=\"" + getFullName() + "\">");
 	
 		if (getTaxonStatus() != null)
 			xml.append(getTaxonStatus().toXML());
 	
 		if (getParent() != null) {
 			xml.append(getParent().toParentXML());
 		}
 		if (getChildren() != null) {
 			for (Taxon child : getChildren())
 				xml.append(child.toChildXML());
 		}
 	
 		xml.append("<footprint>" + getFootprintCSV() + "</footprint>");
 		if (getTaxonomicAuthority() != null)
 			xml.append("<taxonomicAuthority><![CDATA[" + getTaxonomicAuthority() + "]]></taxonomicAuthority>");
 	
 		if (getEdits() != null) {
 			for (Edit edit : getEdits())
 				xml.append(edit.toXML());
 		}
 	
 		if (getNotes() != null) {
 			for (Notes note : getNotes())
 				xml.append(note.toXML());
 		}
 	
 		if (getReference() != null) {
 			for (Reference note : getReference())
 				xml.append(note.toXML());
 		}
 	
 		if (getSynonyms() != null) {
 			for (Synonym note : getSynonyms())
 				xml.append(note.toXML());
 		}
 	
 		if (getCommonNames() != null) {
 			for (CommonName note : getCommonNames())
 				xml.append(note.toXML());
 		}
 	
 		if (getInfratype() != null) {
 			xml.append(getInfratype().toXML());
 		}
 	
 		xml.append("</" + ROOT_TAG + ">");
 		generatedXML = xml.toString();
 		return xml.toString();
 	}
 	
 	public String getGeneratedXML() {
 		return generatedXML;
 	}
 
 	/**
 	 * Returns a synonym with all of the appropriate fields set, based on the
 	 * taxon argument.
 	 * 
 	 * @param taxon
 	 *            - used to generate the synonym
 	 * @return a synonym
 	 */
 	public static Synonym synonymizeTaxon(Taxon taxon) {
 		return new Synonym(taxon);
 	}
 
 	
 
 	public static Taxon createNode(long id, String name, int level, boolean hybrid) {
 	
 		Taxon taxon = new Taxon();
 		if (id > 0)
 			taxon.setId((int) id);
 		taxon.setName(name);
 		taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(level));
 		taxon.setHybrid(hybrid);
 		return taxon;
 	
 	}
 	
 	/**
 	 * Method should only be called from toParentXML and toChildXML
 	 * 
 	 * @param taxon
 	 *            -- the taxon to be searlized
 	 * @param tagName
 	 *            -- either "parent", "child"
 	 * @return
 	 */
 	String toRelatedXML(String tagName) {
 		if (tagName.equals("parent") || tagName.equalsIgnoreCase("child") || tagName.equals(Taxon.ROOT_TAG)) {
 			return "<" + tagName + " id=\"" + getId() + "\" name=\"" + getName() + "\" fullname=\""
 					+ getFriendlyName() + "\"/>";
 		}
 		return null;
 	}
 
 	public String toParentXML() {
 		return toRelatedXML("parent");
 	}
 
 	public String toChildXML() {
 		return toRelatedXML("child");
 	}
 	
 	public static Taxon fromXML(NativeDocument ndoc) {
 		return fromXML(ndoc.getDocumentElement());
 	}
 	
 	public static Taxon fromXML(NativeElement nodeElement) {
 		Taxon taxon = null;
 		long id = LongUtils.safeParseLong(nodeElement.getAttribute("id"));
 		String name = nodeElement.getAttribute("name");
 		String fullName = nodeElement.getAttribute("fullname");
 		int level = Integer.parseInt(nodeElement.getAttribute("level"));
 		boolean hybrid = (nodeElement.getAttribute("hybrid") != null && nodeElement.getAttribute("hybrid").equals(
 				"true"));
 		
 		taxon = Taxon.createNode(id, name, level, hybrid);
 		taxon.setFriendlyName(fullName);
 		
 		
 		
 		NativeNodeList status = nodeElement.getElementsByTagName(TaxonStatus.ROOT_TAG);
 		if (status.getLength() > 0)
 			taxon.setTaxonStatus(TaxonStatus.fromXML(status.elementAt(0)));
 
 		NativeNodeList parent = nodeElement.getElementsByTagName("parent");
 		if (parent.getLength() > 0) {
 			taxon.setParent(Taxon.fromXMLminimal(parent.elementAt(0)));
 		}
 
 		NativeNodeList children = nodeElement.getElementsByTagName("child");
 		if (children.getLength() > 0) {
 			taxon.setChildren(new HashSet<Taxon>());
 			for (int i = 0; i < children.getLength(); i++)
 				taxon.getChildren().add(Taxon.fromXMLminimal(children.elementAt(i)));
 		}
 
 		NativeNodeList list = nodeElement.getElementsByTagName(Infratype.ROOT_NAME);
 		if (list.getLength() > 0)
 			taxon.setInfratype(Infratype.fromXML(list.elementAt(0), taxon));
 
 		NativeNodeList footprint = nodeElement.getElementsByTagName("footprint");
 		if (footprint.getLength() > 0) {
 			taxon.setFootprint(footprint.elementAt(0).getTextContent().split(","));
 		}
 
 		NativeNodeList taxAuths = nodeElement.getElementsByTagName("taxonomicAuthority");
 		if (taxAuths.getLength() > 0)
 			taxon.setTaxonomicAuthority(XMLUtils.clean(taxAuths.elementAt(0).getTextContent()));
 
 		NativeNodeList names = nodeElement.getElementsByTagName(CommonName.ROOT_TAG);
 		taxon.setCommonNames(new HashSet<CommonName>());
 		for (int i = 0; i < names.getLength(); i++) {
 			taxon.getCommonNames().add(CommonName.fromXML(names.elementAt(i)));
 		}
 		
 		NativeNodeList notes = nodeElement.getElementsByTagName(Notes.ROOT_TAG);
 		taxon.setNotes(new HashSet<Notes>());
 		for (int i = 0; i < notes.getLength(); i++) {
 			taxon.getNotes().add(Notes.fromXML(notes.elementAt(i)));
 		}
 
 		NativeNodeList edits = nodeElement.getElementsByTagName(Edit.ROOT_TAG);
 		taxon.setEdits(new HashSet<Edit>());
 		for (int i = 0; i < edits.getLength(); i++) {
 			taxon.getEdits().add(Edit.fromXML(edits.elementAt(i)));
 		}
 
 		NativeNodeList synonyms = nodeElement.getElementsByTagName(Synonym.ROOT_TAG);
 		taxon.setSynonyms(new HashSet<Synonym>());
 		for (int i = 0; i < synonyms.getLength(); i++)
 			taxon.getSynonyms().add(Synonym.fromXML(synonyms.elementAt(i), taxon));
 
 		NativeNodeList assessments = nodeElement.getElementsByTagName(Assessment.ROOT_TAG);
 		taxon.setAssessments(new HashSet<Assessment>());
 		for (int i = 0; i < assessments.getLength(); i++) {
 			Assessment assessment = Assessment.fromXML(assessments.elementAt(i));
 			taxon.getAssessments().add(assessment);
 			assessment.setTaxon(taxon);
 		}
 
 		NativeNodeList references = nodeElement.getElementsByTagName(Reference.ROOT_TAG);
 		taxon.setReference(new HashSet<Reference>());
 		for (int i = 0; i < references.getLength(); i++)
 			taxon.getReference().add(Reference.fromXML(references.elementAt(i)));
 
 		return taxon;
 
 	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Taxon)
			return ((Taxon)obj).getId() == this.id;
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return (Taxon.class.toString() + getId()).hashCode();
	}
 }
