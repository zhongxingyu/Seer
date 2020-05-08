 package at.ppmrob.autopilot;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Vector;
 
 import at.ppmrob.examples.main.LastKnownCircleLinePosition;
 import at.ppmrob.featuredetection.MyLine;
 
 public class LineInformation {
 	
 	private static final Long LAST_LINE_FOUND_TIMEOUT = 6000l;
 	
 	private long lineFoundTime;
 	private long lineFoundTimeDifference;
 	private Vector<MyLine> detectedLines;
 	private Point2D.Double averageLinesCenter;
 	
 	private LastKnownCircleLinePosition lastKnownLinePosition;
 	
 	private int heightDroneCamera = 144; //240 144
 	private int widthDroneCamera = 176; //320 176
 	private Rectangle2D.Double greenZoneCenterRectangle = new Rectangle2D.Double(heightDroneCamera*0.33f, 1, heightDroneCamera*0.33f, widthDroneCamera-1);
 	private Rectangle2D.Double redZoneRightSideRectangle = new Rectangle2D.Double(heightDroneCamera*0.66f, 1, heightDroneCamera*0.33f, widthDroneCamera-1);
 
 	private Rectangle2D.Double upperHalfSideRectangle = new Rectangle2D.Double(1, 1, heightDroneCamera-2, widthDroneCamera*0.5);
 	private Rectangle2D.Double bottomHalfSideRectangle = new Rectangle2D.Double(1, widthDroneCamera*0.5+2, heightDroneCamera-2, widthDroneCamera*0.5f-2);
 
 
 	private Rectangle2D.Double redZoneLeftSideRectangle = new Rectangle2D.Double(1, 1, heightDroneCamera*0.33f, widthDroneCamera-1);
 	
	public LineInformation() {
		detectedLines = new Vector<MyLine>();
		averageLinesCenter = new Point2D.Double();
	}
	
 	public Vector<MyLine> getDetectedLines() {
 		return detectedLines;
 	}
 	
 	public void setDetectedLines(Vector<MyLine> detectedLines) {
 		this.detectedLines = detectedLines;
 		
 		int lineCount = 0;
 		int xCoordCount = 0;
 		int yCoordCount = 0;
 		
 		synchronized (detectedLines) {
 			if(detectedLines!=null){
 				lineCount=detectedLines.size();
 				if(lineCount>0){
 					for(MyLine line_n:detectedLines){
 						xCoordCount+=line_n.point1.x();
 						xCoordCount+=line_n.point2.x();
 						yCoordCount+=line_n.point1.y();
 						yCoordCount+=line_n.point2.y();
 
 					}
 
 					averageLinesCenter.setLocation((xCoordCount/lineCount), (yCoordCount/lineCount));
 
 					if(greenZoneCenterRectangle.contains(averageLinesCenter)){
 						if(upperHalfSideRectangle.contains(averageLinesCenter)){
 							this.lastKnownLinePosition=LastKnownCircleLinePosition.CENTER_RECTANGLE_UPPER_HLAF;
 						}
 						if(bottomHalfSideRectangle.contains(averageLinesCenter)){
 							this.lastKnownLinePosition=LastKnownCircleLinePosition.CENTER_RECTANGLE_BOTTOM_HLAF;
 						}
 					}
 					if(redZoneLeftSideRectangle.contains(averageLinesCenter)){
 						if(upperHalfSideRectangle.contains(averageLinesCenter)){
 							this.lastKnownLinePosition=LastKnownCircleLinePosition.LEFT_RECTANGLE_UPPER_HLAF;
 						}
 						if(bottomHalfSideRectangle.contains(averageLinesCenter)){
 							this.lastKnownLinePosition=LastKnownCircleLinePosition.LEFT_RECTANGLE_BOTTOM_HLAF;
 						}
 					}
 					if(redZoneRightSideRectangle.contains(averageLinesCenter)){
 						if(upperHalfSideRectangle.contains(averageLinesCenter)){
 							this.lastKnownLinePosition=LastKnownCircleLinePosition.RIGHT_RECTANGLE_UPPER_HLAF;
 						}
 						if(bottomHalfSideRectangle.contains(averageLinesCenter)){
 							this.lastKnownLinePosition=LastKnownCircleLinePosition.RIGHT_RECTANGLE_BOTTOM_HLAF;
 						}
 					}
 
 					//				this.dronePleaseStayOver_Current_Bullseye(w, h);
 					return;
 				}
 			} 
 
 			//TODO no circles found, drone lost, drone go home
 			averageLinesCenter.setLocation(0, 0);
 
 		}
 	}
 
 	public long getLineFoundTime() {
 		return lineFoundTime;
 	}
 
 	public void setLineFoundTime(long lineFoundTime) {
 		this.lineFoundTime = lineFoundTime;
 	}
 
 	public long getLineFoundTimeDifference() {
 		return lineFoundTimeDifference;
 	}
 
 	public void setLineFoundTimeDifference(long lineFoundTimeDifference) {
 		this.lineFoundTimeDifference = lineFoundTimeDifference;
 	}
 	
 
 	public boolean isDroneLost() {
 		return (getLineFoundTime() >= LAST_LINE_FOUND_TIMEOUT);
 	}
 
 	public boolean isDroneInGreenRectangle() {
 		return greenZoneCenterRectangle.contains(averageLinesCenter);
 	}
 	
 	public boolean isDroneInLeftRedZoneRectangle() {
 		return redZoneLeftSideRectangle.contains(averageLinesCenter);
 	}
 	
 	public boolean isDroneInRightRedZoneRectangle() {
 		return redZoneRightSideRectangle.contains(averageLinesCenter);
 	}
 	
 	public boolean isDroneOutsideRectangles() {
 		return !(isDroneInGreenRectangle() || isDroneInLeftRedZoneRectangle() || isDroneInRightRedZoneRectangle());
 	}
 	
 }
