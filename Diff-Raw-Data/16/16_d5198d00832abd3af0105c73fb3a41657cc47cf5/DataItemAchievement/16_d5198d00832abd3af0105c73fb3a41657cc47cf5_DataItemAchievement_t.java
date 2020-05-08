 package gameCommons.balanceSettings;
 
 public class DataItemAchievement {
 	public String name;
 	public String awardName;
 	public int threshold;
 
    public DataItemAchievement() {}
     public DataItemAchievement(String name, String awardName, int threshold) {
         this.name = name;
         this.awardName = awardName;
         this.threshold = threshold;
     }
 }
