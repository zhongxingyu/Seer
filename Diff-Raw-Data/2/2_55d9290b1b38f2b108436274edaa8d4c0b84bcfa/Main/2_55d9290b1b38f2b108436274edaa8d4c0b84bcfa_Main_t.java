 import java.awt.Color;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 
 public class Main{
 
     /**
      * Fonction d'initialisation du programme
      */
     public static void init()
     {
 
         if ( !StockageDonnee.init() )
             System.exit(1);
 
         if ( !verifFichierConfig() );
         else;
         /* TODO */
 
     }
 
     /**
      *  Fonction de lancement du programme
      */
     public static void start_program()
     {
     	//Initialisation des parametres
     	int largeurDessin = MenuOption.getConfigDessinLargeur();
     	int hauteurDessin = MenuOption.getConfigDessinHauteur();
     	int posXCurseur;
     	int posYCurseur;
     	if (MenuOption.getConfigCurseurEstCentre()){
     		posXCurseur = largeurDessin/2;
         	posYCurseur = hauteurDessin/2;
     	}
     	else{
     		posXCurseur = 0;
     	    posYCurseur = 0;
     	}
     	Color couleurCurseur = new Color(MenuOption.getConfigCurseurRed(), MenuOption.getConfigCurseurGreen(), MenuOption.getConfigCurseurBlue());
     	Color couleurBackgroundDessin = new Color(MenuOption.getConfigDessinBackgroundRed(), MenuOption.getConfigDessinBackgroundGreen(), MenuOption.getConfigDessinBackgroundBlue());
     	
     	
     	//Initialisation des composants
     	
    	Curseur curseur = new Curseur(posXCurseur, posYCurseur, 90, 1, couleurCurseur, 0, 30, 0);
     	ZoneDessin zoneDessin = new ZoneDessin(largeurDessin,hauteurDessin, couleurBackgroundDessin, curseur);
     	BarreOutils barreOutils = new BarreOutils(curseur, zoneDessin);
     	Fenetre fenetre = new Fenetre(zoneDessin, barreOutils);
         
     	Controleur c = new Controleur();
         c.___hydrate___(fenetre, curseur);
         
     }
 
     /**
     * Fonction de verification du fichier du configuration
     * @return true si le fichier respecte les normes precisees dans la documentation
     */
     public static boolean verifFichierConfig(){
     	boolean recreerFichierConfig = false;
     	
     	//Si le dossier .config n'existe pas, on le créé
     	File dossier = new File("../config");
     	if(!dossier.exists()) dossier.mkdir();
     	
     	
     	File f = new File("../config/.config.txt");   
     	//Verifions que le fichier de configuration existe
     	if(f.exists()){
     		//Le fichier existe, chargeons les données par défauts
     		DataInputStream dis;
     		
     		try{
 	    		dis = new DataInputStream(
 			              				new BufferedInputStream(
 			              					new FileInputStream(
 			              						new File("../config/.config.txt"))));
 			            
 	    		MenuOption.setConfigProgrammeEstFenetre(dis.readBoolean());
 	    		MenuOption.setConfigCurseurEstCentre(dis.readBoolean());
 	    		MenuOption.setConfigCurseurRed(dis.readInt());
 	    		MenuOption.setConfigCurseurGreen(dis.readInt());
 	    		MenuOption.setConfigCurseurBlue(dis.readInt());
 	    		MenuOption.setConfigDessinLargeur(dis.readInt());
 	    		MenuOption.setConfigDessinHauteur(dis.readInt());
 	    		MenuOption.setConfigDessinBackgroundRed(dis.readInt());
 	    		MenuOption.setConfigDessinBackgroundGreen(dis.readInt());
 	    		MenuOption.setConfigDessinBackgroundBlue(dis.readInt());
 	    		//On ferme le stream
 	    			dis.close();
     		} catch (IOException e) {
   		      e.printStackTrace();
   		      f.delete();
   		      recreerFichierConfig = true;
   		    }
     	}
     	else{
     		recreerFichierConfig = true;
     	}
     	//Le fichier config n'existe pas : on en créé un puis on charge les données par défaut
     	if(recreerFichierConfig){
     		String info;
     		if(!f.exists())
     			info = "Le fichier de configuration est introuvable, un nouveau contenant des valeurs par défauts a été créé";
     		else
     			info = "Une erreur est survenue lors du chargement du fichier du configuration, , un nouveau contenant des valeurs par défauts a été créé";
     		JOptionPane infoConfig = new JOptionPane();
     		infoConfig.showMessageDialog(null, info, "Chargement du fichier config", JOptionPane.INFORMATION_MESSAGE);
     		
     		
 	      //Chargement des données par défaut
 	      //Données 1 : si true, la fenetre est en mode fenetré
 	      MenuOption.setConfigProgrammeEstFenetre(true);
 	      //Données 2 : si true, le curseur est centré
 	      MenuOption.setConfigCurseurEstCentre(true);
 	      //Données 3 : valeur Red du curseur
 	      MenuOption.setConfigCurseurRed(0);
 	      //Données 4 : valeur Green du curseur
 	      MenuOption.setConfigCurseurGreen(0);
 	      //Données 5 : valeur Blue du curseur
 	      MenuOption.setConfigCurseurBlue(0);
 	      //Données 6 : Largeur du dessin
 	      MenuOption.setConfigDessinLargeur(300);
 	      //Données 7 : Hauteur du dessin
 	      MenuOption.setConfigDessinHauteur(300);
 	      //Données 8 : valeur Red du dessin
 	      MenuOption.setConfigDessinBackgroundRed(255);
 	      //Données 9 : valeur Green du dessin
 	      MenuOption.setConfigDessinBackgroundGreen(255);
 	      //Données 10 : valeur Blue du dessin
 	      MenuOption.setConfigDessinBackgroundBlue(255);
 
 	      
 	      try{    
 			//Creation du fichier config
     		DataOutputStream dos;
     		
     		dos = new DataOutputStream(
 		              new BufferedOutputStream(
 		                new FileOutputStream(
 		                  new File("../config/.config.txt"))));
 
 		      //On écrit dans le fichier
 		      //Données 1 : si true, la fenetre est en mode fenetré
 		      dos.writeBoolean(true);
 		      //Données 2 : si true, le curseur est centré
 		      dos.writeBoolean(true);
 		      //Données 3 : valeur Red du curseur
 		      dos.writeInt(0);
 		      //Données 4 : valeur Green du curseur
 		      dos.writeInt(0);
 		      //Données 5 : valeur Blue du curseur
 		      dos.writeInt(0);
 		      //Données 6 : Largeur du dessin
 		      dos.writeInt(300);
 		      //Données 7 : Hauteur du dessin
 		      dos.writeInt(300);
 		      //Données 8 : valeur Red du dessin
 		      dos.writeInt(255);
 		      //Données 9 : valeur Green du dessin
 		      dos.writeInt(255);
 		      //Données 10 : valeur Blue du dessin
 		      dos.writeInt(255);
 		      //On ferme l'ecriture
 		      dos.close();
 		      } catch (IOException e) {
 			      e.printStackTrace();
 		      }
     	}
     	
     	
         return true;
 
     }
     
     /**
      *  Fonction main
      *  @param args     Parametres des lignes de commandes
      */
     public static void main(String[] args)
     {
 
         init();
         start_program();
 
     }
 
 }
