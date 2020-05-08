 import javax.swing.JPanel;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.lang.Math;
 
 @SuppressWarnings("serial")
 public class Curseur extends JPanel {
 	/////////////////
 	//  VARIABLES  //
 	/////////////////
 	private int posX;
 	private int posY;
 	private int orientation;
 	private int taille;
 	private int type; //0 : Crayon, 1 : Gomme
 	private Color couleur;
 	private Controleur controleur;
 	private boolean isDown=false;//par defaut false: donc penup
 	private int epaisseur;
	private int forme;
 
 	
 	/////////////////////
 	//  CONSTRUCTEURS  //
 	/////////////////////
     /**
 	 * Constructeur test
 	 */
 	Curseur (int posX, int posY, int orientation, int taille, Color couleur, int type, int epaisseur, int forme) {
 		this.posX = posX;
 		this.posY = posY;
 		this.orientation = orientation;
 		this.taille = taille;
 		this.couleur = couleur;
 		this.type = type;
 		this.epaisseur = epaisseur;
 		this.forme=forme;
     }	
 	
 	//////////////////
 	//  ACCESSEURS  //
 	//////////////////
    
 	public int getPosX () {
 		return this.posX;
 	}
 	public int getPosY () {
 		return this.posY;
 	}
     /**
      *  Accesseur de la variable orientation
      *  @return orientation
      */
 	public double getOrientation () {
 		return this.orientation;
 	}
 	
 	/**
 	 * Accesseur de la variable taille
 	 * @return taille
 	 */
 	public int getTaille () {
 		return this.taille;
 	}
 
     /**
      *  Accesseur de la variable type
      *  @return type
      */
 	public int getType () {
 		return this.type;
 	}
 
     /**
      *  Accesseur de la variable couleur
      *  @return couleur
      */
 	public Color getCouleur () {
 		return this.couleur;
 	}
 	
 	/**
 	 * Accesseur de la variable isDown
 	 * @return
 	 */
 	public boolean isDown(){
 		return this.isDown;
 	}
 	
 	public int getEpaisseur(){
 		return this.epaisseur;
 	}
 	
 	public int getForme(){
 		return this.forme;
 	}
 	/////////////////
 	//  MODIFIEURS //
 	/////////////////
 
 	public void setPosition(int x, int y){
 		this.posX = x;
 		this.posY = y;
 	}
 	public void setPosX (int posX) {
 		this.posX = posX;
 	}
 	public void setPosY (int posY) {
 		this.posY = posY;
 	}
     /**
      *  Modifieur de la variable orientation
      *  @param orientation nouvelle valeur de  l'orientation
      */
 	public void setOrientation (int orientation) {
 		this.orientation = orientation;
 	}
 	
 	/**
 	 * Modifieur de la variable taille
 	 * @param taille nouvelle valeur de la taille
 	 */
 	public void setTaille (int taille) {
 		this.taille = taille;
 	}
 	
     /**
      *  Modifieur de la variable type
      *  @param type nouvelle valeur du type
      */
 	public void setType (int type) {
 		this.type = type;
 	}
 	
 	public void setForme(int forme){
 		this.forme=forme;
 	}
 	
     /**
      *  Modifieur de la variable couleur
      *  @param couleur nouvelle valeur de couleur
      */
 	public void setCouleur (Color couleur) {
 		this.couleur = couleur;
 	}
 
     /**
      *  Modifieur du controleur
      *  @param controleur nouveau controleur
      */
 	public void setControleur(Controleur controleur)
     {
         this.controleur = controleur;
     }
 	
 	/**
 	 * Modifieur de la variable isDown
 	 * @param isDown
 	 */
 	public void setIsDown(boolean isDown){
 		this.isDown=isDown;
 	}
      
     public void setEpaisseur(int epaisseur){
     	this.epaisseur=epaisseur;
     }
      
     public void setCouleurRouge(int i){
     	couleur = new Color(i, couleur.getGreen(), couleur.getBlue());
     }
     public void setCouleurVert(int i){
     	couleur = new Color(couleur.getRed(), i, couleur.getBlue());
     }
     public void setCouleurBleu(int i){
     	couleur = new Color(couleur.getRed(), couleur.getGreen(), i);
     }
 }
