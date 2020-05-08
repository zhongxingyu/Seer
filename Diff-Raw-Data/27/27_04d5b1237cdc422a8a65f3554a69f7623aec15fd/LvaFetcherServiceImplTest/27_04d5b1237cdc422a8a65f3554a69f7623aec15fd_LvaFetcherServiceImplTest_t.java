 package at.ac.tuwien.sepm.service.impl;
 
 import at.ac.tuwien.sepm.entity.Curriculum;
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.entity.Module;
 import at.ac.tuwien.sepm.exception.ServiceException;
 import at.ac.tuwien.sepm.service.LvaFetcherService;
 import at.ac.tuwien.sepm.service.LvaType;
 import at.ac.tuwien.sepm.service.Semester;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.util.Assert;
 
 import java.util.HashMap;
 import java.util.List;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:applicationContext-test.xml"})
 public class LvaFetcherServiceImplTest {
 
     @Autowired
     @Qualifier("lvaFetcherServiceImpl")
     LvaFetcherService lvaFetcherService;
 
 
     @Test
     public void testGetAcademicPrograms() throws Exception {
         List<Curriculum> curriculumList = lvaFetcherService.getAcademicPrograms();
         Assert.notNull(curriculumList);
         Assert.isTrue(curriculumList.get(0).getName().equals("Bachelorstudium Architektur"));
         Assert.isNull(curriculumList.get(0).getModules());
     }
 
     @Test
    @Ignore
     public void testGetAcademicProgramsRecursive() throws Exception {
         List<Curriculum> curriculumList = lvaFetcherService.getAcademicPrograms(true);
         Assert.notNull(curriculumList);
         Assert.isTrue(curriculumList.get(0).getName().equals("Bachelorstudium Architektur"));
         HashMap<Module,Boolean> modules = curriculumList.get(0).getModules();
         Assert.notNull(modules);
     }
 
     @Test
     public void testGetModulesFromSoftwareEngineering() throws Exception {
         List<Module> moduleList = lvaFetcherService.getModules("033 534");
         Assert.notNull(moduleList);
         Assert.isTrue(moduleList.get(0).getName().equals("Modul Algorithmen und Datenstrukturen"));
     }
 
     @Test
     public void testGetModulesFromSoftwareEngineeringRecursive() throws Exception {
         List<Module> moduleList = lvaFetcherService.getModules("033 534", true);
         Assert.notNull(moduleList.get(0));
         Module m = moduleList.get(0);
         Assert.notNull(m.getMetaLvas());
         List<MetaLVA> ml = m.getMetaLvas();
         ml.get(0).getName().equals("Algorithmen und Datenstrukturen 1");
     }
 
     @Test(expected = ServiceException.class)
     public void testGetModulesFromNonExisitingCurriculum() throws Exception {
         List<Module> moduleList = lvaFetcherService.getModules("033 111");
     }
 
     @Test(expected = ServiceException.class)
     public void testGetLvaNULL() throws Exception {
         lvaFetcherService.getLva(null, null);
     }
 
     @Test
     public void testGetLvaOOP() throws Exception {
         MetaLVA lva = lvaFetcherService.getLva("185.A01", "2012W");
         Assert.isTrue(lva.getName().equals("Objektorientierte Programmiertechniken"));
         Assert.isTrue(lva.getECTS() == 3.0f);
         Assert.isTrue(lva.getNr().equals("185.A01"));
         Assert.isTrue(lva.getType().equals(LvaType.VU));
         Assert.notNull(lva.getLVAs());
         Assert.isTrue(lva.getLVAs().size() == 1);
         LVA realLva = lva.getLVAs().get(0);
         Assert.isTrue(realLva.getSemester().equals(Semester.W));
         Assert.isTrue(realLva.getYear() == 2012);
         Assert.isTrue(realLva.getGoals().startsWith("Fachliche und methodische"));
         Assert.isTrue(realLva.getContent().startsWith("Überblick über das objektorientierte"));
         Assert.isTrue(realLva.getAdditionalInfo1().startsWith("Webseite Siehe http"));
         Assert.isTrue(realLva.getAdditionalInfo2().startsWith("Homepage der Lehrveranstaltung"));
         Assert.isTrue(realLva.getInstitute().startsWith("E185"));
         Assert.isTrue(realLva.getPerformanceRecord().startsWith("Beurteilung der Lösung"));
         //Assert.isTrue(realLva.getLecturer().size() == 2);
         //Assert.isTrue(realLva.getLecturer().get(0).startsWith("Puntigam"));
         // TODO: LVADate Test, requires LvaDate in Lva
     }
 
     @Test(expected = ServiceException.class)
     public void testGetLvaNonExistLvaNr() throws Exception {
         lvaFetcherService.getLva("185.A55", "2012W");
     }
 
     @Test(expected = ServiceException.class)
     public void testGetLvaNonExistSemester() throws Exception {
         lvaFetcherService.getLva("185.A01", "2100W");
     }
 
 }
