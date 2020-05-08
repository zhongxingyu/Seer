 package org.benjp.documents.portlet.list;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.services.jcr.ext.common.SessionProvider;
 import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
 
 import javax.jcr.Node;
 import javax.jcr.Session;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 
 public class UploadServlet extends HttpServlet
 {
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
     doPost(request, response);
   }
 
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
 
     String appContext = request.getHeader("app-context");
     String path = request.getHeader("app-filter");
     boolean isPrivateContext = "Personal".equals(appContext);
     String name = (isPrivateContext)?request.getRemoteUser():request.getHeader("app-space");
     String uuid = null;
 
     try
     {
       List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
       for (FileItem item : items)
       {
         if (item.isFormField())
         {
           String fieldName = item.getFieldName();
           if ("app-context".equals(fieldName))
           {
             appContext = item.getString();
             isPrivateContext = "Personal".equals(appContext);
           }
           if ("app-space".equals(fieldName))
           {
             name = (isPrivateContext)?request.getRemoteUser():item.getString();
           }
           if ("data-uuid".equals(fieldName)) uuid = item.getString();
 
         }
         if (item.getFieldName().equals("pic"))
         {
 //          System.out.println("\n#########################");
 //          System.out.println("######### UPLOAD ########");
 //          System.out.println("# name :: "+name);
 //          System.out.println("# isPrivate :: "+isPrivateContext);
 //          System.out.println("# uuid :: "+uuid);
 //          System.out.println("# item value :: "+item.getFieldName());
 
           if (uuid!=null)
           {
             storeFile(path, item, name, isPrivateContext, uuid);
             response.setContentType("text/html");
             response.setCharacterEncoding("UTF-8");
             response.getWriter().write("<div style='background-color:#ffa; padding:20px'>File has been uploaded successfully!</div>");
             return;
           }
           else
           {
             storeFile(path, item, name, isPrivateContext);
             response.setContentType("application/json");
             response.setCharacterEncoding("UTF-8");
             response.getWriter().write("{\"status\":\"File has been uploaded successfully!\"}");
             return;
           }
         }
       }
     }
     catch (FileUploadException e)
     {
       throw new ServletException("Parsing file upload failed.", e);
     }
   }
 
   private void storeFile(String path, FileItem item, String name, boolean isPrivateContext)
   {
     storeFile(path, item, name, isPrivateContext, null);
   }
 
    private void storeFile(String path, FileItem item, String name, boolean isPrivateContext, String uuid)
   {
     String filename = FilenameUtils.getName(item.getName());
     RepositoryService repositoryService = (RepositoryService)PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
     NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)PortalContainer.getInstance().getComponentInstanceOfType(NodeHierarchyCreator.class);
 
 
     SessionProvider sessionProvider = SessionProvider.createSystemProvider();
     try
     {
       //get info
       Session session = sessionProvider.getSession("collaboration", repositoryService.getCurrentRepository());
 
       Node homeNode;
 
       if (isPrivateContext)
       {
         Node userNode = nodeHierarchyCreator.getUserNode(sessionProvider, name);
         homeNode = userNode.getNode("Private");
       }
       else
       {
         Node rootNode = session.getRootNode();
         homeNode = rootNode.getNode(getSpacePath(name));
       }
 
       Node docNode = homeNode.getNode("Documents");
       if (isImage(filename))
       {
         if (!homeNode.hasNode("Pictures")) {
           homeNode.addNode("Pictures", "nt:folder");
           homeNode.save();
         }
         docNode = homeNode.getNode("Pictures");
       }
       if (path.contains("/") && !path.startsWith("Folksonomy/")) {
         docNode = homeNode.getNode(path);
       }
 
 //      System.out.println("# docNode :: "+docNode.getPath());
 
 
       if (!docNode.hasNode(filename) && uuid==null)
       {
         Node fileNode = docNode.addNode(filename, "nt:file");
         Node jcrContent = fileNode.addNode("jcr:content", "nt:resource");
         jcrContent.setProperty("jcr:data", item.getInputStream());
         jcrContent.setProperty("jcr:lastModified", Calendar.getInstance());
         jcrContent.setProperty("jcr:encoding", "UTF-8");
         if (filename.endsWith(".jpg"))
           jcrContent.setProperty("jcr:mimeType", "image/jpeg");
         else if (filename.endsWith(".png"))
           jcrContent.setProperty("jcr:mimeType", "image/png");
         else if (filename.endsWith(".pdf"))
           jcrContent.setProperty("jcr:mimeType", "application/pdf");
         else if (filename.endsWith(".doc"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.ms-word");
         else if (filename.endsWith(".xls"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.ms-excel");
         else if (filename.endsWith(".ppt"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.ms-powerpoint");
         else if (filename.endsWith(".docx"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
         else if (filename.endsWith(".xlsx"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
         else if (filename.endsWith(".pptx"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
         else if (filename.endsWith(".odp"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.oasis.opendocument.presentation");
         else if (filename.endsWith(".odt"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.oasis.opendocument.text");
         else if (filename.endsWith(".ods"))
           jcrContent.setProperty("jcr:mimeType", "application/vnd.oasis.opendocument.spreadsheet");
         docNode.save();
         session.save();
         uuid = fileNode.getUUID();
       }
       else
       {
         Node fileNode=null;
         if (uuid!=null) {
           fileNode = session.getNodeByUUID(uuid);
         }
         else
         {
           fileNode = docNode.getNode(filename);
         }
         if (fileNode.canAddMixin("mix:versionable")) fileNode.addMixin("mix:versionable");
         if (!fileNode.isCheckedOut()) {
           fileNode.checkout();
         }
         fileNode.save();
         fileNode.checkin();
         fileNode.checkout();
         Node jcrContent = fileNode.getNode("jcr:content");
         jcrContent.setProperty("jcr:data", item.getInputStream());
         session.save();
       }
 
       if (path.startsWith("Folksonomy/")) {
         if (uuid!=null)
         {
           //Node fileNode = session.getNodeByUUID(uuid);
           Node tagNode = homeNode.getNode(path);
           Node linkNode = tagNode.addNode(filename, "exo:symlink");
           linkNode.setProperty("exo:uuid", uuid);
           linkNode.setProperty("exo:workspace", "collaboration");
           linkNode.setProperty("exo:primaryType", "nt:file");
           tagNode.save();
           session.save();
 
         }
       }
 
     }
     catch (Exception e)
     {
       System.out.println("JCR::" + e.getMessage());
 //      e.printStackTrace();
     }
     finally
     {
       sessionProvider.close();
     }
   }
 
   private boolean isImage(String filename)
   {
     if (filename.endsWith(".jpg") || filename.endsWith(".png"))
       return true;
     return false;
   }
 
   private static String getSpacePath(String space)
   {
     return "Groups/spaces/"+space;
   }
 
 }
