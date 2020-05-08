 package slick;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 public class Balle extends Thread implements Drawable {
 
 	private Vector2f vect;
 	private float posX;
 	private float posY;
 	private String location;
 	private int sizeX;
 	private int sizeY;
 	private Image visuel;
 	private float distance;
 
 	public Balle(float x, float y, float dirX, float dirY) {
 		this.vect = new Vector2f(dirX - x, dirY - y).normalise();
 		this.posX = x;
 		this.posY = y;
 		this.distance = 350;
 		this.location = "img/balle.png";
 		this.initVisuel();
 	}
 
 	@Override
 	public void run() {
 		for (; this.distance > 0;) {
 			try {
				sleep(5);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			this.setPosX(this.getPosX() + this.vect.x);
 			this.setPosY(this.getPosY() + this.vect.y);
 			this.distance = this.distance - this.vect.length();
 		}
 	}
 
 	public float getDistance() {
 		return distance;
 	}
 
 	@Override
 	public void affich(int xmax, int ymax, Graphics g) {
 		g.drawImage(this.getVisuel(), posX - sizeX / 2, posY - sizeY / 2);
 	}
 
 	@Override
 	public Image getVisuel() {
 		return this.visuel;
 	}
 
 	public float getPosX() {
 		return this.posX;
 	}
 
 	public float getPosY() {
 		return this.posY;
 	}
 
 	@Override
 	public String getLocation() {
 		return this.location;
 	}
 
 	@Override
 	public int getSizeX() {
 		return this.sizeX;
 	}
 
 	@Override
 	public int getSizeY() {
 		return this.sizeY;
 	}
 
 	@Override
 	public void initVisuel() {
 		try {
 			this.visuel = new Image(this.location);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		this.sizeX = this.visuel.getWidth();
 		this.sizeY = this.visuel.getHeight();
 	}
 
 	public Vector2f getVect() {
 		return vect;
 	}
 
 	public void setVect(Vector2f vect) {
 		this.vect = vect;
 	}
 
 	public void setPosX(float posX) {
 		this.posX = posX;
 	}
 
 	public void setPosY(float posY) {
 		this.posY = posY;
 	}
 }
