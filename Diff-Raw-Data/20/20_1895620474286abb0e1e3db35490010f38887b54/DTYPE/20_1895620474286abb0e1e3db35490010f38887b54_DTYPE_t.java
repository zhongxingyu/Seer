 package mjc.gc;
 
 public class DTYPE {
     // inutilisable avec EGG
     // final private DTYPE entier = new DTYPE("entier",4);
     // public DTYPE getTypeEntier(){return entier;}
     // final private DTYPE booleen = new DTYPE("booleen",1);
     // public DTYPE getTypeBooleen(){return booleen;}
     protected int taille;
     protected String nom;
 
     public int getTaille() {
         return taille;
     }
 
     public void setTaille(int t) {
         taille = t;
     }
 
     public String getNom() {
         return nom;
     }
 
     public void setNom(String n) {
         nom=n;
     }
 
     public DTYPE(String n, int t) {
         nom = n;
         taille = t;
     }
 
     public boolean equals(DTYPE autre) {
         return nom.equals(autre.nom) & taille==autre.getTaille();
     }
 
    public boolean canAccept(DTYPE autre) {
        return equals(autre);
    }

     public String toString() {
         return nom + "(" + taille + ")";
     }
 }
