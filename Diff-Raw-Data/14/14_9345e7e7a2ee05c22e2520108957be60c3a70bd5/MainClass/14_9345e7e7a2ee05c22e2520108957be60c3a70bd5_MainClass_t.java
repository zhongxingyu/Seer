 public class MainClass{
 	public static void main(String[] args){
 		Fenetre f;
 		Mot m;
 		Chrono c;
 		Plateau p;
 		
 		while(!Plateau.boucleJeu){ // Tant que le jeu n'est pas fini
 			int iTrys=0;
 			m=new Mot();
 			f=new Fenetre();
 			c=f.getChrono();
 			p=f.getPlateau();
 			Plateau.boucleMots=false;
 			
 			Fonctions.init(f, m, c);
 			while(!Plateau.boucleMots){ // Tant que le mot n'est pas trouv
 				int iChrono=0;
 				while(iChrono<125 && iTrys!=7){ // On fait...
 					try {
 						Thread.sleep(8); // ... 8*125ms = 1 sec
 					} catch (InterruptedException e) {}
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
 
 // version mode texte
 // Jpanel --> jbutton quitter le jeu
 // rorganiser fonction main --> Fonctions
 // amliorer graphiquement le jeu
 // Affichage commentaires
 // renommer variables moches
