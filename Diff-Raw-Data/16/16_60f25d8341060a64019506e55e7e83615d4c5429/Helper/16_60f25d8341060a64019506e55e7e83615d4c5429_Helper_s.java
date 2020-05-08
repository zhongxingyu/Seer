 package ch.jmnetwork.cookieclicker.helper;
 
 import ch.jmnetwork.cookieclicker.achievement.AchievementEventHandler;
 import ch.jmnetwork.cookieclicker.ui.CCAchievementWindow;
 
 @SuppressWarnings("unused")
 public class Helper
 {
     public static Helper[] helpers = new Helper[10];
     public static int[] owned = new int[]
<<<<<<< HEAD
     { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
=======
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
>>>>>>> Crypted I/O, part 2 & a few other tweaks
     
     public float productivity;
     public long price, pBase;
     
     public int helperID;
     
     public EnumHelper type;
     
     public Helper(float prod, long pr, int ID)
     {
         productivity = prod;
         price = pBase = pr;
         helperID = ID;
     }
     
     public long getPriceForNextHelper()
     {
         return (long) (this.pBase * Math.pow(1.15F, Helper.owned[this.helperID]));
     }
     
     public void onBought()
     {
         Helper.owned[this.helperID]++;
     }
     
     public static float getCookieRate()
     {
         float returnValue = 0;
         
         for (int i = 0; i < helpers.length; i++)
             returnValue += (helpers[i].productivity * owned[i]);
         
         return returnValue;
     }
     
     public static void registerHelpers()
     {
         helpers[0] = new HelperClicker(0.1F, 15L, 0);
         helpers[1] = new HelperGrandma(0.5F, 100L, 1);
         helpers[2] = new HelperFarm(2F, 500L, 2);
         helpers[3] = new HelperFactory(10F, 3000L, 3);
         helpers[4] = new HelperMine(40F, 10000L, 4);
         helpers[5] = new HelperShipment(100F, 40000L, 5);
         helpers[6] = new HelperAlchemyLab(400F, 200000L, 6);
         helpers[7] = new HelperPortal(6666F, 1666666L, 7);
         helpers[8] = new HelperTimeMachine(98765F, 123456789L, 8);
         helpers[9] = new HelperCondenser(999999F, 3999999999L, 9);
     }
     
     public static long getPriceForHelper(int helperID)
     {
         return Helper.helpers[helperID].getPriceForNextHelper();
     }
 }
