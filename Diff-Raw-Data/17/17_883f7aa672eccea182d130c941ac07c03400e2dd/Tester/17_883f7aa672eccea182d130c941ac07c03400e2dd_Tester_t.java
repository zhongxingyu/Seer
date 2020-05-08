 package client;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import org.json.JSONObject;
 
 import server.Logger;
import server.Scoreboard;
 
 public class Tester {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		/*
 		try {
 			JSONObject test = new JSONObject();
 			test.put("id", "0");
 			test.put("auth","657412");
 			test.put("x","0");
 			test.put("y","0");
 			
 			String data = URLEncoder.encode(test.toString(), "UTF-8");
 
 			// Send data
 			URL url = new URL("http://localhost:8000/game/status");
 			URLConnection conn = url.openConnection();
 			conn.setDoOutput(true);
 			OutputStreamWriter wr = new OutputStreamWriter(
 					conn.getOutputStream());
 			wr.write(data);
 			wr.flush();
 
 			// Get the response
 			BufferedReader rd = new BufferedReader(new InputStreamReader(
 					conn.getInputStream()));
 			String line;
 			while ((line = rd.readLine()) != null) {
 				System.out.println(line);
 			}
 			wr.close();
 			rd.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		*/
 		
 		Logger log = new Logger();
 		log.writeln("Testing");
 		log.writeln("this");
 		log.writeln("stuff");
 		log.closeLog();
 		
 		
		Scoreboard.updateScores("Caleb Robinson", 5);
		Scoreboard.updateScores("DC DO work", 999);
		Scoreboard.updateScores("Bob", 1);
		Scoreboard.updateScores("Tim", 7);
		Scoreboard.updateScores("Fred", 8);
		Scoreboard.updateScores("Jim", 4);
 		
		Scoreboard.printScores();
 
 	}
 
 }
