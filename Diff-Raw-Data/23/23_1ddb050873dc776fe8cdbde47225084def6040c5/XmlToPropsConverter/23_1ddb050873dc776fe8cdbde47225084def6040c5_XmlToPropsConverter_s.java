 package cz.muni.pb138.log4j;
 
 import cz.muni.pb138.log4j.model.Configuration;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.Element;
 
 
 
 public class XmlToPropsConverter implements Converter {
     private static Logger log = Logger.getLogger(XmlToPropsConverter.class);
     private Configuration configuration;
     
 
     public void convert(File sourceFile, File outputFile) throws FileNotFoundException, UnsupportedEncodingException, IOException{
         FileInputStream fis = new FileInputStream(sourceFile);
         FileOutputStream out = new FileOutputStream(outputFile);
         
         convert(fis,out);
     }
     
    public void convert(InputStream inputStream, OutputStream outputStream) throws UnsupportedEncodingException, IOException {
         DtdValidator dtdValidator = new DtdValidator(inputStream);
         
         Document document = dtdValidator.validate();
         
         if (document == null) {
             AppUtils.crash("Provided file is not valid log4j xml configuration.");
         }
         
         Element rootElement = document.getRootElement();
         
         configuration = new Configuration();
         
         configuration.setUpFromElement(rootElement);
         
         configuration.verify();
                 
         //writing out
         
         List<String> prop = configuration.toProperty();
         AppUtils.store(prop, outputStream);
     }
 }
