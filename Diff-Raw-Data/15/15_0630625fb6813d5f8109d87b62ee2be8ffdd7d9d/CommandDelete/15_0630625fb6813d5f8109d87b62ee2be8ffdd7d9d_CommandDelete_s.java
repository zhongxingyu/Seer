 import java.io.File;
 import java.util.HashMap;
 
 public class CommandDelete extends Command {
 
 	final String homeDirectory = DIRECTORYHOME;
 	public CommandDelete(HashMap<String, String> hashMap, Server server) {
 		super(hashMap, server);
 	}
 
 	@Override
 	public ReplyMessage execute() {
 		File homeFolder = new File(homeDirectory);
 		File[] listOfFiles = homeFolder.listFiles();
 		File firstFile = null;
 		for (File file : listOfFiles) {
 			if (file.isFile()) {
 				firstFile = file;
 				break;
 			}
 		}
 		if (firstFile == null) {
 			replyMessage.error = true;
 			replyMessage.content = "No file in home directory";
 			return replyMessage;
 		}
 		else {
 			try {
 				String fileName = firstFile.getPath();
 				if (firstFile.delete()) {
 					replyMessage.error = false;
 					replyMessage.content = "Delete first file " + fileName + " in home directory successfully";
 				} else {
 					replyMessage.error = true;
 					replyMessage.content = "Delete failed";
 				}
 	
 			} catch (Exception e) {
 				System.out.println("error reading file");
 				e.printStackTrace();
 	
 			}
 		}
 
 		return replyMessage;
 	}
 
 }
