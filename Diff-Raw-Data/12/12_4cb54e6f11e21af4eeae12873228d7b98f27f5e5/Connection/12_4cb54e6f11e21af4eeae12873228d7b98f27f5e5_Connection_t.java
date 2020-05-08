 package comm;
 
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 
 public class Connection {
 	public final int port;
 	public final InetAddress address;
 	
 	public Connection(ByteBuffer b) throws UnknownHostException {
 		byte[] temp = new byte[4];
 		b.get(temp);
 		address = InetAddress.getByAddress(temp);
		port = b.getShort() & 0xffff;
 	}
 	
 	public Connection(InetAddress a, int p) {
 		address = a;
 		port = p;
 	}
 	
 	public Connection(String s) throws UnknownHostException {
 		String[] temp = s.split(":");
 		if (temp.length < 2) {
 			throw new UnknownHostException();
 		} else {
 			address = InetAddress.getByName(temp[0]);
 			port = Integer.parseInt(temp[1]);
 		}
 	}
 	
 	public Connection(String s,int defaultPort) throws UnknownHostException {
 		String[] temp = s.split(":");
 		if (temp.length < 1) {
 			throw new UnknownHostException();
 		} else if (temp.length < 2) {
 			address = InetAddress.getByName(temp[0]);
 			port = defaultPort;
 		} else {
 			address = InetAddress.getByName(temp[0]);
 			port = Integer.parseInt(temp[1]);
 		}
 	}
 	
 	
 	public Connection(DatagramPacket dp) {
 		address = dp.getAddress();
 		port = dp.getPort();
 	}
 	
 	public DatagramPacket makePacket(ByteBuffer b) {
 		return new DatagramPacket(b.array(),b.limit(),address,port);
 	}
 	
 	public ByteBuffer toBytes(ByteBuffer b) {
 		b.put(address.getAddress());
 		b.putShort((short)port);
 		return b;
 	}
 	
 	public String toString() {
		return address.getHostAddress() + ':' + port;
 	}
 	
 	public int hashCode() {
 		return address.hashCode();
 	}
 	
 	public boolean equals(Object o) {
 		if (!(o instanceof Connection))
 			return false;
 		Connection c = (Connection)o;
 		return address.equals(c.address) && port == c.port; 
 	}
 }
