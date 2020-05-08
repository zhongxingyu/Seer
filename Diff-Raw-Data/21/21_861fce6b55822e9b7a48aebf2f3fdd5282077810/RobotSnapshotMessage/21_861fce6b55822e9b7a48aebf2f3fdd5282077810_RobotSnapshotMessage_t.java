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
 
   /**
    * The robot snapshot that is being sent.
    */
    private IRobotSnapshot    snapshot_;
 
   /**
    * Base constructor for the robot snapshot message.
    * 
    * @param snapshot
    *           the robot snapshot that is being sent.
    * @param sender
    *           the the name of the robot that is sending the message.
    * @param time
    *           the Robocode time at which the message is being sent.
    */
    public RobotSnapshotMessage(IRobotSnapshot snapshot, String sender, long time) {
       super(sender, time);
       this.snapshot_ = snapshot;
    }
 
   /**
    * Returns the snapshot that is being sent. This is used by the receiver to retrieve that robot
    * snapshot that was sent to it.
    * 
    * @return the sent robot snapshot.
    */
    public IRobotSnapshot getSnapshot() {
       return snapshot_;
    }
 
 }
