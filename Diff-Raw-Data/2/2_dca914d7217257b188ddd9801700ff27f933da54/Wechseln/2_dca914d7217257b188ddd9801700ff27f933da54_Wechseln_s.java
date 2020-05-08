 package ro.inf.p2.uebung02;
 
 /**
  * Created with IntelliJ IDEA.
  * User: felix
  * Date: 3/28/13
  * Time: 11:41 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Wechseln {
 
     public static final int[] werte =
             new int[]{200, 100, 50, 20, 10, 5, 2, 1};
 
     public static int[] anzahl(int betrag) {
 
        int[] array = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
 
         int i = 0;
 
         for (int wert:werte) {
             while (betrag >= wert) {
                 betrag -= wert;
                 array[i] += 1;
             }
             i++;
         }
         return array;
     }
 }
