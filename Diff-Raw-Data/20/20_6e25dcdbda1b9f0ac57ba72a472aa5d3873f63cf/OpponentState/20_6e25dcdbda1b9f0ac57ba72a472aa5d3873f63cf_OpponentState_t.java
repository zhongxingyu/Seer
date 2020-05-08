 package net.adbenson.robocode.botstate;
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import net.adbenson.utility.Utility;
 import net.adbenson.utility.Vector;
 import robocode.AdvancedRobot;
 import robocode.ScannedRobotEvent;
 
 public class OpponentState extends BotState<OpponentState> {
 	
 	public final double bearing;
 	public final double absoluteBearing;
 	public final double distance;
 	
 	public OpponentState(OpponentState a, OpponentState b, boolean add) {
 		super(a, b, add);
 		
		this.bearing = add? (a.bearing + b.bearing) : 
			Utility.angleDifference(a.bearing, b.bearing);
		
		this.absoluteBearing = add? (a.absoluteBearing + b.absoluteBearing) : 
			Utility.angleDifference(a.absoluteBearing, b.absoluteBearing);
		
 		this.distance = a.distance + (add? b.distance : -b.distance);
 	}
 
 	public OpponentState(ScannedRobotEvent event, AdvancedRobot self) {
 		this(event, null, self);
 	}
 
 	public OpponentState(ScannedRobotEvent current, OpponentState previous, AdvancedRobot self) {		
 		super(
 				current.getName(),
 				current.getEnergy(),
 				current.getHeadingRadians(),
 				current.getVelocity(),
 				calculatePosition(current, self), 
 				previous
 		);
 		
 		this.bearing = current.getBearingRadians();
 		this.absoluteBearing = absoluteBearing(self, current);
 		this.distance = current.getDistance();
 	}	
 
 	private static Vector calculatePosition(ScannedRobotEvent current, AdvancedRobot self) {
 		double absoluteBearing = absoluteBearing(self, current);
 		
 		Vector relative = Vector.getVectorFromAngle(absoluteBearing, current.getDistance());
 
 		return relative.add(new Vector(self.getX(), self.getY()));
 	}
 
 	@Override
 	public OpponentState diff(OpponentState b) {
 		return new OpponentState(this, b, false);
 	}
 	
 	@Override
 	public OpponentState sum(OpponentState b) {
 		return new OpponentState(this, b, true);
 	}
 	
 	private static double absoluteBearing(AdvancedRobot self, ScannedRobotEvent current) {
 		return self.getHeadingRadians() + current.getBearingRadians();
 	}
 
 	public void draw(Graphics2D g) {
 		g.setColor(Utility.setAlpha(Color.orange, 0.6));
 		Utility.drawCrosshairs(g, position, 20, 35);
 		
 		g.setColor(Utility.setAlpha(Color.pink, 0.6));
 		
 		position.drawTo(g, Utility.oppositeAngle(absoluteBearing), distance / 2);
 	}
 
 }
