 package system;
 
 import java.rmi.RemoteException;
 
 
 public class Executor extends Thread{
 	PeerImpl peer;
 	String taskID;
 	
 	public Executor(PeerImpl peer){
 		this.peer = peer;
 	}
 	public void run(){
 		while(true){
 			//System.out.println("Before take task");
			Task t = peer.takeTask();
 			peer.taskExcecuted++;
 			//System.out.print("Executing task... ");
 			
 			t.setPeerImpl(peer);
 			
 			t.execute();
 			//System.out.println("Done");
 			
 			if (t.spawn_next != null){
 				String next_id = t.ID + "-0";
 				t.spawn_next.ID = next_id;
 				t.spawn_next.returnID = t.returnID;
 				t.spawn_next.returnArgumentNumber = t.returnArgumentNumber;
 				t.spawn_next.joinCounter = t.spawned.size();
 				t.spawn_next.args = new Object[t.spawned.size()];
 				t.spawn_next.creator = t.creator;//(Peer)peer;
 				
 				peer.putWaitMap(t.spawn_next);
 				peer.composeTasksCreated++;
 				int spawned_counter = 0;
 				for (Task temp : t.spawned){
 					temp.creator = peer.peerID;
 					temp.ID = t.ID + "-" + String.valueOf(spawned_counter+1);
 					temp.returnID = next_id;
 					temp.returnArgumentNumber = spawned_counter;
 					spawned_counter++;
 					peer.putReadyQ(temp);
 				}
 			}
 			if (t.send_argument != null){
 				while(!peer.placeArgument(t.creator , t.returnID, t.send_argument, t.returnArgumentNumber)){
 					try {
 						System.out.println("Nowhere to send argument!");
 						Thread.sleep(10);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			try {
 				if (peer.remoteQ != null){
 					if (peer.peerMap.containsKey(peer.remoteQueueHost)){
 						peer.remoteQ.removeTask(t.ID);
 					}
 				}
 			} catch (RemoteException e) {
 				System.out.println("unable to remove task from remote queue");
 				peer.hasDisconnected(peer.remoteQueueHost);
 				//e.printStackTrace();
 			}
 		}
 	}
 }
