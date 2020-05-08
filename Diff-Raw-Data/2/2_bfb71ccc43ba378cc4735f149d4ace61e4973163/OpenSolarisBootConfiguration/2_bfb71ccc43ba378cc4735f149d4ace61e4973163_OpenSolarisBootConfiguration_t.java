 package hudson.plugins.pxe;
 
 import hudson.Extension;
 import hudson.Util;
 import hudson.model.Hudson;
 import static hudson.util.FormValidation.error;
 import org.apache.commons.io.IOUtils;
 import org.jvnet.hudson.tftpd.Data;
 import org.kohsuke.loopy.FileEntry;
 import org.kohsuke.loopy.iso9660.ISO9660FileSystem;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.BindException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.logging.Level;
 import static java.util.logging.Level.WARNING;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * OpenSolaris boot.
  *
  * <h2>References</h2>
  * <ul>
  * <li><a href="http://wikis.sun.com/display/OSOLInstall/Automated+Installer+Core+Engine">
  * Auto Installer Code Walkthrough</a>
  *
  * <li><a href="http://src.opensolaris.org/source/xref/caiman/slim_bp_installgrub/usr/src/cmd/auto-install/">
  * Auto Installer source code</a>
  *
  * @author Kohsuke Kawaguchi
  */
 public class OpenSolarisBootConfiguration extends IsoBasedBootConfiguration {
     private transient AIWebServerThread aiServer;
 
     @DataBoundConstructor
     public OpenSolarisBootConfiguration(File iso) throws IOException {
         super(iso);
         startAIServer();
     }
 
     private void startAIServer() {
         try {
             aiServer = new AIWebServerThread();
             aiServer.start();
         } catch (IOException e) {
             LOGGER.log(Level.WARNING, "Failed to restart the AI web server",e);
         }
     }
 
     /**
      * Is this object still being used?
      */
     private boolean isActive() {
         return PXE.get().getConfiguration(getId())==this;
     }
 
     protected String getIdSeed() {
         // try to extract a short name from the release information
         Pattern p = Pattern.compile("snv_[^ ]+");
         Matcher m = p.matcher(getRelease());
         if(m.find())    return m.group(0);
         return "opensolaris";
     }
 
     @Override
     protected void shutdown() throws IOException {
         if(aiServer!=null) // we might have failed to initialize ai server.
             aiServer.close();
     }
 
     /**
      * Chainboot to pxegrub, which in turn knows how to boot Solaris.
      *
      * Solaris boot requires $ISADIR variable that we don't know how to set.
      */
     public String getPxeLinuxConfigFragment() throws IOException {
 //        return String.format("LABEL %1$s\n" +
 //                "    MENU LABEL %2$s\n" +
 //                "    KERNEL pxechain.com\n" +
 //                "    APPEND ::%3$s/boot/grub/pxegrub \n",
 //                getId(), getDisplayName(), getId() );
         String baseUrl = Hudson.getInstance().getRootUrl();
         String host = new URL(baseUrl).getHost();
         baseUrl = baseUrl.replace(host,InetAddress.getByName(host).getHostAddress());
 
         String httpIsoImage = String.format("%1$spxe/configuration/%2$s/image",
                 baseUrl,getId());
         return String.format("LABEL %1$s\n" +
                 "    MENU LABEL %2$s\n" +
                 "    KERNEL mboot.c32\n" +
                 "    APPEND -solaris %1$s/boot/platform/i86pc/kernel/unix -v -m verbose -B install_media=%3$s,install_boot=%3$s/boot,livemode=text,install_service=dummy,install_svc_address=%4$s:%5$s --- %1$s/boot/boot_archive\n",
                 getId(), getDisplayName(), httpIsoImage, host, aiServer.server.getLocalPort());
     }
 
     public Object readResolve() {
         startAIServer();
         return this;
     }
 
     @Override
     public Data tftp(String fileName) throws IOException {
         // TODO: closing this file system voids FileEntryData. Fix it
         ISO9660FileSystem fs = new ISO9660FileSystem(iso, false);
         return new FileEntryData(fs.getRootEntry().grab(fileName));
     }
 
     @Extension
     public static class DescriptorImpl extends IsoBasedBootConfigurationDescriptor {
         public String getDisplayName() {
             return "OpenSolaris";
         }
 
         /**
          * This is like "OpenSolaris 2008.11 svnc_101b_rc2 X86"
          */
         protected String getReleaseInfo(File iso) throws IOException {
             ISO9660FileSystem fs=null;
             try {
                 try {
                     fs = new ISO9660FileSystem(iso,false);
                 } catch (IOException e) {
                     LOGGER.log(Level.INFO,iso+" isn't an ISO file?",e);
                     throw error(iso+" doesn't look like an ISO file");
                 }
 
                 if(fs.get("/solaris.zlib")==null || fs.get("/jack")==null)
                     throw error(iso+" doesn't look like an OpenSolaris CD image");
 
                 FileEntry menu = fs.get("/boot/grub/menu.lst");
                 if(menu==null)
                     throw error(iso+" doesn't look like an OpenSolaris CD image (no GRUB)");
 
                 String menuList = IOUtils.toString(menu.read());
                 Matcher m = RELEASE.matcher(menuList);
                 if(m.find())
                     return m.group(1);
 
                 throw error(iso+" doesn't contain OpenSolaris grub menu");
             } finally {
                 if(fs!=null)
                     fs.close();
             }
         }
     }
 
     /**
      * Starts a micro web server that handles AI web requests. This has to be on a separate TCP port
      * because the client only knows how to access the /manifests.xml
      */
     private class AIWebServerThread extends Thread {
         private final ServerSocket server;
         private boolean closed;
         public AIWebServerThread() throws IOException {
             super("OpenSolaris AI webserver for " + iso);
             setDaemon(true);
             server = openSocket();
             LOGGER.info("OpenSolaris AI server for "+iso+" started on port "+server.getLocalPort());
         }
 
         public void close() throws IOException {
             LOGGER.info("Shutting down "+getName());
             closed=true;
             server.close();
         }
 
         private ServerSocket openSocket() throws IOException {
             // the port could be anything, but it's often easier if the port doesn't change too much,
             // so try to stick to the one that we can programmatically infer
            int preferred = Math.abs(OpenSolarisBootConfiguration.this.getIdSeed().hashCode()) % 40000 + 10000;
             try {
                 return new ServerSocket(preferred);
             } catch (BindException e) {
                 // OK, that one wasn't available. pick available one
                 return new ServerSocket(0);
             }
         }
 
         @Override
         public void run() {
             while(isActive()) {
                 final Socket s;
                 try {
                     s = server.accept();
                 } catch (IOException e) {
                     if(!closed)
                         LOGGER.log(WARNING, "Failed to accept",e);
                     return; // exit
                 }
 
                 // handle the request in a separate thread 
                 new Thread() {
                     @Override
                     public void run() {
                         try {
                             // the goal here is to avoid infinite blocking, so set a long time out.
                             s.setSoTimeout(10*60*1000);
 
                             BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                             PrintStream out = new PrintStream(s.getOutputStream());
                             String request = r.readLine();
                             if(request.startsWith("GET /manifest.xml")) {
                                 out.println("HTTP/1.0 200 OK");
                                 out.println("Content-Type: text/xml");
                                 out.println("");
                                 out.println("<CriteriaList>");
                                 out.println("  <Version Number=\"0.5\"/>");
                                 out.println("</CriteriaList>");
                             } else
                             if(request.startsWith("POST /manifest.xml")) {
                                 out.println("HTTP/1.0 200 OK");
                                 out.println("Content-Type: text/xml");
                                 out.println("");
 
                                 String template = IOUtils.toString(getResourceAsStream("ai.xml"));
                                 Map<String,String> props = new HashMap<String, String>();
                                 props.put("userName","jack");
                                 props.put("rootPassword",Crypt.cryptMD5("abcdefgh","opensolaris"));
                                 props.put("timeZone", TimeZone.getDefault().getID());
                                 out.println(Util.replaceMacro(template,props));
                             } else {
                                 out.println("HTTP/1.0 404 Not Found");
                                 out.println("Content-Type: text/html");
                                 out.println("");
                                 out.println("<html><body>This server only knows how to handle /manifest.xml</body></html>");
                             }
                             // close the write side
                             out.flush();
                             s.shutdownOutput();
 
                             IOUtils.toString(r); // drain the input
                             s.shutdownInput();
                         } catch(IOException e) {
                             LOGGER.log(WARNING, "Failed to serve a request from AI web server",e);
                         } finally {
                             try {
                                 s.close();
                             } catch (IOException e) {
                                 LOGGER.log(WARNING, "Failed to close a socket in AI web server",e);
                             }
                         }
                     }
                 }.start();
             }
             LOGGER.fine(" AI web server thread exiting");
         }
     }
 
     private static final Pattern RELEASE = Pattern.compile("title (.+)\n");
 
     private static final Logger LOGGER = Logger.getLogger(OpenSolarisBootConfiguration.class.getName());
 }
