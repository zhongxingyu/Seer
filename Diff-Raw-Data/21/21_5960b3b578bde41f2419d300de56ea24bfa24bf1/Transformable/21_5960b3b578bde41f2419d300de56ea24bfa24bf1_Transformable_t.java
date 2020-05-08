 package blank.game;
 
 import java.awt.geom.Point2D;
 
 import org.lwjgl.opengl.GL11;
 
 /**
  * Erlaubt das beliebige Verschieben, Drehen und Skalieren seiner
  * Unterklassen...
  * 
  * @author David Bellingroth
  * 
  */
 public abstract class Transformable implements Drawable {
 
 	private Point2D translate = new Point2D.Double(0, 0);
 
 	public Point2D getTranslate() {
 		return translate;
 	}
 
 	public void setTranslate(Point2D translate) {
 		this.translate = translate;
 	}
 
 	public Point2D getRotationPoint() {
 		return rotationPoint;
 	}
 
 	public void setRotationPoint(Point2D rotationPoint) {
 		this.rotationPoint = rotationPoint;
 	}
 
 	public double getRotationAngle() {
 		return rotationAngle;
 	}
 
 	public void setRotationAngle(double rotationAngle) {
 		this.rotationAngle = rotationAngle;
 	}
 
 	public Point2D getScaleFactor() {
 		return scaleFactor;
 	}
 
 	public void setScaleFactor(Point2D scaleFactor) {
 		this.scaleFactor = scaleFactor;
 	}
 
 	private Point2D rotationPoint = new Point2D.Double(0, 0);
 	private double rotationAngle = 0;
 	private Point2D scaleFactor = new Point2D.Double(1, 1);
 
 	/**
 	 * Transformiert die aktuelle Matrix so wie gewnscht. Vorher bitte
 	 * GL11.glPushMatrix() und nachher GL11.glPopMatrix() aufrufen
 	 */
 	protected void transform() {
 		GL11.glTranslated(translate.getX(), translate.getY(), 0);
		
		
		  
 			GL11.glTranslated(rotationPoint.getX(), rotationPoint.getY(), 0);
			// falls Rotation gewnscht
			if (rotationAngle != 0)GL11.glRotatef((float) rotationAngle, 0f, 0f, 1);
			
			GL11.glScaled(scaleFactor.getX(), scaleFactor.getY(), 1);
			
 			GL11.glTranslated(-rotationPoint.getX(), -rotationPoint.getY(), 0);
		
 	}
 
 }
