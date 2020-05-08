 package javax.enterprise.concurrent;
 
 import java.util.Map;
 
 /**
  */
 public interface ContextService {
 
    Object createContextualProperty(Object instance, Class<?>... interfaces) throws IllegalArgumentException;
 
    Object createContextualProperty(Object instance, Map<String, String> executionProperties, Class<?>... interfaces) throws IllegalArgumentException;
 
    <T> T createContextualProperty(T instance, Class<T> _interface) throws IllegalArgumentException;
 
    <T> T createContextualProperty(T instance, Map<String, String> executionProperties, Class<T> _interface) throws IllegalArgumentException;
 
     Map<String, String> getExecutionProperties(Object contextualProxy) throws IllegalArgumentException;
 }
