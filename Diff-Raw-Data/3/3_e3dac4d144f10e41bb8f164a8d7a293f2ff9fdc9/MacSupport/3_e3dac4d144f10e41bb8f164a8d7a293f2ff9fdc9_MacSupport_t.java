 package emcshop.gui;
 
 import java.awt.Image;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.security.AccessControlException;
 
 //@formatter:off
 /**
  * This class contains code which helps to better integrate the application into
  * the Mac OSX environment.
 * @see <a href="http://developer.apple.com/mac/library/documentation/Java/Reference/1.5.0/appledoc/api/index.html">http://developer.apple.com/mac/library/documentation/Java/Reference/1.5.0/appledoc/api/index.html</a>
 * @see <a href="https://developer.apple.com/search/index.php?q=java">https://developer.apple.com/search/index.php?q=java</a>
  * @author Michael Angstadt
  */
 //@formatter:on
 public class MacSupport {
 	/**
 	 * Runs initialization code specific to Mac OSX.
 	 * @param title the title of the application
 	 * @param enablePreferences true to enable the "Preferences" menu option,
 	 * false to disable it. If enabled, you must override
 	 * MacHandler.handlePreferences() in the handler parameter
 	 * @param dockImage the image that will appear in the dock or null for no
 	 * image.
 	 * @param handler handles the various Mac events
 	 */
 	public static void init(String title, boolean enablePreferences, Image dockImage, final MacHandler handler) {
 		try {
 			//enable Mac menu bar
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 
 			//set application name
 			System.setProperty("com.apple.mrj.application.apple.menu.about.name", title);
 
 			//create an implementation of the ApplicationListener interface
 			Class<?> applicationListenerInterface = Class.forName("com.apple.eawt.ApplicationListener");
 			Object applicationListenerInstance = Proxy.newProxyInstance(MacSupport.class.getClassLoader(), new Class<?>[] { applicationListenerInterface }, new InvocationHandler() {
 				@Override
 				public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
 					// the name of the invoked method
 					String methodName = method.getName();
 
 					// the com.apple.eawt.ApplicationEvent object
 					Object applicationEvent = arguments[0];
 
 					if (methodName.equals("handleQuit")) {
 						handler.handleQuit(applicationEvent);
 					} else if (methodName.equals("handleAbout")) {
 						handler.internalHandleAbout(applicationEvent);
 					} else if (methodName.equals("handlePreferences")) {
 						handler.handlePreferences(applicationEvent);
 					}
 					return null;
 				}
 			});
 
 			//equivalent to: Application applicationInstance = Application.getApplication();
 			Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
 			Method getApplicationMethod = applicationClass.getMethod("getApplication");
 			Object applicationInstance = getApplicationMethod.invoke(null);
 
 			//equivalent to: applicationInstance.setEnabledPreferencesMenu(true);
 			//the preferences menu option is disabled by default
 			if (enablePreferences) {
 				Method setEnabledPreferencesMenuMethod = applicationClass.getMethod("setEnabledPreferencesMenu", boolean.class);
 				setEnabledPreferencesMenuMethod.invoke(applicationInstance, enablePreferences);
 			}
 
 			//equivalent to: applicationInstance.addApplicationListener(applicationAdapterImplementation);
 			Method addApplicationListenerMethod = applicationClass.getMethod("addApplicationListener", applicationListenerInterface);
 			addApplicationListenerMethod.invoke(applicationInstance, applicationListenerInstance);
 
 			//this image will appear when then user alt-tabs and in the dock
 			//equivalent to: applicationInstance.setDockIconImage(...)
 			if (dockImage != null) {
 				Method setDockIconImageMethod = applicationClass.getMethod("setDockIconImage", Image.class);
 				setDockIconImageMethod.invoke(applicationInstance, dockImage);
 			}
 		} catch (AccessControlException e) {
 			/*
 			 * If the WebStart application does not have full permissions, this
 			 * exception will be thrown when run from WebStart in two places:
 			 * 
 			 * (1) when setting the
 			 * "com.apple.mrj.application.apple.menu.about.name" property and
 			 * (2) when getting the "com.apple.eawt.Application" class
 			 * 
 			 * However, WebStart does some of these things automatically. It
 			 * sets the title and the dock icon image. An application with full
 			 * permissions may not throw this exception.
 			 */
 		} catch (Throwable e) {
 			//ignore
 		}
 	}
 
 	/**
 	 * Determines if the application is running on a Mac.
 	 * @return true if the application is running on a Mac, false if not
 	 */
 	public static boolean isMac() {
 		String osName = System.getProperty("os.name").toLowerCase();
 		return osName.startsWith("mac os x");
 	}
 
 	/**
 	 * Runs initialization code specific to Mac OSX only if the application is
 	 * running on a Mac.
 	 * @param title the title of the application
 	 * @param enablePreferences true to enable the "Preferences" menu option,
 	 * false to disable it. If enabled, you must override
 	 * MacHandler.handlePreferences() in the handler parameter
 	 * @param dockImage the image that will appear in the dock or null for no
 	 * image.
 	 * @param handler handles the various Mac events
 	 */
 	public static void initIfMac(String title, boolean enablePreferences, Image dockImage, MacHandler handler) {
 		if (isMac()) {
 			init(title, enablePreferences, dockImage, handler);
 		}
 	}
 }
