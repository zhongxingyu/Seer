 import org.apache.commons.math.util.MathUtils;
 
 public class CampLunches {
 
     /**
      * @param args
      */
     public static void main(final String[] args) {
         String[] menu1 = {"pbj", "pizza"};
         String[] menu2 = {"pbj", "pizza"};
         System.out.println(new CampLunches().firstMatching(menu1, menu2));
 
         String[] menu3 = {"pbj", "pizza"};
         String[] menu4 = {"pizza", "pbj"};
         System.out.println(new CampLunches().firstMatching(menu3, menu4));
         
         String[] menu5 = {"pbj", "pizza"};
         String[] menu6 = {"pizza", "pbj", "pizza"};
         System.out.println(new CampLunches().firstMatching(menu5, menu6));
         
         String[] menu7 = {"pbj"};
         String[] menu8 = {"pizza", "tuna", "pbj"};
         System.out.println(new CampLunches().firstMatching(menu7, menu8));
         
         String[] menu9 = {"pizza", "pbj", "meatballs", "peanut butter and jelly", "pizza hero"};
         String[] menu10 = {"pbj", "meatballs", "peanut butter and jelly", "pizza hero"};
         System.out.println(new CampLunches().firstMatching(menu9, menu10));
         
         String[] menu11 = {"pizza"};
         String[] menu12 = {"pizza ", "pizza"};
         System.out.println(new CampLunches().firstMatching(menu11, menu12));
     }
 
     public int firstMatching(final String[] menu1, final String[] menu2) {
         int menu1Size = menu1.length;
         int menu2Size = menu2.length;
         int lcm = MathUtils.lcm(menu1Size, menu2Size);
 
         for (int i = 0; i < lcm; i ++) {
             if (menu1[i % menu1Size].equals(menu2[i % menu2Size])) {
                 return i;
             }
         }
         
         return -1;
     }
 }
