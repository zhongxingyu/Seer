 package com.buglabs.common.tests.junit;
 
 import junit.framework.TestCase;
 
 import com.buglabs.bug.dragonfly.module.IModuleControl;
 import com.buglabs.bug.dragonfly.module.IModuleLEDController;
 import com.buglabs.bug.dragonfly.module.IModuleProperty;
 import com.buglabs.bug.dragonfly.module.ModuleProperty;
 import com.buglabs.bug.dragonfly.module.MutableModuleProperty;
 import com.buglabs.device.ButtonEvent;
 import com.buglabs.device.IButtonEventListener;
 import com.buglabs.device.IButtonEventProvider;
 import com.buglabs.services.ws.DefaultWSImplementation;
 import com.buglabs.services.ws.DefaultWSImplementationWithParams;
 import com.buglabs.services.ws.HttpFormParser;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSAdmin;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider;
 import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.PublicWSProviderWithParams;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.Base64;
 import com.buglabs.util.osgi.BUGBundleConstants;
 import com.buglabs.util.osgi.ConfigAdminUtil;
import com.buglabs.util.osgi.FilterUtil;
 import com.buglabs.util.osgi.LogServiceUtil;
 import com.buglabs.util.osgi.OSGiServiceLoader;
 import com.buglabs.util.osgi.ServiceTrackerUtil;
 import com.buglabs.util.simplerestclient.BasicAuthenticationConnectionProvider;
 import com.buglabs.util.simplerestclient.DefaultConnectionProvider;
 import com.buglabs.util.simplerestclient.FormFile;
 import com.buglabs.util.simplerestclient.HTTPException;
 import com.buglabs.util.simplerestclient.HTTPRequest;
 import com.buglabs.util.simplerestclient.HTTPResponse;
 import com.buglabs.util.simplerestclient.IConnectionProvider;
 import com.buglabs.util.simplerestclient.IFormFile;
 import com.buglabs.util.ui.IDesktopApp;
import com.buglabs.util.xml.XmlNode;
import com.buglabs.util.xml.XmlParser;
import com.buglabs.util.xml.XpathQuery;
 
 /**
  * This test case simply loads BUG contributed classes to verify they are on the
  * classpath.
  * 
  * @author kgilmer
  * 
  */
 public class ClassAvailableTests extends TestCase {
 	public void testPackageDevice() {
 		testClass(ButtonEvent.class);
 		testClass(IButtonEventListener.class);
 		testClass(IButtonEventProvider.class);
 	}
 
 	public void testPackageApplication() {
 		testClass(IDesktopApp.class);	
 	}
 
 	public void testPackageModule() {
 		testClass(IModuleControl.class);
 		testClass(IModuleLEDController.class);
 		testClass(IModuleProperty.class);
 		testClass(ModuleProperty.class);
 		testClass(MutableModuleProperty.class);
 	}
 
 	public void testPackageServicesWs() {
 		testClass(DefaultWSImplementation.class);
 		testClass(DefaultWSImplementationWithParams.class);
 		testClass(HttpFormParser.class);
 		testClass(IWSResponse.class);
 		testClass(PublicWSAdmin.class);
 		testClass(PublicWSDefinition.class);
 		testClass(PublicWSProvider.class);
 		testClass(PublicWSProvider2.class);
 		testClass(PublicWSProviderWithParams.class);
 		testClass(WSResponse.class);
 	}
 
 
 	public void testPackageUtil() {
 		testClass(Base64.class);	
 	}
 	
 	public void testPackageUtilOSGi() {
 		testClass(BUGBundleConstants.class);
 		testClass(ServiceTrackerUtil.class);
 		testClass(ConfigAdminUtil.class);
 		testClass(LogServiceUtil.class);
 		testClass(OSGiServiceLoader.class);
 		testClass(FilterUtil.class);		
 	}
 	
 	public void testPackageUtilXml() {	
 		testClass(XmlNode.class);
 		testClass(XmlParser.class);
 		testClass(XpathQuery.class);
 	}
 
 	public void testPackageSimpleRestClient() {
 		testClass(BasicAuthenticationConnectionProvider.class);
 		testClass(DefaultConnectionProvider.class);
 		testClass(FormFile.class);
 		testClass(HTTPException.class);
 		testClass(HTTPRequest.class);
 		testClass(HTTPResponse.class);
 		testClass(IConnectionProvider.class);
 		testClass(IFormFile.class);
 	}
 	
 	private void testClass(Class<?> class1) {
 		System.out.println("Able to access " + class1.getName());
 	}
 
 }
