 package com.credera.trails.model;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 import javax.validation.Valid;
 import javax.validation.constraints.Size;
 
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.springframework.beans.support.MutableSortDefinition;
 import org.springframework.beans.support.PropertyComparator;
 
 import com.credera.trails.util.UrlUtils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 @Entity
 public class Route {
 
     @Id
     @GeneratedValue(strategy=GenerationType.AUTO)
     private Long id;
 
 	@Column(length = 128, nullable = false)
     @Size(min=0,max=128)
 	private String name;
 
     @Column(length = 128, nullable = false, name="url_friendly_name")
     private String urlFriendlyName;
 
     @Column(nullable = false)
     private String description;
 
 	@Column(nullable = false, name="distance_in_miles")
 	private Double distanceInMiles;
 
     @Column(nullable = false)
     private String location;
 
     @ManyToOne
     private Region region;
 
     @Valid
     @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "route")
     //@OrderColumn(name = "id")
     @Fetch (FetchMode.SELECT)
     private List<Direction> directions = new ArrayList<Direction>();
 
	@OneToMany(mappedBy = "route", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
     //@OrderColumn(name = "id")
 	@Fetch (FetchMode.SELECT)
 	private Set<Rating> ratings  = new HashSet<Rating>();
 
     @OneToMany(mappedBy = "route", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
     //@OrderColumn(name = "id")
 	//@Fetch (FetchMode.SELECT)
     private Set<Comment> comments = new HashSet<Comment>();
 
     @Column(name="creation_date")
     private Date creationDate;
 
     public Route() {
         this.creationDate = new Date();
     }
 
     public Route(String name, String description, Double distanceInMiles, Region region, String location) {
         setName(name);
         this.description = description;
         this.distanceInMiles = distanceInMiles;
         this.region = region;
         this.location = location;
         this.creationDate = new Date();
     }
     
     /**
 	 * @return the id
 	 */
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
         this.urlFriendlyName = UrlUtils.getUrlFriendlyName(name);
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the distanceInMiles
 	 */
 	public Double getDistanceInMiles() {
 		return distanceInMiles;
 	}
 
 	/**
 	 * @param distanceInMiles the distanceInMiles to set
 	 */
 	public void setDistanceInMiles(Double distanceInMiles) {
 		this.distanceInMiles = distanceInMiles;
 	}
 
 	/**
 	 * @return the location
 	 */
 	public String getLocation() {
 		return location;
 	}
 
 	/**
 	 * @param location the location to set
 	 */
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	/**
 	 * @return the region
 	 */
 	public Region getRegion() {
 		return region;
 	}
 
 	/**
 	 * @param region the region to set
 	 */
 	public void setRegion(Region region) {
 		this.region = region;
 	}
 
 	/**
 	 * @return the directions
 	 */
 	public List<Direction> getDirections() {
 		return directions;
 	}
 //	public List<Direction> getDirections() {
 //		List<Direction> sortedPets = new ArrayList<Direction>(this.directions);
 //		PropertyComparator.sort(sortedPets, new MutableSortDefinition("stepNumber", true, true));
 //		return Collections.unmodifiableList(sortedPets);
 //	}
 
 	/**
 	 * @param directions the directions to set
 	 */
 	public void setDirections(List<Direction> directions) {
 		this.directions = directions;
 	}
 
 	/**
 	 * @return the ratings
 	 */
 	public Set<Rating> getRatings() {
 		return ratings;
 	}
 
 	/**
 	 * @param ratings the ratings to set
 	 */
 	public void setRatings(Set<Rating> ratings) {
 		this.ratings = ratings;
 	}
 
 	/**
 	 * @return the comments
 	 */
 	public Set<Comment> getComments() {
 		return comments;
 	}
 
 	/**
 	 * @param comments the comments to set
 	 */
 	public void setComments(Set<Comment> comments) {
 		this.comments = comments;
 	}
 
 	/**
 	 * @return the creationDate
 	 */
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	/**
 	 * @param creationDate the creationDate to set
 	 */
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	/**
 	 * @return the urlFriendlyName
 	 */
 	public String getUrlFriendlyName() {
 		return urlFriendlyName;
 	}
 
 	/**
 	 * @param urlFriendlyName the urlFriendlyName to set
 	 */
 	public void setUrlFriendlyName(String urlFriendlyName) {
 		this.urlFriendlyName = urlFriendlyName;
 	}
 
 	public Integer getAverageRating() {
         if (ratings.size() == 0) {
             return 0;
         }
         
         Integer ratingTotal = 0;
         
         for (Rating rating : ratings) {
             ratingTotal += rating.getValue();
         }
         
         return ratingTotal / ratings.size();
     }
 
 }
