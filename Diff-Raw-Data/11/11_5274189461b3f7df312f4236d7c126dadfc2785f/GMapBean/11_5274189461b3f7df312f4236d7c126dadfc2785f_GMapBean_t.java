 package org.icemobile.samples.mobileshowcase.view.examples.layout.gmap;
 
 import java.io.Serializable;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.Destination;
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.Example;
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.ExampleResource;
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.ExampleResources;
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.ResourceType;
 import org.icemobile.samples.mobileshowcase.view.metadata.context.ExampleImpl;
 
 @Destination(
         title = "example.layout.gmap.destination.title.short",
         titleExt = "example.layout.gmap.destination.title.long",
         titleBack = "example.layout.gmap.destination.title.back"
 )
 @Example(
         descriptionPath = "/WEB-INF/includes/examples/layout/gmap-desc.xhtml",
         examplePath = "/WEB-INF/includes/examples/layout/gmap-example.xhtml",
         resourcesPath = "/WEB-INF/includes/examples/example-resources.xhtml"
 )
 @ExampleResources(
         resources = {
                 // xhtml
                 @ExampleResource(type = ResourceType.xhtml,
                         title = "gmap-example.xhtml",
                         resource = "/WEB-INF/includes/examples/layout/gmap-example.xhtml"),
                 // Java Source
                 @ExampleResource(type = ResourceType.java,
                         title = "GMapBean.java",
                         resource = "/WEB-INF/classes/org/icemobile/samples/mobileshowcase" +
                                 "/view/examples/layout/gmap/GMapBean.java")
         }
 )
 
 @ManagedBean(name = GMapBean.BEAN_NAME)
 @SessionScoped
 public class GMapBean extends ExampleImpl<GMapBean> implements
 Serializable {
 
 	public static final String BEAN_NAME = "gMapBean";
 
 	private double latitude = 0.0;
     private double longitude = 0.0;
     private double altitude = 0.0;
     private double direction = 0.0;
     private int zoom = 4;
     private String type = "map";
     
     public GMapBean() {
         super(GMapBean.class);
     }
 
     public double getLatitude() {
         return latitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 
     public void setLongitude(double longitude) {
     	this.longitude = longitude;
     }
 
     public void setLatitude(double latitude) {
     	this.latitude = latitude;
     }
 
     public double getAltitude() {
 		return altitude;
 	}
 
 	public void setAltitude(double altitude) {
 		this.altitude = altitude;
 	}
 
 	public double getDirection() {
 		return direction;
 	}
 
 	public void setDirection(double direction) {
 		this.direction = direction;
 	}
 	
 	public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public int getZoom() {
         return zoom;
     }
 
     public void setZoom(int zoom) {
         this.zoom = zoom;
     }
 
 
 }
