 package javamud.server;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.Lifecycle;
 
 public class MudServer implements Lifecycle,Runnable {
 	private int port=-1;
 	private Selector selector;
 	private ServerSocketChannel serverSocketChannel;
 	private SelectionKey ssk;
     private Charset charset = Charset.forName("ISO-8859-1");
     private CharsetEncoder encoder = charset.newEncoder();
     private CharsetDecoder decoder = charset.newDecoder();
     private MudStateMachine stateMachine;
     public void setStateMachine(MudStateMachine stateMachine) {
 		this.stateMachine = stateMachine;
 	}
 
 	private ExecutorService executorService;
         
     private static final Logger logger = Logger.getLogger(MudServer.class);
 
 	private volatile boolean running=false;
 	@Override
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	@Override
 	public void start() {
 		assert(port>0);
 		logger.info("Starting the mud server");
 		try {
 			selector = Selector.open();
 			serverSocketChannel = ServerSocketChannel.open();
 			serverSocketChannel.socket().bind(new InetSocketAddress(port));
 			serverSocketChannel.configureBlocking(false);
 			
 			ssk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
 			
 			executorService.execute(this);
 			running=true;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setExecutorService(ExecutorService executorService) {
 		this.executorService = executorService;
 	}
 
 	public void run() {
 		try {
 			while(running) {
 				selector.select();
 				
 				Set<SelectionKey> selectableChannels = selector.selectedKeys();
 				
 				int channelsProcessed=0;
 				for(SelectionKey k: selectableChannels) {
 					channelsProcessed++;
 					// only the server channel is acceptable
 					if (k.isAcceptable() && k==ssk) {
 						SocketChannel client = serverSocketChannel.accept();
						
 						if (client != null) {
 							client.configureBlocking(false);
 							SelectionKey ck = client.register(selector, SelectionKey.OP_READ);
 							Map<String,Object> attribs = new HashMap<String,Object>();
 							attribs.put("decoder", decoder);
 							attribs.put("encoder", encoder);
 							ck.attach(attribs);
 							stateMachine.init(ck);
 						}
 					}
 					else if (k.isReadable()) {
 						stateMachine.processMessage(k);
 					}
					
 					// we don't check for writeable
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 	}
 
 	@Override
 	public void stop() {
 		try {
 			executorService.shutdown();
 			selector.close();
 			serverSocketChannel.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 }
