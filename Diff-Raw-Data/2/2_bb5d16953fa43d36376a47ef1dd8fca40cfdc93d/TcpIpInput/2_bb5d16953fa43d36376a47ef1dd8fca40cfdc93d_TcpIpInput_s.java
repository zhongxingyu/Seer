 package pleocmd.pipe.in;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import pleocmd.Log;
 import pleocmd.cfg.ConfigInt;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.FormatException;
 import pleocmd.exc.InputException;
 import pleocmd.pipe.data.Data;
 import pleocmd.pipe.data.MultiFloatData;
 
 public final class TcpIpInput extends Input { // NO_UCD
 
 	private final ConfigInt cfgTimeoutConn;
 
 	private final ConfigInt cfgTimeoutRead;
 
 	private final ConfigInt cfgPort;
 
 	private Socket socket;
 
 	private ServerSocket serverSocket;
 
 	private DataInputStream in;
 
 	public TcpIpInput() {
 		addConfig(cfgPort = new ConfigInt("Port", 19876, 1, 65535));
 		addConfig(cfgTimeoutConn = new ConfigInt("Connection-Timeout (sec)",
 				60, 0, 3600));
 		addConfig(cfgTimeoutRead = new ConfigInt("Read-Timeout (sec)", 10, 0,
 				3600));
 		constructed();
 	}
 
 	public TcpIpInput(final int port) {
 		this();
 		try {
 			cfgPort.setContent(port);
 		} catch (final ConfigurationException e) {
 			throw new IllegalArgumentException("Cannot set port", e);
 		}
 	}
 
 	@Override
 	protected void init0() throws IOException {
 		serverSocket = new ServerSocket();
 		serverSocket.setPerformancePreferences(0, 2, 1);
 		serverSocket.setReuseAddress(true);
 		serverSocket.setSoTimeout(3000);
 		serverSocket.bind(new InetSocketAddress(cfgPort.getContent()));
 		socket = null;
 		in = null;
 	}
 
 	@Override
 	protected void close0() throws IOException {
 		if (in != null) in.close();
 		if (socket != null) socket.close();
		serverSocket.close();
 	}
 
 	@Override
 	public String getOutputDescription() {
 		return MultiFloatData.IDENT;
 	}
 
 	@Override
 	protected String getShortConfigDescr0() {
 		return String.format("%d [%ds, %ds]", cfgPort.getContent(),
 				cfgTimeoutConn.getContent(), cfgTimeoutRead.getContent());
 	}
 
 	@Override
 	protected Data readData0() throws IOException, InputException {
 		if (socket == null || !socket.isConnected() || socket.isInputShutdown()) {
 			if (in != null) in.close();
 			if (socket != null) socket.close();
 			final int cnt = Math.max(1, cfgTimeoutConn.getContent() / 3);
 			for (int i = 1;; ++i)
 				try {
 					Log.info("Waiting for TCP/IP connection ...");
 					socket = serverSocket.accept();
 					break;
 				} catch (final IOException e) {
 					if (getPipe().isInitPhaseInterrupted()) return null;
 					if (i == cnt) throw e;
 				}
 			socket.setSoTimeout(cfgTimeoutRead.getContent() * 1000);
 			in = new DataInputStream(socket.getInputStream());
 		}
 		try {
 			return new MultiFloatData(Data.createFromBinary(in));
 		} catch (final FormatException e) {
 			throw new InputException(this, false, e, "Cannot read from TCP/IP");
 		} catch (final IOException e) {
 			in.close();
 			socket.close();
 			socket = null;
 			throw new InputException(this, false, e, "Cannot read from TCP/IP");
 		}
 	}
 
 	public static String help(final HelpKind kind) {
 		switch (kind) {
 		case Name:
 			return "TCP/IP Input";
 		case Description:
 			return "Reads Data blocks from a TCP/IP connection";
 		case Config1:
 			return "Port number of the client";
 		case Config2:
 			return "Connection Timeout in seconds (0 means infinite)";
 		case Config3:
 			return "Timeout for reading in seconds (0 means infinite)";
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public String isConfigurationSane() {
 		try {
 			final ServerSocket ss = new ServerSocket();
 			ss.setReuseAddress(true);
 			ss.bind(new InetSocketAddress(cfgPort.getContent()));
 			ss.close();
 		} catch (final IOException e) {
 			return "Port is already in use";
 		}
 		return null;
 	}
 
 	@Override
 	protected int getVisualizeDataSetCount() {
 		return 0;
 	}
 
 }
