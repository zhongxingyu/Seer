 package com.ettrema.web.manage.deploy;
 
 import com.bradmcevoy.http.Resource;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.ettrema.logging.LogUtils;
 import com.ettrema.utils.ClydeUtils;
 import com.ettrema.vfs.DataNode;
 import com.ettrema.vfs.EmptyDataNode;
 import com.ettrema.vfs.NameNode;
 import com.ettrema.web.BaseResource;
 import com.ettrema.web.BinaryFile;
 import com.ettrema.web.Folder;
 import com.ettrema.web.Web;
 import com.ettrema.web.code.AbstractCodeResource;
 import com.ettrema.web.code.CodeResourceFactory;
 import com.ettrema.web.manage.synch.DirectFileTransport;
 import com.ettrema.web.manage.synch.FileLoader;
 import com.ettrema.web.manage.synch.FileScanner;
 import java.io.File;
 import java.util.*;
 
 /**
  *
  * @author brad
  */
 public class DeploymentService {
 
     private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeploymentService.class);
     public static final String DEPLOYMENTS_NODE_NAME = "_sys_deployments";
     private final ZipService zipService;
     private final CodeResourceFactory codeResourceFactory;
 
     public DeploymentService(ZipService zipService, CodeResourceFactory codeResourceFactory) {
         this.zipService = zipService;
         this.codeResourceFactory = codeResourceFactory;
     }
 
     public List<Deployment> getDeployments(Web web) {
         NameNode nn = web.getNameNode().child(DEPLOYMENTS_NODE_NAME);
         if (nn == null) {
             LogUtils.trace(log, "getDeployments: no deployments node");
             return Collections.EMPTY_LIST;
         }
         List<Deployment> list = new ArrayList<>();
         for (NameNode nnDep : nn.children()) {
             DataNode dn = nnDep.getData();
             if (dn instanceof Deployment) {
                 list.add((Deployment) dn);
             }
         }
         LogUtils.trace(log, "getDeployments: children:", list.size());
         return list;
     }
 
     public Deployment deploy(File war, String name, Web web) throws Exception {
 
         Deployment previousDeployment = getPreviousDeployment(web, name);
         if (previousDeployment != null) {
             undeploy(previousDeployment);
         }
 
         File tmp = zipService.getTempDir();
         File unzipped = new File(tmp, name);
         if (unzipped.exists()) {
             if (!delete(unzipped)) {
                 throw new RuntimeException("Couldnt delete previous deployment temp dir: " + unzipped.getAbsolutePath());
             }
         }
         if (!unzipped.mkdirs()) {
             throw new RuntimeException("Couldnt create temporary deployment directory: " + unzipped.getAbsolutePath());
         }
 
         zipService.unzip(war, unzipped);
         File autoload = new File(unzipped, "autoload");
 
         String hostName = web.getHost().getName();
         DeploymentFileLoadCallBack callBack = new DeploymentFileLoadCallBack();
         DirectFileTransport fileTransport = new DirectFileTransport(hostName, codeResourceFactory, callBack);
         HtmlErrorReporter errorReporter = new HtmlErrorReporter();
         FileLoader fl = new FileLoader(errorReporter, fileTransport);
         // null rootContext means it won't do individual transactions per file
         FileScanner fileScanner = new FileScanner(null, fl);
         fileScanner.initialScan(true, autoload);
 
         List<DeploymentItem> items = callBack.asList();
         LogUtils.trace(log, "deploy: items", items.size());
 
         return save(web, name, items);
 
     }
 
     public void undeploy(Web web, String name) throws Exception {
         Deployment previousDeployment = getPreviousDeployment(web, name);
         if (previousDeployment != null) {
             undeploy(previousDeployment);
         }
        previousDeployment.delete();
     }
 
     private void undeploy(Deployment previousDeployment) throws Exception {
         // First delete any files (not directories) which this deployment created
         for (DeploymentItem item : previousDeployment.getItems()) {
             if (!item.isDirectory() && item.isCreated()) {
                 BaseResource res = ClydeUtils.loadResource(item.getItemId());
                 if (res == null) {
                     LogUtils.trace(log, "undeploy: item not found", item.getItemId());
                 } else {
                     try {
                         LogUtils.trace(log, "undeploy: delete previously deployed item", res.getName());
                         res.deleteNoTx();
                     } catch (NotAuthorizedException | ConflictException | BadRequestException ex) {
                         throw new Exception(ex);
                     }
                 }
             }
         }
         
         // Now remove any empty directories which this deploy created
         for (DeploymentItem item : previousDeployment.getItems()) {
             if (item.isDirectory() && item.isCreated()) {
                 BaseResource res = ClydeUtils.loadResource(item.getItemId());
                 if (res == null) {
                     LogUtils.trace(log, "undeploy: item not found", item.getItemId());
                 } else {
                     try {
                         LogUtils.trace(log, "undeploy: delete previously deployed item", res.getName());
                         res.deleteNoTx();
                     } catch (NotAuthorizedException | ConflictException | BadRequestException ex) {
                         throw new Exception(ex);
                     }
                 }
             }
         }        
     }
 
     private Deployment save(Web web, String name, List<DeploymentItem> items) {
         NameNode nnDeployments = web.getNameNode().child(DEPLOYMENTS_NODE_NAME);
         if (nnDeployments == null) {
             nnDeployments = web.getNameNode().add(DEPLOYMENTS_NODE_NAME, new EmptyDataNode());
             nnDeployments.save();
             LogUtils.trace(log, "save: created deployments node", nnDeployments.getId());
         }
         NameNode nnDeployment = nnDeployments.child(name);
         if (nnDeployment == null) {
             Deployment deployment = new Deployment();
             deployment.setItems(items);
             nnDeployment = nnDeployments.add(name, deployment);
             nnDeployment.save();
             LogUtils.trace(log, "save: created new deployment", name, nnDeployment.getId(), "items", items.size());
 
             return deployment;
         } else {
             Deployment deployment = (Deployment) nnDeployment.getData();
             deployment.setItems(items);
             nnDeployment.save();
             LogUtils.trace(log, "save: updated deployment", name, nnDeployment.getId(), "items", items.size());
             return deployment;
         }
     }
 
     private Deployment getPreviousDeployment(Web web, String name) {
         NameNode nn = web.getNameNode().child(DEPLOYMENTS_NODE_NAME);
         if (nn == null) {
             return null;
         }
         NameNode nnDeployment = nn.child(name);
         if (nnDeployment == null) {
             return null;
         }
         Deployment deployment = (Deployment) nnDeployment.getData();
         return deployment;
     }
 
     private boolean delete(File f) {
         if (f.isDirectory()) {
             File[] children = f.listFiles();
             if (children != null) {
                 for (File child : children) {
                     if (!delete(child)) {
                         return false;
                     }
                 }
             }
         }
         return f.delete();
     }
 
     private class DeploymentFileLoadCallBack implements DirectFileTransport.FileLoadCallback {
 
         private final Set<BaseResource> created = new HashSet<>();
         private final Set<BaseResource> modified = new HashSet<>();
 
         @Override
         public void onLoaded(Resource r) {
             if (r instanceof AbstractCodeResource) {
                 AbstractCodeResource acr = (AbstractCodeResource) r;
                 BaseResource br = (BaseResource) acr.getWrapped();
                 onLoaded(br);
             } else if( r instanceof BaseResource) {
                 BaseResource br = (BaseResource)r;
                 LogUtils.trace(log, "onLoaded", br.getClass());
                 if (modified.contains(br)) {// should be an impossible situation, but check anyway
                     System.out.println("remove from modified for created");
                     modified.remove(br);
                 }
                 created.add(br);                
             }
         }
 
         @Override
         public void onDeleted(Resource r) {
         }
 
         @Override
         public void onModified(Resource r) {
             if (r instanceof AbstractCodeResource) {
                 AbstractCodeResource acr = (AbstractCodeResource) r;
                 BaseResource br = (BaseResource) acr.getWrapped();
                 onModified(br);
             } else if(r instanceof BaseResource) {
                 BaseResource br = (BaseResource)r;
                 LogUtils.trace(log, "onModified", br.getClass());
                 if (!created.contains(br)) { // if already in created, ignore the modified
                     System.out.println("add to modified because not in created");
                     modified.add(br);
                 }
             }
         }
 
         private List<DeploymentItem> asList() {
             List<DeploymentItem> list = new ArrayList<>(created.size());
             addToList(created, list, true);
             addToList(modified, list, false);
             Collections.sort(list);
             return list;
         }
 
         private void addToList(Set<BaseResource> items, List<DeploymentItem> list, boolean created) {
             for (BaseResource br : items) {
                 DeploymentItem d = new DeploymentItem(br.getNameNodeId());
                 d.setClazz(br.getClass().getCanonicalName());
                 d.setPath(br.getUrl());
                 if (br instanceof BinaryFile) {
                     BinaryFile bf = (BinaryFile) br;
                     d.setSize(bf.getContentLength());
                 } else if (br instanceof Folder) {
                     d.setDirectory(true);
                 }
                 d.setCreated(created);
                 list.add(d);
             }
         }
     }
 }
