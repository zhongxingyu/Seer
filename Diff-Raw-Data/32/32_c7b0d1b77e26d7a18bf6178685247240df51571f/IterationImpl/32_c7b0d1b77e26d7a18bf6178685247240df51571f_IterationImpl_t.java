 package ss.apiImpl;
 
 import ss.api.Issue;
 import ss.api.Iteration;
 
 public class IterationImpl implements Iteration {
 
 	private Issue backendIssue;
 
 	private Issue frontendIssue;
 
 	private int duration;
 
 	private int estimate = Integer.MAX_VALUE;
 
 	private Integer programmersWorking;
 
	private int lastingDays = Integer.MAX_VALUE;
 
 	public IterationImpl(Issue backendIssue, Issue frontendIssue, int duration) {
 		this.backendIssue = backendIssue;
 		this.frontendIssue = frontendIssue;
 		this.duration = duration;
 		this.programmersWorking = 0;
 	}
 
 	@Override
 	public int getEstimate() {
 		return estimate;
 	}
 
 	@Override
 	public void setEstimate(int estimate) {
 		this.estimate = estimate;
 		this.lastingDays = estimate;
 	}
 
 	public int getDuration() {
 		return duration;
 	}
 
 	@Override
 	public Issue getBackendIssue() {
 		return backendIssue;
 	}
 
 	@Override
 	public Issue getFrontendIssue() {
 		return frontendIssue;
 	}
 
 	@Override
 	public Issue getCurrentIssue() {
 		return null;
 	}
 
 	@Override
 	public Issue getNextIssue(int programmersQty) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isDelayed() {
 		return timeDelayed() > 0;
 	}
 
 	@Override
 	public int timeDelayed() {
 		return getEstimate() - getDuration();
 	}
 
 	@Override
 	public boolean isDelayedWith(int estimation) {
 		return estimation > getDuration();
 	}
 
 	@Override
 	public Integer getProgrammersWorking() {
 		return programmersWorking;
 	}
 
 	@Override
 	public void addProgrammer(int qty) {
 		programmersWorking += qty;
 	}
 
 	@Override
 	public void removeProgrammer() {
 		programmersWorking--;
 	}
 
 	@Override
 	public boolean finished() {
 		return lastingDays == 0;
 	}
 
 	@Override
 	public void decreaseLastingDays() {
 		lastingDays--;
 
 	}
 
 	@Override
 	public int getLastingDays() {
 		return lastingDays;
 	}
 
 	@Override
 	public String toString() {
 		return "Iteration duration: " + duration + " estimate: " + estimate
 				+ " progQty: " + programmersWorking + " lastingDays: "
 				+ lastingDays;
 	}
 
 }
