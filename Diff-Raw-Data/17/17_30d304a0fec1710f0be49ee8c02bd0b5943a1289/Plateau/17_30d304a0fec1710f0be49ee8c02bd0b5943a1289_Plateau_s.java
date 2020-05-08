 package plateau;
 
 import carte.AmenagementMajeur;
 import carte.*;
 import java.util.ArrayList;
 import plateau.action.Action1Argile;
 import plateau.action.Action1Boeuf;
 import plateau.action.Action1Cereale;
 import plateau.action.Action1Legume;
 import plateau.action.Action1Mouton;
 import plateau.action.Action1Pierre;
 import plateau.action.Action1Roseau;
 import plateau.action.Action1Sanglier;
 import plateau.action.Action3Bois;
 import plateau.action.ActionAmenagement;
 import plateau.action.ActionCloture;
 import plateau.action.ActionEtablePain;
 import plateau.action.ActionJournalier;
 import plateau.action.ActionLabourageSemailles;
 import plateau.action.ActionLabourer;
 import plateau.action.ActionNaissance;
 import plateau.action.ActionNaissanceSansPlaceLibre;
 import plateau.action.ActionPeche;
 import plateau.action.ActionPieceEtable;
 import plateau.action.ActionPremierJoueur;
 import plateau.action.ActionRenovation;
 import plateau.action.ActionRenovationCloture;
 import plateau.action.ActionSemailles;
 import plateau.action.CaseAction;
 
 public class Plateau {
     private ArrayList<Periode> periodes;
     private ArrayList<AmenagementMajeur> amenagements;
     private ArrayList<CaseAction> actions_fixes;
     
     public Plateau(){
         this.periodes = new ArrayList<Periode>(6);
         
         //add période 1 : 4 tours 
         ArrayList<CaseAction> liste_periode_1 = new ArrayList<CaseAction>();
         liste_periode_1.add(new Action1Mouton());
         liste_periode_1.add(new ActionCloture());
         liste_periode_1.add(new ActionSemailles());
         liste_periode_1.add(new ActionAmenagement());
         this.periodes.add(new Periode(1, liste_periode_1));
         
         //add période 2 : 3 tours 
         ArrayList<CaseAction> liste_periode_2 = new ArrayList<CaseAction>();
         liste_periode_2.add(new Action1Pierre());
         liste_periode_2.add(new ActionNaissance());
         liste_periode_2.add(new ActionRenovation());
         this.periodes.add(new Periode(2, liste_periode_2));
         
         //add période 3 : 2 tours 
         ArrayList<CaseAction> liste_periode_3 = new ArrayList<CaseAction>();
         liste_periode_3.add(new Action1Sanglier());
         liste_periode_3.add(new Action1Legume());
         this.periodes.add(new Periode(3, liste_periode_3));
         
         //add période 4 : 2 tours 
         ArrayList<CaseAction> liste_periode_4 = new ArrayList<CaseAction>();
         liste_periode_4.add(new Action1Pierre());
         liste_periode_4.add(new Action1Boeuf());
         this.periodes.add(new Periode(4, liste_periode_4));
         
         //add période 5 : 2 tours 
         ArrayList<CaseAction> liste_periode_5 = new ArrayList<CaseAction>();
         liste_periode_5.add(new ActionNaissanceSansPlaceLibre());
         liste_periode_5.add(new ActionLabourageSemailles());
         this.periodes.add(new Periode(5, liste_periode_5));
         
         //add période 6 : 1 tours
         ArrayList<CaseAction> liste_periode_6 = new ArrayList<CaseAction>();
         liste_periode_6.add(new ActionRenovationCloture());
         this.periodes.add(new Periode(6, liste_periode_6));
         
        
         this.amenagements.add(new MajeurFoyer2());
         this.amenagements.add(new MajeurFoyer3());
         this.amenagements.add(new MajeurFourneau4());
         this.amenagements.add(new MajeurFourneau5());
         this.amenagements.add(new MajeurPuits());
         this.amenagements.add(new MajeurFourEnBriques());
         this.amenagements.add(new MajeurFourEnPierre());
         this.amenagements.add(new MajeurMenuiserie());
         this.amenagements.add(new MajeurPoterie());
         this.amenagements.add(new MajeurVannerie());
         
         this.actions_fixes.add(new ActionPieceEtable());
         this.actions_fixes.add(new ActionPremierJoueur());
         this.actions_fixes.add(new Action1Cereale());
         this.actions_fixes.add(new ActionLabourer());
         this.actions_fixes.add(new ActionEtablePain());
         this.actions_fixes.add(new ActionJournalier());
         this.actions_fixes.add(new Action3Bois());
         this.actions_fixes.add(new Action1Argile());
         this.actions_fixes.add(new Action1Roseau());
         this.actions_fixes.add(new ActionPeche());
     }
     
     public ArrayList<Periode> getPeriodes(){
         return this.periodes;
     }
     
     public ArrayList<AmenagementMajeur> getAmenagements(){
         return this.amenagements;
     }
     
     public Periode getPeriodeSuivante(Periode courante){
         return this.periodes.get(this.periodes.indexOf(courante)+1);
     }
     
     public ArrayList<CaseAction> getActions_Fixes(){
         return this.actions_fixes;
     }
 }
