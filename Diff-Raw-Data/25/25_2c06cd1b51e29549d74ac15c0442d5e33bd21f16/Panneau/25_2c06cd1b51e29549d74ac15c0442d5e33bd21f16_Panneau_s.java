 import java.awt.*;
 import java.io.*;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 public class Panneau extends JPanel {
 
 public static final int NBCASES  =  9;
 public static final int TAILLEIM = 62;
 
 Plateau plateau;
 ClickAction listener;
 Image neant,casevide,pionN,pionB,pionBS,pionNS,pionBP,pionNP,moveB,moveN,moveV;
 int bille1,bille2,bille3;
 byte cp0,cp1,cp2,cp3,cp4,cp5;
 
 	/**
 	* Constructeur du panel.
 	* @param plateau Le plateau de jeu
 	* @param listener L'ecouteur des actions de l'utilisateur
 	*/
 	public Panneau(Plateau plateau,ClickAction listener){
 			//initialisation des variables
 			this.plateau = plateau;
 			this.bille1 = 0;
 			this.bille2 = 0;
 			this.bille3 = 0;
 			this.cp0 = -1;
 			this.cp1 = -1;
 			this.cp2 = -1;
 			this.cp3 = -1;
 			this.cp4 = -1;
 			this.cp5 = -1;
 			//permet de gerer la gestion des click dans la classe ClickAction
 			addMouseListener(listener);
 			//chargement des images
 			Toolkit kit=Toolkit.getDefaultToolkit();
 			MediaTracker tracker=new MediaTracker(this);
 			this.neant	  = kit.getImage("images/neant.jpg");
 			this.casevide = kit.getImage("images/casevide.jpg");
 			this.pionB    = kit.getImage("images/pionB.jpg");
 			this.pionN    = kit.getImage("images/pionN.jpg");
 			this.pionBS   = kit.getImage("images/pionBS.jpg");
 			this.pionNS   = kit.getImage("images/pionNS.jpg");
 			this.pionBP   = kit.getImage("images/pionBP.jpg");
 			this.pionNP   = kit.getImage("images/pionNP.jpg");
 			this.moveB   = kit.getImage("images/moveB.jpg");
 			this.moveN   = kit.getImage("images/moveN.jpg");
 			this.moveV   = kit.getImage("images/moveV.jpg");
 			//les trackers c'est la vie !
 			tracker.addImage(neant,0);
 			tracker.addImage(casevide,0);
 			tracker.addImage(pionB,0);
 			tracker.addImage(pionN,0);
 			tracker.addImage(pionNS,0);
 			tracker.addImage(pionNP,0);
 			tracker.addImage(pionBS,0);
 			tracker.addImage(pionBP,0);
 			tracker.addImage(moveB,0);
 			tracker.addImage(moveN,0);
 			tracker.addImage(moveV,0);
 			try {tracker.waitForID(0);}
 			catch(InterruptedException e){}
 
 
 	}
 
 	/**
 	* Methode pour afficher le plateau et les billes.
 	*/
 	public void paintComponent(Graphics g){
 		byte i,j;
 		int taillePlateau = 1;
 		//Pour chaque ligne
 		for (i=1; i<=9; i++){
 			//pour chaque colonne
 			for (j=1; j<=9; j++){
 				//on s'occupe d'abord d'afficher le pourtour du plateau
 				if ( i == 1 && j == 1 || i == 1 && j == 2 || i == 1 && j == 8 || i == 1 && j == 9 ){
 					g.drawImage(this.neant,j*TAILLEIM,i*TAILLEIM,null);
 				}
 
 				else if ( i == 2 && j == 1 || i == 2 && j == 8 || i == 2 && j == 9 || i == 3 && j == 1 || i == 3 && j == 9){
 					g.drawImage(this.neant,j*TAILLEIM,i*TAILLEIM,null);
 				}
 
 				else if ( i == 4 && j == 9 || i == 6 && j == 9 || i == 7 && j == 1 || i == 7 && j == 9){
 					g.drawImage(this.neant,j*TAILLEIM,i*TAILLEIM,null);
 				}
 
 				else if ( i == 8 && j == 1 || i == 8 && j == 8 || i == 8 && j == 9 || i == 9 && j == 1 || i == 9 && j == 2 || i == 9 && j == 8 || i == 9 && j == 9){
 					g.drawImage(this.neant,j*TAILLEIM,i*TAILLEIM,null);
 				}
 				
 				else if(plateau.cases[taillePlateau].getContenu() == Case.NOIR && i%2 == 1 && taillePlateau < 62){
 					//si une bille est selectionnee, on l'a met en surbrillance
 					if(plateau.cases[taillePlateau].getNumero() == bille1 || plateau.cases[taillePlateau].getNumero() == bille2 || plateau.cases[taillePlateau].getNumero() == bille3){
 						if(bille1 != bille2){
 							boolean b = g.drawImage(this.pionNS,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 						}
 						else{
 							g.drawImage(this.pionNP,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 						}
 					}
 					else if(plateau.cases[taillePlateau].getNumero() == cp0 || plateau.cases[taillePlateau].getNumero() == cp1 || plateau.cases[taillePlateau].getNumero() == cp2 || plateau.cases[taillePlateau].getNumero() == cp3 || plateau.cases[taillePlateau].getNumero() == cp4 || plateau.cases[taillePlateau].getNumero() == cp5){
 						g.drawImage(this.moveN,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 					}
 					//sinon on l'affiche normalement
 					else{
 						g.drawImage(this.pionN,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 					}	
 					taillePlateau += 1;
 				}
 
 				else if(plateau.cases[taillePlateau].getContenu() == Case.NOIR && i%2 == 0 && taillePlateau < 62){
 					if(plateau.cases[taillePlateau].getNumero() == bille1 || plateau.cases[taillePlateau].getNumero() == bille2 || plateau.cases[taillePlateau].getNumero() == bille3){
 						if(bille1 != bille2){
 							g.drawImage(this.pionNS,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 						}
 						else{
 							g.drawImage(this.pionNP,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 							}
 					}
 					else if(plateau.cases[taillePlateau].getNumero() == cp0 || plateau.cases[taillePlateau].getNumero() == cp1 || plateau.cases[taillePlateau].getNumero() == cp2 || plateau.cases[taillePlateau].getNumero() == cp3 || plateau.cases[taillePlateau].getNumero() == cp4 || plateau.cases[taillePlateau].getNumero() == cp5){
 						g.drawImage(this.moveN,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 					}
 					else{
 						g.drawImage(this.pionN,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 					}
 					taillePlateau += 1;
 				}
 				
 				else if(plateau.cases[taillePlateau].getContenu() == Case.BLANC && i%2 == 1 && taillePlateau < 62){
 					if(plateau.cases[taillePlateau].getNumero() == bille1 || plateau.cases[taillePlateau].getNumero() == bille2 || plateau.cases[taillePlateau].getNumero() == bille3){
 						if(bille1 != bille2){
 							g.drawImage(this.pionBS,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 						}
 						else{
 							g.drawImage(this.pionBP,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 						}
 					}
 					else if(plateau.cases[taillePlateau].getNumero() == cp0 || plateau.cases[taillePlateau].getNumero() == cp1 || plateau.cases[taillePlateau].getNumero() == cp2 || plateau.cases[taillePlateau].getNumero() == cp3 || plateau.cases[taillePlateau].getNumero() == cp4 || plateau.cases[taillePlateau].getNumero() == cp5){
 							g.drawImage(this.moveB,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 						}
 					else{
 						g.drawImage(this.pionB,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);
 					}
 					taillePlateau += 1;
 				}
 
 				else if(plateau.cases[taillePlateau].getContenu() == Case.BLANC && i%2 == 0 && taillePlateau < 62){
 					if(plateau.cases[taillePlateau].getNumero() == bille1 || plateau.cases[taillePlateau].getNumero() == bille2 || plateau.cases[taillePlateau].getNumero() == bille3){
 						if(bille1 != bille2){
 							g.drawImage(this.pionBS,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 						}
 						else if(plateau.cases[taillePlateau].getNumero() == cp0 || plateau.cases[taillePlateau].getNumero() == cp1 || plateau.cases[taillePlateau].getNumero() == cp2 || plateau.cases[taillePlateau].getNumero() == cp3 || plateau.cases[taillePlateau].getNumero() == cp4 || plateau.cases[taillePlateau].getNumero() == cp5){
 							g.drawImage(this.moveB,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 						}						
 						else{
 							g.drawImage(this.pionBP,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 						}
 					}
 					else{
 						g.drawImage(this.pionB,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 					}
 					taillePlateau += 1;
 				}
 
 				else if(plateau.cases[taillePlateau].getContenu() == Case.VIDE && i%2 == 1 && taillePlateau < 62){
 				 	if(plateau.cases[taillePlateau].getNumero() == cp0 || plateau.cases[taillePlateau].getNumero() == cp1 || plateau.cases[taillePlateau].getNumero() == cp2 || plateau.cases[taillePlateau].getNumero() == cp3 || plateau.cases[taillePlateau].getNumero() == cp4 || plateau.cases[taillePlateau].getNumero() == cp5)
 						g.drawImage(this.moveV,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);	
 				   	else	
 						g.drawImage(this.casevide,(j-1)*TAILLEIM,(i-1)*TAILLEIM,null);	
 					taillePlateau += 1;
 				}
 
 				else if(plateau.cases[taillePlateau].getContenu() == Case.VIDE && i%2 == 0 && taillePlateau < 62){
 					if(plateau.cases[taillePlateau].getNumero() == cp0 || plateau.cases[taillePlateau].getNumero() == cp1 || plateau.cases[taillePlateau].getNumero() == cp2 || plateau.cases[taillePlateau].getNumero() == cp3 || plateau.cases[taillePlateau].getNumero() == cp4 || plateau.cases[taillePlateau].getNumero() == cp5)
 						g.drawImage(this.moveV,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 					else	
 						g.drawImage(this.casevide,(j-1)*TAILLEIM+(TAILLEIM/2),(i-1)*TAILLEIM,null);
 					taillePlateau += 1;
 				}
 				
 			}
 		}
 	}
 
 	/**
 	* methode pour reinitialiser la variable de plateau
 	* @param plateau Le plateau a reinitialiser.
 	*/
 	public void rafraichir(Plateau plateau){
 		this.plateau = plateau;
 		this.bille1 = 0;
 		this.bille2 = 0;
 		this.bille3 = 0;
 		this.cp0 = -1;
 		this.cp1 = -1;
 		this.cp2 = -1;
 		this.cp3 = -1;
 		this.cp4 = -1;
 		this.cp5 = -1;
		this.repaint();
 	}
 	
 	/**
 	* Connaitre le numero de la 1ere bille selectionnee
 	* @param bille1
 	*/
 	public void rafraichirBS1(int bille1){
 		this.bille1 = bille1;
 		this.repaint();
 	}
 	
 	/**
 	* methode pour connaitre le numero de la 2eme bille selectionnee
 	*/
 	public void rafraichirBS2(int bille2, int bille3){
 		this.bille2 = bille2;
 		this.bille3 = bille3;
 		this.repaint();
 	}
 	
 	/**
 	* methode pour connaitre le numero des cases ou le mouvement est valide
 	*/
 	public void rafraichirCP(byte cp, byte i){
 		if(i == 0) this.cp0 = cp;
 		else if(i == 1) this.cp1 = cp;
 		else if(i == 2) this.cp2 = cp;
 		else if(i == 3) this.cp3 = cp;
 		else if(i == 4) this.cp4 = cp;
 		else if(i == 5) this.cp5 = cp;
 		
 		this.repaint();
 	} 
 
 }
 
