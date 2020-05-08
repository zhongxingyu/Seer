 package tpneuneu;
 
 import graphisme.Fenetre;
 import java.awt.Color;
 import java.util.LinkedList;
 
 /**
  *
  * @author Nicolas
  */
 public class TpNeuneu {
 
     /**
      * @param args the command line arguments
      */
     
     //CLASSE PRINCIPALE
     public static void main(String[] args) {
         
        
        

        
         /**
          * Creation of the display
          */
         
         Fenetre fen = new Fenetre();
         fen.getJCanvas().setBackground(Color.WHITE);
         /**
          * Initialisation of the Loft (includes its inhabitants) 
          * /!\ The size of the plateau need to be set to (20,20)
          */
         Loft loft = new Loft(20,20,fen.getJCanvas());
         
         while(!loft.population.isEmpty()){
             /**
              * each Neuneu plays one time
              */
             LinkedList<Mangeable> aSupp;
             LinkedList<Neuneu> neuneuSupp = new LinkedList<>();
 
             for (Neuneu joueur : loft.population){
                 aSupp = new LinkedList<>();
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e){
                     e.printStackTrace();
                 }
                 int a = joueur.getPosX();
                 int b = joueur.getPosY();
                 //remove Neuneu from ListPresence as he is going to move
                 loft.plateau[a][b].listPresence.remove((Mangeable)joueur);
                 //the neuneu moves
                 joueur.seDeplacer();
                 //the neuneu is added to ListPresence of his new position
                 int c = joueur.getPosX();
                 int d = joueur.getPosY();
                 joueur.majPresence(loft.plateau[c][d]);
 
                 //we populate a list of Nourriture and Neuneu which are dead in the new case
                 for(Mangeable element : loft.plateau[c][d].listPresence){
                     if (element.niveau==0){
                        aSupp.add((Mangeable)element);
                     }
                 }
                 //we remove those elements from the new case
                 for(Mangeable element : aSupp){
                     loft.plateau[c][d].listPresence.remove(element);
                 }
 
                if (joueur.niveau==0){
                        neuneuSupp.add((Neuneu)joueur);
                        joueur.setPosX(1000);
                        joueur.setPosY(1000);
                     }
                 //display is refreshed
                 fen.repaint();
                 }
             for (Neuneu element : neuneuSupp){
                 loft.population.remove(element);
             }
         }
         System.out.println("fini");
     }
 }
