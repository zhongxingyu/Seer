 package falgout.backup;
 
 import java.nio.file.FileStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 
 public class MockModule extends AbstractModule {
     public final TemporaryFileStore fs;
     
     @Inject
     public MockModule(TemporaryFileStore fs) {
         this.fs = fs;
     }
     
     @Override
     protected void configure() {
         bind(FileStoreLocator.class).toInstance(fs.locator);
         bind(FileStore.class).toInstance(fs.store);
        try {
            bind(MessageDigest.class).toInstance(MessageDigest.getInstance("md5"));
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
     }
 }
