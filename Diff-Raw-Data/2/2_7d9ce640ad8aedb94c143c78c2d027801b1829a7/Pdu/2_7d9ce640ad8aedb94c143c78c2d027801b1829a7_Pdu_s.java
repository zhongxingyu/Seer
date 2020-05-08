 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import javax.xml.bind.annotation.*;
import edu.nps.moves.disutil.DisTime;
 
 /**
  * The superclass for all PDUs. This incorporates the PduHeader record, section 5.2.29.
  *
  * Copyright (c) 2008, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * This has been patched from the generated source code to handle unmarhsalling an unsigned long.
  *
  * @author DMcG
  */
 public class Pdu extends Object implements Serializable
 {
     /** The DIS absolute timestamp mask; used to ensure the LSB in timestamps is always set to 1 */
     public static final int ABSOLUTE_TIMESTAMP_MASK = 0x00000001;
     
     /** The DIS relative timestamp mask; used to ensure the LSB in timestamps is always set to 0 */
     public static final int RELATIVE_TIMESTAMP_MASK = 0xFFFFFFFE;
     
    /** The version of the protocol. 5=DIS-1995, 6=DIS-1998. */
    protected short  protocolVersion = 6;
 
    /** Exercise ID */
    protected short  exerciseID = 0;
 
    /** Type of pdu, unique for each PDU class */
    protected short  pduType;
 
    /** value that refers to the protocol family, eg SimulationManagement, et */
    protected short  protocolFamily;
 
    /** Timestamp value */
    protected long  timestamp;
 
    /** Length, in bytes, of the PDU */
    protected int  length;
 
    /** zero-filled array of padding */
    protected short  padding = 0;
     
 
 /** Constructor */
  public Pdu()
  {
  }
 
 public int getMarshalledSize()
 {
    int marshalSize = 0; 
 
    marshalSize = marshalSize + 1;  // protocolVersion
    marshalSize = marshalSize + 1;  // exerciseID
    marshalSize = marshalSize + 1;  // pduType
    marshalSize = marshalSize + 1;  // protocolFamily
    marshalSize = marshalSize + 4;  // timestamp
    marshalSize = marshalSize + 2;  // length
    marshalSize = marshalSize + 2;  // padding
 
    return marshalSize;
 }
 
 
 public void setProtocolVersion(short pProtocolVersion)
 { protocolVersion = pProtocolVersion;
 }
 
 @XmlAttribute
 public short getProtocolVersion()
 { return protocolVersion; 
 }
 
 public void setExerciseID(short pExerciseID)
 { exerciseID = pExerciseID;
 }
 
 @XmlAttribute
 public short getExerciseID()
 { return exerciseID; 
 }
 
 public void setPduType(short pPduType)
 { pduType = pPduType;
 }
 
 @XmlAttribute
 public short getPduType()
 { return pduType; 
 }
 
 public void setProtocolFamily(short pProtocolFamily)
 { protocolFamily = pProtocolFamily;
 }
 
 @XmlAttribute
 public short getProtocolFamily()
 { return protocolFamily; 
 }
 
 public void setTimestamp(long pTimestamp)
 { timestamp = pTimestamp;
 }
 
 @XmlAttribute
 public long getTimestamp()
 { return timestamp; 
 }
 
 /** 
  * Does nothing; the length is always determed by the getLength() method,
  * which is dynamically computed each time rather than relying on the 
  * ivar. This method is only here for java beans completeness. This change
  * is a patch from the generated code; see the patches directory for details.
  */
 public void setLength(int pLength)
 { 
 }
 
 @XmlAttribute
 public int getLength()
 { return this.getMarshalledSize(); // post-processing patch; compute length field
 }
 
 public void setPadding(short pPadding)
 { padding = pPadding;
 }
 
 @XmlAttribute
 public short getPadding()
 { return padding; 
 }
 
 
 public void marshal(DataOutputStream dos)
 {
     try 
     {
        dos.writeByte( (byte)protocolVersion);
        dos.writeByte( (byte)exerciseID);
        dos.writeByte( (byte)pduType);
        dos.writeByte( (byte)protocolFamily);
        dos.writeInt( (int)timestamp);
        dos.writeShort( (short)this.getLength()); // post-processing patch
        dos.writeShort( (short)padding);
     } // end try 
     catch(Exception e)
     { 
       System.out.println(e);}
     } // end of marshal method
 
 public void unmarshal(DataInputStream dis)
 {
     try 
     {
        protocolVersion = (short)dis.readUnsignedByte();
        exerciseID = (short)dis.readUnsignedByte();
        pduType = (short)dis.readUnsignedByte();
        protocolFamily = (short)dis.readUnsignedByte();
        int ch1 = dis.read(); // post-processing patch; read unsigned int and put it in a long
        int ch2 = dis.read();
        int ch3 = dis.read();
        int ch4 = dis.read();
        timestamp = (((long)ch1 << 24) + ((long)ch2 << 16) + (ch3 << 8) + (ch4 << 0)); 
        length = (int)dis.readUnsignedShort();
        padding = dis.readShort();
     } // end try 
    catch(Exception e)
     { 
       System.out.println(e); 
     }
  } // end of unmarshal method 
 
 
 /**
  * Packs a Pdu into the ByteBuffer.
  * @throws java.nio.BufferOverflowException if buff is too small
  * @throws java.nio.ReadOnlyBufferException if buff is read only
  * @see java.nio.ByteBuffer
  * @param buff The ByteBuffer at the position to begin writing
  * @since ??
  */
 public void marshal(java.nio.ByteBuffer buff)
 {
        buff.put( (byte)protocolVersion);
        buff.put( (byte)exerciseID);
        buff.put( (byte)pduType);
        buff.put( (byte)protocolFamily);
        buff.putInt( (int)timestamp);   // post-processing patch; fix timestamp
        buff.putShort( (short)this.getLength()); // post-processing patch
        buff.putShort( (short)padding);
     } // end of marshal method
 
 /**
  * Unpacks a Pdu from the underlying data.
  * @throws java.nio.BufferUnderflowException if buff is too small
  * @see java.nio.ByteBuffer
  * @param buff The ByteBuffer at the position to begin reading
  * @since ??
  */
 public void unmarshal(java.nio.ByteBuffer buff)
 {
        protocolVersion = (short)(buff.get() & 0xFF);
        exerciseID = (short)(buff.get() & 0xFF);
        pduType = (short)(buff.get() & 0xFF);
        protocolFamily = (short)(buff.get() & 0xFF);
        int ch1 = buff.get(); // post-processing patch; read unsigned int for timestamp and put it in a long
        int ch2 = buff.get();
        int ch3 = buff.get();
        int ch4 = buff.get();
        timestamp = (((long)ch1 << 24) + ((long)ch2 << 16) + (ch3 << 8) + (ch4 << 0)); 
        length = (int)(buff.getShort() & 0xFFFF);
        padding = buff.getShort();
  } // end of unmarshal method 
 
 
 /**
  * A convenience method for marshalling to a byte array. The method will marshal
  * the PDU as is.
  * This is not as efficient as reusing a ByteBuffer, but it <em>is</em> easy.
  * @return a byte array with the marshalled {@link Pdu}.
  * @since ??
  */
 public byte[] marshal()
 {
     byte[] data = new byte[getMarshalledSize()];
     java.nio.ByteBuffer buff = java.nio.ByteBuffer.wrap(data);
     marshal(buff);
     return data;
 }
 
 /**
  * A convieneince method to marshal to a byte array with the timestamp set to
  * the DIS standard for absolute timestamps (which works only if the host is
  * slaved to NTP). This means the timestamp will roll over every hour.
  */
 public byte[] marshalWithDisAbsoluteTimestamp()
 {
     DisTime disTime = DisTime.getInstance();
     this.setTimestamp(disTime.getDisAbsoluteTimestamp());
     return this.marshal();
 }
 
 /**
  * A convieneince method to marshal to a byte array with the timestamp set to
  * the DIS standard for relative timestamps. The timestamp will roll over every
  * hour
  */
 public byte[] marshalWithDisRelativeTimestamp()
 {
     DisTime disTime = DisTime.getInstance();
     this.setTimestamp(disTime.getDisRelativeTimestamp());
     return this.marshal();
 }
 
 /**
  * A convienience method to marshal a PDU using the NPS-specific format for
  * timestamps, which is hundredths of a second since the start of the year.
  * This effectively eliminates the rollover issues from a practical standpoint.
  * @return
  */
 public byte[] marshalWithNpsTimestamp()
 {
     DisTime disTime = DisTime.getInstance();
     this.setTimestamp(disTime.getNpsTimestamp());
     return this.marshal();
 }
  
 
  /**
   * The equals method doesn't always work--mostly on on classes that consist only of primitives. Be careful.
   */
  public boolean equals(Pdu rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (protocolVersion == rhs.protocolVersion)) ivarsEqual = false;
      if( ! (exerciseID == rhs.exerciseID)) ivarsEqual = false;
      if( ! (pduType == rhs.pduType)) ivarsEqual = false;
      if( ! (protocolFamily == rhs.protocolFamily)) ivarsEqual = false;
      if( ! (timestamp == rhs.timestamp)) ivarsEqual = false;
      if( ! (length == rhs.length)) ivarsEqual = false;
      if( ! (padding == rhs.padding)) ivarsEqual = false;
 
     return ivarsEqual;
  }
 } // end of class
