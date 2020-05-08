 package net.dlogic.kryonet.server;
 
 import java.io.IOException;
 
 import com.esotericsoftware.minlog.Log;
 
 public class KryonetServerInstance {
 	private static KryonetServer server;
 	public static void initialize(int writeBufferSize, int objectBufferSize) throws IOException, KryonetServerException {
 		Log.info("KryonetServerInstance.initialize(" + writeBufferSize + ", " + objectBufferSize + ")");
 		if (server != null) {
			throw new KryonetServerException(KryonetServerException.ALREADY_INITIALIZED);
 		}
 		server = new KryonetServer(writeBufferSize, objectBufferSize);
 	}
 	public static KryonetServer getInstance() throws KryonetServerException {
 		Log.info("KryonetServerInstance.getInstance()");
 		if (server == null) {
			throw new KryonetServerException(KryonetServerException.NOT_INITIALIZED);
 		}
 		return server;
 	}
 }
