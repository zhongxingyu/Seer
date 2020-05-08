 public class MainClass{
 	public static void main(String[] args){
 		while(!Plateau.boucleJeu){ // Tant que le jeu n'est pas fini
 			Mot m=new Mot();
 			Fenetre f=new Fenetre();
 			Fonctions fo=new Fonctions();
 			Chrono c=f.getChrono();
 			Plateau p=f.getPlateau();
 			int iTrys=0;
 			
 			fo.init(f, m, c);
 			while(!Plateau.boucleMots){ // Tant que le mot n'est pas trouv
 				int iChrono=0;
 				while(iChrono<125 && iTrys!=7){ // On fait...
 					fo.wait8ms(); // ... 8*125ms = 1 sec
 					if(MyKey.go){ // Si l'utilisateur appuie sur entre
 						iTrys++;
 						fo.update(f, m, p);
 						fo.testFin(m, iTrys, f, c);
 					}
 					iChrono++;
 				}
 				Fonctions.display=false;
 				if(iTrys!=7)
 					fo.updateChrono(c, f); // Mise  jour du chrono
 			}
 		}
 	}
 }
 
 // ------------------ BUGS importants------------------------------------
 
 // add polices --> linux
// BUG REPAINT entre les chargements de fenetre
 // version linux --> again.bat --> chemin dico
 
 // revoir couleurs texte
 // rorganiser fonction main --> Fonctions
 // amliorer graphiquement le jeu
 // Affichage commentaires
 // renommer variables moches
 // exception son
 
 // ----------------   BUGS optionnels ----------------
 
 //bug lettres en jaunes???
 //affichage boutons  la fin du mot??
 // timer?
 
 // -----------------------------------------------------------------------
 
 //spcifier que l'adulte peut rentrer/effacert des mots dans motsEnfants.txt. 
