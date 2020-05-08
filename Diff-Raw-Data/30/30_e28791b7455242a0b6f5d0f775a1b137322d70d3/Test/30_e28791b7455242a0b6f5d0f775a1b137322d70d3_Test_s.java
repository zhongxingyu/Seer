 import java.math.BigInteger;
 import java.io.*;
 public class Test
 {
    public static void main(String[] args) throws Exception
    {
       RSAPrivateKey key = new RSAPrivateKey(1024);
       RSABaseKey publicKey = key.getPublicKey();
       OpenPGPPacket publicPacket = new OpenPGPPacket(OpenPGP.PUBLIC_KEY_PACKET_TAG, publicKey);
       OpenPGPPacket privatePacket = new OpenPGPPacket(OpenPGP.PRIVATE_KEY_PACKET_TAG, key);
       publicPacket.write(new FileOutputStream("key.pub"));
       privatePacket.write(new FileOutputStream("key"));
       FileEncryptor encryptor = new FileEncryptor(new File("test.txt"), key);
       encryptor.encryptFile();
       encryptor.write(new File("output"));
      //FileOutputStream out = new FileOutputStream(new File("output"));
      //out.write(new byte[]{64,65,66,67});
    }
 }
