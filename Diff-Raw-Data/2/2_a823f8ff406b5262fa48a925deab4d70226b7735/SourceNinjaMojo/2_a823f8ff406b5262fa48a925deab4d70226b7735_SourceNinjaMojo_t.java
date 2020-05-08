 package com.sourceninja.maven;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.collection.CollectRequest;
 import org.sonatype.aether.graph.Dependency;
 import org.sonatype.aether.graph.DependencyNode;
 import org.sonatype.aether.repository.RemoteRepository;
 import org.sonatype.aether.resolution.DependencyRequest;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import java.io.OutputStream;
 import java.io.ByteArrayOutputStream;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 /**
  * @goal send
  */
 @SuppressWarnings("unchecked")
 public class SourceNinjaMojo extends AbstractMojo
 {
     /**
      * @component
      */
     private RepositorySystem system;
 
     /**
      * @parameter default-value="${project}"
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @parameter default-value="${repositorySystemSession}"
      * @readonly
      */
     private RepositorySystemSession session;
 
     /**
      * @parameter default-value="${project.remoteProjectRepositories}"
      * @readonly
      */
     private List<RemoteRepository> repos;
 
     /**
      * @parameter
      * @required
      */
     private String id;
 
     /**
      * @parameter
      * @required
      */
     private String token;
 
     /**
      * @parameter default-value="https://app.sourceninja.com"
      * @required
      */
     private String host;
 
     /**
      * @parameter default-value=false
      */
     private boolean debug;
 
     private static List<Artifact> dependencyNodeListToArtifactList(List<DependencyNode> l) {
         List<Artifact> output = new Vector<Artifact>(l.size());
         for (DependencyNode n : l) {
             output.add(n.getDependency().getArtifact());
         }
         return output;
     }
 
     private static List<Artifact> dependencyListToArtifactList(List<Dependency> l) {
         List<Artifact> output = new Vector<Artifact>(l.size());
         for (Dependency n : l) {
             output.add(n.getArtifact());
         }
         return output;
     }
 
     private static void toJSON(OutputStream output, Object input) throws MojoExecutionException {
         try {
             ObjectMapper mapper = new ObjectMapper();
             mapper.writeValue(output, input);
         }
         catch (java.lang.Exception e) {
             throw new MojoExecutionException("Fatal error while attempting to construct JSON document", e);
         }
     }
 
     private static List<Map<String, Object>> artifactListToHashes(List<Artifact> input, boolean direct) {
         List<Map<String, Object>> output = new Vector<Map<String, Object>>(input.size());
         for (Artifact a : input) {
             HashMap<String, Object> h = new HashMap<String, Object>(3);
             h.put("name", a.getGroupId() + ":" + a.getArtifactId());
             h.put("version", a.getVersion());
             h.put("direct", direct);
             output.add(h);
         }
         return output;
     }
 
     private static boolean post(String url, String token, byte[] data, Log log) throws MojoExecutionException {
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httpPost = new HttpPost(url);
         ByteArrayBody uploadFilePart = new ByteArrayBody(data, "application/json", "maven.json");
         MultipartEntity reqEntity = new MultipartEntity();
 
         try {
             reqEntity.addPart("token", new StringBody(token));
             reqEntity.addPart("import_type", new StringBody("json"));
             reqEntity.addPart("import[import]", uploadFilePart);
             httpPost.setEntity(reqEntity);
         }
         catch (java.io.UnsupportedEncodingException e) {
             throw new MojoExecutionException("Fatal error, unsupported encoding while constructing HTTP post MIME body", e);
         }
 
         log.info("Posting dependency information to " + url);
         try {
             HttpResponse response = httpclient.execute(httpPost);
             StatusLine status = response.getStatusLine();
             switch (status.getStatusCode()) {
             case 201:
                log.info("Dependency data posted to SourceNinja successfully");
                 return true;
             case 404:
                 log.error("Invalid SourceNinja product ID");
                 return false;
             case 403:
                 log.error("Invalid SourceNinja product token");
                 return false;
             default:
                 log.error("Unexpected SourceNinja return status: " + status.getStatusCode() + " " + status.getReasonPhrase());
                 return false;
             }
         }
         catch (java.lang.Exception e) {
             throw new MojoExecutionException("Fatal error while attempting to POST data to SourceNinja", e);
         }
     }
 
     private static String sourceninjaUrl(String host, String id) {
         return host +  "/products/" + id + "/imports";
     }
 
 
     public void execute() throws MojoExecutionException
     {
         try {
             Artifact a = new DefaultArtifact(project.getArtifact().toString());
             DefaultArtifact pom = new DefaultArtifact(a.getGroupId(), a.getArtifactId(), "pom", a.getVersion());
             CollectRequest collectRequest = new CollectRequest();
             collectRequest.setRoot(new Dependency(pom, "compile"));
             collectRequest.setRepositories(repos);
 
             DependencyNode root = system.collectDependencies(session, collectRequest).getRoot();
             DependencyRequest dependencyRequest = new DependencyRequest(root, null);
 
             system.resolveDependencies(session, dependencyRequest);
 
             PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
             root.accept(nlg);
 
             List<Artifact> all = dependencyListToArtifactList(nlg.getDependencies(true));
             List<Artifact> direct = dependencyNodeListToArtifactList(root.getChildren());
             List<Artifact> indirect = new Vector<Artifact>(all);
             indirect.removeAll(direct);
 
             List<Map<String, Object>> hashes = (List<Map<String, Object>>) new Vector<Map<String, Object>>(all.size());
             hashes.addAll(artifactListToHashes(direct, true));
             hashes.addAll(artifactListToHashes(indirect, false));
 
             ByteArrayOutputStream outstream = new ByteArrayOutputStream();
             toJSON(outstream, hashes);
             post(sourceninjaUrl(host, id), token, outstream.toByteArray(), getLog());
 
             if (debug) {
                 System.out.println("All: " + all.size());
                 System.out.println(all);
                 System.out.println();
 
                 System.out.println("Direct: " + direct.size());
                 System.out.println(direct);
                 System.out.println();
 
                 System.out.println("Indirect: " + indirect.size());
                 System.out.println(indirect);
                 System.out.println();
 
                 System.out.println("JSON:");
                 System.out.println(outstream.toString());
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 }
