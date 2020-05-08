 package documents.portlet.list.controllers;
 
 
 import documents.portlet.list.bean.File;
 import juzu.SessionScoped;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.portal.webui.util.Util;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.services.jcr.core.ExtendedNode;
 import org.exoplatform.services.jcr.ext.common.SessionProvider;
 import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Session;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 @Named("documentsData")
 @SessionScoped
 public class DocumentsData {
 
   RepositoryService repositoryService_;
 
   NodeHierarchyCreator nodeHierarchyCreator_;
 
   public static final String TYPE_DOCUMENT="Documents";
   public static final String TYPE_IMAGE="Pictures";
 
   @Inject
   public DocumentsData(RepositoryService repositoryService, NodeHierarchyCreator nodeHierarchyCreator)
   {
     repositoryService_ = repositoryService;
     nodeHierarchyCreator_= nodeHierarchyCreator;
   }
 
 
   protected List<File> getNodes(String type)
   {
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       //get info
       Session session = sessionProvider.getSession("collaboration", repositoryService_.getCurrentRepository());
 
 
       Node rootNode = session.getRootNode();
       Node docNode = rootNode.getNode(getUserPrivatePath()+"/"+type);
 
       NodeIterator nodes = docNode.getNodes();
       List<File> files = new ArrayList<File>();
       while (nodes.hasNext())
       {
         Node node = nodes.nextNode();
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
           // set path
           file.setPath(node.getPath());
           // set public url
          String url = "http://localhost:8080/documents/file/"+Util.getPortalRequestContext().getRemoteUser()+"/"+file.getUuid()+"/"+file.getName();
           file.setPublicUrl(url);
 
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
