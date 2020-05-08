 
 
 import java.util.ArrayList;
 
 public class MessageHandler {
 
 	public MessageHandler() {
 	}
 
 	public ArrayList<String> decodeMessage(String message) {
 		ArrayList<String> commandData = new ArrayList<String>(); 
 		if (message.length() != 11) {
 			return commandData;
 		} else {
 			if(verifyChecksum(message)){
 				String command = message.substring(0, 2);
 				String params = message.substring(2,10);
 				if (command.equalsIgnoreCase("ms")) {
 					commandData = decodeMoveStraight(params);
 					return commandData;
 				} else if (command.equalsIgnoreCase("ma")) {
 					commandData = decodeMoveArc(params);
 					return commandData;
 				} else if (command.equalsIgnoreCase("tn")) {
 					commandData = decodeTurn(params);
 					return commandData;
 				} else if (command.equalsIgnoreCase("st")) {
 					commandData = decodeStop(params);
 					return commandData;
 				} else if (command.equalsIgnoreCase("rs")) {
 					commandData = decodeReadSensor(params);
 					return commandData;
 				} else if (command.equalsIgnoreCase("ss")) {
 					commandData = decodeSetSpeed(params);
 				} else if (command.equalsIgnoreCase("ra")) {
 
 				} else if (command.equalsIgnoreCase("ec")) {
 
 				}
 			}			
 		}
 		return new ArrayList<String>();
 	}
 
 	public String createACK(){
 		System.out.println("ACK created");
 		String ack = "AK00000000";
 		ack += getChecksum(ack);
		System.out.println("Message Length: " + ack.length());
 		return ack;
 	}
 
 	public String encodeMessage(ArrayList<String> message) {
 		if(message.size() > 1){
 			String encoded = "SD";
 			for(String s: message){
 				encoded += s;
 			}
 			while(encoded.length() < 10){
 				encoded += "0";
 			}
 			return encoded;
 		} else return "0000000000";
 	}
 
 	private boolean isNumeric(String number) {
 		try {
 			int i = Integer.parseInt(number);
 		} catch (NumberFormatException nfe) {
 			return false;
 		}
 		return true;
 	}
 
 	private boolean verifyChecksum(String message) {
 		if(message.length() == 11) {
 			byte[] string = message.getBytes();
 			if(getChecksum(message.substring(0, 10)).equals(message.substring(10))){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private String getChecksum(String message) {
 		int sum = 0;
        String ret;
        byte[] buffer = message.getBytes();
        for (int i = 0; i < buffer.length; i++) {
            sum += (int) buffer[i];
        }
        sum = sum % 256;
        byte[] checksum = new byte[1];
        checksum[0] = (byte) sum;
        ret = new String(checksum);
        System.out.println("CheckSum: " + ret);
        return ret;
 	}
 
 	private ArrayList<String> decodeMoveStraight(String parameters) {
 		ArrayList<String> commandData = new ArrayList<String>();
 		commandData.add("straight");
 		String direction = parameters.substring(0,1);
 		if(direction.equalsIgnoreCase("f")){
 			commandData.add("forward");
 		} else if(direction.equalsIgnoreCase("b")){
 			commandData.add("backward");
 		} else {
 			return new ArrayList<String>();
 		}
 		String distance = parameters.substring(1);
 		if(isNumeric(distance)){
 			commandData.add(distance);
 		}else{
 			return new ArrayList<String>();
 		}
 		return commandData;
 	}
 	private ArrayList<String> decodeTurn(String parameters) {
 		ArrayList<String> commandData = new ArrayList<String>();
 		commandData.add("turn");
 		String direction = parameters.substring(0,1);
 		if(direction.equalsIgnoreCase("r")){
 			commandData.add("right");
 		} else if(direction.equalsIgnoreCase("l")){
 			commandData.add("left");
 		} else {
 			return new ArrayList<String>();
 		}
 		String radius = parameters.substring(1);
 		if(isNumeric(radius)){
 			commandData.add(radius);
 		}else{
 			return new ArrayList<String>();
 		}
 		return commandData;
 	}
 	private ArrayList<String> decodeStop(String parameters) {
 		ArrayList<String> commandData = new ArrayList<String>();
 		commandData.add("stop");
 		String params = parameters.substring(0);
 		if( !(isNumeric(params) && Integer.parseInt(params) == 0) ){
 			return new ArrayList<String>();
 		}
 		return commandData;
 	}
 	private ArrayList<String> decodeMoveArc(String parameters) {
 		ArrayList<String> commandData = new ArrayList<String>();
 		commandData.add("arc");		
 		String direction = parameters.substring(0,1);
 		if(direction.equalsIgnoreCase("f")){
 			commandData.add("forward");
 		} else if(direction.equalsIgnoreCase("b")){
 			commandData.add("backward");
 		} else {
 			return new ArrayList<String>();
 		}
 
 		String turn = parameters.substring(1,2);
 		if(turn.equalsIgnoreCase("r")){
 			commandData.add("right");
 		} else if(turn.equalsIgnoreCase("l")){
 			commandData.add("left");
 		} else {
 			return new ArrayList<String>();
 		}
 
 		String distance = parameters.substring(4,7);
 		if(isNumeric(distance)){
 			commandData.add(distance);
 		}else{
 			return new ArrayList<String>();
 		}
 
 		String radius = parameters.substring(7);
 		if(isNumeric(radius)){
 			commandData.add(radius);
 		}else{
 			return new ArrayList<String>();
 		}
 		return commandData;
 	}
 	private ArrayList<String> decodeReadSensor(String parameters)
 	{
 		ArrayList<String> commandData = new ArrayList<String>();
 		commandData.add("readsensor");		
 		String direction = parameters.substring(0,1);
 		if(direction.equalsIgnoreCase("U")){
 			commandData.add("ultrasonic");
 		} else if(direction.equalsIgnoreCase("T")){
 			commandData.add("touch");
 		} else if(direction.equalsIgnoreCase("M")){
 			commandData.add("sound");
 		} else if(direction.equalsIgnoreCase("L")){
 			commandData.add("light");
 		} else {
 			return new ArrayList<String>();
 		}
 
 		return commandData;
 	}
 	private ArrayList<String> decodeSetSpeed(String parameters)
 	{
 		ArrayList<String> commandData = new ArrayList<String>();
 
 
 		return commandData;	
 	}
 }
