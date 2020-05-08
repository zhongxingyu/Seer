 package com.trekker.model;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 @Entity
 @Table(name = "trip")
 @XmlRootElement

 @NamedQueries({
     @NamedQuery(name = "Trip.list", query = "SELECT t FROM Trip t"),
     @NamedQuery(name = "Trip.findByKeyword", query = "SELECT t FROM Trip t WHERE t.name LIKE :name")
 })
 public class Trip implements Serializable {
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "tripId")
     @OrderBy("uploaded")
     private Collection<Media> mediaCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "tripId")
     private Collection<Accommodation> accommodationCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "tripId")
     private Collection<Itinerary> itineraryCollection;
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     @Basic(optional = false)
     @NotNull
    @Size(min = 1, max = 255, message="<div class=\"alert alert-error\">Name required</div>")
     @Column(name = "name")
     private String name;
     @Basic(optional = false)
     @NotNull
     @Column(name = "start_date")
     @Temporal(TemporalType.DATE)
     private Date startDate;
     @Basic(optional = false)
     @NotNull
     @Column(name = "end_date")
     @Temporal(TemporalType.DATE)
     private Date endDate;
     @Basic(optional = false)
     @NotNull
    @Size(min = 1, max = 255, message="<div class=\"alert alert-error\">Start location required</div>")
     @Column(name = "start_location")
     private String startLocation;
     @Basic(optional = false)
     @NotNull
    @Size(min = 1, max = 255, message="<div class=\"alert alert-error\">End location required</div>")
     @Column(name = "end_location")
     private String endLocation;
     @JoinColumn(name = "owner", referencedColumnName = "id")
     @ManyToOne(optional = false)
     private User owner;
 
     public Trip() {
     }
 
     public Trip(Integer id) {
         this.id = id;
     }
 
     public Trip(Integer id, String name, Date startDate, Date endDate, String startLocation) {
         this.id = id;
         this.name = name;
         this.startDate = startDate;
         this.endDate = endDate;
         this.startLocation = startLocation;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     public String getStartLocation() {
         return startLocation;
     }
 
     public void setStartLocation(String startLocation) {
         this.startLocation = startLocation;
     }
 
     public String getEndLocation() {
         return endLocation;
     }
 
     public void setEndLocation(String endLocation) {
         this.endLocation = endLocation;
     }
 
     public User getOwner() {
         return owner;
     }
 
     public void setOwner(User owner) {
         this.owner = owner;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Trip)) {
             return false;
         }
         Trip other = (Trip) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.trekker.model.Trip[ id=" + id + " ]";
     }
 
     @XmlTransient
     public Collection<Itinerary> getItineraryCollection() {
         return itineraryCollection;
     }
 
     public void setItineraryCollection(Collection<Itinerary> itineraryCollection) {
         this.itineraryCollection = itineraryCollection;
     }
 
     @XmlTransient
     public Collection<Accommodation> getAccommodationCollection() {
         return accommodationCollection;
     }
 
     public void setAccommodationCollection(Collection<Accommodation> accommodationCollection) {
         this.accommodationCollection = accommodationCollection;
     }
 
     @XmlTransient
     public Collection<Media> getMediaCollection() {
         return mediaCollection;
     }
 
     public void setMediaCollection(Collection<Media> mediaCollection) {
         this.mediaCollection = mediaCollection;
     }
    
   public Media getFirstPic() {
       if (mediaCollection.isEmpty())
       {return null;}
       else
       {return mediaCollection.iterator().next();}
   }
 }
