 package models;
 
 import java.util.List;
 
 import javax.persistence.*;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import play.db.ebean.Model;
 
 @Entity
 @Table(name="Location")
 public class Location extends Model {
 
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 775165124786881924L;
 
 	@Id
     @GeneratedValue
     @Column(name="location_id")
     private Long locationId;
 	
 	@Column(name="location_textual")
 	private String textual;
 	
 	@Column 
 	private Long accuracy;
 	
 	@Column(name="name")
 	private String placeName;
 	
 	@Column
 	private String description;
 
 	@Column
 	private String environment;
 
 	@Column
 	private String continent;
 
 	@Column
 	private String country;
 
 	@Column
 	private String region;
 
 	@Column(name="city")
 	private String city;
 
 	@Column
 	private String neighborhood;
 
 	@Column
 	private String street;
 
 	@Column
 	private String street_number;
 
 	@Column
 	private String map_url;
 	
 	@Column
 	private Integer coordinates_trust;
 
 	@Column
 	private Double lat;
 	
 	@Column
 	private Double lon;
 
 	@Column
 	private String locale;
 	
 	@Column
 	private Long radius;
 	
 	@OneToOne
 	@MapsId
     @JoinColumn(name="city_id")
 	private City cityBean;
 	
 	// foreign keys
 	@JsonIgnore
 	@OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<LifeStory> lifeStoriesLocation;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<Memento> mementosLocation;
 	
 	@JsonIgnore
 	@OneToMany(mappedBy = "startLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<ContributedMemento> contributedMementoStartLocation;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "endLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<ContributedMemento> contributedMementoEndLocation;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "startLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<Media> mediaLocation;
 	
 	@JsonIgnore
 	@OneToMany(mappedBy = "startLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<Event> eventLocation;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "startLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<FamousPerson> famousBirthPlace;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "endLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<FamousPerson> famousDeathPlace;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "startLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<PublicMemento> publicMementoStartPlace;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "endLocation", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
 	private List<PublicMemento> publicMementoEndPlace;
 	
 	public static Model.Finder<Long,Location> find = new Model.Finder<Long, Location>(
             Long.class,Location.class
     );
     
     public static List<Location> all(){
         return find.all();
     }
     
     public static void create(Location person){
         person.save();
     }
     
     public static Location createOrUpdateIfNotExist(Location location) {
 		if (location != null) {
     		Long id = location.getLocationId();
 			Location existing = null;
 			if (id != null) {
 				existing = read(id);
 				if (existing != null && existing.isEqualTo(location)) {
 					return existing;
 				} else {
 					location.copyNotNullsFrom(existing);
 				}
 			} 
 			
 			location.setLocationId(null);
 			String textual = location.getTextual();
 			
 			String name = location.getPlaceName();
 			String country = location.getCountry();
 			String region = location.getRegion();
 			String city = location.getCity();
 			City cityBean = location.getCityBean();
 			Double lat = location.getLat();
 			Double lon = location.getLon();
 			String locale = location.getLocale();
 
 			Long acc = new Long(0); 
 //			TODO review the whole accuracy model
 //			1, flickr world-level, if we only have textual description (e.g. it was nearby my hometown) or a name of a place
 			if ((textual !=null && !textual.isEmpty()) || (name !=null && !name.isEmpty()))
 				acc = new Long(1);
 			
 //			2, if we have only continent or environment (e.g. it was in a beach, it was in africa)
 //			3, flickr country-level, if we have country or city or neighbourhood
 			if (country !=null && !country.isEmpty())
 				acc = new Long(3);
 			
 //			4, if it is 3 + environment 
 //			5, if we have country + city or neighbourhood
 //			6, flickr region-level, if we have country + region
 			if (region !=null && !region.isEmpty())
 				acc = new Long(6);
 
 //			7, if we have 6 + environment
 //			11, flickr city-level, if we have country + city 
 			if (city != null && !city.isEmpty()) {
 				if (cityBean==null) {
 					City c = City.getCityByName(city);
 					
					if (locale!=null && !locale.isEmpty() && c!=null){
					    location.setCityBean(c);
					    if (c.getCountry()!=null) {	
						location.setCountry(c.getCountry().getNameByLocale(locale));
					    } 
 					} else {
 						location.setCountry(c.getCountry().getShort_name());
 					}
 					location.setRegion(c.getRegion());
 					if (lat == null && lon == null) {
 						location.setLat(location.getCityBean().getLat());
 						location.setLon(location.getCityBean().getLon());
 						location.setCoordinates_trust(new Integer(0));
 						acc = new Long(11);
 					}
 				}
 			}
 			
 //			12, if we have country + city + neighbourhood
 //			16, flickr street-level, if we have 11 or 12 + street
 //			17, if we have 7 + environment
 //			19,  if we have Lat + Lon but coordinates_trust = false
 //			20, the best, if we have both Lat + Longitude and coordinates_trust = true
 			if (lat != null && lon!=null) {
 				Integer coordTrustInt = location.getCoordinates_trust();
 				Boolean coordTrust = coordTrustInt != null && coordTrustInt == 1;
 				if (coordTrust) {
 					acc = new Long(20);
 				} else {
 					acc = new Long(19);
 				}
 			}
 			// TODO add geocoding to every new location added			
 			
 			location.setAccuracy(acc);
 			location.save();
 			return location;
 		} else {
 			return null;
 		}
 	}
     
     private void copyNotNullsFrom(Location existing) {
     	String textual = existing.getTextual();
 		String name = existing.getPlaceName();
 		String country = existing.getCountry();
 		String region = existing.getRegion();
 		String cityName = existing.getCity();
 		City city = existing.getCityBean();
 		Double lat = existing.getLat();
 		Double lon = existing.getLon();
 		if (this.textual == null) 
 			this.textual = textual;
 		if (this.placeName == null) 
 			this.placeName = name;
 		if (this.country== null) 
 			this.country = country;
 		if (this.region == null)
 			this.region = region;
 		if (this.city == null)
 			this.city = cityName;
 		if (this.city == null)
 			this.cityBean = city; 
 		if (this.lat == null)
 			this.lat = lat; 
 		if (this.lon == null)
 			this.lon = lon;		
 	}
 
 	private boolean isEqualTo(Location location) {
 		String textual = location.getTextual();
 		String name = location.getPlaceName();
 		String country = location.getCountry();
 		String region = location.getRegion();
 		String cityName = location.getCity();
 		City city = location.getCityBean();
 		Double lat = location.getLat();
 		Double lon = location.getLon();
 
 		return this.textual == textual 
 				&& this.placeName == name 
 				&& this.country == country
 				&& this.region == region
 				&& this.city == cityName
 				&& this.cityBean == city
 				&& this.lat == lat
 				&& this.lon == lon;
 	}
 
 	public static Location createObject(Location person){
         person.save();
         return person;
     }
     
     public static void delete(Long id){
         find.ref(id).delete();
     }
     
     public static Location read(Long id){
         return find.byId(id);
     }
 
 	/**
 	 * @return the locationId
 	 */
 	public Long getLocationId() {
 		return locationId;
 	}
 
 	/**
 	 * @param locationId the locationId to set
 	 */
 	public void setLocationId(Long locationId) {
 		this.locationId = locationId;
 	}
 
 	public String getTextual() {
 		return textual;
 	}
 
 	public void setTextual(String location_textual) {
 		this.textual = location_textual;
 	}
 
 	public Long getAccuracy() {
 		return accuracy;
 	}
 
 	public void setAccuracy(Long  accuracy) {
 		this.accuracy = accuracy;
 	}
 
 	public String getPlaceName() {
 		return placeName;
 	}
 
 	public void setPlaceName(String name) {
 		this.placeName = name;
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
 	 * @return the environment
 	 */
 	public String getEnvironment() {
 		return environment;
 	}
 
 	/**
 	 * @param environment the environment to set
 	 */
 	public void setEnvironment(String environment) {
 		this.environment = environment;
 	}
 
 	/**
 	 * @return the continent
 	 */
 	public String getContinent() {
 		return continent;
 	}
 
 	/**
 	 * @param continent the continent to set
 	 */
 	public void setContinent(String continent) {
 		this.continent = continent;
 	}
 
 	/**
 	 * @return the country
 	 */
 	public String getCountry() {
 		return country;
 	}
 
 	/**
 	 * @param country the country to set
 	 */
 	public void setCountry(String country) {
 		this.country = country;
 	}
 
 	/**
 	 * @return the region
 	 */
 	public String getRegion() {
 		return region;
 	}
 
 	/**
 	 * @param region the region to set
 	 */
 	public void setRegion(String region) {
 		this.region = region;
 	}
 
 	/**
 	 * @return the cityName
 	 */
 	public String getCity() {
 		return city;
 	}
 
 	/**
 	 * @param city the city to set
 	 */
 	public void setCity(String city) {
 		this.city = city;
 	}
 
 	/**
 	 * @return the neighborhood
 	 */
 	public String getNeighborhood() {
 		return neighborhood;
 	}
 
 	/**
 	 * @param neighborhood the neighborhood to set
 	 */
 	public void setNeighborhood(String neighborhood) {
 		this.neighborhood = neighborhood;
 	}
 
 	/**
 	 * @return the street
 	 */
 	public String getStreet() {
 		return street;
 	}
 
 	/**
 	 * @param street the street to set
 	 */
 	public void setStreet(String street) {
 		this.street = street;
 	}
 
 	/**
 	 * @return the street_number
 	 */
 	public String getStreet_number() {
 		return street_number;
 	}
 
 	/**
 	 * @param street_number the street_number to set
 	 */
 	public void setStreet_number(String street_number) {
 		this.street_number = street_number;
 	}
 
 	/**
 	 * @return the map_url
 	 */
 	public String getMap_url() {
 		return map_url;
 	}
 
 	/**
 	 * @param map_url the map_url to set
 	 */
 	public void setMap_url(String map_url) {
 		this.map_url = map_url;
 	}
 
 	/**
 	 * @return the coordinates_trust
 	 */
 	public Integer getCoordinates_trust() {
 		return coordinates_trust;
 	}
 
 	/**
 	 * @param coordinates_trust the coordinates_trust to set
 	 */
 	public void setCoordinates_trust(Integer coordinates_trust) {
 		this.coordinates_trust = coordinates_trust;
 	}
 
 	
 	/**
 	 * @return the lat
 	 */
 	public Double getLat() {
 		return lat;
 	}
 
 	/**
 	 * @param lat the lat to set
 	 */
 	public void setLat(Double lat) {
 		this.lat = lat;
 	}
 
 	/**
 	 * @return the lon
 	 */
 	public Double getLon() {
 		return lon;
 	}
 
 	/**
 	 * @param lon the lon to set
 	 */
 	public void setLon(Double lon) {
 		this.lon = lon;
 	}
 
 	/**
 	 * @return the locate
 	 */
 	public String getLocale() {
 		return locale;
 	}
 
 	/**
 	 * @param locate the locate to set
 	 */
 	public void setLocale(String locate) {
 		this.locale = locate;
 	}
 
 	/**
 	 * @return the radius
 	 */
 	public Long getRadius() {
 		return radius;
 	}
 
 	/**
 	 * @param radius the radius to set
 	 */
 	public void setRadius(Long radius) {
 		this.radius = radius;
 	}
 
 	public City getCityBean() {
 		return cityBean;
 	}
 
 	public void setCityBean(City city) {
 		this.cityBean = city;
 	}
 
 	public List<LifeStory> getLifeStoriesLocation() {
 		return lifeStoriesLocation;
 	}
 
 	public void setLifeStoriesLocation(List<LifeStory> lifeStoriesLocation) {
 		this.lifeStoriesLocation = lifeStoriesLocation;
 	}
 
 	public List<Memento> getMementosLocation() {
 		return mementosLocation;
 	}
 
 	public void setMementosLocation(List<Memento> mementosLocation) {
 		this.mementosLocation = mementosLocation;
 	}
 
 	public List<ContributedMemento> getContributedMementoStartLocation() {
 		return contributedMementoStartLocation;
 	}
 
 	public void setContributedMementoStartLocation(
 			List<ContributedMemento> contributedMementoStartLocation) {
 		this.contributedMementoStartLocation = contributedMementoStartLocation;
 	}
 
 	public List<ContributedMemento> getContributedMementoEndLocation() {
 		return contributedMementoEndLocation;
 	}
 
 	public void setContributedMementoEndLocation(
 			List<ContributedMemento> contributedMementoEndLocation) {
 		this.contributedMementoEndLocation = contributedMementoEndLocation;
 	}
 
 	public List<Media> getMediaLocation() {
 		return mediaLocation;
 	}
 
 	public void setMediaLocation(List<Media> mediaLocation) {
 		this.mediaLocation = mediaLocation;
 	}
 
 	public List<Event> getEventLocation() {
 		return eventLocation;
 	}
 
 	public void setEventLocation(List<Event> eventLocation) {
 		this.eventLocation = eventLocation;
 	}
 
 	public List<FamousPerson> getFamousBirthPlace() {
 		return famousBirthPlace;
 	}
 
 	public void setFamousBirthPlace(List<FamousPerson> famousBirthPlace) {
 		this.famousBirthPlace = famousBirthPlace;
 	}
 
 	public List<FamousPerson> getFamousDeathPlace() {
 		return famousDeathPlace;
 	}
 
 	public void setFamousDeathPlace(List<FamousPerson> famousDeathPlace) {
 		this.famousDeathPlace = famousDeathPlace;
 	}
 
 	public List<PublicMemento> getPublicMementoStartPlace() {
 		return publicMementoStartPlace;
 	}
 
 	public void setPublicMementoStartPlace(
 			List<PublicMemento> publicMementoStartPlace) {
 		this.publicMementoStartPlace = publicMementoStartPlace;
 	}
 
 	public List<PublicMemento> getPublicMementoEndPlace() {
 		return publicMementoEndPlace;
 	}
 
 	public void setPublicMementoEndPlace(List<PublicMemento> publicMementoEndPlace) {
 		this.publicMementoEndPlace = publicMementoEndPlace;
 	}
 }
