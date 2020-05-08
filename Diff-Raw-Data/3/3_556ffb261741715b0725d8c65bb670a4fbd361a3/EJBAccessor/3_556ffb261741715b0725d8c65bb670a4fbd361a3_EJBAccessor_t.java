 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.ee;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.Properties;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author spuhl
  */
 //ToDo Testcases
 public class EJBAccessor<T> {
 
     private static final String ORB_INITIAL_HOST = "org.omg.CORBA.ORBInitialHost";
     private static final String ORB_INITIAL_PORT = "org.omg.CORBA.ORBInitialPort";
     private final static Logger log = org.apache.log4j.Logger.getLogger(EJBAccessor.class);
     private Properties initalContextProperties;
     private T ejbInterface;
 
     public EJBAccessor(final Properties initalContextProperties) {
         log.info("EJBACCESSOR: server: " + initalContextProperties.getProperty(ORB_INITIAL_HOST));
         log.info("EJBACCESSOR: orbPort: " + initalContextProperties.getProperty(ORB_INITIAL_PORT));
         if (initalContextProperties == null) {
             this.initalContextProperties = getDefaultProperties();
         } else {
             this.initalContextProperties = initalContextProperties;
         }
     }
 
     //ToDo reflect generic/method
     public void initEJBAccessor(Class<T> type) throws NamingException {
        InitialContext ic = new InitialContext(initalContextProperties);        
         ejbInterface = (T) ic.lookup(type.getName());
     }
 
     public static <E> EJBAccessor<E> createEJBAccessor(final String host, final String orbPort, Class<E> type) throws NamingException {
         final Properties props = new Properties();
         props.setProperty(ORB_INITIAL_HOST, host);
         props.setProperty(ORB_INITIAL_PORT, orbPort);
         final EJBAccessor<E> ejbAccess = new EJBAccessor<E>(props);
         ejbAccess.initEJBAccessor(type);
         return ejbAccess;
     }
 
     //ToDo reflect generic/method
 //    public static <T> EJBAccessor<T> createEJBAccessor(final String host, final String orbPort) throws NamingException {
 //        final Properties props = new Properties();
 //        props.setProperty(ORB_INITIAL_HOST, host);
 //        props.setProperty(ORB_INITIAL_PORT, orbPort);
 //        final EJBAccessor<T> ejbAccess = new EJBAccessor<T>(props);
 //        ejbAccess.initEJBAccessor(T);
 //        return ejbAccess;
 //    }
 
     public static <E> EJBAccessor<E> createEJBAccessor(Class<E> type) throws NamingException {
         final EJBAccessor ejbAccessor = new EJBAccessor<E>(getDefaultProperties());
         ejbAccessor.initEJBAccessor(type);
         return ejbAccessor;
     }
 
     //ToDo reflect generic/method
 //     public static <E> EJBAccessor<E> createEJBAccessor() throws NamingException {
 //        final EJBAccessor ejbAccessor = new EJBAccessor<E>(getDefaultProperties());
 //        ejbAccessor.initEJBAccessor();
 //        return ejbAccessor;
 //    }
 
     public static Properties getDefaultProperties() {
         final Properties props = new Properties();
         props.setProperty(ORB_INITIAL_HOST, "localhost");
         props.setProperty(ORB_INITIAL_PORT, "3700");
         return props;
     }
 
     public Properties getInitalContextProperties() {
         return initalContextProperties;
     }
 
     public void setInitalContextProperties(Properties initalContextProperties) {
         this.initalContextProperties = initalContextProperties;
     }
 
     public T getEjbInterface() {
         return ejbInterface;
     }
 
     //ToDo reflect generic/method
 //    private String getEJBInterfaceClassName() {
 //        try {
 //            Field ejbInterfaceField = getClass().getDeclaredField("ejbInterface");
 //            ejbInterfaceField.setAccessible(true);
 //            System.out.println("type: " + getClass().getDeclaredField("ejbInterface").getType());
 //            System.out.println("type name: " + getClass().getDeclaredField("ejbInterface").getType().getName());
 //            return getClass().getField("ejbInterface").getType().getName();
 //        } catch (NoSuchFieldException ex) {
 //            return null;
 //        }
 //    }
 }
