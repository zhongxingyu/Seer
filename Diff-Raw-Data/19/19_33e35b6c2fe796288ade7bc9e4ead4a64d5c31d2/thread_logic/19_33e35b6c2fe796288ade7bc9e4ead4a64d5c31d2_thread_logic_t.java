 package server;
 
 import World.*;
 
 class Thread_Logic extends Thread
 {
 	// Timer
 	private final long Logic_Update = 10;
 	private final long Logic_Delta = 1000/Logic_Update;
 	
 	// World
 	private static World world_data;
 	
 	Thread_Logic(){
 		world_data = new World();
 		this.start();
 	}
 	
 	public void run(){
 		long Last_Update = System.currentTimeMillis();		
 		while(Server.is_runing){
 			if(System.currentTimeMillis() - Last_Update >= Logic_Delta){
 				if(Server.client_list.size() == 0 || Server.event_buffer.size() == 0){
 					try{
 						Thread.sleep(1000);
 					}
 					catch (InterruptedException e){
 						e.printStackTrace();
 					}
 				}
 				else{
 					Thread_Logic.events_process();
 				}
 				
 				Last_Update = System.currentTimeMillis();
 			}
 		}
 	}
 	
 	public void send_time(){
 		for(Client value: Server.client_list.values()){
 			value.send("TIME:" + Long.toString(System.currentTimeMillis()));
 		}
 	}
 	
 	public static void events_process(){
 		for(int i = 0; i < Server.event_buffer.size(); ++i){
 			Event tmp = Server.event_buffer.get(i);
 			
 			if(tmp.data.matches("^TIME:[0-9]+$")){
 				
 			}
 			else if(tmp.data.matches("^UPDATE:AREA$")){
 				update_map(tmp.client_id);
 			}
			else if(tmp.data.matches("^CHAT:[!-~-\\s]{1,128}$")){
 				String [] data = tmp.data.split("^CHAT:");
 				if(data.length ==  2)
 					event_chat_msg(tmp.client_id, data[1]);
 			}
 			else if(tmp.data.matches("^IN:CONNECT:[a-zA-Z0-9]+:[a-zA-Z0-9]+$")){
 				String [] data = tmp.data.split(":");
 				events_in_connect(tmp.client_id, data[2], data[3]);
 			}
 			else if(tmp.data.matches("^IN:CHOSE:PLAYER:[a-zA-Z]+$")){
 				String [] data = tmp.data.split(":");
 				events_chose_player(tmp.client_id, data[3]);
 			}
 			else if(tmp.data.matches("^IN:LOGOUT$")){
 				events_in_logout(tmp.client_id);
 			}
 			else if(tmp.data.matches("^CREATE:PLAYER:[a-zA-Z]+$")){
 				String [] data = tmp.data.split(":");
 				events_create_player(tmp.client_id, data[2]);
 			}
 			else if(tmp.data.matches("^DISCONNECT$")){
 				close_socket(tmp.client_id);
 			}
 		}
 		Server.event_buffer.clear();
 	}
 	
 	private static void events_in_connect(int client_id, String account, String password){
 		int account_id = DB.db_accounts.search_account(account);
 		
 		if(DB.db_accounts.compare_password(account_id, password) == true
 			&& DB.db_accounts.check_login(account_id) == false)
 		{	
 			update_map(client_id);
 			Server.client_list.get(client_id).send("CONNECTION:SUCESS");
 			Server.client_list.get(client_id).set_account_id(account_id);
 			DB.db_accounts.connect(account_id);
 			if(Server.debug)
 				System.out.println("Connection \""+account+"\" is sucess" );
 		}
 		else{
 			Server.client_list.get(client_id).send("CONNECTION:FAILED");
 			if(Server.debug)
 				System.out.println("Connection \""+account+"\" is failed" );
 		}
 	}
 	
 	private static void events_create_player(int client_id, String name){
 		if(Server.client_list.get(client_id).get_account_id() != -1){
 			boolean result = DB.db_players.add_player(Server.client_list.get(client_id).get_account_id(), name);
 			if(result)
 				Server.client_list.get(client_id).send("CREATE:SUCESS");
 			else
 				Server.client_list.get(client_id).send("CREATE:FAILED");
 		}
 		else
 		{
 			Server.client_list.get(client_id).send("CREATE:FAILED");
 		}
 	}
 	private static void events_in_logout(int client_id){
 		try{
 			if(Server.client_list.get(client_id) != null)
 				Server.client_list.get(client_id).disconnect();
 		}catch(Exception e){
 			System.out.println("Disconnection err: " + e);
 		}
 	}
 	private static void close_socket(int client_id){
 		Server.client_list.remove(client_id);
 	}
 	private static void update_map(int client_id){
 		world_data.world_map.get("null").Send_Map(Server.client_list.get(client_id).get_socket().get_out_stream());
 	}
 	private static void events_chose_player(int client_id, String player_name){
 		Server.client_list.get(client_id).set_player_id(DB.db_players.search_player(Server.client_list.get(client_id).get_account_id(), player_name));
 	}
 	private static void event_chat_msg(int client_id, String msg){
 		int account_id = Server.client_list.get(client_id).get_account_id();
 		if(account_id == -1)
 			return;
 		else{
 			String account_name = DB.db_accounts.get_name(account_id);
 			for(Client client: Server.client_list.values()){
 				client.send("CHAT:" + account_name + ":" + msg);
 			}
 		}
 	}
 }
