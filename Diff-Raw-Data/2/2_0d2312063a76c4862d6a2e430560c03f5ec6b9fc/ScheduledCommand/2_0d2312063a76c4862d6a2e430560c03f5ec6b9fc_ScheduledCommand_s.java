 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.quartz.domain;
 
 import java.util.Map;
 
 import com.globant.katari.core.application.Command;
 
 /** A command that can provide information to the user.
  *
  * This command is used to execute tasks that can provide information such as
  * progress and friendly display name to the user. This kind of command is
 * usually ran as a scheduled job.
  *
  * @author waabox (emiliano[dot]arango[at]globant[dot]com)
  */
 public interface ScheduledCommand extends Command<Void> {
 
   /** The progress of the command, only makes sense if the command is running.
    *
    * If the command was never ran or it finished, this should be 0.
    *
    * Implementation may not know how to implement this operation. In that case,
    * they must return null.
    *
    * @return an integer between 0 and 100 for the progress in percent of the
    * command, or null if the implementation cannot estimate the percentage of
    * the command completion.
    */
   Integer getProgressPercent();
 
   /** Additional information that can be shown to the user.
    *
    * @return a map with extra information about the command, never null.
    */
   Map<String, String> getInformation();
 
   /** A friendly display name to show to the user.
    *
    * @return the friendly name of the command, never null. */
   String getDisplayName();
 }
 
