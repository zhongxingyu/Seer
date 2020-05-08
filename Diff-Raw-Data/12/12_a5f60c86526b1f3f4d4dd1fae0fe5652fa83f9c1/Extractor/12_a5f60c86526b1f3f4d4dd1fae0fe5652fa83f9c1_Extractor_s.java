 package org.rsbot.util;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarOutputStream;
 
 public class Extractor implements Runnable {
 	private static void saveTo(final InputStream in, final String outPath) {
 		try {
 			final OutputStream out = new FileOutputStream(new File(outPath));
 			final byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 		} catch (final Exception ignored) {
 		}
 	}
 
 	private final String[] args;
 
 	public Extractor(final String[] args) {
 		this.args = args;
 	}
 
 	public void run() {
 		final ClassLoader loader = getClass().getClassLoader();
 		final String root = GlobalConfiguration.Paths.Resources.ROOT + "/";
 		if (GlobalConfiguration.RUNNING_FROM_JAR) {
 			try {
 				if (GlobalConfiguration.getCurrentOperatingSystem() == GlobalConfiguration.OperatingSystem.WINDOWS) {
 					Extractor.saveTo(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_SCRIPTS_BAT), GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_SCRIPTS_BAT);
 					Extractor.saveTo(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_FIND_JDK), GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_FIND_JDK);
 				} else {
 					Extractor.saveTo(loader.getResourceAsStream(root + GlobalConfiguration.Paths.COMPILE_SCRIPTS_SH), GlobalConfiguration.Paths.getHomeDirectory() + File.separator + GlobalConfiguration.Paths.COMPILE_SCRIPTS_SH);
 				}
 				final URL version = GlobalConfiguration.class.getClassLoader().getResource(GlobalConfiguration.Paths.Resources.VERSION);
 				String p = version.toString().replace("jar:file:", "").replace("!/" + GlobalConfiguration.Paths.Resources.VERSION, "");
 				try {
 					p = URLDecoder.decode(p, "UTF-8");
 				} catch (final UnsupportedEncodingException ignored) {
 				}
 				final JarFile jar = new JarFile(new File(p));
 				final File out = new File(GlobalConfiguration.Paths.getScriptsExtractedCache());
 				FileOutputStream fos = null;
 				JarOutputStream jos = null;
 				final Enumeration<JarEntry> entries = jar.entries();
 				while (entries.hasMoreElements()) {
 					final JarEntry e = entries.nextElement();
 					if (e.getName().startsWith("scripts/")) {
 						if (fos == null) {
 							fos = new FileOutputStream(out);
 							jos = new JarOutputStream(fos);
 						}
 						final InputStream in = loader.getResourceAsStream(e.getName());
 						jos.putNextEntry(new JarEntry(e.getName().substring(8)));
 						final byte[] buffer = new byte[256];
 						while (true) {
 							final int nRead = in.read(buffer, 0, buffer.length);
 							if (nRead < 0) {
 								break;
 							}
 							jos.write(buffer, 0, nRead);
 						}
 						in.close();
 					}
 				}
 				if (fos != null) {
 					jos.close();
 					fos.close();
 				}
 			} catch (final Exception e) {
 				e.printStackTrace();
 			}
 		} else if (args.length > 2) {
 			if (args[0].toLowerCase().startsWith("delete")) {
 				final File jarOld = new File(args[1]);
 				if (jarOld.exists()) {
 					if (!jarOld.delete()) {
 						jarOld.deleteOnExit();
 					}
 				}
 				clearDirectory(new File(GlobalConfiguration.Paths.getCacheDirectory()), false);
 			}
 		}
 	}
 
 	public void clearDirectory(final File path, final boolean deleteParent) {
 		if (!path.exists()) {
 			return;
 		}
 		for (final File file : path.listFiles()) {
 			if (file.isDirectory()) {
 				clearDirectory(file, true);
 			} else {
 				file.delete();
 			}
 		}
 		if (deleteParent) {
 			path.delete();
 		}
 	}
 }
