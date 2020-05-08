 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.tasks;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 
 import org.tanukisoftware.wrapper.WrapperManager;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXParseException;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.ContextThread;
 import com.delcyon.capo.CapoApplication.ApplicationState;
 import com.delcyon.capo.CapoApplication.Location;
 import com.delcyon.capo.annotations.DefaultDocumentProvider;
 import com.delcyon.capo.annotations.DirectoyProvider;
 import com.delcyon.capo.client.CapoClient;
 import com.delcyon.capo.controller.LocalRequestProcessor;
 import com.delcyon.capo.controller.elements.GroupElement;
 import com.delcyon.capo.controller.elements.TaskElement;
 import com.delcyon.capo.controller.elements.TaskElement.Attributes;
 import com.delcyon.capo.modules.ModuleProvider;
 import com.delcyon.capo.preferences.Preference;
 import com.delcyon.capo.preferences.PreferenceInfo;
 import com.delcyon.capo.preferences.PreferenceInfoHelper;
 import com.delcyon.capo.preferences.PreferenceProvider;
 import com.delcyon.capo.protocol.client.CapoConnection;
 import com.delcyon.capo.resourcemanager.CapoDataManager;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.server.CapoServer;
 import com.delcyon.capo.xml.XPath;
 
 /**
  * @author jeremiah
  *
  */
 
 @PreferenceProvider(preferences=TaskManagerThread.Preferences.class)
 @DirectoyProvider(preferenceName="TASK_DIR",preferences=TaskManagerThread.Preferences.class)
 @DefaultDocumentProvider(name="task-status.xml",directoryPreferenceName="TASK_DIR", preferences = TaskManagerThread.Preferences.class)
 public class TaskManagerThread extends ContextThread
 {
 
 	
 	
 	public enum Preferences implements Preference
 	{
 		
 		
 		@PreferenceInfo(arguments={"dir"}, defaultValue="tasks", description="Where to store resource monitor file", longOption="TASK_DIR", option="TASK_DIR")
 		TASK_DIR,
 		@PreferenceInfo(arguments={"ms"}, defaultValue="30000", description="How long before a task is considered orphaned", longOption="TASK_LIFESPAN", option="TASK_LIFESPAN")
         TASK_DEFAULT_LIFESPAN,
         @PreferenceInfo(arguments={"ms"}, defaultValue="30000", description="Delay between client update and task syncing", longOption="DEFAULT_CLIENT_SYNC_INTERVAL", option="DEFAULT_CLIENT_SYNC_INTERVAL",location=Location.CLIENT)
         DEFAULT_CLIENT_SYNC_INTERVAL,
         @PreferenceInfo(arguments={"action"}, defaultValue="DELETE", description="What to do when a task is considered orphaned (DELETE|IGNORE)", longOption="TASK_DEFAULT_ORPHAN_ACTION", option="TASK_DEFAULT_ORPHAN_ACTION")
         TASK_DEFAULT_ORPHAN_ACTION,
         @PreferenceInfo(arguments={"boolean"}, defaultValue="true", description="When shutting down the client. Controls if we want a hard exit, or controlled. Testing wants a hard exit. [true|false] default is false", longOption="QUICK_SHUTDOWN", option="QUICK_SHUTDOWN",location=Location.CLIENT)
 		QUICK_SHUTDOWN,
 		@PreferenceInfo(arguments={"ms"}, defaultValue="10000", description="Interval at which overall task manager thread runs", longOption="TASK_INTERVAL", option="TASK_INTERVAL")
         TASK_INTERVAL;
 		
 		@Override
 		public String[] getArguments()
 		{
 			return PreferenceInfoHelper.getInfo(this).arguments();
 		}
 
 		@Override
 		public String getDefaultValue()
 		{
 			return PreferenceInfoHelper.getInfo(this).defaultValue();
 		}
 
 		@Override
 		public String getDescription()
 		{
 			return PreferenceInfoHelper.getInfo(this).description();
 		}
 
 		@Override
 		public String getLongOption()
 		{
 			return PreferenceInfoHelper.getInfo(this).longOption();
 		}
 
 		@Override
 		public String getOption()
 		{		
 			return PreferenceInfoHelper.getInfo(this).option();
 		}
 		
 		@Override
 		public Location getLocation() 
 		{
 			return PreferenceInfoHelper.getInfo(this).location();
 		}
 	}
 	
 	private boolean runAsService = true;
 	
 	private Transformer transformer;
 	private CapoDataManager capoDataManager;
 	private ReentrantLock lock = new ReentrantLock();
 	//private volatile Document taskManagerDocument;
 	//private ResourceDescriptor taskManagerDocumentFileDescriptor;
 	
 	 
 	
 	
 	private TaskManagerDocumentUpdaterThread tasksDocumentUpdaterThread;
 	private DocumentBuilder documentBuilder;
 	private ConcurrentHashMap<String,HashMap<String, String>> taskConcurrentHashMap = new ConcurrentHashMap<String, HashMap<String,String>>();
 
 	//private volatile boolean interrupted = false;
 	//private volatile boolean finished = false;
 	private volatile ApplicationState taskManagerState = ApplicationState.NONE;
 	private long lastSyncTime = 0l;
     private long lastRunTime = 0l;
 	
 
 	/**
 	 * This should only called once, and is the main initialization method for this class
 	 * @param capoDataManager
 	 * @throws Exception
 	 */
 	public synchronized static void startTaskManagerThread() throws Exception
 	{
 	    boolean runAsService = true;
 		if (CapoApplication.getTaskManagerThread() == null)
 		{
 			if (CapoApplication.getApplication() instanceof CapoServer)
 			{
 				runAsService = true;
 			}
 			else
 			{
 				String clientAsServiceValue = CapoApplication.getConfiguration().getValue(CapoClient.Preferences.CLIENT_AS_SERVICE);
 				
 				if (clientAsServiceValue != null && clientAsServiceValue.equalsIgnoreCase("true"))
 				{
 				    CapoApplication.logger.log(Level.INFO, "Running CapoClient as a service");				    
 					runAsService = true;
 				}
 				else
 				{
 				    CapoApplication.logger.log(Level.INFO, "Running CapoClient in Once NON-SERIVCE mode. See "+CapoClient.Preferences.CLIENT_AS_SERVICE+" preference.");
 					runAsService = false;
 				}
 			}
 			TaskManagerThread taskManagerThread = new TaskManagerThread(runAsService);			
 			CapoApplication.setTaskManagerThread(taskManagerThread);			
 			taskManagerThread.start(); //if this is NOT a service, the thread will only run once.			
 		}
 
 	}
 	
 	/**
 	 * Provides Global Access to The Monitor Thread. This is how things should be called.
 	 * @return
 	 */
 	public static TaskManagerThread getTaskManagerThread()
 	{
 	    if (CapoApplication.getApplication() != null)
 	    {
 	        return CapoApplication.getTaskManagerThread();
 	    }
 	    else
 	    {
 	        return null;
 	    }
 	}
 	
 	/**
 	 * Setup and start the resource monitor thread
 	 * @param capoDataManager
 	 * @throws Exception
 	 */
 	private TaskManagerThread(boolean runAsService) throws Exception
 	{
 		super(TaskManagerThread.class.getName()+" - "+CapoApplication.getApplication().getApplicationDirectoryName().toUpperCase());
 		this.capoDataManager = CapoApplication.getDataManager();
 		this.runAsService = runAsService;
 		
 		//this sets up our basic identity transform, that removes indentation and extraneous spaces
 		//it has nothing to do with figuring out who we are
 		Document identityDocument = CapoApplication.getDefaultDocument("identity_transform.xsl");
 		TransformerFactory tFactory = TransformerFactory.newInstance();		
 		transformer = tFactory.newTransformer(new DOMSource(identityDocument));		
 		transformer.setOutputProperty(OutputKeys.INDENT, "no");	
 		
 		//this.taskManagerDocumentFileDescriptor = capoDataManager.getResourceDescriptor(null,CapoApplication.getConfiguration().getValue(Preferences.TASKS_FILE));
 		//this.taskManagerDocumentFileDescriptor.addResourceParameters(null, new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,Preferences.TASK_DIR));
 		
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		documentBuilderFactory.setNamespaceAware(true);
 		documentBuilder = documentBuilderFactory.newDocumentBuilder();
 //		try
 //		{
 //			this.taskManagerDocument = documentBuilder.parse(taskManagerDocumentFileDescriptor.getInputStream(null));
 //		}
 //		catch (Exception exception)
 //		{
 //			//on an exception, just make a blank file
 //			CapoApplication.logger.log(Level.WARNING, "Error parseing tasks file, using default");
 //			this.taskManagerDocument = CapoApplication.getDefaultDocument("tasks.xml");
 //		}
 		
 		//go ahead and start things up
 		tasksDocumentUpdaterThread = new TaskManagerDocumentUpdaterThread(lock,runAsService);
 		if (runAsService == true) // TODO in theory if this is false, you can do a single pass of monitors, then it will exit, and not update anything
 		{
 			tasksDocumentUpdaterThread.start();
 		}
 		
 	}
 	
 	public ReentrantLock getLock()
 	{
 	    return lock;
 	}
 	
 	public void interrupt()
 	{
 		
 		synchronized (this)
 		{
 			//only set us to stopping if we're running or something earlier, this can happen when we don't run the client as a service
			if (this.taskManagerState.ordinal() < ApplicationState.STOPPING.ordinal())
 			{
 				this.taskManagerState = ApplicationState.STOPPING;
 			}
 			super.interrupt();	
 		}
 		
 		CapoServer.logger.log(Level.INFO, "Waiting for  TaskManager to finish");
 		while(this.taskManagerState != ApplicationState.STOPPED)
 		{			
 		    try
             {
                 Thread.sleep(100);
             } catch (InterruptedException e)
             {               
                 e.printStackTrace();
             }
 		}
 		
 		tasksDocumentUpdaterThread.interrupt();
 		CapoServer.logger.log(Level.INFO, "Waiting for  TaskDocumentUpdater Thread to shutdown");
 		while(tasksDocumentUpdaterThread.getUpdaterState() != ApplicationState.STOPPED)
         {		    
             try
             {
                 Thread.sleep(100);
             } catch (InterruptedException e)
             {               
                 e.printStackTrace();
             }
         }
 	}
 	
 	
 	
 	//SORT OF DONE
 	@Override
 	public void run()
 	{
 		taskManagerState = ApplicationState.READY;
 		while(taskManagerState.ordinal() < ApplicationState.STOPPING.ordinal())
 		{
 			try
 			{
 				//Lock everything down until we finish running
 			    lock.lock();
 			    //if we got asked to stop while waiting for the lock, bail out
 			    if (taskManagerState.ordinal() >= ApplicationState.STOPPING.ordinal())
 			    {
 			        break;    
 			    }
 			    //don't update this unless we are actually going to run.
 			    this.lastRunTime = System.currentTimeMillis();
 			    
 			    //===============================================BEGIN CLIENT SYNC===============================================================
 			    //check to see if we need to check in with the server
 			    if (lastSyncTime == 0) //skip initial sync since we can't get here without it happening first
 			    {
 			    	lastSyncTime = System.currentTimeMillis();			
 			    }
 			    else if (runAsService == true && CapoApplication.isServer() == false)
 			    {
 			    	//see if it's time to run the client sync
 			    	if (System.currentTimeMillis() - lastSyncTime > CapoApplication.getConfiguration().getLongValue(Preferences.DEFAULT_CLIENT_SYNC_INTERVAL))
 			    	{
 			    		//run the standard update and identity scripts. This is not configurable, because we want to make sure that the client always checks and for the basics.
 			    		HashMap<String, String> sessionHashMap = new HashMap<String, String>();
 			    		CapoConnection capoConnection = new CapoConnection();
 			    		((CapoClient)CapoApplication.getApplication()).runUpdateRequest(capoConnection, sessionHashMap);
 			    		//capoConnection.close();
 			    		if (WrapperManager.isShuttingDown()) //bail out of thread and client sync if we're restarting
 			    		{
 			    			break;
 			    		}
 			    		//capoConnection = new CapoConnection();
 			    		((CapoClient)CapoApplication.getApplication()).runIdentityRequest(capoConnection, sessionHashMap);
 			    		
 			    		((CapoClient)CapoApplication.getApplication()).runTasksUpdateRequest(capoConnection, sessionHashMap);
 			    		
 			    		((CapoClient)CapoApplication.getApplication()).runDefaultRequest(capoConnection, sessionHashMap);
 			    		capoConnection.close();
 			    		lastSyncTime = System.currentTimeMillis();
 			    	}
 			    }					
 			    //===============================================END CLIENT SYNC===============================================================
 			    
 			    //===============================================BEGIN OVERALL TASK RUN===============================================================
 			    ResourceDescriptor taskDirResourceDescriptor = capoDataManager.getResourceDirectory(Preferences.TASK_DIR.toString());
 			    ResourceDescriptor taskStatusDocumentResourceDescriptor = taskDirResourceDescriptor.getChildResourceDescriptor(null, "task-status.xml");
 			    Document taskStatusDocument = null;
 			    try
 			    {
 			        taskStatusDocument = CapoApplication.getDocumentBuilder().parse(taskStatusDocumentResourceDescriptor.getInputStream(null));
 			    }
 			    catch (SAXParseException saxParseException) 
 			    {
 			        taskStatusDocument = CapoApplication.getDefaultDocument("task-status.xml");
                 }
 				//don't let the document change while we are running
 			    List<ResourceDescriptor> taskResourceDescriptorList = CapoApplication.getDataManager().findDocuments(capoDataManager.getResourceDirectory(Preferences.TASK_DIR.toString()));
 			    for (ResourceDescriptor resourceDescriptor : taskResourceDescriptorList)
                 {
 			        //skip our status document
                     if (resourceDescriptor.getLocalName().equals("task-status.xml"))
                     {
                         resourceDescriptor.release(null);
                         continue; //loop to next file
                     }
                     
                     Document taskManagerDocument = null;
                     try
                     {
                         taskManagerDocument = documentBuilder.parse(resourceDescriptor.getInputStream(null));
                     }
                     catch (SAXParseException parseException)
                     {
                         CapoApplication.logger.log(Level.WARNING, "Skipping unparsable task '"+resourceDescriptor.getLocalName()+"'");                      
                         resourceDescriptor.release(null);
                         continue; //loop to next file
                     }
 
 					
 					NodeList tasksNodeList = XPath.selectNodes(taskManagerDocument, "//server:task");
 					String taskURI = resourceDescriptor.getLocalName();
 					String resourceMD5 = resourceDescriptor.getResourceMetaData(null).getMD5();
 					
 					//if we didn't find anything see if we are referring to a module
 					if (tasksNodeList.getLength() == 0)
 					{
 					    tasksNodeList = loadModule(tasksNodeList,taskManagerDocument);					    
 					}
 					
 					 //===============================================BEGIN INDIVIDUAL TASK RUN===============================================================
 					for (int monitorInfoIndex = 0; monitorInfoIndex < tasksNodeList.getLength(); monitorInfoIndex++)
 					{
 						//for each resource monitor get the resource that it is monitoring
 						Element taskElement = (Element) tasksNodeList.item(monitorInfoIndex);
 						Document taskDocument = documentBuilder.newDocument();
 						taskDocument.appendChild(taskDocument.importNode(taskElement, true));
 						taskElement = taskDocument.getDocumentElement();
 						
 						String name = taskElement.getAttribute(Attributes.name.toString());
 						//forgot name attribute, use filename if there's only one thing here
 						if (name.trim().isEmpty())
 						{
 						    if (tasksNodeList.getLength() == 1)
 						    {
 						        name = resourceDescriptor.getLocalName();
 						        CapoApplication.logger.log(Level.WARNING, "Defaulting task name to "+name+" task in '"+resourceDescriptor.getLocalName()+"' due to missing name attribute");
 						    } 
 						    else
 						    {
 						        CapoApplication.logger.log(Level.WARNING, "Skipping task in '"+resourceDescriptor.getLocalName()+"' due to missing name attribute");
 						        resourceDescriptor.release(null);
 						        continue; //loop to next task
 						    }
 						}
 						//find the corresponding task in the status document.
 						Element taskStatusElement = (Element) XPath.selectSingleNode(taskStatusDocument, "//server:task[@name = '"+name+"']");
 						//init new task status element for unknown task
 						if (taskStatusElement == null)
 						{
 						    taskStatusElement = taskStatusDocument.createElement("server:task");
 						    taskStatusElement.setAttribute(Attributes.name.toString(), name);
 	                        //make sure we always know where this task came from so we can cull the file 
                             taskStatusElement.setAttribute(Attributes.taskURI.toString(), taskURI);
 						    taskStatusDocument.getDocumentElement().appendChild(taskStatusElement);
 						    //clear out the persisted values since this is a new or replaced task
 						    taskConcurrentHashMap.remove(name);
 						}
 						
 						if (taskStatusElement.getAttribute("ACTION").equals("IGNORE") && taskStatusElement.getAttribute(Attributes.MD5.toString()).equals(resourceMD5))
 						{
 						    continue; //loop to next task
 						}
 						
 						//walk our persisted attributes and stick them back in the task element
                         NamedNodeMap namedNodeMap = taskStatusElement.getAttributes();
                         for(int index = 0; index < namedNodeMap.getLength(); index++)
                         {
                             String attributeName = namedNodeMap.item(index).getNodeName();
                             taskElement.setAttribute(attributeName,taskStatusElement.getAttribute(attributeName));                            
                         }
 						
                         //===============================================BEGIN LIFECYCLE VAR INITIALIZATION===============================================================
 						String lastExecutionTimeValue = taskElement.getAttribute(Attributes.lastExecutionTime.toString());
 						String lastAccessTimeValue = taskElement.getAttribute(Attributes.lastAccessTime.toString());
 						
 						String executionIntervalValue = taskElement.getAttribute(Attributes.executionInterval.toString());						
 						String lifeSpanValue = taskElement.getAttribute(Attributes.lifeSpan.toString());
 						
 						long executionInterval = 0l;
 						long lastExecutionTime = 0l;
 						long lastAccessTime = 0l;
 						long lifeSpan = 0l;
 						
 						if (lifeSpanValue.matches("\\d+"))
                         {
 						    lifeSpan = Long.parseLong(lifeSpanValue);
                         }
 						else
 						{
 						    lifeSpan = CapoApplication.getConfiguration().getLongValue(Preferences.TASK_DEFAULT_LIFESPAN);
 						}
 						
 						if (lastAccessTimeValue.matches("\\d+"))
                         {
                             lastAccessTime = Long.parseLong(lastAccessTimeValue);
                         }
 						else
 						{
 						    lastAccessTime = System.currentTimeMillis();
 						}
 						
 						if (executionIntervalValue.matches("\\d+"))
                         {
                             executionInterval = Long.parseLong(executionIntervalValue);
                         }
 						
 						if (lastExecutionTimeValue.matches("\\d+"))
 						{
 						    lastExecutionTime = Long.parseLong(lastExecutionTimeValue);						    
 						}						
 						//===============================================END LIFECYCLE VAR INITIALIZATION===============================================================
 						
 						//===============================================BEGIN ORPHAN CHECK===============================================================
 						//check to see if task is orphaned
                         if (this.lastRunTime > (lastAccessTime + lifeSpan)) //use last run time so that everything is on equal footing, as opposed to having tasks that take too long and cause other tasks to expire before it's too late
                         {
                             String orpanAction = CapoApplication.getConfiguration().getValue(Preferences.TASK_DEFAULT_ORPHAN_ACTION);
                             if (taskElement.hasAttribute(Attributes.orpanAction.toString()))
                             {
                                 orpanAction = taskElement.getAttribute(Attributes.orpanAction.toString());
                             }
                             
                             if(orpanAction.equals("DELETE"))
                             {
                                 CapoApplication.logger.warning("task '"+name+"' appears to be orphaned, deleteing. lastAccessTime = "+lastAccessTime+" lrt="+this.lastRunTime+" ls="+lifeSpan+" delta ="+(lastAccessTime + lifeSpan - this.lastRunTime));
                                 resourceDescriptor.performAction(null, Action.DELETE);
                                 //mark this task for deletion, by server
                                 //if we're running on the server, then just delete it.
                                 if (taskElement.getAttribute("local").equalsIgnoreCase("true")) 
                                 {
                                     taskStatusElement.getParentNode().removeChild(taskStatusElement);
                                 }
                                 else //otherwise we've got to wait for it to sync back up to the server
                                 {
                                     taskStatusElement.setAttribute("ACTION", Action.DELETE.toString());
                                 }
                             }
                             else
                             {
                                 CapoApplication.logger.warning("task '"+taskElement.getAttribute(Attributes.name.toString())+"' appears to be orphaned, ignoreing. lastAccessTime = "+lastAccessTime);    
                             }
                             resourceDescriptor.release(null);
                             continue; //loop to next task
                         }
                         //===============================================END ORPHAN CHECK===============================================================
                         
                         //check for single run task
 						if (executionInterval == 0l && lastExecutionTime > 0l)
 						{
 						    //this was only to run once
 						    String orpanAction = CapoApplication.getConfiguration().getValue(Preferences.TASK_DEFAULT_ORPHAN_ACTION);
                             if (taskElement.hasAttribute(Attributes.orpanAction.toString()))
                             {
                                 orpanAction = taskElement.getAttribute(Attributes.orpanAction.toString());
                             }
 						    taskStatusElement.setAttribute("ACTION", orpanAction);
 						    resourceDescriptor.release(null);
 						    continue; //loop to next task
 						}
 						
 						
 						//check to see if it's time for this task to run
 						if (System.currentTimeMillis() < lastExecutionTime + executionInterval)
 						{
 							//not time to run yet.
 							resourceDescriptor.release(null);
 							continue;
 						}
 						
 						
 						
 						//===============================================BEGIN PROCESSING OF TASK===============================================================
 						//Tasks get run locally 
 						LocalRequestProcessor localRequestProcessor = new LocalRequestProcessor();
 						HashMap<String, String> variableHashMap = null; //place to store memory persisted values from repeated task runs
 						
 						//see if this is a task we've never heard of
 						if (taskConcurrentHashMap.containsKey(taskElement.getAttribute(Attributes.name.toString())) == true)
 						{
 							//We've run this one before, so get any memory persisted variables from the previous run
 							variableHashMap = taskConcurrentHashMap.get(taskElement.getAttribute(Attributes.name.toString()));
 
 							//walk our previous variables and stick them back in the task document
 							namedNodeMap = taskElement.getAttributes();
 							for(int index = 0; index < namedNodeMap.getLength(); index++)
 							{
 								String attributeName = namedNodeMap.item(index).getLocalName();
 								if (variableHashMap.containsKey(attributeName))
 								{
 									//set attribute for the run
 									taskElement.setAttribute(attributeName, variableHashMap.get(attributeName));
 									//set attributes that need to be persisted									
 									taskStatusElement.setAttribute(attributeName, variableHashMap.get(attributeName));
 								}
 							}
 						}
 						
 						//RUN TASK
 						try
 				    	{
 				    	    GroupElement processedGroupElement = localRequestProcessor.process(taskElement,variableHashMap);
 				    	    processedGroupElement.getGroup();
 				    	    //after running save any variables for the next run
 				    	    taskConcurrentHashMap.put(taskElement.getAttribute(Attributes.name.toString()), processedGroupElement.getGroup().getVariableHashMap());
 				    	} 
 				    	catch (Exception exception)
 				    	{
 				    	    taskStatusElement.setAttribute("ACTION", "IGNORE");
 				    	    taskStatusElement.setAttribute("EXCEPTION", exception.getMessage());						    	    
 				    	}
 				    	
 				    	//update task status
 						taskStatusElement.setAttribute(Attributes.lastExecutionTime.toString(), System.currentTimeMillis()+"");
 						taskStatusElement.setAttribute(Attributes.MD5.toString(), resourceMD5);
 					} //===============================================END INDIVIDUAL TASK RUN===============================================================
 					
 					//cleanup file resource descriptor
 					resourceDescriptor.release(null);
 					
 					//we're done running, so see if a task asked us to restart
 					if (taskManagerState.ordinal() >= ApplicationState.STOPPING.ordinal())
                     {
 					    CapoServer.logger.log(Level.INFO, "Ignoring any remaining tasks due to STOP request");
                         break;    
                     }
 					
 				}//===============================================END OVERALL TASKS RUN===============================================================
 			    
 			    //after running it's time to store all of the status information
 			    updateTasksDocument(taskStatusDocumentResourceDescriptor,taskStatusDocument);
 				
 			} //END MAIN TRY BLOCK
 			catch (Exception exception)
 			{
 				CapoServer.logger.log(Level.WARNING, "error processing tasks",exception);
 			}
 			finally //make sure we always unlock things if we're bailing out
 			{
 			    while(lock.isHeldByCurrentThread())
 			    {
 			        lock.unlock(); //unlock everything since we;re now done and about to sleep or finish
 			    }
 			}
 			
 			//check to see if we need to sleep then loop because we're a service, or bail out because we've been interrupted.
 			if (runAsService == true && taskManagerState.ordinal() < ApplicationState.STOPPING.ordinal())
 			{
 				try
 				{
 					//sleep for a while, then do it all over again
 					Thread.sleep(CapoApplication.getConfiguration().getLongValue(Preferences.TASK_INTERVAL));					
 				} 
 				catch (InterruptedException interruptedException) 
 				{
 					//someone asked us to stop sleeping, not an error
 				} 
 			}
 			else //this is not a service or we were inturrupted, so exit the thread.
 			{
 				break; 
 			}
 			
 		}
 		
 		this.taskManagerState = ApplicationState.STOPPED;
 		//we're the last man standing, if we're not a service, so let the wrapper manager know to shutdown. 
 		//This is a little cleaner than just exiting.
 		//We DON'T want to do this if we are testing, as it will kill the JVM
 		if(runAsService == false && CapoApplication.getConfiguration().getBooleanValue(Preferences.QUICK_SHUTDOWN) == false)
 		{
 		    //execute controlled shutdown
 		    WrapperManager.stopAndReturn(0);
 		}
         
 	}
 	
 	private NodeList loadModule(NodeList tasksNodeList, Document taskManagerDocument) throws Exception
 	{
 		Element moduleElement = ModuleProvider.getModuleElement(taskManagerDocument.getDocumentElement().getLocalName());
 	    if (moduleElement != null && moduleElement.getLocalName().equals("task"))
 	    {
 	      //verify name attribute on module element
             if (moduleElement.hasAttribute("name") == false)
             {
                 moduleElement.setAttribute("name", taskManagerDocument.getDocumentElement().getLocalName());
             }
             
             //copy over attributes from mod declaration to task element
             NamedNodeMap attributeNodeMap = taskManagerDocument.getDocumentElement().getAttributes();
             for(int index = 0; index < attributeNodeMap.getLength(); index++)
             {
                 moduleElement.setAttribute(attributeNodeMap.item(index).getLocalName(), attributeNodeMap.item(index).getNodeValue());
             }
             
             //copy over child elements to first element of temp task
             NodeList childNodes = taskManagerDocument.getDocumentElement().getChildNodes();
             boolean isFirstChild = true;
             Element moduleDataElement = moduleElement.getOwnerDocument().createElement("server:moduleData");
             moduleDataElement.setAttribute("DoNotProcess", "true");
             for(int index = 0; index < childNodes.getLength(); index++)
             {
                 Node childNode = childNodes.item(index);
                 if (childNode instanceof Element)
                 {
                     if (isFirstChild)
                     {
                         moduleElement.appendChild(moduleDataElement);
                     }
                     moduleDataElement.appendChild(moduleElement.getOwnerDocument().importNode(childNode, true));
                 }
             }
             //reselect to make a new list with one item
             tasksNodeList = XPath.selectNodes(moduleDataElement, "//server:task");    
 	    }
 	    return tasksNodeList;
 	}
 	
 	
 	
 	
 	//DONE
 	private void updateTasksDocument(ResourceDescriptor resourceDescriptor, Document taskDocument) throws Exception
 	{
 		
 		tasksDocumentUpdaterThread.add(resourceDescriptor,taskDocument);
 		if (runAsService == false)
 		{
 			tasksDocumentUpdaterThread.run();
 		}
 	}
 
 
 	
 //	/**
 //	 * TODO this should be configurable, as opposed to hard coded
 //	 * @param id
 //	 * @param lastAccessTime - should generally be null
 //	 * @param pollInterval - 15 seconds aka 15000
 //	 * @param expirationInterval - 3 days aka 259200000
 //	 * @return
 //	 * @throws Exception 
 //	 */
 //	private Element createResourceMonitorElement(ResourceType resourceType, String resourceID, Long lastAccessTime, Long pollInterval, Long expirationInterval) throws Exception
 //	{
 //		
 //		
 //		Element resourceMonitorElement = taskManagerDocument.createElementNS(null,TASK_ELEMENT_NAME);
 //		if (resourceID == null || resourceID.trim().isEmpty() == true)
 //		{
 //			throw new NullPointerException("Missing MonitorInfo ID");
 //		}
 //		else
 //		{
 //			resourceMonitorElement.setAttribute("ID", resourceID);
 //		}
 //		
 //		if (resourceType == null)
 //		{
 //			throw new NullPointerException("Missing Resource Monitor Type");
 //		}
 //		else
 //		{
 //			resourceMonitorElement.setAttribute("type", resourceType.getName());
 //		}
 //		
 //		if (lastAccessTime != null)
 //		{
 //			resourceMonitorElement.setAttribute("lastAccessTime", lastAccessTime.toString());
 //		}
 //		else
 //		{
 //			resourceMonitorElement.setAttribute("lastAccessTime", System.currentTimeMillis()+"");
 //		}
 //		
 //		if (pollInterval != null)
 //		{
 //			resourceMonitorElement.setAttribute("pollInterval", pollInterval.toString());
 //		}
 //		else
 //		{
 //			resourceMonitorElement.setAttribute("pollInterval", "15000");
 //		}
 //		
 //		if (expirationInterval != null)
 //		{
 //			resourceMonitorElement.setAttribute("expirationInterval", expirationInterval.toString());
 //		}
 //		else
 //		{
 //			resourceMonitorElement.setAttribute("expirationInterval", "259200000");
 //		}
 //		
 //		Element resourceElementDeclaration = taskManagerDocument.createElementNS(null, "ResourceElement");
 //		resourceElementDeclaration.setAttribute("name", resourceID);
 //		resourceElementDeclaration.setAttribute("uri", resourceID);
 //		resourceElementDeclaration.setAttribute("lifeCycle", LifeCycle.REF.toString());
 //		Element resourceDependentsElement = taskManagerDocument.createElementNS(null, "ResourceDependents");
 //		Element resourceListenersElement = taskManagerDocument.createElementNS(null, "ResourceListeners");
 //		resourceMonitorElement.appendChild(resourceElementDeclaration);
 //		resourceMonitorElement.appendChild(resourceDependentsElement);
 //		resourceMonitorElement.appendChild(resourceListenersElement);
 //		
 //		ResourceElement resourceElement = new ResourceElement(); //TODO absolute bull shit
 //		resourceElement.init(resourceElementDeclaration, null, null, null); //TODO FIXME!!!! too much BS here, making it undependable for use on the client side etc.
 //
 //		
 //		
 //		ResourceDescriptor fileResourceDescriptor =  capoDataManager.getResourceDescriptor(resourceElement,resourceID);
 //		ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
 //		resourceParameterBuilder.addAll(resourceElementDeclaration);
 //		fileResourceDescriptor.open(null);				
 //		Element fileDescriptorElement = XMLSerializer.export(taskManagerDocument,fileResourceDescriptor);
 //		resourceElementDeclaration.appendChild(fileDescriptorElement);
 //		
 //		
 //		
 //		return resourceMonitorElement;
 //	}
 
 //	/**
 //	 * This returns an element of unknown type that corresponds to /ResourceMonitors/ResourceMonitor[@ID = 'resourceID']/ResourceDependents/Dependent[@ID = 'dependantID']/*[1]
 //	 * @param resourceID
 //	 * @param dependantID
 //	 * @return
 //	 * @throws Exception 
 //	 */
 //	public Element getDependentDataElement(String resourceID, String dependantID) throws Exception
 //	{
 //		Element dependantDataElement = null;
 //		Element dependantElement = getDependentElement(resourceID, dependantID);
 //		if (dependantElement != null)
 //		{			
 //			dependantDataElement = (Element) XPath.selectSingleNode(dependantElement, "./*[1]");			
 //		}
 //		
 //		return dependantDataElement;
 //	}
 //	
 //	private Element getResourceMonitorElement(String resourceID) throws Exception
 //	{
 //		Element resourceMonitorElement = (Element) XPath.selectSingleNode(taskManagerDocument, "/ResourceMonitors/ResourceMonitor[@ID = '"+resourceID+"']");
 //		if (resourceMonitorElement != null)
 //		{
 //			resourceMonitorElement.setAttribute("lastAccessTime", System.currentTimeMillis()+"");
 //			updateTasksDocument();
 //		}
 //		return resourceMonitorElement;
 //	}
 //	
 //	private Element getDependentElement(String resourceID, String dependantID) throws Exception
 //	{
 //		Element dependantElement = null;
 //		Element resourceMonitorElement = getResourceMonitorElement(resourceID);
 //		if (resourceMonitorElement != null)
 //		{
 //			dependantElement = (Element) XPath.selectSingleNode(resourceMonitorElement, "ResourceDependents/Dependent[@ID = '"+dependantID+"'][1]");
 //			if (dependantElement != null)
 //			{
 //				dependantElement.setAttribute("lastAccessTime", System.currentTimeMillis()+"");
 //				updateTasksDocument();
 //			}	
 //		}
 //		
 //		return dependantElement;
 //	}
 //
 	/**
 	 * taskName
 	 * clientID - can be null
 	 * dependentID 
 	 * element to be stored
 	 */
 	public void setTaskDataElement(String taskID, String clientID,String dependantID, Element someElement) throws Exception
 	{
 	    lock.lock();
 	    try
 	    {
 	        ResourceDescriptor taskDocumentResourceDescriptor = capoDataManager.findDocumentResourceDescriptor(taskID, clientID, Preferences.TASK_DIR);
 	        if (taskDocumentResourceDescriptor != null && taskDocumentResourceDescriptor.getResourceMetaData(null).exists() == true)
 	        {
 	            Document taskDocument = CapoApplication.getDocumentBuilder().parse(taskDocumentResourceDescriptor.getInputStream(null));
 	            Node taskDataNode = XPath.selectSingleNode(taskDocument, "//server:taskData");
 	            if (taskDataNode == null)
 	            {
 	                taskDataNode = taskDocument.createElement("server:taskData");
 	                taskDocument.appendChild(taskDataNode);
 	            }
 	            Node taskDependentNode = XPath.selectSingleNode(taskDataNode, "server:taskDataItem[@dependantID = '"+dependantID+"']");
 	            if (taskDependentNode == null)
 	            {
 	                taskDependentNode = taskDocument.createElement("server:taskDataItem");
 	                ((Element) taskDependentNode).setAttribute("dependantID",dependantID);
 	                taskDataNode.appendChild(taskDataNode);
 	                taskDependentNode.appendChild(taskDocument.importNode(someElement,true));
 	            }
 
 	            XPath.removeNodes(taskDependentNode,"./*");
 	            taskDependentNode.appendChild(taskDocument.importNode(someElement,true));
 
 	            updateTasksDocument(taskDocumentResourceDescriptor, taskDocument);
 	        }
 	    }
 	    finally
 	    {
 	        lock.unlock();
 	    }
 	}
 
 	public Element getTaskDataElement(String taskID, String clientID,String dependantID) throws Exception
 	{
 	    lock.lock();
         try
         {
             ResourceDescriptor taskDocumentResourceDescriptor = capoDataManager.findDocumentResourceDescriptor(taskID, clientID, Preferences.TASK_DIR);
             if (taskDocumentResourceDescriptor != null && taskDocumentResourceDescriptor.getResourceMetaData(null).exists() == true)
             {
                 Document taskDocument = CapoApplication.getDocumentBuilder().parse(taskDocumentResourceDescriptor.getInputStream(null));
                 Node taskDataNode = XPath.selectSingleNode(taskDocument, "//server:taskData/server:taskDataItem[@dependantID = '"+dependantID+"']/*");
                 return (Element) taskDataNode;
             }
             return null;
         }
         finally
         {
             lock.unlock();
         }
 	}
 	
 //	private Element getResourceDependantsElement(String resourceID) throws Exception
 //	{
 //		Element resourceDependantsElement = null;
 //		Element resourceMonitorElement = getResourceMonitorElement(resourceID);
 //		if (resourceMonitorElement != null)
 //		{
 //			resourceDependantsElement =  (Element) XPath.selectSingleNode(resourceMonitorElement, "ResourceDependents");
 //		}
 //		return resourceDependantsElement;
 //	}
 //
 //	private Element createResourceDependentElement(String dependantID)
 //	{
 //		Element resourceDependentElement = taskManagerDocument.createElementNS(null, "Dependent");
 //		resourceDependentElement.setAttribute("ID", dependantID);
 //		resourceDependentElement.setAttribute("lastAccessTime", System.currentTimeMillis()+"");
 //		resourceDependentElement.setAttribute("removeOnChange", "true");		
 //		return resourceDependentElement;
 //	}
 
 	
 	
 	/**
 	 * This will add a monitor thread to the document if it doesn't already exists, or has changed in some way.
 	 * @param name
 	 * @param md5
 	 * @param taskElement
 	 * @throws Exception
 	 */
 	public void setTask(String name, String md5, TaskElement taskElement,String clientID) throws Exception
 	{
 	    lock.lock();
 	    
 	    try
 	    {
 	    ResourceDescriptor taskDocumentResourceDescriptor = capoDataManager.findDocumentResourceDescriptor(name, clientID, Preferences.TASK_DIR);
 	    Document taskDocument = null;
 	    //create a new task file
 	   
 	    if (taskDocumentResourceDescriptor == null)
 	    {
 	    	if (clientID != null)
 	        {
 	            ResourceDescriptor clientResourceDescriptor = capoDataManager.getResourceDescriptor(null, "clients:"+clientID);
 	            //see if we have a child filesystem
 	            if (clientResourceDescriptor.getResourceMetaData(null).exists() == true)
 	            {
 	                ResourceDescriptor relaventChildResourceDescriptor = clientResourceDescriptor.getChildResourceDescriptor(null,CapoApplication.getConfiguration().getValue(Preferences.TASK_DIR));
 	                if (relaventChildResourceDescriptor != null && relaventChildResourceDescriptor.getResourceMetaData(null).exists() == true)
 	                {
 	                    //search the client filesystem
 	                	taskDocumentResourceDescriptor = relaventChildResourceDescriptor.getChildResourceDescriptor(taskElement, name+".xml");
 	                }
 	            }
 	           
 	        }
 	    	else //this is for server side stuff
 	    	{
 	    		ResourceDescriptor tasksResourceDescriptor = CapoApplication.getDataManager().getResourceDirectory(Preferences.TASK_DIR.toString());
 	    		taskDocumentResourceDescriptor = tasksResourceDescriptor.getChildResourceDescriptor(taskElement, name+".xml");
 	    	}
 	        taskDocument = documentBuilder.newDocument();
 	    }
 	    else if (taskDocumentResourceDescriptor.getResourceMetaData(null).exists() == false)
 	    {	       
 	        taskDocument = documentBuilder.newDocument();
 	    }
 	    else
 	    {
 	        taskDocument = documentBuilder.parse(taskDocumentResourceDescriptor.getInputStream(null));
 	    }
 	    
 		    
 			//check to see if we already have this monitor
 			Element previousMonitorElement = (Element) XPath.selectSingleNode(taskDocument, "//server:task[@name = '"+name+"']");
 			if (previousMonitorElement != null)
 			{
 				//see if the md5s match
 				if (previousMonitorElement.getAttribute("md5").equals(md5))
 				{
 					CapoApplication.logger.log(Level.FINE, "Task hasn't changed: "+name);
 					previousMonitorElement.setAttribute(Attributes.lastAccessTime.toString(), System.currentTimeMillis()+"");
 					updateTasksDocument(taskDocumentResourceDescriptor,taskDocument);
 					return;
 				}
 				else
 				{
 					//remove the old element
 					CapoApplication.logger.log(Level.INFO, "Removing Old Task: "+name);
 					previousMonitorElement.getParentNode().removeChild(previousMonitorElement);					
 				}
 			}
 			CapoApplication.logger.log(Level.INFO, "Updating Task: "+name);
 			//if we made it this far add the element
 			Element monitorElementDeclaration = (Element) taskDocument.importNode(taskElement.getControlElementDeclaration(), true);
 			monitorElementDeclaration.setAttribute("md5", md5);
 			monitorElementDeclaration.setAttribute(Attributes.lastAccessTime.toString(), System.currentTimeMillis()+"");
 			taskConcurrentHashMap.remove(name);			
 			taskDocument.appendChild(monitorElementDeclaration);
 			updateTasksDocument(taskDocumentResourceDescriptor,taskDocument);
 	    }	    
 	    finally
 	    {
 			lock.unlock();
 	    }
 		
 	}
 
     public long getLastRunTime()
     {        
         return lastRunTime;
     }
 
     public ApplicationState getTaskManagerState()
     {
     	return taskManagerState;
     }
 	
 }
