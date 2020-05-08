 /**
  * This file is part of the LDAP Persistence API (LPA).
  * 
  * Copyright Trenton D. Adams <lpa at trentonadams daught ca>
  * 
  * LPA is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * LPA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
  * License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public 
  * License along with LPA.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * See the COPYING file for more information.
  */
 package ca.tnt.ldaputils;
 
 import ca.tnt.ldaputils.impl.LDAPEntryImpl;
 import ca.tnt.ldaputils.impl.LdapEntry;
 import ca.tnt.ldaputils.impl.LdapGroup;
 import ca.tnt.ldaputils.impl.LdapOrganization;
 import ca.tnt.ldaputils.ldapimpl.*;
 import ca.tnt.ldaputils.proprietary.ILdapBusiness;
 import ca.tnt.ldaputils.proprietary.LdapBusiness;
 import org.apache.directory.server.annotations.CreateLdapServer;
 import org.apache.directory.server.annotations.CreateTransport;
 import org.apache.directory.server.core.annotations.ApplyLdifFiles;
 import org.apache.directory.server.core.annotations.ContextEntry;
 import org.apache.directory.server.core.annotations.CreateDS;
 import org.apache.directory.server.core.annotations.CreatePartition;
 import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
 import org.apache.directory.server.core.integ.FrameworkRunner;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import javax.naming.InvalidNameException;
 import javax.naming.NamingException;
 import javax.naming.directory.SearchControls;
 import javax.naming.ldap.LdapName;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Test ldap based annotations
  * <p/>
  * TEST for unhandled field types.  The field should simply not be touched.
  * <p/>
 * TEST for searches and finds where an ldap entry does not have a "supported"
 * object class
  * <p/>
  * Created :  16-Aug-2010 8:03:48 PM MST
  *
  * @author Trenton D. Adams
  */
 @SuppressWarnings({"JavaDoc", "ClassWithoutConstructor", "ChainedMethodCall"})
 @RunWith(FrameworkRunner.class)
 @CreateLdapServer(
     transports =
         {
             @CreateTransport(protocol = "LDAP")
         })
 @CreateDS(allowAnonAccess = false, name = "example-partition",
     partitions =
         {
             @CreatePartition(
                 name = "example",
                 suffix = "dc=example,dc=com",
                 contextEntry = @ContextEntry(
                     entryLdif = "dn: dc=example,dc=com\n" +
                         "objectclass: dcObject\n" +
                         "objectclass: organization\n" +
                         "o: example\n" +
                         "dc: example"))
         })
 @ApplyLdifFiles({
     "example.schema.ldif",
     "add-domain.ldif",
     "add-busgroups.ldif",
     "add-businesses.ldif"})
 public class AnnotationsTest extends AbstractLdapTestUnit
 {
     private LdapManager manager;
 
     @Before
     public void setUp()
     {
         manager = new LdapManager("localhost", "" + ldapServer.getPort(),
             "uid=admin,ou=system", "secret");
     }
 
     @Test
     public void testGetObject() throws NamingException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         final LdapEntry ldapEntry = (LdapEntry) manager.find(LdapEntry.class,
             ldapName);
         Assert.assertEquals("LdapName differs", ldapName, ldapEntry.getDn());
         Assert.assertEquals("Postal code differs", "A1A 2B2",
             ldapEntry.getAttributes().get("postalCode").get());
 //        System.out.println("attributes: " + ldapEntry);
 
         ldapEntry.modifyBatchAttribute(LdapEntry.ADD_ATTRIBUTE,
             "mail", "another@example.com");
         ldapEntry.save();
 
         final LdapEntry changedEntry = (LdapEntry) manager.find(LdapEntry.class,
             ldapName);
         final List values = changedEntry.getAttributeValues("mail");
         Assert.assertTrue("changed entry does not have new email address",
             values.contains("another@example.com"));
         ldapEntry.modifyBatchAttribute(LdapEntry.REMOVE_ATTRIBUTE,
             "mail", "another@example.com");
         ldapEntry.save();
 
 //        System.out.println("objectClasses: " + ldapEntry.getObjectClasses());
     }
 
     /**
      * Tests that the object class validation works.
      *
      * @throws InvalidNameException if the dn is invalid.
      */
     @Test
     public void testObjectClasses() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Hair by Person X,ou=businesses,dc=example,dc=com");
         final LdapEntry ldapEntry = (LdapEntry) manager.find(LdapEntry.class,
             ldapName);
         Assert.assertTrue("Should be organization",
             ldapEntry.isObjectClass("organization"));
         Assert.assertTrue("Should be labeledURIObject",
             ldapEntry.isObjectClass("labeledURIObject"));
     }
 
     @Test
     public void testSubclassNotEntity() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         try
         {
             final LdapEntityNotAnnotated ldapEntry =
                 (LdapEntityNotAnnotated) manager.find(
                     LdapEntityNotAnnotated.class, ldapName);
             Assert.fail("subclassed entity not annotated should fail");
         }
         catch (IllegalArgumentException e)
         {   // this should happen, and we need to make a test case for this.
         }
     }
 
     @Test
     public void testNoDNMethod() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         try
         {
             final LdapEntityNoDNMethod ldapEntry =
                 (LdapEntityNoDNMethod) manager.find(LdapEntityNoDNMethod.class,
                     ldapName);
             Assert.fail("dn get method is correctly defined?");
         }
         catch (IllegalArgumentException e)
         {   // this should happen, as the method is not defined
         }
     }
 
     @Test
     public void testBadDNMethod() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         try
         {
             final LdapEntityBadDNMethod ldapEntry =
                 (LdapEntityBadDNMethod) manager.find(
                     LdapEntityBadDNMethod.class, ldapName);
             Assert.fail("dn get method is correctly defined?");
         }
         catch (IllegalArgumentException e)
         {   // this should happen, as the method is incorrectly defined
         }
     }
 
     @Test
     public void testNoDN() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         try
         {
             final LdapEntityNoDN ldapEntry =
                 (LdapEntityNoDN) manager.find(LdapEntityNoDN.class, ldapName);
             Assert.fail("an @DN is defined, it shouldn't be!");
         }
         catch (IllegalArgumentException e)
         {   // this should happen, as the method is incorrectly defined
         }
     }
 
     @Test
     public void testAggregation() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         final long before;
         final long after;
 
         before = System.currentTimeMillis();
         final ILdapOrganization ldapEntry =
             (ILdapOrganization) manager.find(LdapAggregation.class, ldapName);
         after = System.currentTimeMillis();
         System.out.println("find took: " + (after - before) + "ms");
 
         Assert.assertEquals("Organization should be Pulp Mill",
             "Pulp Mill.",
             ldapEntry.getOrganization());
         Assert.assertEquals("Should be in exactly two categories",
             2, ldapEntry.getBusinessCategories().size());
     }
 
     @Test
     public void testLdapSearch() throws InvalidNameException
     {
         final Map sortedMap;
         sortedMap = manager.find(new LdapName(
             "dc=example,dc=com"), "o=*Hair*", "o", null,
             LdapOrganization.class, LdapManager.SORTED_ORDER,
             SearchControls.SUBTREE_SCOPE);
 
         Assert.assertEquals("number of hair salons in directory", 4,
             sortedMap.size());
     }
 
     /**
      * Tests using the ADD_ATTRIBUTE capability.
      */
     @Test
     public void testAdd() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         final ILdapBusiness ldapEntry = (ILdapBusiness) manager.find(
             LdapBusiness.class, ldapName);
         // logger.info("adding mail trent@example.com");
         ldapEntry.modifyBatchAttribute(ILdapEntry.ADD_ATTRIBUTE,
             "mail", "another@example.com");
         ldapEntry.modifyBatchAttributes();
 
         final ILdapBusiness ldapEntry2 = (ILdapBusiness) manager.find(
             LdapBusiness.class, ldapName);
         Assert.assertArrayEquals("email",
             new String[]{"someone@example.com"}, ldapEntry.getMails()   );
         Assert.assertArrayEquals("email",
             new String[]{"someone@example.com", "another@example.com"},
             ldapEntry2.getMails());
     }
 
     /**
      * Tests using the REMOVE_ATTRIBUTE capability.
      */
     @Test
     public void testRemove() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         final ILdapBusiness ldapEntry = (ILdapBusiness) manager.find(
             LdapBusiness.class, ldapName);
         ldapEntry.modifyBatchAttribute(LDAPEntryImpl.REMOVE_ATTRIBUTE,
             "mail", "someone@example.com");
         ldapEntry.modifyBatchAttributes();
         final ILdapBusiness ldapEntry2 = (ILdapBusiness) manager.find(
             LdapBusiness.class, ldapName);
         Assert.assertArrayEquals("email", ldapEntry2.getMails(),
             new String[]{});
     }
 
     /**
      * Tests to ensure that the system does in fact not find an entry for an
      * object that REQUIRES a certain objectClass.
      * <p/>
      * This test can be forced to fail by changing missing-object-class in
      * add-businesses.ldif to have the tntbusiness object class.
      *
      * @throws InvalidNameException
      */
     @Test
     public void testMissingObjectClass() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=missing-object-class,ou=businesses,dc=example,dc=com");
         final ILdapBusiness ldapEntry = (ILdapBusiness) manager.find(
             LdapBusiness.class, ldapName);
         Assert.assertNull("should not find missing-object-class", ldapEntry);
     }
 
     /**
      * Tests for alternate aggregation storage mechanisms, using {@link
      * AlternateAggregates}
      *
      * @throws InvalidNameException
      */
     @Test
     public void testAlternateAggregation() throws InvalidNameException
     {
         final LdapName ldapName = new LdapName(
             "o=Pulp Mill.,ou=businesses,dc=example,dc=com");
         final long before;
         final long after;
 
         before = System.currentTimeMillis();
         final AlternateAggregates ldapEntry =
             (AlternateAggregates) manager.find(AlternateAggregates.class,
                 ldapName);
         after = System.currentTimeMillis();
         System.out.println("find took: " + (after - before) + "ms");
 
         Assert.assertEquals("sorted set groups",
             2, ldapEntry.getSortedSetGroups().size());
         Assert.assertNotNull("single group",
             ldapEntry.getSingleGroup());
         Assert.assertEquals("array of groups",
             2, ldapEntry.getArrayOfGroups().length);
         Assert.assertEquals("list of groups",
             2, ldapEntry.getListOfGroups().size());
 
     }
 }
