 import java.util.ArrayList;
 import java.util.Date;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.awt.Color;
 
 public class Controleur{
 
     public static final int SUCCESS = 0;
     public static final int COMMANDE_ERRONEE = 100;
     public static final int NOMBRE_PARAM_LESS = 200; 
     public static final int NOMBRE_PARAM_SUP = 201;
     public static final int PARAM_INCORRECTE = 202;
     public static final int HISTO_ALREADY_SAVE = 900;
     /* 
      * TODO
      * mettre en constante les autres erreurs
      */
     
     public Terminal term = null;
     public ZoneDessin zd = null;
     public BarreOutils zb = null;
     public Curseur curseur = null;
 
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
         s = rework_command(s);
 
 		commande_parser = parse(s);
 
         int numero_renvoie = init(commande_parser);
         return numero_renvoie == 0 ? StockageDonnee.ajoutLCEC(s) && StockageDonnee.ajoutLCEG(s)
                   : StockageDonnee.ajoutLCEG(s) && this.setMessageErreur(numero_renvoie);
 
     }
 
     /**
      *  Fonction qui parse la chaîne de commande
      *  @param s Commande entrée par l'utilisateur
      *  @return Tableau comportant la commande et ses arguments ( si besoins )
      */
     public String[] parse(String s)
     {
 	    return s.split(" ");
     }
 
     /**
      *  Fonction qui renvoie la commande tapée par l'utilisateur en enlevant tout espace superflu
      *  @param s Commande entrée par l'utilisateur
      *  @return Commande retravaillée
      */
     public String rework_command(String s)
     {
         s = s.trim();
 
         int i = 0;
         while ( i+1 < s.length() )
         {
             if ( (s.charAt(i) == ' ') && (s.charAt(i+1) == ' ') )
                 s = s.substring(0,i) + s.substring(i+1);
             else
                 i++;
         }
 
         return s;
     }
 
     /**
      *  Fonction qui envoie le message d'erreur au terminal
      *  @param numero_erreur numero de l'erreur
      *  @return boolean
      */
     public boolean setMessageErreur(int numero_erreur)
     {
         String message = "   /!\\ Erreur : ";
         String param = StockageDonnee.getParamErreur();
         if ( !param.equals("") ) 
             message += param + " : ";
         message += StockageDonnee.getMessageErreur(numero_erreur);
         term.addMessage(message);
         return false;
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
         switch ( StockageDonnee.getNumeroFonction( commande_parser[0].toLowerCase() ) )
         {
             case 0:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return pendown();
             
             case 1:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return penup();
            
             case 2:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return pencil();
 
             case 3:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return eraser();
             
             case 4:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return up();
             
             case 5:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return down();
             
             case 6:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return left();
             
             case 7:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return right();
             
             case 8:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else;
 
                 if ( isDouble(commande_parser[1]) )
                     valeur = (int)Double.parseDouble(commande_parser[1]);
                 else
                     return PARAM_INCORRECTE;
 
                 return rotate(valeur);
             
             case 9:
                 if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else;
 
                 if ( isInt(commande_parser[1])  )
                     valeur = Integer.parseInt(commande_parser[1]);
                 else
                     return PARAM_INCORRECTE;
 
                 return forward(valeur);
             
             case 10:
                 if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else;
 
                 if ( isInt(commande_parser[1]) )
                     valeur = Integer.parseInt(commande_parser[1]);
                 else
                     return PARAM_INCORRECTE;
 
                 return backward(valeur);
             
             case 11:
                 if ( commande_parser.length > 3 )
                     return NOMBRE_PARAM_SUP;
                 else if ( commande_parser.length <= 2 )
                 {
                     return NOMBRE_PARAM_LESS;
                 }
                 else;
 
                 if ( isDouble(commande_parser[1])
                         && isDouble(commande_parser[2]) )
                 {
                     valeur_x = Double.parseDouble(commande_parser[1]);
                     valeur_y = Double.parseDouble(commande_parser[2]);
                 }
                 else
                     return PARAM_INCORRECTE;
 
                 return goTo(valeur_x, valeur_y);
             
             case 12:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else;
 
                 if ( isInt(commande_parser[1]) )
                     valeur = Integer.parseInt(commande_parser[1]);
                 else
                     return PARAM_INCORRECTE;
 
                 return cursorWidth(valeur);
             
             case 13:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else;
 
                 return setColor(commande_parser[1]);
             
             case 14:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else;
 
                 return setBackgroundColor(commande_parser[1]);
             
             case 15:
                 /* TODO */
                 doFigure();
                 break;
             
             case 16:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else;
 
                 if ( isInt(commande_parser[1]) )
                     valeur = Integer.parseInt(commande_parser[1]);
                 else
                     return PARAM_INCORRECTE;
 
                 return width(valeur);
             
             case 17:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 else if ( commande_parser.length < 2 )
                     return NOMBRE_PARAM_LESS;
                 else;
 
                 if ( isInt(commande_parser[1]) )
                     valeur = Integer.parseInt(commande_parser[1]);
                 else
                     return PARAM_INCORRECTE;
 
                 return height(valeur);
             
             case 18:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 return newFile();
             
             case 19:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 return open();
             
             case 20:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 return save();
             
             case 21:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 return saveas();
             
             case 22:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
     
                 if ( commande_parser.length == 2 )
                     return savehistory(commande_parser[1]);
                 else
                     return savehistory("");
             
             case 23:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 return exec();
             
             case 24:
                 if ( commande_parser.length > 3 )
                     return NOMBRE_PARAM_SUP;
 
                 if ( commande_parser.length == 1 )
                     return repeat(1,1);
                 else if ( commande_parser.length == 2 )
                 {
                     if ( isInt(commande_parser[1]) )
                         return repeat(Integer.parseInt(commande_parser[1]),1);
                     else
                         return PARAM_INCORRECTE;
                 }
                 else
                 {
                     if ( isInt(commande_parser[1]) && isInt(commande_parser[1]) )
                     {
                         return repeat(Integer.parseInt(commande_parser[1]),
                                         Integer.parseInt(commande_parser[2]));
                     }
                     else
                         return PARAM_INCORRECTE;
                 }
                 
             case 25:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return clear();
             
             case 26:
                 if ( commande_parser.length > 1 )
                     return NOMBRE_PARAM_SUP;
                 return help();
             
             case 27:
                 if ( commande_parser.length > 2 )
                     return NOMBRE_PARAM_SUP;
                 
                 if ( commande_parser.length < 2 )
                     return man(false, "");
                 else
                     return man(true, commande_parser[1]);
             
             case 28:
                 if ( commande_parser.length == 2 )
                 {
                     if ( commande_parser[1].equals("lcec") )
                         function_debug_test( true );
                     else if ( commande_parser[1].equals("lceg") )
                         function_debug_test( false );
                     else
                         return PARAM_INCORRECTE;
                 }
                 else
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
         this.zd.repaint();
         return SUCCESS;
 
     }
 
     /**
      * Fonction qui permet d'arrêter l'écriture lorsque l'utilisateur se déplace
      * @return si la fonction s'est bien déroulée.
      */
     public int penup()
     {
     	this.curseur.setIsDown(false);
         this.zd.repaint();
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
         this.zd.repaint();
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de placer le pointeur vers le bas
      *  @return si la fonction s'est bien déroulée.
      */
     public int down()
     {
     	this.curseur.setOrientation(0);
         this.zd.repaint();
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de placer le pointeur vers la gauche
      *  @return si la fonction s'est bien déroulée.
      */
     public int left()
     {
     	this.curseur.setOrientation(270);
         this.zd.repaint();
         return SUCCESS;
 
     }
 
     /**
      * Fonction qui permet de placer le pointeur vers la droite
      * @return si la fonction s'est bien déroulée.
      */
     public int right()
     {
     	this.curseur.setOrientation(90);
         this.zd.repaint();
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de faire une rotation sur le pointeur
      *  @return si la fonction s'est bien déroulée.
      */
     public int rotate(int valeur)
     {
         /*  TODO
          *  FAIRE DES TESTS SUR LA VARIABLE valeur
          */
     	this.curseur.setOrientation(valeur);
         this.zd.repaint();
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
     	int posX1=curseur.getPosX();
     	int posY1=curseur.getPosY();
     	
     	double posX = curseur.getPosX() + valeur * Math.sin(curseur.getOrientation() * Math.PI / 180);
 		double posY = curseur.getPosY() + valeur * Math.cos(curseur.getOrientation() * Math.PI / 180);
 		
 		//conditions pour que le curseur ne dépasse pas la zone de dessin
 		
 		if(posX <= zd.getLargeurDessin() && posX >= 0) curseur.setPosX((int)posX); //ok
         
         if(posY <= zd.getHauteurDessin() && posY >= 0) curseur.setPosY((int)posY); //ok 
         
         if(posX <0) curseur.setPosX(0); //valeur négative : on replace à 0
         
         if(posY <0) curseur.setPosY(0); //valeur négative : on replace à 0
         
         if(posX > zd.getLargeurDessin()) curseur.setPosX(zd.getLargeurDessin()); //trop grand : on met à la position max
         
         if(posY > zd.getHauteurDessin()) curseur.setPosY(zd.getHauteurDessin()); //trop grand : on met à la position max
         
         if(curseur.isDown() && curseur.getType() == 0){
         	Traceur t = new Traceur(1, curseur.getEpaisseur(), curseur.getCouleur(), posX1, posY1, curseur.getPosX(), curseur.getPosY());
         	StockageDonnee.liste_dessin.add(t);
         }
         if(curseur.isDown() && curseur.getType() == 1){
         	Traceur t = new Traceur(1, curseur.getEpaisseur(), zd.getBackground(), posX1, posY1, curseur.getPosX(), curseur.getPosY());
         	StockageDonnee.liste_dessin.add(t);
         	
         }
         
         
         this.zd.repaint(); 
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
     	
     	int posX1=curseur.getPosX();
     	int posY1=curseur.getPosY();
     	
     	double posX = curseur.getPosX() - valeur * Math.sin(curseur.getOrientation() * Math.PI / 180);
 		double posY = curseur.getPosY() - valeur * Math.cos(curseur.getOrientation() * Math.PI / 180);
 		
 		//conditions pour que le curseur ne dépasse pas la zone de dessin
 		
 		if(posX <= zd.getLargeurDessin() && posX >= 0) curseur.setPosX((int)posX); //ok
 		       
 		if(posY <= zd.getHauteurDessin() && posY >= 0) curseur.setPosY((int)posY); //ok 
 		        
 		if(posX <0) curseur.setPosX(0); //valeur négative : on replace à 0
 		       
 		if(posY <0) curseur.setPosY(0); //valeur négative : on replace à 0
 		        
 		if(posX > zd.getLargeurDessin()) curseur.setPosX(zd.getLargeurDessin()); //trop grand : on met à la position max
 		        
 		if(posY > zd.getHauteurDessin()) curseur.setPosY(zd.getHauteurDessin()); //trop grand : on met à la position max
        
 		if(curseur.isDown() && curseur.getType() == 0){
         	Traceur t = new Traceur(1, curseur.getEpaisseur(), curseur.getCouleur(), posX1, posY1, curseur.getPosX(), curseur.getPosY());
         	StockageDonnee.liste_dessin.add(t);
         	
         }
         if(curseur.isDown() && curseur.getType() == 1){
         	Traceur t = new Traceur(1, curseur.getEpaisseur(), zd.getBackground(), posX1, posY1, curseur.getPosX(), curseur.getPosY());
         	StockageDonnee.liste_dessin.add(t);
         	
         }
 		
 		this.zd.repaint();
         return SUCCESS;
     }
 
     /**
      *  Fonction qui permet de déplacer le pointeur
      *  @return si la fonction s'est bien déroulée.
      */
     public int goTo(double value, double value_2)
     {
         System.out.println("value 1 :: " + value + "\nvalue 2 :: " + value_2);
         
         int posX1=curseur.getPosX();
     	int posY1=curseur.getPosY();
     	
         //conditions pour que le curseur ne dépasse pas la zone de dessin
         
         if( value >= 0 && value <= zd.getLargeurDessin()) curseur.setPosX((int)value); //ok
     		
     	if(value_2 >= 0 && value_2 <= zd.getHauteurDessin()) curseur.setPosY((int)value_2); //ok
 
     	if(value > zd.getLargeurDessin()) curseur.setPosX(zd.getLargeurDessin()); //valeur X > largeur de la zone
     	
     	if(value_2 > zd.getHauteurDessin()) curseur.setPosY(zd.getHauteurDessin()); //valeur Y > hauteur de la zone
     	
     	if(value < 0) curseur.setPosX(0); //valeur négative => on replace à la valeur minimu : 0
     	
     	if(value_2 < 0) curseur.setPosY(0); //valeur négative => on replace à la valeur minimu : 0
     	
     	if(curseur.isDown() && curseur.getType() == 0){
         	Traceur t = new Traceur(1, curseur.getEpaisseur(), curseur.getCouleur(), posX1, posY1, curseur.getPosX(), curseur.getPosY());
         	StockageDonnee.liste_dessin.add(t);
         	
         }
         if(curseur.isDown() && curseur.getType() == 1){
         	Traceur t = new Traceur(1, curseur.getEpaisseur(), zd.getBackground(), posX1, posY1, curseur.getPosX(), curseur.getPosY());
         	StockageDonnee.liste_dessin.add(t);
         	
         }
 
     	this.zd.repaint();
         return SUCCESS;
     }
 
     /**
      *  Fonction qui permet de régler la largeur du curseur
      *  @return si la fonction s'est bien déroulée.
      */
     public int cursorWidth(int valeur)
     {
 
         System.out.println("valeur :: " + valeur);
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de changer la couleur
      *  @return si la fonction s'est bien déroulée.
      */
     public int setColor(String couleur)
     {
 
         System.out.println("couleur :: " + couleur);
         
         if(StockageDonnee.liste_couleur.containsKey(couleur)){
         	Color c = StockageDonnee.liste_couleur.get(couleur);
         	curseur.setCouleur(c);
         }
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de changer la couleur du fond d'écran
      *  @return si la fonction s'est bien déroulée.
      */
     public int setBackgroundColor(String bgColor)
     {
 
         System.out.println("couleur :: " + bgColor);
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
     public int width(int valeur)
     {
 
         System.out.println("value :: " + valeur);
         return SUCCESS;
 
     }
 
     /**
      *  Fonction qui permet de changer la hauteur de l'écran
      *  @return si la fonction s'est bien déroulée.
      */
     public int height(int valeur)
     {
 
         System.out.println("value :: " + valeur);
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
     public int savehistory(String s)
     {
         String format = "yy-MM-yy_H-mm-ss";
         SimpleDateFormat formater = new SimpleDateFormat(format);
         Date date = new java.util.Date();
 
         File history;
 
         if ( s.equals("") )
         {
             
             try
             {
                 File folder = new File("../history");
                 if ( !folder.exists() )
                 {
                     if ( !folder.mkdir() )
                         term.addMessage("   /!\\ LE DOSSIER N'A PAS PU ETRE CREE");
                 }
                 
                 history = new File("../history/history" + formater.format(date) + ".txt");
                 
             }
             catch (Exception e)
             {
                 return COMMANDE_ERRONEE;
             }
 
         }
         else
         {
 
             String[] pathname_split = s.split("/");
             String pathname;
             int i = 0;
 
             if ( pathname_split[0].equals("~") )
             {
                 pathname = new String("/home/" + System.getProperty("user.name") + "/");
                 i = 1;
             }
             else
                 pathname = new String("");
 
             for (i = i; i < pathname_split.length; i++)
             {
                 if ( !pathname_split[i].equals("") )
                     pathname += pathname_split[i] + "/";
 
                 File folder = new File(pathname);
                 if ( !folder.exists() )
                 {
                     if ( !folder.mkdir() )
                         term.addMessage("   /!\\ LE DOSSIER N'A PAS PU ETRE CREE");
                     else
                         System.out.println("folder made on pathname[\"" + pathname + "\"]");
                 }
                 else
                     System.out.println("folder with pathname[\"" + pathname + "\"] already exist");
                 
                 if ( i == pathname_split.length-1 )
                 {
                     if ( pathname_split[i].indexOf('.') < 0 )
                     {
                        pathname += "history" + formater.format(date) + ".txt";
                     }
                     else if  ( pathname_split[i].indexOf('.') == 0 )
                     {
                         pathname = pathname.substring(0,pathname.indexOf('.')) + "history"
                             + formater.format(date) + pathname.substring(pathname.indexOf('.'));
                     }
                     else;
                 }
             }
 
             history = new File (pathname);
             System.out.println(pathname);
 
         }
         
         try
         {
             history.createNewFile();
             FileWriter fw = new FileWriter(history, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter fSortie = new PrintWriter(bw);
 
             for (int i = 0; i < StockageDonnee.getSize_LCEC(); i++)
             {
                 fSortie.println(StockageDonnee.getLCEC(i));
                 fSortie.flush();
             }
 
             fSortie.close();
         }
         catch (Exception e)
         {
             return COMMANDE_ERRONEE;
         }
         
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
     public int repeat(int nombre_de_commandes, int nombre_de_repetition)
     {
 
         System.out.println("value 1 :: " + nombre_de_commandes 
                 + "\nvalue 2 :: " + nombre_de_repetition);
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
     public int man(boolean isNotEmpty, String commande)
     {
 
         if ( isNotEmpty )
         {
             if ( StockageDonnee.manuel.containsKey(commande) )
                 System.out.println(StockageDonnee.getManuel(commande));
             else
                 System.out.println("La commande n'existe pas");
         }
         else
             System.out.println("Quel page voulez vous ? (Syntaxe : man <commande>)");
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
             StockageDonnee.setParamErreur(s);
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
             StockageDonnee.setParamErreur(s);
             return false;
         }
         return true;
     }
 
 
 }
