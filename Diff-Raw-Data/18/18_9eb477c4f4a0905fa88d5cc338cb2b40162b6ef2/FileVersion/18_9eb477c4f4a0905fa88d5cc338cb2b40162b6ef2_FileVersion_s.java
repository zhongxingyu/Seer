 
 /**
  * Version object for transactioned cache entries.
  */
 public class FileVersion implements Comparable<FileVersion> {
   
   public static FileVersion parse(byte[] arr) {
     return parse(new String(arr));
   }
 
   public static FileVersion parse(String s) {
     String[] parts = s.split("-");
     if (parts.length != 3)
       throw new IllegalArgumentException("Invalid FileVersion String!");
     
     try {
       TransactionId id = TransactionId.parse(parts[0]);
       return new FileVersion(id, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
     } catch (NumberFormatException e) {
       throw new IllegalArgumentException("Invalid FileVersion String!", e);
     }
   }
 
   private TransactionId id;
   private int version;
   private int revision;
   private int commitTime;
 
   /**
    * Construct a new FileVersion for a file owned by the given
    * owner at the given version.
    */
   public FileVersion(TransactionId id, int version, int revision) {
     this(id, version, revision, -1);
   }
 
   public FileVersion() {
     //this is a hack! methods that call this should call unpack immediately;
   } 
 
   public FileVersion(FileVersion other) {
     this(other.id, other.version, other.revision, other.commitTime);
   }
 
   public FileVersion(TransactionId id, int version, int revision, int commitTime) {
     this.id = id;
     this.version = version;
     this.revision = revision;
     this.commitTime = commitTime;
   }
 
   public TransactionId getTransactionId() {
     return id;
   }
 
   public int getOwner() {
     return id.getClientId();
   }
 
   public int getVersion() {
     return version;
   }
 
   public int getRevision() {
     return revision;
   }
 
   public void incrementRevision() {
     revision++;
   }
 
   public void clearRevision() {
     revision = 0;
   }
 
   public FileVersion nextVersion() {
     return new FileVersion(id, version + 1, 0);
   }
 
   public boolean isCommitted() {
     return revision == 0;
   }
 
   public void commit(int commitTime) {
     this.commitTime = commitTime;
     clearRevision();
   }
 
   public int getCommitTime() {
     return commitTime;
   }
 
   @Override
   public int compareTo(FileVersion other) {
     if (other.version != version)
       return other.version - version;
     
     return id.compareTo(other.id);
   }
 
   @Override
   public int hashCode() {
     Integer i = new Integer(version ^ id.getClientId() ^ id.getTxId());
     return i.hashCode();
   }
 
   @Override
   public boolean equals(Object o) {
     if (o instanceof FileVersion) {
       return ((FileVersion) o).id.equals(id) &&
         ((FileVersion) o).version == version &&
         ((FileVersion) o).revision == revision;
     }
     return false;
   }
 
   @Override
   public String toString() {
     return "" + id.toString() + "-" + version + "-" + revision;
   }
   
   /*acks need this*/
   public byte[] pack() {
     byte [] packed = new byte[4];
     BinaryUtils.uintToByte(id.getClientId(), packed, 0);
     BinaryUtils.uintToByte(id.getTxId(), packed, 1);
     BinaryUtils.uintToByte(version, packed, 2);
     BinaryUtils.uintToByte(revision, packed, 3);
     return packed;
   }
 
  public boolean unpack(byte[] msg) {
     if (msg.length < 4)
       return false;
    TransactionId tid = new TransactionId(msg[0], msg[1]);
     this.id = tid;
    this.version = msg[2];
    this.revision = msg[3];
     return true;
   }
 }
