 package util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 public class NetworkCommunication {
 	private BufferedReader output;
 	private PrintWriter input;
 
 	public NetworkCommunication(PrintWriter input, BufferedReader output) {
 		this.input = input;
 		this.output = output;
 
 	}
 
 	public void send(String message) {
 		Gson gson = new Gson();
 
 		String json = gson.toJson(message);
 		input.write(json + "\n");
 		input.flush();
 	}
 
	public void sendByteArray(byte[] salt) {
		Gson gson = new Gson();
		
		String json = gson.toJson(salt);
		input.write(json + "\n");
		input.flush();
	}
	
	public byte[] receiveByteArray() {
		Gson gson = new Gson();
		
		try {
			byte[] result = gson.fromJson(output.readLine(), byte[].class);
			return result;
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
 	public String receive() {
 		Gson gson = new Gson();
 
 		try {
 			String result = gson.fromJson(output.readLine(), String.class);
 			return result;
 		} catch (JsonSyntaxException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
