 /*
  * @(#)Skeleton.java
  */
 
 import de.tubs.cs.iti.jcrypt.protokoll.*;
 import de.tubs.cs.iti.jcrypt.chiffre.*;
 
 import java.math.*;
 import java.util.*;
 import java.io.*;
 import java.security.MessageDigest;
 
 import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
 
 /**
  *
  */
 
 public final class StationToStation implements Protocol
 {
     /**
      *
      */
 
     static private int MinPlayer        = 2; // Minimal number of players
     static private int MaxPlayer        = 2; // Maximal number of players
     static private String NameOfTheGame = "ABBA";
     private Communicator com;
 
     public static final BigInteger ZERO = BigInteger.ZERO;
     public static final BigInteger ONE = BigInteger.ONE;
     public static final BigInteger NONE = BigInteger.ONE.negate();
     public static final BigInteger TWO = BigInteger.valueOf(2L);
     public static final BigInteger THREE = BigInteger.valueOf(3L);
 
     public void setCommunicator(Communicator com)
     {
         this.com = com;
     }
 
     /** This ia Alice. */
     public void sendFirst () {
         Random rand = new Random();
 
         BigInteger rsa_e, rsa_d, rsa_n; {
             rsa_e = new BigInteger(512, 42, rand);
             rsa_d = new BigInteger(512, 42, rand);
             rsa_n = rsa_e.multiply(rsa_d);
         }
 
         BigInteger p, x_a, y_a; {
 
             // p = prime
             BigInteger q;
             do {
                 q = new BigInteger(511, rand); // p = random 512 bit number
                 p = q.multiply(TWO).add(ONE); // p = 2q+1
             } while(!p.isProbablePrime(42));
 
             // g = primitive wurzel mod p
 
             // same algorithm to find a generator
             BigInteger pMinusOne = p.subtract(ONE);
             BigInteger g;
             do {
                 // choose 2 < g < q, we should have a 50% probability of hitting a generating number here.
                 g = BigIntegerUtil.randomBetween(THREE, pMinusOne, rand);
                 // check if the required criteria for a generator of G applies
            } while(!p.modPow(q, p).equals(pMinusOne));

             // x_a \in Z_p
             x_a = BigIntegerUtil.randomBetween(TWO, p.subtract(TWO));
 
             // y_a = g^{x_a} mod p
             y_a = g.modPow(x_a, p);
 
             // send p, g, y_a
             com.sendTo(2, p.toString());
             com.sendTo(2, g.toString());
             com.sendTo(2, y_a.toString());
 
         }
 
         BigInteger K; {
 
             // receive y_b, cert_b, xm_b
             BigInteger y_b = new BigInteger(com.receive());
 
 
             // receive cert_a
             {
                 String id = com.receive();
                 byte[] data = new BigInteger(com.receive()).toByteArray();
                 BigInteger sig = new BigInteger(com.receive());
 
                 Certificate cert_b = new Certificate(id, data, sig);
 
                 if(!checkCertificate(cert_b)) {
                     System.err.println("Error: hash check failed");
                     System.exit(1);
                 }
 
             }
 
             BigInteger xm_b = new BigInteger(com.receive());
 
             // CHECK(cert_b)
 
             // K = y_b^{x_a} mod p
             K = y_b.modPow(x_a, p);
 
             // m_b = UNIDEA(K, xm_b)
             BigInteger m_b = decrypt(K, xm_b);
 
             // test m_b == HASH(y_b*p + y_a)
             if(m_b != hash(y_a.multiply(p).add(y_b))) {
                 System.err.println("Error: hash check failed");
                 System.exit(1);
             }
 
             // xm_a = IDEA(K, HASH(y_a*p + y_b)
             BigInteger m_a = hash(y_a.multiply(p).add(y_b));
             BigInteger xm_a = crypt(K, m_a);
 
             Certificate cert_a = TrustedAuthority.newCertificate((rsa_e + "" + rsa_n).getBytes());
 
             // send cert_a, xm_a
             com.sendTo(2, cert_a.getID());
             com.sendTo(2, new BigInteger(cert_a.getData()).toString());
             com.sendTo(2, cert_a.getSignature().toString());
             com.sendTo(2, xm_a.toString());
         }
 
         // chat
         chat(K, true);
 
     }
 
     /** This is Bob. */
     public void receiveFirst () {
 
         Random rand = new Random();
 
         BigInteger rsa_e, rsa_d, rsa_n; {
             rsa_e = new BigInteger(512, 42, rand);
             rsa_d = new BigInteger(512, 42, rand);
             rsa_n = rsa_e.multiply(rsa_d);
         }
 
         BigInteger p, y_a, K, y_b; {
 
             // receive p, g, y_a
             p = new BigInteger(com.receive());
             BigInteger g = new BigInteger(com.receive());
             y_a = new BigInteger(com.receive());
 
             // x_b \in Z_p
             BigInteger x_b = BigIntegerUtil.randomBetween(TWO, p.subtract(TWO));
 
             // K = y_a^{x_b} mod p
             K = y_a.modPow(x_b, p);
 
             // y_b = g^{x_b} mod p
             y_b = g.modPow(x_b, p);
 
             // xm_b = IDEA(K, HASH(y_b*p + y_a)
             BigInteger m_b = hash(y_b.multiply(p).add(y_a));
             BigInteger xm_b = crypt(K, m_b);
 
             Certificate cert_b = TrustedAuthority.newCertificate((rsa_e + "" + rsa_n).getBytes());
 
             // send cert_a, xm_a
             com.sendTo(1, cert_b.getID());
             com.sendTo(1, new BigInteger(cert_b.getData()).toString());
             com.sendTo(1, cert_b.getSignature().toString());
 
             // send y_b, cert_b, xm_b
             com.sendTo(1, y_b.toString());
             com.sendTo(1, cert_b.toString());
             com.sendTo(1, xm_b.toString());
 
         }
 
         // receive cert_a
         {
             String id = com.receive();
             byte[] data = new BigInteger(com.receive()).toByteArray();
             BigInteger sig = new BigInteger(com.receive());
 
             Certificate cert_a = new Certificate(id, data, sig);
 
             // CHECK(cert_a)
             if(!checkCertificate(cert_a)) {
                 System.err.println("Error: hash check failed");
                 System.exit(1);
             }
 
         }
 
         {
             // receive xm_a
             BigInteger xm_a = new BigInteger(com.receive());
 
             // m_a = IDEA(K, xm_a)
             BigInteger m_a = decrypt(K, xm_a);
 
             // test m_b == HASH(y_b*p + y_a)
             if(m_a != hash(y_b.multiply(p).add(y_a))) {
                 System.err.println("Error: hash check failed");
                 System.exit(1);
             }
 
         }
 
         // chat
         chat(K, false);
 
     }
 
     /** Chat, given a K.
      * All other participants are expected to have agreed on this K! Otherwise,
      * there will be garbage.
      */
     public void chat(BigInteger K, boolean init) {
 
         try {
 
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             String line;
             BigInteger tmp;
 
             if(init) {
                 // yeah yeah, code duplication... this saves runtime, one if per loop!
                 line = in.readLine();
                 if(line == null) {
                     return;
                 }
                 tmp = new BigInteger(line.getBytes());
                 tmp = crypt(K, tmp);
                 com.sendTo(init ? 2 : 1, tmp.toString(16));
             }
 
             while(true) {
 
                 line = com.receive();
                 tmp = new BigInteger(line, 16);
                 tmp = decrypt(K, tmp);
                 System.out.println(new String(tmp.toByteArray()));
 
                 line = in.readLine();
                 if(line == null) {
                     return;
                 }
                 tmp = new BigInteger(line.getBytes());
                 tmp = crypt(K, tmp);
                 com.sendTo(init ? 2 : 1, tmp.toString(16));
 
             }
 
         } catch(IOException e) {
             e.printStackTrace();
             System.exit(0);
         }
 
     }
 
     /** Check if a certificate is valid, referencing the TrustedAuthority.
      * This is mostly copy &amp; paste from TrustedAuthority.
      * @see TrustedAuthority.newCertificate
      */
     public static boolean checkCertificate(Certificate cert) {
         MessageDigest sha = null;
 
         // make SHA Hashfunction
         try {
             sha = MessageDigest.getInstance("SHA");
         } catch (Exception e) {
             System.out.println("Could not create message digest! Exception " + e.toString());
         }
 
         // Hashwert bestimmen
         sha.update(cert.getID().getBytes());
         sha.update(cert.getData());
         byte[] digest = sha.digest();
 
         // the hash we generated ourselves
         BigInteger nam = new BigInteger(digest).mod(TrustedAuthority.getModulus());
 
         // the signature, which decrypted should be the hash
         BigInteger nom = cert.getSignature().modPow(TrustedAuthority.getPublicExponent(), TrustedAuthority.getModulus());
 
         return nam.equals(nom);
 
     }
 
 
     public BigInteger hash(BigInteger x) {
         return x;
     }
 
     public BigInteger crypt(BigInteger key, BigInteger msg) {
         byte[] msg_bytes = msg.toByteArray();
         ByteOutputStream  cipherStream = new ByteOutputStream();
         IDEA idea = new IDEA(key);
         
         int offset = 0;
         while (offset < msg_bytes.length) {
             byte[] bytes_block = new byte[8];
             //padding with zero if less than 8 bytes
             for (int i = 0; i < 8; i++) {
                 if (offset+i < msg_bytes.length) {
                     bytes_block[i] = msg_bytes[offset+i];
                 }
                 else {
                     bytes_block[i] = (byte) 0;
                 }
             }
             byte[] cipher_block = new byte[8];
             IDEA.idea_block(bytes_block, cipher_block, idea.keys_enc);
             cipherStream.write(cipher_block);
             offset +=8;
         }
         
         return new BigInteger(cipherStream.getBytes());
     }
     public BigInteger decrypt(BigInteger key, BigInteger msg) {
         byte[] msg_bytes = msg.toByteArray();
         ByteOutputStream  clearStream = new ByteOutputStream();
         IDEA idea = new IDEA(key);
         
         int offset = 0;
         while (offset < msg_bytes.length) {
             assert(offset+8 < msg_bytes.length); //blocks must have 8 bytes each
             byte[] bytes_block =  Arrays.copyOfRange(msg_bytes, offset, offset+8);
             assert(bytes_block.length==8);//blocks must have 8 bytes each
             
             byte[] clear_block = new byte[8];
             IDEA.idea_block(bytes_block, clear_block, idea.keys_dec);
             clearStream.write(clear_block);
             offset += 8;
         }
         
         return new BigInteger(clearStream.getBytes());
     }
 
     public String nameOfTheGame () {
         return NameOfTheGame;
     }
 
     public int minPlayer ()
     {
         return MinPlayer;
     }
 
     public int maxPlayer ()
     {
         return MaxPlayer;
     }
 }
