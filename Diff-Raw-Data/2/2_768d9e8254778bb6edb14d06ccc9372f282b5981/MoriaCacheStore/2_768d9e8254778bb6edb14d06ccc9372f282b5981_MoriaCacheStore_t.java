 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.store;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import no.feide.moria.log.MessageLogger;
 
 import org.jboss.cache.Fqn;
 import org.jboss.cache.Node;
 import org.jboss.cache.PropertyConfigurator;
 import org.jboss.cache.TreeCache;
 import org.jboss.cache.lock.LockingException;
 import org.jboss.cache.lock.TimeoutException;
 
 /**
  * Distributed store implementation using JBoss Cache.
  * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
  * @version $Revision$
  */
 public final class MoriaCacheStore
 implements MoriaStore {
 
     /** The cache instance. */
     private TreeCache store;
 
     /** The configured state of the store. */
     private Boolean isConfigured = new Boolean(false);
 
     /** The logger used by this class. */
     private MessageLogger log = new MessageLogger(MoriaCacheStore.class);
 
     /** Map to contain the ticket ttl values. */
     private Map ticketTTLs;
 
     /** Map containing the default ttl values. */
     private final Map ticketDefaultTTLs = new HashMap();
 
     /** The common hashmap key for the ticket type. */
     private static final String TICKET_TYPE_ATTRIBUTE = "TicketType";
 
     /** The common hashmap key for the time to live. */
     private static final String TTL_ATTRIBUTE = "TimeToLive";
 
     /** The common hashmap key for the principal. */
     private static final String PRINCIPAL_ATTRIBUTE = "Principal";
 
     /**
      * The common hashmap key for the data attributes (MoriaAuthnAttempt &
      * CachedUserData).
      */
     private static final String DATA_ATTRIBUTE = "MoriaData";
 
     /**
      * The common hashmap key for the userorg attribute.
      */
     private static final String USERORG_ATTRIBUTE = "Userorg";
 
     /** The name of the configuration file property. */
     private static final String CACHE_CONFIG_PROPERTY_NAME = "no.feide.moria.store.cachestoreconf";
 
     /** The name of the ttl percentage property. */
     private static final String REAL_TTL_PERCENTAGE_PROPERTY_NAME = "no.feide.moria.store.real_ttl_percentage";
 
 
     /**
      * Constructs a new instance.
      * @throws MoriaStoreException
      *             If creation of JBoss TreeCache fails.
      */
     public MoriaCacheStore() throws MoriaStoreException {
 
         isConfigured = new Boolean(false);
         log = new MessageLogger(no.feide.moria.store.MoriaCacheStore.class);
 
         try {
             store = new TreeCache();
         } catch (RuntimeException re) {
             throw re;
         } catch (Exception e) {
             throw new MoriaStoreException("Unable to create TreeCache instance.", e);
         }
 
         ticketDefaultTTLs.put(MoriaTicketType.LOGIN_TICKET, new Long(300000L));
         ticketDefaultTTLs.put(MoriaTicketType.SERVICE_TICKET, new Long(300000L));
         ticketDefaultTTLs.put(MoriaTicketType.SSO_TICKET, new Long(28800000L));
         ticketDefaultTTLs.put(MoriaTicketType.TICKET_GRANTING_TICKET, new Long(7200000L));
         ticketDefaultTTLs.put(MoriaTicketType.PROXY_TICKET, new Long(300000L));
     }
 
 
     /**
      * Configures the store. This method expects the properties
      * <code>no.feide.moria.store.cacheconf</code> and
      * <code>no.feide.moria.store.real_ttl_percentage</code> to be set. The
      * former must point to a JBossCache specific configuration file, the latter
      * contain a value between 1 and 100. The method will return without
      * actually executing and thus maintain the current state if called more
      * than once per object instance.
      * @param properties
      *            The properties used to configure the store.
      * @throws MoriaStoreConfigurationException
      *             If something fails during the process of starting the store.
      * @throws IllegalArgumentException
      *             If properties is null.
      * @throws NullPointerException
      *             If defaultTTL is null.
      * @see no.feide.moria.store.MoriaStore#setConfig(java.util.Properties)
      */
     public synchronized void setConfig(final Properties properties)
     throws MoriaStoreConfigurationException {
 
         synchronized (isConfigured) {
             if (isConfigured.booleanValue()) {
                 log.logWarn("setConfig() called on already configured instance.");
                 return;
             }
 
             if (properties == null)
                 throw new IllegalArgumentException("properties cannot be null.");
 
             String cacheConfigProperty = properties.getProperty(CACHE_CONFIG_PROPERTY_NAME);
 
             if (cacheConfigProperty == null)
                 throw new MoriaStoreConfigurationException("Configuration property " + CACHE_CONFIG_PROPERTY_NAME + " must be set.");
 
             String realTTLPercentageProperty = properties.getProperty(REAL_TTL_PERCENTAGE_PROPERTY_NAME);
 
             if (realTTLPercentageProperty == null)
                 throw new MoriaStoreConfigurationException("Configuration property " + REAL_TTL_PERCENTAGE_PROPERTY_NAME + " must be set.");
 
             long realTTLPercentage = Long.parseLong(realTTLPercentageProperty);
 
             if (realTTLPercentage < 1L || realTTLPercentage > 100L)
                 throw new MoriaStoreConfigurationException(REAL_TTL_PERCENTAGE_PROPERTY_NAME + " must be between one and one hundred, inclusive.");
 
             FileInputStream cacheConfigFile;
 
             try {
                 cacheConfigFile = new FileInputStream(new File(new URI(cacheConfigProperty)));
             } catch (FileNotFoundException fnnf) {
                 throw new MoriaStoreConfigurationException("Configuration file '" + cacheConfigProperty + "' not found", fnnf);
             } catch (URISyntaxException uris) {
                throw new MoriaStoreConfigurationException("Illegal configuration property "+ CACHE_CONFIG_PROPERTY_NAME + " (" + cacheConfigProperty + ")", uris);
             }
 
             PropertyConfigurator configurator = new PropertyConfigurator();
 
             try {
                 configurator.configure(store, cacheConfigFile);
             } catch (Exception e) {
                 throw new MoriaStoreConfigurationException("Unable to configure the cache.", e);
             }
 
             TicketTTLEvictionPolicy ticketTTLEvictionPolicy = new TicketTTLEvictionPolicy();
 
             try {
                 ticketTTLEvictionPolicy.parseConfig(store.getEvictionPolicyConfig());
             } catch (Exception e) {
                 throw new MoriaStoreConfigurationException("Unable to get ticket TTL's from config", e);
             }
 
             ticketTTLs = new HashMap();
             TicketTTLEvictionPolicy.RegionValue[] regionValues = ticketTTLEvictionPolicy.getRegionValues();
 
             for (Iterator ticketTypeIterator = MoriaTicketType.TICKET_TYPES.iterator(); ticketTypeIterator.hasNext();) {
                 Long ttl = null;
                 MoriaTicketType ticketType = (MoriaTicketType) ticketTypeIterator.next();
 
                 for (int i = 0; i < regionValues.length; i++) {
                     if (ticketType.toString().equals(regionValues[i].getRegionName())) {
                         ttl = new Long(regionValues[i].getTimeToLive() * realTTLPercentage / 100L);
                         break;
                     }
                 }
 
                 if (ttl == null || ttl.compareTo(new Long(1000L)) < 0) {
                     Object defaultTTL = ticketDefaultTTLs.get(ticketType);
 
                     if (defaultTTL == null)
                         throw new NullPointerException("No default value defined for: " + ticketType);
 
                     ticketTTLs.put(ticketType, defaultTTL);
                     log.logCritical("TTL for " + ticketType + " not found.  Using default value. This is not a good thing.");
                 } else {
                     ticketTTLs.put(ticketType, ttl);
                 }
             }
 
             try {
                 store.start();
             } catch (Exception e) {
                 throw new MoriaStoreConfigurationException("Unable to start the cache", e);
             }
 
             isConfigured = new Boolean(true);
         }
     }
 
 
     /**
      * Stops this instance of the store.
      * @see no.feide.moria.store.MoriaStore#stop()
      */
     public synchronized void stop() {
 
         synchronized (isConfigured) {
             store.stop();
             store = null; // Remove object reference for garbage collection.
             isConfigured = new Boolean(false);
         }
         log.logWarn("The cache has been stopped.");
     }
 
 
     /**
      * Creates an authentication attempt based on a service request.
      *
      * @param requestedAttributes
      *          The user attributes the requesting service asks for.
      * @param responseURLPrefix
      *          The forward part of the url the client is to be redirected to.
      * @param responseURLPostfix
      *          The end part of the url the client is to be redirected to.
      * @param forceInteractiveAuthentication
      *          If the user should be forced to login interactively. I.e. disable
      *          support for single sign-on.
      * @param servicePrincipal
      *          The id of the service doing the request.
      * @return A login ticket identifying the authentication attempt.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If any of the arguments are null, and if responseURLPrefix or servicePrincipal are zero length.
      * @see no.feide.moria.store.MoriaStore#createAuthnAttempt(java.lang.String[],
      *      java.lang.String, java.lang.String, boolean, java.lang.String)
      */
     public String createAuthnAttempt(final String[] requestedAttributes,
             final String responseURLPrefix, final String responseURLPostfix,
             final boolean forceInteractiveAuthentication,
             final String servicePrincipal) throws MoriaStoreException {
 
         MoriaTicket ticket = null;
         MoriaAuthnAttempt authnAttempt;
 
         if (requestedAttributes == null) { throw new IllegalArgumentException("requestedAttributes cannot be null."); }
 
         if (responseURLPrefix == null || responseURLPrefix.equals("")) {
             throw new IllegalArgumentException("responseURLPrefix cannot be null or empty string.");
         }
 
         if (responseURLPostfix == null) { throw new IllegalArgumentException("responseURLPostfix cannot be null."); }
 
         if (servicePrincipal == null || servicePrincipal.equals("")) {
             throw new IllegalArgumentException("servicePrincipal cannot be null or empty string.");
         }
 
         authnAttempt = new MoriaAuthnAttempt(requestedAttributes, responseURLPrefix,
                 responseURLPostfix, forceInteractiveAuthentication, servicePrincipal);
 
         final Long expiryTime = new Long(((Long) ticketTTLs.get(MoriaTicketType.LOGIN_TICKET)).longValue() + new Date().getTime());
         ticket = new MoriaTicket(MoriaTicketType.LOGIN_TICKET, servicePrincipal, expiryTime, authnAttempt, null);
 
         insertIntoStore(ticket);
 
         return ticket.getTicketId();
     }
 
 
     /**
      * Gets the authentication attempt associated with the ticket given as argument.
      * @param ticketId
      *            The ticket ID. Must be a non-empty string.
      * @param keep
      *            If <code>false</code>, the ticket will be removed from the
      *            store before returning. Otherwise keep the ticket.
      * @param servicePrincipal
      *            The principal used by the service to authenticate itself to
      *            Moria. May be <code>null</code>.
      * @return The authentication attempt.
      * @throws IllegalArgumentException
      *             If ticket ID is <code>null</code> or an empty string.
      * @throws NonExistentTicketException
      *             If the ticket does not exist in the store.
      * @throws InvalidTicketException
      *             If the ticket is not associated with an authentication
      *             attempt.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @see no.feide.moria.store.MoriaStore#getAuthnAttempt(java.lang.String,
      *      boolean, java.lang.String)
      */
     public MoriaAuthnAttempt getAuthnAttempt(final String ticketId, final boolean keep, final String servicePrincipal)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         // Sanity checks.
         if (ticketId == null || ticketId.equals(""))
             throw new IllegalArgumentException("Ticket ID must be a non-empty string");
 
         // Accepted ticket types; login ticket or service ticket.
         MoriaTicketType[] potentialTicketTypes = new MoriaTicketType[] {MoriaTicketType.LOGIN_TICKET, MoriaTicketType.SERVICE_TICKET};
 
         // Get ticket from store.
         MoriaTicket ticket = getFromStore(potentialTicketTypes, ticketId);
         if (ticket == null) {
             log.logInfo("Ticket does not exist in the store", ticketId);
             throw new NonExistentTicketException(ticketId);
         }
 
         // Validate ticket, depending on type.
         if (ticket.getTicketType().equals(MoriaTicketType.LOGIN_TICKET))
             validateTicket(ticket, MoriaTicketType.LOGIN_TICKET, null);
         else
             validateTicket(ticket, MoriaTicketType.SERVICE_TICKET, servicePrincipal);
 
         // Is this ticket associated with an authentication attempt?
         MoriaAuthnAttempt authnAttempt = null;
         MoriaStoreData data = ticket.getData();
         if (data != null && data instanceof MoriaAuthnAttempt)
             authnAttempt = (MoriaAuthnAttempt) data;
         else
             throw new InvalidTicketException("No authentication attempt associated with ticket. [" + ticketId + "]");
 
         /* Delete the ticket if so indicated. */
         if (!keep) {
             log.logInfo("Removing ticket from store", ticketId);
             removeFromStore(ticket);
         }
 
         // Return the authentication attempt.
         return authnAttempt;
     }
 
 
     /**
      * Creates a new CachedUserData object in the store and associates it with an SSO
      * ticket which is returned.
      *
      * @param attributes
      *          The attribute map to be cached.
      * @param userorg
      *          The userorg that is to be associated with the ticket.
      * @return The SSO ticket that identifies the cached user data.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *             If attributes is null, or
      *             userorg is null or an empty  string.
      *
      * @see no.feide.moria.store.MoriaStore#cacheUserData(java.util.HashMap, String)
      */
     public String cacheUserData(final HashMap attributes, final String userorg)
     throws MoriaStoreException {
 
         // Sanity checks.
         if (attributes == null)
             throw new IllegalArgumentException("Attributes cannot be null");
         if ((userorg == null) || (userorg.length() == 0))
             throw new IllegalArgumentException("User organization must be a non-empty string");
 
         CachedUserData userData = new CachedUserData(attributes);
         /* Create new SSO ticket with null-value servicePrincipal */
         final Long expiryTime = new Long(((Long) ticketTTLs.get(MoriaTicketType.SSO_TICKET)).longValue() + new Date().getTime());
         MoriaTicket ssoTicket = new MoriaTicket(MoriaTicketType.SSO_TICKET, null, expiryTime, userData, userorg);
         insertIntoStore(ssoTicket);
 
         return ssoTicket.getTicketId();
     }
 
 
     /**
      * Returns the userdata associated with the incoming ticket, which must be either a
      * proxy ticket, an SSO ticket or ticket granting ticket.
      *
      * @param ticketId
      *          A ticket to identify a userdata object (SSO, TGT or PROXY).
      * @param servicePrincipal
      *          The name of the service requesting the data,
      * @return A clone of the object containing the userdata.
      * @throws InvalidTicketException
      *          If the incoming ticket is not of the correct type or
      *          has an invalid principal.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If ticketId is null or zero length, or SSO ticket principal
      *          is null or zero length.
      * @see no.feide.moria.store.MoriaStore#getUserData(java.lang.String,
      *      java.lang.String)
      */
     public CachedUserData getUserData(final String ticketId, final String servicePrincipal)
     throws NonExistentTicketException, InvalidTicketException,
     MoriaStoreException {
 
         /* Validate argument. */
         if (ticketId == null || ticketId.equals("")) { throw new IllegalArgumentException("loginTicketId must be a non-empty string."); }
 
         MoriaTicketType[] potentialTicketTypes = new MoriaTicketType[] {
             MoriaTicketType.SSO_TICKET,
             MoriaTicketType.TICKET_GRANTING_TICKET,
             MoriaTicketType.PROXY_TICKET
         };
 
         MoriaTicket ticket = getFromStore(potentialTicketTypes, ticketId);
 
         if (ticket == null) { throw new NonExistentTicketException(ticketId); }
 
         if (!ticket.getTicketType().equals(MoriaTicketType.SSO_TICKET)) {
             if (servicePrincipal == null || servicePrincipal.equals("")) {
 throw new IllegalArgumentException("servicePrincipal must be a non-empty string for this ticket type.");
             }
         }
 
         validateTicket(ticket, potentialTicketTypes, servicePrincipal);
 
         CachedUserData cachedUserData = null;
 
         MoriaStoreData data = ticket.getData();
 
         if (data != null && data instanceof CachedUserData) {
             cachedUserData = (CachedUserData) data;
         } else {
             throw new InvalidTicketException("No user data associated with ticket. [" + ticketId + "]");
         }
 
         removeFromStore(ticket);
         return cachedUserData;
     }
 
 
     /**
      * Creates a service ticket that the service will use when requesting user attributes after a
      * successful authentication.
      *
      * @param loginTicketId
      *          A login ticket associated with an authentication attempt.
      * @return A service ticket associated with the authentication attempt object.
      * @throws InvalidTicketException
      *          If the supplied ticket is not a login ticket.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If loginTicketId is null or zero length.
      * @see no.feide.moria.store.MoriaStore#createServiceTicket(java.lang.String)
      */
     public String createServiceTicket(final String loginTicketId)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate argument. */
         if (loginTicketId == null || loginTicketId.equals("")) { throw new IllegalArgumentException("loginTicketId must be a non-empty string"); }
 
         MoriaTicket loginTicket = getFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);
 
         if (loginTicket == null) { throw new NonExistentTicketException(loginTicketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, null);
 
         /*
          * Create new service ticket and associate it with the same
          * authentication attempt as the login ticket.
          */
         MoriaAuthnAttempt authnAttempt = null;
 
         MoriaStoreData data = loginTicket.getData();
 
         if (data != null && data instanceof MoriaAuthnAttempt) {
             authnAttempt = (MoriaAuthnAttempt) data;
         } else {
             throw new InvalidTicketException("No authentication attempt associated with login ticket. [" + loginTicketId + "]");
         }
 
         final Long expiryTime = new Long(((Long) ticketTTLs.get(MoriaTicketType.SERVICE_TICKET)).longValue() + new Date().getTime());
         MoriaTicket serviceTicket = new MoriaTicket(
                 MoriaTicketType.SERVICE_TICKET, loginTicket.getServicePrincipal(),
                  expiryTime, authnAttempt, loginTicket.getUserorg());
         insertIntoStore(serviceTicket);
         /* Delete the now used login ticket. */
         removeFromStore(loginTicket);
 
         return serviceTicket.getTicketId();
     }
 
 
     /**
      * Creates a new ticket granting ticket, using an sso ticket.
      *
      * @param ssoTicketId
      *          An sso ticket that is already associated with a cached userdata object.
      * @param targetServicePrincipal
      *          The id of the service that will use the TGT.
      * @return A ticket-granting ticket that the requesting service may use for later proxy
      *          authentication.
      * @throws InvalidTicketException
      *          If the argument ticket is not an SSO ticket or has an invalid principal.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If any of the arguments are null or zero length.
      * @see no.feide.moria.store.MoriaStore#createTicketGrantingTicket(java.lang.String,
      *      java.lang.String)
      */
     public String createTicketGrantingTicket(final String ssoTicketId, final String targetServicePrincipal)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate arguments. */
         if (ssoTicketId == null || ssoTicketId.equals("")) { throw new IllegalArgumentException("ssoTicketId must be a non-empty string"); }
 
         if (targetServicePrincipal == null || targetServicePrincipal.equals("")) {
             throw new IllegalArgumentException("targetServicePrincipal must be a non-empty string");
         }
 
         MoriaTicket ssoTicket = getFromStore(MoriaTicketType.SSO_TICKET, ssoTicketId);
 
         if (ssoTicket == null) { throw new NonExistentTicketException(ssoTicketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(ssoTicket, MoriaTicketType.SSO_TICKET, null);
 
         /*
          * Create new ticket granting ticket and associate it with the same user
          * data as the SSO ticket.
          */
         CachedUserData cachedUserData = null;
 
         MoriaStoreData data = ssoTicket.getData();
 
         if (data != null && data instanceof CachedUserData) {
             cachedUserData = (CachedUserData) data;
         } else {
             throw new InvalidTicketException("No user data associated with SSO ticket. [" + ssoTicketId + "]");
         }
 
         final Long expiryTime = new Long(((Long) ticketTTLs.get(MoriaTicketType.TICKET_GRANTING_TICKET)).longValue() + new Date().getTime());
         MoriaTicket tgTicket = new MoriaTicket(
                 MoriaTicketType.TICKET_GRANTING_TICKET, targetServicePrincipal,
                 expiryTime, cachedUserData, ssoTicket.getUserorg());
         insertIntoStore(tgTicket);
 
         // Set TGT in cache to the newly created TGT id
         HashMap map = cachedUserData.getAttributes();
         if (map.containsKey("tgt")) {
             // try to cache the TGT
             removeFromStore(ssoTicket);
             cachedUserData.addAttribute("tgt", tgTicket.getTicketId());
             insertIntoStore(ssoTicket);
         }
         return tgTicket.getTicketId();
     }
 
 
     /**
      * Creates a new proxy ticket from a TGT and associates the new ticket with the same user data as
      * the TGT.
      *
      * @param tgTicketId
      *          A TGT issued earlier to a service.
      * @param servicePrincipal
      *          The id of the service making the request.
      * @param targetServicePrincipal
      *          The id of the service that will use the proxy ticket.
      * @return Proxy ticket that may be used by the requesting service.
      * @throws InvalidTicketException
      *          If the incoming ticket is not a TGT or has an invalid principal.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If any of the arguments are null or zero length.
      * @see no.feide.moria.store.MoriaStore#createProxyTicket(java.lang.String,
      *      java.lang.String, java.lang.String)
      */
     public String createProxyTicket(final String tgTicketId, final String servicePrincipal, final String targetServicePrincipal)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate arguments. */
         if (tgTicketId == null || tgTicketId.equals("")) { throw new IllegalArgumentException("tgTicketId must be a non-empty string."); }
 
         if (servicePrincipal == null || servicePrincipal.equals("")) {
             throw new IllegalArgumentException("servicePrincipal must be a non-empty string.");
         }
 
         if (targetServicePrincipal == null || targetServicePrincipal.equals("")) {
             throw new IllegalArgumentException("targetServicePrincipal must be a non-empty string.");
         }
 
         MoriaTicket tgTicket = getFromStore(MoriaTicketType.TICKET_GRANTING_TICKET, tgTicketId);
 
         if (tgTicket == null) { throw new NonExistentTicketException(tgTicketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(tgTicket, MoriaTicketType.TICKET_GRANTING_TICKET, servicePrincipal);
 
         /*
          * Create new ticket granting ticket and associate it with the same user
          * data as the TG ticket.
          */
         CachedUserData cachedUserData = null;
 
         MoriaStoreData data = tgTicket.getData();
 
         if (data != null && data instanceof CachedUserData) {
             cachedUserData = (CachedUserData) data;
         } else {
             throw new InvalidTicketException("No user data associated with ticket granting ticket. [" + tgTicketId + "]");
         }
 
         final Long expiryTime = new Long(((Long) ticketTTLs.get(MoriaTicketType.PROXY_TICKET)).longValue() + new Date().getTime());
         MoriaTicket proxyTicket = new MoriaTicket(
                 MoriaTicketType.PROXY_TICKET, targetServicePrincipal,
                 expiryTime, cachedUserData, tgTicket.getUserorg());
         insertIntoStore(proxyTicket);
 
         return proxyTicket.getTicketId();
     }
 
 
     /**
      * Sets transient attributes stored with authentication attempt.
      *
      * @param loginTicketId
      *          Ticket that identifies the AuthnAttempt that the attributes will be
      *          associated with.
      * @param transientAttributes
      *          Attributes to store with the AuthnAttempt.
      * @throws InvalidTicketException
      *          If ticket is found invalid.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If loginTicketId is null or zero length, or transientAttributes is null.
      * @see no.feide.moria.store.MoriaStore#setTransientAttributes(java.lang.String,
      *      java.util.HashMap)
      */
     public void setTransientAttributes(final String loginTicketId, final HashMap transientAttributes)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate arguments. */
         if (loginTicketId == null || loginTicketId.equals("")) { throw new IllegalArgumentException("loginTicketId must be a non-empty string."); }
 
         if (transientAttributes == null) { throw new IllegalArgumentException("transientAttributes cannot be null."); }
 
         MoriaTicket loginTicket = getFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);
 
         if (loginTicket == null) { throw new NonExistentTicketException(loginTicketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, null);
 
         MoriaAuthnAttempt authnAttempt = null;
 
         MoriaStoreData data = loginTicket.getData();
 
         if (data != null && data instanceof MoriaAuthnAttempt) {
             authnAttempt = (MoriaAuthnAttempt) data;
         } else {
             throw new InvalidTicketException("No authentication attempt associated with login ticket. [" + loginTicketId + "]");
         }
 
         authnAttempt.setTransientAttributes(transientAttributes);
 
         /* Insert into cache again to trigger distributed update. */
         insertIntoStore(loginTicket);
     }
 
 
     /**
      * Sets transient attributes stored with authentication attempt,
      * copied from a cached user data object.
      *
      * @param loginTicketId
      *          Ticket that identifies the AuthnAttempt that the attributes will be
      *          associated with.
      * @param ssoTicketId
      *          Ticket associated with a set of cached user data.
      * @throws InvalidTicketException
      *          If either ticket is found invalid.
      * @throws NonExistentTicketException
      *          If either ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If either ticket id is null or zero length.
      * @see no.feide.moria.store.MoriaStore#setTransientAttributes(java.lang.String,
      *      java.lang.String)
      */
     public void setTransientAttributes(final String loginTicketId, final String ssoTicketId)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate arguments. */
         if (loginTicketId == null || loginTicketId.equals("")) { throw new IllegalArgumentException("loginTicketId must be a non-empty string."); }
 
         if (ssoTicketId == null || ssoTicketId.equals("")) { throw new IllegalArgumentException("ssoTicketId must be a non-empty string."); }
 
         MoriaTicket loginTicket = getFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);
 
         if (loginTicket == null) { throw new NonExistentTicketException(loginTicketId); }
 
         MoriaTicket ssoTicket = getFromStore(MoriaTicketType.SSO_TICKET, ssoTicketId);
 
         if (ssoTicket == null) { throw new NonExistentTicketException(ssoTicketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, null);
         validateTicket(ssoTicket, MoriaTicketType.SSO_TICKET, null);
 
         CachedUserData cachedUserData = null;
         MoriaAuthnAttempt authnAttempt = null;
 
         MoriaStoreData ssoData = ssoTicket.getData();
 
         if (ssoData != null && ssoData instanceof CachedUserData) {
             cachedUserData = (CachedUserData) ssoData;
         } else {
             throw new InvalidTicketException("No cached user data associated with sso ticket. [" + ssoTicketId + "]");
         }
 
         MoriaStoreData loginData = loginTicket.getData();
 
         if (loginData != null && loginData instanceof MoriaAuthnAttempt) {
             authnAttempt = (MoriaAuthnAttempt) loginData;
         } else {
             throw new InvalidTicketException("No authentication attempt associated with login ticket. [" + loginTicketId + "]");
         }
 
         /* Transfer cached userdata to login attempt. */
         authnAttempt.setTransientAttributes(cachedUserData.getAttributes());
 
         /* Insert into cache again to trigger distributed update. */
         insertIntoStore(loginTicket);
     }
 
 
     /**
      * Removes an SSO ticket from the store.
      *
      * @param ssoTicketId
      *          the ticketId of the ticket to remove
      * @throws NonExistentTicketException
      *          If ticket does not exist
      * @throws MoriaStoreException
      *          If the operation fails
      * @throws IllegalArgumentException
      *          If ssoTicketId is null or zero length
      * @see no.feide.moria.store.MoriaStore#removeSSOTicket(java.lang.String)
      */
     public void removeSSOTicket(final String ssoTicketId)
     throws NonExistentTicketException, MoriaStoreException {
 
         /* Validate parameter. */
         if (ssoTicketId == null || ssoTicketId.equals("")) { throw new IllegalArgumentException("ssoTicketId must be a non-empty string."); }
 
         MoriaTicket ssoTicket = getFromStore(MoriaTicketType.SSO_TICKET, ssoTicketId);
 
         if (ssoTicket != null) {
             removeFromStore(ssoTicket);
         } else {
             throw new NonExistentTicketException(ssoTicketId);
         }
     }
 
 
     /**
      * Returns the service principal for the ticket.
      *
      * @param ticketId The ticket id.
      * @param ticketType The ticket type.
      * @return Service principal.
      * @throws InvalidTicketException
      *          If the ticket is invalid.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If ticketId is null or zero length.
      * @see no.feide.moria.store.MoriaTicket#getServicePrincipal()
      */
     public String getTicketServicePrincipal(final String ticketId, final MoriaTicketType ticketType)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate parameter. */
         if (ticketId == null || ticketId.equals("")) { throw new IllegalArgumentException("ticketId must be non-empty string."); }
 
         MoriaTicket ticket = getFromStore(ticketType, ticketId);
 
         if (ticket == null) { throw new NonExistentTicketException(ticketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(ticket, ticketType, null);
 
         return ticket.getServicePrincipal();
     }
 
     /**
      * Sets the userorg of a ticket.
      *
      * @param ticketId The ticket id.
      * @param ticketType The ticket type.
      * @param userorg The userorg of the user creating the ticket.
      * @throws InvalidTicketException
      *          if the ticket is invalid.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If ticketId is null or zero length.
      *  @see no.feide.moria.store.MoriaStore#setTicketUserorg(String, MoriaTicketType, String)
      */
     public void setTicketUserorg(final String ticketId, final MoriaTicketType ticketType, final String userorg)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate parameter. */
         if (ticketId == null || ticketId.equals("")) { throw new IllegalArgumentException("ticketId must be non-empty string."); }
 
         MoriaTicket ticket = getFromStore(ticketType, ticketId);
 
         if (ticket == null) { throw new NonExistentTicketException(ticketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(ticket, ticketType, null);
 
         removeFromStore(ticket);
         ticket.setUserorg(userorg);
         insertIntoStore(ticket);
     }
 
     /**
      * Gets the userorg of a ticket.
      *
      * @param ticketId the ticket id.
      * @param ticketType the ticket type.
      * @return the organization of the user creating the ticket, or null if not set.
      * @throws InvalidTicketException
      *          If the ticket is invalid.
      * @throws NonExistentTicketException
      *          If ticket does not exist.
      * @throws MoriaStoreException
      *          If the operation fails.
      * @throws IllegalArgumentException
      *          If ticketId is null or zero length.
      * @see no.feide.moria.store.MoriaStore#getTicketUserorg(String, MoriaTicketType)
      */
     public String getTicketUserorg(final String ticketId, final MoriaTicketType ticketType)
     throws InvalidTicketException, NonExistentTicketException,
     MoriaStoreException {
 
         /* Validate parameter. */
         if (ticketId == null || ticketId.equals("")) { throw new IllegalArgumentException("ticketId must be non-empty string."); }
 
         MoriaTicket ticket = getFromStore(ticketType, ticketId);
 
         if (ticket == null) { throw new NonExistentTicketException(ticketId); }
 
         /* Primarily to check timestamp. */
         validateTicket(ticket, ticketType, null);
 
         return ticket.getUserorg();
     }
 
 
     /**
      * Checks validity of ticket against type and expiry time.
      * @param ticket
      *            Ticket to be checked.
      * @param ticketType
      *            The expected type of the ticket.
      * @param servicePrincipal
      *            The service expected to be associated with this ticket.
      * @throws IllegalArgumentException
      *             If ticket is null, or ticketType is null or zero length.
      * @throws InvalidTicketException
      *             If ticket is found invalid.
      */
     private void validateTicket(final MoriaTicket ticket, final MoriaTicketType ticketType, final String servicePrincipal)
     throws InvalidTicketException {
 
         validateTicket(ticket, new MoriaTicketType[] {ticketType}, servicePrincipal);
     }
 
 
     /**
      * Check validity of ticket against a set of types and expiry time.
      * @param ticket
      *            Ticket to be checked.
      * @param ticketTypes
      *            Array of valid types for the ticket.
      * @param servicePrincipal
      *            The service that is using the ticket. May be null if no
      *            service is available.
      * @throws IllegalArgumentException
      *             If ticket is null, or ticketType is null or zero length.
      * @throws InvalidTicketException
      *             If the ticket is found to be invalid.
      */
     private void validateTicket(final MoriaTicket ticket, final MoriaTicketType[] ticketTypes, final String servicePrincipal)
     throws InvalidTicketException {
 
         /* Validate arguments. */
         if (ticket == null) { throw new IllegalArgumentException("ticket cannot be null."); }
 
         if (ticketTypes == null || ticketTypes.length < 1) { throw new IllegalArgumentException("ticketTypes cannot be null or zero length."); }
 
         /*
          * Check if it still is valid. We let the dedicated vacuuming service
          * take care of removing it at later time, so we just throw an
          * exception.
          */
         if (ticket.hasExpired()) { throw new InvalidTicketException("Ticket has expired. [" + ticket.getTicketId() + "]"); }
 
         /* Authorize the caller. */
         if (servicePrincipal != null && !ticket.getServicePrincipal().equals(servicePrincipal)) {
             throw new InvalidTicketException("Illegal use of ticket by " + servicePrincipal + ". [" + ticket.getTicketId() + "]");
         }
 
         /* Loop through ticket types until valid type found. */
         boolean valid = false;
 
         for (int i = 0; i < ticketTypes.length; i++) {
             if (ticket.getTicketType().equals(ticketTypes[i])) {
                 valid = true;
                 break;
             }
         }
 
         /* Throw exception if all types were invalid. */
         if (!valid) { throw new InvalidTicketException("Ticket has wrong type: " + ticket.getTicketType() + ". [" + ticket.getTicketId() + "]"); }
     }
 
 
     /**
      * Retrieves a ticket instance which may be one of a number of types.
      * @param ticketTypes
      *            Array of potential ticket types for the ticket id.
      * @param ticketId
      *            Id of the ticket to be retrieved.
      * @return A ticket, or null if none found.
      * @throws IllegalArgumentException
      *             If the any of arguments are null value or zero length.
      * @throws MoriaStoreException
      *             If access to the store failed in some way.
      */
     MoriaTicket getFromStore(final MoriaTicketType[] ticketTypes, final String ticketId)
     throws MoriaStoreException {
 
         /* Validate parameters. */
         if (ticketTypes == null || ticketTypes.length < 1) { throw new IllegalArgumentException("ticketTypes cannot be null or zero length."); }
 
         if (ticketId == null || ticketId.equals("")) { throw new IllegalArgumentException("ticketId must be a non-empty string."); }
 
         MoriaTicket ticket = null;
 
         /* Itterate of type array. Break if ticket is returned. */
         for (int i = 0; i < ticketTypes.length; i++) {
             ticket = getFromStore(ticketTypes[i], ticketId);
             if (ticket != null)
                 break;
         }
 
         return ticket;
     }
 
 
     /**
      * Retrieves a ticket instance from the store.
      * @param ticketType
      *            The type of ticket.
      * @param ticketId
      *            The ID of the ticket.
      * @return The ticket, or null if none found.
      * @throws IllegalArgumentException
      *             If <code>ticketType</code> is <code>null</code>, or if
      *             <code>ticketId</code> is <code>null</code> or an empty
      *             string.
      * @throws MoriaStoreException
      *             If operations on the underlying <code>TreeCache</code>
      *             fail; acts as a wrapper.
      */
     MoriaTicket getFromStore(final MoriaTicketType ticketType, final String ticketId)
     throws MoriaStoreException {
 
         // Sanity checks.
         if (ticketType == null)
             throw new IllegalArgumentException("Ticket type cannot be null");
         if (ticketId == null || ticketId.equals(""))
             throw new IllegalArgumentException("Ticket ID must be a non-empty string");
 
         // The name of the TreeCache node to be retrieved.
         Fqn fqn = new Fqn(new Object[] {ticketType, ticketId});
 
         // Does the node exist at all?
         if (!store.exists(fqn))
             return null;
 
         // Look up the node.
         Node node = null;
         try {
             node = store.get(fqn);
         } catch (LockingException e) {
             throw new MoriaStoreException("Locking of store failed for ticket. [" + ticketId + "]", e);
         } catch (TimeoutException e) {
             throw new MoriaStoreException("Access to store timed out for ticket. [" + ticketId + "]", e);
         }
 
         // Sanity check.
         if (node == null) {
             log.logInfo(ticketType.toString() + " exists, but cannot be found", ticketId);
             return null;
         }
 
         // Return the node.
         return new MoriaTicket(ticketId, (MoriaTicketType) node.get(TICKET_TYPE_ATTRIBUTE),
                 (String) node.get(PRINCIPAL_ATTRIBUTE), (Long) node.get(TTL_ATTRIBUTE),
                 (MoriaStoreData) node.get(DATA_ATTRIBUTE), (String) node.get(USERORG_ATTRIBUTE));
 
     }
 
 
     /**
      * Inserts an authentication attempt or cached user data into the cache.
      * Either authnAttempt or cachedUserData must be null.
      * @param ticket
      *            The ticket to connect to the inserted object.
      * @throws IllegalArgumentException
      *             If ticket is null.
      * @throws MoriaStoreException
      *             If operations on the TreeCache fail.
      */
     private void insertIntoStore(final MoriaTicket ticket)
     throws MoriaStoreException {
 
         /* Validate parameters */
         if (ticket == null) { throw new IllegalArgumentException("ticket cannot be null."); }
 
         Fqn fqn = new Fqn(new Object[] {ticket.getTicketType(), ticket.getTicketId()});
 
         HashMap attributes = new HashMap();
         attributes.put(TICKET_TYPE_ATTRIBUTE, ticket.getTicketType());
         attributes.put(TTL_ATTRIBUTE, ticket.getExpiryTime());
         attributes.put(PRINCIPAL_ATTRIBUTE, ticket.getServicePrincipal());
         attributes.put(DATA_ATTRIBUTE, ticket.getData());
         attributes.put(USERORG_ATTRIBUTE, ticket.getUserorg());
 
         try {
             store.put(fqn, attributes);
         } catch (RuntimeException re) {
             throw re;
         } catch (Exception e) {
             throw new MoriaStoreException("Insertion into store failed. [" + ticket.getTicketId() + "]", e);
         }
     }
 
 
     /**
      * Removes a ticket, and possibly a connected userdata or authnAttempt from
      * the cache.
      * @param ticket
      *            The ticket to be removed.
      * @throws IllegalArgumentException
      *             If ticket is null.
      * @throws NonExistentTicketException
      *             If the ticket does not exist.
      * @throws MoriaStoreException
      *             If an exception is thrown when operating on the store.
      */
     private void removeFromStore(final MoriaTicket ticket)
     throws NonExistentTicketException, MoriaStoreException {
 
         /* Validate parameters. */
         if (ticket == null) { throw new IllegalArgumentException("ticket cannot be null."); }
 
         Fqn fqn = new Fqn(new Object[] {ticket.getTicketType(), ticket.getTicketId()});
 
         if (store.exists(fqn)) {
             try {
                 store.remove(fqn);
             } catch (RuntimeException re) {
                 throw re;
             } catch (Exception e) {
                 throw new MoriaStoreException("Removal from store failed. [" + ticket.getTicketId() + "]", e);
             }
         } else {
             throw new NonExistentTicketException();
         }
     }
 }
