 package act.model;
 
 public class WithdrawThread implements Runnable{
 
 	private AccountModel model;
 	private String amount;
 	private int ops;
 	
 	public WithdrawThread(AccountModel m, String amt, int ops, String name){
 		this.model = m;
 		this.amount = amt;
 		this.ops = ops;
 	}
 	@Override
 	public void run() {
 		
 			try {
 					while(model.stopWithdrawAgents != 1){
 						model.threadWithdraw(amount);
 						Thread.sleep(1000/ops);
						System.out.println("Thread Running");
 					}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 //				System.out.println("Thread Error");
 				e.printStackTrace();
 			}
 //		}
 	}
 
 }
