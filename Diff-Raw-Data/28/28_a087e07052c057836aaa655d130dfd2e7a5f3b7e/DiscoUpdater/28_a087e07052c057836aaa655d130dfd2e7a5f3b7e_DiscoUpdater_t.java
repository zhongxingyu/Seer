 package gibstick.bukkit.discosheep;
 
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class DiscoUpdater extends BukkitRunnable {
 
 	int frequency = 0, duration = 0;
 	private DiscoSheep parent;
 
 	public DiscoUpdater(DiscoSheep parent) {
 		this.parent = parent;
 	}
 
 	public void stop() {
 		this.duration = 0;
 		parent.cleanUp();
 	}
	
 	public void start(int duration, int frequency) {
 		this.frequency = frequency;
 		this.duration = duration;
 		parent.scheduleUpdate();
 	}
 
 	@Override
 	public void run() {
 		if (duration > 0) {
 			parent.randomizeSheepColours();
 			parent.playSounds();
 			duration -= frequency;
 			parent.scheduleUpdate();
 		} else {
 			this.stop();
 		}
 	}
 }
