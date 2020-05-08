 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import edu.nps.moves.jaxb.dis.*;
 
 /**
  * Section 5.3.10.3 Information about individual mines within a minefield. This is very, very wrong. UNFINISHED
  *
  * Copyright (c) 2007, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
public class MinefieldDataPdu extends MinfieldFamilyPdu implements Serializable
 {
    /** Minefield ID */
    protected EntityID  minefieldID = new EntityID(); 
 
    /** ID of entity making request */
    protected EntityID  requestingEntityID = new EntityID(); 
 
    /** Minefield sequence number */
    protected int  minefieldSequenceNumbeer;
 
    /** request ID */
    protected short  requestID;
 
    /** pdu sequence number */
    protected short  pduSequenceNumber;
 
    /** number of pdus in response */
    protected short  numberOfPdus;
 
    /** how many mines are in this PDU */
    protected short  numberOfMinesInThisPdu;
 
    /** how many sensor type are in this PDU */
    protected short  numberOfSensorTypes;
 
    /** padding */
    protected short  pad2 = 0;
 
    /** 32 boolean fields */
    protected long  dataFilter;
 
    /** Mine type */
    protected EntityType  mineType = new EntityType(); 
 
    /** Sensor types, each 16 bits long */
    protected List sensorTypes = new ArrayList(); 
    /** Padding to get things 32-bit aligned. @@@this is wrong--dyanmically sized padding needed */
    protected short  pad3;
 
    /** Mine locations */
    protected List mineLocation = new ArrayList(); 
 
 /** Constructor */
  public MinefieldDataPdu()
  {
     setPduType( (short)39 );
  }
 
 /** 
  * Constructor--takes a parallel jaxb object and returns an open-dis object 
  * 1.4_sed_bait_start */
  public MinefieldDataPdu(edu.nps.moves.jaxb.dis.MinefieldDataPdu x)
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
 
      this.minefieldSequenceNumbeer = x.getMinefieldSequenceNumbeer();
      this.requestID = x.getRequestID();
      this.pduSequenceNumber = x.getPduSequenceNumber();
      this.numberOfPdus = x.getNumberOfPdus();
      this.numberOfMinesInThisPdu = x.getNumberOfMinesInThisPdu();
      this.numberOfSensorTypes = x.getNumberOfSensorTypes();
      this.pad2 = x.getPad2();
      this.dataFilter = x.getDataFilter();
 
      edu.nps.moves.dis.EntityType foo_10;
      if(x.getMineType() == null)
         foo_10 = new edu.nps.moves.dis.EntityType();
       else
         foo_10 = new edu.nps.moves.dis.EntityType(x.getMineType() );
      this.setMineType(foo_10);
 
      this.sensorTypes = new ArrayList();
      for(int idx = 0; idx < x.getSensorTypes().size(); idx++)
      {
         this.sensorTypes.add( new edu.nps.moves.dis.TwoByteChunk((edu.nps.moves.jaxb.dis.TwoByteChunk) x.getSensorTypes().get(idx)));
      }
      this.pad3 = x.getPad3();
      this.mineLocation = new ArrayList();
      for(int idx = 0; idx < x.getMineLocation().size(); idx++)
      {
         this.mineLocation.add( new edu.nps.moves.dis.Vector3Float((edu.nps.moves.jaxb.dis.Vector3Float) x.getMineLocation().get(idx)));
      }
  }
 /* 1.4_sed_bait_end */
 
 
 /**
  * returns a jaxb object intialized from this object, given an empty jaxb object
  * 1.4_sed_bait_start **/
  public edu.nps.moves.jaxb.dis.MinefieldDataPdu initializeJaxbObject(edu.nps.moves.jaxb.dis.MinefieldDataPdu x)
  {
      super.initializeJaxbObject(x); // Call superclass initializer
 
      ObjectFactory factory = new ObjectFactory();
 
      x.setMinefieldID( this.getMinefieldID().initializeJaxbObject(factory.createEntityID()) );
      x.setRequestingEntityID( this.getRequestingEntityID().initializeJaxbObject(factory.createEntityID()) );
      x.setMinefieldSequenceNumbeer( this.getMinefieldSequenceNumbeer() );
      x.setRequestID( this.getRequestID() );
      x.setPduSequenceNumber( this.getPduSequenceNumber() );
      x.setNumberOfPdus( this.getNumberOfPdus() );
      x.setNumberOfMinesInThisPdu( this.getNumberOfMinesInThisPdu() );
      x.setNumberOfSensorTypes( this.getNumberOfSensorTypes() );
      x.setPad2( this.getPad2() );
      x.setDataFilter( this.getDataFilter() );
      x.setMineType( this.getMineType().initializeJaxbObject(factory.createEntityType()) );
 
      List sensorTypes_1 = x.getSensorTypes();
      for(int idx = 0; idx < sensorTypes.size(); idx++)
      {
          TwoByteChunk a = (edu.nps.moves.dis.TwoByteChunk)sensorTypes.get(idx);
          sensorTypes_1.add(a.initializeJaxbObject(factory.createTwoByteChunk()));
      }
      x.setPad3( this.getPad3() );
 
      List mineLocation_1 = x.getMineLocation();
      for(int idx = 0; idx < mineLocation.size(); idx++)
      {
          Vector3Float a = (edu.nps.moves.dis.Vector3Float)mineLocation.get(idx);
          mineLocation_1.add(a.initializeJaxbObject(factory.createVector3Float()));
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
    marshalSize = marshalSize + 2;  // minefieldSequenceNumbeer
    marshalSize = marshalSize + 1;  // requestID
    marshalSize = marshalSize + 1;  // pduSequenceNumber
    marshalSize = marshalSize + 1;  // numberOfPdus
    marshalSize = marshalSize + 1;  // numberOfMinesInThisPdu
    marshalSize = marshalSize + 1;  // numberOfSensorTypes
    marshalSize = marshalSize + 1;  // pad2
    marshalSize = marshalSize + 4;  // dataFilter
    marshalSize = marshalSize + mineType.getMarshalledSize();  // mineType
    for(int idx=0; idx < sensorTypes.size(); idx++)
    {
         TwoByteChunk listElement = (TwoByteChunk)sensorTypes.get(idx);
         marshalSize = marshalSize + listElement.getMarshalledSize();
    }
    marshalSize = marshalSize + 1;  // pad3
    for(int idx=0; idx < mineLocation.size(); idx++)
    {
         Vector3Float listElement = (Vector3Float)mineLocation.get(idx);
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
 
 public void setMinefieldSequenceNumbeer(int pMinefieldSequenceNumbeer)
 { minefieldSequenceNumbeer = pMinefieldSequenceNumbeer;
 }
 
 public int getMinefieldSequenceNumbeer()
 { return minefieldSequenceNumbeer; 
 }
 
 public void setRequestID(short pRequestID)
 { requestID = pRequestID;
 }
 
 public short getRequestID()
 { return requestID; 
 }
 
 public void setPduSequenceNumber(short pPduSequenceNumber)
 { pduSequenceNumber = pPduSequenceNumber;
 }
 
 public short getPduSequenceNumber()
 { return pduSequenceNumber; 
 }
 
 public void setNumberOfPdus(short pNumberOfPdus)
 { numberOfPdus = pNumberOfPdus;
 }
 
 public short getNumberOfPdus()
 { return numberOfPdus; 
 }
 
 public short getNumberOfMinesInThisPdu()
 { return (short)mineLocation.size();
 }
 
 /** Note that setting this value will ot change the marshalled value. The list whose length this describes is used for that purpose.
  * The getnumberOfMinesInThisPdu method will also be based on the actual list length rather than this value. 
  * The method is simply here for java bean completeness.
  */
 public void setNumberOfMinesInThisPdu(short pNumberOfMinesInThisPdu)
 { numberOfMinesInThisPdu = pNumberOfMinesInThisPdu;
 }
 
 public short getNumberOfSensorTypes()
 { return (short)sensorTypes.size();
 }
 
 /** Note that setting this value will ot change the marshalled value. The list whose length this describes is used for that purpose.
  * The getnumberOfSensorTypes method will also be based on the actual list length rather than this value. 
  * The method is simply here for java bean completeness.
  */
 public void setNumberOfSensorTypes(short pNumberOfSensorTypes)
 { numberOfSensorTypes = pNumberOfSensorTypes;
 }
 
 public void setPad2(short pPad2)
 { pad2 = pPad2;
 }
 
 public short getPad2()
 { return pad2; 
 }
 
 public void setDataFilter(long pDataFilter)
 { dataFilter = pDataFilter;
 }
 
 public long getDataFilter()
 { return dataFilter; 
 }
 
 public void setMineType(EntityType pMineType)
 { mineType = pMineType;
 }
 
 public EntityType getMineType()
 { return mineType; }
 
 public void setSensorTypes(List pSensorTypes)
 { sensorTypes = pSensorTypes;
 }
 
 public List getSensorTypes()
 { return sensorTypes; }
 
 public void setPad3(short pPad3)
 { pad3 = pPad3;
 }
 
 public short getPad3()
 { return pad3; 
 }
 
 public void setMineLocation(List pMineLocation)
 { mineLocation = pMineLocation;
 }
 
 public List getMineLocation()
 { return mineLocation; }
 
 
 public void marshal(DataOutputStream dos)
 {
     super.marshal(dos);
     try 
     {
        minefieldID.marshal(dos);
        requestingEntityID.marshal(dos);
        dos.writeShort( (short)minefieldSequenceNumbeer);
        dos.writeByte( (byte)requestID);
        dos.writeByte( (byte)pduSequenceNumber);
        dos.writeByte( (byte)numberOfPdus);
        dos.writeByte( (byte)mineLocation.size());
        dos.writeByte( (byte)sensorTypes.size());
        dos.writeByte( (byte)pad2);
        dos.writeInt( (int)dataFilter);
        mineType.marshal(dos);
 
        for(int idx = 0; idx < sensorTypes.size(); idx++)
        {
             TwoByteChunk aTwoByteChunk = (TwoByteChunk)sensorTypes.get(idx);
             aTwoByteChunk.marshal(dos);
        } // end of list marshalling
 
        dos.writeByte( (byte)pad3);
 
        for(int idx = 0; idx < mineLocation.size(); idx++)
        {
             Vector3Float aVector3Float = (Vector3Float)mineLocation.get(idx);
             aVector3Float.marshal(dos);
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
        minefieldSequenceNumbeer = dis.readShort();
        requestID = dis.readByte();
        pduSequenceNumber = dis.readByte();
        numberOfPdus = dis.readByte();
        numberOfMinesInThisPdu = dis.readByte();
        numberOfSensorTypes = dis.readByte();
        pad2 = dis.readByte();
        dataFilter = dis.readInt();
        mineType.unmarshal(dis);
         for(int idx = 0; idx < numberOfSensorTypes; idx++)
         {
            TwoByteChunk anX = new TwoByteChunk();
             anX.unmarshal(dis);
             sensorTypes.add(anX);
         };
 
        pad3 = dis.readByte();
         for(int idx = 0; idx < numberOfMinesInThisPdu; idx++)
         {
            Vector3Float anX = new Vector3Float();
             anX.unmarshal(dis);
             mineLocation.add(anX);
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
  public boolean equals(MinefieldDataPdu rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (minefieldID.equals( rhs.minefieldID) )) ivarsEqual = false;
      if( ! (requestingEntityID.equals( rhs.requestingEntityID) )) ivarsEqual = false;
      if( ! (minefieldSequenceNumbeer == rhs.minefieldSequenceNumbeer)) ivarsEqual = false;
      if( ! (requestID == rhs.requestID)) ivarsEqual = false;
      if( ! (pduSequenceNumber == rhs.pduSequenceNumber)) ivarsEqual = false;
      if( ! (numberOfPdus == rhs.numberOfPdus)) ivarsEqual = false;
      if( ! (numberOfMinesInThisPdu == rhs.numberOfMinesInThisPdu)) ivarsEqual = false;
      if( ! (numberOfSensorTypes == rhs.numberOfSensorTypes)) ivarsEqual = false;
      if( ! (pad2 == rhs.pad2)) ivarsEqual = false;
      if( ! (dataFilter == rhs.dataFilter)) ivarsEqual = false;
      if( ! (mineType.equals( rhs.mineType) )) ivarsEqual = false;
 
      for(int idx = 0; idx < sensorTypes.size(); idx++)
      {
         TwoByteChunk x = (TwoByteChunk)sensorTypes.get(idx);
         if( ! ( sensorTypes.get(idx).equals(rhs.sensorTypes.get(idx)))) ivarsEqual = false;
      }
 
      if( ! (pad3 == rhs.pad3)) ivarsEqual = false;
 
      for(int idx = 0; idx < mineLocation.size(); idx++)
      {
         Vector3Float x = (Vector3Float)mineLocation.get(idx);
         if( ! ( mineLocation.get(idx).equals(rhs.mineLocation.get(idx)))) ivarsEqual = false;
      }
 
 
     return ivarsEqual;
  }
 } // end of class
