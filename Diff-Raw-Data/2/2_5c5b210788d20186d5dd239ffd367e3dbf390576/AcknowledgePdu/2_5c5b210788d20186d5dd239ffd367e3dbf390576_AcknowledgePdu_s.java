 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import edu.nps.moves.jaxb.dis.*;
 
 /**
  * Section 5.3.6.5. Acknowledge the receiptof a start/resume, stop/freeze, or RemoveEntityPDU. COMPLETE
  *
  * Copyright (c) 2007, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
public class AcknowledgePdu extends SimulationManagementPdu
 {
    /** type of message being acknowledged */
    protected int  acknowledgeFlag;
 
    /** Whether or not the receiving entity was able to comply with the request */
    protected int  responseFlag;
 
    /** Request ID that is unique */
    protected long  requestID;
 
 
 /** Constructor */
  public AcknowledgePdu()
  {
     setPduType( (short)15 );
  }
 
 /** 
  * Constructor--takes a parallel jaxb object and returns an open-dis object 
  * 1.4_sed_bait_start */
  public AcknowledgePdu(edu.nps.moves.jaxb.dis.AcknowledgePdu x)
  {
      super(x); // Call superclass constructor
 
      this.acknowledgeFlag = x.getAcknowledgeFlag();
      this.responseFlag = x.getResponseFlag();
      this.requestID = x.getRequestID();
  }
 /* 1.4_sed_bait_end */
 
 
 /**
  * returns a jaxb object intialized from this object, given an empty jaxb object
  * 1.4_sed_bait_start **/
  public edu.nps.moves.jaxb.dis.AcknowledgePdu initializeJaxbObject(edu.nps.moves.jaxb.dis.AcknowledgePdu x)
  {
      super.initializeJaxbObject(x); // Call superclass initializer
 
      ObjectFactory factory = new ObjectFactory();
 
      x.setAcknowledgeFlag( this.getAcknowledgeFlag() );
      x.setResponseFlag( this.getResponseFlag() );
      x.setRequestID( this.getRequestID() );
    return x;
  }
 /* 1.4_sed_bait_end */
 
 
 public int getMarshalledSize()
 {
    int marshalSize = 0; 
 
    marshalSize = super.getMarshalledSize();
    marshalSize = marshalSize + 2;  // acknowledgeFlag
    marshalSize = marshalSize + 2;  // responseFlag
    marshalSize = marshalSize + 4;  // requestID
 
    return marshalSize;
 }
 
 
 public void setAcknowledgeFlag(int pAcknowledgeFlag)
 { acknowledgeFlag = pAcknowledgeFlag;
 }
 
 public int getAcknowledgeFlag()
 { return acknowledgeFlag; 
 }
 
 public void setResponseFlag(int pResponseFlag)
 { responseFlag = pResponseFlag;
 }
 
 public int getResponseFlag()
 { return responseFlag; 
 }
 
 public void setRequestID(long pRequestID)
 { requestID = pRequestID;
 }
 
 public long getRequestID()
 { return requestID; 
 }
 
 
 public void marshal(DataOutputStream dos)
 {
     super.marshal(dos);
     try 
     {
        dos.writeShort( (short)acknowledgeFlag);
        dos.writeShort( (short)responseFlag);
        dos.writeInt( (int)requestID);
     } // end try 
     catch(Exception e)
     { 
       System.out.println(e);}
     } // end of marshal method
 
 public void unmarshal(DataInputStream dis)
 {
     super.unmarshal(dis);
 
     try 
     {
        acknowledgeFlag = dis.readShort();
        responseFlag = dis.readShort();
        requestID = dis.readInt();
     } // end try 
    catch(Exception e)
     { 
       System.out.println(e); 
     }
  } // end of unmarshal method 
 
 
  /**
   * The equals method doesn't always work--mostly on on classes that consist only of primitives. Be careful.
   */
  public boolean equals(AcknowledgePdu rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (acknowledgeFlag == rhs.acknowledgeFlag)) ivarsEqual = false;
      if( ! (responseFlag == rhs.responseFlag)) ivarsEqual = false;
      if( ! (requestID == rhs.requestID)) ivarsEqual = false;
 
     return ivarsEqual;
  }
 } // end of class
