 package panchat.order;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 import panchat.clocks.CausalMatrix;
 import panchat.data.User;
 import panchat.messages.Message;
 import panchat.messages.Message.Type;
 
 public class CausalMatrixLayer extends OrderLayer {
 
 	private CausalMatrix matrix;
 
 	public CausalMatrixLayer(User myId) {
 		super(myId);
 		this.matrix = new CausalMatrix(myId);
 	}
 
 	/**
 	 * Envío de mensaje causal
 	 * 
 	 * @param destId
 	 * @param msg
 	 */
 	@Override
 	public synchronized void sendMsg(User user, Message msg) {
 
 		matrix.send(user);
 
 		msg.setClock(orderCapability(), matrix.clone());
 
 		super.sendMsg(user, msg);
 	}
 
 	/**
 	 * Envío de mensaje multicast y causal
 	 * 
 	 * @param destIds
 	 * @param msg
 	 */
 	@Override
 	public synchronized void sendMsg(List<User> users, Message msg) {
 
 		matrix.send(users);
 
 		msg.setClock(orderCapability(), matrix.clone());
 
 		super.sendMsg(users, msg);
 	}
 
 	/**
 	 * Condicción de espera
 	 * 
 	 * @param W
 	 * @param srcId
 	 * @return
 	 */
 	private boolean okayToRecv(CausalMatrix W, User srcId) {
 		// Algoritmo original
 		//
 		// if (W[srcId][myId] > M[srcId][myId] + 1)
 		// ....return false;
 		//
 		// for (int k = 0; k < N; k++)
 		// ........if ((k != srcId) && (W[k][myId] > M[k][myId]))
 		// ........return false;
 		//
 		// return true;
 
 		if (W.getValue(srcId, user) > matrix.getValue(srcId, user) + 1) {
 			return false;
 		}
 
 		// Primero añadimos unas columnas que falten en las filas ya existentes.
 		Iterator<Entry<User, Hashtable<User, Integer>>> iter = matrix.HashMatrix
 				.entrySet().iterator();
 		while (iter.hasNext()) {
 
 			User kIduuid = iter.next().getKey();
 			if ((!kIduuid.equals(srcId))
 					&& (W.getValue(kIduuid, user) > matrix.getValue(kIduuid,
 							user))) {
 
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	protected boolean okayToRecv(Message msg) {
 
 		CausalMatrix cm = (CausalMatrix) msg.getClock(orderCapability());
 
 		if (okayToRecv(cm, msg.getUsuario())) {
 
 			/*
 			 * Actualizamos la matrix
 			 */
 			matrix.receiveAction(cm);
 
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public Type orderCapability() {
 		return Message.Type.CAUSAL;
 	}
 
 	/**
 	 * Añadimos nuevo usuario en el causal linker
 	 * 
 	 * @param usuario
 	 */
 	@Override
 	public void addUser(User usuario) {
 		matrix.addUser(usuario);
 	}
 
 	/**
 	 * Añadimos nuevo usuario en el causal linker
 	 * 
 	 * @param usuario
 	 */
 	@Override
 	public void removeUser(User usuario) {
 		// FIXME
 	}
 }
