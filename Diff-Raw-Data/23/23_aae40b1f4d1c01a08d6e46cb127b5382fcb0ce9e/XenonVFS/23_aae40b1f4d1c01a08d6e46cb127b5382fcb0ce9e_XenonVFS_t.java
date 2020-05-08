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
 
 package nl.esciencecenter.vbrowser.vrs.xenon;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Set;
 
 import nl.esciencecenter.ptk.GlobalProperties;
 import nl.esciencecenter.ptk.data.StringHolder;
 import nl.esciencecenter.ptk.util.StringUtil;
 import nl.esciencecenter.ptk.util.logging.ClassLogger;
 import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
 import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
 import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
 import nl.esciencecenter.vlet.exception.AuthenticationException;
 import nl.esciencecenter.vlet.vrs.ServerInfo;
 import nl.esciencecenter.vlet.vrs.VRSContext;
 import nl.esciencecenter.vlet.vrs.vfs.FileSystemNode;
 import nl.esciencecenter.vlet.vrs.vfs.VFS;
 import nl.esciencecenter.vlet.vrs.vfs.VFSNode;
 import nl.esciencecenter.xenon.XenonException;
 import nl.esciencecenter.xenon.credentials.Credential;
 import nl.esciencecenter.xenon.files.FileAttributes;
 import nl.esciencecenter.xenon.files.FileSystem;
 import nl.esciencecenter.xenon.files.Path;
 import nl.esciencecenter.xenon.files.PathAttributesPair;
 import nl.esciencecenter.xenon.files.PosixFilePermission;
 
 /**
  * Octopus Meta VFileSystem adaptor. 
  */  
 public class XenonVFS extends FileSystemNode
 {
     private static ClassLogger logger=null; 
     
     static 
     {
         logger=ClassLogger.getLogger(XenonVFS.class);
         logger.setLevelToDebug();
     }
     
 	// ========================================================================
 	// Instance
 	// ========================================================================
 	
 	protected XenonClient xenonClient;
 	
 	protected FileSystem octoFS;
 
     private Path entryPath;
 
 
     public XenonVFS(VRSContext context, ServerInfo info,VRL location) throws VrsException 
 	{
 		super(context, info);
 		
 	    // create optional shared client. 
 	    xenonClient=XenonClient.createFor(context,info); 
 	    
 		boolean isSftp="sftp".equals(location.getScheme());
 		boolean isGftp="gsiftp".equals(location.getScheme()) || "gftp".equals(location.getScheme());
         boolean isLocal="file".equals(location.getScheme());
 
 		String fsUriStr=null;
 		String drivePath="/";
 		
 		String vrlUser=location.getUsername(); 
 	    String configeredUser=info.getUsername(); 
         
 		if (isSftp)
 		{
             if (StringUtil.isEmpty(configeredUser))
             {
                 configeredUser=context.getConfigManager().getUserName(); 
             }
             int port=location.getPort(); 
             if (port<=0) 
                 port=22;
             
             fsUriStr=location.getScheme()+"://"+configeredUser+"@"+location.getHostname()+":"+port+"/";
 		}
 		else if (isGftp)
 		{
 		    fsUriStr=location.getScheme()+"://"+location.getHostname()+"/";
 		    
 		}
 		else if (isLocal)
 		{
 			fsUriStr="file:/";
 			if (GlobalProperties.isWindows())
 			{
 				// normalized dos path: '/C:/'
 				String pathStr=location.getPath(); 
 				if ((pathStr.length()>=3) && (pathStr.charAt(2)==':'))
 				{
 					// Get "C:" part. 
 					drivePath=pathStr.substring(1,3);
 		    	}
 				else
 				{
 					drivePath=xenonClient.getFirstDrive();
 				}
 		    }
 		}
 		else
 	    {
 	    	throw new VrsException("File system type not yet supported for:"+location); 
 	    }		
 		try
         {
 		    URI fsUri=new URI(fsUriStr);
 
 		    
 		    if (isSftp)
 		    {
 		        octoFS=authenticateSftpFileSystem(fsUri,info); 
 		    }
 		    else if (isGftp)
             {
                 info.getUserinfo(); 
                 octoFS=xenonClient.createGftpFileSystem(fsUri,xenonClient.createGftpCredentials(info));
             }
 		    else if (isLocal)
 		    {
 		        octoFS=xenonClient.createLocalFileSystem(drivePath);
 		    }
 		    else
 		    {
 		    	throw new VrsException("File system type not yet supported for:"+location); 
 		    }
 		    
 		    this.entryPath = octoFS.getEntryPath();
 		    
         }
         catch (Exception e)
         {
           throw new VrsException(e.getMessage(),e); 
         } 
 	}
 
     private FileSystem authenticateSftpFileSystem(URI fsUri,ServerInfo info) throws VrsException
     {
         StringHolder credentialErrorH1=new StringHolder();
         StringHolder credentialErrorH2=new StringHolder(); 
 
         info.getUserinfo();
         Credential sshCred=null; 
        String sshKeyFile=info.getAttributeValue(ServerInfo.ATTR_SSH_IDENTITY); 
        String sshUser=info.getUserinfo();
         
        if (sshKeyFile!=null)
         {
             try
             {
                 sshCred=xenonClient.createSSHKeyCredential(info, credentialErrorH1);
                 if (sshCred==null)
                 {
                    logger.warnPrintf("Couldn't use SSH Key authentication for key:%s, error=%s\n",sshKeyFile,credentialErrorH1.value);
                     // keep credentialErrorH1
                 }
                 else
                 {
                     octoFS=xenonClient.createSftpFileSystem(fsUri,sshCred);
                     return octoFS;  
                 }
             }
             catch (XenonException e)
             {
                if (e.getMessage().contains("Auth cancel"))
                {
                    credentialErrorH1.value="Invalid User+Keyfile combination for '"+fsUri.getUserInfo()+" and '"+sshKeyFile+"'";
                    logger.warnPrintf("SSH Keyfile authentication failed:%s\n",credentialErrorH2.value); 
                }
                else
                {
                    throw new VrsException(e.getMessage(),e);
                }
             }
         }
         
         // ssh key not valid, use password: 
         try
         {
             sshCred=xenonClient.createSSHPasswordCredential(info, true,credentialErrorH2);
             
             if (sshCred!=null)
             {
                 octoFS=xenonClient.createSftpFileSystem(fsUri,sshCred);
                 return octoFS;  
             }
             else
             {
                logger.warnPrintf("SSH Password authentication failed for user:'%s', error=%s\n",fsUri.getUserInfo(),credentialErrorH2.value);
             }
         }
         catch (XenonException e)
         {
             if (e.getMessage().contains("Auth cancel"))
             {
                 credentialErrorH2.value="Invalid Password for '"+fsUri.getUserInfo()+"' on '"+fsUri.getHost()+"'"; 
             }
             else
             {
                 throw new VrsException("Error connecting to:"+fsUri+"\n"+e.getMessage(),e);
             }
         }
         
         String message="Failed to authenticate remote SSH Location:"+fsUri+"\n";
         
         if (credentialErrorH1.value!=null)
         {
             message+="SSH Identity error:"+credentialErrorH1.value+"\n";
         }
         if (credentialErrorH2.value!=null)
         {
             message+="SSH Password error:"+credentialErrorH2.value+"\n";
         }
                 
         throw new AuthenticationException(message);
     }   
 
     /** 
      * Resolve VRL against this FileSystem 
      */ 
     public Path createPath(VRL vrl) throws VrsException
     {
         try
         {
             // resolve path against FileSystem
             return xenonClient.resolvePath(octoFS,vrl.getPath());
         }
         catch (Exception e)
         {
           throw new VrsException(e.getMessage(),e); 
         }  
     }
     
     /** 
      * Convert Octopus path to (absolute) VRL 
      */ 
     public VRL createVRL(Path path) throws VrsException 
     {
         VRL fsVrl=this.getVRL(); 
         String pathstr=path.getRelativePath().getAbsolutePath();
         return fsVrl.replacePath(pathstr); 
     }
        
     @Override
     public XenonDir newDir(VRL pathVrl) throws VrsException
     {
     	// VDir factory method: 
     	// new VDir object: path doesn't have to exist, just create the (VDir) object. 
         return new XenonDir(this,null,createPath(pathVrl)); 
     }   
 
     @Override
     public XenonFile newFile(VRL pathVrl) throws VrsException
     {
     	// VFile factory method: 
     	// new VFile object: path doesn't have to exist, just create the (VFile) object. 
         return new XenonFile(this,null,createPath(pathVrl)); 
     }
     
     @Override
     public XenonDir getDir(VRL pathVrl) throws VrsException
     {
     	// Open filepath and return new VDir object. 
     	// (remote) directory must exist. 
     	XenonDir dir=newDir(pathVrl); 
         
         // openDir() must return existing directory: 
         if (dir.exists()==false)
             throw new nl.esciencecenter.vlet.exception.ResourceNotFoundException("Directory doesn't exists:"+dir); 
         
         return dir; 
     }
 
     @Override
     public XenonFile getFile(VRL pathVrl) throws VrsException
     {
     	// Open filepath and return new VFile object. 
     	// (remote) file must exist.  
         XenonFile file=newFile(pathVrl);
         
         // openFile() must return existing file: 
         if (file.exists()==false)
             throw new nl.esciencecenter.vlet.exception.ResourceNotFoundException("File doesn't exists:"+file); 
         
         return file; 
     }
 
 	public void connect() throws VrsException 
 	{
 	    
 	}
 
 	public void disconnect() throws VrsException
 	{
 	    // could destroy FileSystem object here. 
 	}
 
 	public boolean isConnected() 
 	{
 		return true;
 	}
 	
 	@Override
 	public VFSNode openLocation(VRL vrl) throws VrsException
 	{
 	    // openLocation: remote object must exist. 
 	    try
 	    {
     	    Path path = createPath(vrl); 
     	    FileAttributes attrs = xenonClient.statPath(path); 
     
     	    return newVFSNode(path,attrs); 
     	    
 	    }
 	    catch ( XenonException e)
 	    {
 	        throw new VrsException(e.getMessage(),e); 
 	    }
 	}
 
 	protected VFSNode newVFSNode(Path path,FileAttributes optFileattrs) throws VrsException
     {
 	    try
 	    {
             if ((optFileattrs!=null) && optFileattrs.isDirectory())
             {
                 return new XenonDir(this,optFileattrs,path);
             }   
             else if ((optFileattrs!=null) && optFileattrs.isSymbolicLink())
             {
                 // resolve links here: 
                 return new XenonFile(this,optFileattrs,path);
             }
             else
             {
                 // default to file: 
                 return new XenonFile(this,optFileattrs,path);
             }
 	    }
 	    catch (Throwable e)
         {
             throw new VrsException(e.getMessage(),e); 
         }
     }
 
     // ========================================================================
 	// Filesystem helper methods: 
 	// ========================================================================
 	
 
     /** List nodes without fetching file attributes. All node are 'VFile' */ 
     public VFSNode[] listNodes(Path octoPath) throws VrsException
     {
         List<Path> paths=null; 
         
         try
         {
             paths = xenonClient.listDir(octoPath);
             if ((paths==null) || (paths.size()==0))
                     return null; 
                 
             VFSNode nodes[]=new VFSNode[paths.size()]; 
             
             for (int i=0;i<paths.size();i++)
             {
                 Path path=paths.get(i); 
                 nodes[i]=newVFSNode(path,null);
             }
             
             return nodes; 
         }
         catch (Throwable e)
         {
             throw new VrsException(e.getMessage(),e); 
         } 
     }
 
     public VFSNode[] listNodesAndAttrs(Path octoPath) throws VrsException
     {
         List<PathAttributesPair> paths=null; 
         
         try
         {
             paths = xenonClient.statDir(octoPath);
             if ((paths==null) || (paths.size()==0))
                     return null; 
                 
             VFSNode nodes[]=new VFSNode[paths.size()]; 
             
             for (int i=0;i<paths.size();i++)
             {
                 PathAttributesPair pathAttrs=paths.get(i); 
                 
                 nodes[i]=newVFSNode(pathAttrs.path(),pathAttrs.attributes()); 
             }
             
             return nodes; 
         }
         catch (Throwable e)
         {
             throw new VrsException(e.getMessage(),e); 
         } 
      }
 
     public long getModificationTime(FileAttributes attrs, long currentTimeMillis) // throws VlException
     {
         try
         {
             return attrs.lastModifiedTime();
         }
         catch (Throwable e)
         {
             // throw new VlException(e.getMessage(),e); 
             return currentTimeMillis; 
         }
     }
 
     public boolean isReadable(FileAttributes attrs, boolean defaultValue) throws VrsException
     {
         try
         {
             return attrs.isReadable();
         }
         catch (Throwable e)
         {
             throw new VrsException(e.getMessage(),e); 
         }
     }
 
     public boolean isWritable(FileAttributes attrs, boolean defaultValue) throws VrsException
     {
         try
         {
             return attrs.isWritable();
         }
         catch (Throwable e)
         {
             throw new VrsException(e.getMessage(),e); 
         }
     }
     
     public long getLength(FileAttributes attrs, long defaultVal) throws IOException
     {
         try
         {
             return attrs.size();
         }
         catch (Throwable e)
         {
             throw new IOException(e.getMessage(),e); 
             // return defaultVal;
         }
     }
 
     public VRL rename(Path octoPath, boolean isDir, String newName, boolean renameFullPath) throws VrsException
     {
         Path newPath=null; 
         VRL baseVRL=createVRL(octoPath); 
         VRL newVRL; 
         
         if (renameFullPath==false)
         {
             newVRL=baseVRL.getParent().resolvePath(newName);
         }
         else
         {
             // resolve against root: 
             VRL oldVRL=createVRL(octoPath); 
             newVRL= oldVRL.replacePath(newName); 
         }
         
         newPath=createPath(newVRL); 
         
         try
         {
             xenonClient.rename(octoPath,newPath);
             return createVRL(newPath);
         }
         catch (Throwable e)
         {
             throw new VrsException(e.getMessage(),e); 
         } 
     }
 
     public String createPermissionsString(FileAttributes attrs, boolean isDir) throws VrsException
     {
         Set<PosixFilePermission> set;
         try
         {
             set = attrs.permissions();
             int mode=xenonClient.getUnixFileMode(set); 
             return VFS.modeToString(mode, isDir); 
         }
         catch (Throwable e)
         {
             throw new VrsException(e.getMessage(),e); 
         }
         
 
     }
 
    
 }
