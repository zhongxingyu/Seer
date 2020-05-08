 // TCPServer2.java: Multithreaded server
 import java.net.*;
 import java.util.StringTokenizer;
 import java.io.*;
 
 public class TCPServer{
     public static void main(String args[]){
         int numero=0;
         ThreadCounter threadArray;
         BetScheduler betScheduler;
         ConnectionWithServerManager connectionWithServerManager;
         Boolean isPrimaryServer;
         int serverPort, partnerPort;
         
         if (args.length < 3){
         	System.out.println("java TCPServer serverPort partnerPort isPrimaryServer (for this last" +
         			"option, type primary or secondary");
     	    System.exit(0);
         }
         
         serverPort = Integer.parseInt(args[0]);
         partnerPort = Integer.parseInt(args[1]);
         if (args[2].toLowerCase().equals("primary")){
         	isPrimaryServer = true;
         }
         else{
         	isPrimaryServer = false;
         }
         try{
             
             threadArray = new ThreadCounter(10);
             System.out.println("A Escuta no Porto " + serverPort);
             ServerSocket listenSocket = new ServerSocket(serverPort);
             System.out.println("LISTEN SOCKET="+listenSocket);
             
             //IMPORTANT: We are temporarily disabling the bets!!!
            betScheduler = new BetScheduler(threadArray);
             connectionWithServerManager = new ConnectionWithServerManager(serverPort, partnerPort, isPrimaryServer);
             
             while(true) {
                 Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                 System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                 numero ++;
                 new ConnectionChat(clientSocket, numero, threadArray, betScheduler);
                 synchronized (threadArray){
                 	threadArray.insertSocket(clientSocket);
                 }
             }
         }catch(IOException e)
         {System.out.println("Listen:" + e.getMessage());}
     }
 }
 //= Thread para tratar de cada canal de comunicao com um cliente
 class ConnectionChat extends Thread {
 	BetScheduler betScheduler;
 	String user="gaia",pass="fixe";
 	boolean loggedIn=false;
     DataInputStream in;
     Socket clientSocket;
     int thread_number;
     ThreadCounter threadArray;
     
     public ConnectionChat (Socket aClientSocket, int numero, ThreadCounter threadArray, BetScheduler betScheduler) {
         thread_number = numero;
         this.betScheduler=betScheduler;
         try{
             clientSocket = aClientSocket;
             in = new DataInputStream(clientSocket.getInputStream());
             this.threadArray = threadArray;
             this.start();
         }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
     }
     
     public String parseFunction(String input){
     	String result="";
     	String temp;
     	
     	StringTokenizer strToken;
         strToken = new StringTokenizer (input);
         temp=strToken.nextToken();
         
         if(temp.equals("show")){
         	temp=strToken.nextToken();
         	if(temp.equals("matches")){
         		threadArray.sendMessageAll(betScheduler.getMatches(), clientSocket);
         	}else if(temp.equals("credits")){
         		//TODO: por o resultado numa string e devolver
         	}else if(temp.equals("users")){
         		//TODO: por o resultado numa string e devolver
         	}else{
         		result="Unknow Command";
         	}
         }else if(temp.equals("send")){
         	temp=strToken.nextToken();
         	if(temp.equals("all")){
         		threadArray.sendMessageAll(temp, clientSocket);
         	} else if(false/*checkUser(temp)*/){
         		//TODO: verificar se o cliente existe e devolver o socket possivelmente
         	} else{
         		result = "Invalid Command or user Unknow";
         	}
         } else if(temp.equals("reset")){
         	//TODO: faz o reset
         	result = "Your credits were reseted to ";//+user.credits+"Cr";
         } else if(temp.equals("bet")){
         	//TODO: check if next token is integer, collect the remaining infos check them 
         	//if successful result="bet done!"
         } else {
         	result = "Unknown command";
         }
         
     	
 		return result;
     }
     //=============================
     public void run(){
         try{
         	while(!loggedIn){
             	StringTokenizer strToken;
             	String userInfo = in.readUTF();
                 strToken = new StringTokenizer (userInfo);
                 if(strToken.nextToken().equals(user) && strToken.nextToken().equals(pass)){
                 	loggedIn=true;
                 	threadArray.sendMessageUser("log successful",clientSocket);
                 }
                 else{
                 	threadArray.sendMessageUser("log error",clientSocket);
                 }
         	}
             while(true){
                 //an echo server
                 String data = in.readUTF();
                 System.out.println("T["+thread_number + "] Recebeu: "+data);
                 //TODO: parseFunction(data)...
                 /*synchronized (threadArray){
                 	threadArray.sendMessage(data, clientSocket);
                 }*/
             }
         }catch(EOFException e){System.out.println("EOF:" + e);
         }catch(IOException e){System.out.println("IO:" + e);}
     }
 }
 
 class ThreadCounter {
 	//We ought to create a list if we want to expand the capacity of the structure.
 	Socket []threadArray;
 	int counter;
 	DataOutputStream out;
 	
 	public ThreadCounter(int no){
 		threadArray = new Socket[no];
 		counter = 0;
 	}
 	
 	public void insertSocket(Socket s){
 		threadArray[counter] = s;
 		counter++;
 	}
 	
 	public void sendMessageAll(String message, Socket clientSocket){
 		int i;
 		try{
 			for (i = 0; i < counter; i++){
 				//Verifies if we aren't forwarding the message to the sender.
 				if (threadArray[i] != clientSocket){
 					out = new DataOutputStream(threadArray[i].getOutputStream());
 					out.writeUTF(message);
 				}
 			}
 		}catch(Exception e){System.out.println("ERROR");}
 	}
 	
 	public void sendMessageUser(String message, Socket clientSocket){
 		try{
 			out = new DataOutputStream(clientSocket.getOutputStream());
 			out.writeUTF(message);
 		}catch(Exception e){System.out.println("ERROR");}
 	}
 	
 }
