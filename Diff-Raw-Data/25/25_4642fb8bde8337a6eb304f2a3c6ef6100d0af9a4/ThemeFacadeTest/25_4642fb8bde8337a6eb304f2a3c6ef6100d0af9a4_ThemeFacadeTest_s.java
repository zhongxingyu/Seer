 package vio.service.doc;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.List;
 import javax.ejb.EJB;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.exporter.ZipExporter;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import vio.model.doc.Theme;
 import vio.service.AbstractFacade;
 import org.apache.log4j.Logger;
 import vio.test.TestHelperBean;
 
 /**
  * @author moroz
  */
 @RunWith(Arquillian.class)
 public class ThemeFacadeTest {
 
     public static Logger log = Logger.getLogger("vio.service.doc.ThemeFacadeTest");
     /* 
      * arquillian not support PersistenceContext injection in jboss-containers
      @PersistenceContext
      EntityManager em;
      */
     //
     @EJB
     ThemeFacade themeFacade_1;
     //
     @EJB
     TestHelperBean testHelper;
 
     public ThemeFacadeTest() {
     }
 
     @Deployment
     public static Archive<?> createTestArchive() throws FileNotFoundException {
 
         Archive<JavaArchive> result = ShrinkWrap.create(JavaArchive.class, "test.jar")
                 .addPackage(ThemeFacadeTest.class.getPackage())
                 .addPackage(AbstractFacade.class.getPackage())
                 .addPackage(Theme.class.getPackage())
                 .addPackage(TestHelperBean.class.getPackage()) //to access to the EntityManager functionality
                 .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                 .addAsResource("test-ds-driver-h2.xml", "META-INF/test-ds.xml")
                 .addAsResource("log4j.properties", "log4j.properties");
 
         result.as(ZipExporter.class).exportTo(new File("target/test-ear.zip"), true);
         //result.writeTo(new FileOutputStream("target/test-ear-arquillian.zip"), Formatters.VERBOSE);
 
         return result;
     }
 
     @Test
     public void test_st_1_CreateAndGet() {
         log.info("------------------>CreateAndGet");
         assertNotNull("error:injected service ThemeFacade is null", themeFacade_1);
         Theme t1 = createTheme("CreateAndGet");
         assertNotNull("error:id of theme after persist is null", t1.getId());
         Theme t1_act = themeFacade_1.get(t1.getId());
         assertNotNull("error:method ThemeFacade.get return null", t1_act);
         assertEquals(t1.getVal(), t1_act.getVal());
         log.info("CreateAndGet<------------------");
     }
 
     @Test
     public void test_st_2_Edit() {
         log.info("------------------>test_st_2_Edit");
         createTheme("test_st_2_Edit");
         List<Theme> t_list = themeFacade_1.list();
         assertNotNull("error:list of themes is null", t_list);
         assertTrue("error:list of themes is empty", !t_list.isEmpty());
         log.info("list of themes size:" + t_list.size());//lists maybe different size
         Theme t1 = t_list.get(0);
         assertNotNull("error:theme(0) in list is null", t1);
         String val = "Edit::theme new name:" + Math.random() * 100;
         t1.setVal(val);
         themeFacade_1.save(t1);
         /*
          * select value natively to avoid some JPA machinery
          */
        String val_act = testHelper.<String>executeNativeQueryWithSingleResult(
                 "select  t.val from Theme t where t.id="
                 + t1.getId());
         assertNotNull("error:val of theme after native query is null:" + t1.getId(), val_act);
         assertEquals("error:val of theme after native query and t1 not same", t1.getVal(), val_act);
         log.info("test_st_2_Edit<------------------");
     }
 
     private Theme createTheme(String namePrefix) {
         Theme t = new Theme(namePrefix + "::theme_" + Math.random() * 100);
         themeFacade_1.create(t);
         return t;
     }
 }
