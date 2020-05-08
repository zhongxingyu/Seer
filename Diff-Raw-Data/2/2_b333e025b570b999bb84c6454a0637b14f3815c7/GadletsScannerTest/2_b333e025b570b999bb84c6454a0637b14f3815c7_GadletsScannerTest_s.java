 package org.gadlets.scanner;
 
 import org.gadlets.tag.GadletsGenerator;
 import org.jboss.shrinkwrap.api.ArchivePath;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.testng.annotations.Test;
 
 import java.net.URL;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: karl
  * Date: 6/17/11
  * Time: 9:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GadletsScannerTest {
 
     @Test
     public void detectResource() throws Exception {
 
         JavaArchive scannerJar = ShrinkWrap.create(JavaArchive.class, "scanner.jar")
                 .addClass(GadletScanner.class)
                 .addAsResource(EmptyAsset.INSTANCE, "beans.xml");
         System.out.println(scannerJar.toString(true));
 
         JavaArchive gadletsJar = ShrinkWrap.create(JavaArchive.class, "gadlets.jar")
                 .addClass(GadletsGenerator.class)
                 .addAsManifestResource(EmptyAsset.INSTANCE, "gadlets.xml");
         System.out.println(gadletsJar.toString(true));
         for(ArchivePath ap : gadletsJar.getContent().keySet()){
             System.out.println(ap + ": " + gadletsJar.get(ap) +  gadletsJar.get(ap).getAsset());
         }
 
         WebArchive someWar = ShrinkWrap.create(WebArchive.class, "some.war")
                .setWebXML("web.xml")
                 .addAsLibrary(scannerJar)
                 .addAsLibrary(gadletsJar);
         System.out.println(someWar.toString(true));
 
         ShrinkWrapClassLoader cl = new ShrinkWrapClassLoader(scannerJar, gadletsJar);
 
 
         GadletScanner gadletScanner = new GadletScanner(cl.getParent());
         Set<URL> urls = gadletScanner.getGadletXmlUrls();
 
         for(URL url : urls){
             System.out.println(url);
         }
 
         cl.close();
     }
 
 }
