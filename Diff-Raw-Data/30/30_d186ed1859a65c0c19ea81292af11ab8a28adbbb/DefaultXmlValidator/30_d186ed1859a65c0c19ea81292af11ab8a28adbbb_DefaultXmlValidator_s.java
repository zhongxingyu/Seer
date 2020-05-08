 package net.soemirno.xmlvalidator;
 
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 
 import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
 import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import java.io.File;
 import java.io.IOException;
 import static java.text.MessageFormat.format;
 
 /**
  * Default xml validator.
  */
 public class DefaultXmlValidator implements XmlValidator {
 
    private Logger logger;
    private Validator validator;
 
     DefaultXmlValidator(final File schema, final Logger defaultLogger) {
         this(schema, defaultLogger, new LoggingErrorHandler(defaultLogger));
     }
 
     DefaultXmlValidator(final File schema, final Logger defaultLogger, final ErrorHandler handler) {
         this.validator = createValidator(schema, handler);
         this.logger = defaultLogger;
         logger.info(format("validator created with {0}.", schema.getName()));
     }
 
     public DefaultXmlValidator(final File schema) {
        this(schema, LoggerFactory.getLogger(DefaultXmlValidator.class));
     }
 
    static Validator createValidator(final File schema, final ErrorHandler errorHandler) {
        final SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
         try {
            final Validator validator = factory.newSchema(new StreamSource(schema)).newValidator();
             if (errorHandler != null) {
                 validator.setErrorHandler(errorHandler);
             }
             return validator;
         } catch (SAXException e) {
             throw new XmlValidator.XmlValidatorException(e);
         }
     }
 
     public void validate(final File xml) {
         logger.info(format("validating {0}.", xml.getName()));
 
         try {
             validator.validate(new StreamSource(xml));
         } catch (SAXException e) {
             logger.error(e.getMessage(), e);
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
         }
 
         logger.info(format("finished validating {0}.", xml.getName()));
     }
 
 }
