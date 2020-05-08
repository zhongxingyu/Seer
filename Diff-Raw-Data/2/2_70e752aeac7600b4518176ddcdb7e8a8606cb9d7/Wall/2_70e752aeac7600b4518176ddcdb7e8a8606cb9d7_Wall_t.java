 package ai.ilikeplaces.entities;
 
 
 import ai.ilikeplaces.entities.etc.*;
 import ai.scribble.*;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Jan 25, 2010
  * Time: 1:01:22 PM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Entity
 public class Wall implements Clearance, Refreshable<Wall>, Serializable {
     public Long wallId = null;
     public static String wallIdCOL = "wallId";
     public Long clearance = 0L;
     public String wallContent = null;
 
     @RefreshId("wallMsgs")
     public List<Msg> wallMsgs = null;
 
     @RefreshId("wallMutes")
     public List<Mute> wallMutes = null;
     public Integer wallType = null;
     public String wallMetadata = null;
 
     final static public int wallTypeMISC = 0;
     final static public int wallTypeHuman = 1;
     final static public int wallTypePrivateEvent = 2;
     final static public int wallTypeTribe = 3;
     final static public int wallTypePrivatePhoto = 4;
 
 
 //    final static public int WALL_LENGTH = 10240;
 
     private static final Refresh<Wall> REFRESH = new Refresh<Wall>();
 
     public static enum WallMetadataKey {
         HUMAN,
         PRIVATE_PHOTO,
         PRIVATE_EVENT,
         TRIBE,
     }
 
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getWallId() {
         return wallId;
     }
 
     public void setWallId(Long wallId) {
         this.wallId = wallId;
     }
 
 //    @Column(length = WALL_LENGTH)
 //    public String getWallContent() {
 //        return wallContent;
 //    }
 //
 //    public void setWallContent(String wallContent) {
 //        this.wallContent = wallContent;
 //    }
 
     @Transient
    public Wall setWallContentR(String wallContent) {
         this.wallContent = wallContent;
         return this;
     }
 
     @_doc(
             NOTE = @_note("Wall msgs are fetched lazy because sometimes private event is required to be loaded fast, and fetches wall, thereby wall msgs.")
     )
     @_fix(issue = "Find out how to limit resultset to say, last 20, in order to limit the results fetched")
     @_unidirectional
     @_todo(task = "Move DESC ASC TO SOME STATIC CLASS FOR REUSE")
     @OrderBy(Msg.msgIdCOL + " DESC")
     @OneToMany(
             cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE},
             fetch = FetchType.LAZY)
     public List<Msg> getWallMsgs() {
         return wallMsgs;
     }
 
     public void setWallMsgs(final List<Msg> wallMsgs) {
         this.wallMsgs = wallMsgs;
     }
 
     @_unidirectional
     @OneToMany(
             cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE},
             fetch = FetchType.LAZY)
     public List<Mute> getWallMutes() {
         return wallMutes;
     }
 
     public void setWallMutes(final List<Mute> wallMutes) {
         this.wallMutes = wallMutes;
     }
 
     @Override
     public Long getClearance() {
         return this.clearance;
     }
 
     public Integer getWallType() {
         return wallType;
     }
 
     public void setWallType(final Integer wallType) {
         this.wallType = wallType;
     }
 
     public Wall setWallTypeR(final Integer wallType) {
         this.wallType = wallType;
         return this;
     }
 
     public String getWallMetadata() {
         return wallMetadata;
     }
 
     public void setWallMetadata(final String wallMetadata) {
         this.wallMetadata = wallMetadata;
     }
 
     @Override
     public void setClearance(final Long clearance) {
         this.clearance = clearance;
     }
 
     @Override
     public Wall refresh(final RefreshSpec refreshSpec) throws RefreshException {
         REFRESH.refresh(this, refreshSpec);
         return this;
     }
 
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         final Wall wall = (Wall) o;
 
         if (wallId != null ? !wallId.equals(wall.wallId) : wall.wallId != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return wallId != null ? wallId.hashCode() : 0;
     }
 
     @Override
     public String toString() {
         return "Wall{" +
                 "wallId=" + wallId +
                 ", clearance=" + clearance +
                 ", wallType=" + wallType +
                 ", wallMetadata='" + wallMetadata + '\'' +
                 '}';
     }
 
     @Transient
     public String getMetadataValueFor(final WallMetadataKey keyEnum){
         final String key = keyEnum.toString();
         final String wallMetadata = this.getWallMetadata();
         final String returnVal;
         if (wallMetadata == null || wallMetadata.isEmpty()) {
             returnVal = null;
         } else {
             final String[] pairs = wallMetadata.split(",");
             String value = null;
             for (final String pairString : pairs) {
                 final String[] pair = pairString.split("=");
                 if (key.equals(pair[0])) {
                     value = pair[1];
                 }
             }
             returnVal = value;
         }
         return returnVal;
     }
 
 }
