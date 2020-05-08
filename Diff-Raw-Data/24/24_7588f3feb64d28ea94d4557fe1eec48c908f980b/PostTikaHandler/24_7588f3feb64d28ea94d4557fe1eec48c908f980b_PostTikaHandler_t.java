 package com.meltmedia.rodimus;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 public class PostTikaHandler
   extends IdentityHandler
 {
   public static final String IMAGE_TAG = "img";
   public static final String SRC_ATTR = "src";
   public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
   
  public String imageDirName;
  
  public PostTikaHandler( String imageDirName ) {
    this.imageDirName = imageDirName;
  }
   
   @Override
   public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
     if( XHTML_NAMESPACE.equals(uri) && IMAGE_TAG.equalsIgnoreCase(localName) ) {
       AttributesImpl newAtts = new AttributesImpl(atts);
       int srcIndex = newAtts.getIndex(SRC_ATTR);
      if( srcIndex > -1 ) newAtts.setValue(srcIndex, newAtts.getValue(srcIndex).replaceFirst("\\Aembedded\\:", imageDirName+"/"));
       this.contentHandler.startElement(uri, localName, qName, newAtts);
     }
     else {
       super.startElement(uri, localName, qName, atts);
     }
   }
 }
