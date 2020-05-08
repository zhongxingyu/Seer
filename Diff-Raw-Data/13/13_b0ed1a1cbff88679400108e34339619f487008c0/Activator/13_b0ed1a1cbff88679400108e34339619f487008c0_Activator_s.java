 package ai.aw;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.eclipse.equinox.http.jetty.JettyConfigurator;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 public class Activator implements BundleActivator {
 
     private static final String HTTP_PORT_KEY = "http.port";
     private static final int HTTP_PORT = 8080;
     private static final String SERVER_NAME = "demojetty";
 
     @Override
     public void start(BundleContext context) throws Exception {
     	
     	// .exe is singleton ?
         try {  
              ServerSocket serverSocket = new java.net.ServerSocket(50382);  
         } catch (IOException ex) {  
              javax.swing.JOptionPane.showMessageDialog(null, "aw уже запущен.  http://localhost:8080/ ");
              this.stop(context);
              context.getBundle(0).stop();
              System.exit(0);
              return;
         }  
 
         // start jetty
         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put(HTTP_PORT_KEY, HTTP_PORT);
         JettyConfigurator.startServer(SERVER_NAME, properties);
         //System.out.println("Server " + SERVER_NAME + " has been started");
         Thread.sleep(5000);  
 
        
         // launch default browser
         double r = Math.random();
         URI uri = new URI("http://localhost:8080/index.jsp?id="+r);
         Desktop.getDesktop().browse(uri);
         
         // wget sqlite.db
         try {
             String urlString = "http://ubokho1.ibrae/sqlite.db";
             URL url = new URL(urlString);
             URLConnection conn = url.openConnection();
             InputStream inputStream = conn.getInputStream(); 
 
         	File f=new File("sqlite.db.new");
         	OutputStream out=new FileOutputStream(f);
         	byte buf[]=new byte[1024];
         	int len;
         	while((len=inputStream.read(buf))>0)   out.write(buf,0,len);
         	out.flush();
         	out.close();
         	inputStream.close();
         	File fdb=new File("sqlite.db");
         	f.renameTo(fdb);
         }
         catch (IOException e) {
         	javax.swing.JOptionPane.showMessageDialog(null, "Не удалось прочитать  http://ubokho1.ibrae/sqlite.db ");
         }
  
     }
 
     @Override
     public void stop(BundleContext context) throws Exception {
         JettyConfigurator.stopServer(SERVER_NAME);
         //System.out.println("Server " + SERVER_NAME + " has been stoped");
     }
 
 }
