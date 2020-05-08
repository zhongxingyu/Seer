 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import edu.nps.moves.jaxb.dis.*;
 
 /**
  * Section 5.3.10.1 Abstract superclass for PDUs relating to minefields. COMPLETE
  *
  * Copyright (c) 2007, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
public class MinefieldStatePdu extends MinefieldFamilyPdu implements Serializable
 {
    /** Minefield ID */
    protected EntityID  minefieldID = new EntityID(); 
 
    /** Minefield sequence */
    protected int  minefieldSequence;
 
    /** force ID */
    protected short  forceID;
 
    /** Number of permieter points */
    protected short  numberOfPerimeterPoints;
 
    /** type of minefield */
    protected EntityType  minefieldType = new EntityType(); 
 
    /** how many mine types */
    protected int  numberOfMineTypes;
 
    /** location of minefield in world coords */
    protected Vector3Double  minefieldLocation = new Vector3Double(); 
 
    /** orientation of minefield */
    protected Orientation  minefieldOrientation = new Orientation(); 
 
    /** appearance bitflags */
    protected int  appearance;
 
    /** protocolMode */
    protected int  protocolMode;
 
    /** perimeter points for the minefield */
    protected List perimeterPoints = new ArrayList(); 
    /** Type of mines */
    protected List mineType = new ArrayList(); 
 
 /** Constructor */
  public MinefieldStatePdu()
  {
     setPduType( (short)37 );
  }
 
 /** 
  * Constructor--takes a parallel jaxb object and returns an open-dis object 
  * 1.4_sed_bait_start */
  public MinefieldStatePdu(edu.nps.moves.jaxb.dis.MinefieldStatePdu x)
  {
      super(x); // Call superclass constructor
 
 
      edu.nps.moves.dis.EntityID foo_0;
      if(x.getMinefieldID() == null)
         foo_0 = new edu.nps.moves.dis.EntityID();
       else
         foo_0 = new edu.nps.moves.dis.EntityID(x.getMinefieldID() );
      this.setMinefieldID(foo_0);
 
      this.minefieldSequence = x.getMinefieldSequence();
      this.forceID = x.getForceID();
      this.numberOfPerimeterPoints = x.getNumberOfPerimeterPoints();
 
      edu.nps.moves.dis.EntityType foo_4;
      if(x.getMinefieldType() == null)
         foo_4 = new edu.nps.moves.dis.EntityType();
       else
         foo_4 = new edu.nps.moves.dis.EntityType(x.getMinefieldType() );
      this.setMinefieldType(foo_4);
 
      this.numberOfMineTypes = x.getNumberOfMineTypes();
 
      edu.nps.moves.dis.Vector3Double foo_6;
      if(x.getMinefieldLocation() == null)
         foo_6 = new edu.nps.moves.dis.Vector3Double();
       else
         foo_6 = new edu.nps.moves.dis.Vector3Double(x.getMinefieldLocation() );
      this.setMinefieldLocation(foo_6);
 
 
      edu.nps.moves.dis.Orientation foo_7;
      if(x.getMinefieldOrientation() == null)
         foo_7 = new edu.nps.moves.dis.Orientation();
       else
         foo_7 = new edu.nps.moves.dis.Orientation(x.getMinefieldOrientation() );
      this.setMinefieldOrientation(foo_7);
 
      this.appearance = x.getAppearance();
      this.protocolMode = x.getProtocolMode();
      this.perimeterPoints = new ArrayList();
      for(int idx = 0; idx < x.getPerimeterPoints().size(); idx++)
      {
         this.perimeterPoints.add( new edu.nps.moves.dis.Point((edu.nps.moves.jaxb.dis.Point) x.getPerimeterPoints().get(idx)));
      }
      this.mineType = new ArrayList();
      for(int idx = 0; idx < x.getMineType().size(); idx++)
      {
         this.mineType.add( new edu.nps.moves.dis.EntityType((edu.nps.moves.jaxb.dis.EntityType) x.getMineType().get(idx)));
      }
  }
 /* 1.4_sed_bait_end */
 
 
 /**
  * returns a jaxb object intialized from this object, given an empty jaxb object
  * 1.4_sed_bait_start **/
  public edu.nps.moves.jaxb.dis.MinefieldStatePdu initializeJaxbObject(edu.nps.moves.jaxb.dis.MinefieldStatePdu x)
  {
      super.initializeJaxbObject(x); // Call superclass initializer
 
      ObjectFactory factory = new ObjectFactory();
 
      x.setMinefieldID( this.getMinefieldID().initializeJaxbObject(factory.createEntityID()) );
      x.setMinefieldSequence( this.getMinefieldSequence() );
      x.setForceID( this.getForceID() );
      x.setNumberOfPerimeterPoints( this.getNumberOfPerimeterPoints() );
      x.setMinefieldType( this.getMinefieldType().initializeJaxbObject(factory.createEntityType()) );
      x.setNumberOfMineTypes( this.getNumberOfMineTypes() );
      x.setMinefieldLocation( this.getMinefieldLocation().initializeJaxbObject(factory.createVector3Double()) );
      x.setMinefieldOrientation( this.getMinefieldOrientation().initializeJaxbObject(factory.createOrientation()) );
      x.setAppearance( this.getAppearance() );
      x.setProtocolMode( this.getProtocolMode() );
 
      List perimeterPoints_1 = x.getPerimeterPoints();
      for(int idx = 0; idx < perimeterPoints.size(); idx++)
      {
          Point a = (edu.nps.moves.dis.Point)perimeterPoints.get(idx);
          perimeterPoints_1.add(a.initializeJaxbObject(factory.createPoint()));
      }
 
      List mineType_1 = x.getMineType();
      for(int idx = 0; idx < mineType.size(); idx++)
      {
          EntityType a = (edu.nps.moves.dis.EntityType)mineType.get(idx);
          mineType_1.add(a.initializeJaxbObject(factory.createEntityType()));
      }
    return x;
  }
 /* 1.4_sed_bait_end */
 
 
 public int getMarshalledSize()
 {
    int marshalSize = 0; 
 
    marshalSize = super.getMarshalledSize();
    marshalSize = marshalSize + minefieldID.getMarshalledSize();  // minefieldID
    marshalSize = marshalSize + 2;  // minefieldSequence
    marshalSize = marshalSize + 1;  // forceID
    marshalSize = marshalSize + 1;  // numberOfPerimeterPoints
    marshalSize = marshalSize + minefieldType.getMarshalledSize();  // minefieldType
    marshalSize = marshalSize + 2;  // numberOfMineTypes
    marshalSize = marshalSize + minefieldLocation.getMarshalledSize();  // minefieldLocation
    marshalSize = marshalSize + minefieldOrientation.getMarshalledSize();  // minefieldOrientation
    marshalSize = marshalSize + 2;  // appearance
    marshalSize = marshalSize + 2;  // protocolMode
    for(int idx=0; idx < perimeterPoints.size(); idx++)
    {
         Point listElement = (Point)perimeterPoints.get(idx);
         marshalSize = marshalSize + listElement.getMarshalledSize();
    }
    for(int idx=0; idx < mineType.size(); idx++)
    {
         EntityType listElement = (EntityType)mineType.get(idx);
         marshalSize = marshalSize + listElement.getMarshalledSize();
    }
 
    return marshalSize;
 }
 
 
 public void setMinefieldID(EntityID pMinefieldID)
 { minefieldID = pMinefieldID;
 }
 
 public EntityID getMinefieldID()
 { return minefieldID; }
 
 public void setMinefieldSequence(int pMinefieldSequence)
 { minefieldSequence = pMinefieldSequence;
 }
 
 public int getMinefieldSequence()
 { return minefieldSequence; 
 }
 
 public void setForceID(short pForceID)
 { forceID = pForceID;
 }
 
 public short getForceID()
 { return forceID; 
 }
 
 public short getNumberOfPerimeterPoints()
 { return (short)perimeterPoints.size();
 }
 
 /** Note that setting this value will ot change the marshalled value. The list whose length this describes is used for that purpose.
  * The getnumberOfPerimeterPoints method will also be based on the actual list length rather than this value. 
  * The method is simply here for java bean completeness.
  */
 public void setNumberOfPerimeterPoints(short pNumberOfPerimeterPoints)
 { numberOfPerimeterPoints = pNumberOfPerimeterPoints;
 }
 
 public void setMinefieldType(EntityType pMinefieldType)
 { minefieldType = pMinefieldType;
 }
 
 public EntityType getMinefieldType()
 { return minefieldType; }
 
 public int getNumberOfMineTypes()
 { return (int)mineType.size();
 }
 
 /** Note that setting this value will ot change the marshalled value. The list whose length this describes is used for that purpose.
  * The getnumberOfMineTypes method will also be based on the actual list length rather than this value. 
  * The method is simply here for java bean completeness.
  */
 public void setNumberOfMineTypes(int pNumberOfMineTypes)
 { numberOfMineTypes = pNumberOfMineTypes;
 }
 
 public void setMinefieldLocation(Vector3Double pMinefieldLocation)
 { minefieldLocation = pMinefieldLocation;
 }
 
 public Vector3Double getMinefieldLocation()
 { return minefieldLocation; }
 
 public void setMinefieldOrientation(Orientation pMinefieldOrientation)
 { minefieldOrientation = pMinefieldOrientation;
 }
 
 public Orientation getMinefieldOrientation()
 { return minefieldOrientation; }
 
 public void setAppearance(int pAppearance)
 { appearance = pAppearance;
 }
 
 public int getAppearance()
 { return appearance; 
 }
 
 public void setProtocolMode(int pProtocolMode)
 { protocolMode = pProtocolMode;
 }
 
 public int getProtocolMode()
 { return protocolMode; 
 }
 
 public void setPerimeterPoints(List pPerimeterPoints)
 { perimeterPoints = pPerimeterPoints;
 }
 
 public List getPerimeterPoints()
 { return perimeterPoints; }
 
 public void setMineType(List pMineType)
 { mineType = pMineType;
 }
 
 public List getMineType()
 { return mineType; }
 
 
 public void marshal(DataOutputStream dos)
 {
     super.marshal(dos);
     try 
     {
        minefieldID.marshal(dos);
        dos.writeShort( (short)minefieldSequence);
        dos.writeByte( (byte)forceID);
        dos.writeByte( (byte)perimeterPoints.size());
        minefieldType.marshal(dos);
        dos.writeShort( (short)mineType.size());
        minefieldLocation.marshal(dos);
        minefieldOrientation.marshal(dos);
        dos.writeShort( (short)appearance);
        dos.writeShort( (short)protocolMode);
 
        for(int idx = 0; idx < perimeterPoints.size(); idx++)
        {
             Point aPoint = (Point)perimeterPoints.get(idx);
             aPoint.marshal(dos);
        } // end of list marshalling
 
 
        for(int idx = 0; idx < mineType.size(); idx++)
        {
             EntityType aEntityType = (EntityType)mineType.get(idx);
             aEntityType.marshal(dos);
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
        minefieldSequence = dis.readShort();
        forceID = dis.readByte();
        numberOfPerimeterPoints = dis.readByte();
        minefieldType.unmarshal(dis);
        numberOfMineTypes = dis.readShort();
        minefieldLocation.unmarshal(dis);
        minefieldOrientation.unmarshal(dis);
        appearance = dis.readShort();
        protocolMode = dis.readShort();
         for(int idx = 0; idx < numberOfPerimeterPoints; idx++)
         {
            Point anX = new Point();
             anX.unmarshal(dis);
             perimeterPoints.add(anX);
         };
 
         for(int idx = 0; idx < numberOfMineTypes; idx++)
         {
            EntityType anX = new EntityType();
             anX.unmarshal(dis);
             mineType.add(anX);
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
  public boolean equals(MinefieldStatePdu rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (minefieldID.equals( rhs.minefieldID) )) ivarsEqual = false;
      if( ! (minefieldSequence == rhs.minefieldSequence)) ivarsEqual = false;
      if( ! (forceID == rhs.forceID)) ivarsEqual = false;
      if( ! (numberOfPerimeterPoints == rhs.numberOfPerimeterPoints)) ivarsEqual = false;
      if( ! (minefieldType.equals( rhs.minefieldType) )) ivarsEqual = false;
      if( ! (numberOfMineTypes == rhs.numberOfMineTypes)) ivarsEqual = false;
      if( ! (minefieldLocation.equals( rhs.minefieldLocation) )) ivarsEqual = false;
      if( ! (minefieldOrientation.equals( rhs.minefieldOrientation) )) ivarsEqual = false;
      if( ! (appearance == rhs.appearance)) ivarsEqual = false;
      if( ! (protocolMode == rhs.protocolMode)) ivarsEqual = false;
 
      for(int idx = 0; idx < perimeterPoints.size(); idx++)
      {
         Point x = (Point)perimeterPoints.get(idx);
         if( ! ( perimeterPoints.get(idx).equals(rhs.perimeterPoints.get(idx)))) ivarsEqual = false;
      }
 
 
      for(int idx = 0; idx < mineType.size(); idx++)
      {
         EntityType x = (EntityType)mineType.get(idx);
         if( ! ( mineType.get(idx).equals(rhs.mineType.get(idx)))) ivarsEqual = false;
      }
 
 
     return ivarsEqual;
  }
 } // end of class
