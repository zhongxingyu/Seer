 package models.spatial;
 
 import com.google.gson.*;
 import com.vividsolutions.jts.geom.Point;
 import controllers.util.ApproxSwissProj;
 import controllers.util.DateFormatHelper;
 import controllers.util.JPAUtil;
 import controllers.util.json.GeoJsonSerializable;
 import models.Mission;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Type;
 import scala.Option;
 
 import javax.persistence.*;
 import java.util.Date;
 
 @Entity
 @Table(name = "trajectorypoint")
 public class TrajectoryPoint implements GeoJsonSerializable {
 
     @Id
     @GeneratedValue(generator="increment")
     @GenericGenerator(name="increment", strategy = "increment")
     private Long id;
 
     private Date timestamp;
 
     @Column(name="coordinate")
     @Type(type="org.hibernate.spatial.GeometryType")
     private Point coordinate;
 
     @ManyToOne
     @JoinColumn(name="mission_id")
     private Mission mission;
 
     private Double speed;
     private Double heading;
 
     public TrajectoryPoint() {
     }
 
     public Long getId() {
         return id;
     }
 
     private void setId(Long id) {
         this.id = id;
     }
 
     public Date getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Date ts) {
         this.timestamp = ts;
     }
 
     public Point getCoordinate() {
         return this.coordinate;
     }
 
     public void setCoordinate(Point pos) {
         this.coordinate = pos;
     }
 
     public Mission getMission() {
         return this.mission;
     }
 
     public void setMission(Mission m) {
         this.mission = m;
     }
 
     public Double getSpeed() {
         return this.speed;
     }
 
     public void setSpeed(Double sp) {
         this.speed = sp;
     }
 
     public Double getHeading() {
         return this.heading;
     }
 
     public void setHeading(Double h) {
         this.heading = h;
     }
 
     public String toString() {
         return id + " -> ts: " + timestamp + ", point: " + coordinate.toString();
     }
 
     @Override
     public String toGeoJson() {
         return new GsonBuilder().registerTypeAdapter(TrajectoryPoint.class, new TrajectoryPointGeoJsonSerializer()).create().toJson(this);
     }
 
     /**
      * Custom Geo JSON Serializer for GPS log
      */
     public static class TrajectoryPointGeoJsonSerializer implements JsonSerializer<TrajectoryPoint> {
         @Override
         public JsonElement serialize(TrajectoryPoint point, java.lang.reflect.Type type, JsonSerializationContext context) {
             Gson gson = new Gson();
             JsonObject geometryObj = new JsonObject();
             geometryObj.addProperty("type", "Point");
             String str = "["+point.coordinate.getCoordinate().x +","+point.coordinate.getCoordinate().y+","+ point.coordinate.getCoordinate().z +"]";
             JsonArray jArr = gson.fromJson(str, JsonArray.class);
             geometryObj.add("coordinates", jArr);
 
             JsonObject propertiesObj = new JsonObject();
             propertiesObj.addProperty("id", point.getId());
             propertiesObj.addProperty("timestamp", DateFormatHelper.postgresTimestampWithMilliFormatter().format(point.getTimestamp()));
             propertiesObj.addProperty("speed", point.getSpeed());
             propertiesObj.addProperty("heading", point.getHeading());
             if (point.getCoordinate() != null) {
                 double[] arr = ApproxSwissProj.WGS84toLV03(point.coordinate.getCoordinate().y, point.coordinate.getCoordinate().x, point.coordinate.getCoordinate().z);
                 propertiesObj.addProperty("coordinate_swiss", arr[0] + "," + arr[1] +","+arr[2]);
                 //propertiesObj.addProperty("speed", gpsLog.getSpeed());
             }
 
             JsonObject featureObj = new JsonObject();
             featureObj.addProperty("type", "Feature");
             featureObj.add("geometry", geometryObj);
             featureObj.add("properties", propertiesObj);
             return featureObj;
         }
     }
 
 
 
     /**
      * Save the GpsLog in Postgres database
      */
     public Boolean save(Option<EntityManager> emOpt) {
         EntityManager em;
         if (emOpt.isEmpty()) {
             em = JPAUtil.createEntityManager();
         } else {
             em = emOpt.get();
         }
         Boolean res = false;
         try {
             if(emOpt.isEmpty()) em.getTransaction().begin();
             em.persist(this);
             if(emOpt.isEmpty()) em.getTransaction().commit();
             res = true;
         } catch (Exception ex) {
             System.out.println("[ERROR][TrajectoryPoint.save()] "+ ex.getMessage());
         } finally {
             if(emOpt.isEmpty()) em.close();
         }
         return res;
     }
 
 }
