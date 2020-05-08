 package panchat.addressing.channels;
 
 import java.util.LinkedList;
 import java.util.Observable;
 
 import panchat.addressing.users.ListaUsuarios;
 import panchat.addressing.users.Usuario;
 
 public class Canal extends Observable implements Comparable<Canal> {
 
 	private static final long serialVersionUID = 1L;
 
 	private String nombreCanal;
 	private LinkedList<Usuario> listadoUsuariosConectados;
 	private LinkedList<Usuario> listadoUsuariosDesconectados;
 	private ListaUsuarios listadoUsuarios;
 
 	private Object mutex = new Object();
 
 	public Canal(String nombreCanal, ListaUsuarios listadoUsuarios) {
 		this.nombreCanal = nombreCanal;
 		this.listadoUsuariosConectados = new LinkedList<Usuario>();
 		this.listadoUsuariosDesconectados = listadoUsuarios
 				.getClonedListaUsuarios();
 		this.listadoUsuarios = listadoUsuarios;
 	}
 
 	/**
 	 * Devuelve el nombre del canal
 	 */
 	public String getNombreCanal() {
 		return nombreCanal;
 	}
 
 	/**
 	 * Método para eleminiar un nuevo usuario a la conversación.
 	 * 
 	 * @param usuario
 	 */
 	public void anyadirUsuario(Usuario usuario) {
 		synchronized (mutex) {
 			listadoUsuariosDesconectados.add(usuario);
 
 			super.setChanged();
 			super.notifyObservers();
 		}
 	}
 	
 	/**
 	 * Método para eleminiar un nuevo usuario a la conversación.
 	 * 
 	 * @param usuario
 	 */
 	public void eliminarUsuario(Usuario usuario) {
 		synchronized (mutex) {
 			listadoUsuariosDesconectados.add(usuario);
 			listadoUsuariosConectados.remove(usuario);
 			
 			super.setChanged();
 			super.notifyObservers();
 		}
 	}
 	
 	/**
 	 * Método para añadir un nuevo usuario a la conversación.
 	 * 
 	 * @param usuario
 	 */
 	public void anyadirUsuarioConectado(Usuario usuario) {
 		synchronized (mutex) {
 			listadoUsuariosConectados.add(usuario);
 			listadoUsuariosDesconectados.remove(usuario);
 			
 			super.setChanged();
 			super.notifyObservers();
 		}
 	}
 
 
 	/**
 	 * Devuelve el elemento conectados cuyo indice es index
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public Usuario getUsuarioConectado(int index) {
 		return listadoUsuariosConectados.get(index);
 	}
 
 	/**
 	 * Devuelve el elemento desconectados cuyo indice es index
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public Usuario getUsuarioDesconectado(int index) {
 		return listadoUsuariosDesconectados.get(index);
 	}
 
 	/**
 	 * Devuelve el número de usuarios conectados
 	 * 
 	 * @return
 	 */
 	public int getNumUsuariosConectados() {
 		return listadoUsuariosConectados.size();
 	}
 
 	/**
 	 * Devuelve el número de usuarios sin conectar
 	 * 
 	 * @return
 	 */
 	public int getNumUsuariosDesconectados() {
 		return listadoUsuarios.getNumUsuarios()
 				- listadoUsuariosConectados.size();
 	}
 
 	/**
 	 * Compueba si el usuario está conectado.
 	 * 
 	 * @param usuario
 	 * @return
 	 */
 	public boolean contains(Usuario usuario) {
 		synchronized (mutex) {
 			return listadoUsuariosConectados.contains(usuario);
 		}
 	}
 
 	/*
 	 * compareTo, equals y toString
 	 */
 
 	@Override
 	public int compareTo(Canal o) {
 		return nombreCanal.compareTo(o.nombreCanal);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Canal) {
 			Canal canal = (Canal) obj;
 			return nombreCanal.equals(canal.nombreCanal);
 		} else
 			return false;
 	}
 
 	@Override
 	public String toString() {
 		return nombreCanal;
 	}
 }
