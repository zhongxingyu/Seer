apackage com.google.gwt.dist.compiler.agent.communicator.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import com.google.gwt.dist.comm.CommMessage;
 import com.google.gwt.dist.comm.CommMessageResponse;
 import com.google.gwt.dist.comm.ProcessingStateResponse;
 import com.google.gwt.dist.comm.ReturnResultResponse;
 import com.google.gwt.dist.comm.CommMessage.CommMessageType;
 import com.google.gwt.dist.comm.SendDataPayload;
 import com.google.gwt.dist.compiler.agent.SessionManager;
 import com.google.gwt.dist.compiler.agent.communicator.Communicator;
 import com.google.gwt.dist.compiler.agent.events.DataReceivedListener;
 import com.google.gwt.dist.util.ZipCompressor;
 
 public class CommunicatorImpl implements Communicator {
 
 	private ZipCompressor compressor;
 	private Set<DataReceivedListener> dataReceivedListeners;
 	private SessionManager sessionManager;
 
 	private static final Logger logger = Logger
 			.getLogger(CommunicatorImpl.class.getName());
 
 	public CommunicatorImpl() {
 		this.dataReceivedListeners = new HashSet<DataReceivedListener>();
 		compressor = new ZipCompressor();
 		compressor.setExcludePattern(Pattern
 				.compile("bin|\\.settings\\.classpath\\.project"));
 	}
 
 	/**
 	 * Closes a connection towards a client.
 	 */
 	public void closeConnection(Socket client) {
 		try {
 			client.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Notify DataReceivedListeners about data transfer being finished.
 	 */
 	public void dataReceived(SendDataPayload receivedData) {
 		for (DataReceivedListener l : dataReceivedListeners) {
 			l.onDataReceived(receivedData);
 		}
 	}
 
 	public byte[] getData(Socket client) {
 		logger.log(Level.INFO, "Getting data from client: "
 				+ client.getInetAddress());
		
 		ByteArrayOutputStream receivedObject = null;
 		try {
 			InputStream is = client.getInputStream();
 
 			// Read contents of the stream and store it into a byte array.
 			byte[] buff = new byte[512];
 			int bytesRead = 0;
 			receivedObject = new ByteArrayOutputStream();
 			while ((bytesRead = is.read(buff)) > -1) {
 				receivedObject.write(buff, 0, bytesRead);
 			}
			
 		} catch (IOException e) {
 			logger.log(Level.SEVERE,
 					"There was a problem while getting data from client "
 							+ client.getInetAddress());
 		}
 		return receivedObject.toByteArray();
 	}
 
 	public SessionManager getSessionManager() {
 		return this.sessionManager;
 	}
 
 	/**
 	 * Processes the incoming CommMessage and returns a modified one. CANDIDATE
 	 * FOR REMOVAL.
 	 * 
 	 * @param message
 	 *            The message to process.
 	 * @return Updated message.
 	 */
 	public CommMessage<ProcessingStateResponse> processCommMessage(
 			CommMessage<ProcessingStateResponse> message) {
 		ProcessingStateResponse response = new ProcessingStateResponse();
 		if (message.getCommMessageType() == CommMessageType.QUERY) {
 			((ProcessingStateResponse) response)
 					.setCurrentState(this.sessionManager.getProcessingState());
 			message.setResponse(response);
 		}
 		message.setResponse(response);
 		return message;
 	}
 
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 
 	public void sendData(byte[] data, Socket client) {
 		OutputStream os;
 		try {
 			os = client.getOutputStream();
 			os.write(data);
 			client.shutdownOutput();
 		} catch (IOException e) {
 			logger.log(Level.SEVERE,
 					"There was a problem while sending data to client "
 							+ client.getInetAddress());
 		}
 	}
 
 	@Override
 	public void serveClient(Socket client) {
 		try {
 			logger.log(Level.INFO, "Accepted a connection from: "
 					+ client.getInetAddress());
 			OutputStream os = client.getOutputStream();
 			InputStream is = client.getInputStream();
 
 			// Read contents of the stream and store it into a byte array.
 			byte[] buff = new byte[512];
 			int bytesRead = 0;
 			ByteArrayOutputStream receivedObject = new ByteArrayOutputStream();
 			while ((bytesRead = is.read(buff)) > -1) {
 				receivedObject.write(buff, 0, bytesRead);
 			}
 
 			// Check if received stream is CommMessage or not.
 			CommMessage<ProcessingStateResponse> commMessage = getCommMessage(receivedObject);
			if (commMessage != null) {
				commMessage = processCommMessage(commMessage);
				commMessage.setResponse(decideResponse(commMessage));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(commMessage);
				os.write(bos.toByteArray());
			} else {
				dataReceived(receivedObject.toByteArray());
			}
 			client.shutdownOutput();
 			is.close();
 			os.close();
 			client.close(); // ???
 		} catch (IOException e) {
 			logger.log(Level.SEVERE, e.getMessage());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private CommMessage<ProcessingStateResponse> getCommMessage(
 			ByteArrayOutputStream baos) {
 		try {
 			ByteArrayInputStream bis = new ByteArrayInputStream(baos
 					.toByteArray());
 			ObjectInputStream ois = new ObjectInputStream(bis);
 			return (CommMessage<ProcessingStateResponse>) ois.readObject();
 		} catch (IOException e) {
 		} catch (ClassNotFoundException e) {
 		}
 		return null;
 	}
 
 	@Override
 	public void addDataReceivedListener(DataReceivedListener listener) {
 		dataReceivedListeners.add(listener);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends CommMessageResponse> T decideResponse(
 			CommMessage<T> message) {
 		T responseToReturn = null;
 		switch (message.getCommMessageType()) {
 		case DELIVERY_DATA:
			
 			break;
 		case ECHO:
 			responseToReturn = message.getResponse();
 			break;
 		case QUERY:
 			responseToReturn = (T) new ProcessingStateResponse(
 					this.sessionManager.getProcessingState());
 			break;
 		case RETURN_RESULT:
 			responseToReturn = (T) new ReturnResultResponse();
 			break;
 		}
 		return responseToReturn;
 	}
 }
