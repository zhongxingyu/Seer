 package snjdck;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.util.logging.Logger;
 
 import snjdck.core.ClientState;
 import snjdck.core.IClient;
 import snjdck.core.IPacketDispatcher;
 import snjdck.ioc.tag.Inject;
 import snjdck.nio.IPacket;
 import snjdck.nio.IoSession;
 import snjdck.nio.impl.Packet;
 import snjdck.nio.io.PacketReader;
 import snjdck.nio.io.PacketWriter;
 
 public class Client implements IClient, IoSession
 {
 	@Inject
 	public ClientManager clientMgr;
 	
 	private static final Logger logger = Logger.getLogger(Client.class.getName());
 	static private final Charset charset = Charset.forName("UTF-8");
 	
 	final private int id;
 	private final IPacketDispatcher packetDispatcher;
 	private IPacket packetFactory;
 	
 	public Client(int id, SelectionKey selectionKey, IPacketDispatcher packetDispatcher)
 	{
 		this.id = id;
 		this.packetDispatcher = packetDispatcher;
 		this.selectionKey = selectionKey;
 		
 		packetFactory = new Packet();
 		packetReader = new PacketReader(this, 0x20000, packetFactory.create());
 		packetWriter = new PacketWriter(this, 0x10000);
 	}
 	
 	@Override
 	public void handlePacket(IPacket packet)
 	{
 		packetDispatcher.dispatch(this, packet);
 	}
 
 	@Override
 	public void onLogin()
 	{
 		logger.info("client enter!");
 	}
 
 	@Override
 	public void onLogout()
 	{
 		logout();
 		logger.info("client quit!");
 	}
 
 	public int getID()
 	{
 		return id;
 	}
 	
 	@Override
 	public ClientState state()
 	{
 		return state;
 	}
 	
 	@Override
 	public void state(ClientState newState)
 	{
 		state = newState;
 	}
 
 	public void login()
 	{
 		clientMgr.addClient(this);
 	}
 	
 	@Override
 	public void onConnected()
 	{
 		logger.info("client connected!");
 	}
 
 	public void logout()
 	{
 		selectionKey.cancel();
 		try{
 			selectionKey.channel().close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		clientMgr.removeClient(this);
 	}
 	
 	@Override
 	public void onReadyRecv()
 	{
 		logger.info("nio ready recv");
 		packetReader.onRecv();
 		while(packetReader.hasPacket()){
 			handlePacket(packetReader.shiftPacket());
 		}
 	}
 	
 	@Override
 	public void onReadySend()
 	{
 		logger.info("nio ready send");
 		packetWriter.onSend();
 	}
 	
 	public void send(int msgId, byte[] msg)
 	{
 		packetWriter.send(packetFactory.create(msgId, msg));
 	}
 	
 	public void interestReadOp()
 	{
 		selectionKey.interestOps(SelectionKey.OP_READ);
 	}
 	
 	public void interestWriteOp()
 	{
 		selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
 	}
 	
 	private SocketChannel getChannel()
 	{
 		return (SocketChannel)selectionKey.channel();
 	}
 	
 	public int doRead(ByteBuffer dst)
 	{
		int bytesRead = 0;
 		try{
			bytesRead = getChannel().read(dst);
 		}catch(IOException e){
 			e.printStackTrace();
 			onLogout();
 		}
		if(bytesRead < 0){
			onLogout();
		}
		return bytesRead;
 	}
 	
 	public int doWrite(ByteBuffer src)
 	{
		int bytesWrite = 0;
 		try{
			bytesWrite = getChannel().write(src);
 		}catch(IOException e){
 			e.printStackTrace();
 			onLogout();
 		}
		if(bytesWrite < 0){
			onLogout();
		}
		return bytesWrite;
 	}
 
 	private ClientState state = ClientState.CONNECTED;
 	
 	private final SelectionKey selectionKey;
 	
 	private final PacketReader packetReader;
 	private final PacketWriter packetWriter;
 }
