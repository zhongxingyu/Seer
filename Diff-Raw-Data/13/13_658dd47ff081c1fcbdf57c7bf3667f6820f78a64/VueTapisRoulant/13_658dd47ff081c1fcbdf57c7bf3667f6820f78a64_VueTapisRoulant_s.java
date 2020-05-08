 package aeroport.sgbag.views;
 
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Transform;
 import org.eclipse.swt.widgets.Canvas;
 
 import lombok.Getter;
 import lombok.Setter;
 import aeroport.sgbag.kernel.TapisRoulant;
 import aeroport.sgbag.utils.Geom;
 
 public class VueTapisRoulant extends VueElem {
 
 	@Getter
 	@Setter
 	private TapisRoulant tapisRoulant;
 
 	public VueTapisRoulant(Canvas parent, TapisRoulant tapisRoulant) {
 		super((VueHall) parent);
 		
 		this.tapisRoulant = tapisRoulant;
 		
 		this.image = new Image(parent.getDisplay(), "data/img/tapis.png");
 		
 		Rectangle rect = image.getBounds();
 		this.width = rect.width;
 		this.height = rect.height;
 
 	}
 
 	@Override
 	public void updateView() {
		offsetX = (int) (-Math.cos(this.angle) * this.width/2);
		offsetY = (int) (-Math.sin(this.angle) * this.width/2);
 	}
 
 }
