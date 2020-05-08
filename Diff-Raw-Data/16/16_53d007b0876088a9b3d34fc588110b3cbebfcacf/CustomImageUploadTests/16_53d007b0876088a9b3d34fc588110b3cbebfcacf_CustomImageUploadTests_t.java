 package providers;
 
 import com.ece.superkids.FileManagerImpl;
 import com.ece.superkids.images.ImageManager;
 import com.ece.superkids.questions.QuestionDatabaseFactory;
 import org.junit.After;
 import org.junit.Test;
 import com.ece.superkids.ui.providers.ImageProvider;
 import com.ece.superkids.ui.providers.ResourceProviderFactory;
 
 import java.io.*;
 import java.util.Properties;
 
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
 
 public class CustomImageUploadTests {
 
    private static final String name = "testelephant";
    private String key;
     private ImageProvider imageProvider = ResourceProviderFactory.anImageProvider();
     private ImageManager imageManager = QuestionDatabaseFactory.anImageManager();
 
     @After
     public void cleanup() throws IOException {
         Properties props = new Properties();
         InputStream in = new FileInputStream(FileManagerImpl.getInstance().getImagePathsFile());
         props.load(in);
         in.close();
 
        File image = new File(FilenameUtils.separatorsToUnix(props.getProperty(key)));
        FileUtils.forceDelete(image);
 
         props.remove(key);
         Writer out = new FileWriter(FileManagerImpl.getInstance().getImagePathsFile());
         props.store(out, "Images");
         out.close();
     }
 
     @Test
     public void uploadImagesAppearInProvider() {
        key=name;
         String path = getClass().getResource("/elephant.png").getPath();
 
         assertNull(imageProvider.getImage(key));
        key = imageManager.saveImage(path, key);
         imageProvider.refresh();
         assertNotNull(imageProvider.getImage(key));
     }
 }
