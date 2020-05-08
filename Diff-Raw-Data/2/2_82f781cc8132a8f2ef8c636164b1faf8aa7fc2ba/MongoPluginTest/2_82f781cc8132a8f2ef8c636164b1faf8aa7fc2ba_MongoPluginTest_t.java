 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pt.ua.dicoogle.mongoplugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.apache.commons.configuration.ConfigurationException;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.io.DicomInputStream;
 import org.hamcrest.core.IsInstanceOf;
 import org.hamcrest.core.IsNot;
 import org.hamcrest.core.IsNull;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import pt.ua.dicoogle.sdk.StorageInputStream;
 import pt.ua.dicoogle.sdk.StorageInterface;
 import pt.ua.dicoogle.sdk.datastructs.Report;
 import pt.ua.dicoogle.sdk.datastructs.SearchResult;
 import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;
 import pt.ua.dicoogle.sdk.task.Task;
 
 /**
  *
  * @author Louis
  */
 public class MongoPluginTest {
 
     private static MongoPlugin instance;
 
     public MongoPluginTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws IOException {
         instance = new MongoPlugin();
         ConfigurationHolder settings = null;
         String pathConfigFile = ".\\settings\\mongoplugin.xml";
         try{
             settings = new ConfigurationHolder(new File(pathConfigFile));
         }catch(ConfigurationException e){
             System.out.println("Error while opening the configuration file\n"+e.getMessage());
         }
         instance.setSettings(settings);
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of query method, of class MongoPlugin.
      */
     @Test
     public void testQuery() {
         System.out.println("query");
         String query = "SOPInstanceUID:[1.3.6.1.4.1.9328.50.4.207 TO 1.3.6.1.4.1.9328.50.4.230]";
         Object[] parameters = null;
         Object result = instance.getQueryPlugins().get(0).query(query, parameters);
         Assert.assertThat(result, IsNull.notNullValue());
         Assert.assertThat(result, IsInstanceOf.instanceOf(Iterable.class));
         Iterator<Object> it = ((Iterable) result).iterator();
         int i = 0;
         while (it.hasNext()) {
             Object obj = it.next();
             Assert.assertThat(obj, IsInstanceOf.instanceOf(SearchResult.class));
             i++;
         }
        //Assert.assertThat(i, IsNot.not(0));
     }
     
     public void testAt(URI uri) throws URISyntaxException, IOException {
         System.out.println("at");
         URI location = uri;
         List<StorageInterface> list = (List<StorageInterface>) instance.getStoragePlugins();
         Object result = list.get(0).at(location);
         Assert.assertThat(result, IsNull.notNullValue());
         Assert.assertThat(result, IsInstanceOf.instanceOf(Iterable.class));
         Iterator<Object> it = ((Iterable) result).iterator();
         while (it.hasNext()) {
             Object obj = it.next();
             Assert.assertThat(obj, IsInstanceOf.instanceOf(StorageInputStream.class));
             Assert.assertThat(obj, IsInstanceOf.instanceOf(MongoStorageInputStream.class));
             int res = ((StorageInputStream) obj).getInputStream().read();
             Assert.assertThat(res, IsNot.not(-1));
         }
     }
 
     private static ArrayList<String> retrieveFileList(File file, int level) {
         ArrayList<String> fileList = new ArrayList<String>();
         ArrayList<String> fileList2;
         for (File f : file.listFiles()) {
             if (f.isDirectory()) {
                 fileList2 = retrieveFileList(f, level + 1);
                 for (int i = 0; i < fileList2.size(); i++) {
                     fileList.add(fileList2.get(i));
                 }
             } else {
                 if (f.getAbsolutePath().contains(".dcm")) {
                     fileList.add(f.getAbsolutePath());
                 }
             }
         }
         return fileList;
     }
 
     /**
      * Test of store method, of class MongoPlugin.
      */
     @Test
     public void testStore_DicomObject() throws IOException {
         ArrayList<String> fileList = retrieveFileList(new File("D:\\DICOM_data\\DATA\\"), 0);
         //ArrayList<String> fileList = retrieveFileList(new File("D:\\DICOM_data\\DICOM_Images"), 0);
         //int borne = fileList.size();
         int borne = 1;
         System.out.println("Store "+borne+" files");
         List<StorageInterface> list = (List<StorageInterface>) instance.getStoragePlugins();
         for (int i = 0; i < borne; i++) {
             URI result = null;
             try {
                 DicomInputStream inputStream = new DicomInputStream(new File(fileList.get(i)));
                 DicomObject dcmObj = inputStream.readDicomObject();
                 System.out.println("Store  from " + fileList.get(i) + " - " + (i + 1) + "th file");
                 result = list.get(0).store(dcmObj);
                 Assert.assertThat(result, IsNull.notNullValue());
             } catch (IOException e) {
             }
             boolean b = list.get(0).handles((URI) result);
             assertEquals(b, true);
             Iterable<StorageInputStream> listRes = list.get(0).at(result);
             Iterator it = listRes.iterator();
             int cmp = 0;
             while(it.hasNext()){
                 it.next();
                 cmp++;
             }
             assertEquals(cmp,1);
             Task<Report> task = instance.getIndexPlugins().get(0).index(listRes);
             task.run();
             System.out.println("Remove to   " + result);
             list.get(0).remove(result);
             listRes = list.get(0).at(result);
             it = listRes.iterator();
             cmp = 0;
             while(it.hasNext()){
                 it.next();
                 cmp++;
             }
             assertEquals(cmp,0);
         }
     }
 
     /**
      * Test of store method, of class MongoPlugin.
      */
     @Test
     public void testStore_DicomInputStream() throws Exception {
         ArrayList<String> fileList = retrieveFileList(new File("D:\\DICOM_data\\DATA\\"), 0);
         //int borne = fileList.size();
         int borne = 1;
         System.out.println("Store "+borne+" files");
         List<StorageInterface> list = (List<StorageInterface>) instance.getStoragePlugins();
         for (int i = 0; i < borne; i++) {
             URI result = null;
             try {
                 DicomInputStream inputStream = new DicomInputStream(new File(fileList.get(i)));
                 System.out.println("Store  from " + fileList.get(i) + " - " + (i + 1) + "th file");
                 result = list.get(0).store(inputStream);
                 Assert.assertThat(result, IsNull.notNullValue());
             } catch (IOException e) {
             }
             boolean b = list.get(0).handles((URI) result);
             assertEquals(b, true);
             Iterable<StorageInputStream> listRes = list.get(0).at(result);
             Iterator it = listRes.iterator();
             int cmp = 0;
             while(it.hasNext()){
                 it.next();
                 cmp++;
             }
             assertEquals(cmp,1);
             System.out.println("Remove to   " + result);
             list.get(0).remove(result);
             listRes = list.get(0).at(result);
             it = listRes.iterator();
             cmp = 0;
             while(it.hasNext()){
                 it.next();
                 cmp++;
             }
             assertEquals(cmp,0);
         }
     }
 }
