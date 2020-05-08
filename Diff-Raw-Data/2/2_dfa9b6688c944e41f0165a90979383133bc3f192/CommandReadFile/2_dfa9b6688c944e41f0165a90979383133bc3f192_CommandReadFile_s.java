 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.HashMap;
 
 public class CommandReadFile extends Command {
 	public CommandReadFile(HashMap<String, String> hashMap, Server server) {
 		super(hashMap, server);
 	}
 
 	@Override
 	public ReplyMessage execute() {
 		// TODO Auto-generated method stub
 
 		File file = new File((String) hashMap.get(KEY_FILENAME));
 		if (!file.exists()) {
 			replyMessage.error = true;
 			replyMessage.content = "file not exist";
 			return replyMessage;
 		}
 		try {
 			String stringOffSet = hashMap.get(KEY_OFFSET);
 			int offset = (stringOffSet != null) ? Integer
 					.parseInt(stringOffSet) : 0;
 			String stringLength = hashMap.get(KEY_LENGTH);
 			int lenght = (stringLength != null) ? Integer
					.parseInt(stringLength) : 0;
 			if (file.length() < offset + lenght) {
 				System.out.println("error out of range");
 				replyMessage.error = true;
 				replyMessage.content = "error out of range";
 				return replyMessage;
 			}
 			readFileAsString(file, offset, lenght, replyMessage);
 		} catch (Exception e) {
 			System.out.println("error reading file");
 			e.printStackTrace();
 
 		}
 
 		return replyMessage;
 
 	}
 
 	private void readFileAsString(File filePath, int offset, int lenght,
 			ReplyMessage replyMessage) throws java.io.IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 		char[] buf = new char[100];
 		byte[] bufferByte = new byte[10000];
 		int numRead = 0;
 		int index = 0;
 		while ((numRead = reader.read(buf)) != -1) {
 			for (int i = 0; i < buf.length; i++) {
 				bufferByte[index + i] = (byte) buf[i];
 
 			}
 			index += buf.length;
 		}
 
 		reader.close();
 		byte[] returnByte = new byte[lenght];
 		System.arraycopy(bufferByte, offset, returnByte, 0, lenght);
 		replyMessage.error = false;
 		replyMessage.content = new String(returnByte);
 		// replyMessage.sendByte = returnByte;
 
 	}
 
 }
