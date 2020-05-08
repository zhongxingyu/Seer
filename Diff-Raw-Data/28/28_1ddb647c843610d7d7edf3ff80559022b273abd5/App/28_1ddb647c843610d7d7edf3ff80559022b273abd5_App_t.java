 package com.ecnmelog.app;
 
 
 import com.ecnmelog.model.AbstractStockage;
 import com.ecnmelog.model.AbstractAttente;
 import com.ecnmelog.model.Stockage;
 import com.ecnmelog.model.Attente;
 
 
 import com.ecnmelog.view.Interface;
 import com.ecnmelog.controller.StockageController;
 
 /**
 * Classe principale de l'application.
  */
 public class App {
     /**
      * Méthode principale de l'application.
      * @param args Les arguments passés en CLI
      */
    public static void main(final String[] args) {
         int capaciteStockage = 100;

         AbstractStockage stock = new Stockage(capaciteStockage);
         AbstractAttente att = new Attente();

         StockageController controller = new StockageController(stock, att);
         Interface fenetre = new Interface(controller);
     }
 }
