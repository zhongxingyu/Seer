 package net.straininfo2.grs.idloader.db;
 
 import net.straininfo2.grs.idloader.IntegrationTest;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Try to get hibernate working in this test.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:applicationContext-test.xml"})
 @Category(IntegrationTest.class)
 public class HibernateTest {
 
     @Autowired
     Configuration configuration;
 
     @Test
     public void testHibernateConfigInjection() {
         assertNotNull(configuration);
         assertTrue(configuration instanceof Configuration);
     }
 
     /*
     This is not really a test, but it's a convenient way to generate an export of the
     schema.
      */
     @Test
     public void schemaExport() {
         SchemaExport export = new SchemaExport(configuration);
         export.setOutputFile("grs-schema.sql");
         export.setDelimiter(";");
        export.execute(false, false, false, true);
     }
 }
