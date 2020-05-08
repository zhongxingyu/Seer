 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4chee.archive.persistence;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 
 import org.dcm4che.data.Attributes;
import org.dcm4che.data.ItemPointer;
 import org.dcm4che.data.PersonName;
 import org.dcm4che.data.Tag;
 import org.dcm4che.util.DateUtils;
 
 /**
  * @author Damien Evans <damien.daddy@gmail.com>
  * @author Justin Falk <jfalkmu@gmail.com>
  * @author Gunter Zeilinger <gunterze@gmail.com>
  */
 @NamedQueries({
 @NamedQuery(
     name="Series.findBySeriesInstanceUID",
     query="SELECT s FROM Series s WHERE s.seriesInstanceUID = ?1"),
 @NamedQuery(
     name="Series.findAttributesBySeriesPk",
     query="SELECT s.study.numberOfStudyRelatedSeries, " +
                  "s.study.numberOfStudyRelatedInstances, " +
                  "s.numberOfSeriesRelatedInstances, " +
                  "s.study.modalitiesInStudy, " +
                  "s.study.sopClassesInStudy, " +
                  "s.encodedAttributes, " +
                  "s.study.encodedAttributes, " +
                  "s.study.patient.encodedAttributes " +
           "FROM Series s WHERE s.pk = ?1"),
 @NamedQuery(
     name="Series.incNumberOfSeriesRelatedInstances",
     query="UPDATE Series s SET numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances + 1 WHERE s = ?1")
 })
 @Entity
 @Table(name = "series")
 public class Series implements Serializable {
 
     private static final long serialVersionUID = -8317105475421750944L;
 
     public static final String FIND_BY_SERIES_INSTANCE_UID =
             "Series.findBySeriesInstanceUID";
     public static final String FIND_ATTRIBUTES_BY_SERIES_PK =
             "Series.findAttributesBySeriesPk";
     public static final String INC_NUMBER_OF_SERIES_RELATED_INSTANCES =
             "Series.incNumberOfSeriesRelatedInstances";
 
     @Id
     @GeneratedValue
     @Column(name = "pk")
     private long pk;
 
     @Basic(optional = false)
     @Column(name = "created_time", updatable = false)
     private Date createdTime;
 
     @Basic(optional = false)
     @Column(name = "updated_time")
     private Date updatedTime;
 
     @Basic(optional = false)
     @Column(name = "series_iuid", updatable = false)
     private String seriesInstanceUID;
 
     @Basic(optional = false)
     @Column(name = "series_no")
     private String seriesNumber;
 
     @Basic(optional = false)
     @Column(name = "series_desc")
     private String seriesDescription;
 
     @Basic(optional = false)
     @Column(name = "modality")
     private String modality;
 
     @Basic(optional = false)
     @Column(name = "department")
     private String institutionalDepartmentName;
 
     @Basic(optional = false)
     @Column(name = "institution")
     private String institutionName;
 
     @Basic(optional = false)
     @Column(name = "station_name")
     private String stationName;
 
     @Basic(optional = false)
     @Column(name = "body_part")
     private String bodyPartExamined;
 
     @Basic(optional = false)
     @Column(name = "laterality")
     private String laterality;
 
     @Basic(optional = false)
     @Column(name = "perf_physician")
     private String performingPhysicianName;
     
     @Basic(optional = false)
     @Column(name = "perf_phys_fn_sx")
     private String performingPhysicianFamilyNameSoundex;
     
     @Basic(optional = false)
     @Column(name = "perf_phys_gn_sx")
     private String performingPhysicianGivenNameSoundex;
 
     @Basic(optional = false)
     @Column(name = "perf_phys_i_name")
     private String performingPhysicianIdeographicName;
 
     @Basic(optional = false)
     @Column(name = "perf_phys_p_name")
     private String performingPhysicianPhoneticName;
 
     @Basic(optional = false)
     @Column(name = "pps_start_date")
     private String performedProcedureStepStartDate;
 
     @Basic(optional = false)
     @Column(name = "pps_start_time")
     private String performedProcedureStepStartTime;
 
     @Basic(optional = false)
     @Column(name = "pps_iuid")
     private String performedProcedureStepInstanceUID;
 
     @Basic(optional = false)
     @Column(name = "series_custom1")
     private String seriesCustomAttribute1;
 
     @Basic(optional = false)
     @Column(name = "series_custom2")
     private String seriesCustomAttribute2;
 
     @Basic(optional = false)
     @Column(name = "series_custom3")
     private String seriesCustomAttribute3;
 
     @Basic(optional = false)
     @Column(name = "num_instances", updatable = false)
     private int numberOfSeriesRelatedInstances;
 
     @Column(name = "src_aet")
     private String sourceAET;
 
     @Column(name = "retrieve_aets")
     private String retrieveAETs;
 
     @Column(name = "ext_retr_aet")
     private String externalRetrieveAET;
 
     @Basic(optional = false)
     @Column(name = "availability")
     private Availability availability;
 
     @Basic(optional = false)
     @Column(name = "series_attrs")
     private byte[] encodedAttributes;
 
     @Override
     public String toString() {
         return "Series[pk=" + pk
                 + ", uid=" + seriesInstanceUID
                 + ", no=" + seriesNumber
                 + ", mod=" + modality
                 + ", numI=" + numberOfSeriesRelatedInstances
                 + "]";
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "inst_code_fk")
     private Code institutionCode;
 
     @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
     @JoinColumn(name = "series_fk")
     private Collection<RequestAttributes> requestAttributes;
 
     @ManyToOne
     @JoinColumn(name = "study_fk")
     private Study study;
 
     @OneToMany(mappedBy = "series", orphanRemoval = true)
     private Collection<Instance> instances;
 
     @PrePersist
     public void onPrePersist() {
         Date now = new Date();
         createdTime = now;
         updatedTime = now;
     }
 
     @PreUpdate
     public void onPreUpdate() {
         updatedTime = new Date();
     }
 
     public Attributes getAttributes() throws IOException {
         return Utils.decodeAttributes(encodedAttributes);
     }
 
     public long getPk() {
         return pk;
     }
 
     public Date getCreatedTime() {
         return createdTime;
     }
 
     public Date getUpdatedTime() {
         return updatedTime;
     }
 
     public String getSeriesInstanceUID() {
         return seriesInstanceUID;
     }
 
     public String getSeriesNumber() {
         return seriesNumber;
     }
 
     public String getSeriesDescription() {
         return seriesDescription;
     }
 
     public String getModality() {
         return modality;
     }
 
     public String getInstitutionalDepartmentName() {
         return institutionalDepartmentName;
     }
 
     public String getInstitutionName() {
         return institutionName;
     }
 
     public String getStationName() {
         return stationName;
     }
 
     public String getBodyPartExamined() {
         return bodyPartExamined;
     }
 
     public String getLaterality() {
         return laterality;
     }
 
     public String getPerformingPhysicianName() {
         return performingPhysicianName;
     }
 
     public String getPerformingPhysicianFamilyNameSoundex() {
         return performingPhysicianFamilyNameSoundex;
     }
 
     public String getPerformingPhysicianGivenNameSoundex() {
         return performingPhysicianGivenNameSoundex;
     }
 
     public String getPerformingPhysicianIdeographicName() {
         return performingPhysicianIdeographicName;
     }
 
     public String getPerformingPhysicianPhoneticName() {
         return performingPhysicianPhoneticName;
     }
 
     public String getPerformedProcedureStepStartDate() {
         return performedProcedureStepStartDate;
     }
 
     public String getPerformedProcedureStepStartTime() {
         return performedProcedureStepStartTime;
     }
 
     public String getPerformedProcedureStepInstanceUID() {
         return performedProcedureStepInstanceUID;
     }
 
     public String getSeriesCustomAttribute1() {
         return seriesCustomAttribute1;
     }
 
     public String getSeriesCustomAttribute2() {
         return seriesCustomAttribute2;
     }
 
     public String getSeriesCustomAttribute3() {
         return seriesCustomAttribute3;
     }
 
     public int getNumberOfSeriesRelatedInstances() {
         return numberOfSeriesRelatedInstances;
     }
 
     public void setNumberOfSeriesRelatedInstances(int numberOfSeriesRelatedInstances) {
         this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
     }
 
     public void incNumberOfSeriesRelatedInstances() {
         numberOfSeriesRelatedInstances++;
     }
 
     public String getSourceAET() {
         return sourceAET;
     }
 
     public void setSourceAET(String sourceAET) {
         this.sourceAET = sourceAET;
     }
 
     public String getRetrieveAETs() {
         return retrieveAETs;
     }
 
     public void setRetrieveAETs(String retrieveAETs) {
         this.retrieveAETs = retrieveAETs;
     }
 
     public String getExternalRetrieveAET() {
         return externalRetrieveAET;
     }
 
     public void setExternalRetrieveAET(String externalRetrieveAET) {
         this.externalRetrieveAET = externalRetrieveAET;
     }
 
     public Availability getAvailability() {
         return availability;
     }
 
     public void setAvailability(Availability availability) {
         this.availability = availability;
     }
 
     public byte[] getEncodedAttributes() {
         return encodedAttributes;
     }
 
     public Code getInstitutionCode() {
         return institutionCode;
     }
 
     public void setInstitutionCode(Code institutionCode) {
         this.institutionCode = institutionCode;
     }
 
     public Collection<RequestAttributes> getRequestAttributes() {
         return requestAttributes;
     }
 
     public void setRequestAttributes(Collection<RequestAttributes> requestAttributes) {
         this.requestAttributes = requestAttributes;
     }
 
     public Study getStudy() {
         return study;
     }
 
     public void setStudy(Study study) {
         this.study = study;
     }
 
     public Collection<Instance> getInstances() {
         return instances;
     }
 
     public void setAttributes(Attributes attrs) {
         seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID, null);
         seriesNumber = AttributeFilter.getString(attrs, Tag.SeriesNumber);
         seriesDescription =
             AttributeFilter.getString(attrs, Tag.SeriesDescription);
         institutionName = AttributeFilter.getString(attrs, Tag.InstitutionName);
         institutionalDepartmentName =
             AttributeFilter.getString(attrs, Tag.InstitutionalDepartmentName);
         modality = AttributeFilter.getString(attrs, Tag.Modality);
         stationName = AttributeFilter.getString(attrs, Tag.StationName);
         bodyPartExamined = AttributeFilter.getString(attrs, Tag.BodyPartExamined);
         laterality = AttributeFilter.getString(attrs, Tag.Laterality);
        performedProcedureStepInstanceUID =
            attrs.getNestedString(Tag.ReferencedSOPInstanceUID, "*",
                    new ItemPointer(Tag.ReferencedPerformedProcedureStepSequence));
         Date dt = attrs.getDate(Tag.PerformedProcedureStepStartDateAndTime, null);
         if (dt != null) {
             performedProcedureStepStartDate = DateUtils.formatDA(null, dt);
             performedProcedureStepStartTime = 
                 attrs.containsValue(Tag.PerformedProcedureStepStartDate)
                     ? DateUtils.formatTM(null, dt)
                     : "*";
         } else {
             performedProcedureStepStartDate = "*";
             performedProcedureStepStartTime = "*";
         }
         String s = AttributeFilter.getString(attrs, Tag.PerformingPhysicianName);
         if (s.equals("*")) {
             performingPhysicianName = "*";
             performingPhysicianIdeographicName = "*";
             performingPhysicianPhoneticName = "*";
             performingPhysicianFamilyNameSoundex = "*";
             performingPhysicianGivenNameSoundex = "*";
         } else {
             PersonName pn = new PersonName(s, true);
             performingPhysicianName =
                     pn.getNormalizedString(PersonName.Group.Alphabetic, "*");
             performingPhysicianIdeographicName =
                     pn.getNormalizedString(PersonName.Group.Ideographic, "*");
             performingPhysicianPhoneticName =
                     pn.getNormalizedString(PersonName.Group.Phonetic, "*");
             //TODO
             performingPhysicianFamilyNameSoundex = "*";
             performingPhysicianGivenNameSoundex = "*";
         }
         //TODO
         seriesCustomAttribute1 = "*";
         seriesCustomAttribute2 = "*";
         seriesCustomAttribute3 = "*";
 
         encodedAttributes = Utils.encodeAttributes(attrs,
                 AttributeFilter.seriesFilter);
         
     }
 }
