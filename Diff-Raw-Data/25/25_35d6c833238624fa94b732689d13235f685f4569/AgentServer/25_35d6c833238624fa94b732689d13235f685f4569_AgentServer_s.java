 package br.com.caelum.selenium.grid;
 
import java.net.MalformedURLException;
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
 
	private static URL urlFor(String x) throws MalformedURLException {
 //		return new URL("file://" + x);
		return AgentServer.class.getResource(x);
 	}
 }
