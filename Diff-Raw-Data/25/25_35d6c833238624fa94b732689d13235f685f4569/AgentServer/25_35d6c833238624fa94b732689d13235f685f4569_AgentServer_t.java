 package br.com.caelum.selenium.grid;
 
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 
 public class AgentServer {
 	public static void main(String[] args) throws Exception {
         // i cannot create any direct reference to JettyServer otherwise the
         // java virtual machine would load in my current classloader the JettyServer and
         // jetty classes, causing version errors between the required jetty version for
         // the web app and the jetty version used by selenium
 		
 		JettyServer j = new JettyServer();
 		String server = "/selenium-server-1.0-SNAPSHOT.jar";
 		String rc = "/selenium-grid-remote-control-standalone-1.0.4-SNAPSHOT.jar";
 		j.start(new CommandExecutor(new URLClassLoader(new URL[] {urlFor(server), urlFor(rc)}, null)));
 		
 		
     }
 
	private static URL urlFor(String x) throws IOException {
 //		return new URL("file://" + x);
		URL url = AgentServer.class.getResource(x);
		File jar = File.createTempFile("selenium", ".jar");
		OutputStream stream = new BufferedOutputStream(new FileOutputStream(jar), 1024000);
		InputStream inputStream = new BufferedInputStream(url.openStream());
		while(inputStream.available() > 0) {
			stream.write(inputStream.read());
		}
		stream.close();
		inputStream.close();
		return jar.toURI().toURL();
 	}
 }
