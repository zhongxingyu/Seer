 /**
  * 
  */
 package com.cargosmart.b2b.edi.common.x12;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cargosmart.b2b.edi.common.Document;
 import com.cargosmart.b2b.edi.common.InterchangeEnvelope;
 import com.cargosmart.b2b.edi.common.Segment;
 
 /**
  * An EDI document. It is the root element to access EDI message.
  * 
  * @author Raymond Chin
  * 
  */
 public class X12Document implements Document {
 
 	private InterchangeEnvelope interchange;
     private String segmentSeparator;
     private String elementSeparator;
     private String subElementSeparator;
 
 	public X12Document() {
 
 	}
 	
 	/**
 	 * Copy constructor
 	 * 
 	 * @param orig
 	 */
 	public X12Document(X12Document orig) {
 		this.segmentSeparator = orig.segmentSeparator;
 		this.elementSeparator = orig.elementSeparator;
 		this.subElementSeparator = orig.subElementSeparator;
 	}
 	
 	/* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#setInterchangeEnvelope(com.cargosmart.b2b.edi.common.x12.InterchangeEnvelope)
      */
 	public Document setInterchangeEnvelope(InterchangeEnvelope interchange) {
 		this.interchange = interchange;
 		interchange.setDocument(this);
 		return this;
 	}
 
 	/* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#getInterchangeEnvelope()
      */
 	public InterchangeEnvelope getInterchangeEnvelope() {
 		return interchange;
 	}
 
 	/* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#setElementSeparator(java.lang.String)
      */
     public void setElementSeparator(String elementSeparator) {
         this.elementSeparator = elementSeparator;        
     }
     
     /* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#getElementSeparator()
      */
     public String getElementSeparator() {
         return elementSeparator;
     }
 
     /* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#getSegmentSeparator()
      */
     public String getSegmentSeparator() {
         return segmentSeparator;
     }
 
     /* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#setSegmentSeparator(java.lang.String)
      */
     public void setSegmentSeparator(String segmentSeparator) {
         this.segmentSeparator = segmentSeparator;
     }
 
     /* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#getSubElementSeparator()
      */
     public String getSubElementSeparator() {
        if (interchange != null) {
            subElementSeparator = interchange.getField(16).getValue();
        }
         return subElementSeparator;
     }
 
     /* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#setSubElementSeparator(java.lang.String)
      */
     public void setSubElementSeparator(String subElementSeparator) {
         this.subElementSeparator = subElementSeparator;
        if (interchange != null) {
            interchange.getField(16).setValue(subElementSeparator);
        }
     }
 
 	/* (non-Javadoc)
      * @see com.cargosmart.b2b.edi.common.x12.DocumentI#getSegment(java.lang.String)
      */
 	public List<Segment> getSegment(String tag) {
 		List<Segment> segments = new ArrayList<Segment>();
 		segments.addAll(interchange.getSegment(tag));
 		return segments;
 	}
 
 	/**
 	 * X12Document doesn't support release character
 	 * 
 	 * @return null
 	 */
     public String getReleaseCharacter() {
         return null;
     }
 
     /**
      * X12Document doesn't support release character
      */
     public void setReleaseCharacter(String releaseCharacter) {
         // do nothing
     }
 
 	public Document copy() {
 		return new X12Document(this);
 	}
 }
