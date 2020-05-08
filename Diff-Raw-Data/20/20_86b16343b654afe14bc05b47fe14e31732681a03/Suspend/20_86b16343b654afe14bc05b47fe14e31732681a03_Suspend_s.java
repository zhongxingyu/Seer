 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 package zhuravlik.ant.vix.tasks;
 
 import org.apache.tools.ant.Project;
 import zhuravlik.ant.vix.LibraryHelper;
 import zhuravlik.ant.vix.Vix;
 import zhuravlik.ant.vix.VixAction;
 
 /**
  *
  * @author anton
  */
 public class Suspend extends VixAction {
     @Override
     public void executeAction(int vmHandle) {
         log("Sending suspend command", Project.MSG_INFO);
         
         int jobHandle = Vix.VIX_INVALID_HANDLE;
 
         jobHandle = LibraryHelper.getInstance().VixVM_Suspend(vmHandle, 0, null, null);
 
         int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
         LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
         checkError(err);
     }
 }
