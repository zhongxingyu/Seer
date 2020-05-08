 package net.cyklotron.cms.files.internal;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.StringTokenizer;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.imageio.ImageIO;
 
 import org.apache.tika.Tika;
 import org.apache.tika.metadata.Metadata;
 import org.imgscalr.Scalr;
 import org.jcontainer.dna.Configuration;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.mail.MailSystem;
 
 import net.cyklotron.cms.files.DirectoryNotEmptyException;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.DirectoryResourceImpl;
 import net.cyklotron.cms.files.FileAlreadyExistsException;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FileResourceImpl;
 import net.cyklotron.cms.files.FilesException;
 import net.cyklotron.cms.files.FilesMapResource;
 import net.cyklotron.cms.files.FilesMapResourceImpl;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.files.ItemResource;
 import net.cyklotron.cms.files.RootDirectoryResource;
 import net.cyklotron.cms.files.RootDirectoryResourceImpl;
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  * Implementation of Files Service
  * 
  * @author <a href="mailto:publo@caltha.pl">Pawel Potempski </a>
  * @version $Id: FilesServiceImpl.java,v 1.9 2007-11-18 21:23:19 rafal Exp $
  */
 public class FilesServiceImpl
     implements FilesService
 {
     // instance variables ////////////////////////////////////////////////////
 
     private static final String FILES_ROOT_DIR_NAME = "files";
 
     private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
 
     // instance variables ////////////////////////////////////////////////////
     
     private static final String RESIZED_IMAGE_CACHE_DIR = "/files/resized/";
 
     /** logging facility */
     private Logger log;
 
     /** file service */
     private FileSystem fileSystem;
 
     /** mail service */
     private MailSystem mailSystem;
 
     /** protected (internal) default base path */
     private String defaultProtectedPath;
 
     /** public (external) default base path */
     private String defaultPublicPath;
     
     /** Tika facade */
     private final Tika tika;
 
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Initializes the service.
      */
     public FilesServiceImpl(Configuration config, Logger logger, 
         FileSystem fileSystem, MailSystem mailSystem)
     {
         this.log = logger;
         this.fileSystem = fileSystem;
         this.mailSystem = mailSystem;
         
         this.tika = new Tika();
         
         defaultPublicPath = config.getChild("default_public_path").getValue("/files");
         defaultProtectedPath = config.getChild("default_protected_path").getValue("/data/files"); 
     }
 
     /**
      * Return the files root node.
      * 
      * @param site
      *            the site resource.
      * @return the files root resource.
      * @throws FilesException if  the operation fails.
      */
     public FilesMapResource getFilesRoot(CoralSession coralSession, SiteResource site) throws FilesException
     {
         Resource[] roots = coralSession.getStore().getResource(site, FILES_ROOT_DIR_NAME);
         if(roots.length == 1)
         {
             return (FilesMapResource)roots[0];
         }
         if(roots.length == 0)
         {
             try
             {
                 return FilesMapResourceImpl.createFilesMapResource(coralSession, FILES_ROOT_DIR_NAME, site);
             }
             catch(Exception e)
             {
                 e.printStackTrace();
                 throw new FilesException("Couldn't create files root node");
             }
         }
         throw new FilesException("Too much files root resources for site: " + site.getName());
     }
 
     /**
      * Return the files administrator.
      * 
      * @param site
      *            the site resource.
      * @return the files adminstrator role.
      * @throws FilesException if  the operation fails.
      */
     public Role getFilesAdministrator(CoralSession coralSession, SiteResource site) throws FilesException
     {
         return getFilesRoot(coralSession, site).getAdministrator();
     }
 
     /**
      * Create the root directory in site.
      * 
      * @param site
      *            the site resource.
      * @param name
      *            the name of the directory.
      * @param external
      *            the type of the link to the resource.
      * @param path
      *            the base path to the parent directory in file system or <code>null</code> if
      *            default.
      * @return the files root resource.
      * @throws FilesException if  the operation fails.
      */
     public RootDirectoryResource createRootDirectory(CoralSession coralSession, SiteResource site, String name,
         boolean external, String path) throws FilesException
     {
         try
         {
             FilesMapResource parent = getFilesRoot(coralSession, site);
             Resource[] resources = coralSession.getStore().getResource(parent, name);
             if(resources.length > 0)
             {
                 throw new FileAlreadyExistsException("The directory '" + name
                     + "' already exists in site '" + site.getName() + "'");
             }
             String basePath = null;
             if(path != null)
             {
                 basePath = path;
             }
             else
             {
                 if(external)
                 {
                     basePath = defaultPublicPath;
                 }
                 else
                 {
                     basePath = defaultProtectedPath;
                 }
             }
             basePath = basePath + "/" + site.getName() + "/" + name;
             fileSystem.mkdirs(basePath);
             RootDirectoryResource directory = RootDirectoryResourceImpl
                 .createRootDirectoryResource(coralSession, name, parent);
             directory.setRootPath(basePath);
             directory.setExternal(external);
             directory.update();
             return directory;
         }
         catch(Exception e)
         {
             throw new FilesException("Exception occured during creating the directory '" + name
                 + "' in site '" + site.getName() + "' ", e);
         }
     }
     
     /**
      * Get root directory for site if exists.
      * @return the files root resource.
      * @throws FilesException if  the operation fails.
      */
     public RootDirectoryResource getRootDirectoryResource(CoralSession coralSession, SiteResource site) 
                     throws FilesException {
         FilesMapResource parent = getFilesRoot(coralSession, site);
         String name = FILES_ROOT_DIR_NAME;
         Resource[] resources = coralSession.getStore().getResource(parent, name);
         
         if(resources.length == 0)
         {
             throw new FilesException("The directory '" + name
                 + "' appears to not exist for site '" + site.getName() + "'");
         }
         
         return (RootDirectoryResource)resources[0];
     }
 
     /**
      * Create the directory.
      * 
      * @param name
      *            the name of the directory.
      * @param parent
      *            the parent directory.
      * @return the created directory.
      * @throws FilesException if  the operation fails.
      */
    
     public DirectoryResource createDirectory(CoralSession coralSession, String name, DirectoryResource parent)
         throws FilesException
     {
         Resource[] resources = coralSession.getStore().getResource(parent, name);
         if(resources.length > 0)
         {
             throw new FileAlreadyExistsException("The directory '" + name
                 + "' already exists in directory");
         }
         try
         {
             String path = getPath(parent) + "/" + name;
             fileSystem.mkdirs(path);
             DirectoryResource directory = DirectoryResourceImpl.createDirectoryResource(
                 coralSession, name, parent);
             return directory;
         }
         catch(Exception e)
         {
             throw new FilesException("Exception occured during creating the directory '" + name, e);
         }
     }
 
     /**
      * Create the file.
      * 
      * @param name
      *            the name of the file.
      * @param is
      *            the InputStream with file data, or <code>null</code> to create an empty file.
      * @param mimetype
      *            the mimetype of the file.
      * @param encoding
      *            the encoding of the file, or null if unknown.
      * @param parent
      *            the parent directory.
      * @return the created file.
      * @throws FilesException if  the operation fails.
      */
     public FileResource createFile(CoralSession coralSession, String name, InputStream is, String mimetype, String encoding,
         DirectoryResource parent) throws FilesException
     {
         Resource[] resources = coralSession.getStore().getResource(parent, name);
         if(resources.length > 0)
         {
             throw new FileAlreadyExistsException("The file '" + name
                 + "' already exists in directory");
         }
         try
         {
             String path = getPath(parent) + "/" + name;
             boolean notExists = fileSystem.createNewFile(path);
             if(!notExists)
             {
                 throw new FilesException("The file '" + name
                     + "' already exists in directory but the resource is missed");
             }
             if(is != null)
             {
                 fileSystem.write(path, is);
             }
             FileResource file = FileResourceImpl.createFileResource(coralSession, name, parent);
             file.setSize(fileSystem.length(path));
             if(mimetype == null || mimetype.equals("")
                 || mimetype.equals(DEFAULT_MIME_TYPE))
             {
                 mimetype = mailSystem.getContentType(name);
             }
             if(encoding != null && mimetype.startsWith("text/") && mimetype.indexOf("charset") < 0)
             {
                 mimetype = mimetype + ";charset=" + encoding;
             }
             file.setMimetype(mimetype);
             if(encoding != null)
             {
                 file.setEncoding(encoding);
             }
             file.update();
             return file;
         }
         catch(Exception e)
         {
             throw new FilesException("Exception occured during file upload '" + name + "' ", e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void unpackZipFile(CoralSession coralSession, InputStream is, String encoding,
         					  DirectoryResource parent)
     	throws FilesException
     {
         try
         {
             String basePath = getPath(parent)+"/";
             ZipInputStream zis = new ZipInputStream(is);
             ZipEntry ze = zis.getNextEntry();
             while(ze != null)
             {
                 if(!ze.isDirectory())
                 {
                     String path = ze.getName();
                     String parentPath = "";
                     String name = ze.getName();
                     int last = path.lastIndexOf('/');
                     if(last != -1)
                     {
                         name = path.substring(last+1);
                         parentPath = path.substring(0, last);
                     }
                     DirectoryResource dirParent = parent;
                     if(parentPath.length() > 0)
                     {                    
                         StringTokenizer st = new StringTokenizer(parentPath,"/");
                         while(st.hasMoreTokens())
                         {
                             String dirName = st.nextToken();
                             try
                             {
                                 dirParent = createDirectory(coralSession, dirName, dirParent);
                             }
                             catch(FileAlreadyExistsException e)
                             {
                                 dirParent = (DirectoryResource)coralSession.
                             		getStore().getUniqueResource(dirParent, dirName);
                             }
                         }
                     }
                     createFile(coralSession, name, zis, null, encoding, dirParent);                    
                     zis.closeEntry();
                 }
                 ze = zis.getNextEntry();
             }
         }
         catch(Exception e)
         {
             throw new FilesException("Exception occured during file upload with unzip", e);
         }
     }
     /**
      * Copy the file.
      * 
      * @param source
      *            the source file.
      * @param name
      *            the name of the new file.
      * @param parent
      *            the parent directory.
      * @return the copied file.
      * @throws FilesException if  the operation fails.
      */
     public FileResource copyFile(CoralSession coralSession, FileResource source, String name, DirectoryResource parent) throws FilesException
     {
         Resource[] resources = coralSession.getStore().getResource(parent, name);
         if(resources.length > 0)
         {
             throw new FileAlreadyExistsException("The file '" + name
                 + "' already exists in directory");
         }
         try
         {
             String path = getPath(parent) + "/" + name;
             boolean notExists = fileSystem.createNewFile(path);
             if(!notExists)
             {
                 throw new FilesException("The file '" + name
                     + "' already exists in directory but the resource is missed");
             }
             OutputStream os = fileSystem.getOutputStream(path);
             String sourcePath = getPath(source);
             InputStream is = fileSystem.getInputStream(sourcePath);
             int data = -1;
             while(true)
             {
                 data = is.read();
                 if(data != -1)
                 {
                     os.write(data);
                 }
                 else
                 {
                     os.close();
                     break;
                 }
             }
             FileResource file = FileResourceImpl.createFileResource(coralSession, name, parent);
             file.setSize(fileSystem.length(path));
             file.setMimetype(source.getMimetype());
             file.update();
             return file;
         }
         catch(Exception e)
         {
             throw new FilesException("Exception occured during file copying '" + name + "' ", e);
         }
     }
 
     /**
      * Delete the directory.
      * 
      * @param directory
      *            the directory to delete.
      * @throws FilesException if  the operation fails.
      */
     public void deleteDirectory(CoralSession coralSession, DirectoryResource directory) throws FilesException
     {
         Resource[] resources = coralSession.getStore().getResource(directory);
         if(resources.length > 0)
         {
             throw new DirectoryNotEmptyException("The file '" + directory.getName()
                 + "' is not empty");
         }
         String path = getPath(directory);
         try
         {
             coralSession.getStore().deleteResource(directory);
             fileSystem.delete(path);
         }
         catch(EntityInUseException e)
         {
             throw new FilesException("Exception occured during deleting the directory resource '"
                 + directory.getName() + "' ", e);
         }
         catch(IOException e)
         {
             throw new FilesException("Exception occured during deleting the directory '" + path
                 + "' ", e);
         }
     }
 
     /**
      * Delete the file.
      * 
      * @param file
      *            the file to delete.
      * @throws FilesException if  the operation fails.
      */
     public void deleteFile(CoralSession coralSession, FileResource file) throws FilesException
     {
         String path = getPath(file);
         try
         {
             coralSession.getStore().deleteResource(file);
             fileSystem.delete(path);
         }
         catch(EntityInUseException e)
         {
             throw new FilesException("Exception occured during deleting the file resource '"
                 + file.getName() + "' ", e);
         }
         catch(IOException e)
         {
             throw new FilesException("Exception occured during deleting the file '" + path + "' ",
                 e);
         }
     }
 
     /**
      * Get the file input stream.
      * 
      * @param file
      *            the file.
      * @return the input stream.
      */
     public InputStream getInputStream(FileResource file)
     {
         String path = getPath(file);
         return fileSystem.getInputStream(path);
     }
 
     /**
      * Get the file output stream.
      * 
      * @param file
      *            the file.
      * @return the output stream.
      */
     public OutputStream getOutputStream(FileResource file)
     {
         String path = getPath(file);
         return fileSystem.getOutputStream(path);
     }
 
     /**
      * Get the file last modified time.
      * 
      * @param file
      *            the file.
      * @return the last modified time.
      */
     public long lastModified(FileResource file)
     {
         String path = getPath(file);
         return fileSystem.lastModified(path);
     }
 
     /**
      * Verify the name of the file.
      * 
      * @param name
      *            the name of the file.
      * @return <code>true</code> if accepted.
      */
     public boolean isValid(String name)
     {
         char[] chars = name.toCharArray();
         for (int i = 0; i < chars.length; i++)
         {
             int ch = (int)chars[i];
             if(ch == 45 || ch == 46 || ch == 95 || (ch >= 48 && ch <= 57)
                 || (ch >= 97 && ch <= 122) || (ch >= 65 && ch <= 90))
             {
                 continue;
             }
             return false;
         }
         return true;
     }
 
     /**
      * Convert name to accepted format.
      * 
      * @param name
      *            the name of the file.
      * @return the converted name.
      */
     public String convertName(String name)
     {
         return name;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public String detectMimeType(InputStream is, String name)
     {
         Metadata metadata = new Metadata();
         if(name != null)
         {
             metadata.set(Metadata.RESOURCE_NAME_KEY, name);
         }
         try
         {
             return tika.detect(is, metadata);
         }
         catch(IOException e)
         {
             log.error("failed to detect MIME type", e);
             return DEFAULT_MIME_TYPE;
         }
     }
 
     /**
      * Extracts text content from the file for the purpose of indexing (search). The implemenation
      * uses <a href="http://lucene.apache.org/tika/">Apache Tika</a> to perform file type
      * recongnition and parsing.
      * 
      * @param file the file to be parsed.
      * @return extracted text content. If file format is not supported empty string will be
      *         returned.
      */
     @Override
     public String extractContent(FileResource file)
     {
         Metadata metadata = new Metadata();
         metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
         try
         {
             final InputStream inputStream = getInputStream(file);
             if(inputStream != null)
             {
                 return tika.parseToString(inputStream, metadata);
             }
         }
         catch(Exception e)
         {
             log.error("failed to parse " + file.getPath(), e);
         }
         return "";
     }  
 
     /**
      * Get the path of the item (file or directory).
      * 
      * @param item
      *            the item.
      * @return the path to the directory.
      */
     public String getPath(ItemResource item)
     {
         String path = "";
         for (Resource parent = item; parent != null; parent = parent.getParent())
         {
             if(parent instanceof RootDirectoryResource)
             {
                 path = ((RootDirectoryResource)parent).getRootPath() + path;
                 break;
             }
             else
             {
                 path = "/" + parent.getName() + path;
             }
         }
         return path;
     }
     
     /**
      * Get the site the given item (file or directory) belongs to.
      * 
      * @param file the file.
      */
     public SiteResource getSite(ItemResource item)
     {
         Resource r = item;
         while(r != null)
         {
             if(r instanceof SiteResource)
             {
                 return (SiteResource)r;
             }
             r = r.getParent();
         }
         throw new IllegalStateException(item+" does not have a SiteResource parent");
     }
     
     /**
      * Retrieves a <code>cms.files.file</code> resource instance from the store.
      *
      * <p>This is a simple wrapper of StoreService.getResource() method plus
      * the typecast.</p>
      *
      * @param session the CoralSession
      * @param path the path of file to be retrieved
      * @param site optional (can be null) site parameter - causes method to prepend site root to the path
      * @return a resource instance.
      * @throws EntityDoesNotExistException if the resource with the given id does not exist.
      * @throws FilesException 
      */
     @Override
     public FileResource getFileResource(CoralSession session, String path, SiteResource site)
         throws EntityDoesNotExistException, FilesException
     {
         if(path.startsWith("/"))
         {
             path = path.substring(1);
         }
         
         String rootPath = "";        
         if(site != null) {
             final Resource root = this.getFilesRoot(session, site);
             rootPath = root.getPath();
         }
         
         final Resource[] res = session.getStore().getResourceByPath(rootPath + "/" +path);   
         
         Resource fileOrDir = null;
         if(res.length >= 1)
         {
             fileOrDir = res[0];
         } else {
             throw new EntityDoesNotExistException("File " + path + " does not exist.");
         }
         return FileResourceImpl.getFileResource(session, fileOrDir.getId());
     }
 
     /**
      * Retrieves a <code>cms.files.file</code> resource instance from the store.
      *
      * <p>This is a simple wrapper of StoreService.getResource() method plus
      * the typecast.</p>
      *
      * @param session the CoralSession
      * @param id the id of the object to be retrieved
      * @return a resource instance.
      * @throws EntityDoesNotExistException if the resource with the given id does not exist.
      */
     @Override
     public FileResource getFileResource(CoralSession session, long id)
         throws EntityDoesNotExistException
     {
         Resource res = session.getStore().getResource(id);
         if(!(res instanceof FileResource))
         {
             throw new IllegalArgumentException("resource #"+id+" is "+
                                                res.getResourceClass().getName()+
                                                " not cms.files.file");
         }
         return (FileResource)res;
     } 
     
     /* (non-Javadoc)
      * @see net.cyklotron.cms.files.FilesService#createParentDirs(org.objectledge.coral.session.CoralSession, java.lang.String, net.cyklotron.cms.site.SiteResource)
      */
     @Override
     public DirectoryResource createParentDirs(CoralSession session, String path, SiteResource site) throws FilesException, InvalidResourceNameException {
                 
         while(path.startsWith("/"))
         {
             path = path.substring(1);
         }
         
         SiteResource siteRoot = null; 
         siteRoot = (SiteResource)session.getStore().getResourceByPath(site.getPath())[0];
 
         final String[] tokens = path.split("/");
 
         Resource parent = session.getStore().getResource(siteRoot, FILES_ROOT_DIR_NAME)[0];
         
         for(int i=0; i<tokens.length-1; i++) {
             String dirname = tokens[i];
             final Resource[] res = session.getStore().getResource(parent, dirname);
             if(res.length > 0) {
                 //dir exists, moving on
                 parent = (DirectoryResource)res[0];
             } else {
                 parent = createDirectory(session, tokens[i], (DirectoryResource)parent);                
             }
         }
         return (DirectoryResource)parent;
     }
     
     @Override
     public void replaceFile(FileResource file, InputStream uploadedInputStream)
         throws IOException
     {
         OutputStream output = getOutputStream(file);
         long size = rewrite(uploadedInputStream, output);
         file.update();
         file.setSize(size);
         file.update();
     }
     
     private long rewrite(InputStream input, OutputStream output)
         throws IOException
     {
         byte[] buffer = new byte[1024];
         long total = 0;
         int bytesRead = 0;
         do
         {
             bytesRead = input.read(buffer, 0, 1024);
             if(bytesRead > 0)
             {
                 output.write(buffer, 0, bytesRead);
                 total += bytesRead;
             }
         }
         while(bytesRead > 0);
         output.close();
         input.close();
         return total;
     }
 
     private String cacheDirectory(FileResource file)
         throws IOException
     {
         final String id = file.getIdString();
         String dirName = RESIZED_IMAGE_CACHE_DIR
             + ("0" + id).substring(id.length() - 1, id.length() + 1);
         if(!fileSystem.exists(dirName))
         {
             fileSystem.mkdirs(dirName);
         }
         return dirName;
     }
 
     @Override
     public String resizeImage(FileResource file, int w, int h)
         throws IOException
     {
         if(w == -1 && h == -1)
         {
             throw new IOException("at least one of w and h parameters must be positive");
         }
 
         InputStream is = getInputStream(file);
         String contentType = detectMimeType(is, file.getName());
         if(!contentType.startsWith("image/"))
         {
             throw new IOException(file.toString() + " is not an image: " + contentType
                 + " detected");
         }
 
         is.reset();
         BufferedImage srcImage = ImageIO.read(is);
         try
         {
             if(h == -1)
             {
                 h = (int)(srcImage.getHeight() * ((float)w / srcImage.getWidth()));
             }
             if(w == -1)
             {
                 w = (int)(srcImage.getWidth() * ((float)h / srcImage.getHeight()));
             }
             String path = cacheDirectory(file) + "/" + file.getIdString()
                 + String.format("_%d_%d.png", w, h);
             if(!fileSystem.exists(path)
                 || fileSystem.lastModified(path) < fileSystem.lastModified(getPath(file)))
             {
                 String tempPath = path + "-" + Thread.currentThread().getName();
                 BufferedImage targetImage = Scalr.resize(srcImage, Scalr.Method.AUTOMATIC,
                     Scalr.Mode.FIT_EXACT, w, h);
                 try
                 {
                     OutputStream os = fileSystem.getOutputStream(tempPath);
                     ImageIO.write(targetImage, "png", os);
                     os.close();
                     fileSystem.rename(tempPath, path);
                 }
                 finally
                 {
                     targetImage.flush();
                 }
             }
             return path;
         }
         finally
         {
             srcImage.flush();
         }
     }
     
     @Override
     public String resizeImage(FileResource file, int w, int h, String rm, boolean crop, int crop_x, int crop_y)
         throws IOException
     {
         if(w == -1 && h == -1)
         {
             throw new IOException("at least one of w and h parameters must be positive");
         }
 
         InputStream is = getInputStream(file);
         String contentType = detectMimeType(is, file.getName());
         if(!contentType.startsWith("image/"))
         {
             throw new IOException(file.toString() + " is not an image: " + contentType
                 + " detected");
         }
 
         is.reset();
         BufferedImage srcImage = ImageIO.read(is);
         try
         {
             if(h == -1)
             {
                 h = (int)(srcImage.getHeight() * ((float)w / srcImage.getWidth()));
             }
             if(w == -1)
             {
                 w = (int)(srcImage.getWidth() * ((float)h / srcImage.getHeight()));
             }
             String path = cacheDirectory(file) + "/" + file.getIdString()
                 + String.format("_%d_%d_%s_%b_%d_%d.png", w, h, rm, crop, crop_x, crop_y);
             if(!fileSystem.exists(path)
                 || fileSystem.lastModified(path) < fileSystem.lastModified(getPath(file)))
             {
                 String tempPath = path + "-" + Thread.currentThread().getName();
                 BufferedImage targetImage = null;
                 if("w".equals(rm)) {
                     targetImage = Scalr.resize(srcImage, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH, w);
                 } else if("h".equals(rm)) {
                     targetImage = Scalr.resize(srcImage, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_HEIGHT, h);
                 } else if("a".equals(rm)) {
                     targetImage = Scalr.resize(srcImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, w, h);
                 } else {
                     targetImage = Scalr.resize(srcImage, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, w, h);
                 }
                 if(crop)
                 {
                     if(crop_x == -1)
                     {
                         crop_x = ((targetImage.getWidth() - w) / 2);
                     }
                     if(crop_y == -1)
                     {
                        crop_y = ((targetImage.getHeight() - h) / 2);
                     }
                     targetImage = Scalr.crop(targetImage, crop_x, crop_y, w, h);
                 }
                 try
                 {
                     OutputStream os = fileSystem.getOutputStream(tempPath);
                     ImageIO.write(targetImage, "png", os);
                     os.close();
                     fileSystem.rename(tempPath, path);
                 }
                 finally
                 {
                     targetImage.flush();
                 }
             }
             return path;
         }
         finally
         {
             srcImage.flush();
         }
     }
 }
 
