 package ie.wombat.landedestates;
 
 import java.util.HashSet;
 import java.util.Set;
 
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.log4j.Logger;
 import org.hibernate.search.annotations.DocumentId;
 import org.hibernate.search.annotations.Indexed;
 
 import ie.wombat.gis.InvalidGridReferenceException;
 import ie.wombat.gis.OSIGridReference;
 import ie.wombat.imagetable.Image;
 
 @Entity
 @Indexed
@Table(name="property")
 public class Property implements Indexable {
 
 	private static Logger log = Logger.getLogger(Property.class);
 	
 	@Id
 	@DocumentId
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private Long id;
 	
 	private String name;
 	private String description;
 	private String townland;
	
	@Column(name="civil_parish")
 	private String civilParish;
	
 	private String barony;
 	private String ded;
 	private String plu;
 	private String county;
	
	@Column(name="osi_grid_ref")
 	private String gridReference;
	
	@Column(name="os_sheet")
 	private String osSheet;
	
	@Column(name="discovery_map")
 	private String discoveryMap;
	
 	private Double latitude;
 	private Double longitude;
 	private int easting;
 	private int northing;
 	
 	//@OneToMany
 	@Transient
 	private Set<Image> images = new HashSet<Image>();
 	
	@Column(name="project_phase")
 	private Integer projectPhase;
 	
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
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
 	public String getBarony() {
 		return barony;
 	}
 	public void setBarony(String barony) {
 		this.barony = barony;
 	}
 	public String getCivilParish() {
 		return civilParish;
 	}
 	public void setCivilParish(String civilParish) {
 		this.civilParish = civilParish;
 	}
 	public String getCounty() {
 		return county;
 	}
 	public void setCounty(String county) {
 		this.county = county;
 	}
 	public String getDed() {
 		return ded;
 	}
 	public void setDed(String ded) {
 		this.ded = ded;
 	}
 	public String getPlu() {
 		return plu;
 	}
 	public void setPlu(String plu) {
 		this.plu = plu;
 	}
 	public String getTownland() {
 		return townland;
 	}
 	public void setTownland(String townland) {
 		this.townland = townland;
 	}
 	
 	
 	public int getEasting() {
 		return easting;
 	}
 	public void setEasting(int easting) {
 		this.easting = easting;
 	}
 	public int getNorthing() {
 		return northing;
 	}
 	public void setNorthing(int northing) {
 		this.northing = northing;
 	}
 	
 	public boolean hasGridReference () {
 		if ( (getEasting() > 0 ) &&  (getNorthing() > 0) ) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Latitude in degrees. 
 	 * @return Latitude in degrees.
 	 */
 	public synchronized Double getLatitude() {
 		return latitude;
 	}
 	
 	/**
 	 * Get latitude in degrees.
 	 * @return Latitude in degrees.
 	 */
 	public double getLatitudeDeg () {
 		return latitude;
 	}
 	
 	/**
 	 * Set latitude in degrees.
 	 * 
 	 * @param latitude Latitude in degrees.
 	 */
 	public void setLatitude(Double latitude) {
 		if (latitude != null) {
 			if (latitude < 51 || latitude > 55) {
 				System.err.println ("WARNING OUT OF RANGE: House#" + getId() + " setting latitude=" + latitude);
 			}
 		}
 		this.latitude = latitude;
 	}
 	
 	/**
 	 * Get longitude in degrees.
 	 * 
 	 * @return Longitude in degrees.
 	 */
 	public synchronized Double getLongitude() {
 		return longitude;
 	}
 	
 	/**
 	 * Get longitude in degrees.
 	 * 
 	 * @return Longitude in degrees.
 	 */
 	
 	public double getLongitudeDeg () {
 		return longitude;
 	}
 	
 	/**
 	 * Longitude in degress.
 	 * @return
 	 */
 	public void setLongitude(Double longitude) {
 		if (latitude != null) {
 			if (longitude < -11 || longitude > -5) {
 				System.err.println ("WARNING OUT OF RANGE: House#" + getId() + " setting longitude=" + longitude);
 			}
 		}
 		this.longitude = longitude;
 	}
 	public String getOsSheet() {
 		return osSheet;
 	}
 	public void setOsSheet(String osSheet) {
 		this.osSheet = osSheet;
 	}
 	
 	
 	public String getDiscoveryMap() {
 		return discoveryMap;
 	}
 	public void setDiscoveryMap(String discoveryMap) {
 		this.discoveryMap = discoveryMap;
 	}
 	
 	
 	public String getGridReference() {
 		return gridReference;
 	}
 	public void setGridReference(String gridReference) {
 		this.gridReference = gridReference;
 
 		if (gridReference == null || gridReference.length() == 0) {
 			this.easting=0;
 			this.northing=0;
 			//this.latitude=0.0;
 			//this.longitude=0.0;
 			return;
 		}
 
 		try {
 			OSIGridReference osigr = new OSIGridReference(gridReference);
 			setEasting((int) osigr.getEasting());
 			setNorthing((int) osigr.getNorthing());
 			setLatitude(osigr.getLatitudeDeg());
 			setLongitude(osigr.getLongitudeDeg());
 		} catch (InvalidGridReferenceException e) {
 			log.warn("property id=" + getId()
 					+ " has invalid grid reference: " 
 					+ gridReference
 					+ ": " + e.getMessage()
 					);
 				
 			this.easting=0;
 			this.northing=0;
 			//this.latitude=0.0;
 			//this.longitude=0.0;
 		}
 	}
 	public Set<Image> getImages() {
 		return images;
 	}
 	public void setImages(Set<Image> images) {
 		this.images = images;
 	}
 	
 	public Integer getProjectPhase() {
 		return projectPhase;
 	}
 	public void setProjectPhase(Integer projectPhase) {
 		this.projectPhase = projectPhase;
 	}
 	/**
 	 * Return true of one or more images.
 	 * @return
 	 */
 	public boolean hasImage () {
 		return (this.images.size() > 0);
 	}
 	/**
 	 * Return the first image if more than one. Return null if no images.
 	 * @return
 	 */
 	public Image getImage() {
 		if (this.images.size() == 0) {
 			return null;
 		}
 		for (Image image : images) {
 			return image;
 		}
 		return null;
 	}
 	public String getLuceneId () {
 		return "H" + this.id;
 	}
 }
