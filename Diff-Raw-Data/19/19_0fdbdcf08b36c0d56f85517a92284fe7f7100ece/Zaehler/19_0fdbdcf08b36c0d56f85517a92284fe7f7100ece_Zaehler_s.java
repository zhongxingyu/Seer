 package petrinet.gui;
 
 import petrinet.logic.Arc;
 import petrinet.logic.Petrinet;
 import petrinet.logic.Place;
 import petrinet.logic.Transition;
 
 public class Zaehler {
 
 
     /**
      * Counts from 0 to 5.
      *
      * Each counter itteration is a transition
      *
      * @param args
      */
 
     public static void main(String[] args) {
 
         Petrinet pn = new Petrinet("Zaehler");
 
         Place vorgaenger = null;
 
         Transition t0 = null;
 
         for (int i = 0; i < 8 ; i++) {
             Transition t = pn.addTransition("Transition #" + i);
             if (null != vorgaenger) {
                 Arc a = pn.addArc("Arc", vorgaenger, t);
             }
             if (0 == i) {
                 t0 = t;
             }
 
             Place p = pn.addPlace("Place #" + i);
             p.setMaxTokens(1);
             Arc a = pn.addArc("Arc", t, p);
             pn.addInhibitorArc("inhibitor", p, t0);
 
             vorgaenger = p;
         }
 
        xxxPetrinetGUI.displayPetrinet(pn);
     }
 }
