 package biz.itcf.uniqueres;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.Scanner;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 /**
  * 
  * @author Florian
  * @goal tag
  * @phase generate-resources
  * @requiresProject true
  */
 public class TagUniqueResourcesMojo extends AbstractMojo {
 
     /**
      * @readonly
      * @parameter default-value="${project}"
      * @required
      */
     private MavenProject project;
 
     /**
      * @parameter
      * @required
      */
     private List<Resource> resources;
 
     /**
      * @parameter default-value="${project.build.directory}/generated-resources"
      */
     private File outputDirectory;
 
     /**
      * @component
      */
     private BuildContext buildContext;
 
     /**
      * Search-pattern matching the un-tagged resource name. The default pattern
      * provides two match-groups: 1. source-path up to the file extension 2. the
      * file extension including the dot
      * 
      * These groups can be referenced by the {@link #taggedReplacementPattern}.
      * 
     * @parameter default-value="(.*?)(\\.[^.]*)"
      */
     private String untaggedSearchPattern;
 
     /**
      * Replacement pattern to create names for the tagged files. A special value
      * is @{unique.id} which addresses the generated Unique-ID of a file. One
      * can use $ plus a number to create back-references to matching groups of
      * {@link #untaggedSearchPattern}. See {@link Matcher#replaceAll(String)}
      * for more information.
      * 
      * @parameter default-value="$1_@{unique.id}$2"
      */
     private String taggedReplacementPattern;
 
     /**
      * @parameter default-value="tagged-index.properties"
      */
     private String indexFilename;
 
     protected MavenProject getProject() {
         return project;
     }
 
     protected File getOutputDirectory() {
         return outputDirectory;
     }
 
     protected List<Resource> getResources() {
         return resources;
     }
 
     protected BuildContext getBuildContext() {
         return buildContext;
     }
 
     protected String getUntaggedSearchPattern() {
         return untaggedSearchPattern;
     }
 
     protected String getTaggedReplacementPattern() {
         return taggedReplacementPattern;
     }
 
     protected String getIndexFilename() {
         return indexFilename;
     }
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         FileTagHelper helper = new FileTagHelper();
 
         IndexWriter indexWriter = new IndexWriter(getOutputDirectory(), getIndexFilename());
         try {
 
             for (Resource res : getResources()) {
                 File baseDir = new File(res.getDirectory());
                 if (!baseDir.isAbsolute()) {
                     baseDir = new File(getProject().getBasedir(), res.getDirectory());
                 }
 
                 Scanner scanner = makeScanner(res);
                 scanner.scan();
 
                 for (String resourceName : scanner.getIncludedFiles()) {
                     File resource = new File(baseDir, resourceName);
 
                     String taggedResourceName = helper.makeTaggedFilename(baseDir, resourceName);
 
                     File taggedResource = new File(getOutputDirectory(), taggedResourceName);
 
                     try {
                         indexWriter.writeIndex(resourceName, taggedResourceName);
 
                         FileUtils.copyFile(resource, taggedResource);
                     } catch (IOException e) {
                         throw new MojoExecutionException("Failed to copy file " + resource.getPath() + " to "
                                 + taggedResource.getPath(), e);
                     }
                 }
 
                 Resource outputResource = new Resource();
                 outputResource.setDirectory(getOutputDirectory().getAbsolutePath());
 
                 if (getLog().isDebugEnabled()) {
                     getLog().debug("Adding resources " + outputResource);
                 }
 
                 getProject().addResource(outputResource);
             }
         } finally {
             try {
                 indexWriter.close();
             } catch (IOException e) {
                 throw new MojoExecutionException("Could not close IndexWriter", e);
             }
         }
     }
 
     protected Scanner makeScanner(Resource res) {
         List<String> includes = res.getIncludes();
         if (includes.isEmpty()) {
             includes.add("**/*");
         }
 
         List<String> excludes = res.getExcludes();
 
         File baseDir = new File(res.getDirectory());
 
         Scanner scanner = getBuildContext().newScanner(baseDir);
         scanner.setIncludes(includes.toArray(new String[includes.size()]));
         scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
         return scanner;
     }
 
     public class FileTagHelper {
 
         private UniqueIDBuilder builder = new CRC32Builder();
         private Pattern untaggedSearchPattern;
         private Pattern uniqueIdPattern = Pattern.compile("@\\{unique\\.id\\}");
 
         public FileTagHelper() throws MojoExecutionException {
             try {
                 untaggedSearchPattern = Pattern.compile(getUntaggedSearchPattern());
             } catch (PatternSyntaxException e) {
                 throw new MojoExecutionException("Failed to compile untaggedSearchPattern", e);
             }
         }
 
         public String makeTaggedFilename(File baseDir, String resourceName) {
             File resource = new File(baseDir, resourceName);
             String uniqueID = builder.buildID(resource);
 
             String replacement = uniqueIdPattern.matcher(taggedReplacementPattern).replaceAll(uniqueID);
             return untaggedSearchPattern.matcher(resourceName).replaceAll(replacement);
         }
     }
 
     public static class IndexWriter implements Closeable {
 
         private FileOutputStream indexOut;
         private PrintWriter indexWriter;
 
         public IndexWriter(File baseDir, String indexFilename) throws MojoExecutionException {
             File indexFile = new File(baseDir, indexFilename);
 
             if (!baseDir.exists()) {
                 baseDir.mkdirs();
             }
 
             try {
                 indexOut = new FileOutputStream(indexFile);
             } catch (FileNotFoundException e) {
                 throw new MojoExecutionException("Could not open index-file " + indexFile + " for writing", e);
             }
 
             try {
                 indexWriter = new PrintWriter(new OutputStreamWriter(indexOut, "ISO8859_1"));
             } catch (UnsupportedEncodingException e) {
                 throw new MojoExecutionException("Charset ISO8859_1 is unsupported in this environment", e);
             }
         }
 
         public void writeIndex(String untaggedResource, String taggedResource) {
             indexWriter.print(untaggedResource);
             indexWriter.print("=");
             indexWriter.println(taggedResource);
         }
 
         @Override
         public void close() throws IOException {
             indexWriter.flush();
             indexWriter.close();
             indexOut.flush();
             indexOut.close();
         }
 
     }
 }
