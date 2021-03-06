 /**
  * Copyright (c) 2009 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package org.fedoraproject.candlepin.resource;
 
 import org.fedoraproject.candlepin.audit.Event;
 import org.fedoraproject.candlepin.audit.EventAdapter;
 import org.fedoraproject.candlepin.audit.EventFactory;
 import org.fedoraproject.candlepin.audit.EventSink;
 import org.fedoraproject.candlepin.auth.Access;
 import org.fedoraproject.candlepin.auth.Principal;
 import org.fedoraproject.candlepin.auth.SystemPrincipal;
 import org.fedoraproject.candlepin.auth.UserPrincipal;
 import org.fedoraproject.candlepin.controller.PoolManager;
 import org.fedoraproject.candlepin.exceptions.BadRequestException;
 import org.fedoraproject.candlepin.exceptions.CandlepinException;
 import org.fedoraproject.candlepin.exceptions.ForbiddenException;
 import org.fedoraproject.candlepin.exceptions.IseException;
 import org.fedoraproject.candlepin.exceptions.NotFoundException;
 import org.fedoraproject.candlepin.model.CertificateSerialDto;
 import org.fedoraproject.candlepin.model.Consumer;
 import org.fedoraproject.candlepin.model.ConsumerCurator;
 import org.fedoraproject.candlepin.model.ConsumerType;
 import org.fedoraproject.candlepin.model.ConsumerType.ConsumerTypeEnum;
 import org.fedoraproject.candlepin.model.ConsumerTypeCurator;
 import org.fedoraproject.candlepin.model.Entitlement;
 import org.fedoraproject.candlepin.model.EntitlementCertificate;
 import org.fedoraproject.candlepin.model.EntitlementCurator;
 import org.fedoraproject.candlepin.model.EventCurator;
 import org.fedoraproject.candlepin.model.IdentityCertificate;
 import org.fedoraproject.candlepin.model.Owner;
 import org.fedoraproject.candlepin.model.OwnerCurator;
 import org.fedoraproject.candlepin.model.Pool;
 import org.fedoraproject.candlepin.model.Product;
 import org.fedoraproject.candlepin.model.Subscription;
 import org.fedoraproject.candlepin.model.User;
 import org.fedoraproject.candlepin.policy.EntitlementRefusedException;
 import org.fedoraproject.candlepin.policy.js.consumer.ConsumerDeleteHelper;
 import org.fedoraproject.candlepin.policy.js.consumer.ConsumerRules;
 import org.fedoraproject.candlepin.service.EntitlementCertServiceAdapter;
 import org.fedoraproject.candlepin.service.IdentityCertServiceAdapter;
 import org.fedoraproject.candlepin.service.ProductServiceAdapter;
 import org.fedoraproject.candlepin.service.SubscriptionServiceAdapter;
 import org.fedoraproject.candlepin.service.UserServiceAdapter;
 import org.fedoraproject.candlepin.sync.ExportCreationException;
 import org.fedoraproject.candlepin.sync.Exporter;
 import org.fedoraproject.candlepin.util.Util;
 
 import com.google.inject.Inject;
 import com.wideplay.warp.persist.Transactional;
 
 import org.apache.log4j.Logger;
 import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.xnap.commons.i18n.I18n;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.fedoraproject.candlepin.auth.interceptor.SecurityHole;
 import org.fedoraproject.candlepin.auth.interceptor.Verify;
 
 /**
  * API Gateway for Consumers
  */
 @Path("/consumers")
 public class ConsumerResource {
     private static final Pattern CONSUMER_NAME_PATTERN = Pattern
         .compile("[\\#\\?\\'\\`\\!@{}()\\[\\]\\?&\\w-\\.]+");
 
     private static Logger log = Logger.getLogger(ConsumerResource.class);
     private ConsumerCurator consumerCurator;
     private ConsumerTypeCurator consumerTypeCurator;
     private ProductServiceAdapter productAdapter;
     private SubscriptionServiceAdapter subAdapter;
     private EntitlementCurator entitlementCurator;
     private IdentityCertServiceAdapter identityCertService;
     private EntitlementCertServiceAdapter entCertService;
     private UserServiceAdapter userService;
     private I18n i18n;
     private EventSink sink;
     private EventFactory eventFactory;
     private EventCurator eventCurator;
     private EventAdapter eventAdapter;
     private static final int FEED_LIMIT = 1000;
     private Exporter exporter;
     private PoolManager poolManager;
     private ConsumerRules consumerRules;
     private ConsumerDeleteHelper consumerDeleteHelper;
     private OwnerCurator ownerCurator;
 
     @Inject
     public ConsumerResource(ConsumerCurator consumerCurator,
         ConsumerTypeCurator consumerTypeCurator,
         ProductServiceAdapter productAdapter,
         SubscriptionServiceAdapter subAdapter,
         EntitlementCurator entitlementCurator,
         IdentityCertServiceAdapter identityCertService,
         EntitlementCertServiceAdapter entCertServiceAdapter, I18n i18n,
         EventSink sink, EventFactory eventFactory, EventCurator eventCurator,
         EventAdapter eventAdapter, UserServiceAdapter userService,
         Exporter exporter, PoolManager poolManager,
         ConsumerRules consumerRules, ConsumerDeleteHelper consumerDeleteHelper,
         OwnerCurator ownerCurator) {
 
         this.consumerCurator = consumerCurator;
         this.consumerTypeCurator = consumerTypeCurator;
         this.productAdapter = productAdapter;
         this.subAdapter = subAdapter;
         this.entitlementCurator = entitlementCurator;
         this.identityCertService = identityCertService;
         this.entCertService = entCertServiceAdapter;
         this.i18n = i18n;
         this.sink = sink;
         this.eventFactory = eventFactory;
         this.eventCurator = eventCurator;
         this.userService = userService;
         this.exporter = exporter;
         this.poolManager = poolManager;
         this.consumerRules = consumerRules;
         this.consumerDeleteHelper = consumerDeleteHelper;
         this.ownerCurator = ownerCurator;
         this.eventAdapter = eventAdapter;
     }
 
     /**
      * List available Consumers
      * 
      * @return list of available consumers.
      */
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Wrapped(element = "consumers")
     public List<Consumer> list(@QueryParam("username") String userName,
         @QueryParam("type") String typeLabel,
         @QueryParam("owner") String ownerKey) {
         ConsumerType type = null;
 
         if (typeLabel != null) {
             type = lookupConsumerType(typeLabel);
         }
 
         Owner owner = null;
         if (ownerKey != null) {
             owner = ownerCurator.lookupByKey(ownerKey);
 
             if (owner == null) {
                 throw new NotFoundException(i18n.tr(
                     "owner with key: {0} was not found.", ownerKey));
             }
         }
 
         // We don't look up the user and warn if it doesn't exist here to not
         // give away usernames
         return consumerCurator.listByUsernameAndType(userName, type, owner);
     }
 
     /**
      * Return the consumer identified by the given uuid.
      * 
      * @param uuid uuid of the consumer sought.
      * @return the consumer identified by the given uuid.
      */
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Path("{consumer_uuid}")
     public Consumer getConsumer(@PathParam("consumer_uuid") @Verify(Consumer.class) String uuid) {
         Consumer consumer = verifyAndLookupConsumer(uuid);
 
         if (consumer != null) {
             // enrich with subscription data
             consumer.setCanActivate(subAdapter
                 .canActivateSubscription(consumer));
         }
 
         return consumer;
     }
 
     /**
      * Create a Consumer.
      * 
      * NOTE: Opening this method up to everyone, as we have nothing we can reliably verify
      * in the method signature. Instead we have to figure out what owner this consumer is 
      * destined for (due to backward compatability with existing clients which do not 
      * specify an owner during registration), and then check the access to the specified 
      * owner in the method itself.
      * 
      * @param consumer Consumer metadata
      * @return newly created Consumer
      * @throws BadRequestException generic exception type for web services We
      *         are calling this "registerConsumer" in the api discussions
      */
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @SecurityHole
     public Consumer create(Consumer consumer, @Context Principal principal,
         @QueryParam("username") String userName, @QueryParam("owner") String ownerKey)
         throws BadRequestException {
         // API:registerConsumer
 
         if (!isConsumerNameValid(consumer.getName())) {
             throw new BadRequestException(
                 i18n.tr("System name cannot contain most special characters."));
         }
 
         if (consumer.getName().indexOf('#') == 0) {
             // this is a bouncycastle restriction
             throw new BadRequestException(
                 i18n.tr("System name cannot begin with # character"));
         }
 
         // If no owner was specified, try to assume based on which owners the principal
         // has admin rights for. If more than one, we have to error out.
         if (ownerKey == null) {
             // check for this cast?
             List<String> ownerKeys = ((UserPrincipal) principal).getOwnerKeys();
 
             if (ownerKeys.size() != 1) {
                 throw new BadRequestException(
                     i18n.tr("Must specify owner for new consumer."));
             }
 
             ownerKey = ownerKeys.get(0);
         }
 
         ConsumerType type = lookupConsumerType(consumer.getType().getLabel());
 
         User user = getCurrentUsername(principal);
         if (userName != null) {
             user = userService.findByLogin(userName);
         }
 
         setupOwners((UserPrincipal) principal);
 
         // TODO: Refactor out type specific checks?
         if (type.isType(ConsumerTypeEnum.PERSON) && user != null) {
             Consumer existing = consumerCurator.findByUser(user);
 
             if (existing != null &&
                 existing.getType().isType(ConsumerTypeEnum.PERSON)) {
                 // TODO: This is not the correct error code for this situation!
                 throw new BadRequestException(i18n.tr(
                     "User {0} has already registered a personal consumer",
                     user.getUsername()));
             }
             consumer.setName(user.getUsername());
         }
 
         Owner owner = ownerCurator.lookupByKey(ownerKey);
         if (owner == null) {
             throw new BadRequestException(i18n.tr("Owner {0} does not exist", ownerKey));
         }
         
         if (!principal.canAccess(owner, Access.ALL)) {
             throw new ForbiddenException(i18n.tr("User {0} cannot access owner {1}", 
                 principal.getPrincipalName(), owner.getKey()));
         }
 
         // When registering person consumers we need to be sure the username
         // has some association with the owner the consumer is destined for:
        if (!user.getOwners().contains(owner) && !user.isSuperAdmin()) {
             throw new ForbiddenException(i18n.tr("User {0} has no roles for owner {1}", 
                 user.getUsername(), owner.getKey()));
         }
 
 
         consumer.setUsername(user.getUsername());
         consumer.setOwner(owner);
         consumer.setType(type);
         consumer.setCanActivate(subAdapter.canActivateSubscription(consumer));
 
         if (log.isDebugEnabled()) {
             log.debug("Got consumerTypeLabel of: " + type.getLabel());
             log.debug("got metadata: ");
             log.debug(consumer.getFacts());
 
             for (String key : consumer.getFacts().keySet()) {
                 log.debug("   " + key + " = " + consumer.getFact(key));
             }
         }
 
         try {
             consumer = consumerCurator.create(consumer);
             IdentityCertificate idCert = generateIdCert(consumer, false);
             consumer.setIdCert(idCert);
 
             sink.emitConsumerCreated(consumer);
             return consumer;
         }
         catch (CandlepinException ce) {
             // If it is one of ours, rethrow it.
             throw ce;
         }
         catch (Exception e) {
             log.error("Problem creating consumer:", e);
             e.printStackTrace();
             throw new BadRequestException(i18n.tr(
                 "Problem creating consumer {0}", consumer));
         }
     }
 
     private boolean isConsumerNameValid(String name) {
         if (name == null) {
             return false;
         }
 
         return CONSUMER_NAME_PATTERN.matcher(name).matches();
     }
 
     /*
      * During registration of new consumers we support an edge case where the user
      * service may have authenticated a username/password for an owner which we have
      * not yet created in the Candlepin database. If we detect this during
      * registration we need to create the new owners, and adjust
      * the principal that was created during authentication to carry it.
      */
     // TODO:  Reevaluate if this is still an issue with the new membership scheme!
     private void setupOwners(UserPrincipal principal) {
 
         for (Owner owner : principal.getOwners()) {
             Owner existingOwner = ownerCurator.lookupByKey(owner.getKey());
 
             if (existingOwner == null) {
                 log.info("Principal carries permission for owner that does not exist.");
                 log.info("Creating new owner: " + owner.getKey());
 
                 // Need elevated privileges to create a new owner:
                 Principal systemPrincipal = new SystemPrincipal();
                 ResteasyProviderFactory.pushContext(Principal.class,
                     systemPrincipal);
 
                 existingOwner = ownerCurator.create(owner);
                 poolManager.refreshPools(existingOwner);
                 //p.setOwner(existingOwner);
 
                 ResteasyProviderFactory.popContextData(Principal.class);
 
                 // Restore the old principal having elevated privileges earlier:
                 ResteasyProviderFactory.pushContext(Principal.class, principal);
             }
 
         }
     }
 
     private ConsumerType lookupConsumerType(String label) {
         ConsumerType type = consumerTypeCurator.lookupByLabel(label);
 
         if (type == null) {
             throw new BadRequestException(i18n.tr("No such consumer type: {0}",
                 label));
         }
         return type;
     }
 
     private User getCurrentUsername(Principal principal) {
         if (principal instanceof UserPrincipal) {
             UserPrincipal user = (UserPrincipal) principal;
             return userService.findByLogin(user.getUsername());
         }
 
         return null;
     }
 
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     @Path("{consumer_uuid}")
     @Transactional
     public void updateConsumer(@PathParam("consumer_uuid") String uuid,
         Consumer consumer, @Context Principal principal) {
         Consumer toUpdate = verifyAndLookupConsumer(uuid);
 
         log.debug("Updating");
 
         if (!toUpdate.factsAreEqual(consumer)) {
             log.debug("Facts are not equal, updating them");
             Event event = eventFactory.consumerModified(toUpdate, consumer);
 
             // TODO: Just updating the facts for now
             toUpdate.setFacts(consumer.getFacts());
             sink.sendEvent(event);
         }
     }
 
     /**
      * delete the consumer.
      * 
      * @param uuid uuid of the consumer to delete.
      */
     @DELETE
     @Produces(MediaType.APPLICATION_JSON)
     @Path("{consumer_uuid}")
     @Transactional
     public void deleteConsumer(@PathParam("consumer_uuid") @Verify(Consumer.class) String uuid,
         @Context Principal principal) {
         log.debug("deleting  consumer_uuid" + uuid);
         Consumer toDelete = verifyAndLookupConsumer(uuid);
         try {
             this.poolManager.revokeAllEntitlements(toDelete);
         }
         catch (ForbiddenException e) {
             String msg = e.message().getDisplayMessage();
             throw new ForbiddenException(i18n.tr(
                 "Cannot unregister {0} consumer {1} because: {2}", toDelete
                     .getType().getLabel(), toDelete.getName(), msg), e);
 
         }
         consumerRules.onConsumerDelete(consumerDeleteHelper, toDelete);
 
         Event event = eventFactory.consumerDeleted(toDelete);
         consumerCurator.delete(toDelete);
         identityCertService.deleteIdentityCert(toDelete);
         sink.sendEvent(event);
     }
 
     /**
      * Return the entitlement certificate for the given consumer.
      * 
      * @param consumerUuid UUID of the consumer
      * @return list of the client certificates for the given consumer.
      */
     @GET
     @Path("{consumer_uuid}/certificates")
     @Produces(MediaType.APPLICATION_JSON)
     public List<EntitlementCertificate> getEntitlementCertificates(
         @PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid,
         @QueryParam("serials") String serials) {
 
         log.debug("Getting client certificates for consumer: " + consumerUuid);
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
 
         Set<Long> serialSet = new HashSet<Long>();
         if (serials != null) {
             log.debug("Requested serials: " + serials);
             for (String s : serials.split(",")) {
                 log.debug("   " + s);
                 serialSet.add(Long.valueOf(s));
             }
         }
 
         List<EntitlementCertificate> returnCerts = new LinkedList<EntitlementCertificate>();
         List<EntitlementCertificate> allCerts = entCertService
             .listForConsumer(consumer);
         for (EntitlementCertificate cert : allCerts) {
             if (serialSet.isEmpty() ||
                 serialSet.contains(cert.getSerial().getId())) {
                 returnCerts.add(cert);
             }
         }
         return returnCerts;
     }
 
     /**
      * Return the client certificate metadata for the given consumer. This is a
      * small subset of data clients can use to determine which certificates they
      * need to update/fetch.
      * 
      * @param consumerUuid UUID of the consumer
      * @return list of the client certificate metadata for the given consumer.
      */
     @GET
     @Path("{consumer_uuid}/certificates/serials")
     @Produces(MediaType.APPLICATION_JSON)
     @Wrapped(element = "serials")
     public List<CertificateSerialDto> getEntitlementCertificateSerials(
         @PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid) {
 
         log.debug("Getting client certificate serials for consumer: " +
             consumerUuid);
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
 
         List<CertificateSerialDto> allCerts = new LinkedList<CertificateSerialDto>();
         for (EntitlementCertificate cert : entCertService
             .listForConsumer(consumer)) {
             allCerts.add(new CertificateSerialDto(cert.getSerial().getId()));
         }
 
         return allCerts;
     }
 
     /**
      * Entitles the given Consumer to the given Product. Will seek out pools
      * which provide access to this product, either directly or as a child, and
      * select the best one based on a call to the rules engine.
      * 
      * @param productId Product ID.
      * @return Entitlement object.
      */
     // TODO: Bleh, very duplicated methods here:
     private List<Entitlement> bindByProducts(String[] productIds,
         Consumer consumer, Integer quantity) {
         // Attempt to create entitlements:
         try {
             List<Entitlement> entitlements = poolManager.entitleByProducts(
                 consumer, productIds, quantity);
             log.debug("Created entitlements: " + entitlements);
             return entitlements;
         }
         catch (EntitlementRefusedException e) {
             // Could be multiple errors, but we'll just report the first one for
             // now:
             // TODO: Convert resource key to user friendly string?
             // See below for more TODOS
             String productId = "XXX FIXME";
             String msg;
             String error = e.getResult().getErrors().get(0).getResourceKey();
             if (error.equals("rulefailed.consumer.already.has.product")) {
                 msg = i18n
                     .tr("This consumer is already subscribed to the product ''{0}''",
                         productId);
             }
             else if (error.equals("rulefailed.no.entitlements.available")) {
                 msg = i18n
                     .tr("No free entitlements are available for the product ''{0}''",
                         productId);
             }
             else if (error.equals("rulefailed.consumer.type.mismatch")) {
                 msg = i18n
                     .tr("Consumers of this type are not allowed to the product ''{0}''",
                         productId);
             }
             else if (error.equals("rulefailed.virt.only")) {
                 msg = i18n.tr(
                     "Only virtual systems can consume the product ''{0}''",
                     productId);
             }
             else {
                 msg = i18n.tr(
                     "Unable to entitle consumer to the product ''{0}'': {1}",
                     productId, error);
             }
             throw new ForbiddenException(msg);
         }
     }
 
     private Entitlement createEntitlementByPool(Consumer consumer, Pool pool,
         Integer quantity) {
         // Attempt to create an entitlement:
         try {
             Entitlement e = poolManager.entitleByPool(consumer, pool, quantity);
             log.debug("Created entitlement: " + e);
             return e;
         }
         catch (EntitlementRefusedException e) {
             // Could be multiple errors, but we'll just report the first one for
             // now:
             // TODO: multiple checks here for the errors will get ugly, but the
             // returned
             // string is dependent on the caller (ie pool vs product)
             String msg;
             String error = e.getResult().getErrors().get(0).getResourceKey();
             if (error.equals("rulefailed.consumer.already.has.product")) {
                 msg = i18n.tr(
                     "This consumer is already subscribed to the product matching pool " +
                         "with id ''{0}''", pool.getId().toString());
             }
             else if (error.equals("rulefailed.no.entitlements.available")) {
                 msg = i18n
                     .tr("No free entitlements are available for the pool with id ''{0}''",
                         pool.getId().toString());
             }
             else if (error.equals("rulefailed.consumer.type.mismatch")) {
                 msg = i18n.tr(
                     "Consumers of this type are not allowed to subscribe to the pool " +
                         "with id ''{0}''", pool.getId().toString());
             }
             else {
                 msg = i18n
                     .tr("Unable to entitle consumer to the pool with id ''{0}'': {1}",
                         pool.getId().toString(), error);
             }
             throw new ForbiddenException(msg);
         }
     }
 
     /**
      * Grants entitlements based on a registration token.
      *
      * @param registrationToken registration token.
      * @param consumer Consumer to bind
      * @return token
      */
     private List<Entitlement> bindByToken(String registrationToken,
         Consumer consumer, Integer quantity, String email, String emailLocale) {
 
         List<Subscription> subs = subAdapter.getSubscriptionForToken(
             consumer.getOwner(), registrationToken, email, emailLocale);
         if ((subs == null) || (subs.isEmpty())) {
             log.debug("token: " + registrationToken);
             throw new BadRequestException(i18n.tr("No such token: {0}",
                 registrationToken));
         }
 
         List<Entitlement> entitlementList = new LinkedList<Entitlement>();
         for (Subscription sub : subs) {
 
             // Make sure we have created/updated a pool for this subscription:
             Pool pool = poolManager.lookupBySubscriptionId(sub.getId());
             if (pool == null) {
                 // WARNING: Assumption here that a bind by token subscription
                 // will only link up to one pool, or at least that we'll try to
                 // bind
                 // to the *first* one it created:
                 pool = poolManager.createPoolsForSubscription(sub).get(0);
             }
             else {
                 poolManager.updatePoolForSubscription(pool, sub);
             }
 
             entitlementList.add(createEntitlementByPool(consumer, pool,
                 quantity));
         }
         return entitlementList;
     }
 
     private List<Entitlement> bindByPool(String poolId, Consumer consumer,
         Integer quantity) {
         Pool pool = poolManager.find(poolId);
         List<Entitlement> entitlementList = new LinkedList<Entitlement>();
 
         if (log.isDebugEnabled() && pool != null) {
             log.debug("pool: id[" + pool.getId() + "], consumed[" +
                 pool.getConsumed() + "], qty [" + pool.getQuantity() + "]");
         }
 
         if (pool == null) {
             throw new BadRequestException(i18n.tr(
                 "No such entitlement pool: {0}", poolId));
         }
 
         // Attempt to create an entitlement:
         entitlementList.add(createEntitlementByPool(consumer, pool, quantity));
         return entitlementList;
     }
 
     /**
      * Request an entitlement.
      * 
      * @param consumerUuid Consumer identifier to be entitled
      * @param poolIdString Entitlement pool id.
      * @param email TODO
      * @param emailLocale TODO
      * @return Entitlement.
      */
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Path("/{consumer_uuid}/entitlements")
     public List<Entitlement> bind(
         @PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid,
         @QueryParam("pool") String poolIdString,
         @QueryParam("token") String token,
         @QueryParam("product") String[] productIds,
         @QueryParam("quantity") @DefaultValue("1") Integer quantity,
         @QueryParam("email") String email,
         @QueryParam("email_locale") String emailLocale) {
 
         // Check that only one query param was set:
         if ((poolIdString != null && token != null) ||
             (poolIdString != null && productIds != null && productIds.length > 0) ||
             (token != null && productIds != null && productIds.length > 0)) {
             throw new BadRequestException(
                 i18n.tr("Cannot bind by multiple parameters."));
         }
 
         // Verify consumer exists:
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
         List<Entitlement> entitlements = null;
         try {
             if (!subAdapter.hasUnacceptedSubscriptionTerms(consumer.getOwner())) {
 
                 if (token != null) {
                     entitlements = bindByToken(token, consumer, quantity,
                         email, emailLocale);
                 }
                 else if (productIds != null && productIds.length > 0) {
                     entitlements = bindByProducts(productIds, consumer,
                         quantity);
                 }
                 else {
                     String poolId = Util.assertNotNull(poolIdString,
                         i18n.tr("Pool ID must be provided"));
                     entitlements = bindByPool(poolId, consumer, quantity);
                 }
             }
         }
 
         catch (CandlepinException e) {
             log.debug(e.getMessage());
             throw e;
         }
 
         // Trigger events:
         for (Entitlement e : entitlements) {
             Event event = eventFactory.entitlementCreated(e);
             sink.sendEvent(event);
         }
 
         return entitlements;
     }
 
     private Consumer verifyAndLookupConsumer(String consumerUuid) {
         Consumer consumer = consumerCurator.findByUuid(consumerUuid);
 
         if (consumer == null) {
             throw new NotFoundException(i18n.tr("No such consumer: {0}",
                 consumerUuid));
         }
         return consumer;
     }
 
     private Entitlement verifyAndLookupEntitlement(String entitlementId) {
         Entitlement entitlement = entitlementCurator.find(entitlementId);
 
         if (entitlement == null) {
             throw new NotFoundException(i18n.tr("No such entitlement: {0}",
                 entitlementId));
         }
         return entitlement;
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Path("/{consumer_uuid}/entitlements")
     public List<Entitlement> listEntitlements(
         @PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid,
         @QueryParam("product") String productId) {
 
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
         if (productId != null) {
             Product p = productAdapter.getProductById(productId);
             if (p == null) {
                 throw new BadRequestException(i18n.tr("No such product: {0}",
                     productId));
             }
             return entitlementCurator.listByConsumerAndProduct(consumer,
                 productId);
         }
 
         return entitlementCurator.listByConsumer(consumer);
 
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Path("/{consumer_uuid}/owner")
     public Owner getOwner(@PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid) {
 
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
         return consumer.getOwner();
     }
 
     /**
      * Unbind all entitlements.
      * 
      * @param consumerUuid Unique id for the Consumer.
      */
     @DELETE
     @Path("/{consumer_uuid}/entitlements")
     public void unbindAll(@PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid) {
 
         // FIXME: just a stub, needs CertifcateService (and/or a
         // CertificateCurator) to lookup by serialNumber
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
 
         if (consumer == null) {
             throw new NotFoundException(i18n.tr("Consumer with ID " +
                 consumerUuid + " could not be found."));
         }
 
         poolManager.revokeAllEntitlements(consumer);
 
         // Need to parse off the value of subscriptionNumberArgs, probably
         // use comma separated see IntergerList in sparklines example in
         // jersey examples find all entitlements for this consumer and
         // subscription numbers delete all of those (and/or return them to
         // entitlement pool)
 
     }
 
     /**
      * Remove an entitlement by ID.
      * 
      * @param dbid the entitlement to delete.
      */
     @DELETE
     @Path("/{consumer_uuid}/entitlements/{dbid}")
     public void unbind(@PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid,
         @PathParam("dbid") String dbid, @Context Principal principal) {
 
         verifyAndLookupConsumer(consumerUuid);
 
         Entitlement toDelete = entitlementCurator.find(dbid);
         if (toDelete != null) {
             poolManager.revokeEntitlement(toDelete);
             return;
         }
 
         throw new NotFoundException(i18n.tr(
             "Entitlement with ID '{0}' could not be found.", dbid));
     }
 
     @DELETE
     @Path("/{consumer_uuid}/certificates/{serial}")
     public void unbindBySerial(@PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid,
         @PathParam("serial") Long serial) {
 
         verifyAndLookupConsumer(consumerUuid);
         Entitlement toDelete = entitlementCurator
             .findByCertificateSerial(serial);
 
         if (toDelete != null) {
             poolManager.revokeEntitlement(toDelete);
             return;
         }
         throw new NotFoundException(
             i18n.tr(
                 "Entitlement Certificate with serial number {0} could not be found.",
                 serial.toString())); // prevent serial number formatting.
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Path("{consumer_uuid}/events")
     public List<Event> getConsumerEvents(
         @PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid) {
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
         List<Event> events = this.eventCurator.listMostRecent(FEED_LIMIT,
             consumer);
         if (events != null) {
             eventAdapter.addMessageText(events);
         }
         return events;
     }
 
     @PUT
     @Path("/{consumer_uuid}/certificates")
     public void regenerateEntitlementCertificates(
         @PathParam("consumer_uuid") @Verify(Consumer.class) String consumerUuid,
         @QueryParam("entitlement") String entitlementId) {
         if (entitlementId != null) {
             Entitlement e = verifyAndLookupEntitlement(entitlementId);
             poolManager.regenerateCertificatesOf(e);
         }
         else {
             Consumer c = verifyAndLookupConsumer(consumerUuid);
             poolManager.regenerateEntitlementCertificates(c);
         }
     }
 
     @GET
     @Produces("application/zip")
     @Path("{consumer_uuid}/export")
     public File exportData(@Context HttpServletResponse response,
         @PathParam("consumer_uuid") String consumerUuid) {
 
         Consumer consumer = verifyAndLookupConsumer(consumerUuid);
         if (!consumer.getType().isType(ConsumerTypeEnum.CANDLEPIN)) {
             throw new ForbiddenException(
                 i18n.tr(
                     "Consumer {0} cannot be exported, as it's of wrong consumer type.",
                     consumerUuid));
         }
 
         File archive;
         try {
             archive = exporter.getExport(consumer);
             response.addHeader("Content-Disposition", "attachment; filename=" +
                 archive.getName());
 
             sink.sendEvent(eventFactory.exportCreated(consumer));
             return archive;
         }
         catch (ExportCreationException e) {
             throw new IseException(i18n.tr("Unable to create export archive"),
                 e);
         }
     }
 
     /**
      * Return the consumer identified by the given uuid.
      *
      * @param uuid uuid of the consumer sought.
      * @return the consumer identified by the given uuid.
      */
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Path("{consumer_uuid}")
     public Consumer regenerateIdentityCertificates(
         @PathParam("consumer_uuid") @Verify(Consumer.class) String uuid) {
 
         Consumer c = verifyAndLookupConsumer(uuid);
 
         try {
             IdentityCertificate ic = generateIdCert(c, true);
             c.setIdCert(ic);
             consumerCurator.update(c);
             Event consumerModified = this.eventFactory.consumerModified(c);
             this.sink.sendEvent(consumerModified);
             return c;
         }
         catch (Exception e) {
             log.error("Problem regenerating id cert for consumer:", e);
             throw new BadRequestException(i18n.tr(
                 "Problem regenerating id cert for consumer {0}", c));
         }
     }
 
     /**
      * Generates the identity certificate for the given consumer and user.
      * Throws RuntimeException if there is a problem with generating the
      * certificate.
      *
      * @param c Consumer whose certificate needs to be generated.
      * @param regen if true, forces a regen of the certificate.
      * @return The identity certificate for the given consumer.
      * @throws IOException thrown if there's a problem generating the cert.
      * @throws GeneralSecurityException thrown incase of security error.
      */
     private IdentityCertificate generateIdCert(Consumer c, boolean regen)
         throws GeneralSecurityException, IOException {
 
         IdentityCertificate idCert = null;
 
         if (regen) {
             idCert = identityCertService.regenerateIdentityCert(c);
         }
         else {
             idCert = identityCertService.generateIdentityCert(c);
         }
 
         if (log.isDebugEnabled()) {
             log.debug("Generated identity cert: " + idCert);
             log.debug("Created consumer: " + c);
         }
 
         if (idCert == null) {
             throw new RuntimeException("Error generating identity certificate.");
         }
 
         return idCert;
     }
 
 }
