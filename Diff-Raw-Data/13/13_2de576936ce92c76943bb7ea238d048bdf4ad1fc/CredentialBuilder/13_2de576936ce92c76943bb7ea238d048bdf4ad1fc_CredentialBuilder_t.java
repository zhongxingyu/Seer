 package com.blackbaud.integration;
 
import com.blackbaud.integration.generated.types.Credential;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import lombok.Data;
 import lombok.NonNull;
 import org.apache.commons.codec.binary.Base64;
 
 /**
  * Builds {@link Credential}s for a particular API user.
  */
 @Data
 public final class CredentialBuilder {
 
 		@NonNull
 		private final String apiUserName;
 		@NonNull
 		private final String sharedSymmetricKey;
 
 		/**
 		 * @return a {@link Credential} authenticated for the current moment in time
 		 * @throws RuntimeException if SHA256 HMAC cannot be calculated
 		 */
 		public Credential sign() throws RuntimeException {
 				return sign(new Date().getTime());
 		}
 
 		/**
 		 * @param millisSinceEpoch
 		 * @return a {@link Credential} authenticated for a particular moment in time
 		 * @throws RuntimeException if SHA256 HMAC cannot be calculated
 		 */
 		Credential sign(long millisSinceEpoch) throws RuntimeException {
 
 				final String toSign = apiUserName + millisSinceEpoch;
 
 				final String algo = "HmacSHA256";
 
 				try
 				{
 						final Mac mac = Mac.getInstance(algo);
 						mac.init(new SecretKeySpec(sharedSymmetricKey.getBytes(), algo));
 						final String hmacSignature = Base64.encodeBase64String(mac.doFinal(toSign.getBytes()));
 
 						return new Credential(apiUserName, millisSinceEpoch, hmacSignature);
 				}
 				catch (NoSuchAlgorithmException ex)
 				{
 						throw new RuntimeException(ex);
 				}
 				catch (InvalidKeyException ex)
 				{
 						throw new RuntimeException(ex);
 				}
 		}
 }
