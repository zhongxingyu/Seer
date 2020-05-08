 package littlegruz.arpeegee.entities;
 
 import java.util.HashMap;
 
 public class RPGParty{
    private String name;
    private HashMap<String, String> members;
    private HashMap<String, String> invited;
    
    public RPGParty(String name){
       this.name = name;
       members = new HashMap<String, String>();
       invited = new HashMap<String, String>();
    }
 
    public void setName(String name){
       this.name = name;
    }
 
    public String getName(){
       return name;
    }
    
    public void addMember(String name){
       members.put(name, name);
    }
    
    public String removeMember(String name){
       return members.remove(name);
    }
    
    public HashMap<String, String> getMembers(){
       return members;
    }
    
    public boolean isMember(String name){
       if(members.get(name) != null)
          return true;
       else
          return false;
    }
    
    public String addInvitation(String name){
       return invited.put(name, name);
    }
    
    public String removeInvitation(String name){
       return invited.remove(name);
    }
    
    public boolean isInvited(String name){
       if(invited.get(name) != null)
          return true;
       else
          return false;
    }
    
    public HashMap<String, String> getInvitations(){
      return members;
    }
 }
