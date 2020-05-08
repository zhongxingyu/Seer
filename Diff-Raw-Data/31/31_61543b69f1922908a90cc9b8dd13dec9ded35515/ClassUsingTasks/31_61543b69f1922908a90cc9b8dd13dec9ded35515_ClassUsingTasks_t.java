 package testclasses;
 
 import xsched.Task;
 
 public class ClassUsingTasks {
 
 	private boolean random() {
 		return System.currentTimeMillis() % 8 == 0;
 	}
 	
	public void xschedTask_A2(Task<Void> now) {
		Task<Void> t1;
		if(random()) {
			this.xschedTask_B((t1 = new Task<Void>()));
		} else {
			this.xschedTask_B((t1 = new Task<Void>()));
		}
		
		Task<Void> t2;
		this.xschedTask_B((t2 = new Task<Void>()));
		t1.hb(t2);		
	}
	
 	
 	public void xschedTask_A(Task<Void> now) {
 		
 		Task<Void> t1;
		this.xschedTask_B((t1 = new Task<Void>()));
 		
 		while(random()) {
 			Task<Void> t2;
			this.xschedTask_B((t2 = new Task<Void>()));
 			t1.hb(t2);
 			t1 = t2;
 		}
 		
 	}
 	
	public void xschedTask_B(Task<Void> now) {
		System.out.println("Hello World!");
 	}
 	
 	public static void main(String[] args) {
 		ClassUsingTasks c = new ClassUsingTasks();
 		c.xschedTask_A(new Task<Void>());
 	}
 }
