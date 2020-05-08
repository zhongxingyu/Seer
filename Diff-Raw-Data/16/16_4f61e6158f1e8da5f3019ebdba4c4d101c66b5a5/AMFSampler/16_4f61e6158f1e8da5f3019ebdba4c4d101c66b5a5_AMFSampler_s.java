 package com.wavify.jmeter.protocol.amf.sampler;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.security.GeneralSecurityException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import javax.net.ssl.SSLContext;
 
 import org.apache.jmeter.protocol.http.control.Cookie;
 import org.apache.jmeter.protocol.http.control.CookieManager;
 import org.apache.jmeter.samplers.AbstractSampler;
 import org.apache.jmeter.samplers.Entry;
 import org.apache.jmeter.samplers.Interruptible;
 import org.apache.jmeter.samplers.SampleResult;
 import org.apache.jmeter.testelement.TestElement;
 import org.apache.jmeter.testelement.property.AbstractProperty;
 import org.apache.jmeter.testelement.property.CollectionProperty;
 import org.apache.jmeter.testelement.property.FloatProperty;
 import org.apache.jmeter.testelement.property.IntegerProperty;
 import org.apache.jmeter.testelement.property.JMeterProperty;
 import org.apache.jmeter.testelement.property.NullProperty;
 import org.apache.jmeter.testelement.property.ObjectProperty;
 import org.apache.jmeter.testelement.property.PropertyIterator;
 import org.apache.jmeter.testelement.property.StringProperty;
 import org.apache.jmeter.util.JsseSSLManager;
 import org.apache.jmeter.util.SSLManager;
 
 import flex.messaging.io.amf.client.AMFConnection;
 import flex.messaging.io.amf.client.exceptions.ClientStatusException;
 import flex.messaging.io.amf.client.exceptions.ServerStatusException;
 import flex.messaging.messages.AcknowledgeMessage;
 import flex.messaging.messages.CommandMessage;
 import flex.messaging.messages.RemotingMessage;
 
 public class AMFSampler extends AbstractSampler implements Interruptible {
 
   private static final long serialVersionUID = 1L;
 
   public static final String COOKIE_MANAGER = "HTTPSampler.cookie_manager";
 
   public static final String ADDRESS_PROP = "amf_address";
   public static final String SERVICE_PROP = "amf_service";
   public static final String METHOD_PROP = "amf_method";
   public static final String ARGUMENT_PROP = "amf_argument";
 
   private AMFConnection connection;
 
   public void setAddress(String address) {
     setProperty(ADDRESS_PROP, address);
   }
 
   public void setService(String service) {
     setProperty(SERVICE_PROP, service);
   }
 
   public void setMethod(String method) {
     setProperty(METHOD_PROP, method);
   }
 
   private CollectionProperty convertCollectionArgument(Collection<?> collection) {
     CollectionProperty properties = new CollectionProperty();
     properties.setName("COLLECTION");
     for (Object object : collection) {
       if (object == null) {
         properties.addProperty(new NullProperty());
       } else if (object instanceof Integer) {
         properties.addProperty(new IntegerProperty("INT", (Integer) object));
       } else if (object instanceof Float) {
         properties.addProperty(new FloatProperty("FLOAT", (Float) object));
       } else if (object instanceof String) {
         properties.addProperty(new StringProperty("STRING", (String) object));
       } else if (object instanceof Collection) {
         properties.addProperty(this
             .convertCollectionArgument((Collection<?>) object));
       }
     }
     return properties;
   }
 
   public void setArguments(List<?> arguments) {
     CollectionProperty property = convertCollectionArgument(arguments);
     property.setName(ARGUMENT_PROP);
 
     setProperty(property);
   }
 
   public void setCookieManager(CookieManager cookieManager) {
     setProperty(new ObjectProperty(COOKIE_MANAGER, cookieManager));
   }
 
   public String getAddress() {
     return getPropertyAsString(ADDRESS_PROP);
   }
 
   public String getService() {
     return getPropertyAsString(SERVICE_PROP);
   }
 
   public String getMethod() {
     return getPropertyAsString(METHOD_PROP);
   }
 
   public List<Object> getArguments() {
     JMeterProperty prop = getProperty(ARGUMENT_PROP);
     ArrayList<Object> arguments = new ArrayList<Object>();
 
     if (prop instanceof CollectionProperty) {
       CollectionProperty collection = (CollectionProperty) prop;
 
       PropertyIterator iterator = collection.iterator();
       while (iterator.hasNext()) {
         JMeterProperty property = iterator.next();

         if (property instanceof NullProperty) {
           arguments.add(null);
         } else if (property instanceof IntegerProperty) {
           arguments.add(property.getIntValue());
         } else if (property instanceof FloatProperty) {
           arguments.add(property.getFloatValue());
         } else if (property instanceof StringProperty) {
           arguments.add(property.getStringValue());
         } else if (property instanceof CollectionProperty) {
           arguments.add(convertCollectionValue((CollectionProperty) property));
         } else {
           arguments.add(property.getObjectValue());
         }
       }
     }
     return arguments;
   }
 
   private Collection<?> convertCollectionValue(CollectionProperty collection) {
     Collection<?> values = (Collection<?>) collection.getObjectValue();
     Collection<Object> items = new ArrayList<Object>();
     Iterator<?> iterator = values.iterator();
     while (iterator.hasNext()) {
       Object obj = iterator.next();
       if (obj instanceof JMeterProperty) {
         AbstractProperty property = (AbstractProperty) obj;
         if (property instanceof NullProperty) {
           items.add(null);
         } else if (property instanceof StringProperty) {
           items.add(property.getStringValue());
         } else if (property instanceof ObjectProperty) {
           items.add(property.getObjectValue());
         } else if (property instanceof IntegerProperty) {
           items.add(property.getIntValue());
         } else if (property instanceof FloatProperty) {
           items.add(property.getFloatValue());
         }
       }
     }
 
     return items;
   }
 
   public CookieManager getCookieManager() {
     return (CookieManager) getProperty(COOKIE_MANAGER).getObjectValue();
   }
 
   public void addTestElement(TestElement el) {
     if (el instanceof CookieManager) {
       setCookieManager((CookieManager) el);
     } else {
       super.addTestElement(el);
     }
   }
 
   @Override
   public SampleResult sample(Entry e) {
     SampleResult result = process();
     result.setSampleLabel(getName());
     return result;
   }
 
   Object ping(AMFConnection connection) throws ClientStatusException,
       ServerStatusException {
     CommandMessage ping = new CommandMessage();
     ping.setOperation(CommandMessage.CLIENT_PING_OPERATION);
     HashMap<String, Object> headers = new HashMap<String, Object>();
     headers.put("DSMessagingVersion", 1);
     headers.put("DSId", null);
     ping.setHeaders(headers);
     ping.setMessageId(UUID.randomUUID().toString().toUpperCase());
     return connection.call(null, ping);
   }
 
   Object call(AMFConnection connection, String destination, String method,
       Object... arguments) throws ClientStatusException, ServerStatusException {
     RemotingMessage message = new RemotingMessage();
     message.setDestination(destination);
     message.setOperation(method);
 
     HashMap<String, Object> headers = new HashMap<String, Object>();
     headers.put("DSId", null);
     headers.put("DSEndpoint", "globalamf");
     message.setHeaders(headers);
 
     message.setMessageId(UUID.randomUUID().toString().toUpperCase());
     message.setClientId("42EA657F-7A69-4623-9A9D-F5BFBD904E24");
     message.setBody(arguments);
     return connection.call(null, new Object[] { message });
   }
 
   AMFSamplerResult process() {
     AMFSamplerResult result = new AMFSamplerResult();
 
     String address = getAddress();
     String service = getService();
     String method = getMethod();
 
     if (connection != null) {
       connection.close();
       connection = null;
     }
 
     if (address.startsWith("https")) {
       connection = new AMFSConnection();
     } else {
       connection = new AMFConnection();
     }
 
     // Set cookie to connection
     CookieManager cookieManager = getCookieManager();
     if (cookieManager != null) {
       CollectionProperty properties = cookieManager.getCookies();
       PropertyIterator iterator = properties.iterator();
 
       StringBuffer buffer = new StringBuffer();
       while (iterator.hasNext()) {
         JMeterProperty property = iterator.next();
         Cookie cookie = (Cookie) property.getObjectValue();
         buffer.append(String.format("%s=%s", cookie.getName(),
             cookie.getValue()));
       }
 
       connection.addHttpRequestHeader(AMFConnection.COOKIE, buffer.toString());
     }
 
     if (address.startsWith("https")) {
       JsseSSLManager sslManager = (JsseSSLManager) SSLManager.getInstance();
       try {
         SSLContext context = sslManager.getContext();
         AMFSConnection sslConnection = (AMFSConnection) connection;
         sslConnection.setSSLFactory(context.getSocketFactory());
       } catch (GeneralSecurityException e) {
       }
 
     }
 
     try {
       connection.connect(address);
 
       ping(connection);
 
       List<Object> arguments = getArguments();
       AcknowledgeMessage message = (AcknowledgeMessage) call(connection,
           service, method, arguments.toArray());
       result.setResult(message.getBody());
       result.setMessageID(message.getMessageId());
       result.setResponseHeaders(message.getHeaders().toString());
       result.setResponseData(message.toString().getBytes());
       result.setResponseMessage(message.toString());
 
       result.setSuccessful(true);
     } catch (Exception e) {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       PrintStream ps = new PrintStream(baos);
       e.printStackTrace(ps);
 
       if (e instanceof ServerStatusException) {
         result.setErrorMsg(((ServerStatusException) e).getData().toString());
       } else {
         result.setErrorMsg(e.getMessage());
       }
 
       result.setSuccessful(false);
     }
 
     return result;
   }
 
   @Override
   public boolean interrupt() {
     AMFConnection conn = connection;
     if (conn != null) {
       connection = null;
       conn.close();
     }
     return conn != null;
   }
 
}
