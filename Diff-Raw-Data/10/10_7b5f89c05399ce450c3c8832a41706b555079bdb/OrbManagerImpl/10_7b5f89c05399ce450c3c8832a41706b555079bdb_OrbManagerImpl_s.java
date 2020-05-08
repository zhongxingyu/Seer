 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 
 public class OrbManagerImpl extends OrbManagerSkel {
 	
 	private ORB _orb = null;
 	private RoomRegistryImpl _roomregistryimpl = null;
 	
 	public OrbManagerImpl(ORB orb) {
 		super();
 		_orb = orb;
 	}
 	
 	@Override
 	boolean migrate(XmlMapper xmlmapper) {
 		boolean result = true;
 		// TODO Auto-generated method stub
 		echo("Registrando os objetos recebidos");
 		echo("Classname: "+xmlmapper.getClassname());
 		if (xmlmapper.getClassname().equals("RoomRegistryImpl")) {
 			RoomRegistryXml room_registry_xml = (RoomRegistryXml) xmlmapper;
 			String key = room_registry_xml.getObjectid();
 			String classname = room_registry_xml.getClassname();
 			_roomregistryimpl = new RoomRegistryImpl(key);
 			//_orb.updateKey(new_room_registry.objectReference().toString(), key);
 			List lista_chatroom = room_registry_xml.filhos();
 			for (int i=0; i < lista_chatroom.size(); i++) {
 				XmlMapper xmlmapper_2 = (XmlMapper) lista_chatroom.get(0);
 				registerXmlMapper(xmlmapper_2);
 			}
 		}else if (xmlmapper.getClassname().equals("ChatRoomImpl")) {
 			_roomregistryimpl = (RoomRegistryImpl) _orb.getRoomRegistry();
 			registerXmlMapper(xmlmapper);
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Lista os objetos registrados
 	 */
 	public void list() {
 		Map table_registrados = _orb.getListaObjRegistrados();
 		Iterator iterator = table_registrados.keySet().iterator();
 		int i = 0;
 		while (iterator.hasNext()) {
 		   String name = (String) iterator.next();
 		   if (!table_registrados.get(name).equals(this)){ //nao listo o proprio orbmanager
 			   prompt(i+". "+name+" -> "+table_registrados.get(name));
 			   i++;
 		   }
 		}
 	}
 	
 	/**
 	 * Lista os objetos migrados
 	 */
 	public void migrated() {
 		Map migrados = _orb.getListaObjMigrados();
 		Iterator iterator = migrados.keySet().iterator();
 		prompt("Objetos migrados:");
 		while (iterator.hasNext()) {
 		   String key = (String) iterator.next();
 		   ObjectReference object_reference = (ObjectReference) migrados.get(key);
 		   String host = object_reference.getHost();
 		   String port = String.valueOf(object_reference.getPort());
 		   prompt(key+" -> "+host+":"+port);
 		}
 
 	}
 	
 	public void prompt(String msg) {
 		System.out.println("> "+msg);
 	}
 
 	private void registerXmlMapper(XmlMapper xmlmapper) {
 		if (xmlmapper.getClassname().equals("ChatRoomImpl")) {
 			ChatRoomXml chatroomxml = (ChatRoomXml) xmlmapper;
 			String roomname = chatroomxml.getName();
 			String key = chatroomxml.getObjectid();
 			echo("ChatRoom key -> " + key);
 			ChatRoomImpl chat_room_impl = new ChatRoomImpl(roomname, key);
 			List lista_chatuser = chatroomxml.filhos();
 			for (int i=0; i < lista_chatuser.size(); i++) {
 				ChatUserXml chatuserxml = (ChatUserXml) lista_chatuser.get(i);
 				String name = chatuserxml.getName();
 				String classname = chatuserxml.getClassname();
 				
 				//reference
 				String host = chatuserxml.getHost();
 				String port = chatuserxml.getPort();
 				String reference = chatuserxml.getReference();
 				ObjectReference object_reference = new ObjectReference(reference, host, port);
 				ChatUser chat_user_stub = new ChatUserStub(object_reference);
 				echo("Criado proxy para o usuario "+name+" -> "+reference);
 				chat_room_impl.register(name, chat_user_stub);
 			}
 			_roomregistryimpl.register(roomname, chat_room_impl);
 		}
 	}
 	
 	/*
 	private void echo(String msg){
 		System.out.println("[OrbManagerImpl] "+msg);
 	}
 	*/
 
 	public Map filhos() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
