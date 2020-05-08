 package cz.muni.pb138.log4j;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 
 import cz.muni.pb138.log4j.model.Configuration;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Locale;
 
 public class PropsToXmlConverter implements Converter {
 
     private static Logger log = Logger.getLogger(PropsToXmlConverter.class);
     
     public void convert(File sourceFile, File outputFile) throws FileNotFoundException, UnsupportedEncodingException, IOException{
         FileInputStream fis = new FileInputStream(sourceFile);
         FileOutputStream out = new FileOutputStream(outputFile);
         
         convert(fis,out);
     }
 
    public void convert(InputStream inputStream, OutputStream outputStream) throws UnsupportedEncodingException, IOException {
         FileInputStream fis = (FileInputStream) inputStream;
 
         Properties properties = new Properties();
         try {
             properties.load(fis);
         } catch (Exception ex) {
             log.error("Provided file is not in valid property format.");
             AppUtils.crash("Provided file is not in valid property format.");
         }
 
         // now you have properties object and you can work with it.
         Configuration configuration = new Configuration();
 
         for (String propertyKey : properties.stringPropertyNames()) {
             String key = propertyKey.toLowerCase(Locale.ENGLISH);                     
             String value = properties.getProperty(propertyKey).toLowerCase(Locale.ENGLISH);   
             // TODO: value moze obsahovat aj premennu vo ${} vyraze, tato premenna je definovana sposobom 
             // nazovPremennej = hodnota.
             if (key.toLowerCase(Locale.ENGLISH).startsWith("log4j")) {
                 String newKey = key.substring(6); // remove initial "log4j."
                 configuration.addConfig(newKey, value);
             } else {
                 AppUtils.crash("every property name must start with 'log4j': " + key);
             }
         }
 
         configuration.verify();
 
         Document document = configuration.toXML();
 
         OutputFormat format = OutputFormat.createPrettyPrint();
         XMLWriter writer = new XMLWriter(outputStream, format);
         writer.write(document);
         writer.close();
     }
 }
