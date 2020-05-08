 package server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.net.Socket;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.logging.Logger;
 
 import server.model.IDBStorableModel;
 import server.model.ServerActiveUserModel;
 import server.model.ServerInvitationModel;
 import server.model.ServerMeetingModel;
 import server.model.ServerMeetingRoomModel;
 import server.model.ServerUserModel;
 import client.AbstractConnection;
 import client.model.NotificationModel;
 import client.model.TransferableModel;
 
 /**
  * ClientConnection
  * 
  * ClientConnection is the server side counter part to the client side
  * ServerConnection. After a client has connected and has authenticated a
  * ClientConnection thread is created and spawned with the given clients socket,
  * streams and ServerUserModel.
  * 
  * ClientConnection will then enter a read loop reading commands from the
  * client, executing the request and then returning a response back
  * 
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public class ClientConnection extends AbstractConnection implements Runnable {
 
 	private static Logger LOGGER = Logger.getLogger("ClientConnection");
 
 	// Internal variables
 	private final ServerActiveUserModel user;
 	private final ClientConnectionListener handler;
 	private boolean running = true;
 
 	/**
 	 * Create a ClientConnection thread
 	 * 
 	 * @param s
 	 * @param reader
 	 * @param writer
 	 */
 	public ClientConnection(Socket s, BufferedReader reader,
 			BufferedWriter writer, ServerActiveUserModel user,
 			ClientConnectionListener handler) {
 		super(s, writer, reader);
 		this.user = user;
 		this.handler = handler;
 	}
 
 	/**
 	 * Broadcast a model that has been added or updated
 	 * 
 	 * @param model
 	 */
 	public void broadcastModel(TransferableModel model) throws IOException {
 		writeModels(Arrays.asList(model), 0, "BROADCAST");
 	}
 
 	/**
 	 * Write a set of models to the client
 	 * 
 	 * @param models
 	 * @param id
 	 * @param method
 	 * @param smethod
 	 * @throws IOException
 	 */
 	private void writeModels(TransferableModel[] models, int id, String method)
 			throws IOException {
 		writeModels(Arrays.asList(models), id, method, "");
 	}
 
 	/**
 	 * Write a set of models to the client
 	 * 
 	 * @param models
 	 * @param id
 	 * @param method
 	 * @param smethod
 	 * @throws IOException
 	 */
 	private void writeModels(TransferableModel[] models, int id, String method,
 			String smethod) throws IOException {
 		writeModels(Arrays.asList(models), id, method, smethod);
 	}
 
 	/**
 	 * Start thread, listen for incoming request, handle and repeat
 	 */
 	@Override
 	public void run() {
 		LOGGER.info(String.format("Client thread for %s (%s) started", socket
 				.getInetAddress().toString(), user));
 
 		DBConnection db = ServerMain.dbConnection;
 		try {
 
 			writeModels(Arrays.asList((TransferableModel) user), 0, "USER");
 
 			// Read loop, read until we're shutting down or we reach EOF
 			String line = null;
 			while (running && (line = reader.readLine()) != null) {
 
 				// All request starts with a ID followed by the method
 				String[] parts = line.split("\\s+");
 				if (parts.length < 2) {
 					LOGGER.severe("Malformed command recived from client: "
 							+ line);
 					continue;
 				}
 				int id = Integer.parseInt(parts[0]);
 				String method = parts[1];
 
 				if (method.equals("REQUEST") && parts.length > 2) {
 					String smethod = parts[2];
 
 					// Request a filtered user list
 					if (smethod.equals("FILTERED_USERLIST")) {
 						// Allow empty filters
 						String filter = "";
 						if (parts.length == 4) {
 							filter = parts[3];
 						}
 
 						ArrayList<ServerUserModel> matches = ServerUserModel
 								.searchByUsernameAndEmail(filter, filter,
 										ServerMain.dbConnection);
 						writeModels(
 								(TransferableModel[]) matches.toArray(new TransferableModel[matches
 										.size()]), id, method, smethod);
 
 					} else if (smethod.equals("MEETING_LIST")) {
						DateFormat df = DateFormat.getDateTimeInstance();
 						Calendar startDate = Calendar.getInstance();
 						startDate.setTime(df.parse(reader.readLine().trim()));
 						Calendar endDate = Calendar.getInstance();
 						endDate.setTime(df.parse(reader.readLine().trim()));
 						String[] users = reader.readLine().split(",");
 						reader.readLine(); // filler line
 
 						ArrayList<ServerMeetingModel> matches = ServerMeetingModel
 								.searchByUsernamesAndPeriod(users, startDate,
 										endDate, ServerMain.dbConnection);
 						writeModels(
 								(TransferableModel[]) matches.toArray(new TransferableModel[
 								        matches.size()]), id, method, smethod);
 
 					} else if (smethod.equals("MEETING") && parts.length == 4) {
 						int mid = Integer.parseInt(parts[3]);
 
 						ServerMeetingModel match = ServerMeetingModel.findById(
 								mid, ServerMain.dbConnection);
 						writeModels(new TransferableModel[] { match }, id,
 								method, smethod);
 
 					} else if (smethod.equals("AVAILABLE_ROOMS")) {
						DateFormat df = DateFormat.getDateTimeInstance();
 						Calendar from = Calendar.getInstance();
 						from.setTime(df.parse(reader.readLine().trim()));
 						Calendar to = Calendar.getInstance();
 						to.setTime(df.parse(reader.readLine().trim()));
 						reader.readLine(); // filler line
 
 						ArrayList<ServerMeetingRoomModel> matches = ServerMeetingRoomModel
 								.findAvailableRooms(from, to,
 										ServerMain.dbConnection);
 						writeModels(
 								(TransferableModel[]) matches.toArray(new TransferableModel[
 								        matches.size()]), id, method, smethod);
 					}
 
 				} else if (method.equals("STORE")) {
 					// Store model
 					TransferableModel model = readModels().getModels().get(0);
 					if (!(model instanceof IDBStorableModel)) {
 						writeError("Model is not storable", id, method);
 					}
 					try {
 						((IDBStorableModel) model).store(db);
 					} catch (IOException e) {
 						writeError(e.getMessage(), id, method);
 					}
 					writeModels(new TransferableModel[] { model }, id, method,
 							"OK");
 
 					// Broadcast changed model (All but changed notifications)
 					if(!(model instanceof NotificationModel))
 						ServerMain.ccl.broadcastModel(model);
 
 				} else if (method.equals("LOGOUT")) {
 					writeLine(formatCommand(id, "LOGOUT"));
 					disconnect();
 
 				} else if (method.equals("DELETE") && parts.length >= 4) {
 					String smethod = parts[2];
 
 					// Delete meeting
 					if (smethod.equals("MEETING")) {
 						ServerMeetingModel meeting = ServerMeetingModel.findById(Integer.parseInt(parts[3]),
 								ServerMain.dbConnection);
 						meeting.delete(ServerMain.dbConnection);
 
 						writeLine(formatCommand(id, method, meeting.getUMID()));
 					} else if (smethod.equals("INVITATION")
 							&& parts.length == 5) {
 						String username = parts[3];
 						int mid = Integer.parseInt(parts[4]);
 						ServerInvitationModel i = ServerInvitationModel
 								.findByMeetingAndUser(
 										ServerMeetingModel.findById(mid, db), 
 										ServerUserModel.findByUsername(username, db), db);
 						
 						boolean isMeetingOwner = i.getMeeting().getOwner().getUsername().equals(user.getUsername());
 						i.delete(db, isMeetingOwner);
 
 						writeLine(formatCommand(id, method, i.getUMID()));
 					}
 				}
 
 				else {
 					LOGGER.severe("Malformed command recived from client: "
 							+ line);
 					continue;
 				}
 
 			}
 		} catch (ParseException e) {
 			LOGGER.info(String.format(
 					"Client %s (%s) dropped due to malformed time formats",
 					socket.getInetAddress().toString(), user));
 			LOGGER.info(e.toString());
 
 		} catch (IOException e) {
 			// Drop client if we cannot read/write socket
 			LOGGER.info(String.format(
 					"Client %s (%s) dropped due to IOException", socket
 							.getInetAddress().toString(), user));
 			LOGGER.info(e.toString());
 		} finally {
 			disconnect();
 		}
 
 	}
 
 	/**
 	 * Disconnect the client
 	 * 
 	 */
 	public void disconnect() {
 		try {
 			running = false;
 			handler.removeClient(user);
 			reader.close();
 			writer.close();
 			socket.close();
 		} catch (IOException e) {
 			// Ignore
 		}
 	}
 
 	/**
 	 * Read a list of models off stream
 	 * 
 	 */
 	@Override
 	protected ModelEnvelope readModels() throws IOException {
 		return new ModelEnvelope(reader, true);
 	}
 
 }
