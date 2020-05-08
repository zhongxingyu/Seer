 package application;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import application.filter.Block;
 
 public class FilterHandler implements ConnectionHandler {
 
 	RequestFilter rf = RequestFilter.getInstance();
 	private DinamicProxyConfiguration configuration = DinamicProxyConfiguration
 			.getInstance();
 
 	static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
 			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
 			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
 			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 
 	private String user;
 	private String pass;
 
 	@Override
 	public void handle(final Socket s) throws IOException {
 		// InputStream in = s.getInputStream();
 		// OutputStream out = s.getOutputStream();
 		final BufferedReader fromClient = new BufferedReader(
 				new InputStreamReader(s.getInputStream()));
 		final PrintWriter toClient = new PrintWriter(s.getOutputStream(), true);
 		// byte[] receiveBuf = new byte[BUFSIZE]; // Receive buffer
 		String response;
 		this.user = this.configuration.getUsername();
 		this.pass = this.configuration.getPassword();
 		boolean auth = false;
 		do {
 			toClient.println("USER:");
 			response = fromClient.readLine();
 			if (response.contains(this.user)) {
 				toClient.println("PASS:");
 				response = fromClient.readLine();
 				if (response.contains(this.pass)) {
 					auth = true;
 				} else {
 					toClient.println("Wrong Username or Password");
 				}
 			}
 		} while (!response.equals("BYE!") && !auth);
 		// Receive until client closes connection
 		do {
 			response = this.parse(fromClient.readLine());
 			toClient.println(response);
 		} while (!response.equals("BYE!"));
 		s.close();
 	}
 
 	public boolean isBrowser(final String browser) {
 		return browser.equals("FIREFOX") || browser.equals("EXPLORER")
 				|| browser.equals("CHROME") || browser.equals("SAFARI");
 	}
 
 	public boolean isOS(final String OS) {
 		return OS.equals("LINUX") || OS.equals("WINDOWS")
 				|| OS.equals("MAC_OS_X");
 	}
 
 	public boolean isIP(final String IP) {
 
 		final Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
 		final Matcher matcher = pattern.matcher(IP);
 		return matcher.matches();
 
 		// return IP
 		// .matches("^.[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}");
 		// return IP.matches("%d.%d.%d.%d") || IP.matches("%d.*.*.*")
 		// || IP.matches("%d.%d.*.*") || IP.matches("%d.%d.%d.*");
 	}
 
 	public boolean isURI(final String URI) {
 		return URI.startsWith("http://");
 
 	}
 
 	private String parse(final String request) {
 
 		final String[] parsedString = request.split(" ");
 		String command = null;
 		String message = null;
 		Block block = null;
 		if (parsedString.length >= 4 && parsedString.length <= 5
 				&& parsedString[0].equals("FOR")) {
 			if (parsedString.length == 5) {
 				command = parsedString[2] + " " + parsedString[3] + " "
 						+ parsedString[4];
 			} else {
 				command = parsedString[2] + " " + parsedString[3];
 			}
 			if (this.isBrowser(parsedString[1])) {
 				block = this.rf.getBrowserBlock(parsedString[1]);
 				message = this.parseAction(command, block);
 			} else if (this.isOS(parsedString[1])) {
 				block = this.rf.getOsBlock(parsedString[1]);
 				message = this.parseAction(command, block);
 			} else if (this.isIP(parsedString[1])) {
 				try {
 					block = this.rf.getIpBlock(parsedString[1]);
 					message = this.parseAction(command, block);
 				} catch (final UnknownHostException e) {
 					return "IP invalida";
 				}
 			} else if (parsedString[1].equals("ALL")) {
 				block = this.rf.getSimpleBlock();
 				message = this.parseAction(command, block);
 			}
 		} else if (parsedString[0].equals("GET")) {
 			message = this.parseAction(request, block);
 		}
 		if (message == null) {
 			return "Comando invalido";
 		}
 		return message;
 
 	}
 
 	public String parseAction(final String request, final Block block) {
 		// if (request.equals("HELP")) {
 		// return
 		// "Manual for usage:\64Type any command from the following list:\64\tBLOCK ACCESS blocks every access from the proxy.\64\tUNLOCK ACCESS grants access\64\tL33T ON turns l33t mode on\64\tL33T OFF turns l33t mode off\64\tBLOCK IP [ip] blocks the given [ip] or group of ip's\64\tUNLOCK IP [ip] unlocks the given [ip] or group of ip's\64\tBLOCK URI [uri] blocks the given [uri] or regular expression for uri\64\tUNLOCK URI [uri] unlocks the given uri or regular expression for uri's\64\tSET MAXSIZE [size] sets a max quantity of bytes that can pass throught the proxy, set on 0 for unlimited amount\64\tIMAGES ON turns on the flipping for images\64\tIMAGES OFF turns off the flipping for images\64\tBLOCK MEDIATYPE [media type] blocks the given [media type]\64\tUNLOCK MEDIATYPE [mediatype] unlocks the given [media type]\64End\64";
 		//
 		// } else
 		if (request.equals("BLOCK ACCESS")) {
 			if (!block.access()) {
 				return "ACCESS IS ALREADY BLOCKED";
 			}
 			block.accessOff();
 			return "ACCESS BLOCKED";
 
 		} else if (request.equals("UNLOCK ACCESS")) {
 			if (block.access()) {
 				return "ACCESS IS ALREADY UNLOCKED";
 
 			}
 			block.accessOn();
 			return "ACCESS UNLOCKED";
 
 		} else if (request.equals("L33T ON")) {
 			if (block.leet()) {
 				return "L33T IS ALREADY ON";
 
 			}
 			block.leetOn();
 			return "L33T IS NOW ON";
 
 		} else if (request.equals("L33T OFF")) {
 			if (!block.leet()) {
 				return "L33T IS ALREADY OFF";
 
 			}
 			block.leetOff();
 			return "L33T IS NOW OFF";
 
 		} else if (request.equals("IMAGES OFF")) {
 			if (!block.images()) {
 				return "IMAGES ARE ALREADY OFF";
 
 			}
 			block.imagesOff();
 			return "IMAGES WILL STOP ROTATING";
 
 		} else if (request.equals("IMAGES ON")) {
 			if (block.images()) {
 				return "IMAGES ARE ALREADY ON";
 			}
 			block.imagesOn();
 			return "IMAGES WILL NOW ROTATE";
 
 		} else if (request.startsWith("BLOCK IP ")) {
 			final String ip = request.substring(9);
 			if (!this.isIP(ip)) {
 				return "INVALID IP";
 
 			}
 			if (!block.blockIP(ip)) {
 				return ip + " IS ALREADY BLOCKED";
 
 			}
 			return ip + " BLOCKED";
 
 		} else if (request.startsWith("UNLOCK IP ")) {
 			final String ip = request.substring(10);
 			if (!this.isIP(ip)) {
 				return "INVALID IP";
 
 			}
			if (!block.unlockIP(ip)) {
 				return ip + " NOT BLOCKED";
 
 			}
 			return ip + " UNLOCKED";
 
 		} else if (request.startsWith("BLOCK URI ")) {
 			final String uri = request.substring(10);
 			// urivalidator!
 			if (!this.isURI(uri)) {
 				return uri + " IS NOT A VALID URI";
 			}
 			final Pattern p = Pattern.compile(uri);
 			if (!block.blockUri(p.pattern())) {
 				return uri + " IS ALREADY BLOCKED";
 
 			}
 
 			return uri + " HAS BEEN BLOCKED";
 
 		} else if (request.startsWith("UNLOCK URI ")) {
 			final String uri = request.substring(11);
 			// urivalidator!
 			if (!this.isURI(uri)) {
 				return uri + "IS NOT A VALID URI";
 			}
 			final Pattern p = Pattern.compile(uri);
 			if (!block.unlockUri(p.pattern())) {
 				return uri + "NOT BLOCKED";
 
 			}
 			return uri + "UNLOCKED";
 
 		} else if (request.startsWith("BLOCK MEDIATYPE ")) {
 			final String mediaType = request.substring(16);
 			if (!block.blockMediaType(mediaType)) {
 				return mediaType
 						+ " IS ALREADY BLOCKED OR ITS NOT A VALID TYPE";
 
 			}
 			return mediaType + " BLOCKED";
 
 		} else if (request.startsWith("UNLOCK MEDIATYPE ")) {
 			final String mediaType = request.substring(17);
 			if (!block.unlockMediaType(mediaType)) {
 				return mediaType + " NOT BLOCKED";
 
 			}
 			return mediaType + " UNLOCKED";
 
 		} else if (request.startsWith("SET MAXSIZE ")) {
 			final String maxSize = request.substring(12);
 			int ms;
 			try {
 				ms = Integer.valueOf(maxSize);
 			} catch (final Exception e) {
 				return "NOT A VALID INTEGER";
 
 			}
 			if (block.setMaxSize(ms)) {
 				return "MAXSIZE SET TO " + maxSize;
 
 			}
 			return "MAXSIZE IS NOW OFF";
 
 		} else if (request.startsWith("GET BLOCKS")) { // A mi gusto tenemos que
 														// hacer un solo
 														// servidor que provea
 														// todo, por eso lo puse
 														// aca.
 			return "TOTAL SITE BLOCKS:"
 					+ Statistics.getInstance().getSiteBlocks()
 					+ "\r\nTOTAL IP BLOCKS: "
 					+ Statistics.getInstance().getIpBlocks() + "\r\nTOTAL"
 					+ " MEDIATYPE BLOCKS: "
 					+ Statistics.getInstance().getContentBlocks()
 					+ "\r\nTOTAL SIZE BLOCKS: "
 					+ Statistics.getInstance().getSizeBlocks();
 
 		} else if (request.startsWith("GET OPEN CONNECTIONS")) {
 			return "TOTAL OPEN CONNECTIONS:"
 					+ Statistics.getInstance().getOpenConnections();
 
 		} else if (request.startsWith("GET CLIENT BYTES TRANSMITED")) {
 			return "TOTAL CLIENT BYTES TRANSMITED:"
 					+ Statistics.getInstance().getProxyClientBytes();
 
 		} else if (request.startsWith("GET SERVER BYTES TRANSMITED")) {
 			return "TOTAL SERVERS BYTES TRANSMITED:"
 					+ Statistics.getInstance().getProxyServerBytes();
 
 		} else if (request.startsWith("GET TRANSFORMATIONS")) {
 			return "TOTAL TRANSFORMATIONS:"
 					+ Statistics.getInstance().getTransformations();
 
 		} else if (request.startsWith("GET TOTAL BYTES TRANSMITED")) {
 			return "TOTAL BYTES TRANSMITED:"
 					+ String.valueOf(Statistics.getInstance()
 							.getProxyClientBytes()
 							+ Statistics.getInstance().getProxyServerBytes());
 
 		} else if (request.equals("EXIT")) {
 			return "BYE!";
 
 		} else {
 			return "INVALID COMMAND, TYPE HELP FOR A LIST OF COMMANDS";
 
 		}
 	}
 }
 // o bloquear accesos a recursos muy grandes: No permitir descargas
 // mayores a cierta cantidad de bytes. (Tener en cuenta el header
 // Content-Lenght, y en su ausencia simplemente los bytes
 // transferidos)
 
