 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.ByteArrayOutputStream;
 
 public class FileDecryptor
 {
    private RSAPrivateKey key;
    private File input;
    private ArrayList<Byte> data;
 
    public FileDecryptor(File input, RSAPrivateKey key) 
       throws MalformedPacketException, IOException, InvalidSelectionException
    {
       this.input = input;
       this.key = key;
       decryptFile();
    }
    
    public void decryptFile() throws MalformedPacketException, IOException
    {
       FileOutputStream out = new FileOutputStream("kermit");
       PacketReader reader = new PacketReader(input);
       List<OpenPGPPacket> packets = reader.readPackets();
       byte[] fr = Common.makeLongBytes(0);
       TripleDESEncryption des;
       SymmetricDataPacket sym;
       for(int i = 0; i < packets.size(); i += 4)
       {
          des = new TripleDESEncryption(Common.makeBytesLong(fr), 
                                        getNextKeys(packets, i));
          byte[] frEncrypted = Common.makeLongBytes(des.encrypt());
          sym = (SymmetricDataPacket) packets.get(i + 3).getPacket();
          byte[] cipher = sym.getEncryptedData();
          byte[] plain = new byte[cipher.length];
          for(int j = 0; j < plain.length; j++)
          {
             plain[j] = (byte) (frEncrypted[j] ^ cipher[j]);
          }
          out.write(plain);
          System.out.println("Plain is " + java.util.Arrays.toString(plain));
          System.out.println("frE is " + java.util.Arrays.toString(frEncrypted));
          System.out.println("cipher is " + java.util.Arrays.toString(cipher));
         fr = cipher;
       }
    }
 
    /*public void decryptFile() throws MalformedPacketException, IOException, 
    {
       ByteArrayOutputStream toBytes = new ByteArrayOutputStream();
       PacketReader reader = new PacketReader(input);
       List<OpenPGPPacket> packets = reader.readPackets();
       long[] sessionKeys = getNextKeys(packets, 0);
       TripleDESEncryption des = new TripleDESEncryption(0, sessionKeys[0],
                                     sessionKeys[1], sessionKeys[2]);
       byte[] frEncrypted = Common.makeLongBytes(des.encrypt());
       SymmetricDataPacket sym = (SymmetricDataPacket) packets.get(3).getPacket();
       byte[] encrypted = sym.getEncryptedData();
       byte[] fr = new byte[8];
       for(int i = 0, j = 3; i < fr.length; i++, j++)
       {
          fr[i] = encrypted[j];
       }
       byte[] random = new byte[8];
       for(int i = 0; i < encrypted.length; i++)
       {
          random[i] = (byte) (encrypted[i] ^ frEncrypted[i]);
       }
       System.out.println("Random data            " + java.util.Arrays.toString(random));
       sessionKeys = getNextKeys(packets, 4);
       des = new TripleDESEncryption(Common.makeBytesLong(encrypted), sessionKeys[0], sessionKeys[1], sessionKeys[2]);
       frEncrypted = Common.makeLongBytes(des.encrypt()); 
       sym = (SymmetricDataPacket) packets.get(7).getPacket();
       encrypted = sym.getEncryptedData();
       if(((byte) (frEncrypted[0] ^ encrypted[0])) != random[6] || 
          ((byte) (frEncrypted[1] ^ encrypted[1])) != random[7])
       {
          throw new MalformedPacketException("Checksum failed");
       }
       fr[6] = (byte) (frEncrypted[0] ^ encrypted[0]);
       fr[7] = (byte) (frEncrypted[1] ^ encrypted[1]);
 
       des = new TripleDESEncryption(Common.makeBytesLong(fr));
       frEncrypted = Common.makeLongBytes(des.encrypt());
       byte[] toWrite = new byte[OpenPGP.TRIPLEDES_BLOCK_BYTES];
       //Now we've checked the random data so get to the real stuff
       for(int i = 8; i < packets.size(); i += 4)
       {
          sym = (SymmetricDataPacket) packets.get(i + 3).getPacket();
          encrypted = sym.getEncryptedData();
          for(int j = 0; j < toWrite.length; j++)
          {
             toWrite[j] = (byte) (encrypted[j] ^ frEncrypted[j]);
             fr[j] = toWrite[j];
          }
          des = new TripleDESEncryption(Common.makeBytesLong(fr));
          frEncrypted = Common.makeLongBytes(des.encrypt());
          toBytes.write(toWrite);
       }
       byte[] result = toBytes.toByteArray();
       FileOutputStream out = new FileOutputStream("munkey");
       for(byte current : result)
       {
          out.write(current);
       }
 
    }*/
 
    public void write(File output) throws MalformedPacketException, IOException, 
       InvalidSelectionException
    {
    }
 
    private long[] getNextKeys(List<OpenPGPPacket> packets, int start)
    {
       long[] keys = new long[3];
       RSAEncryption rsa;
       for(int i = start, j = 0; i < packets.size() && j < keys.length; i++, j++)
       {
          EncryptedSessionKeyPacket sessionKey = (EncryptedSessionKeyPacket) 
                                                     packets.get(i).getPacket();
          
          rsa = new RSAEncryption(sessionKey.getEncryptedKey(), key);
          keys[j] = rsa.decrypt().longValue();
       }
       return keys;
    }
 }
