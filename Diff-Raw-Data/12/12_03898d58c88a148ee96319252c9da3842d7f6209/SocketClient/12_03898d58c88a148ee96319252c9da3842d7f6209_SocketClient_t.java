 package com.chess.genesis;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 class SocketClient
 {
 	public static boolean isLoggedin = false;
 
 	private static String loginHash = null;
 	private static Socket sock = new Socket();
 	private static InputStream input;
 	private static OutputStream output;
 
 	public SocketClient()
 	{
 		NetActive.inc();
 	}
 
 	public static String getHash() throws SocketException, IOException
 	{
 		if (loginHash == null)
 			connect();
 		return loginHash;
 	}
 
 	private static void connect() throws SocketException, IOException
 	{
 		if (sock.isConnected())
 			return;
 		hard_disconnect();
 		sock.connect(new InetSocketAddress("genesischess.com", 8338));
 		input = sock.getInputStream();
 		output = sock.getOutputStream();
 
 		final byte[] buff = new byte[1440];
 		input.read(buff);
 		loginHash = (new String(buff)).trim();
 	}
 
 	public static void hard_disconnect()
 	{
 	try {
 		sock.close();
 		sock = new Socket();
 		loginHash = null;
 		isLoggedin = false;
 	} catch (IOException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	public static void disconnect()
 	{
 		NetActive.dec();
 	}
 
 	public static void write(final JSONObject data) throws SocketException, IOException
 	{
 		connect();
 
 		final String str = data.toString() + "\n";
 
 		output.write(str.getBytes());
 	}
 
 	public static JSONObject read() throws SocketException, IOException, JSONException
 	{
 		connect();
 
		int offset = 0, read;
		final byte[] buff = new byte[4 * 1428];

		// TODO: InputStream reads in 1024 chunks
		// if server responce is exactly (X * 1024) then program will hang
		do {
			read = input.read(buff, offset, buff.length - offset);
			offset += read;
		} while (read == 1024);
 
 		return (JSONObject) (new JSONTokener(new String(buff))).nextValue();
 	}
 }
