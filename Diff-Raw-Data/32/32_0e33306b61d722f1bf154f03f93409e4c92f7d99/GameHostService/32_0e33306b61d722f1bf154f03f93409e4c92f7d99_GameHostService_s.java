 package ca.team615.memorygameandroid;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Random;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 public class GameHostService extends Service {
 
 	private static final String tag = "MemoryGameService";
 
 	int[] assignments;
 
 	int[] scores;
 
 	Socket[] sockets;
 
 	ServerSocket serverSocket;
 
 	PrintWriter[] writers;
 
 	Thread[] threads;
 
 	@Override
 	public void onCreate() {
 
 
 	}
 
 
 
 	@Override
 	public void onStart(Intent intent, int startId){
 		super.onStart(intent, startId);
 
 		scores = new int[2];
 		scores[0] = 0;
 		scores[1] = 0;
 
 		sockets = new Socket[2];
 		writers = new PrintWriter[2];
 
 		threads = new Thread[2];
 
 		try {
 			int port = 9999;
 			Log.i(tag, "Attempting to create server on port " + port);
 			serverSocket = new ServerSocket(port);
 			Log.i(tag, "Created server on port " + port +"\n");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		assignments = new int[16];
 		for(int i = 0; i < 16; i++){
 			assignments[i] = -1;
 		}
 
 		Random random = new Random();
 
 		//for each card, (we have 8) loop through.
 		for(int i = 0; i < 8; i++){
 			//each card goes in 2 slots
 			for (int j = 0; j < 2; j++){
 				//generate a random slot
 				int randomSlot = random.nextInt(16);
 				//make sure that the slot isn't already populated
 				while(assignments[randomSlot] != -1){
 					randomSlot = random.nextInt(16);
 				}
 				//set this card to that slot
 				assignments[randomSlot] = i;
 				Log.i(tag, "Putting " + i + " in slot " + randomSlot);
 			}
 
 		}
 
 		Thread initThread = new Thread(new Initializer());
 		initThread.start();
 
 	}
 
 	private class Initializer implements Runnable {
 
 		@Override
 		public void run() {
 
 
 			try {
 
 				for(int i = 0; i < 2; i++){
 					Log.i(tag, "\nWaiting for Client " + i);
 					sockets[i] = serverSocket.accept();
 					writers[i] = new PrintWriter(sockets[i].getOutputStream());
 					Log.i(tag, "Client " + i +" connected");
 					writers[i].println("disableall");
 					writers[i].flush();
 					GameRunner gameRunner = new GameRunner(sockets[i], i);
 					threads[i] = new Thread(gameRunner);
 					threads[i].start();
 				}
 
 				writers[0].println("loaded");
 				writers[0].println("enableall");
 				writers[0].flush();
 
 				writers[1].println("loaded");
 				writers[1].flush();
 
 				Log.i(tag, "");
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try{
 				threads[0].join();
 			}catch(Exception e){}
 			try{
 				threads[1].join();
 			}catch(Exception e){}
 		}
 
 	}
 
 	private class GameRunner implements Runnable {
 
 		Socket socket;
 		BufferedReader reader;
 		PrintWriter writer;
 		int clientId;
 		int opponentId;
 
 		protected GameRunner(Socket client, int clientId){
 			this.socket = client;
 			this.clientId = clientId;
 			this.opponentId = (clientId + 1) %2;
 			try {
 
 				reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
 				writer = new PrintWriter(client.getOutputStream());
 
 				String init = "cardorder";
 				for(int i: assignments){
 					init += " " + i;
 				}
 				writer.println(init);
 				writer.flush();
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void run() {
 			while(true){
 				try {
 					String command = reader.readLine();
 					if(command == null){
 						Log.i(tag, "Reading nothing from player " + clientId + "'s socket");
 						writers[opponentId].println("opponentlost");
 						writers[opponentId].flush();
 						break;
 					}
 					if(command.startsWith("quit")){
 						//writers[clientId].println("quit");
 						//writers[clientId].flush();
 						socket.close();
						writers[opponentId].println("opponentquit");
						writers[opponentId].flush();
 						break;
 					}else if(command.startsWith("pause")){
 						writers[opponentId].println("opponentpause");
 						writers[opponentId].flush();
 						break;
 					}else if(command.startsWith("resume")){
 						writers[opponentId].println("opponentresume");
 						writers[opponentId].flush();
 						break;
 					}else if(command.startsWith("select")){
 						processSelect(command, clientId);
 					}
 
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 
 		} //end of run
 
 	} //end of inner class
 
 	int flippedCards = 0;
 	int currentIndex = 0;
 	int lastIndex = 0;
 
 	int foundPairs = 0;
 
 	void processSelect(String command, int client){
 		int index = Integer.parseInt(command.substring(7, command.length()));
 		Log.i(tag, "Client " + client + " has selected card " + index);
 
 		int opponent = (client + 1) % 2;
 
 		writers[client].println("flip " + index);
 		writers[client].flush();
 		writers[opponent].println("flip " + index);
 		writers[opponent].flush();
 
 		flippedCards++;
 
 		if(flippedCards == 2){
 
 			currentIndex = index;
 			Log.i(tag, "Assigned at "+ currentIndex +": " + assignments[currentIndex] + " Assigned at " + lastIndex +": " + assignments[lastIndex]);
 
 			flippedCards = 0;
 			writers[client].println("disableall");
 			writers[client].flush();
 			try{
 				Thread.sleep(1000);
 			}catch(InterruptedException e){};
 
 			if(assignments[currentIndex] == assignments[lastIndex]){
 				scores[client]++;
 				foundPairs++;
 
 				writers[client].println("remove " + currentIndex + " " + lastIndex);
 				writers[opponent].println("remove " + currentIndex + " " + lastIndex);
 				writers[client].println("score " + scores[client] +" " + scores[opponent]);
 				writers[opponent].println("score " + scores[opponent] +" " + scores[client]);
 			}else{
 				writers[client].println("flop " + currentIndex + " " + lastIndex);
 				writers[opponent].println("flop " + currentIndex + " " + lastIndex);
 			}
 			if(foundPairs == 8){
				if(scores[client] > scores[opponent]){
 					writers[client].println("win");
 					writers[opponent].println("lose");
 				}else{
 					writers[client].println("lose");
 					writers[opponent].println("win");
 				}
 			}else{
 				writers[opponent].println("enableall");
 
 			}
 			writers[client].flush();
 			writers[opponent].flush();
 
 		}else{
 			lastIndex = index;
 		}
 
 	}
 
 
 	@Override
 	public void onDestroy() {
 		Log.i(tag, "Service Distroyed");
 		super.onDestroy();
 	}
 
 
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// don't bind
 		return null;
 	}
 }
