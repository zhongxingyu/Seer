 package vooga.fighter.model.objects;
 
 import java.awt.Dimension;
 import java.awt.geom.Point2D;
 
 import util.Location;
 import util.Pixmap;
 import vooga.fighter.model.utils.State;
 import vooga.fighter.model.utils.UpdatableLocation;
 
 public class MouseClickObject extends GameObject {
 	private static final String IMAGE_LOC = "fighter/images/fire.png";
 	private static final Dimension SIZE = new Dimension(30, 30);
 	private int myTicks;
 
 	public MouseClickObject(Point2D loc) {
 		setLocation(new UpdatableLocation(loc.getX(),loc.getY()));
 		State mouse = new State(this, 1);
 		mouse.populateImage(new Pixmap(IMAGE_LOC), 0);
 		mouse.populateSize(SIZE, 0);
 		addState("mouse", mouse);
 		setCurrentState("mouse");
 		myTicks = 0;
 	}
 
 	@Override
 	public boolean shouldBeRemoved() {
 		return(myTicks<100);
 	}
 	
 	@Override
 	public void update() {
 		myTicks ++;
 	}
 }
