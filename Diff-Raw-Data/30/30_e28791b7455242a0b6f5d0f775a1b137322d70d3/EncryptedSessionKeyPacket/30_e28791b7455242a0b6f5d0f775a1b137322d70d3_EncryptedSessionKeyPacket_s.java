 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 public class EncryptedSessionKeyPacket implements PacketSpecificInterface
 {
    private static final byte VERSION = 3;
    private BigInteger encryptedKey;
    private byte[] keyID;
    private static final int KEY_ID_SIZE = 8;
    
    public EncryptedSessionKeyPacket(RSABaseKey rsaKey, String sessionKey)
    {
       RSAEncryption rsa = new RSAEncryption(sessionKey, rsaKey);
       encryptedKey = rsa.encrypt();
       keyID = rsaKey.getKeyID();
    }
    
    public EncryptedSessionKeyPacket(byte[] data)
    {
       int i = 0;
       if(data[i++] != VERSION)
       {
          System.err.println("Version " + data[0] + " is not supported");
       }
       for(int j = 0; j < KEY_ID_SIZE; i++, j++)
       {
          keyID[j] = data[i];
       }
       if(data[i++] != OpenPGP.RSA_CONSTANT)
       {
          System.err.println("RSA is the only public key algorithm that is supported");
       }
       int length = (data[i++] << 8) | data[i++];
       byte[] key = new byte[length / Byte.SIZE];
       for(int j = 0; j < key.length && i < data.length; i++, j++)
       {
          key[j] = data[i];
       }
       encryptedKey = new BigInteger(key);
    }
 
    public byte[] getKeyID()
    {
       return keyID;
    }
 
    public BigInteger getEncryptedKey()
    {
       return encryptedKey;
    }
 
    public void write(FileOutputStream output) throws IOException
    {
       output.write(new byte[]{VERSION});
       output.write(keyID);
       output.write(OpenPGP.makeMultiprecisionInteger(encryptedKey));
    }
 
    public int getBodyLength()
    {
       //2 is for MPI
       return 1 + keyID.length + encryptedKey.toByteArray().length + 2;
    }
 }
