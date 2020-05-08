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
			/*
			String str = new String(bytes);
			Log.d("ANE", "My ByteArray is long " + bytes.length + " bytes and contains '" +  str + "'");
			ba.release();
			*/
 			String base64str = Base64.encodeToString(bytes, Base64.DEFAULT);
 			base64 = FREObject.newObject(base64str);
 			
 	     } catch (Exception e) {
 	            Log.e("ANE", "An error occurred while reading the ByteArray", e);
 	     }
	    
		/*
	    FREAcquireByteArray(argv[0], byteArray);
	    NSData *byteData = [NSData dataWithBytes:(void *)byteArray.bytes length:(NSUInteger)byteArray.length];
	    NSString *base64str = [Base64Encoder base64EncodedString:byteData];
	    FRENewObjectFromUTF8(strlen([base64str UTF8String]), (const uint8_t *)[base64str UTF8String], &base64);
	    FREReleaseByteArray(argv[0]);
	    
	    return base64;
		*/
		
 		return base64;
 	}
 
 }
