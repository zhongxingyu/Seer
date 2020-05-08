 package tache;
 
 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author modamod
  */
 public class Ingenieur extends Personne{
     
     private final int NB_TACHE_MAX = 5;
     private String specialite;
     private String typeContrat;
     private int nbrTache;
     private Tache[] listeTaches = new Tache[NB_TACHE_MAX];
 
     public Ingenieur() {
         this.nbrTache = 0;
     }
 
     public Ingenieur(String specialite, String typeContrat, String cin, String nom, String prenom) {
         super(cin, nom, prenom);
         this.specialite = specialite;
         this.typeContrat = typeContrat;
         this.nbrTache = 0;
     }
 
     public String getSpecialite() {
         return specialite;
     }
 
     public void setSpecialite(String specialite) {
         this.specialite = specialite;
     }
 
     public String getTypeContrat() {
         return typeContrat;
     }
 
     public void setTypeContrat(String typeContrat) {
         this.typeContrat = typeContrat;
     }
 
     @Override
     public String toString() {
        String str = "Ingenieur{" + super.toString() + " specialite=" + specialite + ", typeContrat=" + typeContrat + " Liste Tache \n";
         for(int i =0; i < this.nbrTache; i++){
             str += listeTaches[i].toString() + "\n";
         }
         return str;
         
     }
     
     public void ajouterTache(Tache tache){
         System.out.println("En cours d'ajout d'une tache a cet ingenieur");
         if(this.nbrTache == NB_TACHE_MAX){
             System.err.println("Echec on peut pas ajouter une autre tache a cet ingenieur");
             System.exit(-1);
         }
         this.listeTaches[nbrTache] = tache;
         this.nbrTache++;
         System.out.println("L'ajout d'une tache a cet ingenieur a etait effectuer"
                 + " avec success");
     }
     public Tache chercherTache(int idTache){
         for(int i = 0; i < nbrTache; i++){
             if (this.listeTaches[i].getIdTache() == idTache)
                 return this.listeTaches[i];
         }
         return null;
     }
         
     public void supprimerTache(Tache tache){
         System.out.println("En cours d'ajout d'une tache a cet ingenieur");
         Tache res = chercherTache(tache.getIdTache());
         if (res == null){
             System.err.println("Echeck de suppression de tache.");
             System.exit(-1);
         } 
         for (int i = 0; i < nbrTache; i++){
             if (Tache.comparerTache(listeTaches[i], res) == true){
                 listeTaches[i] = listeTaches[nbrTache - 1];
                 listeTaches[nbrTache - 1] = null;
                 nbrTache--;
                 break;
             }
         }
         System.out.println("La suppression de la tache a ete effectue avec succes");
     }
     
     
 }
