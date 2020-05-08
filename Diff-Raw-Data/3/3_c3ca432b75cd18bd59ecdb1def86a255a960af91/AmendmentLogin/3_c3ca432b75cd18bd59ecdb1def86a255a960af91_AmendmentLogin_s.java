 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 
 
 @Entity
 @Table (name = "amendment_logins")
 @GenericGenerator(name="id-generator", strategy = "native",
     parameters = {
         @Parameter(name="sequence", value="seq_amendment_logins_id")
     }
 )
 public class AmendmentLogin extends AbstractMutableDomainObject {
     private Integer studyId;
     private Integer amendmentNumber;
     private String date;
 
     public Integer getStudyId() {
         return studyId;
     }
 
 
     public void setStudyId(Integer studyId) {
         this.studyId = studyId;
     }
 
 
     public Integer getAmendmentNumber() {
         return amendmentNumber;
     }
 
 
     public void setAmendmentNumber(Integer amendmentNumber) {
         this.amendmentNumber = amendmentNumber;
     }
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
 
     @Override
     public String toString(){
         StringBuffer sb = new StringBuffer();
         sb.append(" StudyId = ");
         sb.append(getStudyId());
         sb.append(" AmendmentNumber = ");
         sb.append(getAmendmentNumber());
         sb.append(" Date = ");
         sb.append(getDate());
         return sb.toString();
     }
 }
