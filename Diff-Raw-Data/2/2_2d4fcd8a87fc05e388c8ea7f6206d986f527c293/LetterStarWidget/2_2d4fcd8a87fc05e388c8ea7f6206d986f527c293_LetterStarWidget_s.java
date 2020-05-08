 package arithmea.client.widgets;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import arithmea.shared.gematria.LatinLetter;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class LetterStarWidget extends Composite {
 	private final VerticalPanel panel = new VerticalPanel();
 	private Canvas canvas;
 	private Map<LatinLetter, Double> xPoints = new HashMap<LatinLetter, Double>(LatinLetter.values().length);
 	private Map<LatinLetter, Double> yPoints = new HashMap<LatinLetter, Double>(LatinLetter.values().length);
 	private Map<LatinLetter, Double> xLetterPos = new HashMap<LatinLetter, Double>(LatinLetter.values().length);
 	private Map<LatinLetter, Double> yLetterPos = new HashMap<LatinLetter, Double>(LatinLetter.values().length);
 	
 	public LetterStarWidget(int width, int height) {
 		canvas = Canvas.createIfSupported();
 		canvas.setCoordinateSpaceHeight(height);
 		canvas.setCoordinateSpaceWidth(width);
 		
 		initPositions();
 	    drawStar();
 	    
 	    panel.add(canvas);
 			
 		initWidget(panel);
 		setStyleName("tree-of-life");
 	}
 
 	public Canvas getCanvas() {
 		return canvas;
 	}
 
 	private void initPositions() {
 		for (LatinLetter ll: LatinLetter.values()) {
 			double angle = (ll.iaValue - 1) * (360.0 / (LatinLetter.values().length));
 			angle = angle * Math.PI / 180; //radians to degree
 
 			double x = canvas.getCoordinateSpaceWidth() / 2 + ((Math.sin(angle) * canvas.getCoordinateSpaceWidth() * 0.40));
 			double y = canvas.getCoordinateSpaceHeight() / 2 - ((Math.cos(angle) * canvas.getCoordinateSpaceHeight() * 0.40));
 			xPoints.put(ll, x);
 			yPoints.put(ll, y);
 
 			double xLetter = canvas.getCoordinateSpaceWidth() / 2 + ((Math.sin(angle) * canvas.getCoordinateSpaceWidth() * 0.44) -4);
 			double yLetter = canvas.getCoordinateSpaceHeight() / 2 - ((Math.cos(angle) * canvas.getCoordinateSpaceHeight() * 0.44) -4);
 			xLetterPos.put(ll, xLetter);
 			yLetterPos.put(ll, yLetter);
 		}
 	}
 
 
 	private void drawStar() {
 		Context2d ctx = canvas.getContext2d();
 		
 		//reset content
 		ctx.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
 		ctx.beginPath();
 		
 		ctx.setStrokeStyle(CssColor.make("#FFFFFF"));
 		ctx.setFillStyle(CssColor.make("#FFFFFF"));
 		ctx.setLineWidth(1);
 
 		//draw circle
		ctx.arc(canvas.getCoordinateSpaceWidth() / 2, canvas.getCoordinateSpaceHeight() / 2, canvas.getCoordinateSpaceWidth() * 0.4, 0, 360);
 
 		//draw letters
 		for (LatinLetter ll: LatinLetter.values()) {
 			ctx.fillText(ll.name(), xLetterPos.get(ll), yLetterPos.get(ll));
 		}
 		
 		ctx.closePath();
 		ctx.stroke();
 	}
 
 	public void setWord(String word) {
 		drawStar();
 		
 		if (word.length() <= 1) {
 			return;
 		}
 		for (int i = 0; i < word.length() -1; i++) {
 			String letter = word.substring(i, i+1);
 			String nextLetter = word.substring(i+1, i+2);
 			
 			LatinLetter current = LatinLetter.valueOf(letter);
 			LatinLetter next = LatinLetter.valueOf(nextLetter);
 			
 			if (next != null && current != null) {
 				drawLine(xPoints.get(current), yPoints.get(current), 
 						xPoints.get(next), yPoints.get(next));
 			}
 		}
 	}
 	
 	private void drawLine(double startX, double startY, double endX, double endY) {
 		canvas.getContext2d().setStrokeStyle(CssColor.make("#FFFFFF"));
 		canvas.getContext2d().setLineWidth(3);
 
 		canvas.getContext2d().beginPath();
 		canvas.getContext2d().moveTo(startX, startY);
 		canvas.getContext2d().lineTo(endX, endY);
 		
 		canvas.getContext2d().closePath();
 		canvas.getContext2d().stroke();
 	}
 }
