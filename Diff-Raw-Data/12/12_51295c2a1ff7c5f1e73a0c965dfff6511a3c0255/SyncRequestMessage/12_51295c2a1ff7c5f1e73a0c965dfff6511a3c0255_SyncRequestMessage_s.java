 
 /**
  * This is a HACK HACK HACK! We don't actually have any data in this
  * message, and getData() will always return null. But, the format of
  * this message is so similar to SyncDataMessage, and Java doesn't have
  * multiple inheritance or mixins, so we're stuck with this :-/.
  */
 public class SyncRequestMessage extends FileMessage {
   public SyncRequestMessage() {
     super(MessageWireType.SyncRequest);
   }
 
   protected SyncRequestMessage(MessageWireType wireId) {
     super(wireId);
   }
 
   public SyncRequestMessage(String fileName, FileVersion version, Flags flags) {
     this(MessageWireType.SyncRequest, fileName, version, flags, null);
   }
 
   protected FileVersion version;
   protected Flags flags;
 
   /**
    * HACK HACK HACK constructor.
    * Makes SyncDataMessage work.
    */
   protected SyncRequestMessage(MessageWireType wireId,
                                String fileName,
                                FileVersion version,
                                Flags flags,
                                String data) {
     super(wireId, fileName, data);
 
     if (version.getVersion() < 0)
       throw new IllegalArgumentException("Version should be nonnegative!");
 
     this.version = version;
 
     if (flags.pack().length != Flags.kDefaultEncodedLength)
       throw new IllegalArgumentException("Encoded flags length (" + flags.pack().length +
                                          ") is not equal to default encoded length!");
 
     this.flags = new Flags(flags);
   }
 
   protected byte[] packSyncHeader(int numAdditionalBytes) {
     int trailerSizeBytes = Flags.kDefaultEncodedLength +numAdditionalBytes;
 
     byte[] packed = createPackedHeader(trailerSizeBytes);
 
     int trailerStart = packed.length - trailerSizeBytes;
 
     //BinaryUtils.uintToBEWord(version, packed, trailerStart);
 
     byte[] encodedFlags = flags.pack();
     System.arraycopy(encodedFlags, 0, packed, trailerStart, encodedFlags.length);
     return packed;
   }
 
   public Flags getFlags() {
     return flags;
   }
 
   public FileVersion getVersion() {
     return version;
   }
 
   @Override
   public byte[] pack() {
     return packSyncHeader(0);
   }
 
   protected int deserializeSyncHeader(byte[] packed) {
 //    System.err.println("0");
     int contentStart = unpackFileNameHeader(packed);
     if (contentStart < 0)
       return -1;
 
 //    System.err.println("1");
     // Unpack flags field
    if (contentStart + FileVersion.kFileVersionPackLength + Flags.kDefaultEncodedLength > packed.length)
       return -1;
 
     //version = BinaryUtils.beWordToUint(packed, contentStart);
    FileVersion v = new FileVersion();
    v.unpack(packed, contentStart);

     System.err.println("packed " + (contentStart + FileVersion.kFileVersionPackLength) + " vs " + packed.length);
     flags = Flags.unpack(packed,
                         contentStart + FileVersion.kFileVersionPackLength,
                          Flags.kDefaultEncodedLength);
 //    System.err.println("2");
     if (flags == null)
       return -1;
 
    return contentStart + FileVersion.kFileVersionPackLength + Flags.kDefaultEncodedLength;
   }
 
   @Override
   protected boolean fromByteStream(byte[] packed) {
 //    System.err.println("FBS");
     int trailerStart = deserializeSyncHeader(packed);
     if (trailerStart == -1)
       return false;
 
     data = null;
 
     return deserializeDataTrailerIfPresent(packed, trailerStart);
   }
 
   @Override
   public String toString() {
     return "SyncRequest file=" + getFileName() + ", version=" + getVersion() + ", flags = " + flags.prettyPrint(SyncFlags.byWireId);
   }
 }
