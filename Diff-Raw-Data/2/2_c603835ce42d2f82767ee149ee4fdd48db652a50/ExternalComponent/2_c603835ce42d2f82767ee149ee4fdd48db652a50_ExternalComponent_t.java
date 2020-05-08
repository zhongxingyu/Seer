 package org.xmpp.component;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.jivesoftware.whack.ExternalComponentManager;
 import org.xmpp.packet.*;
 import org.xmpp.packet.PacketError.Condition;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Thiago
  * Date: 09/02/12
  * Time: 12:01
  */
 public class ExternalComponent extends AbstractComponent {
     static final Logger log = Logger.getLogger(ExternalComponent.class);
     private final ConcurrentHashMap<String, List<NamespaceProcessor>> processors = new ConcurrentHashMap<String, List<NamespaceProcessor>>();
     private final List<MessageProcessor> messageProcessors = new ArrayList<MessageProcessor>(1);
     private ExternalComponentManager manager;
 
     /**
      * The XMPP domain to which this component is registered to.
      */
     private final String serverDomain;
 
     /**
      * The name of this component.
      */
     private final String name;
 
     /**
      * The description of this component.
      */
     private final String description;
 
     private final JID jid;
 
     /**
      * Create a new component which provides weather information.
      *
      * @param name         The name of this component.
      * @param description  The name of this component.
      * @param serverDomain The XMPP domain to which this component is registered to.
      */
     public ExternalComponent(final String name, final String description, String serverDomain) {
         this.name = name;
         this.description = description;
         this.serverDomain = serverDomain;
         this.jid = new JID(name + "." + serverDomain);
     }
 
     protected IQ _createPacketError(final IQ iq, final Condition condition) {
         final PacketError pe = new PacketError(condition);
         final IQ ret = IQ.createResultIQ(iq);
         ret.setError(pe);
         //
         //
         return ret;
     }
 
     @Override
     public void send(Packet packet) {
         if (manager != null) {
             if (packet.getFrom() == null)
                 packet.setFrom(jid);
             log.debug("Sending XMPP: " + packet.toXML());
             manager.sendPacket(this, packet);
         }
     }
 
     protected IQ _createPacketError(final Message message, final Condition condition) {
         final PacketError pe = new PacketError(condition);
         final IQ ret = new IQ(IQ.Type.result);
         ret.setID(message.getID());
         ret.setError(pe);
         //
         //
         return ret;
     }
 
     /**
      * Handle a received message and answer the weather information of the requested station id.
      * The request must be made using Message packets where the body of the message should be the
      * station id.<p>
      * <p/>
      * Note: I don't know the list of valid station ids so if you find the list please send it to me
      * so I can add it to this example.
      *
      * @param message the Message requesting information about a certain station id.
      */
     @Override
     protected void handleMessage(Message message) {
         //
 
         final JID toJid = message.getTo();
         final JID fromJid = message.getFrom();
         final String body = message.getBody();
         if (toJid == null || fromJid == null || null == body || body.length() == 0) {
             send(_createPacketError(message, Condition.bad_request));
             return;
         }
 
         try {
             for (final MessageProcessor m : messageProcessors) {
                 m.processMessage(message);
             }
         } catch (ServiceException e) {
             log.warn("Exception Handling Outgoing Message", e);
         }
     }
 
     @Override
     protected void handlePresence(Presence presence) {
         //
     }
 
     @Override
     protected IQ handleIQGet(final IQ iq) throws Exception {
         //logger.debug(iq);
 
 
         // Get 'from'.
         final JID jid = iq.getFrom();
         if (null == jid) return null;
 
         // Get the child element.
         final Element e = iq.getChildElement();
         if (null == e) return null;
 
         // Get namespace.
         final Namespace namespace = e.getNamespace();
         if (null == namespace) return null;
 
         // Parse URI from namespace.
         final String ns = namespace.getURI();
 
         for (final NamespaceProcessor np : processors.get(ns))
             if (null != np) {
                 return np.processIQGet(iq);
             }
 
         return null;
     }
 
     @Override
     protected IQ handleIQSet(final IQ iq) throws Exception {
         //logger.debug(iq);
 
 
         // Get 'from'.
         final JID jid = iq.getFrom();
         if (null == jid) return null;
 
         // Get the child element.
         final Element e = iq.getChildElement();
         if (null == e) return null;
 
         // Get namespace.
         final Namespace namespace = e.getNamespace();
         if (null == namespace) return null;
 
         // Parse URI from namespace.
         final String ns = namespace.getURI();
 
         for (final NamespaceProcessor np : processors.get(ns))
             if (null != np) {
                 return np.processIQSet(iq);
             }
 
         return null;
     }
 
     @Override
     protected void handleIQError(final IQ iq) {
         //
 
         // Get 'to'.
         final JID toJid = iq.getTo();
         if (null == toJid) return;
 
         // Get 'from'.
         final JID fromJid = iq.getFrom();
         if (null == fromJid) return;
 
         // Get the child element.
         final Element e = iq.getChildElement();
         if (null == e) return;
 
         // Get namespace.
         final Namespace namespace = e.getNamespace();
         if (null == namespace) return;
 
         // Parse URI from namespace.
         final String ns = namespace.getURI();
 
         for (final List<NamespaceProcessor> npl : processors.values()) {
             for (final NamespaceProcessor np : npl)
                 np.processIQError(iq);
         }
     }
 
     @Override
     protected void handleIQResult(final IQ iq) {
 
         log.debug("Received Result: " + iq.toXML());
 
         // Get 'to'.
         final JID toJid = iq.getTo();
         if (null == toJid) return;
 
         // Get 'from'.
         final JID fromJid = iq.getFrom();
         if (null == fromJid) return;
 
         // Get the child element.
         final Element e = iq.getChildElement();
         if (null == e) return;
 
         // Get namespace.
         final Namespace namespace = e.getNamespace();
         if (null == namespace) return;
 
         // Parse URI from namespace.
         final String ns = namespace.getURI();
 
         for (final List<NamespaceProcessor> npl : processors.values()) {
             for (final NamespaceProcessor np : npl)
                np.processIQResult(iq);
         }
 
     }
 
     public void addProcessor(final NamespaceProcessor processor) {
 
         List<NamespaceProcessor> lnp = processors.get(processor.getNamespace());
         if (lnp == null) {
             lnp = new ArrayList<NamespaceProcessor>(1);
             processors.put(processor.getNamespace(), lnp);
         }
 
         lnp.add(processor);
         log.info("Processor Added: " + processor.getNamespace());
 
     }
 
     public void addMessageProcessor(final MessageProcessor messageProcessor) {
         messageProcessors.add(messageProcessor);
     }
 
     @Override
     public String getDescription() {
         return description;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     public ExternalComponentManager getManager() {
         return manager;
     }
 
     public void setManager(ExternalComponentManager manager) {
         this.manager = manager;
     }
 
     public String getServerDomain() {
         return serverDomain;
     }
 
     public JID getJID() {
         return jid;
     }
 }
