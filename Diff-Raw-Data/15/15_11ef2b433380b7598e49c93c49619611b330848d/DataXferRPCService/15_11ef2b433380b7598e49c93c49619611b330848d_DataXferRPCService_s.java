 package edu.uw.cs.cse461.Net.RPC;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.util.Base64;
 import edu.uw.cs.cse461.util.Log;
 
 public class DataXferRPCService extends NetLoadableService {
 	private static final String TAG="DataXferRPCService";
 	
 	public static final int MAX_PACKET_SIZE = 1000;
 
 	private boolean isUp = true;
 	private RPCCallableMethod dataxfer;
 	
 	public DataXferRPCService() throws Exception {
 		super("dataxferrpc", true);
 
 		// Set up the method descriptor variable to refer to this->_echo()
 		dataxfer = new RPCCallableMethod(this, "_dataxfer");
 		// Register the method with the RPC service as externally invocable method "echo"
 		((RPCService)NetBase.theNetBase().getService("rpc")).registerHandler(loadablename(), "dataxfer", dataxfer);
 		
 		isUp = true;
 	}
 
 	/**
 	 * This method is required in every RPCCallable class.  If this object has created any 
 	 * threads, it should cause them to terminate.
 	 */
 	@Override
 	public void shutdown() {
 		Log.d(TAG, "Shutting down");
 		isUp = false;
 		super.shutdown();
 	}
 	
 	@Override
 	public String dumpState() {
 		return loadablename() + (isUp ? " is up" : " is down");
 	}
 	
 	/**
 	 * This method is callable by RPC (because of the actions taken by the constructor).
 	 * <p>
 	 * All RPC-callable methods take a JSONObject as their single parameter, and return
 	 * a JSONObject.  (The return value can be null.)  This particular method takes an
 	 * integer argument "xferLength" and sends back that number of bytes as a String
 	 * under the key "data". 
 	 * @param args {"xferLength": int} 
 	 * @return {"data": String} of size xferLength, or size 0 if xferLength is < 0
 	 * @throws JSONException
 	 */
 	public JSONObject _dataxfer(JSONObject args) throws JSONException, IOException {
 		// ANDROID INCOMPATIBILITY
 		//JSONObject result = new JSONObject(args, JSONObject.getNames(args));
 		
 		JSONObject result = new JSONObject();
 		
 		int xferLength = args.getInt("xferLength");
 		// catch bad arguments
 		if (xferLength < 0) {
 			xferLength = 0;
 		}
 		
 		byte[] data = new byte[xferLength];
 		result.put("data", Base64.encodeBytes(data));
 
 		return result;
 	}
 }
 
