 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 
 public class MinecraftProxy extends Thread {
 	
 	private static final File PROPERTIES_FILE = new File(
 			"/home/andreas/ciscomine/static-players.properties");
 	
 	private class Worker {
 		public Worker(Socket proxyClientSocket) throws IOException {
 			InetAddress proxyClientAddress = proxyClientSocket.getInetAddress();
 			Socket serverClientSocket = new Socket(
 					serverSocketAddress.getAddress(),
 					serverSocketAddress.getPort());
 			
 			InputStream proxyClientIn = proxyClientSocket.getInputStream();
 			proxyClientIn = new MinecraftFirstMessageFilter(proxyClientIn,
 					Minecraft.PLAYER, nameFactory, proxyClientAddress,
 					serverSocketAddress);
 			proxyClientIn = new MinecraftPlayerNameFilter(proxyClientIn,
 					Minecraft.PLAYER,
 					(MinecraftFirstMessageFilter) proxyClientIn);
 			OutputStream proxyClientOut = proxyClientSocket.getOutputStream();
 			InputStream serverClientIn = serverClientSocket.getInputStream();
 			OutputStream serverClientOut = serverClientSocket.getOutputStream();
 			
 			new InputStreamPipe(new TeeInputStream(proxyClientIn,
 					new FilterZeroOutputStream(System.out)), serverClientOut);
 			new InputStreamPipe(serverClientIn, proxyClientOut);
 		}
 	}
 	
 	private final NameFactory nameFactory;
 	private final ServerSocket proxyServerSocket;
 	private final InetSocketAddress serverSocketAddress;
 	
 	public MinecraftProxy(NameFactory nameFactory, int port,
 			InetSocketAddress serverSocketAddress) throws IOException {
 		this.nameFactory = nameFactory;
 		this.proxyServerSocket = new ServerSocket(port);
 		this.serverSocketAddress = serverSocketAddress;
 		
 		start();
 	}
 	
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				Socket socket = proxyServerSocket.accept();
 				new Worker(socket);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main(String[] args) throws Throwable {
 		InetSocketAddress socketAddress;
 		int port;
 		
 		try {
 			String[] socketAddressString = args[0].split(":");
 			socketAddress = new InetSocketAddress(
 					socketAddressString[0],
 					(socketAddressString.length > 1) ? Integer.parseInt(socketAddressString[1])
 							: Minecraft.PORT);
			port = (args.length > 1) ? Integer.parseInt(args[1]) : 25566;
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println();
 			System.err.println("usage: <program> <srvAddress>:<srvPort> [<proxyPort> [<staticName>]]");
 			System.exit(-1);
 			return;
 		}
 		
 		NameFactory nameFactory;
 		
 		if (args.length > 2) {
 			nameFactory = new StaticNameFactory(args[2]);
 		} else {
 			NameFactory subNameFactory = new IncrementingNameFactory();
 			
 			if (PROPERTIES_FILE.exists()) {
 				nameFactory = new CachedNameFactory(subNameFactory,
 						PROPERTIES_FILE);
 			} else {
 				nameFactory = new CachedNameFactory(subNameFactory);
 			}
 		}
 		
 		new MinecraftProxy(nameFactory, port, socketAddress);
 		
 		System.out.println("proxy server started...");
 	}
 	
 }
