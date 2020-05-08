 package panchat.linker;
 
 import java.util.*;
 import java.io.*;
 
 import panchat.Panchat;
 import panchat.addressing.users.Usuario;
 import panchat.messages.CausalMessage;
 
 public class CausalLinker extends Linker {
 
 	private CausalMatrix matrix;
 	private Panchat panchat;
 	private UUID myId;
 	private Object mutex = new Object();
 	private Hashtable<UUID, LinkedList<CausalMessage>> ObjectTable = new Hashtable<UUID, LinkedList<CausalMessage>>();
 
 	/*
 	 * Cola de mensajes de entrega.
 	 */
 	LinkedList<CausalMessage> deliveryQ = new LinkedList<CausalMessage>();
 
 	/*
 	 * Cola de mensajes pendientes.
 	 */
 	LinkedList<CausalMessage> pendingQ = new LinkedList<CausalMessage>();
 
 	/**
 	 * 
 	 * @param panchat
 	 * @throws Exception
 	 */
 	public CausalLinker(Panchat panchat) {
 		super(panchat);
 
 		this.panchat = panchat;
 		this.myId = panchat.getUsuario().uuid;
 
		matrix = new CausalMatrix(panchat.getUsuario().uuid, panchat
				.getListaUsuarios());
 	}
 
 	public synchronized void sendMsg(Usuario destId, Object msg) {
 
 		matrix.incrementarDestino(destId);
 
 		super.sendMsg(destId, new CausalMessage(msg, destId, matrix));
 	}
 
 	public synchronized void multicast(LinkedList<Usuario> destIds, Object msg) {
 
 		matrix.incrementarDestino(destIds);
 
 		for (Usuario usuario : destIds)
 			super.sendMsg(usuario, new CausalMessage(msg, usuario, matrix));
 	}
 
 	/**
 	 * Condicción de espera
 	 * 
 	 * @param W
 	 * @param srcId
 	 * @return
 	 */
 	private boolean okayToRecv(CausalMatrix W, UUID srcId) {
 		// if (W[srcId][myId] > M[srcId][myId] + 1)
 		// ....return false;
 		// for (int k = 0; k < N; k++)
 		// ........if ((k != srcId) && (W[k][myId] > M[k][myId]))
 		// ........return false;
 		// return true;
 
 		if (W.getValue(srcId, myId) > matrix.getValue(srcId, myId) + 1)
 			return false;
 
		Iterator<Usuario> iter = panchat.getListaUsuarios().getIterator();
 
		while (iter.hasNext()) {
			UUID k = iter.next().uuid;
 
			if ((k != srcId)
					&& (W.getValue(k, myId) > matrix.getValue(k, myId)))
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Comprobamos la cola de pendientes, y en caso de que estén listos, los
 	 * cambiamos a la cola de entrega.
 	 */
 	private synchronized void checkPendingQ() {
 
 		Iterator<CausalMessage> iter = pendingQ.iterator();
 
 		while (iter.hasNext()) {
 			CausalMessage cm = iter.next();
 
 			if (okayToRecv(cm.getMatrix(), cm.getAddress().uuid)) {
 				iter.remove();
 				deliveryQ.add(cm);
 			}
 		}
 	}
 
 	// polls the channel given by fromId to add to the pendingQ
 	public Object receiveMsg(Usuario fromId) throws IOException {
 		synchronized (mutex) {
 			checkPendingQ();
 		}
 
 		/*
 		 * Mientras no haya mensajes que podamos entregar
 		 */
 		while (deliveryQ.isEmpty()) {
 
 			/*
 			 * Esperamos mientras no hayamos recibido ningún elemento
 			 */
 			try {
 				while (ObjectTable.get(fromId.uuid) == null)
 					ObjectTable.wait();
 
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			CausalMessage cm;
 
 			synchronized (mutex) {
 				cm = ObjectTable.get(fromId.uuid).poll();
 
 				/*
 				 * Añadimos a pendientes
 				 */
 				pendingQ.add(cm);
 
 				/*
 				 * Comprobamos la cola de pendientes, para cambiar los mensajes
 				 * que podamos a la cola de entrega
 				 */
 				checkPendingQ();
 			}
 		}
 
 		synchronized (mutex) {
 			/*
 			 * Obtenemos el primer elemento de pendientes.
 			 */
 			CausalMessage cm = (CausalMessage) deliveryQ.removeFirst();
 
 			/*
 			 * Actualizamos la matrix
 			 */
 			matrix.maxMatrix(cm.getMatrix());
 
 			return cm.getContent();
 		}
 	}
 
 	/**
 	 * Añadimos nuevo usuario en el causal linker
 	 * 
 	 * @param usuario
 	 */
 	public void anyadirUsuario(Usuario usuario) {
 		ObjectTable.put(usuario.uuid, new LinkedList<CausalMessage>());
 	}
 
 	/**
 	 * Añadimos un nuevo mensaje a la cola de mensajes
 	 */
 	public synchronized void anyadirMensaje(UUID uuid, CausalMessage msg) {
 		ObjectTable.get(uuid).add(msg);
 		ObjectTable.notify();
 	}
 }
