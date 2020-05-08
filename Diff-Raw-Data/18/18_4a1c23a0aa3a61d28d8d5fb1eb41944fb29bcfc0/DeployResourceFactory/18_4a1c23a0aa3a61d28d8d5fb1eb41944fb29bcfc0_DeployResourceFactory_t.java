 package com.ettrema.web.manage.deploy;
 
 import com.bradmcevoy.common.Path;
 import com.bradmcevoy.http.Request.Method;
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.exceptions.NotFoundException;
 import com.bradmcevoy.http.http11.auth.DigestResponse;
 import static com.ettrema.context.RequestContext._;
 import com.ettrema.logging.LogUtils;
 import com.ettrema.vfs.VfsSession;
 import com.ettrema.web.ExistingResourceFactory;
 import com.ettrema.web.Web;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.io.IOUtils;
 
 /**
  *
  * @author brad
  */
 public class DeployResourceFactory implements ResourceFactory {
 
     private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeployResourceFactory.class);
     private final ExistingResourceFactory existingResourceFactory;
     private final DeploymentService deploymentService;
     private String deployFolderName = "_deploy";
 
     public DeployResourceFactory(ExistingResourceFactory existingResourceFactory, DeploymentService deploymentService) {
         this.existingResourceFactory = existingResourceFactory;
         this.deploymentService = deploymentService;
     }
 
     @Override
     public Resource getResource(String host, String spath) throws NotAuthorizedException, BadRequestException {
         Path path = Path.path(spath);
         Resource r = find(host, path);
         LogUtils.trace(log, "getResource:", host, spath, r);
         return r;
     }
 
     private Resource find(String host, Path path) throws NotAuthorizedException, BadRequestException {
         // if name is _deploy and its parent is a Web (or Host) we have a DeployFolder
         if (path.getName().equals(deployFolderName)) {
             return getDeployFolderOrNull(host, path);
         } else {
             // if parent is a deployfolder then return its child
             Path parentPath = path.getParent();
             DeployFolder deployFolder = getDeployFolderOrNull(host, parentPath);
             if (deployFolder != null) {
                 return deployFolder.child(path.getName());
             } else {
                 return null;
             }
         }
     }
 
     private DeployFolder getDeployFolderOrNull(String host, Path path) throws BadRequestException, NotAuthorizedException {
         Resource r = existingResourceFactory.getResource(host, path.getParent().toString());
         if (r == null) {
             return null;
         } else if (r instanceof Web) {
             Web web = (Web) r;
             return new DeployFolder(web);
         } else {
             return null;
         }
     }
 
     public class DeployFolder extends AbstractDeploymentResource implements CollectionResource, PutableResource, PropFindableResource, DigestResource, GetableResource {
 
         private final Web web;
 
         public DeployFolder(Web web) {
             super(web);
             this.web = web;
         }
 
         @Override
         public Resource child(String childName) {
             for (Resource r : getChildren()) {
                 if (r.getName().equals(childName)) {
                     return r;
                 }
             }
             return null;
         }
 
         @Override
         public List<? extends Resource> getChildren() {
             List<com.ettrema.web.manage.deploy.Deployment> deployments = deploymentService.getDeployments(web);
             List<DeploymentResource> list = new ArrayList<>();
             for (Deployment d : deployments) {
                 list.add(new DeploymentResource(web, d));
             }
             LogUtils.trace(log, "DeployFolder.getChildren - size:", list.size());
             return list;
         }
 
         @Override
         public String getUniqueId() {
             return null;
         }
 
         @Override
         public String getName() {
             return deployFolderName;
         }
 
         @Override
         public Date getModifiedDate() {
             return null;
         }
 
         @Override
         public String checkRedirect(Request request) {
             return null;
         }
 
         /**
          * Process a deloyment
          *
          * @param newName
          * @param inputStream
          * @param length
          * @param contentType
          * @return
          * @throws IOException
          * @throws ConflictException
          * @throws NotAuthorizedException
          * @throws BadRequestException
          */
         @Override
         public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
             File zippedFile = File.createTempFile("clyde", "deploy-" + newName);
             try (FileOutputStream fout = new FileOutputStream(zippedFile)) {
                 BufferedOutputStream bufOut = new BufferedOutputStream(fout);
                 long copied = IOUtils.copyLarge(inputStream, bufOut);
                 if (length != null) {
                     if (copied != length) {
                         throw new RuntimeException("Uploaded file size does not match saved byte size: saved=" + copied + " uploaded=" + length);
                     }
                 }
                 log.info("createNew: wrote " + copied + " bytes to temp file");
                 bufOut.flush();
                 fout.flush();
             } catch (FileNotFoundException e) {
                 throw new RuntimeException(e);
             }
             try {
                 Deployment deployment = deploymentService.deploy(zippedFile, newName, web);
                 _(VfsSession.class).commit();
                 return new DeploymentResource(web, deployment);
             } catch (Exception ex) {
                 throw new RuntimeException(ex);
             }
 
         }
 
         @Override
         public Date getCreateDate() {
             return null;
         }
 
         @Override
         public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
             generateContent(this, out, HttpManager.request().getAbsolutePath());
         }
 
         @Override
         public Long getMaxAgeSeconds(Auth auth) {
             return null;
         }
 
         @Override
         public String getContentType(String accepts) {
             return "text/html";
         }
 
         @Override
         public Long getContentLength() {
             return null;
         }
     }
 
     public class DeploymentResource extends AbstractDeploymentResource implements DeletableResource, PropFindableResource, ReplaceableResource, GetableResource {
 
         private final Web web;
         private final Deployment deployment;
 
         public DeploymentResource(Web web, Deployment deployment) {
             super(web);
             this.web = web;
             this.deployment = deployment;
         }
 
         @Override
         public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
            log.info("delete: " + getName());
             try {
                deploymentService.undeploy(web, getName());
                 _(VfsSession.class).commit();
             } catch (Exception ex) {
                 _(VfsSession.class).rollback();
             }
         }
 
         @Override
         public String getUniqueId() {
             return deployment.getId().toString();
         }
 
         @Override
         public String getName() {
             return deployment.getName();
         }
 
         @Override
         public Date getModifiedDate() {
             return deployment.getModifiedDate();
         }
 
         @Override
         public String checkRedirect(Request request) {
             return null;
         }
 
         @Override
         public Date getCreateDate() {
             return deployment.getCreatedDate();
         }
 
         @Override
         public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
             try {
                 File zippedFile = File.createTempFile("clyde", "deploy-" + getName());
                 try (FileOutputStream fout = new FileOutputStream(zippedFile)) {
                     BufferedOutputStream bufOut = new BufferedOutputStream(fout);
                     IOUtils.copyLarge(in, bufOut);
                     bufOut.flush();
                     fout.flush();
                     try {
                         deploymentService.deploy(zippedFile, getName(), web);
                         _(VfsSession.class).commit();
                     } catch (Exception ex) {
                         throw new RuntimeException(ex);
                     }
                 } catch (FileNotFoundException e) {
                     throw new RuntimeException(e);
                 }
 
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
 
         }
 
         @Override
         public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
             generateContent(deployment, out, HttpManager.request().getAbsolutePath());
         }
 
         @Override
         public Long getMaxAgeSeconds(Auth auth) {
             return null;
         }
 
         @Override
         public String getContentType(String accepts) {
             return "text/html";
         }
 
         @Override
         public Long getContentLength() {
             return null;
         }
     }
 
     public abstract class AbstractDeploymentResource implements Resource, DigestResource {
 
         private final Web web;
 
         public AbstractDeploymentResource(Web web) {
             this.web = web;
         }
 
         @Override
         public Object authenticate(String user, String password) {
             return web.authenticate(user, password);
         }
 
         @Override
         public boolean authorise(Request request, Method method, Auth auth) {
             return web.authorise(request, Method.DELETE, auth);
         }
 
         @Override
         public Object authenticate(DigestResponse digestRequest) {
             return web.authenticate(digestRequest);
         }
 
         @Override
         public boolean isDigestAllowed() {
             return web.isDigestAllowed();
         }
 
         @Override
         public String getRealm() {
             return web.getRealm();
         }
     }
 
     public String getDeployFolderName() {
         return deployFolderName;
     }
 
     public void setDeployFolderName(String deployFolderName) {
         this.deployFolderName = deployFolderName;
     }
 
     public void generateContent(CollectionResource folder, OutputStream out, String uri) throws NotAuthorizedException, BadRequestException {
         XmlWriter w = new XmlWriter(out);
         w.open("html");
         w.open("head");
         w.close("head");
         w.open("body");
         w.begin("h1").open().writeText(folder.getName()).close();
         w.open("table");
         for (Resource r : folder.getChildren()) {
             w.open("tr");
 
             w.open("td");
             String path = buildHref(uri, r.getName());
             w.begin("a").writeAtt("href", path).open().writeText(r.getName()).close();
 
             //w.begin("a").writeAtt("href", "#").writeAtt("onclick", "editDocument('" + path + "')").open().writeText("(edit with office)").close();
 
             w.close("td");
 
             w.begin("td").open().writeText(r.getModifiedDate() + "").close();
             w.close("tr");
         }
         w.close("table");
         w.close("body");
         w.close("html");
         w.flush();
     }
 
     private void generateContent(Deployment deployment, OutputStream out, String absolutePath) {
         XmlWriter w = new XmlWriter(out);
         w.open("html");
         w.open("head");
         w.close("head");
         w.open("body");
         w.begin("h1").open().writeText(deployment.getName()).close();
         w.begin("p").open().writeText("items: " + deployment.getItems().size()).close();
         w.open("table");
         w.open("tr");
         w.begin("th").open().writeText("Path").close();
         w.begin("th").open().writeText("Class").close();
         w.begin("th").open().writeText("Size").close();
         w.begin("th").open().writeText("Is created?").close();
         w.close("tr");
         for (DeploymentItem r : deployment.getItems()) {
             w.open("tr");
             w.begin("td").open().writeText(r.getPath()).close();
             w.begin("td").open().writeText(r.getClazz()).close();
             w.begin("td").open().writeText(r.getSize() + "").close();
             w.begin("td").open().writeText(r.isCreated() + "").close();
             w.close("tr");
         }
         w.close("table");
         w.close("body");
         w.close("html");
         w.flush();
     }
 
     private String buildHref(String uri, String name) {
         String abUrl = uri;
         if (!abUrl.endsWith("/")) {
             abUrl += "/";
         }
         return abUrl + name;
     }
 }
