 package hhu.propra_2013.gruppe_13;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.concurrent.locks.Lock;
 
 class NetClientIn extends NetIO {
 
 	private ObjectInputStream			receiveObjects;
 	private boolean 					running;
 	
 	private ArrayList<CoreGameObjects> 	allObjects;
 
 	private Lock 						lock;
 	private int							clientNo;
 	
 	/*------------------------------------------------------------------------------------------------------------------------*/
 	NetClientIn (ObjectInputStream inputStream, Lock lock, int clientNo) {
 		running 	= true;
 		
 		allObjects 	= new ArrayList<CoreGameObjects>();
 		
 		receiveObjects = inputStream;
 		this.lock 	= lock;
 		this.clientNo = clientNo;
 	}
 	
 	/*------------------------------------------------------------------------------------------------------------------------*/
 	@Override
 	void setRunning(boolean running) {
 		this.running = running;
 	}
 	
 	void getList(ArrayList<CoreGameObjects> currentRoom, Figure figure) {
 		// Check whether there is anything to work with in the first place
 		if (currentRoom == null)
 			return;
 		
 		boolean inList = false;
 		
		// lock the list, so we don't loose information
		lock.lock();
		
 		// iterate over all objects within the incoming array
 		for (int i=0; i<allObjects.size(); i++) {
 			
 			// check for all objects whether they are already within the gameobject array
 			roomLoop:
 			for (int j=0; j<currentRoom.size(); j++) {
 				if (allObjects.get(i).getID() == currentRoom.get(j).getID()) {
 					inList = true;
 					
 					// first we check whether this is a figure we are working with
 					if (allObjects.get(i) instanceof Figure) {
 						currentRoom.set(j, allObjects.get(i));
 						if (((Figure)allObjects.get(i)).getPlayer() == clientNo) 
 							figure = (Figure)allObjects.get(i);
 					}
 					
 					else if (allObjects.get(i) instanceof Item) {
 						if (allObjects.get(i).getHP() == 0)
 							currentRoom.remove(j);
 						else
 							currentRoom.set(j, allObjects.get(i));
 					}
 					
 					else if (allObjects.get(i) instanceof Enemy) {
 						if (((Enemy)allObjects.get(i)).stopDrawing())
 							currentRoom.remove(j);
 						else
 							currentRoom.set(j, allObjects.get(i));
 					}
 					
 					else if (allObjects.get(i) instanceof MiscWall) {
 						if (allObjects.get(i).getHP() == 0)
 							currentRoom.remove(j);
 						else
 							currentRoom.set(j, allObjects.get(i));
 					}
 					
 					else if (allObjects.get(i) instanceof Attack) {
 						if (((Attack)allObjects.get(i)).getFinished())
 							currentRoom.remove(j);
 						else
 							currentRoom.set(j, allObjects.get(i));
 					}
 					
 					else if (allObjects.get(i).getHP() == 0)
 						currentRoom.remove(j);
 					
 					else
 						currentRoom.add(allObjects.get(i));
 					
 					break roomLoop;
 				}
 			}
 		
 			if (!inList) {
 				currentRoom.add(allObjects.get(i));
 			}
 		}
 		
 		allObjects.clear();
		
		// unlock the list, we are finished with it
		lock.unlock();
 	}
 	
 	/*------------------------------------------------------------------------------------------------------------------------*/
 	@Override
 	public void run() {
 		Object incoming;
 		boolean alreadyThere;
 		
 		// Game loop
 		while (running) {
 			// reset boolean value
 			alreadyThere = false;
 			
 			// read objects from the stream
 			try {
 				incoming = receiveObjects.readObject();
 //				System.out.println("Incoming object: "+incoming);
 				
 				// if they are of the desired type, add them to the current arraylist
 				if (incoming instanceof CoreGameObjects){
 					lock.lock();
 					
 					loop:
 					for (int i=0; i<allObjects.size(); i++) {
 						if (((CoreGameObjects)incoming).getID() == allObjects.get(i).getID()) {
 							allObjects.set(i, (CoreGameObjects)incoming);
 							alreadyThere = true;
 							break loop;
 						}
 					}
 					
 					if (!alreadyThere) {						
 						allObjects.add((CoreGameObjects)incoming);
 					}
 					lock.unlock();
 				}
 				
 			} catch (ClassNotFoundException | IOException e) {
 				System.err.println("Error in NetClientIn");
 				e.printStackTrace();
 			}
 		}
 	}
 }
