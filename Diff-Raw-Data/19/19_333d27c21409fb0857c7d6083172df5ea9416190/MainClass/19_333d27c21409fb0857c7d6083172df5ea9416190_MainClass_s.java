 public class MainClass{
 	public static void main(String[] args){		
 		while(!Plateau.boucleJeu){ // Tant que le jeu n'est pas fini
 			int iTrys=0;
 			Mot m=new Mot();
 			Fenetre f=new Fenetre();
 			Chrono c=f.getChrono();
 			Plateau p=f.getPlateau();
 			Plateau.boucleMots=false;
 			
 			Fonctions.init(f, m, c);
 			while(!Plateau.boucleMots){ // Tant que le mot n'est pas trouv
 				int iChrono=0;
 				while(iChrono<125 && iTrys!=7){ // On fait...
 					Fonctions.wait8ms(); // ... 8*125ms = 1 sec
 					if(MyKey.go){ // Si l'utilisateur appuie sur entre
 						iTrys++;
 						Fonctions.update(f, m, p);
 						Fonctions.testFin(m, iTrys, f, c);
 						f.resetJtf(); // Remet  jour le JTextField
 					}
 					iChrono++;
 				}
 				if(iTrys!=7)
 					Fonctions.gereChrono(c, f); // Mise  jour du chrono
 			}
 		}
 	}
 }
 
 // revoir couleurs texte
 // Jpanel --> jbutton quitter le jeu
 // Menubar?
 // rorganiser fonction main --> Fonctions
 // amliorer graphiquement le jeu
 // Affichage commentaires
 // renommer variables moches
