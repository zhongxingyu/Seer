 package falgout.backup;
 
 import java.nio.file.FileStore;
 
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
     }
 }
