 package ss.apiImpl.strategies;
 
 import java.util.List;
 
 import ss.api.Iteration;
 import ss.api.Project;
 import ss.api.ReasignationStrategy;
 import ss.apiImpl.DistributionManager;
 import ss.gui.out.SimulationListener;
 
 public class ReasignationStrategyImpl implements ReasignationStrategy {
 
 	private boolean idleStrategy;
 	private boolean switchStrategy;
 	private boolean freelanceStrategy;
 	private SimulationListener listener;
 
 	public final static int IDLE_STRATEGY = 0;
 	public final static int SWITCH_STRATEGY = 1;
 	public final static int FREELANCE_STRATEGY = 2;
 
 	public ReasignationStrategyImpl(boolean idleStrategy,
 			boolean switchStrategy, boolean freelanceStrategy,
 			SimulationListener listener) {
 		this.idleStrategy = idleStrategy;
 		this.switchStrategy = switchStrategy;
 		this.freelanceStrategy = freelanceStrategy;
 		this.listener = listener;
 	}
 
 	@Override
 	public int reasing(Project to, List<Project> from, int idleProgrammers) {
 		int ret = 0;
 		if (idleStrategy) {
 			ret = idleStrategyReasign(to, idleProgrammers);
 		}
 		if (switchStrategy) {
 			switchStrategyReasign(to, from);
 		}
 		if (freelanceStrategy) {
 			freelanceStrategyReasign(to);
 		}
 		return ret;
 	}
 
 	@Override
 	public String getStrategy() {
 		if (idleStrategy && switchStrategy && freelanceStrategy) {
 			return "freelance";
 		}
 		if (idleStrategy && switchStrategy) {
 			return "switch";
 		}
 
 		return "idle";
 	}
 
 	private int idleStrategyReasign(Project to, int idleProgrammers) {
 		boolean delayed = true;
 		int newProgrammers = 0;
 		Iteration iteration = to.getCurrentIteration();
 		int projectProgrammers = to.getProgrammersWorking();
 		int newEstimateProgrammers = 0;
 		int newBackEstimation = 0;
 		int newFrontEstimation = 0;
 		int newIterationEstimation = 0;
 		while (newProgrammers < idleProgrammers && delayed
 				&& ((newProgrammers + projectProgrammers) < 4)) {
 			newProgrammers++;
 			newEstimateProgrammers = projectProgrammers + newProgrammers;
 			newBackEstimation = DistributionManager.getInstance()
 					.getLastingDaysForBackendIssue(newEstimateProgrammers);
 			newFrontEstimation = DistributionManager.getInstance()
 					.getLastingDaysForFrontendIssue(newEstimateProgrammers);
 			newIterationEstimation = newBackEstimation + newFrontEstimation;
 
 			delayed = iteration.isDelayedWith(newIterationEstimation);
 
 		}
 		if (newEstimateProgrammers == 0) {
 			return 0;
 		}
 		to.addProgrammers(newProgrammers);
 		iteration.setEstimate(newIterationEstimation);
 		return newProgrammers;
 	}
 
 	private void switchStrategyReasign(Project to, List<Project> from) {
 		int projectIndex = from.indexOf(to);
 		boolean delayed = true;
 
 		// Last proyect cant switch programmers with another project.
 		if (projectIndex == from.size() - 1) {
 			return;
 		}
 		Iteration iteration = to.getCurrentIteration();
 		int projectProgrammers = to.getProgrammersWorking();
 		int newProgrammers = 0;
 		int newEstimateProgrammers = 0;
 		int newBackEstimation = 0;
 		int newFrontEstimation = 0;
 		int newIterationEstimation = 0;
 
 		// Perhaps the idleStrategy already fixed the estimate, so this is
 		// calculated again
		delayed = iteration.isDelayedWith(projectProgrammers);
 
 		// Iterates from minor priority to mayor
 		while (delayed && ((newProgrammers + projectProgrammers) < 4)) {
			for (int i = from.size() - 1; i >= projectIndex; i--) {
 				Project other = from.get(i);
 				int programmersQty = other.getProgrammersWorking();
 
 				if (programmersQty > 0) {
 					other.removeProgrammer();
 					newProgrammers++;
 					newEstimateProgrammers = projectProgrammers
 							+ newProgrammers;
 					newBackEstimation = DistributionManager.getInstance()
 							.getLastingDaysForBackendIssue(
 									newEstimateProgrammers);
 					newFrontEstimation = DistributionManager.getInstance()
 							.getLastingDaysForFrontendIssue(
 									newEstimateProgrammers);
 					newIterationEstimation = newBackEstimation
 							+ newFrontEstimation;
 
 					delayed = iteration.isDelayedWith(newIterationEstimation);
 				}
 			}
 		}
 
 		if (newProgrammers > 0) {
 			to.addProgrammers(newProgrammers);
 			iteration.setEstimate(newIterationEstimation);
 		}
 	}
 
 	private void freelanceStrategyReasign(Project to) {
 		int maxCost = to.getMaxCost();
 		boolean delayed = true;
 		int newProgrammers = 0;
 		int newEstimateProgrammers = 0;
 		int newBackEstimation = 0;
 		int newFrontEstimation = 0;
 		int newIterationEstimation = 0;
 		Iteration iteration = to.getCurrentIteration();
 		int projectProgrammers = to.getProgrammersWorking();
 
 		// Perhaps the idleStrategy and switchStrategy already fixed the
 		// estimate, so this is
 		// calculated again
		delayed = iteration.isDelayedWith(projectProgrammers);
 
 		while (newProgrammers < maxCost && delayed
 				&& ((newProgrammers + projectProgrammers) < 4)) {
 			newProgrammers++;
 			newEstimateProgrammers = projectProgrammers + newProgrammers;
 			newBackEstimation = DistributionManager.getInstance()
 					.getLastingDaysForBackendIssue(newEstimateProgrammers);
 			newFrontEstimation = DistributionManager.getInstance()
 					.getLastingDaysForFrontendIssue(newEstimateProgrammers);
 			newIterationEstimation = newBackEstimation + newFrontEstimation;
 
 			delayed = iteration.isDelayedWith(newIterationEstimation);
 		}
 
 		if (newProgrammers > 0) {
 			to.addProgrammers(newProgrammers);
 			iteration.setEstimate(newIterationEstimation);
 			to.decreaseCost(newProgrammers);
 			listener.updateCost(to);
 		}
 	}
 
 	@Override
 	public boolean isSwitchStrategy() {
 		return switchStrategy;
 	}
 
 	@Override
 	public boolean isFreelanceStrategy() {
 		return freelanceStrategy;
 	}
 
 	@Override
 	public boolean isIdleStrategy() {
 		return idleStrategy;
 	}
 
 	public int getStrategyID() {
 		if (getStrategy().equals("idle")) {
 			return IDLE_STRATEGY;
 		}
 		if (getStrategy().equals("switch")) {
 			return SWITCH_STRATEGY;
 		}
 		return FREELANCE_STRATEGY;
 	}
 
 }
