 public class Man{
   
   private final String NAME;
   private final ArrayList<Woman> PREF;
   private boolean _isMarried;
   private Woman _partner;
   
   public Man(String name, ArrayList<Woman> pref){
     NAME = name;
     PREF = pref;
    isMarried = false;
     _partner = null;
   }
   
   public boolean isMarried(){
     return _isMarried;
   }
   public void switchMarried(){
     _isMarried = ! _isMarried;
   }
   
   public ArrayList<Woman> getPref(){
     return PREF;
   }
   
   public Woman getPartner(){
     return _partner;
   }
   
   public void setPartner(Woman partner){
     _partner = partner;
   }
   
   // Rank "O" is top rank
   // O(n) 
   public int getRankof(Woman woman){
     for (int i = 0; i < PREF.size(); i++){
       if (PREF.get(i).equals(woman))
         return i;
     }
     return - 1;
   }
 }
 
  /*   
     Round 1
     Bob, MaryAnn   X
     Jack, MaryAnn
     Ping, Ginger  X
     
     Round 2
     Bob, Ginger
     
     Round 3
     Ping MaryAnn X
     
     Roudn 4
     Ping, Sarah  
     
     end: marriage is stable
     
     Names: 1,2,3....n
   */  
