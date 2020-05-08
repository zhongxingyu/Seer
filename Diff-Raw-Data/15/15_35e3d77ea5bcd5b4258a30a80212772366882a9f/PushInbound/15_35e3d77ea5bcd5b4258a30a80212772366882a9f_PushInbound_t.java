 package org.softwaresynthesis.mytalk.server.connection;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.List;
 
 import org.apache.catalina.websocket.MessageInbound;
 import com.google.gson.*;
import org.softwaresynthesis.mytalk.server.dao.UserDataDAO;
import org.softwaresynthesis.mytalk.server.abook.IUserData;
import org.softwaresynthesis.mytalk.server.abook.IUserData.State;
 
 public class PushInbound extends MessageInbound {
 
 private Long id;
 	
 	public void setId(Long n){id=n;}
 	public Long getId(){return id;}
 
 	@Override
 	protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	protected void onTextMessage(CharBuffer message) throws IOException {
 		String text=message.toString();
 		
 		Gson gson= new Gson();
 		JsonParser parser = new JsonParser();
 	    JsonArray array = parser.parse(text).getAsJsonArray();
 	    String type = gson.fromJson(array.get(0), String.class);
 	    
 	    //identificazione canale
 		if(type.equals("1")){
 			Long value = gson.fromJson(array.get(1), Long.class);
 			setId(value);
 			String msg= "1|"+gson.fromJson(array.get(1), String.class);
 			getWsOutbound().writeTextMessage(CharBuffer.wrap(msg));
 		}
 		//scambio dati per chiamata
 		else if (type.equals("2")){
 			Long client = gson.fromJson(array.get(1), Long.class);
 			PushInbound sendTo= ChannelServlet.findClient(client);
 			String msg = "2|" + gson.fromJson(array.get(2), String.class);
 			sendTo.getWsOutbound().writeTextMessage(CharBuffer.wrap(msg));
 		}
 		//manda mio id al chiamato
 		else if (type.equals("3")){
 			Long client = gson.fromJson(array.get(1), Long.class);
 			PushInbound sendTo= ChannelServlet.findClient(client);
 			String msg = "3|" + id;
 			sendTo.getWsOutbound().writeTextMessage(CharBuffer.wrap(msg));
 		}
 		//disconnessione ed eliminazione canale
 		else if (type.equals("4")){
 			Long client = gson.fromJson(array.get(1), Long.class);
 			PushInbound remove= ChannelServlet.findClient(client);
 			ChannelServlet.removeClient(remove);
 		}
 		//notifica cambio stato ad utenti della rubrica
 		//array[1]= email, array[2]={available|||offline||occupied} per stato
 		else if (type.equals("5")){
			UserDataDAO database= new UserDataDAO();
 			String email= gson.fromJson(array.get(1), String.class);
 			String state= gson.fromJson(array.get(2), String.class);
 			IUserData utente= database.getByEmail(email);
 			if(state.equals("available")){utente.setState(State.AVAILABLE);}
 			if(state.equals("offline")){utente.setState(State.OFFLINE);}						
 			if(state.equals("occupied")){utente.setState(State.OCCUPIED);}
 			
 			//ricavo tutti gli amici con metodo da definire
 			List<IUserData> friends= utente.getAddressBook();
 			for(int i=0; i<friends.size(); i++){
 				Long idFriend=(friends.get(i)).getId();
 				PushInbound sendTo= ChannelServlet.findClient(idFriend);
 				String msg = "5|" + id + "|" + state;
 				sendTo.getWsOutbound().writeTextMessage(CharBuffer.wrap(msg));
 			}
 		}
 	}
 
 }
