 /*
  * @(#)Skeleton.java
  */
 
 import de.tubs.cs.iti.jcrypt.protokoll.*;
 import de.tubs.cs.iti.jcrypt.chiffre.*;
 
 import java.math.*;
 import java.util.*;
 import java.io.*;
 import java.security.MessageDigest;
 
 /**
  *
  */
 
 public final class SecretSharing implements Protocol
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
 
     // alice: shareSecret("asdf", 5, true);
     // bob: shareSecret("fdsa", 5, false);
 
     static Random rand = new Random();
     static boolean debug = true;
 
     public static BigInteger[] genRandomWords(int n, int l) {
         BigInteger[] ret = new BigInteger[n];
         for(int i = 0; i < n; i++) {
             ret[i] = new BigInteger(l, rand);
         }
 
         if(debug) {
             for(int i = 0; i < n; i++) {
                 System.out.println("word " + i + ": " + ret[i].toString(36));
             }
         }
         return ret;
     }
 
     ElGamalCipher elGamalC_own;
     ElGamalSignature elGamalS_own;
     ElGamalCipher elGamalC_other;
     ElGamalSignature elGamalS_other;
     BigInteger p;
 
     public void tradeElGamal(boolean first) {
 
         {
             // note: q is p in elgamal (too lazy to refactor that, "never touch a running system")
             // we just use a local p here, since we use it rather often anyways
             p = new BigInteger("4988735951183711405443349413015910122453507015594895638933838601555750189585703700647655985269637551634513770201277370413860951650702374379627998821919409");
             BigInteger g = new BigInteger("4403105895869798297264918950735787070665047406714785361037216842427722734684061748868589917485012596281820467352001338223691996653533143166890875549812531");
             BigInteger y = new BigInteger("3670294064109445804998782973709772470002041046377612489028768098078250713079795031354099562309432613560558383306865142781216201315104971340333690591679721");
             BigInteger x = new BigInteger("4589946301809196862611751989088793376762175950291076147544077975213763218505486754450017554342955014202444667772016113058406939298289857995054770609176615");
 
             // Our own cipher
             elGamalC_own = new ElGamalCipher(p, g, y, x);
             elGamalS_own = new ElGamalSignature(p, g, y, x);
         }
 
         if(first) {
 
             // send public key: p, g, y
             com.sendTo(1, p.toString(16));
             com.sendTo(1, elGamalC_own.g.toString(16));
             com.sendTo(1, elGamalC_own.y.toString(16));
 
             // receive p, g, y
             {
                 BigInteger p = new BigInteger(com.receive(),16);
                 BigInteger g = new BigInteger(com.receive(),16);
                 BigInteger y = new BigInteger(com.receive(),16);
 
                 elGamalC_other = new ElGamalCipher(p, g, y);
                 elGamalS_other = new ElGamalSignature(p, g, y);
             }
 
         } else {
 
             // receive p, g, y
             {
                 BigInteger p = new BigInteger(com.receive(),16);
                 BigInteger g = new BigInteger(com.receive(),16);
                 BigInteger y = new BigInteger(com.receive(),16);
 
                 elGamalC_other = new ElGamalCipher(p, g, y);
                 elGamalS_other = new ElGamalSignature(p, g, y);
             }
 
            com.sendTo(1, p.toString(16));
            com.sendTo(1, elGamalC_own.g.toString(16));
            com.sendTo(1, elGamalC_own.y.toString(16));
         }
 
     }
 
     public void otSend(BigInteger M0, BigInteger M1) {
 
         assert(elGamalC_own != null && elGamalS_own != null);
         assert(elGamalC_other != null && elGamalS_other != null);
         assert(p != null);
 
         // send random m0, m1 between 0 and p. m0 != m1
         BigInteger m0, m1; {
             m0 = BigIntegerUtil.randomBetween(ZERO, p);
             do {
                 m1 = BigIntegerUtil.randomBetween(ZERO, p);
             } while(m0.equals(m1));
         }
 
         // send both
         com.sendTo(1, m0.toString(16));
         com.sendTo(1, m1.toString(16));
 
         // receive q
         BigInteger q = new BigInteger(com.receive(), 16);
         
         BigInteger s0, s1;
         BigInteger M0_dash, M1_dash;
         int s; {
 
             BigInteger k0, k1; {
 
                 // k0 = decipher((q-m0) mod p)
                 k0 = elGamalC_own.decipherBlock(q.subtract(m0).mod(p.pow(2)));
                 //k0 = (q.subtract(m0).mod(p)); //without elgamal
                 // k1 = decipher((q-m1) mod p)
                 k1 = elGamalC_own.decipherBlock(q.subtract(m1).mod(p.pow(2)));
                 //k1 = (q.subtract(m1).mod(p)); //without elgamal
             }
 
             System.out.println("k0'" + k0.toString(16));
             System.out.println("k1'" + k1.toString(16));
 
             //S0 = sign(k0)
             s0 = elGamalS_own.signBlock(k0);
             //S1 = sign(k1)
             s1 = elGamalS_own.signBlock(k1);
 
             System.out.println("s0'" + s0.toString(16));
             System.out.println("s1'" + s1.toString(16));
 
 
             // select random s in {0,1}
             s = new Random().nextBoolean() ? 1 : 0;
 
             // send M_strich_0 := (M0 + k_{s xor 0}) mod p
             M0_dash = M0.add(s == 1 ? k1 : k0).mod(p);
             // send M_strich_1 := (M1 + k_{s xor 1}) mod p
             M1_dash = M1.add(s == 0 ? k1 : k0).mod(p);
 
             System.out.println("M0'" + M0_dash.toString(16));
             System.out.println("M1'" + M1_dash.toString(16));
 
         }
 
         com.sendTo(1, M0_dash.toString(16));
         com.sendTo(1, M1_dash.toString(16));
 
         // send s0, s1
         com.sendTo(1, s0.toString(16));
         com.sendTo(1, s1.toString(16));
 
         // send s
         com.sendTo(1, Integer.toString(s));
 
     }
 
     public BigInteger otReceive() {
 
         //receive m0,m1
         BigInteger[] m= new BigInteger[2];
         m[0] = new BigInteger(com.receive(), 16);
         m[1] = new BigInteger(com.receive(), 16);
 
         //select random b in {0,1}
         BigInteger b = new BigInteger(1, new Random());
         System.out.println("b=" + b.toString(16));
         
         //select random k between 0 and p
         BigInteger k;
         do {
             k = new BigInteger(p.bitLength(), new Random());
         } while (k.compareTo(p) >= 0);
         System.out.println("k=" + k.toString(16));
 
         
         //send q:= (crypt(k) + m_b) mod p^2
         BigInteger q = elGamalC_other.encipherBlock(k).add(m[b.intValue()]).mod(p.pow(2));
         //BigInteger q = k.add(m[b.intValue()]).mod(p); // without elgamal for debugging
 
         System.out.println("q=" + q.toString(16));
         com.sendTo(0, q.toString(16));
         
         //receive M_strich_0, M_strich_1
         BigInteger[] M_strich= new BigInteger[2];
         M_strich[0] = new BigInteger(com.receive(), 16);
         M_strich[1] = new BigInteger(com.receive(), 16);
         System.out.println("M0'=" + M_strich[0].toString(16));
         System.out.println("M1'=" + M_strich[1].toString(16));
 
         
         //receive S0,S1
         BigInteger[] S= new BigInteger[2];
         S[0] = new BigInteger(com.receive(), 16);
         S[1] = new BigInteger(com.receive(), 16);
         System.out.println("S0=" + S[0].toString(16));
         System.out.println("S1=" + S[1].toString(16));
 
         
         //receive s
         BigInteger s = new BigInteger(com.receive(), 16);
         System.out.println("s'=" + s.toString(16));
         
         //compute M_{s ^ b} := M_strich_{s ^ b} - k
         BigInteger M_sb = M_strich[s.xor(b).intValue()].mod(p).subtract(k).mod(p);
         
         //compute k_quer := M_strich_{s ^ b ^ 1} - M_{s ^ b}
         BigInteger k_quer = M_strich[s.xor(b).xor(ONE).intValue()].subtract(M_sb).mod(p);
         BigInteger k_quer2 = M_strich[s.xor(b).intValue()].subtract(M_sb).mod(p);
 
         System.out.println("k_quer=" + k_quer.toString(16));
         System.out.println("k_quer2=" + k_quer2.toString(16));
 
         if (elGamalS_other.verifyBlock(k_quer, S[b.xor(ONE).intValue()])) {
             System.out.println("You have been betrayed!");
         }
         else {
             System.out.println("Congratulations! With a probability of 1/2 you were not betrayed!");
             System.out.println("The received secret is: " + M_sb);
             
             if (elGamalS_other.verifyBlock(k_quer2, S[b.intValue()])) {
                 System.out.println("The signature was correct!");
             }
             else {
                 System.out.println("But the signature is wrong.");
             }
         }
 
         return M_sb;
 
     }
 
     /** This ia Alice. */
     public void sendFirst () {
         boolean betray = true;
 
         if(betray) {
             System.out.println("betrayal incoming!");
         }
 
         int n = rand.nextInt(10);
         int k = rand.nextInt(10);
         int wordlen = 4;
         int bitlen = (int) Math.ceil(wordlen * (Math.log(36) / Math.log(2)));
 
         // generate n random words
         BigInteger[] words_a = SecretSharing.genRandomWords(n*2, bitlen);
         BigInteger[] words_b = new BigInteger[n/2];
 
         tradeElGamal(true);
 
         // Send 5 out of 10 secrets (but we don't know which, pairwise)
         for(int i = 0; i < n; i += 2) {
             otSend(words_a[i+0], words_a[i+1]);
         }
 
         // Receive bob's n/2 words using 1-2 OT
         for(int i = 0; i < n; i += 2) {
             words_b[i] = otReceive();
         }
         
         // Words we're going to receive
         SecretReceive[] secretsReceive = new SecretReceive[n];
         for (int i = 0; i < n; i++) {
             secretsReceive[i] = new SecretReceive(k);
         }
         
         // Our secrets to send
         SecretSend[] secretsSend = new SecretSend[n];
         for (int i = 0; i < n; i++) {
             secretsSend[i] = new SecretSend(words_a[i], k);
         }
         
         while (secretsSend[0].getCurrentBitLength() <= bitlen) { //Assuming that all secrets have equal length
             for (int i = 0; i < (int)Math.pow(2, k); i++) {
                 //send secret parts
                 for (int j = 0; i < n; j++) {
                     com.sendTo(1, Integer.toString(secretsSend[j].y(),16));
                 }
                 //receive secret parts
                 for (int j = 0; j < n; j++) {
                     secretsReceive[j].notY(Integer.parseInt(com.receive(),16));
                 }
             }
             
             //expand all prefixes
             for (int j = 0; j < n; j++) {
                 secretsSend[j].nextRound();
                 secretsReceive[j].nextRound();
             }
         }
         
         //now 2^k possiblities should be left for each secret. lets exclude the 2^k - 1 remaining ones
         for (int i = 0; i < (int)Math.pow(2, k) - 1; i++) {
             for (int j = 0; i < n; j++) { 
                 com.sendTo(1, Integer.toString(secretsSend[j].y(),16));
                 secretsReceive[j].notY(Integer.parseInt(com.receive(),16));
             }
         }
         
         for(int i = 0; i < n; i += 2) {
             if(!(secretsReceive[i].solve().equals(words_b[i]) || secretsReceive[i+1].solve().equals(words_b[i]))) {
                 System.err.println("Error!");
             }
         }
     }
 
     /** This is Bob. */
     public void receiveFirst () {
         boolean betray = true;
 
         if(betray) {
             System.out.println("betrayal incoming!");
         }
 
         int n = rand.nextInt(10);
         int k = rand.nextInt(10);
         int wordlen = 4;
         int bitlen = (int) Math.ceil(wordlen * (Math.log(36) / Math.log(2)));
 
         // generate n random words
         BigInteger[] words_a = SecretSharing.genRandomWords(n*2, bitlen);
         BigInteger[] words_b = new BigInteger[n/2];
 
         tradeElGamal(true);
 
         // Receive bob's n/2 words using 1-2 OT
         for(int i = 0; i < n; i += 2) {
             words_b[i] = otReceive();
         }
         
         // Send 5 out of 10 secrets (but we don't know which, pairwise)
         for(int i = 0; i < n; i += 2) {
             otSend(words_a[i+0], words_a[i+1]);
         }
         
         // Words we're going to receive
         SecretReceive[] secretsReceive = new SecretReceive[n];
         for (int i = 0; i < n; i++) {
             secretsReceive[i] = new SecretReceive(k);
         }
         
         // Our secrets to send
         SecretSend[] secretsSend = new SecretSend[n];
         for (int i = 0; i < n; i++) {
             secretsSend[i] = new SecretSend(words_a[i], k);
         }
         
         while (secretsSend[0].getCurrentBitLength() <= bitlen) { //Assuming that all secrets have equal length
             for (int i = 0; i < (int)Math.pow(2, k); i++) {
                 //receive secret parts
                 for (int j = 0; j < n; j++) {
                     secretsReceive[j].notY(Integer.parseInt(com.receive(),16));
                 }
                 
                 //send secret parts
                 for (int j = 0; i < n; j++) {
                    com.sendTo(1, Integer.toString(secretsSend[j].y(),16));
                 }
             }
             
             //expand all prefixes
             for (int j = 0; j < n; j++) {
                 secretsSend[j].nextRound();
                 secretsReceive[j].nextRound();
             }
         }
         
         //now 2^k possiblities should be left for each secret. lets exclude the 2^k - 1 remaining ones
         for (int i = 0; i < (int)Math.pow(2, k) - 1; i++) {
             for (int j = 0; i < n; j++) { 
                 secretsReceive[j].notY(Integer.parseInt(com.receive(),16));
                com.sendTo(1, Integer.toString(secretsSend[j].y(),16));
             }
         }
         
         for(int i = 0; i < n; i += 2) {
             if(!(secretsReceive[i].solve().equals(words_b[i]) || secretsReceive[i+1].solve().equals(words_b[i]))) {
                 System.err.println("Error!");
             }
         }
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
