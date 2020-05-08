 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.util.List;
 
 public class FileDecryptor
 {
    private RSAPrivateKey key;
   private File input;
 
    public FileDecryptor(File input, RSAPrivateKey key)
    {
       this.input = input;
       this.key = key;
    }
 
   public void decrypt() throws MalformedPacketException, IOException
    {
      PacketReader reader = new PacketReader(input);
      List<OpenPGPPacket> encrypted = reader.readPackets();
      for(OpenPGPPacket current : encrypted)
      {
         System.out.println("current " + current);
      }
    }
 
    public void write(File output)
    {
    }
 }
