 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.persistence.deployer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.collections.MultiHashMap;
 import org.apache.commons.configuration.ConfigurationException;
 import org.glite.security.voms.admin.configuration.VOMSConfiguration;
 import org.glite.security.voms.admin.configuration.VOMSConfigurationConstants;
 import org.glite.security.voms.admin.core.VOMSServiceConstants;
 import org.glite.security.voms.admin.core.tasks.DatabaseSetupTask;
 import org.glite.security.voms.admin.core.tasks.UpdateCATask;
 import org.glite.security.voms.admin.error.VOMSException;
 import org.glite.security.voms.admin.operations.VOMSPermission;
 import org.glite.security.voms.admin.persistence.DBUtil;
 import org.glite.security.voms.admin.persistence.HibernateFactory;
 import org.glite.security.voms.admin.persistence.dao.ACLDAO;
 import org.glite.security.voms.admin.persistence.dao.CertificateDAO;
 import org.glite.security.voms.admin.persistence.dao.VOMSAdminDAO;
 import org.glite.security.voms.admin.persistence.dao.VOMSGroupDAO;
 import org.glite.security.voms.admin.persistence.dao.VOMSRoleDAO;
 import org.glite.security.voms.admin.persistence.dao.VOMSUserDAO;
 import org.glite.security.voms.admin.persistence.dao.VOMSVersionDAO;
 import org.glite.security.voms.admin.persistence.dao.generic.DAOFactory;
 import org.glite.security.voms.admin.persistence.error.VOMSDatabaseException;
 import org.glite.security.voms.admin.persistence.model.ACL;
 import org.glite.security.voms.admin.persistence.model.AUP;
 import org.glite.security.voms.admin.persistence.model.Certificate;
 import org.glite.security.voms.admin.persistence.model.VOMSAdmin;
 import org.glite.security.voms.admin.persistence.model.VOMSCA;
 import org.glite.security.voms.admin.persistence.model.VOMSDBVersion;
 import org.glite.security.voms.admin.persistence.model.VOMSGroup;
 import org.glite.security.voms.admin.persistence.model.VOMSRole;
 import org.glite.security.voms.admin.persistence.model.VOMSUser;
 import org.glite.security.voms.admin.util.SysconfigUtil;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.dialect.Dialect;
 import org.hibernate.exception.GenericJDBCException;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 import org.hibernate.tool.hbm2ddl.SchemaUpdate;
 import org.hibernate.type.LongType;
 import org.hibernate.type.ShortType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @SuppressWarnings("deprecation")
 public class SchemaDeployer {
 
 	public static final String ORACLE_PRODUCT_NAME = "Oracle";
 	public static final String MYSQL_PRODUCT_NAME = "MySQL";
 
 	private static final Logger log = LoggerFactory
 			.getLogger(SchemaDeployer.class);
 
 	protected CommandLineParser parser = new PosixParser();
 
 	protected HelpFormatter helpFormatter = new HelpFormatter();
 
 	protected Options options;
 
 	String command;
 
 	String vo;
 
 	String hibernateConfigFile = null;
 
 	String hibernatePropertiesFile = null;
 
 	String adminDN = null;
 
 	String adminCA = null;
 
 	String adminEmailAddress = null;
 
 	SessionFactory sf;
 
 	protected Dialect dialect;
 
 	public SchemaDeployer(String[] args) {
 
 		setupCLParser();
 		checkArguments(args);
 		execute();
 
 	}
 
 	private void printUpgradeScript() {
 
 		SchemaUpdate updater = new SchemaUpdate(loadHibernateConfiguration());
 		updater.execute(true, false);
 
 	}
 
 	private void checkDatabaseConnectivity() {
 
 		log.info("Checking database connectivity...");
 
 		Session s = null;
 
 		try {
 
 			s = HibernateFactory.getFactory().openSession();
 			s.beginTransaction();
 
 		} catch (GenericJDBCException e) {
 			log.error("");
 
 			log.error("===========================================================================================================================");
 			log.error("Error connecting to the voms database! Check your database settings and ensure that the database backend is up and running.");
 			log.error("============================================================================================================================");
 
 			if (log.isDebugEnabled())
 				log.error(e.getMessage(), e);
 
 			System.exit(-1);
 
 		} finally {
 
 			if (s != null)
 				s.close();
 		}
 
 		log.info("Database contacted succesfully");
 	}
 
 	private void checkDatabaseWritable() {
 
 		log.info("Checking that the database is writable...");
 
 		Session s = null;
 
 		try {
 
 			s = HibernateFactory.getFactory().openSession();
 			Transaction t = s.beginTransaction();
 
 			s.createSQLQuery("create table writetest(integer a)");
 			t.commit();
 
 			t = s.beginTransaction();
 			s.createSQLQuery("drop table writetest");
 			t.commit();
 
 		} catch (Throwable t) {
 
 			log.error("Error writing to the voms database. Check your database settings and that the database backend is up and running.");
 
 			if (log.isDebugEnabled())
 				log.error(
 						"Error opening connection to the voms database. Check your database settings, or ensure that the local is up & running\nCause:"
 								+ t.getMessage(), t);
 
 			throw new VOMSDatabaseException(
 					"Error opening connection to the voms database. Check your database settings, or ensure that the local is up & running",
 					t);
 
 		} finally {
 
 			if (s != null)
 				s.close();
 		}
 
 		log.info("Database is writable.");
 
 	}
 
 	private void execute() {
 
 		System.setProperty(VOMSConfigurationConstants.VO_NAME, vo);
 		VOMSConfiguration.load(null);
 
 		if (command.equals("deploy"))
 			doDeploy();
 		else if (command.equals("undeploy"))
 			doUndeploy();
 		else if (command.equals("upgrade"))
 			doUpgrade();
 		else if (command.equals("add-admin"))
 			doAddAdmin();
 		else if (command.equals("remove-admin"))
 			doRemoveAdmin();
 		else if (command.equals("upgrade-script"))
 			printUpgradeScript();
 		else if (command.equals("check-connectivity")) {
 			checkDatabaseExistence();
 		} else if (command.equals("grant-read-only-access")){
 			doGrantROAccess();
 		} else {
 
 			System.err.println("Unkown command specified: " + command);
 			System.exit(2);
 		}
 
 	}
 
 	private boolean isOracleBackend() {
 
 		Session s = HibernateFactory.getSession();
 		s.beginTransaction();
 
 		DatabaseMetaData dbMetadata = null;
 		String dbProductName = null;
 
 		try {
 
 			dbMetadata = s.connection().getMetaData();
 			dbProductName = dbMetadata.getDatabaseProductName();
 
 		} catch (HibernateException e) {
 
 			log.error(
 					"Hibernate error accessing database metadata from Hibernate connection!",
 					e);
 			System.exit(-1);
 
 		} catch (SQLException e) {
 
 			log.error(
 					"SQL error while accessing database metadata from Hibernate connection!",
 					e);
 			System.exit(-1);
 
 		}
 
 		log.debug("Detected database: " + dbProductName);
 		return dbProductName.trim().equals(ORACLE_PRODUCT_NAME);
 
 	}
 
 	private void printException(Throwable t) {
 
 		if (t.getMessage() != null)
 			log.error(t.getMessage());
 		else
 			log.error(t.toString());
 
 		if (log.isDebugEnabled()) {
 
 			if (t.getMessage() != null)
 				log.error(t.getMessage(), t);
 			else
 				log.error(t.toString(), t);
 
 		}
 	}
 
 	private void printAllException(Throwable t) {
 
 		if (t != null)
 			printException(t);
 
 		if (t.getCause() != null) {
 			log.error("caused by:");
 			printAllException(t.getCause());
 
 		}
 	}
 
 	private void printExceptions(List l) {
 
 		Iterator i = l.iterator();
 
 		while (i.hasNext()) {
 
 			Throwable t = (Throwable) i.next();
 			log.error(t.getMessage());
 
 			if (log.isDebugEnabled())
 				printAllException(t);
 		}
 
 	}
 
 	private String getVarcharType() {
 
 		if (isOracleBackend())
 			return "varchar2";
 		else
 			return "varchar";
 	}
 
 	private String getTimestampType() {
 
 		if (isOracleBackend())
 			return "timestamp";
 		else
 			return "datetime";
 
 	}
 
 	private ResultSet getTableNamesMatchingPattern(DatabaseMetaData md,
 			String pattern) {
 
 		String[] names = { "TABLE" };
 
 		ResultSet tableNames = null;
 
 		try {
 
 			tableNames = md.getTables(null, "%", pattern, names);
 
 		} catch (SQLException e) {
 			log.error(
 					"Error reading table names from database metadata object!",
 					e);
 			System.exit(-1);
 		}
 
 		return tableNames;
 	}
 
 	private int checkDatabaseExistence() {
 		checkVoExistence();
 		checkDatabaseConnectivity();
 		log.info("Checking database existence...");
 		Session s = HibernateFactory.getSession();
 
 		DatabaseMetaData dbMetadata = null;
 
 		try {
 
 			dbMetadata = s.connection().getMetaData();
 
 		} catch (HibernateException e) {
 
 			log.error(
 					"Hibernate error accessing database metadata from Hibernate connection!",
 					e);
 			System.exit(-1);
 
 		} catch (SQLException e) {
 
 			log.error(
 					"SQL error while accessing database metadata from Hibernate connection!",
 					e);
 			System.exit(-1);
 
 		}
 
 		ResultSet tableNames = getTableNamesMatchingPattern(dbMetadata, "%");
 
 		boolean foundACL2 = false;
 		boolean foundACL = false;
 		boolean foundAUP = false;
 
 		try {
 
 			while (tableNames.next()) {
 				String tName = tableNames.getString("TABLE_NAME");
 				if (tName.equals("ACL") || tName.equals("acl"))
 					foundACL = true;
 
 				if (tName.equals("ACL2") || tName.equals("acl2"))
 					foundACL2 = true;
 
 				if (tName.equalsIgnoreCase("aup"))
 					foundAUP = true;
 			}
 
 			HibernateFactory.commitTransaction();
 
 		} catch (SQLException e) {
 
 			log.error("Error accessing table names result set!", e);
 			System.exit(-1);
 
 		} catch (HibernateException e) {
 			log.error("Error committing read-only hibernate transaction!", e);
 			System.exit(-1);
 
 		}
 
 		if (foundACL2 && foundAUP) {
 			log.info("Found existing voms-admin > 2.5.x database...");
 			return 3;
 		}
 		if (foundACL2) {
 			log.info("Found existing voms-admin 2.0.x database...");
 			return 2;
 		}
 
 		if (foundACL) {
 			log.info("Found existing voms-admin 1.2.x database...");
 			return 1;
 
 		}
 
 		log.info("No voms-admin database found.");
 		return -1;
 	}
 
 	private void doUpgrade1_2_19(Configuration hibernateConfig) {
 
 		HibernateFactory.beginTransaction();
 
 		try {
 
 			renameTables_1_2_19();
 
 			HibernateFactory.commitTransaction();
 
 			HibernateFactory.beginTransaction();
 
 			SchemaExport exporter = new SchemaExport(hibernateConfig);
 
 			exporter.execute(true, true, false, true);
 
 			log.info("Deploying voms 2 database...");
 
 			List l = exporter.getExceptions();
 
 			if (!l.isEmpty()) {
 				log.error("Error deploying voms 2 database!");
 				printExceptions(l);
 				System.exit(2);
 
 			}
 
 			if (isOracleBackend())
 				fixHibernateSequence();
 
 			removeDuplicatedACLEntries();
 
 			migrateDbContents();
 			migrateMappings();
 			migrateACLs();
 			dropOldTables_1_2_19();
 
 			if (isOracleBackend())
 				dropOldSequences();
 
 			HibernateFactory.commitTransaction();
 			log.info("Database upgraded succesfully!");
 
 		} catch (Throwable t) {
 
 			log.error("Database upgrade failed!");
 			HibernateFactory.rollbackTransaction();
 			HibernateFactory.closeSession();
 			System.exit(2);
 		}
 
 	}
 
 	private void implicitAUPSignup() {
 
 		log.info("Adding implicit AUP sign records for vo users");
 
 		List<VOMSUser> users = VOMSUserDAO.instance().findAll();
 		AUP aup = DAOFactory.instance().getAUPDAO().getVOAUP();
 
 		for (VOMSUser u : users)
 			VOMSUserDAO.instance().signAUP(u, aup);
 
 	}
 
 	private List<String> loadUpgradeScript() throws IOException {
 
 		String upgradeScriptFileName = "/upgrade-scripts/mysql-upgrade_20_25.sql";
 
 		if (isOracleBackend())
 			upgradeScriptFileName = "/upgrade-scripts/oracle-upgrade_20_25.sql";
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(this
 				.getClass().getResourceAsStream(upgradeScriptFileName)));
 
 		ArrayList<String> commands = new ArrayList<String>();
 
 		String line;
 
 		do {
 			line = reader.readLine();
 			if (line != null)
 				commands.add(line);
 
 		} while (line != null);
 
 		return commands;
 
 	}
 	
 	private void fixHibernateSequences261(Configuration hibernateConfig){
 		
 		String[] newOracleSequences = {"VOMS_ACL_SEQ", 
 				"VOMS_ADMIN_SEQ", 
 				"VOMS_ATTR_DESC_SEQ", 
 				"VOMS_AUP_ACC_REC_SEQ", 
 				"VOMS_AUP_SEQ", 
 				"VOMS_AUP_VER_SEQ", 
 				"VOMS_CA_SEQ", 
 				"VOMS_CERT_SEQ", 
 				"VOMS_GROUP_SEQ", 
 				"VOMS_M_SEQ", 
 				"VOMS_PI_SEQ", 
 				"VOMS_PI_TYPE_SEQ", 
 				"VOMS_REQ_INFO_SEQ", 
 				"VOMS_REQ_SEQ", 
 				"VOMS_ROLE_SEQ", 
 				"VOMS_TAG_MAP_SEQ", 
 				"VOMS_TAG_SEQ", 
 				"VOMS_TASK_LR_SEQ", 
 				"VOMS_TASK_SEQ", 
 				"VOMS_TASK_TYPE_SEQ" };
 		
 		log.info("Creating oracle sequences starting from VOMS Admin 2.6.x database.");
 		Session s = HibernateFactory.getSession();
 
 		Long maxSeqValue = (Long) s
 				.createSQLQuery(
 						"select max(last_number) as max from user_sequences where sequence_name = 'HIBERNATE_SEQUENCE'")
 				.addScalar("max", new LongType()).uniqueResult();
 		
 		for (String seq: newOracleSequences){
 			
 			String createHibSeqStatement = "create sequence "+seq+" MINVALUE 1 MAXVALUE 999999999999999999999999999 "
 					+ "INCREMENT BY 1 START WITH "
 					+ maxSeqValue
 					+ " CACHE 20 NOORDER NOCYCLE";
 			
 			s.createSQLQuery(createHibSeqStatement).executeUpdate();
 		}
 		
 		log.info("Sequences migration complete.");
 		
 	}
 	
 	private void doUpgrade2_5(Configuration hibernateConfig){
 		
 		HibernateFactory.beginTransaction();
 		VOMSDBVersion version = VOMSVersionDAO.instance().getVersion();
 		
		if (isOracleBackend() && ( version.getAdminVersion().equals("2.6.1") || 
				version.getAdminVersion().equals("2.5.5"))){
 			try{
 				
 				fixHibernateSequences261(hibernateConfig);
 			
 			}catch (Exception e) {
 				
 				log.error("Error fixing VOMS Admin 2.6.1 oracle sequence: {}", e.getMessage(),e);
 				HibernateFactory.rollbackTransaction();
 			}
 		}
 		
 		HibernateFactory.commitTransaction();
 	}
 
 	private void doUpgrade2_0_x(Configuration hibernateConfig) {
 
 		try {
 
 			HibernateFactory.beginTransaction();
 
 			List<String> upgradeScript = loadUpgradeScript();
 
 			ArrayList<Exception> exceptions = new ArrayList<Exception>();
 
 			log.info("Upgrading voms database...");
 
 			Statement statement = HibernateFactory.getSession().connection()
 					.createStatement();
 
 			for (String command : upgradeScript) {
 				try {
 
 					log.info(command);
 					statement.executeUpdate(command);
 
 				} catch (SQLException e) {
 					log.error("Error while executing: " + command);
 					exceptions.add(e);
 				}
 
 			}
 
 			if (!exceptions.isEmpty()) {
 				log.error("Error upgrading voms database!");
 				printExceptions(exceptions);
 				System.exit(2);
 			}
 
 			dropUnusedTables_2_0_x();
 			fixCaTable();
 			migrateUsrTable();
 			HibernateFactory.commitTransaction();
 
 			HibernateFactory.beginTransaction();
 			fixUsrTable();
 			updateACLPerms();
 
 			DatabaseSetupTask.instance().run();
 
 			// Very old version of VOMS Admin populated the admin table
 			// incorrectly, so we're left
 			// with many orphans there that can be dropped.
 			dropOrphanedAdministrators();
 
 			// Make users accept implicitly the AUP, if you don't want to flood
 			// them
 			// with emails
 			implicitAUPSignup();
 
 			// Upgrade database version
 			log.info("Upgrading database version information");
 
 			VOMSVersionDAO.instance().setupVersion();
 
 			HibernateFactory.commitTransaction();
 			log.info("Database upgrade successfull!");
 
 		} catch (Throwable t) {
 
 			log.error("Database upgrade failed!", t);
 			HibernateFactory.rollbackTransaction();
 			HibernateFactory.closeSession();
 			System.exit(2);
 		}
 		System.exit(0);
 
 	}
 
 	private void doUpgrade() {
 
 		checkVoExistence();
 
 		Configuration hibernateConfig = loadHibernateConfiguration();
 
 		int existingDB = checkDatabaseExistence();
 
 		if (existingDB == -1) {
 			log.error("No voms-admin 1.2.x database found to upgrade!");
 			System.exit(-1);
 		}
 
 		if (existingDB == 1) {
 			log.info("Upgrading voms-admin 1.2.x database to the voms-admin > 2.5.x structure.");
 			doUpgrade1_2_19(hibernateConfig);
 
 		}
 
 		if (existingDB == 2) {
 			log.info("Upgrading voms-admin 2.0.x database to the voms-admin > 2.5.x structure.");
 			doUpgrade2_0_x(hibernateConfig);
 		}
 
 		if (existingDB == 3){
 			log.info("Upgrading voms-admin 2.5.x database to the latest schema.");
 			doUpgrade2_5(hibernateConfig);
 		}
 	}
 
 	private void doRemoveAdmin() {
 
 		checkVoExistence();
 
 		if (adminDN == null || adminCA == null)
 			throw new VOMSException("adminDN or adminCA is not set!");
 
 		try {
 
 			VOMSAdmin a = VOMSAdminDAO.instance().getByName(adminDN, adminCA);
 
 			if (a == null) {
 
 				log.info("Admin '" + adminDN + "," + adminCA
 						+ "' does not exists in database...");
 				return;
 			}
 
 			ACLDAO.instance().deletePermissionsForAdmin(a);
 			VOMSAdminDAO.instance().delete(a);
 
 			HibernateFactory.commitTransaction();
 
 			log.info("Administrator '{},{}' removed", new String[] { a.getDn(),
 					a.getCa().getSubjectString() });
 
 		} catch (Throwable t) {
 
 			log.error("Error removing administrator!");
 			log.error(t.toString(), t);
 
 			System.exit(-1);
 		}
 	}
 
 	private void doAddAdmin() {
 
 		checkVoExistence();
 		if (adminDN == null || adminCA == null)
 			throw new VOMSException("adminDN or adminCA not set!");
 
 		HibernateFactory.beginTransaction();
 
 		try {
 
 			VOMSAdmin a = VOMSAdminDAO.instance().getByName(adminDN, adminCA);
 
 			if (a != null) {
 
 				log.info("Admin '" + a.getDn() + "," + a.getCa().getDn()
 						+ "' already exists in database...");
 				log.warn("This admin will be granted full privileges on the VOMS database.");
 			} else {
 
 				log.info("Admin '" + adminDN + "," + adminCA
 						+ "' not found. It will be created...");
 				// Admin does not exist, create it!
 				a = VOMSAdminDAO.instance().create(adminDN, adminCA,
 						adminEmailAddress);
 			}
 
 			Iterator i = VOMSGroupDAO.instance().findAll().iterator();
 
 			while (i.hasNext()) {
 
 				VOMSGroup g = (VOMSGroup) i.next();
 				g.getACL()
 						.setPermissions(a, VOMSPermission.getAllPermissions());
 				log.info("Adding ALL permissions on '{}' for admin '{},{}'",
 						new String[] { g.toString(), a.getDn(),
 								a.getCa().getSubjectString() });
 
 				Iterator rolesIter = VOMSRoleDAO.instance().findAll()
 						.iterator();
 				while (rolesIter.hasNext()) {
 
 					VOMSRole r = (VOMSRole) rolesIter.next();
 					r.getACL(g).setPermissions(a,
 							VOMSPermission.getAllPermissions());
 					log.info(
 							"Adding ALL permissions on role '{}/{}' for admin '{},{}'",
 							new String[] { g.toString(), r.toString(),
 									a.getDn(), a.getCa().getSubjectString() });
 					HibernateFactory.getSession().save(r);
 				}
 				HibernateFactory.getSession().save(g);
 
 			}
 
 			HibernateFactory.commitTransaction();
 
 		} catch (Throwable t) {
 
 			log.error("Error adding new administrator!");
 			log.error(t.toString(), t);
 
 			System.exit(-1);
 		}
 
 	}
 
 	private String getVOConfigurationDir() {
 		Properties sysconfProps = SysconfigUtil.loadSysconfig();
 
 		String confDir = sysconfProps
 				.getProperty(SysconfigUtil.SYSCONFIG_CONF_DIR);
 
 		if (confDir == null)
 			confDir = "/etc/voms-admin";
 
 		return confDir;
 
 	}
 
 	private boolean isVoConfigured(String voName) {
 
 		String confDir = getVOConfigurationDir();
 
 		File voConfDir = new File(confDir + "/" + voName);
 
 		if (voConfDir.exists() && voConfDir.isDirectory())
 			return true;
 
 		return false;
 
 	}
 
 	private Configuration loadHibernateConfiguration() {
 
 		Configuration cfg;
 		
 		if (hibernatePropertiesFile == null) {
 			
 			cfg = DBUtil.loadHibernateConfiguration(getVOConfigurationDir(), vo);
 			
 		}else{
 			
 			cfg = DBUtil.loadHibernateConfiguration(hibernatePropertiesFile); 
 		}
 
 		dialect = Dialect.getDialect(cfg.getProperties());
 		
 		return cfg;
 	}
 
 	private void doUndeploy() {
 
 		checkVoExistence();
 
 		log.info("Undeploying voms database...");
 		Configuration hibernateConfig = loadHibernateConfiguration();
 
 		int existingDB = checkDatabaseExistence();
 
 		if (existingDB == 1) {
 			log.error("This tool cannot undeploy voms-admin 1.2.x database! Please upgrade to voms-admin 2 or use voms-admin-configure 1.2.x tools to undeploy this database.");
 			System.exit(-1);
 		}
 
 		if (existingDB == 2) {
 
 			log.error("This tool cannot undeploy voms-admin 2.0.x databases! Please either upgrade the database to voms-admin 2.5 (using this tool) or use voms-admin-configure 2.0.x"
 					+ " tools to undeploy this database");
 
 			System.exit(-1);
 		}
 		if (existingDB < 0) {
 
 			log.error("No voms-admin database found!");
 			System.exit(-1);
 		}
 
 		checkDatabaseWritable();
 
 		SchemaExport export = new SchemaExport(hibernateConfig);
 
 		export.drop(false, true);
 
 		List l = export.getExceptions();
 
 		if (!l.isEmpty()) {
 			log.error("Error undeploying voms database!");
 			printExceptions(l);
 			System.exit(2);
 		}
 
 		log.info("Database undeployed correctly!");
 
 	}
 
 	private void checkVoExistence() {
 
 		if (!isVoConfigured(vo)) {
 			log.error("VO {} is not configured on this host.", vo);
 			System.exit(1);
 		}
 
 	}
 
 	private void doGrantROAccess() {
 		try {
 
 			checkVoExistence();
 
 			checkDatabaseWritable();
 
 			HibernateFactory.beginTransaction();
 
 			VOMSAdmin anyUserAdmin = VOMSAdminDAO.instance()
 					.getAnyAuthenticatedUserAdmin();
 
 			VOMSPermission readOnlyPerms = VOMSPermission.getEmptyPermissions()
 					.setContainerReadPermission().setMembershipReadPermission();
 
 			List<VOMSGroup> groups = VOMSGroupDAO.instance().findAll();
 
 			for (VOMSGroup g : groups) {
 
 				g.getACL().setPermissions(anyUserAdmin, readOnlyPerms);
 
 				log.info(
 						"Granting read-only access to any authenticated user on group '{}'",
 						g.getName());
 
 				List<VOMSRole> roles = VOMSRoleDAO.instance().findAll();
 
 				for (VOMSRole r : roles) {
 
 					r.getACL(g).setPermissions(anyUserAdmin, readOnlyPerms);
 					log.info(
 							"Granting read-only access to any authenticated user on role '{}/{}'",
 							new String[] { g.toString(), r.toString() });
 
 					HibernateFactory.getSession().save(r);
 
 				}
 
 				HibernateFactory.getSession().save(g);
 			}
 
 			HibernateFactory.commitTransaction();
 
 		} catch (Throwable t) {
 
 			log.error("Error creating read-only access grants!");
 			log.error(t.toString(), t);
 
 			System.exit(-1);
 		}
 
 	}
 
 	private void doDeploy() {
 
 		checkVoExistence();
 
 		Configuration hibernateConfig = loadHibernateConfiguration();
 
 		int existingDb = checkDatabaseExistence();
 
 		if (existingDb > 0) {
 			log.warn("Existing voms database found. Will not overwrite the database!");
 			System.exit(0);
 		}
 
 		checkDatabaseWritable();
 
 		SchemaExport exporter = new SchemaExport(hibernateConfig);
 
 		exporter.execute(true, true, false, true);
 
 		log.info("Deploying voms database...");
 
 		List l = exporter.getExceptions();
 
 		if (!l.isEmpty()) {
 			log.error("Error deploying voms database!");
 			printExceptions(l);
 			System.exit(2);
 
 		}
 
 		UpdateCATask caTask = new UpdateCATask();
 		caTask.run();
 
 		DatabaseSetupTask task = DatabaseSetupTask.instance();
 		task.run();
 
 		HibernateFactory.commitTransaction();
 		log.info("Database deployed correctly!");
 
 	}
 
 	protected void setupCLParser() {
 
 		options = new Options();
 
 		options.addOption(OptionBuilder.withLongOpt("help")
 				.withDescription("Displays helps and exits.").create("h"));
 
 		options.addOption(OptionBuilder
 				.withLongOpt("command")
 				.withDescription(
 						"Specifies the command to be executed: deploy,undeploy,upgrade,add-admin")
 				.hasArg().create("command"));
 
 		options.addOption(OptionBuilder.withLongOpt("vo")
 				.withDescription("Specifies the vo name.").hasArg()
 				.create("vo"));
 
 		options.addOption(OptionBuilder
 				.withLongOpt("config")
 				.withDescription(
 						"Specifies the hibernate config file to be used.")
 				.hasArg().create("config"));
 
 		options.addOption(OptionBuilder
 				.withLongOpt("properties")
 				.withDescription(
 						"Specifies the hibernate properties file to be used.")
 				.hasArg().create("properties"));
 
 		options.addOption(OptionBuilder
 				.withLongOpt("dn")
 				.withDescription(
 						"Specifies the dn for the admin to add (valid only if add-admin command is given).")
 				.hasArg().create("dn"));
 
 		options.addOption(OptionBuilder
 				.withLongOpt("ca")
 				.withDescription(
 						"Specifies the ca for the admin to add (valid only if add-admin command is given).")
 				.hasArg().create("ca"));
 
 		options.addOption(OptionBuilder
 				.withLongOpt("email")
 				.withDescription(
 						"Specifies the email address for the admin to add (valid only if add-admin command is given).")
 				.hasArg().create("email"));
 
 	}
 
 	protected void checkArguments(String[] args) {
 
 		try {
 
 			CommandLine line = parser.parse(options, args);
 
 			if (line.hasOption("h"))
 
 				printHelpMessageAndExit(0);
 
 			if (!line.hasOption("command")) {
 
 				System.err.println("No command specified!");
 				printHelpMessageAndExit(1);
 
 			}
 
 			if (!line.hasOption("vo")) {
 				System.err.println("No vo specified!");
 				printHelpMessageAndExit(1);
 			}
 
 			command = line.getOptionValue("command");
 
 			// FIXME: use an Enumeration for the commands!!
 			if (!command.equals("deploy") && !command.equals("upgrade")
 					&& !command.equals("add-admin")
 					&& !command.equals("remove-admin")
 					&& !command.equals("undeploy")
 					&& !command.equals("upgrade-script")
 					&& !command.equals("check-connectivity")
 					&& !command.equals("grant-read-only-access")) {
 
 				System.err.println("Unknown command specified: " + command);
 				printHelpMessageAndExit(2);
 			}
 
 			vo = line.getOptionValue("vo");
 
 			if (line.hasOption("hb-config"))
 				hibernateConfigFile = line.getOptionValue("hb-config");
 
 			if (line.hasOption("hb-properties"))
 				hibernatePropertiesFile = line.getOptionValue("hb-properties");
 
 			if (line.hasOption("dn"))
 				adminDN = line.getOptionValue("dn");
 
 			if (line.hasOption("ca"))
 				adminCA = line.getOptionValue("ca");
 
 			if (line.hasOption("email"))
 				adminEmailAddress = line.getOptionValue("email");
 
 		} catch (ParseException e) {
 
 			throw new VOMSException("Error parsing command-line arguments: "
 					+ e.getMessage(), e);
 
 		}
 
 	}
 
 	private void printHelpMessageAndExit(int exitStatus) {
 
 		helpFormatter.printHelp("SchemaDeployer", options);
 		System.exit(exitStatus);
 
 	}
 
 	private int dropTable(String tableName) {
 
 		Session s = HibernateFactory.getSession();
 		String command = "drop table " + tableName;
 		return s.createSQLQuery(command).executeUpdate();
 	}
 
 	private void dropUnusedTables_2_0_x() {
 
 		String[] tableNames = { "admins_history", "history" };
 
 		for (String table : tableNames)
 			dropTable(table);
 
 	}
 
 	private void dropOldTables_2_0_x() {
 
 		DatabaseMetaData md = null;
 
 		try {
 
 			md = HibernateFactory.getSession().connection().getMetaData();
 
 		} catch (Throwable t) {
 			log.error("Error accessing database metadata!", t);
 			System.exit(-1);
 		}
 
 		ResultSet oldTables = getTableNamesMatchingPattern(md, "%_old");
 		ArrayList<String> toBeDropped = new ArrayList<String>();
 
 		try {
 
 			while (oldTables.next())
 				toBeDropped.add(oldTables.getString("TABLE_NAME"));
 
 		} catch (SQLException e) {
 			log.error("Error reading table names from database metadata!", e);
 			System.exit(2);
 		}
 
 		for (String tableName : toBeDropped) {
 			log.debug("Dropping '" + tableName + "'...");
 			dropTable(tableName);
 		}
 
 	}
 
 	private void dropOldTables_1_2_19() {
 		String[] dTables = new String[] { "acl_old", "acld", "admins_old",
 				"attributes_old", "ca_old", "capabilities_old",
 				"capabilitiesd", "group_attrs_old", "groups_old", "groupsd",
 				"m_old", "md", "periodicity", "realtime", "requests_old",
 				"role_attrs_old", "roles_old", "rolesd", "seqnumber_old",
 				"sequences", "usr_old", "usr_attrs_old", "usrd", "version_old",
 				"validity" };
 
 		for (int i = 0; i < dTables.length; i++)
 			dropTable(dTables[i]);
 
 	}
 
 	private int dropSequence(String sequenceName) {
 
 		log.debug("Dropping sequence " + sequenceName);
 		Session s = HibernateFactory.getSession();
 		String command = "drop sequence " + sequenceName;
 
 		try {
 			return s.createSQLQuery(command).executeUpdate();
 
 		} catch (HibernateException e) {
 			if (e.getCause().getMessage().contains("sequence does not exist")) {
 				log.warn("Error dropping sequence: " + sequenceName
 						+ "... such sequence doesn't exist.");
 				log.warn("This error may be ignored at this stage of the database upgrade...");
 			}
 			return 0;
 		}
 	}
 
 	private void dropOldSequences() {
 		log.info("Dropping old sequences...");
 
 		String[] oldSequences = new String[] { "voms_seq_ca",
 				"voms_seq_transaction", "voms_seq_admin", "voms_seq_acl",
 				"voms_seq_role", "voms_seq_group", "voms_seq_user" };
 
 		for (int i = 0; i < oldSequences.length; i++)
 			dropSequence(oldSequences[i]);
 
 	}
 
 	private int renameTable(String tableName) {
 
 		Session s = HibernateFactory.getSession();
 		String command = "alter table " + tableName + " rename to " + tableName
 				+ "_old";
 		return s.createSQLQuery(command).executeUpdate();
 	}
 
 	private void renameTables_1_2_19() {
 
 		String[] oldTables = new String[] { "ca", "acl", "admins",
 				"attributes", "capabilities", "group_attrs", "groups",
 				"role_attrs", "roles", "seqnumber", "usr", "usr_attrs", "m",
 				"requests", "version" };
 
 		for (int i = 0; i < oldTables.length; i++)
 			renameTable(oldTables[i]);
 
 	}
 
 	// See bug https://savannah.cern.ch/bugs/?36291
 	private void fixHibernateSequence() {
 		log.info("Migrating sequences since on oracle backend...");
 		Session s = HibernateFactory.getSession();
 
 		Long maxSeqValue = (Long) s
 				.createSQLQuery(
 						"select max(last_number) as max from user_sequences where sequence_name like 'VOMS_%'")
 				.addScalar("max", new LongType()).uniqueResult();
 
 		// Recreate hibernate sequence
 		String dropHibSeqStatement = "drop sequence HIBERNATE_SEQUENCE";
 		String createHibSeqStatement = "create sequence HIBERNATE_SEQUENCE MINVALUE 1 MAXVALUE 999999999999999999999999999 "
 				+ "INCREMENT BY 1 START WITH "
 				+ maxSeqValue
 				+ " CACHE 20 NOORDER NOCYCLE";
 
 		s.createSQLQuery(dropHibSeqStatement).executeUpdate();
 		s.createSQLQuery(createHibSeqStatement).executeUpdate();
 		log.info("Sequences migration complete.");
 	}
 
 	private void renameTables_2_0_x() {
 
 		String[] tables20x = { "acl2", "acl2_permissions", "admins",
 				"admins_history", "attributes", "ca", "capabilities",
 				"group_attrs", "groups", "history", "m", "memb_req",
 				"role_attrs", "roles", "seqnumber", "usr", "usr_attrs",
 				"version" };
 
 		for (String tableName : tables20x)
 			renameTable(tableName);
 
 	}
 
 	private void executeAndLog(String command) {
 
 		log.info("Executing '" + command + "'");
 
 		HibernateFactory.getSession().createSQLQuery(command).executeUpdate();
 
 	}
 
 	private void fixCaTable() throws HibernateException, SQLException {
 
 		executeAndLog("update ca set subject_string = ca");
 
 		// set creation time
 		HibernateFactory.getSession()
 				.createSQLQuery("update ca set creation_time = :creationTime")
 				.setTimestamp("creationTime", new Date()).executeUpdate();
 
 		dropColumn("ca", "ca");
 		setColumnNullability(false, "ca", "subject_string", getVarcharType()
 				+ "(255)");
 		setColumnNullability(false, "ca", "creation_time", getTimestampType());
 
 	}
 
 	private String getColumnType(String tableName, String columnName)
 			throws HibernateException, SQLException {
 
 		DatabaseMetaData md = HibernateFactory.getSession().connection()
 				.getMetaData();
 
 		ResultSet columnData = md.getColumns("%", "%", tableName, columnName);
 
 		int matches = 0;
 
 		while (columnData.next()) {
 			matches++;
 			String typeName = columnData.getString("TYPE_NAME");
 			int colSize = columnData.getInt("COLUMN_SIZE");
 
 			if (colSize > 0)
 				typeName = typeName + "(" + colSize + ")";
 
 			log.debug(String.format("%s.%s type:%s colSize:%s", tableName,
 					columnName, typeName, colSize));
 			return typeName;
 
 		}
 
 		return null;
 
 	}
 
 	private void setColumnNullability(boolean nullable, String tableName,
 			String columnName, String typeName) {
 
 		String nullString = (nullable ? "" : "not");
 
 		String command = String.format("alter table %s modify %s %s %s null",
 				tableName, columnName, typeName, nullString);
 		executeAndLog(command);
 	}
 
 	private void dropColumn(String tableName, String columnName) {
 
 		executeAndLog(String.format("alter table %s drop column %s", tableName,
 				columnName));
 
 	}
 
 	private List<String> getForeignKeyContraintNamesOnColumn(String tableName,
 			String columnName) throws SQLException {
 		DatabaseMetaData md = HibernateFactory.getSession().connection()
 				.getMetaData();
 
 		ResultSet rs = md.getImportedKeys(null, null, tableName);
 		ArrayList<String> res = new ArrayList<String>();
 
 		while (rs.next()) {
 
 			String importedPkTableName = rs.getString("PKTABLE_NAME");
 			String importedPkColumnName = rs.getString("PKCOLUMN_NAME");
 
 			String fkName = rs.getString("FK_NAME");
 			String pkName = rs.getString("PK_NAME");
 			res.add(fkName);
 
 		}
 
 		return res;
 	}
 
 	private void updateACLPerms() {
 		// Grant all permissions to those that had all permissions in 2.0.x!
 		executeAndLog("update acl2_permissions set permissions = 16383 where permissions = 4095");
 	}
 
 	private void dropOrphanedAdministrators() {
 
 		List<VOMSAdmin> orphanedAdmins = ACLDAO.instance()
 				.getAdminsWithoutActivePermissions();
 
 		for (VOMSAdmin a : orphanedAdmins) {
 			log.info("Dropping orphaned administrator '{}' - email: '{}'",
 					a.getDn(), a.getEmailAddress());
 			HibernateFactory.getSession().delete(a);
 		}
 
 	}
 
 	private void fixUsrTable() throws HibernateException, SQLException {
 
 		// Move email addresses in new column
 		executeAndLog("update usr set email_address = mail");
 
 		// Drop old email field
 		dropColumn("usr", "mail");
 		dropColumn("usr", "cn");
 		dropColumn("usr", "cauri");
 
 		// Fix missing null checks
 		setColumnNullability(false, "usr", "creation_time", getTimestampType());
 		setColumnNullability(false, "usr", "end_time", getTimestampType());
 
 		// Drop nullability from old dn and ca fields
 
 		setColumnNullability(true, "usr", "dn", getVarcharType() + "(255)");
 
 		if (!isOracleBackend())
 			setColumnNullability(true, "usr", "ca", "smallint");
 
 		// Drop foreign key created by 2.0.x installation,
 		// otherwise an undeploy would not perform cleanly
 		String dropForeignKeyString = dialect.getDropForeignKeyString();
 
 		executeAndLog("alter table usr " + dropForeignKeyString + " fk_usr_ca");
 
 		// Set dn null for usr
 		executeAndLog("update usr set dn = null");
 		executeAndLog("update usr set ca = null");
 	}
 
 	private void migrateUsrTable() {
 
 		CertificateDAO certDAO = CertificateDAO.instance();
 
 		Iterator userIterator = HibernateFactory.getSession()
 				.createQuery("select u, u.dn, u.ca from VOMSUser u").iterate();
 
 		while (userIterator.hasNext()) {
 
 			Object[] result = (Object[]) userIterator.next();
 			VOMSUser u = (VOMSUser) result[0];
 			String dn = (String) result[1];
 			VOMSCA ca = (VOMSCA) result[2];
 
 			Certificate candidateCert = certDAO.findByDNCA(dn,
 					ca.getSubjectString());
 			if (candidateCert != null) {
 
 				log.warn("**** WARNING *****");
 				log.warn(
 						"There is a duplicated entry in the database for user: '{}','{}'",
 						dn, ca.getSubjectString());
 				log.warn("The duplicated entry will be REMOVED.\n");
 
 				HibernateFactory.getSession()
 						.createSQLQuery("delete from m where userid = :id")
 						.setLong("id", u.getId()).executeUpdate();
 				HibernateFactory
 						.getSession()
 						.createSQLQuery(
 								"delete from usr_attrs where u_id = :id")
 						.setLong("id", u.getId()).executeUpdate();
 				HibernateFactory.getSession()
 						.createSQLQuery("delete from usr where userid = :id")
 						.setLong("id", u.getId()).executeUpdate();
 
 				continue;
 			}
 
 			candidateCert = certDAO.create(u, ca.getSubjectString());
 			u.addCertificate(candidateCert);
 
 			u.setEmailAddress("temporary_value");
 			u.setCreationTime(new Date());
 
 			Calendar c = Calendar.getInstance();
 			c.setTime(u.getCreationTime());
 
 			// Default lifetime for membership is 12 months if not specified
 			int lifetime = VOMSConfiguration.instance().getInt(
 					VOMSConfigurationConstants.DEFAULT_MEMBERSHIP_LIFETIME, 12);
 
 			c.add(Calendar.MONTH, lifetime);
 			u.setEndTime(c.getTime());
 
 			HibernateFactory.getSession().save(u);
 		}
 
 	}
 
 	private void migrateDbContents() {
 
 		log.info("Migrating db contents...");
 
 		Session s = HibernateFactory.getSession();
 
 		s.createSQLQuery(
 				"insert into ca (cid, ca, cadescr) select cid, ca, cadescr from ca_old")
 				.executeUpdate();
 		s.createSQLQuery(
 				"insert into admins(adminid,dn,ca) select adminid, dn,ca from admins_old")
 				.executeUpdate();
 		s.createSQLQuery(
 				"insert into groups(gid,dn,parent,must) select gid,dn,parent,must from groups_old")
 				.executeUpdate();
 		s.createSQLQuery(
 				"insert into roles(rid,role) select rid, role from roles_old")
 				.executeUpdate();
 		s.createSQLQuery(
 				"insert into usr(userid,dn,ca,cn,mail,cauri) select userid, dn, ca, cn, mail, cauri from usr_old")
 				.executeUpdate();
 		
 		// Fix for http://issues.cnaf.infn.it/browse/VOMS-76
 		s.createSQLQuery("update usr set suspended = false").executeUpdate();
 
 		s.createSQLQuery("insert into version values('3')").executeUpdate();
 
 		// Generic attributes migration
 		s.createSQLQuery(
 				"insert into attributes(a_id, a_name, a_desc) select a_id,a_name,a_desc from attributes_old")
 				.executeUpdate();
 		s.createSQLQuery(
 				"insert into usr_attrs(u_id,a_id,a_value) select u_id,a_id,a_value from usr_attrs_old")
 				.executeUpdate();
 		s.createSQLQuery(
 				"insert into group_attrs(g_id,a_id,a_value) select g_id,a_id,a_value from group_attrs_old")
 				.executeUpdate();
 		s.createSQLQuery(
 				"insert into role_attrs(r_id,g_id,a_id,a_value) select r_id, g_id,a_id,a_value from role_attrs_old")
 				.executeUpdate();
 
 		// Seqnumber migration
 		s.createSQLQuery(
 				"insert into seqnumber(seq) select seq from seqnumber_old")
 				.executeUpdate();
 	}
 
 	private void migrateMappings() {
 
 		Session s = HibernateFactory.getSession();
 
 		List oldMappings = s
 				.createSQLQuery("select userid,gid,rid,cid from m_old")
 				.addScalar("userid", new LongType())
 				.addScalar("gid", new LongType())
 				.addScalar("rid", new LongType())
 				.addScalar("cid", new LongType()).list();
 
 		Iterator i = oldMappings.iterator();
 
 		VOMSUserDAO dao = VOMSUserDAO.instance();
 
 		while (i.hasNext()) {
 
 			Object[] result = (Object[]) i.next();
 
 			if (result == null)
 				break;
 
 			VOMSUser u = dao.findById((Long) result[0]);
 			VOMSGroup g = VOMSGroupDAO.instance().findById((Long) result[1]);
 			VOMSRole r = null;
 
 			if (result[2] != null)
 				r = VOMSRoleDAO.instance().findById((Long) result[2]);
 
 			log.debug("Mapping: " + u + "," + g + "," + r);
 
 			if (r == null) {
 				if (!u.isMember(g))
 					dao.addToGroup(u, g);
 
 			} else {
 				if (!u.isMember(g))
 					dao.addToGroup(u, g);
 
 				VOMSUserDAO.instance().assignRole(u, g, r);
 
 			}
 
 			s.save(u);
 		}
 
 	}
 
 	private void removeDuplicatedACLEntries() {
 		log.info("Removing eventual buggy duplicated ACL entries... ");
 
 		Session s = HibernateFactory.getSession();
 		s.createSQLQuery(
 				"delete from admins_old where dn not like '/O=VOMS/%' and adminid not in (select adminid from acl_old)")
 				.executeUpdate();
 
 	}
 
 	private void migrateACLs() {
 
 		Iterator adminIter = VOMSAdminDAO.instance().getAll().iterator();
 
 		while (adminIter.hasNext()) {
 
 			VOMSAdmin a = (VOMSAdmin) adminIter.next();
 
 			long adminId = a.getId().longValue();
 
 			if ((a.getDn().equals(VOMSServiceConstants.ANYUSER_ADMIN))
 					|| (!a.getDn().startsWith("/O=VOMS"))) {
 
 				log.debug("Migrating acls for admin : " + a.getDn());
 
 				MultiHashMap m = loadDefaultACLEntriesForAdmin(adminId);
 
 				if (m != null) {
 
 					Iterator keys = m.keySet().iterator();
 
 					while (keys.hasNext()) {
 
 						List perms = (List) m.get(keys.next());
 						setGlobalPermission(a,
 								ACLMapper.translatePermissions(perms));
 
 					}
 				}
 
 				m = loadGroupACLEntriesForAdmin(adminId);
 
 				if (m != null) {
 
 					Iterator keys = m.keySet().iterator();
 
 					while (keys.hasNext()) {
 
 						Long groupId = (Long) keys.next();
 
 						List perms = (List) m.get(groupId);
 
 						VOMSGroup targetGroup = VOMSGroupDAO.instance()
 								.findById(groupId);
 
 						targetGroup.getACL().setPermissions(a,
 								ACLMapper.translatePermissions(perms));
 
 					}
 
 				}
 
 				m = loadRoleACLEntriesForAdmin(adminId);
 
 				if (m != null) {
 
 					Iterator keys = m.keySet().iterator();
 
 					while (keys.hasNext()) {
 
 						Long roleId = (Long) keys.next();
 						List perms = (List) m.get(roleId);
 
 						VOMSRole targetRole = VOMSRoleDAO.instance().findById(
 								roleId);
 						setPermissionOnRole(a, targetRole,
 								ACLMapper.translatePermissions(perms));
 					}
 				}
 			}
 		}
 
 	}
 
 	public static void main(String[] args) throws ConfigurationException {
 
 		new SchemaDeployer(args);
 
 	}
 
 	private MultiHashMap buildACLEntries(List acl) {
 
 		if (acl.isEmpty())
 			return null;
 
 		MultiHashMap map = new MultiHashMap();
 
 		Iterator i = acl.iterator();
 
 		while (i.hasNext()) {
 
 			Object[] res = (Object[]) i.next();
 			map.put(res[0], res[1]);
 
 		}
 
 		return map;
 
 	}
 
 	private MultiHashMap loadGroupACLEntriesForAdmin(long adminid) {
 
 		Session s = HibernateFactory.getSession();
 
 		String query = "select groups.gid as gid, acl.operation as operation from acl_old acl, groups_old groups where acl.aid = groups.aclid and acl.allow = 1 and adminid = :adminId";
 
 		List acls = s.createSQLQuery(query).addScalar("gid", new LongType())
 				.addScalar("operation", new ShortType())
 				.setLong("adminId", adminid).list();
 
 		return buildACLEntries(acls);
 
 	}
 
 	private MultiHashMap loadDefaultACLEntriesForAdmin(long adminid) {
 
 		Session s = HibernateFactory.getSession();
 
 		String query = "select -1 as gid, acl.operation as operation from acl_old acl, groups_old groups where acl.aid = 0 and acl.allow = 1 and adminid = :adminId";
 
 		List acls = s.createSQLQuery(query).addScalar("gid", new LongType())
 				.addScalar("operation", new ShortType())
 				.setLong("adminId", adminid).list();
 
 		return buildACLEntries(acls);
 	}
 
 	private MultiHashMap loadRoleACLEntriesForAdmin(long adminid) {
 
 		Session s = HibernateFactory.getSession();
 
 		String query = "select roles.rid as rid, acl.operation as operation from acl_old acl, roles_old roles where acl.aid = roles.aclid and acl.allow = 1 and adminid = :adminId";
 
 		List acls = s.createSQLQuery(query).addScalar("rid", new LongType())
 				.addScalar("operation", new ShortType())
 				.setLong("adminId", adminid).list();
 
 		return buildACLEntries(acls);
 	}
 
 	private void setPermissionOnRole(VOMSAdmin a, VOMSRole r, VOMSPermission p) {
 
 		log.debug("Setting permissions " + p.getCompactRepresentation()
 				+ " for admin " + a.getDn() + " on role " + r.getName());
 		List groups = VOMSGroupDAO.instance().findAll();
 
 		Iterator groupIter = groups.iterator();
 
 		while (groupIter.hasNext()) {
 
 			VOMSGroup g = (VOMSGroup) groupIter.next();
 			ACL roleACL = r.getACL(g);
 			if (roleACL == null) {
 				roleACL = new ACL(g, r, false);
 				roleACL.setPermissions(a, p);
 				r.getAcls().add(roleACL);
 			} else
 				roleACL.setPermissions(a, p);
 		}
 
 	}
 
 	private void setGlobalPermission(VOMSAdmin a, VOMSPermission p) {
 
 		log.debug("Setting global permissions " + p.getCompactRepresentation()
 				+ " for admin " + a.getDn());
 		List groups = VOMSGroupDAO.instance().findAll();
 		List roles = VOMSRoleDAO.instance().getAll();
 
 		Iterator groupIter = groups.iterator();
 
 		while (groupIter.hasNext()) {
 
 			VOMSGroup g = (VOMSGroup) groupIter.next();
 
 			ACL acl = g.getACL();
 
 			if (acl == null) {
 				acl = new ACL(g, false);
 				acl.setPermissions(a, p);
 				g.getAcls().add(acl);
 			} else
 				acl.setPermissions(a, p);
 
 			Iterator roleIter = roles.iterator();
 
 			while (roleIter.hasNext()) {
 
 				VOMSRole r = (VOMSRole) roleIter.next();
 				ACL roleACL = r.getACL(g);
 
 				if (roleACL == null) {
 					roleACL = new ACL(g, r, false);
 					roleACL.setPermissions(a, p);
 					r.getAcls().add(roleACL);
 				} else
 					roleACL.setPermissions(a, p);
 			}
 		}
 	}
 
 }
