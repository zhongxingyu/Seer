 package edu.mit.cci.simulation.client.comm;
 
 import edu.mit.cci.simulation.client.HasId;
 import edu.mit.cci.simulation.client.MetaData;
 import edu.mit.cci.simulation.client.Scenario;
 import edu.mit.cci.simulation.client.Simulation;
 import edu.mit.cci.simulation.client.model.transitional.AdaptedObject;
 import edu.mit.cci.simulation.client.model.transitional.AdaptedMetaData;
 import edu.mit.cci.simulation.client.model.transitional.AdaptedScenario;
 import edu.mit.cci.simulation.client.model.transitional.AdaptedSimulation;
 import edu.mit.cci.simulation.jaxb.JaxbReferenceResolver;
 import edu.mit.cci.simulation.jaxb.JaxbUtils;
 import edu.mit.cci.simulation.model.DefaultScenario;
 import edu.mit.cci.simulation.model.DefaultSimulation;
 import edu.mit.cci.simulation.util.ConcreteSerializableCollection;
 import org.apache.log4j.Logger;
 import sun.util.LocaleServiceProviderPool;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import java.io.IOException;
 import java.io.Reader;
import java.io.StringReader;
 import java.lang.reflect.Proxy;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * User: jintrone
  * Date: 3/16/11
  * Time: 10:38 AM
  */
 public class RepositoryManager implements Deserializer, JaxbReferenceResolver {
 
     public static String CACHE_PROPERTY = "edu.mit.cci.simulation.client.cachesize";
 
 
     private Unmarshaller um;
     private DeserializingConnector deserializingConnector;
     private static int DEFAULT_SCENARIO_CACHE_SIZE = 100;
 
     //package scoped for testing
     int currentScenarioCacheSize;
 
     private static Logger log = Logger.getLogger(RepositoryManager.class);
 
     LinkedHashMap<String, Scenario> scenarioCache;
     Map<String, Simulation> simulationCache = new HashMap<String, Simulation>();
     Map<String, MetaData> variableCache = new HashMap<String, MetaData>();
 
     public RepositoryManager(DeserializingConnector deserializingConnector) {
         String cachesize = System.getProperty("edu.mit.cci.simulation.client.cachesize");
         this.deserializingConnector = deserializingConnector;
         deserializingConnector.setDeserializer(this);
         currentScenarioCacheSize = cachesize == null ? DEFAULT_SCENARIO_CACHE_SIZE : Integer.parseInt(cachesize);
 
         //LRU cache
         scenarioCache = new LinkedHashMap<String, Scenario>(currentScenarioCacheSize + 5, .15f, true) {
 
             @Override
             protected boolean removeEldestEntry(Map.Entry eldest) {
                 return (scenarioCache.size() > currentScenarioCacheSize);
             }
         };
 
         try {
             System.setProperty(JaxbUtils.RESOLVER_FACTORY_PROPERTY, "edu.mit.cci.simulation.client.comm.RepositoryManagerFactory");
             new RepositoryManagerFactory().setInstance(this);
 
             JAXBContext context = JAXBContext.newInstance(DefaultSimulation.class, DefaultScenario.class, ConcreteSerializableCollection.class);
             um = context.createUnmarshaller();
 
         } catch (JAXBException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     public void clearCache() {
         simulationCache.clear();
         variableCache.clear();
         scenarioCache.clear();
     }
 
     public void refreshSimulations() throws IOException {
         //side-effect of this call will register all simulations
         deserializingConnector.get(DefaultSimulation.class, null);
 
     }
 
 
 
 
     public void register(Object o) {
         if (o instanceof edu.mit.cci.simulation.model.Variable) {
             edu.mit.cci.simulation.model.Variable v = (edu.mit.cci.simulation.model.Variable) o;
             if (!variableCache.containsKey(v.getId() + "")) {
                 variableCache.put(v.getId() + "", new AdaptedMetaData(v,this));
             }
         } else if (o instanceof edu.mit.cci.simulation.model.Simulation) {
             edu.mit.cci.simulation.model.Simulation s = (edu.mit.cci.simulation.model.Simulation) o;
             if (!simulationCache.containsKey(s.getIdAsString())) {
                 simulationCache.put(s.getIdAsString(), new AdaptedSimulation(s,this));
             }
 
         } else if (o instanceof edu.mit.cci.simulation.model.Scenario) {
             edu.mit.cci.simulation.model.Scenario s = (edu.mit.cci.simulation.model.Scenario) o;
             if (!scenarioCache.containsKey(s.getId() + "")) {
                 scenarioCache.put(s.getId() + "", new AdaptedScenario(s,this));
             }
         } else if (o instanceof ConcreteSerializableCollection) {
             for (Object e : ((ConcreteSerializableCollection) o).bucket) {
                 register(e);
             }
 
 
         } else {
             log.warn("Tyring to bind reference for type that should not be referenced: " + o.getClass());
         }
     }
 
     public HasId lookup(String s, Class clazz) {
         HasId result = null;
         if (Simulation.class.isAssignableFrom(clazz) || edu.mit.cci.simulation.model.Simulation.class.isAssignableFrom(clazz)) {
             result = simulationCache.get(s);
         } else if (MetaData.class.isAssignableFrom(clazz) || edu.mit.cci.simulation.model.Variable.class.isAssignableFrom(clazz)) {
             result = variableCache.get(s);
         } else if (Scenario.class.isAssignableFrom(clazz) || edu.mit.cci.simulation.model.Scenario.class.isAssignableFrom(clazz)) {
             result = scenarioCache.get(s);
         }
         return result;
     }
 
     private Class mapType(String type) {
         if ("Simulation".equals(type)) return edu.mit.cci.simulation.model.Simulation.class;
         else if ("Scenario".equals(type)) return edu.mit.cci.simulation.model.Scenario.class;
         else if ("Variable".equals(type)) return edu.mit.cci.simulation.model.Variable.class;
         else return Object.class;
     }
 
     private <T> T retrieve(String ref, Class<T> clazz) throws IOException {
         ModelAccessPoint address = ModelAccessPoint.forClass(clazz);
         return (T) deserializingConnector.get(clazz, Collections.<String, String>emptyMap(), ref);
 
     }
 
     public HasId getAdaptor(Object o) {
         if (o == null) return null;
         if (o instanceof edu.mit.cci.simulation.model.Simulation) {
             return lookup(((edu.mit.cci.simulation.model.Simulation)o).getId()+"",Simulation.class);
         } else if (o instanceof edu.mit.cci.simulation.model.Scenario) {
             return lookup(((edu.mit.cci.simulation.model.Scenario)o).getId()+"",Scenario.class);
         } else if (o instanceof edu.mit.cci.simulation.model.Variable) {
             return lookup(((edu.mit.cci.simulation.model.Variable)o).getId()+"",MetaData.class);
         } else {
             return null;
         }
     }
 
     private <T> T deferResolve(final String ref, final Class<T> clazz) throws IOException {
         AdaptedObject<T> po = (AdaptedObject<T>) lookup(ref, clazz);
         T result = po == null ? null : po.model();
         if (result == null) {
             result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                     new ClientProxyObject<T>(clazz, ref, new ClientProxyObject.Resolver<T>() {
                         public T resolve() throws IOException {
                             return retrieve(ref, clazz);
                         }
                     }));
             register(result);
         }
         return result;
     }
 
 
 
 
 
     public <T extends HasId> T resolve(String ref, Class<T> clazz) throws IOException {
        // log.info("Resolve "+clazz.getSimpleName()+":"+ref);
         T result = (T) lookup(ref, clazz);
         if (result!=null) {
             //log.info("Found entity");
         }
         if (result == null) {
             //try one more time - the side effect of this operation is that a new ProxyObject will be registered
             if (retrieve(ref, clazz) != null) {
                 return (T) lookup(ref, clazz);
             }
         }
         return result;
     }
 
     public Object resolve(String id, String type) throws JAXBException {
         try {
             return deferResolve(id, mapType(type));
         } catch (IOException e) {
             throw new JAXBException("Error resolving " + type + ":" + id);
         }
     }
 
 
     public Object deserialize(Reader stream) {
         try {
             Object o = um.unmarshal(stream);
             register(o);
             return o;
            
         } catch (JAXBException e) {
            log.error("Server response:\n" + stream.toString());
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 }
