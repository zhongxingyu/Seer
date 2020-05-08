 package yamb;
 
 import yamb.app.DefaultApplicationContext;
 import yamb.app.MdiMainFrame;
 import yamb.app.bookmark.DefaultBookmarkManager;
 import yamb.app.fileitem.filelist.FileListItemCache;
 import yamb.app.fileitem.filelist.thumbnail.GlobalThumbnailCache;
 import yamb.app.tag.library.TagLibraryManager;
 import yamb.util.commands.CommandProvider;
 import yamb.util.commands.xml.XmlCommandProvider;
 import yamb.util.io.shell.FileOperation;
 import yamb.util.media.Images;
 import yamb.util.media.Videos;
 import yamb.util.media.thumbnail.ThumbnailGenerator;
 import yamb.util.media.thumbnail.ThumbnailGeneratorRegistry;
 import yamb.util.media.thumbnail.image.DefaultImageThumbnailGenerator;
 import yamb.util.media.thumbnail.video.ExternalProcessVideoThumbnailGenerator;
 import yamb.util.media.thumbnail.video.TaggedOnlyVideoGenerator;
 import yamb.util.media.thumbnail.video.VideoThumbnailGenerator;
 import yamb.util.media.thumbnail.video.dsj.DsjVideoThumbnailGenerator;
 import yamb.util.media.thumbnail.video.ffmpeg.FfmpegTempFileThumbnailGenerator;
 import yamb.util.media.thumbnail.video.jmf.JmfVideoThumbnailGenerator;
 import yamb.util.media.thumbnail.video.xuggler.XugglerVideoThumbnailGenerator;
 import org.apache.commons.io.FileUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.UIManager;
 
 public class Main
 {
 
     public static void main(String[] aArgs) throws Exception
     {
         Main main = new Main();
         main.init();
     }
 
     protected void init() throws Exception
     {
         // Set system look & feel
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
         // Load command provider
         InputStream commandStream = getClass().getClassLoader().getResourceAsStream("yamb/commands.xml");
         ResourceBundle commandBundle = null;//ResourceBundle.getBundle("fnj3.commands");
         CommandProvider commandProvider = new XmlCommandProvider(commandStream, commandBundle);
         commandStream.close();
 
         // Setup tag manager
         FileOperation fileOperation = new FileOperation();
         File dataDirectory = new File(System.getProperty("yamb.datadir"));
 //        TagManager tagManager = new AutoTagManager(loadDirectories(dataDirectory, "autotag.txt"));
         TagLibraryManager tagManager = new TagLibraryManager(
                 new File(dataDirectory, "libraries.txt"),
                 new File(dataDirectory, "library"),
                 fileOperation);
         tagManager.addFavoriteTags(loadFavorites(dataDirectory));
 
         // Setup bookmark and session managers
         DefaultBookmarkManager userBookmarkManager = new DefaultBookmarkManager(new File(dataDirectory, "bookmarks.txt"));
         DefaultBookmarkManager sessionBookmarkManager = new DefaultBookmarkManager(new File(dataDirectory, "session.txt"));
 
         DefaultApplicationContext appContext = new DefaultApplicationContext(dataDirectory, tagManager, tagManager,
                 userBookmarkManager, sessionBookmarkManager, fileOperation);
 
         // Setup file thumbnail cache service
         File thumbDirectory = new File(dataDirectory, "thumbnail");
         FileUtils.forceMkdir(thumbDirectory);
         VideoThumbnailGenerator jmfGenerator = new JmfVideoThumbnailGenerator();
        VideoThumbnailGenerator xugglerGenerator = new XugglerVideoThumbnailGenerator();
 //        VideoThumbnailGenerator jmfGenerator = new ExternalProcessVideoThumbnailGenerator(new File(thumbDirectory, "(temp)/jmf"), JmfVideoThumbnailGenerator.class);
         VideoThumbnailGenerator ffmpegGenerator = new FfmpegTempFileThumbnailGenerator(new File(thumbDirectory, ".temp/ffmpeg"));
 //        VideoThumbnailGenerator dsjGenerator = new DsjVideoThumbnailGenerator();
         VideoThumbnailGenerator dsjGenerator = new ExternalProcessVideoThumbnailGenerator(new File(thumbDirectory, ".temp/dsj"), DsjVideoThumbnailGenerator.class);
 //        VideoThumbnailGenerator xugglerGenerator = new ExternalProcessVideoThumbnailGenerator(new File(thumbDirectory, ".temp/xuggler"), XugglerVideoThumbnailGenerator.class);
 
         // Register thumbnail generators
         ThumbnailGeneratorRegistry thumbnailGeneratorRegistry = new ThumbnailGeneratorRegistry();
         //      Videos
         ThumbnailGenerator generator = new TaggedOnlyVideoGenerator(xugglerGenerator, dsjGenerator, ffmpegGenerator/*, jmfGenerator*/);
         thumbnailGeneratorRegistry.registerGenerator(Videos.getVideoExtensions(), generator);
//        thumbnailGeneratorRegistry.registerGenerator("flv", new TaggedOnlyVideoGenerator(dsjGenerator, jmfGenerator));
 //        thumbnailGeneratorRegistry.registerGenerator("asf", new TaggedOnlyVideoGenerator(dsjGenerator, ffmpegGenerator));
 //        thumbnailGeneratorRegistry.registerGenerator("mpg", new TaggedOnlyVideoGenerator(dsjGenerator, ffmpegGenerator));
 //        thumbnailGeneratorRegistry.registerGenerator("mpeg", new TaggedOnlyVideoGenerator(dsjGenerator, ffmpegGenerator));
        thumbnailGeneratorRegistry.registerGenerator("wmv", new TaggedOnlyVideoGenerator(dsjGenerator, ffmpegGenerator));
 
         //      Images
         thumbnailGeneratorRegistry.registerGenerator(Images.getImageExtensions(), new DefaultImageThumbnailGenerator());
 
         FileListItemCache fileListItemCache = new FileListItemCache(fileOperation);
         GlobalThumbnailCache globalThumbnailCache = new GlobalThumbnailCache(
                 thumbnailGeneratorRegistry, thumbDirectory, 175, 150, fileListItemCache, fileOperation);
 
         // Create application main frame
 //        JFrame frame = new MainFrame(appContext, commandProvider, globalThumbnailCache);
         MdiMainFrame frame = new MdiMainFrame(appContext, commandProvider, fileListItemCache, globalThumbnailCache);
 //        TabbedMainFrame frame = new TabbedMainFrame(appContext, commandProvider, fileListItemCache, globalThumbnailCache);
         appContext.setViewFactory(frame);
 
         frame.setVisible(true);
         tagManager.start();
     }
 
     private static List<String> loadFavorites(File aDataDirectory) throws IOException
     {
         List<String> tags = new ArrayList<String>();
         File favoritesFile = new File(aDataDirectory, "favorites.txt");
         if (!favoritesFile.exists())
         {
             FileUtils.touch(favoritesFile);
         }
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(favoritesFile)));
 
         try
         {
             String line;
             while ((line = reader.readLine()) != null)
             {
                 line = line.trim();
                 if (line.length() > 0 && !line.startsWith("#"))
                 {
                     tags.add(line);
                 }
             }
             return tags;
         }
         finally
         {
             reader.close();
         }
     }
 }
