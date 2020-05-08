 package core.playfield.hud;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import com.golden.gamedev.object.GameFont;
 
 public class BarWidget extends HUDWidget {
 
 	String title;
 	double myValue;
 	double maxHP;
 	BarProxy dp;
 
 	public BarWidget(String title, BarProxy dp) {
 		super(150, 15);
 		this.title = title;
 		this.dp = dp;
 	}
 
 	@Override
 	public void update(long t) {
 		this.myValue = dp.get();
		this.maxHP = dp.getInit();
 	}
 
 	@Override
 	public void render(Graphics2D g, HUDGroup h) {
 		h.getFont().drawString(g, title + ":", GameFont.LEFT, xPos, yPos, 100);
 		g.draw(new Rectangle(xPos + 22, yPos + 3, 101, 10));
 		g.setColor(Color.red);
 		g.fill(new Rectangle(xPos + 23, yPos + 4, (int) (myValue / maxHP * 100), 9));
 	}
 
 }
