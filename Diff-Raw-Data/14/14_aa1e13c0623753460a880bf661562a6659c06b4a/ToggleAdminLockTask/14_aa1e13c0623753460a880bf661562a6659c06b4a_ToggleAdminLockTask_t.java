 package com.intelix.digihdmi.app.tasks;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.model.Device;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.Task;
 
 /**
  *
  * @author Michael Caron <michael.r.caron@gmail.com>
  */
 public class ToggleAdminLockTask extends Task<Boolean,Void> {
 
     String password = null;
 
     public ToggleAdminLockTask(Application app) {
         super(app);
     }
 
     public ToggleAdminLockTask(Application app, String password) {
         super(app);
         this.password = password;
     }
 
     @Override
     protected Boolean doInBackground() throws Exception {
         Device d = ((DigiHdmiApp)getApplication()).getDevice();
         if (password == null)
             d.lockAdmin() ;
         else
        {
            if (d.isAdminLocked())
                d.lockAdmin();
             d.unlockAdmin(password);
        }
 
         return d.isAdminLocked();
     }
 }
