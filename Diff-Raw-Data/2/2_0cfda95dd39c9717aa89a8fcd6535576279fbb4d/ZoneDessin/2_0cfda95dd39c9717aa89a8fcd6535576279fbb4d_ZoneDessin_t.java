 package com.display;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 import javax.swing.event.MouseInputAdapter;
 
 import com.controleur.Controleur;
 import com.stockage.StockageDonnee;
 
 @SuppressWarnings("serial")
 public class ZoneDessin extends JPanel{
 	private int largeurDessin; //La largeur du dessin
 	private int hauteurDessin; //La longueur du dessin
 	private Color background;
 	private Curseur curseur;
 	private int ecartHorizontal; //Ecart horizontal entre le bord de la zone de dessin et du dessin
 	private int ecartVertical; //Ecart vertical entre le bord de la zone de dessin et du dessin
 	private Controleur c;
 	private BarreOutils barreOutils;
 	private int clicSouris;//1 : Clic gauche, 2 : Clic du milieu, 3 : Clic Droit
 	private boolean affichageCurseur = true;
 	
 	public boolean gridEnable;
 	private static int widthCaseGrid;
 	private static int heightCaseGrid;
 	public boolean gridMagnetismEnable;
 	
 	public static boolean pixelArtModeEnable;
 	
     private Controleur controleur;
 
     /**Constructeur de la zone de dessin
      * @param largeurDessin La largeur en pixel du dessin
      * @param hauteurDessin La hauteur en pixel du dessin
      * @param Color La couleur d'arrière plan de la zone de Dessin
      * @param curseur Le curseur utilisé par le programme
      * */
 	public ZoneDessin(int largeurDessin, int hauteurDessin, Color background, Curseur curseur){
 		this.largeurDessin = largeurDessin;
 		this.hauteurDessin = hauteurDessin;
 		this.background = background;
 		this.curseur = curseur;
 
 		//Appels de fonction lors des clics de souris sur la zone de dessin
 		//Simple clic (gauche, milieu ou droit)
 		this.addMouseListener(new MouseAdapter() {			
 			public void mousePressed(MouseEvent e) {
 				clicSouris = e.getButton();
 				clicSouris(e.getX(), e.getY());
 	        }
 	    });
 		//Clic continue (avec mouvement) de la souris (ne concerne que le clic gauche)
 		this.addMouseMotionListener(new MouseInputAdapter(){
 			public void mouseDragged(MouseEvent e) {
 				if(clicSouris == 1)
 					clicSouris(e.getX(), e.getY());
 			}
 		});
 		//Defilement de la molette
 		this.addMouseWheelListener(new  MouseWheelListener(){
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				barreOutils.interactionSliderEpaisseur(-e.getWheelRotation()*4);
 			}
 		});
     }
 	
 	/**Fonction appelée lors d'un clic de souris sur la zone de dessin
 	 * @param posX La position X de la souris
 	 * @param posY La position Y de la souris*/ 
 	public void clicSouris(int posX, int posY){
 		switch(clicSouris){
 			//Clic gauche
 			case 1 :
                 int posX_final = (posX - ecartHorizontal < 0) ? 0 
                     : (posX - ecartHorizontal > this.getLargeurDessin()) ? this.getLargeurDessin()
                     : (posX - ecartHorizontal);
                     
                 int posY_final = (posY - ecartVertical < 0) ? 0 
                     : (posY - ecartVertical > this.getHauteurDessin()) ? this.getHauteurDessin()
                     : (posY - ecartVertical);
 
                 if(gridMagnetismEnable){//Si la grille est magnetisee, on centre le point dans le carreau sur lequel il est
                 	posX_final = (posX_final/widthCaseGrid)*widthCaseGrid + widthCaseGrid/2;
                 	posY_final = (posY_final/heightCaseGrid)*heightCaseGrid + heightCaseGrid/2;
                 }
                 if(pixelArtModeEnable){
                 	c.commande("penup", true, true);
                     c.commande("goto " + posX_final + " " + posY_final, true, true);
                 	c.commande("pendown", true, true);
                 }
                 c.commande("goto " + posX_final + " " + posY_final, true, true);
 				repaint();
 				break;
 			//Clic molette
 			case 2 :
 				barreOutils.interactionBoutonOutil();
 				break;
 			//Clic droit
 			case 3 :
 				barreOutils.interactionBoutonPoserOutil();
 				break;
 		}
 				
 	}
 	
 	/**Methode dessinant la zone de dessin puis le curseur*/
 	public void paintComponent(Graphics gd){
         Graphics2D g = (Graphics2D)gd;
     
 		//Calcul des ecarts entre la zone de dessin et le dessin pour le centrer
 		ecartHorizontal = (this.getWidth() - largeurDessin)/2;
 		ecartVertical = (this.getHeight() - hauteurDessin)/2;
 		
 		//ETAPE 1 : Afficher le fond du dessin
 		g.setColor(background); //Couleur de fond du dessin
 		g.fillRect(ecartHorizontal, ecartVertical, this.largeurDessin, this.hauteurDessin);
 							
 		//ETAPE 2 : Afficher les traceurs
 		Traceur t;
 		for (int i = 0; i < StockageDonnee.getSize_ListeDessin(); i ++){
 			t = StockageDonnee.getListeDessin(i);
 			
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
 
 			//Si le t est un trait ou un point
 			if (t.getType() == 1 || t.getType() == 0){
 				//Le dessin est différent en fonction de la forme du curseur
 				if(t.getForme() == 0)//Le curseur est rond
 					g.drawLine(posXAbsolue(t.getXOrigine()), posYAbsolue(t.getYOrigine()), posXAbsolue(t.getXArrivee()), posYAbsolue(t.getYArrivee()));
 				//Dans le cas d'une forme carré, on va dessiner des carré aux points de départ/arrivée pour un effet plus propre
 				else{
 					g.setStroke(new BasicStroke());//On désinne nous meme le tracé, pas besoin de Stroke
 					g.fillRect(posXAbsolue(t.getXOrigine()) - t.getEpaisseur()/2, posYAbsolue(t.getYOrigine()) - t.getEpaisseur()/2, t.getEpaisseur(), t.getEpaisseur());
 					g.fillRect(posXAbsolue(t.getXArrivee()) - t.getEpaisseur()/2, posYAbsolue(t.getYArrivee()) - t.getEpaisseur()/2, t.getEpaisseur(), t.getEpaisseur());
 					//Et on trace deux version du trait entre les points (en fait si on ne laisse qu'une seule des deux version, certains angles seront mal dessinés
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
 				if(t.estRempli()){
 					g.fillRect(posXAbsolue(t.getXOrigine()), posYAbsolue(t.getYOrigine()), t.getLargeur(), t.getHauteur());
                 }
 				else{
					g.drawRect(posXAbsolue(t.getXOrigine()), posYAbsolue(t.getYOrigine()), t.getLargeur(), t.getHauteur());
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
 			
 			//Si le t est une Image
 			else if(t.getType() == 5){
 				try {
 				      Image img = ImageIO.read(new File(t.getPath()));
 				      g.drawImage(img, ecartHorizontal, ecartVertical, this);
 				      //Pour une image de fond
 				      //g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
 				    } catch (IOException e) {
 				      e.printStackTrace();
 				    }         
 			}			
 		}
 		
 		//ETAPE 3 : Dessinon le fond
 		g.setStroke(new BasicStroke());
 		
 		//Fond de la zone de dessin
 		g.setColor(new Color(180,180,180));//Couleur de fond
 		g.fillRect(0, 0, ecartHorizontal, this.getHeight());//On définit une couleur derriere le dessin pour eviter les glitchs graphiques
 		g.fillRect(ecartHorizontal + largeurDessin, 0, this.getWidth(), this.getHeight());
 		g.fillRect(ecartHorizontal - 1, 0, largeurDessin + 1, ecartVertical);
 		g.fillRect(ecartHorizontal - 1, hauteurDessin + ecartVertical, largeurDessin + 1, ecartVertical+1);
 		
 		//Ombre du dessin
 		g.setColor(new Color(220,220,220));
 		g.fillRect(ecartHorizontal + largeurDessin, ecartVertical + 5, 5, hauteurDessin);
 		g.fillRect(ecartHorizontal + 5, ecartVertical + hauteurDessin, largeurDessin, 5);
 		
 		//ETAPE 4 : Afficher la grille
 		if(gridEnable){
 			g.setStroke(new BasicStroke(0));
 			g.setColor(new Color(180, 180, 180));
 			int compteur = 0;
 			while(compteur*widthCaseGrid <= largeurDessin){
 				g.drawLine(posXAbsolue(compteur*widthCaseGrid), posYAbsolue(0), posXAbsolue(compteur*widthCaseGrid), posYAbsolue(hauteurDessin));
 				compteur++;
 			}
 			compteur = 0;
 			while(compteur*heightCaseGrid <= hauteurDessin){
 				g.drawLine(posXAbsolue(0), posYAbsolue(compteur*heightCaseGrid), posXAbsolue(largeurDessin), posYAbsolue(compteur*heightCaseGrid));
 				compteur++;
 			}
 		}
 		
 		//ETAPE 5 : Afficher le curseur
 		if(affichageCurseur){
 			//Deux curseurs à afficher : le curseur négatif (pour plus de lisibilité) et le curseur normal
 			
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
 		
 		
 		//DERNIERE ETAPE : Redefinir la taille du JPanel dans le cas d'un redimensionnement
 		this.setPreferredSize(new Dimension(largeurDessin, hauteurDessin));
 		this.setMinimumSize(new Dimension(largeurDessin, hauteurDessin));
 		this.setMaximumSize(new Dimension(largeurDessin, hauteurDessin));
 	}
 
 	
 	/**Méthode renvoyant la coordonée X absolue dans la zone de dessin par rapport à une coordonée X relative au dessin*/
 	public int posXAbsolue(int x){
 		return x + ecartHorizontal;
 	}
 	/**Méthode renvoyant la coordonée Y absolue dans la zone de dessin par rapport à une coordonée Y relative au dessin*/
 	public int posYAbsolue(int y){
 		return y + ecartVertical;
 	}
 	/**Méthode renvoyant la coordonée X absolue du curseur du curseur dans la zone de dessin par rapport à une coordonée X relative au dessin*/
 	public int getPosX(){
 		return curseur.getPosX() + ecartHorizontal;
 	}
 	/**Méthode renvoyant la coordonée Y absolue du curseur dans la zone de dessin par rapport à une coordonée Y relative au dessin*/
 	public int getPosY(){
 		return curseur.getPosY() + ecartVertical;
 	}
 	
 	/*///
 	 * ACCESSEURS
 	 //*/
 	
 	/**Accesseur renvoyant l'écart horizontal entre le bord de la zone de dessin et du dessin
 	 * @return L'écart horizontal entre le bord de la zone de dessin et du dessin
 	 * */
 	public int getEcartHorizontal(){
 		return ecartHorizontal;
 	}
 	/**Accesseur renvoyant l'écart vertical entre le bord de la zone de dessin et du dessin
 	 * @return L'écart vertical entre le bord de la zone de dessin et du dessin
 	 * */
 	public int getEcartVertical(){
 		return ecartVertical;
 	}
 	/**Accesseur renvoyant la largeur du dessin (et non de la zonne de dessin) en pixel
 	 * @return La largeur du dessin (et non de la zonne de dessin) en pixel
 	 * */
     public int getLargeurDessin(){
     	return largeurDessin;
     }
     /**Accesseur renvoyant la hauteur du dessin (et non de la zonne de dessin) en pixel
 	 * @return La hauteur du dessin (et non de la zonne de dessin) en pixel
 	 * */
     public int getHauteurDessin(){
     	return hauteurDessin;
     }
     /**Accesseur renvoyant la couleur d'arriere plan du dessin
 	 * @return Renvoyant la couleur d'arriere plan du dessin
 	 * */
     public Color getBackground(){
     	return background;
     }
     
 	/*///
 	 * MODIFIEURS
 	 //*/
     
     /** Modifie le controleur
      *  @param c Nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.c = c;
     }
     /**Modifie la couleur d'arriere plan de la zone de dessin
      *  @param c La couleur d'arriere plan à définir */
     public void setBackground(Color c){
     	background = c;
     }
     /**Modifie la largeur du dessin
      *  @param l La largeur du dessin a definir
      *  */
     public void setLargeur(int l){
     	largeurDessin = l;
     }
     /**Modifie la hauteur du dessin
      *  @param h La hauteur du dessin a definir
      *  */
     public void setHauteur(int h){
     	hauteurDessin = h;
     }
     /**Modifie la barre d'outils associée à la zone de dessin
      *  @param b La barre d'outils à associer à la zone de Dessin
      *  */
     public void setBarreOutils(BarreOutils b){ barreOutils = b;}
     
     /**Modifie l'activation de l'affichage du curseur
      *  @param b Booleen définissant l'affichage du curseur
      *  */
     public void setAffichageCurseur(boolean b){ affichageCurseur = b;}
 
 	public boolean isGridEnable() {
 		return gridEnable;
 	}
 
 	public void setGridEnable(boolean a) {
 		gridEnable = a;
 	}
 
 	public void setWidthCaseGrid(int a) {
 		widthCaseGrid = a;
 	}
 
 	public void setHeightCaseGrid(int a) {
 		heightCaseGrid = a;
 	}
 
 	public boolean isGridMagnetismEnable() {
 		return gridMagnetismEnable;
 	}
 
 	public void setGridMagnetismEnable(boolean g) {
 		gridMagnetismEnable = g;
 	}
 	
 	public  void setPixelArtModeEnable(boolean g) {
 		pixelArtModeEnable = g;
 	}
 	public int getWidthCaseGrid(){
 		return widthCaseGrid;
 	}
 
 	public int getHeightCaseGrid(){
 		return heightCaseGrid;
 	}
 }
