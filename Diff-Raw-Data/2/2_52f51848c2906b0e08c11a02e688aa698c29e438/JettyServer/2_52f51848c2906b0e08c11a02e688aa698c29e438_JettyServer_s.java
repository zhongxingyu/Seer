 package org.centny.jetty4a.server.api;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Constructor;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.InvalidParameterException;
 import java.security.MessageDigest;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 /**
  * the JettyServer class for Auto deploy the web application package.
  * 
  * @author Centny
  * 
  */
 public class JettyServer extends Server {
 	/**
 	 * create a server in port by system properties.
 	 * 
 	 * @param port
 	 *            target port.
 	 * @return the JettyServer instance.
 	 */
 	public static JettyServer createServer(Class<?> js, int port) {
 		File wsdir = new File(System.getProperties().getProperty(
 				ServerListener.J4A_WDIR));
 		File dpdir = new File(System.getProperties().getProperty(
 				ServerListener.J4A_DDIR));
 		if (wsdir.exists() && dpdir.exists()) {
 			try {
 				Constructor<?> ctr = js.getConstructor(File.class, File.class,
 						int.class);
 				return (JettyServer) ctr.newInstance(wsdir, dpdir, port);
 			} catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			}
 		} else {
 			System.err.println("workspace or deploy not exist.");
 			return null;
 		}
 	}
 
 	// /////////////////
 	// the log.
 	private Logger log = Log.getLogger(JettyServer.class);
 	private File wsdir;// the workspace directory.
 	private File dydir;// the deploy directory.
 	// all context handler.
 	private ContextHandlerCollection contexts = new ContextHandlerCollection();
 	// context handler map by name.
 	private Map<String, Handler> servers = new HashMap<String, Handler>();
 	// all ServerListener.
 	private Set<ServerListener> listeners = new HashSet<ServerListener>();
 
 	/**
 	 * the default constructor.
 	 * 
 	 * @param wsdir
 	 *            the workspace directory.
 	 * @param deploy
 	 *            the deploy directory.
 	 * @param port
 	 *            the listen port.
 	 */
 	public JettyServer(File wsdir, File deploy, int port) {
 		super(port);
 		this.wsdir = wsdir;
 		this.dydir = deploy;
 		this.log.info("workspace:" + this.wsdir.getAbsoluteFile() + ",deploy:"
 				+ this.dydir);
 		if (!this.wsdir.exists()) {
 			this.wsdir.mkdirs();
 		}
 		if (!this.wsdir.exists()) {
 			throw new InvalidParameterException("initial server in workspace "
 					+ this.wsdir.getAbsolutePath() + " error");
 		}
 		this.setHandler(this.contexts);
 		this.loadEnv();
 	}
 
 	/**
 	 * load the environment configure file.
 	 */
 	public void loadEnv() {
 		String cdirPath = System.getProperty(ServerListener.J4A_CDIR);
 		if (cdirPath == null) {
 			return;
 		}
 		File cdir = new File(cdirPath);
 		if (!cdir.exists()) {
 			return;
 		}
 		File env = new File(cdir, "env.properties");
		if (!cdir.exists()) {
 			return;
 		}
 		try {
 			EnvProperties envp = new EnvProperties();
 			envp.load(new FileInputStream(env));
 			for (Object key : envp.keySet()) {
 				System.getProperties().put(key, envp.get(key));
 			}
 		} catch (Exception e) {
 			this.log.warn("loading env.properties error:", e);
 		}
 	}
 
 	/**
 	 * read MD5 code in .md5 file under folder.
 	 * 
 	 * @param dir
 	 *            target folder.
 	 * @return it will return empty string if not found.
 	 */
 	public static String readMd5(File dir) {
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new InputStreamReader(
 					new FileInputStream(new File(dir, ".md5"))));
 			return reader.readLine();
 		} catch (Exception e) {
 			return "";
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * write MD5 code to .md5 file in folder.
 	 * 
 	 * @param dir
 	 *            target folder.
 	 * @param md5
 	 *            MD5 code.
 	 */
 	public static void writeMd5(File dir, String md5) {
 		BufferedWriter writer = null;
 		try {
 			writer = new BufferedWriter(new OutputStreamWriter(
 					new FileOutputStream(new File(dir, ".md5"))));
 			writer.write(md5);
 		} catch (Exception e) {
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * unzip zip file to folder.
 	 * 
 	 * @param zip
 	 *            the target zip file.
 	 * @param dir
 	 *            the target folder.
 	 * @throws IOException
 	 *             unzip error.
 	 */
 	public static void unzip(File zip, File dir) throws IOException {
 		ZipFile zipFile = null;
 		BufferedInputStream bis = null;
 		BufferedOutputStream bos = null;
 		try {
 			zipFile = new ZipFile(zip);
 			Enumeration<?> emu = zipFile.entries();
 			File tf;
 			int BUFFER = 2048;
 			byte data[] = new byte[BUFFER];
 			while (emu.hasMoreElements()) {
 				ZipEntry entry = (ZipEntry) emu.nextElement();
 				if (entry.isDirectory()) {
 					tf = new File(dir, entry.getName());
 					if (!tf.exists()) {
 						tf.mkdirs();
 					}
 					continue;
 				}
 				bis = new BufferedInputStream(zipFile.getInputStream(entry));
 				tf = new File(dir, entry.getName());
 				File parent = tf.getParentFile();
 				if (parent != null && (!parent.exists())) {
 					parent.mkdirs();
 				}
 				bos = new BufferedOutputStream(new FileOutputStream(tf), BUFFER);
 				int count;
 				while ((count = bis.read(data, 0, BUFFER)) != -1) {
 					bos.write(data, 0, count);
 				}
 				bos.flush();
 				bos.close();
 				bis.close();
 				bis = null;
 				bos = null;
 			}
 			zipFile.close();
 			zipFile = null;
 		} catch (IOException e) {
 			if (zipFile != null) {
 				try {
 					zipFile.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 			if (bis != null) {
 				try {
 					bis.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 			if (bos != null) {
 				try {
 					bos.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 			throw e;
 		}
 	}
 
 	/**
 	 * check MD5 code to file.
 	 * 
 	 * @param file
 	 *            the target file.
 	 * @return MD5 string.
 	 */
 	public static String checkMd5(File file) {
 		FileInputStream in = null;
 		try {
 			in = new FileInputStream(file);
 			MessageDigest digester = MessageDigest.getInstance("MD5");
 			byte[] bytes = new byte[8192];
 			int byteCount;
 			while ((byteCount = in.read(bytes)) > 0) {
 				digester.update(bytes, 0, byteCount);
 			}
 			byte[] digest = digester.digest();
 			return new String(digest);
 		} catch (Exception e) {
 			return "";
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * check deploy in deploy folder.<br/>
 	 * it will auto deploy all WebApp package(.j4a).
 	 */
 	public void checkDeploy() {
 		this.log.info("check deploy folder for update...");
 		File[] dypkg;
 		dypkg = this.dydir.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File pathname) {
 				if (!pathname.isFile()) {
 					return false;
 				}
 				return pathname.getName().toLowerCase(Locale.getDefault())
 						.matches(".*\\.j4a");
 			}
 		});
 		if (dypkg == null) {
 			return;
 		}
 		final Set<String> names = new HashSet<String>();
 		for (File zip : dypkg) {
 			String name = zip.getName();
 			name = name.substring(0, name.length() - 4);
 			names.add(name);
 			File df = new File(this.wsdir, name);
 			try {
 				boolean deploy = false;
 				String fmd5 = checkMd5(zip);
 				if (df.exists()) {
 					String dmd5 = readMd5(df);
 					dmd5 = "";
 					if (dmd5.equals(fmd5)) {
 						deploy = false;
 					} else {
 						FileUtils.deleteDirectory(df);
 						deploy = true;
 					}
 				} else {
 					deploy = true;
 				}
 				if (!deploy) {
 					continue;
 				}
 				df.mkdirs();
 				unzip(zip, df);
 				this.log.info("deploy", zip.getAbsolutePath(), "to",
 						df.getAbsoluteFile());
 				writeMd5(df, fmd5);
 			} catch (Exception e) {
 				this.log.warn(
 						"deploy " + zip.getAbsolutePath() + " to "
 								+ df.getAbsolutePath() + "error", e);
 			}
 		}
 		File[] oldpkg = this.wsdir.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File pathname) {
 				if (pathname.isFile()) {
 					return false;
 				} else {
 					return !names.contains(pathname.getName());
 				}
 			}
 		});
 		if (oldpkg == null) {
 			return;
 		}
 		for (File opkg : oldpkg) {
 			try {
 				FileUtils.deleteDirectory(opkg);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * load all WebApp in workspace.
 	 */
 	public void loadWebContext() {
 		this.log.info("load all app in webapp folder...");
 		File[] webdir = this.wsdir.listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return new File(pathname, "web.properties").exists()
 						|| new File(pathname, "web.xml").exists();
 			}
 		});
 		if (webdir == null) {
 			return;
 		}
 		this.log.info("found " + webdir.length + " WebApp in "
 				+ this.wsdir.getAbsolutePath());
 		for (File wapp : webdir) {
 			try {
 				this.loadWebApp(wapp, wapp);
 				this.log.info("load " + wapp.getAbsolutePath() + " success");
 			} catch (Exception e) {
 				this.log.warn("load " + wapp.getAbsolutePath() + " error", e);
 			}
 		}
 	}
 
 	protected ClassLoader buildClassLoader(File root, ClassLoader tcl) {
 		File tf = new File(root, "lib");
 		try {
 			URLClassLoader ucl = null;
 			ucl = new URLClassLoader(new URL[] {
 					new URL("file://" + tf.getAbsolutePath()),
 					new URL("file://"
 							+ new File(root, "classes").getAbsolutePath()) },
 					tcl);
 			return ucl;
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return tcl;
 		}
 	}
 
 	/**
 	 * load a WebApp to context.
 	 * 
 	 * @param root
 	 *            the root path for WebApp physical path.
 	 * @param croot
 	 *            the configure root path for WebApp physical path.
 	 */
 	public void loadWebApp(File root, File croot) {
 		File rdir = new File(System.getProperty(ServerListener.J4A_RDIR));
 		File droot = new File(rdir, root.getName());
 		if (!droot.exists()) {
 			droot.mkdirs();
 		}
 		this.loadWebApp(root, croot, droot);
 	}
 
 	/**
 	 * load a WebApp to context.
 	 * 
 	 * @param root
 	 *            the root path for WebApp physical path.
 	 * @param croot
 	 *            the configure root path for WebApp physical path.
 	 * @param droot
 	 *            the runtime data root path for WebApp physical path.
 	 */
 	public void loadWebApp(File root, File croot, File droot) {
 		ContextHandlerCollection chcs = new ContextHandlerCollection();
 		File tf;
 		// builder class loader.
 		ClassLoader tcl = this.contexts.getClass().getClassLoader();
 		tcl = this.buildClassLoader(root, tcl);
 		//
 		tf = new File(croot, "web.properties");
 		EnvProperties webp = new EnvProperties(System.getProperties());
 		if (tf.exists()) {
 			try {
 				webp.load(new FileInputStream(tf));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		ServerListener sl = new ServerListener();
 		if (webp.containsKey("ServerListener")) {
 			try {
 				sl = (ServerListener) tcl.loadClass(
 						(String) webp.get("ServerListener")).newInstance();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		sl.init(root, croot, droot, webp);
 		try {
 			Handler h = sl.create(tcl, webp);
 			if (h != null) {
 				chcs.addHandler(h);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		String name = root.getName();
 		if (webp.containsKey("WebName")) {
 			name = webp.getProperty("WebName");
 		}
 		tf = new File(croot, "web.xml");
 		if (tf.exists()) {
 			WebAppContext wapp = new WebAppContext();
 			wapp.setDescriptor(tf.getAbsolutePath());
 			wapp.setParentLoaderPriority(true);
 			wapp.setClassLoader(tcl);
 			if (webp.contains("WebResourceBase")) {
 				wapp.setResourceBase(new File(root, webp
 						.getProperty("WebResourceBase")).getAbsolutePath());
 			} else {
 				File wc = new File(root, "WebContent");
 				if (!wc.exists()) {
 					wc.mkdirs();
 				}
 				wapp.setResourceBase(wc.getAbsolutePath());
 			}
 			if (webp.containsKey("WebContextPath")) {
 				wapp.setContextPath(webp.getProperty("WebContextPath"));
 			} else {
 				wapp.setContextPath("/" + name);
 			}
 			sl.initWebApp(wapp, webp);
 			chcs.addHandler(wapp);
 		}
 		this.listeners.add(sl);
 		this.servers.put(name, chcs);
 		this.contexts.addHandler(chcs);
 	}
 
 	/**
 	 * @return the wsdir
 	 */
 	public File getWsdir() {
 		return wsdir;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jetty.server.Server#doStart()
 	 */
 	@Override
 	protected void doStart() throws Exception {
 		this.log.info("staring server...");
 		this.checkDeploy();
 		this.loadWebContext();
 		super.doStart();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jetty.server.Server#doStop()
 	 */
 	@Override
 	protected void doStop() throws Exception {
 		this.log.info("stopping server...");
 		for (ServerListener sl : this.listeners) {
 			sl.destroy();
 		}
 		this.listeners.clear();
 		super.doStop();
 	}
 
 }
