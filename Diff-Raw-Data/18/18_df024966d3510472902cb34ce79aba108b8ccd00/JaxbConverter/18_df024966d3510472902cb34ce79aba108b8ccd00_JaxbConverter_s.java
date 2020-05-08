 package de.openknowledge.extensions.jsf.jaxb;
 
 import static org.apache.commons.lang3.Validate.notNull;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.convert.FacesConverter;
 import javax.xml.bind.JAXBException;
 
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.api.json.JSONJAXBContext;
 import com.sun.jersey.api.json.JSONMarshaller;
 import com.sun.jersey.api.json.JSONUnmarshaller;
 
 @FacesConverter("JaxbConverter")
 public class JaxbConverter implements Converter {
 
   private Class<?> type;
   private JSONJAXBContext jaxbContext;
   
   public JaxbConverter(Class<?> type) {
     this.type = notNull(type);
     try {
       jaxbContext = new JSONJAXBContext(JSONConfiguration.natural().build(), type);
     } catch (JAXBException e) {
       throw new IllegalArgumentException(e);
     }
   }
 
   @Override
   public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
     try {
       JSONUnmarshaller unmarshaller = jaxbContext.createJSONUnmarshaller();
      return unmarshaller.unmarshalJAXBElementFromJSON(new StringReader(value), type);
     } catch (JAXBException e) {
       throw new ConverterException(e);
     }
   }
 
   @Override
   public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
     try {
       JSONMarshaller marshaller = jaxbContext.createJSONMarshaller();
       StringWriter stringWriter = new StringWriter();
       marshaller.marshallToJSON(value, stringWriter);
       return stringWriter.toString();
     } catch (JAXBException e) {
       throw new ConverterException(e);
     }
   }
 }
