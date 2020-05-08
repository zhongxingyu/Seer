 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Classe;
 
 import java.awt.geom.Point2D;
 
 /**
  *
  * @author Joseph
  */
 public class Noeud {
     
     Point2D.Float m_Position;
    
     //Attributs
      public float obtenir_posX() {
         return m_Position.x;
     }
 
     public float obtenir_posY() {
         return m_Position.y;
     }
     
     public Point2D.Float obtenir_Position()
     {
         return m_Position;
     }
     
     //Constructeur
     Noeud(Point2D.Float p_CoordNoeud)
     {
         m_Position = p_CoordNoeud;
     }
     
     //Méthodes publique
     boolean EstMemePosition(Point2D.Float p_CoordNoeud)
     {
        return (m_Position.equals(p_CoordNoeud));
     }
 }
