 package net.zeroinstall.publish;
 
 import static com.google.common.io.BaseEncoding.base64;
 import java.io.IOException;
 import net.zeroinstall.model.InterfaceDocument;
 import org.apache.xmlbeans.XmlCursor;
 import org.apache.xmlbeans.XmlOptions;
 
 /**
  * Utility class for performing operations on feed files.
  */
 public final class FeedUtils {
 
     private FeedUtils() {
     }
 
     /**
      * Serializes a feed to an XML string with a stylesheet declaration and an
      * optional GnuPG signature.
      *
      * @param feed Thee feed to be serialized.
      * @param gnuPGKey The name of the GnuPG key to use for
      * signing. <code>null</code> for no signature.
      * @return The generated XML string.
     * @throws IOException A problem occured while calling the GnuPG executable.
      */
     public static String getFeedString(InterfaceDocument feed, String gnuPGKey) throws IOException {
         addStylesheet(feed);
         String xmlText = toXmlText(feed);
         return (gnuPGKey == null) ? xmlText : appendSignature(xmlText, gnuPGKey);
     }
 
     static void addStylesheet(InterfaceDocument feed) {
         XmlCursor cursor = feed.newCursor();
         cursor.toNextToken();
         cursor.insertProcInst("xml-stylesheet", "type='text/xsl' href='interface.xsl'");
     }
 
     static String toXmlText(InterfaceDocument feed) {
         return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                 + feed.xmlText(new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
     }
 
     static String appendSignature(String xmlText, String gnuPGKey) throws IOException {
         xmlText += "\n";
 
         String signature = base64().encode(GnuPG.detachSign(xmlText, gnuPGKey));
         return xmlText + "<!-- Base64 Signature\n" + signature + "\n-->\n";
     }
 }
