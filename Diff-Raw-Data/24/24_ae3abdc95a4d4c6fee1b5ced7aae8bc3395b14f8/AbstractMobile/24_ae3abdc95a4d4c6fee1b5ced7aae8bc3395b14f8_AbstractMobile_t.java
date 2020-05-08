 package mobiles;
 
 import event_handler.InteractionEvent;
 import map.Location;
 import map.Map;
 import portal.AbstractPortal;
 
 public abstract class AbstractMobile extends AbstractPortal {
 	String icon;
 	Map mMap;
 	protected int health = 20;
 	int damage = 10;
 	protected int fat = 20;
 	protected int environment=20;
 	protected int speed = 2;
 	//int FramesPerUpdate = fps/speed
 	int framesPerUpdate = 60/speed;
 	int framesSinceUpdate = 60;
 	AbstractPortal[][] drawnMap;
 	protected boolean shouldUpdate = false;
 	/**
 	 * Basic implementation for all moving things.  Not just living ones!
 	 * @param x
 	 * @param y
 	 */
 	public AbstractMobile(int x, int y, Map map) {
 		super();
 		this.x = x;
 		this.y = y;
 		mMap = map;
 	}
 	@Override
 	public boolean acceptInteraction(InteractionEvent e) {
 		System.out.println("GettingFingeredAbstractly");
 
 		switch(e.getType()) {
 		case(InteractionEvent.MOUSE):
 			break;
 		case(InteractionEvent.EAT):
 			System.out.println("Just ate Abstractly.");
 
 		int damage = e.getResultValue();
 		health -= damage;
 		fat -=damage;
 		InteractionEvent ret = new InteractionEvent(InteractionEvent.EAT_RESULT,this);
 		e.setResultValue(damage);
 		e.getSender().acceptInteraction(ret);
 		break;
 		case(InteractionEvent.EAT_RESULT):
 			System.out.println("Just ate Abstractly.");
 		this.health+=e.getResultValue();
 		this.fat+=e.getResultValue();		
 		break;
 		}
 		return true;
 	};
 	/**
 	 * Update the to the new location, stract implementions will handle
 	 * how to determine and validity etc... of new locs
 	 * @param newX
 	 * @param newY
 	 */
 	public boolean move(int newX, int newY) {
 		if(canMove(newX, newY)) {
 			this.x = newX;
 			this.y = newY;
 			shouldUpdate=false;
 			framesSinceUpdate=0;
 
 			return true;
 		} else {
 			return finger(x,y);
 		}
 	}
 	public boolean canMove(int ex, int yy) {
 		return mMap.canPlace(ex, yy, drawnMap);
 	}
 	public Location[] getAdj() {
 		return mMap.getAdjacent(mMap.getLocation(x, y));
 	}
 	/**
 	 * Send out your interaction
 	 * @return
 	 */
 	public boolean finger(int ex, int yh) {
	//	System.out.println("gettin fingered");
 		AbstractPortal p = drawnMap[yh][ex];
 		InteractionEvent e = new InteractionEvent(InteractionEvent.EAT_RESULT,this);
 		e.setResultValue(damage);
 		p.acceptInteraction(e);
 		return false;
 	}
 	/**
 	 * Terminte mobile.
 	 * @return
 	 */
 	public boolean die() {
		System.out.println("Creature Dead!");
 		return false;
 	}
 	public String getIcon() {
 		return this.icon;
 	}
 	public abstract boolean doMaMove();
 
 	public boolean shouldUpdate() {
 		if(shouldUpdate) {
 
 		} else if(framesSinceUpdate>framesPerUpdate) {
 			shouldUpdate=true;
 			framesSinceUpdate=0;
 		} else {
 			shouldUpdate = false;
 			framesSinceUpdate++;
 		}
 		return shouldUpdate;
 	}	
 	public void setDrawnMap(AbstractPortal[][] mmaapp) {
 		drawnMap=mmaapp;
 	}
 }
