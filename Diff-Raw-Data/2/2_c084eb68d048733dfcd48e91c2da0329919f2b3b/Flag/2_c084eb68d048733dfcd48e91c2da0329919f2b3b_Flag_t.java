 package com.gmail.jameshealey1994.restrictedteleport.commands;
 
 /**
  * Flag to be used in Restricted Teleport commands.
  *
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public enum Flag {
 
     /**
      * String signifying the silent flag.
      */
     SILENT_FLAG ("-s");
 
     /**
      * The string value of the Flag.
      */
     private String string;
 
     /**
      * Constructor - Initialises string value.
      *
      * @param string        string value of the Flag
      */
     private Flag(String string) {
         this.string = string;
     }
 
     /**
      * Returns the string value of the Flag.
      *
      * @return      string value of the Flag
      */
     public String getString() {
         return string;
     }
 
     @Override
     public String toString() {
        return string;
     }
 }
