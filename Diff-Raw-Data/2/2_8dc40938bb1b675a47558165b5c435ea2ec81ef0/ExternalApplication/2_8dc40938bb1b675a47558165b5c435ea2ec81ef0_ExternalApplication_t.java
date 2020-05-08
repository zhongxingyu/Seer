 package fr.cg95.cvq.business.payment.external;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="external_application")
 public class ExternalApplication {
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
 
     private String label;
     private String description;
 
     @ElementCollection(fetch=FetchType.EAGER)
     @Column(name="broker")
     @CollectionTable(name="external_application_broker",joinColumns=@JoinColumn(name="external_application_id"))
     private Set<String> brokers = new HashSet<String>();
 
    @OneToMany(mappedBy="externalApplication",fetch=FetchType.LAZY)
     private List<ExternalHomeFolder> externalHomeFolders = new ArrayList<ExternalHomeFolder>();
 
     public ExternalApplication() {
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public Set<String> getBrokers() {
         return brokers;
     }
 
     public String getFormattedBrokers() {
         String result = "";
         if (brokers.size() == 0) return result;
         for (String broker : brokers)
             result += broker + " / ";
         return result.substring(0, result.length() - 2);
     }
 
     public void setBrokers(Set<String> brokers) {
         this.brokers = brokers;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     @OneToMany(cascade=CascadeType.ALL)
     @JoinColumn(name="external_application_id")
     @OrderColumn(name="external_home_application_index")
     public List<ExternalHomeFolder> getExternalHomeFolders() {
         return externalHomeFolders;
     }
 
     public void setExternalHomeFolders(List<ExternalHomeFolder> externalHomeFolders) {
         this.externalHomeFolders = externalHomeFolders;
     }
 
     @Override
     public boolean equals(Object object) {
         if (this == object)
             return true;
         if (!(object instanceof ExternalApplication))
             return false;
         ExternalApplication externalApplication = (ExternalApplication) object;
         return (this.getLabel().equals(externalApplication.getLabel()));
     }
 
     @Override
     public int hashCode() {
         return id == null ? System.identityHashCode(this) : id.hashCode();
     }
 
 }
