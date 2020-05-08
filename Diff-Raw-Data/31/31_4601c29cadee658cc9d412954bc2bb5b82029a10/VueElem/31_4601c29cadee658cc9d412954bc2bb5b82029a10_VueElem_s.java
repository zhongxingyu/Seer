 /**
  * 
  */
 package aeroport.sgbag.views;
 
 import lombok.AllArgsConstructor;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Transform;
 
 import aeroport.sgbag.controler.ViewSelector;
 import aeroport.sgbag.utils.Geom;
 import aeroport.sgbag.utils.Rectangle2D;
 
 /**
  * @author Arnaud Lahache
  * 
  */
 @NoArgsConstructor
 @AllArgsConstructor
 public abstract class VueElem implements Viewable {
 	
 	public static final double RATIO = 0.5;
 
 	@Getter
 	@Setter
 	protected VueHall parent;
 
 	@Getter
 	@Setter
 	protected int x;
 
 	@Getter
 	@Setter
 	protected int y;
 
 	@Getter
 	@Setter
 	protected int width;
 
 	@Getter
 	@Setter
 	protected int height;
 
 	@Getter
 	@Setter
 	protected float angle = 0;
 
 	@Getter
 	@Setter
 	protected Image image;
 	
 	@Getter
 	@Setter
 	protected boolean selected;
 	
 	public VueElem(VueHall parent) {
 		this.parent = parent;
 	}
 	
 	public void destroy() {
 		parent.retirerVue(this);
 		ViewSelector.getInstance().removeByView(this);
 	}
 
 	public Rectangle2D getRectangle2D() {
 		Point centre = new Point(x, y);
 		Point p1 = new Point(x - width / 2, y - height / 2);
 		Point p2 = new Point(x + width / 2, y - height / 2);
 		Point p3 = new Point(x - width / 2, y + height / 2);
 		Point p4 = new Point(x + width / 2, y + height / 2);
 		return new Rectangle2D(Geom.getRotatedPoint(p1, centre, angle),
 				Geom.getRotatedPoint(p2, centre, angle),
 				Geom.getRotatedPoint(p3, centre, angle),
 				Geom.getRotatedPoint(p4, centre, angle));
 	}
 
 	/**
 	 * @see aeroport.sgbag.views.Viewable#updateView()
 	 */
 	public abstract void updateView();
 
 	/**
 	 * @see aeroport.sgbag.views.Viewable#draw()
 	 */
 	public void draw() {
 		GC gc = this.parent.getGcBuffer();
 
 		// We get the size of the original image :
 		Rectangle rect = image.getBounds();
 
 		// We create a transform in order to rotate and translate the image :
 		Transform trImage = new Transform(parent.getDisplay());
 		trImage.translate(this.x, this.y);
 		trImage.rotate(this.angle);
 		gc.setTransform(trImage);
 		
 		// If the element is selected :
 		if(selected) {
 			gc.setAlpha(100);
 		} else {
 			gc.setAlpha(255);
 		}
 
 		// Then we just draw the image on the GC :
 		gc.drawImage(this.image, 0, 0, rect.width, rect.height,
 				-this.width / 2, -this.height / 2, this.width, this.height);
 
 		// We no longer need the transform :
 		gc.setTransform(null);
 		trImage.dispose();
 	}
 	
 	/**
 	 * @see aeroport.sgbag.views.Viewable#isClicked(Point p)
 	 */
 	public boolean isClicked(Point point){
 		Point centre = new Point(x, y);
 		Point rotatedPoint = Geom.getRotatedPoint(point, centre, -angle);
 		
 		if(rotatedPoint.x >= x - width / 2 && rotatedPoint.x <= x + width ){
 			if(rotatedPoint.y >= y - height / 2 && rotatedPoint.y <=   y + height / 2){
 				return true;
 			}
 		}
 		return false;
 	}
 }
