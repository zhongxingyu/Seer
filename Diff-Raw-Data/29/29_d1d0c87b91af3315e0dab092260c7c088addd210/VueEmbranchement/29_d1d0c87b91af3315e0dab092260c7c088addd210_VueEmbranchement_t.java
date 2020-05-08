 package aeroport.sgbag.views;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 
 import aeroport.sgbag.kernel.Noeud;
 
 public class VueEmbranchement extends VueElem {
 
 	@Getter
 	@Setter
 	private Noeud noeud;
 	
 	@Getter
 	@Setter
 	private double coeffiscientImage;
	
	public static int defaultSizeOfNode = 50;
 
 	public VueEmbranchement(VueHall parent) {
 		super(parent);
 		image = new Image(parent.getDisplay(), "data/img/embranchement.png");
 		
 		Rectangle rect = image.getBounds();
		//coeffiscientImage = 1.6;
 		
		this.width = defaultSizeOfNode;//(int) (rect.width*coeffiscientImage);
		this.height = defaultSizeOfNode;//(int) (rect.height*coeffiscientImage);
 	}
 
 	@Override
 	public void updateView() {
 		Rectangle rect = image.getBounds();
		//this.width = ;//(int) (coeffiscientImage*rect.width);
		//this.height = (int) (coeffiscientImage*rect.height);
 	}
 }
