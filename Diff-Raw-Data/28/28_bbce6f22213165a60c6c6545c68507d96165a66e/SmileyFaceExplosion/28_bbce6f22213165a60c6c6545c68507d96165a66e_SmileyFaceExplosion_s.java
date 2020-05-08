 package ohayon.android.fireworks.explosions;
 
import android.graphics.Color;
import android.graphics.Point;
 import java.util.ArrayList;
import java.util.Random;
 
 import ohayon.android.fireworks.Firework;
 
 public class SmileyFaceExplosion implements Explosion {
 
 	@Override
 	public ArrayList<Firework> explode(Firework f) {
 		ArrayList<Firework> list = new ArrayList<Firework>();
 		Point p = f.getPosition();

 		// circle
		Random generator = new Random();
 		for (int i = 0; i < 65; i++) {
			list.add(new Firework(p.x, p.y, f.getV(), (double) generator
					.nextInt(360), Color.YELLOW, generator.nextDouble() + 3, null, null));
 		}
 
 		// eyes
 		list.add(new Firework(p.x, p.y, f.getV()-30, f.getTheta() - 25,
				Color.BLUE, f.getTtl(), null, null));
 		list.add(new Firework(p.x, p.y, f.getV()-30, f.getTheta() + 25,
				Color.BLUE, generator.nextDouble() + 3, null, null));
 
 		// mouth
 		for (int i = 0; i < 65; i++) {
			list.add(new Firework(p.x, p.y, f.getV()-30, -(double) generator
					.nextInt(180), Color.RED, generator.nextDouble() + 3, null, null));
 		}
 		
 		return list;
 	}
 
 }
