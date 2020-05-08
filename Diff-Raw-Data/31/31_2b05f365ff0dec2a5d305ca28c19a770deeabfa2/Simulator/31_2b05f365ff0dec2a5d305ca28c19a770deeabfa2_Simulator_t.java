 import java.util.Scanner;
 
 public class Simulator implements Runnable {
 	//This class will be used to gather and print statistics for the simulation run
 	//as well as being the place where the simulation is actually run
 	//It will also compare two or more different scheduling algorithms
 
 	public void getStats (){
 		
 	}
 	
 	
 	public static void main(String[] args) {
 		Thread t = new Thread(new Simulator());
 		t.start();
 	}
 	
 	//create objects of every other class.
 
 
 	public void run() {
 		Machine comp = Machine.getInstance();
 		
 		QueueManager queue = new QueueManager(1);
 		queue.setDefaultDeviceTable();
 		queue.setDefaultResourceTable();
 		
 		//						pid name pri subtime  burst	dev req		res req
 		Process p1 = new Process(1,"p1", 1, 	0, 		6, 	"D-1 D-2", "R-1 R-3");
 		Process p2 = new Process(2,"p2", 2, 	3, 		1,  "D-4 D-2", "R-2 R-5");
 		Process p3 = new Process(3,"p3", 1, 	3, 		3,  "D-1 D-2", "R-1 R-3");
 		Process p4 = new Process(4,"p4", 3, 	4, 		2,  "D-8 D-9", "R-2 R-4");
 		Process p5 = new Process(5,"p3", 1, 	6, 		9,  "D-1 D-2", "R-1 R-2");
 		
 		queue.addProcess(p1);
 		queue.addProcess(p2);
 		queue.addProcess(p3);
 		queue.addProcess(p4);
 		queue.addProcess(p5);
 		
 		
 		int response = 1;
 		String input;
 		
		System.out.println(queue.toString());
		
 		while (response > 0){
			
 			System.out.println("Welcome to the Scheduling Simulator");
 			System.out.println();
 			System.out.println("Select an option, then press Enter");
 			System.out.println("0. Exit");
 			System.out.println("1. Use Default Set of Processes");
 			System.out.println("2. Add Process");
 			System.out.println("3. Run Simulator");
 			System.out.println("4. Change Scheduling Type");
 			
 			@SuppressWarnings("resource")
 			Scanner reader = new Scanner(System.in);
 			response=reader.nextInt();
 			
 			switch (response){
 			case 1:
 				System.out.println("Done...");
 				break;
 			case 2:
 				int flag = 1;
 				String name;
 				int pid;
 				int prio;
 				int subtime;
 				int bursts;
 				String devices;
 				String resources;
 				while (flag == 1){
 					System.out.println("Please enter a name for the process");
 					name = reader.next();
 					System.out.println("Please enter an integer pid");
 					pid = reader.nextInt();
 					System.out.println("Please enter an integer priority > 0");
 					prio = reader.nextInt();
 					System.out.println("Please enter an integer submit time > 0");
 					subtime = reader.nextInt();
 					System.out.println("Please enter an integer number of bursts > 0");
 					bursts = reader.nextInt();
 					System.out.println("Please enter required devices in the format \"D-x D-y\", where x and y are integers");
 					devices = reader.next();
 					System.out.println("Please enter required resources in the format \"R-x R-y\", where x and y are integers");
 					resources = reader.next();
 					Process pTemp = new Process (pid, name, prio, subtime, bursts, devices, resources);
 					queue.addProcess(pTemp);
 					//add process
 					System.out.println("Process Added");
 					System.out.println("Enter 1 to add another process, 0 to return to main menu");
 					flag = reader.nextInt();
 				}
 				break;
 			case 3:
 				//run da program
 				while (!queue.isFinished()){
 					queue.update();
 					queue.toString();
 					queue.cycleIncrement();
 				}
 				System.out.println("Simulation Done!");
 				break;
 			case 4:
 				//change scheduling
 				System.out.println("Pick one of the following option:");
 				System.out.println("1. First-Come First-Serve");
 				System.out.println("2. Shortest Job First");
 				System.out.println("3. Priotiry Scheduling");
 				queue.setScheduler(reader.nextInt());
 				System.out.println("Scheduler Changed!");
 				break;
 			default:
 				break;
 			}
 			try {
 				Thread.sleep(1000);
 			} catch(InterruptedException ex) {
 			    Thread.currentThread().interrupt();
 			
 			}
 		}
 		
 	}
 
 	
 	
 }
