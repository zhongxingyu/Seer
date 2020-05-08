 package turboprt;
 
 import bluetooth.BluetoothService;
 import bluetooth.StreamListener;
 import java.util.ArrayList;
 
 /**
  *
  * @author marcel
  */
 public class Podcar {
 
     private int id;
     private String name;
     private Location location;
     private ArrayList<Passenger> passengers = new ArrayList<Passenger>();
     private ArrayList<Destination> destinations = new ArrayList<Destination>();
     private Status status = Podcar.Status.DISCONNECTED;
     private String macAddress;
     private boolean IR = false;
     public BluetoothService btService;
     private boolean connected = false;
 
     public enum Status {DRIVING, WAITING, CHARGING, BLOCKED, DISCONNECTED};
     private ArrayList<PodcarListener> listeners = new ArrayList<PodcarListener>();
 
     Podcar() {
         this.id = -1;
         this.location = new Location();
     }
 
     public boolean connect() {
         try {
             System.out.println("Connecting to " + this.macAddress + "...");
             btService = new BluetoothService();
             if (btService.connectToDevice(this.macAddress, 1)) {
                 System.out.println("Connected to " + this.macAddress);
                 this.connected = true;
                 this.status = Status.WAITING;
 
                 new StreamListener(this);
 
                 broadcastChange();
                 return true;
             } else {
                 System.out.println("Failed to connect to " + this.macAddress);
                 return false;
             }
         } catch (Exception e) {
             System.out.println("Couldn't connect to " + this.macAddress);
             return false;
         }
     }
 
     public void disconnect() {
         this.btService.disconnect();
         this.connected = false;
 
         setStatus(Status.DISCONNECTED);
 
         System.out.println(this.getName() + " got disconnected.");
     }
 
     public void sendCommand(String command) {
         if (this.connected == true) {
             System.out.println("Sending command to " + this.macAddress + ": " + command);
             btService.sendCommand(command);
         } else {
             System.out.println("Device is not connected!");
         }
     }
 
     public void processCommand(String command) {
        System.out.println("Received command from "+this.getName()+": " + command);
		
         if (command.equals("ATDRV") && this.getStatus() != Podcar.Status.BLOCKED) {
             if (this.getStatus() == Podcar.Status.WAITING) {
                 this.setStatus(Podcar.Status.DRIVING);
             } else {
                 this.setStatus(Podcar.Status.WAITING);
             }
         } else if (command.equals("E666")) {
             this.setStatus(Podcar.Status.BLOCKED);
         } else if (command.equals("!E666")) {
             this.setStatus(Podcar.Status.WAITING);
         }
     }
 
     public void turn(int direction) {
         // turn
     }
 	
 	public void stop()
 	{
 		this.sendCommand("#S");
 	}
 
     public void blinkIR() {
 		
     }
 	
     void setIR(boolean b) {
         this.IR = b;
     }
     
     boolean getIR() {
         return this.IR;
     }
 
     public void addListener(PodcarListener listener) {
         this.listeners.add(listener);
     }
 
     public void removeListener(PodcarListener listener) {
         this.listeners.remove(listener);
     }
 
     private void broadcastChange() {
         for (PodcarListener listener : this.listeners) {
             listener.update(this);
         }
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
         broadcastChange();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
         broadcastChange();
     }
 
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
         broadcastChange();
 		
 		try
 		{
 			Location destLoc = getCurrentDestination().getLocation();
 
 			int margin = 10;
 
 			if((location.getLatitude() > destLoc.getLatitude() - margin &&
 					location.getLatitude() < destLoc.getLatitude() + margin) &&
 					(location.getLongitude() > destLoc.getLongitude() - margin &&
 					location.getLongitude() < destLoc.getLongitude() + margin))
 			{
 				System.out.println(this.getName()+" reached it's destination!");
 				this.stop();
 			}
 		}
 		catch(Exception e)
 		{
 			//e.printStackTrace();
 		}
     }
 
     public ArrayList<Passenger> getPassengers() {
         return passengers;
     }
 
     public void setPassengers(ArrayList<Passenger> passengers) {
         this.passengers = passengers;
         broadcastChange();
     }
 
     public ArrayList<Destination> getDestinations() {
         return destinations;
     }
 
 	public Destination getCurrentDestination() throws Exception
 	{
 		if(this.destinations.isEmpty()) throw new Exception("No destinations");
 		return this.destinations.get(0);
 	}
 	
     public void setDestinations(ArrayList<Destination> destinations) {
         this.destinations = destinations;
         broadcastChange();
     }
 
     public void addDestination(Destination dest) {
         this.destinations.add(dest);
         broadcastChange();
     }
 
     public void removeDestination(Destination dest) {
         this.destinations.remove(dest);
         broadcastChange();
     }
 
     public boolean isDriving() {
         return (status == Status.DRIVING);
     }
 
     public boolean isAvailable() {
         return (status != Status.DRIVING && status != Status.BLOCKED);
     }
 
     public Status getStatus() {
         return this.status;
     }
 
     public String getStatusString() {
         if (status == Status.CHARGING) {
             return "Charging";
         } else if (status == Status.DRIVING) {
             return "Driving";
         } else if (status == Status.BLOCKED) {
             return "Blocked";
         } else if (status == Status.DISCONNECTED) {
             return "Disconnected";
         } else {
             return "Waiting";
         }
     }
 
     public void setStatus(Status status) {
         this.status = status;
         broadcastChange();
     }
 
     public boolean isConnected() {
         return connected;
     }
 
     public String getMacAddress() {
         return macAddress;
     }
 
     public void setMacAddress(String macAddress) {
         this.macAddress = macAddress;
         broadcastChange();
     }
 }
