 package ch.bli.mez.model;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.GenericGenerator;
 
 /**
  * The persistent class for the mission database table.
  * 
  */
 @Entity
 @Table(name = "mission")
 public class Mission {
 
   @Column(name = "mission_id")
   private Integer id;
   private String missionName;
   private String comment;
   private boolean isOrgan;
   private boolean isActive;
   private Set<Position> positions = new HashSet<Position>();
 
   public Mission() {
 
   }
 
   public Mission(String missionName, String comment, boolean isOrgan) {
     this.missionName = missionName;
     this.comment = comment;
     this.isOrgan = isOrgan;
     this.isActive = true;
   }
 
   @Id
   @GeneratedValue(generator = "increment")
   @GenericGenerator(name = "increment", strategy = "increment")
   @Column(name = "mission_id", unique = true, nullable = false)
   public Integer getId() {
     return this.id;
   }
 
   public void setId(Integer id) {
     this.id = id;
   }
 
   @Column(unique = true, nullable = false)
   public String getMissionName() {
     return missionName;
   }
 
   public void setMissionName(String missionName) {
     this.missionName = missionName;
   }
 
   public String getComment() {
     return comment;
   }
 
   public void setComment(String comment) {
     this.comment = comment;
   }
 
   public boolean getIsOrgan() {
     return isOrgan;
   }
 
   public void setIsOrgan(boolean isOrgan) {
     this.isOrgan = isOrgan;
   }
 
   @Column(nullable = false)
   public boolean getIsActive() {
     return isActive;
   }
 
   public void setIsActive(boolean isActive) {
     this.isActive = isActive;
   }
 
  @ManyToMany(fetch = FetchType.EAGER, mappedBy = "missions")
   public Set<Position> getPositions() {
     return this.positions;
   }
 
   public void setPositions(Set<Position> positions) {
     this.positions = positions;
   }
 
   public void addPosition(Position position) {
     position.addMission(this);
   }
 
   public void addPositions(List<Position> organPositions) {
     for (Position position : organPositions) {
       position.addMission(this);
     }
   }
 
   public void clearPositions() {
     for (Position position : positions) {
       position.removeMission(this);
     }
   }
 }
