 package at.ppmrob.autopilot;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Vector;
 
 import at.ppmrob.examples.main.LastKnownCircleLinePosition;
 import at.ppmrob.featuredetection.MyCircle;
 
 public class CircleInformation {
 	
 	private static final Long LAST_CIRCLE_FOUND_TIMEOUT = 6000l;
 	
 	private long circleFoundTime;
 	private long circleFoundTimeDifference;
 	private Vector<MyCircle> detectedCircles;
 	private Point2D.Double averageBullsEyeCenter;
 
 	private LastKnownCircleLinePosition lastKnownCirclePosition;
 
 	private int heightDroneCamera = 144; //240 144
 	private int widthDroneCamera = 176; //320 176
 	private Rectangle2D.Double greenZoneCenterRectangle = new Rectangle2D.Double(heightDroneCamera*0.33f, 1, heightDroneCamera*0.33f, widthDroneCamera-1);
 	private Rectangle2D.Double redZoneRightSideRectangle = new Rectangle2D.Double(heightDroneCamera*0.66f, 1, heightDroneCamera*0.33f, widthDroneCamera-1);
 
 	private Rectangle2D.Double upperHalfSideRectangle = new Rectangle2D.Double(1, 1, heightDroneCamera-2, widthDroneCamera*0.5);
 	private Rectangle2D.Double bottomHalfSideRectangle = new Rectangle2D.Double(1, widthDroneCamera*0.5+2, heightDroneCamera-2, widthDroneCamera*0.5f-2);
 
 
 	private Rectangle2D.Double redZoneLeftSideRectangle = new Rectangle2D.Double(1, 1, heightDroneCamera*0.33f, widthDroneCamera-1);
 	public Rectangle2D.Double getRedZoneLeftSideRectangle() {
 		return redZoneLeftSideRectangle;
 	}
 
 	public void setRedZoneLeftSideRectangle(
 			Rectangle2D.Double redZoneLeftSideRectangle) {
 		this.redZoneLeftSideRectangle = redZoneLeftSideRectangle;
 	}
 
 	public Rectangle2D.Double getGreenZoneCenterRectangle() {
 		return greenZoneCenterRectangle;
 	}
 
 	public void setGreenZoneCenterRectangle(
 			Rectangle2D.Double greenZoneCenterRectangle) {
 		this.greenZoneCenterRectangle = greenZoneCenterRectangle;
 	}
 
 	public Rectangle2D.Double getRedZoneRightSideRectangle() {
 		return redZoneRightSideRectangle;
 	}
 
 	public void setRedZoneRightSideRectangle(
 			Rectangle2D.Double redZoneRightSideRectangle) {
 		this.redZoneRightSideRectangle = redZoneRightSideRectangle;
 	}
 
 	public Rectangle2D.Double getUpperHalfSideRectangle() {
 		return upperHalfSideRectangle;
 	}
 
 	public void setUpperHalfSideRectangle(Rectangle2D.Double upperHalfSideRectangle) {
 		this.upperHalfSideRectangle = upperHalfSideRectangle;
 	}
 
 	public Rectangle2D.Double getBottomHalfSideRectangle() {
 		return bottomHalfSideRectangle;
 	}
 
 	public void setBottomHalfSideRectangle(
 			Rectangle2D.Double bottomHalfSideRectangle) {
 		this.bottomHalfSideRectangle = bottomHalfSideRectangle;
 	}
 
 
 	public long getCircleFoundTime() {
 		return circleFoundTime;
 	}
 
 	public void setCircleFoundTime(long circleFoundTime) {
 		this.circleFoundTime = circleFoundTime;
 	}
 
 	public Vector<MyCircle> getDetectedCircles() {
 		return detectedCircles;
 	}
 
 	public void setDetectedCircles(Vector<MyCircle> detectedCircles) {
 
 		int circlesCount = 0;
 		int xCoordCount = 0;
 		int yCoordCount = 0;
 
 		synchronized (detectedCircles) {
 			this.detectedCircles = detectedCircles;
 			if(detectedCircles!=null){
 				circlesCount=detectedCircles.size();
 				if(circlesCount>0){
 					for(MyCircle circle_n:detectedCircles){
 						xCoordCount+=circle_n.center.x();
 						yCoordCount+=circle_n.center.y();
 					}
 					averageBullsEyeCenter.setLocation((xCoordCount/circlesCount), (yCoordCount/circlesCount));
 
 					if(greenZoneCenterRectangle.contains(averageBullsEyeCenter)){
 						if(upperHalfSideRectangle.contains(averageBullsEyeCenter)){
 							this.lastKnownCirclePosition=LastKnownCircleLinePosition.CENTER_RECTANGLE_UPPER_HLAF;
 						}
 						if(bottomHalfSideRectangle.contains(averageBullsEyeCenter)){
 							this.lastKnownCirclePosition=LastKnownCircleLinePosition.CENTER_RECTANGLE_BOTTOM_HLAF;
 						}
 					}
 					if(redZoneLeftSideRectangle.contains(averageBullsEyeCenter)){
 						if(upperHalfSideRectangle.contains(averageBullsEyeCenter)){
 							this.lastKnownCirclePosition=LastKnownCircleLinePosition.LEFT_RECTANGLE_UPPER_HLAF;
 						}
 						if(bottomHalfSideRectangle.contains(averageBullsEyeCenter)){
 							this.lastKnownCirclePosition=LastKnownCircleLinePosition.LEFT_RECTANGLE_BOTTOM_HLAF;
 						}
 					}
 					if(redZoneRightSideRectangle.contains(averageBullsEyeCenter)){
 						if(upperHalfSideRectangle.contains(averageBullsEyeCenter)){
 							this.lastKnownCirclePosition=LastKnownCircleLinePosition.RIGHT_RECTANGLE_UPPER_HLAF;
 						}
 						if(bottomHalfSideRectangle.contains(averageBullsEyeCenter)){
 							this.lastKnownCirclePosition=LastKnownCircleLinePosition.RIGHT_RECTANGLE_BOTTOM_HLAF;
 						}
 					}
 
 					//				this.dronePleaseStayOver_Current_Bullseye(w, h);
 
 					System.out.println("LAST KNOWN POSITION ########"+this.lastKnownCirclePosition);
 				}
 			} 
 
 			//TODO no circles found, drone lost, drone go home
 			averageBullsEyeCenter.setLocation(0, 0);
 		}
 	}
 
 	public LastKnownCircleLinePosition getLastKnownCirclePosition() {
 		return lastKnownCirclePosition;
 	}
 
 	public void setLastKnownCirclePosition(
 			LastKnownCircleLinePosition lastKnownCirclePosition) {
 		this.lastKnownCirclePosition = lastKnownCirclePosition;
 	}
 
 	public CircleInformation() {
 		super();
 		averageBullsEyeCenter = new Point2D.Double();
 	}
 
 	public long getCircleFoundTimeDifference() {
 		return circleFoundTimeDifference;
 	}
 
 	public void setCircleFoundTimeDifference(long circleFoundTimeDifference) {
 		this.circleFoundTimeDifference = circleFoundTimeDifference;
 	}
 
 	public boolean isDroneLost() {
 		return (getCircleFoundTime() >= LAST_CIRCLE_FOUND_TIMEOUT);
 	}
 	
 	public boolean isDroneOutsideRectangles() {
 		return !(isDroneInGreenRectangle() || isDroneInLeftRedZoneRectangle() || isDroneInRightRedZoneRectangle());
 	}
 	
 	public boolean isDroneInGreenRectangle() {
 		return greenZoneCenterRectangle.contains(averageBullsEyeCenter);
 	}
 	
 	public boolean isDroneInLeftRedZoneRectangle() {
 		return redZoneLeftSideRectangle.contains(averageBullsEyeCenter);
 	}
 	
 	public boolean isDroneInRightRedZoneRectangle() {
 		return redZoneRightSideRectangle.contains(averageBullsEyeCenter);
 	}
 
 }
