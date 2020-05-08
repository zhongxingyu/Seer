 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.common;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ContainerEvent;
 import java.awt.event.ContainerListener;
 import java.awt.event.KeyListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.prefs.BackingStoreException;
 
 import javax.swing.JApplet;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.transform.TransformerConfigurationException;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import cz.cuni.mff.peckam.java.origamist.configuration.Configuration;
 import cz.cuni.mff.peckam.java.origamist.configuration.ConfigurationManagerImpl;
 import cz.cuni.mff.peckam.java.origamist.jaxb.BindingsManager;
 import cz.cuni.mff.peckam.java.origamist.jaxb.ObjectFactoryConfigurator;
 import cz.cuni.mff.peckam.java.origamist.jaxb.SchemaInfo;
 import cz.cuni.mff.peckam.java.origamist.logging.GUIAppender;
 import cz.cuni.mff.peckam.java.origamist.model.ObjectFactory;
 import cz.cuni.mff.peckam.java.origamist.services.HashCodeAndEqualsHelperImpl;
 import cz.cuni.mff.peckam.java.origamist.services.JAXBListingHandler;
 import cz.cuni.mff.peckam.java.origamist.services.JAXBOrigamiHandler;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.TooltipFactory;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.HashCodeAndEqualsHelper;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ListingHandler;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.OrigamiHandler;
 
 /**
  * Common GUI elements for both the viewer and editor.
  * 
  * Provided properties:
  * appMessages (protected property)
  * 
  * @author Martin Pecka
  */
 public abstract class CommonGui extends JApplet
 {
 
     private static final long serialVersionUID = -9021667515698972438L;
 
     /**
      * The localized messages for the common GUI classes.
      */
     protected ResourceBundle  appMessages      = null;
 
     /**
      * The common message formater that can be used over the GUI. It has its locale set to the configured one
      */
     protected MessageFormat   format           = null;
 
     @Override
     public void init()
     {
         // make the frame resizable if the applet is run using JWS/JNLP (because at default it is run as a fixed-size
         // window)
         Component parent = getTopmostComponent();
         if (parent instanceof Frame) {
             Frame frame = ((Frame) parent);
             if (!frame.isResizable()) {
                 frame.setResizable(true);
                 // see http://forums.oracle.com/forums/thread.jspa?threadID=2152885&stqc=true
                 frame.setLayout(new GridLayout());
             }
         }
 
         super.init();
 
         setupLoggersBeforeServices();
 
         registerServices();
 
         setupLoggers();
 
         final Configuration config = ServiceLocator.get(ConfigurationManager.class).get();
 
         final String bundleName = "application";
 
         appMessages = ResourceBundle.getBundle(bundleName, config.getLocale());
         format = new MessageFormat("", config.getLocale());
         config.addPropertyChangeListener("locale", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if (!appMessages.getLocale().equals(evt.getNewValue())) {
                     ResourceBundle oldMessages = appMessages;
                     appMessages = ResourceBundle.getBundle(bundleName, (Locale) evt.getNewValue());
                     CommonGui.this.firePropertyChange("appMessages", oldMessages, appMessages);
                     MessageFormat oldFormat = format;
                     format = new MessageFormat("", (Locale) evt.getNewValue());
                     CommonGui.this.firePropertyChange("format", oldFormat, format);
                     UIManager.getDefaults().setDefaultLocale((Locale) evt.getNewValue());
                 }
             }
         });
         UIManager.getDefaults().addResourceBundle("application");
 
         // to allow transparent JCanvas3D background
         System.setProperty("j3d.transparentOffScreen", "true");
 
         try {
             SwingUtilities.invokeAndWait(new Runnable() {
                 @Override
                 public void run()
                 {
                     createComponents();
                     buildLayout();
                     registerServicesAfterComponentsAreCreated();
                 }
             });
         } catch (InterruptedException e) {
             Logger.getLogger("application").l7dlog(Level.FATAL, "guiInitializationInterrupted", e);
         } catch (InvocationTargetException e) {
             Logger.getLogger("application").l7dlog(Level.FATAL, "guiInitializationException", e.getCause());
         }
     }
 
     /**
      * Create and setup all the form components.
      */
     protected abstract void createComponents();
 
     /**
      * Setup the form layout.
      */
     protected abstract void buildLayout();
 
     /**
      * Register all common services.
      */
     protected void registerServices()
     {
         ServiceLocator.add(OrigamiHandler.class, new JAXBOrigamiHandler(this.getDocumentBase()));
         ServiceLocator.add(ListingHandler.class, new JAXBListingHandler());
         ServiceLocator.add(ConfigurationManager.class, new ConfigurationManagerImpl());
         ServiceLocator.add(HashCodeAndEqualsHelper.class, new HashCodeAndEqualsHelperImpl());
         ServiceLocator.add(TooltipFactory.class, new TooltipFactory());
 
         // setup JAXB bindings
         try {
             ServiceLocator.add(BindingsManager.class, new BindingsManager(JAXBContext.newInstance(
                     "cz.cuni.mff.peckam.java.origamist.model.jaxb:cz.cuni.mff.peckam.java.origamist.files.jaxb:"
                             + "cz.cuni.mff.peckam.java.origamist.common.jaxb", getClass().getClassLoader())));
 
             BindingsManager manager = ServiceLocator.get(BindingsManager.class);
 
             @SuppressWarnings("unused")
             SchemaInfo c1 = manager.addSchema("http://www.mff.cuni.cz/~peckam/java/origamist/common/v1",
                     "resources/schemata/common_v1.xsd", true);
 
             SchemaInfo d1 = manager.addSchema("http://www.mff.cuni.cz/~peckam/java/origamist/diagram/v1",
                     "resources/schemata/diagram_v1.xsd", true);
 
             SchemaInfo d2 = manager.addSchema("http://www.mff.cuni.cz/~peckam/java/origamist/diagram/v2",
                     "resources/schemata/diagram_v2.xsd", true);
 
             manager.addTransform(d1, d2, "resources/schemata/diagram_v1_to_v2.xsl", null);
 
             manager.addUnmarshallerConfigurator(d2, new ObjectFactoryConfigurator(new ObjectFactory()));
 
             SchemaInfo f1 = manager.addSchema("http://www.mff.cuni.cz/~peckam/java/origamist/files/v1",
                     "resources/schemata/files_v1.xsd", true);
             manager.addUnmarshallerConfigurator(f1, new ObjectFactoryConfigurator(
                     new cz.cuni.mff.peckam.java.origamist.files.ObjectFactory()));
 
         } catch (JAXBException e) {
             Logger.getLogger(getClass()).error("Couldn't initialize JAXB: " + e);
         } catch (IOException e) {
             Logger.getLogger(getClass()).error("Couldn't initialize JAXB: " + e);
         } catch (TransformerConfigurationException e) {
             Logger.getLogger(getClass()).error("Couldn't initialize Transform: " + e);
         }
     }
 
     /**
      * Register services after the components are created.
      */
     protected void registerServicesAfterComponentsAreCreated()
     {
 
     }
 
     /**
      * Setup the loggers used by the application. Count that no services are registered at this time.
      */
     protected void setupLoggersBeforeServices()
     {
         BasicConfigurator.configure();
 
         final Logger l = Logger.getLogger("application");
         l.setResourceBundle(appMessages);
         addPropertyChangeListener("appMessages", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 l.setResourceBundle(appMessages);
             }
         });
         l.setLevel(Level.ALL);
     }
 
     /**
      * Setup the loggers used by the application.
      */
     protected void setupLoggers()
     {
        BasicConfigurator.configure();

        LogManager.getRootLogger().addAppender(new GUIAppender(this));

        Logger.getLogger("application").addAppender(new GUIAppender(this));
     }
 
     /**
      * Add the key listener in such a way that it reacts to the keypresses not depending on what is focused.
      * 
      * @param listener The listener to be added globally.
      */
     protected void addGlobalKeyListener(final KeyListener listener)
     {
         // new GlobalKeyListenerContainerListner(listener).add(getContentPane());
         new GlobalKeyListenerContainerListner(listener).add(getTopmostComponent());
     }
 
     /**
      * A container listener which adds/removes the given KeyListener to/from all added/removed components.
      * 
      * @author Martin Pecka
      */
     protected class GlobalKeyListenerContainerListner implements ContainerListener
     {
 
         /** The key listener to be added to all the components. */
         protected KeyListener listener;
 
         /**
          * @param listener The key listener to be added to all the components.
          */
         public GlobalKeyListenerContainerListner(KeyListener listener)
         {
             this.listener = listener;
         }
 
         @Override
         public void componentRemoved(ContainerEvent e)
         {
             this.remove(e.getChild());
         }
 
         @Override
         public void componentAdded(ContainerEvent e)
         {
             this.add(e.getChild());
         }
 
         /**
          * Make the given component (and all of its children, if it is a {@link Container}) listen by the listener.
          * 
          * @param c
          */
         public void add(Component c)
         {
             c.addKeyListener(listener);
             if (c instanceof Container) {
                 Container cont = (Container) c;
                 cont.addContainerListener(this);
                 for (Component cc : cont.getComponents())
                     this.add(cc);
             }
         }
 
         /**
          * Make the given component (and all of its children, if it is a {@link Container}) not listen by the listener.
          * 
          * @param c
          */
         public void remove(Component c)
         {
             c.removeKeyListener(listener);
             if (c instanceof Container) {
                 Container cont = (Container) c;
                 cont.addContainerListener(this);
                 for (Component cc : cont.getComponents())
                     this.remove(cc);
             }
         }
     };
 
     @Override
     public void start()
     {
         super.start();
     }
 
     @Override
     public void stop()
     {
         try {
             ServiceLocator.get(ConfigurationManager.class).persist();
         } catch (BackingStoreException e) {}
         super.stop();
     }
 
     @Override
     public void destroy()
     {
         try {
             ServiceLocator.get(ConfigurationManager.class).persist();
         } catch (BackingStoreException e) {}
         super.destroy();
     }
 
     /**
      * The component that is the highest in the component hierarchy. Will be either JApplet or a Frame.
      */
     protected abstract Component getTopmostComponent();
 }
