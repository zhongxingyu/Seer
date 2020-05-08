 package org.eclipse.recommenders.livedoc.cli;
 
 import java.io.File;
import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.recommenders.livedoc.ILiveDoc;
 import org.eclipse.recommenders.livedoc.LiveDoc;
 import org.eclipse.recommenders.livedoc.aether.IRepositoryBroker;
 import org.eclipse.recommenders.livedoc.aether.RepoBrokerProvider;
 import org.eclipse.recommenders.livedoc.aether.RepositoryDescriptor;
 import org.eclipse.recommenders.livedoc.args4j.CLIOptions;
 import org.eclipse.recommenders.livedoc.args4j.ExtURLOptionHandler;
 import org.eclipse.recommenders.utils.Zips;

 import org.kohsuke.args4j.CmdLineParser;
 import org.sonatype.aether.RepositoryException;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 
 /**
  * This class controls all aspects of the application's execution
  */
 public class Application implements IApplication {
 
     private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "livedoc");
     private static final File SOURCES_TEMP_DIR = new File(TEMP_DIR, "sources");
     private static final File JAVADOC_TEMP_DIR = new File(TEMP_DIR, "javadoc");
     private static final File SOURCEREPO_CACHE_DIR = new File(TEMP_DIR, "sourceRepo/cache");
     private static final File SOURCEREPO_INDEX_DIR = new File(TEMP_DIR, "sourceRepo/indexes");
 //    private Logger log = LoggerFactory.getLogger(getClass());
     private CLIOptions settings;
     private IRepositoryBroker repoBroker;
     private RepositoryDescriptor sourceRepositoryDescriptor;
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app. IApplicationContext)
      */
     public Object start(IApplicationContext context) throws Exception {
 
         settings = parseArguments(context);
         prepareTempDirectory();
         initializeRepository();
 
 
         Artifact sourceArtifact = downloadSourceArtifact();
 
         String outPutFileName = outputName(sourceArtifact);
         File tmpOutput = new File(JAVADOC_TEMP_DIR, outPutFileName);
 
         generateJavaDoc(sourceArtifact, tmpOutput);
 
         if (settings.getOutputDir() != null) {
             File output = new File(settings.getOutputDir(), createDirectoryHierarchy(sourceArtifact));
             output.mkdirs();
             FileUtils.copyDirectory(tmpOutput, output);
         }
         if (settings.isJarOutput() || settings.getUploadRepo() != null) {
             File jarFile = jarOutput(tmpOutput);
 
             if (settings.isJarOutput()){
                 FileUtils.copyFileToDirectory(jarFile, settings.getOutputDir());
             }
 
             if (settings.getUploadRepo() != null) {
                 uploadJavadocArtifact(sourceArtifact, jarFile);
             }
         }
 
         System.out.println("Done.");
         return IApplication.EXIT_OK;
     }
 
     private void uploadJavadocArtifact(Artifact sourceArtifact, File artifactFile) throws RepositoryException {
         
         Artifact uploadArtifact = new DefaultArtifact(sourceArtifact.getGroupId(), sourceArtifact.getArtifactId(), "javadoc",
                 "jar", sourceArtifact.getVersion());
         
         uploadArtifact = uploadArtifact.setFile(artifactFile);
         
         RepositoryDescriptor uploadRepositoryDescriptor = new RepositoryDescriptor("uploadRepo",
                 settings.getUploadRepo());
         repoBroker.upload(uploadArtifact, uploadRepositoryDescriptor, new NullProgressMonitor());
     }
 
     private File jarOutput(File directory) throws IOException {
         
         String jarFileName = new StringBuffer(directory.getName())
             .append(".jar")
             .toString();
 
         File output = new File(directory.getParentFile() + File.separator + jarFileName);
         Zips.zip(directory, output);
         return output;
     }
 
     private String createDirectoryHierarchy(Artifact sourceArtifact) {
 
         StringBuffer sb = new StringBuffer();
         sb.append(sourceArtifact.getGroupId().replace(".", File.separator))
         .append(File.separator)
         .append(sourceArtifact.getArtifactId())
         .append(File.separator)
         .append(sourceArtifact.getBaseVersion());
         return sb.toString();
     }
 
     private void generateJavaDoc(Artifact sourceArtifact, File output) throws IOException {
         
         File sourceFiles = extractSourceFiles(sourceArtifact);
         List<String> subpackages = filterSourceFiles(sourceFiles);
         
         ILiveDoc livedoc = new LiveDoc(settings.isVerbose(), sourceFiles, output, subpackages);
         livedoc.setGroupId(sourceArtifact.getGroupId());
         livedoc.setArtifactId(sourceArtifact.getArtifactId());
         livedoc.setArtifactVersion(sourceArtifact.getVersion());
         livedoc.setModelsRepo(settings.getModelsRepo());
         livedoc.setSelectedTaglets(settings.getTaglets());
         
         livedoc.generate();
     }
 
     private String outputName(Artifact artifact) {
         StringBuffer sb = new StringBuffer(artifact.getArtifactId());
         sb.append("-");
         sb.append(artifact.getVersion());
         sb.append("-javadoc");
         return sb.toString();
     }
 
     private List<String> filterSourceFiles(File sourceFiles) {
        List<String> subpackages = Arrays.asList(sourceFiles.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !(name.equals("META-INF") 
                        || name.equals("about.html")
                        || name.equals("plugin.properties")
                        || name.equals("about_files"));
             }
         }));
        return subpackages;
     }
 
     private File extractSourceFiles(Artifact artifact) throws IOException {
         
         String fileName = artifact.getFile().getName();
         fileName = StringUtils.removeEnd(fileName, ".jar");
         
         File destFolder = new File(SOURCES_TEMP_DIR, fileName);
         Zips.unzip(artifact.getFile(), destFolder);     
         return destFolder;
     }
 
     private Artifact downloadSourceArtifact() throws RepositoryException {
         
         String artifactCoordinates = parseCoordinates();
 
         Artifact artifact = new DefaultArtifact(artifactCoordinates);
         return repoBroker.download(artifact, sourceRepositoryDescriptor, new NullProgressMonitor());
     }
 
     private String parseCoordinates() {
         
         String coordinates = settings.getMavenCoordinates();
         StringBuffer sb = new StringBuffer(coordinates);
         int lastColon = coordinates.lastIndexOf(":");
         sb.insert(lastColon, ":jar:sources");
         return sb.toString();
     }
 
     private void initializeRepository() throws IOException, Exception {
         repoBroker = RepoBrokerProvider.create(SOURCEREPO_CACHE_DIR, SOURCEREPO_INDEX_DIR);
         
         sourceRepositoryDescriptor = new RepositoryDescriptor("sourceRepo",
                 settings.getSourceRepo());
 
 
         repoBroker.ensureIndexUpToDate(sourceRepositoryDescriptor, new NullProgressMonitor());
     }
 
     private void prepareTempDirectory() {
         SOURCEREPO_CACHE_DIR.mkdirs();
         SOURCEREPO_INDEX_DIR.mkdirs();
          
         try {
             if (JAVADOC_TEMP_DIR.exists()){
                 FileUtils.cleanDirectory(JAVADOC_TEMP_DIR);
             }else{
                 JAVADOC_TEMP_DIR.mkdirs();
             }
             if (SOURCES_TEMP_DIR.exists()){
                 FileUtils.cleanDirectory(SOURCES_TEMP_DIR);            
             }else{
                 SOURCES_TEMP_DIR.mkdirs();
             }
         } catch (Exception e) {
             System.err.print("Couldn't clear livedoc tmp folders: ");
             e.printStackTrace();
         }
     }
 
     @SuppressWarnings({ "unchecked", "static-access" })
     private CLIOptions parseArguments(IApplicationContext context) {
         Iterator<String[]> iter = context.getArguments().values().iterator();
         String[] args = null;
         if (iter.hasNext()) {
             args = iter.next();
         } else {
             System.err.println("No Arguments detected");
         }
 
         CLIOptions settings = new CLIOptions();
         CmdLineParser parser = new CmdLineParser(settings);
         parser.registerHandler(URL.class, ExtURLOptionHandler.class);
         try {
             parser.parseArgument(args);
         } catch (Exception e) {
 
             // handling of wrong arguments
             System.err.println(e.getMessage());
             parser.printUsage(System.err);
             this.stop();
         }
         return settings;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.equinox.app.IApplication#stop()
      */
     public void stop() {
         try {
             Thread.sleep(2000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         System.exit(0);
     }
 }
