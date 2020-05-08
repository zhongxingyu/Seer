 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hritserver.constants;
 
 /**
  * Configurable global settings
  * @author desmond
  */
 public class Globals 
 {
     /** controls local/external fetching of jquery */
     //public static final String JQUERY_SITE = "code.jquery.com";
     public static final String JQUERY_SITE = "localhost";  
     /** disables import */
    public static final boolean DEMO = false;
     /** maximum size of an uploaded file - don't increase unless you have to */
     public static int MAX_UPLOAD_LEN = 102400;
     
 }
