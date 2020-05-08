 package gov.usgs.cida.coastalhazards.model;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedNativeQuery;
 import javax.persistence.Table;
 import org.hibernate.annotations.Loader;
 import org.hibernate.annotations.SQLInsert;
 import org.hibernate.annotations.SQLUpdate;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @Entity
 @Table(name="bbox")
@SQLInsert(sql = "INSERT into bbox (bbox, id) VALUES (CAST(? AS box2d), ?)")
@SQLUpdate(sql = "UPDATE bbox SET bbox = CAST(? AS box2d) WHERE id = ?")
//@Loader(namedQuery = "bbox_load")
//@NamedNativeQuery(name="bbox_load", query = "SELECT id, bbox FROM bbox WHERE id = ?", resultClass = Bbox.class)
 public class Bbox implements Serializable {
 	private static final long serialVersionUID = 1L;
     public static final int SRID = 4326;
     public static final String BOX_FORMAT = "BOX(%f %f, %f %f)";
     
     private transient int id;
     // store the serialized version, use utilities to get Envelope
     private String bbox;
 
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Column(name = "bbox", columnDefinition = "box2d")
     public String getBbox() {
         return bbox;
     }
 
     public void setBbox(String bbox) {
 //        Envelope envelope = bbox.getEnvelopeInternal();
 //        this.bbox = String.format(BOX_FORMAT, envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
         this.bbox = bbox;
     }
     
     public void setBbox(double minX, double minY, double maxX, double maxY) {
         this.bbox = String.format(BOX_FORMAT, minX, minY, maxX, maxY);
     }
 
 //    @Transient
 //    public String getBboxString() {
 //        return bbox;
 //    }
 //
 //    public void setBboxString(String bboxString) {
 //        this.bbox = bboxString;
 //    }
 //    
     
     
     public static Bbox copyValues(Bbox from, Bbox to) {
         Bbox bbox = new Bbox();
         bbox.setId(to.getId());
         bbox.setBbox(from.getBbox());
         return bbox;
     }
     
     public static Polygon envelopeToPolygon(Envelope e) {
         GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING_SINGLE), SRID);
         Coordinate coordA = new Coordinate(e.getMinX(), e.getMinY());
         Coordinate coordB = new Coordinate(e.getMinX(), e.getMaxY());
         Coordinate coordC = new Coordinate(e.getMaxX(), e.getMaxY());
         Coordinate coordD = new Coordinate(e.getMaxX(), e.getMinY());
         Coordinate coordE = coordA;
         Polygon bboxPoly = factory.createPolygon(new Coordinate [] {coordA, coordB, coordC, coordD, coordE});
         return bboxPoly;
     }
 }
