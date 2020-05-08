 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.triviagame.dal;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Iterator;
 import my.triviagame.bll.Database;
 import my.triviagame.xmcd.XmcdDisc;
 import my.triviagame.xmcd.XmcdDiscArchive;
 import my.triviagame.xmcd.XmcdFilters;
 import my.triviagame.xmcd.XmcdImporter;
 import org.apache.commons.io.FileUtils;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class DalImportTest {
 
     @Test   
     public void dummyTest() throws Throwable {
     }
 
     //@Ignore
     @Test
     public void testImportComplete() throws Throwable {
         Database.loadProperties();
         Database.openConnection();
         IDAL myDal = Database.getDataAccessLayer();
         
         File freedb_complete = new File(Database.getPropetyValue("completeFreedbPath"));
 
         XmcdDiscArchive allDiscs = new XmcdDiscArchive(freedb_complete);
 
         Iterator<XmcdDisc> toImport = new XmcdFilters.Factory(allDiscs).defaultFilters().firstN(10000).chain();
 
         XmcdImporter importer = new XmcdImporter(myDal);
        myDal.prepareDbPreImport();
         importer.importFreedb(toImport);
        myDal.prepareDbPreImport();
         allDiscs.close();
         myDal.closeConnection();
 
     }
 
     @Ignore
     @Test
     public void testImportMultipleUpdates() throws Throwable {
         DAL myDal = new DAL();
         myDal.openConnection("localhost", 3306, "cd_db_test", "root", "1234");
 
         File[] freedbUpdates = new File[]{
             FileUtils.toFile(getClass().getResource("resources/freedb-update-20120301-20120401.tar.bz2")),
             FileUtils.toFile(getClass().getResource("resources/freedb-update-20120401-20120501.tar.bz2")),
             FileUtils.toFile(getClass().getResource("resources/freedb-update-20120501-20120601.tar.bz2"))};
         
         for (File updateFile : freedbUpdates) {
             XmcdDiscArchive allDiscs = new XmcdDiscArchive(updateFile);
             Iterator<XmcdDisc> toImport = new XmcdFilters.Factory(allDiscs).defaultFilters().firstN(100).chain();
 
             XmcdImporter importer = new XmcdImporter(myDal);
             importer.importFreedb(toImport);
             allDiscs.close();
         }
 
         System.out.println(myDal.getAlbumDescriptors(Arrays.asList(new Integer[]{1})));
         myDal.closeConnection();
 
     }
 }
