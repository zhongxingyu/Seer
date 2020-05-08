 package com.jsmadja.wall.projectwall.it.service;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 import java.util.UUID;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.jsmadja.wall.projectwall.domain.Software;
 import com.jsmadja.wall.projectwall.service.HudsonService;
 import com.jsmadja.wall.projectwall.service.SoftwareNotCreatedException;
 import com.jsmadja.wall.projectwall.service.SoftwareNotFoundException;
 import com.jsmadja.wall.projectwall.service.SoftwareService;
 import com.jsmadja.wall.projectwall.service.SonarService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"/META-INF/spring/root-context.xml"})
 public class SoftwareServiceITTest {
 
     @Autowired
     SoftwareService softwareService;
 
    Logger LOG = LoggerFactory.getLogger(SoftwareServiceITTest.class);

     @Test
     public void should_find_all_software() throws SoftwareNotCreatedException {
         Software software1 = new Software("software1", HudsonService.class, true, true);
         Software software2 = new Software("software2", SonarService.class, true, true);
         softwareService.persist(software1);
         softwareService.persist(software2);
 
         List<Software> softwares = softwareService.findAll();
         assertEquals(2, softwares.size());
     }
 
     @Test
     public void should_persist_software() throws SoftwareNotFoundException, SoftwareNotCreatedException {
         String name = "hudson";
         String className = HudsonService.class.getName();
         boolean buildSoftware = true;
         boolean qualitySoftware = false;
         Software software = new Software(name, HudsonService.class, buildSoftware, qualitySoftware);
         softwareService.persist(software);
 
         Software persistedSoftware = softwareService.find(name);
 
         assertEquals(name, persistedSoftware.getName());
         assertEquals(className, persistedSoftware.getClassName());
         assertEquals(buildSoftware, persistedSoftware.isBuildSoftware());
         assertEquals(qualitySoftware, persistedSoftware.isQualitySoftware());
     }
 
     @Test(expected = NullPointerException.class)
     public void should_throw_exception_when_passing_null_to_persist() throws SoftwareNotCreatedException {
         softwareService.persist(null);
     }
 
     @Test
     public void should_find_a_software() throws SoftwareNotFoundException, SoftwareNotCreatedException {
         Software mysoftware = new Software("mysoftware", HudsonService.class, true, true);
         softwareService.persist(mysoftware);
         softwareService.find("mysoftware");
     }
 
     @Test(expected=SoftwareNotFoundException.class)
     public void should_throw_exception_when_searching_an_inexistant_software() throws SoftwareNotFoundException {
         softwareService.find("does.not.exist");
     }
 
     @Test(expected=NullPointerException.class)
     public void should_throw_exception_when_passing_null()  throws SoftwareNotFoundException {
         softwareService.find(null);
     }
 
     @Test(expected = Exception.class)
     public void should_throw_exception_when_trying_to_persist_already_existing_software() throws SoftwareNotCreatedException {
         String name = UUID.randomUUID().toString();
         softwareService.persist(new Software(name, HudsonService.class, true, true));
         softwareService.persist(new Software(name, HudsonService.class, true, true));
     }
 }
