 package net.straininfo2.grs.idloader.db;
 
 import net.straininfo2.grs.idloader.IntegrationTest;
 import net.straininfo2.grs.idloader.TargetIdExtractor;
 import net.straininfo2.grs.idloader.bioproject.domain.BioProject;
 import net.straininfo2.grs.idloader.bioproject.domain.Organism;
 import net.straininfo2.grs.idloader.bioproject.domain.mappings.Mapping;
 import net.straininfo2.grs.idloader.bioproject.domain.mappings.Provider;
 import net.straininfo2.grs.idloader.bioproject.eutils.MappingHandler;
 import net.straininfo2.grs.idloader.bioproject.xmlparsing.DocumentChunker;
 import net.straininfo2.grs.idloader.bioproject.xmlparsing.DomainConverter;
 import net.straininfo2.grs.idloader.bioproject.xmlparsing.DomainHandler;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.xml.sax.SAXException;
 
 import javax.annotation.Resource;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * Try to get hibernate working in this test.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:applicationContext-test.xml"})
 @Category(IntegrationTest.class)
 public class HibernateTest {
 
     @Autowired
     Configuration configuration;
 
     @Autowired
     SessionFactory factory;
 
     @Resource(name="bioProjectLoader")
     DomainHandler projectLoader;
 
     @Resource(name="mappingHandler")
     MappingHandler handler;
 
     @Test
     public void testHibernateConfigInjection() {
         assertNotNull(configuration);
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
 
     @Test
     public void tryLoadingBioProjectFile() throws ParserConfigurationException, IOException, SAXException, JAXBException {
         DocumentChunker.parseXmlFile(this.getClass().getClassLoader().getResource("bioproject.xml"), new DomainConverter(projectLoader));
         assertEquals(4L, factory.openSession().createQuery("select count(*) from BioProject").uniqueResult());
     }
 
     @Test
     public void testMergeBehaviour() {
         BioProject pr1 = new BioProject();
         Organism org = new Organism();
         org.setLabel("Some organism");
         pr1.updateOrganism(org);
         pr1.setProjectId(1L);
         // this is what spring does automatically with @Transactional
         Session session = factory.openSession();
         session.save(pr1);
         session.flush();// normally done by the transaction closing
         session.close();
         session = factory.openSession();
         BioProject pr2 = new BioProject();
         pr2.setProjectId(1L);
         org = new Organism();
         org.setLabel("Another organism");
         pr2.updateOrganism(org);
         session.merge(pr2);
         session.flush();
         session.close();
         session = factory.openSession();
         BioProject pr3 = (BioProject)session.get(BioProject.class, 1L);
         assertEquals("Another organism", pr3.retrieveOrganism().getLabel());
         assertEquals(1, (long) session.createQuery("select count(*) from Organism").uniqueResult());
         session.close();
     }
 
     @Test
     public void testMappingSave() {
         Session session = factory.openSession();
         BioProject project = new BioProject();
         project.setProjectId(1);
         session.persist(project);
         session.flush();
         session.close();
         Mapping mapping = new Mapping();
         Provider provider = new Provider();
         provider.setId(1);
         mapping.setProvider(provider);
         mapping.setUrl("http://example.org/");
         handler.addMapping(1, mapping, new TargetIdExtractor(-1));
        handler.endLoading();
         session = factory.openSession();
         project = (BioProject) session.load(BioProject.class, 1L);
         assertEquals("http://example.org/", project.getMappings().iterator().next().getUrl());
         session.close();
     }
 
     @After
     @SuppressWarnings("unchecked")
     public void clearDatabase() {
         Session session = factory.openSession();
         for (BioProject project : (List<BioProject>)session.createQuery("from BioProject").list()) {
             session.delete(project);
         }
         session.flush();
         session.close();
     }
 
 }
