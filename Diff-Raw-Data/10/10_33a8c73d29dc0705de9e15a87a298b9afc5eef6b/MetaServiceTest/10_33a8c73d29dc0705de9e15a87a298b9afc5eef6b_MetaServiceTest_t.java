 package org.joni.test.meta;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import junit.framework.TestCase;
 
 import org.glite.security.trustmanager.ContextWrapper;
 import org.joni.test.meta.client.TMHostnameVerifier;
 import org.joni.test.meta.server.MetaServer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.caucho.hessian.client.HessianProxyFactory;
 import com.caucho.hessian.client.TMHessianURLConnectionFactory;
 import com.eaio.uuid.UUID;
 
 public class MetaServiceTest extends TestCase {
 
     public static final String TEST_USER = "CN=trusted client,OU=Relaxation,O=Utopia,L=Tropic,C=UG";
     public static final String TEST_USER2 = "CN=trusted clientserver,OU=Relaxation,O=Utopia,L=Tropic,C=UG";
     public static final String TRUSTED_CLIENT_CONFIG_FILE = "src/test/meta-client-trusted.conf";
     public static final String TRUSTED_CLIENT2_CONFIG_FILE = "src/test/meta-client2-trusted.conf";
     public static final String SERVER_PURGE_CONFIG_FILE = "src/test/meta-purge.conf";
 
     MetaServer server;
 
     @Before
     public void setarrserver() {
         System.out.println("****Start");
         // server = new MetaServer();
         // try {
         // server.run(40666, false);
         // } catch (Exception e) {
         // // TODO Auto-generated catch block
         // e.printStackTrace();
         // }
     }
 
     @After
     public void endserver() {
         System.out.println("****Stop");
         // try {
         // server.stop();
         // } catch (Exception e) {
         // // TODO Auto-generated catch block
         // e.printStackTrace();
         // }
     }
     
     public void setup() throws Exception {
         server = new MetaServer();
         server.configure(SERVER_PURGE_CONFIG_FILE);
         server.start();
         File configFile = new File(TRUSTED_CLIENT_CONFIG_FILE);
         Properties props = new Properties();
         props.load(new FileReader(configFile));
         ContextWrapper wrapper = new ContextWrapper(props, false);
         HttpsURLConnection.setDefaultSSLSocketFactory(wrapper.getSocketFactory());
         HttpsURLConnection.setDefaultHostnameVerifier(new TMHostnameVerifier());         
         
     }
 
     /**
      * @param args
      * @throws Exception
      */
     @Test
     public void testFilePut() throws Exception {
         try {
             server = new MetaServer();
             server.configure(SERVER_PURGE_CONFIG_FILE);
             server.start();
             
             // client
             File configFile = new File(TRUSTED_CLIENT_CONFIG_FILE);
             Properties props = new Properties();
             props.load(new FileReader(configFile));
             ContextWrapper wrapper = new ContextWrapper(props, false);
             
             TMHostnameVerifier verifier = new TMHostnameVerifier();         
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             TMHessianURLConnectionFactory connectionFactory = new TMHessianURLConnectionFactory();
             connectionFactory.setWrapper(wrapper);
             connectionFactory.setVerifier(verifier);
             connectionFactory.setHessianProxyFactory(factory);
             factory.setConnectionFactory(connectionFactory);
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             service.addUser(info);
             
             boolean exception = false;
             try {
                 service.putFile(null);
             } catch (NullPointerException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName("root");
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
 
             MetaFile subdir = new MetaFileImpl();
             subdir.setDirectory(true);
             subdir.setName("subdir");
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             subdir.addACLItem(new ACLItem(TEST_USER2, true, true));
             service.putFile(subdir);
 
             exception = false;
             try {
                 service.putFile(root);
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.putFile(new MetaFileImpl().setName("Test").setParent(new UUID()));
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             List<ACLItem> acl = new ArrayList<ACLItem>();
             acl.add(new ACLItem("peter", true, true));
             MetaFile testRootWithoutAccess = new MetaFileImpl().setName("TestRootNoPermissiontowrite");
             testRootWithoutAccess.setACL(acl);
             service.putFile(testRootWithoutAccess);
             exception = false;
             try {
                 service.putFile(new MetaFileImpl().setName("Test").setParent(testRootWithoutAccess.getId()));
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             // client
             File config2File = new File(TRUSTED_CLIENT2_CONFIG_FILE);
             Properties props2 = new Properties();
             props2.load(new FileReader(config2File));
             ContextWrapper wrapper2 = new ContextWrapper(props2, false);
             
             // reuse previous //TMHostnameVerifier verifier = new TMHostnameVerifier();         
             
             //String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory2 = new HessianProxyFactory();
             TMHessianURLConnectionFactory connectionFactory2 = new TMHessianURLConnectionFactory();
             connectionFactory.setWrapper(wrapper2);
             connectionFactory.setVerifier(verifier);
             connectionFactory.setHessianProxyFactory(factory2);
             factory2.setConnectionFactory(connectionFactory2);
             MetaDataAPI service2 = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
             
             MetaFile subdirFail = new MetaFileImpl();
             subdirFail.setDirectory(true);
             subdirFail.setName("subdirfail");
             subdirFail.addACLItem(new ACLItem(TEST_USER2, true, true));
             exception = false;
             try { // fail adding new root as not superuser
                 service2.putFile(subdirFail);
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             subdirFail.setParent(root.getId());
             exception = false;
             try { // fail adding subdir to root no access superuser
                 service2.putFile(subdirFail);
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             
             subdirFail.setParent(subdir.getId());
             service2.putFile(subdirFail);
             
         } finally {
             if (server != null) {
                 server.stop();
             }
         }
 
     }
 
     @Test
     public void testFileUpdate() throws Exception {
         try {
             setup();
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             service.addUser(info);
 
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName("root");
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
 
 //            UserInfo user = service.getUserInfo();
 //            MetaFile root = service.getFile(user.getRoots().get(0));
             
             MetaFileImpl subdir = new MetaFileImpl();
             subdir.setName("subdir");
             subdir.setDirectory(true);
             subdir.setParent(root.getId());
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subdir);
 
             MetaFileImpl rootfile = new MetaFileImpl();
             rootfile.setName("rootfile");
             rootfile.setParent(root.getId());
             rootfile.addACLItem(new ACLItem(TEST_USER, false, false));
             service.putFile(rootfile);
 
             MetaFileImpl subfile = new MetaFileImpl();
             subfile.setName("subfile");
             subfile.setParent(subdir.getId());
             subfile.addACLItem(new ACLItem(TEST_USER, true, false));
             service.putFile(subfile);
 
             List<MetaFile> files = new ArrayList<MetaFile>();
                 files = service.getListFile(root.getId());
             for (MetaFile file : files) {
                 System.out.println("ls root: " + file);
             }
 
             boolean exception = false;
             try {
                 service.updateFile(null);
             } catch (NullPointerException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.updateFile(subdir.setParent(null));
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.updateFile(new MetaFileImpl());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.updateFile(subfile.setParent(root.getId()));
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.updateFile(subfile.setParent(new UUID()));
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
 //            service.updateFile(root.removeACLItem(0).addACLItem(new ACLItem(TEST_USER, false, false)));
 //            exception = false;
 //            try {
 //                service.updateFile(subdir.setName("subdirv2").setParent(root.getId()));
 //
 //            } catch (IOException e) {
 //                System.out.println(e.getMessage());
 //                exception = true;
 //            }
 //            assertTrue(exception);
 
 //            exception = false;
 //            try {
 //                service.updateFile(root.setName("rootv2"));
 //
 //            } catch (IOException e) {
 //                exception = true;
 //            }
 //            assertTrue(exception);
 
 //            root = new MetaFileImpl();
 //            root.setDirectory(true);
 //            root.setName("root");
 //            root.addACLItem(new ACLItem(TEST_USER, true, true));
 //            service.putFile(root);
 //
 //            subdir = new MetaFileImpl();
 //            subdir.setName("subdir");
 //            subdir.setDirectory(true);
 //            subdir.setParent(root.getId());
 //            subdir.addACLItem(new ACLItem(TEST_USER, true, true));
 //            service.putFile(subdir);
 //
 //            MetaFile subsubdir = new MetaFileImpl();
 //            subsubdir.setName("subsubdir");
 //            subsubdir.setDirectory(true);
 //            subsubdir.setParent(subdir.getId());
 //            subsubdir.addACLItem(new ACLItem(TEST_USER, true, true));
 //            service.putFile(subsubdir);
 //            service.updateFile(subdir.setName("newsub"));
 //
 //            service.deleteFile(subdir.getId());
 //            exception = false;
 //            try {
 //                service.updateFile(subsubdir.setName("newsub"));
 //
 //            } catch (IOException e) {
 //                exception = true;
 //            }
 //            assertTrue(exception);
 
         } finally {
             server.stop();
         }
     }
 
     @Test
     public void testDelete() throws Exception {
         try {
 
             setup();
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             service.addUser(info);
 
             boolean exception = false;
             try {
                 service.deleteFile(null);
 
             } catch (NullPointerException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.deleteFile(new UUID());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName("root");
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
 
             MetaFileImpl subdir = new MetaFileImpl();
             subdir.setName("subdir");
             subdir.setDirectory(true);
             subdir.setParent(root.getId());
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subdir);
 
             MetaFile subsubdir = new MetaFileImpl();
             subsubdir.setName("subsubdir");
             subsubdir.setDirectory(true);
             subsubdir.setParent(subdir.getId());
             subsubdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subsubdir);
 
             // check that nonexistent file delete fails also when some files exists
             exception = false;
             try {
                 service.deleteFile(new UUID());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.deleteFile(root.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             service.deleteFile(subdir.getId());
             exception = false;
             try {
                 service.deleteFile(subsubdir.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             subdir.removeACLItem(0);
             subdir.addACLItem(new ACLItem(TEST_USER, true, false));
             service.putFile(subdir);
             exception = false;
             try {
                 service.deleteFile(subsubdir.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
         } finally {
             server.stop();
         }
     }
 
     //@SuppressWarnings("null")
    @SuppressWarnings("null")
     @Test
     public void testGetByPath() throws Exception {
         try {
 
             setup();
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
             boolean exception = false;
             try {
                 service.getFileByPath((String)null);
             } catch (NullPointerException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.getFileByPath(new String());
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             String drootdir = "root", 
             	    dsubdir = "/root/subdir", 
             	 dsubsubdir = "/root/subdir/subdir";
             
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName(drootdir);
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
             
             MetaFileImpl subdir = new MetaFileImpl();
             subdir.setDirectory(true);
             subdir.setName("subdir");
             subdir.setParent(root.getId());
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subdir);
 
             MetaFile subsubdir = new MetaFileImpl();
             subsubdir.setName("subsubdir");
             subsubdir.setDirectory(true);
             subsubdir.setParent(subdir.getId());
             subsubdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subsubdir);
 
             // Root has to be added before adding to service
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             List<UUID> roots = new ArrayList<UUID>();
             roots.add(root.getId());
             info.setRoots(roots);
             assertTrue(info.getRoots().size()==1);
             service.addUser(info);
             
             // check that nonexistent file delete fails also when some files exists
             exception = false;
             try {
                 service.getFileByPath(new String());
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             String idOrig = dsubdir;
            service.getFileByPath(idOrig);
 
             System.out.println(service.getFileByPath(idOrig).getId().getClass());
             System.out.println(subdir.getId().getClass());
             System.out.println(service.getFileByPath(idOrig).getId().equals(subdir.getId()));
             System.out.println(service.getFileByPath(idOrig).getId());
             System.out.println(subdir.getId());
             System.out.println(service.getFileByPath(idOrig).getId().compareTo(subdir.getId()));
             System.out.println(service.getFileByPath(idOrig).getId().hashCode());
             System.out.println(subdir.getId().hashCode());
 
             assertEquals(service.getFileByPath(idOrig).getId(), subdir.getId());
             assertEquals(service.getFileByPath(idOrig).getName(), subdir.getName());
 
             // Check that "/" returns null
             assertTrue(service.getFileByPath("/") == null);
             
             service.deleteFile(subdir.getId());            
             // check that nonexistent file delete fails also when some files exists
             exception = false;
             try {
                 service.getFileByPath(idOrig);
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             subdir.removeACLItem(0);
             subdir.addACLItem(new ACLItem(TEST_USER, false, true));
             service.putFile(subdir);
             exception = false;
             try {
                 service.getFileByPath(dsubsubdir);
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             MetaFile testsubdir = service.getFileByPath(idOrig);
             assertTrue(testsubdir != null);
             assertTrue(testsubdir.listFiles() == null);
             root.removeACLItem(0);
             root.addACLItem(new ACLItem(TEST_USER, false, true));
             service.updateFile(root);
             exception = false;
             try {
                 service.getFileByPath(drootdir);
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
         } finally {
             server.stop();
         }
     }
     
     @SuppressWarnings("null")
     @Test
     public void testGet() throws Exception {
         try {
 
             setup();
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             service.addUser(info);
 
             boolean exception = false;
             try {
                 service.getFile((UUID)null);
 
             } catch (NullPointerException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.getFile(new UUID());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName("root");
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
 
             MetaFileImpl subdir = new MetaFileImpl();
             subdir.setName("subdir");
             subdir.setDirectory(true);
             subdir.setParent(root.getId());
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subdir);
 
             MetaFile subsubdir = new MetaFileImpl();
             subsubdir.setName("subsubdir");
             subsubdir.setDirectory(true);
             subsubdir.setParent(subdir.getId());
             subsubdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subsubdir);
 
             // check that nonexistent file delete fails also when some files exists
             exception = false;
             try {
                 service.getFile(new UUID());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             UUID idOrig = subdir.getId();
             UUID id = service.getFile(subdir.getId()).getId();
             System.out.println(id.equals(idOrig));
             System.out.println(idOrig.equals(id));
             System.out.println(id.equals(id));
             System.out.println(idOrig.equals(idOrig));
 
             System.out.println(service.getFile(subdir.getId()).getId().getClass());
             System.out.println(subdir.getId().getClass());
             System.out.println(service.getFile(subdir.getId()).getId().equals(subdir.getId()));
             System.out.println(service.getFile(subdir.getId()).getId());
             System.out.println(subdir.getId());
             // System.out.println(service.getFile(subdir.getId()).getId().getLeastSignificantBits());
             // System.out.println(subdir.getId().getLeastSignificantBits());
             // System.out.println(service.getFile(subdir.getId()).getId().getMostSignificantBits());
             // System.out.println(subdir.getId().getMostSignificantBits());
             System.out.println(service.getFile(subdir.getId()).getId().compareTo(subdir.getId()));
             System.out.println(service.getFile(subdir.getId()).getId().hashCode());
             System.out.println(subdir.getId().hashCode());
 
             assertEquals(service.getFile(subdir.getId()).getId(), subdir.getId());
             assertEquals(service.getFile(subdir.getId()).getName(), subdir.getName());
 
             service.deleteFile(subdir.getId());
             // check that nonexistent file delete fails also when some files exists
             exception = false;
             try {
                 service.getFile(subsubdir.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             subdir.removeACLItem(0);
             subdir.addACLItem(new ACLItem(TEST_USER, false, true));
             service.putFile(subdir);
             exception = false;
             try {
                 service.getFile(subsubdir.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             MetaFile testsubdir = service.getFile(subdir.getId());
             assertTrue(testsubdir != null);
             assertTrue(testsubdir.listFiles() == null);
             root.removeACLItem(0);
             root.addACLItem(new ACLItem(TEST_USER, false, true));
             service.updateFile(root);
             exception = false;
             try {
                 service.getFile(root.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
         } finally {
             server.stop();
         }
     }
 
     @SuppressWarnings("null")
     @Test
     public void testGetListFile() throws Exception {
         try {
 
             setup();
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             service.addUser(info);
 
             boolean exception = false;
             try {
                 service.getListFile(null);
 
             } catch (NullPointerException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.getListFile(new UUID());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName("root");
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
 
             MetaFileImpl subdir = new MetaFileImpl();
             subdir.setName("subdir");
             subdir.setDirectory(true);
             subdir.setParent(root.getId());
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subdir);
 
             MetaFile subsubdir = new MetaFileImpl();
             subsubdir.setName("subsubdir");
             subsubdir.setDirectory(true);
             subsubdir.setParent(subdir.getId());
             subsubdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subsubdir);
 
             MetaFileImpl rootfile = new MetaFileImpl();
             rootfile.setName("rootfile");
             rootfile.setParent(root.getId());
             rootfile.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(rootfile);
 
             // check that nonexistent file delete fails also when some files exists
             exception = false;
             try {
                 service.getListFile(new UUID());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             assertEquals(service.getListFile(subdir.getId()).get(0).getId(), subdir.getId());
             assertEquals(service.getListFile(subdir.getId()).get(0).getName(), subdir.getName());
             assertEquals(service.getListFile(subdir.getId()).get(1).getId(), subsubdir.getId());
             assertEquals(service.getListFile(subdir.getId()).get(1).getName(), subsubdir.getName());
 
             service.deleteFile(subdir.getId());
             // check that nonexistent file delete fails also when some files exists
             exception = false;
             try {
                 service.getListFile(subsubdir.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             subdir.removeACLItem(0);
             subdir.addACLItem(new ACLItem(TEST_USER, false, true));
             service.putFile(subdir);
             exception = false;
             try {
                 service.getListFile(subsubdir.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
 // superuser has access, need to test with another user...            
 //            assertTrue(exception);
             List<MetaFile> testsubdirList = service.getListFile(subdir.getId());
             assertTrue(testsubdirList != null);
             assertTrue(testsubdirList.get(0).listFiles() == null);
 
             List<MetaFile> testrootfileList = service.getListFile(rootfile.getId());
             assertEquals(testrootfileList.size(), 1);
             assertEquals(testrootfileList.get(0).getId(), rootfile.getId());
             assertEquals(testrootfileList.get(0).getName(), rootfile.getName());
 
             root.removeACLItem(0);
             root.addACLItem(new ACLItem(TEST_USER, false, true));
             service.updateFile(root);
             exception = false;
             try {
                 service.getListFile(root.getId());
 
             } catch (IOException e) {
                 exception = true;
             }
 // superuser has access, need to test with another user...            
 //            assertTrue(exception);
         } finally {
             server.stop();
         }
     }
 
     @Test
     public void notestStress() throws Exception {
         try {
 
             setup();
             
             // client
             File configFile = new File(TRUSTED_CLIENT_CONFIG_FILE);
             Properties props = new Properties();
             props.load(new FileReader(configFile));
             ContextWrapper wrapper = new ContextWrapper(props, false);
             
             TMHostnameVerifier verifier = new TMHostnameVerifier();         
             
             String url = "https://sicx1.hip.helsinki.fi:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             TMHessianURLConnectionFactory connectionFactory = new TMHessianURLConnectionFactory();
             connectionFactory.setWrapper(wrapper);
             connectionFactory.setVerifier(verifier);
             connectionFactory.setHessianProxyFactory(factory);
             factory.setConnectionFactory(connectionFactory);
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
 
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             service.addUser(info);
 
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName("root");
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
 
             MetaFileImpl subdir = new MetaFileImpl();
             subdir.setName("subdir");
             subdir.setDirectory(true);
             subdir.setParent(root.getId());
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(subdir);
 
             MetaFileImpl rootfile = new MetaFileImpl();
             rootfile.setName("rootfile");
             rootfile.setParent(root.getId());
             rootfile.addACLItem(new ACLItem(TEST_USER, false, false));
             service.putFile(rootfile);
 
             MetaFileImpl subfile = new MetaFileImpl();
             subfile.setName("rootfile");
             subfile.setParent(subdir.getId());
             subfile.addACLItem(new ACLItem(TEST_USER, true, false));
             service.putFile(subfile);
 
             List<MetaFile> files = new ArrayList<MetaFile>();
             Date start = new Date();
             int i;
             for (i = 0; i < 1; i++) {
                 files = service.getListFile(root.getId());
             }
             Date stop = new Date();
             for (MetaFile file : files) {
                 System.out.println("ls stress root: " + file);
             }
             System.out.println("time for " + i + " rounds of ls : " + (stop.getTime() - start.getTime()));
 
         } finally {
             //server.stop();
         }
     }
 
 //    @Test
     public void testFile() throws Exception {
         try {
 
             setup();
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             service.addUser(info);
 
             MetaFile root = new MetaFileImpl();
             root.setDirectory(true);
             root.setName("root");
             root.addACLItem(new ACLItem(TEST_USER, true, true));
             service.putFile(root);
             MetaFile root2 = service.getFile(root.getId());
             System.out.println("Got: " + root2);
             // ls root
             List<MetaFile> files = service.getListFile(root.getId());
             System.out.println("ls of root: ");
             for (MetaFile file : files) {
                 System.out.println("ls root: " + file);
             }
             MetaFileImpl subdir = new MetaFileImpl();
             subdir.setName("subdir");
             subdir.setDirectory(true);
             subdir.setParent(root.getId());
             subdir.addACLItem(new ACLItem(TEST_USER, true, true));
             System.out.println("Saving: " + subdir);
             service.putFile(subdir);
             System.out.println("updating: " + root);
             // ls root
             System.out.println("ls of root: ");
             files = service.getListFile(root.getId());
             for (MetaFile file : files) {
                 System.out.println("ls root: " + file);
             }
             MetaFileImpl rootfile = new MetaFileImpl();
             rootfile.setName("rootfile");
             rootfile.setParent(root.getId());
             rootfile.addACLItem(new ACLItem(TEST_USER, false, false));
             service.putFile(rootfile);
 
             MetaFileImpl subfile = new MetaFileImpl();
             subfile.setName("rootfile");
             subfile.setParent(subdir.getId());
             subfile.addACLItem(new ACLItem(TEST_USER, true, false));
             service.putFile(subfile);
             files = service.getListFile(root.getId());
             for (MetaFile file : files) {
                 System.out.println("ls root: " + file);
             }
             files = service.getListFile(subdir.getId());
             for (MetaFile file : files) {
                 System.out.println("ls subdir: " + file);
             }
         } finally {
             server.stop();
         }
     }
 
     @SuppressWarnings("null")
     @Test
     public void testGetUserArg() throws Exception {
         try {
 
             setup();
             
             String url = "https://localhost:40666/MetaService";
             HessianProxyFactory factory = new HessianProxyFactory();
             MetaDataAPI service = (MetaDataAPI) factory.create(MetaDataAPI.class, url);
 
 //            UserInfo info = new UserInfo();
 //            info.setName(TEST_USER);
 //            service.addUser(info);
 
             boolean exception = false;
             try {
                 service.getListFile(null);
 
             } catch (NullPointerException e) {
                 exception = true;
             }
             assertTrue(exception);
 
             exception = false;
             try {
                 service.getListFile(new UUID());
 
             } catch (IOException e) {
                 exception = true;
             }
             assertTrue(exception);
             
             UserInfo info = new UserInfo();
             info.setName(TEST_USER);
             
             String rootName = "testingRoot";
             MetaFile root = null;
             if(rootName != null){
                 root = new MetaFileImpl();
                 root.setDirectory(true);
                 root.setName(rootName);
                 root.addACLItem(new ACLItem(TEST_USER, true, true));
                 List<UUID> roots = new ArrayList();
                 roots.add(root.getId());
                 info.setRoots(roots);
             }
             
             service.addUser(info);
             
             UserInfo testUser = service.getOtherUserInfo(TEST_USER2);
             
             assertNull(testUser);
             
             UserInfo info2 = new UserInfo();
             info2.setName(TEST_USER2);
             
             String rootName2 = "testingRoot2";
             MetaFile root2 = null;
             if(rootName2 != null){
                 root2 = new MetaFileImpl();
                 root2.setDirectory(true);
                 root2.setName(rootName2);
                 root2.addACLItem(new ACLItem(TEST_USER, true, true));
                 List<UUID> roots = new ArrayList();
                 roots.add(root2.getId());
                 info2.setRoots(roots);
             }
             
             service.addUser(info2);
             
             assertNotNull(service.getOtherUserInfo(TEST_USER2));
             
             @SuppressWarnings("unused")
             UserInfo user3 = service.getUserInfo();
 
         } finally {
             server.stop();
         }
     }
 
 }
