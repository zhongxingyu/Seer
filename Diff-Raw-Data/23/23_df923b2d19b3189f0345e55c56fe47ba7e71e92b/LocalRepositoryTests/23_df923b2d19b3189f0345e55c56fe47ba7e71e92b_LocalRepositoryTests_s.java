 package com.yoursway.autoupdater.tests;
 
 import static com.google.common.collect.Lists.newLinkedList;
 import static com.yoursway.autoupdater.tests.internal.FileTestUtils.fileContents;
 import static com.yoursway.autoupdater.tests.internal.FileTestUtils.sizeOf;
 import static com.yoursway.utils.YsFileUtils.readAsString;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.yoursway.autoupdater.auxiliary.Component;
 import com.yoursway.autoupdater.auxiliary.ComponentStopper;
 import com.yoursway.autoupdater.auxiliary.Product;
 import com.yoursway.autoupdater.auxiliary.ProductVersion;
 import com.yoursway.autoupdater.filelibrary.Request;
 import com.yoursway.autoupdater.filelibrary.RequestUtils;
 import com.yoursway.autoupdater.installer.Installer;
 import com.yoursway.autoupdater.installer.InstallerException;
 import com.yoursway.autoupdater.localrepository.LocalRepository;
 import com.yoursway.autoupdater.tests.internal.server.WebServer;
 
 public class LocalRepositoryTests {
     
     private boolean installed;
     private WebServer server;
     
     @Before
     public void setup() {
         installed = false;
         server = new WebServer();
     }
     
     @Test
     public void integration_simple() throws IOException, InterruptedException {
         final int first = 5;
         final int last = 12;
         
         Collection<Request> requests = RequestUtils.requests(first, last, ".zip");
         RequestUtils.mount(server, requests);
         
        Product product = new Product();
         Collection<Component> packs = newLinkedList();
         ProductVersion version = new ProductVersion(product, requests, packs);
         
         LocalRepository repo = new LocalRepository(new Installer() {
             public void install(ProductVersion current, ProductVersion version, Map<String, File> packs,
                     File target, File extInstallerFolder, ComponentStopper stopper) throws InstallerException {
                 
                 System.out.println("Installation started!");
                 
                 boolean[] a = new boolean[last - first + 1];
                 
                 Iterator<File> it = packs.values().iterator();
                 for (int i = first; i <= last; i++) {
                     File file = it.next();
                     System.out.println(file.getPath() + " - size: " + file.length());
                     
                     String filename = file.getName();
                     int n = Integer.parseInt(filename.substring(3, filename.length() - 4));
                     a[n - first] = true;
                     assertEquals(sizeOf(n), file.length());
                     
                     try {
                         assertEquals(fileContents(sizeOf(n)), readAsString(file));
                     } catch (IOException e) {
                         fail("IOException at file checking");
                     }
                 }
                 
                 for (int i = 0; i <= last - first; i++)
                     assertTrue(a[i]);
                 
                 System.out.println("Installation finished!");
                 installed = true;
                 
                 synchronized (LocalRepositoryTests.this) {
                     LocalRepositoryTests.this.notify();
                 }
             }
         });
         
         synchronized (this) {
             repo.startUpdating(version);
             wait();
         }
         
         assertTrue("The product version has not been installed.", installed);
     }
     
     @After
     public void clean() {
         server.dispose();
     }
     
 }
