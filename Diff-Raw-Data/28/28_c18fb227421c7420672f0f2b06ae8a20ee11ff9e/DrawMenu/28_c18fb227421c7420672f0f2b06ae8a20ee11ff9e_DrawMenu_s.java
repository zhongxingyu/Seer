 import processing.core.PApplet;
 import processing.core.PVector;
 
 public class DrawMenu extends GameElement {
 	private PVector mprev;
 	private PVector mcurr;
 	private float radius = 60;
 	private boolean done = false;
 
 	public DrawMenu(PApplet par, String i, PVector p) {
 		super(par, i);
 		position = p;
 		setPriority(4);	
 		mprev = new PVector(parent.mouseX, parent.mouseY);
 		mcurr = new PVector(parent.mouseX, parent.mouseY);
 	}
 
 	public void whileUnfocused() {
 		parent.fill(255);
 		parent.ellipse(position.x, position.y, 10, 10);
 		if(done) {
			System.out.println("deleting because done was true");
 			World.deleteElement(this);
 		}
 	}
 
 	@Override
 	public void whileFocused() {
 		renderMenu();
 		mcurr = new PVector(parent.mouseX, parent.mouseY);
 		if(parent.dist(position.x, position.y, mprev.x, mprev.y) <= radius) {
			if(parent.dist(position.x, position.y, mcurr.y, mcurr.y) > radius) {
 				setDrawType(mcurr);
 				done = true;
 			}
 		}
 		mprev = new PVector(mcurr.x, mcurr.y);
 	}
 
 	private void setDrawType(PVector loc) {
 		boolean right = loc.x >= position.x;
 		PVector comparator = new PVector(0,1);
 		PVector relative = PVector.sub(loc, position);
 		float angle = PVector.angleBetween(relative, comparator);
 		if(within(angle, 0, parent.PI/8)) {
 			ElementFactory.setType(ElementFactory.ElementType.FORCE_OBJECT);
 		} else if(within(angle, parent.PI/8, 3 * parent.PI/8)) {
 			if(right) {
 				ElementFactory.setType(ElementFactory.ElementType.PORTALS);
 			} else {
 				ElementFactory.setType(ElementFactory.ElementType.SCATTERER);
 			}
 		}
 	}
 
 	private boolean within(float A, float B, float C) {
 		return A >= B && A <= C;
 	}
 
 	private void renderMenu() {
 		parent.stroke(parent.color(255,255,255,40));
 		parent.noFill();
 		parent.strokeWeight(radius);
 		parent.ellipse(position.x, position.y, 2 * radius, 2 * radius);
 		parent.noStroke();
 		parent.fill(0);
 		PVector to = new PVector(0, radius);
 		for(int i = 0; i < 8; ++i) {
 			PVector at = PVector.add(position, to);
 			parent.ellipse(at.x, at.y, 10, 10);
 			to.rotate(parent.QUARTER_PI);
 		}
 	}
 	
 	@Override
 	public boolean mouseOver() {
 		return parent.dist(position.x, position.y, parent.mouseX, parent.mouseY) <= radius + radius/2;
 	}
 
 	@Override
 	public boolean dropFocus() {
 	//	System.out.println( "Dropping menu." );
 		return done && !mouseOver() && !parent.mousePressed;
 	}
 }
