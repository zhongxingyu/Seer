 package amu.licence.edt.model.beans;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.*;
 
 @Entity
 @Table (name="T_SESSION_TYPE")
 public class SessionType implements Serializable {
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     @Column (name="ID_SESSION_TYPE")
     private int id;
 
    @Column (name="LIBEL_SESSION_TYPE", unique=true)
     private String libel;
 
     @Column (name="TUTOR_WORTH_COEF")
     private double tutorWorthCoef;
 
     @ManyToMany (fetch=FetchType.LAZY, mappedBy="compatibleSessionTypes")
     private Set<CRoomType> compatibleCRoomTypes;
 
     public SessionType() { }
 
     public SessionType(String libel, double tutorWorthCoef,
             Set<CRoomType> compatibleCRoomTypes) {
         super();
         this.libel = libel;
         this.tutorWorthCoef = tutorWorthCoef;
         this.compatibleCRoomTypes = compatibleCRoomTypes;
     }
 
     @Override
     public String toString() {
         return "SessionType [id=" + id + ", libel=" + libel
                 + ", tutorWorthCoef=" + tutorWorthCoef + "]";
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + id;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         SessionType other = (SessionType) obj;
         if (id != other.id)
             return false;
         return true;
     }
 
     public String getLibel() {
         return libel;
     }
 
     public void setLibel(String libel) {
         this.libel = libel;
     }
 
     public double getTutorWorthCoef() {
         return tutorWorthCoef;
     }
 
     public void setTutorWorthCoef(double tutorWorthCoef) {
         this.tutorWorthCoef = tutorWorthCoef;
     }
 
     public Set<CRoomType> getCompatibleCRoomTypes() {
         return compatibleCRoomTypes;
     }
 
     public boolean addCompatibleCRoomType(CRoomType cRoomType) {
         return this.compatibleCRoomTypes.add(cRoomType);
     }
 
     public boolean removeCompatibleCRoomType(CRoomType cRoomType) {
         return this.compatibleCRoomTypes.remove(cRoomType);
     }
 
     public int getId() {
         return id;
     }
 
 }
