 package no.feide.moria.directory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 import no.feide.moria.directory.DirectoryManagerConfigurationException;
 import no.feide.moria.log.MessageLogger;
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 /**
  * Configuration handler for the Directory Manager. Parses the configuration
  * file.
  */
 public class DirectoryManagerConfiguration {
 
     /** The message logger. */
     private final MessageLogger log = new MessageLogger(DirectoryManagerConfiguration.class);
 
     /** The location of the index file. */
     private String indexFilename;
 
     /** The index update frequency, in milliseconds. */
     private long indexUpdateFrequency;
 
     /** Internal representation of the backend class. */
     private Class backendFactoryClass;
 
     /** Internal representation of the backend configuration file structure. */
     private Element backendConfiguration = null;
 
     /**
      * The required configuration file property, for external reference.
      * Currently contains the value
      * <code>no.feide.moria.directory.configuration</code>.
      */
     public final static String CONFIGURATION_PROPERTY = "no.feide.moria.directory.configuration";
 
 
     /**
      * Constructor. Creates a new configuration object and reads the Directory
      * Manager configuration file. <br>
      * <br>
      * Note that the actual parsing of the configuration file is done by
      * <code>parseIndexConfig(Element)</code> and
      * <code>parseBackendConfig(Element)</code>.
      * @param config
      *            The Directory Manager configuration passed on from
      *            <code>DirectoryManager.setConfig(Properties)</code>. Must
      *            include the property given by
      *            <code>DirectoryManagerConfiguration.CONFIGURATION_PROPERTY</code>.
     * @throws NullPointerException
     *             If <code>config</code> is <code>null</code>.
     * @throws DirectoryManagerConfigurationException
     *             If the property given by
      *             <code>DirectoryManagerConfiguration.CONFIGURATION_PROPERTY</code>
      *             is not set or is an empty string. Also thrown if unable to
      *             read from or parse the configuration file.
      * @see DirectoryManager#setConfig(Properties)
      * @see #CONFIGURATION_PROPERTY
      * @see #parseBackendConfig(Element)
      * @see #parseIndexConfig(Element)
      */
     public DirectoryManagerConfiguration(final Properties config) {
 
         // Sanity check.
         if (config == null)
            throw new NullPointerException("Configuration properties cannot be NULL");
 
         // Preparing to read configuration from file.
         final String configFile = (String) config.get(CONFIGURATION_PROPERTY);
         if (configFile == null || configFile.equals(""))
             throw new DirectoryManagerConfigurationException("Property " + DirectoryManagerConfiguration.CONFIGURATION_PROPERTY + " not set)");
 
         // Read index (not the index files themselves, mind you) and backend
         // configuration.
         Element rootElement = null;
         try {
             rootElement = (new SAXBuilder()).build(new File(configFile)).getRootElement();
         } catch (IOException e) {
             throw new DirectoryManagerConfigurationException("Unable to read from configuration file", e);
         } catch (JDOMException e) {
             throw new DirectoryManagerConfigurationException("Unable to parse configuration file", e);
         }
         parseIndexConfig(rootElement);
         backendConfiguration = parseBackendConfig(rootElement);
 
     }
 
 
     /**
      * Get the backend configuration element.
      * @return A copy of the backend configuration element, as per
      *         <code>Element.clone()</code>.
      * @see Element#clone()
      */
     public Element getBackendElement() {
 
         return (Element) backendConfiguration.clone();
 
     }
 
 
     /**
      * Parse the subsection of the configuration file related to the index and
      * update the configuration. <br>
      * <br>
      * If more than one <code>Index</code> element is found only the first is
      * considered.
      * @param rootElement
      *            The root configuration element. Cannot be <code>null</code>.
     * @throws NullPointerException
     *             If <code>rootElement</code> is <code>null</code>.
     * @throws DirectoryManagerConfigurationException
     *             If index file (attribute <code>file</code> in element
     *             <code>Index</code>) is not set, or if index update
     *             frequency (attribute <code>update</code> in element
     *             <code>Index</code>) is not set or is less than zero.
      */
     private void parseIndexConfig(final Element rootElement) {
 
         // Sanity check.
         if (rootElement == null)
            throw new NullPointerException("Root element cannot be NULL");
 
         // Get the index element, with sanity checks.
         final Element indexElement = rootElement.getChild("Index");
         HashMap indexConfig = new HashMap();
 
         // Get index filename, with sanity checks.
         Attribute a = indexElement.getAttribute("file");
         if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
             throw new DirectoryManagerConfigurationException("Index file not set in configuration file");
         indexFilename = a.getValue();
 
         // Get index update frequency, with sanity checks.
         a = indexElement.getAttribute("update");
         if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
             throw new DirectoryManagerConfigurationException("Index update frequency not set in configuration file");
         indexUpdateFrequency = 1000 * Integer.parseInt(a.getValue());
         if (indexUpdateFrequency <= 0)
             throw new DirectoryManagerConfigurationException("Index update frequency must be greater than zero");
 
     }
 
 
     /**
      * Get the serialized index file name.
      * @return The index file name.
      */
     public String getIndexFilename() {
 
         return indexFilename;
 
     }
 
 
     /**
      * Get the index update frequency.
      * @return The index update frequency, in milliseconds.
      */
     public long getIndexUpdateFrequency() {
 
         return indexUpdateFrequency;
 
     }
 
 
     /**
      * Parse the subsection of the configuration file common to all backend
      * implementations and update the configuration. <br>
      * <br>
      * This method will only consider the attribute <code>class</code> in the
      * <code>Backend</code> element; further parsing of the element is left to
      * the backend implementation. If more than one <code>Backend</code>
      * element is found, only the first is considered.
      * @param rootElement
      *            The root configuration element. Cannot be <code>null</code>.
      * @return The backend configuration element, as per
      *         <code>Element.clone()</code>.
     * @throws NullPointerException
     *             If <code>rootElement</code> is <code>null</code>.
     * @throws DirectoryManagerConfigurationException
     *             If backend factory class (attribute <code>class</code> in
     *             element <code>Backend</code>) is not set, or if the given
     *             backend factory class cannot be resolved.
      * @see Element#clone()
      * @see no.feide.moria.directory.backend.DirectoryManagerBackendFactory#setConfig(Element)
      */
     private Element parseBackendConfig(final Element rootElement) {
 
         // Sanity check.
         if (rootElement == null)
            throw new NullPointerException("Root element cannot be NULL");
 
         // Get the backend element, with sanity checks.
         final Element backendElement = rootElement.getChild("Backend");
         HashMap backendConfig = new HashMap();
 
         // Get backend class, with sanity checks.
         final Attribute a = backendElement.getAttribute("class");
         if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
             throw new DirectoryManagerConfigurationException("Backend class not set in configuration file");
         try {
             backendFactoryClass = Class.forName(a.getValue());
         } catch (ClassNotFoundException e) {
             throw new DirectoryManagerConfigurationException("Backend factory class " + a.getValue() + " not found", e);
         }
 
         return (Element) backendElement.clone();
 
     }
 
 
     /**
      * Get the backend factory class implementation.
      * @return The backend factory class.
      */
     public Class getBackendFactoryClass() {
 
         return backendFactoryClass;
 
     }
 
 }
