 /* 
 * synchro
 * 5431038821  
 * Ms.Vibhavee Trairattanapa 
 */
 public class LogisticBelt implements Runnable {
 
 	int num,logisticNum;
 	boolean work;
 	FactoryBelt factory;
 	ProgramGUI program;
 	
 	public LogisticBelt(int num,FactoryBelt factory,ProgramGUI program, int logisticNum){
 		this.num = num;
 		this.factory = factory;
 		this.program = program;
 		this.logisticNum = logisticNum;
 	}
 	public int getNum(){
 		return num;
 	}
 	public synchronized void increase(int val){
 		this.num += val;
 		program.setText(getNum()+"",logisticNum);
 	}
 	public void run(){
 		while(true){
			if(factory.getNum() <= 0){
				try {
					this.wait();
				}
				catch (InterruptedException e) {
					System.err.print("Error");
 				}
 			}
 			this.increase(1);
 			factory.decrease(1);
 			try {
 				Thread.sleep(150);
 			}
 			catch (InterruptedException e) {
 				System.err.print("Error");
 			}
 		}
 	}
 }
