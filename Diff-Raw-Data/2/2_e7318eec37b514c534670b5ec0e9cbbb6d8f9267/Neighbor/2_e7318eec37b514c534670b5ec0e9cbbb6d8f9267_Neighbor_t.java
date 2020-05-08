 package edu.illinois.CS598rhk.models;
 
 import java.nio.ByteBuffer;
 
 import edu.illinois.CS598rhk.interfaces.IBluetoothMessage;
 import edu.illinois.CS598rhk.interfaces.IMessageReader;
 
 public class Neighbor implements IBluetoothMessage {
 	public String name;
     public String ipAddr;
     public String btAddr;
     
     public Neighbor(String name, String ipAddr, String btAddr) {
     	this.name = name;
     	this.ipAddr = ipAddr;
     	this.btAddr = btAddr;
     }
     
     public Neighbor(Neighbor neighbor) {
     	this.name = neighbor.name;
     	this.ipAddr = neighbor.ipAddr;
     	this.btAddr = neighbor.btAddr;
     }
     
     private Neighbor() {
     	// Do nothing
     }
     
     public static IMessageReader newNeighborReader() {
     	return new NeighborReader();
     }
     
     private static class NeighborReader implements IMessageReader {
 		@Override
 		public IBluetoothMessage parse(byte[] message) {
 			Neighbor wifiNeighbor = new Neighbor();
 			wifiNeighbor.unpack(message);
 			return wifiNeighbor;
 		}
     }
     
     public byte[] pack() {
             byte[] tempName = name.getBytes();
             byte[] tempIPAddr = ipAddr.getBytes();
             byte[] tempBTAddr = btAddr.getBytes();
             
             int msgLength = 4 + tempName.length + 4 + tempIPAddr.length + 4 + tempBTAddr.length;
             byte[] bytes = new byte[msgLength];
             int currentIndex = 0;
             
             byte[] strLengthBytes = ByteBuffer.allocate(4).putInt(tempName.length).array();
             System.arraycopy(strLengthBytes, 0, bytes, currentIndex, 4);
             currentIndex += 4;
             
             System.arraycopy(tempName, 0, bytes, currentIndex, tempName.length);
             currentIndex += tempName.length;
             
             strLengthBytes = ByteBuffer.allocate(4).putInt(tempIPAddr.length).array();
             System.arraycopy(strLengthBytes, 0, bytes, currentIndex, 4);
             currentIndex += 4;
             
             System.arraycopy(tempIPAddr, 0, bytes, currentIndex, tempIPAddr.length);
             currentIndex += tempIPAddr.length;
             
             strLengthBytes = ByteBuffer.allocate(4).putInt(tempBTAddr.length).array();
             System.arraycopy(strLengthBytes, 0, bytes, currentIndex, 4);
             currentIndex += 4;
             
             System.arraycopy(tempBTAddr, 0, bytes, currentIndex, tempBTAddr.length);
             
             return bytes;
     }
     
     public void unpack(byte[] bytes) {
             byte[] temp = new byte[4];
             
             int currentIndex = 0;
             System.arraycopy(bytes, currentIndex, temp, 0, 4);
             int strLength = ByteBuffer.wrap(temp).getInt();
             currentIndex += 4;
             
             name = new String(bytes, currentIndex, strLength);
             currentIndex += strLength;
             
             System.arraycopy(bytes, currentIndex, temp, 0, 4);
             strLength = ByteBuffer.wrap(temp).getInt();
             currentIndex += 4;
             
             ipAddr = new String(bytes, currentIndex, strLength);
             currentIndex += strLength;
             
            System.arraycopy(bytes, currentIndex, temp, 0, 4);
             strLength = ByteBuffer.wrap(temp).getInt();
             currentIndex += 4;
             
             btAddr = new String(bytes, currentIndex, strLength);
     }
 
 	@Override
 	public String toString() {
 		String prettyString = "Neighbor:" + "\n\tName: " + name + "\n\tIP: "
 				+ ipAddr + "\n\tMAC: " + btAddr;
 		return prettyString;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof Neighbor) {
 			Neighbor neighbor = (Neighbor) o;
 			return btAddr.equals(neighbor.btAddr)
 					&& ipAddr.equals(neighbor.ipAddr)
 					&& name.equals(neighbor.name);
 		}
 		return false;
 	}
 
     public byte getMessageType() {
             return BluetoothMessage.NEIGHBOR_HEADER;
     }
 }
