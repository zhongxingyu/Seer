 package asl;
 
 import infrastructure.exceptions.InvalidClientException;
 import infrastructure.exceptions.InvalidQueueException;
 import infrastructure.exceptions.ServerException;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import asl.network.ITransport;
 import asl.network.SocketTransport;
 
 public class ThorBangMQ {
 	// MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
 	private final static String SendMessageStringFormat = "MSG,%d,%d,%d,%d,%d,%s";
 	
 	private final static String PeekQueueStringFormat = "PEEKQ,%d,%d,%d";
 	private final static String PeekQueueWithSenderStringFormat = "PEEKS,%d,%d,%d,%d";
 	
 	private final static String PopQueueStringFormat = "POPQ,%d,%d,%d";
 	private final static String PopQueueWithSenderStringFormat = "POPS,%d,%d,%d,%d";
 	
 	private final static String CreateQueueStringFormat = "CREATEQUEUE,%s";
 	private final static String RemoveQueueStringFormat = "REMOVEQUEUE,%d";
 	
 	private final static String CreateClientStringFormat = "CREATECLIENT,%s";
 	private final static String RemoveClientStringFormat = "REMOVECLIENT,%d";
 	
 	
 	private ITransport transport;
 	private long userId;
 	private long pollingInterval = 2000;
 	
 	public ThorBangMQ(ITransport transport, long userId){
 		this.transport = transport;
 		this.userId = userId;
 	}
 	
 	public static ThorBangMQ build(String hostname, int port, long userId) throws UnknownHostException, IOException{
 		Socket socket = new Socket(hostname, port);
 		ITransport transport = new SocketTransport(socket);
 		
 		return new ThorBangMQ(transport, userId);
 	}
 	
 	public void setPollingInterval(long milliseconds){ pollingInterval = milliseconds;}
 	public long getPollingInterval() 				 { return pollingInterval; }
 	
 	public void init() throws Exception{
 		String response = this.transport.SendAndGetResponse("HELLO");
 		
 		if (!response.equals("OK")) {
 			throw new Exception("Connection failed! Expected server to respond with OK (server full?). Got: " + response);
 		}
 	}
 	
 	public void stop(){
 		try {
 			transport.disconnect();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void SendMessage(long recieverId, long queueId, long priority, long context, String content)
 				throws IOException, InvalidQueueException, InvalidClientException, ServerException
 			{
 		String response = transport.SendAndGetResponse(String.format(SendMessageStringFormat, 
 													   recieverId,
 													   userId,
 													   queueId,
 													   priority,
 													   context,
 													   content));
 		if (this.responseIsError(response)) {
 			String resp[] = response.split(" ");
 			if (resp[1] == "UNKNOWN") {
 				throw new ServerException();
 			} else if (resp[1] == "QUEUE") {
 				throw new InvalidQueueException(Long.parseLong(resp[2]));
 			} else if (resp[1] == "CLIENT") {
 				throw new InvalidClientException(Long.parseLong(resp[2]));
 			}
 		}
 	}
 	
 	public Message PeekMessage(long queueId, Boolean getByTime) throws IOException, InvalidQueueException, ServerException {
 		String response = transport.SendAndGetResponse(String.format(PeekQueueStringFormat, 
 						userId,
 						queueId,
 						getByTime ? 1 : 0));
 		
 		if (this.responseIsError(response)) {
 			String resp[] = response.split(" ");
 			if (resp[1] == "UNKNOWN") {
 				throw new ServerException();
 			} else if (resp[1] == "QUEUE") {
 				throw new InvalidQueueException(Long.parseLong(resp[2]));
 			}
 		}
 		
 		if(response.startsWith("MSG0")) {
 			return null;
 		}
 		
 		return parseMessage(response);
 	}
 	
 	public Message peekMessageFromSender(long queueId, long sender, Boolean getByTime) throws IOException, InvalidQueueException, InvalidClientException, ServerException {
 		String response = transport.SendAndGetResponse(String.format(PeekQueueWithSenderStringFormat, 
 						userId,
 						queueId,
 						sender,
 						getByTime ? 1 : 0));
 		
 		if (this.responseIsError(response)) {
 			String resp[] = response.split(" ");
 			if (resp[1] == "UNKNOWN") {
 				throw new ServerException();
 			} else if (resp[1] == "QUEUE") {
 				throw new InvalidQueueException(Long.parseLong(resp[2]));
 			} else if (resp[1] == "CLIENT") {
 				throw new InvalidClientException(Long.parseLong(resp[2]));
 			}
 		}
 		
 		if(response.startsWith("MSG0")) {
 			return null;
 		}
 		
 		return parseMessage(response);
 	}
 	
 	public Message PopMessage(long queueId, Boolean getByTime) throws IOException, InvalidQueueException, ServerException {
 		String response = transport.SendAndGetResponse(String.format(PopQueueStringFormat, 
 						userId,
 						queueId,
 						getByTime ? 1 : 0));
 		
 		if (this.responseIsError(response)) {
 			String resp[] = response.split(" ");
 			if (resp[1] == "UNKNOWN") {
 				throw new ServerException();
 			} else if (resp[1] == "QUEUE") {
 				throw new InvalidQueueException(Long.parseLong(resp[2]));
 			}
 		}
 		
 		if(response.startsWith("MSG0")) {
 			return null;
 		}
 		
 		return parseMessage(response);
 	}
 	
 	public Message popMessageFromSender(long queueId, long sender, Boolean getByTime) throws IOException, InvalidQueueException, InvalidClientException, ServerException {
 		String response = transport.SendAndGetResponse(String.format(PopQueueWithSenderStringFormat, 
 						userId,
 						queueId,
 						sender,
 						getByTime ? 1 : 0));
 		
 		if (this.responseIsError(response)) {
 			String resp[] = response.split(" ");
 			if (resp[1] == "UNKNOWN") {
 				throw new ServerException();
 			} else if (resp[1] == "QUEUE") {
 				throw new InvalidQueueException(Long.parseLong(resp[2]));
 			} else if (resp[1] == "CLIENT") {
 				throw new InvalidClientException(Long.parseLong(resp[2]));
 			}
 		}
 		
 		if (response.startsWith("MSG0")) {
 			return null;
 		}
 		
 		return parseMessage(response);
 	}
 	
 	public Long createQueue(String name) throws IOException, ServerException {
 		String response = transport.SendAndGetResponse(String.format(CreateQueueStringFormat, name));
 		if (this.responseIsError(response)) {
 			throw new ServerException();
 		}
 		
 		return Long.parseLong(response);
 	}
 	
 	public boolean removeQueue(long id) throws IOException, ServerException, InvalidQueueException {
 		String response = transport.SendAndGetResponse(String.format(RemoveQueueStringFormat, id));
 		if (this.responseIsError(response)) {
 			String resp[] = response.split(" ");
 			if (resp[1] == "UNKNOWN") {
 				throw new ServerException();
 			} else if (resp[1] == "QUEUE") {
 				throw new InvalidQueueException(Long.parseLong(resp[2]));
 			}
 		}
 		return true;
 	}
 	
 	public long createClient(String name) throws IOException, ServerException {
 		String response = transport.SendAndGetResponse(String.format(CreateClientStringFormat, name));
 		if (responseIsError(response)) {
 			throw new ServerException();			
 		}
 		
 		return Long.parseLong(response);
 	}
 	
 	public boolean removeClient(long clientId) throws IOException, ServerException, InvalidClientException {
 		String response = transport.SendAndGetResponse(String.format(RemoveClientStringFormat, clientId));
 		if (this.responseIsError(response)) {
 			String resp[] = response.split(" ");
 			if (resp[1] == "UNKNOWN") {
 				throw new ServerException();
 			} else if (resp[1] == "CLIENT") {
 				throw new InvalidClientException(Long.parseLong(resp[2]));
 			}
 		}
 		return true;
 	}
 	
 	private Message parseMessage(String msg){
 		String[] msgParts = msg.split(",", 5);
 		
 		long sender = Long.parseLong(msgParts[1]);
		long context = Long.parseLong(msgParts[2]);
 		long id = Long.parseLong(msgParts[3]);
 		String content = msgParts[4];
 		
 		return new Message(sender, context, id, content);
 	}
 	
 	private boolean responseIsError(String response) {
 		return response.startsWith("FAIL");
 	}
 	
 	public ITransport getTransport(){
 		return transport;
 	}
 }
