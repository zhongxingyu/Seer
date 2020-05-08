 package com.ecnmelog.model;
 
 import java.util.ArrayList;
 import java.sql.*;
 
 import com.ecnmelog.model.DbConn;
 import com.ecnmelog.model.Container;
 import com.ecnmelog.model.Emplacement;
 
 /**
  * Objet passé à la vue par le modèle.
  * Contient l'état actuel de la zone de stockage
  */
 public class StockageBean extends AbstractStockageBean {
     /**
      * Récupère l'état actuel de la zone de stockage dans la base de données et le stocke dans l'objet
      */
     public StockageBean() {
         Connection conn = DbConn.getInstance();

         this.containers = new ArrayList<Container>();
         this.nbContainers = new ArrayList<Integer>();
         this.emplacements = new ArrayList<Emplacement>();
         this.nbEmplacements = new ArrayList<Integer>();
         this.emplacementsDispo = new ArrayList<Emplacement>();
         this.nbEmplacementsDispo = new ArrayList<Integer>();

         // Partie Containers
         
         try {
             // Récupération et stockage de tous les containers stockés
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery("SELECT container_id, type_id, emplacement_id FROM container  WHERE emplacement_id NOT NULL;");
             
             while(rs.next()) {
                 this.containers.add(rs.getInt("emplacement_id"), new Container(rs.getInt("container_id"), rs.getInt("type_id"), rs.getInt("emplacement_id")));
             }
             rs.close();
         } catch(SQLException e) {
             // Notifier la vue de l'erreur
         }
         
         try {
             // Comptage de tous les types de containers stockés
             // La jointure est là pour exclure les containers de type indéfini
             // C'est un choix bourrin, je sais, et ça peut entraîner l'apparition de containers zombi
             // Boaf, du moment qu'ils commencent pas à dévorer les autres containers, ça passe.
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS nb, a.type_id FROM container a NATURAL JOIN type b WHERE a.emplacement_id NOT NULL GROUP BY a.type_id;");
             
             while(rs.next()) {
                 this.nbContainers.add(rs.getInt("type_id"), rs.getInt("nb"));
             }
             rs.close();
         } catch(SQLException e) {
             // Notifier la vue de l'erreur
         }
         
         // Partie Emplacements
         
         try {
             // Récupération et stockage de tous les emplacements
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery("SELECT emplacement_id, type_id FROM emplacement ORDER BY emplacement_id ASC;");
             
             while(rs.next()) {
                 this.emplacements.add(rs.getInt("emplacement_id"), new Emplacement(rs.getInt("emplacement_id"), rs.getInt("type_id")));
             }
             rs.close();
         } catch(SQLException e) {
             // Notifier la vue de l'erreur
         }
         
         try {
             // Comptage de tous les types d'emplacements
             // La jointure est là pour exclure les emplacements de type indéfini
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS nb, a.type_id FROM emplacement a NATURAL JOIN type b GROUP BY a.type_id;");
             
             while(rs.next()) {
                 this.nbEmplacements.add(rs.getInt("type_id"), rs.getInt("nb"));
             }
             rs.close();
         } catch(SQLException e) {
             // Notifier la vue de l'erreur
         }
         
         // Partie Emplacements Libres
         
         try {
             // Récupération et stockage de tous les emplacements libres
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery("SELECT a.emplacement_id, a.type_id FROM emplacement a LEFT JOIN container b ON a.emplacement_id = b.emplacement_id WHERE b.container_id ISNULL;");
             
             while(rs.next()) {
                 this.emplacementsDispo.add(rs.getInt("emplacement_id"), new Emplacement(rs.getInt("emplacement_id"), rs.getInt("type_id")));
             }
             rs.close();
         } catch(SQLException e) {
             // Notifier la vue de l'erreur
         }
         
         try {
             // Comptage de tous les types d'emplacements libres
             // La jointure est là pour exclure les emplacements de type indéfini
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS nb, a.emplacement_id FROM emplacement a NATURAL JOIN type b LEFT JOIN container c ON a.emplacement_id = c.emplacement_id WHERE c.container_id ISNULL GROUP BY a.type_id;");
             
             while(rs.next()) {
                 this.nbEmplacementsDispo.add(rs.getInt("type_id"), rs.getInt("nb"));
             }
             rs.close();
         } catch(SQLException e) {
             // Notifier la vue de l'erreur
         }
     }
 }
