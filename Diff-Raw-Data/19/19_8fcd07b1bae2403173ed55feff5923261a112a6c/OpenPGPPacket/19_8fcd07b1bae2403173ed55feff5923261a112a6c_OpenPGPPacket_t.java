 import java.util.List;
 import java.io.OutputStream;
 import java.io.IOException;
 
 /**
  * This class represents all OpenPGP packets.
  * */
 public class OpenPGPPacket
 {
    /** The tag that identifies this packet */
    private byte tag;
    /** Contains the information specific to this packet */
    private PacketSpecificInterface packetInfo;
    
    /**
     * Constructor that takes a tag and a object with the packet specific 
     * information.
     * @param tag - the tag that identifies the packet
     * @param packetInfo - 
     * */
    public OpenPGPPacket(byte tag, PacketSpecificInterface packetInfo)
    {
       this.tag = tag;
       this.packetInfo = packetInfo;
    }
    
    /**
     * Constructor that takes a tag and the data in the packet and finds the 
     * correct packet specific information.
     * @param tag - the tag that identifies the packet
     * @param data - the data in the packet after the header.
     * */
   public OpenPGPPacket(byte tag, byte[] data) throws MalformedPacketException
    {
       this.tag = tag;
       switch(tag)
       {
          case OpenPGP.LITERAL_DATA_PACKET_TAG:
             packetInfo = new LiteralDataPacket(data);
             break;
          case OpenPGP.PK_SESSION_KEY_TAG:
             packetInfo = new EncryptedSessionKeyPacket(data);
             break;
          case OpenPGP.SYMMETRIC_DATA_TAG:
             packetInfo = new SymmetricDataPacket(data);
             break;
          case OpenPGP.PUBLIC_KEY_PACKET_TAG:
             packetInfo = new RSABaseKey(data);
             break;
          case OpenPGP.PRIVATE_KEY_PACKET_TAG:
             packetInfo = new RSAPrivateKey(data);
             break;
          case OpenPGP.COMPRESSED_DATA_TAG:
             packetInfo = new CompressedDataPacket(data);
             break;
          default:
            throw new MalformedPacketException("Tag " + tag + " is invalid");
       }
    }
    
    /**
     * Returns this packet's tag.
     * @return the tag that identifies the packet
     * */
    public byte getTag()
    {
       return tag;
    }
    
    /**
     * Gets the object holding the packet specific information.
     * @return the object holding the packet specific information.
     * */
    public PacketSpecificInterface getPacket()
    {
       return packetInfo;
    }
    
    public String toString() 
    {
       //XOR removes the 2 1-bits that are required to be at start of tag
       return "Tag : " + (tag ^ OpenPGP.NEW_TAG_MASK);
    }
    
    /**
     * Writes out this packet to an output stream.
     * @param output - the output stream to write to
     * */
    public void write(OutputStream output) throws IOException
    {
       int length = packetInfo.getBodyLength();
       byte[] lengthArray = OpenPGP.makeNewFormatLength(length);
       output.write(new byte[]{tag});
       output.write(lengthArray);
       //Have the packetInfo write its specific stuff to this stream
       packetInfo.write(output);
    }
 }
 
