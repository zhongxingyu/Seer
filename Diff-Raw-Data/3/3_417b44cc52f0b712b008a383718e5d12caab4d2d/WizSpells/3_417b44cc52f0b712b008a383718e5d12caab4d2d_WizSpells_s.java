 package abilities;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import skeleton.*;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Paint;
 import java.awt.PaintContext;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.ColorModel;
 
 import javax.swing.ImageIcon;
 
 import characters.Wizard;
 
 public class WizSpells extends Spells{
 	
 	public WizSpells(){
 	}
 	
 	public class FrostBolt extends WizSpells{
 		public FrostBolt(int x, int y) {
 			super.buttonName = "FrostBolt";
 			super.snare_effect = EffectsDatabase.snareTypes.FROSTBOLT;
 			super.setDamage(10);
			ImageIcon ii = new ImageIcon(this.getClass().getResource(
 					"frostboltimgUP.png"));
 			super.image = ii.getImage();
 			super.visible = true;
 			super.x = x;
 			super.y = y;
 			height = image.getHeight(null);
 			width = image.getWidth(null);
 		}
 		
 		public FrostBolt(int x, int y, String any){
 			super.snare_effect = EffectsDatabase.snareTypes.FROSTBOLT;
 			super.setDamage(20);
 			ClassLoader cl = Thread.currentThread().getContextClassLoader();
 			ImageIcon ii = new ImageIcon(cl.getResource(
 					"frostboltimgDOWN.png"));
 			super.image = ii.getImage();
 			super.visible = true;
 			super.x = x;
 			super.y = y;
 			height = image.getHeight(null);
 			width = image.getWidth(null);
 		}
 		public void moveDown() {
 			y -= MISSILE_SPEED;
 			if (y > Skeleton.height)
 				visible = false;
 		}
 		public void moveUp() {
 			y += MISSILE_SPEED;
 			if (y > Skeleton.height)
 				visible = false;
 		}	
 	}
 }
