 package ch14.ex09;
 
 public class ThreadObserver{
 	public void ShowThread(final ThreadGroup group) {
 		new Thread(new Runnable() {
 			public void run() {
 				for (;;) { 
 					Show(group);
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						return;
 					}
 				}
 			} 
 		}, "Thread").start();
 	}
 
 	private void Show(ThreadGroup group) {
 		Thread[] list = new Thread[group.activeCount()];
 		int threadCount = group.enumerate(list, true);
 		
 		System.out.println("----------Start-----------");
 		for (Thread thread: list) {
			System.out.println("Thread name: " + thread.getName() + ", Group name: " + thread.getThreadGroup().getName());
 		}
 		System.out.println("-----------End-----------");
 	}
 	
 	public static void main(String[] args) {
 		ThreadObserver obj = new ThreadObserver();
 		ThreadGroup group1 = new ThreadGroup("Group1");
		ThreadGroup group2 = new ThreadGroup(group1, "Group2");
		ThreadGroup group3 = new ThreadGroup(group2, "Group3");
 		Thread th1 = new Thread(group1, new ActiveShortRunnable(), "Thread1");
 		Thread th2 = new Thread(group1, new ActiveShortRunnable(), "Thread2");
 		Thread th3 = new Thread(group1, new ActiveShortRunnable(), "Thread3");
		Thread th4 = new Thread(group2, new ActiveShortRunnable(), "Thread4");
		Thread th5 = new Thread(group3, new ActiveShortRunnable(), "Thread5");
 		th1.start();
 		th2.start();
 		th3.start();
		th4.start();
		th5.start();
 		
 		// w̃XbhO[vɏXbh̖O1bԊuŕ\
 		obj.ShowThread(group1);
 	}
 }
