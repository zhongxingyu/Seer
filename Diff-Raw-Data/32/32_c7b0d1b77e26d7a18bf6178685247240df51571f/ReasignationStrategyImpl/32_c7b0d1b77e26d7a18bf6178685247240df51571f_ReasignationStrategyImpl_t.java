 package ss.apiImpl.strategies;
 
 import java.util.List;
 
 import ss.api.Iteration;
 import ss.api.Project;
 import ss.api.ReasignationStrategy;
 import ss.apiImpl.DistributionManager;
 
 public class ReasignationStrategyImpl implements ReasignationStrategy {
 
 	private boolean idleStrategy;
 	private boolean switchStrategy;
 	private boolean freelanceStrategy;
 
 	public ReasignationStrategyImpl(boolean idleStrategy,
 			boolean switchStrategy, boolean freelanceStrategy) {
 		this.idleStrategy = idleStrategy;
 		this.switchStrategy = switchStrategy;
 		this.freelanceStrategy = freelanceStrategy;
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
 
 	private int idleStrategyReasign(Project to, int idleProgrammers) {
 		boolean delayed = true;
 		int newProgrammers = 0;
 		Iteration iteration = to.getCurrentIteration();
 		int iterationProgrammers = iteration.getProgrammersWorking();
 		while (newProgrammers < idleProgrammers & delayed) {
 			newProgrammers++;
 			int newEstimateProgrammers = iterationProgrammers + newProgrammers;
 			int newBackEstimation = DistributionManager.getInstance()
 					.getLastingDaysForBackendIssue(newEstimateProgrammers);
 			int newFrontEstimation = DistributionManager.getInstance()
 					.getLastingDaysForFrontendIssue(newEstimateProgrammers);
 			int newIterationEstimation = newBackEstimation + newFrontEstimation;
 
 			if (!iteration.isDelayedWith(newIterationEstimation)
 					|| newProgrammers == idleProgrammers) {
 				delayed = false;
 				iteration.addProgrammer(newEstimateProgrammers);
 				iteration.setEstimate(newIterationEstimation);
 				return idleProgrammers - newProgrammers;
 			}
 		}
 		return 0;
 	}
 
 	private void switchStrategyReasign(Project to, List<Project> from) {
 		int projectIndex = from.indexOf(to);
 		Iteration iteration = to.getCurrentIteration();
 		int iterationProgrammers = iteration.getProgrammersWorking();
 		int newProgrammers = 0;
 		boolean delayed = true;
 		int programmersAvailable = 0;
 		boolean finished = false;
 		int newEstimateProgrammers = 0;
 		int newBackEstimation = 0;
 		int newFrontEstimation = 0;
 		int newIterationEstimation = 0;
 
 		// Iterates from minor priority to mayor
 		while (delayed && !finished) {
 			programmersAvailable = 0;
			for (int i = from.size() - 1; i >= projectIndex; i--) {
 				Project other = from.get(i);
 				Iteration otherIteration = other.getCurrentIteration();
 				int programmersQty = otherIteration.getProgrammersWorking();
 
 				if (programmersQty > 0) {
 					programmersAvailable++;
 					otherIteration.removeProgrammer();
 					newProgrammers++;
 					newEstimateProgrammers = iterationProgrammers
 							+ newProgrammers;
 					newBackEstimation = DistributionManager.getInstance()
 							.getLastingDaysForBackendIssue(
 									newEstimateProgrammers);
 					newFrontEstimation = DistributionManager.getInstance()
 							.getLastingDaysForFrontendIssue(
 									newEstimateProgrammers);
 					newIterationEstimation = newBackEstimation
 							+ newFrontEstimation;
 
 					if (!iteration.isDelayedWith(newIterationEstimation)) {
 						delayed = false;
 						iteration.addProgrammer(newEstimateProgrammers);
 						iteration.setEstimate(newIterationEstimation);
 						return;
 					}
 				}
 			}
 
 			if (programmersAvailable == 0) {
 				finished = true;
 				if (newProgrammers > 0) {
 					iteration.addProgrammer(newEstimateProgrammers);
 					iteration.setEstimate(newIterationEstimation);
 				}
 			}
 		}
 	}
 
 	private void freelanceStrategyReasign(Project to) {
 		int maxCost = to.getMaxCost();
 		boolean delayed = true;
 		int newProgrammers = 0;
 		Iteration iteration = to.getCurrentIteration();
 		int iterationProgrammers = iteration.getProgrammersWorking();
 		while (newProgrammers < maxCost & delayed) {
 			newProgrammers++;
 			int newEstimateProgrammers = iterationProgrammers + newProgrammers;
 			int newBackEstimation = DistributionManager.getInstance()
 					.getLastingDaysForBackendIssue(newEstimateProgrammers);
 			int newFrontEstimation = DistributionManager.getInstance()
 					.getLastingDaysForFrontendIssue(newEstimateProgrammers);
 			int newIterationEstimation = newBackEstimation + newFrontEstimation;
 
 			if (!iteration.isDelayedWith(newIterationEstimation)
 					|| newProgrammers == maxCost) {
 				delayed = false;
 				iteration.addProgrammer(newEstimateProgrammers);
 				iteration.setEstimate(newIterationEstimation);
 				to.decreaseCost(newProgrammers);
 				return;
 			}
 		}
 		return;
 	}
 }
