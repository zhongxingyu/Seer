 package organisms.g4;
 
 import organisms.Move;
 
 public class SimplePlayer extends TrackingPlayer {
 
 	@Override
 	protected void register(int key) {
 		
 	}
 
 	@Override
 	protected Move reproduce(boolean[] foodpresent, int[] neighbors,
 			int foodleft, int energyleft) {
 		// TODO Auto-generated method stub
 		if (energyleft > 250) {
 			return new Move(REPRODUCE, WEST, getState());
 		}
 
 		return null;
 	}
 
 	@Override
 	protected Move makeMove(boolean[] foodpresent, int[] neighbors,
 			int foodleft, int energyleft) {
 		Move m = null;
 		
 		//WE SHOULDNT MOVE
 		if (energyleft <= ENERGY_TO_MOVE && foodleft == 0) {
 			return null;
 		}
 		
 		
 		int direction = nextRandomInt(3);
 		direction+=1;
 		
 		switch (direction) {
 		case 0:
 			m = new Move(STAYPUT);
 			break;
 		case 1:
 			m = new Move(WEST);
 			break;
 		case 2:
 			m = new Move(EAST);
 			break;
 		case 3:
 			m = new Move(NORTH);
 			break;
 		case 4:
 			m = new Move(SOUTH);
 			break;
 		}
 		return m;
 	}
 
 	@Override
 	public String name() {
 		// TODO Auto-generated method stub
 		return "Basic Player";
 	}
 
 }
