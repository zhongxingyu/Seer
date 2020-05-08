 import java.awt.Image;
 
 public class Throwable extends Entity{
 	private double fancyWeight;
 	private Image fancyIcon;
 	private double throwStartSpeed;
 	private long tossStart;
 	private double tossStrength;
 	private boolean throwing;
 	
 	public Throwable(double xPosition, double yPosition, double zPosition) {
 		super(xPosition, yPosition, zPosition);
 		// TODO Auto-generated constructor stub
 	}
 	@Override
 	protected void updateSpeed() {
 		if (throwing) {
 			if (getyPosition()>0) {
 				setySpeed(tossStrength - 0.00981 * (System.currentTimeMillis() - tossStart));
 			} else if (getyPosition()<=0) {
 				setyPosition(0);
 				setySpeed(0);
 				throwing=false;
 				// TODO Abstand zum Boden beachten
 			}
 		}
 	}
 	@Override
 	public boolean checkForCollision(Entity e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	public void toss(int direction, int tossStrength) {
 		this.tossStrength = tossStrength;
 		throwing = true;
 		tossStart = System.currentTimeMillis();
 		if (direction==1) { // Links
 			setxSpeed(tossStrength*(-1));
 		} else if (direction==2)  { // Rechts
 			setxSpeed(tossStrength);
 		}
 		setySpeed(tossStrength);
 	}
 }
