 package vio.test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import javax.ejb.EJB;
 import javax.persistence.Tuple;
 import org.apache.log4j.Logger;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.exporter.ZipExporter;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import vio.model.doc.Color;
 import vio.model.doc.DocumentType;
 import vio.model.doc.Theme;
 import vio.service.AbstractFacade;
 import vio.service.doc.ColorFacade;
 import vio.service.doc.DocumentTypeFacade;
 import vio.service.doc.ThemeFacade;
 
 /**
  * @author moroz
  */
 @RunWith(Arquillian.class)
 public class LearnCriteriaAPITest {
 
     public static Logger log = Logger.getLogger("vio.service.doc.LearnCriteriaAPITest");
     //
     private static final String[] colors = {"Red", "Blue", "Green", "Yellow", "Black", "White"};
     private static final String[] themes = {"ThemeOne", "ThemeTwo", "ThemeThree", "ThemeFour", "ThemeFive"};
     //
     @EJB
     LearnCriteriaAPIBean learnCriteriaBean;
     //
     @EJB
     ColorFacade cfb;
     //
     @EJB
     ThemeFacade tfb;
     //
     @EJB
     DocumentTypeFacade dtfb;
     //
     @EJB
     TestHelperBean helper;
 
     @Deployment
     public static Archive<?> createTestArchive() throws FileNotFoundException {
 
         Archive<JavaArchive> result = ShrinkWrap.create(JavaArchive.class, "test.jar")
                 .addPackage(ColorFacade.class.getPackage())
                 .addPackage(AbstractFacade.class.getPackage())
                 .addPackage(LearnCriteriaAPIBean.class.getPackage())
                 .addPackage(LearnCriteriaAPITest.class.getPackage())
                 .addPackage(Color.class.getPackage())
                 .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                 .addAsResource("test-ds-driver-postgresql.xml", "META-INF/test-ds.xml") //put *-ds.xml at meta-inf - this creates a datasource before running the tests
                 .addAsResource("log4j.properties", "log4j.properties");
 
      //   result.as(ZipExporter.class).exportTo(new File("target/test-ear.zip"), true);
 
         return result;
     }
 
     @Before
     public void setup() {
         createEnt();
     }
 
     @After
     public void tearDown() {
         deleteEnt();
     }
 
     /*
     * '%' ->A substitute for zero or more characters
     * '_' ->A substitute for a single character
      * [charlist] ->Sets and ranges of characters to match
      * [^charlist] or [!charlist] -> Matches only a character NOT specified within the brackets
      *	
      */
     @Test
     public void test_DD_step_1() {
         Collection<Color> cc = learnCriteriaBean.getColorsByName("%" + colors[0]);
         assertNotNull(cc);
         assertTrue(!cc.isEmpty());
         for (Color c : cc) {
             log.info("Colors in collection:" + c.getVal());
         }
 
     }
 
     @Test
     public void test_DD_step_2() {
         Collection<Color> cc = learnCriteriaBean.getColorsByName("%e%");
         assertNotNull(cc);
         assertTrue(!cc.isEmpty());
         for (Color c : cc) {
             log.info("Colors in collection:" + c.getVal());
         }
 
     }
 
     @Test
     public void test_DD_step_3() {
         Collection<Color> cc = learnCriteriaBean.getColorsByName("%e_");
         assertNotNull(cc);
         assertTrue(!cc.isEmpty());
         for (Color c : cc) {
             log.info("Colors in collection:" + c.getVal());
         }
 
     }
 
     @Test
     public void test_DD_step_4() {
 
         log.info("====================Tuple-step-4======================");
         Collection<Tuple> cc = learnCriteriaBean.getCartesianProduct();
         assertNotNull(cc);
         assertTrue(!cc.isEmpty());
         for (Tuple tuple : cc) {
             log.info(Arrays.toString(tuple.toArray()));
         }
 
         Tuple t = (Tuple) cc.toArray()[0];
         log.info(t.get(0).toString());
 
     }
 
     @Test
     public void test_DD_step_5() {
 
         log.info("===================Fetch-step-5========================");
         Collection<DocumentType> adoctypes = learnCriteriaBean.getFetch();
 
         assertTrue(!adoctypes.isEmpty());
         for (DocumentType dt : adoctypes) {
             log.info(dt.toString());
             assertNotNull(dt.getChildDocTypes());
         }
 
     }
 //    
 
     private void createEnt() {
         String prefix = String.valueOf(Math.random() * 100) + "_";
         for (String c : colors) {
             Color color = new Color(prefix + c);
             cfb.create(color);
         }
         for (String t : themes) {
             Theme theme = new Theme(prefix + t);
             tfb.create(theme);
         }
 
         /**
          * 3-levels of doctypes
          */
         int[] levels = {5, 3, 3};
         Collection<DocumentType> doctypes = createDoctypes(levels, String.valueOf(Math.random() + 100), null);
         for (DocumentType d : doctypes) {
             dtfb.create(d);
         }
 
     }
 
     /**
      * создает levels.lenght-уровневую тестовую иерархию documenttypes
      *
      * @param levels - levels[x] количество сущностей на уровне x (i.e
      * levels[0]=5 -> five entities at level 0, and so on )
      * @param prefix - prefix for name of all entities
      * @param parent - parent entity (mainly for recursive purposes, we start
      * with parent =null)
      * @return tree like collection of documenttypes
      */
     private Collection<DocumentType> createDoctypes(int[] levels, String prefix, DocumentType parent) {
 
         String val = prefix.contains("doctype") ? prefix : prefix + "_doctype";
         Collection<DocumentType> result = new ArrayList<DocumentType>(levels[0]);
 
         for (int i = 0; i < levels[0]; i++) {
             DocumentType d = new DocumentType(val + "_" + i);
             result.add(d);
             if (parent != null) {
                 d.setParentDocType(parent);
             }
             if (levels.length > 1) {
                 int[] nlevels = Arrays.copyOfRange(levels, 1, levels.length);
                 d.setChildDocTypes(createDoctypes(nlevels, d.getVal(), d));
             }
         }
 
         return result;
     }
 
     private void deleteEnt() {
         helper.executeNativeQueryNoResult("delete from color");
         helper.executeNativeQueryNoResult("delete from theme");
         helper.executeNativeQueryNoResult("delete from documenttype");
     }
 }
