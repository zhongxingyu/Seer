 package mta13438;
 
 public class Trap extends Obs {
 	
 	private Sound trapKillSound = new Sound(TRAPKILLSOUND,getPos(),10,10);
 	private Sound trapSound = new Sound(TRAPSOUND,getPos(),10,10);
 	
 	public void update(){
 		trapSound.loop();
 	}
 	public void collide(){
 		trapKillSound.play();
 		Player.setHealth(0);
 	}
 	
 	@Override
 	public String toString() {
 		return "Trap [pos=" + getPos() + ", dx=" + getDx() + ", dz=" + getDz()
				+ ", lenght=" + getDy() + ", sabins=" + getSabins() + "]";
 	}
 }
