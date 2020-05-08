 import lejos.pc.comm.*;
 import java.io.*;
 import java.util.*;
 
 public class Server {
 	public static ArrayList<Transporter> transporters = new ArrayList<Transporter>();;
 
 	public static void main(String[] args) throws Exception {
 		Thread thread = new Thread(new Connections());
 		thread.start();
 	
 		String command = "";
 		while(!command.equals("exit")) {			
 			for (Transporter t: Server.transporters) {
 				System.out.print(t.name + " ");
 			}
 			System.out.println();
 			
 			Scanner sc = new Scanner(System.in);
 			System.out.println("Name: ");
 			String name = sc.next();
			if (name.equals("exit")) {
				for (Transporter t: Server.transporters) {
					t.cmdq.add('.');
				}
				Thread.sleep(10000);
				return;
			}
 			System.out.println("Commands: ");
 			command = sc.next();
 			
 			for (Transporter t: Server.transporters) {
 				if (t.name.equals(name)) {
 				
 					for (char c : command.toCharArray()) {
 						t.cmdq.add(c);
 					}
 				}
 			}
 		}
 	}
 }
