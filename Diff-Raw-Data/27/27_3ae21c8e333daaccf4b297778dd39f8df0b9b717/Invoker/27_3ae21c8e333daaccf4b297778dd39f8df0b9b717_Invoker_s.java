 package org.testng.internal;
 
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.testng.ClassMethodMap;
 import org.testng.IClass;
 import org.testng.IHookable;
 import org.testng.ITestClass;
 import org.testng.ITestContext;
 import org.testng.ITestListener;
 import org.testng.ITestNGMethod;
 import org.testng.ITestResult;
 import org.testng.Reporter;
 import org.testng.SuiteRunState;
 import org.testng.TestException;
 import org.testng.TestNGException;
 import org.testng.internal.InvokeMethodRunnable.TestNGRuntimeException;
 import org.testng.internal.annotations.AnnotationHelper;
 import org.testng.internal.annotations.IAnnotationFinder;
 import org.testng.internal.annotations.IConfiguration;
 import org.testng.internal.thread.IPooledExecutor;
 import org.testng.internal.thread.ThreadExecutionException;
 import org.testng.internal.thread.ThreadUtil;
 import org.testng.xml.XmlClass;
 import org.testng.xml.XmlSuite;
 import org.testng.xml.XmlTest;
 
 /**
  * This class is responsible for invoking methods:
  * - test methods
  * - configuration methods
  * - possibly in a separate thread
  * and then for notifying the result listeners.
  *
  * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
  * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
  */
 public class Invoker implements IInvoker {
   private ITestContext m_testContext;
   private ITestResultNotifier m_notifier;
   private IAnnotationFinder m_annotationFinder;
   private SuiteRunState m_suiteState;
 
   public Invoker(ITestContext testContext,
                  ITestResultNotifier notifier,
                  SuiteRunState state,
                  IAnnotationFinder annotationFinder) {
     m_testContext= testContext;
     m_suiteState= state;
     m_notifier= notifier;
     m_annotationFinder= annotationFinder;
   }
 
   /**
    * Invoke configuration methods if they belong to the same TestClass passed
    * in parameter.. <p/>TODO: Calculate ahead of time which methods should be
    * invoked for each class. Might speed things up for users who invoke the
    * same test class with different parameters in the same suite run.
    *
    * If instance is non-null, the configuration will be run on it.  If it is null,
    * the configuration methods will be run on all the instances retrieved
    * from the ITestClass.
    */
   public void invokeConfigurations(IClass testClass,
                                    ITestNGMethod[] allMethods,
                                    XmlSuite suite,
                                    Map<String, String> params,
                                    Object instance)
   {
     invokeConfigurations(testClass, null, allMethods, suite, params, instance);
   }
 
   private void invokeConfigurations(IClass testClass,
                                    ITestNGMethod currentTestMethod,
                                    ITestNGMethod[] allMethods,
                                    XmlSuite suite,
                                    Map<String, String> params,
                                    Object instance)
   {
     if(null == allMethods) {
       log(5, "No @Configuration methods found");
 
       return;
     }
 
     ITestNGMethod[] methods= filterMethodsUnique(testClass, allMethods);
 
     for(ITestNGMethod tm : methods) {
       if(null == testClass) {
         testClass= tm.getTestClass();
       }
 
       ITestResult testResult= new TestResult(testClass, 
                                              instance,
                                              tm,
                                              null,
                                              System.currentTimeMillis(),
                                              System.currentTimeMillis());
 
       IConfiguration configurationAnnotation= null;
       try {
         Object[] instances= tm.getInstances();
         if (instances == null || instances.length == 0) instances = new Object[] { instance };
         Class objectClass= instances[0].getClass();
         Method method= tm.getMethod();
 
         // Only run the configuration if
         // - the test is enabled and
         // - the Configuration method belongs to the same class or a parent
         if(MethodHelper.isEnabled(objectClass, m_annotationFinder)) {
           configurationAnnotation= (IConfiguration) AnnotationHelper.findConfiguration(m_annotationFinder, method);
 
           boolean isClassConfiguration= isClassConfiguration(configurationAnnotation);
           boolean alwaysRun= isAlwaysRun(configurationAnnotation);
 
           if(!confInvocationPassed(tm) && !alwaysRun) {
             handleConfigurationSkip(tm, testResult);
             continue;
           }
 
           log(3, "Invoking " + Utils.detailedMethodName(tm, true));
           
           Object[] parameters= Parameters.createConfigurationParameters(tm.getMethod(),
                                                                         params,
                                                                         currentTestMethod,
                                                                         m_annotationFinder,
                                                                         suite);
           testResult.setParameters(parameters);
 
           Object[] newInstances= (null != instance) ? new Object[] { instance } : instances;
 
           invokeConfigurationMethod(newInstances, tm, parameters, isClassConfiguration, testResult);
           
           // TODO: probably we should trigger the event for each instance???
           testResult.setEndMillis(System.currentTimeMillis());
           runConfigurationListeners(testResult);
         } // if is enabled
         else {
           log(3,
               "Skipping "
               + Utils.detailedMethodName(tm, true)
               + " because "
               + objectClass.getName()
               + " is not enabled");
         }
       }
       catch(InvocationTargetException ex) {
         handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, suite);
       }
       catch(TestNGException ex) {
         // Don't wrap TestNGExceptions, it could be a missing parameter on a
         // @Configuration method
         handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, suite);
       }
       catch(Throwable ex) { // covers the non-wrapper exceptions
         handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, suite);
       }
     } // for methods
   }
   
   /**
    * Marks the currect <code>TestResult</code> as skipped and invokes the listeners.
    */
   private void handleConfigurationSkip(ITestNGMethod tm, ITestResult testResult) {
     testResult.setStatus(ITestResult.SKIP);
     runConfigurationListeners(testResult);
   }
   
   /**
    * Is the current <code>IConfiguration</code> a class-level method.
    */
   private  boolean isClassConfiguration(IConfiguration configurationAnnotation) {
     if(null == configurationAnnotation) {
       return false;
     }
     
     boolean before= (null != configurationAnnotation)
       ? configurationAnnotation.getBeforeTestClass()
       : false;
 
     boolean after= (null != configurationAnnotation)
       ? configurationAnnotation.getAfterTestClass()
       : false;
 
     return (before || after);
   }
 
   /**
    * Is the <code>IConfiguration</code> marked as alwaysRun.
    */
   private boolean isAlwaysRun(IConfiguration configurationAnnotation) {
     if(null == configurationAnnotation) {
       return false;
     }
     
     boolean alwaysRun= false;
     if ((configurationAnnotation.getAfterSuite()
         || configurationAnnotation.getAfterTest()
         || configurationAnnotation.getAfterTestClass()
         || configurationAnnotation.getAfterTestMethod())
         && configurationAnnotation.getAlwaysRun())
     {
         alwaysRun= true;
     }
     
     return alwaysRun;
   }
   
   private void handleConfigurationFailure(Throwable ite,
                                           ITestNGMethod tm,
                                           ITestResult testResult,
                                           IConfiguration annotation,
                                           XmlSuite suite) 
   {
     Throwable cause= ite.getCause() != null ? ite.getCause() : ite;
     Utils.log("", 3, "Failed to invoke @Configuration method " 
         + tm.getRealClass().getName() + "." + tm.getMethodName() + ":" + cause.getMessage());
     handleException(cause, tm, testResult, 1, false);
     runConfigurationListeners(testResult);
 
     // 
     // If in TestNG mode, need to take a look at the annotation to figure out
     // what kind of @Configuration method we're dealing with
     //
     if (null != annotation) {
       recordConfigurationInvocationFailed(tm, annotation, suite);
     }
   }
 
   /**
    * @return All the classes that belong to the same <test> tag as @param cls
    */
   private XmlClass[] findClassesInSameTest(Class cls, XmlSuite suite) {
     Map<String, XmlClass> vResult= new HashMap<String, XmlClass>();
     String className= cls.getName();
     for(XmlTest test : suite.getTests()) {
       for(XmlClass testClass : test.getXmlClasses()) {
         if(testClass.getName().equals(className)) {
 
           // Found it, add all the classes in this test in the result
           for(XmlClass thisClass : test.getXmlClasses()) {
             vResult.put(thisClass.getName(), thisClass);
           }
           // Note:  we need to iterate through the entire suite since the same
           // class might appear in several <test> tags
         }
       }
     }
 
     XmlClass[] result= vResult.values().toArray(new XmlClass[vResult.size()]);
 
     return result;
   }
 
   /**
    * Record internally the failure of a Configuration, so that we can determine
    * later if @Test should be skipped.
    */
   private void recordConfigurationInvocationFailed(ITestNGMethod tm, IConfiguration annotation, XmlSuite suite) {
     // If beforeTestClass/beforeTestMethod or afterTestClass/afterTestMethod 
     // failed, mark this entire class as failed, but only this class (the other 
     // classes should keep running normally)
     if(annotation.getBeforeTestClass()
       || annotation.getAfterTestClass()
       || annotation.getBeforeTestMethod()
       || annotation.getAfterTestMethod()) 
     {
       setClassInvocationFailure(tm.getRealClass(), false);
     }
 
     // If beforeSuite or afterSuite failed, mark *all* the classes as failed
     // for configurations.  At this point, the entire Suite is screwed
     else if (annotation.getBeforeSuite() || annotation.getAfterSuite()) {
       m_suiteState.failed();
     }
 
     // beforeTest or afterTest:  mark all the classes in the same
     // <test> stanza as failed for configuration
     else if (annotation.getBeforeTest() || annotation.getAfterTest()) {
       XmlClass[] classes= findClassesInSameTest(tm.getRealClass(), suite);
       for(XmlClass xmlClass : classes) {
         setClassInvocationFailure(xmlClass.getSupportClass(), false);
       }
     }
     String[] beforeGroups= annotation.getBeforeGroups();
     if(null != beforeGroups && beforeGroups.length > 0) {
       for(String group: beforeGroups) {
         m_beforegroupsFailures.put(group, Boolean.FALSE);
       }
     }
   }
   
   /**
    * @return true if this class has successfully run all its @Configuration
    * method or false if at least one of these methods failed.
    */
   private boolean confInvocationPassed(ITestNGMethod method) {
     boolean result= true;
 
     Class cls= method.getMethod().getDeclaringClass();
     
     if(m_suiteState.isFailed()) {
       result= false;
     }
     else {
       if(m_classInvocationResults.containsKey(cls)) {
         result= m_classInvocationResults.get(cls);
       }
       else {
         for(Class clazz: m_classInvocationResults.keySet()) {
           if(clazz.isAssignableFrom(cls)) {
             result= false;
             break;
           }
         }
       }
     }
 
     // check if there are failed @BeforeGroups
     String[] groups= method.getGroups();
     if(null != groups && groups.length > 0) {
       for(String group: groups) {
         if(m_beforegroupsFailures.containsKey(group)) {
           result= false;
           break;
         }
       }
     }
     return result;
   }
 
   /** Group failures must be synched as the Invoker is accessed concurrently */
   private Map<String, Boolean> m_beforegroupsFailures= new Hashtable<String, Boolean>();
   
   /** Class failures must be synched as the Invoker is accessed concurrently */
   private Map<Class, Boolean> m_classInvocationResults= new Hashtable<Class, Boolean>();
 
   private void setClassInvocationFailure(Class clazz, boolean flag) {
     m_classInvocationResults.put(clazz, flag);
   }
 
   /**
    * Effectively invokes a configuration method on all passed in instances.
    * 
    * @param instances the instances to invoke the configuration method on
    * @param tm the configuration method
    * @param params the parameters needed for method invocation
    * @param isClass flag if the configuration method is a class level method // FIXME: this looks like a missusage
    * @param testResult
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    */
   private void invokeConfigurationMethod(Object[] instances,
                                          ITestNGMethod tm,
                                          Object[] params,
                                          boolean isClass,
                                          ITestResult testResult)
     throws InvocationTargetException, IllegalAccessException 
   {
     // Mark this method with the current thread id
     tm.setId(ThreadUtil.currentThreadInfo());
     long timeOut= tm.getTimeOut();
 
     for(Object targetInstance : instances) {
       InvokedMethod im= new InvokedMethod(targetInstance,
                                           tm,
                                           params,
                                           false, /* isTest */
                                           isClass, /* ??? */
                                           System.currentTimeMillis());
 
       m_notifier.addInvokedMethod(im);
 
       try {
         Reporter.setCurrentTestResult(testResult);
         MethodHelper.invokeMethod(tm.getMethod(), targetInstance, params);
       } 
       finally {
         Reporter.setCurrentTestResult(testResult);
       }      
     }
   }
   
   /**
    * {@link #invokeTestMethods()} eventually converge here to invoke a single @Test method.  
    * <p/>
    * This method is responsible for actually invoking the method. It decides if the invocation
    * must be done:
    * <ul>
    * <li>through an <code>IHookable</code></li> 
    * <li>directly (through reflection)</li>
    * <li>in a separate thread (in case it needs to timeout)
    * </ul>
    * 
    * <p/>
    * This method is also reponsible for invoking @BeforeGroup, @BeforeMethod, @AfterMethod, @AfterGroup
    * if it is the case for the passed in @Test method.
    */
   private List<ITestResult> invokeTestMethod(Object[] instances,
                                              final ITestNGMethod tm,
                                              Object[] parameterValues,
                                              XmlSuite suite,
                                              Map<String, String> params,
                                              ITestClass testClass,
                                              ITestNGMethod[] beforeMethods,
                                              ITestNGMethod[] afterMethods,
                                              ConfigurationGroupMethods groupMethods)
   {
     List<ITestResult> results = new ArrayList<ITestResult>();
 
     // Mark this method with the current thread id
     tm.setId(ThreadUtil.currentThreadInfo());
 
     for(int i= 0; i < instances.length; i++) {
       //
       // Invoke beforeGroups configurations
       //
       invokeBeforeGroupsConfigurations(testClass, tm, groupMethods, suite, params, instances[i]);
 
       //
       // Invoke beforeMethod configurations
       //
       invokeConfigurations(testClass, tm, beforeMethods, suite, params, instances[i]);
       
       //
       // Create the ExtraOutput for this method
       //
       TestResult testResult = null;
       try {
         testResult= new TestResult(testClass, instances[i],
                                    tm,
                                    null,
                                    System.currentTimeMillis(),
                                    0);
         testResult.setParameters(parameterValues);
         testResult.setHost(m_testContext.getHost());
         testResult.setStatus(ITestResult.STARTED);
         runTestListeners(testResult);
         results.add(testResult);
 
         InvokedMethod invokedMethod= new InvokedMethod(instances[i],
                                                        tm,
                                                        parameterValues,
                                                        true,
                                                        false,
                                                        System.currentTimeMillis());
 
         m_notifier.addInvokedMethod(invokedMethod);
         
         Method thisMethod= tm.getMethod();
 
         if(confInvocationPassed(tm)) {
           log(3, "Invoking " + thisMethod.getDeclaringClass().getName() + "." + thisMethod.getName());
 
           // If no timeOut, just invoke the method
           if(tm.getTimeOut() <= 0) {
             //
             // If this method is a IHookable, invoke its run() method
             //
             if (IHookable.class.isAssignableFrom(thisMethod.getDeclaringClass())) {
               MethodHelper.invokeHookable(instances[i], parameterValues, testClass, thisMethod, testResult);
               testResult.setStatus(ITestResult.SUCCESS);
             }
             //
             // Not a IHookable, invoke directly
             //
             else {
               try {
                 Reporter.setCurrentTestResult(testResult);
                 MethodHelper.invokeMethod(thisMethod, instances[i], parameterValues);
                 testResult.setStatus(ITestResult.SUCCESS);
               } 
               finally {
                 Reporter.setCurrentTestResult(null);
               }                            
             }
           }
           else {
             try {
               Reporter.setCurrentTestResult(testResult);
               MethodHelper.invokeWithTimeout(tm, instances[i], parameterValues, testResult);
             }
             finally {
               Reporter.setCurrentTestResult(null);
             }
           }
         }
         else {
           testResult.setStatus(ITestResult.SKIP);
         }
       }
       catch(InvocationTargetException ite) {
         testResult.setThrowable(ite.getCause());
       }
       catch(ThreadExecutionException tee) { // wrapper for TestNGRuntimeException
         Throwable cause= tee.getCause();
         if(TestNGRuntimeException.class.equals(cause.getClass())) {
           testResult.setThrowable(cause.getCause());
         }
         else {
           testResult.setThrowable(cause);
         }
       }
       catch(Throwable thr) { // covers the non-wrapper exceptions
         testResult.setThrowable(thr);
       }
       finally {
         //
         // Increment the invocation count for this method
         //
         tm.incrementCurrentInvocationCount();
 
         if (testResult != null) testResult.setEndMillis(System.currentTimeMillis());
         //
         // Invoke afterMethods
         //
         invokeConfigurations(testClass, tm, afterMethods, suite, params, instances[i]);
         
         //
         // Invoke beforeGroups configurations
         //
         invokeAfterGroupsConfigurations(testClass, tm, groupMethods, suite, params, instances[i]);
       }
 
       
     } // for instances
     
     return results;
   }
 
   /**
    * Filter all the beforeGroups methods and invoke only those that apply
    * to the current test method
    */
   private void invokeBeforeGroupsConfigurations(ITestClass testClass, 
                                                 ITestNGMethod currentTestMethod, 
                                                 ConfigurationGroupMethods groupMethods, 
                                                 XmlSuite suite, 
                                                 Map<String, String> params, 
                                                 Object instance) 
   {
     synchronized(groupMethods) {
       List<ITestNGMethod> filteredMethods = new ArrayList<ITestNGMethod>();
       String[] groups = currentTestMethod.getGroups();
       Map<String, List<ITestNGMethod>> beforeGroupMap = groupMethods.getBeforeGroupsMap();
       
       for (String group : groups) {
         List<ITestNGMethod> methods = beforeGroupMap.get(group);
         if (methods != null) {
           filteredMethods.addAll(methods);
         }
       }
       
       ITestNGMethod[] beforeMethodsArray = filteredMethods.toArray(new ITestNGMethod[filteredMethods.size()]);
       //
       // Invoke the right groups methods
       //
       if(beforeMethodsArray.length > 0) {
         // don't pass the IClass or the instance as the method may be external
         // the invocation must be similar to @BeforeTest/@BeforeSuite
         invokeConfigurations(null, beforeMethodsArray, suite, params, null);
       }
       
       //
       // Remove them so they don't get run again
       //
       groupMethods.removeBeforeGroups(groups);
     }
   }
 
   private void invokeAfterGroupsConfigurations(ITestClass testClass, 
                                                ITestNGMethod currentTestMethod,
                                                ConfigurationGroupMethods groupMethods,
                                                XmlSuite suite, 
                                                Map<String, String> params, 
                                                Object instance) 
   {
     // Skip this if the current method doesn't belong to any group
     // (only a method that belongs to a group can trigger the invocation
     // of afterGroups methods)
     if (currentTestMethod.getGroups().length == 0) return;
     
     // See if the currentMethod is the last method in any of the groups
     // it belongs to
     Map<String, String> filteredGroups = new HashMap<String, String>();
     String[] groups = currentTestMethod.getGroups();
     synchronized(groupMethods) {
       for (String group : groups) {
         if (groupMethods.isLastMethodForGroup(group, currentTestMethod)) {
           filteredGroups.put(group, group);
         }
       }
       
       if(filteredGroups.isEmpty()) return;
       
       // The list of afterMethods to run
       Map<ITestNGMethod, ITestNGMethod> afterMethods = new HashMap<ITestNGMethod, ITestNGMethod>();
       
       // Now filteredGroups contains all the groups for which we need to run the afterGroups
       // method.  Find all the methods that correspond to these groups and invoke them.
       Map<String, List<ITestNGMethod>> map = groupMethods.getAfterGroupsMap();
       for (String g : filteredGroups.values()) {
         List<ITestNGMethod> methods = map.get(g);
         // Note:  should put them in a map if we want to make sure the same afterGroups
         // doesn't get run twice
         if (methods != null) {
           for (ITestNGMethod m : methods) {
             afterMethods.put(m, m);
           }
         }
       }
       
       // Got our afterMethods, invoke them
       ITestNGMethod[] afterMethodsArray = afterMethods.keySet().toArray(new ITestNGMethod[afterMethods.size()]);
       // don't pass the IClass or the instance as the method may be external
       // the invocation must be similar to @BeforeTest/@BeforeSuite
       invokeConfigurations(null, afterMethodsArray, suite, params, null);
 
       // Remove the groups so they don't get run again
       groupMethods.removeAfterGroups(filteredGroups.keySet());      
     }
   }
 
   /**
    * Invoke all the test methods.  Note the plural:  the method passed in
    * parameter might be invoked several times if the test class it belongs
    * to has more than one instance (i.e., if an @Factory method has been
    * declared somewhere that returns several instances of this TestClass).
    * If no @Factory method was specified, testMethod will only be invoked
    * once.
    * <p/>
    * Note that this method also takes care of invoking the beforeTestMethod
    * and afterTestMethod, if any.
    * 
    * Note (alex): this method can be refactored to use a SingleTestMethodWorker that 
    * directly invokes 
    * {@link #invokeTestMethod(Object[], ITestNGMethod, Object[], XmlSuite, Map, ITestClass, ITestNGMethod[], ITestNGMethod[], ConfigurationGroupMethods)}
    * and this would simplify the implementation (see how DataTestMethodWorker is used)
    */
   public List<ITestResult> invokeTestMethods(ITestNGMethod testMethod,
                                              ITestNGMethod[] allTestMethods,
                                              int testMethodIndex,
                                              XmlSuite suite,
                                              Map<String, String> parameters,
                                              ConfigurationGroupMethods groupMethods,
                                              Object[] instances,
                                              ITestContext testContext)
   {
     // Potential bug here if the test method was declared on a parent class
     assert null != testMethod.getTestClass() 
     : "COULDN'T FIND TESTCLASS FOR " + testMethod.getMethod().getDeclaringClass();
     
     List<ITestResult> result = new ArrayList<ITestResult>();
     
     ITestClass testClass= testMethod.getTestClass();
     Method method= testMethod.getMethod();
     long start= System.currentTimeMillis();
 
     //
     // TODO:
     // - [DONE] revisit invocationCount, threadPoolSize values
     // - try to remove the isWithinThreadedMethod: still needed to determine the @BeforeMethod + @AfterMethod
     // - [DONE] solve the results different approaches: assignment and addAll
     //
     // HINT: for invocationCount>1 and threadPoolSize>1 the method will be invoked on a thread pool
     int invocationCount = (testMethod.getThreadPoolSize() > 1 ? 1 : testMethod.getInvocationCount());
     
     int failureCount = 0;
 
     Class[] expectedExceptionClasses = 
         MethodHelper.findExpectedExceptions(m_annotationFinder, testMethod.getMethod());
     while(invocationCount-- > 0) {
       boolean okToProceed = checkDependencies(testMethod, testClass, allTestMethods);
 
       if (okToProceed) {
         //
         // Invoke the test method if it's enabled
         //
         if (MethodHelper.isEnabled(testMethod.getMethod(), m_annotationFinder)) {
             //
             // HINT: If threadPoolSize specified, run this method in its own pool thread.
             //
             if (testMethod.getThreadPoolSize() > 1 && testMethod.getInvocationCount() > 1) {
               try {
                 result = invokePooledTestMethods(testMethod, allTestMethods, suite, 
                     parameters, groupMethods, testContext);
               }
               finally {
                 failureCount = handleInvocationResults(testMethod, result, failureCount, expectedExceptionClasses, false);
               }
             }
             
             //
             // No threads, regular invocation
             //
             else {
               ITestNGMethod[] beforeMethods = filterMethods(testClass, testClass.getBeforeTestMethods());
               ITestNGMethod[] afterMethods = filterMethods(testClass, testClass.getAfterTestMethods());
 
 
               Map<String, String> allParameterNames = new HashMap<String, String>();
               Object instance = testClass.getInstances(true)[0];
               ParameterBag bag= handleParameters(testMethod, 
                   instance, allParameterNames, parameters, suite, testContext);
 
               if(bag.hasErrors()) {
                 failureCount = handleInvocationResults(testMethod, 
                     bag.errorResults, failureCount, expectedExceptionClasses, true);
                 // there is nothing we can do more
                 continue;
               }
               
               Iterator<Object[]> allParameterValues= bag.parameterValues;
               
               if(testMethod.getThreadPoolSize() > 1) {
                 try {
                   result = invokePooledTestMethods(instances, 
                       testMethod, 
                       allTestMethods, 
                       beforeMethods, 
                       afterMethods, 
                       groupMethods, 
                       suite, 
                       parameters, 
                       allParameterNames, 
                       allParameterValues);
                 }
                 finally {
                   failureCount = handleInvocationResults(testMethod, result, failureCount, expectedExceptionClasses, true);
                 }                
               }
               else {
                 while (allParameterValues.hasNext()) {
                   Object[] parameterValues= allParameterValues.next();
   
                   try {
                       result = invokeTestMethod(instances,
                                                 testMethod,
                                                 parameterValues,
                                                 suite,
                                                 allParameterNames,
                                                 testClass,
                                                 beforeMethods,
                                                 afterMethods,
                                                 groupMethods);
                   }
                   finally {
                     failureCount = handleInvocationResults(testMethod, result, failureCount, expectedExceptionClasses, true);
                   }
                 }
               } // for parameters
             }
 
         } // isTestMethodEnabled 
 
       } // okToProceed
       else {
         //
         // Test is being skipped
         //
         ITestResult testResult= new TestResult(testClass, null,
                                                testMethod,
                                                null,
                                                start,
                                                System.currentTimeMillis());
         testResult.setEndMillis(System.currentTimeMillis());
         String missingGroup = testMethod.getMissingGroup();
         if (missingGroup != null) {
           testResult.setThrowable(new Throwable("Method " + testMethod 
               + " depends on nonexistent group \"" + missingGroup + "\""));
         }
 
         testResult.setStatus(ITestResult.SKIP);
         m_notifier.addSkippedTest(testMethod, testResult);
         runTestListeners(testResult);
       }
     }
     
     return result;
     
   } // invokeTestMethod
 
   private ParameterBag handleParameters(ITestNGMethod testMethod,
       Object instance,
       Map<String, String> allParameterNames,
       Map<String, String> parameters,
       XmlSuite suite,
       ITestContext testContext)
   {
     try {
       return new ParameterBag(Parameters.handleParameters(testMethod,
           allParameterNames, instance, parameters, suite, m_annotationFinder, testContext), null);
     }
     catch(Throwable cause) {
       return new ParameterBag(null, 
           new TestResult(
               testMethod.getTestClass(), 
               instance, 
               testMethod, 
               cause, 
               System.currentTimeMillis(),
               System.currentTimeMillis()));
     }
     
   }
   
   private List<ITestResult> invokePooledTestMethods(Object[] instances,
       ITestNGMethod testMethod, 
       ITestNGMethod[] allTestMethods, 
       ITestNGMethod[] beforeMethods,
       ITestNGMethod[] afterMethods,
       ConfigurationGroupMethods groupMethods,
       XmlSuite suite, 
       Map<String, String> parameters, 
       Map<String, String> allParameterNames,
       Iterator<Object[]> allParameterValues) 
   {
     List<ITestResult> result= new ArrayList<ITestResult>();
     //
     // Create the workers
     //
     List<IMethodWorker> workers= new ArrayList<IMethodWorker>();    
     
     while (allParameterValues.hasNext()) {
       Object[] parameterValues= allParameterValues.next();

       workers.add(new DataTestMethodWorker(instances, 
          testMethod,
           parameterValues,
           beforeMethods,
           afterMethods,
           groupMethods,
           suite,
           allParameterNames));
     }
     
     return runWorkers(testMethod, workers, testMethod.getThreadPoolSize(), groupMethods, suite, parameters);
   }
   
   /**
    * Invokes a method that has a specified threadPoolSize. 
    */
   private List<ITestResult> invokePooledTestMethods(ITestNGMethod testMethod, 
                                                     ITestNGMethod[] allTestMethods, 
                                                     XmlSuite suite, 
                                                     Map<String, String> parameters, 
                                                     ConfigurationGroupMethods groupMethods,
                                                     ITestContext testContext) 
   {
     List<ITestResult> result= new ArrayList<ITestResult>();
     //
     // Create the workers
     //
    List<IMethodWorker> workers= new ArrayList<IMethodWorker>();    
    List<ITestNGMethod> clones= new ArrayList<ITestNGMethod>(testMethod.getInvocationCount());
     
     for (int i = 0; i < testMethod.getInvocationCount(); i++) {
       ITestNGMethod clonedMethod= testMethod.clone();
       clonedMethod.setInvocationCount(1);
       clonedMethod.setThreadPoolSize(1);
      clones.add(clonedMethod);
       MethodInstance mi = new MethodInstance(clonedMethod, clonedMethod.getTestClass().getInstances(true));
       workers.add(new SingleTestMethodWorker(this,
           mi,
           suite, 
           parameters,
           allTestMethods,
           testContext));
     }
 
    try {
      result = runWorkers(testMethod, workers, testMethod.getThreadPoolSize(), groupMethods, suite, parameters);
    }
    finally {
      for(ITestNGMethod clone: clones) {
        clone= null;
      }
      clones.clear();
      clones= null;
    }
    
    return result;
   }
   
   /**
    * @param testMethod
    * @param result
    * @param failureCount
    * @param expectedExceptionClasses
    * @return
    */
   private int handleInvocationResults(ITestNGMethod testMethod, 
                                       List<ITestResult> result, 
                                       int failureCount, 
                                       Class[] expectedExceptionClasses,
                                       boolean triggerListeners) {
     //
     // Go through all the results and create a TestResult for each of them
     //
     for(ITestResult testResult : result) {
       Throwable ite= testResult.getThrowable();
       int status= testResult.getStatus();
 
       // Exception thrown?
       if(ite != null) {
 
         //  Invocation caused an exception, see if the method was annotated with @ExpectedException
         if(isExpectedException(ite, expectedExceptionClasses)) {
           testResult.setStatus(ITestResult.SUCCESS);
           status= ITestResult.SUCCESS;
         }
         else {
           handleException(ite, testMethod, testResult, failureCount++, true);
           status= testResult.getStatus();
         }
       }
 
       // No exception thrown, make sure we weren't expecting one
       else if(status != ITestResult.SKIP) {
         if (expectedExceptionClasses.length > 0) {
           testResult.setThrowable(
               new TestException("Expected an exception in test method " + testMethod));
           status= ITestResult.FAILURE;
         }
       }
 
       testResult.setStatus(status);
 
       // Collect the results
       if(ITestResult.SUCCESS == status) {
         m_notifier.addPassedTest(testMethod, testResult);
       }
       else if(ITestResult.SKIP == status) {
         m_notifier.addSkippedTest(testMethod, testResult);
       }
       else if(ITestResult.FAILURE == status) {
         m_notifier.addFailedTest(testMethod, testResult);
       }
       else if(ITestResult.SUCCESS_PERCENTAGE_FAILURE == status) {
         m_notifier.addFailedButWithinSuccessPercentageTest(testMethod, testResult);
       }
       else {
         assert false : "UNKNOWN STATUS:" + status;
       }
 
       if (triggerListeners) {
         runTestListeners(testResult);
       }
     } // for results
     
     return failureCount;
   }
   
   /**
    * To reduce thread contention and also to correctly handle thread-confinement
    * this method invokes the @BeforeGroups and @AfterGroups corresponding to the current @Test method.
    */
   private List<ITestResult> runWorkers(ITestNGMethod testMethod, 
       List<IMethodWorker> workers, 
       int threadPoolSize, 
       ConfigurationGroupMethods groupMethods, 
       XmlSuite suite, 
       Map<String, String> parameters)
   {
     // HINT: invoke @BeforeGroups on the original method (reduce thread contention, and also solve thread confinement)
     ITestClass testClass= testMethod.getTestClass();
     Object[] instances = testClass.getInstances(true);
     for(Object instance: instances) {
       invokeBeforeGroupsConfigurations(testClass, testMethod, groupMethods, suite, parameters, instance);
     }
     
     
     long maxTimeOut= -1; // 10 seconds
 
     for(IMethodWorker tmw : workers) {
       long mt= tmw.getMaxTimeOut();
       if(mt > maxTimeOut) {
         maxTimeOut= mt;
       }
     }
     
     ThreadUtil.execute(workers, threadPoolSize, maxTimeOut, true);
 
     //
     // Collect all the TestResults
     //
     List<ITestResult> result = new ArrayList<ITestResult>();
     for (IMethodWorker tmw : workers) {
       result.addAll(tmw.getTestResults());
     }
     
     for(Object instance: instances) {
       invokeAfterGroupsConfigurations(testClass, testMethod, groupMethods, suite, parameters, instance);
     }
     
     return result;
   }
 
   /**
    * @param testMethod
    * @param testClass
    * @return dependencies have been run successfully
    */
   private boolean checkDependencies(ITestNGMethod testMethod, ITestClass testClass, ITestNGMethod[] allTestMethods)
   {
     boolean result= true;
 
     // If this method is marked alwaysRun, no need to check for its
     // dependencies
     if (testMethod.isAlwaysRun()) {
       return true;
     }
     
     // Any missing group?
     if (testMethod.getMissingGroup() != null) {
       return false;
     }
 
     // If this method depends on groups, collect all the methods that
     // belong to these groups and make sure they have been run successfully
     if(dependsOnGroups(testMethod)) {
       String[] groupsDependedUpon= testMethod.getGroupsDependedUpon();
 
       // Get all the methods that belong to the group depended upon
       for(int i= 0; i < groupsDependedUpon.length; i++) {
         ITestNGMethod[] methods = 
           MethodHelper.findMethodsThatBelongToGroup(testMethod, 
               m_testContext.getAllTestMethods(),
               groupsDependedUpon[i]);
 
         result = result && haveBeenRunSuccessfully(methods);
       }
     } // depends on groups
 
     // If this method depends on other methods, make sure all these other
     // methods have been run successfully
     if(dependsOnMethods(testMethod)) {
       ITestNGMethod[] methods = 
         MethodHelper.findMethodsNamed(testMethod.getMethod().getName(),
                                                        allTestMethods,
                                                        testMethod.getMethodsDependedUpon());
 
       result= result && haveBeenRunSuccessfully(methods);
     }
 
     return result;
   }
 
   /**
    * @return true if all the methods have been run successfully
    */
   private boolean haveBeenRunSuccessfully(ITestNGMethod[] methods) {
     // Make sure the method has been run successfully
     for(int j= 0; j < methods.length; j++) {
       Set<ITestResult> results= m_notifier.getPassedTests(methods[j]);
       
       // If no results were returned, then these tests didn't pass
       if (results == null || results.size() == 0) return false;
       
       for (ITestResult result : results) {
         if(!result.isSuccess()) {
           return false;
         }
       }
     }
 
     return true;
   }
 
   /**
    * An exception was thrown by the test, determine if this method
    * should be marked as a failure or as failure_but_within_successPercentage
    */
   private void handleException(Throwable throwable,
                                ITestNGMethod testMethod,
                                ITestResult testResult,
                                int failureCount,
                                boolean notify) {
     testResult.setThrowable(throwable);
     int successPercentage= testMethod.getSuccessPercentage();
     int invocationCount= testMethod.getInvocationCount();
     float numberOfTestsThatCanFail= ((100 - successPercentage) * invocationCount) / 100;
 
     if(failureCount < numberOfTestsThatCanFail) {
       testResult.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
       if(notify) {
         m_notifier.addFailedButWithinSuccessPercentageTest(testMethod, testResult);  
       }
       
     }
     else {
       testResult.setStatus(ITestResult.FAILURE);
       if(notify) {
         m_notifier.addFailedTest(testMethod, testResult);
       } 
     }
 
   }
 
   /**
    * @param ite The exception that was just thrown
    * @param expectedExceptions The list of expected exceptions for this
    * test method
    * @return true if the exception that was just thrown is part of the
    * expected exceptions
    */
   private boolean isExpectedException(Throwable ite, Class[] exceptions) {
     if(null == exceptions) {
       return false;
     }
 
     Class realExceptionClass= ite.getClass();
 
     for(int i= 0; i < exceptions.length; i++) {
       if(exceptions[i].isAssignableFrom(realExceptionClass)) {
         return true;
       }
     }
 
     return false;
   }
 
   /**
    * @return Only the ITestNGMethods applicable for this testClass
    */
   private ITestNGMethod[] filterMethods(IClass testClass, ITestNGMethod[] methods) {
     List<ITestNGMethod> vResult= new ArrayList<ITestNGMethod>();
 
     for(ITestNGMethod tm : methods) {
       if(tm.canRunFromClass(testClass)) {
         log(9, "Keeping method " + tm + " for class " + testClass);
         vResult.add(tm);
       }
       else {
         log(9, "Filtering out method " + tm + " for class " + testClass);
       }
     }
 
     ITestNGMethod[] result= vResult.toArray(new ITestNGMethod[vResult.size()]);
 
     return result;
   }
 
   private ITestNGMethod[] filterMethodsUnique(IClass testClass, ITestNGMethod[] methods) {
     if(null == testClass) {
       return methods;
     }
 
     List<ITestNGMethod> vResult= new ArrayList<ITestNGMethod>();
 
     for(ITestNGMethod tm : methods) {
       if(null == testClass) {
         testClass= tm.getTestClass();
       }
 
       if(tm.getTestClass().getName().equals(testClass.getName())) {
         log(9, "        Keeping method " + tm + " for class " + testClass);
 
         vResult.add(tm);
       }
       else {
         log(9, "        Filtering out method " + tm + " for class " + testClass);
       }
     }
 
     ITestNGMethod[] result= vResult.toArray(new ITestNGMethod[vResult.size()]);
 
     return result;
   }
 
   /**
    * @return true if this method depends on certain groups.
    */
   private boolean dependsOnGroups(ITestNGMethod tm) {
     String[] groups= tm.getGroupsDependedUpon();
     boolean result= (null != groups) && (groups.length > 0);
 
     return result;
   }
 
   /**
    * @return true if this method depends on certain groups.
    */
   private boolean dependsOnMethods(ITestNGMethod tm) {
     String[] methods= tm.getMethodsDependedUpon();
     boolean result= (null != methods) && (methods.length > 0);
 
     return result;
   }
 
   private void runConfigurationListeners(ITestResult tr) {
     for(IConfigurationListener icl: m_notifier.getConfigurationListeners()) {
       switch(tr.getStatus()) {
         case ITestResult.SKIP:
           icl.onConfigurationSkip(tr);
           break;
         case ITestResult.FAILURE:
           icl.onConfigurationFailure(tr);
           break;
         case ITestResult.SUCCESS:
           icl.onConfigurationSuccess(tr);
           break;
       }
     }
   }
   
   private void runTestListeners(ITestResult tr) {
     runTestListeners(tr, m_notifier.getTestListeners());
   }
   
   // TODO: move this from here as it is directly called from TestNG
   public static void runTestListeners(ITestResult tr, List<ITestListener> listeners) {
     for (ITestListener itl : listeners) {
       switch(tr.getStatus()) {
         case ITestResult.SKIP: {
           itl.onTestSkipped(tr);
           break;
         }
         case ITestResult.SUCCESS_PERCENTAGE_FAILURE: {
           itl.onTestFailedButWithinSuccessPercentage(tr);
           break;
         }
         case ITestResult.FAILURE: {
           itl.onTestFailure(tr);
           break;
         }
         case ITestResult.SUCCESS: {
           itl.onTestSuccess(tr);
           break;
         }
 
         case ITestResult.STARTED: {
           itl.onTestStart(tr);
           break;
         }
 
         default: {
           assert false : "UNKNOWN STATUS:" + tr;
         }
       }
     }
   }
 
   private static void ppp(String s) {
     System.out.println("[Invoker]" + s);
   }
 
   private void log(int level, String s) {
     Utils.log("Invoker " + Thread.currentThread().hashCode(), level, s);
   }
   
   private class DataTestMethodWorker implements IMethodWorker {
     final Object[] m_instances;
     final ITestNGMethod m_testMethod;
     final ITestNGMethod[] m_beforeMethods;
     final ITestNGMethod[] m_afterMethods;
     final ConfigurationGroupMethods m_groupMethods;
     final Object[] m_parameters;
     final XmlSuite m_suite;
     final Map<String, String> m_allParameterNames;
     
     List<ITestResult> m_results;
     
     public DataTestMethodWorker(Object[] instances, 
         ITestNGMethod testMethod, 
         Object[] params,
         ITestNGMethod[] befores, 
         ITestNGMethod[] afters, 
         ConfigurationGroupMethods groupMethods, 
         XmlSuite suite, 
         Map<String, String> paramNames) {
       m_instances= instances;
       m_testMethod= testMethod;
       m_parameters= params;
       m_beforeMethods= befores;
       m_afterMethods= afters;
       m_groupMethods= groupMethods;
       m_suite= suite;
       m_allParameterNames= paramNames;
     }
     
     public long getMaxTimeOut() {
       return 0;
     }
 
     public void run() {
       m_results= invokeTestMethod(m_instances,
           m_testMethod,
           m_parameters,
           m_suite,
           m_allParameterNames,
           m_testMethod.getTestClass(),
           m_beforeMethods,
           m_afterMethods,
           m_groupMethods);
     }
 
     public List<ITestResult> getTestResults() {
       return m_results;
     }
   }
   
   private static class ParameterBag {
     final Iterator<Object[]> parameterValues;
     final List<ITestResult> errorResults= new ArrayList<ITestResult>();
     
     public ParameterBag(Iterator<Object[]> params, TestResult tr) {
       parameterValues= params;
       if(tr != null) {
         errorResults.add(tr);
       }
     }
     
     public boolean hasErrors() {
       return !errorResults.isEmpty();
     }
   }
 }
