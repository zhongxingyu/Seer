 
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ListIterator;
 import java.util.Vector;
 
 import node.rpc.RPCNode;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class StorageSystemServer extends RPCNode {
 	
 	private static final String ERROR_MESSAGE_FORMAT = 
			"Error: %s on server %d and file %s returned error code %s";
 	
 	private static final int FILE_DOES_NOT_EXIST = 10;	
 	private static final int FILE_ALREADY_EXISTS = 11;
 	private static final int FILE_TOO_LARGE = 30;			
 	
 	/**
 	 * Verifies the name of the method called and calls the appropriate method in this class. If more parameters than
 	 * expected in passed, the extra parameters will just get dropped.
 	 */
 	@Override
 	protected void onMethodCalled(int from, String methodName, Vector<String> params) {
 		
 		if (isServer())
 		{
 			executeServerCommand(from, methodName, params);
 		}
 		else
 		{
 			endClientCommand(from, methodName, params);
 		}
 	}	
 	
 	public void createFile (int from, String fileName)
 	{		
 		Vector<String> params = new Vector<String>();
 		
 		if (Utility.fileExists(this, fileName))
 		{				
 			params.add("error");
 			params.add(String.format(ERROR_MESSAGE_FORMAT, "create", this.addr, fileName, FILE_ALREADY_EXISTS));								
 		}
 		else
 		{
 			try 
 			{			
 					this.getWriter(fileName, false);
 			} 
 			catch (IOException e) 
 			{
 			}
 		}
 		
 		callMethod(from, "endCommand", params);
 	}
 	
 	public void getFile(int from, String fileName)
 	{
 		Vector<String> params = new Vector<String>();
 		
 		if (!Utility.fileExists(this, fileName))
 		{
 			params.add("error");
 			params.add(String.format(ERROR_MESSAGE_FORMAT, "get", this.addr, fileName, FILE_DOES_NOT_EXIST));
 		}
 		else
 		{
 			try
 			{
 				BufferedReader bufferedReader = this.getReader(fileName);
 				
 				StringBuffer contents = new StringBuffer();
 				
 				String line = bufferedReader.readLine();  
 				while (line != null)
 				{
 					contents.append(line);
 					
 					line = bufferedReader.readLine();
 				}
 				
 				bufferedReader.close();
 				
 				params.add(contents.toString());			
 			}
 			catch (IOException e) 
 			{
 				//TODO-Livar: How to deal with this? Same goes for other methods.
 			}
 		}
 		
 		callMethod(from, "endGetFile", params);
 	}
 	
 	//TODO-livar: verify that the file size doesn't exceed the length of the message minus the headers
 	public void putFile(int from, String fileName, String contents)
 	{
 		Vector<String> params = new Vector<String>();
 		
 		if (!Utility.fileExists(this, fileName))
 		{
 			params.add("error");
 			params.add(String.format(ERROR_MESSAGE_FORMAT, "put", this.addr, fileName, FILE_DOES_NOT_EXIST));
 		}
 		else 
 		{
 			try {
 				updateFileContents(fileName, contents, false);
 			} catch (IOException e) {
 			}
 		}		
 		
 		callMethod(from, "endCommand", params);
 	}
 	
 	//TODO-livar: verify that the file size doesn't exceed the length of the message minus the headers
 	public void appendToFile(int from, String fileName, String contents)
 	{
 		Vector<String> params = new Vector<String>();
 		
 		if (!Utility.fileExists(this, fileName))
 		{
 			params.add("error");
 			params.add(String.format(ERROR_MESSAGE_FORMAT, "append", this.addr, fileName, FILE_DOES_NOT_EXIST));
 		}
 		else 
 		{
 			try {
 				updateFileContents(fileName, contents, true);
 			} catch (IOException e) {
 			}
 		}
 		
 		callMethod(from, "endCommand", params);
 	}
 
 	public void deleteFile(int from, String fileName)
 	{
 		Vector<String> params = new Vector<String>();
 		
 		if (!Utility.fileExists(this, fileName))
 		{
 			params.add("error");
 			params.add(String.format(ERROR_MESSAGE_FORMAT, "delete", this.addr, fileName, FILE_DOES_NOT_EXIST));
 		}
 		else 
 		{
 			try {
 				PersistentStorageWriter writer = this.getWriter(fileName, false);
 				
 				writer.delete();
 			} catch (IOException e) {
 			}
 		}
 		
 		callMethod(from, "endCommand", params);
 	}
 	
 	public void beginCreateFile(int targetSender, String fileName) 
 	{
 		Vector<String> params = new Vector<String>();
 		
 		params.add(fileName);
 		
 		callMethod(targetSender, "create", params);		
 	}
 	
 	public void beginGetFile(int targetSender, String fileName) 
 	{
 		Vector<String> params = new Vector<String>();
 		
 		params.add(fileName);
 		
 		callMethod(targetSender, "get", params);
 	}
 	
 	public void beginPutFile(int targetSender, String fileName, String contents) 
 	{
 		Vector<String> params = new Vector<String>();
 		
 		params.add(fileName);
 		params.add(contents);
 		
 		callMethod(targetSender, "put", params);
 	}
 	
 	public void beginAppendFile(int targetSender, String fileName, String contents) 
 	{
 		Vector<String> params = new Vector<String>();
 		
 		params.add(fileName);
 		params.add(contents);
 		
 		callMethod(targetSender, "append", params);
 	}
 	
 	public void beginDeleteFile(int targetSender, String fileName) 
 	{
 		Vector<String> params = new Vector<String>();
 		
 		params.add(fileName);
 		
 		callMethod(targetSender, "delete", params);		
 	}
 	
 	@Override
 	protected void onConnectionAborted(int endpoint)
 	{
 		// prints error
 		error(String.format("NODE %1: The connection to %2 timedout.", addr, endpoint));
 		
 		endCommand();
 	}	
 	
 	/**
 	 * Parses the command received by onCommand and actually executes it.
 	 * 
 	 * @param command - the command to be executed.
 	 */
 	@Override
 	protected void executeClientCommand(String command)
 	{
 		String[] parts = command.split("\\s+");
 		
 		// at least command target fileName
 		if (parts.length >= 3)
 		{
 			String commandName = parts[0];
 			int targetSender = Integer.parseInt(parts[1]);
 			String fileName = parts[2];
 			
 			if (commandName.equals("create"))
 			{
 				beginCreateFile(targetSender, fileName);
 			}
 			else if (commandName.equals("get"))
 			{
 				beginGetFile(targetSender, fileName);
 			}
 			else if (commandName.equals("put") || commandName.equals("append"))
 			{							
 				if (parts.length < 4)
 				{
 					warn(String.format("Invalid number of argumetns for 'put' or 'append'. Expected at least 4. Found: %1", parts.length));
 					endCommand();
 				}
 				
 				StringBuilder contents = new StringBuilder(parts.length - 3);
 				
 				for (int i = 3; i < parts.length; i++) {
 					contents.append(parts[i]);
 				}
 				
 				if (commandName.equals("put"))
 				{
 					beginPutFile(targetSender, fileName, contents.toString());
 				}
 				else
 				{
 					beginAppendFile(targetSender, fileName, contents.toString());
 				}
 			}
 			else if (commandName.equals("delete"))
 			{
 				beginDeleteFile(targetSender, fileName);
 			}
 			else
 			{
 				warn(String.format("Unknown command: %1", commandName));
 			}
 		}
 		else
 		{
 			// Will remove this command from the queue and executes the next one, if any
 			endCommand();
 		}
 	}
 	
 	/**
 	 * Executes commands directed to the server version of the Storage System.
 	 * 
 	 * @param from - who invoked the server.
 	 * @param methodName - the method being invoked.
 	 * @param params - any parameters to the method.
 	 */
 	private void executeServerCommand(int from, String methodName,
 			Vector<String> params) {
 		if (params.size() < 1)
 		{
 			error(String.format("Invalid number of arguments passed. Expected 1, found %1", params.size()));
 		}
 		
 		String fileName = params.get(0);
 		
 		if (methodName.equals("create"))
 		{			
 			createFile(from, fileName);
 		}
 		else if (methodName.equals("get"))
 		{			
 			getFile(from, fileName);
 		}
 		else if (methodName.equals("put") || methodName.equals("append"))
 		{
 			if (params.size() < 2)
 			{
 				error(String.format("Invalid number of arguments passed. Expected 1, found %1", params.size()));
 			}
 			
 			StringBuilder contents = new StringBuilder();
 			ListIterator<String> iterator = params.listIterator(1);
 			while (iterator.hasNext())
 			{
 				contents.append(iterator.next());
 			}
 			
 			if (methodName.equals("put"))
 			{
 				putFile(from, fileName, contents.toString());
 			}
 			else 
 			{
 				appendToFile(from, fileName, contents.toString());
 			}
 		}
 		else if (methodName.equals("delete"))
 		{
 			deleteFile(from, fileName);
 		}
 		else
 		{
 			error(String.format("Unknown command: %1", methodName));
 		}
 	}
 	
 	private void endClientCommand(int from, String methodName, Vector<String> params)
 	{
 		if (params.size() == 2 && params.get(0).equals("error"))
 		{
			error(String.format("NODE %d: %s", addr, params.get(1)));
 		}		
 		else if (methodName.equals("endGetFile"))
 		{
 			info(params.get(0));
 		}
 		
 		// Will remove this command from the queue and executes the next one, if any
 		endCommand();
 	}	
 }
