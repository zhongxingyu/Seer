 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.swing.JPanel;
 import javax.swing.event.MouseInputAdapter;
 
 @SuppressWarnings("serial")
 public class ZoneDessin extends JPanel{
 	int largeurDessin; //La largeur de la zone de dessin
 	int hauteurDessin; //La longueur de la zone de dessin
 	Color background;
 	Curseur curseur;
 	//Les bords de la zones de dessin
 	int ecartHorizontal;
 	int ecartVertical;
 	Controleur c;
 	BarreOutils barreOutils;
 	private int clicSouris;//1 : Clic gauche, 3 : Clic Droit
 	
 	
     private Controleur controleur;
 
     /**
      *  Constructeur de la zone de dessin
      */
 	ZoneDessin(int largeurDessin, int hauteurDessin, Color background, Curseur curseur){
 		this.largeurDessin = largeurDessin;
 		this.hauteurDessin = hauteurDessin;
 		this.background = background;
 		this.curseur = curseur;
 		
 		this.addMouseListener(new MouseAdapter() {			
 			public void mousePressed(MouseEvent e) {
 				clicSouris = e.getButton();
 				clicSouris(e.getX(), e.getY());
 	        }
 	    });
 		this.addMouseMotionListener(new MouseInputAdapter(){
 			public void mouseDragged(MouseEvent e) {
 				if(clicSouris == 1)
 					clicSouris(e.getX(), e.getY());
 			}
 		});
 		this.addMouseWheelListener(new  MouseWheelListener(){
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				barreOutils.interactionSliderEpaisseur(-e.getWheelRotation()*4);
 			}
 		});
     }
 	
 	public void clicSouris(int posX, int posY){
 		switch(clicSouris){
 			case 1 :
				//if((posX > ecartHorizontal - 30) && (posX < ecartHorizontal + largeurDessin + 30) && (posY > ecartVertical - 30) && (posY < ecartVertical + hauteurDessin + 30)){
 				c.goTo(posX - ecartHorizontal, posY - ecartVertical);
 				repaint();
				//}
 				break;
 			case 2 :
 				barreOutils.interactionBoutonOutil();
 				break;
 			case 3 :
 				barreOutils.interactionBoutonPoserOutil();
 				break;
 		}
 				
 	}
 	
 	/**
 	 * Methode dessinant la zone de dessin puis le curseur
 	 */
 	public void paintComponent(Graphics gd){
 		Graphics2D g = (Graphics2D)gd;
 		//Calcul de l'ecart de la zone de dessin pour centrer le dessin
 		ecartHorizontal = (this.getWidth() - largeurDessin)/2;
 		ecartVertical = (this.getHeight() - hauteurDessin)/2;
 		
 		//ETAPE 1 : Afficher la zone de dessin
 			g.setColor(background);//Couleur de fond du dessin
 			g.fillRect(ecartHorizontal, ecartVertical, this.largeurDessin, this.hauteurDessin);
 							
 		//ETAPE 2 : Afficher les traceurs
 		Traceur t;
 		for (int i = 0; i < StockageDonnee.liste_dessin.size(); i ++){
 			t = StockageDonnee.liste_dessin.get(i);
 			//Initialisons les propriétés de l'objet graphics
 			g.setColor(t.getColor());
 			int cap; int join;
 			if(t.getForme() == 0){
 				cap = BasicStroke.CAP_ROUND;
 				join = BasicStroke.JOIN_ROUND;
 			}
 			else{
 				cap = BasicStroke.CAP_BUTT;
 				join = BasicStroke.CAP_ROUND;
 			}
 			g.setStroke(new BasicStroke(t.getEpaisseur(), cap, join));
 			
 			/*System.out.println("Position X Début : " + posXAbsolue(t.getXOrigine()));
 			System.out.println("Position Y Début : " + posYAbsolue(t.getYOrigine()));
 			System.out.println("Position X Fin : " + posXAbsolue(t.getXArrivee()));
 			System.out.println("Position Y Fin : " + posYAbsolue(t.getYArrivee()));
 			System.out.println("Couleur Curseur : " + t.getColor());
 			System.out.println("Epaisseur : " + t.getEpaisseur());
 			*/
 			
 			//Si le t est une droite/point
 			if (t.getType() == 1 || t.getType() == 0){
 				if(t.getForme() == 0)
 					g.drawLine(posXAbsolue(t.getXOrigine()), posYAbsolue(t.getYOrigine()), posXAbsolue(t.getXArrivee()), posYAbsolue(t.getYArrivee()));
 				//Dans le cas d'une forme carré, on va dessiner des carré aux points de départ/arrivée pour un effet pls propre
 				else{
 					g.setStroke(new BasicStroke());
 					g.fillRect(posXAbsolue(t.getXOrigine()) - t.getEpaisseur()/2, posYAbsolue(t.getYOrigine()) - t.getEpaisseur()/2, t.getEpaisseur(), t.getEpaisseur());
 					g.fillRect(posXAbsolue(t.getXArrivee()) - t.getEpaisseur()/2, posYAbsolue(t.getYArrivee()) - t.getEpaisseur()/2, t.getEpaisseur(), t.getEpaisseur());
 					//Et on trace deux version du trait entre les points (en fait si on ne laisse qu'une seule des deux version, certains angles seront mal déssiné
 					int[] x = {posXAbsolue(t.getXOrigine()) - t.getEpaisseur()/2,
 							posXAbsolue(t.getXArrivee()) - t.getEpaisseur()/2, 
 							posXAbsolue(t.getXArrivee()) + t.getEpaisseur()/2,
 							posXAbsolue(t.getXOrigine()) + t.getEpaisseur()/2
 							};
 					int[] y = {posYAbsolue(t.getYOrigine()) - t.getEpaisseur()/2,
 							posYAbsolue(t.getYArrivee()) - t.getEpaisseur()/2,
 							posYAbsolue(t.getYArrivee()) + t.getEpaisseur()/2,
 							posYAbsolue(t.getYOrigine()) + t.getEpaisseur()/2
 							};
 					g.fillPolygon(x, y, 4);
 					int[] x2 = {posXAbsolue(t.getXOrigine()) + t.getEpaisseur()/2,
 							posXAbsolue(t.getXArrivee()) + t.getEpaisseur()/2, 
 							posXAbsolue(t.getXArrivee()) - t.getEpaisseur()/2,
 							posXAbsolue(t.getXOrigine()) - t.getEpaisseur()/2
 							};
 					g.fillPolygon(x2, y, 4);
 							
 				}
 			}
 
 			//Si le t est un Rectangle
 			else if (t.getType() == 2){
 				//On va faire une boucle qui dessin des triangle successifs selon l'epaisseur du curseur
 				if(!t.estRempli()){
 					g.fillRect(posXAbsolue(t.getXOrigine()), posYAbsolue(t.getYOrigine()), t.getLargeur(), t.getHauteur());
 				}
 				else{
 					g.drawRect(posXAbsolue(posXAbsolue(t.getXOrigine()) - t.getEpaisseur()), posYAbsolue(t.getYOrigine()) - t.getEpaisseur(), t.getLargeur() + t.getEpaisseur(), t.getHauteur() + t.getEpaisseur());
 				}
 			}
 			
 			//Si le t est un triangle
 			else if (t.getType() == 3){
 				int[] x = {posXAbsolue(t.getXOrigine()),
 						posXAbsolue(t.getXArrivee()), 
 						posXAbsolue(t.getX3())};
 				int[] y = {posYAbsolue(t.getYOrigine()),
 						posYAbsolue(t.getYArrivee()),
 						posYAbsolue(t.getY3())};
 				if(!t.estRempli()){
 						g.fillPolygon(x, y, 3);
 					}
 				else{
 					g.drawPolygon(x, y, 3);
 				}
 			}
 			
 			//Si le t est un Cercle
 			else if (t.getType() == 4){
 				//On va faire une boucle qui dessin des triangle successifs selon l'epaisseur du curseur
 				if(!t.estRempli()){
 					g.fillOval(posXAbsolue(t.getXOrigine()), posYAbsolue(t.getYOrigine()), t.getLargeur(), t.getHauteur());
 				}
 				else{
 					g.drawOval(posXAbsolue(t.getXOrigine()), posYAbsolue(t.getYOrigine()), t.getLargeur(), t.getHauteur());
 				}
 			}
 			
 			
 		}
 		
 		//DESSINONS LE FOND
 		g.setStroke(new BasicStroke());
 		//Fond de la zone de dessin
 		g.setColor(new Color(180,180,180));//Couleur de fond
 		g.fillRect(0, 0, ecartHorizontal, this.getHeight());//On défini une couleur derriere le dessin pour eviter les glitch graphiques
 		g.fillRect(ecartHorizontal + largeurDessin, 0, this.getWidth(), this.getHeight());
 		g.fillRect(ecartHorizontal - 1, 0, largeurDessin + 1, ecartVertical);
 		g.fillRect(ecartHorizontal - 1, largeurDessin + ecartVertical, largeurDessin + 1, ecartVertical);
 		
 		//Ombre du dessin
 		g.setColor(new Color(220,220,220));
 		g.fillRect(ecartHorizontal + largeurDessin, ecartVertical + 5, 5, hauteurDessin);
 		g.fillRect(ecartHorizontal + 5, ecartVertical + hauteurDessin, largeurDessin, 5);
 		
 		//ETAPE 3 : Afficher le curseur
 		//Deux curseurs à afficher : le curseur négatif (pour plus de lisibilité) et le curseur normal
 		//Initialisons la couleur négative
 		
 		//Forme du curseur en fonction de l'outil
 		BasicStroke forme;
 		if(curseur.getType() == 0){
 			forme = new BasicStroke(0);
 		}
 		else{
 			float[] dashArray = {2, 2};
 			forme = new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashArray, 0);
 		}
 		g.setStroke(forme);
 		
 		//AFFICHAGE DE LA BASE DU CURSEUR
 		//Calcul du rayon de la base
 		int rayonBase;
 		if (curseur.getEpaisseur() > 10)
 			rayonBase = curseur.getEpaisseur() / 2;
 		else rayonBase = 5;
 		
 		
 		//Dessin de la base
 		//Sous curseur negatif
 		g.setColor(Color.white);
 		
 		g.drawLine(this.getPosX() - (curseur.getEpaisseur() / 2), this.getPosY()  + 1, this.getPosX() + (curseur.getEpaisseur() / 2), this.getPosY()  + 1);
 		g.drawLine(this.getPosX() + 1, this.getPosY() - (curseur.getEpaisseur() / 2), this.getPosX() +1, this.getPosY()+ (curseur.getEpaisseur() / 2));
 		if (curseur.isDown()){		
 			if(curseur.getForme() == 0)
 				g.drawOval(this.getPosX() - rayonBase, this.getPosY()  - rayonBase + 1, rayonBase * 2, rayonBase * 2);
 			else
 				g.drawRect(this.getPosX() - curseur.getEpaisseur()/2 + 1, this.getPosY() - curseur.getEpaisseur()/2 + 1, curseur.getEpaisseur(), curseur.getEpaisseur());
 		}
 		
 		
 		//Curseur de la bonne couleur
 		g.setColor(Color.black);
 		g.drawLine(this.getPosX() - (curseur.getEpaisseur() / 2), this.getPosY(), this.getPosX() + (curseur.getEpaisseur() / 2), this.getPosY());
 		g.drawLine(this.getPosX(), this.getPosY() - (curseur.getEpaisseur() / 2), this.getPosX(), this.getPosY() + (curseur.getEpaisseur() / 2));
 		if (curseur.isDown()){	
 			if(curseur.getForme() == 0)
 				g.drawOval(this.getPosX() - rayonBase, this.getPosY()  - rayonBase , rayonBase * 2, rayonBase * 2);
 			else
 				g.drawRect(this.getPosX() - curseur.getEpaisseur()/2, this.getPosY() - curseur.getEpaisseur()/2, curseur.getEpaisseur(), curseur.getEpaisseur());
 		}
 		
 		//Affichage de la fleche d'orientation
 		//Determinons le point d'arrivée du trait symbolisant l'orientation
 		int tailleTrait;
 		if (curseur.getEpaisseur() > 40)
 			tailleTrait = curseur.getEpaisseur();
 		else tailleTrait = 40;
 		double posX2 = this.getPosX() + tailleTrait * Math.sin(curseur.getOrientation() * Math.PI / 180);
 		double posY2 = this.getPosY() + tailleTrait * Math.cos(curseur.getOrientation() * Math.PI / 180);
 		//Dessinons le trait
 		g.setStroke(new BasicStroke(0));
 		g.drawLine(this.getPosX(), this.getPosY(), (int)posX2, (int)posY2);
 		g.setColor(Color.white);
 			g.drawLine(this.getPosX() - 1, this.getPosY() - 1, (int)posX2 - 1, (int)posY2 - 1);
 	}
 
 	
 	/*///
 	 * ACCESSEURS
 	 //*/
 	public int posXAbsolue(int x){
 		return x + ecartHorizontal;
 	}
 	public int posYAbsolue(int y){
 		return y + ecartVertical;
 	}
 	
 	/*///
 	 * ACCESSEURS
 	 //*/
 	
 	public int getPosX(){
 		return curseur.getPosX() + ecartHorizontal;
 	}
 	public int getPosY(){
 		return curseur.getPosY() + ecartVertical;
 	}
 	
 	public int getEcartHorizontal(){
 		return ecartHorizontal;
 	}
 	public int getEcartVertical(){
 		return ecartVertical;
 	}
     public int getLargeurDessin(){
     	return largeurDessin;
     }
     public int getHauteurDessin(){
     	return largeurDessin;
     }
     public Color getBackground(){
     	return background;
     }
     
 	/*///
 	 * MODIFIEURS
 	 //*/
     
     /**
      *  Modifie le controleur
      *  @param c nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.c = c;
     }
     
     public void setBackground(Color c){
     	background = c;
     }
     public void setLargeur(int l){
     	largeurDessin = l;
     }
     public void setHauteur(int h){
     	hauteurDessin = h;
     }
     public void setBarreOutils(BarreOutils b){ barreOutils = b;}
     
 }
