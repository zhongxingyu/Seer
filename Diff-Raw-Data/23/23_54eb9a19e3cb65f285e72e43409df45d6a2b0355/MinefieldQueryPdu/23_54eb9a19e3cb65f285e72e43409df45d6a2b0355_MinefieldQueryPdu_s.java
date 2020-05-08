 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import edu.nps.moves.jaxb.dis.*;
 
 /**
  * Section 5.3.10.2 Query a minefield for information about individual mines. Requires manual clean up to get the padding right. UNFINISHED
  *
  * Copyright (c) 2007, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
public class MinefieldQueryPdu extends MinfieldFamilyPdu implements Serializable
 {
    /** Minefield ID */
    protected EntityID  minefieldID = new EntityID(); 
 
    /** EID of entity making the request */
    protected EntityID  requestingEntityID = new EntityID(); 
 
    /** request ID */
    protected short  requestID;
 
    /** Number of perimeter points for the minefield */
    protected short  numberOfPerimeterPoints;
 
    /** Padding */
    protected short  pad2;
 
    /** Number of sensor types */
    protected short  numberOfSensorTypes;
 
    /** data filter, 32 boolean fields */
    protected long  dataFilter;
 
    /** Entity type of mine being requested */
    protected EntityType  requestedMineType = new EntityType(); 
 
    /** perimeter points of request */
    protected List requestedPerimeterPoints = new ArrayList(); 
    /** Sensor types, each 16 bits long */
    protected List sensorTypes = new ArrayList(); 
 
 /** Constructor */
  public MinefieldQueryPdu()
  {
     setPduType( (short)38 );
  }
 
 /** 
  * Constructor--takes a parallel jaxb object and returns an open-dis object 
  * 1.4_sed_bait_start */
  public MinefieldQueryPdu(edu.nps.moves.jaxb.dis.MinefieldQueryPdu x)
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
      this.numberOfPerimeterPoints = x.getNumberOfPerimeterPoints();
      this.pad2 = x.getPad2();
      this.numberOfSensorTypes = x.getNumberOfSensorTypes();
      this.dataFilter = x.getDataFilter();
 
      edu.nps.moves.dis.EntityType foo_7;
      if(x.getRequestedMineType() == null)
         foo_7 = new edu.nps.moves.dis.EntityType();
       else
         foo_7 = new edu.nps.moves.dis.EntityType(x.getRequestedMineType() );
      this.setRequestedMineType(foo_7);
 
      this.requestedPerimeterPoints = new ArrayList();
      for(int idx = 0; idx < x.getRequestedPerimeterPoints().size(); idx++)
      {
         this.requestedPerimeterPoints.add( new edu.nps.moves.dis.Point((edu.nps.moves.jaxb.dis.Point) x.getRequestedPerimeterPoints().get(idx)));
      }
      this.sensorTypes = new ArrayList();
      for(int idx = 0; idx < x.getSensorTypes().size(); idx++)
      {
         this.sensorTypes.add( new edu.nps.moves.dis.TwoByteChunk((edu.nps.moves.jaxb.dis.TwoByteChunk) x.getSensorTypes().get(idx)));
      }
  }
 /* 1.4_sed_bait_end */
 
 
 /**
  * returns a jaxb object intialized from this object, given an empty jaxb object
  * 1.4_sed_bait_start **/
  public edu.nps.moves.jaxb.dis.MinefieldQueryPdu initializeJaxbObject(edu.nps.moves.jaxb.dis.MinefieldQueryPdu x)
  {
      super.initializeJaxbObject(x); // Call superclass initializer
 
      ObjectFactory factory = new ObjectFactory();
 
      x.setMinefieldID( this.getMinefieldID().initializeJaxbObject(factory.createEntityID()) );
      x.setRequestingEntityID( this.getRequestingEntityID().initializeJaxbObject(factory.createEntityID()) );
      x.setRequestID( this.getRequestID() );
      x.setNumberOfPerimeterPoints( this.getNumberOfPerimeterPoints() );
      x.setPad2( this.getPad2() );
      x.setNumberOfSensorTypes( this.getNumberOfSensorTypes() );
      x.setDataFilter( this.getDataFilter() );
      x.setRequestedMineType( this.getRequestedMineType().initializeJaxbObject(factory.createEntityType()) );
 
      List requestedPerimeterPoints_1 = x.getRequestedPerimeterPoints();
      for(int idx = 0; idx < requestedPerimeterPoints.size(); idx++)
      {
          Point a = (edu.nps.moves.dis.Point)requestedPerimeterPoints.get(idx);
          requestedPerimeterPoints_1.add(a.initializeJaxbObject(factory.createPoint()));
      }
 
      List sensorTypes_1 = x.getSensorTypes();
      for(int idx = 0; idx < sensorTypes.size(); idx++)
      {
          TwoByteChunk a = (edu.nps.moves.dis.TwoByteChunk)sensorTypes.get(idx);
          sensorTypes_1.add(a.initializeJaxbObject(factory.createTwoByteChunk()));
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
    marshalSize = marshalSize + 1;  // numberOfPerimeterPoints
    marshalSize = marshalSize + 1;  // pad2
    marshalSize = marshalSize + 1;  // numberOfSensorTypes
    marshalSize = marshalSize + 4;  // dataFilter
    marshalSize = marshalSize + requestedMineType.getMarshalledSize();  // requestedMineType
    for(int idx=0; idx < requestedPerimeterPoints.size(); idx++)
    {
         Point listElement = (Point)requestedPerimeterPoints.get(idx);
         marshalSize = marshalSize + listElement.getMarshalledSize();
    }
    for(int idx=0; idx < sensorTypes.size(); idx++)
    {
         TwoByteChunk listElement = (TwoByteChunk)sensorTypes.get(idx);
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
 
 public short getNumberOfPerimeterPoints()
 { return (short)requestedPerimeterPoints.size();
 }
 
 /** Note that setting this value will ot change the marshalled value. The list whose length this describes is used for that purpose.
  * The getnumberOfPerimeterPoints method will also be based on the actual list length rather than this value. 
  * The method is simply here for java bean completeness.
  */
 public void setNumberOfPerimeterPoints(short pNumberOfPerimeterPoints)
 { numberOfPerimeterPoints = pNumberOfPerimeterPoints;
 }
 
 public void setPad2(short pPad2)
 { pad2 = pPad2;
 }
 
 public short getPad2()
 { return pad2; 
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
 
 public void setDataFilter(long pDataFilter)
 { dataFilter = pDataFilter;
 }
 
 public long getDataFilter()
 { return dataFilter; 
 }
 
 public void setRequestedMineType(EntityType pRequestedMineType)
 { requestedMineType = pRequestedMineType;
 }
 
 public EntityType getRequestedMineType()
 { return requestedMineType; }
 
 public void setRequestedPerimeterPoints(List pRequestedPerimeterPoints)
 { requestedPerimeterPoints = pRequestedPerimeterPoints;
 }
 
 public List getRequestedPerimeterPoints()
 { return requestedPerimeterPoints; }
 
 public void setSensorTypes(List pSensorTypes)
 { sensorTypes = pSensorTypes;
 }
 
 public List getSensorTypes()
 { return sensorTypes; }
 
 
 public void marshal(DataOutputStream dos)
 {
     super.marshal(dos);
     try 
     {
        minefieldID.marshal(dos);
        requestingEntityID.marshal(dos);
        dos.writeByte( (byte)requestID);
        dos.writeByte( (byte)requestedPerimeterPoints.size());
        dos.writeByte( (byte)pad2);
        dos.writeByte( (byte)sensorTypes.size());
        dos.writeInt( (int)dataFilter);
        requestedMineType.marshal(dos);
 
        for(int idx = 0; idx < requestedPerimeterPoints.size(); idx++)
        {
             Point aPoint = (Point)requestedPerimeterPoints.get(idx);
             aPoint.marshal(dos);
        } // end of list marshalling
 
 
        for(int idx = 0; idx < sensorTypes.size(); idx++)
        {
             TwoByteChunk aTwoByteChunk = (TwoByteChunk)sensorTypes.get(idx);
             aTwoByteChunk.marshal(dos);
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
        numberOfPerimeterPoints = dis.readByte();
        pad2 = dis.readByte();
        numberOfSensorTypes = dis.readByte();
        dataFilter = dis.readInt();
        requestedMineType.unmarshal(dis);
         for(int idx = 0; idx < numberOfPerimeterPoints; idx++)
         {
            Point anX = new Point();
             anX.unmarshal(dis);
             requestedPerimeterPoints.add(anX);
         };
 
         for(int idx = 0; idx < numberOfSensorTypes; idx++)
         {
            TwoByteChunk anX = new TwoByteChunk();
             anX.unmarshal(dis);
             sensorTypes.add(anX);
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
  public boolean equals(MinefieldQueryPdu rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (minefieldID.equals( rhs.minefieldID) )) ivarsEqual = false;
      if( ! (requestingEntityID.equals( rhs.requestingEntityID) )) ivarsEqual = false;
      if( ! (requestID == rhs.requestID)) ivarsEqual = false;
      if( ! (numberOfPerimeterPoints == rhs.numberOfPerimeterPoints)) ivarsEqual = false;
      if( ! (pad2 == rhs.pad2)) ivarsEqual = false;
      if( ! (numberOfSensorTypes == rhs.numberOfSensorTypes)) ivarsEqual = false;
      if( ! (dataFilter == rhs.dataFilter)) ivarsEqual = false;
      if( ! (requestedMineType.equals( rhs.requestedMineType) )) ivarsEqual = false;
 
      for(int idx = 0; idx < requestedPerimeterPoints.size(); idx++)
      {
         Point x = (Point)requestedPerimeterPoints.get(idx);
         if( ! ( requestedPerimeterPoints.get(idx).equals(rhs.requestedPerimeterPoints.get(idx)))) ivarsEqual = false;
      }
 
 
      for(int idx = 0; idx < sensorTypes.size(); idx++)
      {
         TwoByteChunk x = (TwoByteChunk)sensorTypes.get(idx);
         if( ! ( sensorTypes.get(idx).equals(rhs.sensorTypes.get(idx)))) ivarsEqual = false;
      }
 
 
     return ivarsEqual;
  }
 } // end of class
