 package za.ac.sun.cs.intlola.processing;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import za.ac.sun.cs.intlola.Intlola;
 import za.ac.sun.cs.intlola.file.ArchiveFile;
 import za.ac.sun.cs.intlola.file.Const;
 import za.ac.sun.cs.intlola.file.FileUtils;
 import za.ac.sun.cs.intlola.file.IntlolaFile;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 public class Processor {
 
 	private InetSocketAddress address;
 
 	private final ExecutorService executor;
 
 	protected IntlolaMode mode;
 
 	private Project project;
 
 	private InputStream rcv = null;
 	private OutputStream snd = null;
 	private Socket sock = null;
 
 	private String username;
 
 	private Project[] projects;
 
 	/**
 	 * Construct processor with default values.
 	 * 
 	 * @param username
 	 * @param project
 	 * @param mode
 	 * @param address
 	 * @param port
 	 */
 	public Processor(final String username, final IntlolaMode mode,
 			final String address, final int port) {
 		setFields(username, mode, address, port);
 		// Only one thread worker. Interaction with server should be sequential.
 		executor = Executors.newFixedThreadPool(1);
 	}
 
 	public String getAddress() {
 		return address.getHostName();
 	}
 
 	public IntlolaMode getMode() {
 		return mode;
 	}
 
 	public int getPort() {
 		return address.getPort();
 	}
 
 	public Project getProject() {
 		return project;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void handleArchive(final String location, final String zipName) {
 		executor.execute(new ArchiveBuilder(location, zipName));
 		if (mode.isRemote()) {
 			sendFile(new ArchiveFile(zipName));
 			FileUtils.delete(zipName);
 		}
 		logout();
 	}
 
 	private boolean init() {
 		try {
 			sock = new Socket();
 			sock.connect(address, 5000);
 			snd = sock.getOutputStream();
 			rcv = sock.getInputStream();
 			return true;
 		} catch (final IOException e) {
 			Intlola.log(e, "No server detected");
 			return false;
 		}
 	}
 
 	public IntlolaError login(String username, final String password,
 			final String address, final int port) {
 		this.username = username;
 		this.address = new InetSocketAddress(address, port);
 		final JsonObject params = new JsonObject();
 		params.addProperty(Const.REQ, Const.LOGIN);
 		params.addProperty(Const.USER, username);
 		params.addProperty(Const.PASSWORD, password);
 		params.addProperty(Const.MODE, mode.toString());
 		if (!init()) {
 			return IntlolaError.CONNECTION
 					.specific("Could not initialise connection on: "
 							+ address.toString());
 		} else {
 			final byte[] buffer = new byte[1024];
 			try {
 				snd.write(params.toString().getBytes());
 				snd.write(Const.EOF);
 				snd.flush();
 				int count = rcv.read(buffer);
 				final String received = new String(buffer, 0, count);
 				if (received.equals(Const.OK)) {
 					return IntlolaError.SUCCESS;
 				} else {
 					Intlola.log(null, received);
 					return IntlolaError.LOGIN
 							.specific("Login attempt failed with: " + received);
 				}
 			} catch (final IOException e) {
 				Intlola.log(e, "Login error");
 				return IntlolaError.LOGIN
 						.specific("Login attempt failed with: "
 								+ e.getMessage());
 			}
 		}
 	}
 
 	public void logout() {
 		if (mode.isRemote()) {
 			executor.execute(new SessionEnder(sock, snd, rcv));
 		}
 		executor.shutdown();
 	}
 
 	public void sendFile(final IntlolaFile file) {
 		executor.execute(new FileSender(file, sock, snd, rcv));
 	}
 
 	private void setFields(final String username, final IntlolaMode mode,
 			final String address, final int port) {
 		this.username = username;
 		this.mode = mode;
 		this.address = new InetSocketAddress(address, port);
 	}
 
 	public class Project {
 		String Id, Name, User, Lang;
 		long Time;
 	}
 
 	public void loadProjects() throws IOException {
 		final JsonObject params = new JsonObject();
 		params.addProperty(Const.REQ, Const.PROJECTS);
 		final byte[] buffer = new byte[1024];
 		snd.write(params.toString().getBytes());
 		snd.write(Const.EOF);
 		snd.flush();
 		int count = rcv.read(buffer);
 		final String received = new String(buffer, 0, count);
 		Gson gson = new Gson();
 		Project[] p = new Project[1];
		System.out.println(received);
 		projects = gson.fromJson(received, p.getClass());
 	}
 
 	public String[] getProjects() {
 		String[] vals = new String[projects.length];
 		for (int i = 0; i < projects.length; i++) {
 			vals[i] = projects[i].Name + "(" + projects[i].Lang + ")" + " @ "
					+ projects[i].Time;
 		}
 		return vals;
 	}
 
 	public IntlolaError createSubmission(int pindex) {
 		this.project = projects[pindex];
 		if (mode.isRemote()) {
 			final JsonObject params = new JsonObject();
 			params.addProperty(Const.REQ, Const.SUBMISSION);
 			params.addProperty(Const.PROJECT_ID, project.Id);
 			final byte[] buffer = new byte[1024];
 			try {
 				snd.write(params.toString().getBytes());
 				snd.write(Const.EOF);
 				snd.flush();
 				int count = rcv.read(buffer);
 				final String received = new String(buffer, 0, count);
 				if (received.equals(Const.OK)) {
 					return IntlolaError.SUCCESS;
 				} else {
 					Intlola.log(null, received);
 					return IntlolaError.LOGIN
 							.specific("Login attempt failed with: " + received);
 				}
 			} catch (final IOException e) {
 				Intlola.log(e, "Login error");
 				return IntlolaError.LOGIN
 						.specific("Login attempt failed with: "
 								+ e.getMessage());
 			}
 		} else {
 			return IntlolaError.SUCCESS;
 		}
 	}
 
 	public void setMode(IntlolaMode mode) {
 		this.mode = mode;
 	}
 }
