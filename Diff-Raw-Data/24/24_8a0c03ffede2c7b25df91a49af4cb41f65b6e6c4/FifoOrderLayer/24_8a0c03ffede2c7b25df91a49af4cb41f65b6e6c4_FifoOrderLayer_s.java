 package panchat.order;
 
 import java.util.List;
 
 import panchat.clocks.VectorClock;
 import panchat.data.User;
 import panchat.messages.Message;
 import panchat.messages.Message.Type;
 
 public class FifoOrderLayer extends OrderLayer {
 
 	private VectorClock sendClock;
 	private VectorClock receiveClock;
 
 	int i = 0;
 
 	public FifoOrderLayer(User user) {
 		super(user);
 		sendClock = new VectorClock(user, true);
 		receiveClock = new VectorClock(user, false);
 	}
 
 	@Override
 	public synchronized void sendMsg(List<User> users, Message msg) {
 		// Añadir un tick en cada usuario
 		for (User user : users)
 			sendClock.send(user);
 
 		msg.setClock(orderCapability(), sendClock.clone());
 		super.sendMsg(users, msg);
 	}
 
 	@Override
 	public synchronized void sendMsg(User user, Message msg) {
 		sendClock.send(user);
 		msg.setClock(orderCapability(), sendClock.clone());
 		super.sendMsg(user, msg);
 	}
 
 	@Override
 	protected boolean okayToRecv(Message msg) {
 
 		VectorClock vc = (VectorClock) msg.getClock(orderCapability());
 
 		User sendUser = msg.getUsuario();
 
 		if (vc.getValue(this.user) == receiveClock.getValue(sendUser) + 1) {
 			receiveClock.send(this.user);
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public Type orderCapability() {
 		return Message.Type.FIFO;
 	}
 
 	@Override
 	public void addUser(User user) {
 		sendClock.addUser(user);
 		receiveClock.addUser(user);
 	}
 
 	@Override
 	public void removeUser(User user) {
 		sendClock.removeUser(user);
 		receiveClock.removeUser(user);
 	}
 }
