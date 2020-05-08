 package net.tangamampilia.functions;
 
 import java.nio.ByteBuffer;
 
 import android.util.Base64;
 import android.util.Log;
 
 import com.adobe.fre.FREContext;
 import com.adobe.fre.FREFunction;
 import com.adobe.fre.FREObject;
 import com.adobe.fre.FREByteArray;
 
 public class Encode implements FREFunction {
 
 	@Override
 	public FREObject call(FREContext context, FREObject[] args) {
 		
		// TODO Auto-generated method stub		
 		FREObject base64 = null;
 		
 		try {
 			FREByteArray ba = (FREByteArray) args[0];
						
 			ba.acquire();
 			ByteBuffer bb = ba.getBytes();
 			byte[] bytes = new byte[(int) ba.getLength()];
 			bb.get(bytes);
			
 			String base64str = Base64.encodeToString(bytes, Base64.DEFAULT);
			
 			base64 = FREObject.newObject(base64str);
			ba.release();
 			
 	     } catch (Exception e) {
 	            Log.e("ANE", "An error occurred while reading the ByteArray", e);
 	     }
	    		
 		return base64;
 	}
 
 }
