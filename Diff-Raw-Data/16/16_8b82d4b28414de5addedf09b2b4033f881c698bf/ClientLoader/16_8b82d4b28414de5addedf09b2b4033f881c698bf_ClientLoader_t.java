 package org.rsbot.loader;
 
 import org.rsbot.Configuration;
 import org.rsbot.loader.asm.ClassReader;
 import org.rsbot.loader.script.ModScript;
 import org.rsbot.loader.script.ParseException;
 import org.rsbot.util.io.HttpClient;
 
 import javax.swing.*;
 import java.io.*;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.util.*;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarOutputStream;
 import java.util.logging.Logger;
 
 /**
  */
 public class ClientLoader {
 
 	private final Logger log = Logger.getLogger(ClientLoader.class.getName());
 
 	private ModScript script;
 	private Map<String, byte[]> classes;
 	private int world = nextWorld();
 
 	public void init(final URL script, final File cache) throws IOException, ParseException {
 		byte[] data = null;
 		FileInputStream fis = null;
 
 		try {
 			HttpClient.download(script, cache);
 			fis = new FileInputStream(cache);
 			data = load(fis);
 		} catch (final IOException ioe) {
 			log.severe("Could not load ModScript data");
 		} finally {
 			try {
 				if (fis != null) {
 					fis.close();
 				}
 			} catch (final IOException ignored) {
 			}
 		}
 
 		this.script = new ModScript(data);
 	}
 
 	public void load(final File cache, final File version_file) throws IOException {
 		classes = new HashMap<String, byte[]>();
 		final int version = script.getVersion();
 		final String target = script.getAttribute("target");
 
 		int cached_version = 0;
 		if (cache.exists() && version_file.exists()) {
 			final BufferedReader reader = new BufferedReader(new FileReader(version_file));
 			cached_version = Integer.parseInt(reader.readLine());
 			reader.close();
 		}
 
 		if (script.getAttribute("minbotversion") != null) {
 			int botVersion = Configuration.getVersion();
			int minVersion = Integer.parseInt(script.getAttribute("minbotversion"));
			if (botVersion < minVersion || true) {
				throw new IOException("Client patch requires newer application version");
 			}
 		}
 
 		if (version <= cached_version) {
 			final JarFile jar = new JarFile(cache);
 
 			checkVersion(jar.getInputStream(jar.getJarEntry("client.class")));
 
 			log.info("Processing client");
 			final Enumeration<JarEntry> entries = jar.entries();
 			while (entries.hasMoreElements()) {
 				final JarEntry entry = entries.nextElement();
 				String name = entry.getName();
 				if (name.endsWith(".class")) {
 					name = name.substring(0, name.length() - 6).replace('/', '.');
 					classes.put(name, script.process(name, jar.getInputStream(entry)));
 				}
 			}
 		} else {
 			log.info("Downloading client: " + target);
 			final JarFile loader = getJar(target, true);
 			final JarFile client = getJar(target, false);
 
 			final List<String> replace = Arrays.asList(script.getAttribute("replace").split(" "));
 
 			Enumeration<JarEntry> entries = client.entries();
 			while (entries.hasMoreElements()) {
 				final JarEntry entry = entries.nextElement();
 				String name = entry.getName();
 				if (name.endsWith(".class")) {
 					name = name.substring(0, name.length() - 6).replace('/', '.');
 					classes.put(name, load(client.getInputStream(entry)));
 				}
 			}
 
 			entries = loader.entries();
 			while (entries.hasMoreElements()) {
 				final JarEntry entry = entries.nextElement();
 				String name = entry.getName();
 				if (name.endsWith(".class")) {
 					name = name.substring(0, name.length() - 6).replace('/', '.');
 					if (replace.contains(name)) {
 						classes.put(name, load(loader.getInputStream(entry)));
 					}
 				}
 			}
 
 			final FileOutputStream stream = new FileOutputStream(cache);
 			final JarOutputStream out = new JarOutputStream(stream);
 
 			for (final Map.Entry<String, byte[]> entry : classes.entrySet()) {
 				out.putNextEntry(new JarEntry(entry.getKey() + ".class"));
 				out.write(entry.getValue());
 			}
 
 			out.close();
 			stream.close();
 
 			int client_version = 0;
 
 			try {
 				client_version = checkVersion(new ByteArrayInputStream(classes.get("client")));
 			} finally {
 				if (client_version != 0) {
 					final FileWriter writer = new FileWriter(Configuration.Paths.getVersionCache());
 					writer.write(Integer.toString(client_version));
 					writer.close();
 				}
 			}
 
 			log.info("Processing client");
 			for (final Map.Entry<String, byte[]> entry : classes.entrySet()) {
 				entry.setValue(script.process(entry.getKey(), entry.getValue()));
 			}
 
 		}
 	}
 
 	public Map<String, byte[]> getClasses() {
 		return classes;
 	}
 
 	public String getTargetName() {
 		return script.getAttribute("target");
 	}
 
 	private int checkVersion(final InputStream in) throws IOException {
 		final ClassReader reader = new ClassReader(in);
 		final VersionVisitor vv = new VersionVisitor();
 		reader.accept(vv, ClassReader.SKIP_FRAMES);
 		if (vv.getVersion() != script.getVersion()) {
 			JOptionPane.showMessageDialog(
 					null,
 					"The bot is currently oudated, please wait patiently for a new version.",
 					"Outdated",
 					JOptionPane.INFORMATION_MESSAGE);
 			throw new IOException("ModScript #" + script.getVersion() + " != #" + vv.getVersion());
 		}
 		return vv.getVersion();
 	}
 
 	private JarFile getJar(final String target, final boolean loader) {
 		while (true) {
 			try {
 				String s = "jar:http://world" + world + "." + target + ".com/";
 				if (loader) {
 					s += "loader.jar!/";
 				} else {
 					s += target + ".jar!/";
 				}
 				final URL url = new URL(s);
 				final JarURLConnection juc = (JarURLConnection) url.openConnection();
 				juc.setConnectTimeout(5000);
 				return juc.getJarFile();
 			} catch (final Exception ignored) {
 				world = nextWorld();
 			}
 		}
 	}
 
 	private int nextWorld() {
 		return 1 + new Random().nextInt(169);
 	}
 
 	private byte[] load(final InputStream is) throws IOException {
 		final ByteArrayOutputStream os = new ByteArrayOutputStream();
 		final byte[] buffer = new byte[4096];
 		int n;
 		while ((n = is.read(buffer)) != -1) {
 			os.write(buffer, 0, n);
 		}
 		return os.toByteArray();
 	}
 }
