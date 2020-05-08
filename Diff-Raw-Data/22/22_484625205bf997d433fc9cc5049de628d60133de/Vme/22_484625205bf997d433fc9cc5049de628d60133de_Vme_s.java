 package org.fao.fi.vme.domain;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 /**
  * 
  * @author Erik van Ingen
  * 
  */
 @Entity
 public class Vme {
 
 	@Id
 	private Long id;
 
 	/**
 	 * This VME is managed by this Rfmo
 	 */
 	@ManyToOne
 	private Rfmo rfmo;
 
	@ManyToMany
 	@JoinTable(name = "VME_SPECIFIC_MEASURES", //
	joinColumns = { @JoinColumn(name = "VME_ID", referencedColumnName = "ID") }, //
	inverseJoinColumns = { @JoinColumn(name = "SPECIFIC_MEASURES_ID", referencedColumnName = "ID") })
 	private List<SpecificMeasures> specificMeasureList;
 
 	@OneToMany(cascade = { CascadeType.ALL })
 	private List<Profile> profileList;;
 
 	@OneToMany(cascade = { CascadeType.ALL })
 	private List<GeoRef> geoRefList;;
 
 	@OneToMany(cascade = { CascadeType.ALL })
 	private List<History> historyList;
 
 	/**
 	 * This validity period on the level of the reference object and applies to the VME itself. It has noting to do with
 	 * the reporting year.
 	 * 
 	 * 
 	 */
 	private ValidityPeriod validityPeriod;
 
 	/**
 	 *
 	 */
 	@OneToOne(cascade = { CascadeType.ALL })
 	private MultiLingualString name;
 
 	/**
 	 *
 	 */
 	private String geoform;
 
 	/**
 	 *
 	 */
 	private String areaType;
 
 	/**
 	 *
 	 */
 	private String criteria;
 
 	public MultiLingualString getName() {
 		return name;
 	}
 
 	public void setName(MultiLingualString name) {
 		this.name = name;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Rfmo getRfmo() {
 		return rfmo;
 	}
 
 	public void setRfmo(Rfmo rfmo) {
 		this.rfmo = rfmo;
 	}
 
 	public List<SpecificMeasures> getSpecificMeasureList() {
 		return specificMeasureList;
 	}
 
 	public void setSpecificMeasureList(List<SpecificMeasures> specificMeasureList) {
 		this.specificMeasureList = specificMeasureList;
 	}
 
 	public List<Profile> getProfileList() {
 		return profileList;
 	}
 
 	public void setProfileList(List<Profile> profileList) {
 		this.profileList = profileList;
 	}
 
 	public List<GeoRef> getGeoRefList() {
 		return geoRefList;
 	}
 
 	public void setGeoRefList(List<GeoRef> geoRefList) {
 		this.geoRefList = geoRefList;
 	}
 
 	public List<History> getHistoryList() {
 		return historyList;
 	}
 
 	public void setHistoryList(List<History> vmeHistoryList) {
 		this.historyList = vmeHistoryList;
 	}
 
 	public ValidityPeriod getValidityPeriod() {
 		return validityPeriod;
 	}
 
 	public void setValidityPeriod(ValidityPeriod validityPeriod) {
 		this.validityPeriod = validityPeriod;
 	}
 
 	public String getGeoform() {
 		return geoform;
 	}
 
 	public void setGeoform(String geoform) {
 		this.geoform = geoform;
 	}
 
 	public String getAreaType() {
 		return areaType;
 	}
 
 	public void setAreaType(String areaType) {
 		this.areaType = areaType;
 	}
 
 	public String getCriteria() {
 		return criteria;
 	}
 
 	public void setCriteria(String criteria) {
 		this.criteria = criteria;
 	}
 
 }
