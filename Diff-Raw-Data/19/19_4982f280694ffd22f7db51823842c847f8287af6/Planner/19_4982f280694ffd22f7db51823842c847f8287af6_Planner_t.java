 package device;
 
 import core.Logger;
 import data.Position;
 import javaclient3.LocalizeInterface;
 import javaclient3.MapInterface;
 import javaclient3.PlannerInterface;
 import javaclient3.PlayerClient;
 import javaclient3.PlayerException;
 import javaclient3.structures.PlayerConstants;
 import javaclient3.structures.PlayerPose;
 import javaclient3.structures.localize.PlayerLocalizeSetPose;
 import javaclient3.structures.planner.PlayerPlannerData;
 
 public class Planner extends Device implements Runnable {
 	// Planner is running its own Player server
 	protected PlayerClient playerclient = null;
 	protected PlannerInterface plan = null;
 	protected LocalizeInterface loci  = null;
 	protected MapInterface mapi = null;
 	protected PlayerLocalizeSetPose locPose = null;
 	protected final int SLEEPTIME = 100;
 
 	// Every class of this type has it's own thread
 	public Thread thread = new Thread ( this );
 
 	// initial values for the covariance matrix (c&p example from playernav)
 	protected double cov[] = { 0.5*0.5, 0.5*0.5, (Math.PI/6.0)*(Math.PI/6.0), 0, 0, 0 };
 
 	// set the initial guessed pose for localization (AMCL)
 	protected PlayerPose goal = null;
 	protected PlayerPose pose = null;
 	protected PlayerPlannerData ppd = null;
 	private boolean isNewGoal = false;
 	private boolean isNewPose = false;
 
 	// Host id
 	public Planner (String host, int port, int id) {
 		super(id);
 		try {
 			// Connect to the Player server and request access to Position
 			playerclient  = new PlayerClient (host, port);
 			Logger.logActivity(false, "Running", this.toString(), id, thread.getName());
 
 			mapi = this.playerclient.requestInterfaceMap(0, PlayerConstants.PLAYER_OPEN_MODE);
 			loci = this.playerclient.requestInterfaceLocalize(0, PlayerConstants.PLAYER_OPEN_MODE);
 			plan = this.playerclient.requestInterfacePlanner(0, PlayerConstants.PLAYER_OPEN_MODE);
 
 			// set the initial guessed pose for localization (AMCL)
 			this.locPose = new PlayerLocalizeSetPose ();
 			
 			this.pose = new PlayerPose();
 			this.goal = new PlayerPose();
 
 			// enable motion
 			plan.setRobotMotion(1);
 
 			// set the first mean values
 			locPose.setMean (this.pose);
 			locPose.setCov (cov);
 			
 			// Automatically start own thread in constructor
 			this.thread.start();
 		} catch ( PlayerException e ) {
			System.err.println ("    [ " + e.toString() + " ]");
 			Logger.logActivity(true, "Connecting", this.toString(), id, thread.getName());
 			throw new IllegalStateException();
 		}
 	}
 	public void setGoal (Position goal) {
 		this.goal.setPx(goal.getX());
 		this.goal.setPy(goal.getY());
 		this.goal.setPa(goal.getYaw());
 		isNewGoal = true;
 	}
 	public Position getGoal() {
 		return new Position(
 				goal.getPx(),
 				goal.getPy(),
 				goal.getPa());
 	}
 	// Only to be called @~10Hz
 	protected void update () {
 		if (loci.isDataReady()) {
 			if ( loci.getData() != null ) {
 				if ( loci.getData().getHypoths_count() > 0 ) {
 					System.out.println("Hypothesis #: " + this.loci.getData().getHypoths_count());
 					this.pose = this.loci.getData().getHypoths()[0].getMean();
 					this.printPos(this.pose);
 				}
 			}
 			if (loci.getParticleData() != null) {
 				System.out.println("Particle #: " + this.loci.getParticleData().getParticles_count());
 			}
 		}
 		// TODO check if position is on map
 		if (plan.isDataReady()) {
 			// request recent planner data
 			this.ppd = plan.getData ();
 			System.out.println (ppd.getWaypoints_count());
 		}
 		// update goal
 		if(isNewGoal) {
 			isNewGoal = false;
 			plan.setGoal(this.goal);
 		}
 		// update location belief
 		if(isNewPose) {
 			isNewPose = false;
 			loci.setPose (locPose);			
 		}
 
 		try { Thread.sleep (this.SLEEPTIME); }
 		catch (InterruptedException e) { this.thread.interrupt(); }
 
 	}
 
 	private void printPos(PlayerPose pose2) {
 		System.out.printf("Current pos belief: (%5.2f,%5.2f,%5.2f)",
 			pose2.getPx(),
 			pose2.getPy(),
 			pose2.getPa() );
 	}
 	@Override
 	public void run() {
 		while ( ! this.thread.isInterrupted()) {
 			this.update();
 		}
 	}
 	public void shutdown() {
 		this.playerclient.close();
 		while (this.playerclient.isAlive());
 		this.thread.interrupt();
 		while (this.thread.isAlive());
 		Logger.logActivity(false, "Shutdown", this.toString(), id, thread.getName());
 	}
 	public void setPose(Position position) {
 		this.pose.setPx(position.getX());
 		this.pose.setPy(position.getY());
 		this.pose.setPa(position.getY());
 		this.isNewPose = true;
 	}
 	public Position getPose() {
 		return new Position(pose.getPx(),pose.getPy(),pose.getPa());
 	}
 }
