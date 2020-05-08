 package hudson.plugins.pxe;
 
 import hudson.DescriptorExtensionList;
 import static hudson.Functions.defaulted;
 import hudson.model.Describable;
 import hudson.model.Hudson;
 import hudson.model.AbstractModelObject;
 import org.jvnet.hudson.tftpd.Data;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Configuration of a bootable operating system.
  *
  * <p>
  * Say "Ubuntu 2008.10 + such and such preseed config".
  *
  * <p>
  * These objects are bound to URL as <tt>"/pxe/configuration/{@linkplain #getId() ID}/"</tt>
  *
  * @author Kohsuke Kawaguchi
  */
 public abstract class BootConfiguration extends AbstractModelObject implements Describable<BootConfiguration> {
     /**
      * Computed by {@link PXE#doConfigSubmit(StaplerRequest, StaplerResponse)}.
      */
     /*package almost final*/ String id;
 
     /**
      * Returns a unique ID that distinguishes {@link BootConfiguration}s among other siblings.
      *
      * For serving dynamic data from TFTP, it's often useful to have an unique ID per {@link BootConfiguration}.
      * This method provides that.
      */
     public String getId() {
         return id;
     }
 
     /**
      * Returns the string that becomes the seed of the {@link #getId()}. The ID
      * is in turn used for HTTP URLs and TFTP file names, so this method should
      * return something stable and not entirely illegible. But avoid unsafe characters like spaces, '/', etc.
      * A good example would be "OpenSolaris2008.11" or "Ubuntu8.10".
      *
      * <p>
      * Several {@link BootConfiguration}s can return the same ID seed, and {@link #getId()} handles
      * those situations correctly. 
      */
     protected abstract String getIdSeed();
 
     public final String getSearchUrl() {
         return "configuration/"+getId();
     }
 
     public final String getUrl() {
         return getSearchUrl();
     }
 
     /**
      * Gets the absolute URL to this {@link BootConfiguration}.
      *
      * @return
      *      String like "http://foobar/path/to/hudson/pxe/configuration/me" without trailing '/'.
      */
     public final String getAbsoluteUrl() {
         return defaulted(PXE.ROOT_URL,Hudson.getInstance().getRootUrl())+"pxe/"+getUrl();
     }
 
     public BootConfigurationDescriptor getDescriptor() {
         return (BootConfigurationDescriptor) Hudson.getInstance().getDescriptor(getClass());
     }
 
     /**
      * Convenience method to load a resource from the same place as Jelly views are loaded.
      */
     protected InputStream getResourceAsStream(String fileName) {
         for(Class c=getClass(); c!=null; c=c.getSuperclass()) {
             InputStream s = c.getClassLoader().getResourceAsStream(c.getName().replace('.', '/') + '/' + fileName);
             if(s!=null) return s;
         }
         return null; // not found
     }
 
     /**
      * Serves data from TFTP.
      *
      * <p>
      * This mechanism is useful when you need to generate the data to be served on the fly.
      * Static resources can be more easily served by simply placing them as resources
      * under /tftp.
      *
      * <p>
      * The TFTP file namespace is divided for each {@link BootConfiguration}s as
      * <tt>/{@linkplain #getId() ID}/...</tt> to avoid collisions between different configurations.
      *
      * @param fileName
      *      Relative path within the space designated for this boot configuration. For example,
      *      if the client requests "foo/bar/zot" and the ID of this configuration is "foo", this
      *      parameter will be "bar/zot".
      * @return
      *      null if no such file exists, as far as this plugin is concerned.
      *      The PXE plugin will continue to search other {@link BootConfiguration}s to
      *      see if anyone understands it.
      * @throws IOException
      *      If a problem occurs. The PXE plugin will abort the search and the download will fail.
      */
     public Data tftp(String fileName) throws IOException {
         return null;
     }
 
     /**
      * Called before the {@link BootConfiguration} instances are let go because of the reconfiguration.
      * This is a good place to clean up background threads, etc.
      */
     protected void shutdown() throws IOException {
     }
 
     /**
      * Returns the fragment to be merged into <tt>pxelinux.cfg/default</tt>.
      */
     public abstract String getPxeLinuxConfigFragment() throws IOException;
 
     /**
      * Returns all the registered {@link BootConfigurationDescriptor}s.
      */
     public static DescriptorExtensionList<BootConfiguration, BootConfigurationDescriptor> all() {
        return Hudson.getInstance().<BootConfiguration,BootConfigurationDescriptor>getDescriptorList(BootConfiguration.class);
     }
 }
