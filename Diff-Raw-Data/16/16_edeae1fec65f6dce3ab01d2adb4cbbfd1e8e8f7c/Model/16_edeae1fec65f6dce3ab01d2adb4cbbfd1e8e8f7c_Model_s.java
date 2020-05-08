 package edu.agh.tunev.model.kkm;
 
 import java.util.Vector;
 
 import edu.agh.tunev.model.AbstractModel;
 import edu.agh.tunev.model.PersonProfile;
 import edu.agh.tunev.model.PersonState;
 import edu.agh.tunev.statistics.LifeStatistics;
 import edu.agh.tunev.statistics.Statistics.AddCallback;
 import edu.agh.tunev.world.World;
 import edu.agh.tunev.world.World.ProgressCallback;
 
 public final class Model extends AbstractModel {
 
 	public final static String MODEL_NAME = "Autorski (\u00a9 2012 Jakub Rakoczy)";
 
 	public Model(World world) {
 		super(world);
 	}
 
 	private static final double DT = 0.05;
 
 	@Override
 	public void simulate(double duration, Vector<PersonProfile> profiles,
 			ProgressCallback progressCallback, AddCallback addCallback) {
 		// number of iterations
 		int num = (int) Math.round(Math.ceil(world.getDuration() / DT));
 		progressCallback.update(0, num, "Initializing...");
 
 		// init charts
 		LifeStatistics lifeStatistics = new LifeStatistics();
 		addCallback.add(lifeStatistics);
 
 		// init board
 		Board board = new Board(world);
 
 		// init people
 		for (PersonProfile profile : profiles)
 			board.addAgent(new Agent(board, profile));
 
 		// simulate
 		double t = 0;
 		for (int iteration = 1; iteration <= num; iteration++) {
 			// update
 			t += DT;
 			board.update(t, DT);
 
 			// chart inputs
 			int currentNumDead = 0;
 			int currentNumAlive = 0;
 			int currentNumRescued = 0;
 
 			// save states
 			for (Agent p : board.getAgents()) {
				interpolator.saveState(p.profile, t, new PersonState(
						p.position, p.phi, p.getStance()));
				if (p.isExited())
 					currentNumRescued++;
				else if (!p.isAlive())
 					currentNumDead++;
 				else
 					currentNumAlive++;
 			}
 
 			// update charts
 			lifeStatistics.add(t, currentNumAlive, currentNumRescued,
 					currentNumDead);
 
 			// UI simulation progress bar
 			progressCallback.update(iteration, num,
 					(iteration < num ? "Simulating..." : "Done."));
 		}
 	}
 
 }
