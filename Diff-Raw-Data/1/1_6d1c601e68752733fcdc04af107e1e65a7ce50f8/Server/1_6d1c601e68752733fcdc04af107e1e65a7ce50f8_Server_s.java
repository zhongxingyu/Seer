 /*
 	Define the detailed practice of a server
  */
 import java.rmi.RemoteException;
 import java.lang.*;
 import java.rmi.server.*;
 import maze_game.*;
 
 class Server implements ServerRemote {
 
 	static int MaxPlayer = 10;
 
 	Status status;	// the global game status
 	ClientRemote[] clients;	// the remote stubs of clients
 	ServerRemote backup;	// the remote stub of the backup or the server
 	Boolean flag;	// if the game starts
 	Boolean first;	// if it's the first player to join the game
 	public Boolean isBackup;	// if it's the backup server
 	public static boolean isOn = true;	//if the game is still on
 	int totalBeans;	// total beans
 	int id;	// its own player_id
 	Heartbeat hb;
 
 // initiate the server object as the first server
 	public Server(int N, int M) {
 		status = new Status(N);
 		clients = new ClientRemote[MaxPlayer];
 		totalBeans = M;
 		flag = true;
 		first = true;
 		isBackup = false;
 		id = -1;
 		hb = new Heartbeat();
 		(new Thread(hb)).start();
 	}
 
 // initiate the server object as the backup server
 	public Server(ServerRemote server, Status new_status,
 			ClientRemote[] new_clients) {
 		status = new_status;
 		clients = new_clients;
 		backup = server;
 		totalBeans = 0;
 		for (int i = 0; i < status.size * status.size; i++)
 			totalBeans += status.board[i];
 		flag = false;
 		first = false;
 		isBackup = true;
 		hb = new Heartbeat();
 		(new Thread(hb)).start();
 	}
 
 // set the server's player_id
 	public void setId(int player_id) {
 		id = player_id;
 	}
 	
 // the implementation of join()
 	public synchronized int join(ClientRemote client) throws RemoteException {
 		int player_id = 0;
 		synchronized (first) {
 			if (first) {
 				first = false;
 				FirstJoin firstjoin = new FirstJoin();
 				(new Thread(firstjoin)).start();
 			}
 		}
 		synchronized (flag) {
 			if (flag) {
 				for (int i = 0; i < status.players.length; i++)
 					if (status.players[i] == null) {
 						status.players[i] = new Player();
 						status.players[i].id = i;
 						status.players[i].ingame = 1;
 						player_id = i;
 						status.numPlayers++;
 						clients[player_id] = client;
 						return player_id;
 					}
                 System.out.println("full.");
 				return -1;
 			} else{
                 System.out.println("game start.");
 				return -1;
             }
 		}
 	}
 
 // the implementation of move()
 	public synchronized Status move(int player_id, int dir) throws RemoteException {
 		boolean ifMove = true;
 		synchronized (status) {
 			int delta = status.players[player_id].position + dir;
 			for (int i = 0; i < status.numPlayers; i++)
 				if (status.players[i] != null && delta == status.players[i].position)
 					ifMove = false;
 			if (delta < 0 || delta >= status.size * status.size)
 				ifMove = false;
 			if (status.players[player_id].position % status.size == 0 && dir == -1)
 				ifMove = false;
 			if ((status.players[player_id].position + 1) % status.size == 0
 					&& dir == 1)
 				ifMove = false;
 			if (ifMove) {
 				status.players[player_id].position = delta;
 				status.players[player_id].beans += status.board[delta];
 				totalBeans -= status.board[delta];
 				status.board[delta] = 0;
 				if (!isBackup) 
 					try {
 						backup.sync(player_id, delta, status.players[player_id].beans);
 					} catch (RemoteException re) {
 						
 					}
 				if (totalBeans == 0)
 					endgame();
 			}
 		}
 		return status;
 	}
 
 // end the game and notify all clients
 	public void endgame() throws RemoteException {
         synchronized (flag) {
             flag = true;
         }
         for (int i = 0; i < status.numPlayers; i++)
             if (status.players[i].ingame == 1)
                 try {
                     clients[i].stop(status);
                 } catch (RemoteException re) {
 
                 }
         try {
             backup.refresh(true);
         } catch (RemoteException re) {
 
         }
         try{
             UnicastRemoteObject.unexportObject(Peer.exportedServer, true);
         }catch(RemoteException e){
             System.err.println("Unexport exception: " + e.toString());
         }
 	}
 
 // start the game, initiate game states, and nofity all clients
 	public void gamestart() throws RemoteException{
 		int posi;
 		synchronized (flag) {
 			flag = false;
 		}
 		for (int i = 0; i < totalBeans; i++) {
 			posi = (int) (Math.random() * (status.size * status.size));
 			status.board[posi]++;
 		}
 
 		for (int i = 0; i < status.numPlayers; i++) {
 			posi = (int) (Math.random() * (status.size * status.size));
 			while (status.board[posi] != 0)
 				posi = (int) (Math.random() * (status.size * status.size));
 			status.players[i].position = posi;
 			status.board[posi] = -1;
 		}
 		for (int i = 0; i < status.size * status.size; i++)
 			if (status.board[i] == -1)
 				status.board[i] = 0;
 		for (int i = 0; i < status.numPlayers; i++)
 			clients[i].start(status);
 	}
 
 // the implementation of quit()
 	public synchronized boolean quit(int player_id) throws RemoteException {
 		status.players[player_id].ingame = 0;
 		clients[player_id] = null;
 		return true;
 	}
 
 // the implementation of sync()
 	public synchronized void sync(int player_id, int position, int beans)
 			throws RemoteException {
 		status.board[position] = 0;
 		status.players[player_id].position = position;
 		status.players[player_id].beans = beans;
 	}
 	
 // used to get the server itself
 	public ServerRemote getServerRemote() {
 		return this;
 	}
 	
 // used to make sure the state of the server and the backup is normal
 	public void refresh(boolean stop) {
         if (stop){
             isOn = false;
             try{
                 UnicastRemoteObject.unexportObject(Peer.exportedServer, true);
             }catch(RemoteException e){
                 System.err.println("Unexport exception: " + e.toString());
             }
         }
 	}
 	
 // the thread for counting the time
 	class FirstJoin implements Runnable {
 		public synchronized void run() {
 			try {
 				wait(20000);
 			} catch (InterruptedException ie) {
 				
 			}
 			try {
 				gamestart();
 			} catch (RemoteException re) {
 				
 			}
 		}
 	}
 	
 // the thread for maintaining the state, and in case of crashing, find a new client to be the server or backup
     class Heartbeat implements Runnable {
         public synchronized void run() {
             while (isOn == true) {
                 //System.err.println("heartbeat");
                 try {
                     this.wait(1000);
                 } catch (InterruptedException ie) {
 
                 }
                 synchronized (flag) {
                     //System.out.println("flag = "+flag);
                     if (flag) continue;
                 }
                 try {
                     backup.refresh(false);
                 } catch (Exception e) {
                     System.out.println("Start finding backup server");
                     synchronized (status) {
                         for (int i = 0; i < status.numPlayers; i++) 
                             if (i != id)
                                 try {
                                     backup = clients[i].becomeBackup(getServerRemote(), status, clients);
                                     System.out.println("new backup server: "+i);
                                     break;
                                 } catch (RemoteException re2) {
 
                                 }
                         isBackup = false;
                     }
                     System.out.println("start broadcasting");
                     ServerRemote self = getServerRemote();
                     for (int j = 0; j < status.numPlayers; j++){
                         System.out.println("broadcasting p"+j);
                         if (status.players[j].ingame == 1){
                             try {
                                 clients[j].changeServer(self, backup);
                             } catch (RemoteException re3) {
                                 status.players[j].ingame = 0;
                             }
                         }else{
                             System.out.println("p"+j+" not in game");
                         }
                     }
                 }
             }
             // if the other server down
 				// lock status
 				// for all peers != self, ask becomeBackup
 				// isBackup = false
 				// unlock status
 				// broadcast new server and backup server to all client
 		}
 	}
 
 }
