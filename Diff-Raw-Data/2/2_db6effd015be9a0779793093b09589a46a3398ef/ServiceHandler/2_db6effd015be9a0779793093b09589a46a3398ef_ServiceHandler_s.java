 package com.tomazkovacic.boilerpipe.thrift;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.nio.ByteBuffer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.thrift.TException;
 
 import com.tomazkovacic.boilerpipe.thrift.gen.ExceptionCode;
 import com.tomazkovacic.boilerpipe.thrift.gen.ExtractorException;
 import com.tomazkovacic.boilerpipe.thrift.gen.ExtractorType;
 
 import de.l3s.boilerpipe.BoilerpipeExtractor;
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import de.l3s.boilerpipe.extractors.ArticleExtractor;
 import de.l3s.boilerpipe.extractors.CanolaExtractor;
 import de.l3s.boilerpipe.extractors.DefaultExtractor;
 import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;
 import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;
 
 
 public class ServiceHandler 
 implements com.tomazkovacic.boilerpipe.thrift.gen.ExtractorService.Iface
 {
    private static Log LOG = LogFactory.getLog(ServerSettings.class);
     
     /**
      * Get stack trace as a string
      * */
     public static String getStackTrace(Throwable e){
         Writer result = new StringWriter();
         PrintWriter printWriter = new PrintWriter(result);
         e.printStackTrace(printWriter);
         return result.toString();
     }
 
     /**
      * Get extractor instance based on the enum selector
      * */
     private static BoilerpipeExtractor getExtractor(ExtractorType etype){
         BoilerpipeExtractor extractor = null;
         switch(etype){
             case DEFAULT: 
                 extractor = new DefaultExtractor();
             break;
             
             case ARTICLE: 
                 extractor = new ArticleExtractor();
             break;
                 
             case CANOLA: 
                 extractor = new CanolaExtractor();
             break;
             
             case ARTICLE_SENTENCE:
                 extractor =  new ArticleSentencesExtractor();
             break;
                 
             case DEBUG: 
                 extractor = KeepEverythingExtractor.INSTANCE;
             break;
         }
         return extractor;
     }
     
     /**
      * Extract text from bianry html data
      * 
      * @param htmlData
      *         Binary html data 
      * @param encoding
      *         Encoding of html data
      * @param etype
      *         Extractor selector
      * */
     public String extract_binary(ByteBuffer htmlData, String encoding,
             ExtractorType etype) throws ExtractorException, TException {
         
         if(htmlData.hasArray()){
             
             BoilerpipeExtractor extractor = getExtractor(etype);
             try {
                 String htmlString =  new String(htmlData.array(), encoding);
                 return extractor.getText(htmlString);
             } catch (UnsupportedEncodingException e) {
                 LOG.error("unsupported encoding " + encoding, e);
                 throw new ExtractorException(ExceptionCode.ENCODING, e.getMessage(), getStackTrace(e));
             } catch (BoilerpipeProcessingException e) {
                 LOG.error("failed to extract text using extract_binary", e);
                 throw new ExtractorException(ExceptionCode.PROCESSING,
                         e.getMessage(),getStackTrace(e));
             }
             
         }
         else{
             String msg =  "byte buffer not backed by an accseeible array";
             LOG.error(msg);
             throw new ExtractorException(ExceptionCode.GENERIC,msg, "");
         }
 
     }
 
     /**
      * Extract text from string html data
      * 
      * @param htmlString
      *         Html data 
      * @param etype
      *         Extractor selector
      * */
     public String extract_string(String htmlString, ExtractorType etype)
             throws ExtractorException, TException {
         
         BoilerpipeExtractor extractor = getExtractor(etype);
         try {
             return extractor.getText(htmlString); 
         } catch (BoilerpipeProcessingException e) {
             LOG.error("failed to extract text using extract_string", e);
             throw new ExtractorException(ExceptionCode.PROCESSING,
                     e.getMessage(),getStackTrace(e));
         }
     }
 
     /**
      * Debugging utility
      * */
     public String ping(String input) throws TException {
         LOG.debug("pong");
         return "pong";
     }
 
 }
