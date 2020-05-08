 package no.ntnu.fp.net.network.client;
 
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import no.ntnu.fp.model.Appointment;
 import no.ntnu.fp.model.Meeting;
 import no.ntnu.fp.model.XmlHandler;
 import no.ntnu.fp.net.network.Request;
 import no.ntnu.fp.net.network.Request.Method;
 
 
 
 
 public class ClientWorker implements Runnable {
 	//fields
 	//private HashMap<String, Socket> clients;
 	private BlockingDeque<Object> inQueue;
 	private Socket mySocket;
 	private CommunicationController communication;
 	private LinkedBlockingDeque<Object> testQueue;
 	
 	//Constructor
 	public ClientWorker(Socket mySocket, LinkedBlockingDeque<Object>testQueue, CommunicationController communicationController){
 		this.mySocket = mySocket;
 		//this.inQueue = inQueue;
 		this.testQueue = testQueue;
 		communication = communicationController;
 	}
 	
 	
 	//methods
 	@Override
 	public void run() {
 		while(true){
 			//Sleep
 			
 			try {
 				Thread.currentThread().sleep(5000);
				
 				Object obj = testQueue.takeFirst();
 				if(handle(obj)){
 					System.out.println("dropped");
 				}else {
 					testQueue.putFirst(obj);
 				}
 				
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	public boolean handle(Object data){
 		Request response = (Request) data;
 		if (response.getMethod() == Method.MEETING_NOTIFICATION) {
 			Meeting meeting = (Meeting)response.getObject();
 			//communication.updateMeeting(meeting);
 			System.out.println("We have a notification");
 			return true;
 		
 		} else if(response.getMethod() == Method.CHANGE_MEETING_NOTFICATION) {
 			Meeting meeting = (Meeting)response.getObject();
			//communication.updateMeeting(meeting);
 			System.out.println("Someone changed a meeting (views)");
 			return true;
 		}else if(response.getMethod() == Method.CHANGE_APPOINTMENT_NOTIFICATION) {
 			Appointment appointment = (Appointment)response.getObject();
			//communication.updateAppointment(appointment);
 			System.out.println("Someone changed an appointment (views)");
 			return true;
 		}else {
 			return false;
 		}
 		
 		
 	}
 
 }
