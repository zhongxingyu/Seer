 
 /**
  * Generic flags class.
  *
  * Handles managing a set of flag states and efficiently encoding and decoding them
  * to/from platform-independent binary format.
  */
 public class Flags {
   
   public static final int kDefaultNumFlags = 16;
   public static final int kDefaultEncodedLength = (kDefaultNumFlags + 7) / 8;
 
   private byte[] backingData;
 
   /**
    * Default constructor. Provides the default number of flags and
    * clears all of them initially.
    */
   public Flags() {
     this(kDefaultNumFlags);
   }
 
   public Flags(int numFlags) {
     backingData = new byte[(numFlags + 7) / 8];
   }
   
   public Flags(SyncFlags flag) {
	backingData = new byte[(flag.getId() + 7)/8];
   }
 
   public Flags(Flags other) {
     backingData = new byte[other.backingData.length];
     System.arraycopy(other.backingData, 0, backingData, 0, backingData.length);
   }
 
   private Flags(byte[] packed, int offset, int length) {
     if (offset < 0 || offset >= packed.length)
       throw new IllegalArgumentException("Invalid offset!");
     
     if (length < 0 || offset + length >= packed.length)
       throw new IllegalArgumentException("Packed data runs off the end of the array!");
 
     backingData = new byte[length];
     System.arraycopy(packed, offset, backingData, 0, length);
   }
 
   private int flagToByteId(int flag) {
     if (flag >= (backingData.length * 8))
       throw new IllegalArgumentException("No flag by that number!");
 
     return backingData.length - ((flag + 7) / 8);
   }
 
   public int flagToBit(int flag) {
     return (1 << (7 - (flag % 8)));
   }
 
   public boolean isSet(int flag) {
     return (backingData[flagToByteId(flag)] & flagToBit(flag)) != 0;
   }
 
   public boolean isSet(SyncFlags flag) {
 	return (backingData[flagToByteId(flag.getId())] & flagToBit(flag.getId())) != 0;
   }
   
   public void set(int flag) {
     backingData[flagToByteId(flag)] |= flagToBit(flag);
   }
 
   public void set(SyncFlags flag) {
     set(flag.getId());
   }
 
   public void clear(int flag) {
     backingData[flagToByteId(flag)] &= ~flagToBit(flag);
   }
  
   public void clear(SyncFlags flag) {
     clear(flag.getId());
   }
 
   public void twiddle(int flag) {
     backingData[flagToByteId(flag)] ^= flagToBit(flag);
   }
 
   public void twiddle(SyncFlags flag) {
     twiddle(flag.getId());
   }
 
   public byte[] pack() {
     byte[] packed = new byte[backingData.length];
     System.arraycopy(backingData, 0, packed, 0, backingData.length);
     return packed;
   }
 
   public static Flags unpack(byte[] packed, int start, int length) {
     return new Flags(packed, start, length);
   }
 }
