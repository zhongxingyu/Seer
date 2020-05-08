 /*
  *  Player Java Client 3 - GripperInterface.java
  *  Copyright (C) 2002-2006 Radu Bogdan Rusu, Maxim Batalin
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * $Id: GripperInterface.java 115 2011-03-03 19:38:51Z corot $
  *
  */
 package javaclient3;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 import javaclient3.structures.PlayerBbox3d;
 import javaclient3.structures.PlayerMsgHdr;
 import javaclient3.structures.PlayerPose3d;
 import javaclient3.structures.gripper.PlayerGripperData;
 import javaclient3.structures.gripper.PlayerGripperGeom;
 import javaclient3.xdr.OncRpcException;
 import javaclient3.xdr.XdrBufferDecodingStream;
 
 /**
  * The gripper interface provides access to a robotic gripper. A gripper is
  * a device capable of closing around and carrying an object of suitable size
  * and shape. On a mobile robot, a gripper is typically mounted near the floor
  * on the front, or on the end of a robotic limb. Grippers typically have two
  * "fingers" that close around an object. Some grippers can detect whether an
  * object is within the gripper (using, for example, light beams). Some grippers
  * also have the ability to move the a carried object into a storage system,
  * freeing the gripper to pick up a new object, and move objects from the storage
  * system back into the gripper.
  * @author Radu Bogdan Rusu, Maxim Batalin
  * @version
  * <ul>
  *      <li>v3.0 - Player 3.0 supported
  * </ul>
  */
 public class GripperInterface extends PlayerDevice {
 
     private static final boolean isDebugging = PlayerClient.isDebugging;
 
     // Logging support
     private Logger logger = Logger.getLogger (GripperInterface.class.getName ());
 
     private PlayerGripperData pgdata;
     private boolean           readyPgdata = false;
     private PlayerGripperGeom pggeom;
     private boolean           readyPggeom = false;
 
     /**
      * Constructor for GripperInterface.
      * @param pc a reference to the PlayerClient object
      */
     public GripperInterface (PlayerClient pc) { super (pc); }
 
     /**
      * The gripper interface returns the current state of the gripper
      * and information on a potential object in the gripper.
      * State may be PLAYER_GRIPPER_STATE_OPEN, PLAYER_GRIPPER_STATE_CLOSED,
      * PLAYER_GRIPPER_STATE_MOVING or PLAYER_GRIPPER_STATE_ERROR.
      * Beams provides information on how far into the gripper an object is.
      * For most grippers, this will be a bit mask, with each bit representing
      * whether a beam has been interrupted or not.
      * Stored provides the number of currently stored objects.
      */
     public synchronized void readData (PlayerMsgHdr header) {
         try {
             switch (header.getSubtype ()) {
                 case PLAYER_GRIPPER_DATA_STATE: {
                     this.timestamp = header.getTimestamp();
 
                     pgdata = new PlayerGripperData ();
 
                     // Buffer for reading gripper data
                     byte[] buffer = new byte[12];
                     // Read gripper data
                     is.readFully (buffer, 0, 12);
 
                     // Begin decoding the XDR buffer
                     XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
                     xdr.beginDecoding ();
                     pgdata.setState  (xdr.xdrDecodeByte());
                     pgdata.setBeams  (xdr.xdrDecodeInt ());
                     pgdata.setStored (xdr.xdrDecodeByte());
                     xdr.endDecoding   ();
                     xdr.close ();
 
                     readyPgdata = true;
                     break;
                 }
             }
         } catch (IOException e) {
             throw new PlayerException
                 ("[Gripper] : Error reading payload: " +
                         e.toString(), e);
         } catch (OncRpcException e) {
             throw new PlayerException
                 ("[Gripper] : Error while XDR-decoding payload: " +
                         e.toString(), e);
         }
     }
 
     /**
      * Command: Open.
      * <br>
      * Tells the gripper to open.
      */
     public void open () {
         setGripper (PLAYER_GRIPPER_CMD_OPEN);
     }
 
     /**
      * Command: Close.
      * <br>
      * Tells the gripper to close.
      */
     public void close () {
         setGripper (PLAYER_GRIPPER_CMD_CLOSE);
     }
 
     /**
      * Command: Stop.
      * <br>
      * Tells the gripper to stop.
      */
     public void stop () {
         setGripper (PLAYER_GRIPPER_CMD_STOP);
     }
 
     /**
      * Command: Store.
      * <br>
      * Tells the gripper to store whatever it is holding.
      */
     public void store () {
         setGripper (PLAYER_GRIPPER_CMD_STORE);
     }
 
     /**
      * Command: Retrieve.
      * <br>
      * Tells the gripper to retrieve a stored object (so that it can
      * be put back into the world). The opposite of store.
      */
     public void retrieve () {
         setGripper (PLAYER_GRIPPER_CMD_RETRIEVE);
     }
 
     /**
      * The gripper interface accepts 5 different commands with codes:
      * <br>
      * <ul>
      *      <li>GRIPopen     - 1
      *      <li>GRIPclose    - 2
      *      <li>GRIPstop     - 3
      *      <li>GRIPstore    - 4
      *      <li>GRIPretrieve - 5
      * </ul>
      * @param cmd the command
      */
     public void setGripper (int cmd) {
         try {
             sendHeader (PLAYER_MSGTYPE_CMD, cmd, 0);
             os.flush ();
         } catch (IOException e) {
             throw new PlayerException
                 ("[Gripper] : Couldn't send state command request: " +
                         e.toString(), e);
         }
     }

     /**
      * Request/reply: Get geometry.
      * <br><br>
      * The geometry (pose and size, beams number and store capacity) of the gripper
      * device can be queried by sending a null PLAYER_GRIPPER_REQ_GET_GEOM request.
      * <br><br>
      * See the player_gripper_geom structure from player.h
      */
     public void queryGeometry () {
         try {
             sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_GRIPPER_REQ_GET_GEOM, 0);
             os.flush ();
         } catch (IOException e) {
             throw new PlayerException
                 ("[Gripper] : Couldn't send PLAYER_GRIPPER_REQ_GET_GEOM " +
                         "command: " + e.toString(), e);
         }
     }
 
     /**
      * Handle acknowledgement response messages
      * @param header Player header
      */
     public void handleResponse (PlayerMsgHdr header) {
         try {
             switch (header.getSubtype ()) {
                 case PLAYER_GRIPPER_REQ_GET_GEOM: {
                     pggeom = new PlayerGripperGeom ();
                     PlayerPose3d pose = new PlayerPose3d ();
                     PlayerBbox3d innb = new PlayerBbox3d ();
                     PlayerBbox3d outb = new PlayerBbox3d ();
 
                     // Buffer for reading configuration data
                     byte[] buffer = new byte[96 + 8];
                     // Read configuration data
                     is.readFully (buffer, 0, 96 + 8);
 
                     // Begin decoding the XDR buffer
                     XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
                     xdr.beginDecoding ();
                     pose.setPx       (xdr.xdrDecodeDouble ());
                     pose.setPy       (xdr.xdrDecodeDouble ());
                     pose.setPz       (xdr.xdrDecodeDouble ());
                     pose.setProll    (xdr.xdrDecodeDouble ());
                     pose.setPpitch   (xdr.xdrDecodeDouble ());
                     pose.setPyaw     (xdr.xdrDecodeDouble ());
                     outb.setSw       (xdr.xdrDecodeDouble ());
                     outb.setSl       (xdr.xdrDecodeDouble ());
                     outb.setSh       (xdr.xdrDecodeDouble ());
                     innb.setSw       (xdr.xdrDecodeDouble ());
                     innb.setSl       (xdr.xdrDecodeDouble ());
                     innb.setSh       (xdr.xdrDecodeDouble ());
                     pggeom.setNumBeams (xdr.xdrDecodeByte ());
                     pggeom.setCapacity (xdr.xdrDecodeByte ());
                     xdr.endDecoding   ();
                     xdr.close ();
 
                     pggeom.setPose      (pose);
                     pggeom.setInnerSize (innb);
                     pggeom.setOuterSize (outb);
 
                     readyPggeom = true;
                     break;
                 }
                 default:{
                     if (isDebugging)
                         logger.log (Level.FINEST, "[Gripper]Debug] : " +
                                 "Unexpected response " + header.getSubtype () +
                                 " of size = " + header.getSize ());
                     break;
                 }
             }
         } catch (IOException e) {
             throw new PlayerException
                 ("[Gripper] : Error reading payload: " +
                         e.toString(), e);
         } catch (OncRpcException e) {
             throw new PlayerException
                 ("[Gripper] : Error while XDR-decoding payload: " +
                         e.toString(), e);
         }
     }
 
     /**
      * Get the Gripper data.
      * @return an object of type PlayerGripperData containing the requested data
      */
     public PlayerGripperData getData () { return this.pgdata; }
 
     /**
      * Get the Gripper geometry data.
      * @return an object of type PlayerGripperGeom containing the requested data
      */
     public PlayerGripperGeom getGeom () { return this.pggeom; }
 
     /**
      * Check if data is available.
      * @return true if ready, false if not ready
      */
     public boolean isDataReady () {
         if (readyPgdata) {
             readyPgdata = false;
             return true;
         }
         return false;
     }
 
     /**
      * Check if geometry data is available.
      * @return true if ready, false if not ready
      */
     public boolean isGeomReady () {
         if (readyPggeom) {
             readyPggeom = false;
             return true;
         }
         return false;
     }
 }
