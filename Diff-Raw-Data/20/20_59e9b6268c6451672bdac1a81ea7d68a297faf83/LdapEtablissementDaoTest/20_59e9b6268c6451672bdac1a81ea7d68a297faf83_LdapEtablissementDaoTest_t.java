 /**
  * 
  */
 package org.esco.portlet.changeetab.dao.impl;
 
 import java.util.Collection;
 
 import org.esco.portlet.changeetab.model.Etablissement;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.Resource;
 import org.springframework.ldap.core.ContextSource;
 import org.springframework.ldap.core.DistinguishedName;
 import org.springframework.ldap.test.LdapTestUtils;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
 * FIXME: Unable to load the Apache Directory for the test !
 * 
  * @author GIP RECIA 2013 - Maxime BOSSARD.
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations="classpath:ldapEtablissementDaoContext.xml")
 public class LdapEtablissementDaoTest {
 
 	private static int port = 42539;
 	private static String defaultPartitionSuffix = "dc=esco-centre,dc=fr";
 	private static String defaultPartitionName = "root";
 	private static String principal = "uid=admin,ou=system";
 	private static String credentials = "secret";
 
 	@Autowired
 	private ContextSource contextSource;
 
 	@Autowired
 	private LdapEtablissementDao dao;
 
	@Value(value="classpath:esco-structure-schema.ldif")
	private Resource escoStructuresSchemaLdif;

	@Value(value="classpath:init.ldif")
 	private Resource initLdif;
 
 	@BeforeClass
 	public static void init() throws Exception {
 		LdapTestUtils.startApacheDirectoryServer(LdapEtablissementDaoTest.port,
 				LdapEtablissementDaoTest.defaultPartitionSuffix, LdapEtablissementDaoTest.defaultPartitionName,
 				LdapEtablissementDaoTest.principal, LdapEtablissementDaoTest.credentials, null);
 	}
 
 	@AfterClass
 	public static void end() throws Exception {
 		LdapTestUtils.destroyApacheDirectoryServer(LdapEtablissementDaoTest.principal, LdapEtablissementDaoTest.credentials);
 	}
 
 	@Before
 	public void initLdap() throws Exception {
		final DistinguishedName schemaDn = new DistinguishedName("cn=schema,dc=esco-centre,dc=fr");
		final DistinguishedName structuresDn = new DistinguishedName("ou=structures,dc=esco-centre,dc=fr");

		LdapTestUtils.cleanAndSetup(this.contextSource, schemaDn , this.escoStructuresSchemaLdif);
		LdapTestUtils.cleanAndSetup(this.contextSource, structuresDn , this.initLdif);
 	}
 
 	@Test
	@Ignore // FIXME: Unable to load the Apache Directory for the test !
 	public void testFindAllEtablissements() throws Exception {
 		final Collection<Etablissement> etabs = this.dao.findAllEtablissements();
 
 		Assert.assertNotNull("Etabs list shoud be empty not null !", etabs);
 
 		Assert.assertTrue("Etabs list shoud not be empty !", etabs.size() > 0);
 	}
 
 
 
 }
