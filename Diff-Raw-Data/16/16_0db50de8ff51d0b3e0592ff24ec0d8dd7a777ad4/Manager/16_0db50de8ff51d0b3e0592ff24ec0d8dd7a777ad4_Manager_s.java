 package edu.se.se441.threads;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.CyclicBarrier;
 
 import edu.se.se441.Office;
 
 public class Manager extends Thread {
 	
 	final int NUMQUESTIONS = 10;
 	
 	private Office office;
 	private CountDownLatch startSignal;
 	private BlockingQueue<Employee> hasQuestion = new ArrayBlockingQueue<Employee>(NUMQUESTIONS);
 	private boolean attendedMeeting1 = false;
 	private boolean attendedMeeting2 = false;
 	private boolean ateLunch = false;
 	private boolean attendedFinalMeeting = false;
 
 	
 	public Manager(Office office){
 		this.office = office;
 	}
 	
 	public void run(){
 		try {
 			// Starting all threads at the same time (clock == 0 / "8:00AM").
 			startSignal.await();
 			
 			// Waiting for team leads for the meeting.
 			office.waitForStandupMeeting();
 			Thread.sleep(150);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		while(office.getTime() < 1700){
 			try {
				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			while(!hasQuestion.isEmpty()){
 				answerQuestion();
 			}
 			if(office.getTime() >= 1000 && !attendedMeeting1){
 				 try {
 					sleep(600);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				 attendedMeeting1 = true;
 			}
 			if(office.getTime() >= 1200 && !ateLunch){
 				try {
 					sleep(600);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}				
 				ateLunch = true;
 			}
 			if(office.getTime() >= 1400 && !attendedMeeting2){
 				try {
 					sleep(600);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				attendedMeeting2 = true;
 			}
 			
 			// Is it time for the 4 oclock meeting?
 			if(office.getTime() >= 1600 && !attendedFinalMeeting){
 				office.waitForEndOfDayMeeting();
 				try {
 					sleep(150);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				attendedFinalMeeting = true;
 			}
 		}
 	}
 	
 	public void setStartSignal(CountDownLatch startSignal) {
 		this.startSignal = startSignal;
 	}
 	
 	public void askQuestion(Employee employee){
 		// Add question to queue
 		synchronized(hasQuestion){
 			hasQuestion.add(employee);
 		}
 		
 		// Waiting until question can be answered
 		while(hasQuestion.contains(employee)){
 			// Is it time for the 4 oclock meeting?
 			try {
 				if(office.getTime() >= 1600 && !employee.isAttendedEndOfDayMeeting()){
 					office.waitForEndOfDayMeeting();
 					sleep(150);
 					employee.setAttendedEndOfDayMeeting(true);
 				}
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		// Question is being answered
 		while(employee.isWaitingQuestion()){
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean isLeadAsking(Employee lead){
 		return hasQuestion.contains(lead);
 	}
 	
 	private void answerQuestion(){
 		Employee employee = hasQuestion.poll();
 		notifyAll();
 		try {
 			sleep(100);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		employee.questionAnswered();
 		notifyAll();
 	}
 
 }
