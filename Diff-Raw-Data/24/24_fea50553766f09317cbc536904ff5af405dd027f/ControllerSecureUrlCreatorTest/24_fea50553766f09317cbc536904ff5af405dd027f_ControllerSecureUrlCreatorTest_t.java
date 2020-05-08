 package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;
 
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
 import gov.nih.nci.cabig.ctms.tools.spring.BeanNameControllerUrlResolver;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 import org.acegisecurity.intercept.web.FilterSecurityInterceptor;
 import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
 import org.acegisecurity.ConfigAttributeDefinition;
 import org.acegisecurity.ConfigAttribute;
 import org.acegisecurity.SecurityConfig;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Iterator;
 
 public class ControllerSecureUrlCreatorTest extends StudyCalendarTestCase {
     private static final String PREFIX = "prefix";
 
     private ControllerSecureUrlCreator creator;
     private DefaultListableBeanFactory beanFactory;
     private BeanNameControllerUrlResolver resolver;
     private FilterSecurityInterceptor filterInvocationInterceptor;
 
     protected void setUp() throws Exception {
         super.setUp();
 
         resolver = new BeanNameControllerUrlResolver();
         resolver.setServletName(PREFIX);
 
         filterInvocationInterceptor = new FilterSecurityInterceptor();
 
         creator = new ControllerSecureUrlCreator();
         creator.setUrlResolver(resolver);
         creator.setfilterInvocationInterceptor(filterInvocationInterceptor);
 
         beanFactory = new DefaultListableBeanFactory();
     }
 
     private void registerControllerBean(String name, Class clazz) {
         String beanName = name + "Controller";
         beanFactory.registerBeanDefinition(beanName, createBeanDef(clazz));
         beanFactory.registerAlias(beanName, '/' + name);
     }
 
     private BeanDefinition createBeanDef(Class clazz) {
         return new RootBeanDefinition(clazz);
     }
 
     public void testSingleGroupRegistered() throws Exception {
         registerControllerBean("single", SingleGroupController.class);
         doProcess();
 
         ConfigAttributeDefinition defs = lookupConfigAttributeDefinitions("single");
         assertEquals("Wrong Protection Group Size", 1, defs.size());
         assertEquals("Wrong Role", "STUDY_COORDINATOR", ((ConfigAttribute)defs.getConfigAttributes().next()).getAttribute());
     }
 
     public void testMultiGroupRegistered() throws Exception {
         registerControllerBean("multi", MultiGroupController.class);
         doProcess();
 
         ConfigAttributeDefinition defs = lookupConfigAttributeDefinitions("multi");
         assertEquals("Wrong Protection Group Size", 2, defs.size());
 
         Iterator configAttribIter = defs.getConfigAttributes();
         assertEquals("Wrong Role", "STUDY_COORDINATOR", ((ConfigAttribute)configAttribIter.next()).getAttribute());
         assertEquals("Wrong Role", "SUBJECT_COORDINATOR", ((ConfigAttribute)configAttribIter.next()).getAttribute());
     }
 
     public void testNoGroupAllowsAll() throws Exception {
         registerControllerBean("zero", NoGroupController.class);
         doProcess();
 
         ConfigAttributeDefinition defs = lookupConfigAttributeDefinitions("zero");
         assertEquals("Wrong Protection Group Size", 5, defs.size());
 
         Iterator configAttribIter = defs.getConfigAttributes();
         assertEquals("Wrong Role", "STUDY_COORDINATOR", ((ConfigAttribute)configAttribIter.next()).getAttribute());
         assertEquals("Wrong Role", "STUDY_ADMIN", ((ConfigAttribute)configAttribIter.next()).getAttribute());
         assertEquals("Wrong Role", "SYSTEM_ADMINISTRATOR", ((ConfigAttribute)configAttribIter.next()).getAttribute());
         assertEquals("Wrong Role", "SUBJECT_COORDINATOR", ((ConfigAttribute)configAttribIter.next()).getAttribute());
         assertEquals("Wrong Role", "SITE_COORDINATOR", ((ConfigAttribute)configAttribIter.next()).getAttribute());
     }
 
     public void testMapResolvesPathsLongestFirst() throws Exception {
         registerControllerBean("long/plus", SingleGroupController.class);
         registerControllerBean("long", MultiGroupController.class);
         registerControllerBean("long/plus/more", NoGroupController.class);
         doProcess();
 
         PathBasedFilterInvocationDefinitionMap actual = extractActualPathMap();
         ConfigAttributeDefinition noGroup = actual.lookupAttributes("/prefix/long/plus/more");
         assertEquals("Wrong number of groups: " + noGroup, 5, noGroup.size());
         ConfigAttributeDefinition singleGroup = actual.lookupAttributes("/prefix/long/plus");
         assertEquals("Wrong number of groups: " + singleGroup, 1, singleGroup.size());
         ConfigAttributeDefinition multiGroup = actual.lookupAttributes("/prefix/long");
         assertEquals("Wrong number of groups: " + multiGroup, 2, multiGroup.size());
     }
 
    public void testMapResolvesSeparatePathsWithSameLength() throws Exception {
        registerControllerBean("pear", SingleGroupController.class);
        registerControllerBean("pome", MultiGroupController.class);
        doProcess();

        PathBasedFilterInvocationDefinitionMap actual = extractActualPathMap();
        ConfigAttributeDefinition singleGroup = actual.lookupAttributes("/prefix/pear");
        assertNotNull("Could not resolve pear", singleGroup);
        assertEquals("Wrong number of groups: " + singleGroup, 1, singleGroup.size());

        ConfigAttributeDefinition multiGroup = actual.lookupAttributes("/prefix/pome");
        assertNotNull("Could not resolve pome", multiGroup);
        assertEquals("Wrong number of groups: " + multiGroup, 2, multiGroup.size());
    }

     public ConfigAttributeDefinition lookupConfigAttributeDefinitions(String controllerName) {
         assertNotNull("Secure Url List is null", filterInvocationInterceptor.getObjectDefinitionSource());
         ConfigAttributeDefinition defs = extractActualPathMap()
                 .lookupAttributes(new StringBuilder().append('/').append(PREFIX).append('/').append(controllerName).toString());
         assertNotNull("Configuration Attribute Definitions are null", defs);
         return defs;
     }
 
     private PathBasedFilterInvocationDefinitionMap extractActualPathMap() {
         return (PathBasedFilterInvocationDefinitionMap)filterInvocationInterceptor
                 .getObjectDefinitionSource();
     }
 
     public void testPathBasedFilterDefinitionMap() {
         ConfigAttributeDefinition def = new ConfigAttributeDefinition();
         def.addConfigAttribute(new SecurityConfig("CreateStudyAccess"));
 
         PathBasedFilterInvocationDefinitionMap map = new PathBasedFilterInvocationDefinitionMap();
         map.addSecureUrl("/path", def);
 
         FilterSecurityInterceptor filter = new FilterSecurityInterceptor();
         filter.setObjectDefinitionSource(map);
 
         ConfigAttributeDefinition actualDef = map.lookupAttributes("/path");
 
         assertEquals("Wrong Definition Size", 1, actualDef.size());
         assertEquals("Wrong Attribute", "CreateStudyAccess", ((ConfigAttribute)actualDef.getConfigAttributes().next()).getAttribute());
         assertNotNull("Filter Object Definition Source is Null", filter.getObjectDefinitionSource());
     }
     
     private void doProcess() {
         replayMocks();
         resolver.postProcessBeanFactory(beanFactory);
         creator.postProcessBeanFactory(beanFactory);
         verifyMocks();
     }
 
     private static class TestingController implements Controller {
         public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
             throw new UnsupportedOperationException("handleRequest not implemented");
         }
     }
     @AccessControl(roles = STUDY_COORDINATOR)
     public static class SingleGroupController extends TestingController { }
 
     @AccessControl(roles = { STUDY_COORDINATOR, SUBJECT_COORDINATOR })
     public static class MultiGroupController extends TestingController { }
 
     public static class NoGroupController extends TestingController { }
 
 
 }
