 
 package edu.neu.nutrons.ultimateascent.commands.onoff;
 
 import edu.neu.nutrons.ultimateascent.subsystems.OnOffSubsystem;
 
 /**
  *
  * @author NUTRONs
  */
public class OOSetOffCmd extends OOSetCmd {
 
    public OOSetOffCmd(OnOffSubsystem sys) {
         super(sys, false);
     }
 }
