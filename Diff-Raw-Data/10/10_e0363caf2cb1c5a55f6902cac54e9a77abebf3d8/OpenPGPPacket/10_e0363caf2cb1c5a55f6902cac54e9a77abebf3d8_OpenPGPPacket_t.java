 import java.util.List;
 import java.io.FileOutputStream;
 import java.io.IOException;
 public class OpenPGPPacket
 {
    private byte tag;
    private PacketSpecificInterface packetInfo;
    
    public OpenPGPPacket(byte tag, PacketSpecificInterface packetInfo)
    {
       this.tag = tag;
       this.packetInfo = packetInfo;
    }
 
    public OpenPGPPacket(byte tag, byte[] data) 
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
          default:
             System.err.println("Tag " + tag + " is not a supported tag");
       }
    }
 
    public byte getTag()
    {
       return tag;
    }
 
    public PacketSpecificInterface getPacket()
    {
       return packetInfo;
    }
 
    public String toString() 
    {
       //XOR removes the 2 1-bits that are required to be at start of tag
       return "Tag : " + (tag ^ OpenPGP.NEW_TAG_MASK);
    }
 
    public void write(FileOutputStream output) throws IOException
    {
       int length = packetInfo.getBodyLength();
       byte[] lengthArray = OpenPGP.makeNewFormatLength(length);
       output.write(new byte[]{tag});
       output.write(lengthArray);
       packetInfo.write(output);
    }
 }
 
