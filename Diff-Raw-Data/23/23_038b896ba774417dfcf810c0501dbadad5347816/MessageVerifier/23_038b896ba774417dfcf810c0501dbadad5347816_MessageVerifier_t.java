package jamex.link;
 
 import java.io.ByteArrayInputStream;
 import java.net.URL;
 import java.util.regex.Pattern;
 import javax.jms.Message;
 import javax.jms.TextMessage;
 import javax.xml.XMLConstants;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 public abstract class MessageVerifier
 {
   public abstract void verifyMessage( final Message message ) throws Exception;
 
   public static MessageVerifier newRelaxNGVerifier( final URL url )
       throws Exception
   {
     return newXmlVerifier( XMLConstants.RELAXNG_NS_URI, url );
   }
 
   public static MessageVerifier newDTDVerifier( final URL url )
       throws Exception
   {
     return newXmlVerifier( XMLConstants.XML_DTD_NS_URI, url );
   }
 
   public static MessageVerifier newXSDVerifier( final URL url )
       throws Exception
   {
     return newXmlVerifier( XMLConstants.W3C_XML_SCHEMA_NS_URI, url );
   }
 
   public static MessageVerifier newRegexVerifier( final String pattern )
       throws Exception
   {
     return new RegexMessageVerifier( Pattern.compile( pattern ) );
   }
 
   public static MessageVerifier newRegexVerifier( final Pattern pattern )
       throws Exception
   {
     return new RegexMessageVerifier( pattern );
   }
 
   private static MessageVerifier newXmlVerifier( final String schemaLanguage, final URL url )
       throws Exception
   {
     if( null == schemaLanguage ) throw new NullPointerException( "schemaLanguage" );
     if( null == url ) throw new NullPointerException( "url" );
     final SchemaFactory factory = SchemaFactory.newInstance( schemaLanguage );
     final Schema schema = factory.newSchema( url );
     return new XmlMessageVerifier( schema.newValidator() );
   }
 
   private static class XmlMessageVerifier
       extends MessageVerifier
   {
     private final Validator validator;
 
     private XmlMessageVerifier( final Validator validator )
     {
       this.validator = validator;
     }
 
     public void verifyMessage( final Message message ) throws Exception
     {
       final TextMessage textMessage = MessageUtil.castToType( message, TextMessage.class );
       validator.validate( new StreamSource( new ByteArrayInputStream( textMessage.getText().getBytes() ) ) );
     }
   }
 
   private static class RegexMessageVerifier
       extends MessageVerifier
   {
     private final Pattern pattern;
 
     private RegexMessageVerifier( final Pattern pattern )
     {
       this.pattern = pattern;
     }
 
     public void verifyMessage( final Message message ) throws Exception
     {
       final TextMessage textMessage = MessageUtil.castToType( message, TextMessage.class );
       if( !pattern.matcher( textMessage.getText() ).matches() )
       {
         throw new Exception( "Message failed to match pattern: " + pattern.pattern() + ". Message: " + message );
       }
     }
   }
 }
 
