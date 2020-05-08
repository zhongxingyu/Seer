 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.*;
 
 import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 
 import com.avaje.ebean.ExpressionList;
 
 import enums.MementoCategory;
 
 import play.db.ebean.Model;
 import pojos.LocationMinimalBean;
 
 @Entity
 @Table(name="Famous_Person")
 public class FamousPerson extends Model {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7758981950323371564L;
 
 	@Id
     @GeneratedValue
     @Column(name="famous_id")
     private Long famousId;
 	
 	@Column
 	private String fullname;
 	
 	@Column
 	private String firstname;
 	
 	@Column
 	private String lastname;
 	
 	@Column(name="famous_for")
 	private String famousFor;
 
 	@Column
 	private String source;
 
 	@Column(name="source_url")
 	private String sourceUrl;
 
 	@Column
 	private String locale;
 
 	@Column(name="picture_url")
 	private String resourceUrl;
 
 	@Column
 	private String status;
 
 	@Column
 	private String tags;
 
 	@Column(name="creator_type")
 	private String creatorType;
 
 	@Temporal(TemporalType.DATE)
 	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
 	@Column(name="creation_date")
 	private DateTime creationDate;
 
 	@Temporal(TemporalType.DATE)
 	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
 	@Column(name="last_update")
 	private DateTime lastUpdate;
 
 	@Column
 	private boolean indexed;
 	
 
     @Column (name="resource_type")
     private String resourceType;
     @Column
     private String category;
 	
 	@ManyToOne
 	@MapsId
     @JoinColumn(name="birthdate_fuzzy_id")
 	private FuzzyDate startDate;
 
 	@ManyToOne
 	@MapsId
     @JoinColumn(name="deathdate_fuzzy_id")
 	private FuzzyDate endDate;
 	
 	@ManyToOne
 	@MapsId
     @JoinColumn(name="birthplace_id")
 	private Location startLocation;
 	
 	@ManyToOne
 	@MapsId
     @JoinColumn(name="deathplace_id")
 	private Location endLocation;
 	
 	@ManyToOne
 	@MapsId
     @JoinColumn(name="nationality_country_id")
 	private Country country;
 	
 	
 	public static Model.Finder<Long,FamousPerson> find = new Finder<Long, FamousPerson>(
             Long.class,FamousPerson.class
     );
     
     public static List<FamousPerson> all(){
         return find.all();
     }
     
     public static void create(FamousPerson famousPerson){
         famousPerson.save();
     }
     
     public static FamousPerson createObject(FamousPerson famousPerson){
         famousPerson.save();
         return famousPerson;
     }
     
     public static void delete(Long id){
         find.ref(id).delete();
     }
     
     public static FamousPerson read(Long id){
         return find.byId(id);
     }
 
 	public Long getFamousId() {
 		return famousId;
 	}
 
 	public void setFamousId(Long famousId) {
 		this.famousId = famousId;
 	}
 
 	public String getFullname() {
 		return fullname;
 	}
 
 	public void setFullname(String fullname) {
 		this.fullname = fullname;
 	}
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 
 	public String getLastname() {
 		return lastname;
 	}
 
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	public String getFamousFor() {
 		return famousFor;
 	}
 
 	public void setFamousFor(String famousFor) {
 		this.famousFor = famousFor;
 	}
 
 	public String getSource() {
 		return source;
 	}
 
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	public String getSourceUrl() {
 		return sourceUrl;
 	}
 
 	public void setSourceUrl(String sourceUrl) {
 		this.sourceUrl = sourceUrl;
 	}
 
 	public String getLocale() {
 		return locale;
 	}
 
 	public void setLocale(String locale) {
 		this.locale = locale;
 	}
 
 	public String getResourceUrl() {
 		return resourceUrl;
 	}
 
 	public void setResourceUrl(String pictureUrl) {
 		this.resourceUrl = pictureUrl;
 	}
 
 	public String getStatus() {
 		return status;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	public String getCreatorType() {
 		return creatorType;
 	}
 
 	public void setCreatorType(String creatorType) {
 		this.creatorType = creatorType;
 	}
 
 	public DateTime getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(DateTime dateAdded) {
 		this.creationDate = dateAdded;
 	}
 
 	public DateTime getLastUpdate() {
 		return lastUpdate;
 	}
 
 	public void setLastUpdate(DateTime lastUpdate) {
 		this.lastUpdate = lastUpdate;
 	}
 
 	public boolean isIndexed() {
 		return indexed;
 	}
 
 	public void setIndexed(boolean indexed) {
 		this.indexed = indexed;
 	}
 
 	public FuzzyDate getStartBirthDate() {
 		return startDate;
 	}
 
 	public void setStartBirthDate(FuzzyDate birthDate) {
 		this.startDate = birthDate;
 	}
 
 	public FuzzyDate getEndDeathDate() {
 		return endDate;
 	}
 
 	public void setEndDeathDate(FuzzyDate deathDate) {
 		this.endDate = deathDate;
 	}
 
 	public Location getStartLocation() {
 		return startLocation;
 	}
 
 	public void setStartLocation(Location birhplace) {
 		this.startLocation = birhplace;
 	}
 
 	public Location getEndLocation() {
 		return endLocation;
 	}
 
 	public void setEndLocation(Location deathplace) {
 		this.endLocation = deathplace;
 	}
 
 	public Country getCountry() {
 		return country;
 	}
 
 	public void setCountry(Country country) {
 		this.country = country;
 	}
 
 	public String getTags() {
 		return tags;
 	}
 
 	public void setTags(String tags) {
 		this.tags = tags;
 	}
 
 	public String getResourceType() {
 		return resourceType;
 	}
 
 	public void setResourceType(String resourceType) {
 		this.resourceType = resourceType;
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public static List<FamousPerson> readForContext(String locale,
 			MementoCategory category, Long decade,
 			List<LocationMinimalBean> locations, String level,
 			int itemsPerLevel) {
 		
 		List<FamousPerson> result = new ArrayList<FamousPerson>();
 		ExpressionList<FamousPerson> el = find.where();
 		
 		if (level.equals("WORLD")) {
 			el.eq("locale", locale)
 				.eq("category", category.toString())
				.eq("startDate.decade",decade)
 				.orderBy("rand()")
 				.setMaxRows(itemsPerLevel);
 				result.addAll(el.findList());
 		} else if (level.equals("COUNTRY")) {
 			List<String> countries = new ArrayList<String>();
 			for (LocationMinimalBean loc : locations) {
 				String country = loc.getCountry();
 				countries.add(country);
 			}
 			el.eq("locale", locale)
 			.eq("category", category.toString())
 			.eq("startDate.decade",decade)
 			.in("startLocatoin.country", countries)
 			.orderBy("rand()")
 			.setMaxRows(itemsPerLevel);	
 			result.addAll(el.findList());
 		} else if (level.equals("REGION")) {
 			for (LocationMinimalBean loc : locations) {
 				el.eq("locale", locale)
 				.eq("category", category.toString())
 				.eq("startDate.decade",decade);
 				
 				String country = loc.getCountry();
 				if (country != null && !country.isEmpty()) {
 					el.eq("startLocation.country", country);
 					String region = loc.getRegion();
 					if (region != null && !region.isEmpty()) {
 						el.eq("startLocation.region", region);
 						el.orderBy("rand()")
 						.setMaxRows(itemsPerLevel);		
 						result.addAll(el.findList());
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 }
