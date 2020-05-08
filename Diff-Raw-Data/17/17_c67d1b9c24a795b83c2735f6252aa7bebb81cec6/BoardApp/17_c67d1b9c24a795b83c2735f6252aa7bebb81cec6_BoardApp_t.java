 package net.jlj.board;
 
 import java.io.InputStream;
 import java.lang.reflect.Type;
 import java.util.Properties;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import net.jlj.board.delegate.BoardDelegate;
 import net.jlj.board.delegate.MemberDelegate;
 import net.jlj.board.delegate.SettingsDelegate;
 
 import org.apache.log4j.Logger;
 import org.picocontainer.DefaultPicoContainer;
 import org.picocontainer.PicoCompositionException;
 import org.picocontainer.PicoContainer;
 import org.picocontainer.injectors.FactoryInjector;
 /**
  * Class to hold application scope information. 
  * @author john
  *
  */
 public class BoardApp
 {
    private static Logger log = Logger.getLogger(BoardApp.class.getName ());
    private static final String OPENJPA_PROPS = "openjpa.properties";
    protected PicoContainer mPico;
    protected static BoardApp mApp;
    /**
     * Injector for the openjpa EntityManagerFactory.
     * @author john
     *
     */
    public static class EmfInjector extends FactoryInjector<EntityManagerFactory> 
    {  
       private static EntityManagerFactory sFactory = Persistence.createEntityManagerFactory("board", loadProperties (OPENJPA_PROPS));
       public EntityManagerFactory getComponentInstance(PicoContainer container, final Type into) 
          throws PicoCompositionException 
       {  
          return sFactory;  
       }  
    };  
         
    protected BoardApp ()
    {
       mPico = new DefaultPicoContainer ();
    }
    
    public static BoardApp getInstance ()
    {
       if (mApp == null)
       {
          mApp = new BoardApp ();
          log.debug ("Initialising the container");
          mApp.init ();
       }
       return mApp;
    }
    
    public static <T> T getComponent (Class<T> o)
    {
       return getInstance ().mPico.getComponent (o);
    }
    
    private void init ()
    {
       DefaultPicoContainer pico = (DefaultPicoContainer) mPico;
       pico.addAdapter (new EmfInjector());
       pico.addComponent (MemberDelegate.class, MemberDelegate.class);
       pico.addComponent (BoardDelegate.class, BoardDelegate.class);
       pico.addComponent (SettingsDelegate.class, SettingsDelegate.class);
       pico.addComponent (Facade.class, Facade.class);
    }   
    
   private static Properties loadProperties (String name)
    {
       Properties props = new Properties();
       InputStream in = BoardApp.class.getClassLoader().getResourceAsStream("/" + name);
       try
       {
          props.load (in);
          in.close ();
       }
       catch (Exception e)
       {
          log.fatal (e);
          System.out.println ("Could not open " + name + ": " +  e.getMessage ());
          throw new RuntimeException ("Could not open " + name + ": ", e);
       }
      // Special handling for Derby. Must set derby.system.home to something sensible.
      String dictionary = props.getProperty ("openjpa.jdbc.DBDictionary");
      log.info ("using " + dictionary + " DBDictionary");
      if (dictionary.startsWith ("derby"))
      {
         String dbHome = System.getProperty ("derby.system.home");
         log.info ("derby.system.home is currently " + dbHome);
         if (dbHome == null)
         {
            log.info ("set derby.system.home to " + System.getProperty ("user.home") + "/.jboard");
            System.setProperty ("derby.system.home", System.getProperty ("user.home") + "/.jboard");
         }
      }
      
       return props;
    }
 }
