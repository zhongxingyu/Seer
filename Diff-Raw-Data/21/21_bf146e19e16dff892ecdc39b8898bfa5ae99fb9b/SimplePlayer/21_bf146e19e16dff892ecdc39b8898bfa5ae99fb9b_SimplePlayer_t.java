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
 		if (energyleft > 200) {
 			int direction = -1;
			for (int i =1; i < 5; i++) {
 				if (foodpresent[i] && neighbors[i] == -1) {
 					direction = i;
 				}
 			}
 			if (direction == -1) {
				for (int i =1; i < 5; i++) {
 					if ( neighbors[i] == -1) {
 						direction = i;
 					}
 				}
 			}
 			return new Move(REPRODUCE, direction, getState());
 		}
 
 		return null;
 	}
 
 	@Override
 	protected Move makeMove(boolean[] foodpresent, int[] neighbors,
 			int foodleft, int energyleft) {
 		Move m = null;
 
 		
		if (energyleft <= ENERGY_TO_MOVE * 5 && foodleft != 0) {
 			return null;
 		}
		
 		int direction = 4;
		for (int i =1; i < 5; i++) {
 			if (foodpresent[i]) {
 				direction = i;
 			}
 		}
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
 		return "Basic Player";
 	}
 
 }
