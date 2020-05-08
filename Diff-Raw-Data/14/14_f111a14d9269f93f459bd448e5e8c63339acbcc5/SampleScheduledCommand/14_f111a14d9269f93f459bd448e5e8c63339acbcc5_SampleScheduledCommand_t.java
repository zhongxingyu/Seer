 /* vim: set ts=2 et sw=2   cindent fo=qroca: */
 
 package com.globant.katari.sample;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Date;
 
 import com.globant.katari.quartz.domain.ScheduledCommand;
 
 /** This is a sample scheduled command to showcase the quartz module.
  *
  * It just waits for 100 seconds. The percent is simply the number of seconds
  * since execute was called.
  */
 public class SampleScheduledCommand implements ScheduledCommand {
 
  /** The duration of the task, in millis.
   */
  private static final int DURATION = 100000;

  /** Milliseconds per second.
   */
  private static final int MS_PER_SECOND = 1000;

   /** The time that execute started, null if this task is not beign executed.
    */
   private Date startedOn = null;
 
   /** {@inheritDoc}
    */
   public String getDisplayName() {
     return "100 seconds delay";
   }
 
   /** {@inheritDoc}
    */
   public Integer getProgressPercent() {
     Date when = startedOn;
     if (when != null) {
      return (int) ((new Date()).getTime() - when.getTime()) / MS_PER_SECOND;
     } else {
       return null;
     }
   }
 
   /** {@inheritDoc}
    */
   public Map<String, String> getInformation() {
     Map<String, String> information = new HashMap<String, String>();
     if (getProgressPercent() != null) {
       information.put("Status", "Running");
     } else {
       information.put("Status", "Finished");
     }
     return information;
   }
 
   /** {@inheritDoc}
    */
   public Void execute() {
     startedOn = new Date();
     try {
      Thread.sleep(DURATION);
     } catch (InterruptedException e) {
       Thread.currentThread().interrupt();
     }
     startedOn = null;
     return null;
   }
 }
 
