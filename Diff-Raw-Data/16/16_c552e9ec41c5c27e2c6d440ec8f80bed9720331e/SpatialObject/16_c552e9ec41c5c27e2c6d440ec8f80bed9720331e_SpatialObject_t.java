 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.mobiloc.lobgasp.model;
 
 import com.vividsolutions.jts.geom.Geometry;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
 import org.hibernate.annotations.Type;
 import org.mobiloc.lobgasp.osm.parser.model.AbstractNode;
 
 /**
  *
  * @author rainerdreyer
  */
 @Entity
 @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
 public class SpatialObject {
     Long id;
 
     public SpatialObject() {}
 
     public SpatialObject construct(AbstractNode in) {return this;}
 
     @Id @GeneratedValue(strategy=GenerationType.TABLE)
     public Long getId() { return id; }
 
     public void setId(Long id) { this.id = id; }
 
     private Geometry geom;
 
     public boolean xmlRule(AbstractNode in) {
         return false;
     }
 
    @Column(name = "LOC")
    @Type(type = "org.hibernatespatial.GeometryUserType")
     public Geometry getGeom() {
         return geom;
     }
 
     public void setGeom(Geometry geom) {
         this.geom = geom;
     }
 }
