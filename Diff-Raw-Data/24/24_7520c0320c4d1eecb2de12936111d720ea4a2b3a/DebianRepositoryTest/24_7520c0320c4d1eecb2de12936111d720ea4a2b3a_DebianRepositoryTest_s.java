 package com.tw.pkg.deb.repo;
 
 import com.tw.pkg.deb.helper.VerificationHelper;
 import org.apache.commons.io.FileUtils;
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DebianRepositoryTest {
     @Test
     public void shouldCheckDebainReporitoryValidityCorrectly() throws Exception {
        DebianRepository debianRepository1 = new DebianRepository("http://in.archive.ubuntu.com/ubuntu/dists/hardy/main/binary-amd64/Packages.gz",
                 "/tmp/getPackagesForQuery");
         Assert.assertEquals(true, debianRepository1.isRepositoryValid());
 
        DebianRepository debianRepository2 = new DebianRepository("http://in.archive.ubuntu.com/ubuntu/dists/invalid/main/binary-amd64/Packages.gz",
                 "/tmp/getPackagesForQuery");
         Assert.assertEquals(false, debianRepository2.isRepositoryValid());
     }
 
     @Test
     public void shouldTellIfRepositoryHasChangesCorrectly() throws Exception {
        DebianRepository debianRepository = new DebianRepository("http://in.archive.ubuntu.com/ubuntu/dists/hardy/main/binary-amd64/Packages.gz", "/tmp/getPackagesForQuery");
         debianRepository.clearDownloadFolder();
         FileUtils.deleteQuietly(new File(debianRepository.getLastKnowDateStoreFilePath()));
         debianRepository.setKnownDate(0L);
         Assert.assertEquals(true, debianRepository.hasChanges());
         Assert.assertEquals(false, debianRepository.hasChanges());
     }
 
     @Ignore
     @Test
     public void shouldFetchPackageDataCorrectly() throws Exception {
        DebianRepository debianRepository = new DebianRepository("http://in.archive.ubuntu.com/ubuntu/dists/hardy/main/binary-amd64/Packages.gz", "/tmp/getPackagesForQuery");
         List<DebianPackage> allPackages = debianRepository.getAllPackages();
         System.out.println(allPackages.size());
     }
 
     @Test
     public void shouldReadPackageDataCorrectly() throws Exception {
         DebianRepository debianRepository = new DebianRepository(null, "/tmp/getPackagesForQuery");
         List<DebianPackage> debianPackages = new ArrayList<DebianPackage>();
         debianRepository.readData(debianPackages, new BufferedReader(new FileReader("test-data/data1.txt")));
         VerificationHelper.assertCorrectnessOfDebianPackage(debianPackages.get(0), 1);
         VerificationHelper.assertCorrectnessOfDebianPackage(debianPackages.get(1), 2);
     }
 
     @Test
     public void shouldReadPackageDataWithMultilineDescriptionCorrectly() throws Exception {
         DebianRepository debianRepository = new DebianRepository(null, "/tmp/getPackagesForQuery");
         List<DebianPackage> debianPackages = new ArrayList<DebianPackage>();
         debianRepository.readData(debianPackages, new BufferedReader(new FileReader("test-data/data2.txt")));
 
         Assert.assertEquals("name_1", debianPackages.get(0).getName());
 
         Assert.assertEquals("name_2", debianPackages.get(1).getName());
         Assert.assertEquals("version_2", debianPackages.get(1).getVersion());
     }
 }
