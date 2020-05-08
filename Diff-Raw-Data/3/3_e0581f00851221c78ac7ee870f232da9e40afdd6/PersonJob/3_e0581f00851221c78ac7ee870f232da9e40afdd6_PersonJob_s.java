 package com.seitenbau.testing.personmanager;
 
 import java.util.Date;
 
 import javax.persistence.AssociationOverride;
 import javax.persistence.AssociationOverrides;
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 @Entity
 @Table(name = "person_job")
 @AssociationOverrides({
     @AssociationOverride(name = "pk.person", joinColumns = @JoinColumn(name = "person_id")),
     @AssociationOverride(name = "pk.job", joinColumns = @JoinColumn(name = "job_id"))
   })
 public class PersonJob
 {
 
   private PersonJobPK pk = new PersonJobPK();
 
   @EmbeddedId
   public PersonJobPK getPk()
   {
     return pk;
   }
 
   public void setPk(PersonJobPK pk)
   {
     this.pk = pk;
   }
 
   @Transient
   public Person getPerson(){
    return this.pk.getPerson();
   }
 
   @Transient
   public Job getJob(){
    return this.pk.getJob();
   }
 
   public void setPerson(Person person){
       this.pk.setPerson(person);
   }
 
   public void setJob(Job job){
       this.pk.setJob(job);
   }
 
  @Column
   private Date engagementStart;
 
   public Date getEngagementStart()
   {
     return engagementStart;
   }
 
   public void setEngagementStart(Date engagementStart)
   {
     this.engagementStart = engagementStart;
   }
 
 }
