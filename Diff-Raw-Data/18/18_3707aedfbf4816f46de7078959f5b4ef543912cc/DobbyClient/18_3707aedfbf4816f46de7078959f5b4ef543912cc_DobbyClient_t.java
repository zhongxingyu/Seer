 package com.dobby.app;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.dobby.app.comm.BroadcastListener;
 import com.dobby.core.ChangeListener;
 import com.dobby.core.Request;
 import com.dobby.core.Session;
 import com.dobby.core.requests.DeleteRequest;
 import com.dobby.core.requests.IdentityRequest;
 import com.dobby.core.requests.InsertRequest;
 import com.spartango.io.AsyncReadEvent;
 import com.spartango.io.AsyncReadListener;
 import com.spartango.io.AsyncWriteEvent;
 import com.spartango.io.AsyncWriteSender;
 import com.spartango.network.AsyncSocket;
 
 /**
  * 
  * @author anand
  * 
  */
 public class DobbyClient implements AsyncReadListener, AsyncWriteSender,
 		BroadcastListener, ChangeListener {
 	private DobbyServer server;
 
 	private Session session;
 	private AsyncSocket connection;
 
 	public DobbyClient(DobbyServer server, Socket sock, String document)
 			throws IOException {
 		this(server, new AsyncSocket(sock), document);
 	}
 
 	public DobbyClient(DobbyServer server, AsyncSocket socket, String document) {
 		this.server = server;
 		connection = socket;
 		socket.addAsyncSocketListener(this);
 		registerClient();
 	}
 
 	private void registerClient() {
		server.registerClient(connection.hashCode() + "", this);
 	}
 
 	@Override
 	public void onWriteSuccess(AsyncWriteEvent e) {
 		System.out.println("Request Sent Successfully");
 	}
 
 	@Override
 	public void onWriteFailure(AsyncWriteEvent e) {
 		// TODO Decide if a resend is necessary
 	}
 
 	@Override
 	public void onWriterClosed(AsyncWriteEvent e) { // Disconnected
 		// TODO try and reconnect
 		// Exit safely
 		System.out.println("Writer Closed");
 		stop();
 	}
 
 	private void stop() {
 		if (session != null) {
 			server.releaseClient(session.getUserName());
 			session.stop();
 		}
 		connection.close();
 	}
 
 	@Override
 	public void onDataReceived(AsyncReadEvent e) {
 		try {
 			JSONObject message = new JSONObject(e.getData());
 			if (message.has("op"))
 				handleMessage(message);
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	private void handleMessage(JSONObject message) throws JSONException {
 		if (session != null) { // Session must be sync'd!
 			if (message.getString("op").equals("Ins")) {
 				InsertRequest request = InsertRequest.fromJSON(message);
 				handleRequest(request);
 			} else if (message.getString("op").equals("Del")) {
 				DeleteRequest request = DeleteRequest.fromJSON(message);
 				handleRequest(request);
 			} else if (message.getString("op").equals("Id")) {
 				IdentityRequest request = IdentityRequest.fromJSON(message);
 				handleRequest(request);
 			} else if (message.getString("op").equals("username")) {
 				session.removeChangeListener(this);
 				server.releaseClient(session.getUserName());
 				session.setUserName(message.getString("name"));
 				server.registerClient(session.getUserName(), this);
 			}
 		}
 	}
 
 	private void handleRequest(Request request) {
 		// Associate a statevector with this request if it doesnt have one
 		if (request.getStateVector() == null) {
 			request.setStateVector(session.getCurrentState());
 		}
 		// Add this request to the queue
 		session.receiveRequest(request);
 		broadcastRequest(request);
 		// Broadcast this request out
 	}
 
 	@Override
 	public void onReceiveFailed(AsyncReadEvent e) {
 	}
 
 	@Override
 	public void onReaderClosed(AsyncReadEvent e) {
 		this.stop();
 	}
 
 	private void broadcastRequest(Request r) {
 		server.broadcastRequest(r);
 	}
 
 	@Override
 	public void onBroadcastReceived(Request r) {
 		session.receiveRequest(r);
 	}
 
 	@Override
 	public void onProviderClosed() {
 		this.stop();
 	}
 
 	@Override
 	public void syncState(Session newSession) {
 		this.session = newSession;
 		session.addChangeListener(this);
		System.out.println("Acquring new session: " + newSession);
 		JSONObject payload = new JSONObject();
 		try {
 			payload.put("op", "sync");
 			payload.put("text", session.getCurrentText());
 			payload.put("assignedName", session.getUserName());
 			connection.send(payload.toString(), this);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void onRequestApplied(Request r) {
 		String data = r.toLightJSON().toString();
		if (!r.getUser().equals(session.getUserName()))
			connection.send(data, this);
 	}
 
 }
