 
 package edu.neu.nutrons.ultimateascent.commands.onoff;
 
 import edu.neu.nutrons.ultimateascent.subsystems.OnOffSubsystem;
 
 /**
  *
  * @author NUTRONs
  */
public class OOSetOFFCmd extends OOSetCmd {
 
    public OOSetOFFCmd(OnOffSubsystem sys) {
         super(sys, false);
     }
 }
