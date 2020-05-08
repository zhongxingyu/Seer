 /**
  * 
  * @author Jesse Fish
  *
  */
 import java.io.*;
 import java.net.*;
 import java.util.Random;
 
 public class Server {
 	private InetAddress group=null;
 	private MulticastSocket s=null;
 	private int throw_number;
 	private int timeout_ns;
 
 	private ClientInfo client_a=new ClientInfo();
 	private ClientInfo client_b=new ClientInfo();
 
 	private boolean master=false;
 	private int multicast_port;
 	private String broadcast_ip;
 
 	private ServerSocket serverSocket_a = null;
 	private Socket clientSocket_a = null;
 	private ServerSocket serverSocket_b = null;
 	private Socket clientSocket_b = null;
 
 	private int garbage_counter=0;
 
 	private boolean resultLock=false;
 	
 	private boolean running=true;
 
 	private ThrowResult results [];
 
 	private long start;
 	private int Awins=0;
 	private int Bwins=0;
 	private int ties=0;
 	
 	public Server(int secure_port_a, int secure_port_b, int multicast_port, String broadcast_ip, int throw_number,int timeout_ns, boolean master){
 		this.master=master;
 		this.multicast_port=multicast_port;
 		this.broadcast_ip=broadcast_ip;
 		this.throw_number=throw_number;
 		this.timeout_ns=timeout_ns;
 	}
 
 	/**
 	 * Generates client info an populates client with it
 	 * ensures that the info does not collide with info in check_against
 	 * @param client
 	 * @param check_against
 	 */
 	private void generateInfo(ClientInfo client, ClientInfo check_against){
 
 		Random a=new Random();
 
 		client.scissors=(byte)a.nextInt();
 
 		do{
 			client.paper=(byte)a.nextInt();
 		}while(client.paper==client.scissors);
 		do{
 			client.rock=(byte)a.nextInt();
 		}while(client.rock==client.paper||client.rock==client.scissors);
 		do{
 			client.garbage=(byte)a.nextInt();
 		}while(client.garbage==client.paper||client.garbage==client.scissors||client.garbage==client.rock);
 
 		do{
 			a.nextBytes(client.requestSignal);
 		}while(client.requestSignal[1]==0||checkInfoCollision(client.requestSignal[1],check_against));
 
 		do{
 			client.client_signature=(byte)a.nextInt();
 		}while(client.requestSignal[1]==client.client_signature||checkInfoCollision(client.client_signature,check_against));
 
 		do{
 			a.nextBytes(client.shutdownSignal);
 		}while(client.shutdownSignal[1]==0||client.requestSignal[1]==client.shutdownSignal[1]||client.shutdownSignal[1]==client.client_signature ||checkInfoCollision(client.shutdownSignal[1],check_against));
 
 		do{
 			client.resultSignature=(byte)a.nextInt();
 		}while(client.requestSignal[1]==client.resultSignature||client.shutdownSignal[1]==client.resultSignature || client.client_signature==client.resultSignature ||checkInfoCollision(client.resultSignature,check_against));
 	}
 	/**
 	 * helper function to check if a value collides with any value in client
 	 * @param value
 	 * @param client
 	 * @return
 	 */
 	private boolean checkInfoCollision(byte value,ClientInfo client){
 		if(client==null){
 			return false;
 		}
 		if(value==client.requestSignal[1]||value==client.client_signature||value==client.shutdownSignal[1]||value==client.resultSignature){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * class used to break off a thread to listen for an incoming connection on a socket
 	 * without this you have to listen for one client and then for the other
 	 * @author Jesse Fish
 	 *
 	 */
 	private class ConnectClient extends Thread{
 		ServerSocket socket;
 		public Socket clientSocket;
 		public ConnectClient(ServerSocket socket){
 			this.socket=socket;
 		}
 		public void run() {
 			try {
 				clientSocket=socket.accept();
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.out.println("Accept failed: "+socket.getLocalPort());
 				System.exit(-1);
 			}
 		}
 	}
 
 	/**
 	 * function that blocks until both clients are connected to the
 	 * server on the secure connections
 	 * @param secure_port_a
 	 * @param secure_port_b
 	 */
 	private void connectClients(int secure_port_a, int secure_port_b){
 
 		/**
 		 * *****************Connect to client a********************
 		 * 
 		 */
 		try {
 			serverSocket_a = new ServerSocket(secure_port_a);
 		} catch (IOException e) {
 			System.err.println("Could not listen on port: "+secure_port_a);
 			e.printStackTrace();
 			System.exit(1);
 		}
 		System.out.println("listening for client a");
 		ConnectClient connect1=new ConnectClient(serverSocket_a);
 		connect1.start();
 
 
 		/**
 		 * *****************Connect to client b********************
 		 * 
 		 */
 		if(!master){
 			try {
 				serverSocket_b = new ServerSocket(secure_port_b);
 			} catch (IOException e) {
 				System.err.println("Could not listen on port: "+secure_port_b);
 				e.printStackTrace();
 				System.exit(1);
 			}
 
 			System.out.println("listening for client b");
 			ConnectClient connect2=new ConnectClient(serverSocket_b);
 			connect2.start();
 
 			try {
 				connect2.join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			clientSocket_b=connect2.clientSocket;
 		}
 
 		try {
 			connect1.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		clientSocket_a=connect1.clientSocket;
 	}
 
 
 	/**
 	 * This function receives the names from each client and then generates and
 	 * sends each client its clientinfo for that client
 	 */
 	private void setupClients(){
 		try {
 			OutputStream out_a=null;
 			InputStream in_a=null;
 			OutputStream out_b=null;
 			InputStream in_b=null;
 
 			out_a = clientSocket_a.getOutputStream();
 			in_a =clientSocket_a.getInputStream();
 
 
 			if(!master){
 				out_b = clientSocket_b.getOutputStream();
 				in_b =clientSocket_b.getInputStream();	
 			}
 
 			/*** get a's name ***/
 			int name_length_a=Network.getByte(in_a);
 			client_a.name=new String(Network.getBytesOfLength(in_a, name_length_a));
 
 			if(!master){
 				/*** get b's name ***/
 				int name_length_b=Network.getByte(in_b);
 				client_b.name=new String(Network.getBytesOfLength(in_b, name_length_b));
 
 				/*** send the names to each other ***/
 				out_a.write(name_length_b);
 				out_a.write(client_b.name.getBytes(),0,name_length_b);
 				out_b.write(name_length_a);
 				out_b.write(client_a.name.getBytes(),0,name_length_a);
 
 				/*** send broadcast info to clients ***/
 				out_a.write(broadcast_ip.getBytes().length);
 				out_b.write(broadcast_ip.getBytes().length);
 				out_a.write(broadcast_ip.getBytes());
 				out_b.write(broadcast_ip.getBytes());
 				byte[] port_array=Network.intToByteArray(this.multicast_port);
 				out_a.write(port_array);
 				out_b.write(port_array);
 
 
 				/*** generate and send client info ***/
 				generateInfo(client_a,null);
 				generateInfo(client_b,client_a);
 
 				out_a.write(client_a.getBytes());
 				out_b.write(client_b.getBytes());
 
 				/*** print client information generated ***/
 				System.out.println("client A: ");
 				client_a.print();
 				System.out.println("client B: ");
 				client_b.print();
 			}else{
 				client_b.name="The Master";
 				/*** send client name ***/
 				out_a.write(client_b.name.length());
 				out_a.write(client_b.name.getBytes());
 				/*** send broadcast info***/
 				out_a.write(broadcast_ip.getBytes().length);
 				out_a.write(broadcast_ip.getBytes());
 				
 				byte[] port_array=Network.intToByteArray(this.multicast_port);
 				out_a.write(port_array);
 				/*** generate and send client info ***/
 				generateInfo(client_a,null);
 				out_a.write(client_a.getBytes());
 				/*** print client info ***/
 				System.out.println("client A: ");
 				client_a.print();
 				System.out.println("client B: is the Master");
 			}
 
 			/*** clean up sockets ***/
 			out_a.close();
 			in_a.close();
 			clientSocket_a.close();
 			serverSocket_a.close();
 
 			if(!master){
 				out_b.close();
 				in_b.close();
 				clientSocket_b.close();
 				serverSocket_b.close();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void connectToMulticast(){
 		/**
 		 * *****************Connect to multicast group********************
 		 * 
 		 */
 		try {
 			group = InetAddress.getByName(broadcast_ip);
 			s = new MulticastSocket(multicast_port);
 			s.joinGroup(group);
 			System.out.println("joined multicast group");
 
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private synchronized void setThrowsToGarbage(){
 		client_a.lastThrow=RPSThrow.garbage;
 		client_b.lastThrow=RPSThrow.garbage;
 		client_a.submitTime=start+timeout_ns+1;
 		client_b.submitTime=start+timeout_ns+1;
 	}
 
 	private class UDPThread extends Thread{
 		public UDPThread(){}
 		public void run(){
 			examineUDP();
 		}
 	}
 
 	private void examineUDP(){
 		byte[] buf = new byte[4];
 		while(running){
 
 			DatagramPacket recv = new DatagramPacket(buf, buf.length);
 
 			try {
 				s.receive(recv);
 				synchronized (this){
 					if(!resultLock){
 						if(recv.getLength()==2){
 							if(client_a.lastThrow==RPSThrow.garbage && recv.getData()[1]==this.client_a.client_signature){
 								if(processThrow(recv.getData()[0],client_a)){
 									if(master){
 										getMasterThrow();
 									}
 									continue;
 								}
 							}else if(client_b.lastThrow==RPSThrow.garbage && recv.getData()[1]==this.client_b.client_signature){
 								if(processThrow(recv.getData()[0],client_b))
 									continue;
 							}
 						}
 					}
					garbage_counter++;
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	private void getMasterThrow(){
 		if(client_a.lastThrow==RPSThrow.rock){
 			client_b.lastThrow=RPSThrow.paper;
 		}else if(client_a.lastThrow==RPSThrow.paper){
 			client_b.lastThrow=RPSThrow.scissors;
 		}else{
 			client_b.lastThrow=RPSThrow.rock;
 		}
 		client_b.submitTime=System.nanoTime();
 	}
 
 	private boolean processThrow(byte thrown,ClientInfo client){
 		if(thrown==client.paper){
 			client.lastThrow=RPSThrow.paper;
 			client.submitTime=System.nanoTime();
 			return true;
 		}else if(thrown==client.rock){
 			client.lastThrow=RPSThrow.rock;
 			client.submitTime=System.nanoTime();
 			return true;
 		}else if(thrown==client.scissors){
 			client.lastThrow=RPSThrow.scissors;
 			client.submitTime=System.nanoTime();
 			return true;
 		}
 		return false;
 	}
 	private void sendRequset(ClientInfo client){
 		DatagramPacket requeset = new DatagramPacket(new byte[] {client.requestSignal[0],client.requestSignal[1]},2,group, multicast_port);
 		try {
 			s.send(requeset);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void sendShutdown(ClientInfo client){
 		DatagramPacket requeset = new DatagramPacket(new byte[] {client.shutdownSignal[0],client.shutdownSignal[1]},2,group, multicast_port);
 		try {
 			s.send(requeset);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void runGame(){
 		//spawn udp thread
 		UDPThread udpThread=new UDPThread();
 		udpThread.start();
 
 		results=new ThrowResult[throw_number];
 
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 
 		for(int throwCount=0;throwCount<this.throw_number;throwCount++){
 			//set the old throws to garbage
 			setThrowsToGarbage();
 			synchronized(this){
 				resultLock=false;
 			}
 			//send out the request to A and to B
 			start = System.nanoTime();
 			sendRequset(client_a);
 			if(!master){
 				sendRequset(client_b);
 			}
 	//		System.out.println("Start Throw:"+(throwCount+1));
 			long elapsed=0;
 			//while A and B haven't responded
 			boolean check=true;
 			synchronized(this){
 				check=client_a.lastThrow==RPSThrow.garbage||client_b.lastThrow==RPSThrow.garbage;
 			}
 			//	System.out.println(client_a.lastThrow.name()+" "+client_b.lastThrow.name());
 			while(elapsed<timeout_ns && check){
 				synchronized(this){
 					check=client_a.lastThrow==RPSThrow.garbage||client_b.lastThrow==RPSThrow.garbage;
 				}
 				//time how long it is taking in here
 				elapsed = System.nanoTime() - start;
 			}
 			if(master){
 				if(client_a.lastThrow==RPSThrow.garbage){
 					client_b.lastThrow=RPSThrow.rock;
 				}
 			}
 			synchronized(this){
 				resultLock=true;
 			}
 	//		System.out.println("results are in at "+elapsed + "ns");
 			//determine winner
 		//	System.out.printf("A:%-10s B:%-10s times: %d \t %d \n",client_a.lastThrow.name(),client_b.lastThrow.name(),(client_a.submitTime-start) ,(client_b.submitTime-start) );
 
 			results[throwCount]=new ThrowResult(client_a.lastThrow,client_b.lastThrow,(client_a.submitTime-start),(client_b.submitTime-start));
 			results[throwCount].findWinner();
 
 			//send results
 			try {
 				DatagramPacket resultA = new DatagramPacket( results[throwCount].generateAResultPacket(),3,group, multicast_port);
 				s.send(resultA);
 				if(!master){
 					DatagramPacket resultB = new DatagramPacket( results[throwCount].generateBResultPacket(),3,group, multicast_port);
 					s.send(resultB);
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		//send shutdown signal
 		sendShutdown(client_a);
 		
 		if(!master){
 			sendShutdown(client_b);
 		}
 		calculateTotals();
 		displayResults();
		if(!master){
			this.garbage_counter-=(this.throw_number)*4+2;
		}else{
			this.garbage_counter-=this.throw_number*2+1;
		}
 		System.out.println("garbage "+this.garbage_counter);
 		
 		this.running=false;
 		try {
 			udpThread.join(100);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void calculateTotals(){
 		for(int i=0;i<results.length;i++){
 			if(results[i].winner==Winner.A){
 				this.Awins++;
 			}else if(results[i].winner==Winner.B){
 				this.Bwins++;
 			}else{
 				this.ties++;
 			}
 		}
 	}
 	
 	public void displayResults(){
 		
 		System.out.println("\nFINAL RESULTS:");
 		System.out.printf("%-20s wins: %-5d  losses: %-5d  ties: %-5d\n",client_a.name,Awins,Bwins,ties);
 		System.out.printf("%-20s wins: %-5d  losses: %-5d  ties: %-5d\n",client_b.name,Bwins,Awins,ties);
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 		if(args.length<6){
 			System.out.println("usage: Server secure_port_a secure_port_b muticast_port broadcast_ip throw_number timeout_ms");
			System.out.println("example:Java Server 5500 5501 6789 230.0.0.1 1000 100000000 false");
 			System.exit(1);
 		}
 		System.out.println("Server Starting");
 
 		int secure_port_a=Integer.parseInt(args[0]);
 		int secure_port_b=Integer.parseInt(args[1]);
 		int multicast_port=Integer.parseInt(args[2]);
 		String broadcast_ip=args[3];
 		int throw_number=Integer.parseInt(args[4]);
 		int timeout_ms=Integer.parseInt(args[5]);
 		boolean master=false;
 		if(args.length==7){
 			if(Boolean.parseBoolean(args[6])){
 				master=true;
 			}
 		}
 		Server server=new Server(secure_port_a, secure_port_b, multicast_port, broadcast_ip, throw_number, timeout_ms, master);
 
 		server.connectClients(secure_port_a,secure_port_b);
 		server.setupClients();
 		server.connectToMulticast();
 		try {
 			Thread.sleep(10);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		server.runGame();
 		System.exit(0);
 	}
 
 
 	class ThrowResult{
 		RPSThrow AThrow;
 		RPSThrow BThrow;
 		long AThrowTime;
 		long BThrowTime;
 		Winner winner;
 		public ThrowResult(RPSThrow AThrow, RPSThrow BThrow, long AThrowTime, long BThrowTime){
 			this.AThrow=AThrow;
 			this.BThrow=BThrow;
 			this.AThrowTime=AThrowTime;
 			this.BThrowTime=BThrowTime;
 		}
 		public void findWinner(){
 			if(AThrow!=RPSThrow.garbage && AThrow==BThrow){
 				winner=Winner.Tie;
 			}
 			else if(AThrow!=RPSThrow.garbage&&BThrow==RPSThrow.garbage){
 				winner=Winner.A;
 			}else if(BThrow!=RPSThrow.garbage&&AThrow==RPSThrow.garbage){
 				winner=Winner.B;
 			}
 			else if(AThrow==RPSThrow.paper&&BThrow==RPSThrow.rock||AThrow==RPSThrow.scissors&&BThrow==RPSThrow.paper||AThrow==RPSThrow.rock&&BThrow==RPSThrow.scissors){
 				winner=Winner.A;
 			}
 			else{
 				winner=Winner.B;
 			}
 		}
 		public byte[] generateAResultPacket(){
 			return new byte[]{ client_a.getByteForThrow(AThrow),client_a.getByteForThrow(BThrow) ,client_a.resultSignature};
 		}
 		public byte[] generateBResultPacket(){
 			return new byte[]{ client_b.getByteForThrow(BThrow),client_b.getByteForThrow(AThrow) ,client_b.resultSignature};
 		}
 	}
 	enum Winner{A,B,Tie};
 }
