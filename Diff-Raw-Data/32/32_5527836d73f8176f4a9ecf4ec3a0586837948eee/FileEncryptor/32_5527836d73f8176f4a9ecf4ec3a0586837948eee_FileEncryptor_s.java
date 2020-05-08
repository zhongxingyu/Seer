 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.List;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ByteArrayOutputStream;
 import java.io.ByteArrayInputStream;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.InflaterOutputStream;
 
 /**
  * This class will encrypt a file and allows the user to write the encrypted
  * data to multiple files
  * @author Jonathan Nanney
  * */
 public class FileEncryptor
 {
    /** The file to encrypt */
    private File input;
    /** The encrypted packets */
    private ArrayList<OpenPGPPacket> encrypted;
    /** The public key to use to encrypt */
    private RSABaseKey publicKey;
    /** The data to encrypt */
    private byte[] toEncrypt;
    
    /**
     * Constructor that takes a file to encrypt and a key to encrypt it with
     * @param input - the file to encrypt
     * @param key - the key to encrypt the file with
     * */
    public FileEncryptor(File input, RSABaseKey key) 
       throws FileNotFoundException, IOException, InvalidSelectionException
    {
       this.input = input;
       this.publicKey=key;
      makeLiteralPacket(new FileInputStream(input));
      compress();
       encryptFile();
    }
    
    /**
     * This method will compress the data and puts the result in the toEncrypt 
     * array.  Algorithm used is DEFLATE
     * */
    private void compress() throws IOException
    {
       //TODO: clean this code up
       ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
       DeflaterOutputStream deflater = new DeflaterOutputStream(arrayOut);
       //DeflaterOutputStream deflater = new DeflaterOutputStream(new FileOutputStream("weez"));
       deflater.write(toEncrypt, 0, toEncrypt.length);
       deflater.finish();
       byte[] compressed = arrayOut.toByteArray();
 
       CompressedDataPacket comp = new CompressedDataPacket(compressed, false);
       OpenPGPPacket tempPacket = new OpenPGPPacket(OpenPGP.COMPRESSED_DATA_TAG,
          comp);
       ByteArrayOutputStream toEncryptArrayOut = new ByteArrayOutputStream();
       tempPacket.write(toEncryptArrayOut);
       toEncrypt = toEncryptArrayOut.toByteArray();
    }
    
    /**
     * This method writes the encrypted file to a file.
     * @param output - the file to write to
     * */
    public void write(File output) throws IOException, FileNotFoundException
    {
       FileOutputStream out = new FileOutputStream(output);
       for(OpenPGPPacket current : encrypted)
       {
          current.write(out);
       }
    }
    
    /**
     * This turns the original file into a literal data packet and puts the 
     * result into encrypt.  
     * @param in - the input stream to read the original file from
     * */
    private void makeLiteralPacket(InputStream in) throws IOException
    {
       byte[] data = Common.readAllData(in);
       //b in ascii means that the data is treated as binary data.  Another
       //option could be 0x72 or t in ascii which would treat the data as text.
       //That isn't currently implemented.
       byte FORMAT = 0x62;
       LiteralDataPacket literal = new LiteralDataPacket(FORMAT, input.getName(), 
                                                         data);
       OpenPGPPacket literalPacket = new OpenPGPPacket(
                                     OpenPGP.LITERAL_DATA_PACKET_TAG, literal);
       ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
       literalPacket.write(arrayOut);
       toEncrypt = arrayOut.toByteArray();
    }
    
    /**
     * This method encrypts a file and puts the results in the encrypted packet
     * list.
     * */
    private void encryptFile() throws InvalidSelectionException
    {
       //TODO: clean this code up a lot.  Probably move the code that encrypts
       //random data into its method.
       encrypted = new ArrayList<OpenPGPPacket>();
       byte[] fr = Common.makeLongBytes(0);
       long frLong = Common.makeBytesLong(fr);
       TripleDESEncryption des = new TripleDESEncryption(frLong);
       byte[] frEncrypted = Common.makeLongBytes(des.encrypt());
       byte[] randomData = new byte[OpenPGP.TRIPLEDES_BLOCK_BYTES];
       Random rand = new Random();
       rand.nextBytes(randomData);
       byte[] cipher = new byte[OpenPGP.TRIPLEDES_BLOCK_BYTES];
       for(int i = 0; i < OpenPGP.TRIPLEDES_BLOCK_BYTES; i++)
       {
          cipher[i] = (byte) (frEncrypted[i] ^ randomData[i]);
          fr[i] = cipher[i];
       }
       encrypted.addAll(createPackets(des, cipher));
       des = new TripleDESEncryption(Common.makeBytesLong(fr));
       frEncrypted = Common.makeLongBytes(des.encrypt());
       //We XOR the left two octets of frEncrypted with the last two octets of
       //random data.
       byte[] tempCipher = new byte[2];
       tempCipher[0] = (byte) (frEncrypted[0] ^ randomData[6]);
       tempCipher[1] = (byte) (frEncrypted[1] ^ randomData[7]);
       encrypted.addAll(createPackets(des, tempCipher));
 
       for(int i = 2; i < cipher.length; i++)
       {
          fr[i - 2] = cipher[i];
       }
       fr[6] = tempCipher[0];
       fr[7] = tempCipher[1];
       des = new TripleDESEncryption(Common.makeBytesLong(fr));
       frEncrypted = Common.makeLongBytes(des.encrypt());
       
       //We've encrypted 10 bytes of random data so now do the actual data
       cipher = new byte[OpenPGP.TRIPLEDES_BLOCK_BYTES];
       for(int i = 0; i < toEncrypt.length; i += OpenPGP.TRIPLEDES_BLOCK_BYTES)
       {
          if(i + OpenPGP.TRIPLEDES_BLOCK_BYTES >= toEncrypt.length)
          {
             cipher = new byte[toEncrypt.length - i];
          }
          for(int j = i, k = 0; j < i + cipher.length; j++, k++)
          {
             cipher[k] = (byte) (frEncrypted[k] ^ toEncrypt[j]);
             fr[k] = cipher[k];
          }
          encrypted.addAll(createPackets(des, cipher));
          des = new TripleDESEncryption(Common.makeBytesLong(cipher));
          frEncrypted = Common.makeLongBytes(des.encrypt());
       }
    }
    
    /**
     * This method creates the next 4 encrypted packets.  The first three 
     * packets hold the 3 public key encrypted session keys and the last packet
     * holds the encrypted data
     * @param des - holds the 3DES keys to encrypt and put in 
     *              EncryptedSessionKeyPacket
     * @param cipher - the data that was encrypted with 3DES to put into a 
     *                 SymmetricDataPacket
     * @return A list of the packets that were created
     * */
    private List<OpenPGPPacket> createPackets(TripleDESEncryption des, byte[] cipher)
    {
       ArrayList<OpenPGPPacket> result = new ArrayList<OpenPGPPacket>();
 
       EncryptedSessionKeyPacket key1 = new EncryptedSessionKeyPacket(publicKey,
                                            des.getKey1());
       EncryptedSessionKeyPacket key2 = new EncryptedSessionKeyPacket(publicKey,
                                            des.getKey2());
       EncryptedSessionKeyPacket key3 = new EncryptedSessionKeyPacket(publicKey,
                                            des.getKey3());
       SymmetricDataPacket symData = new SymmetricDataPacket(cipher, false);
       result.add(new OpenPGPPacket(OpenPGP.PK_SESSION_KEY_TAG, key1));
       result.add(new OpenPGPPacket(OpenPGP.PK_SESSION_KEY_TAG, key2));
       result.add(new OpenPGPPacket(OpenPGP.PK_SESSION_KEY_TAG, key3));
       result.add(new OpenPGPPacket(OpenPGP.SYMMETRIC_DATA_TAG, symData));
       return result;
    }
 }
