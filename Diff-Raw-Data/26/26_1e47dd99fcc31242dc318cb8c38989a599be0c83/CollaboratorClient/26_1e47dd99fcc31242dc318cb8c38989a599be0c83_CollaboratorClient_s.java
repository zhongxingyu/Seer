 package coed.collab.client;
 
 import coed.base.common.ICoedCollaborator;
 import coed.base.common.ICoedObject;
 import coed.base.common.ICollabObject;
 import coed.base.common.ICollabStateObserver;
 import coed.base.data.CoedFileLine;
 import coed.base.data.CoedFileLock;
 import coed.base.data.IFileObserver;
 import coed.base.data.exceptions.NotConnectedToServerException;
 import coed.collab.client.config.ICoedConfig;
 import coed.collab.protocol.SendChangesMsg;
 
 public class CollaboratorClient implements ICoedCollaborator {
 	
 	private ServerConnection conn;
 	private int nrOnlineFiles;
 	
 	String host;
 	int port;
 
 	public CollaboratorClient(ICoedConfig conf) {
 		nrOnlineFiles = 0;
 		//goOffline();
 		
 		//host = conf.getString("server.host");
 		//port = conf.getInt("server.port");
 	}
 	
 	public void ensureConnected() throws NotConnectedToServerException {
 		if(getState() != STATUS_CONNECTED)
 			throw new NotConnectedToServerException();
 	}
 
 	@Override
 	public String getState() {
 		if(nrOnlineFiles == 0) return STATUS_OFFLINE;
 		return conn.isConnected() ? STATUS_CONNECTED : STATUS_ERROR;
 	}
 	
 	@Override
 	public void addStateListener(ICollabStateObserver stateObserver) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void removeStateListener(ICollabStateObserver stateObserver) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public ICollabObject makeCollabObject(ICoedObject obj) {
		boolean isFile = true; // TODO: this
		if(isFile) {
 			return new CoedCollabFile(obj, this);
 		} else
 			return new CoedCollabFolder(obj, this);
 	}
 	
 	public void incNrOnline() {
 		if(nrOnlineFiles == 0) {
 			conn = new ServerConnection("localhost", 1234);
 		}
 		
 		nrOnlineFiles++;
 	}
 	
 	public void decNrOnline() {
 		nrOnlineFiles--;
 		if(nrOnlineFiles == 0) {
 			if(conn != null) {
 				conn.shutdown();
 				conn = null;
 			}
 		}
 	}
 }
