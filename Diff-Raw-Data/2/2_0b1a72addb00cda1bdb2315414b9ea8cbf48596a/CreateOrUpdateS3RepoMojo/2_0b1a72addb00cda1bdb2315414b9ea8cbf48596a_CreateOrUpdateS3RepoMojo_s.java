 package com.bazaarvoice.maven.plugin.s3repo.create;
 
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.GetObjectRequest;
 import com.amazonaws.services.s3.model.ListObjectsRequest;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.amazonaws.services.s3.model.S3Object;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 import com.bazaarvoice.maven.plugin.s3repo.S3RepositoryPath;
 import com.bazaarvoice.maven.plugin.s3repo.WellKnowns;
 import com.bazaarvoice.maven.plugin.s3repo.support.LocalYumRepoFacade;
 import com.bazaarvoice.maven.plugin.s3repo.util.ExtraFileUtils;
 import com.bazaarvoice.maven.plugin.s3repo.util.ExtraIOUtils;
 import com.bazaarvoice.maven.plugin.s3repo.util.S3Utils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.io.InputStreamFacade;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.resolution.ArtifactRequest;
 import org.sonatype.aether.resolution.ArtifactResolutionException;
 import org.sonatype.aether.util.StringUtils;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Creates or updates a YUM repository in S3.
  */
 @Mojo(name = "create-update", defaultPhase = LifecyclePhase.DEPLOY)
 public class CreateOrUpdateS3RepoMojo extends AbstractMojo {
 
     @Component
     private MavenProject project;
 
     @Component
     private RepositorySystem repositorySystem;
 
     @Parameter(property = "session.repositorySession", required = true, readonly = true)
     private RepositorySystemSession session;
 
     /** Staging directory. This is where we will generate *bucket-relative* files. */
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
 
     /** Execute all steps up to and excluding the upload to the S3. This can be set to true to perform a "dryRun" execution. */
     @Parameter(property = "s3repo.doNotUpload", defaultValue = "false")
     private boolean doNotUpload;
 
     /** The createrepo executable. */
     @Parameter(property = "s3repo.createrepo", defaultValue = "createrepo")
     private String createrepo;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         CreateOrUpdateContext context = new CreateOrUpdateContext();
 
         // parse s3 repository path and set bucketAndFolder field
         context.setS3Session(createS3Client());
         context.setS3RepositoryPath(parseS3RepositoryPath());
         context.setLocalYumRepo(determineLocalYumRepo(context.getS3RepositoryPath()));
 
         // always clean the staging directory -- it never makes sense to start with existing staging directory
         ExtraFileUtils.createOrCleanDirectory(stagingDirectory);
 
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
         // push/upload staging directory to repository if doNotUpload = false
         maybeUploadRepositoryUpdate(context);
     }
 
     /** Create a {@link LocalYumRepoFacade} which will allow us to query and operate on a local (on-disk) yum repository. */
     private LocalYumRepoFacade determineLocalYumRepo(S3RepositoryPath s3RepositoryPath) {
         return new LocalYumRepoFacade(
                 s3RepositoryPath.hasBucketRelativeFolder()
                     ? new File(stagingDirectory, s3RepositoryPath.getBucketRelativeFolder())
                     : stagingDirectory, createrepo, getLog());
     }
 
     private void maybeUploadRepositoryUpdate(CreateOrUpdateContext context) throws MojoExecutionException {
         if (doNotUpload) {
             getLog().info("Per configuration, not uploading to S3.");
             return;
         }
         final String targetBucket = context.getS3RepositoryPath().getBucketName();
         AmazonS3 s3Session = context.getS3Session();
         for (File toUpload : ExtraIOUtils.listAllFiles(stagingDirectory)) {
             String relativizedPath = ExtraIOUtils.relativize(stagingDirectory, toUpload);
             // replace *other* file separators with S3-style file separators and strip first & last separator
             relativizedPath = relativizedPath.replaceAll("\\\\", "/").replaceAll("^/", "").replaceAll("/$", "");
             String key = relativizedPath;
             getLog().info("Uploading " + toUpload.getName() + " to s3://" + targetBucket + "/" + key + "...");
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
         if (context.getLocalYumRepo().isRepoDataExists()) { // if repo exists...
             // determine primary metadata file from metadata xml and parse it to determine repository files *declared* by the metadata
             List<String> repoRelativeFilePathList = context.getLocalYumRepo().parseFileListFromRepoMetadata();
             S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
             ListObjectsRequest request = new ListObjectsRequest()
                     .withBucketName(context.getS3RepositoryPath().getBucketName());
             if (s3RepositoryPath.hasBucketRelativeFolder()) {
                 request.withPrefix(s3RepositoryPath.getBucketRelativeFolder() + "/");
             }
             List<S3ObjectSummary> result = S3Utils.listAllObjects(context.getS3Session(), request);
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
                 ExtraIOUtils.touch(file);
                 context.addSynthesizedFile(file);
             }
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
             if (!context.getLocalYumRepo().isRepoDataExists()) {
                 throw new MojoExecutionException("Repository folder " + context.getS3RepositoryPath().getBucketRelativeFolder() +
                     " is not an existing repository (i.e., it doesn't a contain " + WellKnowns.YUM_REPODATA_FOLDERNAME + " folder)," +
                         " use allowCreateRepository = true to create");
             }
         }
     }
 
     private File determineLocalYumMetadataFile(CreateOrUpdateContext context) {
         S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
         String repodataFilePath = WellKnowns.YUM_REPODATA_FOLDERNAME + "/" + WellKnowns.YUM_REPOMETADATA_FILENAME;
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
                 // if a targetBaseName isn't specified, use <artifactID>-<version> as extensionless filename
                 final String baseFileName = artifactItem.hasTargetBaseName()
                             ? artifactItem.getTargetBaseName()
                             : artifactItem.getArtifactId() + "-" + artifactItem.getVersion();
                 int snaphshotIndex = 0;
                 File targetFile;
                 do {
                     String baseFileNameToUse = baseFileName;
                     if (artifactItem.isSnapshot() && autoIncrementSnapshotArtifacts && snaphshotIndex > 0 /*never suffix with 0*/) {
                         // snapshots are treated specially -- given an incrementing suffix that will be incremented on collisions
                         baseFileNameToUse = baseFileName + snaphshotIndex;
                     }
                     // create filename from dependency's file name but using pom-configured target subfolder and target extension
                     String targetFileName = baseFileNameToUse + "." + artifactItem.getTargetExtension();
                     String bucketRelativeFolderPath =
                         joinExcludeEmpties('/', s3RepositoryPath.getBucketRelativeFolder(), artifactItem.getTargetSubfolder());
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
                getLog().info("Copying artifact to " + targetFile.getPath());
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
 
     private AmazonS3Client createS3Client() {
         return new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));
     }
 
     private void ensureS3BucketExists(CreateOrUpdateContext context) throws MojoExecutionException {
         if (!context.getS3Session().doesBucketExist(context.getS3RepositoryPath().getBucketName())) {
             throw new MojoExecutionException("Bucket doesn't exist in S3: " + context.getS3RepositoryPath().getBucketName());
         }
     }
 
     private void pullExistingRepositoryMetadata(CreateOrUpdateContext context) throws MojoExecutionException {
         S3RepositoryPath s3RepositoryPath = context.getS3RepositoryPath();
         // build bucket-relative metadata folder path *with "/" suffix*
         String bucketRelativeMetadataFolderPath = WellKnowns.YUM_REPODATA_FOLDERNAME + "/";
         if (s3RepositoryPath.hasBucketRelativeFolder()) {
             // prefix repodata/ with repository folder
             bucketRelativeMetadataFolderPath = s3RepositoryPath.getBucketRelativeFolder() + "/" + bucketRelativeMetadataFolderPath;
         }
         ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                 .withBucketName(s3RepositoryPath.getBucketName())
                 .withPrefix(bucketRelativeMetadataFolderPath/*, which has "/" suffix*/);
         List<S3ObjectSummary> result = S3Utils.listAllObjects(context.getS3Session(), listObjectsRequest);
         getLog().debug("Found " + result.size() + " objects in bucket '" + s3RepositoryPath.getBucketName()
                 + "' with prefix '" + bucketRelativeMetadataFolderPath + "'...");
         for (S3ObjectSummary summary : result) {
             getLog().info("Downloading " + summary.getKey() + " from S3...");
             final S3Object object = context.getS3Session()
                     .getObject(new GetObjectRequest(s3RepositoryPath.getBucketName(), summary.getKey()));
             try {
                 File targetFile =
                     new File(stagingDirectory,
                         /*assume object key is *bucket-relative* path to the filename with extension*/summary.getKey());
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
 
     private void createRepo(CreateOrUpdateContext context) throws MojoExecutionException {
         if (context.getLocalYumRepo().isRepoDataExists()) {
             context.getLocalYumRepo().updateRepo();
         } else {
             context.getLocalYumRepo().createRepo();
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
