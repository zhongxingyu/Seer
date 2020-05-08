 import com.krypto.idea.IDEA;
 import com.krypto.rsa.RSA;
 import com.krypto.fingerprint.Fingerprint;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.SecureRandom;
 import java.util.Random;
 import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
 import de.tubs.cs.iti.krypto.protokoll.*;
 
 public final class StationToStation implements Protocol {
 
   static private int MinPlayer = 2; // Minimal number of players
   static private int MaxPlayer = 2; // Maximal number of players
   static private String NameOfTheGame = "Station To Station";
   private Communicator Com;
 
   private BigInteger ONE = BigIntegerUtil.ONE;
   private BigInteger TWO = BigIntegerUtil.TWO;
 
   private BigInteger p, g; // Primzahl p, prim. W. g
 
   private Fingerprint fingerprint;
   private IDEA idea;
 
   public void getPrimeAndGenerator() {
     Random sc = new SecureRandom();
     int k = 512; // prime number with k=512 bits
     int certainty = 100; // The probability that the new BigInteger
     // represents a prime number will
     // exceed (1-1/2^certainty)
 
     p = null;
     BigInteger q = null;
     do {
       q = new BigInteger(k - 1, certainty, sc);
       p = q.multiply(TWO).add(ONE); // secure
       // prime
       // p =
       // 2q+1
     } while (!p.isProbablePrime(certainty));
 
     BigInteger MINUS_ONE = ONE.negate().mod(p); // -1 mod p
 
     g = null;
     BigInteger factor = null;
     do {
       // 2 <= g < p-1
       g = BigIntegerUtil.randomBetween(TWO, p.subtract(ONE), sc);
       factor = g.modPow(q, p);
     } while (!factor.equals(MINUS_ONE));
   }
 
   public void setCommunicator(Communicator com) {
     Com = com;
   }
 
   /**
    * Aktionen der beginnenden Partei. Bei den 2-Parteien-Protokollen seien dies die Aktionen von
    * Alice.
    */
   public void sendFirst() {
     System.out.println("-- Alice --");
 
     // fingerprint werte aus datei auslesen
     fingerprint = new Fingerprint(new File("HashParameter"));
 
     // alice wählt p und g und sendet diese an bob
     getPrimeAndGenerator();
     System.out.println("p: " + p);
     System.out.println("g: " + g);
     Com.sendTo(1, p.toString(16)); // S1
     Com.sendTo(1, g.toString(16)); // S2
 
     // alice sendet öffentlichen schlüssel an bob
     RSA rsa_A = new RSA();
     System.out.println("RSA Alice e: " + rsa_A.e);
     System.out.println("RSA Alice n: " + rsa_A.n);
     System.out.println("RSA Alice d: " + rsa_A.d);
     Com.sendTo(1, rsa_A.e.toString(16)); // S3
     Com.sendTo(1, rsa_A.n.toString(16)); // S4
 
     // alice empfängt öffentlichen schlüssel von bob
     BigInteger e_B = new BigInteger(Com.receive(), 16); // R5
     BigInteger n_B = new BigInteger(Com.receive(), 16); // R6
     System.out.println("Bob e: " + e_B);
     System.out.println("Bob n: " + n_B);
 
     // zufällige zahl x_A in {1,...,p-2} -> randomBetween 1 <= x_A < p-1
     BigInteger x_A = BigIntegerUtil.randomBetween(ONE, p.subtract(ONE));
     // alice wählt x_A = g^(x_A) mod p
     BigInteger y_A = g.modPow(x_A, p);
     // y_A an bob senden
     Com.sendTo(1, y_A.toString(16)); // S7
 
     System.out.println("Alice: Receive cert");
 
     // alice empfängt certificate in einzelteilen
     Certificate Z_B = buildCertificateBasedOnStrings(Com.receive(), Com.receive(), Com.receive()); // R8,9,10
     System.out.println("Z_B");
     printCertificate(Z_B);
 
     // alice empfängt y_B, S_B_encrypted
     BigInteger y_B = new BigInteger(Com.receive(), 16); // R11
     BigInteger[] S_B_encrypted = deserializeBigIntegerArray(Com.receive()); // R12
 
     // alice berechnet k
     BigInteger k = y_B.modPow(x_A, p);
 
     // check certificate
     if (checkCertificate(Z_B) == true) {
       System.out.println("Zertifikat Check: Zertifikat von Bob ist korrekt!");
     } else {
       System.out.println("Zertifikat Check: Zertifikat von Bob ist NICHT korrekt! ABBRUCH!");
       System.exit(0);
     }
 
     // decrypt S_B_encrypted with idea
     BigInteger key = getIDEAKeyBasedOnK(k);
     BigInteger iv = new BigInteger("ddc3a8f6c66286d2", 16);
     idea = new IDEA(key, iv);
     String S_B_decrypted = idea.decipher(S_B_encrypted);
     BigInteger S_B = new BigInteger(S_B_decrypted, 16);
 
     // generate hash
     BigInteger hash = hash(y_B, y_A);
 
     // alice überprüft die gültigkeit von S_B
     if (checkSignature(hash, S_B, e_B, n_B) == true) {
       System.out.println("Signature Check: hashs h(y_B, y_A) sind gleich! Alice akzeptiert k!");
     } else {
       System.out.println("Signature Check: Hashs nicht gleich! ABBRUCH!");
       System.exit(0);
     }
 
     // signatur von alice S_A generieren
    // generate hash
    BigInteger hash2 = hash(y_A, y_B);
    BigInteger S_A = rsa_A.getSignatur(hash2); // S_A = hash^d_A mod n_A
     System.out.println("Signatur S_A: " + S_A);
 
     // zertifikat generieren
     Certificate Z_A = generateCertificate(rsa_A.e, rsa_A.n);
 
     // signatur encrypted
     String message = S_A.toString(16);
     String S_A_encrypted = serializeBigIntegerArray(idea.encipher(message));
 
     // alice sendet Z_A in einzelteilen
     Com.sendTo(1, Z_A.getID()); // send ID // S13
     // String data_send = new String(Z_A.getData());
     String data_send = serialize(Z_A.getData());
     Com.sendTo(1, data_send); // send data (pub key) // S14
     Com.sendTo(1, Z_A.getSignature().toString(16)); // send signature // S15
 
     // und S_A_encrypted (ohne y_A, das wurde schon gesendet)
     Com.sendTo(1, S_A_encrypted); // S16
 
   }
 
   /**
    * Aktionen der uebrigen Parteien. Bei den 2-Parteien-Protokollen seien dies die Aktionen von Bob.
    */
   public void receiveFirst() {
     System.out.println("-- Bob --");
 
     // fingerprint werte aus datei auslesen
     fingerprint = new Fingerprint(new File("HashParameter"));
 
     // bob bekommt p und g von alice
     p = new BigInteger(Com.receive(), 16); // R1
     g = new BigInteger(Com.receive(), 16); // R2
     System.out.println("p: " + p);
     System.out.println("g: " + g);
 
     // bob bekommt öffentlichen rsa schlüssel von alice
     BigInteger e_A = new BigInteger(Com.receive(), 16); // R3
     BigInteger n_A = new BigInteger(Com.receive(), 16); // R4
     System.out.println("Alice e: " + e_A);
     System.out.println("Alice n: " + n_A);
 
     // bob sendet seinen öffentlichen schlüssel
     RSA rsa_B = new RSA();
     System.out.println("RSA Bob e: " + rsa_B.e);
     System.out.println("RSA Bob n: " + rsa_B.n);
     System.out.println("RSA Bob d: " + rsa_B.d);
     Com.sendTo(0, rsa_B.e.toString(16)); // S5
     Com.sendTo(0, rsa_B.n.toString(16)); // S6
 
     // bob empfängt y_A
     BigInteger y_A = new BigInteger(Com.receive(), 16); // R7
     System.out.println("y_A: " + y_A);
 
     // zufällige zahl x_B in {1,...,p-2} -> randomBetween 1 <= x_B < p-1
     BigInteger x_B = BigIntegerUtil.randomBetween(ONE, p.subtract(ONE));
     // bob wählt x_B = g^(x_B) mod p
     BigInteger y_B = g.modPow(x_B, p);
 
     // bob bestimmt schlüssel
     BigInteger k = y_A.modPow(x_B, p);
     System.out.println("k: " + k.toString());
 
     // signatur
     BigInteger hash = hash(y_B, y_A);
     BigInteger S_B = rsa_B.getSignatur(hash); // S_B = hash^d_B mod n_B
     System.out.println("Signatur S_B: " + S_B);
 
     // zertifikat generieren
     Certificate Z_B = generateCertificate(rsa_B.e, rsa_B.n);
     System.out.println("Z_B");
     printCertificate(Z_B);
 
     // encrypted S_B with idea
     BigInteger key = getIDEAKeyBasedOnK(k);
 
     BigInteger iv = new BigInteger("ddc3a8f6c66286d2", 16);
     idea = new IDEA(key, iv);
 
     String message = S_B.toString(16);
     String S_B_encrypted = serializeBigIntegerArray(idea.encipher(message));
 
     // bob sendet certificate in einzelteilen
     Com.sendTo(0, Z_B.getID()); // send ID // S8
     // String data_send = new String(Z_B.getData());
     String data_send = serialize(Z_B.getData());
     Com.sendTo(0, data_send); // send data (pub key) // S9
     Com.sendTo(0, Z_B.getSignature().toString(16)); // send signature //S10
 
     // bob sendet y_B
     Com.sendTo(0, y_B.toString(16)); // S11
 
     // bob sendet S_B_encrypted
     Com.sendTo(0, S_B_encrypted); // S12
 
     System.out.println("Bob: Receive cert");
 
     // bob empfängt certificate in einzelteilen
     Certificate Z_A = buildCertificateBasedOnStrings(Com.receive(), Com.receive(), Com.receive()); // R13,14,15
 
     // check certificate
     if (checkCertificate(Z_A) == true) {
       System.out.println("Zertifikat Check: Zertifikat von Alice ist korrekt!");
     } else {
       System.out.println("Zertifikat Check: Zertifikat von Alice ist NICHT korrekt! ABBRUCH!");
       System.exit(0);
     }
 
     // bob empfängt S_A_encrypted
     BigInteger[] S_A_encrypted = deserializeBigIntegerArray(Com.receive()); // R16
 
     // decrypt S_A_encrypted with idea
     String S_A_decrypted = idea.decipher(S_A_encrypted);
     BigInteger S_A = new BigInteger(S_A_decrypted, 16);
 
    // bob überprüft die gültigkeit von S_B
    BigInteger hash2 = hash(y_A, y_B);
    if (checkSignature(hash2, S_A, e_A, n_A) == true) {
      System.out.println("Signature Check: hashs h(y_A, y_B) sind gleich! Bob akzeptiert k!");
     } else {
       System.out.println("Signature Check: Hashs nicht gleich! ABBRUCH!");
       System.exit(0);
     }
 
   }
 
   public String nameOfTheGame() {
     return NameOfTheGame;
   }
 
   public int minPlayer() {
     return MinPlayer;
   }
 
   public int maxPlayer() {
     return MaxPlayer;
   }
 
   private BigInteger getIDEAKeyBasedOnK(BigInteger k) {
     int l = k.bitLength();
     BigInteger key = k.shiftRight(l - 128);
 
     return key;
   }
 
   private Certificate generateCertificate(BigInteger e, BigInteger n) {
     BigInteger en = concat(e, n);
     byte[] data = en.toByteArray();
     Certificate cert = TrustedAuthority.newCertificate(data);
 
     return cert;
   }
 
   private Certificate buildCertificateBasedOnStrings(String ID, String data, String signature) {
     // byte[] dataArray = data.getBytes();
     byte[] dataArray = deserialize(data);
     BigInteger signatureInteger = new BigInteger(signature, 16);
     // wieder certificate objekt draus machen
     Certificate cert = new Certificate(ID, dataArray, signatureInteger);
 
     return cert;
   }
 
   private BigInteger hash(BigInteger y_A, BigInteger y_B) {
     // hash h(y_B, y_A) generieren
     BigInteger m = y_B.multiply(p).add(y_A); // h(y_B,y_A) = y_B * p + y_A laut heft
     BigInteger hash = fingerprint.hash(m.toString(16));
 
     return hash;
   }
 
   private boolean checkSignature(BigInteger hash, BigInteger S, BigInteger e, BigInteger n) {
     boolean isCorrekt = false;
 
     // left part of equation
     BigInteger left = S.modPow(e, n); // decrypted with pub key (RSA)
 
     System.out.println("checkSignature left: " + left);
     System.out.println("checkSignature hash: " + hash);
 
     if (left.equals(hash)) {
       isCorrekt = true;
     }
 
     return isCorrekt;
   }
 
   private void printCertificate(Certificate cert) {
     System.out.println("printCertificate ID: " + cert.getID());
     System.out.println("printCertificate Data: " + new String(cert.getData()));
     System.out.println("printCertificate Signature: " + cert.getSignature());
   }
 
   private boolean checkCertificate(Certificate cert) {
     boolean isCorrekt = false;
     MessageDigest sha = null;
 
     // get public key of trusted authority
     BigInteger n_T = TrustedAuthority.getModulus();
     BigInteger e_T = TrustedAuthority.getPublicExponent();
 
     // make SHA Hashfunction
     try {
       sha = MessageDigest.getInstance("SHA");
     } catch (Exception e) {
       System.out.println("Could not create message digest! Exception " + e.toString());
     }
 
     // hash with ID + Data
     sha.update(cert.getID().getBytes());
     sha.update(cert.getData());
     byte[] digest = sha.digest();
     BigInteger hash = new BigInteger(digest).mod(n_T);
 
     // RSA signature
     BigInteger M = cert.getSignature().modPow(e_T, n_T);
 
     System.out.println("checkCertificate M: " + M);
     System.out.println("checkCertificate hash: " + hash);
 
     if (M.equals(hash)) {
       isCorrekt = true;
     }
 
     return isCorrekt;
   }
 
   public BigInteger concat(BigInteger leftBlock, BigInteger rightBlock) {
     int rightBlockLength = rightBlock.bitLength();
     return (leftBlock.shiftLeft(rightBlockLength)).add(rightBlock);
   }
 
   public String serializeBigIntegerArray(BigInteger[] array) {
     String output = "";
     for (int i = 0; i < array.length; i++) {
       output += array[i].toString(16) + " ";
     }
     return output;
   }
 
   public BigInteger[] deserializeBigIntegerArray(String message) {
     String[] array = message.split(" ");
     BigInteger[] output = new BigInteger[array.length];
     for (int i = 0; i < array.length; i++) {
       output[i] = new BigInteger(array[i], 16);
     }
     return output;
   }
 
   public String serialize(byte[] array) {
     String output = "";
     for (int i = 0; i < array.length; i++) {
       output += array[i] + " ";
     }
     return output;
   }
 
   public byte[] deserialize(String message) {
     String[] array = message.split(" ");
     byte[] output = new byte[array.length];
     for (int i = 0; i < array.length; i++) {
       output[i] = Byte.valueOf(array[i]);
     }
     return output;
   }
 
 }
