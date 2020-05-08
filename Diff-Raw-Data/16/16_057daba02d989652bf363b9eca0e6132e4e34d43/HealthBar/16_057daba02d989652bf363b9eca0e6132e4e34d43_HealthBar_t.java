 package pkids;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import jgame.Context;
 import jgame.GObject;
 import jgame.listener.TimerListener;
 
 public class HealthBar extends GObject {
	private static final int BORDER = 3;
 	private double healthPercentage = 1;
 
 	public HealthBar() {
 		this.setSize(90, 15);
 		TimerListener timer = new TimerListener(10) {
 			@Override
 			public void invoke(GObject target, Context context) {
 				healthPercentage -= 0.01;
 			}
 		};
 		addListener(timer);
 	}
 
 	@Override
 	public void paint(Graphics2D g) {
 
 		super.paint(g);
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, getIntWidth(), getIntHeight());
 
 		float hbHue = (float) Math.pow(healthPercentage, 2) / 3f;
 		g.setColor(Color.getHSBColor(hbHue, 1f, 1f));
		g.fillRect(BORDER, BORDER, (int) ((getWidth() * healthPercentage) - BORDER * 2),
				getIntHeight() - BORDER * 2);
 	}
 
 	public double getHealthPercentage() {
 		return healthPercentage;
 	}
 
 	public void setHealthPercentage(double healthPercentage) {
 		this.healthPercentage = healthPercentage;
 	}
 
 }
