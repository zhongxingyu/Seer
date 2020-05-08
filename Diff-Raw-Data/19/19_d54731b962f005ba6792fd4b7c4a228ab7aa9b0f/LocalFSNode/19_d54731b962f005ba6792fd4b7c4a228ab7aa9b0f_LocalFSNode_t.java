 /*
  * Copyrighted 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").  
  * You may not use this file except in compliance with the License. 
  * For details, see the LICENCE.txt file location in the root directory of this 
  * distribution or obtain the Apache License at the following location: 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
  * ---
  */
 // source: 
 
 package nl.esciencecenter.ptk.io.local;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.nio.file.attribute.FileTime;
 import java.nio.file.attribute.PosixFileAttributes;
 import java.nio.file.attribute.PosixFilePermission;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import nl.esciencecenter.ptk.GlobalProperties;
 import nl.esciencecenter.ptk.io.FSNode;
 import nl.esciencecenter.ptk.io.FileURISyntaxException;
 import nl.esciencecenter.ptk.net.URIFactory;
 
 /**
  * Local file implementation of FSNode based on java.nio.Files;
  */
 public class LocalFSNode extends FSNode
 {
     // nio !
     private Path _path;
 
     private BasicFileAttributes basicAttrs;
 
     private PosixFileAttributes posixAttrs;
     
     
     public LocalFSNode(LocalFSHandler fsHandler,Path path)
     {
         super(fsHandler,path.toUri());
         init(path);
     }
 
     private void init(Path path)
     {
         setURI(path.toUri());
         this._path = path;
     }
 
     @Override
     public boolean sync()
     {
         this.basicAttrs = null;
         this.posixAttrs = null;
         return true;
     }
 
     public LocalFSNode(LocalFSHandler fsHandler,URI loc)
     {
         super(fsHandler,loc);
         FileSystem fs = FileSystems.getDefault();
         if (GlobalProperties.isWindows())
         {
         	String dosPath=new URIFactory(loc).getDosPath();
         	init(fs.getPath(dosPath));
         }
         else
         {
         	init(fs.getPath(loc.getPath()));
         }
     }
     
     protected LocalFSHandler getFSHandler()
     {
         return (LocalFSHandler)super.getFSHandler(); 
     }
     
     @Override
     public boolean exists(LinkOption... linkOptions)
     {
        if (linkOptions==null)
        {
            return Files.exists(_path);
        }
        else
        {
            return Files.exists(_path, linkOptions);
        }
     }
 
     @Override
     public boolean isDirectory(LinkOption... linkOptions)
     {
        if (linkOptions==null)
        {
            return Files.isDirectory(_path);
        }
        else
        {
            return Files.isDirectory(_path, linkOptions);
        }
     }
 
     @Override
     public String[] list() throws IOException
     {
         DirectoryStream<Path> dirStream = Files.newDirectoryStream(_path);
         Iterator<Path> dirIterator = dirStream.iterator();
         ArrayList<String> list = new ArrayList<String>();
 
         while (dirIterator.hasNext())
         {
             list.add(dirIterator.next().getFileName().toString());
         }
 
         return list.toArray(new String[0]);
     }
 
     @Override
     public LocalFSNode[] listNodes() throws IOException
     {
         DirectoryStream<Path> dirStream = Files.newDirectoryStream(_path);
         Iterator<Path> dirIterator = dirStream.iterator();
         ArrayList<LocalFSNode> list = new ArrayList<LocalFSNode>();
 
         while (dirIterator.hasNext())
         {
             list.add(new LocalFSNode(getFSHandler(),dirIterator.next()));
         }
 
         return list.toArray(new LocalFSNode[0]);
     }
 
     public void delete() throws IOException
     {
         Files.delete(_path);
     }
 
     @Override
     public long length() throws IOException
     {
         return (Long) Files.getAttribute(_path, "size");
     }
 
     @Override
     public boolean isFile(LinkOption... linkOptions)
     {
         return Files.isRegularFile(_path, linkOptions);
     }
 
     @Override
     public void mkdir() throws IOException
     {
         Files.createDirectory(_path);
     }
 
     @Override
     public void mkdirs() throws IOException
     {
         Files.createDirectories(_path);
     }
 
     @Override
     public OutputStream createOutputStream() throws IOException
     {
         return Files.newOutputStream(_path); // OpenOptions..
     }
 
     @Override
     public InputStream createInputStream() throws IOException
     {
         return Files.newInputStream(_path);
     }
 
     @Override
     public String getPathname()
     {
         return _path.toUri().getPath();
     }
 
     public Path getPath()
     {
         return _path; 
     }
     
     @Override
     public LocalFSNode getParent()
     {
         return new LocalFSNode(getFSHandler(),_path.getParent());
     }
 
     @Override
     public long getModificationTime() throws IOException
     {
         FileTime value = getBasicAttributes().lastModifiedTime();
         return value.toMillis();
     }
 
     @Override
     public long getCreationTime() throws IOException
     {
         FileTime value = getBasicAttributes().creationTime();
         return value.toMillis();
     }
 
     @Override
     public long getAccessTime() throws IOException
     {
         FileTime value = getBasicAttributes().lastAccessTime();
         return value.toMillis();
     }
 
     @Override
     public LocalFSNode newFile(String path) throws FileURISyntaxException
     {
         LocalFSNode lfile = new LocalFSNode(getFSHandler(),resolvePathURI(path));
         return lfile;
     }
 
     public java.io.File toJavaFile()
     {
         return _path.toFile();
     }
 
     @Override
     public boolean isSymbolicLink()
     {
         return Files.isSymbolicLink(_path);
     }
 
     public boolean isBrokenLink() throws IOException
     {
         if (isSymbolicLink()==false)
             return false; 
         
         return (getSymbolicLinkTarget().exists()==false);
     }
     
     /**
      * Returns symbolic link target or NULL
      */
     public LocalFSNode getSymbolicLinkTarget() throws IOException
     {
         if (this.isSymbolicLink() == false)
             return null;
 
         Path target = Files.readSymbolicLink(_path);
 
         return new LocalFSNode(getFSHandler(),target);
     }
 
     public BasicFileAttributes getBasicAttributes() throws IOException
     {
         try
         {
             if (basicAttrs == null)
             {
                 basicAttrs = Files.readAttributes(_path, BasicFileAttributes.class);
             }
         }
         catch (IOException e)
         {
             // auto dereference in the case of a borken link: 
             if (isBrokenLink())
             {
                 basicAttrs = Files.readAttributes(_path, BasicFileAttributes.class,LinkOption.NOFOLLOW_LINKS);
             }
             else
             {
                 throw e;
             }
         }
 
         return basicAttrs;
     }
 
     public PosixFileAttributes getPosixAttributes() throws IOException
     {   
         try
         {
             if (posixAttrs == null)
             {
                 posixAttrs = Files.readAttributes(_path, PosixFileAttributes.class);
             }
         }
         catch (IOException e)
         {
             // auto dereference in the case of a borken link: 
             if (isBrokenLink())
             {
                 posixAttrs = Files.readAttributes(_path, PosixFileAttributes.class,LinkOption.NOFOLLOW_LINKS);
             }
             else
             {
                 throw e;
             }
         }
         catch (UnsupportedOperationException e)
         {
         	return null; 
         }
         
         return posixAttrs;
     }
 
 	public int getUnixFileMode() throws IOException
     {
 		PosixFileAttributes attrs;
 		if ((attrs=getPosixAttributes())==null)
 			return 0; 
 		
         Set<PosixFilePermission> perms = attrs.permissions();
         
         return toUnixFileMode(perms);
     }
 
     public static int toUnixFileMode(Set<PosixFilePermission> perms)
     {
         int mode = 0;
 
         if (perms.contains(PosixFilePermission.OWNER_READ))
             mode |= 0400;
         if (perms.contains(PosixFilePermission.OWNER_WRITE))
             mode |= 0200;
         if (perms.contains(PosixFilePermission.OWNER_EXECUTE))
             mode |= 0100;
         if (perms.contains(PosixFilePermission.GROUP_READ))
             mode |= 0040;
         if (perms.contains(PosixFilePermission.GROUP_WRITE))
             mode |= 0020;
         if (perms.contains(PosixFilePermission.GROUP_EXECUTE))
             mode |= 0010;
         if (perms.contains(PosixFilePermission.OTHERS_READ))
             mode |= 0004;
         if (perms.contains(PosixFilePermission.OTHERS_WRITE))
             mode |= 0002;
         if (perms.contains(PosixFilePermission.OTHERS_EXECUTE))
             mode |= 0001;
 
         return mode;
     }
 
     public static Set<PosixFilePermission> fromUnixFileMode(int mode)
     {
         Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
 
         if ((mode & 0400) > 0)
             perms.add(PosixFilePermission.OWNER_READ);
         if ((mode & 0200) > 0)
             perms.add(PosixFilePermission.OWNER_WRITE);
         if ((mode & 0100) > 0)
             perms.add(PosixFilePermission.OWNER_EXECUTE);
 
         if ((mode & 0040) > 0)
             perms.add(PosixFilePermission.GROUP_READ);
         if ((mode & 0020) > 0)
             perms.add(PosixFilePermission.GROUP_WRITE);
         if ((mode & 0010) > 0)
             perms.add(PosixFilePermission.GROUP_EXECUTE);
 
         if ((mode & 0004) > 0)
             perms.add(PosixFilePermission.OTHERS_READ);
         if ((mode & 0002) > 0)
             perms.add(PosixFilePermission.OTHERS_WRITE);
         if ((mode & 0001) > 0)
             perms.add(PosixFilePermission.OTHERS_EXECUTE);
 
         return perms;
     }
 
     public void setUnixFileMode(int mode) throws IOException
     {
         Files.setPosixFilePermissions(_path, fromUnixFileMode(mode));
     }
 
 	public String getOwnerName() throws IOException
 	{
 		PosixFileAttributes attrs;
 		
 		if ((attrs=this.getPosixAttributes())==null)
 			return null;
 		
 		return attrs.owner().getName(); 
 	}
 
 	public String getGroupName() throws IOException
 	{
 		PosixFileAttributes attrs;
 		
 		if ((attrs=this.getPosixAttributes())==null)
 			return null;
 		
 		return attrs.group().getName(); 
 	}
 }
