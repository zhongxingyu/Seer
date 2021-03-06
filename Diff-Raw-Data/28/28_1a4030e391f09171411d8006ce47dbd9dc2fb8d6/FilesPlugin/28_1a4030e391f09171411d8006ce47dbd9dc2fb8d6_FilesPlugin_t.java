 package de.deepamehta.plugins.files;
 
 import de.deepamehta.plugins.files.service.FilesService;
 import de.deepamehta.plugins.proxy.service.ProxyService;
 
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.model.AssociationModel;
 import de.deepamehta.core.model.CompositeValue;
 import de.deepamehta.core.model.SimpleValue;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.model.TopicRoleModel;
 import de.deepamehta.core.service.Plugin;
 import de.deepamehta.core.service.PluginService;
 import de.deepamehta.core.util.JavaUtils;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.util.logging.Logger;
 
 
 
 @Path("/")
 @Produces("application/json")
 public class FilesPlugin extends Plugin implements FilesService {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private ProxyService proxyService;
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // **************************************************
     // *** Core Hooks (called from DeepaMehta 4 Core) ***
     // **************************************************
 
 
 
     @Override
     public void serviceArrived(PluginService service) {
         logger.info("########## Service arrived: " + service);
         if (service instanceof ProxyService) {
             proxyService = (ProxyService) service;
         }
     }
 
     @Override
     public void serviceGone(PluginService service) {
         logger.info("########## Service gone: " + service);
         if (service == proxyService) {
             proxyService = null;
         }
     }
 
 
 
     // **********************
     // *** Plugin Service ***
     // **********************
 
 
 
     @POST
    @Path("/file/{path:.+}")       // Note: we also match slashes as they are already decoded by an apache reverse proxy
     @Override
     public Topic createFileTopic(@PathParam("path") String path) {
         String text = "Creating file topic for path \"" + path + "\"";
         try {
             Topic fileTopic = getFileTopic(path);
             if (fileTopic != null) {
                 logger.info(text + " ABORTED -- already exists");
                 return fileTopic;
             }
             logger.info(text);
             //
             File file = proxyService.locateFile(path);
             String fileName = file.getName();
             String fileType = JavaUtils.getFileType(fileName);
             long fileSize = file.length();
             //
             CompositeValue comp = new CompositeValue();
             comp.put("dm4.files.file_name", fileName);
             comp.put("dm4.files.path", path);
             if (fileType != null) {
                 comp.put("dm4.files.media_type", fileType);
             }
             comp.put("dm4.files.size", fileSize);
             //
             return dms.createTopic(new TopicModel("dm4.files.file", comp), null);       // clientContext=null
         } catch (Throwable e) {
             throw new RuntimeException(text + " failed", e);
         }
     }
 
     @POST
    @Path("/folder/{path:.+}")     // Note: we also match slashes as they are already decoded by an apache reverse proxy
     @Override
     public Topic createFolderTopic(@PathParam("path") String path) {
         String text = "Creating folder topic for path \"" + path + "\"";
         try {
             Topic folderTopic = getFolderTopic(path);
             if (folderTopic != null) {
                 logger.info(text + " ABORTED -- already exists");
                 return folderTopic;
             }
             logger.info(text);
             //
             CompositeValue comp = new CompositeValue();
             comp.put("dm4.files.folder_name", new File(path).getName());
             comp.put("dm4.files.path", path);
             //
             return dms.createTopic(new TopicModel("dm4.files.folder", comp), null);     // clientContext=null
         } catch (Throwable e) {
             throw new RuntimeException(text + " failed", e);
         }
     }
 
     // ---
 
     @POST
    @Path("/{id}/file/{path:.+}")  // Note: we also match slashes as they are already decoded by an apache reverse proxy
     @Override
     public Topic createChildFileTopic(@PathParam("id") long folderTopicId, @PathParam("path") String path) {
         Topic childTopic = createFileTopic(path);
         associateChildTopic(folderTopicId, childTopic.getId());
         return childTopic;
     }
 
     @POST
    @Path("/{id}/folder/{path:.+}")// Note: we also match slashes as they are already decoded by an apache reverse proxy
     @Override
     public Topic createChildFolderTopic(@PathParam("id") long folderTopicId, @PathParam("path") String path) {
         Topic childTopic = createFolderTopic(path);
         associateChildTopic(folderTopicId, childTopic.getId());
         return childTopic;
     }
 
     // ---
 
     // ### FIXME: Jersey WARNING: A HTTP GET method MUST return a non-void type
     @GET
     @Path("/{id}")
     @Override
     public void openFile(@PathParam("id") long fileTopicId) {
         File file = null;
         try {
             Topic fileTopic = dms.getTopic(fileTopicId, true, null);    // fetchComposite=true, clientContext=null
             String path = fileTopic.getCompositeValue().get("dm4.files.path").toString();
             file = proxyService.locateFile(path);
             logger.info("### Opening file \"" + file + "\"");
             Desktop.getDesktop().open(file);
         } catch (Throwable e) {
             throw new RuntimeException("Opening file \"" + file + "\" failed", e);
         }
     }
 
 
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
     private Topic getFileTopic(String path) {
         Topic topic = dms.getTopic("dm4.files.path", new SimpleValue(path), false);     // fetchComposite=false
         if (topic != null) {
             return topic.getRelatedTopic("dm4.core.composition", "dm4.core.part", "dm4.core.whole", "dm4.files.file",
                 true, false);
         }
         return null;
     }
 
     private Topic getFolderTopic(String path) {
         Topic topic = dms.getTopic("dm4.files.path", new SimpleValue(path), false);     // fetchComposite=false
         if (topic != null) {
             return topic.getRelatedTopic("dm4.core.composition", "dm4.core.part", "dm4.core.whole", "dm4.files.folder",
                 true, false);
         }
         return null;
     }
 
     // ---
 
     private void associateChildTopic(long folderTopicId, long childTopicId) {
         if (!childAssociationExists(folderTopicId, childTopicId)) {
             dms.createAssociation(new AssociationModel("dm4.core.aggregation",
                 new TopicRoleModel(folderTopicId, "dm4.core.whole"),
                 new TopicRoleModel(childTopicId,  "dm4.core.part")), null);    // clientContext=null
         }
     }
 
     private boolean childAssociationExists(long folderTopicId, long childTopicId) {
         return dms.getAssociations(folderTopicId, childTopicId, "dm4.core.aggregation").size() > 0;
     }
 }
