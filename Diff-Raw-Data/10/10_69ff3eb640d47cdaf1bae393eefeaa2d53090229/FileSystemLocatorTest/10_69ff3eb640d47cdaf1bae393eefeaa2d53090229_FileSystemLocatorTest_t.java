 package org.netmelody.menodora.core.locator;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.contains;
 
 
 public final class FileSystemLocatorTest {
 
     @Rule
     public final TemporaryFolder folder = new TemporaryFolder();
 
     @Test public void
     locatesFiles() throws IOException {
         FileUtils.touch(new File(folder.getRoot(), "myfile.txt"));
         FileUtils.touch(new File(folder.getRoot(), "subfolder/myfile.txt"));
         
         final FileSystemLocator locator = new FileSystemLocator(folder.getRoot(), "myfile.txt");
         
        assertThat(locator.locate(), contains("/myfile.txt", "/subfolder/myfile.txt"));
     }
     
 }
