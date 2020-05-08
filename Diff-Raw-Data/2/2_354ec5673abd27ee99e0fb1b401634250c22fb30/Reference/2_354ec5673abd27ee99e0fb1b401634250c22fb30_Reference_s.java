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
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared;
 import org.iucn.sis.shared.api.citations.ReferenceCitationGeneratorShared.ReturnedCitation;
 import org.iucn.sis.shared.api.debug.Debug;
 
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.portable.PortableReplacer;
 import com.solertium.util.portable.XMLWritingUtils;
 public class Reference implements Serializable, AuthorizableObject {
 	
 	private static final long serialVersionUID = 1L;
 
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
 	public static final String ROOT_TAG = "reference";
 	
 	public static boolean isCitationValid(String complete) {
 		return (complete != null && complete.equalsIgnoreCase("y"));
 	}
 	
 	public String getFullURI() {
 		return "resource/reference";
 	}
 	
 	public String getProperty(String key) {
 		return "";
 	}
 	
 	public static Reference fromMap(Map<String, String> map) {
 		Reference reference = new Reference();
 		reference.setId(0);
 		
 		fromMap(reference, map);
 		
 		return reference;
 	}
 	
 	public static void fromMap(Reference reference, Map<String, String> map) {
 		for (Map.Entry<String, String> entry : map.entrySet())
 			addField(reference, entry.getKey(), entry.getValue());
 	}
 	
 	public static Reference fromXML(NativeNode element) throws IllegalArgumentException {
 		return fromXML(element, false);
 	}
 	
 	public static Reference fromXML(NativeNode element, boolean allowNew) {
 		final Reference reference = new Reference();
 		reference.setId(-1);
 		
 		final NativeNodeList nodes = element.getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			final NativeNode field = nodes.item(i);
 			final String name = field.getNodeName();
 			final String value = field.getTextContent();
 	
 			addField(reference, name, value);
 		}	
 		
 		if (!allowNew && reference.getId() <= 0)
 			throw new IllegalArgumentException("Error building reference from node, required fields not present.");
 		
 		if (reference.getType() == null) {
 			Debug.println("Reference type null for {0}, setting to 'other'", reference.getId());
 			reference.setType("other");
 		}
 		
 		return reference;
 	}
 	
 	public static void addField(Reference reference, String name, String rawValue) {
 		String value = toStringOrBlank(rawValue);
 		
 		if ("id".equals(name)) {
 			try {
 				reference.setId(Integer.parseInt(value));
 			} catch (NumberFormatException e) {
 				Debug.println("References being build from invalid model!");
 			}
 		}
 		else if ("type".equals(name))
 			reference.setType(value);
 		else if (matches("citationShort", name))
 			reference.setCitationShort(value);
 		else if (matches("citation", name))
 			reference.setCitation(value);
 		else if (matches("citationComplete", name) && !isBlank(value))
 			reference.setCitationComplete(Boolean.valueOf(value));
 		else if (matches("author", name))
 			reference.setAuthor(value);
 		else if (matches("year", name))
 			reference.setYear(value);
 		else if (matches("title", name))
 			reference.setTitle(value);
 		else if (matches("secondaryAuthor", name))
 			reference.setSecondaryAuthor(value);
 		else if (matches("secondaryTitle", name))
 			reference.setSecondaryTitle(value);
 		else if (matches("placePublished", name))
 			reference.setPlacePublished(value);
 		else if (matches("publisher", name))
 			reference.setPublisher(value);
 		else if (matches("volume", name))
 			reference.setVolume(value);
 		else if (matches("numberOfVolumes", name))
 			reference.setNumberOfVolumes(value);
 		else if (matches("number", name))
 			reference.setNumber(value);
 		else if (matches("pages", name))
 			reference.setPages(value);
 		else if (matches("section", name))
 			reference.setSection(value);
 		else if (matches("tertiaryAuthor", name))
 			reference.setTertiaryAuthor(value);
 		else if (matches("tertiaryTitle", name))
 			reference.setTertiaryTitle(value);
 		else if (matches("edition", name))
 			reference.setEdition(value);
 		else if (matches("date", name))
 			reference.setDateValue(value);
 		else if (matches("subsidiaryAuthor", name))
 			reference.setSubsidiaryAuthor(value);
 		else if (matches("shortTitle", name))
 			reference.setShortTitle(value);
 		else if (matches("alternateTitle", name))
 			reference.setAlternateTitle(value);
 		else if (matches("isbnissn", name))
 			reference.setIsbnIssn(value);
 		else if (matches("keywords", name))
 			reference.setKeywords(value);
 		else if (matches("url", name))
 			reference.setUrl(value);
 		else if (matches("hash", name))
 			reference.setHash(value);
 		else if (matches("bibCode", name) && !isBlank(value))
 			reference.setBibCode(Integer.valueOf(value));
 		else if (matches("bibNoInt", name) && !isBlank(value))
 			reference.setBibNoInt(Integer.valueOf(value));
 		else if (matches("bibNumber", name) && !isBlank(value))
 			reference.setBibNumber(Integer.valueOf(value));
 		else if (matches("externalBibCode", name))
 			reference.setExternalBibCode(value);
 		else if (matches("submissionType", name))
 			reference.setSubmissionType(value);
 	}
 	
 	/**
 	 * Ensures backward compatiblity with the reference server 
 	 * when looking up information.
 	 * @param first
 	 * @param second
 	 * @return
 	 */
 	private static boolean matches(String first, String second) {
 		String s1 = PortableReplacer.stripNonword(first).toLowerCase();
 		String s2 = PortableReplacer.stripNonword(second).toLowerCase();
 		
 		return s1.equals(s2);
 	}
 	
 	private static boolean isBlank(String value) {
 		return value == null || "".equals(value);
 	}
 	
 	private static String toStringOrNull(Object value) {
 		if (value == null)
 			return null;
 		
 		String ret = value.toString();
 		
 		return isBlank(ret) ? null : ret;
 	}
 	
 	private static String toStringOrBlank(String value) {
 		return value == null || "null".equals(value) ? "" : value;
 	}
 	
 	public String toXML() {
 		StringBuilder xml = new StringBuilder();
 		xml.append("<" + ROOT_TAG + ">");
 		for (Map.Entry<String, String> entry : toMap().entrySet())
 			xml.append(XMLWritingUtils.writeCDATATag(entry.getKey(), entry.getValue(), true));
 		xml.append("</" + ROOT_TAG + ">");
 		return xml.toString();	
 	}
 	
 	/**
 	 * Returns a map of fields that exist for this reference. 
 	 * If the field value is blank or null, it will not be in  
 	 * the data map.
 	 * @return
 	 */
 	public ReferenceMap toMap() {
 		final ReferenceMap map = new ReferenceMap();
 		map.put("id", toStringOrNull(getId()));
 		map.put("type", getType());
 		map.put("citationShort", getCitationShort());
 		map.put("citation", getCitation());
 		map.put("citationComplete", ""+(getCitationComplete()));
 		map.put("author", getAuthor());
 		map.put("edition", getEdition());
 		map.put("year", getYear());
 		map.put("title", getTitle());
 		map.put("secondaryAuthor", getSecondaryAuthor());
 		map.put("secondaryTitle", getSecondaryTitle());
 		map.put("placePublished", getPlacePublished());
 		map.put("publisher", getPublisher());
 		map.put("volume", getVolume());
 		map.put("numberOfVolumes", getNumberOfVolumes());
 		map.put("number", getNumber());
 		map.put("pages", getPages());
 		map.put("section", getSection());
 		map.put("tertiaryAuthor", getTertiaryAuthor());
 		map.put("tertiaryTitle", getTertiaryTitle());
 		map.put("date", getDateValue());
 		map.put("subsidiaryAuthor", getSubsidiaryAuthor());
 		map.put("shortTitle", getShortTitle());
 		map.put("alternateTitle", getAlternateTitle());
 		map.put("isbnissn", getIsbnIssn());
 		map.put("keywords", getKeywords());
 		map.put("url", getUrl());
 		map.put("hash", getHash());
 		map.put("bibCode", toStringOrNull(getBibCode()));
 		map.put("bibNumtoStringOrNull", toStringOrNull(getBibNumber()));
 		map.put("bibNoInt", toStringOrNull(getBibNoInt()));
 		map.put("externalBibCode", getExternalBibCode());
 		map.put("submissionType", getSubmissionType());
 	
 		return map;
 	}
 	
 	public String getField(String name) {
 		return toMap().get(name);
 	}
 	
 	public boolean hasField(String name) {
 		return toMap().containsKey(name);
 	}
 	
 	public boolean isCitationValid() {
 		return getCitationComplete();
 	}
 	
 	public void setCitationComplete(boolean isComplete) {
 		this.citationComplete = isComplete;
 	}
 	
 	public Boolean getCitationComplete() {
 		return citationComplete;
 	}
 	
 	public String generateCitation() {
 		ReturnedCitation returnedCitation = ReferenceCitationGeneratorShared.generateNewCitation(toMap(), type);
 		if (returnedCitation != null) {
 			citation = returnedCitation.citation;
 			citationComplete = returnedCitation.allFieldsEntered;
 		}
 		return citation;
 	}
 	
 	public String generateCitationIfNotAlreadyGenerate() {
 		if (toStringOrBlank(citation).trim().equals(""))
 			return generateCitation();
 		return citation;
 	}
 	
 	private ReturnedCitation returnedCitation;
 	
 	public ReturnedCitation getReturnedCitation() {
 		return returnedCitation;
 	}
 	
 	public void setReturnedCitation(ReturnedCitation returnedCitation) {
 		this.returnedCitation = returnedCitation;
 	}
 	
 	public Reference deepCopy() {
 		Reference copy = new Reference();
 		copy.setId(getId());
 		copy.setAlternateTitle(getAlternateTitle());
 		copy.setAuthor(getAuthor());
 		copy.setBibCode(getBibCode());
 		copy.setBibNoInt(getBibNoInt());
 		copy.setBibNumber(getBibNumber());
 		copy.setCitation(getCitation());
 		copy.setCitationComplete(getCitationComplete());
 		copy.setCitationShort(getCitationShort());
 		copy.setDateValue(getDateValue());
 		copy.setEdition(getEdition());
 		copy.setExternalBibCode(getExternalBibCode());
 		copy.setHash(getHash());
 		copy.setIsbnIssn(getIsbnIssn());
 		copy.setKeywords(getKeywords());
 		copy.setNumber(getNumber());
 		copy.setNumberOfVolumes(getNumberOfVolumes());
 		copy.setPages(getPages());
 		copy.setPlacePublished(getPlacePublished());
 		copy.setPublisher(getPublisher());
 		copy.setReferenceID(getReferenceID());
 		copy.setReturnedCitation(getReturnedCitation());
 		copy.setSecondaryAuthor(getSecondaryAuthor());
 		copy.setSecondaryTitle(getSecondaryTitle());
 		copy.setSection(getSection());
 		copy.setShortTitle(getShortTitle());
 		copy.setSubmissionType(getSubmissionType());
 		copy.setSubsidiaryAuthor(getSubsidiaryAuthor());
 		copy.setTertiaryAuthor(getTertiaryAuthor());
 		copy.setTertiaryTitle(getTertiaryTitle());
 		copy.setTitle(getTitle());
 		copy.setType(getType());
 		copy.setUrl(getUrl());
 		copy.setVolume(getVolume());
 		copy.setYear(getYear());
 		
 		return copy;
 	}
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
 	
 	private String generationCode;
 	
 	public Reference() {
 		generationCode = new Date().getTime()+"";
 	}
 	
 	private int id;
 	
 	private String type;
 	
 	private String citationShort;
 	
 	private String citation;
 	
 	private Boolean citationComplete;
 	
 	private String author;
 	
 	private String year;
 	
 	private String title;
 	
 	private String secondaryAuthor;
 	
 	private String secondaryTitle;
 	
 	private String placePublished;
 	
 	private String publisher;
 	
 	private String volume;
 	
 	private String numberOfVolumes;
 	
 	private String number;
 	
 	private String pages;
 	
 	private String section;
 	
 	private String tertiaryAuthor;
 	
 	private String tertiaryTitle;
 	
 	private String edition;
 	
 	private String dateValue;
 	
 	private String subsidiaryAuthor;
 	
 	private String shortTitle;
 	
 	private String alternateTitle;
 	
 	private String isbnIssn;
 	
 	private String keywords;
 	
 	private String url;
 	
 	private String hash;
 	
 	private Integer bibCode;
 	
 	private Integer bibNumber;
 	
 	private Integer bibNoInt;
 	
 	private String externalBibCode;
 	
 		
 	private String submissionType;
 	
 	private java.util.Set<Synonym> synonym = new java.util.HashSet<Synonym>();
 	
 	private java.util.Set<CommonName> common_name = new java.util.HashSet<CommonName>();
 	
 	private java.util.Set<Assessment> assessment = new java.util.HashSet<Assessment>();
 	
 	private java.util.Set<Field> field = new java.util.HashSet<Field>();
 	
 	private java.util.Set<Taxon> taxon = new java.util.HashSet<Taxon>();
 	
 	
 	
 	public String getHash() {
 		return hash;
 	}
 
 	public void setHash(String hash) {
 		this.hash = hash;
 	}
 
 	public Integer getBibCode() {
 		return bibCode;
 	}
 
 	public void setBibCode(Integer bibCode) {
 		this.bibCode = bibCode;
 	}
 
 	public Integer getBibNumber() {
 		return bibNumber;
 	}
 
 	public void setBibNumber(Integer bibNumber) {
 		this.bibNumber = bibNumber;
 	}
 
 	public Integer getBibNoInt() {
 		return bibNoInt;
 	}
 
 	public void setBibNoInt(Integer bibNoInt) {
 		this.bibNoInt = bibNoInt;
 	}
 
 	public String getExternalBibCode() {
 		return externalBibCode;
 	}
 
 	public void setExternalBibCode(String externalBibCode) {
 		this.externalBibCode = externalBibCode;
 	}
 
 	public String getSubmissionType() {
 		return submissionType;
 	}
 
 	public void setSubmissionType(String submissionType) {
 		this.submissionType = submissionType;
 	}
 
 	public void setId(int value) {
 		this.id = value;
 		this.generationCode = value + "";
 	}
 	
 	public void setReferenceID(int value) {
 		setId(value);
 	}
 	
 	public int getReferenceID() {
 		return getId();
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public int getORMID() {
 		return getId();
 	}
 	
 	public void setType(String value) {
 		this.type = value;
 	}
 	
 	public String getType() {
 		return type;
 	}
 	
 	public void setCitationShort(String value) {
 		this.citationShort = value;
 	}
 	
 	public String getCitationShort() {
 		return citationShort;
 	}
 	
 	public void setCitation(String value) {
 		this.citation = value;
 	}
 	
 	public String getCitation() {
 		return citation;
 	}
 	
 	
 	
 	public void setAuthor(String value) {
 		this.author = value;
 	}
 	
 	public String getAuthor() {
 		return author;
 	}
 	
 	public void setYear(String value) {
 		this.year = value;
 	}
 	
 	public String getYear() {
 		return year;
 	}
 	
 	public void setTitle(String value) {
 		this.title = value;
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 	
 	public void setSecondaryAuthor(String value) {
 		this.secondaryAuthor = value;
 	}
 	
 	public String getSecondaryAuthor() {
 		return secondaryAuthor;
 	}
 	
 	public void setSecondaryTitle(String value) {
 		this.secondaryTitle = value;
 	}
 	
 	public String getSecondaryTitle() {
 		return secondaryTitle;
 	}
 	
 	public void setPlacePublished(String value) {
 		this.placePublished = value;
 	}
 	
 	public String getPlacePublished() {
 		return placePublished;
 	}
 	
 	public void setPublisher(String value) {
 		this.publisher = value;
 	}
 	
 	public String getPublisher() {
 		return publisher;
 	}
 	
 	public void setVolume(String value) {
 		this.volume = value;
 	}
 	
 	public String getVolume() {
 		return volume;
 	}
 	
 	public void setNumberOfVolumes(String value) {
 		this.numberOfVolumes = value;
 	}
 	
 	public String getNumberOfVolumes() {
 		return numberOfVolumes;
 	}
 	
 	public void setNumber(String value) {
 		this.number = value;
 	}
 	
 	public String getNumber() {
 		return number;
 	}
 	
 	public void setPages(String value) {
 		this.pages = value;
 	}
 	
 	public String getPages() {
 		return pages;
 	}
 	
 	public void setSection(String value) {
 		this.section = value;
 	}
 	
 	public String getSection() {
 		return section;
 	}
 	
 	public void setTertiaryAuthor(String value) {
 		this.tertiaryAuthor = value;
 	}
 	
 	public String getTertiaryAuthor() {
 		return tertiaryAuthor;
 	}
 	
 	public void setTertiaryTitle(String value) {
 		this.tertiaryTitle = value;
 	}
 	
 	public String getTertiaryTitle() {
 		return tertiaryTitle;
 	}
 	
 	public void setEdition(String value) {
 		this.edition = value;
 	}
 	
 	public String getEdition() {
 		return edition;
 	}
 	
 	public void setDateValue(String value) {
 		this.dateValue = value;
 	}
 	
 	public String getDateValue() {
 		return dateValue;
 	}
 	
 	public void setSubsidiaryAuthor(String value) {
 		this.subsidiaryAuthor = value;
 	}
 	
 	public String getSubsidiaryAuthor() {
 		return subsidiaryAuthor;
 	}
 	
 	public void setShortTitle(String value) {
 		this.shortTitle = value;
 	}
 	
 	public String getShortTitle() {
 		return shortTitle;
 	}
 	
 	public void setAlternateTitle(String value) {
 		this.alternateTitle = value;
 	}
 	
 	public String getAlternateTitle() {
 		return alternateTitle;
 	}
 	
 	public void setIsbnIssn(String value) {
 		this.isbnIssn = value;
 	}
 	
 	public String getIsbnIssn() {
 		return isbnIssn;
 	}
 	
 	public void setKeywords(String value) {
 		this.keywords = value;
 	}
 	
 	public String getKeywords() {
 		return keywords;
 	}
 	
 	public void setUrl(String value) {
 		this.url = value;
 	}
 	
 	public String getUrl() {
 		return url;
 	}
 	
 	public void setSynonym(java.util.Set<Synonym> value) {
 		this.synonym = value;
 	}
 	
 	public java.util.Set<Synonym> getSynonym() {
 		return synonym;
 	}
 	
 	
 	public void setCommon_name(java.util.Set<CommonName> value) {
 		this.common_name = value;
 	}
 	
 	public java.util.Set<CommonName> getCommon_name() {
 		return common_name;
 	}
 	
 	
 	public void setAssessment(java.util.Set<Assessment> value) {
 		this.assessment = value;
 	}
 	
 	public java.util.Set<Assessment> getAssessment() {
 		return assessment;
 	}
 	
 	
 	public void setField(java.util.Set<Field> value) {
 		this.field = value;
 	}
 	
 	public java.util.Set<Field> getField() {
 		return field;
 	}
 	
 	
 	public void setTaxon(java.util.Set<Taxon> value) {
 		this.taxon = value;
 	}
 	
 	public java.util.Set<Taxon> getTaxon() {
 		return taxon;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((generationCode == null) ? 0 : generationCode.hashCode());
 		result = prime * result + id;
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
 		Reference other = (Reference) obj;
 		if (generationCode == null) {
 			if (other.generationCode != null)
 				return false;
 		} else if (!generationCode.equals(other.generationCode))
 			return false;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 	public String toString() {
 		return "Reference #" + String.valueOf(getId()) + " with generation code " + generationCode;
 	}
 	
 	public static class ReferenceMap extends LinkedHashMap<String, String> {
 		
 		private static final long serialVersionUID = 1L;
 		
 		@Override
 		public boolean containsKey(Object arg0) {
 			return arg0 instanceof String ? false : super.containsKey(clean((String)arg0));
 		}
 		
 		@Override
 		public String put(String key, String value) {
 			return super.put(clean(key), value);
 		}
 		
 		private String clean(String s) {
 			return s == null ? null : PortableReplacer.stripNonword(s).toLowerCase();
 		}
 
 		public void addField(final String name, final String value) {
 			put(clean(name), value);
 		}
 		
 		public String get(Object key) {
 			if (key == null)
 				return null;
 			if (key instanceof String)
				return super.get(((String) key).toLowerCase());
 			return super.get(key);
 		}
 
 		public String getField(final String name) {
 			return get(name.toLowerCase());
 		}
 		
 	}
 	
 }
