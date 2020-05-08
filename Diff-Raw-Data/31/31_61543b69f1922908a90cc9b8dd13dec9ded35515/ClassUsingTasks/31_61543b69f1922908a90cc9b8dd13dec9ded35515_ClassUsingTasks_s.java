 package testclasses;
 
 import xsched.Task;
 
 public class ClassUsingTasks {
 
 	private boolean random() {
 		return System.currentTimeMillis() % 8 == 0;
 	}
 	
 	
 	public void xschedTask_A(Task<Void> now) {
 		
 		Task<Void> t1;
		this.xschedTask_B((t1 = new Task<Void>()), "hello world");
 		
 		while(random()) {
 			Task<Void> t2;
			this.xschedTask_B((t2 = new Task<Void>()), "hello world");
 			t1.hb(t2);
 			t1 = t2;
 		}
 		
 	}
 	
	public void xschedTask_B(Task<Void> now, String s) {
		System.out.println(s);
 	}
 	
 	public static void main(String[] args) {
 		ClassUsingTasks c = new ClassUsingTasks();
 		c.xschedTask_A(new Task<Void>());
 	}
 }
