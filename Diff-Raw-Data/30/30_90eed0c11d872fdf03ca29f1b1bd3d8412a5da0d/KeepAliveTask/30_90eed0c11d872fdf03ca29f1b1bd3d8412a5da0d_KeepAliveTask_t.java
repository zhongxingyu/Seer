 package edu.niu.cs.students.mvstool.tasks;
 
 import edu.niu.cs.students.task.Task;
 import edu.niu.cs.students.mvs.MVSClient;
 import edu.niu.cs.students.mvstool.Utils;
 import edu.niu.cs.students.mvstool.Profile;
 
 public class KeepAliveTask extends Task {
   
  public void run()
    throws Exception {
    
    Profile profile = Profile.getCurrentProfile();
    MVSClient client = profile.getMVSClient();
    
    while(true){
      
      try {
        
       synchronized(client){
         client.noop();
       }
       
        Utils.sleep(60);
        
      } catch(Exception e){
        /* We don't care, pass.... */
      }
    }
    
  }
  
  public void failure(Exception e){} 
  public void success(){}
  
 }
        
        
        
