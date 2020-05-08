 package Dama;
 
 import javax.swing.ImageIcon;
 
 public class Pedina {
    
     public static enum Colori {
         BIANCO,
         NERO,
         NULL;
         @Override
         public String toString() {
           switch(this) {
             case BIANCO: return "BIANCO";
             case NERO: return "NERO";
             case NULL: return "NULL";    
             default: throw new IllegalArgumentException();
           }
         }
     };
     
     private Colori colore;
     
     public Pedina(Colori pColore) {
         this.colore = pColore;
     }
 
     public Colori getColore() {
         return this.colore;
     }
     
     public ImageIcon getImgColore(Colori pColore) {
         if (pColore.equals(Colori.BIANCO)){
             return new ImageIcon(getClass().getResource("/Images/pedina_bianca.png"));
         }else if (pColore.equals(Colori.NERO)){
             return new ImageIcon(getClass().getResource("/Images/pedina_nera.png"));
         } else {
             return new ImageIcon(getClass().getResource("/Images/pedina_vuota.png"));
         }
     }
     
 }
