 package client;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 
 import player_data.World;
 
 class Thread_Socket extends Thread
 {
 	protected BufferedReader in;
 	protected PrintWriter out;
 	protected Socket socket;
 	
 	Thread_Socket() throws IOException
 	{
 		// port 3450
 		InetAddress server_address = InetAddress.getByName(Game.addres);
 		this.socket = new Socket(server_address, 3450);
 	}
 	
 	public void run()
 	{	
 		try
 		{
 			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
 			this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream())), true);
 			new Thread_Update(this.socket).start();
 			
 			String msg;
 			
 			
 			
 			while(Game.is_runing)
 			{
 				msg = in.readLine();
 				process_msg(msg);
 			}
 		}
 		catch (IOException e) 
 		{
 			
 		}
 		finally{
 			out.println("IN:LOGOUT");
 			try {
 				in.close();
 				out.close();
 				socket.close();
 			}catch (IOException e){
 				e.printStackTrace();
 			}
 		}	
 	}
 	
 	public void process_msg(String msg){
 		if(msg.matches("^TIME:[0-9]+$")){
 			String [] tmp = msg.split(":");
 			Game.server_time = Long.parseLong(tmp[1]);
 			Game.ping();
 		}
		else if(msg.matches("^CHAT:[a-zA-Z0-9]+:[!-~-\\s]{1,128}$")){
 			String [] tmp = msg.split(":");
 			Chat.add_to_msg_log("["+tmp[1]+"]: " + tmp[2]);
 		}
 		else if(msg.matches("^LOAD:MAP:SIZE:[0-9]+:[0-9]+$")){
 			String [] tmp = msg.split(":");
 			World.map.rebuild_size(Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4]));
 		}
 		else if(msg.contains("LOAD:MAP:LINE:")){
 			String [] tmp = msg.split(":");			
 			int line_index = Integer.parseInt(tmp[3]);
 			int [] line_data = new int[Integer.parseInt(tmp[4])];
 			for(int i = 5; i < tmp.length; ++i){
 				line_data[i-5] = Integer.parseInt(tmp[i]);
 			}
 			World.map.rebuild_line(line_index, line_data);
 		}
 		else if(msg.matches("^CONNECTION:SUCESS$")){
 			Game.server_msg = msg;
 			Thread_Update.send("UPDATE:AREA");
 			Game.state.set_state("map");
 			Chat.add_to_msg_log("[SERVER] Connection to \""+ Game.addres  + "\" sucess.");
 		}
 		else if(msg.matches("^CONNECTION:FAILED$")){
 			Game.state.set_state("menu");
 			Chat.add_to_msg_log("[SERVER] Failed by connection.");
 		}
 		else{
 			Game.server_msg = msg;
 		}
 	}
 	
 	public void send_msg(String msg){
 		out.println(msg);
 	}
 }
