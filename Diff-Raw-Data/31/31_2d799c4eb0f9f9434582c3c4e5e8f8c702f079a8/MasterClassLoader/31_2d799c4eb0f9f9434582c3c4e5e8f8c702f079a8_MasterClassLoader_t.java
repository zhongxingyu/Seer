 package app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.AllPermission;
 import java.security.CodeSource;
 import java.security.Permissions;
 import java.security.ProtectionDomain;
 import java.security.cert.Certificate;
 
 final class MasterClassLoader
 		extends ClassLoader
 {
	private static final MasterClassLoader INSTANCE = new MasterClassLoader();
 	private static ClassLoader _systemClassLoader;
 
 	@Override
 	public final Class<?> loadClass(String name)
 			throws ClassNotFoundException
 	{
 		if (appletviewer.Debug) {
 			System.out.println("MasterClassLoader.loadClass(name = " + name + ")");
 		}
 
 		if (name.equals("netscape.javascript.JSObject")) {
 			Permissions permissions = new Permissions();
 			permissions.add(new AllPermission());
 			ProtectionDomain protectionDomain = new ProtectionDomain(new CodeSource(null, (Certificate[]) null), permissions);
 
 			try {
 				URL jsObjectUrl = super.getClass().getClassLoader().getResource("netscape/javascript/JSObjec_.class");
 				URLConnection jsObjUrlConnection = jsObjectUrl.openConnection();
 
 				byte[] jsObjectClass = new byte[jsObjUrlConnection.getContentLength()];
 				InputStream reader = jsObjUrlConnection.getInputStream();
 				for (int i = 0; i < jsObjectClass.length; i += reader.read(jsObjectClass, i, jsObjectClass.length - i))
 					;
 				reader.close();
 
 				// JSObjec_ -> JSObject
 				for (int j = 0; j < jsObjectClass.length; j++) {
 					int k = jsObjectClass[j] & 0xFF;
 					if (k == "JSObject".charAt(0)) {
 						for (int l = 1; j + l < jsObjectClass.length; l++) {
 							// replace '_' with 't'
 							if ("JSObject".length() - 1 == l) {
 								jsObjectClass[j + l] = (byte) "JSObject".charAt(l);
 								break;
 							}
 
 							k = jsObjectClass[j + l] & 0xFF;
 							if ("JSObject".charAt(l) != k) {
 								break;
 							}
 						}
 					}
 				}
 
 				return defineClass(name, jsObjectClass, 0, jsObjectClass.length, protectionDomain);
 			} catch (IOException ioEx) {
 				if (appletviewer.Debug) {
 					ioEx.printStackTrace();
 				}
 				try {
 					return super.getClass().getClassLoader().loadClass(name);
 				} catch (Exception ex) {
                     if (appletviewer.Debug) {
                         ex.printStackTrace();
                     }
 				}
 			}
 		}
 
 		// fall back to the original system class loader
 		return _systemClassLoader.loadClass(name);
 	}
 
 	public static void init()
 	{
		try {
			// save system class loader
			_systemClassLoader = ClassLoader.getSystemClassLoader();
 
			// hijack system class loader
			Field scl = ClassLoader.class.getDeclaredField("scl");
			scl.setAccessible(true);
			scl.set(null, INSTANCE);
			scl.setAccessible(false);
		} catch (Exception ex) {
			if (appletviewer.Debug) {
				ex.printStackTrace();
 			}
 		}
 	}
 }
 
 /*
  * Location: \\.psf\Home\Documents\java\jagexappletviewer\ Qualified Name:
  * app.Class_e JD-Core Version: 0.5.4
  */
