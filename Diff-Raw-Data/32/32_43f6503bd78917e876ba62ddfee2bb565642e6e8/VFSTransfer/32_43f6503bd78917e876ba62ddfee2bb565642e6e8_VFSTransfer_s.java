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
 
 import nl.esciencecenter.ptk.task.ITaskMonitor;
 import nl.esciencecenter.ptk.task.TransferMonitor;
 import nl.esciencecenter.ptk.util.StringUtil;
 import nl.esciencecenter.ptk.util.logging.ClassLogger;
 import nl.esciencecenter.vbrowser.vrs.data.Attribute;
 import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
 import nl.esciencecenter.vlet.vrs.VNode;
 import nl.esciencecenter.vlet.vrs.vrl.VRLUtil;
 
 /**
  * VFSTransfer monitor class. 
  * 
  * Monitor object for ongoing VFS Transfers. 
  * 
  * @author P.T. de Boer
  */
 public class VFSTransfer extends TransferMonitor
 {    
     // ========================================================================
     // instance
     // ========================================================================
 
     // VFS Transfer Fields 
     private String resourceType="";
     
     /** Whether this transfer is move */  
     private final boolean isMove;
   
     private boolean multiTransfer=false;
 
     private VFSActionType actionType=VFSActionType.UNKNOWN; 
   
     // not source but current (sub)resource; 
     private VRL currentSource; 
     
     // instance methods
     public VFSTransfer(ITaskMonitor parentMonitor, String resourceType,VRL sources[], VRL destination,boolean isMove)
     {
         super("VFSTransfer", VRLUtil.toURIs(sources), destination.toURINoException());
         setParent(parentMonitor); // add this transfer to parent monitor 
         this.resourceType=resourceType; 
         this.isMove=isMove; 
     }
 
     public VFSTransfer(ITaskMonitor optParentMonitor, String resourceType,
 			VRL source, VRL targetParentDirVrl, boolean isMove)
     {
     	this(optParentMonitor,resourceType,new VRL[]{source},targetParentDirVrl,isMove);
 	}
 
 	/** 
 	 * Specify what kind of action this transfer is.
 	 */ 
     public void setVFSTransferType(VFSActionType type)
     {
         this.actionType=type; 
     }
     
     // =======================================================================
     // VFSTransfer methods 
     // =======================================================================
     
     public void printReport(ClassLogger logger)
     {
         logger.debugPrintf("--- VFSTransfer report ---");
         logger.debugPrintf(" transfer ID   =%s\n",this.getID());
         logger.debugPrintf(" source        =%s\n",this.getSource());
         logger.debugPrintf(" destination   =%s\n",this.getDestination()); 
         logger.debugPrintf(" type          =%s\n",this.actionType); 
         logger.debugPrintf(" is move       =%s\n",StringUtil.boolString(this.isMove));  
         logger.debugPrintf(" transfer time =%f(s)\n",(double)this.getTime()/1000.0); 
         logger.debugPrintf(" transfer size =%d\n",+getSubTaskTodo()); 
         logger.debugPrintf(" Exception     =%s\n",(this.getException()==null?getException():"no")); 
     }
       
     public Attribute[] getAttributes()
     {
         Attribute attrs[]= 
           {
              new Attribute("transferID",getID()),  
              new Attribute("type",resourceType),  
              new Attribute("method",(isMove?"Move":"Copy")),
              new Attribute("source",getSource()),  
              new Attribute("destination",getDestination()),  
              new Attribute("done",isDone()),  
              new Attribute("exception",getException().toString())  
           };
         
         return attrs; 
     }
    
     public String toString()
     {
         return "transferID  ="+this.getID()+"\n"
                 +"resourceType="+this.resourceType+"\n"
                 +"method      ="+(isMove?"move":"copy")+"\n"
                 +"source      ="+this.getSource()+"\n"
                 +"destination ="+this.getDestination()+"\n"
                 +"exception   ="+(this.getException()==null?"none":getException().getClass().getName());
     }
            
     /**
      * @return Returns the current source
      */
     public VRL getCurrentSource()
     {
         return currentSource; 
     }
     
     public void setCurrentSource(VRL vrl)
     {
         currentSource=vrl;
     }
   
     /**
      * @return Returns the isMove.
      */
     public boolean isMove()
     {
         return isMove;
     }
     
     public long getStartTime()
     {
         return this.getTaskStats().startTimeMillies; 
     }
     
     /** Return current transfer speed string in KB/s */
     public String getCurrentSpeedString()
     {
         String speedstr="";
         
         // only print current transfer is have it:
         
         if (getSubTaskDone()<=0) 
             speedstr+="";
         else
             speedstr+=getSubTaskDone()/(getSubTaskDoneLastUpdateTime()-getStartTime()+1)+"/";
         
         if (getTotalWorkDone()<=0) 
             speedstr+="(?)KB/s";
         else
             speedstr+=getTotalWorkDone()/(getTotalWorkDoneLastUpdateTime()-getStartTime()+1)+"KB/s"; 
         
                 
         return speedstr; 
     }
    
     public void endTask(String taskName)
     {
         //Global.warnPrintf(this,"Ending VRS Transfer:%s\n",taskName); 
         
         super.endTask(taskName); 
         
         // bug: after recursive directory copy: the stats 
         // might be wrong 
         if (this.getTotalWorkTodo()<=0)
         {
             // match done with todo:
             long size=this.getTotalWorkDone(); 
             this.setTotalWorkTodo(size);
             this.setTotalWorkDone(size);
         }
     }
 
     public void setMultiTransfer(boolean value)
     {
         this.multiTransfer=value; 
     }
     
     public boolean isMultiTransfer()
     {
         return multiTransfer;  
     }
 
     /** Return information about the current source */ 
     public String getSourceText()
     {
         String source="?";
         
         if (isMultiTransfer())
             source="(Multi) "; 
         else
             source="";
          
         if (this.currentSource!=null)
             source+=currentSource;    
        
         return source; 
     }
 
     public void addSourcesDone(int i)
     {
         this.updateSourcesDone(getSourcesDone()+i);  
     }   
     
     // ----------------------
     // Legacy Methods: Todo
     // -----------------------
     
     public void updateSubTaskDone(long numTransferred)
     {
         super.updateSubTaskDone(this.getCurrentSubTaskName(), numTransferred);
     }
 
     public void setTotalWorkTodo(long i)
     {
         this.taskStats.todo=i; 
     }
 
     public void updateWorkDone(long i)
     {
         super.updateTaskDone(i);
     }
     
     public void setTotalWorkDone(long size)
     {
         super.updateTaskDone(size);
     }
         
     public TaskStats getCurrentSubTask()
     {
         taskStats = super.getSubTaskStats(super.getCurrentSubTaskName());
 
         if (taskStats == null)
             taskStats = this.subTaskStats.get("");
 
         if (taskStats == null)
             taskStats = super.createSubTask("", -1);
 
         return taskStats;
     }
 
     public void setSubTaskTodo(long size)
     {
         TaskStats stats = getCurrentSubTask();
         if (stats == null)
             return;
         stats.todo = size;
     }
 
     public long getTime()
     {
         if (isDone())
         {
             return this.taskStats.stopTimeMillies-this.taskStats.startTimeMillies;
         }
         else
         {
             return System.currentTimeMillis() - this.taskStats.startTimeMillies;
         }
     }
     
     private long getSubTaskTodo()
     {
         return this.getCurrentSubTask().todo; 
     }
     
     private long getTotalWorkDoneLastUpdateTime()
     {
         return this.taskStats.doneLastUpdateTimeMillies;
     }
 
 
     private long getSubTaskDoneLastUpdateTime()
     {
         return this.getCurrentSubTask().doneLastUpdateTimeMillies; 
     }
 
     private long getSubTaskDone()
     {
         return this.getCurrentSubTask().done; 
     }
 
     public long getTotalWorkTodo()
     {
         TaskStats stats=this.getTaskStats();
         if (stats==null)
             return -1; 
         return stats.todo; 
     }
     
     public long getTotalWorkDone()
     {
         TaskStats stats=this.getTaskStats();
         if (stats==null)
             return -1; 
         return stats.done; 
     }
 
 	public void setSources(VRL[] vrls) 
 	{
 		super.updateSources(VRLUtil.toURIs(vrls)); 
 	}
 
 	public void updateSources(VNode[] nodes) 
 	{
	    java.net.URI uris[]=new java.net.URI[nodes.length];
		
		for (int i=0;i<nodes.length;i++)
		    uris[i]=nodes[i].getVRL().toURINoException(); 
 		super.updateSources(uris);
 	}
 
 }
