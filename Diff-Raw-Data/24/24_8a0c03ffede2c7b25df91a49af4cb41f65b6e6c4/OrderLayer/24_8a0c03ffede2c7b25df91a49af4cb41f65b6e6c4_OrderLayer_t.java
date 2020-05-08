 package panchat.order;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Queue;
 
 import panchat.data.User;
 import panchat.messages.Message;
 
 public abstract class OrderLayer extends Observable implements Observer {
 
 	protected final static boolean DEBUG = true;
 
 	private List<OrderLayer> bottomLayer = new LinkedList<OrderLayer>();
 
 	protected User user;
 
 	/*
 	 * Cola de mensajes pendientes y mensajes de entrega.
 	 */
 	protected Queue<Message> deliveryQueue = new LinkedList<Message>();
 	protected Queue<Message> pendingQueue = new LinkedList<Message>();
 
 	/*
 	 * Lista de usuarios
 	 */
 	protected LinkedList<User> userList = new LinkedList<User>();
 
 	/**
 	 * Crear capa de ordenacion
 	 * 
 	 * @param user
 	 * @param upLayer
 	 */
 	public OrderLayer(User user) {
 		this.user = user;
 	}
 
 	/**
 	 * Devuelve la capability de ordenación que implementa la capa actual.
 	 * 
 	 * @return
 	 */
 	public abstract Message.Type orderCapability();
 
 	/**
 	 * 
 	 */
 	protected abstract boolean okayToRecv(Message msg);
 
 	/**
 	 * Añadir
 	 */
 	public void addBottomLayers(OrderLayer... bottomLayers) {
 		for (OrderLayer layer : bottomLayers) {
 			this.bottomLayer.add(layer);
 			layer.addObserver(this);
 		}
 	}
 
 	/**
 	 * Enviar mensaje a un usuario
 	 * 
 	 * @param user
 	 * @param msg
 	 */
 	public synchronized void sendMsg(User user, Message msg) {
 		for (OrderLayer layer : bottomLayer)
 			if (msg.isType(layer.orderCapability())) {
 				debug("msg send to " + layer.getClass().toString());
 				layer.sendMsg(user, msg);
 				break;
 			}
 	}
 
 	/**
 	 * Enviar mensaje a multiples usuarios
 	 * 
 	 * @param users
 	 * @param msg
 	 */
 	public synchronized void sendMsg(List<User> users, Message msg) {
 		for (OrderLayer layer : bottomLayer)
 			if (msg.isType(layer.orderCapability())) {
 				debug("msg send to " + layer.getClass().toString());
 				layer.sendMsg(users, msg);
 				break;
 			}
 	}
 
 	/**
 	 * Cuando se actualiza una capa de abajo, cogemos los mensajes que soporta
 	 * nuestra capa.
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 
 		debug("clase " + this.getClass().toString());
 		debug("comprobando pending");
 
 		OrderLayer orderLayer = (OrderLayer) o;
 
 		// Recorremos los mensajes de la cola de mensaje recibidos
 		Iterator<Message> iter = orderLayer.deliveryQueue.iterator();
 
 		while (iter.hasNext()) {
 			Message mensaje = iter.next();
 
 			debug("comprobando mensaje " + mensaje.toString());
 
 			// Si el mensaje es del tipo de la capacidad de la capa actual, lo
 			// eliminamos de la lista y lo añadimos a la lista de mensajes
 			// pendientes.
 			if (mensaje.isType(orderCapability())) {
 				iter.remove();
 
 				this.pendingQueue.offer(mensaje);
 
 				debug("añadido a pending ");

 				debug("pendingQueue " + pendingQueue);
 			}
 		}
 
 		debug("comprobando delivery");
 
 		boolean delivery = false;
 
 		// Comprobamos ahora nuestra cola de mensajes pendientes
 		iter = this.pendingQueue.iterator();
 
 		while (iter.hasNext()) {
 			Message mensaje = iter.next();
 
 			debug("comprobando mensaje " + mensaje.toString());
 
 			if (okayToRecv(mensaje)) {
 
 				// Hay algo que mandar
 				delivery = true;
 
 				// Añadimos nuestro mensaje a la cola de mensajes listos para
 				// enviar a la capa superior
 				deliveryQueue.offer(mensaje);
 
 				// Borramos el mensaje de la lista de mensajes pendientes
 				iter.remove();
 
 				// Volvemos a pasar la lista de pendientes desde el principio
 				iter = pendingQueue.iterator();
 
 				debug("añadido a delivery");
 			}
 		}
 
 		// Si hay algo que enviar, notificamos a las capas superiores
 		if (delivery) {
 			this.setChanged();
 			this.notifyObservers();
 		}
 	}
 
 	/*
 	 * Añadir y quitar usuarios
 	 */
 
 	/**
 	 * Añadir usuario
 	 * 
 	 * @param user
 	 */
 	public void addUser(User user) {
 		// añadimos el usuario a la lista de usuarios
 		this.userList.add(user);
 
 		// añadimos los usuarios en las capas de abajo
 		for (OrderLayer layer : bottomLayer)
 			layer.addUser(user);
 	}
 
 	/**
 	 * Eliminar usuario
 	 * 
 	 * @param user
 	 */
 	public void removeUser(User user) {
 		// borramos el usuario a la lista de usuarios
 		this.userList.remove(user);
 
 		// borramos el usuarios de las capas de abajo
 		for (OrderLayer layer : bottomLayer)
 			layer.addUser(user);
 	}
 
 	protected void debug(String msg) {
 		if (DEBUG)
 			System.out.println(msg);
 	}
 }
