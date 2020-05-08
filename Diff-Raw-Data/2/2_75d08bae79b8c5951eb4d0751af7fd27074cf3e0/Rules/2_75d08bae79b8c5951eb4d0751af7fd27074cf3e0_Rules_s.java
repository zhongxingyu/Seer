 package RedPointMaven;
 
 public class Rules {
     //test 1 of 3 - is giver giving to self?
     public static boolean giveeNotSelf(String giver, String givee) {
         return !giver.equals(givee);
     }
 
     //test 2 of 3 - is givee giving to giver?
     public static boolean giveeNotRecip(String giver, String givee, Roster roster, int thisYear) {
         String giveeGivingTo;
         giveeGivingTo = roster.returnGivee(givee, thisYear);
         return !giver.equals(giveeGivingTo);
     }
 
    //test 3 of 3 - has giver given to givee in past 4 years/
     public static boolean giveeNotRepeat(String giver, String givee, Roster roster, int thisYear) {
         int counter;
         String giveeInYear;
         boolean result = true;
         for (counter = thisYear - 1; (counter >= 0) && (counter >= (thisYear - 4)); counter--) {
             giveeInYear = roster.returnGivee(giver, counter);
             if (givee.equals(giveeInYear)) {
                 result = false;
             }
         }
         return result;
     }
 }
