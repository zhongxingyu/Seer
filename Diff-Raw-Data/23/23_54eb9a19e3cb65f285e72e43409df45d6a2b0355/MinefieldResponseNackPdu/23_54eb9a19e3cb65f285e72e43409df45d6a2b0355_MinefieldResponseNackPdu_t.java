 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import edu.nps.moves.jaxb.dis.*;
 
 /**
  * Section 5.3.10.4 proivde the means to request a retransmit of a minefield data pdu. COMPLETE
  *
  * Copyright (c) 2007, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
public class MinefieldResponseNackPdu extends MinefieldFamilyPdu implements Serializable
 {
    /** Minefield ID */
    protected EntityID  minefieldID = new EntityID(); 
 
    /** entity ID making the request */
    protected EntityID  requestingEntityID = new EntityID(); 
 
    /** request ID */
    protected short  requestID;
 
    /** how many pdus were missing */
    protected short  numberOfMissingPdus;
 
    /** PDU sequence numbers that were missing */
    protected List missingPduSequenceNumbers = new ArrayList(); 
 
 /** Constructor */
  public MinefieldResponseNackPdu()
  {
     setPduType( (short)40 );
  }
 
 /** 
  * Constructor--takes a parallel jaxb object and returns an open-dis object 
  * 1.4_sed_bait_start */
  public MinefieldResponseNackPdu(edu.nps.moves.jaxb.dis.MinefieldResponseNackPdu x)
  {
      super(x); // Call superclass constructor
 
 
      edu.nps.moves.dis.EntityID foo_0;
      if(x.getMinefieldID() == null)
         foo_0 = new edu.nps.moves.dis.EntityID();
       else
         foo_0 = new edu.nps.moves.dis.EntityID(x.getMinefieldID() );
      this.setMinefieldID(foo_0);
 
 
      edu.nps.moves.dis.EntityID foo_1;
      if(x.getRequestingEntityID() == null)
         foo_1 = new edu.nps.moves.dis.EntityID();
       else
         foo_1 = new edu.nps.moves.dis.EntityID(x.getRequestingEntityID() );
      this.setRequestingEntityID(foo_1);
 
      this.requestID = x.getRequestID();
      this.numberOfMissingPdus = x.getNumberOfMissingPdus();
      this.missingPduSequenceNumbers = new ArrayList();
      for(int idx = 0; idx < x.getMissingPduSequenceNumbers().size(); idx++)
      {
         this.missingPduSequenceNumbers.add( new edu.nps.moves.dis.EightByteChunk((edu.nps.moves.jaxb.dis.EightByteChunk) x.getMissingPduSequenceNumbers().get(idx)));
      }
  }
 /* 1.4_sed_bait_end */
 
 
 /**
  * returns a jaxb object intialized from this object, given an empty jaxb object
  * 1.4_sed_bait_start **/
  public edu.nps.moves.jaxb.dis.MinefieldResponseNackPdu initializeJaxbObject(edu.nps.moves.jaxb.dis.MinefieldResponseNackPdu x)
  {
      super.initializeJaxbObject(x); // Call superclass initializer
 
      ObjectFactory factory = new ObjectFactory();
 
      x.setMinefieldID( this.getMinefieldID().initializeJaxbObject(factory.createEntityID()) );
      x.setRequestingEntityID( this.getRequestingEntityID().initializeJaxbObject(factory.createEntityID()) );
      x.setRequestID( this.getRequestID() );
      x.setNumberOfMissingPdus( this.getNumberOfMissingPdus() );
 
      List missingPduSequenceNumbers_1 = x.getMissingPduSequenceNumbers();
      for(int idx = 0; idx < missingPduSequenceNumbers.size(); idx++)
      {
          EightByteChunk a = (edu.nps.moves.dis.EightByteChunk)missingPduSequenceNumbers.get(idx);
          missingPduSequenceNumbers_1.add(a.initializeJaxbObject(factory.createEightByteChunk()));
      }
    return x;
  }
 /* 1.4_sed_bait_end */
 
 
 public int getMarshalledSize()
 {
    int marshalSize = 0; 
 
    marshalSize = super.getMarshalledSize();
    marshalSize = marshalSize + minefieldID.getMarshalledSize();  // minefieldID
    marshalSize = marshalSize + requestingEntityID.getMarshalledSize();  // requestingEntityID
    marshalSize = marshalSize + 1;  // requestID
    marshalSize = marshalSize + 1;  // numberOfMissingPdus
    for(int idx=0; idx < missingPduSequenceNumbers.size(); idx++)
    {
         EightByteChunk listElement = (EightByteChunk)missingPduSequenceNumbers.get(idx);
         marshalSize = marshalSize + listElement.getMarshalledSize();
    }
 
    return marshalSize;
 }
 
 
 public void setMinefieldID(EntityID pMinefieldID)
 { minefieldID = pMinefieldID;
 }
 
 public EntityID getMinefieldID()
 { return minefieldID; }
 
 public void setRequestingEntityID(EntityID pRequestingEntityID)
 { requestingEntityID = pRequestingEntityID;
 }
 
 public EntityID getRequestingEntityID()
 { return requestingEntityID; }
 
 public void setRequestID(short pRequestID)
 { requestID = pRequestID;
 }
 
 public short getRequestID()
 { return requestID; 
 }
 
 public short getNumberOfMissingPdus()
 { return (short)missingPduSequenceNumbers.size();
 }
 
 /** Note that setting this value will ot change the marshalled value. The list whose length this describes is used for that purpose.
  * The getnumberOfMissingPdus method will also be based on the actual list length rather than this value. 
  * The method is simply here for java bean completeness.
  */
 public void setNumberOfMissingPdus(short pNumberOfMissingPdus)
 { numberOfMissingPdus = pNumberOfMissingPdus;
 }
 
 public void setMissingPduSequenceNumbers(List pMissingPduSequenceNumbers)
 { missingPduSequenceNumbers = pMissingPduSequenceNumbers;
 }
 
 public List getMissingPduSequenceNumbers()
 { return missingPduSequenceNumbers; }
 
 
 public void marshal(DataOutputStream dos)
 {
     super.marshal(dos);
     try 
     {
        minefieldID.marshal(dos);
        requestingEntityID.marshal(dos);
        dos.writeByte( (byte)requestID);
        dos.writeByte( (byte)missingPduSequenceNumbers.size());
 
        for(int idx = 0; idx < missingPduSequenceNumbers.size(); idx++)
        {
             EightByteChunk aEightByteChunk = (EightByteChunk)missingPduSequenceNumbers.get(idx);
             aEightByteChunk.marshal(dos);
        } // end of list marshalling
 
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
        minefieldID.unmarshal(dis);
        requestingEntityID.unmarshal(dis);
        requestID = dis.readByte();
        numberOfMissingPdus = dis.readByte();
         for(int idx = 0; idx < numberOfMissingPdus; idx++)
         {
            EightByteChunk anX = new EightByteChunk();
             anX.unmarshal(dis);
             missingPduSequenceNumbers.add(anX);
         };
 
     } // end try 
    catch(Exception e)
     { 
       System.out.println(e); 
     }
  } // end of unmarshal method 
 
 
  /**
   * The equals method doesn't always work--mostly on on classes that consist only of primitives. Be careful.
   */
  public boolean equals(MinefieldResponseNackPdu rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (minefieldID.equals( rhs.minefieldID) )) ivarsEqual = false;
      if( ! (requestingEntityID.equals( rhs.requestingEntityID) )) ivarsEqual = false;
      if( ! (requestID == rhs.requestID)) ivarsEqual = false;
      if( ! (numberOfMissingPdus == rhs.numberOfMissingPdus)) ivarsEqual = false;
 
      for(int idx = 0; idx < missingPduSequenceNumbers.size(); idx++)
      {
         EightByteChunk x = (EightByteChunk)missingPduSequenceNumbers.get(idx);
         if( ! ( missingPduSequenceNumbers.get(idx).equals(rhs.missingPduSequenceNumbers.get(idx)))) ivarsEqual = false;
      }
 
 
     return ivarsEqual;
  }
 } // end of class
