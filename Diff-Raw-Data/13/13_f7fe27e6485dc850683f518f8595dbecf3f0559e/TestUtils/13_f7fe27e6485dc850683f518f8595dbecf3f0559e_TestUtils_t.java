 package cc.explain.server.utils;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.mock.jndi.SimpleNamingContextBuilder;
 
 import javax.naming.NamingException;
 import java.io.*;
 import java.util.Properties;
 
 /**
  * User: kzimnick
  * Date: 02.08.13
  * Time: 18:00
  */
 public class TestUtils {
 
     private TestUtils(){}
 
        private static Logger LOG = LoggerFactory.getLogger(TestUtils.class);

     public static void initTestJNDIFromPropertiesFile() throws IOException, NamingException {
         SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
         Properties prop = new Properties();
        String homePath = System.getProperty("user.home");
        String configPath = String.format("%s/.ecc/jndi.properties", homePath);
        LOG.info(configPath);
        InputStream stream = new FileInputStream(configPath);
         prop.load(stream);
         stream.close();
 
         for(Object key : prop.keySet()){
             builder.bind((String)key, prop.get(key));
         }
         builder.activate();
     }
 
 }
