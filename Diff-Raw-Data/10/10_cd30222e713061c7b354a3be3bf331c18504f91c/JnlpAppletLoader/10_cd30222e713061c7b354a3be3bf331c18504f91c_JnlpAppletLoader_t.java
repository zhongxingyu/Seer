 package org.lwjgl.util.applet;
 
 import java.applet.Applet;
 import java.applet.AppletStub;
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class JnlpAppletLoader extends Applet implements AppletStub {
 
	private static final long serialVersionUID = -2459790398016588477L;

 	@Override
 	public void init() {
 
 		// parseParameters
 
 		// get jars info?
 
 		// check cache and remove already downloader jars from jars to download?
 		
 		final AppletParameters appletParameters = new AppletParametersProxy(new AppletParametersUtil(this)).getAppletParameters();
 		
		List<FileInfo> jarFiles = new ArrayList<FileInfo>();
		List<FileInfo> nativeFiles = new ArrayList<FileInfo>();
 		
 		List<FileInfo> files = new ArrayList<FileInfo>();
 		
 		files.addAll(jarFiles);
 		files.addAll(nativeFiles);
 		
 		Cache cache = new Cache(new HashMap<String, FileInfo>());
 		CacheFilter cacheFilter = new CacheFilter(cache);
 		
 		List<FileInfo> newFiles = cacheFilter.removeCachedFiles(files);
 		
 		// download jars
 
 		// update cache?
 		
 		// another stuff
 		
 		// switch applet
 
 		try {
 			EventQueue.invokeAndWait(new Runnable() {
 				public void run() {
 					try {
 						switchApplet(appletParameters.getMain());
 					} catch (Exception e) {
 						throw new RuntimeException("switch applet failed... ", e);
 					}
 					repaint();
 				}
 			});
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 	
 	/**
 	 * replace the current applet with the lwjgl applet using AppletStub and initialise and start it
 	 * @param appletClassName the applet class name to be loaded.
 	 */
 	@SuppressWarnings("unchecked")
 	protected void switchApplet(String appletClassName) throws Exception {
 		Class appletClass = classLoader.loadClass(appletClassName);
 		Applet applet = (Applet) appletClass.newInstance();
 
 		applet.setStub(this);
 		applet.setSize(getWidth(), getHeight());
 
 		setLayout(new BorderLayout());
 		add(applet);
 		validate();
 
 		applet.init();
 
 		applet.start();
 	}
 
 	@Override
 	public void appletResize(int width, int height) {
 		resize(width, height);
 	}
 
 }
