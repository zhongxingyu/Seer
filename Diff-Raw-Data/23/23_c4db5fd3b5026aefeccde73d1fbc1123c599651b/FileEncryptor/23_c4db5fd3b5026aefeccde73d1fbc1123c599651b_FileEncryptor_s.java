 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class FileEncryptor
 {
    private File input;
    private ArrayList<Byte> literalData;
    private ArrayList<OpenPGPPacket> encrypted;
    //private byte[] literalData;
    private RSABaseKey publicKey;
 
    public FileEncryptor(File input, RSABaseKey key) throws FileNotFoundException, IOException
    {
       this.input = input;
       this.publicKey=key;
       makeLiteralPacket();
    }
 
    public void encryptFile() throws InvalidSelectionException
    {
       encrypted = new ArrayList<OpenPGPPacket>();
       for(int i = 0; i < literalData.size(); i += OpenPGP.TRIPLEDES_BLOCK_BYTES) 
       {
 
          int subListEnd = i + 8 > literalData.size() ? literalData.size() : 8 + i;
          List<Byte> block = literalData.subList(i, subListEnd);
          byte[] primitiveBlock = Common.makeByteListPrimitive(block); 
          encrypted.addAll(encryptPacket(publicKey, primitiveBlock));
       }
    }
 
    public void write(File output) throws IOException, FileNotFoundException
    {
       FileOutputStream out = new FileOutputStream(output);
       for(OpenPGPPacket current : encrypted)
       {
          current.write(out);
       }
    }
 
 
    private void makeLiteralPacket() throws FileNotFoundException, IOException
    {
       /*FileInputStream readIn = new FileInputStream(input);
       byte[] fileName = input.getName().getBytes("UTF-8");
       literalData = new byte[readIn.available() + 1+1 + fileName.length];
       int i = 0;
       literalData[i++] = 0x62;
       literalData[i++] = fileName.length;
       for(int j = 0; j < fileName.length; i++, j++)
       {
          literalData[i] = fileName[j];
       }
       readIn.read(literalData, i, literalData.length);*/
       literalData = new ArrayList<Byte>();
       byte binary = 0x62;
       FileInputStream inputStream = new FileInputStream(input);
       long length = 1;
       while(inputStream.available() > 0)
       {
          literalData.add(Byte.valueOf((byte) inputStream.read()));
          length++;
       }
       byte[] bodyLength = OpenPGP.makeNewFormatLength(length);
       literalData.add(new Byte((byte) 0));
       literalData.add(new Byte((byte) 0));
       literalData.add(new Byte((byte) 0));
       literalData.add(new Byte((byte) 0));
       literalData.add(new Byte((byte) 0));
       literalData.add(0, new Byte(binary));
       for(byte temp : bodyLength)
       {
          literalData.add(0, temp);
       }
       literalData.add(0, new Byte(OpenPGP.LITERAL_DATA_PACKET_TAG));
    }
 
    public List<OpenPGPPacket> encryptPacket(RSABaseKey rsaKey, byte[] data) 
       throws InvalidSelectionException
    {
       ArrayList<OpenPGPPacket> result = new ArrayList<OpenPGPPacket>();
       long message = Common.makeBytesLong(data);
       TripleDESEncryption des = new TripleDESEncryption(message);
       long encrypted = des.encrypt();
       byte[] encryptedBytes = Common.makeLongBytes(encrypted);
       SymmetricDataPacket symData = new SymmetricDataPacket(encryptedBytes);
       OpenPGPPacket encryptedData = new OpenPGPPacket(OpenPGP.SYMMETRIC_DATA_TAG, symData);
 
       DESKey[] desKeys = des.getKeys();
       byte[] key1 = Common.makeLongBytes(desKeys[0].getKey());
       byte[] key2 = Common.makeLongBytes(desKeys[1].getKey());
       byte[] key3 = Common.makeLongBytes(desKeys[2].getKey());
       EncryptedSessionKeyPacket ek1 = new EncryptedSessionKeyPacket(rsaKey, key1);
       EncryptedSessionKeyPacket ek2 = new EncryptedSessionKeyPacket(rsaKey, key2);
       EncryptedSessionKeyPacket ek3 = new EncryptedSessionKeyPacket(rsaKey, key3);
       OpenPGPPacket keyPacket1 = new OpenPGPPacket(OpenPGP.PK_SESSION_KEY_TAG, ek1);
       OpenPGPPacket keyPacket2 = new OpenPGPPacket(OpenPGP.PK_SESSION_KEY_TAG, ek2);
       OpenPGPPacket keyPacket3 = new OpenPGPPacket(OpenPGP.PK_SESSION_KEY_TAG, ek3);
       result.add(keyPacket1);
       result.add(keyPacket2);
       result.add(keyPacket3);
       result.add(encryptedData);
       return result; 
    }
 }
