 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model;
 
 /**
  *
  * @author paulHaller
  */
 public class SpielFeld {
 
     private String SPIELER_NAME = "Super Mario";
     private String COMPUTER_NAME = "Super C";
     private int playerPosition;
     private int computerPosition;
     private int playerRoll;
     private boolean playerOil;
     private int computerRoll;
     private boolean computerOil;
     private boolean finished;
     private int[] OIL_FIELDS = {2, 5};
     private final int NUMBER_OF_FIELDS = 6;
 
     public SpielFeld() {
         playerPosition = 0;
         computerPosition = 0;
         finished = false;
         playerOil = false;
         computerOil = false;
         playerRoll = 0;
         computerRoll = 0;
 
     }
 
     public String getFuehrender() {
         if (getPlayerPosition() == getComputerPosition()) {
             return "mehrere";
         } else if (getPlayerPosition() > getComputerPosition()) {
             return getSPIELER_NAME();
         } else {
             return getCOMPUTER_NAME();
         }
     }
 
     /**
     * @return the SPIELER_NAME
     */
     public void move(int player, int computer) {
         if (!isFinished()) {
             setPlayerRoll(player);
             setComputerRoll(computer);
             setPlayerOil(false);
             setComputerOil(false);
             for (int i : getOIL_FIELDS()) {
                 if (getPlayerPosition() + player == i) {
                     setPlayerPosition(0);
                     setPlayerOil(true);
                 }
                 if (getComputerPosition() + computer == i) {
                     setComputerPosition(0);
                     setComputerOil(true);
                 }
             }
             if (!isPlayerOil()) {
                 setPlayerPosition(getPlayerPosition() + player);
                 if (getPlayerPosition() >= getNUMBER_OF_FIELDS()) {
                     setPlayerPosition(getNUMBER_OF_FIELDS());
                     setFinished(true);
                 }
             }
             if (!isComputerOil() && !isFinished()) {
                 setComputerPosition(getComputerPosition() + computer);
                 if (getComputerPosition() >= getNUMBER_OF_FIELDS()) {
                     setComputerPosition(getNUMBER_OF_FIELDS());
                     setFinished(true);
                 }
             }
         }
 
 
 
     }
 
     public String getSPIELER_NAME() {
         return SPIELER_NAME;
     }
 
     /**
      * @param SPIELER_NAME the SPIELER_NAME to set
      */
     public void setSPIELER_NAME(String SPIELER_NAME) {
         this.SPIELER_NAME = SPIELER_NAME;
     }
 
     /**
      * @return the COMPUTER_NAME
      */
     public String getCOMPUTER_NAME() {
         return COMPUTER_NAME;
     }
 
     /**
      * @param COMPUTER_NAME the COMPUTER_NAME to set
      */
     public void setCOMPUTER_NAME(String COMPUTER_NAME) {
         this.COMPUTER_NAME = COMPUTER_NAME;
     }
 
     /**
      * @return the playerPosition
      */
     public int getPlayerPosition() {
         return playerPosition;
     }
 
     /**
      * @param playerPosition the playerPosition to set
      */
     public void setPlayerPosition(int playerPosition) {
         this.playerPosition = playerPosition;
     }
 
     /**
      * @return the computerPosition
      */
     public int getComputerPosition() {
         return computerPosition;
     }
 
     /**
      * @param computerPosition the computerPosition to set
      */
     public void setComputerPosition(int computerPosition) {
         this.computerPosition = computerPosition;
     }
 
     /**
      * @return the playerRoll
      */
     public int getPlayerRoll() {
         return playerRoll;
     }
 
     /**
      * @param playerRoll the playerRoll to set
      */
     public void setPlayerRoll(int playerRoll) {
         this.playerRoll = playerRoll;
     }
 
     /**
      * @return the playerOil
      */
     public boolean isPlayerOil() {
         return playerOil;
     }
 
     /**
      * @param playerOil the playerOil to set
      */
     public void setPlayerOil(boolean playerOil) {
         this.playerOil = playerOil;
     }
 
     /**
      * @return the computerRoll
      */
     public int getComputerRoll() {
         return computerRoll;
     }
 
     /**
      * @param computerRoll the computerRoll to set
      */
     public void setComputerRoll(int computerRoll) {
         this.computerRoll = computerRoll;
     }
 
     /**
      * @return the computerOil
      */
     public boolean isComputerOil() {
         return computerOil;
     }
 
     /**
      * @param computerOil the computerOil to set
      */
     public void setComputerOil(boolean computerOil) {
         this.computerOil = computerOil;
     }
 
     /**
      * @return the OIL_FIELDS
      */
     public int[] getOIL_FIELDS() {
         return OIL_FIELDS;
     }
 
     /**
      * @param OIL_FIELDS the OIL_FIELDS to set
      */
     public void setOIL_FIELDS(int[] OIL_FIELDS) {
         this.setOIL_FIELDS(OIL_FIELDS);
     }
 
     /**
      * @return the finished
      */
     public boolean isFinished() {
         return finished;
     }
 
     /**
      * @param finished the finished to set
      */
     public void setFinished(boolean finished) {
         this.finished = finished;
     }
 
     
 
     /**
      * @return the NUMBER_OF_FIELDS
      */
     public int getNUMBER_OF_FIELDS() {
         return NUMBER_OF_FIELDS;
     }
 }
