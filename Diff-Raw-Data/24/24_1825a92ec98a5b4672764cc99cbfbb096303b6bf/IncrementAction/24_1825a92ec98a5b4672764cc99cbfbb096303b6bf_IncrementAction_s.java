 package ch.hsr.objectCaching.action;
 
 import ch.hsr.objectCaching.account.Account;
 
 public class IncrementAction extends Action {
 	private static final long serialVersionUID = 1L;
 	private double balanceResult;
 	private float factor;
 	private long delay;
 
 	public IncrementAction(long delay, float factor) {
 		this.factor = factor;
 		this.delay = delay;
 	}
 
 	@Override
 	public void execute(Account account) {
 		boolean successfull = false;
 		boolean runOnce = true;
 
 		do {
 			result.startMeasuring();
 			// Read
 			balanceResult = account.getBalance();
 			// Delay
 			if (runOnce) {
 				try {
 					Thread.sleep(delay);
 					runOnce = false;
 				} catch (InterruptedException e) {
 					System.out
 							.println("Action execution with delay interrupted");
 				}
 			}
 			// Writing
 			try {
				account.setBalance((int) (balanceResult * factor));
 				successfull = true;
 			} catch (RuntimeException exeption) {
 				System.out.println("Writing values failed with the value "
 						+ balanceResult * factor);
 				successfull = false;
 			}
 			result.stopMeasuring();
 		} while (!successfull);
 	}
 
 	public long getDelay() {
 		return delay;
 	}
 	
 	public float getFactor()
 	{
 		return factor;
 	}
 
 }
