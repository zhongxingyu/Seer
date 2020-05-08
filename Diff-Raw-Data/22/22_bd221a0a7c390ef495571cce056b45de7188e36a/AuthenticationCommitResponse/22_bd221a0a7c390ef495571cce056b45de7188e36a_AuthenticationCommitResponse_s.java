 package net.link.safeonline.auth.ws.json;
 
 import com.google.common.base.Charsets;
 import net.link.safeonline.auth.ws.soap.AuthenticationStep;
 import org.bouncycastle.util.encoders.Base64;
 
 
 /**
  * <h2>{@link AuthenticationCommitResponse}<br> <sub>[in short] (TODO).</sub></h2>
  *
  * <p> <i>12 01, 2010</i> </p>
  *
  * @author lhunath
  */
 public class AuthenticationCommitResponse extends AuthenticationResponse {
 
     private final String userId;
     private final String deviceName;
     private final String b64Assertion;
 
     public AuthenticationCommitResponse(final String userId, final String deviceName, final byte[] assertion) {
 
        super( AuthenticationStep.SUCCESS );
 
         this.userId = userId;
         this.deviceName = deviceName;
         b64Assertion = new String( Base64.encode( assertion ), Charsets.UTF_8 );
     }
 
     public String getUserId() {
 
         return userId;
     }
 
     public String getDeviceName() {
 
         return deviceName;
     }
 
     public byte[] getAssertion() {
 
         return Base64.decode( b64Assertion );
     }
 }
