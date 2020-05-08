 /**
  * 
  */
 package aeroport.sgbag.views;
 
 import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;

 import lombok.*;
 
 /**
  * @author Arnaud Lahache
  * 
  */
 @NoArgsConstructor
 @AllArgsConstructor
 @RequiredArgsConstructor
 public abstract class VueElem implements Viewable {
 
 	@Getter
 	@Setter
 	@NonNull
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
 	protected Image image;
 
 	/**
 	 * @see aeroport.sgbag.views.Viewable#updateView()
 	 */
 	public abstract void updateView();
 
 	/**
 	 * @see aeroport.sgbag.views.Viewable#draw()
 	 */
 	public abstract void draw();
 
 	/**
 	 * @see aeroport.sgbag.views.Viewable#isClicked()
 	 */
 	public boolean isClicked() {
 		// TODO calcul en fonction de la position de la souris et de des
 		// propriétés x, y, width, height.
 		return false;
 	}
 
 }
