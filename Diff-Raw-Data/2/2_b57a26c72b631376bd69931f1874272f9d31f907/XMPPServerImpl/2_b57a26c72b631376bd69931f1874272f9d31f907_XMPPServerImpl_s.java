 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.xmpp;
 
 import com.google.common.util.concurrent.AbstractIdleService;
 import com.stackframe.sarariman.Authenticator;
 import com.stackframe.sarariman.AuthenticatorImpl;
 import com.stackframe.sarariman.Directory;
 import com.stackframe.sarariman.Employee;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.Executor;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.vysper.mina.TCPEndpoint;
 import org.apache.vysper.storage.OpenStorageProviderRegistry;
 import org.apache.vysper.storage.StorageProviderRegistry;
 import org.apache.vysper.xmpp.addressing.Entity;
 import org.apache.vysper.xmpp.addressing.EntityImpl;
 import org.apache.vysper.xmpp.authorization.AccountCreationException;
 import org.apache.vysper.xmpp.authorization.AccountManagement;
 import org.apache.vysper.xmpp.authorization.UserAuthorization;
 import org.apache.vysper.xmpp.delivery.StanzaRelay;
 import org.apache.vysper.xmpp.delivery.failure.DeliveryException;
 import org.apache.vysper.xmpp.delivery.failure.DeliveryFailureStrategy;
 import org.apache.vysper.xmpp.modules.extension.xep0045_muc.MUCModule;
 import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
 import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
 import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PublishSubscribeModule;
 import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
 import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
 import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
 import org.apache.vysper.xmpp.modules.roster.AskSubscriptionType;
 import org.apache.vysper.xmpp.modules.roster.Roster;
 import org.apache.vysper.xmpp.modules.roster.RosterException;
 import org.apache.vysper.xmpp.modules.roster.RosterGroup;
 import org.apache.vysper.xmpp.modules.roster.RosterItem;
 import org.apache.vysper.xmpp.modules.roster.SubscriptionType;
 import org.apache.vysper.xmpp.modules.roster.persistence.RosterManager;
 import org.apache.vysper.xmpp.server.SessionContext;
 import org.apache.vysper.xmpp.stanza.PresenceStanza;
 import org.apache.vysper.xmpp.stanza.PresenceStanzaType;
 import org.apache.vysper.xmpp.stanza.Stanza;
 import org.apache.vysper.xmpp.stanza.StanzaBuilder;
 import org.apache.vysper.xmpp.state.presence.LatestPresenceCache;
 import org.apache.vysper.xmpp.state.resourcebinding.ResourceRegistry;
 
 /**
  *
  * @author mcculley
  */
 public class XMPPServerImpl extends AbstractIdleService implements XMPPServer {
 
     private final org.apache.vysper.xmpp.server.XMPPServer xmpp = new org.apache.vysper.xmpp.server.XMPPServer("stackframe.com");
 
     private final Directory directory;
 
     private final File keyStore;
 
     private final String keyStorePassword;
 
     private final Executor executor;
 
     public XMPPServerImpl(Directory directory, File keyStore, String keyStorePassword, Executor executor) {
         this.directory = directory;
         this.keyStore = keyStore;
         this.keyStorePassword = keyStorePassword;
         this.executor = executor;
     }
 
     private static Entity entity(Employee employee) {
         return new EntityImpl(employee.getUserName(), "stackframe.com", null);
     }
 
     private static Entity entity(Employee employee, String resource) {
         return new EntityImpl(employee.getUserName(), "stackframe.com", resource);
     }
 
     @Override
     protected void startUp() throws Exception {
         ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout());
         Logger.getRootLogger().addAppender(consoleAppender);
         final Authenticator authenticator = new AuthenticatorImpl(directory);
         StorageProviderRegistry providerRegistry = new OpenStorageProviderRegistry() {
             {
                 add(new AccountManagement() {
                     public void addUser(Entity entity, String string) throws AccountCreationException {
                     }
 
                     public void changePassword(Entity entity, String string) throws AccountCreationException {
                     }
 
                     public boolean verifyAccountExists(Entity entity) {
                         return true; // FIXME
                     }
 
                 });
                 add(new UserAuthorization() {
                     public boolean verifyCredentials(Entity entity, String passwordCleartext, Object credentials) {
                         System.err.println("in verifyCredentials with Entity. entity=" + entity);
                         return authenticator.checkCredentials(entity.getNode(), passwordCleartext);
                     }
 
                     public boolean verifyCredentials(String username, String passwordCleartext, Object credentials) {
                         System.err.println("in verifyCredentials with username. username=" + username);
                         return authenticator.checkCredentials(username, passwordCleartext);
                     }
 
                 });
                 add(new RosterManager() {
                     private final List<RosterGroup> groups = new ArrayList<RosterGroup>();
 
                     {
                         RosterGroup staff = new RosterGroup("staff");
                         groups.add(staff);
                     }
 
                     private RosterItem rosterItem(Employee employee) {
                         return new RosterItem(entity(employee), employee.getDisplayName(), SubscriptionType.BOTH,
                                               AskSubscriptionType.ASK_SUBSCRIBED, groups);
                     }
 
                     public Roster retrieve(final Entity entity) throws RosterException {
                         return new Roster() {
                             public Iterator<RosterItem> iterator() {
                                 Collection<RosterItem> items = new ArrayList<RosterItem>();
                                 for (Employee employee : directory.getEmployees()) {
                                     if (!employee.isActive()) {
                                         continue;
                                     }
 
                                     if (employee.getUserName().equals(entity.getNode())) {
                                         continue;
                                     }
 
                                     items.add(rosterItem(employee));
                                 }
 
                                 items = Collections.unmodifiableCollection(items);
                                 return items.iterator();
                             }
 
                             public RosterItem getEntry(Entity entryEntity) {
                                 Employee employee = directory.getByUserName().get(entryEntity.getNode());
                                 return rosterItem(employee);
                             }
 
                         };
                     }
 
                     public void addContact(Entity entity, RosterItem ri) throws RosterException {
                     }
 
                     public RosterItem getContact(Entity entity, Entity e1) throws RosterException {
                         Employee employee = directory.getByUserName().get(e1.getNode());
                         return rosterItem(employee);
                     }
 
                     public void removeContact(Entity entity, Entity entity1) throws RosterException {
                     }
 
                 });
                 add("org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.storageprovider.LeafNodeInMemoryStorageProvider");
                 add("org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.storageprovider.CollectionNodeInMemoryStorageProvider");
             }
 
         };
 
         xmpp.addEndpoint(new TCPEndpoint());
         xmpp.setStorageProviderRegistry(providerRegistry);
         xmpp.setTLSCertificateInfo(keyStore, keyStorePassword);
 
         xmpp.start();
 
        xmpp.addModule(new MUCModule());
         xmpp.addModule(new XmppPingModule());
         xmpp.addModule(new SoftwareVersionModule());
         xmpp.addModule(new EntityTimeModule());
         xmpp.addModule(new VcardTempModule());
         xmpp.addModule(new PrivateDataModule());
         xmpp.addModule(new PublishSubscribeModule());
     }
 
     @Override
     protected void shutDown() throws Exception {
         xmpp.stop();
     }
 
     @Override
     protected String serviceName() {
         return "XMPP Server";
     }
 
     @Override
     protected Executor executor() {
         return executor;
     }
 
     private static PresenceType type(PresenceStanza stanza) {
         if (PresenceStanzaType.isAvailable(stanza.getPresenceType())) {
             return PresenceType.available;
         } else {
             return PresenceType.unavailable;
         }
     }
 
     private PresenceStanza stanza(Employee employee, Presence p, Entity to) {
         Entity from = entity(employee);
         String lang = null;
         String show = p.getShow().toString();
         String status = p.getStatus();
         PresenceStanzaType type = p.getType() == PresenceType.unavailable ? PresenceStanzaType.UNAVAILABLE : null;
         StanzaBuilder b = StanzaBuilder.createPresenceStanza(from, to, lang, type, show, status);
         return new PresenceStanza(b.build());
     }
 
     private Employee employeeFromJID(String jid) {
         String bareUserName = jid.substring(0, jid.indexOf('@'));
         Employee employee = directory.getByUserName().get(bareUserName);
         return employee;
     }
 
     public Presence getPresence(String username) {
         Employee employee = employeeFromJID(username);
         LatestPresenceCache presenceCache = xmpp.getServerRuntimeContext().getPresenceCache();
         PresenceStanza presence = presenceCache.getForBareJID(entity(employee));
         if (presence == null) {
             return new Presence(PresenceType.unavailable, ShowType.away, null);
         } else {
             try {
                 String showString = presence.getShow();
                 ShowType show = showString == null ? ShowType.away : ShowType.valueOf(showString);
                 Presence p = new Presence(type(presence), show, presence.getStatus(null));
                 return p;
             } catch (Exception e) {
                 System.err.println("exception getting presence status. e=" + e);
                 e.printStackTrace();
                 return new Presence(PresenceType.unavailable, ShowType.away, null);
             }
         }
     }
 
     private Collection<Employee> destinations(Employee from) {
         Collection<Employee> result = new ArrayList<Employee>();
         for (Employee employee : directory.getEmployees()) {
             if (!employee.isActive()) {
                 continue;
             }
 
             if (employee == from) {
                 continue;
             }
 
             result.add(employee);
         }
 
         return result;
     }
 
     public void setPresence(String username, Presence presence) {
         LatestPresenceCache presenceCache = xmpp.getServerRuntimeContext().getPresenceCache();
         Employee employee = employeeFromJID(username);
         presenceCache.put(entity(employee, "sarariman"), stanza(employee, presence, null));
         Collection<Employee> employees = destinations(employee);
         for (Employee destination : employees) {
             Entity to = entity(destination);
             Stanza stanza = stanza(employee, presence, to);
             StanzaRelay relay = xmpp.getServerRuntimeContext().getStanzaRelay();
             ResourceRegistry resourceRegistry = xmpp.getServerRuntimeContext().getResourceRegistry();
             List<SessionContext> sessions = resourceRegistry.getSessions(to);
             for (SessionContext session : sessions) {
                 for (String resource : resourceRegistry.getResourcesForSession(session)) {
                     Entity e = new EntityImpl(to.getNode(), to.getDomain(), resource);
                     try {
                         relay.relay(e, stanza, new DeliveryFailureStrategy() {
                             public void process(Stanza stanza, List<DeliveryException> list) throws DeliveryException {
                                 for (DeliveryException de : list) {
                                     // FIXME: Log this?
                                     System.err.println("de=" + de);
                                     de.printStackTrace();
                                 }
                             }
 
                         });
                     } catch (DeliveryException de) {
                         System.err.println("deliveryException=" + de);
                         de.printStackTrace();
                     }
                 }
             }
         }
     }
 
 }
