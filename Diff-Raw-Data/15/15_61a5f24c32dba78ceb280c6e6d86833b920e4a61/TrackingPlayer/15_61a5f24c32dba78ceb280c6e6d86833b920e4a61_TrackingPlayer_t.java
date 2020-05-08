 package organisms.g4;
 
 import organisms.Move;
 import organisms.g4.trackers.FoodTracker;
 import organisms.g4.trackers.OtherOrganismTracker;
 import organisms.g4.trackers.PersonalSettingsTracker;
 
 public abstract class TrackingPlayer extends Group4BasePlayer {
 	protected FoodTracker foodTracker;
 	protected OtherOrganismTracker organismTracker;
 	protected PersonalSettingsTracker settingsTracker;
 
 	@Override
 	protected void init() {
 		foodTracker = new FoodTracker();
 		organismTracker = new OtherOrganismTracker();
 		settingsTracker = new PersonalSettingsTracker();
 	}
 
 	@Override
 	protected void preMoveTrack(boolean[] foodpresent, int[] neighbors,
 			int foodleft, int energyleft) {
		foodTracker.add(foodleft);
 	}
 
 	@Override
 	protected void postMoveTrack(Move move, boolean[] foodpresent,
 			int[] neighbors, int foodleft, int energyleft) {
		settingsTracker.psTrackerStore(energyleft, move.type()==REPRODUCE);	
		foodTracker.add(move, foodpresent);
		organismTracker.add(move, neighbors);

 	}
 }
