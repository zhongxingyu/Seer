 package protocol;
 
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBException;
 
 import properties.NioProperties;
 import properties.StanzaProperties;

 import session.AdminSession;
 import stanzasUtils.StanzaAndType;
 import stanzasUtils.Stanzas;
 import stanzasUtils.StanzasReader;
 import utility.Administrator;
 import utility.Attachment;
 import utility.DummieServer;
 import utility.Statistics;
 import exception.InvalidBytesException;
 import exception.NoUserConnectedException;
 import filters.FilterChain;
 import filters.Monitor;
 
 public class XmppProtocol implements TCPProtocol{
 
 	private int BUFFERSIZE;
 	private int SERVER;
 	private int CLIENT;
 	private int ADMIN;
 	private FilterChain chain = FilterChain.getInstance();
 	private AdminSession session;
 
 	public XmppProtocol() {
 		NioProperties nioProperties = NioProperties.getInstance();
 		this.BUFFERSIZE = nioProperties.getBufferSize();
 		this.SERVER = nioProperties.getServerType();
 		this.CLIENT = nioProperties.getClientType();
 		this.ADMIN = nioProperties.getAdminType();
 	}
 
 	@Override
 	public void handleAccept(SelectionKey key) throws IOException {
 
 		System.out.println("Start : ACCEPT");
 
 		//-------------------------ACCEPT A NEW CONECTION-------------------------
 		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
 		clntChan.configureBlocking(false);
 		int type = ((Attachment)key.attachment()).getType();
 		SelectionKey cKey = clntChan.register(key.selector(), SelectionKey.OP_READ, new Attachment(Attachment.NEGOTIATING,null, null, type, BUFFERSIZE));
 		Attachment att = (Attachment) cKey.attachment();
 		att.setChannel(clntChan);
 		att.setKey(cKey);
 		//------------------------------------------------------------------------
 
 		//-------------STADISTICS---------------------
 		Statistics statistics = Statistics.getInstance();
 		statistics.addAccess();
 		statistics.addUserConnected();
 		//-------------------------------------------
 
 		System.out.println("Finish : ACCEPT");
 
 	}
 
 	@Override
 	public void handleRead(SelectionKey key) throws IOException, JAXBException, NoUserConnectedException, InvalidBytesException {
 		//----------------CLIENT OR SERVER--------------------
 		int type = ((Attachment) key.attachment()).getType();
 		//----------------------------------------------------
 
 		//-------------------------GET THIS SIDE THINGS-------------------------
 		Attachment att =  ((Attachment) key.attachment());
 		SocketChannel chan = (SocketChannel) key.channel();
 		//----------------------------------------------------------------------
 
 		//-----------------------GET THE OTHER SIDE THINGS----------------------
 		SelectionKey aKey = att.getKey();
 		Attachment aAtt = ((Attachment) aKey.attachment());
 		SocketChannel otherChan = aAtt.getChannel();
 		//----------------------------------------------------------------------
 
 
 		//-------------SETTING LOCAL BUFFER-----------
 		ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);
 		int bytesRead = chan.read(buf);
 		//----------------------------------------------------
 
 		Statistics statistics = Statistics.getInstance();
 
 		if(bytesRead == -1){
 
 			//--------CIERRO CONEXION--------
 			chan.close();
 			key.cancel();
 			otherChan.close();
 			aKey.cancel();
 			statistics.removeUserConnected();
 			//-------------------------------
 
 		}else if (bytesRead >= 0) {
 			statistics.addBytesRcv(bytesRead);
 
 			if(att.isNegotiating()) {
 
 				if(type == ADMIN || type == CLIENT){
 					
 					String stanzaToParse1 = "";
 					if(att.getStanzas() != null && att.getStanzas().getIncompleteStanza() != null){
 						stanzaToParse1 = new String(att.getStanzas().getIncompleteStanza().array());
 					}
 					String stanzaToParse2 = new String(buf.array(),0,bytesRead);
 					String totalStanza = (stanzaToParse1 + stanzaToParse2);
 					ByteBuffer buffToParse = ByteBuffer.wrap(totalStanza.getBytes());
 									System.out.println("READ : " + totalStanza);
 					//-------------------------------------------------------------------------------------
 
 					att.concatStanza(StanzasReader.hasSomething(buffToParse));
 					
 					
 					Stanzas stanza = att.getStanzas();
 					System.out.println("-------");
 					stanza.printAll();
 					System.out.println("-------");
 					if(stanza.hasMore()){
 						ByteBuffer received = stanza.peekNextStanza();
 						ByteBuffer dummBuff = DummieServer.chekcStanza(stanza);
 						if(dummBuff != null){
 							att.setStanza(StanzasReader.hasSomething(dummBuff));
 							Attachment.hostSetter(received, att);
 							 if(Attachment.userSetter(stanza, att)){
 								System.out.println("test");
 								String host;
 								String fullUser = att.getUser().concat("@"+ att.getHost());
 								if(chain.getmFilter().checkUser(fullUser)){
 									host = chain.getmFilter().getHost(fullUser);
 									System.out.println(host);
 								}else{
 									host = att.getHost();
 								}
 								SocketChannel serverChannel = SocketChannel.open(new InetSocketAddress(host,5222));
 								serverChannel.configureBlocking(false);
								SelectionKey serverKey =  serverChannel.register(key.selector(), SelectionKey.OP_WRITE, new Attachment(Attachment.NEGOTIATION_DONE,chan, key, SERVER, BUFFERSIZE));
 								aAtt = (Attachment) serverKey.attachment();
 								att.setChannel(serverChannel);
 								att.setKey(serverKey);
 								att.setStanza(null);
 								Stanzas stanzaAux = new Stanzas();
 								att.setStanza(stanzaAux);
 								att.setNegotiating(Attachment.NEGOTIATION_DONE);
 								aAtt.concatStanza(StanzasReader.hasSomething(DummieServer.getFirstNegotiation(att.getHost(), att.getEncodedUserPwd())));
 								key.interestOps(SelectionKey.OP_READ);
 								return;
 							}
 							key.interestOps(SelectionKey.OP_WRITE);
 						}
 					}
 				}else if(type == SERVER){
 					Stanzas stanza = StanzasReader.hasSomething(buf);
 					if(stanza.hasMore()){
 						att.setStanza(null);
 						aAtt.setStanza(null);
 						att.setNegotiating(Attachment.NEGOTIATION_DONE);
 					}
 				}
 			}else{
 
 
 
 				String stanzaToParse1 = "";
 				if(aAtt.getStanzas() != null && aAtt.getStanzas().getIncompleteStanza() != null){
 					stanzaToParse1 = new String(aAtt.getStanzas().getIncompleteStanza().array());
 				}
 				String stanzaToParse2 = new String(buf.array(),0,bytesRead);
 				String totalStanza = (stanzaToParse1 + stanzaToParse2);
 				ByteBuffer buffToParse = ByteBuffer.wrap(totalStanza.getBytes());
 				//				System.out.println(totalStanza);
 				//-------------------------------------------------------------------------------------
 
 				aAtt.concatStanza(StanzasReader.hasSomething(buffToParse));
 
 				if(aAtt.getStanzas().hasMore() && bytesRead  != BUFFERSIZE){
 					aKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
 				}
 				key.interestOps(SelectionKey.OP_READ);
 			}
 		}
 	}
 
 
 
 	@Override
 	public void handleWrite(SelectionKey key) throws IOException, JAXBException, InvalidBytesException {
 
 		//----------------CLIENT OR SERVER--------------------
 		int type = ((Attachment) key.attachment()).getType();
 		//----------------------------------------------------
 
 
 		//-----------------------GET MY SIDE THINGS----------------------
 		Attachment att = ((Attachment) key.attachment());
 		Stanzas stanzas = att.getStanzas();
 		if(stanzas != null){
 //			stanzas.printAll();
 		}
 		SocketChannel chan = (SocketChannel) key.channel();
 		//---------------------------------------------------------------
 
 
 
 		//-----------------------GET THE OTHER SIDE THINGS----------------------
 		SelectionKey aKey = att.getKey();
 		Attachment aAtt = (Attachment) aKey.attachment();
 		int aType = aAtt.getType();
 		//----------------------------------------------------------------------
 
 
 
 
 		StanzaAndType myStanza = stanzas.getNextStanzaAndType();
 		Statistics statistics = Statistics.getInstance();
 		int bytesWrited = 0;
 		ByteBuffer localBuff = null;
 
 		if(!att.isNegotiating()){
 			if(aType == ADMIN){
 				Administrator.adminstrate(myStanza, chain, aAtt);
 				StanzaProperties stanzaProperties = StanzaProperties.getInstance();
 				if(myStanza.getStringType().equals(stanzaProperties.getMessageName())){
 					String monitor = Administrator.parseBody(myStanza.getStanza(), aAtt);
 					if(monitor != null){
 						Pattern pattern = Pattern.compile("^(MONITOR)$");
 						Matcher matcher = pattern.matcher(monitor);
 						if(matcher.find()){
 							Monitor mFilter = Monitor.getInstance();
 							mFilter.execute( att,  aAtt,  stanzas,  myStanza, type);
 						}
 					}
 				}
 			}else if (type != ADMIN){
 				String user = statistics.getUserFromIq(myStanza, type);
 				if (user != null) {
 					aAtt.setUser(user);
 					aAtt.setResources(user);
 				}
 				chain.doFilter(att, aAtt, stanzas, myStanza, type);
 				statistics.connectSpecificUsers(myStanza, type);
 			}
 			if (myStanza.getStanza() != null) {
 				localBuff = ByteBuffer.wrap(myStanza.getStanza().array());
 				bytesWrited = chan.write(localBuff);
 				statistics.addBytesSend(bytesWrited);
 			}
 
 			if ((localBuff == null) || (!localBuff.hasRemaining() && !stanzas.hasMore()))  {
 				key.interestOps(SelectionKey.OP_READ );
 			}
 			
 		}else{
 			System.out.println("-------");
 			System.out.println("Stanza : " + new String(myStanza.getStanza().array()));
 			System.out.println("-------");
 			localBuff = ByteBuffer.wrap(myStanza.getStanza().array());
 			bytesWrited = chan.write(localBuff);
 			statistics.addBytesSend(bytesWrited);
 			if (!localBuff.hasRemaining() && !stanzas.hasMore())  {
 				att.setStanza(new Stanzas());
 				aAtt.setStanza(new Stanzas());
 				key.interestOps(SelectionKey.OP_READ );
 			}
 		}
 	}
 }
