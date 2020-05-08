 package ca.hilikus.jrobocom_samples.bank_jumper;
 
 import ca.hilikus.jrobocom.player.Bank;
 
 /**
  * Simple bank that jumps to the next bank. Intended to be a "sitting target"
  *
  * @author hilikus
  */
 public class JumpBank extends Bank {
     
     private static int currentBank = 0;
 
     public JumpBank(int pTeamId) {
 	super(pTeamId);
     }
 
     @Override
     public void run() {
 	control.turn(true);
 	currentBank = (currentBank + 1) % info.getBanksCount();
 	control.changeBank(currentBank);
     }
 
 }
