 package org.sakaiproject.tool.impl;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.Registration;
 import com.esotericsoftware.kryo.Serializer;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 import com.esotericsoftware.kryo.serializers.DefaultSerializers;
 import com.esotericsoftware.kryo.serializers.JavaSerializer;
 import com.esotericsoftware.minlog.Log;
 import org.apache.commons.lang.mutable.MutableLong;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.event.impl.BaseUsageSession;
 import org.sakaiproject.event.impl.UsageSessionServiceAdaptor;
 import org.sakaiproject.id.api.IdManager;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.sakaiproject.time.impl.BasicTimeService;
 import org.sakaiproject.time.impl.MyTime;
 import org.sakaiproject.tool.api.NonPortableSession;
 import org.sakaiproject.tool.api.SessionAttributeListener;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.SessionStore;
 import org.sakaiproject.util.TerracottaClassLoader;
 
 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Currency;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * The class handle serializing Sakai's session objects.
  *
  * The session has 3 maps.  Before each object in a particular map is written, we write out the name of classloader
  * for the object.  Then when reading the object back in we use the name of the classloader, look it up in the
  * registry and switches kryo to use that classloader before loading the class.  This is to deal
  * with classloading issues from the component manager from one node to the next.
  *
  * Created with IntelliJ IDEA.
  * User: jbush
  * Date: 12/6/13
  * Time: 3:19 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SessionSerializer extends Kryo {
     private static org.apache.commons.logging.Log M_log = LogFactory.getLog(SessionSerializer.class);
     static final public String SESSION_MAP_CONTEXT_KEY = "session.map.context.key";
     static final public String MY_SESSION_CONTEXT_KEY = "mysession.context.key";
     public static final String NOT_A_COMPONENT_CLASSLOADER = "X";
 
     public SessionSerializer() {
         /**
          * kryo is not thread safe, a new instance is needed per thread.
          * may want to consider pushing into threadlocal if instantiating this thing
          * over and over proves to be slow.
          */
         super();
         if (M_log.isDebugEnabled()) {
             Log.DEBUG();
         }
         register(ArrayList.class);
         register(LinkedList.class);
         register(HashSet.class);
         register(HashMap.class);
         register(Currency.class, new DefaultSerializers.CurrencySerializer());
         register(StringBuffer.class, new DefaultSerializers.StringBufferSerializer());
         register(StringBuilder.class, new DefaultSerializers.StringBuilderSerializer());
         register(Collections.EMPTY_LIST.getClass(), new DefaultSerializers.CollectionsEmptyListSerializer());
         register(Collections.EMPTY_MAP.getClass(), new DefaultSerializers.CollectionsEmptyMapSerializer());
         register(Collections.EMPTY_SET.getClass(), new DefaultSerializers.CollectionsEmptySetSerializer());
         register(Collections.singletonList("").getClass(), new DefaultSerializers.CollectionsSingletonListSerializer());
         register(Collections.singleton("").getClass(), new DefaultSerializers.CollectionsSingletonSetSerializer());
         register(Collections.singletonMap("", "").getClass(), new DefaultSerializers.CollectionsSingletonMapSerializer());
         register(Class.class, new DefaultSerializers.ClassSerializer());
         register(BigDecimal.class, new DefaultSerializers.BigDecimalSerializer());
         register(BigInteger.class, new DefaultSerializers.BigIntegerSerializer());
         register(MyTime.class, new MyTimeSerializer());
         register(BaseUsageSession.class, new BaseUsageSessionSerializer());
         addDefaultSerializer(Locale.class, JavaSerializer.class);
         register(MySession.class, new MySessionSerializer());
         register(MyLittleSession.class, new MyLittleSessionSerializer());
 
     }
 
     public MySession deserialize(final byte[] data) {
         long start = System.currentTimeMillis();
         MySession session = readObject(new Input(data), MySession.class);
        M_log.debug("serialized session in " + (System.currentTimeMillis() - start) + " ms");
         return session;
     }
 
 
     public byte[] serialize(final MySession object) {
         long start = System.currentTimeMillis();
         Output output = new Output(4096);
         writeObject(output, object, new MySessionSerializer());
         output.flush();
         M_log.debug("serialized session in " + (System.currentTimeMillis() - start) + " ms");
         return output.getBuffer();
     }
 
     public void serializeSessionMap(Kryo kryo, Output output, Object session, String name, String type) {
         Field attributesField = null;
         kryo.getContext().put(SESSION_MAP_CONTEXT_KEY, type);
 
         try {
             try {
                 attributesField = session.getClass().getDeclaredField(name);
             } catch (java.lang.NoSuchFieldException e) {
                 // necessary for unit test which extends the MySession
                 attributesField = session.getClass().getSuperclass().getDeclaredField(name);
             }
             attributesField.setAccessible(true);
             Map<String, Object> attributes = (Map<String, Object>) attributesField.get(session);
             output.writeInt(attributes.size());
 
             M_log.debug("found " + attributes.size() + " " + type + " attributes to serialize");
 
             for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                 output.writeString(entry.getKey());
                 Object value = entry.getValue();
                 try {
                     if (value.getClass().getClassLoader() instanceof TerracottaClassLoader) {
                         TerracottaClassLoader classLoader = (TerracottaClassLoader) value.getClass().getClassLoader();
                         M_log.debug("TerracottaClassLoader: writing classLoaderName=" + classLoader.__tc_getClassLoaderName());
 
                         output.writeString(classLoader.__tc_getClassLoaderName());
                     } else {
                         M_log.debug("writing classLoaderName=" + NOT_A_COMPONENT_CLASSLOADER);
 
                         output.writeString(NOT_A_COMPONENT_CLASSLOADER);
                     }
 
                     kryo.writeClassAndObject(output, value);
                     M_log.debug("serialized " + type + " attribute with name:" + entry.getKey() +
                             " type:" + value.getClass().getName());
 
                 } catch (Exception e) {
                     M_log.error("couldn't serialize " + type + " attribute with name:" + entry.getKey() +
                             " and type:" + value.getClass().getName() + " error:" + e.getMessage(), e);
                     //kryo.writeObjectOrNull(output, null, entry.getValue().getClass());
                 }
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
     }
 
     public void deserializeSessionMap(Kryo kryo, Input input, Object session, String name, String type) {
         int attributesLength = input.readInt();
         kryo.getContext().put(SESSION_MAP_CONTEXT_KEY, type);
         for (int i = 0; i < attributesLength; i++) {
             String paramName = input.readString();
             M_log.debug("deserializing " + type + " attribute with name:" + paramName);
 
             ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
 
             try {
                 String classLoaderName = input.readString();
                 M_log.debug("reading classLoaderName=" + classLoaderName);
 
                 ClassLoader classLoader = ComponentManager.getClassLoader(classLoaderName);
 
                 if (classLoader != null) {
                     if (currentLoader instanceof TerracottaClassLoader) {
                         M_log.debug("current classloader: " + ((TerracottaClassLoader) currentLoader).__tc_getClassLoaderName());
                     }
                     M_log.debug("switching to classloader:" + classLoaderName);
                     kryo.setClassLoader(classLoader);
                 }
                 Object value = kryo.readClassAndObject(input);
                 if (value != null) {
                     M_log.debug(type + " attribute type:" + value.getClass().getName());
                     Field attributesField = session.getClass().getDeclaredField(name);
                     attributesField.setAccessible(true);
                     Map<String, Object> attributes = (Map<String, Object>) attributesField.get(session);
 
                     attributes.put(paramName, value);
                 } else {
                     M_log.debug(paramName + " attribute value is null");
                 }
 
 
             } catch (Exception e) {
                 // rather than just blow up lets continue on our merry way and log the issue
                 // there are bound to be things put into the session that cause issues
                 // we have both classloading and serialization issues to contend with here
                 M_log.error("Failed to deserialize " + type + " attribute: " + paramName + " error: " + e.getMessage(), e);
             } finally {
                 kryo.setClassLoader(currentLoader);
             }
         }
     }
 
     public class MySessionSerializer extends Serializer<MySession> {
 
         public MySessionSerializer() {
         }
 
         @Override
         public void write(Kryo kryo, Output output, MySession session) {
             output.writeString(session.getId());
             output.writeString(session.getUserEid());
             output.writeString(session.getUserId());
             output.writeInt(session.getMaxInactiveInterval());
             serializeSessionMap(kryo, output, session, "m_attributes", "session");
             serializeSessionMap(kryo, output, session, "m_toolSessions", "toolSession");
             serializeSessionMap(kryo, output, session, "m_contextSessions", "contextSession");
         }
 
 
         @Override
         public MySession read(Kryo kryo, Input input, Class<MySession> aClass) {
             SessionManager sessionManager = (SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class);
 
             SessionStore sessionStore = (SessionStore) ComponentManager.get(org.sakaiproject.tool.api.SessionStore.class);
 
             ThreadLocalManager threadLocalManager = (ThreadLocalManager) ComponentManager.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class);
 
             IdManager idManager = (IdManager) ComponentManager.get(org.sakaiproject.id.api.IdManager.class);
 
             SessionAttributeListener sessionListener = (SessionAttributeListener) ComponentManager.get(org.sakaiproject.tool.api.SessionBindingListener.class);
 
             // create a non portable session object if this is a clustered environment
             NonPortableSession nPS = new MyNonPortableSession();
             String id = input.readString();
             String eid = input.readString();
             String userId = input.readString();
             int m_defaultInactiveInterval = input.readInt();
 
 
             MySession session = new MySession(sessionManager, id, threadLocalManager, idManager,
                     sessionStore, sessionListener, m_defaultInactiveInterval, nPS, new MutableLong(System.currentTimeMillis()));
             session.setUserEid(eid);
             session.setUserId(userId);
             session.setActive();
 
             kryo.getContext().put(MY_SESSION_CONTEXT_KEY, session);
 
             deserializeSessionMap(kryo, input, session, "m_attributes", "session");
             deserializeSessionMap(kryo, input, session, "m_toolSessions", "toolSession");
             deserializeSessionMap(kryo, input, session, "m_contextSessions", "contextSession");
 
 
             return session;
         }
     }
 
 
     public class MyLittleSessionSerializer extends Serializer<MyLittleSession> {
 
         @Override
         public void write(Kryo kryo, Output output, MyLittleSession myLittleSession) {
             output.writeString(myLittleSession.getId());
             output.writeString(myLittleSession.getContextId());
             String type = (String) kryo.getContext().get(SESSION_MAP_CONTEXT_KEY);
 
             serializeSessionMap(kryo, output, myLittleSession, "m_attributes", type);
 
         }
 
         @Override
         public MyLittleSession read(Kryo kryo, Input input, Class<MyLittleSession> myLittleSessionClass) {
             SessionManager sessionManager = (SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class);
 
             SessionStore sessionStore = (SessionStore) ComponentManager.get(org.sakaiproject.tool.api.SessionStore.class);
 
             ThreadLocalManager threadLocalManager = (ThreadLocalManager) ComponentManager.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class);
 
             SessionAttributeListener sessionListener = (SessionAttributeListener) ComponentManager.get(org.sakaiproject.tool.api.SessionBindingListener.class);
 
             // create a non portable session object if this is a clustered environment
             NonPortableSession nPS = new MyNonPortableSession();
 
 
             String id = input.readString();
             String contextId = input.readString();
 
             MySession session = (MySession) kryo.getContext().get(MY_SESSION_CONTEXT_KEY);
 
             MyLittleSession myLittleSession = new MyLittleSession(sessionManager, id, session, contextId, threadLocalManager, sessionListener, sessionStore, nPS);
 
             String type = (String) kryo.getContext().get(SESSION_MAP_CONTEXT_KEY);
 
             deserializeSessionMap(kryo, input, myLittleSession, "m_attributes", type);
 
             return myLittleSession;
         }
     }
 
     public class MyTimeSerializer extends Serializer<MyTime> {
 
         public MyTimeSerializer() {
         }
 
         @Override
         public void write(Kryo kryo, Output output, MyTime myTime) {
             output.writeLong(myTime.getTime());
         }
 
         @Override
         public MyTime read(Kryo kryo, Input input, Class<MyTime> myTimeClass) {
             org.sakaiproject.component.api.ComponentManager compMgr =
                     org.sakaiproject.component.cover.ComponentManager.getInstance();
             BasicTimeService timeService = (BasicTimeService) compMgr.get("org.sakaiproject.time.api.TimeService");
 
             MyTime time = new MyTime(timeService, input.readLong());
             time.resolveTransientFields();
             return time;
         }
     }
 
     public class BaseUsageSessionSerializer extends Serializer<BaseUsageSession> {
 
         public BaseUsageSessionSerializer() {
         }
 
         @Override
         public void write(Kryo kryo, Output output, BaseUsageSession usageSession) {
             output.writeString(usageSession.getId());
         }
 
         @Override
         public BaseUsageSession read(Kryo kryo, Input input, Class<BaseUsageSession> usageSessionClass) {
             org.sakaiproject.component.api.ComponentManager compMgr =
                     org.sakaiproject.component.cover.ComponentManager.getInstance();
 
             UsageSessionServiceAdaptor usageSessionServiceAdaptor = (UsageSessionServiceAdaptor) compMgr.get("org.sakaiproject.event.api.UsageSessionService");
             // just reload it from storage
             return (BaseUsageSession) usageSessionServiceAdaptor.getSession(input.readString());
         }
     }
 
 }
