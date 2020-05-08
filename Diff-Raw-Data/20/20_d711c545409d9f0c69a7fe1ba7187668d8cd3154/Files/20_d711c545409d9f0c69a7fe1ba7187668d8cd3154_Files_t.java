 package com.patelsoft.fastopen;
 
 import java.util.*;
 import org.gjt.sp.jedit.*;
 import projectviewer.vpt.VPTNode;
 
 import projectviewer.vpt.VPTProject;
 // import projectviewer.*;
 // import projectviewer.config.*;
 // import projectviewer.vpt.*;
 
 public class Files
 {
 	private boolean PVThere;
 
 	public Files()
 	{
 		//Test if PV is there or else use Buffers as the base.
 		try
 		{
 			Class.forName("projectviewer.ProjectViewer");
 			PVThere = true;
 		}
 		catch(ClassNotFoundException exp)
 		{
 			//Ahh no PV.
 			PVThere = false;// Sanity sake. What if PV was there when calling the first time. and then the user unloads and deletes PV!!
 		}
 	}
 
 
 	/**
 	 *  Gets the currentProjectViewer attribute of the FastOpen object
 	 *
 	 * @return    The currentProjectViewer value
 	 */
 	public projectviewer.ProjectViewer getCurrentProjectViewer(View view)
 	{
 		Object obj = projectviewer.ProjectViewer.getViewer(view);
 		if(obj instanceof projectviewer.ProjectViewer)
 		{
 			return (projectviewer.ProjectViewer)obj;
 		}
 		return null;
 	}
 
 	/**
 	 *  Gets the currentProject attribute of the FastOpen object
 	 *
 	 * @return    The currentProject value
 	 */
 	public VPTProject getCurrentProject(View view)
 	{
 		if(isPVThere())
 		{
 			/* projectviewer.ProjectViewer pv = getCurrentProjectViewer(view);
 
 			if(pv != null)
 			{
 				System.out.println("PV obj exists. returning active project " + pv.getActiveProject(view));
 				return pv.getActiveProject(view);
 			}
 
 				//currPrj = projectviewer.PVActions.getCurrentProject(view);
 			//System.out.println("PV instance does NOT exist. returning last project as per config " + (projectviewer.config.ProjectViewerConfig.getInstance().getLastNode()));
             System.out.println("PV instance does not exists. returning last project " + projectviewer.config.ProjectViewerConfig.getInstance().getLastNode());
 			//return (VPTProject)projectviewer.config.ProjectViewerConfig.getInstance().getLastNode();
             VPTNode nodePrj = projectviewer.config.ProjectViewerConfig.getInstance().getLastNode();
             if(nodePrj instanceof VPTProject)
             {
                 return (VPTProject)nodePrj;
             }
             //return (projectviewer.vpt.VPTProject)projectviewer.ProjectViewer.getActiveNode(view); */
 
 			return projectviewer.ProjectViewer.getActiveProject(view);
 		}
 		return null;
   		/*if(isPVThere())
 		{
 			return projectviewer.PVActions.getCurrentProject(view);
 		}
 		return null;*/
 	}
 
 
 	public boolean isPVThere()
 	{
 		return PVThere;
 	}
 
         public boolean isProjectLoaded(String projectName)
         {
             return projectviewer.ProjectManager.getInstance().isLoaded(projectName);
         }
 
 	/**
 	 *  Description of the Method
 	 *
 	 */
 	void prjFile2FOFile(View view, Collection allFiles)
 	{
 		System.out.println("Inside prjFile2FOFile");
		if(isPVThere() && atleatOneProject(view))
 		{
                     //Loop until project is loaded
                     while(!isProjectLoaded(getCurrentProject(view).getName()))
                     {
                         try
                         {
                             Thread.sleep(5000);
                         }
                         catch(InterruptedException e)
                         {}
                     }
 			System.out.println("Inside isPVThere");
 			//Iterator iter = ((projectviewer.vpt.VPTProject)getCurrentProject(view)).getOpenableNodes().iterator();
                         System.out.println("Got project " + getCurrentProject(view));
 						Collection nodes = (getCurrentProject(view)).getOpenableNodes();
 						//synchronized(nodes)
 						//{
 							Iterator iterPrj = nodes.iterator();
 							while(iterPrj.hasNext())
 							{
 									projectviewer.vpt.VPTFile file = (projectviewer.vpt.VPTFile)iterPrj.next();
 									/*boolean exists = false;
 									Iterator iterAllFiles = allFiles.iterator();
 									while(iterAllFiles.hasNext())
 									{
 											FastOpenFile fofile = (FastOpenFile)iterAllFiles.next();
 											if(fofile.equals(file))
 											{
 													exists = true;
 													break;
 											}
 									}
 
 									if(!exists)
 									{*/
 											FastOpenFile fofile = new FastOpenFile(file);
 											boolean added = allFiles.add(fofile);
 											if(!added) //Means hashcode is same but status is different
 											{
 													allFiles.remove(fofile); //Removes the old duplicate class as per FOFile's hashCode impl.
 													allFiles.add(fofile);
 											}
 									/* } */
 							}
 						//}//End of Synchronized
 		}
 	}//End of prjFile2FOFile
	
	
	/**
	*	Checks if PV has atleast one project created.
	*/
	private boolean atleatOneProject(View view)
	{
		if(isPVThere())
		{
			if(getCurrentProject(view) != null)
			{
				return true;
			}
		}
		
		return false;
	}
 
 	/**
 	*  Description of the Method
 	*
 	* @param  buffer   Description of the Parameter
 	*/
 	void diffPrjFilesWithOpenBuffers(Buffer buffer[],View view, Collection allFiles)
 	{
 		System.out.println("Inside diffPrjFilesWithOpenBuffers");
 		if(buffer == null || buffer.length == 0)
 		{
 			return;
 		}
 
 		if(isPVThere())
 		{
 			projectviewer.vpt.VPTProject project = getCurrentProject(view);
 			System.out.println("Got curr project in diffPrjFilesWithOpenBuffers " + project);
 			if(project != null)
 			{
 				//if(!project.isInProject(buffer[i].getPath()))
 				for(int i = 0; i < buffer.length; i++)
 				{
 					if(!project.isInProject(buffer[i].getPath()))
 					{
 						boolean exists = false;
 						Iterator iterAllFiles = allFiles.iterator();
 						while(iterAllFiles.hasNext())
 						{
 							FastOpenFile fofile = (FastOpenFile)iterAllFiles.next();
 							if(fofile.equals(buffer[i]))
 							{
 								exists = true;
 								break;
 							}
 						}
 
 						if(!exists)
 						{
 							FastOpenFile fofile = new FastOpenFile(buffer[i]);
 							allFiles.add(fofile);
 						}
 					}
 				}
 			}
 		}
 		else
 		{
 			for(int i = 0; i < buffer.length; i++)
 			{
 				allFiles.add(new FastOpenFile(buffer[i]));
 			}
 		}
 	}
 
 
 	public void bufferEntry2FastOpenFile(List files, Collection allFiles)
 	{
 		System.out.println("Inside bufferEntry2FastOpenFile");
 		Iterator iter = files.iterator();
 		while(iter.hasNext())
 		{
 			BufferHistory.Entry entry = (BufferHistory.Entry)iter.next();
 			boolean exists = false;
 			Iterator iterAllFiles = allFiles.iterator();
 			while(iterAllFiles.hasNext())
 			{
 				FastOpenFile fofile = (FastOpenFile)iterAllFiles.next();
 				if(fofile.equals(entry))
 				{
 					exists = true;
 					break;
 				}
 			}
 
 			if(!exists)
 			{
 				FastOpenFile fofile = new FastOpenFile(entry);
 				allFiles.add(fofile);
 			}
 		}
 	}
 
 	void getRecentFiles(Collection allFiles)
 	{
 		bufferEntry2FastOpenFile(BufferHistory.getHistory(), allFiles);
 	}
 
 }
