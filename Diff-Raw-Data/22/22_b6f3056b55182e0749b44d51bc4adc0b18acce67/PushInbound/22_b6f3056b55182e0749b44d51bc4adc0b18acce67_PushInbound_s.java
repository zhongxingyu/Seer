 package org.softwaresynthesis.mytalk.server.connection;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.catalina.websocket.MessageInbound;
 import org.apache.catalina.websocket.WsOutbound;
 
 import com.google.gson.*;
 
 import org.softwaresynthesis.mytalk.server.ControllerManager;
 import org.softwaresynthesis.mytalk.server.abook.IAddressBookEntry;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 public class PushInbound extends MessageInbound {
 
 	public static enum State {AVAILABLE, OCCUPIED;};
 	
 	private Long id;
 	private State state;
 	
 	public void setId(Long n){id=n;}
 	public Long getId(){return id;}
 	public State getState()	{return state;}
 	public void setState(State stato) {state = stato;}
 
 	@Override
 	protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
 		throw new IOException("Metodo non implementato");
 	}
 
 	/**
 	 * Metodo invocato al momento della ricezione di un
 	 * messaggio da parte del client
 	 * 
 	 * @param 	message		{@link CharBuffer} rappresenta il messaggio
 	 * 						sotto forma di testo
 	 */
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
 			ControllerManager.putClient(id, this);
 		}
 		//scambio dati per chiamata
 		else if (type.equals("2")){
 			Long client = gson.fromJson(array.get(1), Long.class);
 			PushInbound sendTo= ControllerManager.findClient(client);
 			String msg = "2|" + gson.fromJson(array.get(2), String.class);
 			getWsOutbound(sendTo).writeTextMessage(CharBuffer.wrap(msg));
 		}
 		//manda mio id al chiamato
 		else if (type.equals("3")){
 			Long client = gson.fromJson(array.get(1), Long.class);
 			PushInbound sendTo= ControllerManager.findClient(client);
 			String msg = "3|" + id;
 			getWsOutbound(sendTo).writeTextMessage(CharBuffer.wrap(msg));
 		}
 		//disconnessione ed eliminazione canale
 		else if (type.equals("4")){
 			Long client = gson.fromJson(array.get(1), Long.class);
 			PushInbound remove= ControllerManager.findClient(client);
 			ControllerManager.removeClient(remove);
 		}
 		//notifica cambio stato ad utenti della rubrica
 		//array[1]={available|||offline||occupied} per stato
 		else if (type.equals("5")){
 			DataPersistanceManager database = this.getDAOFactory();
 			String status= gson.fromJson(array.get(1), String.class);
 			IUserData utente= database.getUserData(this.id);
 			if(status.equals("available")){setState(State.AVAILABLE);}					
 			if(status.equals("occupied")){setState(State.OCCUPIED);}
 			
 			//ricavo tutti gli amici con metodo da definire
 			Set<IAddressBookEntry> friends = utente.getAddressBook();
 			Iterator<IAddressBookEntry> iter = friends.iterator();
 			while(iter.hasNext())
 			{
 				IAddressBookEntry entry = (IAddressBookEntry)iter.next();
 				Long idFriend=entry.getContact().getId();
 				PushInbound sendTo= ControllerManager.findClient(idFriend);
				String msg = "5|" + id + "|" + state.toString().toLowerCase();
				getWsOutbound(sendTo).writeTextMessage(CharBuffer.wrap(msg));
 			}
 		}
 		else if (type.equals("6")){
 			String msg = "6|";
			getWsOutbound(this).writeTextMessage(CharBuffer.wrap(msg));
 		}
 	}
 
 	/*
 	 * Sssssssh! Se leggi queste righe non dire niente a nessuno! 
 	 */
 	WsOutbound getWsOutbound(PushInbound inbound) {
 		return inbound.getWsOutbound();
 	}
 	
 	DataPersistanceManager getDAOFactory() {
 		return new DataPersistanceManager();
 	}
 }
