 package nz.ac.auckland.netlogin.negotiation;
 
 import com.sun.jna.NativeLong;
 import com.sun.jna.platform.win32.*;
 import com.sun.jna.ptr.NativeLongByReference;
 import nz.ac.auckland.netlogin.negotiation.win32.Secur32Ext;
 import javax.security.auth.login.LoginException;
 
 /**
  * SSPI is a Microsoft protocol for authenticating between client and server.
  * It is <a href="http://msdn.microsoft.com/en-us/library/aa380496(VS.85).aspx">interoperable with GSSAPI, subject to conditions</a>.
  */
 public class SSPIAuthenticator extends AbstractGSSAuthenticator {
 
     // http://code.dblock.org/jna-acquirecredentialshandle-initializesecuritycontext-and-acceptsecuritycontext-establishing-an-authenticated-connection
     // obtaining a spn - http://msdn.microsoft.com/en-us/library/ff649429.aspx
 
 	private static final String authenticationMechanism = "Kerberos";
 	private Sspi.CredHandle phClientCredential;
 	private Sspi.CtxtHandle phClientContext;
     private NativeLongByReference pfClientContextAttr;
     
     public SSPIAuthenticator() throws ClassNotFoundException {
         // attempt to load Secur32 to see if the native libs are available
         Class.forName("com.sun.jna.platform.win32.Secur32", true, SSPIAuthenticator.class.getClassLoader());
     }
 
     public String getName() {
         return "Windows Domain Account";
     }
 
     public byte[] unwrap(byte[] wrapper) throws LoginException {
 		final int SECBUFFER_STREAM = 10;
 
		Sspi.SecBuffer.ByReference sspiBuffer = new Sspi.SecBuffer.ByReference(SECBUFFER_STREAM, Sspi.MAX_TOKEN_SIZE);
		Sspi.SecBuffer.ByReference messageBuffer = new Sspi.SecBuffer.ByReference(Sspi.SECBUFFER_DATA, wrapper);
		Secur32Ext.SecBufferDesc2 buffers = new Secur32Ext.SecBufferDesc2(sspiBuffer, messageBuffer);
 
 		NativeLongByReference pfQOP = new NativeLongByReference();
 
 		int responseCode = Secur32Ext.INSTANCE.DecryptMessage(
 				phClientContext, buffers, new NativeLong(0), pfQOP);
 
		if (responseCode == W32Errors.SEC_E_OK) return messageBuffer.getBytes();
 		throw handleError(responseCode);
     }
 
     protected String getUserName() {
 		return Secur32Util.getUserNameEx(Secur32.EXTENDED_NAME_FORMAT.NameServicePrincipal);
     }
 
     protected void initializeContext() throws LoginException {
 		assert phClientCredential == null;
 		assert phClientContext == null;
 
         // client ----------- acquire outbound credential handle
         phClientCredential = new Sspi.CredHandle();
         Sspi.TimeStamp ptsClientExpiry = new Sspi.TimeStamp();
 
         // http://msdn.microsoft.com/en-us/library/windows/desktop/aa374713%28v=VS.85%29.aspx
         int responseCode = Secur32.INSTANCE.AcquireCredentialsHandle(
                 null,
                 authenticationMechanism,
                 new NativeLong(Sspi.SECPKG_CRED_OUTBOUND),
                 null,
                 null,
                 null,
                 null,
                 phClientCredential,
                 ptsClientExpiry);
 
         if (responseCode != W32Errors.SEC_E_OK) {
             throw new LoginException("Unable to get credentials handle");
         }
 
         // client ----------- security context
         phClientContext = new Sspi.CtxtHandle();
         pfClientContextAttr = new NativeLongByReference();
 
     }
 
     protected byte[] initSecContext(byte[] gssToken) throws LoginException {
 		assert phClientCredential != null;
 		assert phClientContext != null;
 
         // get the target name
         String targetPrincipal = GSSAPIAuthenticator.getServicePrincipalName();
 
         // the server token to use as input
         Sspi.SecBufferDesc pbServerToken = null;
         if (gssToken != null) pbServerToken = new Sspi.SecBufferDesc(Sspi.SECBUFFER_TOKEN, gssToken);
 
         // buffer to hold the client token
         Sspi.SecBufferDesc pbClientToken = new Sspi.SecBufferDesc(Sspi.SECBUFFER_TOKEN, Sspi.MAX_TOKEN_SIZE);
 
         // server token is empty the first time
         // http://msdn.microsoft.com/en-us/library/windows/desktop/aa375507%28v=VS.85%29.aspx
         int clientRc = Secur32.INSTANCE.InitializeSecurityContext(
                 phClientCredential,
                 phClientContext.isNull() ? null : phClientContext,
                 targetPrincipal,
                 new NativeLong(Sspi.ISC_REQ_CONNECTION | Sspi.ISC_REQ_MUTUAL_AUTH | Sspi.ISC_REQ_CONFIDENTIALITY | Sspi.ISC_REQ_INTEGRITY | Sspi.ISC_REQ_REPLAY_DETECT),
                 new NativeLong(0),
                 new NativeLong(Sspi.SECURITY_NATIVE_DREP), // vs SECURITY_NETWORK_DREP
                 pbServerToken,
                 new NativeLong(0),
                 phClientContext,
                 pbClientToken,
                 pfClientContextAttr,
                 null);
 
         if (clientRc == W32Errors.SEC_E_OK || clientRc == W32Errors.SEC_I_CONTINUE_NEEDED) {
             return pbClientToken.getBytes();
         }
 
         cleanup();
         throw handleError(clientRc);
 	}
 
     protected void cleanup() {
         // release client context
         if (phClientContext != null) {
             if (!phClientContext.isNull()) Secur32.INSTANCE.DeleteSecurityContext(phClientContext);
             phClientContext = null;
         }
         if (phClientCredential != null) {
             if (!phClientCredential.isNull()) Secur32.INSTANCE.FreeCredentialsHandle(phClientCredential);
             phClientCredential = null;
         }
     }
 
 	private LoginException handleError(int clientRc) {
 		Win32Exception exception = new Win32Exception(clientRc);
 		System.err.printf("SSPI: %1$#x: %2$s\n", exception.getHR().intValue(), exception.getMessage().trim());
 		
 //		// check for security errors
 //		switch(exception.getHR().intValue()) {
 //			case W32Errors.SEC_E_NO_CREDENTIALS: //  No credentials are available in the security package.
 //			case W32Errors.SEC_E_INVALID_HANDLE: // The handle passed to the function is invalid.
 //			case W32Errors.SEC_E_TARGET_UNKNOWN: // The target was not recognized.
 //			case W32Errors.SEC_E_LOGON_DENIED: // The logon failed.
 //			case W32Errors.SEC_E_INTERNAL_ERROR: // The Local Security Authority cannot be contacted.
 //			case W32Errors.SEC_E_NO_AUTHENTICATING_AUTHORITY: // No authority could be contacted for authentication.
 //		}
 //
 //		// check for generic errors
 //		switch(exception.getHR().intValue() & 0xFFFF) {
 //			case W32Errors.ERROR_ACCESS_DISABLED_NO_SAFER_UI_BY_POLICY: // Access to %1 has been restricted by your Administrator by policy rule %2.
 //		}
 
 		return new LoginException(exception.getMessage());
 	}
 
 }
