 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package internet;
 
 import internet.OS.*;
 import java.util.Random;
 
 /**
  *
  * @author FedorUvarychev
  */
 public class Computer {
 
     /**
      * Class which represents a computer with operation system and ability to
      * become infested. i hope it'll work.
      * }|{ecTka4 noJlHblN /0\
      * Нужно запилить нормальный пул с осями и оттуда брать чертовы объескты.
      * @param string is one of 'windows' 'ubuntu' 'macos'
      */
     public Computer(int num, int opSystem, int info) {
         this.number = num;
        this.isInfected = (0 != info);
        
         if (opSystem == 1) {
             this.installedOS = new Windows();
         } else if (opSystem == 2) {
             this.installedOS = new Ubuntu();
         } else if (opSystem == 3) {
             this.installedOS = new MacOS();
         }
     }
 
     /**
      * Function imitating a procedure of infestation and a work of antivirus :D.
      */
     public void infestation(Randomizer rand) {
         if (installedOS.getVulner() > rand.nextInt(100)) {
             isInfected = true;
         }
     }
     
     public int getNumber() {
         return number;
     }
     public boolean getState() {
         return isInfected;
     }
     public String getOS() {
         return installedOS.getName();
     }
     
     private int number;
     private boolean isInfected;
     private OperationSystem installedOS;
 }
