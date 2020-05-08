 package petrinet.gui;
 
 import petrinet.logic.Arc;
 import petrinet.logic.InhibitorArc;
 import petrinet.logic.Petrinet;
 import petrinet.logic.Place;
 import petrinet.logic.Transition;
 
 public class PNGuiTest {
 
     public static void main(String[] args) {
         Petrinet pn = new Petrinet("mein Netz");
         Transition t1 = pn.addTransition("t1");
         Place p1 = pn.addPlace("p1", 2);
         Place p2 = pn.addPlace("p2");
         Arc a1 = pn.addArc("a1", p1,t1);
         Arc a2 = pn.addArc("a2", t1,p2);
         a2.setWeight(2);
         
         Transition t2 = pn.addTransition("t2");
         Place p3 = pn.addPlace("p3");
         Arc a3 = pn.addArc("a3", p2, t2);
         Arc a4 = pn.addArc("a4", t2, p3);
         
         InhibitorArc inhibitor = pn.addInhibitorArc("inhibitor", p3, t1);
 
        xxxPetrinetGUI.displayPetrinet(pn);
     }
     
 }
