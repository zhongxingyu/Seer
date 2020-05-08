 package multiplexer.jmx.test.util;
 
 import java.net.InetSocketAddress;
 
 import multiplexer.jmx.server.JmxServer;
 
 /**
  * @author Piotr Findeisen
  * 
  */
 public final class JmxServerRunner {
 
 	private JmxServer server;
 	private Thread serverThread;
 
 	public void start() throws Exception {
 		assert server == null;
 
 		server = new JmxServer(new InetSocketAddress("0.0.0.0", 0));
 		server.setTransferUpdateIntervalMillis(1000);
 		server.loadMessageDefinitionsFromFile("test.rules");
 
 		serverThread = new Thread(server);
 		serverThread.start();
 
 		synchronized (server) {
			// FIXME race condition
			server.wait();
 		}
 	}
 
 	public int getLocalServerPort() {
 		assert server != null;
 		return server.getLocalPort();
 	}
 
 	public void stop() {
 		stop(true);
 	}
 
 	private void stop(boolean check) {
 		assert !check || server != null;
 		assert !check || serverThread.isAlive();
 
 		server.shutdown();
 		try {
 			serverThread.join(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		if (serverThread.isAlive())
 			serverThread.interrupt();
 
 		server = null;
 		serverThread = null;
 	}
 
 	@Override
 	public void finalize() throws Throwable {
 		super.finalize();
 		stop(false);
 	}
 }
