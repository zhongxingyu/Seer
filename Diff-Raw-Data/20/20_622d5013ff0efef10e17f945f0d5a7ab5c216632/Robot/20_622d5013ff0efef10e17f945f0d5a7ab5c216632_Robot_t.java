 package raisa.domain;
 
 import java.awt.geom.Point2D.Float;
 
 public class Robot {
 	public final static float ROBOT_WIDTH = 10f;
 	public final static float ROBOT_LENGTH = 20f;
 	public final static float WHEEL_DIAMETER = 3.6f;
 	public final static float TICK_RADIANS = (float) Math.PI / 8.0f;
 	
 	private long timestampMillis;
 	private float heading;
 	private Float positionLeftTrack;
 	private Float positionRightTrack;
 	private float speedLeftTrack;
 	private float speedRightTrack;
 
 	public Robot() {
 		this((float)Math.PI, new Float());		
 	}
 	
 	public Robot(float heading, Float position) {
 		this.heading = heading;
 		float tmpX = position.x - ROBOT_WIDTH/2.0f;
 		float tmpY = position.y;
 		this.positionLeftTrack = new Float(
 				(float)Math.cos(heading) * tmpX - (float)Math.sin(heading) * tmpY,
 				(float)Math.sin(heading) * tmpX + (float)Math.cos(heading) * tmpY);
 		tmpX = position.x + ROBOT_WIDTH/2.0f;
 		this.positionRightTrack = new Float(
 				(float)Math.cos(heading) * tmpX - (float)Math.sin(heading) * tmpY,
 				(float)Math.sin(heading) * tmpX + (float)Math.cos(heading) * tmpY);
 	}
 	
 	public Float getPosition() {
 		return new Float(
 				(positionLeftTrack.x + positionRightTrack.x) / 2.0f,
 				(positionLeftTrack.y + positionRightTrack.y) / 2.0f);
 	}
 
 	public Float getPositionLeftTrack() {
 		return new Float(positionLeftTrack.x, positionLeftTrack.y);
 	}	
 
 	public Float getPositionRightTrack() {
 		return new Float(positionRightTrack.x, positionRightTrack.y);
 	}	
 	
 	public float getSpeedLeftTrack() {
 		return speedLeftTrack;
 	}	
 	
 	public float getSpeedRightTrack() {
 		return speedRightTrack;
 	}	
 	
 	public float getHeading() {
 		return heading;
 	}
 
 	public long getTimestampMillis() {
 		return timestampMillis;
 	}
 
 	public void setSpeedLeftTrack(float speed) {
 		speedLeftTrack = speed;
 	}	
 	
 	public void setSpeedRightTrack(float speed) {
 		speedRightTrack = speed;
 	}		
 	
 	public void setHeading(float heading) {
 		this.heading = heading;
 	}
 	
 	public void setTimestampMillis(long timestamp) {
 		timestampMillis = timestamp;
 	}		
 
 	public Robot moveRobot(Sample sample) {
 		Robot r = new Robot();		
		float leftTrackTrip = (WHEEL_DIAMETER * sample.getLeftTrackTicks() * TICK_RADIANS) / 2.0f;
		float rightTrackTrip = (WHEEL_DIAMETER * sample.getRightTrackTicks() * TICK_RADIANS) / 2.0f;
 		
 		float h = sample.getCompassDirection();
 		r.positionLeftTrack = new Float(
				this.positionLeftTrack.x + leftTrackTrip * (float)Math.sin(h),
				this.positionLeftTrack.y - leftTrackTrip * (float)Math.cos(h));
 				
 		r.positionRightTrack = new Float(
				this.positionRightTrack.x + rightTrackTrip * (float)Math.sin(h),
				this.positionRightTrack.y - rightTrackTrip * (float)Math.cos(h));
 		r.heading = h;
 		r.setTimestampMillis(sample.getTimestampMillis());
 		return r;
 	}
 	
 	public String toString() {
 		return 				
 			"position (x=" + getPosition().x + ",y=" + getPosition().y + "), " +
 			"lefttrack (x=" + positionLeftTrack.x + ",y=" + positionLeftTrack.y + "), " +
 			"righttrack (x=" + positionRightTrack.x + ",y=" + positionRightTrack.y + ")";
 	}
 	
 	
 }
