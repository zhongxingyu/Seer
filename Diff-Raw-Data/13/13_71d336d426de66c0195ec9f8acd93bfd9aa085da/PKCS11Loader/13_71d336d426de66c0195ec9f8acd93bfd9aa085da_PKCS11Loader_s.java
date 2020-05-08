 package se.op.jsse;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Method;
 import java.security.KeyStore;
 import java.security.Provider;
 import java.security.KeyStore.ProtectionParameter;
 import java.security.Provider.Service;
 import java.security.Security;
 import java.security.cert.Certificate;
 import java.util.Arrays;
 import java.util.Enumeration;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.UnsupportedCallbackException;
 
import sun.security.pkcs11.SunPKCS11;

 public class PKCS11Loader {
 	private static final String CfgFile="MyPKCS11.cfg";
 	private static final String CfgName="TODOS_PERSONAL";
 
 	public static void main(String[] args) throws Exception {
 		{
 			PrintWriter pw=new PrintWriter(new File(CfgFile));
 			pw.println("name = "+CfgName);
 //			pw.println("library = /usr/libexec/SmartCardServices/pkcs11/tokendPKCS11.so");
 			pw.println("library = /Applications/Personal.app/Contents/PlugIns/CSPBundleNexus.bundle/Contents/MacOS/../Frameworks/tokenapi.framework/tokenapi");
 			pw.println("description = George Test");
 			pw.close();
 		}
 
		Class<java.security.AuthProvider> c=SunPKCS11.class;
		Method ctor=c.getDeclaredConstructor(CfgFile.class)//new SunPKCS11(CfgFile);
				Provider p =ctor.invoke(null,CfgFile); 
 				
 		int pos=Security.addProvider(p);
 
 		int i=0;
 		for (Provider pr : Security.getProviders()) {
 			System.err.println("Provider:["+(i++)+"]:"+pr+" ("+pr.getInfo()+")");
 		}
 		
 		//TODO use the Provider. SecurityInfo.main(args);
 		p=Security.getProvider("SunPKCS11-"+CfgName);
 		for(Service svc:p.getServices()){
 			System.err.println(svc);
 		}
 		KeyStore ks=KeyStore.getInstance("PKCS11", p);
 		System.err.println("Got ks of type:"+ks.getType()
 				//+" size:"+ks.size()
 				+" class:"+ks.getClass().getName()+" of course, its an spi thing");
 		
 		ks.load(new KeyStore.LoadStoreParameter() {
 			
 			@Override
 			public ProtectionParameter getProtectionParameter() {
 				System.err.println("getProtectionParameter");
 				return /*new KeyStore.CallbackHandlerProtection(new CallbackHandler() {
 					@Override
 					public void handle(Callback[] cbs) throws IOException, UnsupportedCallbackException {
 						System.err.println("handle("+Arrays.toString(cbs)+")");	
 						for (Callback cb : cbs) {
 							System.err.println("\t"+cb.getClass().getName());
 						}
 					}
 				});/*/
 				new KeyStore.PasswordProtection("165974".toCharArray());
 			}
 		});
 		System.err.println("About to do ks.aliases(). size:"+ks.size());
 		Enumeration<String> aliases=ks.aliases();
 		while(aliases.hasMoreElements()){
 			String alias=aliases.nextElement();
 			System.err.println("Alias:"+alias);
 			Certificate cert=ks.getCertificate(alias);
 			System.err.println(cert);
 		}
 		System.err.println("End of aliases.");
 		
 		System.err.println("program end.");
 	}
 
 }
