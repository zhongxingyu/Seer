 /**
  * Jour - INF2015 - TP Agile - EQUIPE 17
  *
  * @author Francois Dufault
  * @author Lyes Tamazouzt
  * @author Abdessamad Essakhi
  */
 package inf2015.tp;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Jour {
 
     private static final int MINUTES_JOURNEE_FERIEE = 480;
     private static final int MINUTES_JOURNEE_MALADIE = 480;
     private static final int MINUTES_JOURNEE_CONGE_PARENTAL = 480;
     private static final int MINUTES_JOURNEE_CONGE_VACANCES = 480;
     private static final int MAX_MINUTES_PAR_JOURS = 24 * 60;
     private static final int MAX_MINUTES_PAR_JOURS_AVEC_CONGE = 32 * 60;
     private ArrayList<Projet> projetsJournee = new ArrayList<>();
     private TypeJour typeJournee;
     private String nomJour;
 
     protected Jour(String nomJour, TypeJour typeJournee) {
         this.typeJournee = typeJournee;
         this.nomJour = nomJour;
     }
 
     public int getMinutesJourneeFeriee() {
         int minutes = 0;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeFerie()) {
                 minutes += projet.getMinutes();
             }
         }
 
         return minutes;
     }
 
     public void verifierMinutes(int minutesJournee) {
         if (this.estJourneeVacances() || this.estJourneeFerie()) {
             if (minutesJournee > MAX_MINUTES_PAR_JOURS_AVEC_CONGE) {
                 ErreurJournal.Instance().ajoutErreur("Erreur : " + minutesJournee + " dépasse le maximum autorisé (" + MAX_MINUTES_PAR_JOURS_AVEC_CONGE + ")");
             }
         } else {
             if (minutesJournee > MAX_MINUTES_PAR_JOURS) {
                 ErreurJournal.Instance().ajoutErreur("Erreur : " + minutesJournee + " dépasse le maximum autorisé (" + MAX_MINUTES_PAR_JOURS + ")");
             }
         }
     }
 
     public int getMinutesParJour() {
         int minutes = 0;
         for (Projet projet : this.projetsJournee) {
             minutes += projet.getMinutes();
         }
         return minutes;
     }
 
     public int getMinutesJourneeVacance() {
         int minutes = 0;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeVacance()) {
                 minutes += projet.getMinutes();
             }
         }
 
         return minutes;
     }
 
     public int getMinutesJourneeMaladie() {
         int minutes = 0;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeMaladie()) {
                 minutes += projet.getMinutes();
             }
         }
 
         return minutes;
     }
 
     public int getMinutesTeletravail() {
         int minutes = 0;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estTeleTravail()) {
                 minutes = minutes + projet.getMinutes();
             }
         }
 
         return minutes;
     }
 
     public int getMinutesBureau() {
         int minutes = 0;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estTravailBureau() || projet.estCongeFerie() || projet.estCongeVacance() || projet.estCongeParental() || projet.estCongeMaladie()) {
                 minutes = minutes + projet.getMinutes();
             }
 
         }
         return minutes;
     }
 
     public int getMinutesJourneeCongeParental() {
         int minutes = 0;
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeParental()) {
                 minutes = minutes + projet.getMinutes();
             }
         }
 
         return minutes;
     }
 
     public boolean estJourneeVacances() {
         boolean estVacances = false;
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeVacance()) {
                 estVacances = true;
             }
         }
 
         return estVacances;
     }
 
     public boolean estJourneeFerie() {
         boolean estFerie = false;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeFerie()) {
                 estFerie = true;
             }
         }
 
         return estFerie;
     }
 
     public boolean estJourMaladie() {
         boolean estMaladie = false;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeMaladie()) {
                 estMaladie = true;
             }
         }
 
         return estMaladie;
     }
 
     public boolean estJourneeCongeParental() {
         boolean estCongeParental = false;
 
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estCongeParental()) {
                 estCongeParental = true;
             }
         }
 
 
         return estCongeParental;
     }
 
     public boolean contientTeleTravail() {
         boolean estTeleTravail = false;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estTeleTravail()) {
                 estTeleTravail = true;
             }
         }
 
         return estTeleTravail;
     }
 
     public boolean contientTravailBureau() {
         boolean estTravailBureau = false;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.estTravailBureau()) {
                 estTravailBureau = true;
             }
         }
 
         return estTravailBureau;
     }
 
     public boolean contientAutresProjetsQue(int projetID) {
         boolean contientAutreProjet = false;
 
         for (Projet projet : this.projetsJournee) {
             if (projet.getProjetID() != projetID) {
                 contientAutreProjet = true;
             }
         }
 
         return contientAutreProjet;
     }
 
     public void ajoutProjet(Projet nouveauProjet) {
         this.projetsJournee.add(nouveauProjet);
     }
 
     public void analyserJour() throws IOException {
 
         if (this.estJourneeVacances()) {
             this.analyserJourVacances();
         } else if (this.estJourneeCongeParental()) {
             this.analyserJourParental();
         } else if (this.estJourneeFerie()) {
             this.analyserJourFerie();
         } else if (this.estJourMaladie()) {
             this.analyserJourMaladie();
         }
     }
 
     public boolean estJourOuvrable() {
         return (this.typeJournee == TypeJour.OUVRABLE);
     }
 
     @Override
     public String toString() {
         return String.format("%s | Type: %s", this.nomJour, this.typeJournee);
     }
 
     protected void analyserJourFerie() {
         if (this.typeJournee == TypeJour.WEEKEND) {
             ErreurJournal.Instance().ajoutErreur(String.format("\nLe jour \"%s\" qui est férié ne doit pas être le weekend.", this.nomJour));
         }
 
 
         comparerJourSpecialEtMinutesRequis(this.nomJour, "férié", this.getMinutesJourneeFeriee(), Jour.MINUTES_JOURNEE_FERIEE);
     }
 
     protected void analyserJourMaladie() {
 
         if (this.typeJournee == TypeJour.WEEKEND) {
            ErreurJournal.Instance().ajoutErreur(String.format("\nLe jour \"%s\" qui est %s ne doit pas être le weekend.", this.nomJour, "maladie"));
         }
         if (this.contientAutresProjetsQue(Projet.PROJET_ID_CONGE_MALADIE)) {
             System.out.println("\nLe jour " + this.nomJour + " a d'autre code de projet dans la même journée.");
         }
 
         comparerJourSpecialEtMinutesRequis(this.nomJour, "maladie", this.getMinutesJourneeMaladie(), Jour.MINUTES_JOURNEE_MALADIE);
     }
 
     protected void analyserJourVacances() {
 
         if (this.typeJournee == TypeJour.WEEKEND) {
             ErreurJournal.Instance().ajoutErreur(String.format("\nLe jour \"%s\" qui est %s ne doit pas être le weekend.", this.nomJour, "vacances"));
         }
 
         comparerJourSpecialEtMinutesRequis(this.nomJour, "vacance", this.getMinutesJourneeVacance(), Jour.MINUTES_JOURNEE_CONGE_VACANCES);
     }
 
     protected void analyserJourParental() {
 
         if (this.typeJournee == TypeJour.WEEKEND) {
             ErreurJournal.Instance().ajoutErreur(String.format("\nLe jour \"%s\" qui est %s ne doit pas être le weekend.", this.nomJour, "Congé parental"));
         }
         if (this.contientAutresProjetsQue(Projet.PROJET_ID_CONGE_PARENTAL)) {
             ErreurJournal.Instance().ajoutErreur(String.format("\nLe jour \"%s\" qui est congé parental ne doit pas avoir d'autre projet dans la même journée.", this.nomJour));
         }
 
         comparerJourSpecialEtMinutesRequis(this.nomJour, "congé parental", this.getMinutesJourneeCongeParental(), Jour.MINUTES_JOURNEE_CONGE_PARENTAL);
     }
 
     public enum TypeJour {
 
         OUVRABLE,
         WEEKEND
     }
 
     public static Jour CreerJour(String nomJour) {
         Jour jour;
 
         if (nomJour.startsWith("jour")) {
             jour = new Jour(nomJour, TypeJour.OUVRABLE);
         } else {
             jour = new Jour(nomJour, TypeJour.WEEKEND);
         }
 
         return jour;
     }
 
     protected static void comparerJourSpecialEtMinutesRequis(String nomJour, String typeJourSpecial, int jourMinutes, int jourMinutesRequis) {
 
         if (jourMinutes != jourMinutesRequis) {
             ErreurJournal.Instance().ajoutErreur(String.format("Le jour \"%s\" qui est %s, doit contenir %d minutes. (Il contient %d minutes.)",
                     nomJour, typeJourSpecial, jourMinutesRequis, jourMinutes));
         }
 
     }
 }
