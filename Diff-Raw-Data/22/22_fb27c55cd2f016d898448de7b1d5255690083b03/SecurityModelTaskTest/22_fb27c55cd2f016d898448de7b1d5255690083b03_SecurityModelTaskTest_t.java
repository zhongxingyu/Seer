 package net.codjo.test.release.task.security;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 import static net.codjo.database.common.api.confidential.DatabaseTranscoder.LONGVARCHAR;
 import static net.codjo.database.common.api.confidential.DatabaseTranscoder.INTEGER;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.Assert;
 import net.codjo.database.common.api.DatabaseFactory;
 import net.codjo.database.common.api.DatabaseScriptHelper;
 import net.codjo.database.common.api.JdbcFixture;
 import net.codjo.database.common.api.structure.SqlFieldDefinition;
 import net.codjo.database.common.api.structure.SqlTable;
 import net.codjo.database.common.api.structure.SqlTableDefinition;
 import net.codjo.test.common.FileComparator;
 import net.codjo.test.common.LogString;
 import net.codjo.test.common.fixture.DirectoryFixture;
 import net.codjo.test.release.TestEnvironment;
 import net.codjo.test.release.TestEnvironmentMock;
 import net.codjo.test.release.task.util.RemoteCommandMock;
 import net.codjo.util.file.FileUtil;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 public class SecurityModelTaskTest {
     private JdbcFixture jdbc;
     private SecurityModelTask task;
     private DirectoryFixture directoryFixture = DirectoryFixture.newTemporaryDirectoryFixture(
           "SecurityModelTask");
     private LogString log = new LogString();
 
 
     @Before
     public void setUp() throws Exception {
         jdbc = new DatabaseFactory().createJdbcFixture();
         jdbc.doSetUp();
        directoryFixture.doSetUp();
         createSecurityTable();
         createTask();
 
         task.setRemoteCommand(new FtpCommandMock(log));
     }
 
 
     @After
     public void tearDown() throws Exception {
         jdbc.doTearDown();
         directoryFixture.doTearDown();
     }
 
 
     @Test
     public void test_modeRemote() {
         task.getProject().setProperty("agf.test.remote", "YES");
         task.getProject().setProperty("ftp.remoteDir", "//BALBALBAL//IN");
 
         task.setUser("  smith  ");
         task.setRoles(" guest , star  ");
 
         //En mode DatabaseStorage --> pas de remote
         task.execute();
         log.assertContent("");
 
         //On passe en mode FileStorage
         task.getProject().setProperty(SecurityModelTask.SECURITY_MODEL_STORAGE_TYPE, "file");
         File securityConfigFile = new File(directoryFixture.getAbsolutePath(), "security.xml");
 
         task.getProject()
               .setProperty(SecurityModelTask.SECURITY_MODEL_STORAGE_FILE,
                            securityConfigFile.getAbsolutePath());
         task.getProject()
               .setProperty(SecurityModelTask.SECURITY_MODEL_STORAGE_FILE + ".remote",
                            "/TOTO/DAT/IN/security.xml");
         task.execute();
         log.assertContent("FtpCommandMock.execute(/TOTO/DAT/IN)");
 
         try {
             task.determineRemoteRepository();
         }
         catch (IOException e) {
             e.printStackTrace();  // Todo
         }
     }
 
 
     @Test
     public void test_fileStorageConfigurationNotComplete() throws Exception {
         task.setUser("  smith  ");
         task.setRoles(" guest , star  ");
 
         task.getProject().setProperty(SecurityModelTask.SECURITY_MODEL_STORAGE_TYPE, "file");
 
         try {
             task.execute();
             fail("Configuration is not complete");
         }
         catch (BuildException e) {
             assertEquals(
                   "L'attribut 'securityModel.storageFile' est obligatoire "
                   + "si l'attribut 'securityModel.storageType' est de type 'file'.",
                   e.getMessage());
         }
     }
 
 
     @Test
     public void test_loadModelFromFileStorage() throws Exception {
         File securityConfigFile = new File(directoryFixture.getAbsolutePath(), "security.xml");
 
         task.setUser("  smith  ");
         task.setRoles(" guest , star  ");
 
         task.getProject().setProperty(SecurityModelTask.SECURITY_MODEL_STORAGE_TYPE, "file");
 
         task.getProject()
               .setProperty(SecurityModelTask.SECURITY_MODEL_STORAGE_FILE,
                            securityConfigFile.getAbsolutePath());
         task.execute();
 
         FileComparator comparator = new FileComparator("*");
         File expectedFile = new File(directoryFixture.getAbsolutePath(), "security_expected.xml");
         String expectedContent = "<xml version=\"0\">"
                                  + "<model>"
                                  + "<grants>"
                                  + "<entry>"
                                  + "<user name='smith'/>"
                                  + "<list><role name='guest'/><role name='star'/></list>"
                                  + "</entry>"
                                  + "</grants>"
                                  + "</model>"
                                  + "</xml>";
         FileUtil.saveContent(expectedFile, expectedContent);
 
         Assert.assertTrue("Pb de comparaison", comparator.equals(expectedFile,
                                                                  securityConfigFile));
     }
 
 
     @Test
     public void test_loadModel() throws Exception {
         task.setUser("  smith  ");
         task.setRoles(" guest , star  ");
 
         task.execute();
 
         jdbc.assertContent(SqlTable.table("PM_SEC_MODEL"), new String[][]{
               {"0", "<model>"
                     + "<grants>"
                     + "<entry>"
                     + "<user name='smith'/>"
                     + "<list><role name='guest'/><role name='star'/></list>"
                     + "</entry>"
                     + "</grants>"
                     + "</model>"}
         });
     }
 
 
     @Test
     public void test_loadModel_withlogDate() throws Exception {
         task.setUser("  smith  ");
         task.setRoles(" guest , star  ");
         task.setLastLogin("2007-02-05 12:10:05");
         task.setLastLogout("2007-02-04 08:10:05");
 
         task.execute();
 
         jdbc.assertContent(SqlTable.table("PM_SEC_MODEL"), new String[][]{
               {"0", "<model>"
                     + "<grants>"
                     + "<entry>"
                     + "<user name='smith' lastLogin='05/02/2007 12:10:05' lastLogout='04/02/2007 08:10:05'/>"
                     + "<list><role name='guest'/><role name='star'/></list>"
                     + "</entry>"
                     + "</grants>"
                     + "</model>"}
         });
     }
 
 
     @Test
     public void test_loadModel_twice() throws Exception {
         task.setUser("smith");
         task.execute();
 
         task.setUser("neo");
         task.execute();
 
         jdbc.assertContent(SqlTable.table("PM_SEC_MODEL"), new String[][]{
               {"0", "<model>"
                     + "<grants>"
                     + "<entry>"
                     + "<user name='neo'/>"
                     + "<list/>"
                     + "</entry>"
                     + "</grants>"
                     + "</model>"}
         });
     }
 
 
     @Test
     public void test_loadModel_noUser() throws Exception {
         task.setUser(null);
 
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex) {
             assertEquals(SecurityModelTask.UNDEFINED_USER_ERROR, ex.getMessage());
         }
 
         task.setUser("  ");
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex) {
             assertEquals(SecurityModelTask.UNDEFINED_USER_ERROR, ex.getMessage());
         }
 
         jdbc.assertIsEmpty(SqlTable.table("PM_SEC_MODEL"));
     }
 
 
     @Test
     public void test_loadModel_noRole() throws Exception {
         task.setUser("smith");
 
         task.execute();
 
         jdbc.assertContent(SqlTable.table("PM_SEC_MODEL"), new String[][]{
               {"0", "<model>"
                     + "<grants>"
                     + "<entry>"
                     + "<user name='smith'/>"
                     + "<list/>"
                     + "</entry>"
                     + "</grants>"
                     + "</model>"}
         });
     }
 
 
     private void createSecurityTable() throws SQLException {
     	String tableName = "PM_SEC_MODEL";
         jdbc.drop(SqlTable.table(tableName));
         
 //        jdbc.create(SqlTable.table(tableName), "VERSION int  not null,"
 //                                                    + "MODEL  text null");
         SqlTableDefinition sqlTableDefinition = new SqlTableDefinition(tableName);
         List<SqlFieldDefinition> fields = Arrays.asList(new SqlFieldDefinition[]{
         		field("VERSION", INTEGER, true, null),
        		field("MODEL", LONGVARCHAR, false, null),
         });
         sqlTableDefinition.setSqlFieldDefinitions(fields);
 
         DatabaseFactory databaseFactory = new DatabaseFactory();        
         DatabaseScriptHelper sh = databaseFactory.createDatabaseScriptHelper();
         String sql = sh.buildCreateTableScript(sqlTableDefinition);
        executeScript(sql);
    }

    protected void executeScript(String scriptContent) throws SQLException {
        File scriptFile = new File(directoryFixture, "script.sql");
        try {
            FileUtil.saveContent(scriptFile, scriptContent);
        }
        catch (IOException e) {
            SQLException sqle = new SQLException(e.getMessage());
            sqle.initCause(e);
            throw sqle;
        }
        jdbc.advanced().executeCreateTableScriptFile(scriptFile);
     }
 
     private static SqlFieldDefinition field(String name, String type, boolean required, String precision) {
     	SqlFieldDefinition def = new SqlFieldDefinition(name);
     	def.setType(type);
     	def.setRequired(required);
     	def.setPrecision(precision);
     	return def;
     }
 
     private void createTask() {
         task = new SecurityModelTask();
         Project project = new Project();
         TestEnvironmentMock testEnvironmentMock = new TestEnvironmentMock(project);
         testEnvironmentMock.mockGetConnection(jdbc.getConnection());
         project.addReference(TestEnvironment.class.getName(), testEnvironmentMock);
         task.setProject(project);
     }
 
 
     class FtpCommandMock extends RemoteCommandMock {
         private LogString log;
 
 
         FtpCommandMock(LogString log) {
             super(log);
             this.log = log;
         }
 
 
         @Override
         public int execute() throws Exception {
             log.call("FtpCommandMock.execute", task.determineRemoteRepository());
             return 0;
         }
     }
 }
