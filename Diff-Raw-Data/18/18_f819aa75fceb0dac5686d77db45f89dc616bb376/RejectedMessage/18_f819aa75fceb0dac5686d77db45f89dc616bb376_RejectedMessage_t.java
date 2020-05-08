 package calendar;
 
 import java.io.IOException;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import no.ntnu.fp.model.Person;
 import client.Starter;
import server.RoundTime;
import server.MessageHandler;

 
 public class RejectedMessage extends Message {
 	
 	private Appointment app;
 	
 	public RejectedMessage(String title, String content, Appointment app) throws IOException {
		super(0);
 		this.app = app;
		this.title = title;
		this.content = content;
		this.dateSent = RoundTime.roundTime(new Date(System.currentTimeMillis()));
		MessageHandler.createMessage(this);
 	}
 	
 	private RejectedMessage(long id){
 		super(id);
 	}
 	
 	public static RejectedMessage recreateRejectedMessage(long msgId, String title, String content, Date dateSent, Appointment app) throws IOException{
 		RejectedMessage m = new RejectedMessage(msgId);
 		m.setDateSent(dateSent);
 		m.msgId = msgId;
 		m.content = content;
 		m.title = title;
 		m.setReceivers(new ArrayList<Person>());
 		m.app = app; 
 		return m;
 	}
 	
 	public Appointment getApp(){
 		return app;
 	}
 	
 	public void getAndExecuteUserResponse(Starter starter) throws IOException{
 		System.out.println("Hva vil du gjre?");
 		String[] options = {"Endre avtale", "Slett avtale", "Ignorer deltager"};
 		for(int i = 0; i < options.length; i++){
 			System.out.printf("%d. %s\n", i+1, options[i]);
 		}
 		Scanner scanner = new Scanner(System.in);
 		int userInput = scanner.nextInt();
 		switch(userInput){
 		case 1:
 			starter.changeAppointment(app);
 			break;
 		case 2:
 			starter.deleteAppointment(app);
 		}
 		
 	}
 
 }
