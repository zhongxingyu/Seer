 package net.threads;
 
 import gui.GUI_Part;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import state.FactoryState;
 import state.PartConfig;
 import net.ServerThread;
 
 public class PartThread extends ServerThread {
	public Map<String, Object> sendMap;
 
 	public PartThread(Socket s, ObjectOutputStream o, ObjectInputStream i,
 			FactoryState st) {
 		super(s, o, i, st);
 		sendMap = new ConcurrentHashMap<String, Object>();
 
 	}
 
	@SuppressWarnings("unchecked")
 	public void loop() {
 		sendMap.clear();
 		
 		//Send and receive from client.
 		sendMap = this.receiveAndSend(sendMap);
 
 		ArrayList<PartConfig> newList = (ArrayList<PartConfig>) sendMap.get("newlist");
 		// Only update list if its not null
 		if (newList != null){
 			updateMap(newList);
 		}
 	}
 
 	// Process updating the factory state part
 	private void updateMap(ArrayList<PartConfig> newList){
 		synchronized(state.partConfigList) {
 			state.partConfigList = newList;
 		}
 	}
 }
