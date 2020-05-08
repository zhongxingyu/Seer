 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package serveur;
 
 import interfaces.Function;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import exception.UnloggedUserException;
 
 /**
  *
  * @author Nicolas & Sylvain
  */
 public class Serveur implements Function{
     
     protected String name ;
     protected ArrayList<Thread> listThreads;
     protected ArrayList<String> listMessages;
     protected HashMap<String,Integer> listCorrespondance; //the "String" represents the last message received by the user number "Integer"
     
     public Serveur(){
       this.name="Call me daddy";
       this.listThreads = new ArrayList<Thread>();
       this.listMessages = new ArrayList<String>();
       this.listCorrespondance = new HashMap<String,Integer>();
     }
     
     public static void main(String[] args){
         Serveur serv = new Serveur();
     }
 
     @Override
     public String request(String str) {
         char parseChar = ' ';
         int indiceToParse;
         indiceToParse = str.indexOf(parseChar);
         
         String subStr = str.substring(0, indiceToParse);
         
         switch (subStr){
            case "connect":
                 return "l'utilisateur s'est connecté"; //rajouter son nom et son id
            case "send":
                return "message envoyé"; //rajouter le message
            case "bye":
                return "l'utilisateur s'est déconnecté"; //rajouter son nom et son id
            case "who":
                String listUsers = new String();
               for(String idUser : this.listeCorrespondance.keySet()){
                    listUsers+=idUser;
                }
                return listUsers; //liste des personnes présentent dans la hashmap
            default:
                return""; 
         }
         
     }
 
     @Override
     public String[] getMessage(String id) throws UnloggedUserException {
     	String[] messages=null;
 	
 		if(listCorrespondance.get(id)==null){
 			throw new UnloggedUserException("The user '"+id+"' isn't logged on the server");
 		}
 		else{
 	    	int i = listCorrespondance.get(id);
 	    	//Creation of a patern string array ofthe correct size
 	    	String[] patern = new String[listMessages.size()-1-i];
 	    	//Obtention of the subList representing the new messages
 	    	List<String> subList = listMessages.subList(i, listMessages.size()-1);
 	    	//Conversion of the sublist into String array
 	    	messages=subList.toArray(patern);
 		}
     	
     	return messages;
     }
     
     
 }
