 package falgout.backup.app;
 
 import static org.hamcrest.Matchers.contains;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.UUID;
 
 import org.jukito.JukitoModule;
 import org.jukito.JukitoRunner;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Scopes;
 
 import falgout.backup.BackupModule;
 import falgout.backup.Directories;
 import falgout.backup.Directories.FileChecker;
 import falgout.backup.MockModule;
 import falgout.backup.TemporaryFileStore;
 import falgout.backup.TemporaryFileStructure;
 
 @RunWith(JukitoRunner.class)
 public class DefaultManagerTest {
     public static class A extends JukitoModule {
         @Override
         protected void configureTest() {
             bind(TemporaryFileStructure.class).in(Scopes.NO_SCOPE);
         }
     }
     
     @Rule @Inject public TemporaryFileStore fs;
     @Rule @Inject public TemporaryFileStructure conf;
     @Rule @Inject public TemporaryFileStructure backup;
     private DefaultManager m;
     private Device dev;
     
     @Before
     public void init() throws IOException, NoSuchAlgorithmException {
         Map<String, String> props = new LinkedHashMap<>();
         props.put("conf", conf.dir.toString());
         props.put("location", backup.dir.toString());
        final MessageDigest md = MessageDigest.getInstance("md5");
        Injector i = Guice.createInjector(new BackupModule(props), new DeviceModule(), new MockModule(fs),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MessageDigest.class).toInstance(md);
                    }
                });
         m = i.getInstance(DefaultManager.class);
         dev = i.getInstance(DeviceFactory.class).create(fs.store);
         dev.addPathToBackup(fs.dir1);
     }
     
     @Test
     public void ManagedDevicesIsInitiallyEmpty() throws IOException {
         assertEquals(0, m.getManagedDevices().size());
     }
     
     @Test
     public void BackupDatesForNonExistingUUIDIsEmpty() throws IOException {
         assertEquals(0, m.getBackupDates(UUID.randomUUID()).size());
     }
     
     @Test
     public void AfterBackingDeviceUpItIsInManagedDevices() throws IOException {
         Date time = new Date();
         
         m.backup(fs.store);
         assertThat(m.getManagedDevices(), contains(dev.getID()));
         SortedSet<Date> dates = m.getBackupDates(dev.getID());
         assertEquals(1, dates.size());
         Date backedUp = dates.last();
         assertTrue(backedUp.after(time));
     }
     
     @Test
     public void BackingUpNewDeviceCreatesCopy() throws IOException {
         m.backup(fs.store);
         
         Path dir = Directories.enumerateEntries(m.getBackupRoot(dev)).get(1);
         assertTrue(Directories.isStructureSame(dir.resolve(fs.dir1.getFileName()), fs.dir1));
     }
     
     @Test
     public void BackingUpTwiceWithNoChangesUsesHardLinks() throws IOException {
         m.backup(fs.store);
         m.backup(fs.store);
         
         checkBackups(new FileChecker<Path>() {
             @Override
             public boolean areFilesSame(Path p1, Path p2) throws IOException {
                 return Files.isRegularFile(p1) ? Files.isSameFile(p1, p2) : true;
             }
         });
     }
     
     private void checkBackups(FileChecker<Path> checker) throws IOException {
         SortedSet<Date> dates = m.getBackupDates(dev.getID());
         assertEquals(2, dates.size());
         Path d1 = m.getBackupDir(dev, dates.first());
         Path d2 = m.getBackupDir(dev, dates.last());
         
         assertTrue(Directories.isStructureSame(d1, d2, checker));
     }
     
     @Test
     public void BackingUpTwiceWithChangesCreatesCopies() throws IOException {
         final byte[] newContent = { 1, 2, 3 };
         Files.createFile(fs.dir1.resolve("foobar"));
         m.backup(fs.store);
         Files.write(fs.file2, newContent);
         m.backup(fs.store);
         
         checkBackups(new FileChecker<Path>() {
             @Override
             public boolean areFilesSame(Path p1, Path p2) throws IOException {
                 if (Files.isRegularFile(p1)) {
                     if (p1.getFileName().toString().equals("foobar")) {
                         return Files.isSameFile(p1, p2);
                     } else {
                         int dif = Files.readAllBytes(p1).length - Files.readAllBytes(p2).length;
                         return Math.abs(dif) == newContent.length;
                     }
                 } else {
                     return true;
                 }
             }
         });
     }
 }
