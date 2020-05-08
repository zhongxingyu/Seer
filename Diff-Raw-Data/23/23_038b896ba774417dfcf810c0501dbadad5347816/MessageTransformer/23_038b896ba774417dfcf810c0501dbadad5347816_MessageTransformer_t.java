package jamex.link;
 
 import java.io.ByteArrayOutputStream;
 import java.io.StringReader;
 import java.net.URL;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 public abstract class MessageTransformer
 {
   public abstract Message transformMessage( final Session session,
                                             final Message message )
       throws Exception;
 
   public static MessageTransformer newXSLTransformer( final URL url )
       throws Exception
   {
     if( null == url ) throw new NullPointerException( "url" );
 
     final Source xsltSource = new StreamSource( url.openStream() );
     final TransformerFactory transFact = TransformerFactory.newInstance();
     final Transformer transformer = transFact.newTransformer( xsltSource );
 
     return new XslMessageVerifier( transformer );
   }
 
   private static class XslMessageVerifier
       extends MessageTransformer
   {
     private final Transformer transformer;
 
     private XslMessageVerifier( final Transformer transformer )
     {
       this.transformer = transformer;
     }
 
     @Override
     public Message transformMessage( final Session session,
                                      final Message message )
         throws Exception
     {
       final TextMessage textMessage = MessageUtil.castToType( message, TextMessage.class );
       final String text = transformText( textMessage );
 
       final TextMessage result = session.createTextMessage( text );
       MessageUtil.copyMessageHeaders( textMessage, result );
 
       return result;
     }
 
     private String transformText( final TextMessage textMessage )
         throws JMSException, TransformerException
     {
       final Source xmlSource = new StreamSource( new StringReader( textMessage.getText() ) );
       final ByteArrayOutputStream baos = new ByteArrayOutputStream();
       final Result result = new StreamResult( baos );
       transformer.transform( xmlSource, result );
       final String text = baos.toString();
       return text;
     }
   }
 }
