 package ru.allgage.geofriend.server;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.Random;
 
 /**
  * Client socket handler thread.
  */
 public class SocketHandler implements Runnable {
 	final String ERROR_HEADER = "error";
 	final String LOGGED_IN_HEADER = "logged in";
 
 	final Socket socket;
 	final UserDAO userDAO;
 
 	User loggedUser;
 
 	/**
 	 * Creates a socket handler.
 	 *
 	 * @param socket  client socket.
 	 * @param userDAO user data access object.
 	 */
 	SocketHandler(Socket socket, UserDAO userDAO) {
 		this.socket = socket;
 		this.userDAO = userDAO;
 		System.out.println(socket.getInetAddress().toString());
 	}
 
 	/**
 	 * Runs the handler.
 	 */
 	@Override
 	public void run() {
 		try (Socket socket = this.socket;
 			 DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
 			 DataInputStream din = new DataInputStream(socket.getInputStream())) {
 
 			String auth = din.readUTF();
 			if (auth.equals("login")) {
 				String user = din.readUTF();
 				String pass = din.readUTF();
 
 				loggedUser = userDAO.load(user, pass);
 				if (loggedUser == null) {
 					writeError(dout, "invalid login or password");
 					return;
 				}
 			} else if (auth.equals("register")) {
 				String user = din.readUTF();
 				String pass = din.readUTF();
 				String email = din.readUTF();
 
 				loggedUser = userDAO.create(user, pass, email);
 				if (loggedUser == null) {
 					writeError(dout, "cannot register user");
 					return;
 				}
 			} else {
 				writeError(dout, "invalid login sequence");
 				return;
 			}
 
 			writeLoggedIn(dout);
 
 			String str = din.readUTF(); // TODO: investigate what is it.
 			Random rnd = new Random();
 
 			while (true) {
 				writeStatus(dout, "Twice", rnd.nextInt(180) - 90, rnd.nextInt(360) - 180, "lol");
 				din.readUTF(); // TODO: investigate what is it.
 				Thread.sleep(2000);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Writes one or many strings to the stream, flushing it afterwards.
 	 *
 	 * @param stream   data stream.
 	 * @param messages messages to write.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeMessages(DataOutputStream stream, String... messages) throws IOException {
 		for (String message : messages) {
 			stream.writeUTF(message);
 		}
 
 		stream.flush();
 	}
 
 	/**
 	 * Writes the proper-formed error to the data stream.
 	 *
 	 * @param stream  data stream.
 	 * @param message error message.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeError(DataOutputStream stream, String message) throws IOException {
 		writeMessages(stream, ERROR_HEADER, message);
 	}
 
 	/**
 	 * Writes the "logged in" message to the data stream.
 	 *
 	 * @param stream data stream.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeLoggedIn(DataOutputStream stream) throws IOException {
 		writeMessages(stream, LOGGED_IN_HEADER);
 	}
 
 	/**
 	 * Writes the status message to the data stream.
 	 *
 	 * @param stream    data stream.
 	 * @param userName  user name of the status owner.
 	 * @param latitude  latitude of the message.
	 * @param longitude longtitude of the message.
 	 * @param message   the message text.
 	 * @throws IOException thrown in case something going wrong.
 	 */
 	private void writeStatus(
 			DataOutputStream stream,
 			String userName,
 			double latitude,
 			double longitude,
 			String message) throws IOException {
 		// TODO: Proper format double.
 		String datagram = String.format("%s:%d:%d:%s", userName, (int) latitude, (int) longitude, message);
 		writeMessages(stream, datagram);
 	}
 }
