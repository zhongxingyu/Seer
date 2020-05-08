 package bisbat;
 
 import java.io.*;
 import java.net.SocketException;
 import java.util.Vector;
 import java.util.regex.*;
 
 public class RecieveGameOutput extends Thread {
 	
 	private BufferedReader reader;
 	private Bisbat bisbat;
 
 	public RecieveGameOutput (Bisbat bisbat, InputStreamReader i) {
 		this.bisbat = bisbat;
 		reader = new BufferedReader(i);
 	}
 	
 	public void run(){
 		String line = "Starting PrintSteam";
 		String buffer = "";
 		while (line != null){
 			try {	
 				line = reader.readLine();
 				if(line == null) {
 					return; // done reading lines game output is closed
 				} else if(!line.equals("")) {
 					line = decolor(line);
 					if(line.matches(bisbat.getPromptMatch())) {
 						//Handle the buffer then clear it.
						//Bisbat.debug("Found the prompt!  Handling contents of buffer."); // debugger
						//Bisbat.debug(buffer);
 						handleOutput(buffer);
 						buffer = "";
 					} else {
 						buffer += line + "\n";
 						//System.out.println("Line(' " + line + " ' != '"  + bisbat.getPromptMatch()+ "'."); // debugger
 					}
 					//System.out.println("<-" + line); //print game output
 				}
 			} catch (SocketException e) {
 				System.err.println("Socket failed");
 				e.printStackTrace();
 				return;
 			} catch (IOException e) {
 				System.err.println("PrintSteam failed");
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Eliminates color information sent from the game.
 	 * @param string: string to be de-colored
 	 */
 	public String decolor(String string) {
 	    char ESCAPE = '\033';
 	    string = string.replaceAll(ESCAPE + "\\[[01];[0-9][0-9]m", "");
         return string;
 	}
 	
 	/**
 	 * Dispatch the output from the game to a room, data for 
 	 * bisbat to see, or the failure of a commands like move.
 	 * @param string The output from the game.
 	 */
 	public void handleOutput(String string) {
 		Pattern roomPattern = Pattern.compile(".*<>(.*)<>(.*)Exits:([^\\.]*)\\.(.*)$" , Pattern.MULTILINE | Pattern.DOTALL);
 		Matcher roomMatcher = roomPattern.matcher(string);
 
 		if(roomMatcher.matches()) {
 			//Bisbat.debug("~~~~~ Found a Room! ~~~~~");
 			String title = roomMatcher.group(1);
 			String description =roomMatcher.group(2);
 			String exits = roomMatcher.group(3);
 			String beingsAndObjects = roomMatcher.group(4);
 			
 			String[] roomOccupants = beingsAndObjects.split("\n");
 			//System.out.println("Beings and objects = '" + beingsAndObjects + "'."); // debugger
 			Vector<Being> beings = new Vector<Being>();
 			Vector<Item> items = new Vector<Item>();
 			
 			for(String occupant : roomOccupants) {
 				if(occupant.startsWith("M:")) {
 					Being b = new Being(occupant.substring(2));
 					beings.add(b);
 					bisbat.addKnowledgeOf(b);
 				} else if(occupant.startsWith("I:")) {
 					Item i = new Item(occupant.substring(2));
 					items.add(i);
 					bisbat.addKnowledgeOf(i);
 				}
 			}
 
 			Room recentlyDiscoveredRoom = new Room(title, description, exits, beings, items);
 			bisbat.foundRoom(recentlyDiscoveredRoom);
 			
 		} else {
 			//System.out.println("~~~~~ Not a Room! ~~~~~"); // debugger
 			string = string.trim();
			 // print non-room recieved game information
 			if(string.contains("You are too tired to go there now.")) {
 					bisbat.toDoList.add(new Pair<String,Object>("sleep",20));
 					bisbat.roomFindingThread.failure();
 
			} else if(string.length() < 1) {
 				
			} else {
				System.out.println("'<--" + string + "'");
				bisbat.resultQueue.add(string);
			}
 		}
 		//Bisbat.print("Done handling output."); // debugger
 	}
 }
 
