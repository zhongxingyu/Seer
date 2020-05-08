 package final_project.control;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class TournamentRunner implements Runnable {
 	SMSController _smsController;
 	InputStream _input;
 
 	public TournamentRunner(SMSController cont, InputStream input){
 		_smsController = cont;
 		_input = input;
 	}
 	@Override
 	public void run() {
 		BufferedReader in = new BufferedReader(new InputStreamReader(_input));
 		String line;
 		try {
 			line = in.readLine();
 			while (line!=null) {
 				String[] parts = line.split("\\s+");
 				for (int i = 0; i < parts.length; i++) {
 					System.out.println("PARTS: "+parts[i]);
 				}
 				try {
 					_smsController.returnResults(Integer.parseInt(parts[0]),
 							Integer.parseInt(parts[1]),
 							Integer.parseInt(parts[2]),
 							Integer.parseInt(parts[3]),
 							Integer.parseInt(parts[4]));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
