 package Classes;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 public class DataStorage {
 
 	private Map<String, Usuario> userList ;
 	public DataStorage() {
 
 		userList = new TreeMap<String, Usuario>();
 
 	}
 
 	public void addUsuario(Usuario user){
 		userList.put(user.getId(), user);
 	}
 
 	public boolean hasUser(String login, String email) {
		Set<String> keySet = userList.keySet();
 		Iterator<String> i = keySet.iterator();
 		while ( i.hasNext() ) {
 			String key = i.next();
 			Usuario user = userList.get(key);
			if ( user.getLogin().equals(login) || user.getEmail().equals(email) ) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getUserID(String login) {
		Set<String> keySet = userList.keySet();
 		Iterator<String> i = keySet.iterator();
 		while ( i.hasNext() ) {
 			String key = i.next();
 			Usuario user = userList.get(key);
			if ( user.getLogin().equals(login) ) {
 				return user.getId();
 			}
 		}
 		return null;
 	}
 
 	public Usuario getUser(String userID) {
 		return userList.get(userID);
 	}
 
 	/**
 	public Usuario getUsuarioDesejado(String login) throws EntradaInvalidaException{
 		for (Usuario user : users) {
 			if(user.getLogin() == login){
 				return user;
 			}
		throw new EntradaInvalidaException("Login inv�lido");
 	}
 	**/
}
