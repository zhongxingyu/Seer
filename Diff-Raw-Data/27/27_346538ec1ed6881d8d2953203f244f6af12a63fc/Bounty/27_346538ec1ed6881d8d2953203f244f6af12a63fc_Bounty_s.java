 package to.joe.decapitation;
 
 import java.sql.Timestamp;
 import java.util.Comparator;
 import java.util.Date;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 public class Bounty {
 
     private int ID;
     private String issuer;
     private String hunted;
 
     public void setID(int iD) {
         ID = iD;
     }
 
     private double reward;
     private Date created;
     private String hunter;
     private Date turnedIn;
     private Date redeemed;
 
     public Bounty(int ID, String issuer, String hunted, double reward, Timestamp created, String hunter, Timestamp turnedIn, Timestamp redeemed) {
         this.ID = ID;
         this.issuer = issuer;
         this.hunted = hunted;
         this.reward = reward;
         this.created = created;
         this.hunter = hunter;
         this.turnedIn = turnedIn;
         this.redeemed = redeemed;
     }
     
     public Bounty(int ID, String issuer, String hunted, double reward, Date created, String hunter, Date turnedIn, Date redeemed) {
         this.ID = ID;
         this.issuer = issuer;
         this.hunted = hunted;
         this.reward = reward;
         this.created = created;
         this.hunter = hunter;
         this.turnedIn = turnedIn;
         this.redeemed = redeemed;
     }
 
     public Bounty(String issuer, String hunted, double reward) {
         this.issuer = issuer;
         this.hunted = hunted;
         this.reward = reward;
     }
     
     public static Bounty fromConfigurationSection (int id, ConfigurationSection section) {
         return new Bounty(id, section.getString("issuer"), section.getString("hunted"), section.getDouble("reward"), 
                 new Date(section.getLong("created")), section.getString("hunter"), new Date(section.getLong("turnedIn")), new Date(section.getLong("redeemed")));
     }
 
     public double getReward() {
         return reward;
     }
 
     public String getHunted() {
         return hunted;
     }
 
     public String getIssuer() {
         return issuer;
     }
 
     public void setHunted(String hunted) {
         this.hunted = hunted;
     }
 
     public void setHunter(String hunter) {
         this.hunter = hunter;
     }
 
     public void setTurnedIn(Date turnedIn) {
         this.turnedIn = turnedIn;
     }
 
     public void setRedeemed(Date redeemed) {
         this.redeemed = redeemed;
     }
 
     public int getID() {
         return ID;
     }
 
     public Date getCreated() {
         return created;
     }
 
     public void setReward(double reward) {
         this.reward = reward;
     }
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
     public String getHunter() {
         return hunter;
     }
 
     public Date getTurnedIn() {
         return turnedIn;
     }
 
     public Date getRedeemed() {
         return redeemed;
     }
     
    public static class BountyComparator implements Comparator<Bounty> {
 
         @Override
         public int compare(Bounty o1, Bounty o2) {
             return (o1.getReward() < o2.getReward()) ? -1 : (o1.getReward() > o2.getReward()) ? 1:0;
         }
         
     }
 
 }
