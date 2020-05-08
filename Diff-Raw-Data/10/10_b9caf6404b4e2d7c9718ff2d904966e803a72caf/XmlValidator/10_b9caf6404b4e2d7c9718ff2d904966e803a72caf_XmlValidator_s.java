 package edu.northwestern.bioinformatics.studycalendar.utils.validators;
 
 import org.springframework.validation.Validator;
 import org.springframework.validation.Errors;
 import org.xml.sax.SAXException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.xml.validation.SchemaFactory;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import java.net.URL;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class XmlValidator implements Validator {
     private static Logger log = LoggerFactory.getLogger(XmlValidator.class);
 
     private Schema schema;
 
     public XmlValidator(Schema schema) {
         this.schema = schema;
     }
 
     public boolean supports(Class aClass) {
         return InputStream.class.isAssignableFrom(aClass);
     }
 
     public void validate(Object o, Errors errors) {
         InputStream input = (InputStream) o;
 
         // 1. Specify you want a factory for XSD
         SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 
         // 2. Load the specific schema you want.
         File uncompiledSchema = getSchema();
 
         try {
             // 3. Compile the schema.
             javax.xml.validation.Schema schema = factory.newSchema(uncompiledSchema);
 
             // 4. Get a validator from the schema.
             javax.xml.validation.Validator validator = schema.newValidator();
 
             // 5. Parse the document you want to check.
             Source source = new StreamSource(input);
 
             // 6. Check the document
             validator.validate(source);
             log.info("{} file is valid.", getSchemaTitle());
         }
         catch (SAXException ex) {
             // TODO: get cause of SaxException and display in form other than cryptic SaxException message.
            errors.reject("error.file.not.valid", getSchemaTitle());
             log.debug("Activities file {} is not valid because {}", getSchemaTitle(), ex.getMessage());
         }
         catch (IOException ioe) {
            errors.reject("error.problem.reading.file", getSchemaTitle());
             log.debug("Error reading file {} because {}", getSchemaTitle(), ioe.getMessage());
         }
     }
 
     protected File getSchema() {
         URL schemaLocation = getClass().getClassLoader().getResource(schema.fileName());
         return new File(schemaLocation.getFile());
     }
 
     protected String getSchemaTitle() {
         return schema.title();
     }
 }
