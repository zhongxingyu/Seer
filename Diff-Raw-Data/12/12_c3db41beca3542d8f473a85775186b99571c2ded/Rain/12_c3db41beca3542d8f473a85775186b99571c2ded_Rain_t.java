 package bitcity;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 public class Rain extends WorldObject {
 	
 	private boolean raining = false;
 	private int dropMax;
 	private int width;
 	private int height;
 	private int time;
 	private int elapsed;
 	private World world;
	private ArrayList<Drop> drops;
 
 	final private static double THUNDER_PROBABILITY = 1/3.; /* 1 in 3 raining frames. */
	final private static float DROP_MAX_SIZE = 2; /* 2x tileWidth size of the drop */
	final private static int RAIN_MAX_TIME = 20;
 	final private static int RAIN_MIN_TIME = 5;
	
 
 	public Rain(World world){
 		this.world = world;
 		this.width = this.world.columnCount();
 		this.height = this.world.rowCount();
 		this.dropMax = 33;
 		this.drops = new ArrayList<Drop>();
 	}
 	
 	public void updateRain(float tileWidht, float tileHeight){
 		
 		int meio = (int) (this.time / 2);
 		
 		float rain_force;
 		
 		if (this.elapsed <= meio)
 			rain_force = (float) (this.elapsed) / meio;
 		else
 			rain_force = 1 - (float) (this.elapsed - meio ) / (this.time - meio);
 		
 		
 		if (rain_force > 0.8 && Math.random() < Rain.THUNDER_PROBABILITY) {
 			if (!Sound.THUNDER.isRunning()) {
 				Sound.THUNDER.play();
 			}
 		}
 		
 		int nDrops = (int) (dropMax * rain_force);
 		
 		int width = (int) (tileWidht * this.width);
 		int heigth = (int) (tileHeight * this.height);
 		
 		for (int i = 0; i < nDrops; i++) {
 			int x = (int) Math.abs(Application.random.nextInt() % width);
 			int y = (int) Math.abs(Application.random.nextInt() % heigth);
 			this.drops.add(new Drop(x, y, this.elapsed));
 		}
 		
 		this.world.dropRandomLeavesFromTrees();
 
 		if (this.elapsed == this.time){
 			Sound.RAIN.stop();
 			this.raining = false;
 			this.drops.clear();
 		}
 	}
 	
 	public void run() {
 		int frameRate;
 		
 		try {
 			while (true) {
 				try {
 					synchronized (this) {
 						this.wait();
 					}
 					System.out.println("Starting to rain, slow down.");
 					/* Actually, slowing down the world seems to cause a weird visual effect.
 					 * Just take the message above as a warning.
 					 */
 					
 					frameRate = WorldMap.FPS;
 					this.elapsed = 0;
 					int gap = RAIN_MAX_TIME - Rain.RAIN_MIN_TIME + 1;
 					this.time = Rain.RAIN_MIN_TIME + (Math.abs(Application.random.nextInt()) % gap);
 					this.time *= frameRate;
 					this.raining = true;
 					Sound.RAIN.loop();
 					
 				} catch (InterruptedException e) {
 					System.out.println("Rain exception: " + e.getMessage());
 					break;
 				}
 			}
 		} finally {
 			System.out.println("Rain is leaving this world..");
 		}
 	}
 
 	public boolean isRaining() {
 		return raining;
 	}
 
 	public void setRaining(boolean raining) {
 		this.raining = raining;
 	}
 	
 	@Override
 	void draw(Graphics2D ctx, float tileWidth, float tileHeight) {
 
 		if (this.isRaining()) {
 	
 			this.elapsed++;
 		    this.updateRain(tileWidth, tileHeight);	
 		    ctx.setColor(Color.CYAN);
 			for (int i = 0; i < drops.size(); i++){
 				float drop_force = drops.get(i).getForce(this.elapsed);
 				if (drop_force == 0) {
 					drops.remove(i);
 				} else {
					int size = (int) (Rain.DROP_MAX_SIZE * tileWidth * drop_force);
 					ctx.fillOval((int) (drops.get(i).getX()), (int) (drops.get(i).getY()), size, size);
 				}
 			}
 		}
 	}
 }
