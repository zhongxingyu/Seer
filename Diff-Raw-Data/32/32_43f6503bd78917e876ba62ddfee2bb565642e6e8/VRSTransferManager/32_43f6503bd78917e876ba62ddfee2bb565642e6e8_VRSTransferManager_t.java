 /*
  * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
  * Copyright 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * You may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at the following location:
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
  * ---
  */
 // source:
 
 package nl.esciencecenter.vlet.vrs.vfs;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import nl.esciencecenter.ptk.data.BooleanHolder;
 import nl.esciencecenter.ptk.data.LongHolder;
 import nl.esciencecenter.ptk.data.StringHolder;
 import nl.esciencecenter.ptk.task.ActionTask;
 import nl.esciencecenter.ptk.task.ITaskMonitor;
 import nl.esciencecenter.ptk.util.StringUtil;
 import nl.esciencecenter.ptk.util.logging.ClassLogger;
 import nl.esciencecenter.vbrowser.vrs.data.Attribute;
 import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
 import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
 import nl.esciencecenter.vlet.VletConfig;
 import nl.esciencecenter.vlet.exception.NestedIOException;
 import nl.esciencecenter.vlet.exception.NestedInterruptedException;
 import nl.esciencecenter.vlet.exception.ResourceAlreadyExistsException;
 import nl.esciencecenter.vlet.exception.ResourceCreationFailedException;
 import nl.esciencecenter.vlet.exception.ResourceTypeMismatchException;
 import nl.esciencecenter.vlet.vrs.VComposite;
 import nl.esciencecenter.vlet.vrs.VDeletable;
 import nl.esciencecenter.vlet.vrs.VNode;
 import nl.esciencecenter.vlet.vrs.VRSContext;
 import nl.esciencecenter.vlet.vrs.data.VAttributeConstants;
 import nl.esciencecenter.vlet.vrs.events.ResourceEvent;
 import nl.esciencecenter.vlet.vrs.io.VSize;
 import nl.esciencecenter.vlet.vrs.io.VStreamReadable;
 import nl.esciencecenter.vlet.vrs.tasks.VRSTaskWatcher;
 import nl.esciencecenter.vlet.vrs.ui.ICopyInteractor;
 import nl.esciencecenter.vlet.vrs.ui.ICopyInteractor.InteractiveAction;
 import nl.esciencecenter.vlet.vrs.util.VRSIOUtil;
 import nl.esciencecenter.vlet.vrs.vdriver.http.HTTPNode;
 import nl.esciencecenter.vlet.vrs.vfs.VFileActiveTransferable.ActiveTransferType;
 import nl.esciencecenter.vlet.vrs.vrl.VRLUtil;
 import nl.esciencecenter.vlet.vrs.vrms.VLogicalResource;
 
 
 /**
  * Core "transfer" class which performs all the VRS/VFS copy and move actions. 
  * For actual copy of move methods, use the VFile,VDir or VFSClient classes.  
  * This class contains the actual implementations and transfer logic.    
  */ 
 public final class VRSTransferManager
 {
     private static ClassLogger logger;
     
     static
     {
         logger=ClassLogger.getLogger(VRSTransferManager.class); 
     }
     
 	private VRSContext vrsContext;
 
 	private Vector<VFSTransfer>transfers=new Vector<VFSTransfer>();
 
 	public VRSTransferManager(VRSContext context)
 	{
 		// ubiquitous VRSContext !
 		this.vrsContext=context; 
 	}
 	
 	public VRSTaskWatcher getTaskWatcher()
 	{
 	    return this.vrsContext.getTaskWatcher(); 
     }
 	 
 	protected VFSTransfer newTransfer(ITaskMonitor monitor, VNode source,VRL targetVRL, boolean isMove) throws Exception
 	{
 		VFSTransfer transfer=new VFSTransfer(monitor,source.getResourceType(),source.getVRL(),targetVRL,isMove);
 		transfer.setTotalWorkTodo(0); // init
 		this.transfers.add(transfer); 
 		return transfer; 
 	}
 
 
 	/** Direct VFile to VFile copy/move */ 
     protected VFile doCopyMove(VFile source, VFile target, boolean isMove) throws VrsException
     {
         try
         {
            VRL targetVRL=target.getVRL();
            VFSTransfer transferInfo = newTransfer(null,source,targetVRL,isMove); 
            VFileSystem fs=target.getFileSystem();
            
            // create new object:
            return (VFile)masterCopyMoveTo(transferInfo,source,fs,targetVRL,isMove,null);
         }
         catch (VrsException e)
         {
             throw e;
         }
         catch (IOException e)
         {
             throw new NestedIOException(e); 
         }
         catch (Exception e)
         {
             throw new VrsException(e); 
         }
     }
 
     /** Generic VFSNode to target Directory copy/move */ 
 	protected VFSNode doCopyMove(VFSNode sourceNode,VDir parentDir, String optNewName, boolean isMove) 
 	            throws VrsException
 	{
 	    try
 	    {
     		// resolve Opt New Name: 
     		optNewName=(optNewName==null?sourceNode.getBasename():optNewName);
     		VRL targetVRL=parentDir.resolvePath(optNewName);
     		VFSTransfer transferInfo = newTransfer(null,sourceNode,targetVRL,isMove); 
     		VFileSystem fs=parentDir.getFileSystem();
     		// create new object: 
     		return masterCopyMoveTo(transferInfo,sourceNode,fs,targetVRL,isMove,null);
 
 	    }
         catch (VrsException e)
         {
             throw e;
         }
         catch (IOException e)
         {
             throw new NestedIOException(e); 
         }
         catch (Exception e)
         {
             throw new VrsException(e); 
         }
 	}
 
 	/**
 	 * Master Copy of all Copy/Move methods. <br>
 	 * <p>
 	 * ================================================================================
 	 * <p>
 	 * Initiate a Copy or Move with the specified Transfer Information object.
 	 * Master copy assumes it is the only copy/move action within the 'current'. 
 	 * VFSTransfer so it will update this transfer accordingly.  
 	 * This master of all (synchronous) Copy/Move methods() in VFile and VDir. 
 	 * Asynchronous: This method fires Childs Added/Deleted Events !
 	 * <p>
 	 * =================================================================================
 	 * @param interactor 
 	 * @param copyInteractor 
 	 */
 	protected final VFSNode masterCopyMoveTo(VFSTransfer transfer,
 	        VNode source,
 	        VFileSystem targetFS,
 	        VRL targetVRL, 
 	        boolean isMove, ICopyInteractor interactor) throws Exception
 	{
 		VFile sourceFile=null; 
 		VDir sourceDir=null; 
 		VFSNode newnode=null;
 		boolean fileCopy=false;
 		boolean dirCopy=false;
 		boolean nodeCopy=false;  
 		String taskStr="Transfering."; 
 		
 		// unknown
 		long totalTodo=-1; 
         VFSNode vfsSource=null; 
         
 		if ((source instanceof VFSNode)==true)
 		{
 		    nodeCopy=false; 
 		    vfsSource=(VFSNode)source;
 		    
             if (vfsSource.isFile())
                 fileCopy=true; 
             else vfsSource.isDir();
                 dirCopy=true;  
 		}
 		else
 		{
 		    nodeCopy=true; 
 		}
  
 		if (nodeCopy==true)
 		{
 		    taskStr="Downloading resource.";  
    		}
 		else if (fileCopy==true)
 		{
 		    // single file transfer. Ony here can this be detected. 
 		    // the other doCopyMoveTo method are not aware if it is a 
 		    // single file transfer or a recursive directory copy !
 		    sourceFile=(VFile)source;
 		    totalTodo=sourceFile.getLength();
 		    taskStr="File transfer.";  
 		}
 		else
 		{
 		    sourceDir=(VDir)source;
 		    taskStr="Directory transfer.";  
 		}
 	    
 		try
 		{
 		    
 		    if (fileCopy==true) 
 		    {
 		        // use Filesystem method to create new object
 		        VFile targetFile=targetFS.newFile(targetVRL);
 		        targetFile=interactiveCopyMove(transfer,sourceFile,targetFile,isMove,interactor);
 		        newnode=targetFile; 
 		    }    
 		    else if (dirCopy==true)
 		    {
 		        VDir targetDir=targetFS.newDir(targetVRL); 
 		        BooleanHolder skipH=new BooleanHolder(false); 
                 targetDir=checkTarget(interactor,source,targetDir,skipH);
 
                 if (skipH.value!=true)
                 	newnode=doDirCopyMove(transfer,sourceDir,targetDir,isMove,interactor);
                 else
                 {
                     transfer.logPrintf("Skipping directory:\n - %s\n",sourceDir.getBasename()); 
                 	newnode=targetDir;
                 }
 		    }
 		    else // if (nodeCopy==true)
             {
                 // use Filesystem method to create new object
                 VFile targetFile=targetFS.newFile(targetVRL);
                 BooleanHolder skipH=new BooleanHolder(false); 
                 // this.interactiveCopyMove(transfer, source, targetFile, isMove, interactor)
                 targetFile=checkTarget(interactor,source,targetFile,skipH);
                 
                 if(skipH.value==false)
                 	newnode=this.putAnyNode(transfer,targetFile.getParent(),source,targetFile.getBasename(),isMove);
                 else
                 {
                     transfer.logPrintf("Skipping resource:\n - %s\n",targetFile.getBasename()); 
                 	newnode=targetFile;
                 }
             }
 
 		    //transfer.markStopped();
 		    
 		    //transfer.printReport(System.err);
 		    // Warning: set ResultNode before calling setDone, since setDone
 		    // wakes up waiting threads ! 
 		    //transfer.setResultNode(newnode);
 
 		    if (newnode==null)
 		        throw new ResourceCreationFailedException("NULL Pointer Exception: get NULL as resulting file"); 
 
 		    // ===
 		    // Result is a single file: Update single transfer statistics!
 		    // ===
 		    
 		    if (newnode instanceof VFile)
 		    {
 		        // refetch Actual File length 
 		        //transfer.setTotalTransferred(((VFile)newnode).getLength());
 		        long len=((VFile)newnode).getLength();
 		        
 		        transfer.setTotalWorkTodo(len); 
 		        transfer.updateTaskDone(len);
 		        transfer.updateSourcesDone(1);
 		        transfer.setSubTaskTodo(len); 
 	            transfer.updateSubTaskDone(len);
 		    }
 		    //else VDir: dir copy already update statistics 
 		    
 		    // master stop and start are now called in a higher context 
 		    //transfer.endTask(); // setDone();
 
 		    if (logger.hasEffectiveLevel(ClassLogger.DEBUG))
 		        transfer.printReport(VletConfig.getRootLogger());  
 
 		    // ===================================================================
 		    // ResourceEvent => update resources ! 
 		    // ====================================================================
 		    
 		    // File VRL optimalization: use parent VRL as parent ! 
 		    this.fireNewChild(newnode.getParentLocation(),newnode.getVRL());
 		    
 		    if (isMove)
 		        this.fireNodeDeleted(source.getVRL(),source.getParentLocation()); 
 	
 		    return newnode; 
 		}
 		catch (Throwable tr)
 		{
 		    //transfer.markStopped();
 
 		    if (tr instanceof Exception)
 		    {
 		        transfer.setException(tr);
 		        //transfer.endTask(); //transfer.setDone();    
 		        
 		        if (logger.hasEffectiveLevel(ClassLogger.DEBUG))
 	                transfer.printReport(VletConfig.getRootLogger());  
 
 		        throw ((Exception)tr); 
 		    }
 		    else
 		    {
 		        // Exception Chaining: Throwable/Exception to Exception 
 		        Exception ex = new NestedIOException(tr.getMessage(),tr); 
 		        transfer.setException(ex);
 		        transfer.endTask(taskStr); //transfer.setDone();
 		        
 		        if (logger.hasEffectiveLevel(ClassLogger.DEBUG))
                     transfer.printReport(VletConfig.getRootLogger());  
 
 		        throw ex;
 		    }
 		}
 	}
 
 	private VFile interactiveCopyMove(VFSTransfer transfer, VFile sourceFile,
 			VFile targetFile, boolean isMove, ICopyInteractor interactor) throws Exception
 	{
 		  
 		BooleanHolder skipH=new BooleanHolder(false); 
 		
         if (interactor!=null)
         	targetFile=checkTarget(interactor,sourceFile,targetFile,skipH);
         
         if (skipH.value!=true)
         {
         	final String actionText=(isMove)?"Moving":"Copying"; 
     		transfer.logPrintf(actionText+" file:\n - %s\n",sourceFile.getVRL()); 
 
         	masterFileCopyMove(transfer,sourceFile, targetFile, isMove);
         }
     	else
         {
     		transfer.logPrintf("Skipping file:\n - %s\n",sourceFile); 
         }
 		return targetFile;
 	}
 
 	private VFile checkTarget(ICopyInteractor interactor,
 			VNode source,
             VFile targetFile,
             BooleanHolder skipHolder) throws Exception
     {
 	    // === PRE === // 
 	    if (interactor==null)
 	        return targetFile;
 	    
         VRL targetVRL=targetFile.getVRL(); 
         
         if (targetFile.exists()==false)
             return targetFile; 
         
         Attribute sourceAttrs[]=null; 
         Attribute targetAttrs[]=null; 
         
         do
         {
         	String message = "Resource exists: ("+targetFile.getHostname()+")"+targetFile.getBasename()+"\n" 
              	            +"Destination location ="+targetFile+"\n"; 
             
         	if (source instanceof VFile)
         	{	
         		sourceAttrs=new Attribute[2];
         		
         		VFile sourceFile=(VFile)source;
         		
         		sourceAttrs[0]=sourceFile.getAttribute(VAttributeConstants.ATTR_FILE_SIZE);  
         		sourceAttrs[1]=sourceFile.getAttribute(VAttributeConstants.ATTR_MODIFICATION_TIME);
         	}
         	
         	targetAttrs=new Attribute[2];
         	targetAttrs[0]=targetFile.getAttribute(VAttributeConstants.ATTR_FILE_SIZE);  
         	targetAttrs[1]=targetFile.getAttribute(VAttributeConstants.ATTR_MODIFICATION_TIME);
         	
             StringHolder newName=new StringHolder(targetVRL.getBasename());
                 
             InteractiveAction result=interactor.askTargetExists(message,
             		    source.getVRL(),
             		    sourceAttrs,
                         targetFile.getVRL(),
                         targetAttrs,
                         newName);  
             
             switch(result)
             {
                 case SKIP:
             	   skipHolder.value=true; 
             	   return targetFile;
             	   // break;
             	case CANCEL: 
             		throw new ResourceAlreadyExistsException("Will not overwrite existing file:"+targetVRL);
             		//break; 
             	case CONTINUE: 
             		return targetFile;
             		//break; 
         		case RENAME:  	
         		{
         			if ((newName==null) || (newName.value==null))
         				throw new ResourceAlreadyExistsException("New name can not be NULL");
                 
         			VRL newFileVrl=new VRL(targetFile.getVRL().getParent().appendPath(newName.value)); 
         			targetFile=targetFile.getFileSystem().newFile(newFileVrl);
         			break; 
         		}
         		default: 
         			throw new ResourceAlreadyExistsException("Will not overwrite existing file:"+targetVRL);
         			//break; 
             }
 
         } while(targetFile.exists()==true); 
         
         return targetFile;         
     }
 	
 	private VDir checkTarget(ICopyInteractor interactor,
 			VNode source,
             VDir targetDir,
             BooleanHolder skipH) throws Exception
     {
 	    if (interactor==null)
             return targetDir; 
 	    
         VRL targetVRL=targetDir.getVRL(); 
         
         if (targetDir.exists()==false)
             return targetDir;
         
         do
         {
             String message="Directory already exists:"+targetVRL+"\n";   
             StringHolder newName=new StringHolder(targetVRL.getBasename());
             
             InteractiveAction result = interactor.askTargetExists(message, 
                         source.getVRL(),
                         null,
                         targetDir.getVRL(),
                         null,
                         newName);   
             
             switch(result)
             {
             	case CANCEL: 
             		throw new ResourceAlreadyExistsException("Will not overwrite existing directory:"+targetVRL);
             	case SKIP:
             		skipH.value=true;
             		return targetDir;
             	case CONTINUE: 
             		return targetDir;
             	case RENAME: 
             	{
             		if ((newName==null) || (newName.value==null))
             			throw new ResourceAlreadyExistsException("New name can not be NULL");
                 
             		VRL newDirVrl=targetDir.getVRL().getParent().appendPath(newName.value); 
             		targetDir=targetDir.getFileSystem().newDir(newDirVrl);
             		break; 
             	}
             	default:  
             		throw new ResourceAlreadyExistsException("Will not overwrite existing directory:"+targetVRL);
             }
 
         } while(targetDir.exists()==true);
         
        return targetDir; 
     }
 
     protected VFSNode doCopyMove(VFSNode sourceNode,VRL targetVRL, boolean isMove) 
             throws VrsException
 	{
         try
         {
     		VFSTransfer transferInfo = newTransfer(null,sourceNode,targetVRL,isMove);
     
     		// use new FileSystem object
     		VFileSystem fs=VFS.openFileSystem(vrsContext,targetVRL); 
     
     		return masterCopyMoveTo(transferInfo,sourceNode,fs,targetVRL,isMove,null);
         }
         catch (VrsException e)
         {
             throw e;
         }
         catch (IOException e)
         {
             throw new NestedIOException(e); 
         }
         catch (Exception e)
         {
             throw new VrsException(e); 
         }
     }
 
 	/**
 	 * Copies multiple resources from different locations into target directory. 
 	 * All new resources are created as childs (sub file/directories) in the target
 	 * directory. 
 	 * Helper method for VBrowser's multi copy drop. 
 	 * @param vrls
 	 * @param targetDirectory
 	 * @param isMove
 	 * @return VFSTransfer object for monitoring purposes. 
 	 * @throws Exception 
 	 */
     public VFSTransfer asyncMultiCopyMove(ITaskMonitor optParentMonitor,
              final VRL[] vrls, 
              final VRL targetParentDirVrl,
              final boolean isMove,
              final ICopyInteractor interactor
             ) throws Exception
     {
         final VFSTransfer transfer=new VFSTransfer(optParentMonitor,"MultiCopy",vrls,targetParentDirVrl,isMove); 
         transfer.setTotalWorkTodo(0); // init! 
         transfer.setMultiTransfer(true); 
         //transfer.setInteractor(interactor); 
         
         final VRSContext context=this.vrsContext; 
         final String actionText=(isMove)?"Moving":"Copying"; 
         
         ActionTask transferTask = new ActionTask(this.getTaskWatcher(),"MultiTransferTask",transfer)
         {
             public void doTask() throws Exception
             {
                 VDir targetParentDir=context.openFileSystem(targetParentDirVrl).getDir(targetParentDirVrl); 
                                 
                 ArrayList<VNode> nodes=new ArrayList<VNode>(); 
                 for (VRL vrl:vrls)
                 {
                     nodes.add(context.openLocation(vrl));  
                 }
                 
                 for (VNode node:nodes)
                 {
                     transfer.setCurrentSource(node.getVRL()); 
                     
                     if (node instanceof VDir)
                     {
                         String name=node.getName(); 
                         
                         VDir targetDir=targetParentDir.newDir(name);
                         
                         BooleanHolder skipH=new BooleanHolder(false); 
                         targetDir=checkTarget(interactor,node,targetDir,skipH);
                         
                         if (skipH.value!=true)
                         {
                         	doDirCopyMove(transfer,(VDir)node,targetDir,isMove,interactor); 
                         	transfer.logPrintf(actionText+" directory:\n - %s\n",node.getVRL()); 
                         }
                         else
                         {
                         	transfer.logPrintf("Skipping directory:\n - %s\n",node.getVRL()); 
                         }
                     }
                     else if (node instanceof VFile)
                     {
                         VFile targetFile=targetParentDir.newFile(node.getBasename());
         		        targetFile=interactiveCopyMove(transfer,((VFile)node),targetFile,isMove,interactor);
                     }
                     else
                     {
                         putAnyNode(targetParentDir,node,null,isMove); 
                     }
                     
                     transfer.addSourcesDone(1);
                 }
             }
 
             @Override
             public void stopTask()
             {
                 transfer.setIsCancelled(); 
             }
         };
         
         //transferTask.setTaskMonitor(transfer); // set taskmonitor ! 
         transferTask.startTask(); 
         return transfer; 
     }
     
     // Wrapper method for VBrowser to CopyDrop a single resource 
     public VFSTransfer asyncCopyMove(ITaskMonitor optParentMonitor,
             final VRL sourceVrl,
             final VRL targetDirVRL,
             String optNewName,
             final boolean isMove, 
             ICopyInteractor copyInteractor) throws Exception
     {
         VNode node=this.vrsContext.openLocation(sourceVrl); 
         
         if (node instanceof VLogicalResource)
         {
             VRL storageVrl=((VLogicalResource)node).getStorageLocation();
             if (storageVrl==null)
                 throw new nl.esciencecenter.vlet.exception.ResourceTypeNotSupportedException("Can not copy logical nodes without a storage location:"+node);
             
             // use storage location as source!
             node=this.vrsContext.openLocation(storageVrl); 
         }
         
         // Resolve Target VRL:
         VRL targetVRL=this.createTargetVRL(targetDirVRL,node,optNewName);  
         
 //        if ((node instanceof VFSNode)==false) 
 //            throw new nl.uva.vlet.exception.ResourceTypeNotSupportedException("Can only copy VFS resources:"+node); 
 //        
         VFileSystem targetFS = this.vrsContext.openFileSystem(targetVRL); 
         
         return asyncCopyMoveTo(optParentMonitor,node,targetFS,targetVRL,isMove,copyInteractor); 
     }
     
 	public VFSTransfer asyncCopyMoveTo(ITaskMonitor optParentMonitor,
 			final VNode node,
 	        final VFileSystem targetFS,
 			final VRL targetVRL, 
 			final boolean isMove, 
 			final ICopyInteractor copyInteractor) throws VrsException
 	{
 	    try
 	    {
     		final VFSTransfer transfer=newTransfer(optParentMonitor,node,targetVRL,isMove); 
     		// ransfer.setInteractor(copyInteractor); 
     		
     		ActionTask transferTask = new ActionTask(this.getTaskWatcher(),"SingleTransferTask",transfer)
     		{
     			public void doTask() throws Exception
     			{
     			    try
     			    {
     			        masterCopyMoveTo(transfer,node,targetFS,targetVRL,isMove,copyInteractor);
     			    }
     			    catch (Exception e)
     			    {
     			        this.setException(e); 
     			        ITaskMonitor monitor = this.getMonitor();
     			        if (monitor!=null)
     			        {    
     			            monitor.logPrintf("*** Exception ***\n%s\n", e.getMessage()); 
     			        }
     			        throw e; 
     			    }
     			}
     
     			@Override
     			public void stopTask()
     			{
     				transfer.setIsCancelled(); 
     			}
     		};
     
     		// Set monitor BEFORE starting the task:
     			// This Action Task will contain (as in own) the VFSTransferMonitor
     		// It can be fetched using ActionTask.getCurrentThreadActionTask();
     		//transferTask.setTaskMonitor(transfer); // set taskmonitor ! 
     		transferTask.startTask(); 
     
     		return transfer; 
 	    }
 	    catch (VrsException e)
 	    {
 	        throw e;
 	    }
 	    catch (IOException e)
 	    {
 	        throw new NestedIOException(e); 
 	    }
 	    catch (Exception e)
 	    {
 	        throw new VrsException(e); 
 	    }
 	}
 
 	// ========================================================================
 	// File Copy Methods 
 	// ========================================================================
 
 	/**
 	 * Main method which does the actual File copy or move.  
 	 * Decides whether to upload, download or perform a stream copy depending
 	 * on the location of this file (local or remote) and the source or the 
 	 * destination can perform an "Active Transfer" for example a Third Party 
 	 * transfer. <br> 
 	 * The default copy method is stream copy.<br>
 	 * <p>
 	 * Copy (Move) Transfer mechanism checked in the following order:  <br>
 	 * <ul>
 	 * <li> I)  Rename : Source and destination are on same server/filesystem and isMove==true  <br>
 	 * <li> II) Active Party Transfer: Check whether source or destination supports 3rd party  <br>
 	 *      transfers or other "active" transfer mechanism. <br>
 	 *     -IIa): Active transfer from source file to remote location. <br> 
 	 *         Source=active, Destination=passive.   <br>
 	 *     -IIb): Active transfer by remote target file from source location. <br>
 	 *         Source=passive, Destination=active. <br>
 	 * <li> III) Upload/Download from and to Local <br>
 	 *      -IIIa) Upload from local to remote <br>
 	 *      -IIIb) Download from remote to local <br> 
 	 * <li> VI)  StreamCopy (default). <br>
 	 * <p>
 	 *
 	 * @param transfer
 	 * @throws Exception
 	 *  
 	 * @see VFile#renameTo(String)
 	 * @see VThirdPartyTransferable#canTransferTo(VRL, StringHolder)
 	 * @see VThirdPartyTransferable#canTransferFrom(VRL, StringHolder)
 	 * @see VFile#uploadFrom(VFSTransfer, VFile)
 	 * @see VFile#downloadTo(VFSTransfer, VFile) 
 	 */
 	protected final void masterFileCopyMove(VFSTransfer transfer,VFile sourceFile,VFile targetFile,boolean isMove) 
 	    throws Exception
 	{
 		debugPrintf("doCopyMove: source=%s\n",sourceFile); 
 		debugPrintf("doCopyMove: dest=%s\n",targetFile); 
 
 		String newpath=targetFile.getPath(); 
 		debugPrintf("newpath=%s\n",newpath);
 
 		// ===========
 		// (I) Check rename optimization in the case of a move on the same server ! 
 		// ===========
 
 		if ( (VRLUtil.hasSameServer(sourceFile.getLocation(),targetFile.getVRL())) && (isMove))
 		{
 			debugPrintf("doCopyMove: (I) Active method= VFile.renameTo()\n");
 
 			if (transfer!=null)
 			    transfer.setVFSTransferType(VFSActionType.RENAME); 
 			
 			boolean result=sourceFile.renameTo(targetFile.getPath(),true);
 
 			if (result==false)
 				throw new nl.esciencecenter.vlet.exception.ResourceCreationFailedException("Rename failed:"+sourceFile);
 
 			return; 
 		}
 
 		// ==============
 		// (II) 3rd party or other Active File Transfer Mode ! Check Optimized file transfers 
 		// Remote -> Remote : Check Active File Transfer  
 		// ==============
 
 		boolean checkActiveParty=true; 
 		
 		// (IIIa) Check active transfer from file to other: 
 		if (checkActiveParty)
 		{
 			if (doActiveFileTransfer(transfer,sourceFile,targetFile,null))
 				return; // SUCCCEEDED 
 		}
 
 		// ==============
 		// (IIIa) Check Optimized Upload 
 		// Copy Local -> Remote = (Bulk) Upload 
 		// ==============
 
 		if  ((sourceFile.isLocal()==true)  && (targetFile.isLocal()==false))
 		{
 			debugPrintf("doCopyMove: (IIIa) Active Method: dest.uploadLocalFile()\n");
 		    if (transfer!=null)
                 transfer.setVFSTransferType(VFSActionType.UPLOAD); 
 
 			targetFile.uploadFrom(transfer,sourceFile);
 
 			if (isMove)
 				sourceFile.delete(); 
 
 			return; 
 		}
 
 		// ==============
 		// (IIIb)  Optimized Download 
 		// Copy Remote -> Local = Download 
 		// ==============
 
 		if ((sourceFile.isLocal()==false) && (targetFile.isLocal()==true))
 		{
 			// optional optimized (bulk) download method
 			debugPrintf("doCopyMove: (IIIb) Active Method: file.downloadTo()\n");
 		    if (transfer!=null)
                 transfer.setVFSTransferType(VFSActionType.DOWNLOAD); 
 
 			sourceFile.downloadTo(transfer,targetFile);
 
 			if (isMove)
 				sourceFile.delete();
 
 			return;
 		}
 
 		// ====================================
 		// Default Stream Copy Transfer 
 		// (V)
 		// for stream copy to work both local and destination must
 		// support streams 
 		// ====================================
 
 		if (true) // New Copy Policy under construction 
 		{
 			debugPrintf("doCopyMove: activeMethod: file.streamCopy()\n"); 
 			if (transfer!=null)
 			    transfer.setVFSTransferType(VFSActionType.STREAM_TRANSFER); 
 
 			doStreamCopy(transfer,sourceFile,targetFile);
 
 			if (isMove)
 				sourceFile.delete();
 
 			return; 
 		}
 //		else
 //		{
 //		    throw new nl.uva.vlet.exception.Exception(
 //				"There is no (File) Transfer Policy defined to perform the following transfer:\n"
 //				+"source="+sourceFile
 //				+"destination="+targetFile);
 //		}
 	}
 	
 	/**
 	 * Helper method which performs active transfer if possible between sourceFile and targetFile. 
 	 *   
 	 * @return false if not possible, true after a successful transfer.
 	 * @throws Exception when active transfer is possible but failed !
 	 */ 
 	public boolean doActiveFileTransfer(ITaskMonitor monitor,VFile sourceFile,VFile targetFile, StringHolder reasonH) throws Exception
 	{
 	    VFSTransfer transfer=null;
 	    
 	    if (monitor instanceof VFSTransfer)
 	        transfer=(VFSTransfer)monitor;
 	    
 	   // 3rd party from file to remote:
 		ActiveTransferType canTransferToOther=ActiveTransferType.NONE; 
 		ActiveTransferType canTransferFromOther=ActiveTransferType.NONE; 
 		VRL targetVRL=targetFile.getVRL(); 
 		VRL sourceVRL=sourceFile.getVRL(); 
 
 		if (reasonH==null)
 			reasonH=new StringHolder(); 
 	
 		VFile resultFile=null; 
 		String toStr=null;
 		String fromStr=null;  
 		VFileSystem sourceVFS=sourceFile.getFileSystem(); 
 		VFileSystem targetVFS=targetFile.getFileSystem(); 
 		
 		if (sourceVFS instanceof VFileActiveTransferable)
 		{    
 			//
 			// IIa) Check file::canTransferTo (target) 
 			//
 			canTransferToOther=((VFileActiveTransferable)sourceVFS).canTransferTo(sourceFile,targetVRL, reasonH);
 			if (canTransferToOther==ActiveTransferType.ACTIVE_3RDPARTY)
 			{
 				//transfer.addLogText("Source can perform active (3rd party) transfer to:"+targetVRL+"\n"); 
 				logger.infoPrintf("Source can perform Active (3rd party) File Transfer to:%s\n",targetVRL); 
 				toStr=reasonH.value; 
 				logger.infoPrintf(" - Explanation:%s\n",toStr); 
 			}
 		}
 	
 		if (targetVFS instanceof VFileActiveTransferable)
 		{    
 			//
 			// IIb) Check targetFile::canTransferFrom (file); 
 			//
 			canTransferFromOther=((VFileActiveTransferable)targetVFS).canTransferFrom(targetFile,sourceVRL,reasonH);
 			if (canTransferFromOther==ActiveTransferType.ACTIVE_3RDPARTY)
 			{
 				logger.infoPrintf("Destination can perform Active (3rd party) File Transfer from:%s\n",sourceVRL); 
 				fromStr=reasonH.value; 
 				logger.infoPrintf(" - Explanation:%s\n",fromStr); 
 				//transfer.addLogText("Destination can perform active (3rd party) transfer from:"+sourceVRL+"\n"); 
 				//transfer.addLogText(" - "+explanation.value+"\n"); 
 			}
 		}
 	
 		if ((canTransferToOther==ActiveTransferType.ACTIVE_3RDPARTY) && ((canTransferFromOther==ActiveTransferType.ACTIVE_3RDPARTY))) 
 		{
 			//transfer.addLogText("Both source and target support active (3rd party) file transfer methods\n"); 
 			logger.infoPrintf("Both source and target support Active (3rd party) File Transfer methods\n"); 
 		}
 	
 		if (canTransferToOther==ActiveTransferType.ACTIVE_3RDPARTY)
 		{
 		    if (transfer!=null)
                 transfer.setVFSTransferType(VFSActionType.ACTIVE_THIRD_PARTY_TRANSFER_BY_SOURCE);  
             
         	monitor.logPrintf("VFS: Initiating 3rd party transfer with active Source: source => target.\n");  
 			resultFile=((VFileActiveTransferable)sourceVFS).activeTransferTo(monitor,sourceFile,targetVRL);
 			return true; 
 		}
 		else if (canTransferFromOther==ActiveTransferType.ACTIVE_3RDPARTY)
 		{
 		    if (transfer!=null)
                 transfer.setVFSTransferType(VFSActionType.ACTIVE_THIRD_PARTY_TRANSFER_BY_DESTINATION);  
         
 			monitor.logPrintf("VFS: Initiating 3rd party transfer with active Target: target <= source.\n");  
 			resultFile=((VFileActiveTransferable)targetVFS).activeTransferFrom(monitor,targetFile,sourceVRL); 
 			return true;  
 		}
 		else
 		{
 			reasonH.value="Neither Target not Source can perform Active (File) Transfers!";
 			debugPrintf("No active (3rd) party transfers possible ...\n"); 
 			return false; 
 		}
 	}	
 
 	private void debugPrintf(String msg,Object ... args)
 	{
 		logger.debugPrintf(msg,args); 
 	}
 
 	// ========================================================================
 	// Node Copy Methods 
 	// ========================================================================
 	/**
 	 *  Default VFile stream copy uses VNode to VNode stream Copy.
 	 *  
 	 *  @see nl.esciencecenter.vlet.vrs.util.VRSIOUtil.IOUtil#streamCopy(VFSTransfer, VNode, VNode, int)
 	 */ 
 	protected void doStreamCopy(VFSTransfer transfer,
 			VNode sourceNode, 
 			VNode destNode)  throws VrsException
 	{
 	    try
 	    {
     	    long len=-1; 
     	    
     	    if (sourceNode instanceof VFile)
     	    {
     	       len=((VFile)sourceNode).getLength(); 
     	    }
     	    
     		VRSIOUtil.streamCopy(transfer,sourceNode,destNode,len,VFS.DEFAULT_STREAM_COPY_BUFFER_SIZE); 
 	    }
 	    catch (IOException e)
 	    {
 	        throw new NestedIOException(e); 
         }
 	    catch (Exception e)
         {
 	        throw new VrsException(e); 
         }
     }
 
 	protected VFile putAnyNode(VDir dir, VNode sourceNode, String optNewName,
 			boolean isMove)  throws VrsException
 	{
 		VFSTransfer transfer=new VFSTransfer(null,sourceNode.getResourceType(), sourceNode.getVRL(),dir.getVRL(), isMove);
 		return putAnyNode(transfer,dir,sourceNode,optNewName,isMove); 
 	}
 	
 	/** Copy any node and seem to do what is the right thing */ 
 	protected VFile putAnyNode(VFSTransfer transfer,VDir dir, VNode sourceNode, String optNewName,
 	        boolean isMove)  throws VrsException
 	{
 	    if (sourceNode.isComposite()) 
 	    {
 	        // Non VDir but has composite resources: to be done. 
 	        throw new nl.esciencecenter.vlet.exception.ResourceTypeMismatchException("Can not copy resource:"+sourceNode);
 	    }
 	    else
 	    {
 	        //single source must be stream readable 
             if ((sourceNode instanceof VStreamReadable)==false)
                 throw new nl.esciencecenter.vlet.exception.ResourceTypeMismatchException("Can not read contents from:"+sourceNode);
 	    }
 	    
 		VRL destVRL=this.createTargetVRL(dir.getVRL(),sourceNode,optNewName); 
 		
 		// Create Target Object 
 		VFile destFile=dir.getFileSystem().newFile(destVRL); 
 
 		if (isMove) 
 			if ((sourceNode instanceof VDeletable)==false)
 				throw new ResourceTypeMismatchException("Source is not deletable, cannot move it (use copy instead):"+sourceNode); 
 
 		doStreamCopy(transfer,sourceNode,destFile);
 
 		if (isMove) 
 		{
 			((VDeletable)sourceNode).delete();
 		}
 
 		return destFile; 
     }
 
 	// ========================================================================
 	// Directory Copy Methods 
 	// ========================================================================
 
 	/**
 	 * Helper method to ensure name consitancy when dropping/copying nodes between resources. 
 	 * VBrowser used this method as well to check whether a resource already exists, by
 	 * first creating the targetVRL of the new-to-be-created resource.   
 	 */
 	public VRL createTargetVRL(VRL targetDirVrl,VNode node,String optNewName) throws VrsException
 	{
 	    if (StringUtil.isEmpty(optNewName))
         {
             // use default name 
             optNewName=node.getName();
 
             // ===
             // VLogicalResource Handling 
             // Argg must get valid filename since ResourceNode might have "://" in it!
             // or other strange characters:
             // ===
             if (node instanceof VLogicalResource)
             {
                 // TODO: better name convention (name must supplied by calling code!) 
                 // use file name of Storage Location: 
                 VRL storage=((VLogicalResource)node).getStorageLocation();
                 // ResourceNode might not have a storage name:
                 if (storage!=null)
                     optNewName=storage.getBasename();
 
             }
 
             // === 
             // HTTPNode handling 
             // === 
             // Another patch: When a HTTPNode is dropped, behave like 
             // wget and use modified url, again name should be provided at earlier stage
             // for example by an interactive menu ("download selection as..."):
             // this is just a patch to be robuust. 
             else if (node instanceof HTTPNode)
             {
                 String str=node.getVRL().toString();  
                 str=str.replaceAll("[/\\*&%$:;~`']+","_");
                 optNewName=str;
                 debugPrintf("new file name from http=%s\n",optNewName); 
             }
         }
 	    
 	    return targetDirVrl.appendPath(optNewName); 
 	}
 	
 //	/**
 //	 * Deprecated! 
 //	 * 
 //	 * Default Copy/Move: performs recursive copy of a directory.<br> 
 //	 * lists children and performs doCopyMoveTo/defaultCopyMove on childs.<br>
 //	 * 
 //	 * @deprecated Use HeapCopy 
 //	 */ 
 //	// packace modifier:
 //	protected VDir doRecursiveDirCopy(VFSTransfer transfer,VDir source,VDir dest,String optNewName) 
 //	throws Exception
 //	{
 //		if (transfer.getMustStop())
 //		{
 //			throw new VlInterruptedException("Interrupted"); 
 //		}
 //
 //		VFSNode nodes[]=source.list();
 //
 //		// default copyMove behaviour is to overwrite existing directories 
 //
 //		VDir newVDir=dest.createDir((optNewName!=null?optNewName:source.getName()),true);
 //		dest.getVRSContext().fireEvent(
 //				ResourceEvent.createChildAddedEvent(dest.getVRL(), newVDir.getVRL())); 
 //
 //		if (newVDir==null)
 //		{
 //			throw new ResourceCreationFailedException("Could not create destination directory in:"+dest); 
 //		} 
 //
 //		transfer.logPrintf("Recursing directory:"+source+"\n");
 //		transfer.startSubTask("Recursing directory:"+source.getName(),-1L);
 //
 //		// go multithreaded? : ActionTask tasks[]=new ActionTask[nodes.length]; 
 //		if (nodes!=null)
 //		{
 //			// update total to transfer: 
 //			int val=transfer.getTotalSources(); 
 //			val+=nodes.length;
 //			transfer.setTotalSources(val); 
 //
 //
 //			for (VFSNode node:nodes)
 //			{
 //
 //				if (transfer.getMustStop())
 //				{
 //					throw new VlInterruptedException("Interrupted"); 
 //				}
 //
 //				if (transfer.getMustStop())
 //					return newVDir; 
 //
 //				VNode newNode; 
 //
 //				if (node instanceof VDir)
 //				{
 //					// Recurse Copy Dir: 
 //					newNode=doRecursiveDirCopy(transfer,((VDir)node),newVDir,null); 
 //				}
 //				else if (node instanceof VFile) 
 //				{
 //					// Copy File: 
 //
 //					// tasks[i]=new ActionTask(){  doTask(...
 //					VFile file=(VFile)node;
 //					// use new FileSystem methods
 //					VRL targetVRL=newVDir.resolvePathVRL(node.getBasename()); 
 //					VFile targetFile=newVDir.getFileSystem().newFile(targetVRL);
 //					masterFileCopyMove(transfer,file, targetFile, false);
 //					//file.doCopyMoveTo(transfer,newVDir,null,isMove);
 //
 //					// update with file size:
 //					long newSize=transfer.getTotalWorkDone()+file.getLength();
 //					// when transfering a diectory total work done 
 //					// is total amount of bytes transferred. 
 //					transfer.updateWorkDone(newSize);
 //					newNode=targetFile;
 //
 //				} 
 //				else
 //					throw new nl.uva.vlet.exception.ResourceTypeNotSupportedException("Cannot copy: Unkown node type:"+node); 
 //
 //				// New: use Asynchronous Update to the VBrowser ! 
 //				newVDir.getVRSContext().fireEvent(
 //						ResourceEvent.createChildAddedEvent(newVDir.getVRL(), newNode.getVRL())); 
 //
 //			}// for node:nodes: 
 //		}
 //
 //		return newVDir; 
 //	}
 
 	/** 
 	 * Heap Directory copy, first get all the contents, calculates statistics, then performs the heap copy
 	 * The TargetDirVRL specifies the target directory as to be created. It is NOT the parent 
 	 * of the target directory !  
 	 */ 
 	protected VDir doHeapDirCopy(VFSTransfer newTransfer,
 			VDir sourceDir,
 			VFileSystem targetFS,
 			VRL targetDirVRL,
 			ICopyInteractor interactor)
 	    throws Exception
 	{
 		if (newTransfer.isCancelled())
 		{
 			throw new NestedInterruptedException("Cancelled!"); 
 		}
 
 		// =======================================
 		// === Pre Stage: get contents in one heap 
 		// =======================================
 
 		newTransfer.logPrintf("Checking contents of directory: '%s'\n",sourceDir.getBasename());  
 		newTransfer.startSubTask("Reading contents",1); 
 
 		LongHolder sizeHolder=new LongHolder(); 
 		// depth first tree walk. 
 		Vector<VNode>vnodeHeap=new Vector<VNode>(); 
         
 		listRecursive(newTransfer,sourceDir,vnodeHeap,sizeHolder,true); 
 
 		// convert VNode to VFSNode since the source is always a VDir!
 		
 		Vector<VFSNode>heap=new Vector<VFSNode>(); 
 		for (int i=0;i<vnodeHeap.size();i++)
 		{
 		    heap.add((VFSNode)vnodeHeap.get(i)); 
 		}
 		
 		newTransfer.updateSubTaskDone(1); 
 		newTransfer.endSubTask("Reading contents");
 		newTransfer.logPrintf("Total contents to copy is: #"+heap.size()+" files and directories ("+sizeHolder.value+" bytes)\n");
 		long todo=sizeHolder.value; 
 		
 		long totalWorkStart=newTransfer.getTotalWorkTodo();
 		if (totalWorkStart<0)
 		    totalWorkStart=0;
 		
 		// INCREMENT extra work: 
 		newTransfer.setTotalWorkTodo(totalWorkStart+todo); 
 
 		// =======================================
 		// Heap Copy  
 		// =======================================
 
 		// default copyMove behaviour is to overwrite existing directories 
 
 		VDir targetDir=targetFS.newDir(targetDirVRL);
 		boolean result=targetDir.create(true);
 		
 		if (result==false)
 		{
 			throw new ResourceCreationFailedException("Could not create destination directory:"+targetDirVRL); 
 		} 
 		
 		// fire event using target VRSContext ! 
 		targetFS.getVRSContext().fireEvent(
 				ResourceEvent.createChildAddedEvent(targetDir.getVRL(), targetDir.getVRL())); 
 
 		newTransfer.logPrintf("Preforming heap copy of:'"+sourceDir+"'\n");
 		newTransfer.startTask("Preforming heap copy of:"+sourceDir.getName(),sizeHolder.value);
 		
 		//
 		// Bulk Copy Optimalization Here ? 
 		// targetFS.bulkCopy(tagetDirVRL,nodes); 
 		//
 		int index=heap.size(); 
 
 		for (index=0;index<heap.size();index++)
 		{
 			VNode node=heap.get(index); 
 
 			if (newTransfer.isCancelled())
 				throw new NestedInterruptedException("Cancelled!"); 
 
 			if ((node==null) || (node.equals(sourceDir)))
 				continue; // first element = sourceDir! ; 
 
 			// get relative path starting from source directory path: 
 			String relPath=VRLUtil.isSubPath(sourceDir.getPath(),node.getPath());
 			// full path VRL of directory or file: 
 			VRL targetPath=targetDir.resolvePath(relPath);
 
 			debugPrintf("Relative Path=%s\n",relPath);
 			debugPrintf("Target Path=%s\n",targetPath);
 
 			if (node instanceof VDir) 
 			{
 				VDir resultDir = targetFS.newDir(targetPath); 
 				result=resultDir.create(true); 
 				 
 				newTransfer.logPrintf("Created new directory:'"+resultDir+"'\n");
 				// asynchronous update
 				this.fireNewChild(resultDir.getVRL().getParent(),resultDir.getVRL()); 
 			}
 			else if (node instanceof VFile) 
 			{
 				// tasks[i]=new ActionTask(){  doTask(...
 				VFile file=(VFile)node;
 				VFile targetFile=targetFS.newFile(targetPath);
 				
 				// update subTask ! 
 				debugPrintf("file copy:%s to: %s\n",file,targetFile); 
 
 				BooleanHolder skipH=new BooleanHolder(false); 
 				
 		        if (interactor!=null)
 		        	targetFile=checkTarget(interactor,file,targetFile,skipH);
 		        
 		        if (skipH.value!=true)
 		        {
 		        	masterFileCopyMove(newTransfer,file, targetFile, false);
 		        }
 				// masterFileCopyMove will update subTask done ! 
 				//masterFileCopyMove(newTransfer,file, targetFile, false);
 				//targetFile=this.interactiveCopyMove(newTransfer, file,targetFile, false, interactor); 
 				
 				//file.doCopyMoveTo(transfer,newVDir,null,isMove);
 				long len=file.getLength(); 
 				// update with file size:
 				long newSize=newTransfer.getTotalWorkDone()+len;
 				// when transferring a directory total work done 
 				// is total amount of bytes transferred. 
 				newTransfer.updateWorkDone(newSize);
 				// asynchronous update
 				this.fireNewChild(targetFile.getVRL().getParent(),targetFile.getVRL());
 				
 				if (skipH.value!=true)
 		        {
 					newTransfer.logPrintf("Copied file:'%s' (size=%d)\n",targetFile,len);
 		        }
 		        else
 		        {
 		        	newTransfer.logPrintf("Skipped file:'%s'\n",targetFile);
 		        }
 		        
 			}
 
 			newTransfer.addSourcesDone(1); // increment;  
 		}
 
 		// ====
 		// POST 
 		// ==== 
 
 
 		return targetDir; 
 	}
 
 	/**
 	 * List directory contents and build heap. 
 	 * Returns heap of VNode so all (V)Composite resources can be listed. 
 	 * @param heap Node Heap which will contain the contents
 	 * @param sourceDir source Directory to scan
 	 * @param totalSize
 	 * @param depthFirstWalk  
 	 */
 	public Vector<VNode> listRecursive(ITaskMonitor monitor,
 	        VComposite sourceDir,
 	        Vector<VNode> heap, 
 	        LongHolder totalSize,
 	        boolean depthFirstWalk) throws Exception
 	{
 		if (totalSize==null) 
 			totalSize=new LongHolder(); 
 
 		heap.add((VNode)sourceDir); 
 		long sumSizes=0;
 		int dirIndex=0;
 		int totalSources=0; 
 		
 		// update monitoring as we work ! 
         if ((monitor!=null) && (monitor instanceof VFSTransfer))
             totalSources=((VFSTransfer)monitor).getTotalSources();
         
         if (totalSources<0)
             totalSources=0; // init
          
 		//
 		// Build heap. Calculate statistics as well.
 		// Recursive List Optimilazation ?  heap=targetFS.listRecursive(sourceDir);
 		// 
 		Vector<VComposite> dirStack=new Vector<VComposite>(); 
 		dirStack.add(sourceDir); 
 
 		do
 		{
 			// get current dir: 
 		    VComposite dir=dirStack.get(dirIndex);   
 			// add children to heap (inclusive directories!) 
 			VNode nodes[]=dir.getNodes();
 			int offset=0;
 			
 			if ((nodes!=null) && (nodes.length>0)) 
 			{
 			    for (VNode node:nodes)
 			    {
     				if (monitor.isCancelled())
     					throw new nl.esciencecenter.vlet.exception.NestedInterruptedException("Interrupted");
     
     				// add to heap: 
     				heap.add(node);
     				// Depth-First Tree Walk !
     				// insert sub directories to dir stack as next dir to process, resulting in a recursive
     				// depth-first tree walk of the directory structure.
     				//
     				if (node instanceof VComposite)
     				{
     					if (depthFirstWalk)
     						dirStack.add(dirIndex+1+offset++,(VComposite)node);
     					else
     						dirStack.add((VComposite)node); // add to heap: breadth first  
     				}
     				else
     				{
     				    if (node instanceof VSize)
     				    {
     				        sumSizes+=((VSize)node).getLength(); 
     				        totalSize.value=sumSizes;
     				    }
     				}
 			    }// for 
 			}// if 
 
 			// update monitoring as we work ! 
 			if ((monitor!=null) && (monitor instanceof VFSTransfer))
 			{
 				// Do a microsoft and change the statistics during a (multi) file copy here.  
 				((VFSTransfer)monitor).updateSources(nodes); //
 			}
 			
 			dirIndex++; // process next dir (if existant)
 
 		}while(dirIndex<dirStack.size()); 
 		totalSize.value=sumSizes; 
 
 		return heap; 
 	}
 
 	/**
 	 * Actual Directory copy method. 
 	 * @param targetDirVrl This is the VRL of the actual directory to be created, NOT the Parent directory. 
 	 * @throws Exception
 	 */
 	protected VDir doDirCopyMove(VFSTransfer transfer,
 			VDir sourceDir, 
 			VDir targetDir,
 			boolean isMove,
 			ICopyInteractor interactor) throws Exception
 	{
 	    VFileSystem targetFS=targetDir.getFileSystem(); 
 	    VRL _targetDirVrl=targetDir.getVRL(); 
 	    
 		// ===
 		// (I) Check rename ! 
 		// ===
 
 		if ((isMove) && (VRLUtil.hasSameServer(sourceDir.getLocation(),_targetDirVrl) ))
 		{
 
 			// java says a move is actually just a rename:
 			boolean result=sourceDir.renameTo(targetDir.getPath(),true);
 
 			if (result==true)
 			{
 				// use filesystem method: 
 				VDir newDir=targetFS.getDir(_targetDirVrl);
 				if (newDir==null)
 					throw new nl.esciencecenter.vlet.exception.ResourceCreationFailedException("Rename succeeded, but could not fetch new dir:"+targetDir);
 
 				return newDir;  
 			}
 			else
 			{
 				throw new nl.esciencecenter.vlet.exception.ResourceCreationFailedException("Rename failed:"+this);
 			}
 		}
 
 		// ===
 		// II) 3rd party transfers ? Use RFTS ? 
 		// ===
 
 		// ===
 		// (IIIa) Check Upload: use Bulk Upload (if possible): 
 		// ===
 
 		VDir parentOfTargetDir=targetFS.newDir(_targetDirVrl.getParent());
 		VDir resultDir=null; 
 
 		if  ( (sourceDir.isLocal()==true)  && (parentOfTargetDir.isLocal()==false))
 		{
 			resultDir=uploadLocalDir(transfer,sourceDir,parentOfTargetDir.getFileSystem(),_targetDirVrl,interactor);
 		}
 
 		// ===
 			// (IIIb) Check Download: use Bulk Download (if possible): 
 		// ===
 
 		else if ((sourceDir.isLocal()==false) && (parentOfTargetDir.isLocal()==true))
 		{
 			// use explicit download method from PARENT 
 			resultDir=downloadDir(transfer,
 					sourceDir,
 					parentOfTargetDir.getFileSystem(),
 					_targetDirVrl,
 					interactor);
 		}
 
 		// ===
 		// (IV) Default Recursive Copy:
 		// ===
 
 		else 
 		{
 			// default heap copy: 
 			resultDir=doHeapDirCopy(transfer,
 					sourceDir,
 					parentOfTargetDir.getFileSystem(),
 					_targetDirVrl,
 					interactor);
 		}
 
 		if (isMove) 
 		{
 			sourceDir.delete(true);
 			this.fireNodeDeleted(sourceDir.getVRL(),sourceDir.getParentLocation()); 
 		}
 
 		return resultDir;  
 
 	}
 
 	// TODO: optimized bulk opload method
 	protected VDir uploadLocalDir(VFSTransfer newTransfer,
 			VDir source,
 			VFileSystem targetFS,
 			VRL targetDirVRL,
 			ICopyInteractor interactor)
 	throws Exception
 	{
 		// default recursive (file by file) copy. 
 		return doHeapDirCopy(newTransfer,source,targetFS,targetDirVRL,interactor); 
 	}
 
 	// TODO: optimized bulk download method 
 	protected VDir downloadDir (VFSTransfer newTransfer,
 			VDir source,
 			VFileSystem targetFS,
 			VRL targetDirVRL,
 			ICopyInteractor interactor) 
 	throws Exception
 	{
 		return doHeapDirCopy(newTransfer,source,targetFS,targetDirVRL,interactor);
 	}
 
 	// Asynchronous updates ! 
 	protected void fireNewChild(VRL parent,VRL child)
 	{
 		// Asynchronous Update ! 
 		ResourceEvent event = ResourceEvent.createChildAddedEvent(parent,child); 
 		vrsContext.getResourceEventNotifier().fire(event);
 	}
 
 	// Asynchronous updates !
 	protected void fireNewChilds(VRL parent,VRL childs[])
 	{
 		// Asynchronous Update ! 
 		ResourceEvent event = ResourceEvent.createChildsAddedEvent(parent,childs); 
 		vrsContext.getResourceEventNotifier().fire(event);
 	}
 	
     // Asynchronous updates !
     protected void fireNodeDeleted(VRL node)
     {
         // Asynchronous Update ! 
         ResourceEvent event = ResourceEvent.createDeletedEvent(node);  
         vrsContext.getResourceEventNotifier().fire(event);
     }
     
     // Asynchronous updates !
     protected void fireNodeDeleted(VRL node,VRL parent)
     {
         // Asynchronous Update ! 
         ResourceEvent event = ResourceEvent.createDeletedEvent(node,parent);  
         vrsContext.getResourceEventNotifier().fire(event);
     }
 
     /** 
      * Deletes contents of directory, does not delete directory itself. 
      * @param dir - directory to be deleted 
      * @param force - try to delete as much as possible. 
      * @throws VrsException 
      */
     public void recursiveDeleteDirContents(ITaskMonitor  monitor,VDir dir,boolean force) throws VrsException
     {
         defaultRecursiveDeleteDirContents(monitor,dir,force); 
     }
 
     /** 
      * Default Recursive delete: lists children and performs delete() on child list. 
      * This method does NOT delete the parent node itself !  
      * @throws VrsException 
      */
     protected boolean defaultRecursiveDeleteDirContents(ITaskMonitor  monitor,VDir dir, boolean force) throws VrsException
     {
         int len=0; 
         VFSNode nodes[]=dir.list(); 
 
         if ((nodes==null) || (nodes.length<=0)) 
             return true; 
 
         len=nodes.length; 
 
         if (monitor==null)
             monitor =vrsContext.getTaskWatcher().getCurrentThreadTaskMonitor("Deleting contents of:"+dir,len);
 
         monitor.logPrintf("Deleting %d entries from:%s\n",len,dir.getPath()); 
         
         for (int i=0;i<len;i++)
         {
             if (nodes[i]==null)
                 continue; // be robust. 
             
             if (nodes[i] instanceof VDir)
             {
                 VDir subDir=(VDir)nodes[i];
                 // go into recursion
                 defaultRecursiveDeleteDirContents(monitor,subDir,force); 
                 // delete dir itself: 
                 subDir.delete();
             }
             else
             {
                 monitor.startSubTask("deleting:"+nodes[i].getBasename(),1); 
                 monitor.logPrintf(" - deleting:%s\n",nodes[i]); 
                 nodes[i].delete();
                 monitor.endSubTask("deleting:"+nodes[i].getBasename()); 
             }
             
             // new asynchronous update to the VBrowser: 
             dir.getVRSContext().fireEvent(ResourceEvent.createDeletedEvent(nodes[i].getVRL()));  
         }
         
         return true; // if no exception occurred, the result=true
     }
 }
