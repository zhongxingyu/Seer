 package com.hifiremote.jp1;
 
 import java.awt.Color;
 import java.util.Properties;
 
 public class Highlight
 {
   public Highlight() {};
   
   public Highlight( Properties props )
   {
     // SegmentFlags is omitted if it is 0 (which it is for JP1.3 and earlier as it is not used by them )
     String temp = props.getProperty( "SegmentFlags" );
     segmentFlags = temp == null ? 0 : Integer.parseInt( temp );
   }
   
   private int memoryUsage = 0;
   
   private Color highlight = Color.WHITE;
   
   private Segment segment = null;
   
   private int segmentFlags = 0;
   
   private int index = 0;
   
   public Color getHighlight()
   {
     return highlight;
   }
   
   public void setHighlight( Color highlight )
   {
     this.highlight = highlight;
   }
   
   public int getMemoryUsage()
   {
     return memoryUsage;
   }
 
   public void clearMemoryUsage()
   {
     memoryUsage = 0;
   }
   
   public void setMemoryUsage( int memoryUsage )
   {
     this.memoryUsage = memoryUsage;
   }
   
   public void addMemoryUsage( int memoryUsage )
   {
     this.memoryUsage += memoryUsage;
   }
 
   public Segment getSegment()
   {
     return segment;
   }
 
   public void setSegment( Segment segment )
   {
     this.segment = segment;
   }
   
   public void setSegment( Segment segment, int index )
   {
     this.segment = segment;
     this.index = index;
   }
 
   public int getIndex()
   {
     return index;
   }
 
   public int getSegmentFlags()
   {
     return segmentFlags;
   }
 
   public void setSegmentFlags( int segmentFlags )
   {
     this.segmentFlags = segmentFlags;
   }
   
   public void store( PropertyWriter pw )
   {
     if ( segmentFlags > 0 )
     {
       pw.print( "SegmentFlags", segmentFlags );
     }
   }
 
 }
