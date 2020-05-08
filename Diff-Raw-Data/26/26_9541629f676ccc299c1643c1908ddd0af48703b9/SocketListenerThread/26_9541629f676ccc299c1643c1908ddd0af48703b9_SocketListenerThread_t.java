 package panchat.listeners;
 
 import java.io.*;
 import java.net.Socket;
 
 import panchat.Panchat;
 import panchat.data.Usuario;
 import panchat.messages.CausalMessage;
 import panchat.messages.SimpleMessage;
 
 public class SocketListenerThread extends Thread {
 
 	private final static boolean DEBUG = false;
 
 	private Panchat panchat;
 	private ObjectInputStream ois;
 	private Socket socket;
 	private Usuario usuario;
 
 	public SocketListenerThread(Panchat panchat, Usuario usuario,
 			Socket socket, ObjectInputStream ois) {
 		this.panchat = panchat;
 		this.ois = ois;
 		this.usuario = usuario;
 		this.socket = socket;
 	}
 
 	public void run() {
 
 		printDebug("Creado hilo para escuchar a : " + usuario.nickName);
 
 		while (!socket.isClosed()) {
 			try {
 				// Leyendo objeto
 				Object msg = ois.readObject();
 
 				// Si es un mensaje causal
 				if (msg instanceof CausalMessage) {
 
 					printDebug("mensaje añadido al causal linker");
 
 					// Lo añadimos en la cola de pendientes del causalLinker
 					panchat.getCausalLinker().anyadirMensaje(
 							(CausalMessage) msg);
 
 				}
 				// Si es un mensaje simple
 				else if (msg instanceof SimpleMessage) {
 
 					printDebug("mensaje añadido al simple linker");
 
 					// Lo añadimos en la cola de pendientes del causalLinker
 					panchat.getLinker().anyadirMensaje((SimpleMessage) msg);
 
 				}
 			} catch (IOException e) {
 				// El socket se ha cerrado
 				try {
 					socket.close();
 				} catch (IOException e1) {
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
		printDebug("Hilo terminado, se ha cerrado el socket");

		// Lo eliminamos del listado de usuarios

		printDebug("Borramos a " + usuario.nickName
				+ " de la lista de usuarios");
		panchat.getListaUsuarios().eliminarUsuario(usuario);

		// Lo eliminamos del listado de canales

		panchat.getListaCanales().eliminarUsuario(usuario);
 	}
 
 	private void printDebug(String string) {
 		String msgClase = "ListenerThread.java: ";
 		if (DEBUG)
 			System.out.println(msgClase + string);
 	}
 }
