 package org.fao.fi.vme.domain.model;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import org.fao.fi.vme.domain.interfaces.Period;
 import org.fao.fi.vme.domain.support.MultiLingualStringConverter;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.RSGReport;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGConverter;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGIdentifier;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGInstructions;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGMandatory;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGName;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGOneAmong;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGSection;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGSimpleReference;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGWeight;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.constants.ConceptData;
 import org.gcube.application.rsg.support.compiler.bridge.converters.impl.LongDataConverter;
 import org.gcube.application.rsg.support.compiler.bridge.converters.impl.StringDataConverter;
 import org.gcube.application.rsg.support.compiler.bridge.interfaces.Report;
 
 /**
  * 
  * @author Erik van Ingen
  * 
  */
 @Entity
 @RSGReport(name="Vulnerable Marine Ecosystem Data")
 public class Vme implements Period<Vme>, Report {
 
 	@RSGIdentifier
 	@RSGConverter(LongDataConverter.class)
 	@Id
 	private Long id;
 
 	/**
 	 * This VME is managed by this Rfmo
 	 */
 	@RSGName("Authority")
 	@RSGSimpleReference
 	@ManyToOne(fetch = FetchType.EAGER)
 	private Rfmo rfmo;
 
 	/*
 	 * This is the owning side of the manyToMany relationship
 	 */
 	@RSGName("VME Specific Measures")
 	@RSGWeight(3)
 	@RSGSection
 	@ManyToMany(cascade = { CascadeType.ALL })
 	@JoinTable(name = "VME_SPECIFIC_MEASURE", //
 			   joinColumns = @JoinColumn(name = "VME_ID"), inverseJoinColumns = @JoinColumn(name = "SPECIFIC_MEASURE_ID"))
 	private List<SpecificMeasure> specificMeasureList;
 
 	@RSGName("VME Profiles")
 	@RSGWeight(2)
 	@RSGSection
 	@OneToMany(cascade = { CascadeType.ALL })
 	private List<Profile> profileList;
 
 	@RSGName("VME Map Reference")
 	@RSGWeight(4)
 	@RSGSection
 	@OneToMany(cascade = { CascadeType.ALL })
 	private List<GeoRef> geoRefList;
 
 	/**
 	 * This validity period on the level of the reference object and applies to the VME itself. It has noting to do with
 	 * the reporting year.
 	 * 
 	 * 
 	 */
 	@RSGName("Validity Period")
 	@RSGWeight(1)
 	private ValidityPeriod validityPeriod;
 
 	/**
 	 *
 	 */
 	@RSGName("VME Name")
 	@RSGConverter(MultiLingualStringConverter.class)
 	@RSGMandatory
 	@OneToOne(cascade = { CascadeType.ALL })
 	private MultiLingualString name;
 
 	/**
 	 *
 	 */
 	private String geoform;
 
 	@RSGName("Area Type")
 	@RSGMandatory
 	@RSGOneAmong(concept=VmeType.class, label=ConceptData.NAME, value=ConceptData.NAME)
 	@RSGConverter(StringDataConverter.class)
 	@RSGWeight(1)
 	private String areaType;
 
 	/**
 	 *
 	 */
 	@OneToOne(cascade = { CascadeType.ALL })
 	private MultiLingualString geoarea;
 
 	/**
 	 *
 	 */
 	@RSGName("Criteria")
 	@RSGMandatory
 	@RSGOneAmong(concept=VmeCriteria.class, label=ConceptData.NAME, value=ConceptData.NAME)
 	@RSGConverter(StringDataConverter.class)
 	@RSGWeight(1)
 	private String criteria;
 
 	private String inventoryIdentifier;
 
 	public String getInventoryIdentifier() {
 		return inventoryIdentifier;
 	}
 
 	public void setInventoryIdentifier(String inventoryIdentifier) {
 		this.inventoryIdentifier = inventoryIdentifier;
 	}
 
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
 
 	/**
 	 * @return the geoarea
 	 */
 	public MultiLingualString getGeoArea() {
 		return geoarea;
 	}
 
 	/**
 	 * @param geoarea
 	 *            the geoarea to set
 	 */
 	public void setGeoArea(MultiLingualString geoarea) {
 		this.geoarea = geoarea;
 	}
 
 	public List<SpecificMeasure> getSpecificMeasureList() {
 		return specificMeasureList;
 	}
 
 	public void setSpecificMeasureList(List<SpecificMeasure> specificMeasureList) {
 		this.specificMeasureList = specificMeasureList;
 	}
 
 //	/* (non-Javadoc)
 //	 * @see java.lang.Object#hashCode()
 //	 */
 //	@Override
 //	public int hashCode() {
 //		final int prime = 31;
 //		int result = 1;
 //		result = prime * result + ((this.areaType == null) ? 0 : this.areaType.hashCode());
 //		result = prime * result + ((this.criteria == null) ? 0 : this.criteria.hashCode());
 //		result = prime * result + ((this.geoRefList == null) ? 0 : this.geoRefList.hashCode());
 //		result = prime * result + ((this.geoarea == null) ? 0 : this.geoarea.hashCode());
 //		result = prime * result + ((this.geoform == null) ? 0 : this.geoform.hashCode());
 //		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
 //		result = prime * result + ((this.inventoryIdentifier == null) ? 0 : this.inventoryIdentifier.hashCode());
 //		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
 //		result = prime * result + ((this.profileList == null) ? 0 : this.profileList.hashCode());
 //		result = prime * result + ((this.rfmo == null) ? 0 : this.rfmo.hashCode());
 //		result = prime * result + ((this.specificMeasureList == null) ? 0 : this.specificMeasureList.hashCode());
 //		result = prime * result + ((this.validityPeriod == null) ? 0 : this.validityPeriod.hashCode());
 //		return result;
 //	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Vme other = (Vme) obj;
 		if (this.areaType == null) {
 			if (other.areaType != null)
 				return false;
 		} else if (!this.areaType.equals(other.areaType))
 			return false;
 		if (this.criteria == null) {
 			if (other.criteria != null)
 				return false;
 		} else if (!this.criteria.equals(other.criteria))
 			return false;
 		if (this.geoRefList == null) {
 			if (other.geoRefList != null)
 				return false;
 		} else if (!this.geoRefList.equals(other.geoRefList))
 			return false;
 		if (this.geoarea == null) {
 			if (other.geoarea != null)
 				return false;
 		} else if (!this.geoarea.equals(other.geoarea))
 			return false;
 		if (this.geoform == null) {
 			if (other.geoform != null)
 				return false;
 		} else if (!this.geoform.equals(other.geoform))
 			return false;
 		if (this.id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!this.id.equals(other.id))
 			return false;
 		if (this.inventoryIdentifier == null) {
 			if (other.inventoryIdentifier != null)
 				return false;
 		} else if (!this.inventoryIdentifier.equals(other.inventoryIdentifier))
 			return false;
 		if (this.name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!this.name.equals(other.name))
 			return false;
 		if (this.profileList == null) {
 			if (other.profileList != null)
 				return false;
 		} else if (!this.profileList.equals(other.profileList))
 			return false;
 		if (this.rfmo == null) {
 			if (other.rfmo != null)
 				return false;
 		} else if (!this.rfmo.equals(other.rfmo))
 			return false;
 		if (this.specificMeasureList == null) {
 			if (other.specificMeasureList != null)
 				return false;
 		} else if (!this.specificMeasureList.equals(other.specificMeasureList))
 			return false;
 		if (this.validityPeriod == null) {
 			if (other.validityPeriod != null)
 				return false;
 		} else if (!this.validityPeriod.equals(other.validityPeriod))
 			return false;
 		return true;
 	}
 }
