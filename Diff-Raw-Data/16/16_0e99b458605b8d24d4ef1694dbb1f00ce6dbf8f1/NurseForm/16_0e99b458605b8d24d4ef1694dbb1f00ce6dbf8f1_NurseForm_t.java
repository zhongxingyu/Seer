 package com.tda.model.nurse;
 
 import java.util.Collection;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 import javax.validation.constraints.Digits;
 
 import org.hibernate.annotations.CollectionOfElements;
 import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
 
 import com.tda.model.patient.Patient;
 
 @Entity
 public class NurseForm {
 	private Long id;
 
 	private Patient patient;
 
 	private Date fillingDate;
 
 	private Double size;
 
 	private Double weight;
 
 	private Double headCircumference;
 
 	private Double percentile;
 
 	private Double TAmin;
 	private Double TAmax;
 
 	private Double TempMin;
 	private Double TempMax;
 
 	@Digits(fraction = 0, integer = 4)
 	private Integer saturation;
 
 	// Vaxines
 	private Collection<Vaxine> vaxines;
 
 	// Nurse Actions
 	private Collection<NurseAction> nurseActions;
 
 	// TODO CURATION
 
 	// Observations
 	private String observations;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Long getId() {
 		return id;
 	}
 
 	@Transient
 	public boolean isNew() {
 		return id == null;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@ManyToOne
 	public Patient getPatient() {
 		return patient;
 	}
 
 	public void setPatient(Patient patient) {
 		this.patient = patient;
 	}
 
 	public Date getFillingDate() {
 		return fillingDate;
 	}
 
 	public void setFillingDate(Date fillingDate) {
 		this.fillingDate = fillingDate;
 	}
 
 	public Double getSize() {
 		return size;
 	}
 
 	public void setSize(Double size) {
 		this.size = size;
 	}
 
 	public Double getWeight() {
 		return weight;
 	}
 
 	public void setWeight(Double weight) {
 		this.weight = weight;
 	}
 
 	public Double getHeadCircumference() {
 		return headCircumference;
 	}
 
 	public void setHeadCircumference(Double headCircumference) {
 		this.headCircumference = headCircumference;
 	}
 
 	public Double getPercentile() {
 		return percentile;
 	}
 
 	public void setPercentile(Double percentile) {
 		this.percentile = percentile;
 	}
 
 	public Double getTAmin() {
 		return TAmin;
 	}
 
 	public void setTAmin(Double tAmin) {
 		TAmin = tAmin;
 	}
 
 	public Double getTAmax() {
 		return TAmax;
 	}
 
 	public void setTAmax(Double tAmax) {
 		TAmax = tAmax;
 	}
 
	@ManyToMany(targetEntity = Vaxine.class)
	@LazyCollection(LazyCollectionOption.FALSE)
 	@ForeignKey(name = "ID_NURSE_FORM", inverseName = "ID_VAXINE")
 	public Collection<Vaxine> getVaxines() {
 		return vaxines;
 	}
 
 	public void setVaxines(Collection<Vaxine> vaxines) {
 		this.vaxines = vaxines;
 	}
 
	@CollectionOfElements(targetElement = NurseAction.class)
	@LazyCollection(LazyCollectionOption.FALSE)
 	@JoinTable(name = "NURSE_ACTION", joinColumns = @JoinColumn(name = "NURSE_FORM_ID"))
 	@Column(name = "NURSE_ACTION", nullable = true)
 	@Enumerated(EnumType.STRING)
 	public Collection<NurseAction> getNurseActions() {
 		return nurseActions;
 	}
 
 	public void setNurseActions(Collection<NurseAction> nurseActions) {
 		this.nurseActions = nurseActions;
 	}
 
 	public String getObservations() {
 		return observations;
 	}
 
 	public void setObservations(String observations) {
 		this.observations = observations;
 	}
 
 	public Double getTempMin() {
 		return TempMin;
 	}
 
 	public void setTempMin(Double tempMin) {
 		TempMin = tempMin;
 	}
 
 	public Double getTempMax() {
 		return TempMax;
 	}
 
 	public void setTempMax(Double tempMax) {
 		TempMax = tempMax;
 	}
 
 	public Integer getSaturation() {
 		return saturation;
 	}
 
 	public void setSaturation(Integer saturation) {
 		this.saturation = saturation;
 	}
 }
