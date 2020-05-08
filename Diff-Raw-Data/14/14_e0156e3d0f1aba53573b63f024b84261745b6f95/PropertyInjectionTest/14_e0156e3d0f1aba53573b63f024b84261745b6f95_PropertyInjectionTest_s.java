 package eu.gruchala.spring.props;
 
 import eu.gruchala.spring.TestConfig;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.annotation.Resource;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * <p/>
  *
  * @author lgr
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = TestConfig.class)
 public class PropertyInjectionTest {
 
     @Autowired
     private PropertyValuesInjection property;
 
     @Resource(name = "getOrganisationNameFromEnv")
     private String jugName;
    
     @Value("#{propertyValuesInjection.cityOfJug}")
     private String cityOfJug;
 
     @Test
     public void testPropertyFromXmlConfigExists() {
         assertEquals("DI in Spring without XML", property.getTopic());
     }
 
     @Test
     public void testPropertyFromPropertySourceExists() {
         assertEquals("Szczecin Java User Group", jugName);
     }
 
     @Test
     public void testPropertyInjectionByValueAnnotation() {
         assertEquals("Szczecin Java User Group", property.getOrganisationName());
     }
 
     @Test
     public void testInjectionBySpringExpressionLanguage() {
         assertEquals("Szczecin", cityOfJug);
     }
 }
