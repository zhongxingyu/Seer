 package org.hyperpath.persistence.entities;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 @Entity
 @Table(name = "services", catalog = "hyperPath", schema = "")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Services.findAll"             , query = "SELECT s FROM Services s"),
     @NamedQuery(name = "Services.findById"            , query = "SELECT s FROM Services s WHERE s.Id.id = :id"),
     @NamedQuery(name = "Services.findByLabel"         , query = "SELECT s FROM Services s WHERE s.label = :label"),
     @NamedQuery(name = "Services.findByDescription"   , query = "SELECT s FROM Services s WHERE s.description = :description"),
     @NamedQuery(name = "Services.findByCategoriesId"  , query = "SELECT s FROM Services s WHERE s.Id.categoriesId = :categoriesId"),
     @NamedQuery(name = "Services.findByLocation"      , query = "SELECT s FROM Services s WHERE s.location = :location"),
     @NamedQuery(name = "Services.findByUsersReview"   , query = "SELECT s FROM Services s WHERE s.usersReview = :usersReview"),
     @NamedQuery(name = "Services.findByEntitiesId"    , query = "SELECT s FROM Services s WHERE s.Id.entitiesId = :entitiesId"),
     @NamedQuery(name = "Services.findByOpeningHoursid", query = "SELECT s FROM Services s WHERE s.Id.openingHoursid = :openingHoursid"),
     @NamedQuery(name = "Services.findByRating"        , query = "SELECT s FROM Services s WHERE s.rating = :rating")})
 public class Services implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @NotNull
     @Column(name = "id", nullable = false)
     protected Integer Id;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 45)
     @Column(name = "label", nullable = false, length = 45)
     private String label;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 45)
     @Column(name = "description", nullable = false, length = 45)
     private String description;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 45)
     @Column(name = "usersReview", nullable = false, length = 45)
     private String usersReview;
     @Basic(optional = false)
     @NotNull
     @Column(name = "rating", nullable = false)
     private int rating;
     @ManyToMany(mappedBy = "servicesList")
     private List<Clients> clientsList;
     @JoinTable(name = "services_has_reviews", 
         joinColumns        = {@JoinColumn(name = "services_id", referencedColumnName = "id", nullable = false)}, 
         inverseJoinColumns = {@JoinColumn(name = "reviews_id" , referencedColumnName = "id", nullable = false)})
     @ManyToMany
     private List<Reviews> reviewsList;
     @JoinColumn(name = "openingHours_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
     @ManyToOne(optional = false)
     private OpeningHours openingHours;
     @JoinColumn(name = "entities_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
     @ManyToOne(optional = false)
     private Entities entities;
     @JoinColumn(name = "categories_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
     @ManyToOne(optional = false)
     private Categories categories;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "services")
     private List<Ads> adsList;
     @JoinColumn(name = "gpslocation_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Gpslocation gpsLocation;
     
     public Services() {
     }
 
     public Services(Integer Id) {
         this.Id = Id;
     }
 
    public Services(Integer Id, String label, String description, Gpslocation gpsLocation, String usersReview, int rating) {
         this.Id = Id;
         this.label = label;
         this.description = description;
        this.gpsLocation = gpsLocation;
         this.usersReview = usersReview;
         this.rating = rating;
     }
 
     public Integer getServicesPK() {
         return Id;
     }
 
     public void setServicesPK(Integer Id) {
         this.Id = Id;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getUsersReview() {
         return usersReview;
     }
 
     public void setUsersReview(String usersReview) {
         this.usersReview = usersReview;
     }
 
     public int getRating() {
         return rating;
     }
 
     public void setRating(int rating) {
         this.rating = rating;
     }
 
     public Gpslocation getLocation(){
        return this.gpsLocation;
     }
     
    public void setLocation(Gpslocation gpsLocation) {
        this.gpsLocation = gpsLocation;
     }
     
     
     @XmlTransient
     public List<Clients> getClientsList() {
         return clientsList;
     }
 
     public void setClientsList(List<Clients> clientsList) {
         this.clientsList = clientsList;
     }
 
     @XmlTransient
     public List<Reviews> getReviewsList() {
         return reviewsList;
     }
 
     public void setReviewsList(List<Reviews> reviewsList) {
         this.reviewsList = reviewsList;
     }
 
     public OpeningHours getOpeningHours() {
         return openingHours;
     }
 
     public void setOpeningHours(OpeningHours openingHours) {
         this.openingHours = openingHours;
     }
 
     public Entities getEntities() {
         return entities;
     }
 
     public void setEntities(Entities entities) {
         this.entities = entities;
     }
 
     public Categories getCategories() {
         return categories;
     }
 
     public void setCategories(Categories categories) {
         this.categories = categories;
     }
 
     @XmlTransient
     public List<Ads> getAdsList() {
         return adsList;
     }
 
     public void setAdsList(List<Ads> adsList) {
         this.adsList = adsList;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (Id != null ? Id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Services)) {
             return false;
         }
         Services other = (Services) object;
         if ((this.Id == null && other.Id != null) || (this.Id != null && !this.Id.equals(other.Id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "org.hyperpath.persistence.entities.Services[ Id=" + Id + " ]";
     }
     
 }
