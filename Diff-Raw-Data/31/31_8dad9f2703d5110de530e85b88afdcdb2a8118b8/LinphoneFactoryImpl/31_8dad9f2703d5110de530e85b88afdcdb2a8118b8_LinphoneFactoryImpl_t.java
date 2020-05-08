 package org.linphone.jlinphone.core;
 
 import org.linphone.core.LinphoneAddress;
 import org.linphone.core.LinphoneAuthInfo;
 import org.linphone.core.LinphoneCoreException;
 import org.linphone.core.LinphoneCoreFactory;
 import org.linphone.core.LinphoneCoreListener;
 import org.linphone.core.LinphoneCore;
 import org.linphone.core.LinphoneProxyConfig;
 
 
 
 public class LinphoneFactoryImpl extends LinphoneCoreFactory {
 	
 	public LinphoneCore createLinphoneCore(LinphoneCoreListener listener, String userConfig,String factoryConfig,Object  userdata) throws LinphoneCoreException {
 		return new LinphoneCoreImpl(listener,userConfig,factoryConfig,userdata);
 	}
 
 	public LinphoneAuthInfo createAuthInfo(String username, String password, String realm) {
 		return new LinphoneAuthInfoImpl(username,password, null);
 	}
 
 	public LinphoneAddress createLinphoneAddress(String username,
 			String domain, String displayName) {
 		return null;
 	}
 	
 	public LinphoneProxyConfig createProxyConfig(String identity, String proxy,
 			String route, boolean enableRegister) throws LinphoneCoreException {
 		return new LinphoneProxyConfigImpl(identity,proxy,route,enableRegister);
 	}
 
 	
 	public void setDebugMode(boolean enable) {
 		// TODO Auto-generated method stub
 		
 	}
 	public LinphoneFactoryImpl(){
 		
 	}
 
 	public LinphoneAddress createLinphoneAddress(String address) {
		LinphoneAddress ret=null;
		try{
			ret=new LinphoneAddressImpl(address);
		}catch(Exception e){
			
		}
		return ret;
 	}
 }
