 package projectubernahme.lifeforms;
 
 import projectubernahme.gfx.ConvertedGraphics;
 import projectubernahme.gfx.LifeformTreeGraphics;
 import projectubernahme.simulator.MainSimulator;
 
 public class Tree extends Lifeform {
 	private static ConvertedGraphics cg = new LifeformTreeGraphics();
 	
 	private double growthFactor = 0.01;
 
 	public Tree (MainSimulator sim) {
 		super(sim);
 		setCanFly(false);
 		setCanSee(false);
 		canMove = false;
 		setBiomass(500+Math.random()*500);
 		
 		x = Math.random()-0.5;
 		y = Math.random()-0.5;
 		
 		viewAngle = 2*Math.PI*Math.random();
 	}
 
 	public Tree(MainSimulator sim, double d, double e) {
 		this(sim);
 		x = d;
 		y = e;
 	}
 
 	@Override
 	public void act(int sleepTime) {
 		double t = sleepTime/1000.0;
		setBiomass(getBiomass() + getBiomass()*(Math.exp(growthFactor*t)-1)*(500*500 / Math.pow(getBiomass(), 2)));
 	}
 
 	@Override
 	public boolean canSee(Lifeform l) {
 		return false;
 	}
 
 	@Override
 	public ConvertedGraphics getConvertedGraphics() {
 		return cg;
 	}
 }
