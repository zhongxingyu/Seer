 package objectIO.netObject;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Queue;
 
 import objectIO.connections.Connection;
 import objectIO.connections.Hub;
 import objectIO.markupMsg.MarkupMsg;
 
 public class ObjController implements ObjControllerI, Runnable{
 	private ConcurrentHashMap<String, NetObject> objects = new ConcurrentHashMap<String, NetObject>();
 	private boolean run = true;
 	private Hub<?> hub;
 	
 	public int threadSleep = 10;
 	
 	public ObjController(Hub<?> talkhub) {
 		this.hub = talkhub;
 	}
 	
 	public void stopRunning() {
 		run = false;
 	}
 	
 	public void run() {
 		run = true;
 		while(run) {
 			distributeRecievedUpdates();
 			try { Thread.sleep(threadSleep); } catch (Exception ex) { ex.printStackTrace(); }
 		}
 	}
 	
 	public NetObject getById(String id) {
 		return objects.get(id);
 	}
 	
 	public boolean syncObject(NetObject obj) {
 		if (objects.containsKey(obj.id))
 			throw new HashMapKeyCollision(obj.id + " has already been used");
 		return (objects.put(obj.id, obj) == null);
 	}
 
 	public boolean unsyncObject(NetObject obj) {
 		return (objects.remove(obj.id) != null);
 	}
 	
 	public void sendUpdate(MarkupMsg msg, NetObject object, long connectionId) {
 		msg.name = object.getId();
 		hub.sendMsg(msg, connectionId);
 	}
 	
 	public void distributeRecievedUpdates() {
 		for (Connection c : hub.getAllConnections()) {
 			distrubute(c);
 		}
 	}
 	
 	private void distrubute(Connection c) {
 		Queue<MarkupMsg> queue = c.msgQueue();
 		MarkupMsg msg = null;
		while ((msg = queue.poll()) != null) {
 			NetObject obj = objects.get(msg.name);
 			if (obj != null)
 				obj.parseUpdate(msg, c);
 			else
 				System.out.println("Missing: " + msg.name + " content:" + msg.toString());
 		}
 	}
 }
