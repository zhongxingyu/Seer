 /***********************************************************************
  * MVSTool                                                             *
  *                                                                     *
  * Copyright (C) 2012 John-Charles D. Sokolow                          *
  *                                                                     *
  * MVSTool is free software; you can redistribute it and/or modify     *
  * it under the terms of the GNU General Public License as published   *
  * by the Free Software Foundation; either version 2 of the License,   *
  * or (at your option) any later version.                              *
  *                                                                     *
  * MVSTool is distributed in the hope that it will be useful, but    *
  * WITHOUT ANY WARRANTY; without even the implied warranty of          *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU    *
  * General Public License for more details.                            *
  *                                                                     *
  * You should have received a copy of the GNU General Public License   *
  * along with MVSTool; if not, write to the Free Software              *
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 *  
  * USA                                                                 *
  ***********************************************************************/
 package edu.niu.cs.students.mvstool.tasks;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 
 import edu.niu.cs.students.task.Task;
 
 import edu.niu.cs.students.netio.LineInput;
 
 import edu.niu.cs.students.mvs.MVSJob;
 import edu.niu.cs.students.mvs.MVSClient;
 
 import edu.niu.cs.students.mvstool.Utils;
 import edu.niu.cs.students.mvstool.Profile;
 import edu.niu.cs.students.mvstool.gui.GUIUtils;
 import edu.niu.cs.students.mvstool.gui.viewer.ViewerWindow;
 
 
 public class GetJobOutputTask extends Task {
   
   File file;
   MVSJob job;
       
   public GetJobOutputTask(MVSJob job){
     
     this.job = job;        
   }
   
   public void run() throws Exception {
     
     file = File.createTempFile("mvsftp","out");
     
     MVSClient client = Profile.getCurrentProfile().getMVSClient();
     
    synchronized(client){
     
      OutputStream out = new FileOutputStream(file);
      LineInput in = client.getJob(job);
      
      Utils.copyLines(in, out);
      
      in.close();
      out.close();
      
      client.finishTransfer();
     
    }
     
     
   }
   
   
   public void success(){
     
     ViewerWindow.showViewerWindow(file);
     
   }
   
   public void failure(final Exception e){
     
     GUIUtils.postMessage(e.getMessage());
     
   }
   
 }
   
   
