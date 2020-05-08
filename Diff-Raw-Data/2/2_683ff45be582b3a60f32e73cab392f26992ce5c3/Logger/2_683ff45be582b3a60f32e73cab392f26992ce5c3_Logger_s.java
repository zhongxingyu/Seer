 package logging;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 
 public class Logger {
     private static final String LOCATION = "Log";
     private ArrayList<Message> messages= new ArrayList<Message>();
     
     public void log(Message message){
     	System.out.println(message.getMessage());
     	messages.add(message);
     }
     
     public void writeMessages(){
     	try{
     		BufferedWriter bw = new BufferedWriter(new FileWriter(getLatestFileName(0)));
     		for(Message m : messages)
    			bw.write(m.getMessage()+"\n");
     		bw.flush();
     		bw.close();
     	}
     	catch(Exception ex){
     		ex.printStackTrace();
     		log(new Message("Failed to write log", Message.Type.Error, ex));
     	}
     }
     
     private String getLatestFileName(int i){
     	File f = new File(Logger.LOCATION+i+".txt");
     	if(!f.exists())
     		return LOCATION+i+".txt";
     	return getLatestFileName(i+1);
     }
     
 }
