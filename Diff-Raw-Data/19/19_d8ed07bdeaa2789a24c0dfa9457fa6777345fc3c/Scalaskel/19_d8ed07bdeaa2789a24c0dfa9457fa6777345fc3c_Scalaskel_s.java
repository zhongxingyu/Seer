 package net.binout.codestory2013.scalaskel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Scalaskel {
 
     public List<ScalaskelResult> change(int nbCents) {
         List<ScalaskelResult> toReturn = new ArrayList<ScalaskelResult>();
 
         ScalaskelResult withFoo  = new ScalaskelResult();
         withFoo.setFoo(nbCents);
 
         toReturn.add(withFoo);
 
         if (nbCents == 7) {
             ScalaskelResult withBar  = new ScalaskelResult();
             withBar.setBar(1);
             toReturn.add(withBar);
         }
 
         if (nbCents > 7) {
              int nbBar = (nbCents / 7) -1;
              if (nbBar == 0) nbBar = 1;
              for (int i = 1; i<=nbBar; i++) {
                  List<ScalaskelResult> barChange = change(nbCents - 7 * i);
                  for (ScalaskelResult result : barChange) {
                      result.addBar(i);
                  }
                  toReturn.addAll(barChange);
              }
         }
 
         if (nbCents > 11) {
             int nbQix = (nbCents / 11) -1;
             if (nbQix == 0) nbQix = 1;
             for (int i = 1; i<=nbQix; i++) {
                 List<ScalaskelResult> qixChange = change(nbCents - 11 * i);
                 for (ScalaskelResult result : qixChange) {
                     result.addQix(i);
                 }
                 toReturn.addAll(qixChange);
             }
         }
 
         if (nbCents > 21) {
             int nbBaz = (nbCents / 21) -1;
             if (nbBaz == 0) nbBaz = 1;
             for (int i = 1; i<=nbBaz; i++) {
                 List<ScalaskelResult> bazChnage = change(nbCents - 21 * i);
                 for (ScalaskelResult result : bazChnage) {
                     result.addBaz(i);
                 }
                 toReturn.addAll(bazChnage);
             }
         }
 
         return toReturn;
     }
 }
