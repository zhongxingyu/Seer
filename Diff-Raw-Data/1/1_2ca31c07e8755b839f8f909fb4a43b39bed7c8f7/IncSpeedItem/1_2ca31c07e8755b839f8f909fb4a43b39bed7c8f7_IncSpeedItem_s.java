 package se.chalmers.kangaroo.model.item;
 
 import se.chalmers.kangaroo.model.Item;
 import se.chalmers.kangaroo.model.Kangaroo;
 import se.chalmers.kangaroo.model.Position;
 
 public class IncSpeedItem implements Item, Runnable {
 	
 	private Position pos;
 	
 	private Thread t;
 	
 	private Kangaroo k;
 	
 	private int id;
 	
 	public IncSpeedItem(int id, int x, int y) {
 		this.id = id;
 		this.pos = new Position(x,y);
 	}
 	
 	@Override
 	public void onPickup(Kangaroo k) {
 		this.k = k;
 	}
 
 	@Override
 	public void onDrop(Kangaroo k) {
 		;
 		
 	}
 
 	@Override
 	public void onUse(Kangaroo k) {
 		k.setMaxSpeed(15f);
 		t.start();
 		
 	}
 
 	@Override
 	public Position getPosition() {
 		return pos;
 	}
 
 	@Override
 	public void run() {
 		try {
 		t.sleep(7000);
 		} catch(InterruptedException e) {}
 		k.setMaxSpeed(10f);
 		
 	}
 
 	@Override
 	public int getId() {
 
 		return id;
 	}
 	
 
 }
