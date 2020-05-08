 package game.things;
 
 import game.*;
 
 import java.util.*;
 import util.*;
 
 public class Stairs extends AbstractGameThing {
 	private int up, down;
 	private final String renderer;
 	private final Direction allow;
 
 	public Stairs(GameWorld w, String r, int u, int d, Direction a){
 		super(w);
 		up = u;
 		down = d;
 		renderer = r;
 		allow = a;
 		update();
 	}
 
 	public String renderer(){
 		return renderer;
 	}
 
 	public List<String> interactions(){
 		List<String> out = new LinkedList<String>();
 		if(up > 0)
 			out.add("go up");
 		if(down > 0)
 			out.add("go down");
 		out.addAll(super.interactions());
 		return out;
 	}
 
 	public void walkAndGo(final boolean goup, final Player p){
 		Location l = location();
 		if(l instanceof Level.Location)
			p.moveTo(((Level.Location)l).rotate(allow).next(), 0, new Runnable(){
 				public void run(){
 					Location pl = p.location();
 					if(pl instanceof Level.Location){
 						if(goup)
 							((Level.Location)pl).above(up).put(p);
 						else
 							((Level.Location)pl).below(down).put(p);
 					}
 				}
 			});
 	}
 
 	public void interact(String name, Player who){
 		if(name.equals("go up") && up > 0)
 			walkAndGo(true, who);
 		else if(name.equals("go down") && down > 0)
 			walkAndGo(false, who);
 		else
 			super.interact(name, who);
 	}
 
 	public String name(){
 		return "Stairs";
 	}
 
 	public boolean canWalkInto(Direction d, Character c){
 		return false;
 	}
 
 	public int up(){ return up; }
 	public int up(int s){ return up = s; }
 	public int down(){ return down; }
 	public int down(int s){ return down = s; }
 
 	public int renderLevel(){
 		return ui.isometric.abstractions.IsoSquare.WALL;
 	}
 }
