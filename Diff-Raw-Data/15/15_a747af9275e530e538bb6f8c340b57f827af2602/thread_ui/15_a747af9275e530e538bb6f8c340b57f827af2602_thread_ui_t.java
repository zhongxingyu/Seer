 package server;
 
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 import java.sql.SQLException;
 import java.util.Scanner;
 
 import database.DBAccounts;
 import database.DBItems;
 import database.DBPlayers;
 
 class Server_UI extends Thread{
 	
 	Server_UI(){
 		this.start();
 	}
 	
 	public void run(){
 		Scanner in = new Scanner(System.in);
 		String command;
 		
 		while(Server.is_runing){
 			System.out.print(">");
 			command = in.nextLine();
 			
 			try {
 				command_process(command);
 			} catch (FileNotFoundException | UnsupportedEncodingException e) {
 				System.out.println("Thread_ui err: 1 - invalid command");
 			} catch (SQLException e) {
 				System.out.println("Error in execute SQL command");
 			}
 		}
 		
 		in.close();
 	}
 	
 	private void command_process(String command) throws FileNotFoundException, UnsupportedEncodingException, SQLException{
 		if(command.matches("^help$")){
 			System.out.println("========HELP=========");
 			System.out.println(">shutdown");
 			System.out.println(">debug [on|off]");
 			System.out.println(">add [account|item|player]");
 			System.out.println(">show [accounts|players|connections|items]");
 			System.out.println("=====================");
 		}
 		else if(command.contains("add")){
 			if(command.matches("^add account [a-zA-Z]+ [a-zA-Z0-9]+ [a-zA-Z0-9]+$")){
 				// account_name, password, password
 				String [] tmp = command.split(" ");
 				
 				if(tmp[2] != null && tmp[3] != null && tmp[4] != null){
 					if(tmp[3].equals(tmp[4])){
 						boolean add_result = DBAccounts.addAccount(tmp[2], tmp[3]);
 						
 						if(add_result)
 							System.out.println("Account \"" + tmp[2] + "\" is secessful created");
 						else
 							System.out.println("Account \"" + tmp[2] + "\" is already used");
 					}
 					else{
 						System.out.println(">Different passwords;");
 					}
 				}
 				else{
 					System.out.println(">add account [account] [password] [password]");
 				}
 			}
 			else if(command.matches("^add player [a-zA-Z]+ [a-zA-Z]+$")){
 				String [] tmp = command.split(" ");
 				
 				int accountId = DBAccounts.searchId(tmp[2]);
 				if(accountId != -1){
 					boolean result = DBPlayers.addPlayer(accountId, tmp[3]);
 					if(result)
 						System.out.println("Player " + tmp[3] + " created;");
 					else
 						System.out.println("Player " + tmp[3] + " already created :(;");
 				}
 				else{
 					System.out.println("Wrong Account name;");
 				}
 			}
 			else if(command.matches("^add player [0-9]+ [a-zA-Z]+$")){
 				String [] tmp = command.split(" ");
 				
 				int accountId = Integer.parseInt(tmp[2]);
 				boolean result = DBPlayers.addPlayer(accountId, tmp[3]);
 				if(result)
 					System.out.println("Player " + tmp[3] + " created;");
 				else
 					System.out.println("Player " + tmp[3] + " already created :(;");
 			}
 			else if(command.matches("^add item [0-9]+ [_a-zA-Z]+ [0-9]+ [0-9]+$")){
 				String [] tmp = command.split(" ");
 				
 				// itemType, Name, weight, modelId
 				if(tmp[2] != null && tmp[3] != null && tmp[4] != null && tmp[5] != null){
 					int itemType = Integer.parseInt(tmp[2]);
 					int weight = Integer.parseInt(tmp[4]);
 					int modelId = Integer.parseInt(tmp[5]);
 					tmp[3] = tmp[3].replace("_", " "); // change '_' -> ' ';
 					
 					if(itemType > 0 || weight > 0 || modelId > 0){
 						DBItems.addItem(itemType, tmp[3], weight, modelId);
 					}
 					else{
 						System.out.println(">Item Type, Weight, ModelId > 0 only");
 					}
 				}
 				else{
 					System.out.println(">add item [itemType] [Item_name] [weight] [modelId]");
 				}
 			}
 		}
 		else if(command.matches("^show [a-z]+$")){
 			String [] tmp = command.split(" ");
 			switch(tmp[1]){
 			case "accounts":
 				System.out.println("--------Accounts-----");
 				DBAccounts.showAllAccounts();
 				break;
 			case "players":
 				System.out.println("--------Players------");
 				DBPlayers.showAllPlayers();
 				break;
 			case "connections":
 				show_all_connections();
 				
 				break;
 			case "items":
 				System.out.println("--------Items--------");
 				DBItems.showAllItems();
 				break;
 			default:
				System.out.println(">show [accounts|players|connections|items]");
 			}
 		}
 		else if(command.contains("debug")){
 			if(command.equals("debug on")){
 				Server.debug = true;
 				System.out.println(">debug mode on");
 			}
 			else if(command.equals("debug off")){
 				Server.debug = false;
 				System.out.println(">debug mode off");
 			}
 			else{
 				System.out.println(">debug [on\\off]");
 			}
 		}
 		else if(command.matches("^shutdown$")){
 			Server.is_runing = false;
 			System.exit(0);
 		}
 	}
 	
 	private void show_all_connections(){
 		System.out.println("Connection list: " + Server.client_list.size());
 		System.out.println("client_id | account_id");
 		for(Client client: Server.client_list.values()){
			System.out.println("id: " + client.get_id() + "\taccount: " + client.get_account_id());
 		}
 		System.out.println();
 	}
 }
