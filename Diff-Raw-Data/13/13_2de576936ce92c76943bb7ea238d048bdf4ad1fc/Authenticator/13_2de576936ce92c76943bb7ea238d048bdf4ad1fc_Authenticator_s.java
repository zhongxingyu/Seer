 package com.blackbaud.integration;
 
 import com.blackbaud.integration.SharedKeyProvider.NoSuchUserException;
import com.blackbaud.integration.errors.AuthenticationFailureCode;
import com.blackbaud.integration.errors.FailedAuthenticationException;
import com.blackbaud.integration.types.Credential;
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import java.util.Date;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 import javax.inject.Inject;
 import javax.inject.Named;
 import lombok.extern.slf4j.Slf4j;
 
 /**
  * Supports authentication of thrift API requests.
  */
 @Slf4j
 public final class Authenticator {
 
 		private final SharedKeyProvider sharedKeyProdiver;
 		private final long maxCredentialAgeMillis;
 
 		@Inject
 		public Authenticator(
 						SharedKeyProvider sharedKeyProdiver,
 						@Named("maxCredentialAgeMillis") long maxCredentialAgeMillis) {
 				
 				this.sharedKeyProdiver = Preconditions.checkNotNull(sharedKeyProdiver);
 
 				Preconditions.checkArgument(maxCredentialAgeMillis > 0, "Credential's max age must be greater than zero.");
 				this.maxCredentialAgeMillis = maxCredentialAgeMillis;
 		}
 
 		/**
 		 * Checks whether the submitted {@link Credential} is currently valid.
 		 *
 		 * @param cred
 		 * @throws FailedAuthenticationException if the submitted credential is not valid; see also {@link AuthenticationFailureCode} for possible reasons.
 		 */
 		public void authenticate(Credential cred) throws FailedAuthenticationException {
 				Preconditions.checkNotNull(cred);
 
 				if (isCredentialStale(cred))
 				{
 						throw new FailedAuthenticationException(cred, AuthenticationFailureCode.EXPIRED_CREDENTIAL);
 				}
 
 				if (isCredentialAuthentic(cred))
 				{
 						throw new FailedAuthenticationException(cred, AuthenticationFailureCode.INVALID_CREDENTIAL);
 				}
 
 		}
 
 		boolean isCredentialStale(Credential cred) {
 				final long now = new Date().getTime();
 				return Math.abs(now - cred.getMillisSinceEpoch()) > maxCredentialAgeMillis;
 		}
 
 		boolean isCredentialAuthentic(Credential cred) {
 
 				// make timing based crypto attacks more difficult
 				try
 				{
 						TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10));
 				}
 				catch (InterruptedException ex)
 				{
 						log.warn(ex.getMessage(), ex);
 				}
 
 				// check submitted credential
 				try
 				{
 						final String sharedKey = sharedKeyProdiver.lookupKey(cred.getApiUserName());
 						
 						final CredentialBuilder expectedCredentialBuilder =	
 										new CredentialBuilder(cred.getApiUserName(), sharedKey);
 
 						final Credential expectedCredential = expectedCredentialBuilder.sign(cred.getMillisSinceEpoch());
 
 						return Objects.equal(expectedCredential.getHmacSha256(), cred.getHmacSha256());
 
 				}
 				catch (NoSuchUserException ex)
 				{
 						log.info(ex.getMessage());
 						return false;
 				}
 		}
 }
