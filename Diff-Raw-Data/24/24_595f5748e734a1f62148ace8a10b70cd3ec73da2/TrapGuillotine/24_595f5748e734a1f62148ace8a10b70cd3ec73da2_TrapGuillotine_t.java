 package mta13438;
 
 public class TrapGuillotine extends Trap{
 	
 	private boolean trapOn;
 	private final int downtime = 5; // x "delta" downtime between the trap being on and off
 	private final int killtime = 2;
 	private int counter;
 	
 
 	public TrapGuillotine(){
 		super();
 		trapOn = true;
		setLoopSound(new Sound(SOUNDS.GUILLOTINE_TRAP, new Point(0,0,0), true,true));
 	}
 	public TrapGuillotine(Point point,float dx,float dy,float dz,MATERIALS material) {
 		super(point, dx,dy,dz,material);
 		trapOn = false;
		setLoopSound(new Sound(SOUNDS.GUILLOTINE_TRAP, new Point(0,0,0), true,true));
 	}
 	@Override
 	public void collision(Player player, Level level, int currentRoom){
 		System.out.println("Collision with guillotine trap");
 		if(trapOn == true){
 			player.kill(level);
 		}
 	}
 	public void update(int delta){
 		if(trapOn == false){
 			counter += delta;
 			if(counter>=downtime){
 				trapOn = true;
 				counter = 0;
 				//Play trap sound
 			}
 		}
 		else if(trapOn ==false){
 			counter += delta;
 			if(counter >= killtime){
 				trapOn = false;
 				counter = 0;
 			}
 		}
 		
 		//trapSound.loop();
 		System.out.println("delta");
 		
 	}
 }
