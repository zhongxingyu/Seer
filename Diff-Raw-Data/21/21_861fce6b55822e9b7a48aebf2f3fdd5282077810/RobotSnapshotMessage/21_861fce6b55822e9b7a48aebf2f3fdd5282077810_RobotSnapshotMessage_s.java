 package kid.messages;
 
 import kid.robots.IRobotSnapshot;
 
 /**
  * A {@link Message} containing an {@link IRobotSnapshot} that can be sent between robots.
  * 
  * @author Brian Norman
  * @version 1.0
  */
 public class RobotSnapshotMessage extends Message {
 
    /**
     * Determines if a deserialized file is compatible with this class. Maintainers must change this
     * value if and only if the new version of this class is not compatible with old versions.
     */
    private static final long serialVersionUID = 3847165817821631453L;
 
    private IRobotSnapshot    snapshot_;
 
    public RobotSnapshotMessage(IRobotSnapshot snapshot, String sender, long time) {
       super(sender, time);
       this.snapshot_ = snapshot;
    }
 
    public IRobotSnapshot getSnapshot() {
       return snapshot_;
    }
 
 }
