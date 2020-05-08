 package websocket;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.catalina.websocket.MessageInbound;
 import org.apache.catalina.websocket.StreamInbound;
 import org.apache.catalina.websocket.WebSocketServlet;
 import org.apache.catalina.websocket.WsOutbound;
 
 import logic.Characters;
 
 public class WSserver extends WebSocketServlet
 {		
 		//Mysql
 		static Connection con = null;
 		Statement st = null;	
 		static PreparedStatement selectData = null;
 		static PreparedStatement selectData2 = null;
 		static PreparedStatement setUnicod1 = null;
 		static PreparedStatement setUnicod2 = null;
 
 		static String url = "jdbc:mysql://127.0.0.1:3306/MyGame?useUnicode=true&amp;characterEncoding=utf-8";
 		static String user = "MyGame";
 		static String password = "MyGame";
 		
 	
 	 	private final static Set<EchoMessageInbound> connections = new CopyOnWriteArraySet<EchoMessageInbound>(); //все соединения
 	 	static HashMap<String, StreamInbound> allConnections = new HashMap<String, StreamInbound>(); //для поиска соединения по логину игрока	
 	 	 
 		
 	    @Override
 	    protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) 
 	    {
 	        return new EchoMessageInbound();
 	    }
 
 	    private final class EchoMessageInbound extends MessageInbound 
 	    {
 	    	 
 	    	StreamInbound currentConnect; //Для запоминания текущего соединения
 	    	Characters myChar;
 	    	String player1; //4 переменных в которые положим логины игроков
 			String player2;
 			String player3;
 			String currentPlayer;
 			String gameId; //переменная для хранения id игровой сессии, для обновления списка игроков в сессии
 	    	
 	    	@Override
 	        protected void onOpen(WsOutbound outbound) {
 	            connections.add(this);
 	            currentConnect = this;
 	            myChar = new Characters();
 	        }
 
 	        @Override
 	        protected void onClose(int status) {
 	        	connections.remove(this);
 	        	allConnections.remove(currentPlayer);
 	        	myChar.x = 10;
 	        	myChar.y = 240;
 	         }
 	        @Override
 	        protected void onBinaryMessage(ByteBuffer message) throws IOException {
 	        	
 	            getWsOutbound().writeBinaryMessage(message);   
 	        }
 
 	        @Override
 	        protected void onTextMessage(CharBuffer message) throws IOException {
 	        	   	
 	        	String[] result = message.toString().split(",");
 	        	
 	        	if ( result[0].equals("id")) //при соединении с игрой проверка на наличии логина в игровой сессии по ее id
 	        	{	        	
 	        		
 	        		try {
 		        		Class.forName("com.mysql.jdbc.Driver");
 						con = DriverManager.getConnection(url, user, password);
 						setUnicod1 = con.prepareStatement("set character set utf8");
 						setUnicod2 = con.prepareStatement("set names utf8");
 						setUnicod1.execute();
 						setUnicod2.execute();
 			
 						selectData = con.prepareStatement("SELECT * FROM `heroes` WHERE `login` = ? AND `name` = ?"); //проверяем соответствует ли персонаж логину
 						selectData.setString(1, result[2]);
 						selectData.setString(2, result[3]);
 						ResultSet rs = selectData.executeQuery();
 						rs.next();
 						if ( rs.getRow() != 0 ) 
 						{					
 							selectData = con.prepareStatement("SELECT `knight`,`mage`,`archer` FROM `games` WHERE `id` = ?"); //проверяем есть ли доступ к этой игровой сессии
 							selectData.setString(1, result[1]);
 							rs = selectData.executeQuery();
 							rs.next();
 							if ( rs.getString(1).equals(result[2]) || rs.getString(2).equals(result[2]) || rs.getString(3).equals(result[2]) )
 							{
 								allConnections.put(result[2], currentConnect);
 									
 								//пишем логины игроков данной сессии в переменные
 								currentPlayer = result[2];
 								player1 = rs.getString(1);
 								player2 = rs.getString(2);
 								player3 = rs.getString(3);
 								
 								toPlayers("new" + "," + result[2]);
 								
 								gameId = result[1];	//записываем ID игровой сессии для обновления списка игроков в дальнейшем	
 							}
 						}
 	        		} catch (SQLException | ClassNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 	        	}
 	        	else if( result[0].equals("toMembersOfGame"))
 	        	{
 	        		for ( int i = 2; i < result.length; i++ ) //цикл для перебора всех сообщений и последующей записью в переменную с запятими для отправки клиенту
 	        		{
 	        			result[1] = result[1] + "," + result[i];	
 	        		}
 	        		toPlayers(result[1]);	        			
 	        	}
 	        	else if( result[0].equals("move")) //при получении сообщения о передвижении - считаем траекторию
 	        	{
 	        		myChar.move(result[1]);
 	        		toPlayers(currentPlayer + "," + Integer.toString(myChar.x) + "," + Integer.toString(myChar.y));
 	        	}
 	        	else if( result[0].equals("refreshPlayers"))
 	        	{
 	        		refreshPlayers();
 	        	}
 	        	else
 	        	{
 	        		currentConnect.getWsOutbound().writeTextMessage(CharBuffer.wrap("Error, unknown query =)"));
 	        	}
 
 	        }
 	        public void refreshPlayers() //функция обновления игроков в сесии
 	        {
 	        	try {
 	        		Class.forName("com.mysql.jdbc.Driver");
 					con = DriverManager.getConnection(url, user, password);
 					setUnicod1 = con.prepareStatement("set character set utf8");
 					setUnicod2 = con.prepareStatement("set names utf8");
 					setUnicod1.execute();
 					setUnicod2.execute();
 	        		
 		        	ResultSet rs = selectData.executeQuery();
 		        	
 		        	selectData = con.prepareStatement("SELECT `knight`,`mage`,`archer` FROM `games` WHERE `id` = ?"); //проверяем есть ли доступ к этой игровой сессии
 					selectData.setString(1, gameId);
 					
 					rs = selectData.executeQuery();
 					rs.next();
 			
 					//пишем логины игроков данной сессии в переменные
 					player1 = rs.getString(1);
 					player2 = rs.getString(2);
 					player3 = rs.getString(3);
 				
 	        	} catch (SQLException | ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	        	
 	        }
 	        
 	        public void toPlayers(String message) //функция отправки сообщения игрокам текущей сессии
 	        {
 	        	try
         		{
         			StreamInbound temporary1 = allConnections.get(player1); //получаем соединение по логину
         			StreamInbound temporary2 = allConnections.get(player2); //получаем соединение по логину
         			StreamInbound temporary3 = allConnections.get(player3); //получаем соединение по логину
         			 
         			//если соединение по логину было найдено...
         			
         			if ( temporary1 != null )
         			{
         				CharBuffer msgToPlayers = CharBuffer.wrap(message);
         				temporary1.getWsOutbound().writeTextMessage(msgToPlayers); //отправляем сообщение если соединение найдено
         				
         			}
         			if( temporary2 != null )
         			{
         				CharBuffer msgToPlayers = CharBuffer.wrap(message);
         				temporary2.getWsOutbound().writeTextMessage(msgToPlayers); //отправляем сообщение если соединение найдено    
         				
         			}
         			if( temporary3 != null )
         			{
         				CharBuffer msgToPlayers = CharBuffer.wrap(message);
         				temporary3.getWsOutbound().writeTextMessage(msgToPlayers); //отправляем сообщение если соединение найдено	
         				
         			}
         			
         		}
        		catch( Exception ex )
         		{ ex.printStackTrace(); }
 	        }
 
 	        private void broadcast( String messageAll ) //функция отправки сообщения каждому игроку что подключен к серверу, кроме себя
 	        {
 	            for (EchoMessageInbound connection : connections) 
 	            {
 	                try {
 		                    CharBuffer buffer = CharBuffer.wrap(messageAll);	                    
 	                    	connection.getWsOutbound().writeTextMessage(buffer);
    
 	                } catch (IOException ex) {
 	                    
 	                }
 	            }
 	        }
 	    }
 	    
 }
