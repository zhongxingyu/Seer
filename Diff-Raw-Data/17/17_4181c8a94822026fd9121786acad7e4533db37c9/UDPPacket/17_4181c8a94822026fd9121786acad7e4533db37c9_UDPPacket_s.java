 package netproj.hosts;
 
 import netproj.skeleton.Packet;
 
 /**
  * This class adds a checksum to packet; the checksum is the 16-bit
  * one's complement of the one's complement sum of a pseudo header of information
  * from the header, which in this case is source + dest.
  * @author Robert
  *
  */
 public class UDPPacket extends Packet{
 	private short checksum = 0;
 	
 	public UDPPacket(int source, int dest, int size, int ttl) {
 		super(source, dest, size, ttl);
 		checksum = 0;
 	}
 	
 	/**
 	 * Returns the checksum of the packet.
 	 */
 	public int getCheckSum() {
 		return checksum;
 	}
 	
 	/**
 	 * Calculates the one's complement of the sum of all 16-bit words in the header
 	 * and returns that 16-bit value.
 	 */
 	public short calcCheckSum() {
 		short sum = 0;
 		// This would give us the left 16 bits of source.
 		short source1 = (short) (this.getSourceAddress() / (2^16));
 		// Last 16 bits of source.
 		short source2 = (short) (this.getSourceAddress() 
 				- (this.getSourceAddress() / (2^16)));
 		// First 16 bits of dest.
 		short dest1 = (short) (this.getDestAddress() / (2^16));
 		// Last 16 bits of dest.
 		short dest2 = (short) (this.getDestAddress() 
 				- (this.getDestAddress() / (2^16)));
 		
 		/**
 		 * Add the four words to get the two's complement sum.  Adding the four
 		 * words in two's complement addition, then converting to one's complement
 		 * arithmetic should be the same as converting to one's complement and then
 		 * adding them in one's complement arithmetic.
 		 */
 		sum = (short) (source1 + source2 + dest1 + dest2);
 		/**
 		 * Java runs in two's complement, and in two's complement -x inverts
 		 * the bits of x and adds 1.  To get one's complement, which is just
 		 * an inversion of the bits, we'd take -x and subtract 1 if x is positive. 
 		 * So (short) (-sum - 1) is the 16-bit one's complement of the sum of all 
 		 * 16-bit words in the header.
 		 */
 		if (sum >= 0) {
 			sum = (short) (- sum);
 			sum = (short) (sum - 1);
 		/**
 		 * If x is already negative, then -x would be positive, and positive 
 		 * numbers are the same in one's and two's complement, so we just need
 		 * to take -sum to get the right value for the checksum.
 		 */
 		} else {
 			sum = (short) (-sum);
 		}
 		
 		return sum;
 	}
 	
 	/**
 	 * Calculates the checksum and sets the variable checksum to that value.
 	 */
 	public void setCheckSum() {
 		checksum = calcCheckSum();
 	}
 }
