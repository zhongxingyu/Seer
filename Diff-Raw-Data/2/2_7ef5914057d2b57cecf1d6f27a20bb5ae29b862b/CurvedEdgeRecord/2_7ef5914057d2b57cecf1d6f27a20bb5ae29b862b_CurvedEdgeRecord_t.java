 package com.ciaranwood.vultan.types;
 
 import com.ciaranwood.vultan.codec.SignedNumber;
 import org.codehaus.preon.annotation.BoundNumber;
 import org.codehaus.preon.buffer.ByteOrder;
 
 public class CurvedEdgeRecord implements ShapeRecord {
 
     @BoundNumber(size = "4", byteOrder = ByteOrder.BigEndian)
     public Integer numBits;
 
     @SignedNumber(size = "numBits + 2")
     public Twip controlDeltaX;
 
     @SignedNumber(size = "numBits + 2")
     public Twip controlDeltaY;
 
     @SignedNumber(size = "numBits + 2")
     public Twip anchorDeltaX;
 
     @SignedNumber(size = "numBits + 2")
    public Twip anchorDeltaY;
 }
