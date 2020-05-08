 package models;
 
 import org.hibernate.annotations.GenericGenerator;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 @Table(name = "Project")
 public class Project {
 
     @Id
     @GeneratedValue(generator = "projectId")
     @GenericGenerator(name = "projectId", strategy = "increment")
     private long id;
 
     @Column(nullable = false)
     private String name;
 
     @Column(nullable = false)
     private Date creationDate;
 
     @Column(nullable = false)
     private Date endDate;
 
     @Column(nullable = true)
     private Double targetAmount;
 
     @Column (nullable = false ,columnDefinition = "LONGTEXT" ) 
     private String description;
 
     @Column(nullable = false)
     private String image;
 
     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private ProjectStatus status;
 
     @Column(nullable = false)
     private String thumbnail;
 
     @Column(nullable = false)
     private String summary;
    
     @OneToMany(mappedBy = "project")
     private List<Donation> donations;
 
     @Column(nullable = false)
     private long charityId;
 
     public Project(long id, String name, Date creationDate, Date endDate, Double targetAmount, ProjectStatus status, String summary, String description, String image, String thumbnail, long charityId) {
         this.id = id;
         this.name = name;
         this.creationDate = creationDate;
         this.endDate = endDate;
         this.targetAmount = targetAmount;
         this.description = description;
         this.image = image;
         this.status = status;
         this.thumbnail = thumbnail;
         this.summary = summary;
         this.donations = new ArrayList<Donation>();
         this.charityId = charityId;
     }
 
     public Project(String name, String description, String image, Date creationDate, Date endDate, Double targetAmount) {
         this(0, name, creationDate, endDate, targetAmount, ProjectStatus.CURRENT, "", description, image, "", 655);
     }
 
     public Project(String name, String description, String image, ProjectStatus status, String thumbnail, String summary, long charityId) {
         this.name=name;
         this.description = description;
         this.image = image;
         this.status = status;
         this.thumbnail = thumbnail;
         this.summary = summary;
         this.donations = new ArrayList<Donation>();
         this.charityId = charityId;
     }
 
 
     public Project() {
         this.donations = new ArrayList<Donation>();
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public Double getTargetAmount() {
         return targetAmount;
     }
 
     public String getDescription() {
         return description;
     }
 
     public String getImage() {
         return image;
     }
 
     public String getThumbnail() {
         return thumbnail;
     }
 
     public String getSummary() {
         return summary;
     }
 
     public long getCharityId() {
         return charityId;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Project project = (Project) o;
 
         if (description != null ? !description.equals(project.description) : project.description != null) return false;
         if (name != null ? !name.equals(project.name) : project.name != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name != null ? name.hashCode() : 0;
         result = 31 * result + (description != null ? description.hashCode() : 0);
         return result;
     }
 
 
     public Double totalDonations() {
         Double totalAmount = 0.0;
         for (Donation donation : donations) {
                 totalAmount += donation.getAmount();
         }
             return totalAmount;
     }
 
     public void addDonation(Donation donation) {
         donations.add(donation);
     }
 }
