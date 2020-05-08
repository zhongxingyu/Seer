 package com.cahoots.connection.websocket;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.NameValuePair;
 
 import com.cahoots.connection.ConnectionDetails;
 import com.cahoots.connection.http.CahootsHttpClient;
 import com.cahoots.connection.http.CahootsHttpResponseReceivedListener;
 import com.google.gson.Gson;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.AsyncHttpClientConfig;
 import com.ning.http.client.ListenableFuture;
 import com.ning.http.client.websocket.WebSocket;
 import com.ning.http.client.websocket.WebSocketUpgradeHandler;
 
 public class CahootsWebSocket {
 
 	protected ListenableFuture<WebSocket> connection;
 	protected ConnectionDetails connectionDetails;
 	
 	private final AtomicLong sent = new AtomicLong(0);
 
 	public CahootsWebSocket(final ConnectionDetails connectionDetails) {
 		super();
 		this.connectionDetails = connectionDetails;
 	}
 
 	public void connect(final String username, final String password,
 			final String server) {
 		final List<NameValuePair> data = new LinkedList<NameValuePair>();
 		data.add(new NameValuePair("username", username));
 		data.add(new NameValuePair("password", password));
 
 		final CahootsHttpClient client = new CahootsHttpClient();
 		final CahootsHttpResponseReceivedListener received = new CahootsHttpResponseReceivedListener() {
 			@Override
 			public void onReceive(final int statusCode,
 					final HttpMethodBase method) {
 				try {
 					final String response = method.getResponseBodyAsString();
 					if (statusCode == 200) {
 						final String authToken = response;
 
 						connectionDetails.setUsername(username);
 						connectionDetails.setPassword(password);
 						connectionDetails.setAuthToken(authToken);
 						connectionDetails.setServer(server);
 
 						connect(server, authToken);
 					} else {
 						throw new RuntimeException(String.format(
 								"Error connecting to server: %s", response));
 					}
 				} catch (final IOException e) {
 					throw new RuntimeException("Error connecting to server", e);
 				}
 			}
 		};
 
 		client.post(server, "/app/login", data, received);
 	}
 
 	public void disconnect() {
 		if (connection != null) {
 			connection.cancel(true);


 		}
 		connection = null;
 	}
 
 	public void connect(final String server, final String authToken) {
 		try {
 			disconnect();
 			final AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder()
 					.build();
 			final AsyncHttpClient c = new AsyncHttpClient(cf);
 
 			final String uriString = String.format(
 					"ws://%s/app/message?auth_token=%s", server, authToken);
 			final CahootsSocketListener cahootsSocketClient = new CahootsSocketListener(
 					this);
 			connection = c.prepareGet(uriString).execute(
					new WebSocketUpgradeHandler.Builder().addWebSocketListener(
							cahootsSocketClient).build());
 
 			onSuccessfulConnect();
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public String send(final Object obj) {
 		try {
 			final String json = new Gson().toJson(obj);
 			send(json);
 			return json;
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void send(final String message) throws IOException {
 		if (connection != null) {
 			try {
 				connection.get().sendTextMessage(message);
 				sent.incrementAndGet();
 			} catch (final InterruptedException e) {
 				e.printStackTrace();
 			} catch (final ExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void onSuccessfulConnect() {
 	}
 	
 	public void onSuccessfulDisconnect() {
 	}
 	
 	public void onMessage(final String message) {
 	}
 }
