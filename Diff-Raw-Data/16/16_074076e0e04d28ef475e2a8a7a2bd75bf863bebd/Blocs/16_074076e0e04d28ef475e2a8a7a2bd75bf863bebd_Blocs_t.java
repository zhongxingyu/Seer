 package blocs;
 
 import net.phys2d.raw.Body;
 import net.phys2d.raw.StaticBody;
 import net.phys2d.raw.shapes.Box;
 
 import org.newdawn.slick.Image;
 
 import personnages.Robot;
 
 import interfaces.Drawable;
 import interfaces.SlickAdapter;
 
 /**
  * <i> Elle synthetise l'aspect visuel et physique d'un bloc</i>
  * 
  * @author Equipe RoboTech
  * 
  **/
 public abstract class Blocs implements Drawable, SlickAdapter {
 
 	/**
 	 * Signal �mis par un Bouton
 	 */
 	boolean signal = false;
 	/**
 	 * Image du Blocs
 	 */
 	Image boxImage;
 	/**
 	 * Largeur du Bloc
 	 */
 	private float width;
 	/**
 	 * Hauteur du Bloc
 	 */
 	private float height;
 	/**
 	 * Corps Physique du Bloc
 	 */
 	Body body;
 
 	/**
 	 * Coordonn�e du centre du blocs
 	 */
 	Point center;
 
 	/**
 	 * Personnage sur le blocs
 	 */
 	private boolean onBloc = false;
 
 	public void setWidth(float Width) {
 		this.width = Width;
 	}
 
 	public void setHeight(float Height) {
 		this.height = Height;
 	}
 
 	public float getWidth() {
 		return width;
 	}
 
 	public float getHeight() {
 		return height;
 	}
 
 	public Body getBody() {
 		return body;
 	}
 
 	public Image getImage() {
 		return boxImage;
 	}
 
 	public void setImage(Image box_image) {
 		this.boxImage = box_image;
 	}
 
 	public void setSignal(boolean signal) {
 		this.signal = signal;
 	}
 
 	public boolean getSignal() {
 		return signal;
 	}
 
 	public Body setBody() {
 		return body;
 	}
 
 	/**
 	 * @param box_image
 	 *            Image du rendu du blocs
 	 * @param Width
 	 *            Largeur du Bloc
 	 * @param Height
 	 *            Hauteur du Bloc
 	 * @param x
 	 *            Abscisse du Bloc
 	 * @param y
 	 *            Ordonn�e du Bloc
 	 */
 	public Blocs(Image box_image, float Width, float Height, Point org) {
 		this.boxImage = box_image;
 		this.width = Width;
 		this.height = Height;
 		this.body = new StaticBody("StaticBody_" + org.getX() + "_"
 				+ org.getY(), new Box(Width, Height));
 		center = org;
 		body.setPosition(org.getX(), org.getY());
 		onBloc = false;
 	}
 
 	public void setOnBloc(boolean value) {
 		onBloc = value;
 	}
 
 	public boolean getOnBloc() {
 		return onBloc;
 	}
 
 	public void setBody(Body Body) {
 		this.body = Body;
 	}
 
 	/**
 	 * Permet de d�terminer si il y a contact avec le robot et le blocs
 	 * 
 	 * @param player
 	 *            le Robot contr�l� par le joueur
 	 */
 	
 	public void collision(Robot player) {
 		float eps = (float) 1e-01;
 		float n = player.getTaille();
 		if((Math.abs(player.getY() - center.getY()) - (getHeight()/2 + player.getTaille()/2)) <eps){
 			float x_gauche = player.getX() - n / 2;
 			float x_droite = player.getX() + n / 2;
 
 			float p_gauche = center.getX() - getWidth() / 2;
 			float p_droite = center.getX() + getWidth() / 2;
 
 			if ((x_droite <= p_droite && x_droite >= p_gauche)
 					|| (x_gauche <= p_droite && x_gauche >= p_gauche)) {
 				onBloc = true;
 			}
 			collisionAction(player);
 		}
		onBloc = false;
 	}
 	/**
 	 * Permet que les classes filles ont un comportement diff�rents lors d'une
 	 * collision
 	 * 
 	 * @param player
 	 *            le Robot contr�l� par le joueur
 	 */
 	public abstract void collisionAction(Robot player);
 
 	@Override
 	public String toString() {
 		String res = "";
 		res = res + "Position = " + center.toString();
 		res = res + "Hauteur = " + getHeight() + "\n";
 		res = res + "Largeur = " + getWidth() + "\n";
 		res = res + "Signal = " + getSignal() + "\n";
 		res = res + "On_bloc = " + getOnBloc() + "\n";
 		return res;
 	}
 
 }
