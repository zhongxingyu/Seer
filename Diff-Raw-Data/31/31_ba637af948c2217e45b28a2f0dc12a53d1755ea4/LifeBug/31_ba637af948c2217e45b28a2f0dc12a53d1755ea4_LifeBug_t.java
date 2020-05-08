 import java.util.*;
 import java.math.*;
 
 const double probBreed = 0.14285714285;
 const double probDeath = 0.2;
 
 public class LifeBug extends Bug {
 	public LifeBug() {
 		super();
 		setDirection( Location.NORTH );
 	}
 
 	public LifeBug(Color bugColor) {
 		super(bugColor);
 		setDirection( Location.NORTH );
 	}
 
 	void act() {
		if(turn && !roll()) move();
 	}
 
	boolean turn() {
 		Random r = new Random();
 		ArrayList<Location> poss_dirs = turn_poss();
		if(!poss_dirs.empty()) {
			dir_ind = r.nextInt( poss_dirs.size() );
			setDirection( poss_dirs.get( dir_ind ) );
			return true;
		}
		return false;
 	}
 
 	ArrayList<Location> turn_poss() {
 		ArrayList<Location> empty_locs = Grid.getEmptyAdjacentLocations( getLocation() );
 		ArrayList<Location> ret = new ArrayList<Location>();
 		
 		for(int i = 0; i < empty_locs.size(); i++) {
 			int dir = Location.getDirectionToward( empty_locs[i] );
 			switch (dir) {
 			case Location.NORTH:
 			case Location.SOUTH:
 			case Location.EAST:
 			case Location.WEST:
				if((getDirection() == NORTH && dir == SOUTH) ||
				   (getDirection() == EAST && dir == WEST) ||
				   (getDirection() == SOUTH && dir == NORTH) ||
				   (getDirection() == WEST && dir == EAST))
					break;
 				ret.add( dir );
 				break;
 			default:
 				break;
 			}
 		}
 		return ret;
 	}
 
 	void move() {
 		moveTo( getLocation().getAdjacentLocation( getDirection() ) );
 	}
 
 	bool roll() {
 		Random r = new Random();
 		bool noBreed = r.nextInt( Math.round( 1 / probBreed ));
 		bool noDeath = r.nextInt( Math.round( 1 / probBreed ));
 		if(!noBreed) breed();	
 		if(!noDeath) removeSelfFromGrid();
 		if(!noBreed || !noDeath) return false;
 		return true;
 	}
 
 	void breed() {
 		Location loc = getLocation();
		getGrid().put( loc.getAdjacentLocation( Location.NORTH ), new LifeBug( getColor() ));
		getGrid().put( loc.getAdjacentLocation( Location.EAST ), new LifeBug( getColor() ));
		getGrid().put( loc.getAdjacentLocation( Location.SOUTH ), new LifeBug( getColor() ));
		getGrid().put( loc.getAdjacentLocation( Location.WEST ), new LifeBug( getColor() ));
 	}
 }
