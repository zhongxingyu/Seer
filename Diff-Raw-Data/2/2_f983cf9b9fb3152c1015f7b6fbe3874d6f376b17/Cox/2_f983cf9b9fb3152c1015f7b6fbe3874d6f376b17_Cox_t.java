 package siver.cox;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import repast.simphony.engine.schedule.ScheduledMethod;
 import siver.river.lane.Lane;
 import siver.boat.Boat;
 import siver.boat.BoatNavigation;
 import siver.context.SiverContextCreator;
 import siver.cox.actions.*;
 import siver.cox.control_policies.BasicBrain;
 import siver.cox.control_policies.CoxBrain;
 
 public class Cox {
 	//The boat the cox is controlling.
 	protected Boat boat;
 	
	//how fast and how far the boat would like to travel
 	private int desired_gear;
 	private double distance_to_cover;
 	
 	private boolean incapcitated;
 	
 	protected Action action;
 	protected BoatNavigation navigator;
 	
 	private CoxBrain control_policy;
 	
 	public Cox() {
 		
 	}
 	
 	public void launch(Boat boat, Lane launchLane, int desGear, double speedMult, double distance_to_cover, Integer launch_schedule_id) throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		launch(BasicBrain.class, boat, launchLane, desGear, speedMult, distance_to_cover, launch_schedule_id);
 	}
 	
 	public void launch(Class<? extends CoxBrain> control_policy_type, Boat boat, Lane launchLane, int desGear, double speedMult, double distance_to_cover, Integer launch_schedule_id) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		//save reference to boat launched in
 		this.boat = boat;
 		this.incapcitated = false;
 		this.desired_gear = desGear;
 		this.distance_to_cover = distance_to_cover;
 		
 		Constructor<? extends CoxBrain> cons = control_policy_type.getConstructor();
 		control_policy = cons.newInstance();
 		
 		boat.launch(this, launch_schedule_id);
 		navigator = new BoatNavigation(this, boat, false);
 		navigator.launch(launchLane);
 	}
 	
 	public void land() {
 		getNavigator().getEdge().removeCox(this);
 		boat.land();
 		SiverContextCreator.getContext().remove(this);
 	}
 	
 	//BEHAVIOUR
 	@ScheduledMethod(start = 1, interval = 1, priority=100)
 	public void step() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		makeObservations();
 		if(incapcitated) { return; } //cox can only look around if incapcitated, not allowed to carry out an action (and so there is no point choosing an action)
 		chooseAction();
 		executeAction();
 	}
 	
 	private void makeObservations() {
 		CoxObservations observations = CoxObservations.make(this, boat, navigator);
 		control_policy.updateObservations(observations);
 	}
 	
 	private void chooseAction() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		if(action == null) {
 			//have control policy pick a new action if the cox isn't in the middle of one at the moment
 			Constructor<? extends Action> cons = control_policy.chooseAction().getConstructor(Cox.class);
 			action = cons.newInstance(this);
 		}
 	}
 	
 	private void executeAction() {
 		action.execute();
 	}
 	
 	/*
 	 * HELPERS
 	 */
 	public Action getAction() {
 		return action;
 	}
 	
 	public void clearAction() {
 		action = null;
 	}
 	
 	public Boat getBoat() {
 		return boat;
 	}
 	
 	public BoatNavigation getNavigator() {
 		return navigator;
 	}
 	
 	public void incapcitate() {
 		boat.deadStop();
 		incapcitated = true;
 	}
 	
 	public void recapcitate() {
 		incapcitated = false;
 	}
 	
 	public boolean isIncapcitated() {
 		return incapcitated;
 	}
 	
 	public int desired_gear() {
 		return desired_gear;
 	}
 	
 	public String control_policy() {
 		return this.control_policy.getClass().getSimpleName();
 	}
 	
 	public double getGoalDistance() {
 		return distance_to_cover;
 	}
 }
