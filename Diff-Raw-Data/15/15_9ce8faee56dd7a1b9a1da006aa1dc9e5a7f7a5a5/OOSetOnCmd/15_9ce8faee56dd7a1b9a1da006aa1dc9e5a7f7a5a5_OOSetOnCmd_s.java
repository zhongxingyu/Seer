 
 package edu.neu.nutrons.ultimateascent.commands.onoff;
 
 import edu.neu.nutrons.ultimateascent.subsystems.OnOffSubsystem;
 
 /**
  *
  * @author NUTRONs
  */
public class OOSetONCmd extends OOSetCmd {
 
    public OOSetONCmd(OnOffSubsystem sys) {
         super(sys, true);
     }
 }
