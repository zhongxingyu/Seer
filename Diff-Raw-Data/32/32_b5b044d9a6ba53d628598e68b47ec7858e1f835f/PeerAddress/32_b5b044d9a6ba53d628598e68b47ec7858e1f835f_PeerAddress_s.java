 package kimononet.peer;
 
 import java.util.Arrays;
 
 import kimononet.net.parcel.Parcel;
 import kimononet.net.parcel.Parcelable;
 
 /**
  * The object represents a unique peer address. 
  * 
  * @author Zorayr Khalapyan
  *
  */
 public class PeerAddress implements Parcelable{
 
 	/**
 	 * Peer address length in bytes.
 	 */
 	public static final int ADDRESS_LENGTH = 6;
 	
 	/**
 	 * Current implementation adds a two byte padding to the MAC-48 address in 
 	 * case address space has to increase in the future.
 	 */
 	public static final int ADDRESS_PADDING_LENGTH = 2;
 	
 	/**
 	 * Peer address's parcel size.
 	 */
 	public static final int PARCEL_SIZE = ADDRESS_LENGTH + ADDRESS_PADDING_LENGTH;
 
 	/**
 	 * A MAC-48 address according to IEEE 802 standard.
 	 */
 	private byte[] address = new byte[ADDRESS_LENGTH];
 	
 	/**
 	 * Creates a peer address from the relative {@link #PARCEL_SIZE} bytes of 
 	 * the provided parcel.
 	 * 
 	 * @param parcel
 	 */
 	public PeerAddress(Parcel parcel){
 		
 		parcel.getByteArray(address);
 		
 		//Discard the address padding.
 		parcel.getByteArray(new byte[ADDRESS_PADDING_LENGTH]);
 		
 	}
 	
 	/**
 	 * Creates a peer address from a byte array representation of a MAC-48 
 	 * address.
 	 * 
 	 * @param address Peer address to set.
 	 */
 	public PeerAddress(byte[] address){
 		
 		if(address.length != ADDRESS_LENGTH){
 			throw new PeerAddressException("Invalid address length.");
 		}
 		
 		this.address = address;
 	}
 	
 	/**
 	 * Creates a new peer address from a specified string.
 	 * 
 	 * @param address 
 	 */
 	public PeerAddress(String address){
 		setAddress(address);
 	}
 	
 	public void setAddress(String address){
 		
 		String[] hex = address.split("(\\:|\\-)");
 		
 		if (hex.length != this.address.length) {
 			throw new PeerAddressException("Invalid address legth: " + address);
 		}
 		
 		try {
 			
 			for (int i = 0; i < this.address.length; i++) {				
 				this.address[i] = (byte) Integer.parseInt(hex[i], 16);
 			}
 		}
 		catch (NumberFormatException e) {
 			throw new PeerAddressException("Invalid hex digit in address: " + address, e);
 		}
 	}
 	
 	/**
 	 * Returns the Parcel representation of the address.
 	 * @return Parcel representation of the address.
 	 */
 	@Override
 	public Parcel toParcel(){
 		
 		//Pad and return the address parcel.
 		Parcel parcel = new Parcel(ADDRESS_LENGTH + ADDRESS_PADDING_LENGTH);
 		parcel.add(address);
 		
 		return parcel;
 		 
 	}
 	
 	/**
 	 * Returns the address's parcel's size.
 	 * @return The address's parcel's size.
 	 */
 	@Override
 	public int getParcelSize(){
 		return PARCEL_SIZE;
 	}
 	
 	/**
 	 * Represents a string representation of the address.
 	 */
 	@Override
 	public String toString(){
 		
 		StringBuilder addressBuilder = new StringBuilder();
 				
 		for (int i = 0; i < address.length; i++) {
 			addressBuilder.append(String.format("%02X%s", address[i], (i < address.length - 1) ? ":" : ""));
 		}
 		
 		return addressBuilder.toString();
 	}
 	
 	@Override
 	public boolean equals(Object obj){
 		
 		if(obj instanceof PeerAddress){
 			return Arrays.equals(address, ((PeerAddress)(obj)).address);  
 		}else{
 			return false;
 		}
 		
 	}
 	
 	@Override
 	public int hashCode(){
		return this.toParcel().getInt();
 	}
 }
