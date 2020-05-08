 package models;
 
 import com.google.gson.*;
 import com.vividsolutions.jts.geom.LineString;
 import controllers.util.DateFormatHelper;
 import controllers.util.JPAUtil;
 import controllers.util.json.JsonSerializable;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Type;
 
 import javax.persistence.*;
 import java.util.Date;
 import java.util.Set;
 
 @Entity
 @Table(name = "mission")
 public class Mission implements JsonSerializable {
 
 
     @Id
     @GeneratedValue(generator="increment")
     @GenericGenerator(name="increment", strategy = "increment")
     private Long id;
 
     private Date departureTime;
 
    @Column(name="coordinate")
     @Type(type="org.hibernate.spatial.GeometryType")
     private LineString trajectory;
 
     @OneToOne
     @JoinColumn(name="vehicle_id")
     private Vehicle vehicle;
 
     /*@ManyToMany(cascade = CascadeType.ALL)
     @JoinTable(name = "equipment", joinColumns = { @JoinColumn(name = "mission_id") }, inverseJoinColumns = { @JoinColumn(name = "device_id") })
     private Set<Device> devices;
     */
 
     public Mission() {
     }
 
     public Long getId() {
         return id;
     }
 
     private void setId(Long id) {
         this.id = id;
     }
 
     public Date getDepartureTime() {
         return departureTime;
     }
 
     public void setDepartureTime(Date d) {
         this.departureTime = d;
     }
 
     public LineString getTrajectory() {
         return this.trajectory;
     }
 
     public void setTrajectory(LineString tra) {
         this.trajectory = tra;
     }
 
     public Vehicle getVehicle() {
         return this.vehicle;
     }
 
     public void setVehicle(Vehicle v) {
         this.vehicle = v;
     }
 
     /*public Set<Device> getDevices() {
         return this.devices;
     }
 
     public void setDevices(Set<Device> devs) {
         this.devices = devs;
     }*/
 
     public String toString() {
         return id + " -> date: " + departureTime + ", vehicle: " + vehicle.getName();
     }
 
     @Override
     public String toJson() {
         return new GsonBuilder().registerTypeAdapter(Mission.class, new MissionSerializer()).create().toJson(this);
     }
 
     /**
      * Custom JSON Serializer for GPS log
      */
     public static class MissionSerializer implements JsonSerializer<Mission> {
         @Override
         public JsonElement serialize(Mission mission, java.lang.reflect.Type type, JsonSerializationContext context) {
             JsonElement missionJson = new JsonObject();
             missionJson.getAsJsonObject().addProperty("id", mission.getId());
             missionJson.getAsJsonObject().addProperty("date", DateFormatHelper.selectYearFormatter().format(mission.getDepartureTime()));
             missionJson.getAsJsonObject().addProperty("vehicle", mission.getVehicle().getName());
             return missionJson;
         }
     }
 
 
     /**
      * Save the GpsLog in Postgres database
      */
     public Boolean save() {
         EntityManager em = JPAUtil.createEntityManager();
         Boolean res = false;
         try {
             em.getTransaction().begin();
             em.persist(this);
             em.getTransaction().commit();
             res = true;
         } catch (Exception ex) {
             System.out.println("[WARNING] "+ ex.getMessage());
         } finally {
             em.close();
         }
         return res;
     }
 
 }
