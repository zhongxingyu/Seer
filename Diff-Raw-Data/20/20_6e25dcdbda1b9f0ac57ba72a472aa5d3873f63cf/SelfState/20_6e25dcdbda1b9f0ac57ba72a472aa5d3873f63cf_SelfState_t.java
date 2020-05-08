 package net.adbenson.robocode.botstate;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 
import net.adbenson.utility.Utility;

 import robocode.AdvancedRobot;
 
 public class SelfState extends BotState<SelfState> {
 
 	public final double gunHeading;
 	public final double gunHeat;
 
 	protected SelfState(SelfState a, SelfState b, boolean add) {
 		super(a, b, add);
 		
		this.gunHeading = add? (a.gunHeading + b.gunHeading) : 
			Utility.angleDifference(a.gunHeading, b.gunHeading);
		
 		this.gunHeat = a.gunHeat + (add? b.gunHeat : -b.gunHeat);
 	}
 	
 	public SelfState(AdvancedRobot self) {
 		this(self, null);
 	}
 
 	public SelfState(AdvancedRobot self, SelfState previous) {
 		super(self, previous);
 		this.gunHeading = self.getGunHeading();
 		this.gunHeat = self.getGunHeat();
 	}
 
 	@Override
 	public SelfState diff(SelfState b) {
 		return new SelfState(this, b, false);
 	}
 	
 	@Override
 	public SelfState sum(SelfState b) {
 		return new SelfState(b, this, true);
 	}
 
 	public void draw(Graphics2D g) {
 		g.setColor(velocity > 0? Color.green : Color.red);
 		g.setStroke(new BasicStroke(5));
 
 		position.drawTo(g, heading, velocity * 5);
 		
 //		g.drawLine(position.intX() - 30, position.intY() - 30, position.intX() - 30, )
 	}
 
 }
