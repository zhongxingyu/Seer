 import java.io.*;
 import java.net.*;
 import java.util.concurrent.ArrayBlockingQueue;
 
 public class ServerThreadedWorker implements  Runnable{
 
     protected Socket clientSocket = null;
     protected String serverText   = null;
     protected PrintWriter outputChannel  = null;
  	static char char_typed;
 	protected String thread_type;
 	protected InputStream input_stream;
 	static int semaphore=0;
 	int num_of_clients;
 	static int countOfPersistentThreads=0;
 	public static ArrayBlockingQueue<String>  queue = new ArrayBlockingQueue<String>(100);
 	static int persistentThreadStatus[] = new int[100]; 		//There can be a max of 100 Persistent threads
 
 
 	public ServerThreadedWorker(Socket clientSocket , String serverText, int num_of_clients){
 		this.clientSocket = clientSocket;
 		this.thread_type = serverText;
 		this.num_of_clients = num_of_clients;
 		try{
 			//Set up the input & output channels
 			outputChannel = new PrintWriter(clientSocket.getOutputStream(),true);
 			input_stream= clientSocket.getInputStream();
 		}catch(IOException e){
 			System.out.println("Could not capture the imput/output stream ");
 		}
 
 		if(serverText == "Persistent"){
 			//Reset the persistentThreadStatus array
 			for(int i=0;i<100;i++)
 				persistentThreadStatus[i] = 0;
 			//Thread.currentThread().setName(Integer.toString(countOfPersistentThreads++) );
 			Thread.currentThread().setName("PUSHKAR " + Integer.toString(countOfPersistentThreads++ ) );
 			System.out.println("Set the name to thread : " + Integer.toString(countOfPersistentThreads) );
 		}
 	}
 
 	public ServerThreadedWorker( String serverText){
 		this.serverText = serverText;
 	}
 
 	synchronized void setPersistentThreadStatus(int threadId){
 		persistentThreadStatus[threadId] = 1;
 	}
 	synchronized void resetPersistentThreadStatus(int threadId){
 		persistentThreadStatus[threadId] = 0;
 	}
 	synchronized void setPersistentThreadStatusAllThreads(){
 		for(int i=0;i<100;i++)	
 			persistentThreadStatus[i] = 1;
 	}
 	synchronized void resetPersistentThreadStatusAllThreads(){
 		for(int i=0;i<100;i++)	
 			persistentThreadStatus[i] = 0;
 	}
 	synchronized boolean getPersistentThreadStatus(int threadId){
 		if( persistentThreadStatus[threadId] == 1)
 			return true;
 		else return false;
 	}
 
 	synchronized void setChar(char char_typed){
 		//System.out.println("Char written by Thread : " + Thread.currentThread() );
 		this.char_typed = char_typed;
 		char temp[] = new char[2];
 		temp[0] = char_typed;
 		try{
 			queue.put(new String(temp));
 			queue.put(new String(temp));
 		}catch(InterruptedException e){
 		}
 		setPersistentThreadStatusAllThreads();
 		//this.semaphore++;
 	}
 	synchronized String getCharTyped(){
 		String op = null;
 		//System.out.println("String : " + Thread.currentThread().getName() );
 		String threadName[] = Thread.currentThread().getName().split("-");
 		int threadId=0;
 		if(threadName[1].length()>0) 
 			threadId = Integer.parseInt( threadName[1] );
 		try{
 			op = queue.take();
 			resetPersistentThreadStatus( threadId);
 		}catch(InterruptedException e){
 		}
 		return op;
 			//return char_typed;
 	}
 	synchronized void increment(){
 		this.semaphore++;
 	}
 	synchronized int getSemaphore(){
 		System.out.println("Notified !");
 		//return (int)queue.take();
 		//if(semaphore==0){
 			return semaphore;
 		//}
 		//semaphore--;
 		//System.out.println("Sem returned : " + semaphore + " , for thread : " + Thread.currentThread());
 		//return this.semaphore + 1;
 	}
 
 
 	public void runPersistentConnectionThread(){
 		int counter =0;
 		String persistent_header="HTTP/1.1: 200 OK\r\n"+"Content-Type: text/html\r\n" + "Connection:Keep-Alive\r\n" + "\r\n";
  		outputChannel.println( persistent_header);
 		while(true){
 			String threadName[] = Thread.currentThread().getName().split("-");
 			int threadId = 0;
 			if(threadName[1].length()>0) 
 				threadId=Integer.parseInt( threadName[1] );
 			//int threadId = Integer.parseInt( Thread.currentThread().getName()) ;
 			if(getPersistentThreadStatus(threadId)){
 				String typed = getCharTyped();
 				System.out.println("Thread details : " + Thread.currentThread() + " Thread name ; " + Thread.currentThread().getName() );
 				System.out.println("CHAR SENT !");
  				outputChannel.println(typed.charAt(0));
 			}
 		}
 
 	}
 
 	void runKeyPressThread(){
 		try{
 			//System.out.println("Num of chars available : " + input_stream.available() );
 			int num_of_chars = input_stream.available();
 			char input_char_seq[] = new char[num_of_chars];
 			for(int i =0;i<input_stream.available() ; i++){
 				input_char_seq[i] = (char) input_stream.read();
 			}
 			String client_string = new String(input_char_seq);
 			//System.out.println( "str : " + client_string );
 			
 			int position_of_question =client_string.indexOf( '?' ,  client_string.indexOf("POST") );
 			if(client_string.length() == 0 || position_of_question <=0 ){
 			}else{
 				String query_params = client_string.substring(position_of_question+1,client_string.indexOf("&param=EOS"));
 				//System.out.println("Query param : " + query_params );
 				String q_params[] = query_params.split("&");
 				for(int i=0;i<q_params.length; i++){
 					if( q_params[i].contains("key") ){
 						String  key[] = q_params[i].split("=") ;
 						System.out.println("Key : " + key[1] );
 						setChar(  key[1].charAt(0) );
 					}
 				}
 			}
 
 		}catch(IOException e){
 			
 		}
 		try{
			input_stream.close();
 			outputChannel.println("a");
 			clientSocket.close();	
 		}catch(Exception e){
 			
 		}
 	}
 
 	public void run(){
 		if(this.thread_type == "Persistent" )
 			runPersistentConnectionThread();
 		else if(this.thread_type == "Key")
 			runKeyPressThread();
 		
 	}
 }
