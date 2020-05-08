 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Classe;
 
 import Classe.Strategies.SuivreParcoursOptimal;
 import Classe.Strategies.AllerNoeudPlusAncien;
 import Classe.Strategies.AllerNoeudPlusPret;
 import Classe.Strategies.AttendreNoeud;
 import Classe.Strategies.AllerPortAttache;
 import Classe.Strategies.IStrategieAttente;
 import Classe.Strategies.IStrategieTraitement;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 /**
  *
  * @author Joseph
  */
 public class Vehicule {
     private float m_tempsTraitementUrgence;
     private float m_tempsEcouleSurUrgence;
     private float m_distanceParcourue;
     
     Noeud m_noeudCourant;
     Noeud m_prochainNoeud;
     Point2D.Float m_Position;
     
     Noeud m_portAttache;
     IStrategieAttente m_strategieAttente;
     IStrategieTraitement m_strategieTraitement;
     
     public Vehicule (Point2D.Float p_point)
     {
         m_Position = p_point;
         m_tempsTraitementUrgence = 10;
         m_portAttache = null;
         m_tempsEcouleSurUrgence = 0;
         m_distanceParcourue = 0;
         
         DefinirStrategieAttente(0);
         DefinirStrategieTraitement(0);
     }
     
     public void DefinirPortAttache(Noeud p_nouveauPort)
     {
         m_portAttache = p_nouveauPort;
         m_noeudCourant = p_nouveauPort;
         m_prochainNoeud = p_nouveauPort;
         m_Position = new Point2D.Float(p_nouveauPort.obtenir_posX(), p_nouveauPort.obtenir_posY());
     }
     
     public void DefinirTempsTraitement(float nouveauTemps)
     {
         m_tempsTraitementUrgence = nouveauTemps;
     }
     
     public void DefinirStrategieAttente(int indexStrategie)
     {
         if(indexStrategie == 2)
         {
             m_strategieAttente = new AllerPortAttache();
         }
         else
         {
             m_strategieAttente = new AttendreNoeud();
         }
     }
     
     public void DefinirStrategieTraitement(int indexStrategie)
     {
         if(indexStrategie == 2)
         {
             m_strategieTraitement = new AllerNoeudPlusPret();
         }
         else
         {
             if(indexStrategie == 3)
             {
                 m_strategieTraitement = new SuivreParcoursOptimal();
             }
             else
             {
                 m_strategieTraitement = new AllerNoeudPlusAncien();
             }
         }
     }
     
     public void AvancerTemps(ArrayList<Noeud> systemeRoutier, double vitesse)
     {
         double rapportDistance;
         double DistanceSegment;
         
         //Verifie si véhicule est sur un noeud
         if(m_noeudCourant.EstMemePosition(m_Position))
         {
            if(m_noeudCourant.ContientUrgenceDeclencheeNonTraitee())
             {
                 AvancerTraitementUrgence(vitesse);
             }
             else
             {
                 if(SystemeContientUrgenceRestante(systemeRoutier))
                 {
                     m_prochainNoeud = m_strategieTraitement.ObtenirProchainNoeud(m_noeudCourant, systemeRoutier);
                 }
                 else
                 {
                     m_prochainNoeud = m_strategieAttente.ObtenirProchainNoeud(m_noeudCourant, systemeRoutier, m_portAttache);
                     if(m_prochainNoeud == null)
                     {
                         m_prochainNoeud = m_noeudCourant;
                     }
                 }
                 Avancer(vitesse);
             }
         }
         else
         {
                Avancer(vitesse);
         }
     }
     
     private void AvancerTraitementUrgence(double vitesse)
     {
         m_tempsEcouleSurUrgence += vitesse;
         
         if(m_tempsEcouleSurUrgence >= m_tempsTraitementUrgence)
         {
             m_tempsEcouleSurUrgence = 0;
             
             m_noeudCourant.ObtenirUrgenceCouranteDeclenchee().DefinirTerminee();
         }
     }
     
     private boolean SystemeContientUrgenceRestante(ArrayList<Noeud> systemeRoutier)
     {
         boolean systemeContientUrgence =  false;
         int compteur = 0;
         
         while(systemeContientUrgence == false && compteur < systemeRoutier.size())
         {
             if(systemeRoutier.get(compteur).ContientUrgenceDeclencheeNonTraitee())
             {
                 systemeContientUrgence = true;
             }
             
             compteur++;
         }
         
         return systemeContientUrgence;
     }
     
     private void Avancer(double vitesse)
     {
         double distanceSegment, rapportDistance;
         
         distanceSegment = m_noeudCourant.GetDistance(m_prochainNoeud.obtenir_Position());
         rapportDistance = (distanceSegment == 0) ? 0 : (vitesse / distanceSegment);
         m_Position.x += rapportDistance * (m_prochainNoeud.obtenir_posX() - m_noeudCourant.obtenir_posX());
         m_Position.y += rapportDistance * (m_prochainNoeud.obtenir_posY() - m_noeudCourant.obtenir_posY());
         if(m_noeudCourant.GetDistance(m_Position) >= distanceSegment)
         {
             m_noeudCourant = m_prochainNoeud;
             m_Position = new Point2D.Float(m_prochainNoeud.obtenir_posX(), m_prochainNoeud.obtenir_posY());
         }
         
         m_distanceParcourue += vitesse;
     }
     
     public float ObtenirDistanceParcourue()
     {
         return m_distanceParcourue;
     }
     
     public void Reinitialiser()
     {
         m_Position = new Point2D.Float(m_portAttache.obtenir_posX(), m_portAttache.obtenir_posY());
         m_tempsEcouleSurUrgence = 0;
         m_distanceParcourue = 0;
         m_noeudCourant = m_portAttache;
         m_prochainNoeud = m_portAttache;
     }
 
 }
