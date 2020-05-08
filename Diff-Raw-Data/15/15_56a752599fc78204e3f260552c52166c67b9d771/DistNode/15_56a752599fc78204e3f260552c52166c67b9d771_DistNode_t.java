 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 
 public class DistNode extends RIONode {
 	
 	public String get(String fileName) throws IOException{
 		PersistentStorageReader r = this.getReader(fileName);
 		String file = "";
 		String line = r.readLine();
 		while(line != null){
 			file += line + "\n";
 			line = r.readLine();
 		}
 		return file;
 	}
 	
 	public void write(String fileName, String content, boolean append, boolean force) throws IOException{
 		if(fileExists(fileName) || force)
 			if(!append)
 				putFile(fileName, content, force);
 			else
 				this.getWriter(fileName, true).write(content);
 		else 
 			throw new IOException();
 	}
 	
 	public void create(String fileName) throws IOException {
		//if(!fileExists(fileName))
			this.getWriter(fileName, false).write("");
		//else
			//throw new IOException();
 	}
 	
 	public void delete(String fileName) throws IOException {
 		if(fileExists(fileName))
 			this.getWriter(fileName, true).delete();
 		else
 			throw new IOException();
 	}
 	
 	public void printError(Command c, int errCode){
 		System.out.println(buildErrorString(c.getDest(), this.addr, c.getType(), c.getFileName(), errCode));
 	}
 	
 	public void printError(String msg){
 		System.out.println(msg);
 	}
 	
 	/**
 	 * Prints out an error in the following form Node #{addr}: Error: #{protocol} on server #{server} and file #{fileName} returned error code #{code}"
 	 * @param server server node
 	 * @param client client node
 	 * @param protocol protocol used
 	 * @param fileName name of file in command
 	 * @param code error code returned as defined by Error class
 	 */
 	public static String buildErrorString(int server, int client, int protocol, String fileName, int code){
 		return "Node " + client + ": Error: " + RPCProtocol.protocolToString(protocol) + " on server " + 
 						server + " and file " + fileName + " returned error code " + Error.ERROR_STRINGS[code];
 	}
 	
 	public void printData(String data){
 		System.out.println(data);
 	}
 	
 	public void printSuccess(Command c){
 		System.out.println("Success: Command - " + c + " executed succesfully on file: " + c.getFileName() );
 	}
 	
 	/**
 	 * Handles the special case of replacing a file's contents with PUT
 	 * @param fileName The name of the file to overwrite
 	 * @param content The content to replace the file with.
 	 * @throws IOException 
 	 */
 	private void putFile(String fileName, String content, boolean force) throws IOException{
 		PersistentStorageWriter temp = null;
 		boolean exists = fileExists(fileName);
 		if(exists){
 			PersistentStorageReader oldFile = this.getReader(fileName);
 			temp = getWriter(".temp", false);
 		
 			copyFile(oldFile, temp, fileName + "\n");
 		}
 		
 		PersistentStorageWriter newFile = getWriter(fileName, false);
 		newFile.write(content);
 		
 		if(exists)
 			temp.delete();
 	}
 	
 	/**
 	 * @param fileName The name of the file to check for
 	 * @return true if the file exists, false otherwise
 	 */
 	private boolean fileExists(String fileName){
 		try {
 			this.getReader(fileName);
 			return true;
 		} catch (FileNotFoundException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Copies the file in r to the file in w, appending start to the beginning of the file
 	 * @param r Buffer to read from - source
 	 * @param w Buffer to write to - destination
 	 * @param start String to append to the beginning of the file
 	 * @throws IOException
 	 */
 	private void copyFile(PersistentStorageReader r, PersistentStorageWriter w, String start) throws IOException{
 		String file = start;
 		String line = r.readLine(); 
 		while(line != null){
 			file += line + "\n";
 			line = r.readLine();
 		}
 		w.write(file);
 	}
 	
 	@Override
 	/**
 	 * Starts up the node. Checks for unfinished PUT commands.
 	 */
 	public void start() {
 		if(fileExists(".temp")){
 			try{
 				PersistentStorageReader tempR = this.getReader(".temp");
 				String fileName = tempR.readLine();
 				
 				if (fileName == null)
 					this.getWriter(".temp", false).delete();
 				else{
 					copyFile(tempR, this.getWriter(fileName, false), "");
 					this.getWriter(".temp", false).delete();
 				}
 			}catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	/**
 	 * Handles given command. Prints error if command is not properly formed.
 	 */
 	public void onCommand(String command) {
 		command = command.trim();
 		if(!Pattern.matches("^((create|get|delete) [^ ]+|(put|append) [^ ]+ (\\\".+\\\"|[^ ]+))$", command)){
 			System.out.println("Node: " + this.addr + " Error: Invalid command: " + command);
 			return;
 		}else if(command.equals("pc")){
 			this.CCLayer.printCache();
 		}
 		
 		int indexOfSpace = command.indexOf(' ');
 		int lastSpace = indexOfSpace + 1;
 		String code = command.substring(0, indexOfSpace).trim();
 		
 		indexOfSpace = command.indexOf(' ', lastSpace);
 		if(indexOfSpace < 0)
 			indexOfSpace = command.length();
 		String fileName = command.substring(lastSpace, indexOfSpace).trim();
 		lastSpace = indexOfSpace + 1;
 		
 		if(code.equals("create")){
 			this.CCLayer.create(fileName);
 		}else if(code.equals("get")){
 			this.CCLayer.get(fileName);
 		}else if(code.equals("put")){
 			String content = command.substring(lastSpace);
 			this.CCLayer.put( fileName, content);
 		}else if(code.equals("append")){
 			String content = command.substring(lastSpace);
 			this.CCLayer.append(fileName, content);
 		}else if(code.equals("delete")){
 			this.CCLayer.delete(fileName);
 		}
 	}
 	
 	
 	
 }
 
 
