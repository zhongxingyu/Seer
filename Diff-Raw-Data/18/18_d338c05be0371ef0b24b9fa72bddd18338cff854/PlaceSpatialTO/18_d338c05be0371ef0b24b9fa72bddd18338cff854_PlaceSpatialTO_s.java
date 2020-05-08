 package br.com.findplaces.model.spatial.to;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import br.com.findplaces.model.to.PlaceTO;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 @XmlRootElement(name="placespatial")
 public class PlaceSpatialTO implements Serializable{
 	
 	private static final long serialVersionUID = 6531733027369822780L;
 
 	@XmlTransient
 	private Long id;
 			
 	@XmlTransient
 	private Double lat;
 	
 	@XmlTransient
 	private Double lon;
 	
	@XmlTransient
	private Geometry geom;
 
 	public Long getId() {
 		return id;
 	}
 	
 	public Double getLat() {
 		return lat;
 	}
 
 	public Double getLon() {
 		return lon;
 	}
 
	public Geometry getGeom() {
		return geom;
	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 
 	public void setLat(Double lat) {
 		this.lat = lat;
 	}
 
 	public void setLon(Double lon) {
 		this.lon = lon;
 	}
 
	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
 
 }
