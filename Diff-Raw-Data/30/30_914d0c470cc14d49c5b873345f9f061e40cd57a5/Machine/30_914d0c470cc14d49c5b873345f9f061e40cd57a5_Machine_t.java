 import java.util.ArrayList;
 
 
 public class Machine {
 
 	private ArrayList<Task> tasks = new ArrayList<Task>();
 	private int machine_id;
	private double speed;
	private double currentTime;
 	
 	public Machine(int machine_id, ArrayList<Task> tasks, int speed) {
 		this.machine_id = machine_id;
 		this.tasks = tasks;
 		this.speed = speed;
 	}
 	
 	public ArrayList<Task> getTasks() {
 		return tasks;
 	}
	public double getSpeed() {
 		return speed;
 	}
	public double calcCurrentTime() {
		double time = 0;
 		if (tasks.size() > 0) {
 			for (int i = 0; i < tasks.size(); i++) {
 				time += tasks.get(i).getProcessing_time();
 			}
 			this.currentTime = time/speed;
 			return currentTime;
 		}
 		else {
 			return 0;
 		}
 	}
 	public int getMachine_id() {
 		return machine_id;
 	}
 	public String toString() {
 		return "Machine id: " + machine_id +", speed: " + speed + ", tasks: " + tasks;
 	}
 	
 
 }
