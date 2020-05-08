 public class Woman{
   
  private FINAL String NAME;
  private FINAL ArrayList<Man> PREF;
   private int[] _proposals;
   private boolean _isMarried;
   
   public Woman(String name, ArrayList<Man> pref){
     NAME = name;
     PREF = pref;
    isMarried = false;
    _proposals = new Man[pref.size()];
   }
   public boolean isMarried(){
     return _isMarried;
   }
   public void switchMarried(){
     _isMarried = ! _isMarried;
   }
   
   public ArrayList<Man> getPref(){
     return PREF;
   }
   
   public int[] getProp(){
     return _proposals;
   }
   
   public void addProp(Man man){
     _proposals[getRankofMan(man)] = 1;
   }
   
   // Rank "O" is top rank
   // O(n) 
   public int getRankofMan(Man man){
     for (int i = 0; i < PREF.size(); i++){
       if (PREF.get(i).equals(man))
         return i;
     }
     return - 1;
   }
 }
 
     
  
