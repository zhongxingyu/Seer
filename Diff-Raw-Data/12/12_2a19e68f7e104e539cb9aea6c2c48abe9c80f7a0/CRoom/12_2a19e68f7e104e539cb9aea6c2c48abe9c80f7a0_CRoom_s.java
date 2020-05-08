 package amu.licence.edt.model.beans;
 
 import java.io.Serializable;
 
 import javax.persistence.*;
 
 @Entity
 @Table (name="T_CROOM")
 @NamedNativeQueries ({
    @NamedNativeQuery (name=CRoom.FIND_AVAILABLES_BY_ST_PERIOD,
                        query="SELECT croom_occupied(?1, ?2, ?3) FROM dual"),
 })
 public class CRoom implements Serializable {
     private static final long serialVersionUID = 1L;
 
    public static final String FIND_AVAILABLES_BY_ST_PERIOD = "findAvailablesCRoomsBySTPeriod";
 
     @Id
     @GeneratedValue
     @Column (name="ID_CROOM")
     private int id;
 
     @Column (name="NUM_SALLE", nullable=false, unique=true)
     private String num;
 
     @Column (name="MAX_SIZE")
     private int maxSize;
 
     @ManyToOne
     @JoinColumn (name="ID_CROOM_TYPE", nullable=false)
     private CRoomType cRoomType;
 
     public CRoom() { }
 
     public CRoom(String num, int maxSize, CRoomType cRoomType) {
         super();
         this.num = num;
         this.maxSize = maxSize;
         this.cRoomType = cRoomType;
     }
 
     @Override
     public String toString() {
         return "CRoom [id=" + id + ", num=" + num + ", maxSize=" + maxSize
                 + ", cRoomType=" + ((cRoomType != null) ? cRoomType.getLibel() : null) + "]";
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
         CRoom other = (CRoom) obj;
         if (id != other.id)
             return false;
         return true;
     }
 
     public String getNum() {
         return num;
     }
 
     public void setNum(String num) {
         this.num = num;
     }
 
     public int getMaxSize() {
         return maxSize;
     }
 
     public void setMaxSize(int maxSize) {
         this.maxSize = maxSize;
     }
 
     public CRoomType getcRoomType() {
         return cRoomType;
     }
 
     public void setcRoomType(CRoomType cRoomType) {
         this.cRoomType = cRoomType;
     }
 
     public int getId() {
         return id;
     }
 
 }
