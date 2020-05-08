 /**
  * Copyright 2013 Tommi S.E. Laukkanen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.agocontrol.client;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.agocontrol.dao.BusDao;
 import org.agocontrol.dao.ElementDao;
 import org.agocontrol.dao.EventDao;
 import org.agocontrol.model.Bus;
 import org.agocontrol.model.BusConnectionStatus;
 import org.agocontrol.model.Element;
 import org.agocontrol.model.ElementType;
 import org.agocontrol.model.Event;
 import org.apache.log4j.Logger;
 import org.apache.qpid.client.AMQAnyDestination;
 import org.apache.qpid.messaging.Address;
 import org.vaadin.addons.sitekit.model.Company;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.MapMessage;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.UUID;
 
 /**
  * Ago control qpid client.
  *
  * @author Tommi S.E. Laukkanen
  */
 public class BusClient {
     /** The logger. */
     private static final Logger LOGGER = Logger.getLogger(BusClient.class);
     /** Default bus name. */
     private static final String DEFAULT = "";
     /**
      * The entityManagerFactory.
      */
     private EntityManagerFactory entityManagerFactory;
     /**
      * JSON object mapper.
      * */
     private final ObjectMapper mapper = new ObjectMapper();
     /**
      * The context.
      */
     private final Context context;
     /**
      * The connection factory.
      */
     private final ConnectionFactory connectionFactory;
     /**
      * The connection.
      */
     private final Connection connection;
     /**
      * The session.
      */
     private final Session session;
     /**
      * The reply queue.
      */
     private final Queue replyQueue;
     /**
      * The message producer.
      */
     private final MessageProducer messageProducer;
     /**
      * The message consumer.
      */
     private final MessageConsumer messageConsumer;
     /**
      * The reply consumer.
      */
     private final MessageConsumer replyConsumer;
     /**
      * Set true to shutdown threads.
      */
     private boolean closeRequested = false;
     /**
      * Event handler thread.
      */
     private final Thread eventHandlerThread;
     /**
      * Reply handler thread.
      */
     private final Thread replyHandlerThread;
     /**
      * Inventory request thread.
      */
     private final Thread inventoryRequestThread;
     /**
      * The bus this client is connected to.
      */
     private final Bus bus;
 
     /**
      * Constructor which sets entityManagerFactory.
      *
      * @param entityManagerFactory the entityManagerFactory
      * @param bus the bus to connect to
      *
      * @throws Exception if exception occurs in connecting to bus.
      */
     public BusClient(final EntityManagerFactory entityManagerFactory, final Bus bus) throws Exception {
         this.entityManagerFactory = entityManagerFactory;
         this.bus = bus;
 
         final String userName = bus.getUserName();
         final String password = bus.getUserPassword();
         final String host = bus.getHost();
         final Integer port = bus.getPort();
         //final String userName = "agocontrol";
         //final String password = "letmein";
 
 
         final Properties properties = new Properties();
         properties.put("java.naming.factory.initial",
                 "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
         properties.put("connectionfactory.qpidConnectionfactory",
                 "amqp://" + userName + ":" + password + "@agocontrolvaadinsite/client" +
                         "?brokerlist='tcp://" + host + ":" + port + "'");
 
         context = new InitialContext(properties);
         connectionFactory = (ConnectionFactory) context.lookup("qpidConnectionfactory");
         connection = connectionFactory.createConnection();
 
         connection.start();
 
         session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         final Destination sendDestination = new AMQAnyDestination(new Address("agocontrol", "", null));
         final Destination receiveDestination = new AMQAnyDestination(new Address("agocontrol", "#", null));
 
         replyQueue = session.createTemporaryQueue();
 
         messageProducer = session.createProducer(sendDestination);
         messageConsumer = session.createConsumer(receiveDestination);
         replyConsumer = session.createConsumer(replyQueue);
 
         eventHandlerThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 final EntityManager entityManager = entityManagerFactory.createEntityManager();
                 final Company company = entityManager.getReference(Company.class, bus.getOwner().getCompanyId());
                 while (!closeRequested) {
                     try {
                         Thread.sleep(100);
                         handleEvent(entityManager, company);
                     } catch (final Throwable t) {
                         LOGGER.error("Error in handling events.", t);
                     }
                 }
                 entityManager.close();
             }
         });
         eventHandlerThread.start();
 
         replyHandlerThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 final EntityManager entityManager = entityManagerFactory.createEntityManager();
                 final Company company = entityManager.getReference(Company.class, bus.getOwner().getCompanyId());
                 while (!closeRequested) {
                     try {
                         Thread.sleep(100);
                         handleReply(entityManager, company);
                     } catch (final Throwable t) {
                         LOGGER.error("Error in handling events.", t);
                     }
                 }
                 entityManager.close();
             }
         });
         replyHandlerThread.start();
 
         inventoryRequestThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 final EntityManager entityManager = entityManagerFactory.createEntityManager();
                 while (!closeRequested) {
                     try {
                         requestInventory(entityManager);
                     } catch (final Throwable t) {
                         LOGGER.error("Error in inventory request message sending.", t);
                     }
                     try {
                         Thread.sleep(5 * 60 * 1000);
                     } catch (final Throwable t) {
                         LOGGER.warn("Interrupted inventory request message send wait.", t);
                     }
                 }
                 entityManager.close();
             }
         });
         inventoryRequestThread.start();
         LOGGER.info("Connected to bus: " + bus.getName());
     }
 
 
     /**
      * Closes client.
      *
      * @throws Exception if exception occurs.
      */
     public final void close() throws Exception {
         closeRequested = true;
         inventoryRequestThread.interrupt();
         inventoryRequestThread.join();
         replyHandlerThread.interrupt();
         replyHandlerThread.join();
         eventHandlerThread.interrupt();
         eventHandlerThread.join();
         connection.close();
         context.close();
         LOGGER.info("Disconnected from bus: " + bus.getName());
     }
 
 
     /**
      * Request inventory.
      *
      * @param entityManager the entityManager
      * @throws Exception exception occurs in inventory request message sending.
      */
     private void requestInventory(final EntityManager entityManager) throws Exception {
         final MapMessage message = session.createMapMessage();
         message.setJMSMessageID("ID:" + UUID.randomUUID().toString());
         message.setString("command", "inventory");
         message.setJMSReplyTo(replyQueue);
         messageProducer.send(message);
     }
 
     /**
      * Handle reply.
      *
      * @param entityManager the entityManager
      * @param owner the owning company
      * @throws Exception if exception occurs.
      */
     private void handleReply(final EntityManager entityManager, final Company owner) throws Exception  {
         final MapMessage message = (MapMessage) replyConsumer.receive();
         final Map<String, Object> result = convertMapMessageToMap(message);
 
         final List<Element> elements = new ArrayList<>(ElementDao.getElements(entityManager, owner));
         final Map<String, Element> idElementMap = new HashMap<>();
         final Map<String, Element> nameBuildingMap = new HashMap<>();
 
         for (final Element element : elements) {
             if (element.getType() == ElementType.BUILDING) {
                 nameBuildingMap.put(element.getName(), element);
             }
             idElementMap.put(element.getElementId(), element);
         }
 
         if (!nameBuildingMap.containsKey(DEFAULT)) {
             final Element building = new Element(owner, ElementType.BUILDING, DEFAULT, "");
             nameBuildingMap.put(building.getName(), building);
             idElementMap.put(building.getElementId(), building);
             elements.add(building);
         }
 
         if (result.containsKey("rooms")) {
             final Map<String, Object> rooms = (Map) result.get("rooms");
             for (final String roomId : ((Map<String, Object>) result.get("rooms")).keySet()) {
                 final Map<String, Object> roomMessage = (Map) rooms.get(roomId);
                final String roomName = bytesToString(roomMessage.get("name"));
                final String roomLocation = bytesToString(roomMessage.get("location"));
 
                 final Element building;
                 if (nameBuildingMap.containsKey(roomLocation)) {
                     building = nameBuildingMap.get(roomLocation);
                 } else {
                     building = new Element(owner, ElementType.BUILDING, DEFAULT, "");
                     nameBuildingMap.put(DEFAULT, building);
                     elements.add(building);
                     idElementMap.put(building.getElementId(), building);
                 }
 
                 final Element room;
                 if (idElementMap.containsKey(roomId)) {
                     room = idElementMap.get(roomId);
                     room.setParentId(building.getElementId());
                     room.setName(roomName);
                 } else {
                     room = new Element(roomId, building.getElementId(), owner, ElementType.ROOM, roomName, "");
                     elements.add(room);
                     idElementMap.put(room.getElementId(), room);
                 }
             }
         }
 
         if (result.containsKey("inventory")) {
             final Map<String, Object> inventory = (Map) result.get("inventory");
             for (final String elementId : ((Map<String, Object>) result.get("inventory")).keySet()) {
                 final Map<String, Object> elementMessage = (Map) inventory.get(elementId);
                final String name = bytesToString(elementMessage.get("name"));
                final String roomId = bytesToString(elementMessage.get("room"));
                final String category = bytesToString(elementMessage.get("devicetype"));
 
                 final Element parent;
                 if (idElementMap.containsKey(roomId)) {
                     parent = idElementMap.get(roomId);
                 } else {
                     parent =  nameBuildingMap.get(DEFAULT);
                 }
 
                 final Element element;
                 if (idElementMap.containsKey(elementId)) {
                     element = idElementMap.get(elementId);
                     element.setParentId(parent.getElementId());
                     element.setName(name);
                     element.setCategory(category);
                     element.setType(ElementType.DEVICE);
                 } else {
                     element = new Element(elementId, parent.getElementId(), owner, ElementType.DEVICE, name, category);
                     elements.add(element);
                     idElementMap.put(element.getElementId(), element);
                 }
             }
         }
 
         final List<Element> roots = new ArrayList<>();
         final Map<Element, Set<Element>> treeMap = new HashMap<>();
 
         for (final Element element : elements) {
             if (element.getElementId().equals(element.getParentId())) {
                 element.setTreeDepth(0);
                 roots.add(element);
             } else {
                 final Element parent = idElementMap.get(element.getParentId());
                 if (parent != null) {
                     if (!treeMap.containsKey(parent)) {
                         treeMap.put(parent, new TreeSet<Element>());
                     }
                     treeMap.get(parent).add(element);
                 }
             }
         }
 
         final LinkedList<Element> elementsToIterate = new LinkedList<Element>();
         elementsToIterate.addAll(roots);
 
         int startTreeIndex = bus.getBusId().hashCode();
         int treeIndex = startTreeIndex;
         while (elementsToIterate.size() > 0) {
             final Element element = elementsToIterate.removeFirst();
             element.setTreeIndex(++treeIndex);
             final Set<Element> children = treeMap.get(element);
             if (children != null) {
                 for (final Element child : children) {
                     child.setTreeDepth(element.getTreeDepth() + 1);
                 }
                 elementsToIterate.addAll(children);
             }
         }
 
         ElementDao.saveElements(entityManager, elements);
 
         final Bus loadedBus = BusDao.getBus(entityManager, bus.getBusId());
         if (loadedBus != null) {
             loadedBus.setInventorySynchronized(new Date());
             BusDao.saveBuses(entityManager, Collections.singletonList(loadedBus));
             LOGGER.info("Synchronized inventory from bus: " + bus.getName());
         }
 
         //return treeIndex;
     }
 
     /**
      * Handle event.
      *
      * @param entityManager the entityManager
      * @param owner the owning company
      * @throws Exception if exception occurs.
      */
     private void handleEvent(final EntityManager entityManager, final Company owner) throws Exception  {
         final MapMessage message = (MapMessage) messageConsumer.receive();
 
         final String subject = message.getStringProperty("qpid.subject");
         if (subject == null || !subject.startsWith("event")) {
             return;
         }
         if (subject.equals("event.environment.timechanged")) {
             return; // Ignore time changed events.
         }
 
         final Map<String, Object> map = convertMapMessageToMap(message);
 
         map.put("event", subject);
 
         final String eventJsonString = mapper.writeValueAsString(map);
         EventDao.saveEvents(entityManager, Collections.singletonList(
                 new Event(owner, eventJsonString, new Date())));
     }
 
     /**
      * Converts UTF-8 encoded byte to String.
      *
      * @param bytes the bytes
      * @return the string
      */
     private String bytesToString(final Object bytes) {
         if (bytes == null) {
             return null;
         }
         return new String((byte[]) bytes, Charset.forName("UTF-8"));
     }
 
     /**
      * Converts MapMessage to Map.
      *
      * @param message the message
      * @return the map
      * @throws JMSException if exception occurs in conversion.
      */
     private Map<String, Object> convertMapMessageToMap(final MapMessage message) throws JMSException {
         final Map<String, Object> map = new HashMap<>();
 
         final Enumeration<String> keys = message.getMapNames();
         while (keys.hasMoreElements()) {
             final String key = keys.nextElement();
             map.put(key, message.getObject(key));
         }
         return map;
     }
 
 }
