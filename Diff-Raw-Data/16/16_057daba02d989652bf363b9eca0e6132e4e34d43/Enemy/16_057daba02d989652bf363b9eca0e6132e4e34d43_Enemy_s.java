 package pkids.enemies;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Polygon;
 
 import jgame.GObject;
 import jgame.GSprite;
 import jgame.controller.PolygonController;
 import jgame.listener.BoundaryRemovalListener;
 import pkids.HealthBar;
 
 public abstract class Enemy extends GSprite { // can not instantiate an abstract
 												// class
 	private double maxHealth;
 	private double currentHealth;
 	private HealthBar hb = new HealthBar();
 	
 	public Enemy(Image image, double maxHlth) {
 		super(image);
 		maxHealth = maxHlth;
 		currentHealth =maxHealth;
		
 		addAtCenter(hb);
 		hb.setY(this.getHeight()-hb.getHeight()/2);
 		hb.setHealthPercentage(1);
 		
 		setScale(0.75);
 		int[] x = new int[] { 102, 102, 102, 103, 104, 104, 104, 104, 109, 123,
 				146, 171, 197, 222, 248, 274, 300, 325, 351, 377, 401, 415,
 				418, 420, 420, 422, 434, 455, 480, 505, 526, 535, 537, 538,
 				538, 537, 553, 575, 600, 623, 646, 668, 689, 708, 722, 731,
 				737, 735, 732, 725, 713, 696, 677, 656, 634, 609, 584, 558,
 				533, 509, 492, 482, 475, 469, 464, 458, 452, 445, 433, 411,
 				386, 361, 336, 310, 285, 260, 235, 210, 184, 158, 133, 112,
 				110, 109, 109, 110, 111, 112, 112, 111, 111, 110, 110, 111,
 				111, 113 };
 		int[] y = new int[] { 6, 32, 58, 83, 108, 134, 160, 186, 211, 233, 243,
 				244, 244, 243, 243, 243, 243, 242, 243, 243, 235, 214, 189,
 				164, 138, 113, 91, 77, 71, 75, 89, 113, 138, 163, 189, 214,
 				234, 246, 253, 264, 275, 287, 301, 318, 339, 363, 388, 414,
 				439, 464, 486, 505, 523, 538, 550, 556, 558, 557, 553, 544,
 				524, 500, 475, 450, 425, 400, 375, 350, 328, 315, 309, 307,
 				306, 306, 305, 306, 307, 308, 308, 308, 310, 325, 351, 376,
 				403, 428, 453, 478, 504, 530, 556, 581, 607, 632, 658, 683 };
 		Polygon p = new Polygon(x, y, 96);
 		PolygonController pc = new PolygonController(p);
 
 		pc.goToStart(this);
 		pc.setRotateToFollow(false);
 
 		double slowness = getSlowness();
 		pc.setMaxSpeed(slowness * 3);
 
 		addController(pc);
 	
 		BoundaryRemovalListener brl = new BoundaryRemovalListener();
 		addListener(brl);
 		
 		
 	}
     
 	
 	
 	/**
 	 * Gets the slowness (the time spent between waypoints) of this enemy.
 	 * 
 	 * @return the slowness
 	 */
 	public abstract double getSlowness();
 	@Override
 	public void preparePaint(Graphics2D g) {
 		super.preparePaint(g);
 		GObject.antialias(g);
 		goodImageTransforms(g);
 	}
 	
 	public double getCurrentHealth() {
 		return currentHealth;
 	}
 	
 	public void setCurrentHealth(double currentHealth) {
 		this.currentHealth = currentHealth;
 		hb.setHealthPercentage(this.currentHealth/maxHealth);
 	}
 
 }
