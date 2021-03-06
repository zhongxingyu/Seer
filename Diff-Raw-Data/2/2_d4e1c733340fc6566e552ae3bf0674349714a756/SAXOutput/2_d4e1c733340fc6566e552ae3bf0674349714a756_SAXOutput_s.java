 package com.sun.xml.bind.v2.runtime.output;
 
 import java.io.IOException;
 
 import javax.xml.stream.XMLStreamException;
 
 import com.sun.xml.bind.util.AttributesImpl;
 import com.sun.xml.bind.v2.runtime.XMLSerializer;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.LocatorImpl;
 
 /**
  * {@link XmlOutput} implementation that writes to SAX {@link ContentHandler}.
  *
  * @author Kohsuke Kawaguchi
  */
 public class SAXOutput extends XmlOutput {
     protected final ContentHandler out;
 
     public SAXOutput(ContentHandler out) {
         this.out = out;
         out.setDocumentLocator(new LocatorImpl());
     }
 
     private String elementNsUri,elementLocalName,elementQName;
 
     private char[] buf = new char[256];
 
     private final AttributesImpl atts = new AttributesImpl();
 
 
     // not called if we are generating fragments
     @Override
     public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws SAXException, IOException, XMLStreamException {
         super.startDocument(serializer, fragment,nsUriIndex2prefixIndex,nsContext);
         if(!fragment)
             out.startDocument();
     }
 
     public void endDocument(boolean fragment) throws SAXException, IOException, XMLStreamException {
         if(!fragment)
             out.endDocument();
         super.endDocument(fragment);
     }
 
     public void beginStartTag(int prefix, String localName) {
         elementNsUri = nsContext.getNamespaceURI(prefix);
         elementLocalName = localName;
         elementQName = getQName(prefix,localName);
         atts.clear();
     }
 
     public void attribute(int prefix, String localName, String value) {
         String qname;
         String nsUri;
         if(prefix==-1) {
             nsUri = "";
             qname = localName;
         } else {
             nsUri = nsContext.getNamespaceURI(prefix);
             qname = nsContext.getPrefix(prefix)+':'+localName;
         }
         atts.addAttribute( nsUri, localName, qname, "CDATA", value );
     }
 
     public void endStartTag() throws SAXException {
         NamespaceContextImpl.Element ns = nsContext.getCurrent();
         if(ns!=null) {
             int sz = ns.count();
             for( int i=0; i<sz; i++ ) {
                 String p = ns.getPrefix(i);
                 String uri = ns.getNsUri(i);
                 if(uri.length()==0 && ns.getBase()==1)
                    continue;   // no point in definint xmlns='' on the root
                 out.startPrefixMapping(p,uri);
             }
         }
         out.startElement(elementNsUri,elementLocalName,elementQName,atts);
     }
 
     public void endTag(int prefix, String localName) throws SAXException {
         out.endElement(
             nsContext.getNamespaceURI(prefix),
             localName,
             getQName(prefix, localName)
         );
 
         NamespaceContextImpl.Element ns = nsContext.getCurrent();
         if(ns!=null) {
             int sz = ns.count();
             for( int i=sz-1; i>=0; i-- ) {
                 String p = ns.getPrefix(i);
                 String uri = ns.getNsUri(i);
                 if(uri.length()==0 && ns.getBase()==1)
                     continue;   // no point in definint xmlns='' on the root
                 out.endPrefixMapping(p);
             }
         }
     }
 
     private String getQName(int prefix, String localName) {
         String qname;
         String p = nsContext.getPrefix(prefix);
         if(p.length()==0)
             qname = localName;
         else
             qname = p+':'+localName;
         return qname;
     }
 
     public void text(CharSequence value, boolean needsSP) throws SAXException {
         int vlen = value.length();
         if(buf.length<=vlen) {
             buf = new char[Math.max(buf.length*2,vlen+1)];
         }
         if(needsSP) {
             value.toString().getChars(0,vlen,buf,1);
             buf[0] = ' ';
         } else {
             value.toString().getChars(0,vlen,buf,0);
         }
         out.characters(buf,0,vlen+(needsSP?1:0));
     }
 
 }
