 package controllers;
 
 /**
  *
  * @author pierregaste
  */
 public class Way extends CRUD {
     
     /**
      * Action par défaut
      */
     public static void index()
     {
        redirect("way/list");
     }
     
     /**
      * Listing des trajets
      */
     public static void list()
     {
         render();
     }
 }
