 package dk.itu.realms.model.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Named;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.springframework.context.annotation.Scope;
 
 @Named("markModel")
 @Scope("request")
 
 @Entity
 @Table(name = "marks", schema = "realms")
 public class Mark {
 	
 	private static final String[] TYPES = new String[] { "INFORMATION", "QUESTION" };
 	
 	private static final Double DEFAULT_RADIUS = 300d;
 	private static final String DEFAULT_TYPE = TYPES[0];
 	
 	private Long id;
 	
 	private String markTitle;
 	private String markDescription;
 	private Double latitude;
 	private Double longitude;
 	private Double radius; // accuracy
 	
 	/* possible types: INFORMATION, QUESTION */
 	private String type;
 
 	/* can represent a Question or INFORMATION -> the data stored here will be formatted! */
 	private String textBlob;
 	
 	private List<Option> options;
 
 	public static String[] getSupportedTypes() {
 		return TYPES;
 	}
 	
 	public Mark() { }
 	
 	public Mark(Double lat, Double lng) {
 		this.latitude = lat;
 		this.longitude = lng;
 		
 		radius = DEFAULT_RADIUS;
 		type = DEFAULT_TYPE;
 		
 		options = new ArrayList<Option>();
 	}
 	
 	@Id
 	@GeneratedValue
 	@Column(name = "id")
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Column(name = "title", nullable = false)
 	public String getMarkTitle() {
 		return markTitle;
 	}
 	public void setMarkTitle(String markTitle) {
 		this.markTitle = markTitle;
 	}
 
 	@Column(name = "description", nullable = true)
 	public String getMarkDescription() {
 		return markDescription;
 	}
 	public void setMarkDescription(String markDescription) {
 		this.markDescription = markDescription;
 	}
 
 	@Column(name = "latitude", nullable = false)
 	public Double getLatitude() {
 		return latitude;
 	}
 	public void setLatitude(Double latitude) {
 		this.latitude = latitude;
 	}
 
 	@Column(name = "longitude", nullable = false)
 	public Double getLongitude() {
 		return longitude;
 	}
 	public void setLongitude(Double longitude) {
 		this.longitude = longitude;
 	}
 
 	@Column(name = "radius", nullable = false)
 	public Double getRadius() {
 		return radius;
 	}
 	public void setRadius(Double radius) {
 		this.radius = radius;
 	}
 
 	@Column(name = "type", nullable = false)
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	
	@Column(name = "text_blob", columnDefinition="text", nullable = true)
 	public String getTextBlob() {
 		return textBlob;
 	}
 	public void setTextBlob(String textBlob) {
 		this.textBlob = textBlob;
 	}
 	
 	@OneToMany(targetEntity = Option.class, cascade = CascadeType.ALL)
 	public List<Option> getOptions() {
 		return options;
 	}
 	public void setOptions(List<Option> options) {
 		this.options = options;
 	}
 	
 }
