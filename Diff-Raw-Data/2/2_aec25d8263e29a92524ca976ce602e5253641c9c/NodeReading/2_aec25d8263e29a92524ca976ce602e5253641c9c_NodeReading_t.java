 package eu.wisebed.wisedb.model;
 
 import org.hibernate.annotations.Index;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * This is a persistent class for the object NodeReading that has the
  * properties of a wisedb entry. In the class there are
  * getter and setter methods for the properties.
  */
 @Entity
 @Table(name = "nodeReadings2")
 public final class NodeReading implements Serializable {
 
     /**
      * Serial Version Unique ID.
      */
     private static final long serialVersionUID = -1984083831602799368L;
 
     /**
      * Reading id.
      */
     private int id;
 
     /**
      * Capability reference.
      */
     private NodeCapability capability;
 
     /**
      * Timestamp.
      */
     private Date timestamp;
 
     /**
      * Capability reading value for this node.
      */
     private Double reading;
 
     /**
      * Capability string reading value for this node.
      */
     private String stringReading;
 
     /**
      * Returns NodeReading's id.
      *
      * @return NodeReading's id.
      */
     @Id
     @GeneratedValue
     @Column(name = "reading_id")
     public int getId() {
         return id;
     }
 
     /**
      * Returns the capability that indicated this reading.
      *
      * @return capability persistent object.
      */
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
     @JoinColumn(name = "capability_id", referencedColumnName = "id")
     public NodeCapability getCapability() {
         return capability;
     }
 
     /**
      * Returns the timestamp that this reading occured.
      *
      * @return timestamp of the reading.
      */
     @Column(name = "timestamp", nullable = false)
    @Index(name = "timestamp")
     public Date getTimestamp() {
         return timestamp;
     }
 
     /**
      * Returns this reading value.
      *
      * @return this reading value.
      */
     @Column(name = "reading", nullable = true)
     public Double getReading() {
         return reading;
     }
 
     /**
      * Returns string reading.
      *
      * @return string reading.
      */
     @Column(name = "stringReading", nullable = true, length = 1000)
     public String getStringReading() {
         return stringReading;
     }
 
     /**
      * Sets NodeReading's id.
      *
      * @param id , nodereading's id.
      */
     public void setId(final int id) {
         this.id = id;
     }
 
     /**
      * Sets the capability that indicated this reading.
      *
      * @param capability , must be persistent.
      */
     public void setCapability(final NodeCapability capability) {
         this.capability = capability;
     }
 
     /**
      * Sets the timestamp that this reading occured.
      *
      * @param timestamp , timestamp of the reading.
      */
     public void setTimestamp(final Date timestamp) {
         this.timestamp = timestamp;
     }
 
     /**
      * Sets this reading value.
      *
      * @param reading , this reading value.
      */
     public void setReading(final Double reading) {
         this.reading = reading;
     }
 
     /**
      * Sets string reading.
      *
      * @param stringReading string reading.
      */
     public void setStringReading(final String stringReading) {
         this.stringReading = stringReading;
     }
 
     @Override
     public String toString() {
         return new StringBuilder().append("NodeReading{").append(id).append('}').toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         NodeReading that = (NodeReading) o;
 
         if (id != that.id) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return toString().hashCode();
     }
 }
