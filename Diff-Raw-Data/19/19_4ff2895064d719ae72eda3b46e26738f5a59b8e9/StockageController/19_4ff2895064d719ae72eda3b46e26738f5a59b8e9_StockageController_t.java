 //TODO : Tout
 
 package com.ecnmelog.controller;
 
 import com.ecnmelog.model.AbstractStockage;
 import com.ecnmelog.model.AbstractAttente;
 import com.ecnmelog.model.Container;
 import com.ecnmelog.model.ContainerException;
 
 import java.util.ArrayList;
 
 /**
  * Partie du contrôleur dédiée à la communication avec le modèle
  */
 public class StockageController {
     protected AbstractStockage stock;
     protected AbstractAttente att;
     protected ArrayList<Integer> containerTypes;
     
     public StockageController(AbstractStockage stock, AbstractAttente att){
         this.stock = stock;
         this.att = att;
         this.containerTypes = Container.getTypes();
     }
     
     /**
      * Ajouter un container à la zone d'attente
      * @param id L'identifiant du container à ajouter
      * @param type Le type de container à ajouter
      */
     public void addContainer(int id, int type) {
         if(this.containerTypes.contains(type)) {
             try {
                 this.att.addContainer(new Container(id, type));
             } catch (ContainerException e) {
                this.att.notifyWarning(e.getMessage());
             }
         }
     }
     
     /**
      * Stocker les containers en attente
      */
     public void traiterAttente() {
         this.stock.traiterAttente();
     }
     
     /**
      * Supprimer un conteneur de la zone d'attente
      * @param id L'identifiant du container à enlever
      */
     public void removeFromAttente(int id) {
         this.att.removeContainerById(id);
     }
     
     /**
      * Supprimer un conteneur de la zone de stockage
      * @param id L'identifiant du container à enlever
      */
     public void removeFromStockage(int id) {
         this.stock.removeContainerById(id);
     }
     
     /**
      * Vider la zone d'attente
      */
     public void emptyAttente() {
         this.att.empty();
     }
     
     /**
      * Vider la zone de stockage
      */
     public void emptyStockage() {
         this.stock.empty();
     }
 }
