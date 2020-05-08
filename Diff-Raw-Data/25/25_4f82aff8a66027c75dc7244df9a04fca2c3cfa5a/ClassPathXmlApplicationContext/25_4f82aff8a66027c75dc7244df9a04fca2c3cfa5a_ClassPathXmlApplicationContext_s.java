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
 
 public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {
    
    private static final Logger LOG = LoggerFactory.getLogger(
          ClassPathXmlApplicationContext.class);
    
    ////
    
    /**
     * Create a new ClassPathXmlApplicationContext for bean-style configuration.
     * @see #setConfigLocation
     * @see #setConfigLocations
     * @see #afterPropertiesSet()
     */
    public ClassPathXmlApplicationContext() {
       //nothing
    }
    
    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML file and automatically refreshing the context.
     * @param configLocation resource location
     * @throws BeansException if context creation failed
     */
   public ClassPathXmlApplicationContext(String configLocation) {
	   super(configLocation);
    }
 
    protected void processContext(String location) {
       LOG.trace("Processing context: " + location);
       getContextStack().push(location);
       XmlPullParserFactory factory = null;
       try {
          factory = XmlPullParserFactory.newInstance(
                System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
       } catch (XmlPullParserException exp) {
          LOG.error(exp.getMessage());
          LOG.error("Error processing Context: " + location);
          LOG.debug("Details: ", exp);
       }
       factory.setNamespaceAware(true);
       try {
          XmlPullParser xpp = factory.newPullParser();
          Reader reader = getResorceManager().getReader(
                new File(getResourceResolver().resolvePath(location)));
          xpp.setInput(reader);
          
          processDocument(xpp);
          
          reader.close();
       } catch (XmlPullParserException exp) {
          LOG.error(exp.getMessage());
          LOG.error("Error processing Context: " + location);
          LOG.debug("Details: ", exp);
       } catch (IOException exp) {
          LOG.error(exp.getMessage());
          LOG.error("Error processing Context: " + location);
          LOG.debug("Details: ", exp);
       }
       getContextStack().push(location);
    }
 }
