 package hudson.plugins.pxe.ubuntu;
 
 import hudson.Extension;
 import hudson.Util;
 import hudson.model.Hudson;
 import hudson.plugins.pxe.BootConfiguration;
 import hudson.plugins.pxe.BootConfigurationDescriptor;
 import hudson.plugins.pxe.ISO9660Tree;
 import hudson.util.FormValidation;
 import static hudson.util.FormValidation.error;
 import static hudson.util.FormValidation.ok;
 import org.apache.commons.io.IOUtils;
 import org.jvnet.hudson.tftpd.Data;
 import org.kohsuke.loopy.FileEntry;
 import org.kohsuke.loopy.iso9660.ISO9660FileSystem;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Ubuntu boot configuration.
  *
  * @author Kohsuke Kawaguchi
  */
 public class UbuntuBootConfiguration extends BootConfiguration {
     /**
      * Location of the CD/DVD image file.
      */
     public final File iso;
 
     private volatile String release;
 
     @DataBoundConstructor
     public UbuntuBootConfiguration(File iso) {
         this.iso = iso;
     }
 
     public String getPxeLinuxConfigFragment() throws IOException {
         return String.format("LABEL %1$s\n" +
                 "    MENU LABEL %2$s\n" +
                 "    KERNEL vesamenu.c32\n" +
                 "    APPEND %1$s/menu.txt \n",
                 getId(), getRelease());
     }
 
     /**
      * This returns string like "Ubuntu-Server 8.10 "Intrepid Ibex" - Release i386 (20081028.1)"
      */
     public String getRelease() {
         if(release==null)
             try {
                 release = getReleaseInfo(iso);
             } catch (IOException e) {
                 release = "Broken Ubuntu image at "+iso;
             }
         return release;
     }
 
     protected String getIdSeed() {
         // try to extract a short name from the release information
         Pattern p = Pattern.compile("Ubuntu[^ ]* ([0-9.]+).+?(i386|amd64)?");
         Matcher m = p.matcher(getRelease());
         if(m.find()) {
             if(m.group(2)!=null)    return "ubuntu"+m.group(1)+'.'+m.group(2);
             else                    return "ubuntu"+m.group(1);
         }
         return "ubuntu";
     }
 
     /**
      * Serves menu.txt by replacing variables.
      */
     public Data tftp(String fileName) throws IOException {
         String prefix = getId() + "/";
         if(!fileName.startsWith(prefix))   return null;    // not ours
 
         fileName=fileName.substring(prefix.length());
 
         if(fileName.equals("menu.txt")) {
             // menu
             String template = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("tftp/ubuntu/menu.txt"));
             Map<String,String> props = new HashMap<String, String>();
             props.put("RELEASE",getRelease());
             props.put("ID",getId());
 
             return Data.from(Util.replaceMacro(template,props));
         }
 
         if(fileName.equals("linux") || fileName.equals("initrd.gz")) {
             ISO9660FileSystem fs = new ISO9660FileSystem(iso,false);
             FileEntry installer = fs.get("/install/netboot/ubuntu-installer");
             if(installer==null) throw new IOException("/install/netboot/ubuntu-installer not found on "+iso);
             LinkedHashMap<String,FileEntry> children = installer.childEntries();
             FileEntry arch = children.get("i386");
             if(arch==null)  arch=children.get("amd64");
             if(arch==null)      throw new IOException("/install/netboot/ubuntu-installer/(amd64|i386) not found on "+iso);
 
             final FileEntry data = arch.grab(fileName);
             return new Data() {
                 public InputStream read() throws IOException {
                     return data.read();
                 }
 
                 @Override
                 public int size() throws IOException {
                     return data.getSize();
                 }
             };
         }
 
         return null;
     }
 
     private static String getReleaseInfo(File iso) throws IOException {
         ISO9660FileSystem fs=null;
         try {
             try {
                 fs = new ISO9660FileSystem(iso,false);
             } catch (IOException e) {
                 LOGGER.log(Level.INFO,iso+" isn't an ISO file?",e);
                 throw error(iso+" doesn't look like an ISO file");
             }
 
             FileEntry info = fs.get("/.disk/info");
             if(info==null)
                 throw error(iso+" doesn't look like an Ubuntu CD/DVD image");
 
             FileEntry installer = fs.get("/install/netboot/ubuntu-installer");
             if(installer==null)
                 throw error(iso+" doesn't have the network boot installer in it. Perhaps it's a desktop CD?");
 
             return IOUtils.toString(info.read());
         } finally {
             if(fs!=null)
                 fs.close();
         }
     }
 
     public ISO9660Tree doImage() {
         return new ISO9660Tree(iso);
     }
 
     public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
         rsp.sendRedirect("./image/");
     }
 
     public String getDisplayName() {
         return getRelease();
     }
 
     @Extension
     public static class DescriptorImpl extends BootConfigurationDescriptor {
         public String getDisplayName() {
             return "Ubuntu";
         }
 
         public FormValidation doCheckIso(@QueryParameter String value) throws IOException {
             // insufficient permission to perform validation?
             if(!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return ok();
 
             File f = new File(value);
             if(!f.exists())
                 return error("No such file file exists: "+value);
 
             try {
                 return ok(getReleaseInfo(f));
             } catch (FormValidation e) {
                 return e;
             }
         }
     }
 
     private static final Logger LOGGER = Logger.getLogger(UbuntuBootConfiguration.class.getName());
 }
