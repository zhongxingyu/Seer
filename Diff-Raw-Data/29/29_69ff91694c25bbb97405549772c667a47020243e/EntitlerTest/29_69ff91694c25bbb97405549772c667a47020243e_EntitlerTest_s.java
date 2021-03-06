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
 package org.fedoraproject.candlepin.controller.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.fedoraproject.candlepin.auth.Principal;
 import org.fedoraproject.candlepin.auth.Role;
 import org.fedoraproject.candlepin.auth.UserPrincipal;
 import org.fedoraproject.candlepin.controller.Entitler;
 import org.fedoraproject.candlepin.model.Attribute;
 import org.fedoraproject.candlepin.model.Consumer;
 import org.fedoraproject.candlepin.model.ConsumerType;
 import org.fedoraproject.candlepin.model.Entitlement;
 import org.fedoraproject.candlepin.model.Owner;
 import org.fedoraproject.candlepin.model.Pool;
 import org.fedoraproject.candlepin.model.Product;
 import org.fedoraproject.candlepin.policy.Enforcer;
 import org.fedoraproject.candlepin.policy.EntitlementRefusedException;
 import org.fedoraproject.candlepin.policy.js.JavascriptEnforcer;
 import org.fedoraproject.candlepin.test.DatabaseTestFixture;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 
 public class EntitlerTest extends DatabaseTestFixture {
     
     public static final String PRODUCT_MONITORING = "monitoring";
     public static final String PRODUCT_PROVISIONING = "provisioning";
     public static final String PRODUCT_VIRT_HOST = "virtualization_host";
     public static final String PRODUCT_VIRT_HOST_PLATFORM = "virtualization_host_platform";
     public static final String PRODUCT_VIRT_GUEST = "virt_guest";
     
     private Product virtHost;
     private Product virtHostPlatform;
     private Product virtGuest;
     private Product monitoring;
     private Product provisioning;
     
     private ConsumerType guestType;
     
     private Owner o;
     private Consumer parentSystem;
     private Consumer childVirtSystem;
     private Entitler entitler;
     private Principal principal;
 
     @Before
     public void setUp() throws Exception {
         o = createOwner();
         ownerCurator.create(o);
         
         //String certString = SpacewalkCertificateCuratorTest.readCertificate(
         //        "/certs/spacewalk-with-channel-families.cert");
         //spacewalkCertCurator.parseCertificate(CertificateFactory.read(certString), o);
 
         //List<Pool> pools = poolCurator.listByOwner(o);
         //assertTrue(pools.size() > 0);
         principal = injector.getInstance(Principal.class);
 
         virtHost = new Product(PRODUCT_VIRT_HOST, PRODUCT_VIRT_HOST);
         virtHostPlatform = new Product(PRODUCT_VIRT_HOST_PLATFORM, 
             PRODUCT_VIRT_HOST_PLATFORM);
         virtGuest = new Product(PRODUCT_VIRT_GUEST, PRODUCT_VIRT_GUEST);
         monitoring = new Product(PRODUCT_MONITORING, PRODUCT_MONITORING);
         provisioning = new Product(PRODUCT_PROVISIONING, PRODUCT_PROVISIONING);        
         
         virtHost.addAttribute(new Attribute(PRODUCT_VIRT_HOST, ""));
         virtHostPlatform.addAttribute(new Attribute(PRODUCT_VIRT_HOST_PLATFORM, ""));
         virtGuest.addAttribute(new Attribute(PRODUCT_VIRT_GUEST, ""));
         monitoring.addAttribute(new Attribute(PRODUCT_MONITORING, ""));
         provisioning.addAttribute(new Attribute(PRODUCT_PROVISIONING, ""));
         
         productAdapter.createProduct(virtHost);
         productAdapter.createProduct(virtHostPlatform);
         productAdapter.createProduct(virtGuest);
         productAdapter.createProduct(monitoring);
         productAdapter.createProduct(provisioning);
         
        //subCurator.create(new Subscription(o, virtHost.getId(), 500, new Date(), ))
         
         
         entitler = injector.getInstance(Entitler.class);
 
         ConsumerType system = new ConsumerType(ConsumerType.SYSTEM);
         consumerTypeCurator.create(system);
         
         guestType = new ConsumerType(ConsumerType.VIRT_SYSTEM);
         consumerTypeCurator.create(guestType);
         
         parentSystem = new Consumer("system", o, system);
         parentSystem.getFacts().put("total_guests", "0");
         consumerCurator.create(parentSystem);
         
         childVirtSystem = new Consumer("virt system", o, guestType);
         parentSystem.addChildConsumer(childVirtSystem);
         
         consumerCurator.create(childVirtSystem);
     }
    
    @Test
    public void testGuestTypeCreated() {
        // This guest product type should have been created just by parsing a sat cert
        // with virt entitlements:
        assertNotNull(virtGuest);
    }
 
     @Test
     public void testEntitlementPoolsCreated() {
         List<Pool> pools = poolCurator.listByOwner(o);
         assertTrue(pools.size() > 0);
 
         Pool virtHostPool = poolCurator.listByOwnerAndProduct(o, virtHost).get(0);
         assertNotNull(virtHostPool);
     }
 
     @Test(expected = EntitlementRefusedException.class)
     public void testVirtEntitleFailsIfAlreadyHasGuests() 
         throws EntitlementRefusedException {
         
         parentSystem.getFacts().put("total_guests", "10");
         consumerCurator.update(parentSystem);
         entitler.entitle(parentSystem, virtHost, 
             new UserPrincipal("user", o, new LinkedList<Role>()));
     }
 
     @Test(expected = EntitlementRefusedException.class)
     public void testVirtHostEntitleFailsIfAlreadyHasGuests() 
         throws EntitlementRefusedException {
         
         parentSystem.getFacts().put("total_guests", "10");
         consumerCurator.update(parentSystem);
         entitler.entitle(parentSystem, virtHostPlatform,
             new UserPrincipal("user", o, new LinkedList<Role>()));
     }
     
     @Test(expected = EntitlementRefusedException.class)
     public void testVirtEntitleFailsForVirtSystem() throws Exception {
         parentSystem.setType(guestType);
         consumerCurator.update(parentSystem);
         entitler.entitle(parentSystem, virtHost,
             new UserPrincipal("user", o, new LinkedList<Role>()));
     }
     
     @Test(expected = EntitlementRefusedException.class)
     public void testVirtHostEntitleFailsForVirtSystem() throws Exception {
         parentSystem.setType(guestType);
         consumerCurator.update(parentSystem);
         entitler.entitle(parentSystem, virtHostPlatform,
             new UserPrincipal("user", o, new LinkedList<Role>()));
     }
     
     @Test
     public void testVirtSystemGetsWhatParentHasForFree() throws Exception {
         // Give parent virt host ent:
         Entitlement e = entitler.entitle(parentSystem, virtHost,
             new UserPrincipal("user", o, new LinkedList<Role>()));
         assertNotNull(e);
         
         // Give parent provisioning:
         e = entitler.entitle(parentSystem, provisioning,
             new UserPrincipal("user", o, new LinkedList<Role>()));
         assertNotNull(e);
         
         Pool provisioningPool = poolCurator.listByOwnerAndProduct(o, 
                 provisioning).get(0);
         
         Long provisioningCount = new Long(provisioningPool.getConsumed());
         assertEquals(new Long(1), provisioningCount);
         
         // Now guest requests monitoring, and should get it for "free":
         e = entitler.entitle(childVirtSystem, provisioning,
             new UserPrincipal("user", o, new LinkedList<Role>()));
         assertNotNull(e);
         assertTrue(e.isFree());
         assertEquals(new Long(1), provisioningPool.getConsumed());
     }
     
     @Test
     public void testVirtSystemPhysicalEntitlement() throws Exception {
         // Give parent virt host ent:
         Entitlement e = entitler.entitle(parentSystem, virtHost,
             new UserPrincipal("user", o, new LinkedList<Role>()));
         assertNotNull(e);
         
         Pool provisioningPool = poolCurator.listByOwnerAndProduct(o, 
                 provisioning).get(0);
         
         Long provisioningCount = new Long(provisioningPool.getConsumed());
         assertEquals(new Long(0), provisioningCount);
         
         e = entitler.entitle(childVirtSystem, provisioning,
             new UserPrincipal("user", o, new LinkedList<Role>()));
         assertNotNull(e);
         assertFalse(e.isFree());
         // Should have resorted to consuming a physical entitlement, because the guest's
         // parent does not have this.
         assertEquals(new Long(1), provisioningPool.getConsumed());
     }
     
     @Test
     public void testQuantityCheck() throws Exception {
         Pool monitoringPool = poolCurator.listByOwnerAndProduct(o, 
                 monitoring).get(0);
         assertEquals(new Long(5), monitoringPool.getQuantity());
         for (int i = 0; i < 5; i++) {
             Entitlement e = entitler.entitle(parentSystem, monitoring,
                 new UserPrincipal("user", o, new LinkedList<Role>()));
             assertNotNull(e);
         }
         
         // The cert should specify 5 monitoring entitlements, taking a 6th should fail:
         try {
             entitler.entitle(parentSystem, monitoring,
                 new UserPrincipal("user", o, new LinkedList<Role>()));
             fail();
         }
         catch (EntitlementRefusedException e) {
             //expected
         }
         assertEquals(new Long(5), monitoringPool.getConsumed());
     }
 
     @Test
     public void testRevocation() throws Exception {
         Entitlement e = entitler.entitle(parentSystem, monitoring,
             new UserPrincipal("user", o, new LinkedList<Role>()));
         entitler.revokeEntitlement(e, principal);
 
         List<Entitlement> entitlements = entitlementCurator.listByConsumer(parentSystem);
         assertTrue(entitlements.isEmpty());
     }
     
     @Override
     protected Module getGuiceOverrideModule() {
         return new AbstractModule() {
             
             @Override
             protected void configure() {
                 bind(Enforcer.class).to(JavascriptEnforcer.class);
             }
         };
     }
 }
