 package eu.wisebed.wisedb.model;
 
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
 
import javax.persistence.*;
 import java.io.Serializable;
 
 /**
  * This is a persistant class for the object setup that has the
  * properties of a setup. In the class there are
  * getter and setter methods for the properties.
  */
 @Entity
 @Table(name = "setup")
 public class Setup implements Serializable {
 
     /**
      * Serial Version Unique ID.
      */
     private static final long serialVersionUID = -8742610512262219055L;
 
     /**
      * id of setup.
      */
     private int id;
 
     /**
      * the origin of node.
      */
     private Origin origin;
 
     /**
      * the information of time used in the experiment.
      */
     private TimeInfo timeinfo;
 
     /**
      * the description of the experiment.
      */
     private String description;
 
     /**
      * the type of the coordinate system.
      */
     private String coordinateType;
 
     private Testbed testbed;
 
     @OneToOne(fetch = FetchType.LAZY)
     @PrimaryKeyJoinColumn
     public Testbed getTestbed() {
         return testbed;
     }
 
     /**
      * this method returns the id of the setup.
      *
      * @return the id of the setup.
      */
     @GenericGenerator(name = "generator", strategy = "foreign",
             parameters = @Parameter(name = "property", value = "testbed"))
     @Id
     @GeneratedValue(generator = "generator")
     @Column(name = "setup_id", unique = true, nullable = false)
     public int getId() {
         return id;
     }
 
     /**
      * this method returns the origin of nodes.
      *
      * @return the nodes origin
      */
     @Basic(fetch = FetchType.LAZY)
     @Embedded
     @AttributeOverrides({
             @AttributeOverride(name = "x", column = @Column(name = "position_x")),
             @AttributeOverride(name = "y", column = @Column(name = "position_y")),
             @AttributeOverride(name = "z", column = @Column(name = "position_z")),
             @AttributeOverride(name = "theta", column = @Column(name = "position_theta")),
             @AttributeOverride(name = "phi", column = @Column(name = "position_phi"))
     })
     public Origin getOrigin() {
         return origin;
     }
 
     /**
      * this method returns the information of time used in the experiment.
      *
      * @return the information time
      */
     @Basic(fetch = FetchType.LAZY)
     @Embedded
     @AttributeOverrides({
             @AttributeOverride(name = "duration", column = @Column(name = "timeinfo_duration")),
             @AttributeOverride(name = "start", column = @Column(name = "timeinfo_start")),
             @AttributeOverride(name = "end", column = @Column(name = "timeinfo_end")),
             @AttributeOverride(name = "unit", column = @Column(name = "timeinfo_unit"))
     })
     public TimeInfo getTimeinfo() {
         return timeinfo;
     }
 
     /**
      * this method returns the description of the experiment.
      *
      * @return the description
      */
     @Basic(fetch = FetchType.LAZY)
    @Column(name = "description", columnDefinition = "LONGTEXT", unique = false, nullable = true)
     public String getDescription() {
         return description;
     }
 
     /**
      * this method returns the coordinate type used for describing the position of the nodes.
      *
      * @return the coordinate type
      */
     @Basic(fetch = FetchType.LAZY)
     @Column(name = "coordinateType", unique = false, nullable = false)
     public String getCoordinateType() {
         return coordinateType;
     }
 
     /**
      * this method sets the id of the setup.
      *
      * @param id the id.
      */
     public void setId(final int id) {
         this.id = id;
     }
 
     public void setTestbed(Testbed testbed) {
         this.testbed = testbed;
     }
 
     /**
      * this method sets the origin of nodes.
      *
      * @param origin the nodes origin
      */
     public void setOrigin(final Origin origin) {
         this.origin = origin;
     }
 
     /**
      * this method sets the information of time used in the experiment.
      *
      * @param timeinfo the time information
      */
     public void setTimeinfo(final TimeInfo timeinfo) {
         this.timeinfo = timeinfo;
     }
 
     /**
      * this method sets the description of the experiment.
      *
      * @param description the experiment description
      */
     public void setDescription(final String description) {
         this.description = description;
     }
 
 
     /**
      * this method sets the coordinate type used for describing the position of the nodes.
      *
      * @param description the coordinate type
      */
     public void setCoordinateType(final String description) {
         this.coordinateType = description;
     }
 
     @Override
     public String toString() {
         return new StringBuilder().append("Setup{").append(id).append('}').toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Setup setup = (Setup) o;
 
         if (id != setup.id) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return toString().hashCode();
     }
 }
