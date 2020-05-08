 package per.andy.test;
 
 public class Methods {
  public void basic(){
 		ThreadPool.t[0] = new Runner().transfer(0, 50);
 		ThreadPool.count++;
 		ThreadPool.t[1] = new Runner().transfer(1, 50);
 		ThreadPool.count++;
 		ThreadPool.t[2] = new Runner().transfer(2, 50);
 		ThreadPool.count++;
 	}
 	public void mid(){
 		ThreadPool.t[0] = new Runner().transfer(0, 200);
 		ThreadPool.count++;
 		ThreadPool.t[1] = new Runner().transfer(1, 200);
 		ThreadPool.count++;
 		ThreadPool.t[2] = new Runner().transfer(2, 200);
 		ThreadPool.count++;
 	}
 	public void extreme(){
 		ThreadPool.t[0] = new Runner().transfer(0, 500);
 		ThreadPool.count++;
 		ThreadPool.t[1] = new Runner().transfer(1, 500);
 		ThreadPool.count++;
 		ThreadPool.t[2] = new Runner().transfer(2, 500);
 		ThreadPool.count++;
 	}
 }
