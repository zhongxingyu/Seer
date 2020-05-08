 package RMON;
 
 import Structure.User;
 
 public class Alarm
 {
     private User user;
     private String name;
     private String threshold;
     private int type;
     private boolean threshold_crossed;
     
    public Alarm(User user, String name, String threshold, int type)
     {
         this.user = user;
         this.name = name;
         this.threshold = threshold;
         this.type = type;
         this.threshold_crossed = false;
     }
     
     public User getUser()
     {
         return user;
     }
     
     public String getName()
     {
         return name;
     }
     
     public String getThreshold()
     {
         return threshold;
     }
     
     public int getType()
     {
         return type;
     }
     
     public boolean has_crossed()
     {
         return threshold_crossed;
     }
     
     public void threshold_crossed()
     {
         threshold_crossed = true;
     }
     
     public void threshold_rescinded()
     {
         threshold_crossed = false;
     }
 }
