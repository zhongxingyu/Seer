 package bot;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import cx.ath.matthew.unix.*;
 
 import util.InvalidMessageException;
 import util.Logger;
 
 class InputOutputUnit{
 	
 	private BufferedReader input;
 	private Socket mySocket;
 	private UnixSocket myUnixSocket;
 	private PrintWriter output;
 	
 	public InputOutputUnit(){
 		input = new BufferedReader(new InputStreamReader(System.in));
 		output = new PrintWriter(System.out);
 	}
 	
 	public InputOutputUnit(String unixSocketAddress)
 			throws IOException{
 		myUnixSocket = new UnixSocket(unixSocketAddress);//Connection is done in the builder
 		input = new BufferedReader(new InputStreamReader(myUnixSocket.getInputStream()));
 		output = new PrintWriter(myUnixSocket.getOutputStream());
 	}
 	
 	public InputOutputUnit(String hostname, int portNo)
 			throws UnknownHostException, IOException{
 		mySocket = new Socket(hostname, portNo);
 		input = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
 		output = new PrintWriter(mySocket.getOutputStream());
 	}
 	
 	public void parseNextTurn(Bot b) throws InvalidMessageException, IOException{
 		try{
 			Logger.println("Parsing Next Turn");
 			char buffer[] = new char[1];
 			// Verify start of message :
 			// All middle_man communications must starts with a precise char
 			int nb_read;
 			nb_read = input.read(buffer, 0, 1);
 			Logger.println(nb_read + " chars read");
 			Logger.println("first char received : '" + buffer[0] + "'");
 			if (buffer[0] != Protocole.START_TOKEN)
 			    throw new InvalidMessageException("Invalid start Token. Expected \"" +
 			    								  Protocole.START_TOKEN + "\" received \"" +
 			    								  buffer[0] + "\"");
 			// now, read all submessages
 			Logger.println("Parsing informations");
 			while (true){
 				// each message is prefixed by a char
 				nb_read = input.read(buffer, 0, 1);
 				Logger.println("Token read : " + buffer[0]);
 				switch(buffer[0]){
 				case Protocole.END_TOKEN :{
 					if (!b.map.isKnownPosition()){
 						Logger.println("Position unknown at end of message, parsing again");
 						parseNextTurn(b);
 						return;
 					}
 					Logger.println("Informations Parsed");
 					return;
 				}
 				case Protocole.GLYPH_TOKEN: parseGlyph(b); break;
 				case Protocole.SEED_TOKEN: parseSeed(b); break;
 				case Protocole.MAP_SIZE_TOKEN: parseMapSize(b); break;
 				default: throw new InvalidMessageException("Invalid submessage token :");
 				}
 			}
 		}catch(IOException e){
 			close();
 			throw e;
 		}
 	}
 	
 	public void parseGlyph(Bot b) throws InvalidMessageException, IOException{
 		char buffer[] = new char[3];
 		int nb_read;
 		Logger.println("Reading Glyph");
 		// All glyph message are formatted with g<l><c><g>
 		nb_read = input.read(buffer, 0, 3);
 		if (nb_read != 3)
 			throw new InvalidMessageException("Expected 3 chars, received " + nb_read);
 		int line = (int)buffer[1];
 		int col = (int)buffer[0];
 		Logger.println("Update glyph " + buffer[2] + " in [" + line +','+col+"]");
 		b.map.updateSquare(line, col, buffer[2]);
 	}
 	
 	public void parseMapSize(Bot b) throws IOException, InvalidMessageException{
 		char buffer[] = new char[2];
 		int nb_read;
 		Logger.println("Reading Map Size");
 		// All mapSize message are formatted with <#c><#l>
 		nb_read = input.read(buffer, 0, 2);
 		if (nb_read != 2)
 			throw new InvalidMessageException("Expected 2 chars, received " + nb_read);
 		int width = buffer[0];
 		int height = buffer[1];
 		Logger.println("Reading map Size : [" + height + "," + width +"]");
 		b.map.updateSize(height, width);
 		
 		Logger.println("Sending message to pass the class choice");
		output.write("\n");
 		output.flush();
 	}
 	
 	public void parseSeed(Bot b){
 		throw new RuntimeException("Unhandled function, parse Seed is not implemented yet");
 	}
 	
 	
 	public void broadcastMove(Direction d){
 		char action = d.getValue();
 		Logger.println("ACTION : " + action);
 		output.print(action);
 		output.flush();
 	}
 	
 	public void broadcastSearch(){
 		output.print(Protocole.SEARCH_TOKEN);
 		Logger.println("ACTION : " + Protocole.SEARCH_TOKEN);
 		output.flush();
 	}
 	
 	public void broadcastOpeningDoor(Direction d){
 		Logger.println("ACTION : " + Protocole.OPEN_TOKEN + d.getValue());
 		output.print(Protocole.OPEN_TOKEN);
 		output.print(d.getValue());
 		output.flush();
 	}
 	
 	public void close(){
 		try{
 			output.close();
 			input.close();
 			if (mySocket != null)
 				mySocket.close();
 			if (myUnixSocket != null)
 				myUnixSocket.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		
 	}
 }
