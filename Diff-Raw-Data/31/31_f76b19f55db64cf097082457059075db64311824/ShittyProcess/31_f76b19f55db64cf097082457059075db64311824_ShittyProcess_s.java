 public class ShittyProcess implements MigratableProcess {
 
 	private volatile boolean suspending;
 	private int i;
 	
 	public ShittyProcess(String args[]) {
 		i = 0;
 	}
 	
 	@Override
 	public void run() {
 		while (!suspending) { 
 			System.out.println(i);
 			Fib(i++);
			if (i > 45) {
 				break;
 			}
 		}
 		suspending = false;
 	}
 
 	@Override
 	public void suspend()
 	{
 		suspending = true;
 		while (suspending);
 	}
 	
 	public int Fib(int n) {
 		switch(n) {
 		case 0: return 1;
 		case 1: return 1;
 		default: return Fib(n-1) + Fib(n-2);
 		}
 	}
 }
