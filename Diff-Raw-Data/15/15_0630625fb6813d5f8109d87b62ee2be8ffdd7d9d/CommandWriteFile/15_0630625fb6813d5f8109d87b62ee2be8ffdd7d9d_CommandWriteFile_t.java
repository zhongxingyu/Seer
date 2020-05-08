 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class CommandWriteFile extends Command {
 	final String homeDirectory = DIRECTORYHOME;
 	public CommandWriteFile(HashMap<String, String> hashMap, Server server) {
 		super(hashMap, server);
 	}
 
 	public ReplyMessage execute() {
 
 		File file = new File(homeDirectory+(String) hashMap.get(KEY_FILENAME));
 		if (!file.exists()) {
 			replyMessage.error = true;
 			replyMessage.content = "file not exist";
 			return replyMessage;
 		}
 
 		try {
 			int offset = Integer.parseInt((String) hashMap.get(KEY_OFFSET));
 			String insertText = (String) hashMap.get(KEY_CONTENT);
 
 			if (file.length() < offset) {
 				System.out.println("error out of range");
 				replyMessage.error = true;
 				replyMessage.content = "error out of range";
 				return replyMessage;
 			}
 
 			writeFileAsString(file, offset, insertText, replyMessage);
 		} catch (Exception e) {
 			System.out.println("error reading file");
 			e.printStackTrace();
 		}
 		return replyMessage;
 
 	}
 
 	/*
 	 * write to file
 	 */
 	private void writeFileAsString(File filePath, int offset, String text,
 			ReplyMessage replyMessage) throws java.io.IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 		char[] buf = new char[100];
 		char[] bufferByte = new char[1000];
 		int numRead = 0;
 		int index = 0;
 		long fileLength = filePath.length();
 
 		// read the current content of the file
 		while ((numRead = reader.read(buf)) != -1) {
 			for (int i = 0; i < buf.length; i++) {
 				bufferByte[index + i] = buf[i];
 
 			}
 			index += buf.length;
 		}
 
 		reader.close();
 		FileWriter fstream = new FileWriter(filePath, false);
 		BufferedWriter out = new BufferedWriter(fstream);
 
 		// insert the data to the position
 		out.write(bufferByte, 0, offset);
 		out.write(text);
 		out.write(bufferByte, offset, (int) (fileLength - offset));
 		out.close();
 
 		replyMessage.error = false;
 		replyMessage.content = "Successfull add";
 		System.out.println("successfull add");
 		sendToRegisterClient(filePath.getPath());
 	}
 
 	/*
 	 * acknowledge the monitored client for this file
 	 */
 	public void sendToRegisterClient(String filePath) throws IOException {
 		RegisterClientList clientList = CommandRegister.registerFileList
 				.get(filePath);
 		if (clientList == null) {
 			System.out.println("don't have the client list for this file");
 			return;
 		} else {
 			// update the monitored client list
 			if (clientList.refresh()) {
 				// list still have element
 				System.out.println("have a client register for this file");
 				for (int i = 0; i < clientList.size(); i++) {
 					RegisterClient client = clientList.get(i);
 					server.sendMessage(acknowledgeMessage(filePath), client.ip,
 							client.port);
 					System.out.println("a client register for this file "
 							+ client.ip + " port " + client.port);
 				}
 			} else {
 				// list is empty
 				System.out.println("client list is empty");
 				CommandRegister.registerFileList.remove(filePath);
 			}
 		}
 	}
 
 	/*
 	 * acknowledge message
 	 */
 	public String acknowledgeMessage(String fileName) {
 		StringBuilder sb = new StringBuilder();
		fileName = fileName.substring(homeDirectory.length(),fileName.length());
 		sb.append(Constants.KEY_STATUS + ":" + Constants.VAL_STATUS_OK + DELIM);
 		sb.append(Constants.KEY_CONTENT + ":" + "file " + fileName
 				+ " has been changed");
 		return sb.toString();
 	}
 
 }
