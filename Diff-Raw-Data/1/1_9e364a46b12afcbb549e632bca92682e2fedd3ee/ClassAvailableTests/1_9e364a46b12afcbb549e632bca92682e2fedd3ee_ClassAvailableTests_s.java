 package com.buglabs.common.tests.junit;
 
 import junit.framework.TestCase;
 
 import com.buglabs.application.AbstractServiceTracker;
 import com.buglabs.application.AbstractServiceTracker2;
 import com.buglabs.application.IDesktopApp;
 import com.buglabs.application.IServiceProvider;
 import com.buglabs.application.MainApplicationThread;
 import com.buglabs.application.RunnableWithServices;
 import com.buglabs.application.ServiceChangeListener;
 import com.buglabs.application.ServiceTrackerHelper;
 import com.buglabs.device.ButtonEvent;
 import com.buglabs.device.IButtonEventListener;
 import com.buglabs.device.IButtonEventProvider;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.module.MutableModuleProperty;
 import com.buglabs.osgi.shell.ICommand;
 import com.buglabs.osgi.shell.IShellCommandProvider;
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
 import com.buglabs.util.BugBundleConstants;
 import com.buglabs.util.ConfigAdminUtil;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.OSGiServiceLoader;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.SelfReferenceException;
 import com.buglabs.util.ServiceFilterGenerator;
 import com.buglabs.util.XmlNode;
 import com.buglabs.util.XmlParser;
 import com.buglabs.util.XpathQuery;
 import com.buglabs.util.simplerestclient.BasicAuthenticationConnectionProvider;
 import com.buglabs.util.simplerestclient.DefaultConnectionProvider;
 import com.buglabs.util.simplerestclient.FormFile;
 import com.buglabs.util.simplerestclient.HTTPException;
 import com.buglabs.util.simplerestclient.HTTPRequest;
 import com.buglabs.util.simplerestclient.HTTPResponse;
 import com.buglabs.util.simplerestclient.IConnectionProvider;
 import com.buglabs.util.simplerestclient.IFormFile;
import com.sun.net.httpserver.BasicAuthenticator;
 
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
 		testClass(AbstractServiceTracker.class);
 		testClass(AbstractServiceTracker2.class);
 		testClass(IDesktopApp.class);
 		testClass(IServiceProvider.class);
 		testClass(MainApplicationThread.class);
 		testClass(RunnableWithServices.class);
 		testClass(ServiceChangeListener.class);
 		testClass(ServiceTrackerHelper.class);
 	}
 
 	public void testPackageModule() {
 		testClass(IModuleControl.class);
 		testClass(IModuleLEDController.class);
 		testClass(IModuleProperty.class);
 		testClass(ModuleProperty.class);
 		testClass(MutableModuleProperty.class);
 	}
 
 	public void testPackageOsgiShell() {
 		testClass(ICommand.class);
 		testClass(IShellCommandProvider.class);		
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
 		testClass(BugBundleConstants.class);
 		testClass(ServiceTrackerHelper.class);
 		testClass(ConfigAdminUtil.class);
 		testClass(LogServiceUtil.class);
 		testClass(OSGiServiceLoader.class);
 		testClass(RemoteOSGiServiceConstants.class);
 		testClass(SelfReferenceException.class);
 		testClass(ServiceFilterGenerator.class);
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
