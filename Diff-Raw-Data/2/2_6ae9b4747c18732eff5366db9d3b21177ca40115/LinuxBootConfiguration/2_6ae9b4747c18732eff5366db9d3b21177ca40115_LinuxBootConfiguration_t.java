 package hudson.plugins.pxe;
 
 import hudson.Util;
 import hudson.util.VariableResolver;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.jexl.ExpressionFactory;
 import org.apache.commons.jexl.context.HashMapContext;
 import org.jvnet.hudson.tftpd.Data;
 import org.kohsuke.loopy.FileEntry;
 import org.kohsuke.loopy.iso9660.ISO9660FileSystem;
 import org.kohsuke.stapler.StaplerResponse;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Locale;
 import java.util.TimeZone;
 
 public abstract class LinuxBootConfiguration extends IsoBasedBootConfiguration {
     protected LinuxBootConfiguration(File iso) {
         super(iso);
     }
 
     /**
      * Directs the user to the OS specific submenu.
      */
     public String getPxeLinuxConfigFragment() throws IOException {
         return String.format("LABEL %1$s\n" +
                 "    MENU LABEL %2$s\n" +
                 "    KERNEL vesamenu.c32\n" +
                 "    APPEND %1$s/menu.txt \n",
                 getId(), getRelease());
     }
 
     /**
      * Serves menu.txt by replacing variables.
      */
     public Data tftp(String fileName) throws IOException {
         if(fileName.equals("menu.txt")) {
             // pxelinux boot menu
             String template = IOUtils.toString(getResourceAsStream("menu.txt"));
             return Data.from(Util.replaceMacro(template,createResolver()));
         }
 
         // look them up in the ISO file
         ISO9660FileSystem fs = new ISO9660FileSystem(iso,false);
         FileEntry dir = getTftpIsoMountDir(fs);
         if(dir!=null) {
             FileEntry f = dir.get(fileName);
             if(f!=null)
                 return new FileEntryData(f);
         }
 
         return null;
     }
 
     /**
      * Directory inside the ISO file that gets "mounted" to TFTP. Used normally to serve
      * kernel and initrd.
      *
      * @return
      *      null if no directory from ISO is mount to tftp. 
      */
     protected abstract FileEntry getTftpIsoMountDir(ISO9660FileSystem fs) throws IOException;
 
 
     /**
      * Creates a variable resolver that looks up properties on this class.
      */
     protected VariableResolver<String> createResolver() {
         final HashMapContext context = new HashMapContext();
        context.put("it", this);
         // these are commonly used values
         context.put("timeZone", TimeZone.getDefault().getID());
         context.put("locale", Locale.getDefault().toString());
 
         return new VariableResolver<String>() {
             public String resolve(String name) {
                 try {
                     if (context.containsKey(name)) return context.get(name).toString();
                     return String.valueOf(ExpressionFactory.createExpression("it." + name).evaluate(context));
                 } catch (Exception e) {
                     throw new Error(e); // tunneling. this must indicate a programming error
                 }
             }
         };
     }
 
     /**
      * Serves the static text resource after expanding macro variables.
      */
     protected void serveMacroExpandedResource(StaplerResponse rsp, String resourceName) throws IOException {
         rsp.setContentType("text/plain");
         rsp.getWriter().println(
                 Util.replaceMacro(IOUtils.toString(getResourceAsStream(resourceName)), createResolver()));
     }
 
 }
