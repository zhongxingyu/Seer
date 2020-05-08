 package security;
 import java.security.PublicKey;
 
 import dht.DistributedMap;
 import dht.ValueEvent;
 import dht.ValueListener;
 public class SecureUserTable {
 	//protected SecureMessageHandler messageHandler;
 	protected DistributedMap<PublicKey, SignedMessage> dt;
 	public SecureUserTable() {
		dt = new DistributedMap();
 		//messageHandler = new SecureMessageHandler();
 	}
 	public void get(final PublicKey key, final ValueListener<byte[]> handler) {
 		
 		dt.get(key, new ValueListener<SignedMessage>(){
 			public void valueComplete(final ValueEvent<SignedMessage> val){
 				final SecureMessageHandler messageHandler = new SecureMessageHandler();
 				if(messageHandler.verifyMessage(val.getValue().getMessage(), val.getValue().getSignature(), key) && val.exists())
 					handler.valueComplete(new ValueEvent<byte[]>(val.getValue().getMessage()));
 				else
 					handler.valueComplete(new ValueEvent<byte[]>());
 			}
 		});
 		
 
 		
 	}
 	
 	public void put(PublicKey key, SignedMessage val) {
 		//final SecureMessageHandler messageHandler = new SecureMessageHandler();
 		dt.put(key, val);
 	}
 }
