 package org.mule.galaxy.maven.publish;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.activation.MimeType;
 import javax.xml.namespace.QName;
 
 import org.apache.abdera.Abdera;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.i18n.text.UrlEncoding;
 import org.apache.abdera.i18n.text.CharUtils.Profile;
 import org.apache.abdera.model.Document;
 import org.apache.abdera.model.Element;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.protocol.client.AbderaClient;
 import org.apache.abdera.protocol.client.ClientResponse;
 import org.apache.abdera.protocol.client.RequestOptions;
 import org.apache.axiom.om.util.Base64;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.settings.Server;
 import org.apache.maven.settings.Settings;
 import org.codehaus.plexus.util.DirectoryScanner;
 
 /**
  * Publishes artifacts and resources to a workspace.
  * 
  * @goal execute
  */
 public class PublishMojo extends AbstractMojo {
     public static final String NAMESPACE = "http://galaxy.mule.org/2.0";
     /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
     private MavenProject project;
     
     /**
      * The user's settings.
      *
      * @parameter expression="${settings}"
      * @required
      * @readonly
      */
     private Settings settings;
     
     /**
      * The base directory
      *
      * @parameter expression="${basedir}"
      * @required
      * @readonly
      */
     private File basedir;
     
     /**
      * The Galaxy workspace URL.
      *
      * @parameter
      */
     private String url;
     
     /**
      * The server id to use for username/password information inside your settings.xml.
      *
      * @parameter
      */
     private String serverId;
     
     /**
      * The username (see serverId as well).
      *
      * @parameter
      */
     private String username;
 
     /**
      * The username (see serverId as well).
      *
      * @parameter
      */
     private String password;
     
     /**
      * Resources to publish to Galaxy
      *
      * @parameter
      */
     private String[] includes;
 
     /**
      * Resources to exclude from publishing to Galaxy
      *
      * @parameter
      */
     private String[] excludes;
 
     /**
      * Dependency filter to determine which artifacts which are attached to this project 
      * will be published to Galaxy
      *
      * @parameter
      */
     private String[] dependencyIncludes;
 
     /**
      * Dependency filter to determine which artifacts which are attached to this project 
      * will NOT be published to Galaxy
      *
      * @parameter
      */
     private String[] dependencyExcludes;
     
     /**
      * Whether or not to publish this project's dependencies to Galaxy.
      *
      * @parameter
      */
     private boolean publishProjectDependencies = true;
     
     /**
      * Whether or not to publish the artifact produced by this build.
      *
      * @parameter
      */
     private boolean publishProjectArtifact = true;
 
     /**
      * Whether or not to publish metadata such as the Maven artifact/group IDs
      * and SCM information.
      *
      * @parameter
      */
     private boolean publishProjectMetadata = true;
     
     /**
      * Whether or not to clear the artifacts from the workspace before
      * uploading new ones.
      *
      * @parameter
      */
     private boolean clearWorkspace = false;
     
     /**
      * Whether or not to overwrite artifact versions.
      *
      * @parameter
      */
     private boolean overwrite = false;
 
     private AbderaClient client;
 
     private String authorization;
 
     /**
      * Whether or not dependency excludes should act transitively.
      *
      * @parameter
      */
     private boolean actTransitively = true;
 
 
     /**
      * Whether or not the plugin should fail if there were filtering dependency criteria
      * which were not met.
      *
      * @parameter
      */
     private boolean strictFiltering;
     
     /**
      * Set this to true to skip execution. Or set the property galaxy.publish.skip to true
      * to skip execution. 
      * @parameter expression="${galaxy.publish.skip}"
      */
     private boolean skip;
     
     /**
      * Set this to true if you only want to show what would be uploaded.
      * 
      * @parameter expression="${galaxy.showOnly}"
      */
     private boolean showOnly;
     
     /**
      * Use the artifact's version when publishing. Otherwise use the project's version.
      * Defaults to false;
      * 
      * @parameter
      */
     private boolean useArtifactVersion;
     
     private Factory factory;
     private String scmUrl;
     
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (skip) {
             getLog().info("Skipping Galaxy publishing.");
             return;
         }
         
         if (showOnly) {
             getLog().info("showOnly mode is on. No changes will be made to the repository.");
         }
         
         String auth = null;
         if (serverId != null) {
             Server server = settings.getServer(serverId);
             
             if (server == null) {
                 throw new MojoFailureException("Could not find server: " + serverId);
             }
 
             if (server.getUsername() == null || server.getPassword() == null) {
                 throw new MojoFailureException("You must specify a username & password in your settings.xml!");
             }
             
             auth = server.getUsername() + ":" + server.getPassword();
         } else {
             if (username == null || password == null) {
                 throw new MojoFailureException("You must specify a username & password.");
             }
             auth = username + ":" + password;
         }
         
         Abdera abdera = Abdera.getInstance();
         factory = abdera.getFactory();
         client = new AbderaClient(abdera);
         
         authorization = "Basic " + Base64.encode(auth.getBytes());
     
         publish();
     }
 
     private void publish() throws MojoFailureException, MojoExecutionException {
         ensureWorkspaceExists();
         
         if (clearWorkspace) {
             clearWorkspace();
         }
         
         Set<Object> artifacts = new HashSet<Object>();
         
         if (publishProjectDependencies) {
             Set<?> deps = project.getArtifacts();
             if (deps != null && deps.size() > 0) artifacts.addAll(deps);
         }
         
         if (publishProjectArtifact) {
             artifacts.add(project.getArtifact());
         }
         
         initScmUrl();
 
         if (artifacts.size() > 0) {
             List<?> filterIncludes = dependencyIncludes != null ? Arrays.asList(dependencyIncludes) : Collections.EMPTY_LIST;
             List<?> filterExcludes = dependencyExcludes != null ? Arrays.asList(dependencyExcludes) : Collections.EMPTY_LIST;
             
             FilterUtils.filterArtifacts(artifacts, 
                                         filterIncludes, 
                                         filterExcludes, 
                                         strictFiltering, 
                                         actTransitively, 
                                         Collections.EMPTY_LIST, 
                                         getLog());
             for (Iterator<Object> itr = artifacts.iterator(); itr.hasNext();) {
                 Artifact a = (Artifact)itr.next();
                 
                 publishArtifact(a);
             }
         }
 
         if (includes != null || excludes != null) {
             DirectoryScanner scanner = new DirectoryScanner();
             scanner.setIncludes(includes);
             scanner.setExcludes(excludes);
             scanner.setBasedir(basedir);
             scanner.scan();
             
             String[] files = scanner.getIncludedFiles();
             if (files != null) {
                 for (String file : files) {
                     publishFile(new File(file), project.getArtifact().getVersion(), null);
                 }
             }
         }
     }
 
     private void initScmUrl() {
         // Find the SCM URL
         MavenProject parent = project;
         do {
             String newUrl = parent.getScm() != null ? parent.getScm().getUrl() : null;
             if (newUrl != null) {
                 scmUrl = newUrl;
             }
             parent = parent.getParent();
         } while (parent != null);
     }
 
     private void ensureWorkspaceExists() throws MojoFailureException {
         RequestOptions opts = new RequestOptions();
         opts.setAuthorization(authorization);
         
         ClientResponse res = client.head(url, opts);
         
         String url2 = url;
         while (res.getStatus() == 404) {
             int idx = url2.lastIndexOf('/');
             
             if (idx == url2.length()) {
                 continue;
             }
             
             if (idx != -1) {
                 url2 = url.substring(0, idx);
              
                 res.release();
                 res = client.head(url2, opts);
             } else {
                 break;
             }
         }
         
         if (url2 != null) {
             createWorkspace(url2);
         }
         
         res.release();
     }
 
     private void createWorkspace(String url2) throws MojoFailureException {
         String workspaceUrl = url.substring(url2.length());
         if ("".equals(workspaceUrl)) {
             return;
         }
         
         String[] workspaces = workspaceUrl.split("/");
         
         RequestOptions defaultOpts = getEntryRequestOptions();
         
         Factory factory = Abdera.getInstance().getFactory();
         
         String wkspcUrl = url2;
         for (String wkspc : workspaces) {
             if ("".equals(wkspc)) {
                 continue;
             }
             
             Entry entry = factory.newEntry();
             entry.setTitle(wkspc);
             entry.setUpdated(new Date());
             entry.addAuthor("Ignored");
             entry.setId(factory.newUuidUri());
             // Once we support workspace descriptions, the description will go here
             entry.setContent("");
             
             Element workspaceInfo = factory.newElement(new QName(NAMESPACE, "item-info"));
             workspaceInfo.setAttributeValue("name", wkspc);
             entry.addExtension(workspaceInfo);
             
             ClientResponse res = client.post(wkspcUrl, entry, defaultOpts);
             if (res.getStatus() != 409 && res.getStatus() >= 300) {
                 throw new MojoFailureException("Could not create a workspace. Got status: " 
                                                + res.getStatus()
                                                + " (" + res.getStatusText() + ") for " + wkspcUrl);
             }
             res.release();
             
             wkspcUrl += "/" + wkspc;
         }
     }
 
     private RequestOptions getEntryRequestOptions() {
         RequestOptions defaultOpts = client.getDefaultRequestOptions();
         defaultOpts.setAuthorization(authorization);
         defaultOpts.setContentType("application/atom+xml;type=entry");
         return defaultOpts;
     }
 
     private void clearWorkspace() throws MojoFailureException {
         RequestOptions opts = new RequestOptions();
         opts.setAuthorization(authorization);
         
         getLog().info("Clearing workspace " + url);
         
         ClientResponse res = client.get(url, opts);
         if (res.getStatus() == 404) {
             return;
         }
         
         if (res.getStatus() >= 300) {
             throw new MojoFailureException("Could not GET the workspace URL. Got status: " 
                                            + res.getStatus()
                                            + " (" + res.getStatusText() + ")");
         }
         
         assertResponseIsFeed(res);
         
         Document<Feed> doc = res.getDocument();
         Feed feed = doc.getRoot();
         
         for (Entry e : feed.getEntries()) {
             getLog().info("Deleting " + e.getTitle());
             
             if (!showOnly) {
                 ClientResponse delRes = client.delete("http://localhost:9002" + e.getLink("edit").getHref().toString(), opts);
                 delRes.release();
             }
         }
         
         res.release();
     }
 
     private void assertResponseIsFeed(ClientResponse res) throws MojoFailureException {
         MimeType contentType = res.getContentType();
         if ("application/atomcoll+xml".equals(contentType.getPrimaryType())) {
             throw new MojoFailureException("URL is not a valid Galaxy workspace. "
                                            + "It must be an Atom Collection. Received Content-Type: "
                                            + contentType);
         }
     }
 
     private void publishArtifact(Artifact a) throws MojoExecutionException, MojoFailureException {
         String version;
         if (useArtifactVersion) {
             version = a.getVersion();
         } else {
             version = project.getVersion();
         }
         File file = a.getFile();
         if (file != null) {
             publishFile(file, version, a);
         }
     }
 
     /**
      * @param file
      * @param version
      * @param a
      * @throws MojoFailureException
      * @throws MojoExecutionException
      */
     private void publishFile(File file, String version, Artifact a) throws MojoFailureException, MojoExecutionException {
         if (version == null) {
             throw new NullPointerException("Version can not be null!");
         }
         
         String name = file.getName();
         
         RequestOptions opts = new RequestOptions();
         opts.setAuthorization(authorization);
         opts.setContentType("application/octet-stream");
         opts.setSlug(name);
         opts.setHeader("X-Artifact-Version", version);
 
         String artifactUrl = url;
         if (!url.endsWith("/")) {
             artifactUrl += "/";
         }
         artifactUrl += UrlEncoding.encode(name, Profile.PATH.filter());
         
         try {
             // Check to see if this artifact exists already.
             ClientResponse res = client.head(artifactUrl, opts);
             int artifactExists = res.getStatus();
             res.release();
             
             // Check to see if this artifact version exists
             int artifactVersionExists = 404;
             String versionUrl = artifactUrl + "?version=" + version;
             if (artifactExists != 404 && artifactExists < 300) {
                 res = client.head(versionUrl, opts);
                 artifactVersionExists = res.getStatus();
                 res.release();
             }
             
             if (artifactExists == 404 && artifactVersionExists == 404) {
                 // create a new artifact
                 if (!showOnly) {
                     res = client.post(url, new FileInputStream(file), opts);
                     checkUploadResponse(res);
                     res.release();
                 }
                 getLog().info("Created artifact " + name + " (version " + version + ")");
             } else if (artifactVersionExists < 300) {
                 if (overwrite) {
                     if (!showOnly) {
                         res = client.put(versionUrl, new FileInputStream(file), opts);
                         res.release();
                     }
                     getLog().info("Overwrote artifact " + name + " (version " + version + ")");
                 } else {
                     getLog().info("Skipping artifact " + name + " as the current version already exists in the destination workspace.");                    
                 }
             } else if (artifactVersionExists >= 300 && artifactVersionExists != 404) {
                 throw new MojoFailureException("Could not determine if resource already exists in: " + name
                     + ". Got status " + res.getStatus() + " for URL " + artifactUrl + ".");
             } else {
                 // update the artifact
                 if (!showOnly) {
                     res = client.post(artifactUrl, new FileInputStream(file), opts);
                     checkUploadResponse(res);
                     res.release();
                 }
                 getLog().info("Updated artifact " + name + " (version " + version + ")");
             }
             
         } catch (IOException e) {
             throw new MojoExecutionException("Could not upload artifact to Galaxy: " 
                                              + name, e);
         }
         
         if (publishProjectMetadata && !showOnly) {
             publishProjectMetadata(name, artifactUrl + ";atom", a);
         }
     }
 
     private void checkUploadResponse(ClientResponse res) throws IOException, MojoFailureException {
        if (res.getStatus() != 200 || res.getStatus() != 201) {
             InputStream inputStream = res.getInputStream();
             copy(inputStream, System.out, 8096);
             inputStream.close();
             if (res.getStatus() == 404) {
                 throw new MojoFailureException("The specified repository URL is invalid. Got status: " + res.getStatus());
             } else {
                 throw new MojoFailureException("Could not upload artifact to that location. Got status: " + res.getStatus());
             }
         }
     }
     
     public static int copy(final InputStream input,
                            final OutputStream output,
                            int bufferSize)
        throws IOException {
        int avail = input.available();
        if (avail > 262144) {
            avail = 262144;
        }
        if (avail > bufferSize) {
            bufferSize = avail;
        }
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        n = input.read(buffer);
        int total = 0;
        while (-1 != n) {
            output.write(buffer, 0, n);
            total += n;
            n = input.read(buffer);
        }
        return total;
    }
     
     private void publishProjectMetadata(String name, String artifactUrl, Artifact a) throws MojoExecutionException {
         try{         
             RequestOptions defaultOpts = getEntryRequestOptions();
             ClientResponse res = client.get(artifactUrl, defaultOpts);
             Document<Entry> entryDoc = res.getDocument();
             Entry entry = entryDoc.getRoot();
             
             List<Element> extensions = entry.getExtensions(new QName(NAMESPACE, "metadata"));
             for (Element e : extensions) {
                 if ("versioned".equals(e.getAttributeValue("id"))) {
                     publishProjectMetadata(entry, e, name, artifactUrl, defaultOpts, a);
                 }
             }
             res.release();
         } catch (Exception e) {
             throw new MojoExecutionException("Could not publish metadata for artifact to Galaxy: " 
                                              + name, e);
         }
     }
 
     private void publishProjectMetadata(Element entry, 
                                         Element metadata, 
                                         String name, 
                                         String artifactUrl, 
                                         RequestOptions defaultOpts, 
                                         Artifact a) throws MojoFailureException {
         List<Element> elements = metadata.getElements();
         Element mavenProjectId = null;
         Element mavenArtifactId = null;
         Element ciInfo = null;
         Element sourceControl = null;
         Element issueTracker = null;
         
         for (Element e : elements) {
             if ("maven.project.id".equals(e.getAttributeValue("name"))) {
                 mavenProjectId = e;
             } else if ("maven.project.id".equals(e.getAttributeValue("name"))) {
                 mavenArtifactId = e;
             } else if ("ci.server".equals(e.getAttributeValue("name"))) {
                 ciInfo = e;
             } else if ("issue.tracker".equals(e.getAttributeValue("name"))) {
                 issueTracker = e;
             } else if ("scm".equals(e.getAttributeValue("name"))) {
                 sourceControl = e;
             } 
         }
         
         if (factory == null) {
             throw new NullPointerException("foobar");
         }
         
         mavenProjectId = ensureElementExists(mavenProjectId, new QName("property"), metadata, "maven.project.id");
         mavenProjectId.setAttributeValue("value", project.getGroupId() + ":" + project.getArtifactId());
 
         // Publish information only for the main artifact or resources
         if (a == project.getArtifact() || a == null) {
             String issueTrackerUrl = project.getIssueManagement() != null ? project.getIssueManagement().getUrl() : null;
             if (issueTrackerUrl != null) {
                 issueTracker = ensureElementExists(issueTracker, new QName("property"), metadata, "issue.tracker");
                 issueTracker.setAttributeValue("value", issueTrackerUrl);
             }
             
             if (scmUrl != null) {
                 sourceControl = ensureElementExists(sourceControl, new QName("property"), metadata, "scm");
                 sourceControl.setAttributeValue("value", scmUrl);
             }
             
             String ciServerUrl = project.getCiManagement() != null ? project.getCiManagement().getUrl() : null;
             if (ciServerUrl != null) {
                 ciInfo = ensureElementExists(ciInfo, new QName("property"), metadata, "ci.server");
                 ciInfo.setAttributeValue("value", ciServerUrl);
             }
         }
 
         // Only publish an artifact ID if this is an actual artifact
         if (a != null) {
             mavenArtifactId = ensureElementExists(mavenArtifactId, new QName("property"), metadata, "maven.artifact.id");
             mavenArtifactId.setAttributeValue("value", a.getGroupId() + ":" + a.getArtifactId());
         }
         
         ClientResponse res = client.put(artifactUrl, entry, defaultOpts);
         if (res.getStatus() >= 300) {
             throw new MojoFailureException("Could not update artifact metadata: " + name
                                            + ". Got \"" + res.getStatusText() + 
                                            "\" (" + res.getStatus() + ") for URL " + artifactUrl + ".");
         }
 
         res.release();
     }
 
     private Element ensureElementExists(Element element, QName name, Element metadata, String propName) {
         if (element == null) {
             element = factory.newElement(name, metadata);
             element.setAttributeValue("name", propName);
         }        
         return element;
     }
 
     public void setProject(MavenProject project) {
         this.project = project;
     }
 
     public void setSettings(Settings settings) {
         this.settings = settings;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public void setServerId(String serverId) {
         this.serverId = serverId;
     }
 
     public void setPublishProjectDependencies(boolean publishProject) {
         this.publishProjectDependencies = publishProject;
     }
 
     public void setClearWorkspace(boolean clearWorkspace) {
         this.clearWorkspace = clearWorkspace;
     }
 
     public void setIncludes(String[] includes) {
         this.includes = includes;
     }
 
     public void setExcludes(String[] excludes) {
         this.excludes = excludes;
     }
 
     public void setBasedir(File basedir) {
         this.basedir = basedir;
     }
 
     public void setDependencyIncludes(String[] dependencyIncludes) {
         this.dependencyIncludes = dependencyIncludes;
     }
 
     public void setDependencyExcludes(String[] dependencyExcludes) {
         this.dependencyExcludes = dependencyExcludes;
     }
 
     public void setStrictFiltering(boolean strictFiltering) {
         this.strictFiltering = strictFiltering;
     }
 
     public void setSkip(boolean skip) {
         this.skip = skip;
     }
 
     public void setShowOnly(boolean showOnly) {
         this.showOnly = showOnly;
     }
 
     public void setUseArtifactVersion(boolean useArtifactVersion) {
         this.useArtifactVersion = useArtifactVersion;
     }
 
     public void setOverwrite(boolean b) {
         this.overwrite = true;
     }
     
 }
