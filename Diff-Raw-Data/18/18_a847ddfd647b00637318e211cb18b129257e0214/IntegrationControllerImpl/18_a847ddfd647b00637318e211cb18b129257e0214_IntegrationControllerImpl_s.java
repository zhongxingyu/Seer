 /**
  * Apache License 2.0
  */
 package com.googlecode.madschuelerturnier.business.integration;
 
 import com.googlecode.madschuelerturnier.business.integration.core.TransportEndpointSender;
 import com.googlecode.madschuelerturnier.model.enums.MessageTyp;
 import com.googlecode.madschuelerturnier.model.integration.IncommingMessage;
 import com.googlecode.madschuelerturnier.model.integration.MessageWrapperToSend;
 import com.googlecode.madschuelerturnier.model.integration.OutgoingMessage;
 import com.googlecode.madschuelerturnier.model.integration.state.MasterStateEnum;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.ApplicationEventPublisher;
 import org.springframework.context.ApplicationEventPublisherAware;
 import org.springframework.context.ApplicationListener;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.*;
 
 /**
  * Kontrolliert die eingehenden und ausgehenden JMS Verbindungen und verbindet diese mit dem ApplicationEventPublisher
  * Diese Klasse startet den eingebetteten JMS Server
  *
  * @author $Author: marthaler.worb@gmail.com $
  * @since 1.2.8
  */
 @Component
 public class IntegrationControllerImpl extends Thread implements ApplicationEventPublisherAware, ApplicationListener<OutgoingMessage>, IntegrationController {
 
     private final static long WAIT_IF_NO_MESSAGE = 1000;
     private static final Logger LOG = Logger.getLogger(IntegrationControllerImpl.class);
     private boolean master;
 
     private LinkedList<MessageWrapperToSend> incommingMessages = new LinkedList<MessageWrapperToSend>();
     private LinkedList<MessageWrapperToSend> outgoingMessages = new LinkedList<MessageWrapperToSend>();
 
     private Set<String> allIncommingMessagesIDs = new HashSet<String>();
     private MessageWrapperToSend registration;
     private Map<String, TransportEndpointSender> senderEndpoints = new HashMap<String, TransportEndpointSender>();
     private List<String> messagefilter = new ArrayList<String>();
     @Value("${transport.remote.address}")
     private String remoteConnectionString = "";
     @Value("${transport.local.address}")
     private String ownConnectionString = "";
     private MessageWrapperToSend latest = null;
     private ApplicationEventPublisher applicationEventPublisher = null;
 
     private int inboundMessages;
 
     private int outboundMessages;
 
 
     private Map<String, MessageWrapperToSend> storeMap = new LinkedHashMap<String, MessageWrapperToSend>();
 
     private boolean running = true;
 
     public IntegrationControllerImpl() {
 
     }
 
     public IntegrationControllerImpl(String ownAddress, String remoteAddress) {
         this.ownConnectionString = ownAddress;
         this.remoteConnectionString = remoteAddress;
     }
 
     public MessageWrapperToSend pullMessageForNode(String id) {
         TransportEndpointSender sender = this.senderEndpoints.get(id);
         if (sender == null) {
             return null;
         }
         return this.senderEndpoints.get(id).getMessageToSendOutRequest();
     }
 
     @PostConstruct
     public void init() {
 
         // own adress erraten, wenn nicht gesetzt
         if(this.ownConnectionString.contains("${")){
 
             if(System.getProperty("transport.local.address") != null && !System.getProperty("transport.local.address").equals("") ){
                 this.ownConnectionString = System.getProperty("transport.local.address");
             }else{
 
             try {
                 InetAddress ip =InetAddress.getLocalHost();
                 this.ownConnectionString = "http://" + ip.getHostAddress() + "/app/transport";
             } catch (UnknownHostException e) {
                 LOG.error(e.getMessage(),e);
                 this.ownConnectionString = "http://"+System.currentTimeMillis()+"/app/transport";
             }
             }
         }
 
         MessageWrapperToSend wr = new MessageWrapperToSend();
         wr.setSource(this.ownConnectionString);
         // registration request senden
         wr.setTyp(MessageTyp.REGISTRATION_REQUEST);
 
         this.registration = wr;
         if (remoteConnectionString != null && !"".equals(remoteConnectionString)) {
 
             if (remoteConnectionString.split(",").length > 1) {
                 String[] arr = remoteConnectionString.split(",");
                 for (String ep : arr) {
                     createSender(ep);
                 }
             } else {
                 if(remoteConnectionString.length() > 0 && !remoteConnectionString.contains("${")){
                 createSender(remoteConnectionString);
                 }
             }
         }
         this.start();
     }
 
     public MessageWrapperToSend createAckMessage(String id) {
         MessageWrapperToSend wrapper = new MessageWrapperToSend();
         wrapper.setSource(ownConnectionString);
         wrapper.setTyp(MessageTyp.ACK);
         wrapper.setPayload(id);
         return wrapper;
     }
 
     // von onApplication event
     private void sendMessage(String id, Serializable object, MessageTyp typ, boolean trans) {
         MessageWrapperToSend wr=null;
         for (TransportEndpointSender send : senderEndpoints.values()) {
 
             wr = new MessageWrapperToSend();
             wr.setPayload(object);
             wr.setId(id);
             wr.setTyp(typ);
             wr.setTrans(trans);
             wr.setSource(ownConnectionString);
 
             send.sendMessage(wr);
         }
         storeMap.put(id, wr);
     }
 
     private synchronized void sendMessage(MessageWrapperToSend wr) {
         this.outgoingMessages.add(wr);
     }
 
     public MessageWrapperToSend getLatest() {
         return this.latest;
     }
 
 
     public void messageReceivedFromRemote(MessageWrapperToSend wr) {
         this.incommingMessages.add(wr);
     }
 
     public void run() {
 
         try {
             Thread.sleep(3000);
         } catch (InterruptedException e) {
             LOG.error(e.getMessage(), e);
         }
 
         while (running) {
 
             boolean sleepFlag = true;
 
             // 1) versenden
             while (outgoingMessages.size() > 0) {
 
                 sleepFlag = false;
 
                 MessageWrapperToSend outgoingMessage = outgoingMessages.removeFirst();
 
                 if (outgoingMessage != null) {
 
                     for (TransportEndpointSender send : senderEndpoints.values()) {
                         send.sendMessage(outgoingMessage);
                     }
 
                     if (outgoingMessage.getTyp() == MessageTyp.PAYLOAD) {
 
                         // setzen der ausgehenden integration in zwischenspeicher
                        if (outgoingMessage.isTrans()) {
                            storeMap.put(outgoingMessage.getId(), null);
                        } else {
                             storeMap.put(outgoingMessage.getId(), outgoingMessage);

                         }
                     }
                 }
             }
 
             MessageWrapperToSend incommingMessage = null;
 
             // 2) empfangen
             while (!this.incommingMessages.isEmpty()) {
                 sleepFlag = false;
                 incommingMessage = this.incommingMessages.removeFirst();
 
                 if (incommingMessage != null && !allIncommingMessagesIDs.contains(incommingMessage.getId())) {
 
                     // weiterschicken
                     this.sendMessage(incommingMessage);
 
                     LOG.debug("IntegrationControllerImpl: nachricht angekommen: " + incommingMessage);
                     allIncommingMessagesIDs.add(incommingMessage.getId());
 
                     // speichern in der map fuers weitersenden im fall, dass dieser node master ist und eine anmeldung passiert
                     if (!incommingMessage.isTrans()) {
                         storeMap.put(incommingMessage.getId(), incommingMessage);
                     }
 
                     // registration request behandeln falls noetig
                     if (handleRegistrationRequest(incommingMessage)) {
                         continue;
                     }
 
                     // master state behandeln
                     if (handleMasterState(incommingMessage)) {
                         continue;
                     }
 
                     latest = incommingMessage;
 
                     IncommingMessage in = new IncommingMessage(this);
                     in.setPayload(incommingMessage.getPayload());
                     if (applicationEventPublisher != null) {
                         this.applicationEventPublisher.publishEvent(in);
                         this.inboundMessages++;
                     } else{
                         LOG.warn("uebergeben einer nachricht an den internen context nicht moeglich, keinen applicationEventPublisher");
                     }
 
 
                 }
             }
 
             if (sleepFlag) {
                 try {
                     Thread.sleep(WAIT_IF_NO_MESSAGE);
                 } catch (InterruptedException e) {
                     LOG.error(e.getMessage(), e);
                 }
             }
         }
     }
 
     private boolean handleRegistrationRequest(MessageWrapperToSend obj) {
         if (obj.getTyp().equals(MessageTyp.REGISTRATION_REQUEST)) {
             TransportEndpointSender sender = null;
             String url = obj.getSource();
             if (!senderEndpoints.containsKey(url) && !url.equals(this.ownConnectionString)) {
                 createSender(url);
 
                 // alles senden, falls ich der master bin
                 if(this.master){
                    for(String s : this.storeMap.keySet()){
                       Serializable pay = storeMap.get(s);
                        MessageWrapperToSend envelop = new MessageWrapperToSend();
                       envelop.setPayload(pay);
                        envelop.setId(s);
                        envelop.setTyp(MessageTyp.PAYLOAD);
                        this.sendMessage(envelop);
                    }
                 }
                this.sendMessage(registration);
                 return true;
             }
 
             this.sendMessage(obj);
         }
         return false;
     }
 
     private boolean handleMasterState(MessageWrapperToSend obj) {
 
         if (obj.getPayload() instanceof MasterStateEnum) {
             this.master = false;
             return true;
         }
         return false;
     }
 
     private synchronized TransportEndpointSender createSender(String url) {
 
         if (this.senderEndpoints.containsKey(url)) {
             LOG.info("versuche einen neuen senderEndpoints zu kreieren, den es bereits gibt: " + url);
             return this.senderEndpoints.get(url);
         }
 
         TransportEndpointSender send = new TransportEndpointSender(url, this.ownConnectionString, this);
         senderEndpoints.put(url, send);
         this.sendMessage(this.registration);
         return send;
     }
 
 
     @PreDestroy
     public void shutdown() {
 
         for (TransportEndpointSender send : senderEndpoints.values()) {
             send.teardown();
         }
 
         this.running = false;
     }
 
     @Override
     public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
         this.applicationEventPublisher = applicationEventPublisher;
     }
 
     @Override
     public void onApplicationEvent(OutgoingMessage event) {
         this.outboundMessages = this.outboundMessages + 1;
         Serializable obj = event.getPayload();
         this.sendMessage(UUID.randomUUID().toString(), obj, MessageTyp.PAYLOAD, event.isTrans());
     }
 
     public void sendMessage(OutgoingMessage event){
         onApplicationEvent(event);
     }
 
     public List<String> getMessagefilter() {
         return messagefilter;
     }
 
     public void setMessagefilter(List<String> messagefilter) {
         this.messagefilter = messagefilter;
     }
 
     @Override
     public boolean isMaster() {
         return this.master;
     }
 
 
     public void setMaster(boolean master) {
         LOG.warn("master set");
     }
 
     public void thisMaster() {
         if (this.master == false) {
             this.master = true;
             MasterStateEnum ms = new MasterStateEnum();
             OutgoingMessage m = new OutgoingMessage(ms);
             m.setPayload(ms);
             m.setTrans(true);
             this.onApplicationEvent(m);
         }
     }
 
     @Override
     public int countOutboundMessages() {
         return this.outboundMessages;
     }
 
     @Override
     public int countInboundMessages() {
         return this.inboundMessages;
     }
 
     @Override
     public int countStoredObjects() {
         return this.storeMap.size();
     }
 
     @Override
     public List<TransportEndpointSender> getAllReceivers() {
         return new LinkedList<TransportEndpointSender>(this.senderEndpoints.values());
     }
 
     @Override
     public void registerReceiver(String address) {
         MessageWrapperToSend wr = new MessageWrapperToSend();
         wr.setSource(address);
         // registration request senden
         wr.setTyp(MessageTyp.REGISTRATION_REQUEST);
         wr.setId(UUID.randomUUID().toString());
 
         // so tun als ob der request von aussen gekommen waere
         this.messageReceivedFromRemote(wr);
     }
 
     @Override
     public String getOwnAddress() {
         return this.ownConnectionString;
     }
 }
