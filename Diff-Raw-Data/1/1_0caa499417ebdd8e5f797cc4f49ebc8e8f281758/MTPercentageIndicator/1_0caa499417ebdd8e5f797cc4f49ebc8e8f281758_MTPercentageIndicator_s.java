 package view.widgets;
 
 import org.mt4j.components.visibleComponents.shapes.MTRectangle;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.math.Vector3D;
 
 import processing.core.PApplet;
 
 public class MTPercentageIndicator extends MTRectangle {
 	private float value;
 	private final float margin = 2;
 	private MTRectangle bars[] = new MTRectangle[10];
 	
 	public MTPercentageIndicator(PApplet pApplet, float width, float height) {
 		super(pApplet, 0, 0, 0, width, height);
 		setComposite(true);
 		float sWidth = (width-9*margin)/10;
 		
 		this.setNoFill(true);
 		this.setNoStroke(true);
 		
 		for (int i=0; i<10; i++) {
 			float x = i*(margin+sWidth)+sWidth/2;
 			bars[i] = new MTRectangle(pApplet, sWidth, height);
 			this.addChild(bars[i]);
 			bars[i].setPositionRelativeToParent(new Vector3D(x,height/2));
 			bars[i].setNoStroke(true);
 		}
 	}
 	
 	public void setValue(float value) {
 		this.value = value;
 		
 		double floor = Math.floor(value*10);
 		for (int i=0; i<10; i++) {
 			if (i<floor)
 				bars[i].setFillColor(new MTColor(255, 255, 255, 255));
 			else
 				bars[i].setFillColor(new MTColor(255, 255, 255, 50));
 		}
 	}
 	
 	public float getValue() {
 		return this.value;
 	}

 }
