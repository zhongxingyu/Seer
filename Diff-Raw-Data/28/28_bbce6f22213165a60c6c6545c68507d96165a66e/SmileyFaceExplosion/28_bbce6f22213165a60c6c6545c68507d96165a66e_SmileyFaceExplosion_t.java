 package ohayon.android.fireworks.explosions;
 
 import java.util.ArrayList;
 
 import ohayon.android.fireworks.Firework;
import android.graphics.Color;
import android.graphics.Point;
 
 public class SmileyFaceExplosion implements Explosion {
 
 	@Override
 	public ArrayList<Firework> explode(Firework f) {
 		ArrayList<Firework> list = new ArrayList<Firework>();
 		Point p = f.getPosition();
		double ttl = random.nextDouble() + 3;
 		// circle
 		for (int i = 0; i < 65; i++) {
			list.add(new Firework(p.x, p.y, f.getV(), (double) random
					.nextInt(360), Color.YELLOW, ttl, null, null));
 		}
 
 		// eyes
 		list.add(new Firework(p.x, p.y, f.getV()-30, f.getTheta() - 25,
				Color.BLUE, ttl, null, null));
 		list.add(new Firework(p.x, p.y, f.getV()-30, f.getTheta() + 25,
				Color.BLUE, ttl, null, null));
 
 		// mouth
 		for (int i = 0; i < 65; i++) {
			list.add(new Firework(p.x, p.y, f.getV()-30, -(double) random
					.nextInt(180), Color.RED, ttl, null, null));
 		}
 		
 		return list;
 	}
 
 }
