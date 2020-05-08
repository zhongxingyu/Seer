 package com.selfdiagnosis.core.entity;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.Length;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.Range;
 
 import com.selfdiagnosis.SelfDiagnosisConstants;
 
 /**
  * Disease entity.
  * 
  * @author mmieszkowski
  * 
  */
 @Entity
 @Table(name = "Disease")
 public class DiseaseEntity extends SelfDiagnosisEntity implements Serializable {
 
     /**
      * Serial.
      */
     private static final long serialVersionUID = -6865332512873021190L;
 
     /**
      * Primary key..
      */
     @Id
     @GeneratedValue
     @Column(name = "id", unique = true, nullable = false)
     private Long id;
 
     /**
      * Name of the disease.
      */
    @NotBlank (message = "{NotBlank.name}")
     @Length(max = SelfDiagnosisConstants.DISEASE_NAME_LENGTH_MAX)
     @Column(name = "name", unique = false, nullable = false)
     private String name;
 
     /**
      * Description of the disease.
      */
     @Column(name = "description", unique = false, nullable = true)
     private String description;
     
     /**
      * Frequency of disease.
      */
    @NotNull (message = "{NotBlank.frequency}")
     @Range(min = SelfDiagnosisConstants.ENTITY_FREQUENCY_MIN, 
             max = SelfDiagnosisConstants.ENTITY_FREQUENCY_MAX)
     @Column(name = "frequency", nullable = false)
     private Short frequency;
 
     /**
      * Specialties of doctors you should see having a disease.
      */
     @ManyToMany
     @JoinTable(name = "DiseaseDoctorSpecialty", joinColumns = @JoinColumn(name = "disease_id"),
             inverseJoinColumns = @JoinColumn(name = "doctorSpecialty_id"))
     private Collection<DoctorSpecialtyEntity> doctorSpecialtyCollection;
 
     /**
      * Treatments that should be applied when having a disease.
      */
     @ManyToMany
     @JoinTable(name = "DiseaseTreatment", joinColumns = @JoinColumn(name = "disease_id"),
             inverseJoinColumns = @JoinColumn(name = "treatment_id"))
     private Collection<TreatmentEntity> treatmentCollection;
 
     /**
      * Temporary field necessary for adding symptoms.
      */
     @Transient
     private DiseaseSymptomEntity diseaseSymptom = new DiseaseSymptomEntity();
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Short getFrequency() {
         return frequency;
     }
 
     public void setFrequency(Short frequncy) {
         this.frequency = frequncy;
     }
 
     public Collection<DoctorSpecialtyEntity> getDoctorSpecialtyCollection() {
         return doctorSpecialtyCollection;
     }
 
     public void setDoctorSpecialtyCollection(Collection<DoctorSpecialtyEntity> doctorSpecialtyCollection) {
         this.doctorSpecialtyCollection = doctorSpecialtyCollection;
     }
 
     public Collection<TreatmentEntity> getTreatmentCollection() {
         return treatmentCollection;
     }
 
     public void setTreatmentCollection(Collection<TreatmentEntity> treatmentCollection) {
         this.treatmentCollection = treatmentCollection;
     }
 
     public DiseaseSymptomEntity getDiseaseSymptom() {
         return diseaseSymptom;
     }
 
     public void setDiseaseSymptom(DiseaseSymptomEntity diseaseSymptom) {
         this.diseaseSymptom = diseaseSymptom;
     }
 }
