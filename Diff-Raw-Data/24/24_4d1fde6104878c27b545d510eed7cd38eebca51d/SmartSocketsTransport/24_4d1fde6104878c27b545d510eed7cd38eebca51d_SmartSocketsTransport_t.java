 package interdroid.vdb.transport;
 
 import ibis.smartsockets.naming.NameResolver;
 import ibis.smartsockets.util.MalformedAddressException;
 import ibis.smartsockets.virtual.InitializationException;
 import ibis.smartsockets.virtual.VirtualSocket;
 import ibis.smartsockets.virtual.VirtualSocketAddress;
 import ibis.smartsockets.virtual.VirtualSocketFactory;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jgit.JGitText;
 import org.eclipse.jgit.errors.NotSupportedException;
 import org.eclipse.jgit.errors.TransportException;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.transport.BasePackFetchConnection;
 import org.eclipse.jgit.transport.BasePackPushConnection;
 import org.eclipse.jgit.transport.FetchConnection;
 import org.eclipse.jgit.transport.PackTransport;
 import org.eclipse.jgit.transport.PacketLineOut;
 import org.eclipse.jgit.transport.PushConnection;
 import org.eclipse.jgit.transport.TcpTransport;
 import org.eclipse.jgit.transport.Transport;
 import org.eclipse.jgit.transport.TransportProtocol;
 import org.eclipse.jgit.transport.URIish;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SmartSocketsTransport extends TcpTransport implements PackTransport {
 	private static final Logger logger = LoggerFactory
 	.getLogger(SmartSocketsTransport.class);
 
 	private static final int SMARTSOCKETS_PORT = 9090;
 
	public static final TransportProtocol PROTO = new TransportProtocol() {
 		public String getName() {
			return "Smart Sockets";
 		}
 
 		public Set<String> getSchemes() {
 			return Collections.singleton("ss"); //$NON-NLS-1$
 		}
 
 		public Set<URIishField> getRequiredFields() {
			return Collections.unmodifiableSet(EnumSet.of(URIishField.HOST, URIishField.USER,
 					URIishField.PATH));
 		}
 
 		public Set<URIishField> getOptionalFields() {
 			return Collections.unmodifiableSet(EnumSet.of(URIishField.PORT));
 		}
 
 		public int getDefaultPort() {
 			return SMARTSOCKETS_PORT;
 		}
 
 		public Transport open(URIish uri, Repository local, String remoteName)
 		throws NotSupportedException {
 			return new SmartSocketsTransport(local, uri);
 		}
 	};
 
 	public static final String REAL_NAME = "name";
 	public static final String EMAIL = "email";
 
 	// TODO: This should come from a property or something
 	private static final long TIMEOUT = 1000 * 30;
 
 	protected SmartSocketsTransport(Repository local, URIish uri) {
 		super(local, uri);
 	}
 
 	public SmartSocketsTransport(URIish uri) {
 		super(null, uri);
 	}
 
 	@Override
 	public FetchConnection openFetch() throws TransportException {
 		try {
 			return new SmartSocketsFetchConnection();
 		} catch (Exception e) {
 			throw new TransportException(uri, "Unable to open socket.", e);
 		}
 	}
 
 	@Override
 	public PushConnection openPush() throws TransportException {
 		try {
 			return new SmartSocketsPushConnection();
 		} catch (Exception e) {
 			throw new TransportException(uri, "Unable to open socket.", e);
 		}
 	}
 
 	@Override
 	public void close() {
 		// TODO: Unregister our endpoint with smartsockets
 	}
 
 	static VirtualSocket openConnection(URIish uri, int timeout) throws MalformedAddressException, IOException, InitializationException {
		VirtualSocketAddress otherSide = NameResolver.getDefaultResolver().resolve(uri.getUser() + "@" + uri.getHost(), timeout);
 		VirtualSocket s = VirtualSocketFactory.getDefaultSocketFactory().createClientSocket(otherSide, timeout, null);
 		return s;
 	}
 
 	void service(final String name, final PacketLineOut pckOut)
 	throws IOException {
 		final StringBuilder cmd = new StringBuilder();
 		cmd.append(name);
 		cmd.append(' ');
 		cmd.append(uri.getPath());
 		cmd.append('\0');
 		cmd.append("host=");
 		cmd.append(uri.getHost());
		cmd.append("user=");
		cmd.append(uri.getHost());
 		if (uri.getPort() > 0 && uri.getPort() != SMARTSOCKETS_PORT) {
 			cmd.append(":");
 			cmd.append(uri.getPort());
 		}
 		cmd.append('\0');
 		pckOut.writeString(cmd.toString());
 		pckOut.flush();
 	}
 
 	class SmartSocketsFetchConnection extends BasePackFetchConnection {
 		private VirtualSocket sock;
 
 		SmartSocketsFetchConnection() throws MalformedAddressException, IOException, InitializationException {
 			super(SmartSocketsTransport.this);
 			sock = openConnection(uri, getTimeout());
 			try {
 				InputStream sIn = sock.getInputStream();
 				OutputStream sOut = sock.getOutputStream();
 
 				sIn = new BufferedInputStream(sIn);
 				sOut = new BufferedOutputStream(sOut);
 
 				init(sIn, sOut);
 				service("git-upload-pack", pckOut);
 			} catch (IOException err) {
 				close();
 				throw new TransportException(uri,
 						JGitText.get().remoteHungUpUnexpectedly, err);
 			}
 			readAdvertisedRefs();
 		}
 
 		@Override
 		public void close() {
 			super.close();
 
 			if (sock != null) {
 				try {
 					sock.close();
 				} catch (IOException err) {
 					// Ignore errors during close.
 				} finally {
 					sock = null;
 				}
 			}
 		}
 	}
 
 	class SmartSocketsPushConnection extends BasePackPushConnection {
 		private VirtualSocket sock;
 
 		SmartSocketsPushConnection() throws MalformedAddressException, IOException, InitializationException {
 			super(SmartSocketsTransport.this);
 			sock = openConnection(uri, getTimeout());
 			try {
 				InputStream sIn = sock.getInputStream();
 				OutputStream sOut = sock.getOutputStream();
 
 				sIn = new BufferedInputStream(sIn);
 				sOut = new BufferedOutputStream(sOut);
 
 				init(sIn, sOut);
 				service("git-receive-pack", pckOut);
 			} catch (IOException err) {
 				close();
 				throw new TransportException(uri,
 						JGitText.get().remoteHungUpUnexpectedly, err);
 			}
 			readAdvertisedRefs();
 		}
 
 		@Override
 		public void close() {
 			super.close();
 
 			if (sock != null) {
 				try {
 					sock.close();
 				} catch (IOException err) {
 					// Ignore errors during close.
 				} finally {
 					sock = null;
 				}
 			}
 		}
 	}
 
 	public static List<Map<String, ?>> getRepositories(URIish uri) throws MalformedAddressException, IOException, InitializationException {
 		List<Map<String, ?>> repositories = null;
 		VirtualSocket socket = openConnection(uri, 60);
 
 		OutputStream out = new BufferedOutputStream(socket.getOutputStream());
 		out.write("git-list-repos\0".getBytes());
 
 		repositories = new ArrayList<Map<String, ?>>();
 
 		DataInputStream in = new DataInputStream( new BufferedInputStream(socket.getInputStream()) );
 		int count = in.readInt();
 		for (int i = 0; i < count; i++) {
			// TODO: Need to finish this
 			@SuppressWarnings("rawtypes")
 			HashMap<String, ?> data = new HashMap();
 		}
 		return repositories;
 	}
 
 }
