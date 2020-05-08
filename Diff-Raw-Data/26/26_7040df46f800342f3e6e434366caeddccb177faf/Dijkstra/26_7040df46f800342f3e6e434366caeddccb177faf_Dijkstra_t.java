 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package dijkstra;
 
 import java.util.HashMap;
 import java.util.Set;
 
 /**
  *
  * @author Lukas Baron, Daniel Kolz
  */
 public class Dijkstra {
 
     private HashMap<String, KnotenZustand> dijkstra;
     private LinkStates linkStates;
     private Set<String> knotenSet;
     
 
     public Dijkstra() {
         linkStates = new LinkStates();
         linkStates.matrixDrucken();
         knotenSet = linkStates.getAlleKnoten();
 
         dijkstra = new HashMap<String, KnotenZustand>(knotenSet.size());
         for (String knoten:knotenSet){
             dijkstra.put(knoten, new KnotenZustand(1000000, "", "-"));
         }
         dijkstra.get("a").setQs("q");
         dijkstra.get("a").setDist(0);
 
         while(nichtAlleKnotenSettled()){
            String knoten = extracMinimum();
            relaxNeighbours(knoten);
         }
 
         /*setze die Distanzen aller Knoten auf unendlich: z.B. $dist = 1000000$
         setze den Vorgaengerknoten aller Knoten auf "" (leerer String)
         setze die offen/erledigt-Eigenschaft (qs) aller Knoten auf "-"
         setze qs des Startknotens auf "q"
         setze die Distanz dist des Startknotens auf dist=0
         while( es gibt noch knoten, bei denen qs=="q" ist ){
             knoten u = extractMinimum();
             relaxNeighbors(u);
         }
 */
 
     }
 
     public void druckeZustaende(){
 
     }
 
     public boolean nichtAlleKnotenSettled(){
        boolean notAlleSettled = false;
         for (String knoten : knotenSet){
             if (!dijkstra.get(knoten).getQs().equals("s")){
                notAlleSettled = true;
             }
         }
        return notAlleSettled;
     }
 
     public String extracMinimum(){
   /*    finde den Knoten u mit minimaler Distanz dist aus der Menge
         aller Knoten, bei denen qs=="q" ist ;
         u.setQs("s");
         return u;*/
         String knoten = "a";
         int minimum = 10000000;
         for (String knot : knotenSet){
             if (dijkstra.get(knot).getQs().equals("q")){
                 if (dijkstra.get(knot).getDist() < minimum){
                     minimum = dijkstra.get(knot).getDist();
                     knoten = knot;
                 }
             }
         }
         dijkstra.get(knoten).setQs("s");
         return knoten;
     }
 
     public void relaxNeighbours(String u){
            //foreach Knoten v, v.getQs() ist nicht "s" && v ist nachbar von u{
            for(String v : knotenSet) {
                if (dijkstra.get(v).getDist() > dijkstra.get(u).getDist() + linkStates.getDistanz( u,v) ){
                    dijkstra.get(v).setDist( dijkstra.get(u).getDist() + linkStates.getDistanz(u,v) );
                    dijkstra.get(v).setPre(u);
                    dijkstra.get(v).setQs("q");
                }
            }
     }
 
}
