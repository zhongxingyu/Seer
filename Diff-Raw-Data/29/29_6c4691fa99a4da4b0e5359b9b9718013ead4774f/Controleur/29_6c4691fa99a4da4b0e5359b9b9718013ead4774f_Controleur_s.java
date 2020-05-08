 import java.util.ArrayList;
 
 public class Controleur{
 
     public static final int SUCCESS = 0;
     public static final int COMMANDE_ERRONEE = 1;
     public static final int NOMBRE_ARG_SUP = 2; 
     public static final int NOMBRE_ARG_LESS = 3;
     public static final int PARAM_INCORRECTE = 4;
     /* 
      * TODO
      * mettre en constante les autres erreurs
      */
     
     Terminal term = null;
     ZoneDessin zd = null;
     ZoneBouton zb = null;
     Curseur curseur = null;
 
     /**
      *  Constructeur vide
      */
     public Controleur(){};
 
     /**
      *  Fonction qui permet d'initialiser les liens entre objets
      *  @param f Fenetre principale
      *  @param c Curseur
      */
     public void ___hydrate___(Fenetre f, Curseur c)
     {
 
         term = f.getTerminal();
         term.setControleur(this);
 
         zd = f.getZoneDessin();
         zd.setControleur(this);
         
         zb = f.getZoneBouton();
         zb.setControleur(this);
 
         curseur = c;
         c.setControleur(this);
 
     }
 
     /**
      *  Fonction qui permet de contrôler le commande entrée par l'utilisateur
      *  @param s Commande entrée par l'utilisateur
      *  @return Si la fonction s'est correctement déroulée
      */
     public boolean commande(String s)
     {
 
 	    String[] commande_parser;
         s = s.trim();
 
 		commande_parser = parse(s);
 
         int numero_renvoie = init(commande_parser);
         return numero_renvoie == 0 ? StockageDonnee.ajoutLCEC(s) && StockageDonnee.ajoutLCEG(s)
                   : StockageDonnee.ajoutLCEG(s) && this.afficheErreur(numero_renvoie);
 
     }
 
     /**
      *  Fonction qui parse la chaîne de commande
      *  @param s Commande entrée par l'utilisateur
      *  @return Tableau comportant la commande et ses arguments ( si besoins )
      */
     public String[] parse(String s)
     {
 	    return s.toLowerCase().split(" ");
     }
 
     /**
      *  Fonction qui envoie le message d'erreur au terminal
      *  @param numero_erreur numero de l'erreur
      *  @return boolean
      */
     public boolean afficheErreur(int numero_erreur)
     {
         term.afficheErreur(GenerationErreur.genererErreur(numero_erreur));
         return true;
     }
     
 
     /**
      *  Fonction qui traite le string
      *  @param commande_parser Tableau contenant le nom de la commande ainsi que ses arguments
      *  @return 0 si la fonction s'est bien déroulée.
      */
     public int init(String[] commande_parser)
     {
         int valeur = 0;
         double valeur_x = 0;
         double valeur_y = 0;
         switch ( StockageDonnee.getNumeroFonction( commande_parser[0] ) )
         {
             case 0:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return pendown();
             
             case 1:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return penup();
            
             case 2:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return pencil();
 
             case 3:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return eraser();
             
             case 4:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return up();
             
             case 5:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return down();
             
             case 6:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return left();
             
             case 7:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return right();
             
             case 8:
                 rotate(commande_parser);
                 break;
             
             case 9:
                 if ( commande_parser.length == 1 )
                     return NOMBRE_ARG_LESS;
                 else if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 else;
 
                 if ( isInt(commande_parser[1])  )
                     valeur = Integer.parseInt(commande_parser[1]);
                 else
                     return COMMANDE_ERRONEE; /* TODO : Changer la valeur de retour */
 
                 return forward(valeur);
             
             case 10:
                 if ( commande_parser.length == 1 )
                     return NOMBRE_ARG_LESS;
                 else if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 else;
 
                 if ( isInt(commande_parser[1]) )
                     valeur = Integer.parseInt(commande_parser[1]);
                 else
                     return COMMANDE_ERRONEE; /* TODO : Changer la valeur de retour */
                 
                 return backward(valeur);
             
             case 11:
                 if ( commande_parser.length > 3 )
                     return NOMBRE_ARG_SUP;
                 else if ( commande_parser.length <= 2 )
                     return NOMBRE_ARG_LESS;
                 else;
 
                 if ( isDouble(commande_parser[1])
                         && isDouble(commande_parser[2]) )
                 {
                     valeur_x = Double.parseDouble(commande_parser[1]);
                     valeur_y = Double.parseDouble(commande_parser[2]);
                 }
                 else
                     return COMMANDE_ERRONEE;
                     
                 return goTo(valeur_x, valeur_y);
             
             case 12:
                 cursorwidth();
                 break;
             
             case 13:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return setColor();
             
             case 14:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return setBackgroundColor();
             
             case 15:
                 doFigure();
                 break;
             
             case 16:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return width();
             
             case 17:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return height();
             
             case 18:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return newFile();
             
             case 19:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return open();
             
             case 20:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return save();
             
             case 21:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return saveas();
             
             case 22:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return savehistory();
             
             case 23:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return exec();
             
             case 24:
                 repeat();
                 break;
             
             case 25:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return clear();
             
             case 26:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_ARG_SUP;
                 return help();
             
             case 27:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_ARG_SUP;
                 return man();
             
             case 28:
                if ( commande_parser.length >= 2 )
                 {
                    System.out.println(commande_parser[1] + commande_parser[2]);
                    if ( commande_parser[1].equals("LCEC") )
                         function_debug_test( true );
                    else if ( commande_parser[1].equals("LCEG") )
                         function_debug_test( false );
                     else
                         return PARAM_INCORRECTE;
                 }
                function_debug_test( false );
                 break;
             
             default:
                 return COMMANDE_ERRONEE;
         }
 
         return SUCCESS;
 
     }
 
 
 
 
     /**
      * Fonction qui permet l'écriture lorsque l'utilisateur se déplace
      * @return si la fonction s'est bien déroulée.
      */
     public int pendown()
     {
     	this.curseur.setIsDown(true);
         return SUCCESS;
 
     }
 
     /**
      * Fonction qui permet d'arrêter l'écriture lorsque l'utilisateur se déplace
      * @return si la fonction s'est bien déroulée.
      */
     public int penup()
     {
     	this.curseur.setIsDown(false);
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de passer en mode crayon
      *  @return si la fonction s'est bien déroulée.
      */
     public int pencil()
     { 
     	this.curseur.setType(0);
         return SUCCESS;
     }
 
     /**
      * Fonction qui permet de passer en mode gomme
      * @return si la fonction s'est bien déroulée.
      */
     public int eraser()
     {
     	this.curseur.setType(1);
         return SUCCESS;
     }
 
     /**
      *  Fonction qui permet de placer le pointeur vers le haut
      *  @return si la fonction s'est bien déroulée.
      */
     public int up()
     {
     	this.curseur.setOrientation(180);
     	/*il reste a faire une rotation de l'image du curseur*/
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de placer le pointeur vers le bas
      *  @return si la fonction s'est bien déroulée.
      */
     public int down()
     {
     	this.curseur.setOrientation(0);
     	/*il reste a faire une rotation de l'image du curseur*/
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de placer le pointeur vers la gauche
      *  @return si la fonction s'est bien déroulée.
      */
     public int left()
     {
     	this.curseur.setOrientation(270);
     	/*il reste a faire une rotation de l'image du curseur*/
         return SUCCESS;
 
     }
 
     /**
      * Fonction qui permet de placer le pointeur vers la droite
      * @return si la fonction s'est bien déroulée.
      */
     public int right()
     {
     	this.curseur.setOrientation(90);
     	/*il reste a faire une rotation de l'image du curseur*/
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de faire une rotation sur le pointeur
      *  @return si la fonction s'est bien déroulée.
      */
     public int rotate(String[] commande_parser)
     {
     	/*on compte le nombre d'elts present dans le tableau*/
     	if(commande_parser.length==2){
     		
     		String arg_deg=commande_parser[1];
     		int degre;
     		
     		if(isInt(arg_deg)){
     			degre=Integer.parseInt(arg_deg);
     			this.curseur.setOrientation(degre);
     		}
     		else{
     			/*ce n'est pas un int : message d'erreur*/
     			System.out.println("Le parametre passe dans la commande n'est pas un int.");
     		}
     	}
     	else{
     		/*mauvais arguments passes dans le terminal*/
     		System.out.println("Vous devez passer en parametre 1 argument de type int.");
     	}
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de faire avancer le pointeur
      *  @param valeur Valeur d'avancée
      *  @return si la fonction s'est bien déroulée.
      */
     public int forward(int valeur)
     {
     	//Calcul de la nouvelle position du curseur
     	double posX = curseur.getPosX() + valeur * Math.sin(curseur.getOrientation() * Math.PI / 180);
 		double posY = curseur.getPosY() + valeur * Math.cos(curseur.getOrientation() * Math.PI / 180);
 		
 		//Changement effectif
         curseur.setPosX((int)posX);
         curseur.setPosY((int)posY);
         
         return SUCCESS;
     }
 
     /**
      *  Fonction qui permet de faire reculer le pointeur
      *  @param valeur Valeur de recul
      *  @return si la fonction s'est bien déroulée.
      */
     public int backward(int valeur)
     {
     	//Calcul de la nouvelle position du curseur
     	double posX = curseur.getPosX() - valeur * Math.sin(curseur.getOrientation() * Math.PI / 180);
 		double posY = curseur.getPosY() - valeur * Math.cos(curseur.getOrientation() * Math.PI / 180);
 		
 		//Changement effectif
         curseur.setPosX((int)posX);
         curseur.setPosY((int)posY);
         
         return SUCCESS;
     }
 
     /**
      *  Fonction qui permet de déplacer le pointeur
      *  @return si la fonction s'est bien déroulée.
      */
     public int goTo(double value, double value_2)
     {
         System.out.println("value 1 :: " + value + "\nvalue 2 :: " + value_2); 
         return SUCCESS;
     }
 
     /**
      *  Fonction qui permet de régler la largeur du curseur
      *  @return si la fonction s'est bien déroulée.
      */
     public int cursorwidth()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de changer la couleur
      *  @return si la fonction s'est bien déroulée.
      */
     public int setColor()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de changer la couleur du fond d'écran
      *  @return si la fonction s'est bien déroulée.
      */
     public int setBackgroundColor()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de tracer des figures particulières
      *  @return si la fonction s'est bien déroulée.
      */
     public int doFigure()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de changer la largeur de l'écran
      *  @return si la fonction s'est bien déroulée.
      */
     public int width()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de changer la hauteur de l'écran
      *  @return si la fonction s'est bien déroulée.
      */
     public int height()
     {
 
         return SUCCESS;
     
     }
 
     /**
      *  Fonction qui permet de créer un nouveau document
      *  @return si la fonction s'est bien déroulée.
      */
     public int newFile()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet d'ouvrir une image
      *  @return si la fonction s'est bien déroulée.
      */
     public int open()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de sauvegarder un document en une image
      *  @return si la fonction s'est bien déroulée.
      */
     public int save()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui sauvegarde dans un dossier donner par l'utilisateur
      *  @return si la fonction s'est bien déroulée
      */
     public int saveas()
     {
     
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui sauvegarde l'historique dans un format .txt
      *  @return si la fonction s'est bien déroulée
      */
     public int savehistory()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui lit un fichier et execute les lignes de commandes si celles-ci sont correctes
      *  @return si la fonction s'est bien déroulée
      */
     public int exec()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui répète les dernières commandes lancés par l'utilisateur
      *  @return si la fonction s'est bien déroulée.
      */
     public int repeat()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui efface l'écran de dessin
      *  @return si la fonction s'est bien déroulée.
      */
     public int clear()
     {
 
         term.clear();
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui affiche une fenêtre avec la liste des commandes
      *  @return si la fonction s'est bien déroulée.
      */
     public int help()
     {
 
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui affiche le manuel de la commande
      *  @return si la fonction s'est bien déroulée.
      */
     public int man()
     {
 
         return SUCCESS;
 
     }
 
     private void function_debug_test(boolean b)
     {
         if ( !b )
         {
             for (int i = 0; i < StockageDonnee.getSize_LCEG(); i++)
             {
                 System.out.println(StockageDonnee.getLCEG(i));
             }
         }
 
         else
         {
             for (int i = 0; i < StockageDonnee.getSize_LCEC(); i++)
             {
                 System.out.println(StockageDonnee.getLCEC(i));
             }
         }
     }
     
     /*cette fonction teste si une chaine de caractere est un int ou pas*/
     public boolean isInt(String s){
     	try{
     		Integer.parseInt(s);
     	}
     	catch(NumberFormatException e){
     		return false;
     	}
         return true;
     }
 
     public boolean isDouble(String s)
     {
         try
         {
             Double.parseDouble(s);
         }
         catch(NumberFormatException e)
         {
             return false;
         }
         return true;
     }
 
 
 }
