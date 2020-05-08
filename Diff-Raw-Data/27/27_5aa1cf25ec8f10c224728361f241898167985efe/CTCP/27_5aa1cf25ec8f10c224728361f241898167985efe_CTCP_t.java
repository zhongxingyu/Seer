 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lighting;
 
 /**
  *
  * @author StompingBrokenGlass <stompingbrokenglass@gmail.com>
  * @since 2013-06-20
  */
 public class CTCP {
     
    
    
    public String processCTCP(String query){
        
        String response = "";
        int space = query.indexOf(" ");
        
        String command ;
        String paramenters ;
        if (space < 0){
            command = query;
        } else {
            command = query.substring(0,space);
            paramenters = query.substring(space+1);
        }
        
        switch (command){
            case "VERSION" :
                response = "VERSION Lighting-catbot ALPHA";
                break;
            default:
                // send an error message identifying an unknown command
                response = "ERRMSG " + query + " :Unknown CTCP query";
        }
        
        return response;
    }
 }
