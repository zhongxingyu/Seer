 package documents.portlet.list.controllers;
 
 
 import documents.portlet.list.bean.File;
 import documents.portlet.list.controllers.validator.NameValidator;
 import juzu.SessionScoped;
 import org.exoplatform.portal.application.PortalRequestContext;
 import org.exoplatform.portal.webui.util.Util;
 import org.exoplatform.services.cms.folksonomy.NewFolksonomyService;
 import org.exoplatform.services.cms.link.LinkManager;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.services.jcr.ext.common.SessionProvider;
 import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Session;
 import javax.servlet.http.HttpServletRequest;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 @Named("documentsData")
 @SessionScoped
 public class DocumentsData {
 
   RepositoryService repositoryService_;
 
   NewFolksonomyService newFolksonomyService_;
 
   NodeHierarchyCreator nodeHierarchyCreator_;
 
   LinkManager linkManager_;
 
   public static final String TYPE_DOCUMENT="Documents";
   public static final String TYPE_IMAGE="Pictures";
 
   @Inject
   public DocumentsData(RepositoryService repositoryService, NodeHierarchyCreator nodeHierarchyCreator, NewFolksonomyService newFolksonomyService, LinkManager linkManager)
   {
     repositoryService_ = repositoryService;
     nodeHierarchyCreator_= nodeHierarchyCreator;
     newFolksonomyService_ = newFolksonomyService;
     linkManager_ = linkManager;
   }
 
 
   protected List<File> getNodes(String filter)
   {
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       //get info
       Session session = sessionProvider.getSession("collaboration", repositoryService_.getCurrentRepository());
 
 
       Node rootNode = session.getRootNode();
       String space = getSpaceName();
       String path = (space!=null)?getSpacePath(space):getUserPrivatePath();
 
       if (space != null && filter.startsWith("Folksonomy/"))
       {
         filter = filter.replace("Folksonomy/", "ApplicationData/Tags/");
       }
 
       if (!rootNode.hasNode(path+"/"+filter)) {
         Node parentNode = rootNode.getNode(path);
         parentNode.addNode(filter, "nt:folder");
         parentNode.save();
       }
 
       Node docNode = rootNode.getNode(path+"/"+filter);
 
       NodeIterator nodes = docNode.getNodes();
       List<File> files = new ArrayList<File>();
       while (nodes.hasNext())
       {
         Node node = nodes.nextNode();
         node = getTargetNode(node);
         if (isAcceptedFile(node.getName()))
         {
           File file = new File();
           //set name
           file.setName(node.getName());
           //set uuid
           file.setUuid(node.getUUID());
           // set creted date
           file.setCreatedDate(node.getProperty("exo:dateCreated").getDate());
           //set file size
           if (node.hasNode("jcr:content")) {
             Node contentNode = node.getNode("jcr:content");
             if (contentNode.hasProperty("jcr:data")) {
               double size = contentNode.getProperty("jcr:data").getLength();
               String fileSize = calculateFileSize(size);
               file.setSize(fileSize);
             }
           }
           // set versions
           String version = "";
           if (node.isNodeType("mix:versionable"))
           {
            version = (node.getVersionHistory().getAllVersions().getSize()==0)?"":"V"+node.getVersionHistory().getAllVersions().getSize();
           }
           file.setVersion(version);
           // set path
           file.setPath(node.getPath());
           // set public url
           HttpServletRequest request = Util.getPortalRequestContext().getRequest();
           String baseURI = request.getScheme() + "://" + request.getServerName() + ":"
                   + String.format("%s", request.getServerPort());
 
           String url = baseURI+"/documents/file/"+Util.getPortalRequestContext().getRemoteUser()+"/"+file.getUuid()+"/"+file.getName();
           file.setPublicUrl(url);
 
           //set tags
           List<Node> tags;
           if (space!=null)
           {
             tags = newFolksonomyService_.getLinkedTagsOfDocumentByScope(NewFolksonomyService.GROUP,
                     "/spaces/"+space,
                     node, "collaboration");
           }
           else
           {
             tags = newFolksonomyService_.getLinkedTagsOfDocumentByScope(NewFolksonomyService.PRIVATE,
                     Util.getPortalRequestContext().getRemoteUser(),
                     node, "collaboration");
           }
 
 
           List<String> stags = new ArrayList<String>();
           if (tags!=null && tags.size()>0)
           {
 
             for (Node tag:tags)
             {
               stags.add(tag.getName());
             }
           }
           file.setTags(stags);
 
           files.add(file);
         }
       }
 
       return files;
 
     }
     catch (Exception e)
     {
       System.out.println("JCR::\n" + e.getMessage());
     }
     finally
     {
       sessionProvider.close();
     }
     return null;
   }
 
   private Node getTargetNode(Node showingNode) throws Exception {
     Node targetNode = null;
     if (linkManager_.isLink(showingNode)) {
       try {
         targetNode = linkManager_.getTarget(showingNode);
       } catch (ItemNotFoundException e) {
         targetNode = showingNode;
       }
     } else {
       targetNode = showingNode;
     }
     return targetNode;
   }
 
 
   protected void deleteFile(String uuid) throws Exception
   {
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       Session session = sessionProvider.getSession("collaboration", repositoryService_.getCurrentRepository());
       session.getNodeByUUID(uuid).remove();
       session.save();
     }
     finally
     {
       sessionProvider.close();
     }
 
   }
 
   protected void renameFile(String uuid, String name) throws Exception
   {
     NameValidator.validateName(name);
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       Session session = sessionProvider.getSession("collaboration", repositoryService_.getCurrentRepository());
       Node node = session.getNodeByUUID(uuid);
       String extension = node.getName().substring(node.getName().lastIndexOf("."));
       StringBuilder newPath = new StringBuilder(node.getParent().getPath()).append('/')
               .append(name).append(extension);
       session.move(node.getPath(), newPath.toString());
       session.save();
     }
     finally
     {
       sessionProvider.close();
     }
 
   }
 
   protected void editTags(String uuid, String tags) throws Exception
   {
     String tagsPath = "/"+getUserPrivatePath()+"/Folksonomy/";
 
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       Session session = sessionProvider.getSession("collaboration", repositoryService_.getCurrentRepository());
       Node node = session.getNodeByUUID(uuid);
 
       /**
        * TODO : Optimize this, remove existing if not in new list, add only if new
        * */
       List<Node> tagsNodes;
       String space = getSpaceName();
       if (space!=null)
       {
         tagsNodes = newFolksonomyService_.getLinkedTagsOfDocumentByScope(NewFolksonomyService.GROUP,
                 "/spaces/"+space,
                 node, "collaboration");
       }
       else
       {
         tagsNodes = newFolksonomyService_.getLinkedTagsOfDocumentByScope(NewFolksonomyService.PRIVATE,
                 Util.getPortalRequestContext().getRemoteUser(),
                 node, "collaboration");
       }
       if (tagsNodes!=null && tagsNodes.size()>0)
       {
         for (Node tag:tagsNodes)
         {
           newFolksonomyService_.removeTagOfDocument(tagsPath+tag.getName(), node, "collaboration");
         }
       }
 
       if (tags!=null && !"".equals(tags))
       {
         String[] atags = tags.replaceAll(" ", "").toLowerCase().split(",");
         if (space!=null)
         {
           newFolksonomyService_.addGroupsTag(atags, node, "collaboration", new String[]{"/spaces/"+space});
         }
         else
         {
           newFolksonomyService_.addPrivateTag(atags, node, "collaboration", Util.getPortalRequestContext().getRemoteUser());
         }
 
         session.save();
       }
     }
     finally
     {
       sessionProvider.close();
     }
 
   }
 
   private String getUserPrivatePath()
   {
     String userName = Util.getPortalRequestContext().getRemoteUser();
 
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       Node userNode = nodeHierarchyCreator_.getUserNode(sessionProvider, userName);
       return userNode.getPath().substring(1)+"/Private";
     }
     catch (Exception e)
     {
       System.out.println("JCR::" + e.getMessage());
     }
     finally
     {
       sessionProvider.close();
     }
 
     return null;
   }
 
   private static String getSpacePath(String space)
   {
     return "Groups/spaces/"+space;
   }
 
   public static String calculateFileSize(double fileLengthLong) {
     int fileLengthDigitCount = Double.toString(fileLengthLong).length();
     double fileSizeKB = 0.0;
     String howBig = "";
     if (fileLengthDigitCount < 5) {
       fileSizeKB = Math.abs(fileLengthLong);
       howBig = "Byte(s)";
     } else if (fileLengthDigitCount >= 5 && fileLengthDigitCount <= 6) {
       fileSizeKB = Math.abs((fileLengthLong / 1024));
       howBig = "KB";
     } else if (fileLengthDigitCount >= 7 && fileLengthDigitCount <= 9) {
       fileSizeKB = Math.abs(fileLengthLong / (1024 * 1024));
       howBig = "MB";
     } else if (fileLengthDigitCount > 9) {
       fileSizeKB = Math.abs((fileLengthLong / (1024 * 1024 * 1024)));
       howBig = "GB";
     }
     String finalResult = roundTwoDecimals(fileSizeKB);
     return finalResult + " " + howBig;
   }
 
   public String getSpaceName()
   {
     PortalRequestContext portalRequestContext = PortalRequestContext.getCurrentInstance();
     // /portal/g/:spaces:espace_rh/espace_rh/DocumentsListApplication
     String uri = portalRequestContext.getRequest().getRequestURI();
 
     // /Groups/spaces/espace_rh/Documents
 
     int io = uri.indexOf(":spaces:");
     if (io==-1) return null;
 
     String suri = uri.substring(io+8);
     suri = suri.substring(0, suri.indexOf("/"));
 
     return suri;
   }
 
 
 
 
   private static String roundTwoDecimals(double d) {
     DecimalFormat twoDForm = new DecimalFormat("#.##");
     return twoDForm.format(d);
   }
 
   private boolean isAcceptedFile(String filename)
   {
     if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".pdf"))
       return true;
     return false;
   }
 
 
 }
