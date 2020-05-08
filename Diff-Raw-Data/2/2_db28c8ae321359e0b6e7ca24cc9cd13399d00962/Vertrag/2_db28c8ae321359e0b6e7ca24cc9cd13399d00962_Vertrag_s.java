 import com.krypto.elGamal.ElGamal;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.util.Random;
 
 import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
 import de.tubs.cs.iti.krypto.protokoll.*;
 
 public final class Vertrag implements Protocol {
 
   static private int MinPlayer = 2; // Minimal number of players
   static private int MaxPlayer = 2; // Maximal number of players
   static private String NameOfTheGame = "Geheimnisaustausch";
   private Communicator Com;
 
   private BigInteger ZERO = BigIntegerUtil.ZERO;
   private BigInteger ONE = BigIntegerUtil.ONE;
   private BigInteger TWO = BigIntegerUtil.TWO;
 
   private boolean betray = false;
 
   public void setCommunicator(Communicator com) {
     Com = com;
   }
 
   /**
    * Aktionen der beginnenden Partei. Bei den 2-Parteien-Protokollen seien dies die Aktionen von Alice.
    */
   public void sendFirst() {
     System.out.println("-- Alice --");
     if (betray) {
       System.out.println("ACHTUNG: Betrugsmodus aktiv!!!");
     }
 
     int n = 2; // n in {1,...,10}
 
     // Part 0 ---------------------------------------------------------------
 
     // Hard coded ElGamal
     BigInteger El_p_A = new BigInteger("7789788965135663714690749102453072297748091458564354001035945418057913886819451721947477667556269500246451521462308030406227237346483679855991947569361139");
     BigInteger El_g_A = new BigInteger("6064211169633122201619014531987050083527855665630754543345421103270545526304595525644519493777291154802011605984321393354028831270292432551124003674426238");
     BigInteger El_y_A = new BigInteger("3437627792030969437324738830672923365331058766427964788898937390314623633227168012908665090706697391878208866573481456022491841700034626290242749535475902");
     // private:
     BigInteger El_x_A = new BigInteger("3396148360179732969395840357777168909721385739804535508222449486018759668590512304433229713789117927644143586092277750293910884717312503836910153525557232");
     // Objekt initialisieren mit priv key
     ElGamal elGamal_A = new ElGamal(El_p_A, El_g_A, El_y_A, El_x_A);
 
     // Alice sendet ElGamal public key an Bob
     Com.sendTo(1, elGamal_A.p.toString(16)); // S1
     Com.sendTo(1, elGamal_A.g.toString(16)); // S2
     Com.sendTo(1, elGamal_A.y.toString(16)); // S3
 
     // Alice empfängt Bobs ElGamal pub key
     BigInteger El_p_B = new BigInteger(Com.receive(), 16); // R1
     BigInteger El_g_B = new BigInteger(Com.receive(), 16); // R2
     BigInteger El_y_B = new BigInteger(Com.receive(), 16); // R3
     // ElGamal Objekt ohne priv key bauen
     ElGamal elGamal_B = new ElGamal(El_p_B, El_g_B, El_y_B);
 
     // Vertrag einlesen
     String vertrag_A = vertragString(new File("vertrag.txt"));
 
     BigInteger p_A = computePrime();
 
     BigInteger M = computeMessage(p_A);
 
     // Sende n, p_A, M an Bob
     Com.sendTo(1, Integer.toHexString(n)); // S4
     Com.sendTo(1, p_A.toString(16)); // S5
     Com.sendTo(1, M.toString(16)); // S6
 
     // Alice empfängt p_B
     BigInteger p_B = new BigInteger(Com.receive(), 16); // R4
 
     if (!p_B.isProbablePrime(100)) {
       System.out.println("Bobs p_B is keine Primzahl!");
       System.exit(1);
     }
 
     // Part 1 ---------------------------------------------------------------
 
     // Arrays bauen
     BigInteger[][] A = getDoubleArray(n, p_A);
     BigInteger[] C_A = get_C_Array(M, A, p_A);
 
     BigInteger[] C_B = new BigInteger[2 * n];
     for (int i = 0; i < C_A.length; i++) {
       Com.sendTo(1, C_A[i].toString(16)); // Si
       System.out.println("Send C_A[i]: " + C_A[i].toString(16));
       C_B[i] = new BigInteger(Com.receive(), 16); // Ri
       System.out.println("Receive C_B[i]: " + C_B[i].toString(16));
     }
 
     System.out.println("Alice:");
     for (int i = 0; i < C_A.length; i++) {
       System.out.println("C_A[i] = " + C_A[i]);
     }
 
     for (int i = 0; i < C_B.length; i++) {
       System.out.println("C_B[i] = " + C_B[i]);
     }
 
     String erklaerung_A = erklaerungAlice(C_A);
     String text_A = erklaerung_A + vertrag_A;
     BigInteger H_A = computeSHA(text_A);
     BigInteger H_A_signed = elGamal_A.sign(H_A);
 
     // Sende erklärung, text, signed hash
     Com.sendTo(1, erklaerung_A); // S5
     Com.sendTo(1, vertrag_A); // S6
     Com.sendTo(1, H_A_signed.toString(16)); // S7
 
     // Empfange erklärung, text und signed hash von bob
     String erklaerung_B = Com.receive(); // R5
     String vertrag_B = Com.receive(); // R6
     BigInteger H_B_signed = new BigInteger(Com.receive(), 16);
 
     System.out.println("Erklaerung_B: " + erklaerung_B);
     System.out.println("vertrag_B: " + vertrag_B);
     System.out.println("H_B_signed: " + H_B_signed);
 
     // check sig
     BigInteger H_B = computeSHA(erklaerung_B + vertrag_B);
 
     if (elGamal_B.verify(H_B, H_B_signed)) {
       System.out.println("Jo is signed!");
       Com.sendTo(1, "0");
     } else {
       System.out.println("Is nich signed, Exit!");
       Com.sendTo(1, "1");
       System.exit(1);
     }
 
     if (Com.receive() == "1") {
       System.out.println("Bobs bricht ab!");
       System.exit(1);
     }
 
     // Geheimnisprotokoll
     BigInteger[][] B = geheimnisAlice(n, 4, 51, A);
     BigInteger[] C2_B = get_C_Array(M, B, p_B);
 
     System.out.println("Alice: Checking Arrays !-o-o-o-o-o-!");
     if (check(C_B, C2_B)) {
       System.out.println("Alice: Alles klar!");
     } else {
       System.out.println("Alice: FUUUUUUUUU");
       System.exit(0);
     }
 
   }
 
   /**
    * Aktionen der uebrigen Parteien. Bei den 2-Parteien-Protokollen seien dies die Aktionen von Bob.
    */
   public void receiveFirst() {
     System.out.println("-- Bob --");
     if (betray) {
       System.out.println("ACHTUNG: Betrugsmodus aktiv!!!");
     }
 
     // Part 0 ---------------------------------------------------------------
 
     // Hard coded ElGamal
     BigInteger El_p_B = new BigInteger("7789788965135663714690749102453072297748091458564354001035945418057913886819451721947477667556269500246451521462308030406227237346483679855991947569361139");
     BigInteger El_g_B = new BigInteger("6064211169633122201619014531987050083527855665630754543345421103270545526304595525644519493777291154802011605984321393354028831270292432551124003674426238");
     BigInteger El_y_B = new BigInteger("3437627792030969437324738830672923365331058766427964788898937390314623633227168012908665090706697391878208866573481456022491841700034626290242749535475902");
     // private:
     BigInteger El_x_B = new BigInteger("3396148360179732969395840357777168909721385739804535508222449486018759668590512304433229713789117927644143586092277750293910884717312503836910153525557232");
     // Objekt initialisieren mit priv key
     ElGamal elGamal_B = new ElGamal(El_p_B, El_g_B, El_y_B, El_x_B);
 
     // Bob empfängt Alice ElGamal pub key
     BigInteger El_p_A = new BigInteger(Com.receive(), 16); // R1
     BigInteger El_g_A = new BigInteger(Com.receive(), 16); // R2
     BigInteger El_y_A = new BigInteger(Com.receive(), 16); // R3
     // ElGamal Objekt ohne priv key bauen
     ElGamal elGamal_A = new ElGamal(El_p_A, El_g_A, El_y_A);
 
     // Bob sendet ElGamal public key an Alice
     Com.sendTo(0, elGamal_B.p.toString(16)); // S1
     Com.sendTo(0, elGamal_B.g.toString(16)); // S2
     Com.sendTo(0, elGamal_B.y.toString(16)); // S3
 
     // Vertrag einlesen
     String vertrag_B = vertragString(new File("vertrag.txt"));
 
     // Bob empfängt n, p_A und M
     int n = Integer.parseInt(Com.receive(), 16); // R4
     BigInteger p_A = new BigInteger(Com.receive(), 16); // R5
     BigInteger M = new BigInteger(Com.receive(), 16); // R6
 
     if (!p_A.isProbablePrime(100)) {
       System.out.println("Bobs p_B is keine Primzahl!");
       System.exit(1);
     }
 
     // eigene Primzahl M < p_B < 2^52
     BigInteger p_B = computePrimeBetween(M);
 
     // Bob sendet p_B an Alice
     Com.sendTo(0, p_B.toString(16)); // S4
 
     // Part 1 ---------------------------------------------------------------
 
     // Arrays bauen
     BigInteger[][] B = getDoubleArray(n, p_B);
     BigInteger[] C_B = get_C_Array(M, B, p_B);
 
     BigInteger[] C_A = new BigInteger[2 * n];
     for (int i = 0; i < C_B.length; i++) {
       C_A[i] = new BigInteger(Com.receive(), 16); // Ri
       System.out.println("Receive C_A[i]: " + C_A[i].toString(16));
       Com.sendTo(0, C_B[i].toString(16)); // Si
       System.out.println("Send C_B[i]: " + C_B[i].toString(16));
     }
 
     System.out.println("Bob:");
     for (int i = 0; i < C_A.length; i++) {
       System.out.println("C_A[i] = " + C_A[i]);
     }
 
     for (int i = 0; i < C_B.length; i++) {
       System.out.println("C_B[i] = " + C_B[i]);
     }
 
     // Empfange erklärung, text und signed hash von alice
     String erklaerung_A = Com.receive(); // R5
     String vertrag_A = Com.receive(); // R6
     BigInteger H_A_signed = new BigInteger(Com.receive(), 16); // R7
 
     System.out.println("Erklaerung_A: " + erklaerung_A);
     System.out.println("vertrag_A: " + vertrag_A);
     System.out.println("H_A_signed: " + H_A_signed);
 
     String erklaerung_B = erklaerungBob(C_B);
     String text_B = erklaerung_B + vertrag_B;
     BigInteger H_B = computeSHA(text_B);
     BigInteger H_B_signed = elGamal_B.sign(H_B);
 
     // Sende erklärung, text, signed hash
     Com.sendTo(0, erklaerung_B); // S5
     Com.sendTo(0, vertrag_B); // S6
     Com.sendTo(0, H_B_signed.toString(16)); // S7
 
     // check sig
     BigInteger H_A = computeSHA(erklaerung_A + vertrag_A);
 
     if (Com.receive() == "1") {
       System.out.println("Alice bricht ab!");
       System.exit(1);
     }
 
     if (elGamal_A.verify(H_A, H_A_signed)) {
       System.out.println("Jo is signed!");
       Com.sendTo(0, "0");
     } else {
       System.out.println("Is nich signed, Exit!");
       Com.sendTo(0, "1");
       System.exit(1);
     }
 
     // Geheimnisprotokoll
     BigInteger[][] A = geheimnisBob(n, 4, 51, B);
 
     BigInteger[] C2_A = get_C_Array(M, A, p_A);
     System.out.println("Bob: Checking Arrays !-o-o-o-o-o-!");
     if (check(C_A, C2_A)) {
       System.out.println("Bob: Alles klar!");
     } else {
       System.out.println("Bob: FUUUUUUUUU");
       System.exit(0);
     }
 
   }
 
   public BigInteger[][] geheimnisAlice(int n, int k, int m, BigInteger[][] geheimnisse) {
     // int n = 2; // n in {1,...,10}
     // int k = 2; // k in {0,...,7}
     // int wordlength = 4; // in {1,...,10}
 
     // int m = (int) Math.ceil(wordlength * (Math.log(36) / Math.log(2))); // bits of wordlength
     // System.out.println("m: " + m);
 
     // n, k, wordlength an Bob
     // Com.sendTo(1, Integer.toHexString(n)); // S1
     // Com.sendTo(1, Integer.toHexString(k)); // S2
     // Com.sendTo(1, Integer.toHexString(wordlength)); // S3
 
     Secret[][] a = new Secret[n][2];
     Secret[][] b = new Secret[n][2];
 
     // generiere alle a[i][j]
     for (int i = 0; i < n; i++) {
       for (int j = 0; j < 2; j++) {
         // BigInteger randomWord = BigIntegerUtil.randomBetween(ZERO, new
         // BigInteger("36").pow(wordlength));
         // System.out.println("randomWord: " + randomWord.toString(36));
         // a[i][j] = new Secret(randomWord, k, m);
         a[i][j] = new Secret(geheimnisse[i][j], k, m);
       }
     }
 
     // 1-OF-2-OBLIVIOUS
     // --------------------------------------------------------------------
     // send
     for (int i = 0; i < n; i++) {
       obliviousSend(1, a[i][0].getWord(), a[i][1].getWord());
     }
 
     // receive
     for (int i = 0; i < n; i++) {
       BigInteger word = obliviousReceive(1);
 
       // set beide secrets
       b[i][0] = new Secret(word, k, m);
       b[i][1] = new Secret(word, k, m);
     }
 
     // PROTOKOLL
     // --------------------------------------------------------------------
     int half = (int) (Math.pow(2, k + 1) / 2);
 
     for (int binaryBits = k + 1; binaryBits <= m; binaryBits++) {
 
       // lösche solange round in {0,...,(2^(k+1))/2)
       for (int round = 0; round < half; round++) {
         // lösche ein binary das kein prefix is und sende index davon
         for (int i = 0; i < n; i++) {
           for (int j = 0; j < 2; j++) {
             // System.out.println("A:");
             int index = a[i][j].removeRandomBinary();
             Com.sendTo(1, Integer.toHexString(index));
             a[i][j].debug();
           }
         }
 
         // streiche prefixe aus b mit empfangenem index weg
         for (int i = 0; i < n; i++) {
           for (int j = 0; j < 2; j++) {
             // System.out.println("B:");
             b[i][j].removeBinary(Integer.parseInt(Com.receive(), 16));
             b[i][j].debug();
           }
         }
       }
 
       // expandiere alle
       if (binaryBits < m) {
         for (int i = 0; i < n; i++) {
           for (int j = 0; j < 2; j++) {
             a[i][j].expandBinaries();
             b[i][j].expandBinaries();
           }
         }
       }
     }
 
     System.out.println("------------------------------------ Ende der Hauptschleife!");
 
     // am ende noch alle nicht-prefixe schicken
     for (int round = 0; round < (half - 1); round++) {
       for (int i = 0; i < n; i++) {
         for (int j = 0; j < 2; j++) {
           // System.out.println("A:");
           int index = a[i][j].removeRandomBinary();
           Com.sendTo(1, Integer.toHexString(index));
           a[i][j].debug();
         }
       }
     }
 
     // streiche prefixe aus b mit empfangenem index weg
     for (int round = 0; round < (half - 1); round++) {
       for (int i = 0; i < n; i++) {
         for (int j = 0; j < 2; j++) {
           // System.out.println("B:");
           b[i][j].removeBinary(Integer.parseInt(Com.receive(), 16));
           b[i][j].debug();
         }
       }
     }
 
     System.out.println("------------------------------------ Ende der Übertragungen!");
 
     // build array
     BigInteger[][] output = new BigInteger[n][2];
 
     for (int i = 0; i < n; i++) {
       for (int j = 0; j < 2; j++) {
         output[i][j] = b[i][j].getLastBinary();
 
         if (b[i][j].containsWord()) {
           System.out.println("Das folgende Wort wurde schon durch 1-of-2-Oblivious Transfer übertragen: " + b[i][j].getWord().toString(36));
         } else {
           System.out.println("Das folgende Wort wurde NICHT durch 1-of-2-Oblivious Transfer übertragen: " + b[i][j].binariesToString());
         }
       }
     }
 
     return output;
 
   }
 
   public BigInteger[][] geheimnisBob(int n, int k, int m, BigInteger[][] geheimnisse) {
     // n, k, wordlength von Alice
     // int n = Integer.parseInt(Com.receive(), 16);// R1
     // int k = Integer.parseInt(Com.receive(), 16); // R2
     // int wordlength = Integer.parseInt(Com.receive(), 16); // R3
 
     // int m = (int) Math.ceil(wordlength * (Math.log(36) / Math.log(2))); // bits of wordlength
 
     Secret[][] a = new Secret[n][2];
     Secret[][] b = new Secret[n][2];
 
     // generiere alle b[i][j]
     for (int i = 0; i < n; i++) {
       for (int j = 0; j < 2; j++) {
         // BigInteger randomWord = BigIntegerUtil.randomBetween(ZERO, new
         // BigInteger("36").pow(wordlength));
         // System.out.println("randomWord: " + randomWord.toString(36));
         // b[i][j] = new Secret(randomWord, k, m);
         b[i][j] = new Secret(geheimnisse[i][j], k, m);
       }
     }
 
     // 1-OF-2-OBLIVIOUS
     // --------------------------------------------------------------------
     // receive
     for (int i = 0; i < n; i++) {
       BigInteger word = obliviousReceive(0);
 
       // set beide secrets
       a[i][0] = new Secret(word, k, m);
       a[i][1] = new Secret(word, k, m);
     }
 
     // send
     for (int i = 0; i < n; i++) {
       obliviousSend(0, b[i][0].getWord(), b[i][1].getWord());
     }
 
     // PROTOKOLL
     // --------------------------------------------------------------------
 
     int half = (int) (Math.pow(2, k + 1) / 2);
 
     for (int binaryBits = k + 1; binaryBits <= m; binaryBits++) {
       // lösche solange round in {0,...,(2^(k+1))/2)
       for (int round = 0; round < half; round++) {
         // streiche prefixe aus a mit empfangenem index weg
         for (int i = 0; i < n; i++) {
           for (int j = 0; j < 2; j++) {
             a[i][j].removeBinary(Integer.parseInt(Com.receive(), 16));
             a[i][j].debug();
           }
         }
 
         // lösche ein binary das kein prefix is und sende index davon
         for (int i = 0; i < n; i++) {
           for (int j = 0; j < 2; j++) {
             int index = b[i][j].removeRandomBinary();
             Com.sendTo(0, Integer.toHexString(index));
             b[i][j].debug();
           }
         }
       }
 
       // expandiere alle
       if (binaryBits < m) {
         for (int i = 0; i < n; i++) {
           for (int j = 0; j < 2; j++) {
             a[i][j].expandBinaries();
             b[i][j].expandBinaries();
           }
         }
       }
     }
 
     System.out.println("------------------------------------ Ende der Hauptschleife!");
 
     // am ende noch alle nicht-prefixe schicken
     for (int round = 0; round < (half - 1); round++) {
       for (int i = 0; i < n; i++) {
         for (int j = 0; j < 2; j++) {
           // System.out.println("B:");
           int index = b[i][j].removeRandomBinary();
           Com.sendTo(0, Integer.toHexString(index));
           b[i][j].debug();
         }
       }
     }
 
     // streiche prefixe aus b mit empfangenem index weg
     for (int round = 0; round < (half - 1); round++) {
       for (int i = 0; i < n; i++) {
         for (int j = 0; j < 2; j++) {
           // System.out.println("A:");
           a[i][j].removeBinary(Integer.parseInt(Com.receive(), 16));
           a[i][j].debug();
         }
       }
     }
 
     System.out.println("------------------------------------ Ende der Übertragungen!");
 
     // build array
     BigInteger[][] output = new BigInteger[n][2];
 
     for (int i = 0; i < n; i++) {
       for (int j = 0; j < 2; j++) {
        output[i][j] = b[i][j].getLastBinary();
 
         if (a[i][j].containsWord()) {
           System.out.println("Das folgende Wort wurde schon durch 1-of-2-Oblivious Transfer übertragen: " + a[i][j].getWord().toString(36));
         } else {
           System.out.println("Das folgende Wort wurde NICHT durch 1-of-2-Oblivious Transfer übertragen: " + a[i][j].binariesToString());
         }
       }
     }
 
     return output;
 
   }
 
   public void obliviousSend(int sendTo, BigInteger M_0, BigInteger M_1) {
     System.out.println("Oblivious Transfer");
 
     // Hard coded messages M_0 and M_1
     BigInteger[] M = new BigInteger[2];
     M[0] = M_0;
     M[1] = M_1;
 
     if (betray) {
       M[1] = M[0];
     }
 
     // Hard coded ElGamal
     BigInteger p_A = new BigInteger("7789788965135663714690749102453072297748091458564354001035945418057913886819451721947477667556269500246451521462308030406227237346483679855991947569361139");
     BigInteger g_A = new BigInteger("6064211169633122201619014531987050083527855665630754543345421103270545526304595525644519493777291154802011605984321393354028831270292432551124003674426238");
     BigInteger y_A = new BigInteger("3437627792030969437324738830672923365331058766427964788898937390314623633227168012908665090706697391878208866573481456022491841700034626290242749535475902");
     // private:
     BigInteger x_A = new BigInteger("3396148360179732969395840357777168909721385739804535508222449486018759668590512304433229713789117927644143586092277750293910884717312503836910153525557232");
     // Objekt initialisieren mit priv key
     ElGamal elGamal_A = new ElGamal(p_A, g_A, y_A, x_A);
 
     BigInteger p = elGamal_A.p;
 
     // Alice sendet ElGamal public key an Bob
     Com.sendTo(sendTo, elGamal_A.p.toString(16)); // S1
     Com.sendTo(sendTo, elGamal_A.g.toString(16)); // S2
     Com.sendTo(sendTo, elGamal_A.y.toString(16)); // S3
 
     // Alice wählt zufällig zwei Nachrichten m_0, m_1 in Z_p, 1 <= m < p
     BigInteger[] m = new BigInteger[2];
     m[0] = BigIntegerUtil.randomBetween(ONE, p);
     m[1] = BigIntegerUtil.randomBetween(ONE, p);
     // System.out.println("m_0: " + m[0]);
     // System.out.println("m_1: " + m[1]);
 
     // Alice sendet m_0, m_1 an Bob
     Com.sendTo(sendTo, m[0].toString(16)); // S4
     Com.sendTo(sendTo, m[1].toString(16)); // S5
 
     // Alice empfängt q
     BigInteger q = new BigInteger(Com.receive(), 16); // R6
 
     // Alice berechnet k_0', k_1', hier k_A[0] und k_A[1] genannt
     BigInteger[] k_strich = new BigInteger[2];
     for (int i = 0; i < 2; i++) {
       k_strich[i] = elGamal_A.decipher((q.subtract(m[i])).mod(p.multiply(p))); // D_A((q-m_i) mod
                                                                                // p^2)
     }
     // System.out.println("k_strich[0]: " + k_strich[0]);
     // System.out.println("k_strich[1]: " + k_strich[1]);
 
     // zufällig s wählen
     int s = BigIntegerUtil.randomBetween(ZERO, TWO).intValue();
     // System.out.println("s: " + s);
 
     BigInteger[] send = new BigInteger[2];
     send[0] = M[0].add(k_strich[s]).mod(p);
     send[1] = M[1].add(k_strich[s ^ 1]).mod(p);
 
     // System.out.println("send_0: " + send[0]);
     // System.out.println("send_1: " + send[1]);
 
     int r = -1;
     if (betray) { // try to find right r :D
       r = BigIntegerUtil.randomBetween(ZERO, TWO).intValue();
       // System.out.println("guessed r: " + r);
     }
 
     // Signatur berechnen
     BigInteger[] S = new BigInteger[2];
     for (int i = 0; i < 2; i++) {
       if (betray) {
         if (i == r) { // gefälschte signatur
           S[i] = BigIntegerUtil.randomBetween(BigIntegerUtil.TWO, p.multiply(p));
         } else {
           S[i] = elGamal_A.sign(k_strich[i]);
         }
       } else { // no betraying
         S[i] = elGamal_A.sign(k_strich[i]);
       }
     }
     // System.out.println("S_0: " + S[0]);
     // System.out.println("S_1: " + S[1]);
 
     // Alice sendet send_0, send_1, s, S[0], S[1]
     Com.sendTo(sendTo, send[0].toString(16)); // S7
     Com.sendTo(sendTo, send[1].toString(16)); // S8
     Com.sendTo(sendTo, s + ""); // S9
     Com.sendTo(sendTo, S[0].toString(16)); // S10
     Com.sendTo(sendTo, S[1].toString(16)); // S11
   }
 
   public BigInteger obliviousReceive(int sendTo) {
     System.out.println("Oblivious Transfer");
 
     // Bob empfängt Alice ElGamal pub key
     BigInteger p_A = new BigInteger(Com.receive(), 16); // R1
     BigInteger g_A = new BigInteger(Com.receive(), 16); // R2
     BigInteger y_A = new BigInteger(Com.receive(), 16); // R3
     // ElGamal Objekt ohne priv key bauen
     ElGamal elGamal_A = new ElGamal(p_A, g_A, y_A);
 
     BigInteger p = elGamal_A.p;
 
     // Bob empfängt m_0 und m_1
     BigInteger[] m = new BigInteger[2];
     m[0] = new BigInteger(Com.receive(), 16); // R4
     m[1] = new BigInteger(Com.receive(), 16); // R5
 
     // Bob wählt zufällig ein r in {0,1} und k in Z_p
     int r = BigIntegerUtil.randomBetween(ZERO, TWO).intValue();
     // System.out.println("r: " + r);
     BigInteger k = BigIntegerUtil.randomBetween(ONE, p);
     // System.out.println("k: " + k);
 
     // Bob berechnet q
     BigInteger q = elGamal_A.encipher(k).add(m[r]); // E_A(k) + m_r
     q = q.mod(p.multiply(p)); // mod p^2
     // System.out.println("q: " + q);
     // Bob sendet q
     Com.sendTo(sendTo, q.toString(16)); // S6
 
     // Bob empfängt send_0, send_1, s, S[0], S[1]
     BigInteger[] send = new BigInteger[2];
     send[0] = new BigInteger(Com.receive(), 16); // R7
     send[1] = new BigInteger(Com.receive(), 16); // R8
     int s = Integer.valueOf(Com.receive()); // R9
     BigInteger[] S = new BigInteger[2];
     S[0] = new BigInteger(Com.receive(), 16); // R10
     S[1] = new BigInteger(Com.receive(), 16); // R11
     // System.out.println("S_0: " + S[0]);
     // System.out.println("S_1: " + S[1]);
 
     // System.out.println("s: " + s);
     // System.out.println("r: " + r);
 
     BigInteger M = send[s ^ r].subtract(k).mod(p); // M = M_{s xor r}
 
     BigInteger k_quer = send[s ^ r ^ 1].subtract(M).mod(p);
 
     BigInteger k_quer2 = send[s ^ r].subtract(M).mod(p);
 
     // System.out.println("S[r^1]: " + S[r ^ 1]);
     // System.out.println("k_dach: " + k_quer);
 
     if (elGamal_A.verify(k_quer, S[r ^ 1])) {
       System.out.println("Betrug!!!!!!!!");
       System.exit(0);
 
       return null;
     } else {
       if (elGamal_A.verify(k_quer2, S[r])) {
         System.out.println("Alles OK!");
         System.out.println("Message choosen: M_" + (s ^ r) + ": " + M);
 
         return M;
       } else {
         System.out.println("Betrug!!!!!!!!");
         System.exit(0);
 
         return null;
       }
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
 
   /**
    * Tafel: 1.)1.3
    */
   private BigInteger computeSHA(String text) {
     MessageDigest sha = null;
     byte[] digest;
 
     try {
       sha = MessageDigest.getInstance("SHA");
     } catch (Exception e) {
       System.out.println("Could not create message digest! Exception " + e.toString());
       System.exit(0);
     }
 
     sha.update(text.getBytes());
     digest = sha.digest();
 
     return new BigInteger(digest);
   }
 
   /**
    * Tafel: 1.)1.3
    */
   private String erklaerungAlice(BigInteger C[]) {
     String C_String = "";
     for (int i = 0; i < C.length; i++) {
       C_String += C[i].toString(16) + ", ";
     }
 
     String a = "Die Symbole A'_i,j bezeichnen Loesungen der zugehoerigen S-Puzzles ";
     String b = C_String + ", i.element{1,...,n}, j.element{1,2}. ";
     String c = "Der untenstehende Vertrag ist von mir unterzeichnet, ";
     String d = "wenn Bob fuer ein i.element{1,...,n} die beiden Schluessel ";
     String e = "A'_i,1 und A'_i,2 nennen kann, d.h., wenn er die Loesung ";
     String f = "des (i,1)-ten und (i,2)-ten Puzzles kennt.";
 
     return a + b + c + d + e + f;
   }
 
   /**
    * Tafel: 1.)1.3
    */
   private String erklaerungBob(BigInteger[] C) {
     String C_String = "";
     for (int i = 0; i < C.length; i++) {
       C_String += C[i].toString(16) + ", ";
     }
 
     String a = "Die Symbole A'_i,j bezeichnen Loesungen der zugehoerigen S-Puzzles ";
     String b = C_String + ", i.element{1,...,n}, j.element{1,2}. ";
     String c = "Der untenstehende Vertrag ist von mir unterzeichnet, ";
     String d = "wenn Alice fuer ein i.element{1,...,n} die beiden Schluessel ";
     String e = "B'_i,1 und B'_i,2 nennen kann, d.h., wenn er die Loesung ";
     String f = "des (i,1)-ten und (i,2)-ten Puzzles kennt.";
 
     return a + b + c + d + e + f;
   }
 
   private String vertragString(File file) {
 
     String output = "";
     try {
       BufferedReader in = new BufferedReader(new FileReader(file));
 
       String inputLine;
       while ((inputLine = in.readLine()) != null) {
         output += inputLine;
       }
     } catch (FileNotFoundException e) {
       System.out.println("Dude! Vertrag nicht vorhanden!");
       System.exit(0);
       e.printStackTrace();
     } catch (IOException e) {
       System.out.println("Dude! Vertrag nicht vorhanden!");
       System.exit(0);
       e.printStackTrace();
     }
 
     return output;
   }
 
   /*
    * Testet ob gcd(val1, val2) = 1 ist und liefert TRUE falls ja
    */
   private boolean checkGGT(BigInteger val1, BigInteger val2) {
     boolean result = false;
 
     BigInteger gcd = val1.gcd(val2);
 
     if (gcd.compareTo(ONE) == 0) {
       result = true;
     }
 
     return result;
   }
 
   /**
    * Tafel: 1.)1.1
    */
   private BigInteger[][] getDoubleArray(int n, BigInteger p) { // p_A oder p_B
     BigInteger[][] array = new BigInteger[n][2];
 
     for (int i = 0; i < n; i++) {
       boolean check = false;
       while (!check) {
         array[i][0] = BigIntegerUtil.randomSmallerThan(p);
         check = checkGGT(array[i][0], p.subtract(ONE));
       }
       check = false;
       while (!check) {
         array[i][1] = BigIntegerUtil.randomSmallerThan(p);
         check = checkGGT(array[i][1], p.subtract(ONE));
       }
     }
 
     return array;
   }
 
   /**
    * Tafel: 1.)1.1
    */
   private BigInteger[] get_C_Array(BigInteger M, BigInteger[][] array, BigInteger p) {
     int length1 = array.length;
     int length2 = array[0].length;
     BigInteger[] output = new BigInteger[length1 * length2];
 
     int counter = 0;
     for (int i = 0; i < length1; i++) {
       for (int j = 0; j < length2; j++) {
         output[counter++] = M.modPow(array[i][j], p);
       }
     }
 
     return output;
   }
 
   /**
    * Tafel: 0.)3.1
    */
   private BigInteger computeMessage(BigInteger p_A) {
     int bl = p_A.bitLength();
     int shift = 0;
 
     if (bl > 10) { // for wesentlich kleiner
       shift = bl - 10;
     }
 
     return BigIntegerUtil.randomSmallerThan(p_A.shiftRight(shift));
   }
 
   /**
    * Tafel: 0.)3.1 , 0.)4.1
    */
   private BigInteger computePrime() {
     Random rnd = new Random();
 
     int bitlength = rnd.nextInt(51); // random < 52
 
     return new BigInteger(bitlength, 100, rnd);
   }
 
   /**
    * Tafel: 0.)4.2
    */
   private BigInteger computePrimeBetween(BigInteger M) { // p_B, Modulus 2^52
     BigInteger output = computePrime();
 
     // Bedingung: M < output < 2^52
     while ((output.compareTo(M) == -1) || (output.compareTo(M) == 0)) {
       output = computePrime();
     }
 
     return output;
 
   }
 
   private boolean check(BigInteger[] ar1, BigInteger[] ar2) {
 
     for (int i = 0; i < ar1.length; i++) {
       BigInteger val1 = ar1[i];
       BigInteger val2 = ar2[i];
 
       System.out.println(val1 + " 0^~^0 " + val2);
       if ((val1.compareTo(val2)) == 0) {
         // yay!
       } else {
         return false;
       }
     }
     return true;
   }
 
 }
