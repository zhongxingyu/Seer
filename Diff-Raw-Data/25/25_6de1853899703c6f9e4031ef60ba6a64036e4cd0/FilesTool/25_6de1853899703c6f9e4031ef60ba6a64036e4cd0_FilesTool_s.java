 package net.cyklotron.cms.files;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.tools.LinkTool;
 
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  * A context tool used for files application.
  *
  * @author <a href="mailto:pablo@ngo.pl">Pawel Potempski</a>
 * @version $Id: FilesTool.java,v 1.7 2005-02-21 16:28:39 zwierzem Exp $
  */
 public class FilesTool
 {
     /** logging service */
     private Logger log;
 
     /** cms files service */
     private FilesService filesService;
     
     private Context context;
     // initialization ////////////////////////////////////////////////////////
 
     public FilesTool(Context context, Logger logger, FilesService filesService)
     {
         this.context = context;
         this.log = logger;
         this.filesService = filesService;
     }
     
     // public interface ///////////////////////////////////////////////////////
     
     /**
      * Get the main files node for the site
      * 
      * @param site the site resource.
      * @return the root directory.
      */
     public Resource getFilesRoot(SiteResource site)
         throws FilesException
     {
         return filesService.getFilesRoot(getCoralSession(context), site);
     }
     
     /**
      * Checks whether the resource is directory.
      *
      * @param resource the resource.
      * @return <code>true</code> if resource is the directory.
      */
     public boolean isDirectory(Resource resource)
     {
         return (resource instanceof DirectoryResource);
     }
 
     /**
      * Checks whether the resource is root directory.
      *
      * @param resource the resource.
      * @return <code>true</code> if resource is the root directory.
      */
     public boolean isRootDirectory(Resource resource)
     {
         return (resource instanceof RootDirectoryResource);
     }
 
     /**
      * Checks whether the resource is ordinary directory.
      *
      * @param resource the resource.
      * @return <code>true</code> if resource is the ordinary directory.
      */
     public boolean isOrdinaryDirectory(Resource resource)
     {
         return (isDirectory(resource) && !isRootDirectory(resource));
     }
 
     /**
      * Checks whether the resource is files map class.
      *
      * @param resource the resource.
      * @return <code>true</code> if resource is the files map.
      */
     public boolean isFilesMap(Resource resource)
     {
         return (resource instanceof FilesMapResource);
     }
 
     /**
      * Checks whether resource is file.
      *
      * @param resource the resource.
      * @return <code>true</code> if resource is the file.
      */
     public boolean isFile(Resource resource)
     {
         return (resource instanceof FileResource);
     }
 
     /**
      * gets the pathlist of all nodes in between two nodes.
      *
      * @param root the root node.
      * @param leaf the leaf.
      * @return list of resources.
      */
     public List getPath(Resource root, Resource leaf)
     {
         List list = new ArrayList();
         for(Resource parent = leaf; parent!=null; parent = parent.getParent())
         {
             list.add(parent);
             if(parent.equals(root))
             {
                 break;
             }
         }
         Collections.reverse(list);
         return list;
     }
     
     /**
      * get the link to the file resource.
      *
      * @param resource the file resource.
      * @return the link to the file.
      */
     public String getLink(Resource resource)
         throws FilesException
     {
         return getLink(resource, false);
     }
 
     /**
      * get the link to the file resource.
      *
      * @param resource the file resource.
      * @return the link to the file.
      */
     public String getAbsoluteLink(Resource resource)
         throws FilesException
     {
         return getLink(resource, true);
     }
 
 
     private String getLink(Resource resource, boolean absolute)
         throws FilesException
     {
         HttpContext httpContext = HttpContext.getHttpContext(context);
         if(!(resource instanceof FileResource))
         {
             throw new FilesException("Resource is not the instance of cms.files.file class");
         }
         FileResource file = (FileResource)resource;
         RootDirectoryResource rootDirectory = null;
         for(Resource parent = file.getParent(); parent != null; parent = parent.getParent())
         {
             if(parent instanceof RootDirectoryResource)
             {
                 rootDirectory = ((RootDirectoryResource)parent);
                 break;
             }
         }
         if(rootDirectory.getExternal())
         {
             String path = "";
             for(Resource parent = file; parent != null; parent = parent.getParent())
             {
                 if(parent instanceof RootDirectoryResource)
                 {
                     break;
                 }
                 else
                 {
                     try
                     {
                         path = "/"+URLEncoder.encode(parent.getName(), "UTF-8")+path;
                     }
                     catch(UnsupportedEncodingException e)
                     {
                         throw new FilesException("really should not happen", e);
                     }
                 }
             }
             //path = URLEncoder.encode(path);
             
             StringBuilder sb = new StringBuilder();
             sb.append("http://");
             sb.append(httpContext.getRequest().getServerName());
             if(httpContext.getRequest().getServerPort() != 80)
             {
                 sb.append(":");
                 sb.append(httpContext.getRequest().getServerPort());
             }
             sb.append(httpContext.getRequest().getContextPath());
             sb.append("/files/");
             sb.append(rootDirectory.getParent().getParent().getName());
             sb.append("/");
             sb.append(rootDirectory.getName());
             sb.append(path);
             return sb.toString();
         }
         else
         {
             String path = "";
             for(Resource parent = file; parent != null; parent = parent.getParent())
             {
                 if(parent instanceof RootDirectoryResource)
                 {
                     break;
                 }
                 else
                 {
                     path = ","+parent.getName()+path;
                 }
             }
             path = "/"+rootDirectory.getName()+path;
 
             TemplatingContext tContext = (TemplatingContext)
                 context.getAttribute(TemplatingContext.class);
             LinkTool link = (LinkTool)tContext.get("link");
             if(absolute)
             {
                 link = link.absolute();
             }
            link = link.sessionless().view("files,Download").set("path",path).set("file_id",file.getIdString());
             return link.toString();
         }
     }
     
     private CoralSession getCoralSession(Context context)
     {
         return (CoralSession)context.getAttribute(CoralSession.class);
     }
 }
 
