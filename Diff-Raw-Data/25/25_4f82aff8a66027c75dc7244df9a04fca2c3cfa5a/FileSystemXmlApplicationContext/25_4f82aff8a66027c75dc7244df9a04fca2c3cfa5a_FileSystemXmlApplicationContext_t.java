 package org.springframework.context.support;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 public class FileSystemXmlApplicationContext extends AbstractXmlApplicationContext {
    
    private static final Logger LOG = LoggerFactory.getLogger(
          FileSystemXmlApplicationContext.class);
    
    /**
     * Create a new ClassPathXmlApplicationContext for bean-style configuration.
     * @see #setConfigLocation
     * @see #setConfigLocations
     * @see #afterPropertiesSet()
     */
    public FileSystemXmlApplicationContext() {
       super();
    }
    
    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML file and automatically refreshing the context.
     * @param configLocation resource location
     * @throws BeansException if context creation failed
     */
   public FileSystemXmlApplicationContext(String ... configLocations) {
      super(configLocations);
    }
    
    protected void processContext(String location) {
       XmlPullParserFactory factory = null;
       try {
          factory = XmlPullParserFactory.newInstance(
                System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
       } catch (XmlPullParserException exp) {
          LOG.error(exp.getMessage());
          LOG.debug("Details: ", exp);
       }
       factory.setNamespaceAware(true);
       try {
          XmlPullParser xpp = factory.newPullParser();
          Reader reader = getResorceManager().getReader(new File(location));
          xpp.setInput(reader);
          
          processDocument(xpp);
          
          reader.close();
       } catch (XmlPullParserException exp) {
          LOG.error(exp.getMessage());
          LOG.debug("Details: ", exp);
       } catch (IOException exp) {
          LOG.error(exp.getMessage());
          LOG.debug("Details: ", exp);
       }
    }
    
 }
