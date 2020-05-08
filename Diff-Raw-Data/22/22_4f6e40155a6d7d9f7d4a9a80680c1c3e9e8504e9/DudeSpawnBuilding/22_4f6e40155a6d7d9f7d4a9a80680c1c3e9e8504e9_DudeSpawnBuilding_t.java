 package state;
 
import java.util.Random;

 public class DudeSpawnBuilding extends Structure {
 	public DudeSpawnBuilding(int x, int y) {
 		super(x, y, 1, 1, "Assets/EnvironmentObjects/Stalagmite.png");
 	}
 
 	int delay;
 	@Override
 	public void update() {
 		Tile t = getWorld().getTile(getX(), getY());
 		if(getWorld().isDudeSpawningEnabled() && t.getDude() == null) {
 			if(delay <= 0) {
				if(new Random().nextBoolean())
					getWorld().addDude(new Dude(getWorld(), getX(), getY(), 1, 1, "Assets/Characters/Man.png"));
				else
					getWorld().addDude(new Octodude(getWorld(), getX(), getY(), 1, 1, "Assets/Characters/Man.png"));
 				delay = 15;
 			} else {
 				delay--;
 			}
 		}
 	}
 }
