 package li.rudin.arduino.core.test.mock;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import li.rudin.arduino.core.util.Message;
 import li.rudin.arduino.core.util.MessageParser;
 
 public class ArduinoEthernetDeviceServer implements Runnable
 {
 
 	public ArduinoEthernetDeviceServer(int port)
 	{
 		this.port = port;
 	}
 
 	private final Map<String, String> valueMap = new HashMap<>();
 	
 	private final int port;
 	private boolean run = false;
 	private ServerSocket serversocket;
 
 	private List<Client> clients = new CopyOnWriteArrayList<>();
 
 	public void start() throws IOException
 	{
 		run = true;
 		serversocket = new ServerSocket(port);
 		new Thread(this).start();
 	}
 
 	public void stop() throws IOException
 	{
 		run = false;
 		serversocket.close();
 		
 		for (Client c: clients)
 			c.stop();
 	}
 
 	public void send(String key, String value) throws IOException
 	{
 		for (Client c: clients)
 			c.send(key, value);
 	}
 
 	@Override
 	public void run()
 	{
 		while(run)
 		{
 			try
 			{
 				Socket socket = serversocket.accept();
 				new Client(socket);
 			}
 			catch (Exception e)
 			{
 				return;
 			}
 		}
 	}
 	
 	public Stack<Message> getRxMessages()
 	{
 		return rxMessages;
 	}
 
 	public Map<String, String> getValueMap()
 	{
 		return valueMap;
 	}
 
 	private final Stack<Message> rxMessages = new Stack<>();
 
 	class Client implements Runnable
 	{
 
 		public Client(Socket socket) throws IOException
 		{
 			input = socket.getInputStream();
 			output = socket.getOutputStream();
 
 			clients.add(this);
 			new Thread(this).start();
 		}
 
 		private final InputStream input;
 		private final OutputStream output;
 
 		public void send(String key, String value) throws IOException
 		{
 			output.write( new Message(key, value).toString().getBytes() );
 		}
 		
 		public void stop() throws IOException
 		{
 			input.close();
 		}
 
 		@Override
 		public void run()
 		{
 			byte[] buffer = new byte[1024];
 
 			while(run)
 			{
 				try
 				{
 					int count = input.read(buffer);
 
 					List<Message> list = MessageParser.parse(buffer, count);
 					
 					for (Message msg: list)
 					{
						if (msg.value.equals("get") && valueMap.containsKey(msg.key))
 						{
 							//Get message
 							send(msg.key, valueMap.get(msg.key));
 						}
 						else
 							rxMessages.push(msg);
 					}
 				}
 				catch (Exception e)
 				{
 					return;
 				}
 			}
 		}
 
 	}
 
 
 }
