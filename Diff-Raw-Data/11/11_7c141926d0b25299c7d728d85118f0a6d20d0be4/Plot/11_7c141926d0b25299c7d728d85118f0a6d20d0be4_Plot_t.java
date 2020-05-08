 package ch.minepvp.spout.kingdoms.database.table;
 
 import ch.minepvp.spout.kingdoms.Kingdoms;
 import com.alta189.simplesave.Field;
 import com.alta189.simplesave.Id;
 import com.alta189.simplesave.Table;
 import org.spout.api.geo.discrete.Point;
 
 @Table("Plot")
 public class Plot {
 
     @Id
     private int id;
 
     @Field
     private int kingdom;
 
     @Field
     private int owner = 0;
 
     // Corner One
     @Field
     private Integer cornerOneX;
 
     @Field
     private Integer cornerOneY;
 
     @Field
     private Integer cornerOneZ;
 
     // Corner One
     @Field
     private Integer cornerTwoX;
 
     @Field
     private Integer cornerTwoY;
 
     @Field
     private Integer cornerTwoZ;
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getKingdom() {
 
         Kingdom kingdom = Kingdoms.getInstance().getDatabase().select(Kingdom.class).where().equal("id", this.kingdom).execute().findOne();
 
         if ( kingdom != null ) {
             return kingdom.getName();
         }
 
         return null;
     }
 
     public void setKingdom(int kingdom) {
         this.kingdom = kingdom;
     }
 
     public String getOwner() {
 
         Member member = Kingdoms.getInstance().getDatabase().select(Member.class).where().equal("id", this.owner).execute().findOne();
 
         if ( member != null ) {
             return member.getName();
         }
 
         return null;
     }
 
     public void setOwner(int owner) {
         this.owner = owner;
     }
 
     public Integer getCornerOneX() {
         return cornerOneX;
     }
 
     public void setCornerOneX(Integer cornerOneX) {
         this.cornerOneX = cornerOneX;
     }
 
     public Integer getCornerOneY() {
         return cornerOneY;
     }
 
     public void setCornerOneY(Integer cornerOneY) {
         this.cornerOneY = cornerOneY;
     }
 
     public Integer getCornerOneZ() {
         return cornerOneZ;
     }
 
     public void setCornerOneZ(Integer cornerOneZ) {
         this.cornerOneZ = cornerOneZ;
     }
 
     public Point getCornerOne() {
         return new Point( Kingdoms.getInstance().getEngine().getWorld("world"), getCornerOneX(), getCornerOneY(), getCornerOneZ() );
     }
 
     public Integer getCornerTwoX() {
         return cornerTwoX;
     }
 
     public void setCornerTwoX(Integer cornerTwoX) {
         this.cornerTwoX = cornerTwoX;
     }
 
     public Integer getCornerTwoY() {
         return cornerTwoY;
     }
 
     public void setCornerTwoY(Integer cornerTwoY) {
         this.cornerTwoY = cornerTwoY;
     }
 
     public Integer getCornerTwoZ() {
         return cornerTwoZ;
     }
 
     public void setCornerTwoZ(Integer cornerTwoZ) {
         this.cornerTwoZ = cornerTwoZ;
     }
 
     public Point getCornerTwo() {
         return new Point( Kingdoms.getInstance().getEngine().getWorld("world"), getCornerTwoX(), getCornerTwoY(), getCornerTwoZ() );
     }
 
     public boolean contains(Point point) {
 
        if ( getCornerTwoX() >= point.getBlockX() && getCornerOneX() <= point.getBlockX()) {

            if ( getCornerTwoZ() >= point.getBlockZ() && getCornerOneZ() <= point.getBlockZ() ) {

                if ( getCornerTwoY() >= point.getBlockY() && getCornerOneY() <= point.getBlockY() ) {
                    return true;
                }
 
             }
 
         }
 
         return false;
     }
 
 }
