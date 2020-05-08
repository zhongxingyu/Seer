 package org.bigfs.internode.service;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.BindException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.channels.AsynchronousCloseException;
 import java.nio.channels.ServerSocketChannel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.naming.ConfigurationException;
 
 import org.bigfs.concurrent.DebuggableThreadPoolExecutor;
 import org.bigfs.internode.configuration.MessagingConfiguration;
 import org.bigfs.internode.events.StreamSendingEvent;
 import org.bigfs.internode.message.AsyncResult;
 import org.bigfs.internode.message.CallbackInfo;
 import org.bigfs.internode.message.IAsyncResult;
 import org.bigfs.internode.message.IMessage;
 import org.bigfs.internode.message.IMessageCallback;
 import org.bigfs.internode.message.IMessageHandler;
 import org.bigfs.internode.message.IVersionedSerializer;
 import org.bigfs.internode.message.MessageConnection;
 import org.bigfs.internode.message.MessageConnectionPool;
 import org.bigfs.internode.message.MessageDeliveryTask;
 import org.bigfs.internode.message.MessageIn;
 import org.bigfs.internode.message.MessageOut;
 import org.bigfs.internode.message.MessageReader;
 import org.bigfs.internode.metrics.ConnectionMetrics;
 import org.bigfs.internode.streaming.ACompressedFileStreamTask;
 import org.bigfs.internode.streaming.AFileStreamReaderTask;
 import org.bigfs.internode.streaming.AFileStreamTask;
 import org.bigfs.internode.streaming.IStreamHeader;
 import org.bigfs.internode.streaming.utils.DataOutputBuffer;
 import org.bigfs.utils.ExpiringMap;
 import org.bigfs.utils.Helper;
 import org.cliffc.high_scale_lib.NonBlockingHashMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.common.eventbus.AsyncEventBus;
 
 public class MessagingService implements MessagingServiceMBean
 {
     public static final String MBEAN_NAME = "org.bigfs.internode.service:type=MessagingService";
     
     // 4 byte int for protocol identifier
     public static int PROTOCOL_MAGIC = 0xBFDDABFF;
     
     public static final int VERSION_1 = 1; 
     
     public static final int CURRENT_VERSION = VERSION_1;
     
     private static final Logger logger = LoggerFactory.getLogger(MessagingService.class);
     
     private final List<MessagingThread> messagingThreads = Lists.newArrayList();
     
     private final NonBlockingHashMap<InetAddress, Integer> versions = new NonBlockingHashMap<InetAddress, Integer>();  
     
     private final NonBlockingHashMap<InetAddress, MessageConnectionPool> messageConnectionPool = new NonBlockingHashMap<InetAddress, MessageConnectionPool>();
     
     private static final AtomicInteger idGen = new AtomicInteger(0);
     
     private static final HashMap<Integer, IVersionedSerializer<? extends IMessage>> messageSerializers = new HashMap<Integer, IVersionedSerializer<? extends IMessage>>();
     
     private static final HashMap<String, ThreadPoolExecutor> executors = new HashMap<String, ThreadPoolExecutor>();
 
     private static final List<String> dropabbleMessageGroups = Lists.newArrayList();
 
     private static final Map<Integer, IMessageHandler<?>> messageHandlers = new HashMap<Integer, IMessageHandler<?>>();
     
     /* This records all the results mapped by message Id */
     private final ExpiringMap<String, CallbackInfo> callbacks = new ExpiringMap<String, CallbackInfo>(MessagingConfiguration.getRpcTimeout(), null);
     
     private static final HashMap<Integer, IVersionedSerializer<?>> responseSerializers = new HashMap<Integer, IVersionedSerializer<?>>();
     
     private static String fileStreamClass;
     
     private static String compressedFileStreamClass;
 
     private static String fileStreamReaderClass;
     
     private final AsyncEventBus eventHandler;
     
     /**
      * One executor per destination InetAddress for streaming.
      * <p/>
      * See CASSANDRA-3494 for the background. We have streaming in place so we do not want to limit ourselves to
      * one stream at a time for throttling reasons. But, we also do not want to just arbitrarily stream an unlimited
      * amount of files at once because a single destination might have hundreds of files pending and it would cause a
      * seek storm. So, transfer exactly one file per destination host. That puts a very natural rate limit on it, in
      * addition to mapping well to the expected behavior in many cases.
      * <p/>
      * We will create our stream executors with a core size of 0 so that they time out and do not consume threads. This
      * means the overhead in the degenerate case of having streamed to everyone in the ring over time as a ring changes,
      * is not going to be a thread per node - but rather an instance per node. That's totally fine.
      */
     private final ConcurrentMap<InetAddress, DebuggableThreadPoolExecutor> streamExecutors = new NonBlockingHashMap<InetAddress, DebuggableThreadPoolExecutor>();
 
     
     private MessagingService() {
         eventHandler = new AsyncEventBus(
                 "MessagingService-Events",
                 DebuggableThreadPoolExecutor.createWithMaximumPoolSize("EventExecutor", Helper.getAvailableProcessors(), 1, TimeUnit.SECONDS)
         );
         
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         try
         {
             mbs.registerMBean(this, new ObjectName(MBEAN_NAME));
         }
         catch (Exception e)
         {
             throw new RuntimeException(e);
         }
     }
     
     
     private static class MSHandle
     {
         public static final MessagingService instance = new MessagingService();
     }
     
     
     public static MessagingService instance()
     {
         return MSHandle.instance;
     }
     
     private static String nextId()
     {
         return Integer.toString(idGen.incrementAndGet());
     }
     
     /**
      * Listen on the specified port.
      *
      * @param localEp InetAddress whose port to listen on.
      */
     public void listen() throws Exception
     {
         for (ServerSocket ss : getServerSocket())
         {
             MessagingThread th = new MessagingThread(ss, "ACCEPT-" + ss.getInetAddress());
             th.start();
             messagingThreads.add(th);
         }   
     }
     /**
      * returns remote node messaging protocol version, if remote node 
      * not registered then returns current version it self
      *
      */
     public int getRemoteMessagingVersion(InetAddress to)
     {
         
         if(versions.containsKey(to)) {
            return  versions.get(to);
         }
         
         return MessagingService.CURRENT_VERSION;
     }
     
     public MessageConnectionPool getConnectionPool(InetAddress to)
     {
        MessageConnectionPool pool = this.messageConnectionPool.get(to);
        
    	if(pool == null) 
     	{
    		this.messageConnectionPool.putIfAbsent(to, new MessageConnectionPool(to));
    		pool = this.messageConnectionPool.get(to);
     	}
     	
    	return pool;
     }
     
     private MessageConnection getConnection(InetAddress to, MessageOut<?> m)
     {
     	return this.getConnectionPool(to).getConnection(m);
     } 
     
     public void sendOneWay(MessageOut message, InetAddress to, String id, String replyTo)
     {
         this.getConnection(to, message).addToQueue(message, id, replyTo);     
     }
         
     public void sendOneWay(MessageOut message, InetAddress to)
     {
     	this.sendOneWay(message, to, MessagingService.nextId(), null);
     }
     
     /*
      * @see #send(Message message, InetAddress to, IMessageCallback cb, long timeout)
      */
     public String send(MessageOut message, InetAddress to, IMessageCallback cb)
     {
         return send(message, to, cb, message.getMessageTimeout());
     }
     /**
      * Send a message to a given endpoint. This method specifies a callback
      * which is invoked with the actual response.
      *
      * @param message message to be sent.
      * @param to      remote address to which the message needs to be sent
      * @param cb      callback interface which is used to pass the responses or
      *                suggest that a timeout occurred to the invoker of the send().
      *                suggest that a timeout occurred to the invoker of the send().
      * @param timeout the timeout used for expiration
      * @return an reference to message id used to match with the result
      */
     public String send(MessageOut message, InetAddress to, IMessageCallback cb, long timeout)
     {
         String id = addCallback(cb, message, to, timeout);
 
         
         sendOneWay(message, to, id, null);
         return id;
     }
     
     public IAsyncResult send(MessageOut message, InetAddress to)
     {
         IAsyncResult iar = new AsyncResult();
         send(message, to, iar);
         return iar;
     }
     
     public void sendReply(MessageOut message, String replyTo, InetAddress to)
     {
         this.sendOneWay(message, to, MessagingService.nextId(), replyTo);
     }
     
     public String addCallback(IMessageCallback cb, MessageOut message, InetAddress to, long timeout)
     {
         String messageId = nextId();
         CallbackInfo previous;
 
         previous = callbacks.put(messageId, new CallbackInfo(to, cb, message), timeout);
         
         assert previous == null;
         return messageId;
     }
     
     /**
      * register remote node version.
      * @param to
      * @param version
      */
     public void setRemoteMessagingVersion(InetAddress to, int version)
     {
         versions.put(to, version);
     }
     
     public static void registerMessageSerializer(int messageType, IVersionedSerializer<? extends IMessage> serializer)
     {
         if(!MessagingService.messageSerializers.containsKey(messageType))
         {            
             MessagingService.messageSerializers.put(messageType, serializer);
         }
     }
     
     public static void registerMessageSerializers(Map<Integer, IVersionedSerializer<? extends IMessage>>serializers)
     {
         for(Integer messageType: serializers.keySet())
         {
             registerMessageSerializer(messageType, serializers.get(messageType));
         }
     }
     
     public static void registerMessageResponseSerializers(Map<Integer, IVersionedSerializer<?>>serializers)
     {
         for(Integer messageType: serializers.keySet())
         {
             registerMessageResponseSerializer(messageType, serializers.get(messageType));
         }
     }
     
     public static void registerMessageResponseSerializer(Integer messageType, IVersionedSerializer<?> serializer)
     {
       responseSerializers.put(messageType, serializer);      
     }
     
     public static void registerFileStreamMessageClass(String _class)
     {
       MessagingService.fileStreamClass = _class;
     }
     
     public static void registerFileStreamReaderClass(String _class)
     {
       MessagingService.fileStreamReaderClass = _class;
     }
     
     public static void registerCompressedFileStreamMessageClass(String _class)
     {
       MessagingService.compressedFileStreamClass = _class;
     }
     
     public CallbackInfo getRegisteredCallback(String messageId)
     {
         return callbacks.get(messageId);
     }
     
     public static void registerDroppableMessageGroup(String messageGroup)
     {
         if(!dropabbleMessageGroups.contains(messageGroup)){
             dropabbleMessageGroups.add(messageGroup);
         }
     }
     
     public static void registerDroppableMessageGroups(List<String> messageGroups)
     {
         for(String messageGroup: messageGroups) {
             registerDroppableMessageGroup(messageGroup);
         }
     }
     
     public static IVersionedSerializer<? extends IMessage> getMessageSerializer(int messageType)
     {
         return MessagingService.messageSerializers.get(messageType);
     }
     
     private List<ServerSocket> getServerSocket() throws ConfigurationException
     {
         final List<ServerSocket> ss = new ArrayList<ServerSocket>();
         
         ServerSocketChannel serverChannel = null;
         try
         {
             serverChannel = ServerSocketChannel.open();
         }
         catch (IOException e)
         {
             throw new RuntimeException(e);
         }
         ServerSocket socket = serverChannel.socket();
         try
         {
             socket.setReuseAddress(true);
         }
         catch (SocketException e)
         {
             throw new ConfigurationException("Insufficient permissions to setReuseAddress");
         }
         InetSocketAddress address = new InetSocketAddress(MessagingConfiguration.getListenAddress(), MessagingConfiguration.getPort());
         try
         {
             socket.bind(address);
         }
         catch (BindException e)
         {
             if (e.getMessage().contains("in use"))
                 throw new ConfigurationException(address + " is in use by another process.  Change listen_address to values that do not conflict with other services");
             else if (e.getMessage().contains("Cannot assign requested address"))
                 throw new ConfigurationException("Unable to bind to address " + address
                                                  + ". Set listen_address to an interface you can bind to, e.g., your private IP address on EC2");
             else
                 throw new RuntimeException(e);
         }
         catch (IOException e)
         {
             throw new RuntimeException(e);
         }
         logger.info("Starting Messaging Service on port {}", MessagingConfiguration.getPort());
         ss.add(socket);
         return ss;
     }
     
     
     /**
      * validate protocol magic, we must sure this message comes from our services.
      * @param magic
      * @throws IOException
      */
     public static void validateProcotolMagic(int magic) throws IOException {
         
         if (magic != PROTOCOL_MAGIC)
             throw new IOException("invalid protocol header");
     }
     
     
     public static int getMessageHeader(boolean compressionEnabled, boolean isStream, int guessedRemoteVersion) {
         int header = 0; // 32 bit int
          
         // set 3. bit as 1 if compression enabled  
         if (compressionEnabled)
             header |= 4;
         
         // set 4. bit as 1 if is stream mode enabled
         if (isStream)           
             header |= 8;
         // set our version from 15. bit to 23. bit
         header |= (MessagingService.CURRENT_VERSION << 8);
         
         // set remote known version from 24. bit to 32. bit. 
         header |= (guessedRemoteVersion << 16);
         
         return header;        
     }
     
     /**
      * get bits 
      * @param packed
      * @param start
      * @param count
      * @return
      */
     public static int getBits(int packed, int start, int count)
     {
         return packed >>> (start + 1) - count & ~(-1 << count);
     }
     
     
     public void receive(MessageIn message, long timestamp)
     {          
         Runnable runnable = new MessageDeliveryTask(message, timestamp);
         ExecutorService stage = this.getMessageGroupExecutor(message.getMessageGroup());
         stage.execute(runnable);        
     }
         
     public static void shutdownNow()
     {
         for (String messageGroup: executors.keySet())
         {
             executors.get(messageGroup).shutdownNow();
         }
     }
     
     public static void registerMessageGroupExecutor(String messageGroup, ThreadPoolExecutor executor) 
     {
         if(!MessagingService.executors.containsKey(messageGroup)){
             MessagingService.executors.put(messageGroup, executor);
         }
     }
     
     public ThreadPoolExecutor getMessageGroupExecutor(String messageGroup) 
     {
         return executors.get(messageGroup);        
     }
     
     public static void registerMessageHandlers(int messageType, IMessageHandler<?> messageHandler)
     {
         assert !MessagingService.messageHandlers.containsKey(messageType);
         MessagingService.messageHandlers.put(messageType, messageHandler);
     }
     
     public IMessageHandler<? extends IMessage> getMesssageHandler(int messageType) {
         return messageHandlers.get(messageType);
     } 
     
     static class MessagingThread extends Thread {
         
         private final ServerSocket server;
         
         public MessagingThread(ServerSocket socket, String name) {
             super(name);
             this.server = socket;
         }
         
         public void run() {
             while(true) {
                 
                 try
                 {
                     Socket socket = server.accept();
                     new MessageReader(socket).start();
                 } 
                 catch (AsynchronousCloseException e)
                 {
                     // this happens when another thread calls close().
                     logger.info("MessagingService shutting down server thread.");
                     break;
                 }
                 catch (IOException e)
                 {
                     throw new RuntimeException(e);
                 }
                 
                 logger.info("We accept new connection");
             }
         }
         
         public void close() throws IOException {
                 this.server.close();            
         }
     }
 
 
     public static boolean isDroppableMessage(String messageGroup)
     {
         return dropabbleMessageGroups.contains(messageGroup);
     }
     
     public <T extends IStreamHeader> void send(T header, InetAddress to)
     {
         DebuggableThreadPoolExecutor executor = streamExecutors.get(to);
         if (executor == null)
         {
             // Using a core pool size of 0 is important. See documentation of streamExecutors.
             executor = DebuggableThreadPoolExecutor.createWithMaximumPoolSize("Streaming to " + to, 1, 1, TimeUnit.SECONDS);
             DebuggableThreadPoolExecutor old = streamExecutors.putIfAbsent(to, executor);
             if (old != null)
             {
                 // bir diğer thread tarafından aynı yöne doğru bir  
                 // executor eklenmiş.
                 executor.shutdown();
                 executor = old;
             }
         }
 
         executor.execute(header.fileCount() == 0 || header.isCompressed() == false
                          ? getFileStreamTask(header, to)
                          : getCompressedFileStreamTask(header, to));
         
         this.getEventHandler().post(new StreamSendingEvent(
                     to, header
                 )
         );
     }
     
     @SuppressWarnings("unchecked")
     private static <T extends AFileStreamTask<IStreamHeader>> T getFileStreamTask(IStreamHeader header, InetAddress to) throws RuntimeException
     {
         try 
         {
             ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
             Class<?> myClass = myClassLoader.loadClass(MessagingService.fileStreamClass);
             
             
             AFileStreamTask<IStreamHeader> instance = (AFileStreamTask<IStreamHeader>)  myClass.newInstance();
             instance.initialize(header,to);
             
             return (T) instance;
         }
         catch(Exception e){
             throw new RuntimeException(e);
         }
     }
     
     @SuppressWarnings("unchecked")
     public static <T extends AFileStreamReaderTask> T getFileStreamReader(Socket socket, int guessedOurVersion) throws RuntimeException
     {
         try 
         {
             ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
             Class<?> myClass = myClassLoader.loadClass(MessagingService.fileStreamReaderClass);
             
             
             AFileStreamReaderTask instance = (AFileStreamReaderTask) myClass.newInstance();
             instance.initialize(socket, guessedOurVersion);
             
             return (T) instance;
         }
         catch(Exception e){
             throw new RuntimeException(e);
         }
     }
     
     
     @SuppressWarnings("unchecked")
     private static <T extends ACompressedFileStreamTask<IStreamHeader>> T getCompressedFileStreamTask(IStreamHeader header, InetAddress to) throws RuntimeException
     {
         try 
         {
             ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
             Class<?> myClass = myClassLoader.loadClass(MessagingService.compressedFileStreamClass);
             
             
             ACompressedFileStreamTask<IStreamHeader> instance = (ACompressedFileStreamTask<IStreamHeader>)  myClass.newInstance();
             instance.initialize(header,to);
             
             return (T) instance;
         }
         catch(Exception e){
             throw new RuntimeException(e);
         }
     }
     
     
     
     @SuppressWarnings("unchecked")
     public static <T extends IStreamHeader> ByteBuffer getStreamHeader(T streamHeader, int version)
     {
         int header =  MessagingService.getMessageHeader(streamHeader.isCompressed(), true, version);
         
         byte[] bytes;
         try
         {
             DataOutputBuffer buffer = new DataOutputBuffer();
             streamHeader.getSerializer().serialize(streamHeader, buffer, version);
             
             bytes = buffer.getData();
         }
         catch (IOException e)
         {
             throw new RuntimeException(e);
         }
         assert bytes.length > 0;
        
         ByteBuffer buffer = ByteBuffer.allocate(12 + bytes.length);
         buffer.putInt(PROTOCOL_MAGIC);
         buffer.putInt(header);
         buffer.putInt(bytes.length);
         buffer.put(bytes);
         buffer.flip();
         
         return buffer;
     }
 
     public AsyncEventBus getEventHandler()
     {  
         return this.eventHandler;
     }
 
     @Override
     public Map<String, Integer> getPendingMessages()
     {
         Map<String, Integer> pendingMessages = new HashMap<String, Integer>();
         for (Map.Entry<InetAddress, MessageConnectionPool> entry : messageConnectionPool.entrySet())
         {   
             pendingMessages.put(entry.getKey().getHostAddress(), entry.getValue().getPendingMessages());
         }
         
         return pendingMessages;
     }
 
     @Override
     public Map<String, Long> getCompletedMessages()
     {
         Map<String, Long> completedMessages = new HashMap<String, Long>();
         for (Map.Entry<InetAddress, MessageConnectionPool> entry : messageConnectionPool.entrySet())
         {
             completedMessages.put(entry.getKey().getHostAddress(), entry.getValue().getCompletedMesssages());
         }    
         
         return completedMessages;
     }
 
 
     @Override
     public Map<String, Long> getDroppedMessages()
     {
         Map<String, Long> droppedMessages = new HashMap<String, Long>();
         for (Map.Entry<InetAddress, MessageConnectionPool> entry : messageConnectionPool.entrySet())
         {
           droppedMessages.put(entry.getKey().getHostAddress(), entry.getValue().getDroppedMessages());
         }    
         
         return droppedMessages;
     }
 
     
     @Override
     public long getTotalTimeouts()
     {        
         return ConnectionMetrics.totalTimeouts.count();
     }
 
     @Override
     public Map<String, Long> getTimeoutsPerHost()
     {
         Map<String, Long> result = new HashMap<String, Long>();
         for (Map.Entry<InetAddress, MessageConnectionPool> entry: messageConnectionPool.entrySet())
         {
             String ip = entry.getKey().getHostAddress();
             long recent = entry.getValue().getTimeouts();
             result.put(ip, recent);
         }        
         return result;
     }
 
     @Override
     public long getRecentTotalTimouts()
     {        
         return ConnectionMetrics.getRecentTotalTimeout();
     }
 
     @Override
     public Map<String, Long> getRecentTimeoutsPerHost()
     {
         Map<String, Long> result = new HashMap<String, Long>();
         for (Map.Entry<InetAddress, MessageConnectionPool> entry: messageConnectionPool.entrySet())
         {
             String ip = entry.getKey().getHostAddress();
             long recent = entry.getValue().getRecentTimeouts();
             result.put(ip, recent);
         }
         return result;
     }
 
     @Override
     public int getVersion(String address) throws UnknownHostException
     {
         return getRemoteMessagingVersion(InetAddress.getByName(address));
     }
     
     @Override
     public Map<String, Integer> getPendingMessageGroups()
     {
         Map<String, Integer> result = new HashMap<String, Integer>();
         for(Map.Entry<InetAddress, MessageConnectionPool> entry: messageConnectionPool.entrySet())   
         {
             for(String groupName: entry.getValue().getConnectionGroups())
             {
                 if(!result.containsKey(groupName)){
                     result.put(groupName, 0);                    
                 }
                 result.put(groupName, result.get(groupName) + entry.getValue().getConnection(groupName).getPendingMessages());
             }
         }
         
         return result;
     }
     
     @Override
     public Map<String, Long> getCompletedMessageGroups()
     {
         Map<String, Long> result = new HashMap<String, Long>();
         for(Map.Entry<InetAddress, MessageConnectionPool> entry: messageConnectionPool.entrySet())   
         {
             for(String groupName: entry.getValue().getConnectionGroups())
             {
                 if(!result.containsKey(groupName)){
                     result.put(groupName, 0L);                    
                 }
                 result.put(groupName, result.get(groupName) + entry.getValue().getConnection(groupName).getCompletedMessages());
             }
         }
         
         return result;
     }
     
     @Override
     public Map<String, Long> getDroppedMessageGroups()
     {
         Map<String, Long> result = new HashMap<String, Long>();
         for(Map.Entry<InetAddress, MessageConnectionPool> entry: messageConnectionPool.entrySet())   
         {
             for(String groupName: entry.getValue().getConnectionGroups())
             {
                 if(!result.containsKey(groupName)){
                     result.put(groupName, 0L);                    
                 }
                 result.put(groupName, result.get(groupName) + entry.getValue().getConnection(groupName).getDroppedMessages());
             }
         }
         
         return result;
     }
 }
