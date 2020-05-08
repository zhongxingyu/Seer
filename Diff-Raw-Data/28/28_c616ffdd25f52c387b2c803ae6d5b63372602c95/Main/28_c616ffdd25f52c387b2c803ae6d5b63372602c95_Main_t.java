 package org.codehaus.xsite;
 
 import org.nanocontainer.script.ScriptedContainerBuilder;
 import org.nanocontainer.script.xml.XMLContainerBuilder;
 import org.picocontainer.PicoContainer;
 import org.picocontainer.defaults.ObjectReference;
 import org.picocontainer.defaults.SimpleReference;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 
 /**
 * Command line entry point for building XSite.
  *
  * @author Joe Walnes
  * @author J&ouml;rg Schaible
  */
 public class Main {
 
     // TODO use CLI
     private static String XSITE_FILE = "xsite.xml";
 
     public static void main(String[] args) throws IOException {
         if (args.length != 3) {
             System.err.println("Usage: java " + XSite.class.getName()
                    + " <path-to-sitemap> <path-to-skin> <output-dir>");
             System.exit(-1);
         }
 
        final URL xsiteURL;
         File xsiteFile = new File(XSITE_FILE);
         if (xsiteFile.exists()) {
            xsiteURL = xsiteFile.toURL();
         } else {
            xsiteURL = Main.class.getResource(XSITE_FILE);
         }
 
        if (xsiteURL == null) {
             System.err.println("Could not find " + XSITE_FILE);
             System.exit(-1);
         }
 
        XSite xsite = instantiateXSite(xsiteURL);
        xsite.build(new File(args[0]), new File(args[1]), new File(args[2]));
    }

    private static XSite instantiateXSite(URL url) {
         ScriptedContainerBuilder builder = new XMLContainerBuilder(url, Main.class.getClassLoader());
         ObjectReference reference = new SimpleReference();
         builder.buildContainer(reference, null, null, false);
         PicoContainer pico = (PicoContainer)reference.get();
        return (XSite)pico.getComponentInstanceOfType(XSite.class);        
     }
 
 }
