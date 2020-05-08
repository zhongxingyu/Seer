 package plateau;
 
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.Collections;
 import plateau.action.CaseAction;
 
 public class Periode {
     private int numero;
     private ArrayList<Tour> tours;
     //prévoir hashmap joueur, élements . Exemple : 1 pt de nourriture sur les num pair
     
     public Periode(int numero, ArrayList<CaseAction> liste_actions){
         this.numero = numero;
         ArrayList<CaseAction> liste_melangee = new ArrayList<CaseAction>(liste_actions);
         Collections.shuffle(liste_melangee);
         for(int i = 0; i < liste_melangee.size(); i++){
             tours.add(new Tour(i+1, liste_melangee.get(i)));
         }
     }
     
     public ArrayList<Tour> getTours(){
         return this.tours;
     }
     
     public Tour getTour(int index){
         return this.tours.get(index);
     }
 }
