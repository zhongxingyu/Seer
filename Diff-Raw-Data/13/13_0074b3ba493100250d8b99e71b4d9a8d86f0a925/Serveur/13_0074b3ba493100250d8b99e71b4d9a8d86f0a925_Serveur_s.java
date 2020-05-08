 package serveur;
 
 import interfaces.Function;
 
 import java.rmi.Naming;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import exception.BadRequest;
 import exception.UnloggedUserException;
 
 /**
  *
  * @author Nicolas & Sylvain
  */
 public class Serveur extends UnicastRemoteObject implements Function{
 
 	private static final long serialVersionUID = -6816583950234559142L;
 	private static Registry reg;
 	protected String name ;
     protected ArrayList<Thread> listThreads;
     protected ArrayList<String> listMessages;
     protected HashMap<String,Integer> listCorrespondance; //the "String" represents the last message received by the user number "Integer"
     
     public Serveur() throws RemoteException{
       this.name="Call me daddy";
       this.listThreads = new ArrayList<Thread>();
       this.listMessages = new ArrayList<String>();
       this.listCorrespondance = new HashMap<String,Integer>();
     }
     
     public static void main(String[] args){
     	Serveur serveur;
     	
     	System.out.println("Mise en place du security manager");
     	if(System.getSecurityManager() == null)
     		System.setSecurityManager(new RMISecurityManager());
     	try
     	{
     		reg = LocateRegistry.createRegistry(1099);
     		serveur = new Serveur();
     		//Change Url for the server's one
     		//Naming.rebind("//127.0.0.1/C:/users/Nicolas/Desktop/ei3/info/objet/tp_tchat/tchat/", serveur);
    		Naming.rebind("//127.0.0.1/C:/users/sylvain/workspace/tchat/tchat/bin", serveur);
         	System.out.println("Serveur " + serveur.name +" configuré");
     	}
     	catch(Exception e)
      	{
     		System.out.println("\nProblème !!\n\n " + e.toString());
 			e.printStackTrace();
      	}
     	
         /* Test for the server
         //Test
         try{
         //Connection of diferents users
         System.out.println(serv.request("connect Loutrosky"));
         
         System.out.println(serv.request("connect GitanEnCaravane"));
         System.out.println(serv.request("connect T0t0_du_44"));
         
         System.out.println(serv.request("who"));
         
         //Sending messages
         
         System.out.println(serv.request("send Loutrosky Je suis une loutre bien grasse"));
         System.out.println(serv.request("send T0t0_du_44 Et tu aimes ça!!!"));
         System.out.println(serv.request("send GitanEnCaravane tu t'appellerais pas Sébastien par hasard ?"));
         String[]str = serv.getMessage("GitanEnCaravane");
         for(int i = 0; i<str.length; i++){
         	System.out.println(str[i]);
         }
         
         //Quit
         System.out.println(serv.request("bye Loutrosky"));
         
         //Sending messages
         System.out.println(serv.request("send T0t0_du_44 Tu l'as vexé..."));
         System.out.println(serv.request("send GitanEnCaravane Quelle tata celui-la"));
         str = serv.getMessage("T0t0_du_44");
         for(int i = 0; i<str.length; i++){
         	System.out.println(str[i]);
         }
         System.out.println(serv.request("send GitanEnCaravane Bon je m'en vais"));
         str = serv.getMessage("GitanEnCaravane");
         for(int i = 0; i<str.length; i++){
         	System.out.println(str[i]);
         }
         
         //Quit
         System.out.println(serv.request("bye GitanEnCaravane"));
         System.out.println(serv.request("bye T0t0_du_44"));
         }
         catch(Exception e){
             System.out.println("erreur " + e.getMessage());
         }*/
     }
 
     
     public String request(String str) throws BadRequest{
         char parseChar = ' ';
         int indiceToParseREQ;
         int indiceToParseID;
         
         indiceToParseREQ = str.indexOf(parseChar); 
         String subStrA;
         String subStrBC;
         String subStrB;
         String subStrC;
         
         if(indiceToParseREQ==-1){
             subStrA = str;
             subStrBC="";
         }
         else {
             subStrA = str.substring(0, indiceToParseREQ); 
             subStrBC = str.substring(indiceToParseREQ+1); 
         }
         
         indiceToParseID = subStrBC.indexOf(parseChar);
 
         if (indiceToParseID == -1){
             subStrB=subStrBC;
             subStrC="";
         }
         else {
             subStrB = subStrBC.substring(0, indiceToParseID); 
             subStrC = subStrBC.substring(indiceToParseID+1);
         }
       
         switch (subStrA){
            case "connect":
                //TODO if B empty throws exception
                if (subStrB.equals("")){
                    throw new BadRequest("Please give us your name... =( ");
                }
                else if(!subStrC.equals("")){
                     throw new BadRequest("the pattern of a connection is \"connect id\", please respect this pattern !");
                }
                else{
                    this.listCorrespondance.put(subStrB,this.listMessages.size());
                    System.out.println("Serv info :" +subStrB+ " connected");
                    return "the user " +subStrB+ " is connected";
                }
                
            case "send":
                
                if (subStrB.equals("")){
                    throw new BadRequest("Please give us your name... =( ");
                }
                else if(subStrC.equals("")){
                     throw new BadRequest("the pattern of a connection is \"send id message\", please respect this pattern !");
                }
                else{
                    if(!this.listCorrespondance.containsKey(subStrB)){
                        throw new BadRequest("You have to connect before tchating!");
                    }
                    else{
                        this.listMessages.add(subStrC);
                        System.out.println("Serv info : message send");
                        return "message send";
                    }
 
                }
                
            case "bye":
  
                if (subStrB.equals("")){
                    throw new BadRequest("Please give us your name... =( ");
                }
                else if(!subStrC.equals("")){
                     throw new BadRequest("the pattern of a connection is \"bye id\", please respect this pattern !");
                }
                else{
                     if(!this.listCorrespondance.containsKey(subStrB)){
                        throw new BadRequest("You have to connect before leaving... Daft boy !!!!");
                    }
                    else{
                         this.listCorrespondance.remove(subStrB);
                         System.out.println("Serv info : "+subStrB+" disconnected"); 
                         return "l'utilisateur "+subStrB+" s'est déconnecté"; 
                     }
                }
                
            case "who":
                if(!subStrB.equals("")){
                     throw new BadRequest("the pattern of a connection is \"who\", please respect this pattern !");
                }
                else{
                 String listUsers = new String();
                 for(String idUser : this.listCorrespondance.keySet()){
                     listUsers+=idUser+" ";
                 }
                 System.out.println("Serv info : who request");
                 return listUsers; //liste des personnes présentent dans la hashmap
                }
            default:
                throw new BadRequest("The request "+ subStrA +" does not exist ! Try another one please.");
                
         }
         
     }
 
     @Override
     public String[] getMessage(String id) throws UnloggedUserException, BadRequest {
     	String[] messages=null;
 	
 		if(listCorrespondance.get(id)==null){
 			throw new UnloggedUserException("The user '"+id+"' isn't logged on the server");
 		}
 		else{
 	    	int i = listCorrespondance.get(id);
 	    	if(i == listMessages.size()){
 	    		throw new BadRequest("No new messages");
 	    	}
 	    	else{
 		    	//Creation of a patern string array ofthe correct size
 		    	String[] patern = new String[listMessages.size()-1-i];
 		    	//Obtention of the subList representing the new messages
 		    	List<String> subList = listMessages.subList(i, listMessages.size());
 		    	//Conversion of the sublist into String array
 		    	messages=subList.toArray(patern);
 		    	listCorrespondance.put(id, listMessages.size());
 	    	}
 		}
     	
     	return messages;
     }
     
     
 }
