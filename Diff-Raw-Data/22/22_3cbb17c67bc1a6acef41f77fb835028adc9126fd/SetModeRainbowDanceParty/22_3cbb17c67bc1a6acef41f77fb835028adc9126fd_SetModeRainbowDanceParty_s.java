 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package storm2013.commands.LEDcommands;
 
 /**
  *
  * @author evan1026
  */
 public class SetModeRainbowDanceParty extends Flash {
     
     private static final Color[] colors = {new Color(255,0,0), new Color(255,255,0), new Color(0,255,0),
                                            new Color(0,255,255), new Color(0,0,255), new Color(255,0,255)};
     
     public SetModeRainbowDanceParty(){
        super(colors);
     }
 }
