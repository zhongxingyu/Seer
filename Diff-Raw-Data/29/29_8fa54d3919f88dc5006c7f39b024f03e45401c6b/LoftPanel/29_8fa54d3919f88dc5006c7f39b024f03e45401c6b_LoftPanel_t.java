 package com.ei3info.tp.lofteurs;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 
 import javax.swing.JPanel;
 
 /**
  * un panneau de dessin pour le loft
  * @author moreau
  */
 class LoftPanel extends JPanel
 {
     /**
      * référence sur la liste des objets à dessiner
      */
     private LinkedList<ObjetDessinable> listeObjets;
 
     /**
     * référence sur la liste des objets détruits pendant la réactualisation
     */
    private LinkedList<ObjetDessinable> listeObjetsDetruits;

    /**
      * constructeur
      * @param listeObjets
      *            r�f�rence sur la liste des objets (g�r�e par la ZoneGraphique)
      */
     public LoftPanel(LinkedList<ObjetDessinable> listeObjets)
     {
         this.listeObjets = new LinkedList<ObjetDessinable>(listeObjets);
        this.listeObjetsDetruits = new LinkedList<ObjetDessinable>();
     }
 
     /**
      * on red�finit la méthode paint() : elle se contente d'appeler les méthodes
      * dessinerObjet() de la liste d'objets dessinables
      */
     public void paintComponent(Graphics g)
     {
         super.paintComponents(g);
 
         g.setColor(Color.BLACK);
         g.drawRect(0, 0, Saison.tailleLoft * ObjetPositionnable.tailleX - 1,
                 Saison.tailleLoft * ObjetPositionnable.tailleX - 1);
         g.setColor(Color.WHITE);
         g.fillRect(0, 0, Saison.tailleLoft * ObjetPositionnable.tailleX,
                 Saison.tailleLoft * ObjetPositionnable.tailleX);
         // on redessine tout
 
         for (ObjetDessinable x : listeObjets)
         {
            if (! listeObjetsDetruits.contains(x))
            {
                 x.dessinerObjet(g);
            }
        }
        for (ObjetDessinable obj : listeObjetsDetruits)
        {
            listeObjets.remove(obj);
         }
        listeObjetsDetruits.clear();
        
     }
 
     public void updateListeObjets(LinkedList<ObjetDessinable> listeObjets)
     {
         this.listeObjets = new LinkedList<ObjetDessinable>(listeObjets);
         this.revalidate();
         this.repaint();
     }
 
     public void removeObjet(ObjetDessinable objetDetruit)
     {
        this.listeObjetsDetruits.add(objetDetruit);
     }
 
     public void nettoyer()
     {
         listeObjets.clear();
     }
 }
