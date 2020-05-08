 package cinnamon;
 
import server.User;
import server.interfaces.Repository;
import server.interfaces.Response;
 
 import java.util.Map;
 
 /**
  * PoBox (ParameterObjectBox) - store parameters for ChangeTriggers, command invocation and 
  * CinnamonResponse handling.
  */
 public class PoBox {
     
     public Response response;
    public User user;
     public Repository repository;
     public Map<String, Object> params;
     public String command;
     public Boolean endProcessing = false;
 
     public PoBox() {
     }
 
    public PoBox(Response response, User user, Repository repository, Map<String, Object> params, String command) {
         this.response = response;
         this.user = user;
         this.repository = repository;
         this.params = params;
         this.command = command;
     }
 }
