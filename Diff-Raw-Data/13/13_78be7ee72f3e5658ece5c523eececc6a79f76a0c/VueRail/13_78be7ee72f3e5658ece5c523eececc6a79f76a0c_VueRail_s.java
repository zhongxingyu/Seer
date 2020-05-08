 /**
  * 
  */
 package aeroport.sgbag.views;
 
import lombok.*;
 
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
 
 import aeroport.sgbag.kernel.Rail;
 
 /**
  * @author Arnaud Lahache
  *
  */
 @NoArgsConstructor
 public class VueRail extends VueElem {
 	
 	@Getter
 	@Setter
 	private Rail rail;
 	
 	@Getter
 	@Setter
 	private float angle = 0;
 
 	public VueRail(VueHall parent) {
 		super(parent);
 		this.image = new Image(parent.getDisplay(), "data/img/rail.png");
 	}
 	
 	/**
 	 * @see aeroport.sgbag.views.VueElem#updateView()
 	 */
 	@Override
 	public void updateView() {
 		// TODO implémentation méthode update
 
 	}
 
 	/**
 	 * @see aeroport.sgbag.views.VueElem#draw()
 	 */
 	@Override
 	public void draw() {
 		GC gc = this.parent.getGcBuffer();
 		
 		// Create a working copy of the original image :
 		Rectangle rect = image.getBounds();
 		Image imageCpy = new Image(parent.getDisplay(), rect);
 		GC gcImage = new GC(imageCpy);
 		
 		// We create a transform in order to rotate the image :
 		Transform trImage = new Transform(parent.getDisplay());
 		trImage.rotate(angle);
 		gcImage.setTransform(trImage);
 		
 		// Now we're going to draw the expanded image :
 		gcImage.drawImage(this.image, 0, 0, rect.width, rect.height, 0, 0, rail.getLength(), rect.height);
 		
 		// We no longer need the GC for imageCpy :
 		trImage.dispose();
 		gcImage.dispose();
 		
 		// We will add this imageCpy into the main GC :
 		gc.drawImage(imageCpy, this.x, this.y);
 		
 		// We no longer need imageCpy :
 		imageCpy.dispose();
 	}
 
 }
