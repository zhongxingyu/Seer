 /*******************************************************************************
  * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
  * 
  * This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.obiba.opal.shell;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.obiba.opal.shell.commands.Command;
 import org.obiba.opal.web.model.Commands.Message;
 import org.obiba.opal.web.model.Commands.CommandStateDto.Status;
 
 /**
  * Contains a command and the state of its execution.
  */
 public class CommandJob implements OpalShell, Runnable {
   //
   // Constants
   //
 
   private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH'h'mm";
 
   //
   // Instance Variables
   //
 
   private final Command<?> command;
 
   private final List<Message> messages;
 
   private Integer id;
 
   private String owner;
 
   private Status status;
 
   private long submitTime;
 
   private Long startTime;
 
   private Long endTime;
 
   //
   // CommandJob
   //
 
   public CommandJob(Command<?> command) {
     if(command == null) throw new IllegalArgumentException("command cannot be null");
     this.command = command;
     this.command.setShell(this);
     this.messages = new ArrayList<Message>();
     this.status = Status.NOT_STARTED;
   }
 
   //
   // OpalShell Methods
   //
 
   public void printf(String format, Object... args) {
     if(format == null) throw new IllegalArgumentException("format cannot be null");
     messages.add(createMessage(String.format(format, args)));
   }
 
   public void printUsage() {
     // nothing to do
   }
 
   public char[] passwordPrompt(String format, Object... args) {
     // nothing to do -- return null
     return null;
   }
 
   public String prompt(String format, Object... args) {
     // nothing to do -- return null
     return null;
   }
 
   public void exit() {
     // nothing to do
   }
 
   public void addExitCallback(OpalShellExitCallback callback) {
     // nothing to do
   }
 
   //
   // Runnable Methods
   //
 
   public void run() {
     try {
       printf("Job started.");
 
      int errorCode = 0;

       // Don't execute the command if the job has been cancelled.
       if(!status.equals(Status.CANCEL_PENDING)) {
         status = Status.IN_PROGRESS;
         startTime = getCurrentTime();
         errorCode = command.execute();
       }
 
       // Update the status. Set to SUCCEEDED/FAILED, based on the error code, unless the status was changed to
       // CANCEL_PENDING (i.e., job was interrupted); in that case set it to CANCELED.
       if(status.equals(Status.IN_PROGRESS)) {
         status = (errorCode == 0) ? Status.SUCCEEDED : Status.FAILED;
       } else if(status.equals(Status.CANCEL_PENDING)) {
         status = Status.CANCELED;
       } else {
         // Should never get here!
         throw new IllegalStateException("Unexpected CommandJob status: " + status);
       }

      printf("Job completed successfully.");
     } catch(RuntimeException ex) {
       status = Status.FAILED;
      printf("Job has failed due to the following error :\n%s", ex.getMessage());
       ex.printStackTrace();
     } finally {
       endTime = getCurrentTime();
     }
   }
 
   //
   // Methods
   //
 
   public Integer getId() {
     return id;
   }
 
   public void setId(Integer id) {
     this.id = id;
   }
 
   public Command<?> getCommand() {
     return command;
   }
 
   public String getOwner() {
     return owner;
   }
 
   public void setOwner(String owner) {
     this.owner = owner;
   }
 
   public Status getStatus() {
     return status;
   }
 
   public void setStatus(Status status) {
     this.status = status;
   }
 
   public Date getSubmitTime() {
     return new Date(submitTime);
   }
 
   public void setSubmitTime(Date submitTime) {
     this.submitTime = submitTime.getTime();
   }
 
   public Date getStartTime() {
     return startTime != null ? new Date(startTime) : null;
   }
 
   public String getStartTimeAsString() {
     return formatTime(getStartTime());
   }
 
   public Date getEndTime() {
     return endTime != null ? new Date(endTime) : null;
   }
 
   public String getEndTimeAsString() {
     return formatTime(getEndTime());
   }
 
   public List<Message> getMessages() {
     return Collections.unmodifiableList(messages);
   }
 
   protected long getCurrentTime() {
     return System.currentTimeMillis();
   }
 
   protected Message createMessage(String msg) {
     return Message.newBuilder().setMsg(msg).setTimestamp(System.currentTimeMillis()).build();
   }
 
   protected String formatTime(Date date) {
     if(date == null) return null;
     SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
     return dateFormat.format(date);
   }
 }
