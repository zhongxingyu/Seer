 package edu.northwestern.bioinformatics.studycalendar.core;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
 import edu.nwu.bioinformatics.commons.ComparisonUtils;
 import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import org.apache.commons.beanutils.PropertyUtils;
 import org.easymock.IArgumentMatcher;
 import org.easymock.classextension.EasyMock;
 import org.springframework.context.ApplicationContext;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Rhett Sutphin
  */
 public abstract class StudyCalendarTestCase extends CoreTestCase {
     protected Set<Object> mocks = new HashSet<Object>();
     protected ApplicationSecurityManager applicationSecurityManager;
 
     // static {
     //     SLF4JBridgeHandler.install();
     // }
 
     public static ApplicationContext getDeployedApplicationContext() {
         return StudyCalendarTestCoreApplicationContextBuilder.getApplicationContext();
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         applicationSecurityManager = new ApplicationSecurityManager();
         FormatTools.setLocal(new FormatTools("MM/dd/yyyy"));
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
         applicationSecurityManager.removeUserSession();
         FormatTools.clearLocalInstance();
     }
 
     ////// MOCK REGISTRATION AND HANDLING
 
     protected <T> T registerMockFor(Class<T> forClass) {
         return registered(EasyMock.createMock(forClass));
     }
 
     protected <T> T registerNiceMockFor(Class<T> forClass) {
         return registered(EasyMock.createNiceMock(forClass));
     }
 
     protected <T> T registerMockFor(Class<T> forClass, Method... methodsToMock) {
         return registered(EasyMock.createMock(forClass, methodsToMock));
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     protected <T extends StudyCalendarDao> T registerDaoMockFor(Class<T> forClass) {
         List<Method> methods = new LinkedList<Method>(Arrays.asList(forClass.getMethods()));
         for (Iterator<Method> iterator = methods.iterator(); iterator.hasNext();) {
             Method method = iterator.next();
             if ("domainClass".equals(method.getName())) {
                 iterator.remove();
             }
         }
         return registerMockFor(forClass, methods.toArray(new Method[methods.size()]));
     }
 
     protected void replayMocks() {
         for (Object mock : mocks) EasyMock.replay(mock);
     }
 
     protected void verifyMocks() {
         for (Object mock : mocks) EasyMock.verify(mock);
     }
 
     protected void resetMocks() {
         for (Object mock : mocks) EasyMock.reset(mock);
     }
 
     private <T> T registered(T mock) {
         mocks.add(mock);
         return mock;
     }
 
     protected static <T> T matchByProperties(T template) {
         EasyMock.reportMatcher(new PropertyMatcher<T>(template));
         return null;
     }
 
     public static <T extends Date> T sameDay(int year, int month, int day) {
         EasyMock.reportMatcher(new DayMatcher(year, month, day));
         return null;
     }
 
     /**
      * Finds a directory relative to the given module, whether the working directory is the
      * module (as when running the tests from buildr) or the root of the project (as when running
      * in IDEA).
      */
     public static File getModuleRelativeDirectory(String moduleName, String directory) throws IOException {
         File dir = new File(directory);
         if (dir.exists()) return dir;
 
         dir = new File(moduleName.replaceAll(":", "/"), directory);
         if (dir.exists()) return dir;
 
         throw new FileNotFoundException(
             String.format("Could not find directory %s relative to module %s from current directory %s",
                 directory, moduleName, new File(".").getCanonicalPath()));
     }
 
     /**
      * Easymock matcher that compares two objects on their property values
      */
     @SuppressWarnings({ "unchecked" })
     private static class PropertyMatcher<T> implements IArgumentMatcher {
         private T template;
         private Map<String, Object> templateProperties;
 
         public PropertyMatcher(T template) {
             this.template = template;
             try {
                 templateProperties = PropertyUtils.describe(template);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             }
         }
 
         public boolean matches(Object argument) {
             try {
                 Map<String, Object> argumentProperties = PropertyUtils.describe(argument);
                 for (Map.Entry<String, Object> entry : templateProperties.entrySet()) {
                     Object argProp = argumentProperties.get(entry.getKey());
                     Object templProp = entry.getValue();
                     if (!ComparisonUtils.nullSafeEquals(templProp, argProp)) {
                         throw new AssertionError("Argument's " + entry.getKey()
                                 + " property doesn't match template's: " + templProp + " != " + argProp);
                     }
                 }
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             } catch (NoSuchMethodException e) {
                 throw new RuntimeException(e);
             }
 
             return true;
         }
 
 
         public void appendTo(StringBuffer buffer) {
             buffer.append(template).append(" (by properties)");
         }
     }
 
     private static class DayMatcher implements IArgumentMatcher {
         private int year, month, date;
 
         private DayMatcher(int year, int month, int day) {
             this.year = year;
             this.month = month;
             this.date = day;
         }
 
         public boolean matches(Object o) {
             return DateTools.daysEqual((Date) o, year, month, date);
         }
 
         public void appendTo(StringBuffer sb) {
             sb.append("same day as ").append(date);
         }
     }
 }
