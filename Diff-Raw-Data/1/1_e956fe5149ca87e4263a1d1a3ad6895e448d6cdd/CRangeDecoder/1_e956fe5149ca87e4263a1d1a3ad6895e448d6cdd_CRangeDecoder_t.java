 package LZMA;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 class CRangeDecoder
 {
   static final int kNumTopBits = 24;
   static final int kTopValue = 16777216;
   static final int kTopValueMask = -16777216;
   static final int kNumBitModelTotalBits = 11;
   static final int kBitModelTotal = 2048;
   static final int kNumMoveBits = 5;
   InputStream inStream;
   int Range;
   int Code;
   byte[] buffer;
   int buffer_size;
   int buffer_ind;
   static final int kNumPosBitsMax = 4;
   static final int kNumPosStatesMax = 16;
   static final int kLenNumLowBits = 3;
   static final int kLenNumLowSymbols = 8;
   static final int kLenNumMidBits = 3;
   static final int kLenNumMidSymbols = 8;
   static final int kLenNumHighBits = 8;
   static final int kLenNumHighSymbols = 256;
   static final int LenChoice = 0;
   static final int LenChoice2 = 1;
   static final int LenLow = 2;
   static final int LenMid = 130;
   static final int LenHigh = 258;
   static final int kNumLenProbs = 514;
 
   CRangeDecoder(InputStream paramInputStream)
     throws IOException
   {
     this.buffer = new byte[16384];
     this.inStream = paramInputStream;
     this.Code = 0;
     this.Range = -1;
     for (int i = 0; i < 5; i++)
       this.Code = (this.Code << 8 | Readbyte());
   }
 
   int Readbyte() throws IOException {
     if (this.buffer_size == this.buffer_ind) {
       this.buffer_size = this.inStream.read(this.buffer);
       this.buffer_ind = 0;
 
       if (this.buffer_size < 1)
         throw new LzmaException("LZMA : Data Error");
     }
     return this.buffer[(this.buffer_ind++)] & 0xFF;
   }
 
   int DecodeDirectBits(int paramInt) throws IOException {
     int i = 0;
     for (int j = paramInt; j > 0; j--) {
       this.Range >>>= 1;
       int k = this.Code - this.Range >>> 31;
       this.Code -= (this.Range & k - 1);
       i = i << 1 | 1 - k;
 
       if (this.Range >= 16777216)
         continue;
       this.Code = (this.Code << 8 | Readbyte());
       this.Range <<= 8;
     }
 
     return i;
   }
 
   int BitDecode(int[] paramArrayOfInt, int paramInt) throws IOException {
     int i = (this.Range >>> 11) * paramArrayOfInt[paramInt];
     if ((this.Code & 0xFFFFFFFF) < (i & 0xFFFFFFFF))
     {
       this.Range = i;
       paramArrayOfInt[paramInt] += (2048 - paramArrayOfInt[paramInt] >>> 5);
 
       if ((this.Range & 0xFF000000) == 0) {
         this.Code = (this.Code << 8 | Readbyte());
         this.Range <<= 8;
       }
       return 0;
     }
     this.Range -= i;
     this.Code -= i;
     paramArrayOfInt[paramInt] -= (paramArrayOfInt[paramInt] >>> 5);
 
     if ((this.Range & 0xFF000000) == 0) {
       this.Code = (this.Code << 8 | Readbyte());
       this.Range <<= 8;
     }
     return 1;
   }
 
   int BitTreeDecode(int[] paramArrayOfInt, int paramInt1, int paramInt2) throws IOException
   {
     int i = 1;
     for (int j = paramInt2; j > 0; j--) {
       i = i + i + BitDecode(paramArrayOfInt, paramInt1 + i);
     }
     return i - (1 << paramInt2);
   }
 
   int ReverseBitTreeDecode(int[] paramArrayOfInt, int paramInt1, int paramInt2) throws IOException {
     int i = 1;
     int j = 0;
 
     for (int k = 0; k < paramInt2; k++) {
       int m = BitDecode(paramArrayOfInt, paramInt1 + i);
       i = i + i + m;
       j |= m << k;
     }
     return j;
   }
 
   byte LzmaLiteralDecode(int[] paramArrayOfInt, int paramInt) throws IOException {
     int i = 1;
     do
       i = i + i | BitDecode(paramArrayOfInt, paramInt + i);
     while (i < 256);
 
     return (byte)i;
   }
 
   byte LzmaLiteralDecodeMatch(int[] paramArrayOfInt, int paramInt, byte paramByte) throws IOException {
     int i = 1;
     do {
       int j = paramByte >> 7 & 0x1;
       paramByte = (byte)(paramByte << 1);
       int k = BitDecode(paramArrayOfInt, paramInt + (1 + j << 8) + i);
       i = i << 1 | k;
 
       if (j != k) {
         while (i < 256) {
           i = i + i | BitDecode(paramArrayOfInt, paramInt + i);
         }
       }
     }
     while (i < 256);
 
     return (byte)i;
   }
 
   int LzmaLenDecode(int[] paramArrayOfInt, int paramInt1, int paramInt2)
     throws IOException
   {
     if (BitDecode(paramArrayOfInt, paramInt1 + 0) == 0) {
       return BitTreeDecode(paramArrayOfInt, paramInt1 + 2 + (paramInt2 << 3), 3);
     }
 
     if (BitDecode(paramArrayOfInt, paramInt1 + 1) == 0) {
       return 8 + BitTreeDecode(paramArrayOfInt, paramInt1 + 130 + (paramInt2 << 3), 3);
     }
 
     return 16 + BitTreeDecode(paramArrayOfInt, paramInt1 + 258, 8);
   }
 }
