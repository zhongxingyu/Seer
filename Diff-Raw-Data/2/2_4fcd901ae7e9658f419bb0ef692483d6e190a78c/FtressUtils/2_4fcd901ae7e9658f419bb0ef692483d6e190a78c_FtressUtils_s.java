 package com.actividentity.ftress.portal;
 
 import java.awt.image.DirectColorModel;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.rmi.RemoteException;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.Security;
 import java.security.Signature;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 
 import org.apache.log4j.Logger;
 
 import sun.misc.BASE64Encoder;
 
 import com.aspace.ftress.interfaces70.ejb.SessionTransfer;
 import com.aspace.ftress.interfaces70.ftress.DTO.ALSI;
 import com.aspace.ftress.interfaces70.ftress.DTO.ALSISession;
 import com.aspace.ftress.interfaces70.ftress.DTO.AssetAuthorisationRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.AssetCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.AssetSet;
 import com.aspace.ftress.interfaces70.ftress.DTO.Attribute;
 import com.aspace.ftress.interfaces70.ftress.DTO.AttributeTypeCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuditSearchCriteria;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuditSearchResults;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationRequestParameter;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationResponse;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationStatistics;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationTypeCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthenticatorStatus;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthorisationRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.AuthorisationResponse;
 import com.aspace.ftress.interfaces70.ftress.DTO.ChannelCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.EntityIdentifier;
 import com.aspace.ftress.interfaces70.ftress.DTO.EventIdentifier;
 import com.aspace.ftress.interfaces70.ftress.DTO.ExternalAuditRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.ExternalAuditResponse;
 import com.aspace.ftress.interfaces70.ftress.DTO.GroupCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.GroupFunctionSetPrivilege;
 import com.aspace.ftress.interfaces70.ftress.DTO.GroupTransactionSetPrivilege;
 import com.aspace.ftress.interfaces70.ftress.DTO.MDAuthenticationAnswer;
 import com.aspace.ftress.interfaces70.ftress.DTO.MDAuthenticationPrompt;
 import com.aspace.ftress.interfaces70.ftress.DTO.MDAuthenticationPrompts;
 import com.aspace.ftress.interfaces70.ftress.DTO.MDAuthenticationRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.MDAuthenticator;
 import com.aspace.ftress.interfaces70.ftress.DTO.MDPrompt;
 import com.aspace.ftress.interfaces70.ftress.DTO.MDPromptCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.Parameter;
 import com.aspace.ftress.interfaces70.ftress.DTO.Password;
 import com.aspace.ftress.interfaces70.ftress.DTO.SecurityDomain;
 import com.aspace.ftress.interfaces70.ftress.DTO.SeedPositions;
 import com.aspace.ftress.interfaces70.ftress.DTO.SessionTransferCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.SessionTransferRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.SessionTransferType;
 import com.aspace.ftress.interfaces70.ftress.DTO.SessionTransferTypeCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.Transaction;
 import com.aspace.ftress.interfaces70.ftress.DTO.TransactionCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.TransactionSetItem;
 import com.aspace.ftress.interfaces70.ftress.DTO.UPAuthenticationRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.UPAuthenticator;
 import com.aspace.ftress.interfaces70.ftress.DTO.User;
 import com.aspace.ftress.interfaces70.ftress.DTO.UserAssetTransactionSetPrivilege;
 import com.aspace.ftress.interfaces70.ftress.DTO.UserCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.UserFunctionPrivilege;
 import com.aspace.ftress.interfaces70.ftress.DTO.UserGroupAssetGroupFunctionSetPrivilege;
 import com.aspace.ftress.interfaces70.ftress.DTO.UserGroupAssetSetTransactionSetPrivilege;
 import com.aspace.ftress.interfaces70.ftress.DTO.UserSearchCriteria;
 import com.aspace.ftress.interfaces70.ftress.DTO.UserTransactionPrivilege;
 import com.aspace.ftress.interfaces70.ftress.DTO.constants.AuthenticationResponseConstants;
 import com.aspace.ftress.interfaces70.ftress.DTO.constants.DeviceIssuanceRequestConstants;
 import com.aspace.ftress.interfaces70.ftress.DTO.constants.STMConstants;
 import com.aspace.ftress.interfaces70.ftress.DTO.credential.Credential;
 import com.aspace.ftress.interfaces70.ftress.DTO.credential.CredentialCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.AuthenticationChallenge;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.Authenticator;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.ChallengeId;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.Device;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceId;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceIssuanceRequest;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceIssuanceRequestEntry;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceIssuanceRequestStatus;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceIssuanceSearchCriteria;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceSearchCriteria;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceSearchResults;
 import com.aspace.ftress.interfaces70.ftress.DTO.device.DeviceTypeCode;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.ALSIInvalidException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.AuthenticationTierException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.AuthenticatorException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.ConstraintFailedException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.CreateDuplicateException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.DeleteObjectException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.DeviceAuthenticationException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.DeviceException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.InternalException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.InvalidChannelException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.InvalidParameterException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.MDAnswerException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.NoFunctionPrivilegeException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.ObjectNotFoundException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.PasswordExpiredException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.SeedingException;
 import com.aspace.ftress.interfaces70.ftress.DTO.exception.SessionTransferException;
 
 
 public class FtressUtils {
 	// Create a channel object, based on value read from configuration.txt
     final ChannelCode remoteChannel = new ChannelCode("CH_VPN");
     final DeviceTypeCode webSoftTokenDeviceType = new DeviceTypeCode("DT_STW_OE");
     final AuthenticationTypeCode customerOTP = new AuthenticationTypeCode("AT_CUSTOTP",false);
     // Create a domain object, based on value read from configuration.txt
     //final SecurityDomain domain1 = new com.aspace.ftress.interfaces70.ftress.DTO.SecurityDomain("DOMAIN1");
     
     com.aspace.ftress.interfaces70.ejb.Authenticator authenticatorInterface = null;
 	com.aspace.ftress.interfaces70.ejb.AuthenticatorManager authenticatorManagerInterface = null;
 	com.aspace.ftress.interfaces70.ejb.UserManager userManagerInterface = null;
 	com.aspace.ftress.interfaces70.ejb.UserAuthorisationPrivilegeManager userAuthorisationPrivilegeManager = null;
 	com.aspace.ftress.interfaces70.ejb.UserFunctionPrivilegeManager userFunctionPrivilegeManager = null;
 	com.aspace.ftress.interfaces70.ejb.UserAssetPrivilegeManager userAssetPrivilegeManager = null;
 	com.aspace.ftress.interfaces70.ejb.UserGroupAssetPrivilegeManager userGroupAssetPrivilegeManager = null;
 	com.aspace.ftress.interfaces70.ejb.UserGroupAuthorisationPrivilegeManager userGroupAuthorisationPrivilegeManager = null;
 	com.aspace.ftress.interfaces70.ejb.UserGroupFunctionPrivilegeManager userGroupFunctionPrivilegeManager = null;
 	com.aspace.ftress.interfaces70.ejb.AssetManager assetManager = null; 
 	com.aspace.ftress.interfaces70.ejb.AuthorisationTransactionConfiguration authorisationTransactionConfiguration = null;
 	com.aspace.ftress.interfaces70.ejb.Authorisor authorisor = null;
 	com.aspace.ftress.interfaces70.ejb.AssetAuthorisor assetAuthorisor = null;
 	com.aspace.ftress.interfaces70.ejb.DeviceManager deviceManager = null;
 	com.aspace.ftress.interfaces70.ejb.SessionTransfer sessionTransfer = null;
 	com.aspace.ftress.interfaces70.ejb.AuthorisationTransactionManager authorisationTransactionManager=null;
 	com.aspace.ftress.interfaces70.ejb.Auditor auditor=null;
 	com.aspace.ftress.interfaces70.ejb.FunctionManager functionManager=null;
 	com.aspace.ftress.interfaces70.ejb.CredentialManager credentialManager=null;
 	
 	final Logger log = Logger.getLogger("4TRESS_PORTAL");
 	
 	public static void main(String[] args) throws InternalException, RemoteException, PasswordExpiredException, AuthenticationTierException, ObjectNotFoundException, InvalidChannelException, SeedingException, InvalidParameterException, ALSIInvalidException, NoFunctionPrivilegeException, CreateDuplicateException, AuthenticatorException, ConstraintFailedException, DeviceAuthenticationException, DeviceException {
 		System.out.println("Starts");
 		FtressUtils ftressUtils = new FtressUtils();
 		ALSI alsi = ftressUtils.primaryPKIAuthentication(new SecurityDomain("DOMAIN1"),new ChannelCode("CH_DIRECT"),"system01","C:\\Users\\psena\\Documents\\01-Products\\10-4Tress AS\\EvalKit\\Certs\\4TClientCert.p12","actividentity");
 
 		if(alsi!=null){
 			System.out.println("Primary Authentication Sucessful");
 		}else{
 			System.out.println("Primary Authentication Failed");
 		}		
 		try{
 			
 		}catch (Exception e){
 			System.out.println("ERROR: " + e.getMessage());
 			e.printStackTrace();
 		}
 		System.out.println("Ends");
 	}
 	public FtressUtils() throws InternalException, RemoteException{
 		init();
 	}
 	public void init() throws InternalException, RemoteException{
 		//getting authenticator interface
 		log.debug("[init] ==> Starts");
         try {
 			authenticatorInterface = FtressServiceFactory.getAuthenticatorEJB();
 		} catch (InternalException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//getting user manager interface
    	    userManagerInterface = FtressServiceFactory.getUserManagerEJB();
    	    log.debug("[init] 4TRESS UserManager object...SUCCESS");
    	    
    	    //getting authenticator manager interface
   	  	authenticatorManagerInterface = FtressServiceFactory.getAuthenticatorManagerEJB();
   	  	log.debug("[init] 4TRESS AuthenticatorManager object...SUCCESS");
   	  	
   	  	//getting the userAuthorisationPrivilegeManager interface
   	  	userAuthorisationPrivilegeManager = FtressServiceFactory.getUserTransactionAuthorisationPrivilegeManagerEJB();
   	  	log.debug("[init] 4TRESS UserAuthorisationPrivilegeManager object...SUCCESS");
   	  	
   	  	//getting the userFunctionPrivilegeManager interface
   	  	userFunctionPrivilegeManager = FtressServiceFactory.getUserFunctionPrivilegeManagerEJB();
   	  	log.debug("[init] 4TRESS UserFunctionPrivilegeManager object...SUCCESS");
   	  	
   	  	//getting the userAssetPrivilegeManager interface
   	  	userAssetPrivilegeManager = FtressServiceFactory.getUserAssetPrivilegeManagerEJB();
   	  	log.debug("[init] 4TRESS UserAssetPrivilegeManager object...SUCCESS");
   	  	
   	  	//getting the userGroupAuthorisationPrivilegeManager interface
   	  	userGroupAuthorisationPrivilegeManager = FtressServiceFactory.getUserGroupTransactionAuthorisationPrivilegeManagerEJB();
   	  	log.debug("[init] 4TRESS UserGroupAuthorisationPrivilegeManager object...SUCCESS");
   	  	
   	  	//getting the userGroupFunctionPrivilegeManager interface
   	  	userGroupFunctionPrivilegeManager = FtressServiceFactory.getUserGroupFunctionPrivilegeManagerEJB();
   	  	log.debug("[init] 4TRESS UserGroupFunctionPrivilegeManager object...SUCCESS");
   	  	
   	  	//getting the userGroupAssetPrivilegeManager interface
   	  	userGroupAssetPrivilegeManager = FtressServiceFactory.getUserGroupAssetPrivilegeManagerEJB();
   	  	log.debug("[init] 4TRESS UserGroupAssetPrivilegeManager object...SUCCESS");
   	  	
   	  	//getting the assetManager interface
   	  	assetManager = FtressServiceFactory.getAssetManagerEJB();
   	  	log.debug("[init] 4TRESS AssetManager object...SUCCESS");
   	  	
   	  	//getting the AuthorisationTransactionConfiguration interface
   	  	authorisationTransactionConfiguration = FtressServiceFactory.getAuthorisationTransactionConfiguration();
   	  	log.debug("[init] 4TRESS AuthorisationTransactionConfiguration object...SUCCESS");
   	  	
   	  	//getting the AuthorisationTransactionManager interface
   	  	authorisationTransactionManager = FtressServiceFactory.getTransactionAuthorisationManagerEJB();
   	  	log.debug("[init] 4TRESS AuthorisationTransactionManager object...SUCCESS");
   	    	  	
   	  	//getting the Authorisor interface
   	  	authorisor = FtressServiceFactory.getAuthorisor();
   	  	log.debug("[init] 4TRESS Authorisor object...SUCCESS");
 
   	  	//getting the AssetAuthorisor interface
   	  	assetAuthorisor = FtressServiceFactory.getAssetAuthorisorEJB();
   	  	log.debug("[init] 4TRESS AssetAuthorisor object...SUCCESS");
   	  	
   	  	//getting the DeviceManager interface
   	  	deviceManager = FtressServiceFactory.getDeviceManagerEJB();
   	  	log.debug("[init] 4TRESS DeviceManager object...SUCCESS");
   	  	
   	  	//getting the SessionTransfer interface
   	  	sessionTransfer = FtressServiceFactory.getSessionTransferEJB();
   	  	log.debug("[init] 4TRESS SessionTransfer object...SUCCESS");
   	  	
   	  	//getting the Auditor interface
   	  	auditor = FtressServiceFactory.getAuditorEJB();
   	  	log.debug("[init] 4TRESS Auditor object...SUCCESS");
   	  	
   	  	//getting the FunctionManager interface
   	  	functionManager = FtressServiceFactory.getFunctionManagerEJB();
   	  	log.debug("[init] 4TRESS FunctionManager object...SUCCESS");
   	  	
   	  	//getting the CredentialManager interface
   	  	credentialManager = FtressServiceFactory.getCredentialManagerEJB();
   	  	log.debug("[init] 4TRESS CredentialManager object...SUCCESS");
   	  	
   	  	log.debug("[init] ==> Ends");
 	}
 	public ALSI primaryAuthentication(SecurityDomain domain,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode,String username,String password) throws PasswordExpiredException, AuthenticationTierException, ObjectNotFoundException, InvalidChannelException, SeedingException, InternalException, RemoteException, InvalidParameterException{
     	UPAuthenticationRequest authenticationRequest=null;
     	AuthenticationResponse authenticationResponse=null;
     	ALSI alsi=null;
 		
     	log.debug("[primaryAuthentication] ==> Starts");
 	    authenticationRequest = new UPAuthenticationRequest();
 	    authenticationRequest.setUsername(username);
 	    authenticationRequest.setPassword(password);
 	    authenticationRequest.setAuthenticationTypeCode(authenticationTypeCode);
 	    
 	    authenticationResponse = authenticatorInterface.primaryAuthenticateUP(channel,authenticationRequest, domain);
 	    if ( authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED )
 	    {
 	    	log.info("[primaryAuthentication] Primary login OK.");
 	        alsi=authenticationResponse.getAlsi();
 	        log.debug("[primaryAuthentication] Alsi: " + alsi);
 	    }
 	    else if (authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_FAILED)
 	    {
 	    	log.info("[primaryAuthentication] Primary User ID or password was incorrect.");
 	    }
 	    else if (authenticationResponse.getStatus()!=null && authenticationResponse.getStatus().equals(AuthenticationResponse.EXPIRED)) 
 	    {
 	    	log.info("[primaryAuthentication] Primary login has expired.");
 	    }
 	    log.debug("[primaryAuthentication] ==> Ends");
 	    return alsi;
 	}
     public ALSI primaryAuthentication(SecurityDomain domain,String username,String password) throws PasswordExpiredException, AuthenticationTierException, ObjectNotFoundException, InvalidChannelException, SeedingException, InternalException, RemoteException, InvalidParameterException{
	    return primaryAuthentication(domain,new ChannelCode("CH_DIRECT"),new AuthenticationTypeCode("AT_SYSLOG",false), username, password);
 	}
 
     public ALSI verifyUserOTP(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode,String username,String otp) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException, InvalidChannelException, AuthenticationTierException, DeviceAuthenticationException, SeedingException, DeviceException, PasswordExpiredException{
     	ALSI palsi=null;
     	log.debug("[verifyUserOTP] ==> Starts");
 	    AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		
 		// look for device authenticator
 		Authenticator[] userDeviceAuthenticator = null;
 		log.debug("[verifyUserOTP] checking for Device");
 		userDeviceAuthenticator = authenticatorManagerInterface.getAllAuthenticatorsForUser(alsi, channel, userCode, domain);
 		for(int i=0;i<userDeviceAuthenticator.length;i++){
 			userAuthType = userDeviceAuthenticator[i].getAuthenticationTypeCode();
 			if(userAuthType.getCode().equals(authenticationTypeCode.getCode()) && palsi==null){
 				log.debug("[verifyUserOTP] Verifying otp");
 		  		AuthenticationRequest userAuthenticationRequest = new AuthenticationRequest();
 				UserSearchCriteria crit = new UserSearchCriteria();
 				crit.setUserCode(userCode); 		// user
 				userAuthenticationRequest.setUserSearchCriteria(crit);
 				userAuthenticationRequest.setOneTimePassword(otp); 		// verification code
 				userAuthenticationRequest.setAuthenticationMode(AuthenticationRequest.SYNCHRONOUS);
 				userAuthenticationRequest.setAuthenticationTypeCode(userAuthType);			
 				
 				AuthenticationResponse authenticationResponse = authenticatorInterface.indirectPrimaryAuthenticateDevice(alsi, channel, userAuthenticationRequest, domain);
 				
 	    		if ( authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED){
 	    			com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationResponseParameter[] authenticationResponseParameters = authenticationResponse.getParameters();
 	    			boolean oobOtpSent = false;
 		    		boolean oobAuthProcessIncomplete = false;
 		    		for(int j=0;j<authenticationResponseParameters.length;j++){
 		    			log.debug("[verifyUserOTP]" + authenticationResponseParameters[j].getName() + " = " + authenticationResponseParameters[j].getValue());
 		    			if(authenticationResponseParameters[j].getName().equals(AuthenticationResponseConstants.PARAMETER_AUTHENTICATION_OOB_SENT) && authenticationResponseParameters[j].getValue().equals(AuthenticationResponseConstants.PARAMETER_VALUE_AUTHENTICATION_OOB_SENT_TRUE )){
 		    				log.debug("[verifyUserOTP] PARAMETER_AUTHENTICATION_OOB_SENT");
 		    				oobOtpSent = true;
 		    			}
 		    			if(authenticationResponseParameters[j].getName().equals(AuthenticationResponseConstants.PARAMETER_AUTHENTICATION_PROCESS) && authenticationResponseParameters[j].getValue().equals(AuthenticationResponseConstants.PARAMETER_VALUE_AUTHENTICATION_PROCESS_INCOMPLETE)){
 		    				log.debug("[verifyUserOTP] PARAMETER_AUTHENTICATION_PROCESS");
 		    				oobAuthProcessIncomplete = true;
 		    			}
 		    		}
 		    		if(!oobAuthProcessIncomplete && !oobOtpSent){
 		    			log.info("[verifyUserOTP] Authentication is OK");
 		    			palsi=authenticationResponse.getAlsi();
 		    		}else{
 		    			log.info("[verifyUserOTP] Sent OOB OTP, not authentication.");
 		    		}
 		        }else if (authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_FAILED ){
 		        	log.info("[verifyUserOTP] Authentication is NOT OK");
 		        }else{
 		        	log.info("[verifyUserOTP] Authentication is ????");
 		        }
 	    		log.debug("[verifyUserOTP] Authentication Type: " + userAuthType.getCode());
 			//end if
 			}
 		//end for
 		}
 		
 		log.debug("[verifyUserOTP] ==> Ends");
 		return palsi;
     }
 	
     public AuthenticationResponse verifyUserPassword(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,String password,int[] seedPositions) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException, SeedingException, InvalidChannelException, PasswordExpiredException, AuthenticationTierException{
     	log.debug("[verifyUserPassword] ==> Starts");
 	    AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		log.debug("[verifyUserPassword] checking for UP");
 		UPAuthenticator[] userUPAuthenticator = null;
 		AuthenticationResponse userAuthenticationResponse = null;
 		userUPAuthenticator = authenticatorManagerInterface.getAllUPAuthenticatorsForUser(alsi, channel, userCode, domain);
 		
 		if (userUPAuthenticator != null){
 			for(int i=0;i<userUPAuthenticator.length;i++){
 				log.debug("[verifyUserPassword] Found UP authenticator: " + userUPAuthenticator.length);
 				userAuthType = userUPAuthenticator[0].getAuthenticationTypeCode();
 				log.debug("[verifyUserPassword] Found UP authenticator userAuthType: " + userAuthType.toString());
 				if(userAuthType.getCode().equals(authenticationTypeCode.getCode())){
 					
 					log.debug("[verifyUserPassword] Verifying static password");
 			  		// do indirect UP authentication for user
 		    		UPAuthenticationRequest userAuthenticationRequest = new UPAuthenticationRequest();
 		    		userAuthenticationRequest.setUserCode(userCode);
 		    		userAuthenticationRequest.setPassword(password);
 		    		userAuthenticationRequest.setAuthenticationTypeCode(userAuthType);
 		    		
 		    		if(seedPositions!=null){
 		    			userAuthenticationRequest.setSeedPositions(seedPositions);
 		    		}
 		    		userAuthenticationResponse = authenticatorInterface.indirectPrimaryAuthenticateUP(alsi, channel, userAuthenticationRequest, domain);
 					
 		    		if ( userAuthenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED ){
 		    			log.info("[verifyUserPassword] Authentication is OK");
 			        }else if (userAuthenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_FAILED ){
 			        	log.info("[verifyUserPassword] Authentication is NOT OK");
 			        }else if (userAuthenticationResponse.getStatus()!=null && userAuthenticationResponse.getStatus().equals(AuthenticationResponse.EXPIRED)){
 			        	log.info("[verifyUserPassword] Password Expired");
 			        }
 				}
 			}
 		}else{
 			log.info("[verifyUserPassword] No UP authenticator found");
 		}
 		
 		log.debug("[verifyUserPassword] ==> Ends");
 		return userAuthenticationResponse;
 	}
     
     public AuthenticationResponse verifyUserPassword(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,String password,int[] seedPositions, AuthenticationRequestParameter[] paramList) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException, SeedingException, InvalidChannelException, PasswordExpiredException, AuthenticationTierException{
     	log.debug("[verifyUserPassword] ==> Starts");
 	    AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		log.debug("[verifyUserPassword] checking for UP");
 		UPAuthenticator[] userUPAuthenticator = null;
 		AuthenticationResponse userAuthenticationResponse = null;
 		userUPAuthenticator = authenticatorManagerInterface.getAllUPAuthenticatorsForUser(alsi, channel, userCode, domain);
 		
 		if (userUPAuthenticator != null){
 			for(int i=0;i<userUPAuthenticator.length;i++){
 				log.debug("[verifyUserPassword] Found UP authenticator: " + userUPAuthenticator.length);
 				userAuthType = userUPAuthenticator[0].getAuthenticationTypeCode();
 				log.debug("[verifyUserPassword] Found UP authenticator userAuthType: " + userAuthType.toString());
 				if(userAuthType.getCode().equals(authenticationTypeCode.getCode())){
 
 					log.debug("[verifyUserPassword] Verifying static password");
 			  		// do indirect UP authentication for user
 		    		UPAuthenticationRequest userAuthenticationRequest = new UPAuthenticationRequest();
 		    		userAuthenticationRequest.setUserCode(userCode);
 		    		userAuthenticationRequest.setPassword(password);
 		    		userAuthenticationRequest.setAuthenticationTypeCode(userAuthType);
 		    		userAuthenticationRequest.setParameters(paramList);
 		    		
 		    		if(seedPositions!=null){
 		    			userAuthenticationRequest.setSeedPositions(seedPositions);
 		    		}
 		    		userAuthenticationResponse = authenticatorInterface.indirectPrimaryAuthenticateUP(alsi, channel, userAuthenticationRequest, domain);
 					
 		    		if ( userAuthenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED ){
 		    			log.info("[verifyUserPassword] Authentication is OK");
 		         	//	palsi = userAuthenticationResponse.getAlsi();
 			        }else if (userAuthenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_FAILED ){
 			        	log.info("[verifyUserPassword] Authentication is NOT OK");
 			        }else if (userAuthenticationResponse.getStatus()!=null && userAuthenticationResponse.getStatus().equals(AuthenticationResponse.EXPIRED)){
 			        	log.info("[verifyUserPassword] Password Expired");
 			        }
 				}
 			}
 		}else{
 			log.info("[verifyUserPassword] No UP authenticator found");
 		}
 		
 		log.debug("[verifyUserPassword] ==> Ends");
 		return userAuthenticationResponse;
 	}
     
     
     public ALSI verifyUserMD(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,MDAuthenticationAnswer[] mdAuthenticationAnswers) throws ALSIInvalidException, SeedingException, InvalidChannelException, NoFunctionPrivilegeException, AuthenticationTierException, ObjectNotFoundException, RemoteException, InvalidParameterException, InternalException{
     	log.debug("[verifyUserMD] ==> Starts");
 		ALSI palsi=null;
 		log.debug("[verifyUserMD] Creating authentication request...");
 		MDAuthenticationRequest request = new MDAuthenticationRequest();
 		request.setUserCode(new UserCode(username));
 		log.debug("[verifyUserMD] username: " + username);
 		request.setAuthenticationTypeCode(authenticationTypeCode);
 		log.debug("[verifyUserMD] authenticationTypeCode: " + authenticationTypeCode.getCode());
 		log.debug("[verifyUserMD] Adding questions & answers");
 		request.setAnswers(mdAuthenticationAnswers);
 		
 		AuthenticationResponse response = authenticatorInterface.indirectPrimaryAuthenticateMD(alsi, channel,request, domain);
 		
 		if (response.getResponse()==AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED){
 			log.info("[verifyUserMD] Authentication is OK");
      		palsi = response.getAlsi();
         }else if (response.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_FAILED ){
         	log.info("[verifyUserMD] Authentication is NOT OK");
         }
 
 		log.debug("[verifyUserMD] ==> Ends");
     	return palsi;
     }
     
     public SeedPositions getUserPasswordSeedPositions(SecurityDomain domain,ChannelCode channel,String username,AuthenticationTypeCode authenticationTypeCode) throws PasswordExpiredException, NoFunctionPrivilegeException, ObjectNotFoundException, ALSIInvalidException, SeedingException, InternalException, RemoteException, InvalidParameterException{
     	log.debug("[getUserPasswordSeedPositions] ==> Starts");
     	SeedPositions seedposition = authenticatorInterface.getPasswordSeedPositions(channel, new UserCode(username), authenticationTypeCode, 0,domain);
     	log.debug("[getUserPasswordSeedPositions] ==> Ends");
     	return seedposition;
     }
     
     public String convertUserSeedPasswordPositionsToString(int positions[]){
     	log.debug("[convertUserSeedPasswordPositionsToString] ==> Starts");
     	String seedPositions="";
     	for(int i=0;i<positions.length;i++){
     		if(i!=0){
     			seedPositions += ", ";
     		}
     		seedPositions += positions[i];
     	}
     	log.debug("[convertUserSeedPasswordPositionsToString] ==> Ends seeds positions: " + seedPositions);
     	return seedPositions;
     }
     public ALSI verifyUserPKI(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,X509Certificate cert) throws NoFunctionPrivilegeException, InvalidChannelException, AuthenticationTierException, DeviceAuthenticationException, SeedingException, ALSIInvalidException, DeviceException, PasswordExpiredException, RemoteException, InternalException, InvalidParameterException{
     	log.debug("[verifyUserPKI] ==> Starts");
 		ALSI palsi=null;
 		
 		//Create the credential
 		Credential credential = new Credential();
 		credential.setCredentialCode(new CredentialCode(cert.getIssuerDN().toString() + cert.getSerialNumber().toString()));
 		log.debug("[verifyUserPKI] Certificate Credential code: " + credential.getCredentialCode().getCode());
 		
 		//Create authentification request
 		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
 		UserSearchCriteria usc = new UserSearchCriteria();
 		usc.setUserCode(new UserCode(username));
 		log.debug("[verifyUserPKI] User code: " + usc.getUserCode().getCode());
 		authenticationRequest.setUserSearchCriteria(usc);
 		authenticationRequest.setAuthenticationMode(AuthenticationRequest.SYNCHRONOUS);
 		authenticationRequest.setAuthenticationTypeCode(authenticationTypeCode);
 		log.debug("[verifyUserPKI] Authentication type: " + authenticationTypeCode.getCode());
 		authenticationRequest.setOneTimePassword(credential.getCredentialCode().getCode());
 		log.debug("[verifyUserPKI] About to authenticate");
 		AuthenticationResponse resp=null;
 		try {
 			resp = authenticatorInterface.indirectPrimaryAuthenticateDevice(alsi, channel,authenticationRequest, domain);
 		} catch (ObjectNotFoundException e) {
 			log.error("[verifyUserPKI] Object not Found Expection, authentication failed");
 			e.printStackTrace();
 		}
 		log.debug("[verifyUserPKI] Authentication done");
 		if(resp!=null){
 			palsi = resp.getAlsi();
 			if(palsi!=null){
 				log.info("[verifyUserPKI] palsi is not null, Authentication Success !!!! palsi: " + palsi.getAlsi());
 			}else{
 				log.info("[verifyUserPKI] palsi is null, Authentication Failled !!!!");
 			}
 		}else{
 			log.info("[verifyUserPKI] Authentication response and palsi are null, Authentication Failled !!!!");
 		}
 		log.debug("[verifyUserPKI] ==> Ends");
 		return palsi;
     }
     public ALSI primaryPKIAuthentication(SecurityDomain domain,ChannelCode channel,String username,String pkcs12File,String password) throws ObjectNotFoundException, ALSIInvalidException, DeviceException, SeedingException, InvalidChannelException, PasswordExpiredException, DeviceAuthenticationException, AuthenticationTierException, InvalidParameterException, RemoteException, InternalException{
     	log.debug("[primaryPKIAuthentication] Begins");
     	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
     	KeyStore keyStore = loadPKCS12KeyStore(pkcs12File, password);
     	PrivateKey privateKey = getPrivateKeyFromPKCS12(keyStore, password);
     	
     	UserCode userCode = new UserCode(username);
 
 		AuthenticationTypeCode authCode = new AuthenticationTypeCode(("AT_SYSPKI"),false);
 		log.debug("[primaryPKIAuthentication] Get Challenge for user..." + userCode.getCode());
 
 		AuthenticationChallenge authChallenge = authenticatorInterface.getAuthenticationChallengeByUserCode(channel, userCode, authCode, domain);
 		log.debug("[primaryPKIAuthentication] After getting challenge");
 		String challenge = authChallenge.getChallenge();
 		
 		if (challenge == null) {
 			log.debug("[primaryPKIAuthentication] No Challenge");
 			return null;
 		}
 		
 		log.debug("[primaryPKIAuthentication] Challenge: " + challenge);
 		
 		AuthenticationRequest activrequest = new AuthenticationRequest();
 		activrequest.setAuthenticationMode(AuthenticationRequest.ASYNCHRONOUS);
 		activrequest.setAuthenticationTypeCode(authCode);
 		UserSearchCriteria usc = new UserSearchCriteria();
 		usc.setUserCode(userCode);
 		activrequest.setUserSearchCriteria(usc);
 		activrequest.setChallenge(challenge);
 		activrequest.setOneTimePassword(signData(challenge, privateKey));
 	
 		AuthenticationResponse resp = authenticatorInterface.primaryAuthenticateDevice(channel, activrequest, domain);
 		
 		log.debug("[primaryPKIAuthentication] 4TPKI Alsi: " + resp.getAlsi().toString());
 		
 		if (resp.getResponse() == 1)
 		{
 			log.debug("[primaryPKIAuthentication] Direct PKI authentication Successful");
 			return resp.getAlsi();
 		}
 		else {
 			log.debug("[primaryPKIAuthentication] Direct PKI authentication failed: " + resp.getMessage());
 			return null;
 		}
 		
     }
     private KeyStore loadPKCS12KeyStore(String directPKIKeyStoreFile,
 			String password) {
 
 		try {
 
 			KeyStore keyStore = KeyStore.getInstance("PKCS12");
 			InputStream inputStream = new FileInputStream(directPKIKeyStoreFile);
 			keyStore.load(inputStream, password.toCharArray());
 			inputStream.close();
 			return keyStore;
 
 		} catch (Exception e) {
 			System.out.println("Error in loading Key store: " + e.getMessage());
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	private PrivateKey getPrivateKeyFromPKCS12(KeyStore keyStore,
 			String password) {
 
 		try {
 			Enumeration<String> e = keyStore.aliases();
 
 			String alias = e.nextElement();
 
 			return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
 
 		} catch (Exception e) {
 			System.out.println("Error in getting private key from Key store: "
 					+ e.getMessage());
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
     private static String signData(String plaintext, PrivateKey privateKey) {
 		String signedString = null;
 		try {
 
 			Signature signature = Signature.getInstance("SHA1WithRSA");
 			signature.initSign(privateKey);
 			signature.update(plaintext.getBytes());
 
 
 			byte[] signed = signature.sign();
 			signedString = new BASE64Encoder().encode(signed);
 			
 			System.out.println(signedString);
 			
 
 		} catch (Exception e) {
 			System.out.println("Error in generating signature : "
 					+ e.getMessage());
 			e.printStackTrace();
 		}
 
 		return signedString;
 	}
 
     public ALSI authenticationStepUp(SecurityDomain domain,ALSI alsi,ALSI palsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,String otp) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException, InvalidChannelException, AuthenticationTierException, DeviceAuthenticationException, SeedingException, DeviceException, PasswordExpiredException{
     	log.debug("[authenticationStepUp] ==> Starts");
     	ALSI stepedUpPalsi=null;
 	    AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		
 		// look for device authenticator
 		Authenticator[] userDeviceAuthenticator = null;
 		log.debug("[authenticationStepUp] checking for Device");
 		userDeviceAuthenticator = authenticatorManagerInterface.getAllAuthenticatorsForUser(alsi, channel, userCode, domain);
 		for(int i=0;i<userDeviceAuthenticator.length;i++){
 			userAuthType = userDeviceAuthenticator[i].getAuthenticationTypeCode();
 			//We only check 2 authentication policies: Customer One Time Password (AT_CUSTOTP) and Customer Out Of Band (AT_CUSTOOB), only if there is no valid authentication already done
 			if((userAuthType.getCode().equals(authenticationTypeCode.getCode())) && stepedUpPalsi==null){
 				log.debug("[authenticationStepUp] Verifying otp");
 		  		AuthenticationRequest userAuthenticationRequest = new AuthenticationRequest();
 				UserSearchCriteria crit = new UserSearchCriteria();
 				crit.setUserCode(userCode); 		// user
 				userAuthenticationRequest.setUserSearchCriteria(crit);
 				userAuthenticationRequest.setOneTimePassword(otp); 		// verification code
 				userAuthenticationRequest.setAuthenticationMode(AuthenticationRequest.SYNCHRONOUS);
 				userAuthenticationRequest.setAuthenticationTypeCode(userAuthType);			
 				
 				AuthenticationResponse authenticationResponse = authenticatorInterface.indirectSecondaryAuthenticateDevice(alsi, palsi, channel, userAuthenticationRequest, domain);
 				    			
 	    		if ( authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED){
 	    			com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationResponseParameter[] authenticationResponseParameters = authenticationResponse.getParameters();
 	    			boolean oobOtpSent = false;
 		    		boolean oobAuthProcessIncomplete = false;
 		    		for(int j=0;j<authenticationResponseParameters.length;j++){
 		    			log.debug("[authenticationStepUp] " + authenticationResponseParameters[j].getName() + " = " + authenticationResponseParameters[j].getValue());
 		    			if(authenticationResponseParameters[j].getName().equals(AuthenticationResponseConstants.PARAMETER_AUTHENTICATION_OOB_SENT) && authenticationResponseParameters[j].getValue().equals(AuthenticationResponseConstants.PARAMETER_VALUE_AUTHENTICATION_OOB_SENT_TRUE )){
 		    				log.debug("[authenticationStepUp] PARAMETER_AUTHENTICATION_OOB_SENT");
 		    				oobOtpSent = true;
 		    			}
 		    			if(authenticationResponseParameters[j].getName().equals(AuthenticationResponseConstants.PARAMETER_AUTHENTICATION_PROCESS) && authenticationResponseParameters[j].getValue().equals(AuthenticationResponseConstants.PARAMETER_VALUE_AUTHENTICATION_PROCESS_INCOMPLETE)){
 		    				log.debug("[authenticationStepUp] PARAMETER_AUTHENTICATION_PROCESS");
 		    				oobAuthProcessIncomplete = true;
 		    			}
 		    		}
 		    		if(!oobAuthProcessIncomplete && !oobOtpSent){
 		    			log.info("[authenticationStepUp] Authentication is OK");
 		    			stepedUpPalsi=authenticationResponse.getAlsi();
 		    		}else{
 		    			log.info("[authenticationStepUp] Sent OOB OTP, not authentication.");
 		    		}
 		        }else if (authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_FAILED ){
 		        	log.info("[authenticationStepUp] Authentication is NOT OK");
 		        }else{
 		        	log.info("[authenticationStepUp] Authentication is ????");
 		        }
 	    		log.debug("[authenticationStepUp] Authentication Type: " + userAuthType.getCode());
 			//end if
 			}
 		//end for
 		}
 		
 		log.debug("[authenticationStepUp] ==> Ends");
     	return stepedUpPalsi;
     }
     
     public void authenticationStepDown(SecurityDomain domain,ALSI alsi,ALSI palsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,String otp) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
     	log.debug("[authenticationStepDown] ==> Starts");
     	AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		
 		// look for device authenticator
 		Authenticator[] userDeviceAuthenticator = null;
 		log.debug("[authenticationStepDown] checking for Device");
 		userDeviceAuthenticator = authenticatorManagerInterface.getAllAuthenticatorsForUser(alsi, channel, userCode, domain);
 		for(int i=0;i<userDeviceAuthenticator.length;i++){
 			userAuthType = userDeviceAuthenticator[i].getAuthenticationTypeCode();
 			//We only check 2 authentication policies: Customer One Time Password (AT_CUSTOTP) and Customer Out Of Band (AT_CUSTOOB), only if there is no valid authentication already done
 			if(userAuthType.getCode().equals(authenticationTypeCode.getCode())){
 				log.debug("[authenticationStepDown] Verifying otp");
 				authenticatorInterface.indirectRemoveSecondaryAuthentication(alsi, palsi, channel, userAuthType, domain);
 				log.debug("[authenticationStepDown] Authentication Type: " + userAuthType.getCode());
 			//end if
 			}
 		//end for
 		}
 		log.debug("[authenticationStepDown] ==> Ends");
     }
     
     public void logout(SecurityDomain domain,ALSI alsi,ALSI palsi,ChannelCode channel) throws ObjectNotFoundException, ALSIInvalidException, RemoteException, InvalidParameterException, InternalException{
     	authenticatorInterface.indirectLogout(alsi, palsi, channel, domain);
     }
     
     public UserTransactionPrivilege[] getUserAuthorisations(SecurityDomain domain,ALSI alsi,ChannelCode channel,String username) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
     	return userAuthorisationPrivilegeManager.getUserTransactionPrivileges(alsi, channel, new UserCode(username), domain);
     }
     
     public UserFunctionPrivilege[] getUserFunctions(SecurityDomain domain,ALSI alsi,ChannelCode channel,String username) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
     	return userFunctionPrivilegeManager.getUserFunctionPrivileges(alsi, channel, new UserCode(username), domain);
     }
     
     public UserAssetTransactionSetPrivilege[] getUserAssets(SecurityDomain domain,ALSI alsi,ChannelCode channel,String username) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	return userAssetPrivilegeManager.getUserAssetTransactionSetPrivileges(alsi, channel, new UserCode(username), domain);
     }
     
     public GroupTransactionSetPrivilege[] getUserGroupAuthorisations(SecurityDomain domain,ALSI alsi,ChannelCode channel,String groupname) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
     	return userGroupAuthorisationPrivilegeManager.getUserGroupTransactionSetPrivileges(alsi, channel, new GroupCode(groupname,false), domain);
     }
     
     public GroupFunctionSetPrivilege[] getUserGroupFunctions(SecurityDomain domain,ALSI alsi,ChannelCode channel,String groupname) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
     	return userGroupFunctionPrivilegeManager.getUserGroupFunctionSetPrivileges(alsi, channel, new GroupCode(groupname,false), domain);
     }
     
     public UserGroupAssetSetTransactionSetPrivilege[] getUserGroupTransactionAssets(SecurityDomain domain,ALSI alsi,ChannelCode channel,String groupname) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	return userGroupAssetPrivilegeManager.getUserSubGroupAssetSetTransactionSetPrivileges(alsi, channel, new GroupCode(groupname,false), domain);
     }
     
     public UserGroupAssetGroupFunctionSetPrivilege[] getUserGroupFunctionAssets(SecurityDomain domain,ALSI alsi,ChannelCode channel,String groupname) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	return userGroupAssetPrivilegeManager.getUserSubGroupAssetGroupFunctionSetPrivileges(alsi, channel, new GroupCode(groupname,false), domain);
     }
     
     public AssetCode[] getAssetsForUserByTransaction(SecurityDomain domain,ALSI alsi,ChannelCode channel,ALSI palsi,String transaction) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	return assetManager.getAssetsForUserByTransaction(alsi,channel,palsi,channel,new TransactionCode(transaction),domain);
     }
     
     public Transaction[] getAllTransactions(SecurityDomain domain,ALSI alsi, ChannelCode channel) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	return authorisationTransactionConfiguration.getAllTransactions(alsi, channel, domain);
     }
     
     public AuthorisationResponse isTransactionAuthorisedForUser(SecurityDomain domain,ALSI alsi,ALSI palsi,ChannelCode channel, String transactionCode,String threshold) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	log.debug("[isTransactionAuthorisedForUser] ==> Starts");
     	
     	AuthorisationRequest authorisationRequest = new AuthorisationRequest();
     	authorisationRequest.setTransactionCode(new TransactionCode(transactionCode));
     	if(threshold!=null && !threshold.equals("")){
     		log.debug("[isTransactionAuthorisedForUser] Using threshold: " + threshold);
     		authorisationRequest.setValue(threshold);
     	}
     	log.debug("[isTransactionAuthorisedForUser] ==> Ends");
     	return authorisor.indirectAuthoriseTransaction(alsi, palsi, channel, authorisationRequest, domain);
     }
     
     public AuthorisationResponse isAssetAuthorisedForUser(SecurityDomain domain,ALSI alsi,ALSI palsi,ChannelCode channel, String assetCode,String transactionCode,String threshold) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	log.debug("[isAssetAuthorisedForUser] ==> Starts");
     	
     	AssetAuthorisationRequest assetAuthorisationRequest = new AssetAuthorisationRequest();
     	assetAuthorisationRequest.setAssetCode(new AssetCode(assetCode));
     	assetAuthorisationRequest.setTransactionCode(new TransactionCode(transactionCode));
     	if(threshold!=null && !threshold.equals("")){
     		log.debug("[isAssetAuthorisedForUser] Using threshold: " + threshold);
     		assetAuthorisationRequest.setValue(threshold);
     	}
     	log.debug("[isAssetAuthorisedForUser] ==> Ends");
     	return assetAuthorisor.indirectAuthoriseAssetTransaction(alsi, palsi, channel, assetAuthorisationRequest, domain);
     }
     
     public String[] getAuthenticationTypeCodeForTransaction(SecurityDomain domain,ALSI alsi,ChannelCode channel,String username,String transactionCode,String assetCode,String threshold) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
     	log.debug("[getAuthenticationTypeCodeForTransaction] ==> Starts");
     	ArrayList result = new ArrayList();
     	log.debug("[getAuthenticationTypeCodeForTransaction] Check if user has direct access to the transaction for this asset");
     	//check if the user has direct access to the privilege
     	UserAssetTransactionSetPrivilege[] userAssetTransactionSetPrivileges = userAssetPrivilegeManager.getUserAssetTransactionSetPrivileges(alsi, channel, new UserCode(username), domain);
     	if(userAssetTransactionSetPrivileges!=null){
     		//for each userAssetTransactionSetPrivilege check if contains the asset and a transaction set that contains the transaction
 	    	for(int i=0;i<userAssetTransactionSetPrivileges.length;i++){
 	    		UserAssetTransactionSetPrivilege userAssetTransactionSetPrivilege = userAssetTransactionSetPrivileges[i];
 	    		if(userAssetTransactionSetPrivilege.getAssetCode().getCode()==null || userAssetTransactionSetPrivilege.getAssetCode().getCode().equals(assetCode)){
 	    			log.debug("[getAuthenticationTypeCodeForTransaction] Found a assetTransactionSetPrivilege that match this asset...");
 	    			TransactionSetItem[] transactionSetItems = (authorisationTransactionManager.getTransactionSet(alsi, channel, userAssetTransactionSetPrivilege.getTransactionSetCode(), domain)).getTransactionSetItems();
 	    			if(transactionSetItems!=null){
 	    				for(int j=0;j<transactionSetItems.length;j++){
 	    					TransactionSetItem transactionSetItem = transactionSetItems[j];
 	    					if(transactionSetItem.getTransactionsCode().getCode()==null || transactionSetItem.getTransactionsCode().getCode().equals(transactionCode)){
 	    						//Found a valid authenticationTypeCode
 	    						if(transactionSetItem.getThreshold()==null || Integer.parseInt(threshold)<=Integer.parseInt(transactionSetItem.getThreshold())){
 	    							log.debug("[getAuthenticationTypeCodeForTransaction] Found one valid authentication policy: " + userAssetTransactionSetPrivilege.getAuthenticationTypeCode().getCode());
 	    							String policy = userAssetTransactionSetPrivilege.getAuthenticationTypeCode().getCode();
 	    							if(policy==null){
 	    								policy="ANY";
 	    							}
 	    							if(!result.contains(policy)){
 	    								result.add(policy);
 	    							}
 	    						}else{
 	    							log.debug("[getAuthenticationTypeCodeForTransaction] Threshold is too high.");
 	    						}
 	    					}
 	    				}
 	    			}
 	    		}
 	    	}
     	}
     	
     	log.debug("[getAuthenticationTypeCodeForTransaction] Check if user belongs to a group that has direct access to the transaction for this asset");
     	//get the assetSets that contains this asset
     	AssetSet[] assetSets = assetManager.getAssetSetsForAsset(alsi, channel, new AssetCode(assetCode), domain);
 
     	//check if userGroup has direct access to the privilege
     	UserGroupAssetSetTransactionSetPrivilege[] userGroupAssetSetTransactionSetPrivileges = userGroupAssetPrivilegeManager.getUserSubGroupAssetSetTransactionSetPrivileges(alsi, channel, (userManagerInterface.getUser(alsi, channel, new UserCode(username), domain).getGroupCode()), domain);
     	for(int i=0;i<userGroupAssetSetTransactionSetPrivileges.length;i++){
     		UserGroupAssetSetTransactionSetPrivilege userGroupAssetSetTransactionSetPrivilege = userGroupAssetSetTransactionSetPrivileges[i];
     		//System.out.println("  userGroupAssetSetTransactionSetPrivilege.getAssetSetCode().getCode() = " + userGroupAssetSetTransactionSetPrivilege.getAssetSetCode().getCode());
 			for(int j=0;j<assetSets.length;j++){
     			//System.out.println("   Dealing with asset set " + assetSets[j].getCode().getCode());
 	    		if(userGroupAssetSetTransactionSetPrivilege.getAssetSetCode().getCode()==null || userGroupAssetSetTransactionSetPrivilege.getAssetSetCode().getCode().equals(assetSets[j].getCode().getCode())){
 	    			//System.out.println("     Match between userGroupAssetSetTransactionSetPrivilege.getAssetSetCode().getCode().equals(assetSets[j].getCode())");
 	    			TransactionSetItem[] transactionSetItems = (authorisationTransactionManager.getTransactionSet(alsi, channel, userGroupAssetSetTransactionSetPrivilege.getTransactionSetCode(), domain)).getTransactionSetItems();
 	    			if(transactionSetItems!=null){
 	    				//System.out.println("     Transaction not null");
 	    				for(int k=0;k<transactionSetItems.length;k++){
 	    					TransactionSetItem transactionSetItem = transactionSetItems[k];
 	    					//System.out.println("      i:" + i + " j:" + j + " k:" + k + " transactionSetItem.getTransactionsCode().getCode() = " + transactionSetItem.getTransactionsCode().getCode());
 	    					if(transactionSetItem.getTransactionsCode().getCode().equals(transactionCode)){
 	    						//Found a valid authenticationTypeCode
 	    						//System.out.println("      Transaction code is matching...");
 	    						if(transactionSetItem.getThreshold()==null || Integer.parseInt(threshold)<=Integer.parseInt(transactionSetItem.getThreshold())){
 	    							log.debug("[getAuthenticationTypeCodeForTransaction] Found one valid authentication policy: " + userGroupAssetSetTransactionSetPrivilege.getAuthenticationTypeCode().getCode());
 	    							//System.out.println("      Threshold is validated... found valid policy: " + userGroupAssetSetTransactionSetPrivilege.getAuthenticationTypeCode().getCode());
 	    							String policy = userGroupAssetSetTransactionSetPrivilege.getAuthenticationTypeCode().getCode();
 	    							if(policy==null){
 	    								policy="ANY";
 	    							}
 	    							if(!result.contains(policy)){
 	    								result.add(policy);
 	    							}
 	    						}else{
 	    							//System.out.println("      Threshold NOT validated... failed with policy: " + userGroupAssetSetTransactionSetPrivilege.getAuthenticationTypeCode().getCode());
 	    							log.debug("[getAuthenticationTypeCodeForTransaction] Threshold is too high.");
 	    						}
 	    					}
 	    				}
 	    			}
 	    		}
     		}
     	}
     	ArrayList validAuthenticators = new ArrayList();
     	Authenticator[] authenticators = this.getExistingAuthenticator(domain,alsi,channel,username);
     	for(int i=0;i<authenticators.length;i++){
     		Authenticator authenticator = authenticators[i];
     		if(authenticator.getStatus().equals("ENABLED")){
     			Calendar validToCal = authenticator.getValidTo();
     	    	GregorianCalendar todayCal = new GregorianCalendar();
     	    	if(validToCal.after(todayCal)){
     	    		validAuthenticators.add(authenticator.getAuthenticationTypeCode().getCode());
     	    	}
     		}
     	}
     	UPAuthenticator[] UPauthenticators = this.getExistingUPAuthenticator(domain,alsi,channel,username);
     	for(int i=0;i<UPauthenticators.length;i++){
     		UPAuthenticator UPauthenticator = UPauthenticators[i];
     		if(UPauthenticator.getStatus().equals("ENABLED")){
     			Calendar validToCal = UPauthenticator.getValidTo();
     	    	GregorianCalendar todayCal = new GregorianCalendar();
     	    	if(validToCal.after(todayCal)){
     	    		validAuthenticators.add(UPauthenticator.getAuthenticationTypeCode().getCode());
     	    	}
     		}
     	}
     	MDAuthenticator[] MDauthenticators = this.getExistingMDAuthenticator(domain,alsi,channel,username);
     	for(int i=0;i<MDauthenticators.length;i++){
     		MDAuthenticator MDauthenticator = MDauthenticators[i];
     		if(MDauthenticator.getStatus().equals("ENABLED")){
     			Calendar validToCal = MDauthenticator.getValidTo();
     	    	GregorianCalendar todayCal = new GregorianCalendar();
     	    	if(validToCal.after(todayCal)){
     	    		validAuthenticators.add(MDauthenticator.getAuthenticationTypeCode().getCode());
     	    	}
     		}
     	}
     	for(int i=0;i<result.size();i++){
     		if(!result.get(i).equals("ANY")){
 	    		if(!validAuthenticators.contains(result.get(i))){
 	    			result.remove(i);		
 	    		}
     		}
     	}
     	log.debug("[getAuthenticationTypeCodeForTransaction] ==> Ends");
     	String[] resultArr  = new String[result.size()];
     	result.toArray(resultArr);
     	return resultArr;
     }
 
     public boolean isSessionValid(SecurityDomain domain,ALSI palsi) throws ObjectNotFoundException, ALSIInvalidException, RemoteException, InvalidParameterException, InternalException{
     	log.debug("[isSessionValid] ==> Starts");
     	boolean result = false;
     	if(palsi!=null){
 	    	ALSISession alsiSession = authenticatorInterface.getSessionData(palsi, domain);
 	    	Calendar validToCal = alsiSession.getValidTo();
 	    	GregorianCalendar todayCal = new GregorianCalendar();
 	    	if(validToCal.after(todayCal)){
 	    		result = true;
 	    	}
     	}
     	log.debug("[isSessionValid] ==> Ends");
     	return result;
     }
     public  AuthenticationTypeCode[] getSessionAuthenticationCode(SecurityDomain domain,ALSI palsi) throws ObjectNotFoundException, ALSIInvalidException, RemoteException, InvalidParameterException, InternalException{
     	log.debug("[getSessionAuthenticationCode] ==> Starts");
     	ALSISession alsiSession = authenticatorInterface.getSessionData(palsi, domain);
     	log.debug("[getSessionAuthenticationCode] ==> Ends");
     	return alsiSession.getUniqueAuthenticationTypes();
     } 
     public String getSessionUserCode(SecurityDomain domain,ALSI palsi){
     	log.debug("[getSessionUserCode] ==> Starts");
     	ALSISession alsiSession=null;
 		try {
 			if(palsi!=null){
 				alsiSession = authenticatorInterface.getSessionData(palsi, domain);
 			}
 		} catch (ObjectNotFoundException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		} catch (ALSIInvalidException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		} catch (InvalidParameterException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		} catch (InternalException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		}
     	log.debug("[getSessionUserCode] ==> Ends");
     	String usercode = null;
     	if(alsiSession!=null){
     		usercode = alsiSession.getUserCode().getCode();
     	}	
     	return usercode;
     }                            
 	public boolean isUserExistingIn4TRESS(SecurityDomain domain,ChannelCode channel,ALSI alsi,String username) throws ALSIInvalidException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
 		boolean result = false;
 		log.debug("[isUserExistingIn4TRESS] ==> Starts");
 		User user;
 		try {
 			user = userManagerInterface.getUser( alsi, channel, new UserCode(username), domain);
 		
 			if(user!=null){
 				log.info("[isUserExistingIn4TRESS] User is already existing!!");
 				result = true;
 			}
 			log.debug("[isUserExistingIn4TRESS] userCode = #" + user.getCode() + "#");
 			Attribute[] attributes =user.getAttributes();
 			if(attributes!=null){
 				for(int i=0;i<attributes.length;i++){
 					Attribute attribute = attributes[i];
 					log.debug("[isUserExistingIn4TRESS]" + attribute.getTypeCode().getCode() + " = #" + attribute.getValue() + "#");
 				}
 			}
 			log.debug("[isUserExistingIn4TRESS] ==> Ends");
 			return result;
 		} catch (ObjectNotFoundException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			return false;
 		}
 	}
 	public boolean isUserExistingIn4TRESS(SecurityDomain domain,ALSI alsi,String username) throws ALSIInvalidException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
 		return isUserExistingIn4TRESS(domain,remoteChannel,alsi,username);
 	}
 	public User createUser(SecurityDomain domain,ALSI alsi,String username, String firstname, String lastname,String mail,String mobile) throws ALSIInvalidException, NoFunctionPrivilegeException, CreateDuplicateException, ObjectNotFoundException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[createUser] ==> Starts");
 		Attribute[] attributes = new Attribute[4];
 		//set the first name
 		Attribute attribute = new Attribute();
 		attribute.setTypeCode(new AttributeTypeCode("FIRSTNAME"));
 		attribute.setValue(firstname);
 		attributes[0]=attribute;
 		//set the last name
 		attribute = new Attribute();
 		attribute.setTypeCode(new AttributeTypeCode("LASTNAME"));
 		attribute.setValue(lastname);
 		attributes[1]=attribute;
 		//set the mail
 		attribute = new Attribute();
 		attribute.setTypeCode(new AttributeTypeCode("ATR_EMAIL"));
 		attribute.setValue(mail);
 		attributes[2]=attribute;
 		//set the mobile
 		attribute = new Attribute();
 		attribute.setTypeCode(new AttributeTypeCode("ATR_MOBILE"));
 		attribute.setValue(mobile);
 		attributes[3]=attribute;
 		//instanciate user
 		User user = new User();
 		user.setCode(new UserCode(username));
 		user.setAttributes(attributes);
 		user.setUserType("UT_CUST");
 		user.setGroupCode(new GroupCode("USG_CUST1", false));
 		//Creates user
 		user = userManagerInterface.createUser(alsi, new ChannelCode("CH_DIRECT"), user, domain);
 		
 		log.debug("[createUser] ==> Ends");
 		return user;
 	}
 	
 	
 	public MDAuthenticationPrompts getMDPrompts(SecurityDomain domain,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username) throws ALSIInvalidException, NoFunctionPrivilegeException, SeedingException, ObjectNotFoundException, InvalidParameterException, InternalException, RemoteException{
 		return authenticatorInterface.getMDAuthenticationPrompts (new UserCode(username), channel, authenticationTypeCode,domain);
 	}
 	
 	public MDAuthenticator getMDPromptsForCreation(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode) throws ALSIInvalidException, NoFunctionPrivilegeException, SeedingException, ObjectNotFoundException, InvalidParameterException, InternalException, RemoteException, AuthenticatorException{
 		return authenticatorManagerInterface.getMDAuthenticatorForCreation(alsi,channel,authenticationTypeCode, domain);
 	}
 	public void createMDauthenticator(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,MDAuthenticationAnswer[] mdAuthenticationAnswers) throws InvalidChannelException, AuthenticatorException, NoFunctionPrivilegeException, CreateDuplicateException, ALSIInvalidException, ObjectNotFoundException, ConstraintFailedException, InvalidParameterException, RemoteException, InternalException, MDAnswerException, SeedingException{
 		log.debug("[createMDauthenticator] ==> Starts");
 		MDAuthenticator mdauthenticator = new MDAuthenticator();
 		mdauthenticator.setUserCode(new UserCode(username));
 		log.debug("[createMDauthenticator] username: " + username);
 		mdauthenticator.setAuthenticationTypeCode(authenticationTypeCode);
 		log.debug("[createMDauthenticator] authenticationTypeCode: " + authenticationTypeCode.getCode());
 		log.debug("[createMDauthenticator] adding questions & answsers");
 		mdauthenticator.setPrompts((getMDPromptsForCreation(domain,alsi, channel, authenticationTypeCode)).getPrompts());
 		mdauthenticator.setAnswers(mdAuthenticationAnswers);
 		mdauthenticator.setStatus("ENABLED");
 		authenticatorManagerInterface.createMDAuthenticator(alsi, channel,mdauthenticator,domain);
 		log.debug("[createMDauthenticator] ==> Ends");
 	}
 	
 	public void createUPauthenticator(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,String password) throws InvalidChannelException, AuthenticatorException, NoFunctionPrivilegeException, CreateDuplicateException, ALSIInvalidException, ObjectNotFoundException, ConstraintFailedException, InvalidParameterException, RemoteException, InternalException{
 		log.debug("[createUPauthenticator] ==> Starts");
 		UPAuthenticator upAuthenticator = new UPAuthenticator();
 		upAuthenticator.setUsername(username);
 		log.debug("[createUPauthenticator] username: " + username);
 		upAuthenticator.setPassword(password);
 		upAuthenticator.setAuthenticationTypeCode(authenticationTypeCode);
 		log.debug("[createUPauthenticator] authenticationTypeCode: " + authenticationTypeCode.getCode());
 		upAuthenticator.setUserCode(new UserCode(username));
 		upAuthenticator.setStatus("ENABLED");
 		log.debug("[createUPauthenticator] channel: " + channel.getCode());
 		authenticatorManagerInterface.createUPAuthenticator(alsi, channel, upAuthenticator, domain);
 		
 		log.debug("[createUPauthenticator] ==> Ends");
 	}
 		
 	public void createOOBauthenticator(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,String activationCode) throws InvalidChannelException, ALSIInvalidException, DeviceException, CreateDuplicateException, AuthenticationTierException, ObjectNotFoundException, DeviceAuthenticationException, NoFunctionPrivilegeException, AuthenticatorException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[createOOBauthenticator] ==> Starts");
 		Parameter param = new Parameter();
 		param.setName(AuthenticationResponseConstants.PARAMETER_ACTIVATION_CODE);
 		param.setValue(activationCode);
 		Parameter[] paramArray = new Parameter[1];
 		paramArray[0] = param;
 		authenticatorManagerInterface.registerUserForOutOfBand(alsi, channel, new UserCode(username), authenticationTypeCode, paramArray, domain);
 		log.debug("[createOOBauthenticator] ==> Ends");
 	}
 		
 	public UPAuthenticator[] getExistingUPAuthenticator(SecurityDomain domain,ALSI alsi,ChannelCode channel,String username) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[getExistingUPAuthenticator] ==> Starts");
 		AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		
 		// look for Static Password (UP) authenticator
 		UPAuthenticator[] userUPAuthenticator = null;
 		userUPAuthenticator = authenticatorManagerInterface.getAllUPAuthenticatorsForUser(alsi, channel, userCode, domain);
 		log.debug("[getExistingUPAuthenticator] ==> Ends");
 		return userUPAuthenticator;
 	}
 	
 	public MDAuthenticator[] getExistingMDAuthenticator(SecurityDomain domain,ALSI alsi,ChannelCode channel,String username) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[getExistingMDAuthenticator] ==> Starts");
 		AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		
 		// look for Static Password (UP) authenticator
 		MDAuthenticator[] userMDAuthenticator = null;
 		userMDAuthenticator = authenticatorManagerInterface.getAllUserMDAuthenticators(alsi, channel, userCode, domain);
 		log.debug("[getExistingMDAuthenticator] ==> Ends");
 		return userMDAuthenticator;
 	}
 	
 	public Authenticator[] getExistingAuthenticator(SecurityDomain domain,ALSI alsi,ChannelCode channel,String username) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[getExistingAuthenticator] ==> Starts");
 		AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		
 		// look for  authenticator
 		Authenticator[] userDeviceAuthenticator = null;
 		Authenticator[] userAuthenticator = null;
 		userAuthenticator = authenticatorManagerInterface.getAllAuthenticatorsForUser(alsi, channel, userCode, domain);
 		log.debug("[getExistingAuthenticator] ==> Ends");
 		return userAuthenticator;
 	}
 	
 	public void deleteUser(SecurityDomain domain,ALSI alsi,String username) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[deleteUser] ==> Starts");
 		userManagerInterface.deleteUser(alsi, remoteChannel, new UserCode(username), domain);
 		log.debug("[deleteUser] ==> Ends");
 	}
 	public boolean sendOOBOTP(SecurityDomain domain,ALSI alsi,ChannelCode channel, AuthenticationTypeCode authenticationTypeCode,String username, String activationCode) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException, InvalidChannelException, AuthenticationTierException, DeviceAuthenticationException, SeedingException, DeviceException, PasswordExpiredException{
 		log.debug("[sendOOBOTP] ==> Starts");
 		boolean result=false;
 		AuthenticationTypeCode userAuthType = null;
 		UserCode userCode= new UserCode(username);
 		AuthenticationResponse authenticationResponse=null;
 		// look for authenticator
 		Authenticator[] userDeviceAuthenticator = null;
 		log.debug("[sendOOBOTP] checking for Authenticators");
 		Authenticator[] userAuthenticator = null;
 		userAuthenticator = authenticatorManagerInterface.getAllAuthenticatorsForUser(alsi, channel, userCode, domain);
 		for(int i=0;i<userAuthenticator.length;i++){
 			userAuthType = userAuthenticator[i].getAuthenticationTypeCode();
 			if(userAuthType.getCode().equals(authenticationTypeCode.getCode())){
 				//logAuthenticator(userAuthenticator[i]);
 				AuthenticationRequest userAuthenticationRequest = new AuthenticationRequest();
 				UserSearchCriteria crit = new UserSearchCriteria();
 				crit.setUserCode(userCode); 		// user
 				userAuthenticationRequest.setUserSearchCriteria(crit);
 				userAuthenticationRequest.setOneTimePassword(activationCode); 		// verification code
 				userAuthenticationRequest.setAuthenticationMode(AuthenticationRequest.SYNCHRONOUS);
 				userAuthenticationRequest.setAuthenticationTypeCode(userAuthType);
 				
 				authenticationResponse = authenticatorInterface.indirectPrimaryAuthenticateDevice(alsi, channel, userAuthenticationRequest, domain);
 				//logAuthenticationResponse(authenticationResponse);
 		    	if ( authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED ){
 		    		com.aspace.ftress.interfaces70.ftress.DTO.AuthenticationResponseParameter[] authenticationResponseParameters = authenticationResponse.getParameters();
 		    		boolean oobOtpSent = false;
 		    		boolean oobAuthProcess = false;
 		    		for(int j=0;j<authenticationResponseParameters.length;j++){
 		    			log.debug("[sendOOBOTP] " +authenticationResponseParameters[j].getName() + " = " + authenticationResponseParameters[j].getValue());
 		    			if(authenticationResponseParameters[j].getName().equals(AuthenticationResponseConstants.PARAMETER_AUTHENTICATION_OOB_SENT) && authenticationResponseParameters[j].getValue().equals(AuthenticationResponseConstants.PARAMETER_VALUE_AUTHENTICATION_OOB_SENT_TRUE )){
 		    				log.debug("[sendOOBOTP] PARAMETER_AUTHENTICATION_OOB_SENT");
 		    				oobOtpSent = true;
 		    			}
 		    			if(authenticationResponseParameters[j].getName().equals(AuthenticationResponseConstants.PARAMETER_AUTHENTICATION_PROCESS) && authenticationResponseParameters[j].getValue().equals(AuthenticationResponseConstants.PARAMETER_VALUE_AUTHENTICATION_PROCESS_INCOMPLETE)){
 		    				log.debug("[sendOOBOTP] PARAMETER_AUTHENTICATION_PROCESS");
 		    				oobAuthProcess = true;
 		    			}
 		    		}
 		    		if(oobAuthProcess && oobOtpSent){
 		    			result = true;
 		    			log.info("[sendOOBOTP] OOB OTP Sent");
 		    		}else{
 		    			log.info("[sendOOBOTP] OOB OTP Not Sent");
 		    		}
 			    }else if (authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_FAILED ){
 			    	log.info("[sendOOBOTP] OOB OTP Not Sent");
 			    }
 			//end if
 			}
 		//end for
 		}
 		
 		log.debug("[sendOOBOTP] ==> Ends");
 		return result;
 	}
 	
 	public DeviceIssuanceRequestEntry createWebSoftTokenIssuanceRequest(SecurityDomain domain,ALSI alsi,ChannelCode channel,DeviceTypeCode deviceTypeCode,AuthenticationTypeCode authenticationTypeCode,String username,String serialNumber) throws InvalidChannelException, AuthenticatorException, NoFunctionPrivilegeException, CreateDuplicateException, ALSIInvalidException, DeviceException, ObjectNotFoundException, InvalidParameterException, RemoteException, InternalException{
 		log.debug("[createWebSoftTokenIssuanceRequest] Starts");
 		String activationCode=null;
 		UserCode userCode = new UserCode();
 		userCode.setCode(username);
 		
 		// Retrieve the current device issuance requests for the user with the defined device type
 		// ========================================================================================
 		DeviceIssuanceSearchCriteria devIssSearchCriteria = new DeviceIssuanceSearchCriteria();
 		devIssSearchCriteria.setDeviceTypeCode(deviceTypeCode);
 
 		UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
 		userSearchCriteria.setUserCode(userCode);
 		devIssSearchCriteria.setUserSearchCriteria(userSearchCriteria);
 
 		DeviceIssuanceRequestStatus deviceReqStatus = new DeviceIssuanceRequestStatus();
 		deviceReqStatus.setStatus(DeviceIssuanceRequestConstants.UNPROCESSED);
 		devIssSearchCriteria.setStatus(deviceReqStatus);
 
 
 		// Delete device issuance requests with state UNPROCESSED
 		// =================================================================
 		log.debug("[createWebSoftTokenIssuanceRequest] deleteDeviceIssuanceRequests(): ...");
 		long deleteResult = deviceManager.deleteDeviceIssuanceRequests(
 				alsi,
 				channel,
 				devIssSearchCriteria,
 				domain);
 		log.debug("[createWebSoftTokenIssuanceRequest] deleteDeviceIssuanceRequests(): result=" + deleteResult);
 		log.debug("[createWebSoftTokenIssuanceRequest] deleteDeviceIssuanceRequests(): OK");
 		
 		// Create device issuance request
 		// ==============================
 		DeviceIssuanceRequest deviceIssuanceReq = new DeviceIssuanceRequest();
 		deviceIssuanceReq.setAuthenticationTypeCode(authenticationTypeCode);
 		deviceIssuanceReq.setDeviceTypeCode(deviceTypeCode);
 		deviceIssuanceReq.setUserCode(userCode);
 
 		log.debug("[createWebSoftTokenIssuanceRequest] createDeviceIssuanceRequest(): ...");
 		deviceManager.createDeviceIssuanceRequest(
 				alsi,
 				channel,
 				deviceIssuanceReq,
 				domain);
 		log.debug("[createWebSoftTokenIssuanceRequest] createDeviceIssuanceRequest(): OK");
 
 		// Retrieve the device issuance request just created
 		// =================================================
 		log.debug("[createWebSoftTokenIssuanceRequest] retrieveDeviceIssuanceRequests(): ...");
 		
 		DeviceIssuanceRequestEntry[] devReqs = deviceManager.retrieveDeviceIssuanceRequests(
 									alsi,
 									channel,
 									devIssSearchCriteria,
 									domain);
 		if (devReqs==null){
 			log.debug("[createWebSoftTokenIssuanceRequest] retrieveDeviceIssuanceRequests: OK (returned null");
 		} else{
 			log.debug("[createWebSoftTokenIssuanceRequest] retrieveDeviceIssuanceRequests: OK (count = " + devReqs.length + ")");	
 		}
 
 		DeviceIssuanceRequestEntry devIssReqEntry = null;
 		if (devReqs != null) {
 			for (DeviceIssuanceRequestEntry devReq : devReqs) {
 				if(devReq.getUserCode() != null && username.equals(devReq.getUserCode().getCode())){
 					if(devIssReqEntry == null){
 						devIssReqEntry = devReq;
 					}else{
 						log.error("[createWebSoftTokenIssuanceRequest] retrieveDeviceIssuanceRequests() returned too many device issuance request for user " + username);
 						return null;
 					}
 				}
 			}
 		}
 
 		if (devIssReqEntry == null) {
 			log.error("[createWebSoftTokenIssuanceRequest] no retrieveDeviceIssuanceRequests() returned for usercode " + username );
 			return null;
 		}
 
 		// Update device issuance request
 		// ==============================
 
 		// Set status
 		DeviceIssuanceRequestStatus status = new DeviceIssuanceRequestStatus();
 		status.setStatus(DeviceIssuanceRequestConstants.IN_ISSUANCE);
 		devIssReqEntry.setStatus(status);
 
 		// Set serial number in the parameters
 		Parameter[] parameters = new Parameter[1];
 		parameters[0]= new Parameter();
 		parameters[0].setName(STMConstants.SERIAL_NUMBER);
 		parameters[0].setValue(serialNumber);
 		devIssReqEntry.setParameters(parameters);
 		log.debug("[createWebSoftTokenIssuanceRequest] updateDeviceIssuanceRequest (submit serial number): ...");
 		
 		DeviceIssuanceRequestEntry devIssReqEntryResult = deviceManager.updateDeviceIssuanceRequest(
 																			alsi,
 																			channel,
 																			devIssReqEntry,
 																			domain);
 		log.debug("[createWebSoftTokenIssuanceRequest] updateDeviceIssuanceRequest (submit serial number): OK");
 
 		if (devIssReqEntryResult==null)	{
 			log.error("[createWebSoftTokenIssuanceRequest] updateDeviceIssuanceRequest() returned null!");
 			return null;
 		}
 
 		
 		log.debug("[createWebSoftTokenIssuanceRequest] ==> Ends");
 		return devIssReqEntryResult;
 	}
 	
 	public String getActivationCodeFromDeviceRequestEntry(DeviceIssuanceRequestEntry devIssReqEntryResult){
 		String activationCode=null;
 		// Get activation code from parameters
 		// ===================================
 		if (devIssReqEntryResult.getParameters()!=null)	{
 			Parameter[] params = devIssReqEntryResult.getParameters();
 			for (int j = 0; j < params.length; j++)	{
 				log.debug("[createWebSoftTokenIssuanceRequest] deviceIssuanceRequestResultParameter " + params[j].getName() + " = " + params[j].getValue().toString());
 				if (params[j].getName().equals(STMConstants.ACTIVATION_CODE)) {
 					activationCode = params[j].getValue().toString();
 					log.debug("[createWebSoftTokenIssuanceRequest] Got Activation code: " + activationCode);
 				}
 			}
 		}
 		return activationCode;
 	}
 	public boolean updateWebSoftTokenIssuanceRequest(SecurityDomain domain,ALSI alsi,ChannelCode channel,DeviceTypeCode deviceTypeCode,String username,DeviceIssuanceRequestEntry devIssReqEntry,String serialNumber,String activationCode,String registrationCode) throws InvalidChannelException, AuthenticatorException, NoFunctionPrivilegeException, CreateDuplicateException, ALSIInvalidException, DeviceException, ObjectNotFoundException, InvalidParameterException, RemoteException, InternalException{
 		log.debug("[updateWebSoftTokenIssuanceRequest] Starts");
 		log.debug("[updateWebSoftTokenIssuanceRequest] Parameters username: #" + username + "# serialNumber: #" + serialNumber + "# registrationCode: #" + registrationCode + "#");
 
 		boolean result =false;
 		UserCode userCode = new UserCode();
 		userCode.setCode(username);
 		
 		
 		// Update device issuance request
 		// ==============================
 		DeviceIssuanceRequestStatus status = new DeviceIssuanceRequestStatus();
 		status.setStatus(DeviceIssuanceRequestConstants.REG_PROCESS);
 		devIssReqEntry.setStatus(status);
 
 		// Set Parameters
 		Parameter[] parameters_reg = new Parameter[3];
 		parameters_reg[0]= new Parameter();
 		parameters_reg[0].setName(STMConstants.SERIAL_NUMBER);
 		parameters_reg[0].setValue(serialNumber);
 
 		parameters_reg[1]= new Parameter();
 		parameters_reg[1].setName(STMConstants.ACTIVATION_CODE);
 		parameters_reg[1].setValue(activationCode);
 
 		parameters_reg[2]= new Parameter();
 		parameters_reg[2].setName(STMConstants.REG_CODE);
 		parameters_reg[2].setValue(registrationCode);
 	      
 		devIssReqEntry.setParameters(parameters_reg);
 
 		log.debug("[updateWebSoftTokenIssuanceRequest] updateDeviceIssuanceRequest (submit registration code): ...");
 
 		DeviceIssuanceRequestEntry devIssReqEntryResult = deviceManager.updateDeviceIssuanceRequest(
 				alsi,
 				channel,
 				devIssReqEntry,
 				domain);
 		if (devIssReqEntryResult==null)	{
 			log.error("[updateWebSoftTokenIssuanceRequest] updateDeviceIssuanceRequest() returned null!");
 			result = false;
 		}else{
 			log.info("[updateWebSoftTokenIssuanceRequest] updateDeviceIssuanceRequest (submit registration code): OK");
 			result = true;
 		}
 		log.debug("[updateWebSoftTokenIssuanceRequest] ==> Ends");
 		return result;
 	}
 	
 	public boolean deleteDevice(SecurityDomain domain,ALSI alsi,ChannelCode channel,String serialNumber,String username) throws DeleteObjectException, ALSIInvalidException, NoFunctionPrivilegeException, ObjectNotFoundException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[deleteDevice] ==> Starts");
 		UserCode userCode = new UserCode();
 		userCode.setCode(username);
 		
 		DeviceSearchCriteria deviceSearchCriteria = new DeviceSearchCriteria();
 		deviceSearchCriteria.setSerialNumber(serialNumber);
 		deviceSearchCriteria.setUserCode(userCode);
 		
 		DeviceSearchResults deviceResults = deviceManager.searchDevices(alsi, channel, deviceSearchCriteria, domain);
 		if (deviceResults != null && deviceResults.getDevices() != null && deviceResults.getDevices().length > 1) {
 			log.debug("[deleteDevice] Found device matching serial number and usercode");
 			Device[] devices = deviceResults.getDevices();
 			if (devices.length == 1) {
 				DeviceId[] deviceIDs = new DeviceId[1];
 				deviceIDs[0]= devices[0].getDeviceId();
 				log.debug("[deleteDevice] Deleting device...");
 				deviceManager.deleteDevices(alsi, channel, deviceIDs, domain);
 				log.debug("[deleteDevice] Deleting device OK");
 			}else{
 				log.error("[deleteDevice] Too many device matche");
 				return false;
 			}
 		}else{
 			log.error("[deleteDevice] No device match");
 			return false;
 		}
 		log.debug("[deleteDevice] ==> Ends");
 		return true;
 	}
 	
 	public String getSessionTransferCode(SecurityDomain domain,ALSI alsi,ALSI palsi) throws ALSIInvalidException, ObjectNotFoundException, SessionTransferException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[getSessionTransferCode] ==> Starts");
 		String sessionTranferCode=null;
 		
 		SessionTransferRequest request = new SessionTransferRequest();
 		request.setPalsi(palsi);
 		request.setSessionTransferType(new SessionTransferTypeCode("ALP001"));
 		//10 seconds 10.000
 		//request.setExpiryPeriod(new Long(10000));
 		//10 minutes 600.000
 		request.setExpiryPeriod(new Long(600000));
 		SessionTransferCode stc= sessionTransfer.createSessionTransferCode(alsi, request, domain);
 		sessionTranferCode = stc.getSessionTransferCode();
 		log.debug("[getSessionTransferCode] sessionTranferCode: " + sessionTranferCode);
 		log.debug("[getSessionTransferCode] ==> Ends");
 		return sessionTranferCode;
 	}
 	public ALSI retrieveALSIBySessionTransferCode(SecurityDomain domain,ALSI alsi,String sessionTransferCode) throws ALSIInvalidException, ObjectNotFoundException, SessionTransferException, InvalidParameterException, InternalException, RemoteException{
 		log.debug("[retrieveALSIBySessionTransferCode] ==> Starts");
 		ALSI palsi=null;
 		palsi = sessionTransfer.retrieveALSIBySessionTransferCode(alsi, new SessionTransferCode(sessionTransferCode), domain);
 		log.debug("[retrieveALSIBySessionTransferCode] ==> Ends");
 		return palsi;
 	}
 	public boolean verifySignature(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode, String username,String signature,String strParam1,String strParam2,String strParam3,String strParam4) throws ObjectNotFoundException, NoFunctionPrivilegeException, InvalidChannelException, AuthenticationTierException, DeviceAuthenticationException, SeedingException, ALSIInvalidException, DeviceException, PasswordExpiredException, RemoteException, InternalException, InvalidParameterException{
 		log.debug("[verifySignature] ==> Starts");
 		boolean result = false;
 		log.debug("[verifySignature] Creating AuthenticationRequest...");
 		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
 		authenticationRequest.setAuthenticationMode(AuthenticationRequest.SIGN_SYNCHRONOUS);
 		authenticationRequest.setAuthenticationTypeCode(authenticationTypeCode);
 		authenticationRequest.setChallenge(null);
 		authenticationRequest.setOneTimePassword(signature);
 
 		UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
 		userSearchCriteria.setUserCode(new UserCode(username));
 
 		authenticationRequest.setUserSearchCriteria(userSearchCriteria);
 		
 		log.debug("[verifySignature] Adding signature parameters to AuthenticationRequest.");
 		// now add the authentication parameters 1.
 		AuthenticationRequestParameter param1 = new AuthenticationRequestParameter("SIG_PARAM1", strParam1);
 		param1.setOrder(0);
 		param1.setSign(true);
 		
 		// now add the authentication parameters 2.
 		AuthenticationRequestParameter param2 = new AuthenticationRequestParameter("SIG_PARAM2", strParam2);
 		param2.setOrder(1);
 		param2.setSign(true);
 		
 		// now add the authentication parameters 3.
 		AuthenticationRequestParameter param3 = new AuthenticationRequestParameter("SIG_PARAM3", strParam3);
 		param3.setOrder(2);
 		param3.setSign(true);
 		
 		// now add the authentication parameters 4.
 		AuthenticationRequestParameter param4 = new AuthenticationRequestParameter("SIG_PARAM4", strParam4);
 		param4.setOrder(3);
 		param4.setSign(true);
 
 		authenticationRequest.setParameters(new AuthenticationRequestParameter[] {param1, param2, param3 , param4});
 		log.debug("[verifySignature] AuthenticationRequest created.");
 		
 		AuthenticationResponse authenticationResponse = authenticatorInterface.indirectPrimaryAuthenticateDevice(alsi,channel,authenticationRequest, domain);
 		
 		if (authenticationResponse.getResponse() == AuthenticationResponse.RESPONSE_AUTHENTICATION_SUCCEEDED) {
 			log.debug("[verifySignature] Got successful authentication response.");
 			result = true;
 		} else {
 			log.debug("[verifySignature] Got failed authentication response.");
 			result = false;
 		}
 		log.debug("[verifySignature] ==> Ends");
 		return result;
 	}
 	public String createExternalAuditRecord(SecurityDomain domain,ALSI alsi,ALSI palsi,ChannelCode channel,String entity,String event,Parameter parameters[]) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException {
 		log.debug("[createExternalAuditRecord] ==> Starts");
 		String auditId = "";
 		ExternalAuditRequest externalAuditRequest = new ExternalAuditRequest();
 		
 		
 		EntityIdentifier entityIdentifier = new EntityIdentifier();
 		entityIdentifier.setId(entity);
 		//entityIdentifier.setType("Type " + entity);
 		
 		externalAuditRequest.setEntityIdentifier(entityIdentifier);
 		
 		EventIdentifier eventIdentifier = new EventIdentifier();
 		eventIdentifier.setId(event);
 		//eventIdentifier.setType("Type " + event);
 		
 		externalAuditRequest.setEventIdentifier(eventIdentifier);
 		externalAuditRequest.setTargetUserID(getSessionUserCode(domain, palsi));
 		externalAuditRequest.setParameters(parameters);
 		externalAuditRequest.setIndirectSessionID(palsi);
 		
 		log.debug("[createExternalAuditRecord] About to create audit record.");
 		ExternalAuditResponse externalAuditResponse = auditor.indirectAudit(alsi, palsi, channel, externalAuditRequest, domain);
 		
 		log.debug("[createExternalAuditRecord] Audit record created.");
 		if(externalAuditResponse!=null){
 			if(externalAuditResponse.getAuditId()!=null){
 				auditId = externalAuditResponse.getAuditId().getIdAsString();
 				log.debug("[createExternalAuditRecord] AuditID: " + auditId);
 			}
 		}
 		log.debug("[createExternalAuditRecord] ==> Ends");
 		return auditId;
 	}
 	public String getChallenge(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode,String username) throws PasswordExpiredException, DeviceAuthenticationException, DeviceException, ObjectNotFoundException, SeedingException, InternalException, RemoteException, InvalidParameterException{
 		log.debug("[getChallenge] ==> Starts");
 		String challenge ="";
 		AuthenticationChallenge authChallenge = authenticatorInterface.getAuthenticationChallengeByUserCode(channel, new UserCode(username), authenticationTypeCode, domain);
 		if(authChallenge!=null){
 			challenge = authChallenge.getChallenge();
 			log.debug("[getChallenge] Challenge is: " + challenge);
 		}else{
 			log.error("[getChallenge] Error got a null challenge");
 		}
 		log.debug("[getChallenge] ==> Ends");
 		return challenge;
 	}
 	public ALSI verifyUserResponse(SecurityDomain domain,ALSI alsi,ChannelCode channel,AuthenticationTypeCode authenticationTypeCode,String username,String password) throws ObjectNotFoundException, NoFunctionPrivilegeException, InvalidChannelException, AuthenticationTierException, DeviceAuthenticationException, SeedingException, ALSIInvalidException, DeviceException, PasswordExpiredException, RemoteException, InternalException, InvalidParameterException{
 		log.debug("[verifyUserResponse] ==> Starts");
 		ALSI palsi=null;
 		AuthenticationRequest authRequest = new AuthenticationRequest();
 		authRequest.setAuthenticationTypeCode(authenticationTypeCode);
 		authRequest.setOneTimePassword(password);
 		
 		UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
 		UserCode indirectUserCode = new UserCode(username);
 		userSearchCriteria.setUserCode(indirectUserCode);
 		
 		authRequest.setUserSearchCriteria(userSearchCriteria);
 		authRequest.setAuthenticationMode(AuthenticationRequest.ASYNCHRONOUS);
 		
 		AuthenticationResponse authResp = authenticatorInterface.indirectPrimaryAuthenticateDevice(alsi, channel, authRequest, domain);
 		if (authResp.getResponse() == AuthenticationResponseConstants.RESPONSE_AUTHENTICATION_SUCCEEDED) {
 			palsi = authResp.getAlsi();
 			log.info("[verifyUserResponse] Authentication success with challenge response for user: " + username + " with response: " + password);
 		}else{
 			log.info("[verifyUserResponse] Authentication failed with challenge response for user: " + username + " with response: " + password);
 		}
 		log.debug("[verifyUserResponse] ==> Ends");
 		return palsi;
 	}
 	
 	public Device addDevice(ALSI alsi, ChannelCode channel, Device device, SecurityDomain domain) throws NoFunctionPrivilegeException, DeviceException, ObjectNotFoundException, ALSIInvalidException, CreateDuplicateException, InternalException, RemoteException, InvalidParameterException
 	{
 
 		return  deviceManager.addDevice(alsi, channel, device, domain);
 	}
 	
 	public Device assignDeviceToUser(ALSI alsi, ChannelCode channel, DeviceId deviceId, UserCode userCode, SecurityDomain domain) throws ALSIInvalidException, DeviceException, NoFunctionPrivilegeException, ObjectNotFoundException, InvalidParameterException, InternalException, RemoteException
 	{
 		return deviceManager.assignDeviceToUser(alsi, channel, deviceId, userCode, domain);
 	}
 	
 	public void updateUserStatus(ALSI alsi, ChannelCode channel, String username, String status, SecurityDomain domain) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException
 	{
 		UserCode userCode = new UserCode();
 		userCode.setCode(username);
 		User user = new User();
 		user.setCode(userCode);
 		user.setStatus(status);
 		userManagerInterface.updateUserStatus(alsi, channel, user, domain);
 	}
 	
 	public void updateUPAuthenticatorStatus(ALSI alsi, ChannelCode channel, String username, AuthenticationTypeCode authenticationTypeCode, String status, SecurityDomain domain) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException, AuthenticatorException
 	{
 		UserCode userCode = new UserCode();
 		userCode.setCode(username);
 		AuthenticatorStatus authStatus = new AuthenticatorStatus();
 		authStatus.setStatus(status);
 
 		authenticatorManagerInterface.updateUPAuthenticatorStatus(alsi, channel, userCode, authenticationTypeCode, authStatus, domain);
 
 	}
 	
 	public AuditSearchResults searchAudit(ALSI alsi, ChannelCode channel, AuditSearchCriteria crit, SecurityDomain domain) throws ALSIInvalidException, ObjectNotFoundException, NoFunctionPrivilegeException, InvalidParameterException, InternalException, RemoteException
 	
 	{
 		return auditor.searchAuditLog(alsi, channel, crit, domain);
 	}
 	
 	
 	public void changeUserPassword(ALSI alsi, ChannelCode channel, String username, AuthenticationTypeCode authenticationTypeCode, String password, SecurityDomain domain ) throws ConstraintFailedException, NoFunctionPrivilegeException, ObjectNotFoundException, ALSIInvalidException, InvalidChannelException, InternalException, RemoteException, InvalidParameterException
 	{
 		Password pCode = new Password();
 		pCode.setPassword(password);
 		UserCode userCode = new UserCode();
 		userCode.setCode(username);
 
 		credentialManager.changeUserPassword(alsi, channel, userCode, authenticationTypeCode, pCode, new SecurityDomain("DOMAIN1"));
 
 	}
 }
