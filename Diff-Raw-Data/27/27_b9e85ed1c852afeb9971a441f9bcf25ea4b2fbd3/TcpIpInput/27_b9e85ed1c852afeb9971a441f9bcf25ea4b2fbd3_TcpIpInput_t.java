 package pleocmd.pipe.in;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 import pleocmd.cfg.ConfigInt;
 import pleocmd.cfg.ConfigString;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.pipe.data.Data;
 
 public final class TcpIpInput extends Input {
 
 	private final ConfigString cfgHost;
 
 	private final ConfigInt cfgPort;
 
 	private Socket socket;
 
 	private DataInputStream in;
 
 	public TcpIpInput() {
 		addConfig(cfgHost = new ConfigString("Host", ""));
 		addConfig(cfgPort = new ConfigInt("Port", 19876, 1, 65535));
 		constructed();
 	}
 
 	public TcpIpInput(final String host, final int port)
 			throws ConfigurationException {
 		this();
 		cfgHost.setContent(host);
 		try {
 			cfgPort.setContent(port);
 		} catch (final ConfigurationException e) {
 			throw new IllegalArgumentException("Cannot set port", e);
 		}
 	}
 
 	@Override
 	protected void configure0() {
 		// nothing to do
 	}
 
 	@Override
 	protected void init0() throws IOException {
 		socket = new Socket(cfgHost.getContent(), cfgPort.getContent());
 		in = new DataInputStream(socket.getInputStream());
 	}
 
 	@Override
 	protected void close0() throws IOException {
 		socket.close(); // closes "in", too
 	}
 
 	@Override
 	protected boolean canReadData0() throws IOException {
 		// TODO use a put-back stream and just read one byte?
 		return true; // impossible to detect beforehand on socket stream
 	}
 
 	@Override
 	protected Data readData0() throws IOException {
 		return Data.createFromBinary(in);
 	}
 
 	public static String help(final HelpKind kind) {
 		switch (kind) {
 		case Name:
 			return "TCP/IP Input";
 		case Description:
 			return "Reads Data blocks from a TCP/IP connection";
 		case Configuration:
 			return "1: Name of the Host (machine name or IP address), "
 					+ "empty for loopback device\n"
 					+ "2: Port number of the Host\n";
 		default:
 			return "???";
 		}
 	}
 
 }
