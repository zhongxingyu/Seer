 public class Curupira implements BlockCipher { 
 
     private int keyBits;
     private int R;  // Number of block cypher rounds.
     private int t;  // see end of page 5.
     private byte[] cipherKey;
     private byte[] sBoxTable;
     private byte[] xTimesTable;
     private byte[][] encryptionRoundKeys;
     private byte[][] decryptionRoundKeys;
 
     /**************************************************************
      *                   ELEMENTARY OPERATIONS 
      **************************************************************/
 
     /**
      * Fills xTimesTable
      *
      */
     private void initXTimesTable() {
         xTimesTable = new byte[256];
         int u, d;
         for (u = 0x00; u <= 0xFF; ++u) {
             d = u << 1;
             if (d >= 0x100)
                 d = d ^ 0x14D;
             xTimesTable[u] = (byte) d;
         }
     }
 
     /**
      * Fills sBoxTable
      *
      */
     private void initSBoxTable() {
         int[] P = {0x3, 0xF, 0xE, 0x0, 0x5, 0x4, 0xB, 0xC, 0xD, 0xA, 0x9, 0x6,
             0x7, 0x8, 0x2, 0x1};
         int[] Q = {0x9, 0xE, 0x5, 0x6, 0xA, 0x2, 0x3, 0xC, 0xF, 0x0, 0x4, 0xD,
             0x7, 0xB, 0x1, 0x8};
         int u, uh1, uh2, ul1, ul2;
         sBoxTable = new byte[256];
         for (u = 0x00; u <= 0xFF; ++u) {
             uh1 = P[(u >>> 4) & 0xF];
             ul1 = Q[u & 0xF];
             uh2 = Q[(uh1 & 0xC) ^ ((ul1 >>> 2) & 0x3)];
             ul2 = P[((uh1 << 2) & 0xC) ^ (ul1 & 0x3)];
             uh1 = P[(uh2 & 0xC) ^ ((ul2 >> 2) & 0x3)];
             ul1 = Q[((uh2 << 2) & 0xC) ^ (ul2 & 0x3)];
             sBoxTable[u] = (byte) ((uh1 << 4) ^ ul1);
         }
     }
 
     /**
      * Multiplies the polynomial u by x.
      *
      * @param   u     a byte representing a polynomial.
      * @return        a byte representing u * 0b10 (i.e. x).
      *
      */
     private byte xTimes(byte u) {
         return xTimesTable[u & 0xFF];
     }
 
     /**
      * Multiplies the polynomial u by c(x), c(x) = x**4 + x**3 + x**2.
      *
      * @param   u     a byte.
      * @return        a byte representing the 
      *                polynomial u * c(x).
      *
      */
     private byte cTimes(byte u) {
         // see page 13, item 5.
         return xTimes(xTimes((byte) (xTimes((byte) (xTimes(u) ^ u)) ^ u)));
     }
 
     /**
      * Multiplies the matrix D, by the vector aj
      *
      * @param   a     3x4 matrix.
      * @param   j     Rank of the input column.
      * @param   b     Resulting 3x4 matrix.
      * @return        resulting height 3 column vector.
      *
      */
     private void dTimesa(byte[] a, int j, byte[] b) {
         // see page 13.
         int d = 3 * j;  // Column delta
         byte v = xTimes((byte)(a[0 + d] ^ a[1 + d] ^ a[2 + d]));
         byte w = xTimes(v);
         b[0 + d] = (byte)(a[0 + d] ^ v);
         b[1 + d] = (byte)(a[1 + d] ^ w);
         b[2 + d] = (byte)(a[2 + d] ^ v ^ w);
     }
 
     /**
      * Multiplies the matrix E (or it's inverse), by the vector aj.
      *
      * @param   a     3x4 matrix.
      * @param   j     Rank of the input column.
      * @param   b     Resulting 3x4 matrix.
      * @param   e     pass true for using E or false for using 
      *                the inverse of E.
      * @return        resulting height 3 column vector.
      *
      */
     private void eTimesa(byte[] a, int j, byte[] b, boolean e) {
         // see page 14.
         int d = 3 * j;  // Column delta.
         byte v = (byte)(a[0 + d] ^ a[1 + d] ^ a[2 + d]);
         if (e) {
             v = cTimes(v);
         } else {
             v = (byte)(cTimes(v) ^ v);
         }
         b[0 + d] = (byte)(a[0 + d] ^ v);
         b[1 + d] = (byte)(a[1 + d] ^ v);
         b[2 + d] = (byte)(a[2 + d] ^ v);
     }
 
     /**
      * Gets the byte mapped to u, through the sBoxTable 
      *
      * @param   u     a byte representing a polynomial.
      * @return        a byte representing a mapped polynomial.
      *
      */
     private byte sBox(byte u) {
         return sBoxTable[u & 0xFF];
     }
 
     /**************************************************************
      *                   TRANSFORMATION LAYERS
      **************************************************************/
 
     /**
      * Applies the non-linear layer to matrix a.
      *
      * @param   a     3x4 matrix.
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] applyNonLinearLayer(byte[] a) {
         // see page 6.
         byte[] b = new byte[12];
         for (int i = 0; i < 12; i++) {
             b[i] = sBox(a[i]);
         }
         return b;
     }
 
     /**
      * Applies the permutation layer to matrix a.
      *
      * @param   a     3x4 matrix.
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] applyPermutationLayer(byte[] a) {
         // see page 7.
         byte[] b = new byte[12];
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 4; j++) {
                 b[i + 3 * j] = a[i + 3 * (i ^ j)];
             }
         }
         return b;
     }
 
     /**
      * Applies the linear diffusion layer to matrix a.
      *
      * @param   a     3x4 matrix.
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] applyLinearDiffusionLayer(byte[] a) {
         // see page 7.
         byte[] b = new byte[12];
         for (int j = 0; j < 4; j++) {
             dTimesa(a, j, b);
         }
         return b;
     }
 
     /**
      * Applies the key addition layer to matrix a.
      *
      * @param   a     3x4 matrix.
      * @param   kr    3x4 matrix representing subkey.
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] applyKeyAddition(byte[] a, byte[] kr) {
         // see page 7.
         byte[] b = new byte[12];
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 4; j++) {
                 b[i + 3 * j] = (byte)(a[i + 3 * j] ^ kr[i + 3 * j]);
             }
         }
         return b;
     }
 
     /**************************************************************
      *                   ROUND FUNCTIONS
      **************************************************************/
 
     /**
      * Performs a Curupira whitening round on matrix a
      *
      * @param   a     3x4 matrix (from previous round).
      * @param   k0    k(0) round key.
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] performWhiteningRound(byte[] a, byte[] k0) {
         // see page 9.
         return applyKeyAddition(a, k0);
     }
 
     /**
      * Performs a Curupira last round on matrix a
      *
      * @param   a     3x4 matrix (from previous round).
      * @param   kR    k(R) round key.
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] performLastRound(byte[] a, byte[] kR) {
         // see page 9.
         return applyKeyAddition(applyPermutationLayer(applyNonLinearLayer(a)), kR);
     }
 
     /**
      * Performs a Curupira "intermediary" round on matrix a
      *
      * @param   a     3x4 matrix (from previous round).
      * @param   kr    k(r) round key.
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] performRound(byte[] a, byte[] kr) {
         // see page 9.
         return applyKeyAddition(applyLinearDiffusionLayer(applyPermutationLayer(applyNonLinearLayer(a))), kr);
     }
 
     /**
      * Performs a Curupira unkeyed round on matrix a
      *
      * @param   a     3x4 matrix (from previous round).
      * @return        Resulting 3x4 matrix.
      *
      */
     private byte[] performUnkeyedRound(byte[] a) {
         return applyLinearDiffusionLayer(applyPermutationLayer(applyNonLinearLayer(a)));
     }
 
     /**************************************************************
      *                   CALCULATION OF ROUND KEYS
      **************************************************************/
 
     /**
      * Calculates schedule constant matrix of s.
      *
      * @param   s       A positive integer.
      * @return          Resulting 3x2t schedule constants matrix.
      *
      */
     public byte[] calculateScheduleConstant(int s) {
         // see page 7
         int t = this.keyBits / 48;
         byte[] q = new byte[3 * 2 * t];
         if (s == 0) {
           return q;
         } 
         // For i = 0
         for (int j = 0; j < 2 * t; j++) {
             q[3 * j] = sBox((byte)(2 * t * (s - 1) + j));
             // Note: 2t(s-1) + j is at most 144 for 192 bits cipher key.
         }
         // For i > 0
         for (int i = 1; i < 3; i++) {
             for (int j = 0; j < 2 * t; j++) {
                 q[i + 3 * j] = 0;
             }
         }            
         return q;
     }
 
     /**
      * Applies constant addition to subkey Kr.
      *
      * @param   Kr              3x2t matrix representing the Kr subkey.
      * @param   subkeyRank      Subkey rank.
      * 
      * @return                  Resulting 3x2t matrix.
      *
      */
     public byte[] applyConstantAddition(byte[] Kr, int subkeyRank) {
         // see page 8
         byte[] b = new byte[3 * 2 * t];
         // Do constant addition
         byte[] q = calculateScheduleConstant(subkeyRank);
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 2 * t; j++) {
                 b[i + 3 * j] = (byte)(Kr[i + 3 * j] ^ q[i + 3 * j]);
             }
         }
         return b;
     }
 
     /**
      * Applies cyclic shift to matrix a.
      *
      * @param   a       3x2t matrix.
      * @return          Resulting 3x2t matrix.
      *
      */
     public byte[] applyCyclicShift(byte[] a) {
         // see page 8
         byte[] b = new byte[3 * 2 * t];
         for (int j = 0; j < 2 * t; j++) {
             // For i = 0.
             b[3 * j] = a[3 * j];
             // For i = 1.
             b[1 + 3 * j] = a[1 + 3 * ((j + 1) % (2 * t))];
             // For i = 2.
             if (j > 0) {
                 b[2 + 3 * j] = a[2 + 3 * ((j - 1) % (2 * t))];
                 // Note that (0 - 1) % 2t would give -1.
             } else {
                 b[2] = a[2 + 3 * (2 * t - 1)];
             }
         }
         return b;
     }
 
     /**
      * Applies linear diffusion to matrix a.
      *
      * @param   a       3x2t matrix.
      * @return          Resulting 3x2t matrix.
      *
      */
     public byte[] applyLinearDiffusion(byte[] a) {
         // see page 8
         byte[] b = new byte[3 * 2 * t];
         for (int j = 0; j < 2 * t; j++) {
             eTimesa(a, j, b, true);
         }
         return b;
     }
 
     /**
      * Gets subkey for the current round given subkey from the previous round.
      *
      * @param   kr      3x4 matrix representing the Kr subkey.
      * @return          3x4 matrix representing the Kr+1 subkey.
      *
      */
     public byte[] calculateNextSubkey(byte[] Kr, int subkeyRank) {
         // see pages 7, 8 and 9.
         return applyLinearDiffusion(applyCyclicShift(applyConstantAddition(Kr, subkeyRank)));
     }
   
     /**
      * Selects the round key that corresponds to the given subkey.
      *
      * @param   kr      3x4 matrix representing the Kr subkey.
      * @return          3x4 matrix representing the kr round key.
      *
      */
     public byte[] selectRoundKey(byte[] Kr) {
         // see page 9.
         byte[] kr = new byte[12];
         // For i = 0.
         for (int j = 0; j < 4; j++) {
             kr[3 * j] = sBox(Kr[3 * j]);
         }
         // For i > 0.
         for (int i = 1; i < 3; i++) {
             for (int j = 0; j < 4; j++) {
                 kr[i + 3 * j] = Kr[i + 3 * j];
             }
         }
         return kr;
     }
 
     /**
      * Calculates both encryption and decryption round keys.
      *
      */
     private void calculateRoundKeys() {
         // see pages 9 and 10.
         this.encryptionRoundKeys = new byte[this.R + 1][12];
         this.decryptionRoundKeys = new byte[this.R + 1][12];
         byte[] Kr = this.cipherKey;
         byte[] kr = selectRoundKey(Kr);
         this.encryptionRoundKeys[0] = kr;
         for (int r = 1; r <= this.R; r++) {
             Kr = calculateNextSubkey(Kr, r);
             kr = selectRoundKey(Kr);
             this.encryptionRoundKeys[r] = kr;
             this.decryptionRoundKeys[this.R - r] = applyLinearDiffusionLayer(kr);
         }
         this.decryptionRoundKeys[0] = this.encryptionRoundKeys[this.R];
         this.decryptionRoundKeys[this.R] = this.encryptionRoundKeys[0];
     }
 
     /**************************************************************
      *                   CURUPIRA ENCRYPTION/DECRIPTION
      **************************************************************/
 
     /** 
      * Encrypt/Decrypts (depending on the keys) exactly one block of plaintext. 
      * 
      * @param  mBlock      plaintext block.
      * @param  cBlock      ciphertext block. 
      */ 
     public void processBlock(byte[] mBlock, byte[] cBlock, byte[][] roundKeys) {
         // see page 9.
         byte[] cBlockTmp;
         cBlockTmp = performWhiteningRound(mBlock, roundKeys[0]);
         for (int r = 1; r <= this.R - 1; r++) {
             cBlockTmp = performRound(cBlockTmp, roundKeys[r]);
         }
         cBlockTmp = performLastRound(cBlockTmp, roundKeys[this.R]);
         // Copy results to given output reference
         for (int k = 0; k < 12; k++) {
           cBlock[k] = cBlockTmp[k];
         }
     }
 
     /**************************************************************
      *                   PUBLIC INTERFACE
      **************************************************************/
 
     /**
      * Initializes xTimesTable and sBoxTable.
      *
      */
     public Curupira() {
         initXTimesTable();
         initSBoxTable();  
     }
 
     /** 
      * Curupira's block size is always 96 bits. 
      */ 
     public int blockBits() {
         return 96;
     } 
 
     /** 
      * This is set through the makeKey method; 
      * Curupira accepts keys of size 96, 144 and 192 bits. 
      */ 
     public int keyBits() {
         return this.keyBits;
     } 
 
     /** 
      * Setup the cipher key for this block cipher instance. 
      * 
      * @param  cipherKey   the cipher key of keyBits size. 
      *                     It must be serialized in column major order.
      * @param  keyBits     size of the cipher key in bits.
      *                     This must be either 96, 144 or 192.
      */ 
     public void makeKey(byte[] cipherKey, int keyBits) {
         this.keyBits = keyBits;
         this.t = keyBits / 48;
         this.cipherKey = cipherKey;
         switch (keyBits) {
             case 96:
                 this.R = 10;  // See end of page 9.
                 break;
             case 144:
                 this.R = 14;
                 break;
             case 192:
                 this.R = 18;
                 break;
         }
         calculateRoundKeys();
     }
 
     /** 
      * Encrypt exactly one block of plaintext. 
      * 
      * @param  mBlock      plaintext block. 
      * @param  cBlock      ciphertext block. 
      */ 
     public void encrypt(byte[] mBlock, byte[] cBlock) {
         // see page 9.
         processBlock(mBlock, cBlock, this.encryptionRoundKeys);
     }
 
     /** 
      * Decrypt exactly one block of ciphertext. 
      * 
      * @param  cBlock      ciphertext block. 
      * @param  mBlock      plaintext block. 
      */ 
     public void decrypt(byte[] cBlock, byte[] mBlock) {
         // see page 10.
        processBlock(mBlock, cBlock, this.decryptionRoundKeys);
     }
 
     /** 
      * Applies a square-complete transform to exactly
      * one block of ciphertext, by performing 4 unkeyed 
      * Curupira rounds. 
      * 
      * @param  cBlock      ciphertext block. 
      * @param  mBlock      plaintext block. 
      */ 
     public void sct(byte[] cBlock, byte[] mBlock) {
         byte[] mBlockTmp = new byte[12];
         mBlockTmp = performUnkeyedRound(cBlock);
         for (int r = 0; r < 3; r++) {
             mBlockTmp = performUnkeyedRound(mBlockTmp);
         }
         // Copy results to given output reference
         for (int k = 0; k < 12; k++) {
             mBlock[k] = mBlockTmp[k];
         }
     }
 } 
 
