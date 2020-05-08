 package com.bazaarvoice.maven.plugin.s3repo;
 
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.GetObjectRequest;
 import com.amazonaws.services.s3.model.ListObjectsRequest;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.amazonaws.services.s3.model.S3Object;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 import com.bazaarvoice.maven.plugin.s3repo.util.IOUtils;
 import com.bazaarvoice.maven.plugin.s3repo.util.LogStreamConsumer;
 import com.bazaarvoice.maven.plugin.s3repo.util.NullStreamConsumer;
 import com.bazaarvoice.maven.plugin.s3repo.util.SimpleNamespaceResolver;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.util.io.InputStreamFacade;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.resolution.ArtifactRequest;
 import org.sonatype.aether.resolution.ArtifactResolutionException;
 import org.sonatype.aether.util.StringUtils;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.GZIPInputStream;
 
 /**
  * Creates or updates a YUM repository in S3.
  */
 @Mojo(name = "create-update", defaultPhase = LifecyclePhase.DEPLOY)
 public class CreateOrUpdateS3RepoMojo extends AbstractMojo {
 
     /** Well-known names. */
     private static final String YUM_REPODATA_FOLDERNAME = "repodata";
     private static final String YUM_REPOMETADATA_FILENAME = "repomd.xml";
 
     @Component
     private MavenProject project;
 
     @Component
     private RepositorySystem repositorySystem;
 
     @Parameter(property = "session.repositorySession", required = true, readonly = true)
     private RepositorySystemSession session;
 
     /**
      * Staging directory. This is where we will generate bucket relative-files.
      */
     @Parameter(property = "s3repo.stagingDirectory", defaultValue = "${project.build.directory}/s3repo")
     private File stagingDirectory;
 
     /** Whether or not this goal should be allowed to create a new repository if it's needed. */
     @Parameter(property = "s3repo.allowCreateRepository", defaultValue = "false")
     private boolean allowCreateRepository;
 
     /** Auto increment snapshot dependencies. */
     @Parameter(property = "s3repo.autoIncrementSnapshotArtifacts", defaultValue = "true")
     private boolean autoIncrementSnapshotArtifacts;
 
     @Parameter(required = true)
     private List<ArtifactItem> artifactItems;
 
     /**
      * The s3 path to the root of the target repository.
      * These are all valid values:
      *      "s3://Bucket1/Repo1"
      *      "/Bucket/Repo1"
      * This goal does not create buckets; the plugin goal execution will fail if the bucket does not exist in S3.
      * Note that {@link #artifactItems} can optionally specify a per-artifact repositoryPath-relative target subfolder.
      */
     @Parameter(property = "s3repo.repositoryPath", required = true)
     private String s3RepositoryPath;
 
     @Parameter(property = "s3repo.accessKey", required = true)
     private String s3AccessKey;
 
     @Parameter(property = "s3repo.secretKey", required = true)
     private String s3SecretKey;
 
     /**
      * Execute all steps up to and excluding the upload to the S3. This can be set to true to perform a "dryRun" execution.
      */
     @Parameter(property = "s3repo.doNotUpload", defaultValue = "false")
     private boolean doNotUpload;
 
     /**
      * The createrepo executable.
      */
     @Parameter(property = "s3repo.createrepo", defaultValue = "createrepo")
     private String createrepo;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         CreateOrUpdateContext context = new CreateOrUpdateContext();
 
         // parse s3 repository path and set bucketAndFolder field
         context.setS3Session(createS3Client());
         context.setS3RepositoryPath(parseS3RepositoryPath());
 
         // always delete the staging directory before resolving dependencies -- it never makes sense to start with existing staging directory
         createOrCleanStagingDirectory();
 
         // require S3 bucket to exist before continuing
         ensureS3BucketExists(context);
         // download existing repository metadata
         pullExistingRepositoryMetadata(context);
         // require existing repository metadata if allowCreateRepository = false
         maybeEnsureExistingRepositoryMetadata(context);
         // synthesize/touch zero-size files to represent existing repository-managed files
         synthesizeExistingRepositoryFiles(context);
         // resolve artifacts, copy to staging directory
         resolveAndCopyArtifactItems(context);
         // create the actual repository
         createRepo(context);
         // pathologically delete files that we do not wish to push to target repository
         cleanupSynthesizedFiles(context);
         // push/upload staging directory to repository
         maybeUploadRepositoryUpdate(context);
     }
 
     private void maybeUploadRepositoryUpdate(CreateOrUpdateContext context) throws MojoExecutionException {
         if (doNotUpload) {
             getLog().info("Per configuration, not uploading to S3.");
             return;
         }
         final String targetBucket = context.getS3RepositoryPath().getBucketName();
         AmazonS3 s3Session = context.getS3Session();
         for (File toUpload : IOUtils.listAllFiles(stagingDirectory)) {
            String relativedPath = IOUtils.relativize(stagingDirectory, toUpload);
            // pathological -- replace *other* file separators with S3-style file separators and strip last separator
            relativedPath = relativedPath.replaceAll("\\\\", "/").replaceAll("/$", "");
            String key = relativedPath;
            getLog().info("Uploading " + toUpload.getName() + "to s3://" + targetBucket + "/" + key + "...");
             PutObjectRequest putObjectRequest = new PutObjectRequest(targetBucket, key, toUpload);
             s3Session.putObject(putObjectRequest);
         }
     }
 
     private void cleanupSynthesizedFiles(CreateOrUpdateContext context) throws MojoExecutionException {
         for (File synthesizedFile : context.getSynthesizedFiles()) {
             if (!synthesizedFile.delete()) {
                 throw new MojoExecutionException("Failed to delete synthesized file: " + synthesizedFile);
             }
         }
     }
 
     private void synthesizeExistingRepositoryFiles(CreateOrUpdateContext context) throws MojoExecutionException {
         // Here's what we'll do in this method:
         //      1) parse "repodata/repomd.xml" to determine the primary metadata file (typically "repodata/primary.xml.gz")
         //      2) extract a file list from the primary metadata file
         //      3) ensure that all files in the primary file list exist in the s3 repo
         //      4) "touch"/synthesize a zero-sized file for each file in the primary list
         File repoMetadataFile = determineLocalYumMetadataFile(context); // the repomd.xml file
         if (repoMetadataFile.isFile()) { // if it exists...
             // determine primary metadata file from metadata xml and parse it to determine repository files *declared* by the metadata
             List<String> repoRelativeFilePathList = extractFileListFromPrimaryMetadataFile(
                     parseXmlFile(resolvePrimaryMetadataFile(context, parseXmlFile(repoMetadataFile))));
             S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
             ListObjectsRequest request = new ListObjectsRequest()
                     .withBucketName(context.getS3RepositoryPath().getBucketName());
             if (s3RepositoryPath.hasBucketRelativeFolder()) {
                 request.withPrefix(s3RepositoryPath.getBucketRelativeFolder() + "/");
             }
             List<S3ObjectSummary> result = internalListObjects(context.getS3Session(), request);
             // we will start with a set of metadata-declared files and remove any file we find that exists in the repo;
             // we expect the Set to be empty when finished iteration. note that s3 api returns bucket-relative
             // paths, so we prefix each of our repoRelativeFilePaths with the repository path.
             Set<String> bucketRelativePaths = new HashSet<String>();
             for (String repoRelativeFilePath : repoRelativeFilePathList) {
                 if (s3RepositoryPath.hasBucketRelativeFolder()) {
                     bucketRelativePaths.add(s3RepositoryPath.getBucketRelativeFolder() + "/" + repoRelativeFilePath);
                 } else {
                     bucketRelativePaths.add(repoRelativeFilePath);
                 }
             }
             // for each bucket relative path in the listObjects result, remove from our set
             for (S3ObjectSummary summary : result) {
                 // note: the filesNotInRepo is are paths *relative* to repo
                 bucketRelativePaths.remove(summary.getKey());
             }
             // now, expect set to be empty
             if (!bucketRelativePaths.isEmpty()) {
                 throw new MojoExecutionException("Primary metadata file declared files that did not exist in the repository: " + bucketRelativePaths);
             }
             // for each file in our repoRelativeFilePathList, touch/synthesize the file
             for (String repoRelativeFilePath : repoRelativeFilePathList) {
                 String bucketRelativeFilePath = repoRelativeFilePath;
                 if (s3RepositoryPath.hasBucketRelativeFolder()) {
                     bucketRelativeFilePath = s3RepositoryPath.getBucketRelativeFolder() + "/" + repoRelativeFilePath;
                 }
                 File file = new File(stagingDirectory, bucketRelativeFilePath);
                 if (file.exists()) {
                     throw new MojoExecutionException("Repo already has this file: " + file.getPath());
                 }
                 IOUtils.touch(file);
                 context.addSynthesizedFile(file);
             }
         }
     }
 
     private List<String> extractFileListFromPrimaryMetadataFile(Document primaryMetadataFile) throws MojoExecutionException {
         // we will return a list of *repo-relative* file paths
         List<String> retval = new ArrayList<String>();
         String rootNamespaceUri = determineRootNamespaceUri(primaryMetadataFile);
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(SimpleNamespaceResolver.forPrefixAndNamespace("common", rootNamespaceUri));
         NodeList repoRelativeFilePaths =
             (NodeList) evaluateXPathNodeSet(xpath, "//common:metadata/common:package/common:location/@href", primaryMetadataFile);
         for (int i = 0; i < repoRelativeFilePaths.getLength(); ++i) {
             retval.add(repoRelativeFilePaths.item(i).getNodeValue());
         }
         return retval;
     }
 
     private File resolvePrimaryMetadataFile(CreateOrUpdateContext context, Document metadata) throws MojoExecutionException {
         S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
         // determine root namespace for use in xpath queries
         String rootNamespaceUri = determineRootNamespaceUri(metadata);
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(SimpleNamespaceResolver.forPrefixAndNamespace("repo", rootNamespaceUri));
         // primary metadata file, relative to *repository* root
         String repoRelativePrimaryMetadataFilePath =
             evaluateXPathString(xpath, "//repo:repomd/repo:data[@type='primary']/repo:location/@href", metadata);
         final String bucketRelativePrimaryMetadataFilePath;
         if (s3RepositoryPath.hasBucketRelativeFolder()) {
             bucketRelativePrimaryMetadataFilePath = s3RepositoryPath.getBucketRelativeFolder() + "/" + repoRelativePrimaryMetadataFilePath;
         } else {
             bucketRelativePrimaryMetadataFilePath = repoRelativePrimaryMetadataFilePath;
         }
         File primaryMetadataFile = new File(stagingDirectory, bucketRelativePrimaryMetadataFilePath);
         if (!primaryMetadataFile.isFile() || !primaryMetadataFile.getName().endsWith(".gz")) {
             throw new MojoExecutionException("Primary metadata file, '" + primaryMetadataFile.getPath() +
                 "', does not exist or does not have .gz extension");
         }
         return primaryMetadataFile;
     }
 
     private String determineRootNamespaceUri(Document metadata) {
         return metadata.getChildNodes().item(0).getNamespaceURI();
     }
 
     private Object evaluateXPathNodeSet(XPath xpath, String expression, Document document) throws MojoExecutionException {
         try {
             return xpath.evaluate(expression, document, XPathConstants.NODESET);
         } catch (XPathExpressionException e) {
             throw new MojoExecutionException(expression, e);
         }
     }
 
     private String evaluateXPathString(XPath xpath, String expression, Document document) throws MojoExecutionException {
         try {
             return xpath.evaluate(expression, document);
         } catch (XPathExpressionException e) {
             throw new MojoExecutionException(expression, e);
         }
     }
 
     private Document parseXmlFile(File file) throws MojoExecutionException {
         InputStream in = toInputStream(file);
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setNamespaceAware(true);
             DocumentBuilder builder = factory.newDocumentBuilder();
             return builder.parse(in);
         } catch (Exception e) {
             throw new MojoExecutionException("failed to parse", e);
         } finally {
             IOUtils.closeQuietly(in);
         }
     }
 
     private S3RepositoryPath parseS3RepositoryPath() throws MojoExecutionException {
         try {
             S3RepositoryPath parsed = S3RepositoryPath.parse(s3RepositoryPath);
             if (parsed.hasBucketRelativeFolder()) {
                 getLog().info("Using bucket '" + parsed.getBucketName() + "' and folder '" + parsed.getBucketRelativeFolder() + "' as repository...");
             } else {
                 getLog().info("Using bucket '" + parsed.getBucketName() + "' as repository...");
             }
             return parsed;
         } catch (Exception e) {
             throw new MojoExecutionException("Failed to parse S3 repository path: " + s3RepositoryPath, e);
         }
     }
 
     private void resolveAndCopyArtifactItems(CreateOrUpdateContext context) throws MojoExecutionException {
         copyArtifactItems(context, resolveArtifactItems(artifactItems));
     }
 
     private void maybeEnsureExistingRepositoryMetadata(CreateOrUpdateContext context) throws MojoExecutionException {
         if (!allowCreateRepository) {
             if (!determineLocalYumMetadataFile(context).isFile()) {
                 throw new MojoExecutionException("Repository folder " + context.getS3RepositoryPath().getBucketRelativeFolder() +
                     " is not an existing repository (i.e., it doesn't a contain " + YUM_REPODATA_FOLDERNAME + " folder)," +
                         " use allowCreateRepository = true to create");
             }
         }
     }
 
     private File determineLocalYumMetadataFile(CreateOrUpdateContext context) {
         S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
         String repodataFilePath = YUM_REPODATA_FOLDERNAME + "/" + YUM_REPOMETADATA_FILENAME;
         if (s3RepositoryPath.hasBucketRelativeFolder()) {
             // *prepend* repository folder path to filepath
             repodataFilePath = s3RepositoryPath.getBucketRelativeFolder() + "/" + repodataFilePath;
         }
         return new File(stagingDirectory, repodataFilePath);
     }
 
     private void copyArtifactItems(CreateOrUpdateContext context, List<ArtifactItem> resolvedArtifactItems) throws MojoExecutionException {
         for (ArtifactItem artifactItem : resolvedArtifactItems) {
             try {
                 S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
                 // use <artifactID>-<version> as extensionless filename
                 String baseFileName = artifactItem.getArtifactId() + "-" + artifactItem.getVersion();
                 int snaphshotIndex = 0;
                 File targetFile = null;
                 do {
                     if (artifactItem.isSnapshot() && autoIncrementSnapshotArtifacts) {
                         // snapshots are treated specially -- given an incrementing suffix that will be incremented on collisions
                         baseFileName += "-" + snaphshotIndex;
                     }
                     // create filename from dependency's file name but using pom-configured target subfolder and target extension
                     String targetFileName = baseFileName + "." + artifactItem.getTargetExtension();
                     String bucketRelativeFolderPath =
                         joinExcludeEmpties(',', artifactItem.getTargetSubfolder(), s3RepositoryPath.getBucketRelativeFolder());
                     final File targetDirectory;
                     if (StringUtils.isEmpty(bucketRelativeFolderPath)) {
                         targetDirectory = stagingDirectory;
                     } else {
                         targetDirectory = new File(stagingDirectory, bucketRelativeFolderPath);
                     }
                     targetFile = new File(targetDirectory, targetFileName);
                     if (targetFile.exists()) {
                         if (!artifactItem.isSnapshot() || !autoIncrementSnapshotArtifacts) {
                             // fail on file collisions!
                             throw new MojoExecutionException("Dependency " + artifactItem.getResolvedArtifact().getArtifact() + " already exists in repository!");
                         }
                         // file is a snapshot; increment snapshotIndex retry targetFile
                         ++snaphshotIndex;
                     } else {
                         // targetFile does not exist; we will copy to this file
                         break;
                     }
                 } while (true);
                 FileUtils.copyFile(artifactItem.getResolvedArtifact().getArtifact().getFile(), targetFile);
             } catch (IOException e) {
                 throw new MojoExecutionException("failed to copy artifact " + artifactItem + " to target", e);
             }
         }
     }
 
     private List<ArtifactItem> resolveArtifactItems(List<ArtifactItem> artifactItems) throws MojoExecutionException {
         // resolved artifacts have been downloaded and are available locally
         for (ArtifactItem item : artifactItems) {
             try {
                 item.setResolvedArtifact(repositorySystem.resolveArtifact(session, toArtifactRequest(item)));
             } catch (ArtifactResolutionException e) {
                 throw new MojoExecutionException("couldn't resolve: " + item, e);
             }
         }
         return artifactItems;
     }
 
     private ArtifactRequest toArtifactRequest(ArtifactItem item) {
         return new ArtifactRequest(toDefaultArtifact(item), project.getRemoteProjectRepositories(), "project");
     }
 
     private org.sonatype.aether.artifact.Artifact toDefaultArtifact(ArtifactItem item) {
         return new DefaultArtifact(item.getGroupId(), item.getArtifactId(), item.getType()/*extension*/, item.getClassifier(), item.getVersion());
     }
 
     private void createOrCleanStagingDirectory() throws MojoExecutionException {
         try {
             FileUtils.deleteDirectory(stagingDirectory);
             FileUtils.mkdir(stagingDirectory.getAbsolutePath());
         } catch (IOException e) {
             throw new MojoExecutionException("failed to clean or create staging directory: " + stagingDirectory, e);
         }
     }
 
     private AmazonS3Client createS3Client() {
         return new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));
     }
 
     private void ensureS3BucketExists(CreateOrUpdateContext context) throws MojoExecutionException {
         if (!context.getS3Session().doesBucketExist(context.getS3RepositoryPath().getBucketName())) {
             throw new MojoExecutionException("bucket doesn't exist in S3: " + context.getS3RepositoryPath().getBucketName());
         }
     }
 
     private void pullExistingRepositoryMetadata(CreateOrUpdateContext context) throws MojoExecutionException {
         S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
         // build bucket-relative metadata folder path *with "/" suffix*
         String bucketRelativeMetadataFolderPath = YUM_REPODATA_FOLDERNAME + "/";
         if (s3RepositoryPath.hasBucketRelativeFolder()) {
             // prefix repodata/ with repository folder
             bucketRelativeMetadataFolderPath = s3RepositoryPath.getBucketRelativeFolder() + "/" + bucketRelativeMetadataFolderPath;
         }
         ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                 .withBucketName(s3RepositoryPath.getBucketName())
                 .withPrefix(bucketRelativeMetadataFolderPath/*, which has "/" suffix*/);
         List<S3ObjectSummary> result = internalListObjects(context.getS3Session(), listObjectsRequest);
         for (S3ObjectSummary summary : result) {
             getLog().info("Downloading " + summary.getKey() + " from S3...");
             final S3Object object = context.getS3Session()
                     .getObject(new GetObjectRequest(s3RepositoryPath.getBucketName(), summary.getKey()));
             try {
                 File targetFile =
                     new File(stagingDirectory, /*assume object key is relative path to filename with extension*/summary.getKey());
                 // target file's directories will be created if they don't already exist
                 FileUtils.copyStreamToFile(new InputStreamFacade() {
                     @Override
                     public InputStream getInputStream() throws IOException {
                         return object.getObjectContent();
                     }
                 }, targetFile);
             } catch (IOException e) {
                 throw new MojoExecutionException("failed to downlod object from s3: " + summary.getKey(), e);
             }
         }
     }
 
     private List<S3ObjectSummary> internalListObjects(AmazonS3 s3Session, ListObjectsRequest request) {
         List<S3ObjectSummary> allResults = new ArrayList<S3ObjectSummary>();
         ObjectListing result = s3Session.listObjects(request);
         allResults.addAll(result.getObjectSummaries());
         while (result.isTruncated()) {
             result = s3Session.listNextBatchOfObjects(result);
             allResults.addAll(result.getObjectSummaries());
         }
         return allResults;
     }
 
     private void createRepo(CreateOrUpdateContext context) throws MojoExecutionException {
         Commandline commandline = new Commandline();
         commandline.setExecutable(createrepo);
         if (determineLocalYumMetadataFile(context).isFile()) {
             // if metadata already exists, we will execute "createrepo --update --skip-stat ."
             commandline.createArg().setValue("--update");
             commandline.createArg().setValue("--skip-stat");
         }
         S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
         if (s3RepositoryPath.hasBucketRelativeFolder()) {
             commandline.createArg().setValue(stagingDirectory.getPath() + "/" + context.getS3RepositoryPath().getBucketRelativeFolder());
         } else {
             commandline.createArg().setValue(stagingDirectory.getPath());
         }
         getLog().debug("Executing \'" + commandline.toString() + "\'");
         try {
             int result = CommandLineUtils.executeCommandLine(commandline, NullStreamConsumer.theInstance, new LogStreamConsumer(getLog()));
             if (result != 0) {
                 throw new MojoExecutionException(createrepo +" returned: \'" + result + "\' executing \'" + commandline + "\'");
             }
         } catch (CommandLineException e) {
             throw new MojoExecutionException("Unable to execute: " + commandline, e);
         }
         getLog().info("Successfully built repo using directory: " + stagingDirectory);
     }
 
     private static InputStream toInputStream(File file) throws MojoExecutionException {
         try {
             InputStream in = new FileInputStream(file);
             if (file.getName().endsWith(".gz")) {
                 in = new GZIPInputStream(in);
             }
             return in;
         } catch (IOException e) {
             throw new MojoExecutionException("Failed to read file " + file.getPath(), e);
         }
     }
 
     private static String joinExcludeEmpties(char delimiter, String... values) {
         StringBuilder buf = new StringBuilder();
         String separator = "";
         for (String value : values) {
             if (!StringUtils.isEmpty(value)) {
                 buf.append(separator).append(value);
                 separator = String.valueOf(delimiter);
             }
         }
         return buf.toString();
     }
 
 }
