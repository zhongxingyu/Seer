 package vinna;
 
 import vinna.exception.VuntimeException;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class DefaultControllerFactory implements ControllerFactory {
 
     private final String basePackage;
 
     public DefaultControllerFactory(String basePackage) {
         this.basePackage = basePackage;
     }
 
     @Override
     public Object create(String id, Class<?> clazz) {
         if (clazz == null) {
             try {
                 clazz = Class.forName(id);
             } catch (ClassNotFoundException e) {
                id = basePackage + ".controllers." + id;
                Matcher m = Pattern.compile("(.+\\.)([^\\.])([^\\.]+)").matcher(id);
                 if (!m.matches()) {
                    throw new VuntimeException("Something really fishy here: " + id);
                 }
                id = m.group(1) + m.group(2).toUpperCase() + m.group(3);
                 try {
                    clazz = Class.forName(id);
                 } catch (ClassNotFoundException e1) {
                    throw new VuntimeException("Invalid object id " + id);
                 }
             }
         }
         try {
             return clazz.newInstance();
         } catch (InstantiationException | IllegalAccessException e) {
             throw new VuntimeException("Can't create an instance of the controller " + (clazz == null ? " with the id " + id : " of type " + clazz), e);
         }
     }
 }
