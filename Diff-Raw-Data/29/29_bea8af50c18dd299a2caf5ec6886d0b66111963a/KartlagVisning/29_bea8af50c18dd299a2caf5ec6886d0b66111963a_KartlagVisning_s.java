 package no.imr.geoexplorer.admindatabase.jsp.pojo;
 
 public class KartlagVisning {
 	private long id;
 	private String title;
 	private String layers;
 	private String gruppe;
 	private String url;
 	private double exGeographicBoundingBoxEastBoundLongitude;
 	private double exGeographicBoundingBoxWestBoundLongitude;
 	private double exGeographicBoundingBoxNorthBoundLatitude;
 	private double exGeographicBoundingBoxSouthBoundLatitude;
 	private String keyword;
 	private String abstracts;
    private double scalemin;
    private double scalemax;
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getAbstracts() {
 		return abstracts;
 	}
 	public void setAbstracts(String abstracts) {
 		this.abstracts = abstracts;
 	}
 	public String getKeyword() {
 		return keyword;
 	}
     public void setKeyword(String keyword) {
 		this.keyword = keyword;
 	}
 	public String getLayers() {
 		return layers;
 	}
 	public void setLayers(String layers) {
 		this.layers = layers;
 	}
 	public String getGruppe() {
 		return gruppe;
 	}
 	public void setGruppe(String gruppe) {
 		this.gruppe = gruppe;
 	}
 	public String getUrl() {
 		return url;
 	}
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	public double getExGeographicBoundingBoxEastBoundLongitude() {
 		return exGeographicBoundingBoxEastBoundLongitude;
 	}
 	public void setExGeographicBoundingBoxEastBoundLongitude(double exGeographicBoundingBoxEastBoundLongitude) {
 		this.exGeographicBoundingBoxEastBoundLongitude = exGeographicBoundingBoxEastBoundLongitude;
 	}
 	public double getExGeographicBoundingBoxWestBoundLongitude() {
 		return exGeographicBoundingBoxWestBoundLongitude;
 	}
 	public void setExGeographicBoundingBoxWestBoundLongitude(double exGeographicBoundingBoxWestBoundLongitude) {
 		this.exGeographicBoundingBoxWestBoundLongitude = exGeographicBoundingBoxWestBoundLongitude;
 	}
 	public double getExGeographicBoundingBoxNorthBoundLatitude() {
 		return exGeographicBoundingBoxNorthBoundLatitude;
 	}
 	public void setExGeographicBoundingBoxNorthBoundLatitude(double exGeographicBoundingBoxNorthBoundLatitude) {
 		this.exGeographicBoundingBoxNorthBoundLatitude = exGeographicBoundingBoxNorthBoundLatitude;
 	}
 	public double getExGeographicBoundingBoxSouthBoundLatitude() {
 		return exGeographicBoundingBoxSouthBoundLatitude;
 	}
 	public void setExGeographicBoundingBoxSouthBoundLatitude(double exGeographicBoundingBoxSouthBoundLatitude) {
 		this.exGeographicBoundingBoxSouthBoundLatitude = exGeographicBoundingBoxSouthBoundLatitude;
 	}
    public double getScalemin() {
        return scalemin;
    }
    public void setScalemin(double scalemin) {
        this.scalemin = scalemin;
    }
    public double getScalemax() {
        return scalemax;
    }
    public void setScalemax(double scalemax) {
        this.scalemax = scalemax;
    }
 
 }
