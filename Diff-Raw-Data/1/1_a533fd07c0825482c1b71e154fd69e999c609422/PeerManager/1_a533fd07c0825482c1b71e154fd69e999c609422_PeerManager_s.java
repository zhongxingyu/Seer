 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import music.IMusicLibrary;
 import music.MusicLibrary;
 
 import org.apache.log4j.Logger;
 import org.omg.CORBA.DATA_CONVERSION;
 
 import at.ac.tuwien.software.architectures.ws2012.General;
 import at.ac.tuwien.software.architectures.ws2012.General.PeerData;
 import at.ac.tuwien.software.architectures.ws2012.General.PeerRegistrationStatus;
 import at.ac.tuwien.software.architectures.ws2012.General.Request;
 import at.ac.tuwien.software.architectures.ws2012.General.RequestType;
 import at.ac.tuwien.software.architectures.ws2012.General.SearchRequest;
 import at.ac.tuwien.software.architectures.ws2012.General.SongData;
 import at.ac.tuwien.software.architectures.ws2012.General.ValidateSearchStatus;
 import at.ac.tuwien.software.architectures.ws2012.Peer;
 import at.ac.tuwien.software.architectures.ws2012.Peer.AreYouAliveRequest;
 import at.ac.tuwien.software.architectures.ws2012.Peer.SearchDeniedRequest;
 import at.ac.tuwien.software.architectures.ws2012.Peer.SearchSuccesful;
 import at.ac.tuwien.software.architectures.ws2012.Peer.SearchUnsuccesfulRequest;
 import at.ac.tuwien.software.architectures.ws2012.Server;
 import at.ac.tuwien.software.architectures.ws2012.Server.BootstrapRequest;
 import at.ac.tuwien.software.architectures.ws2012.Server.PeerDeadRequest;
 import at.ac.tuwien.software.architectures.ws2012.Server.RegisterPeerResponse;
 import at.ac.tuwien.software.architectures.ws2012.Server.ValidateSearchRequest;
 
 import com.google.protobuf.ByteString;
 
 
 public class PeerManager {
 	static Logger log=Logger.getLogger(PeerManager.class.toString());	
 	
 	String serverAddress;
 	int numOfPeers;
 	String musicfolder;
 	AddressedRequestQueue outqueue;
 	String listenAddress;
 	int reqid;
 	ConnectionManager connManager;
 	Map<ByteString,SearchData> searchMap;
 	
 	IMusicLibrary musicLib = new MusicLibrary("sampleAudio/");
 	
 	int searchHopsToLive;
 	int clientID;
 	
 	public PeerManager(AddressedRequestQueue out, String srvr, int peers, String music, String localadd, ConnectionManager mgr, int hopsToLive, int clientid)
 	{
 		serverAddress=srvr;
 		numOfPeers=peers;
 		musicfolder=music;
 		outqueue = out;
 		listenAddress=localadd;
 		reqid=0;
 		connManager = mgr;
 		searchHopsToLive=hopsToLive;
 		searchMap=new HashMap<ByteString, SearchData>();
 		clientID=clientid;
 	}
 	
 	public void RegisterPeer(AddressedRequest req) {
 		Request request=req.req;
 		RegisterPeerResponse regResponse=request.getExtension(Server.registerPeerResponse);
 		
 		PeerRegistrationStatus status = regResponse.getStatus();
 		if (status==PeerRegistrationStatus.PEER_OK)
 		{
 			BootstrapRequest boots=BootstrapRequest.newBuilder().setNumberOfPeers(numOfPeers).build();
 			Request main=Request.newBuilder().setRequestId(2).setRequestType(RequestType.BOOTSTRAP_REQUEST).setTimestamp((new Date()).getTime()).setListenAddress(listenAddress).setExtension(Server.bootstrapRequest, boots).build();
 			outqueue.addElement(new AddressedRequest(main, serverAddress, true));
 		}
 	}
 	
 	public void BootstrapResponse(AddressedRequest req)
 	{
 		Request request=req.req;
 		at.ac.tuwien.software.architectures.ws2012.Server.BootstrapResponse bsresponse=request.getExtension(Server.bootstrapResponse);
 		log.info(String.format("Received bootstrap response: %d contacts", bsresponse.getDataCount()));
 		
 		List<PeerData> peerlist = bsresponse.getDataList();
 		Iterator<PeerData> it=peerlist.iterator();
 		while(it.hasNext())
 		{
 			PeerData peer=it.next();
 			String peerAddress=peer.getPeerAddress();
 			log.info(String.format("Trying to connect to peer %s", peerAddress));
 			checkAlive(peerAddress);
 		}
 	}
 
 	public void AreYouAlive(AddressedRequest req) {
 		Request request=req.req;
 		AreYouAliveRequest areUalive=request.getExtension(Peer.areYouAliveRequest);
 		
 		String peerAddress=req.address;
 		String listenAddress=request.getListenAddress();
 
 		if (!req.server_conn)
 			UpdateActual(peerAddress, listenAddress);
 		
 		Request response = Request.newBuilder().
 				setRequestType(RequestType.ARE_YOU_ALIVE_RESPONSE).
 				setRequestId(request.getRequestId()).
 				setListenAddress(listenAddress).
 				setTimestamp((new Date()).getTime()).
 				build();
 		outqueue.addElement(new AddressedRequest(response, peerAddress, false));
 	}
 
 	private void UpdateActual(String peerAddress, String listenaddr) {
 		Connection conn;
 		conn=connManager.GetConnection(peerAddress);
 		if (conn!=null)
 		{
 			conn.listenAddress=listenaddr;
 			conn.actualAddress=peerAddress;
 		}
 		else 
 		{
 			log.error("IMPOSSIBLE cannot have this");
 		}
 	}
 
 	public void AreYouAliveResponse(AddressedRequest req) {
 		Request request=req.req;
 		at.ac.tuwien.software.architectures.ws2012.Peer.AreYouAliveResponse resp=request.getExtension(Peer.areYouAliveResponse);
 		
 		String actual=req.address;
 		String listen=request.getListenAddress();
 		UpdateActual(actual, listen);
 		
 		connManager.connectionLive(req.address, new Date());
 	}
 
 	public void reportDead(String name, String listen) {
 		PeerDeadRequest peerDead=PeerDeadRequest.newBuilder().setDestinationPeer(listen).build();
 		Request request=Request.newBuilder().setRequestId(++reqid).setListenAddress(listenAddress).setRequestType(RequestType.PEER_DEAD_REQUEST).setExtension(Server.peerDeadRequest, peerDead).build();
 		
 		log.info(String.format("Reporting dead peer: %s", name));
 		outqueue.addElement(new AddressedRequest(request, serverAddress, true));
 	}
 
 	private String GetListen(String name) {
 		Connection conn;
 		conn=connManager.GetConnection(name);
 		if (conn!=null)
 			return conn.listenAddress;
 			
 		return "";
 	}
 
 	public void checkAlive(String address) {
 		AreYouAliveRequest areyoualive=AreYouAliveRequest.newBuilder().setDestinationPeer(listenAddress).build();
 		Request containerreq=Request.newBuilder().setRequestId(++reqid).setListenAddress(listenAddress).setRequestType(RequestType.ARE_YOU_ALIVE_REQUEST).setTimestamp((new Date()).getTime()).setExtension(Peer.areYouAliveRequest, areyoualive).build();
 		outqueue.addElement(new AddressedRequest(containerreq, address, false));
 	}
 
 	//we first ask for validation from server, 
 	//if user has coins, we distribute request (SearchRequest is sent to other peers), search locally
 	//all the peers keep information about original peer, fingerprint, timeout.
 	//all the peers check if timeout has passed, when that happens, they remove information about 
 	//the search from their record
 	//if user does not have enough coins we immediately inform user that it is not possible to perform search
 	public void ClientSearchRequest(AddressedRequest req) {
 		Request request=req.req;
 		General.ClientSearchRequest csrch=request.getExtension(Peer.clientSearchRequest);
 		SearchData data=new SearchData(); 
 		data.clientAddress=req.address; 
 		data.clientID=csrch.getClientid();
 		data.fingerprint=csrch.getFingerprint();
 		data.originalPeerListen=listenAddress;
 		data.originalPeer=true;
 		data.timestamp=new Date();
 		
 		searchMap.put(data.fingerprint, data);
 		
 		SearchRequest srchrequest=SearchRequest.newBuilder().setClientid(data.clientID).setFingerprint(data.fingerprint).setHopsToLive(searchHopsToLive).setOriginalPeer(listenAddress).build();
 		data.request=srchrequest;
 		
 		ValidateSearchRequest validate=ValidateSearchRequest.newBuilder().setSearchRequest(srchrequest).build();
 		Request container=Request.newBuilder().setListenAddress(listenAddress).setRequestId(++reqid).setRequestType(RequestType.VALIDATE_SEARCH_REQUEST).setTimestamp((new Date()).getTime()).setExtension(Server.validateSearchRequest, validate).build();
 		
 		outqueue.addElement(new AddressedRequest(container, serverAddress, true));
 		log.info("new search request... validating");
 	}
 
 	public void ValidateSearchResponse(AddressedRequest req) {
 		Request request=req.req;
 		Server.ValidateSearchResponse validateResponse=request.getExtension(Server.validateSearchResponse);
 		
 		ValidateSearchStatus status = validateResponse.getSearchStatus();
 		SearchRequest search = validateResponse.getSearchRequest();
 		
 		SearchData data=searchMap.get(search.getFingerprint());
 		
 		
 			
 		
 		
 		//if denied just throw the search away and forget about it
 		if(status!=ValidateSearchStatus.SEARCH_OK)
 		{
 			SearchDeniedRequest inner=SearchDeniedRequest.newBuilder().setSearchRequest(search).build();
 			Request response=Request.newBuilder().setListenAddress(listenAddress).setTimestamp((new Date()).getTime()).setRequestId(++reqid).setRequestType(RequestType.SEARCH_DENIED_REQUEST).setExtension(Peer.searchDeniedRequest, inner).build();
 			outqueue.addElement(new AddressedRequest(response, data.clientAddress, false));
 			searchMap.remove(search.getFingerprint());
 			log.info("Search request denied");
 			return;
 		}
 		
 		//if allowed, first send search to all connected peers, then start searching locally
 		Request peersearch=Request.newBuilder().
 				setRequestId(++reqid).
 				setListenAddress(listenAddress).
 				setRequestType(RequestType.SEARCH_REQUEST).
 				setTimestamp((new Date()).getTime()).
 				setExtension(Peer.searchRequest, search).
 				build();
 		connManager.sendToPeers(peersearch);
 		
 		//start local search
 		
 	}
 
 	public void SearchRequest(AddressedRequest req) {
 		Request request=req.req;
 		SearchRequest search=request.getExtension(Peer.searchRequest);
 		
 		int hopsToLive=search.getHopsToLive();
 		
 		
 		if (searchMap.containsKey(search.getFingerprint()))
 		{
 			log.info("ignoring the search, duplicate");
 			return;
 		}else{
 			// do the local search
 			SearchData data=searchMap.get(search.getFingerprint());
 
 			try{
 				Fingerprint f= FingerprintUtil.deserializeFingerprint(data.fingerprint.toByteArray());
 
 				String songName = musicLib.matchSong(f);
 				if(songName != "NO"){
 					// TODO: Inform server found it
 					SongData dataa = SongData.getDefaultInstance();
 					dataa.setSongName(songName);
 					
 					this.HandleMusicFound(search, dataa);
 					// TODO: Inform client foind it
 					return; 
 				}
 				
 				}catch(Exception e){
 					
 			}
 		}
 		
 		if (hopsToLive==1)
 		{
 			log.info("maximum hops to live reached, performing only local search");
 		}
 		else
 		{
 			SearchRequest newsearch=SearchRequest.newBuilder(search).setHopsToLive(hopsToLive-1).build();
 			Request newreq=Request.newBuilder().
 					setRequestId(++reqid).
 					setListenAddress(listenAddress).
 					setRequestType(RequestType.SEARCH_REQUEST).
 					setTimestamp((new Date()).getTime()).
 					setExtension(Peer.searchRequest, newsearch).
 					build();	
 		}
 		
 		//perform local search
 		
 	}
 	
 	public void HandleMusicFound(SearchRequest search, SongData song )
 	{
 		SearchSuccesful success=SearchSuccesful.newBuilder().
 				setFounderClientid(clientID).
 				setSearchRequest(search).setSongData(song).build();
 		
 		Request found=Request.newBuilder().
 				setRequestId(++reqid).
 				setListenAddress(listenAddress).
 				setRequestType(RequestType.SEARCH_SUCCESFULL).
 				setTimestamp((new Date()).getTime()).
 				setExtension(Peer.searchSuccesful, success).
 				build();	
 		
 		SearchData data=searchMap.remove(search.getFingerprint());
 		
 		
 		outqueue.addElement(new AddressedRequest(found, serverAddress, true));
 		
 		//if original peer, send to client, otherwise, send to original peer
 		if (!search.getOriginalPeer().equals(listenAddress))
 			outqueue.addElement(new AddressedRequest(found, search.getOriginalPeer(), false));
 		else
 			outqueue.addElement(new AddressedRequest(found, data.clientAddress, false));
 	}
 	
 	public void HandleMusicNotFound(SearchRequest search)
 	{
 		SearchUnsuccesfulRequest unsuccess=SearchUnsuccesfulRequest.newBuilder().
 				setSearchRequest(search).build();
 		
 		Request notfound=Request.newBuilder().
 				setRequestId(++reqid).
 				setListenAddress(listenAddress).
 				setRequestType(RequestType.SEARCH_UNSUCCESFUL_REQUEST).
 				setTimestamp((new Date()).getTime()).
 				setExtension(Peer.searchUnsuccesfulRequest, unsuccess).
 				build();	
 
 		SearchData data=searchMap.remove(search.getFingerprint());
 		
 		outqueue.addElement(new AddressedRequest(notfound, serverAddress, true));
 
 		//if original peer, send to client, otherwise, send to original peer
 		if (!search.getOriginalPeer().contentEquals(listenAddress))
 			outqueue.addElement(new AddressedRequest(notfound, search.getOriginalPeer(), false));
 		else
 			outqueue.addElement(new AddressedRequest(notfound, data.clientAddress, false));
 	}
 	
 	public String addSong(String song){
 		return musicLib.addMusic(song);
 	}
 	
 	
 	public String removeSong(String song){
 		return musicLib.removeMusic(song);
 	}
 	
 	public void updateMusicLibrary(){
 		musicLib.updateLibrary();
 	}
 
 	public void searchSuccessful(AddressedRequest req) {
 		Request request=req.req;
 		SearchSuccesful success=request.getExtension(Peer.searchSuccesful);
 		
 		SearchData data=searchMap.remove(success.getSearchRequest().getFingerprint());
 		
 		Request clientResponse=Request.newBuilder().setRequestId(++reqid).setRequestType(RequestType.SEARCH_SUCCESFULL).setExtension(Peer.searchSuccesful, success).build();
 		
 		outqueue.addElement(new AddressedRequest(clientResponse, data.clientAddress, false));
 	}
 
 	public void searchUnsuccessful(AddressedRequest req) {
 		Request request=req.req;
 		SearchUnsuccesfulRequest failed=request.getExtension(Peer.searchUnsuccesfulRequest);
 		
 		SearchData data=searchMap.remove(failed.getSearchRequest().getFingerprint());
 		
 		Request clientResponse=Request.newBuilder().setRequestId(++reqid).setRequestType(RequestType.SEARCH_UNSUCCESFUL_REQUEST).setExtension(Peer.searchUnsuccesfulRequest, failed).build();
 		
 		outqueue.addElement(new AddressedRequest(clientResponse, data.clientAddress, false));
 	}
 }
