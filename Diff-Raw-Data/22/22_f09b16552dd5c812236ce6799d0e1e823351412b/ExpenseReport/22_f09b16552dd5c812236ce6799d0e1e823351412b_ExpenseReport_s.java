 package cz.muni.fi.pv243.et.model;
 
 import org.hibernate.annotations.LazyCollection;
 import org.hibernate.annotations.LazyCollectionOption;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.Indexed;
 import org.hibernate.search.annotations.IndexedEmbedded;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 
 @Entity
 @Indexed
 public class ExpenseReport implements Serializable {
 
     @Id
     @GeneratedValue
     private Long id;
 
     @NotNull
     private String name;
 
     private String description;
 
     private Boolean isSelected;
 
     public ExpenseReport() {
     }
     
     public ExpenseReport(String name, String description, Person submitter, Person verifier, ReportStatus status) {
         this.name = name;
         this.description = description;
         this.submitter = submitter;
         this.verifier = verifier;
         this.status = status;
     }
     
     @NotNull
     @ManyToOne
     @IndexedEmbedded
     private Person submitter;
 
     @ManyToOne(cascade= CascadeType.MERGE)
     @IndexedEmbedded
     private Person verifier;
 
     @OneToMany(mappedBy = "report", cascade = CascadeType.MERGE)
     @LazyCollection(LazyCollectionOption.FALSE)
     private List<Payment> payments;
 
     @OneToMany(mappedBy = "report", cascade = CascadeType.MERGE)
     @LazyCollection(LazyCollectionOption.FALSE)
     private List<MoneyTransfer> moneyTransfers;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastSubmittedDate;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date approvedDate;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastChangeDate;
 
     @Field
     @Enumerated(EnumType.ORDINAL)
     private ReportStatus status;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Person getSubmitter() {
         return submitter;
     }
 
     public void setSubmitter(Person submitter) {
         this.submitter = submitter;
     }
 
     public Person getVerifier() {
         return verifier;
     }
 
     public void setVerifier(Person verifier) {
         this.verifier = verifier;
     }
 
     public List<Payment> getPayments() {
         return payments;
     }
 
     public void setPayments(List<Payment> payments) {
         this.payments = payments;
     }
 
     public List<MoneyTransfer> getMoneyTransfers() {
         return moneyTransfers;
     }
 
     public void setMoneyTransfers(List<MoneyTransfer> moneyTransfers) {
         this.moneyTransfers = moneyTransfers;
     }
 
     public Date getLastSubmittedDate() {
         return lastSubmittedDate;
     }
 
     public void setLastSubmittedDate(Date lastSubmittedDate) {
         this.lastSubmittedDate = lastSubmittedDate;
     }
 
     public Date getApprovedDate() {
         return approvedDate;
     }
 
     public void setApprovedDate(Date approvedDate) {
         this.approvedDate = approvedDate;
     }
 
     public ReportStatus getStatus() {
         return status;
     }
 
     public void setStatus(ReportStatus status) {
         this.status = status;
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
 
     public Date getLastChangeDate() {
         return lastChangeDate;
     }
 
     public void setLastChangeDate(Date lastChangeDate) {
         this.lastChangeDate = lastChangeDate;
     }
 
     public Boolean getSelected() {
         return isSelected;
     }
 
     public void setSelected(Boolean selected) {
         isSelected = selected;
     }
 
     @Override
     public final boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof ExpenseReport)) return false;
 
         ExpenseReport report = (ExpenseReport) o;
 
         if (getId() != null ? !getId().equals(report.getId()) : report.getId() != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return getId() != null ? getId().hashCode() : 0;
     }
 
     @Override
     public String toString() {
         return "ExpenseReport{" +
                 "id=" + id +
                 ", name='" + name + '\'' +
                 ", description='" + description + '\'' +
                 ", submitter=" + submitter +
                 ", verifier=" + verifier +
                 ", payments=" + payments +
                 ", moneyTransfers=" + moneyTransfers +
                 ", lastSubmittedDate=" + lastSubmittedDate +
                 ", approvedDate=" + approvedDate +
                 ", lastChangeDate=" + lastChangeDate +
                 ", status=" + status +
                 '}';
     }
 }
