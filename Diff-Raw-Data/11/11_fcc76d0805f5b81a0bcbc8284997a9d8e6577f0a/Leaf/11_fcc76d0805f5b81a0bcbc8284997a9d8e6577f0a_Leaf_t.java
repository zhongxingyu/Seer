 package de.tuberlin.dima.presslufthammer.transport;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.Set;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Sets;
 
 import de.tuberlin.dima.presslufthammer.data.SchemaNode;
 import de.tuberlin.dima.presslufthammer.data.columnar.InMemoryWriteonlyTablet;
 import de.tuberlin.dima.presslufthammer.data.columnar.Tablet;
 import de.tuberlin.dima.presslufthammer.data.ondisk.OnDiskDataStore;
 import de.tuberlin.dima.presslufthammer.qexec.TabletCopier;
 import de.tuberlin.dima.presslufthammer.query.Projection;
 import de.tuberlin.dima.presslufthammer.query.Query;
 import de.tuberlin.dima.presslufthammer.transport.messages.SimpleMessage;
 import de.tuberlin.dima.presslufthammer.transport.messages.Type;
 import de.tuberlin.dima.presslufthammer.util.ShutdownStopper;
 import de.tuberlin.dima.presslufthammer.util.Stoppable;
 
 /**
  * @author feichh
  * @author Aljoscha Krettek
  * 
  */
 public class Leaf extends ChannelNode implements Stoppable {
     private static final SimpleMessage REGMSG = new SimpleMessage(Type.REGLEAF,
             (byte) 0, "Hello".getBytes());
     private static int CONNECT_TIMEOUT = 10000;
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private Channel coordinatorChannel;
     private ClientBootstrap bootstrap;
     private String serverHost;
     private int serverPort;
 
     private OnDiskDataStore dataStore;
 
     public Leaf(String serverHost, int serverPort, File dataDirectory) {
         this.serverHost = serverHost;
         this.serverPort = serverPort;
 
         try {
             dataStore = OnDiskDataStore.openDataStore(dataDirectory);
         } catch (IOException e) {
            log.warn("Exception caught while while loading datastore: {}",
                    e.getMessage());
         }
     }
 
     public void start() {
         ChannelFactory factory = new NioClientSocketChannelFactory(
                 Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
         bootstrap = new ClientBootstrap(factory);
 
         bootstrap.setPipelineFactory(new LeafPipelineFac(this));
         bootstrap.setOption("connectTimeoutMillis", CONNECT_TIMEOUT);
 
         SocketAddress address = new InetSocketAddress(serverHost, serverPort);
 
         ChannelFuture connectFuture = bootstrap.connect(address);
 
         if (connectFuture.awaitUninterruptibly().isSuccess()) {
             coordinatorChannel = connectFuture.getChannel();
             coordinatorChannel.write(REGMSG);
             log.info("Connected to coordinator at {}:{}", serverHost,
                     serverPort);
             Runtime.getRuntime().addShutdownHook(new ShutdownStopper(this));
         } else {
             bootstrap.releaseExternalResources();
             log.info("Failed to conncet to coordinator at {}:{}", serverHost,
                     serverPort);
         }
 
     }
 
     @Override
     public void query(SimpleMessage message) {
         Query query = new Query(message.getPayload());
         log.info("Received query: " + query);
 
         String table = query.getFrom();
 
         try {
             Tablet tablet = dataStore.getTablet(table, query.getPart());
             log.debug("Tablet: {}:{}", tablet.getSchema().getName(),
                     query.getPart());
 
             Set<String> projectedFields = Sets.newHashSet();
             for (Projection project : query.getSelect()) {
                 projectedFields.add(project.getColumn());
             }
             SchemaNode projectedSchema = null;
             if (projectedFields.contains("*")) {
                 log.debug("Query is a 'select * ...' query.");
                 projectedSchema = tablet.getSchema();
             } else {
 
                 projectedSchema = tablet.getSchema().project(projectedFields);
             }
 
             InMemoryWriteonlyTablet resultTablet = new InMemoryWriteonlyTablet(
                     projectedSchema);
 
             TabletCopier copier = new TabletCopier();
             copier.copyTablet(projectedSchema, tablet, resultTablet);
 
             SimpleMessage response = new SimpleMessage(Type.INTERNAL_RESULT,
                     message.getQueryID(), resultTablet.serialize());
 
             coordinatorChannel.write(response);
         } catch (IOException e) {
             log.warn("Caught exception while creating result: {}",
                     e.getMessage());
         }
     }
 
     public void query(Query query) {
         // TODO
         log.debug("Query received: " + query);
     }
 
     @Override
     public void stop() {
         log.info("Stopping leaf.");
         if (coordinatorChannel != null) {
             coordinatorChannel.close().awaitUninterruptibly();
         }
         bootstrap.releaseExternalResources();
         log.info("Leaf stopped.");
     }
 
     private static void printUsage() {
         System.out.println("Usage:");
         System.out.println("hostname port data-dir");
     }
 
     public static void main(String[] args) throws InterruptedException {
         // Print usage if necessary.
         if (args.length < 3) {
             printUsage();
             return;
         }
         // Parse options.
         String host = args[0];
         int port = Integer.parseInt(args[1]);
         File dataDirectory = new File(args[2]);
 
         Leaf leaf = new Leaf(host, port, dataDirectory);
         leaf.start();
     }
 }
