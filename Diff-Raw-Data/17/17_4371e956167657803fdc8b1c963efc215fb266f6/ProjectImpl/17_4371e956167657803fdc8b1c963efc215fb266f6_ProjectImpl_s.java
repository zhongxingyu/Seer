 package ss.apiImpl;
 
 import java.util.Deque;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import ss.api.Iteration;
 import ss.api.Project;
 
 public class ProjectImpl implements Project {
 
 	private Deque<Iteration> iterations = new LinkedBlockingDeque<>();
 
 	private Iteration currentIteration = null;
 
 	private Integer maxInvestment;
 
 	private Integer totalInvestment = 0;
 
 	private Integer duration = 0;
 
 	private boolean finished = false;
 
 	private Integer programmersWorking;
 
 	private Integer freelanceWorkers = 0;
 
 	private Integer id;
 
 	private Integer iterationsQty;
 
 	private Integer iterationNumber;
 
 	private double finalIterationProbability;
 
 	public ProjectImpl(Deque<Iteration> iterations, Integer maxInvestment,
 			Integer id) {
 		super();
 		this.id = id;
 		this.iterations = iterations;
 		this.iterationsQty = iterations.size();
 		this.iterationNumber = 1;
 		this.maxInvestment = maxInvestment;
 		setDuration();
 		this.currentIteration = iterations.pop();
 		this.programmersWorking = 0;
 		this.finalIterationProbability = 0.25;
 		this.freelanceWorkers = 0;
 	}
 
 	@Override
 	public int getIterationsQty() {
 		return this.iterationsQty;
 	}
 
 	@Override
 	public int getIterationNumber() {
 		return this.iterationNumber;
 	}
 
 	@Override
 	public void removeFreelanceProgrammers() {
 		freelanceWorkers = 0;
 	}
 
 	@Override
 	public void addFreelanceProgrammers(int qty) {
 		freelanceWorkers += qty;
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	private void setDuration() {
 		for (Iteration iteration : iterations) {
 			duration += iteration.getDuration();
 		}
 	}
 
 	public Integer getDuration() {
 		return duration;
 	}
 
 	public Integer getMaxInvestment() {
 		return maxInvestment;
 	}
 
 	public void addIteration(Iteration iteration) {
 		this.iterations.add(iteration);
 	}
 
 	public boolean nextIteration(int extraTime) {
 		try {
 			currentIteration = iterations.pop();
 			currentIteration.setDuration(currentIteration.getDuration()
 					+ extraTime);
 			iterationNumber++;
 		} catch (NoSuchElementException e) {
 			if (extraTime > 0) {
 				currentIteration = new IterationImpl(
 						IssueFactory.createBackendIssue(),
 						IssueFactory.createFrontEndIssue(), extraTime);
 				iterationNumber++;
 			} else {
 				Random r = new Random();
 				double random = r.nextDouble();
 				if (random < finalIterationProbability) {
 					finalIterationProbability /= 1.3;
 					currentIteration = new IterationImpl(
 							IssueFactory.createBackendIssue(),
 							IssueFactory.createFrontEndIssue(),
 							r.nextInt(22 - 17) + 17); // (17,22]);
 					iterationNumber++;
 					return false;
 				}
 				finished = true;
 
 			}
 		}
 
 		return finished;
 	}
 
 	public Iteration getCurrentIteration() {
 		if (currentIteration == null) {
 			currentIteration = iterations.pop();
 		}
 		return currentIteration;
 	}
 
 	@Override
 	public void decreaseInvestment() {
 		if (freelanceWorkers > 0) {
 			int availableInvestment = maxInvestment - totalInvestment;
 			if (availableInvestment < freelanceWorkers) {
 				totalInvestment += availableInvestment;
 				maxInvestment -= availableInvestment;
 				int diff = freelanceWorkers - availableInvestment;
 				freelanceWorkers = diff;
 			} else {
 				totalInvestment += freelanceWorkers;
 				maxInvestment -= freelanceWorkers;
 			}
 		}
 	}
 
 	@Override
 	public boolean finished() {
 		return finished;
 	}
 
 	@Override
 	public Integer getProgrammersWorking() {
 		return programmersWorking + freelanceWorkers;
 	}
 
 	@Override
 	public void addProgrammers(int qty) {
 		programmersWorking += qty;
 	}
 
 	@Override
 	public void removeProgrammer() {
 		programmersWorking--;
 		if (programmersWorking + freelanceWorkers == 0) {
 			currentIteration.setEstimate(Integer.MAX_VALUE);
 			return;
 		}
		if(programmersWorking<0){
			System.out.println(programmersWorking);
		}
 			
 		int newBackEstimation = DistributionManager.getInstance()
 				.getLastingDaysForBackendIssue(
 						programmersWorking + freelanceWorkers);
 		int newFrontEstimation = DistributionManager.getInstance()
 				.getLastingDaysForFrontendIssue(
 						programmersWorking + freelanceWorkers);
 		int newIterationEstimation = newBackEstimation + newFrontEstimation;
 		currentIteration.setEstimate(newIterationEstimation);
 	}
 	
 @Override
 	public int getFreelanceProgrammersWorking() {
 		return freelanceWorkers;
 	}
 
 	@Override
 	public int removeProgrammers() {
 		int old = programmersWorking;
 		programmersWorking = 0;
 		return old;
 	}
 
 	@Override
 	public int getTotalCost() {
 		return totalInvestment;
 	}
 
 	@Override
 	public String toString() {
 		return "Project It: " + iterations.size() + " maxCost: "
 				+ maxInvestment + " duration: " + getDuration() + " progQty: "
 				+ programmersWorking + " It:[" + currentIteration + "]";
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ProjectImpl other = (ProjectImpl) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 }
