 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Aye Thu
  */
 
 package com.netspective.medigy.model.org;
 
 import com.netspective.medigy.model.TestCase;
 import com.netspective.medigy.model.common.GeographicBoundary;
 import com.netspective.medigy.model.party.PartyRelationship;
 import com.netspective.medigy.model.party.PartyRole;
 import com.netspective.medigy.model.party.PostalAddress;
 import com.netspective.medigy.model.person.TestPerson;
 import com.netspective.medigy.model.session.ProcessSession;
 import com.netspective.medigy.model.session.Session;
 import com.netspective.medigy.model.session.SessionManager;
 import com.netspective.medigy.reference.custom.party.PartyRelationshipType;
 import com.netspective.medigy.reference.custom.party.PartyRoleType;
 import com.netspective.medigy.reference.custom.GeographicBoundaryType;
 import com.netspective.medigy.util.HibernateUtil;
 import org.hibernate.Criteria;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.Set;
 import java.util.List;
 
 public class TestOrganization  extends TestCase
 {
     private static final Log log = LogFactory.getLog(PostalAddress.class);
 
     public void testPostalAddress()
     {
         Session session = new ProcessSession();
         session.setProcessName(TestPerson.class.getName() + ".testPostalAddress()");
         HibernateUtil.getSession().save(session);
         SessionManager.getInstance().setActiveSession(session);
 
         Organization org1 = new Organization();
         org1.setOrganizationName("Acme Corporation");
 
         /*
         final GeographicBoundary stateGeo = new GeographicBoundary();
         stateGeo.setType(GeographicBoundaryType.Cache.STATE.getEntity());
         stateGeo.setName("Virginia");
 
         final GeographicBoundary cityGeo = new GeographicBoundary();
         cityGeo.setType(GeographicBoundaryType.Cache.CITY.getEntity());
         cityGeo.setName("Fairfax");
         */
 
         PostalAddress address = new PostalAddress();
         address.setAddress1("123 Acme Road");
         address.setAddress2("Apt 9");
         address.setDirections("Go straight and jump!");
 
         HibernateUtil.beginTransaction();
         HibernateUtil.getSession().save(org1);
         // create the state Geographic Boundary
         //HibernateUtil.getSession().save(stateGeo);
         //HibernateUtil.getSession().save(cityGeo);
 
         address.setParty(org1);
         HibernateUtil.getSession().save(address);
         HibernateUtil.commitTransaction();
         HibernateUtil.closeSession();
 
         final PostalAddress savedAddress = (PostalAddress) HibernateUtil.getSession().load(PostalAddress.class,  address.getAddressId());
         assertNotNull(savedAddress);
         assertEquals(0, savedAddress.getAddressBoundaries().size());
         assertEquals(savedAddress.getAddress1(), "123 Acme Road");
         assertEquals(savedAddress.getAddress2(), "Apt 9");
         assertEquals(savedAddress.getDirections(), "Go straight and jump!");
         log.info("VALID: Postal Address");
 
         HibernateUtil.beginTransaction();
         // now relate the address with the city and state!
         final GeographicBoundary cityBoundary = new GeographicBoundary("Fairfax", GeographicBoundaryType.Cache.CITY.getEntity());
         final GeographicBoundary stateBoundary = new GeographicBoundary("Virginia", GeographicBoundaryType.Cache.STATE.getEntity());
         final GeographicBoundary postalCodeboundary = new GeographicBoundary("22033", GeographicBoundaryType.Cache.POSTAL_CODE.getEntity());
         final GeographicBoundary countyBoundary = new GeographicBoundary("Fairfax County", GeographicBoundaryType.Cache.COUNTY.getEntity());
         final GeographicBoundary countryBoundary = new GeographicBoundary("USA", GeographicBoundaryType.Cache.COUNTRY.getEntity());
 
         HibernateUtil.getSession().save(cityBoundary);
         HibernateUtil.getSession().save(stateBoundary);
         HibernateUtil.getSession().save(postalCodeboundary);
         HibernateUtil.getSession().save(countyBoundary);
         HibernateUtil.getSession().save(countryBoundary);
         HibernateUtil.commitTransaction();
 
         GeographicBoundary newCityBoundary = (GeographicBoundary) HibernateUtil.getSession().load(GeographicBoundary.class,  cityBoundary.getGeoId());
         assertNotNull(newCityBoundary);
         HibernateUtil.beginTransaction();
         savedAddress.setCity(newCityBoundary);
 
         savedAddress.setState(stateBoundary);
         savedAddress.setPostalCode(postalCodeboundary);
         savedAddress.setCounty(countyBoundary);
         savedAddress.setCountry(countryBoundary);
 
         HibernateUtil.getSession().save(savedAddress);
 
         HibernateUtil.commitTransaction();
         HibernateUtil.closeSession();
 
         // load all boundaries
         final Criteria criteria1 = HibernateUtil.getSession().createCriteria(GeographicBoundary.class);
         List boundaryList1  = criteria1.list();
         assertEquals(5, boundaryList1.size());
         log.info("VALID: Geo Boundary count = " + boundaryList1.size());
 
         final PostalAddress savedAddress2 = (PostalAddress) HibernateUtil.getSession().load(PostalAddress.class,  address.getAddressId());
         assertEquals(5, savedAddress2.getAddressBoundaries().size());
         assertNotNull(savedAddress2.getCity());
         assertNotNull(savedAddress2.getState());
         assertNotNull(savedAddress2.getPostalCode());
         assertNotNull(savedAddress2.getCounty());
         assertNotNull(savedAddress2.getCountry());
         assertEquals("Fairfax", savedAddress2.getCity().getName());
         assertEquals("Virginia", savedAddress2.getState().getName());
         assertEquals("22033", savedAddress2.getPostalCode().getName());
         assertEquals("Fairfax County", savedAddress2.getCounty().getName());
         assertEquals("USA", savedAddress2.getCountry().getName());
         log.info("VALID: Postal Address with geo relationships");
 
         HibernateUtil.beginTransaction();
         // change the city and state now
         final GeographicBoundary cityBoundary2 = new GeographicBoundary("Silver Spring", GeographicBoundaryType.Cache.CITY.getEntity());
         final GeographicBoundary stateBoundary2 = new GeographicBoundary("Maryland", GeographicBoundaryType.Cache.STATE.getEntity());
         HibernateUtil.getSession().save(cityBoundary2);
         HibernateUtil.getSession().save(stateBoundary2);
         HibernateUtil.commitTransaction();
 

         HibernateUtil.beginTransaction();
         savedAddress2.setCity(cityBoundary2);
         savedAddress2.setState(stateBoundary2);
         HibernateUtil.getSession().update(savedAddress2);
         HibernateUtil.commitTransaction();
 
         // load all boundaries
         final Criteria criteria2 = HibernateUtil.getSession().createCriteria(GeographicBoundary.class);
         List boundaryList2  = criteria2.list();
         assertEquals(7, boundaryList2.size());
 
         final PostalAddress savedAddress3 = (PostalAddress) HibernateUtil.getSession().load(PostalAddress.class,  savedAddress2.getAddressId());
         assertEquals(5, savedAddress3.getAddressBoundaries().size());
         assertNotNull(savedAddress3.getCity());
         assertNotNull(savedAddress3.getState());
         assertNotNull(savedAddress3.getPostalCode());
         assertNotNull(savedAddress3.getCounty());
         assertNotNull(savedAddress3.getCountry());
         assertEquals("Silver Spring", savedAddress3.getCity().getName());
         assertEquals("Maryland", savedAddress3.getState().getName());
         assertEquals("22033", savedAddress3.getPostalCode().getName());
         assertEquals("Fairfax County", savedAddress3.getCounty().getName());
         assertEquals("USA", savedAddress3.getCountry().getName());
     }
 
     public void testOrg()
     {
         Session session = new ProcessSession();
         session.setProcessName(TestPerson.class.getName() + ".testOrg()");
         HibernateUtil.getSession().save(session);
         SessionManager.getInstance().setActiveSession(session);
 
         Organization org1 = new Organization();
         org1.setOrganizationName("Acme Corporation");
 
         final Organization org2 = new Organization();
         org2.setOrganizationName("Acme Subsidiary");
 
         HibernateUtil.beginTransaction();
 
         HibernateUtil.getSession().save(org1);
         HibernateUtil.getSession().save(org2);
 
         HibernateUtil.commitTransaction();
         //HibernateUtil.closeSession();
 
         final Organization parentOrg = (Organization) HibernateUtil.getSession().load(Organization.class,
                 org1.getOrgId());
         assertEquals(parentOrg.getOrganizationName(), "Acme Corporation");
         assertEquals(parentOrg.getPartyName(), "Acme Corporation");
 
         final Organization childOrg = (Organization) HibernateUtil.getSession().load(Organization.class,
                 org2.getOrgId());
         assertEquals(childOrg.getOrganizationName(), "Acme Subsidiary");
         assertEquals(childOrg.getPartyName(), "Acme Subsidiary");
 
         HibernateUtil.beginTransaction();
         // add a new role belonging to the parent org
         final PartyRole role1 = new PartyRole();
         role1.setParty(parentOrg);
         role1.setType(PartyRoleType.Cache.PARENT_ORG.getEntity());
         parentOrg.getPartyRoles().add(role1);
         HibernateUtil.getSession().update(parentOrg);
         // add a new role belonging to the child org
         final PartyRole role2 = new PartyRole();
         role2.setParty(childOrg);
         role2.setType(PartyRoleType.Cache.SUBSIDIARY.getEntity());
         childOrg.getPartyRoles().add(role2);
         HibernateUtil.getSession().update(childOrg);
 
         PartyRelationship rel = new PartyRelationship();
        rel.setRelationshipType(PartyRelationshipType.Cache.ORGANIZATION_ROLLUP.getEntity());
        rel.setPartyRole(role1);
         HibernateUtil.getSession().save(rel);
 
         rel = new PartyRelationship();
        rel.setRelationshipType(PartyRelationshipType.Cache.ORGANIZATION_ROLLUP.getEntity());
        rel.setPartyRole(role2);
         HibernateUtil.getSession().save(rel);
 
 
         HibernateUtil.commitTransaction();
         HibernateUtil.closeSession();
 
         // validate that the roles are assigned to the organizations
         final Organization updatedParentOrg = (Organization) HibernateUtil.getSession().load(Organization.class,
                 parentOrg.getOrgId());
         // verify that the parent org has one role defined
         assertEquals(1, updatedParentOrg.getPartyRoles().size());
         //info("Success. Parent org has 1 role(s).");
         final PartyRole parentOrgRole = (PartyRole) updatedParentOrg.getPartyRoles().toArray()[0];
         assertEquals(PartyRoleType.Cache.PARENT_ORG.getEntity().getCode(),
                 parentOrgRole.getType().getCode());
         //info("Success. Parent org's role is " + PartyRoleType.Cache.PARENT_ORG);
 
         final Organization updatedChildOrg = (Organization) HibernateUtil.getSession().load(Organization.class,
                 childOrg.getOrgId());
         // verify that the child org has one role
         assertEquals(1, updatedChildOrg.getPartyRoles().size());
         //info("Success. Child org has 1 role(2).");
         // verify that the child org's roles are the right ones
         final PartyRole childOrgRole = (PartyRole) updatedChildOrg.getPartyRoles().toArray()[0];
         assertEquals(PartyRoleType.Cache.SUBSIDIARY.getEntity().getPartyRoleTypeId(),
                 childOrgRole.getType().getPartyRoleTypeId());
         //info("Success. Child org's role is " + PartyRoleType.Cache.SUBSIDIARY);
 
         // verify that the parent org's one role has one relationship
        Set<PartyRelationship> parentOrgRoleRelationships = parentOrgRole.getPartyRelationships();
         assertEquals(1, parentOrgRoleRelationships.size());
         //info("Success. Parent org's role has 1 relationship.");
         // verify that the child org's one role has one relationship
        Set<PartyRelationship> childOrgRoleRelationships = childOrgRole.getPartyRelationships();
         assertEquals(1, childOrgRoleRelationships.size());
         //info("Success. Child org's role has 1 relationship.");
 
         // verify that the two org's respective roles are involved in the same relationship?
         final PartyRelationship parentOrgRelationship = (PartyRelationship)parentOrgRoleRelationships.toArray()[0];
         final PartyRelationship childOrgRelationship = (PartyRelationship)childOrgRoleRelationships.toArray()[0];
        assertEquals(parentOrgRelationship.getRelationshipType().getCode(),
                childOrgRelationship.getRelationshipType().getCode()) ;
         //info("Success. Parent and Child org's respective role's share one relationship.");
 
         HibernateUtil.closeSession();
     }
 }
