 import java.util.ArrayList;
 import java.util.Hashtable;
 
 public class StockageDonnee
 {
 
     public static ArrayList<String> liste_commande_entree_correcte;
     public static ArrayList<String> liste_commande_entree_generale;
     public static Hashtable<String, Integer> liste_des_commandes;
     public static Hashtable<Integer, String> liste_erreurs;
 
     /**
      *  Fonction qui initialise toutes les collections
      *  @return boolean vérifiant l'initialisation de chacunes des collections
      */
     public static boolean init()
     {
 
         return init_lcec() && init_lceg()
                             && init_ldc()
                             && init_le();
 
     }
 
     /**
      *  Fonction initialisant la collection des listes de commandes correctes entrées par l'utilisateur
      *  Il servira pour la création d'un fichier historique effectuer lors de la demande de l'utilisateur
      *  @return boolean vérifiant l'initialisation de la collection
      */
     public static boolean init_lcec()
     {
 
         liste_commande_entree_correcte = new ArrayList<String>();
         return true;
 
     }
 
     /**
      *  Fonction initialisant la collection des listes de commandes générale (vraies ou fausses) entrées
      *  par l'utilisateur.
      *  @return boolean vérifiant l'initialisation de la collection
      */
     public static boolean init_lceg()
     {
 
         liste_commande_entree_generale = new ArrayList<String>();
         return true;
 
     }
 
     /**
      *  Fonction initialisant la collection de la liste des commandes disponnible pour l'utilisateur
      *  @return boolean vérifiant l'initialisation de la collection
      */
     public static boolean init_ldc()
     {
 
         liste_des_commandes = new Hashtable<String, Integer>();
 
         liste_des_commandes.put("pendown", 0);
         liste_des_commandes.put("penup", 1);
         liste_des_commandes.put("pencil", 2);
         liste_des_commandes.put("eraser", 3);
         liste_des_commandes.put("up", 4);
         liste_des_commandes.put("down", 5);
         liste_des_commandes.put("left", 6);
         liste_des_commandes.put("right", 7);
         liste_des_commandes.put("rotate", 8);
         liste_des_commandes.put("forward", 9);
         liste_des_commandes.put("backward", 10);
         liste_des_commandes.put("goto", 11);
        liste_des_commandes.put("cursorwidth", 12);
         liste_des_commandes.put("setcolor", 13);
         liste_des_commandes.put("setbackgroundcolor", 14);
         liste_des_commandes.put("do", 15);
         liste_des_commandes.put("width",16);
         liste_des_commandes.put("height", 17);
         liste_des_commandes.put("new", 18);
         liste_des_commandes.put("open", 19);
         liste_des_commandes.put("save", 20);
         liste_des_commandes.put("saveas", 21);
         liste_des_commandes.put("savehistory", 22);
         liste_des_commandes.put("exec", 23);
         liste_des_commandes.put("repeat", 24);
         liste_des_commandes.put("clear", 25);
         liste_des_commandes.put("help", 26);
         liste_des_commandes.put("man", 27);
         liste_des_commandes.put("function_debug_test", 28);
         
         return true;
 
     }
 
     /**
      *  Fonction initialisant la collection de message d'erreurs
      *  @return boolean vérifiant l'initialisation de la collection
      */
     public static boolean init_le()
     {
 
         liste_erreurs = new Hashtable<Integer, String>(); 
 
         liste_erreurs.put(1, "la commande n'existe pas");
         liste_erreurs.put(2, "nombre(s) d'argument(s) trop élevé");
         liste_erreurs.put(3, "nombre(s) d'argument(s) trop faible");
         liste_erreurs.put(4, "paramètre incorrect");
 
         return true;
 
     }
 
     /**
     *   Fonction renvoyant le message d'erreur
     *   @param numero Numero de l'erreur
     *   @return String correspondant au message d'erreur
     */
     public static String getMessageErreur(int numero)
     {
         return liste_erreurs.get( numero );       
     }
 
     /**
      *  Fonction renvoyant l'entier correspondant à la commande
      *  @return l'entier de la commande entrée par l'utilisateur
      */
     public static int getNumeroFonction(String commande)
     {
         return ( liste_des_commandes.containsKey(commande) ) ? liste_des_commandes.get(commande) : -1;
     }
 
     /**
      *  Fonction renvoyant la commande entrée selon son numéro
      *  @return la commande
      */
     public static String getLCEC(int numero)
     {
         return liste_commande_entree_correcte.get( numero );
     }
 
     /**
      *  Fonction renvoyant la commande entrée selon son numéro
      *  @return la commande
      */
     public static String getLCEG(int numero)
     {
         return liste_commande_entree_generale.get( numero );
     }
 
     /**
      *  Fonction ajoutant la commande à la collection correspondante
      *  @param commande Commande entrée par l'utilisateur
      */
     public static boolean ajoutLCEC(String commande)
     {
         liste_commande_entree_correcte.add(commande);
         return true;
     }
 
     /**
      *  Fonction ajoutant la commande à la collection correspondante
      *  @param commande Commande entréé par l'utilisateur
      */
     public static boolean ajoutLCEG(String commande)
     {
         liste_commande_entree_generale.add(commande);
         return true;
     }
 
     /**
      *  Fonction renvoyant la taille de la collection
      *  @return taille
      */
     public static int getSize_LCEG()
     {
         return liste_commande_entree_generale.size();
     }
 
     /**
      *  Fonction renvoyant la taille de la collection
      *  @return taille
      */
     public static int getSize_LCEC()
     {
         return liste_commande_entree_correcte.size();
     }
 
 }
