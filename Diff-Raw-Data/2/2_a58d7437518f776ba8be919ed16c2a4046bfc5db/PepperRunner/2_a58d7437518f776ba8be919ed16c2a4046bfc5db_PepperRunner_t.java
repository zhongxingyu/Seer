 package org.pepper.core;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.junit.runner.Description;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 import org.junit.runners.model.Statement;
 import org.pepper.core.annotations.Given;
 import org.pepper.core.annotations.Pending;
 import org.pepper.core.annotations.Then;
 import org.pepper.core.annotations.When;
 import org.pepper.core.models.GivenFrameworkMethod;
 import org.pepper.core.models.StepMethod;
 import org.pepper.core.models.ThenFrameworkMethod;
 import org.pepper.core.models.WhenFrameworkMethod;
 
 public class PepperRunner extends BlockJUnit4ClassRunner {
 
   private Class<?> klass;
 
   public PepperRunner(Class<?> klass) throws InitializationError {
     super(klass);
 
     this.klass = klass;
   }
 
   private StepDefinition stepDef;
   private String path;
 
   private void newStepDefinition() {
     try {
       stepDef = (StepDefinition) getTestClass().getOnlyConstructor().newInstance();
 
       // This is where you can pass configuration information from the StepDefinition to the PepperRunner.
       if (path == null) {
         path = new File(stepDef.getFeaturesFolder() + "/" + stepDef.getFeatureName() + stepDef.getFeatureExtension()).getAbsolutePath();
       }
     }
     catch (InvocationTargetException invocationTarget) {
       invocationTarget.printStackTrace();
     }
     catch (IllegalAccessException illegalAccess) {
       illegalAccess.printStackTrace();
     }
     catch (InstantiationException instantiation) {
       instantiation.printStackTrace();
     }
   }
 
   public static String generateStub(String line) {
     StringBuilder strBuilder = new StringBuilder();
 
     // System.out.println("@Pending");
     strBuilder.append("@Pending\n");
 
     // Given = 5 characters long
     // When = 4 characters long
     // Then = 4 characters long
     int length = line.startsWith("Given") ? 5 : 4;
 
     // System.out.println("@" + line.substring(0, length) + "(\"" + line.substring(length + 1) + "\")");
     strBuilder.append("@");
     strBuilder.append(line.substring(0, length));
     strBuilder.append("(\"");
     strBuilder.append(line.substring(length + 1));
     strBuilder.append("\")\n");
 
     // System.out.println("public void " + StringUtils.camelCase(line) + "() {");
     strBuilder.append("public void ");
     line = line.replaceAll("\\\\\"","");    // remove all instances of \"
     strBuilder.append(StringUtils.camelCase(line));
     strBuilder.append("() {\n");
 
     // System.out.println("}\n");
     strBuilder.append("}\n");
 
     // Note: This method returns a String so it can be tested.
     // I left the System.out.println statements, because they're slightly easier to read.
 
     return strBuilder.toString();
   }
 
   private void runStep(String line, List<? extends StepMethod> stepMethods, RunNotifier runNotifier) {
     for (StepMethod stepMethod : stepMethods) {
       if (stepMethod.matches(line)) {
         PepperRunner.this.runChild(stepMethod, runNotifier);
         return;
       }
     }
 
    System.out.println(generateStub(line));
   }
   
   // Invokes Step methods in StepDefinition
   @Override
   public void run(final RunNotifier runNotifier) {
 
     newStepDefinition();
     final PepperRunnerListener runListener = new PepperRunnerListener();
     runNotifier.addListener(runListener);
 
     // keep track of last step method, this is needed to handle And steps
     char ch = '?';  // TODO: this should probably be an enum, not a char
 
     try {
       File file = new File(path);
       Scanner scanner = new Scanner(file); // <- FileNotFoundException
       String line;
 
       while (scanner.hasNextLine()) {
         line = scanner.nextLine().trim();
         line = line.replaceAll("\\\"", "\\\\\""); // escape quotes
         runListener.setLine(line);
         
         if(line.startsWith("Given")) {
           ch = 'G';
           runStep(line, givenMethods, runNotifier);
         }
         else if(line.startsWith("When")) {
           ch = 'W';
           runStep(line, whenMethods, runNotifier);
         }
         else if(line.startsWith("Then")) {
           ch = 'T';
           runStep(line, thenMethods, runNotifier);
         }
         else if(line.startsWith("And")) {
           // notice that it replaces the beginning of the line (ie. "And blah blah") with the appropriate step (ie. "Given blah blah)
           if(ch == 'G') {
             runStep("Given " + line.substring(4), givenMethods, runNotifier);
           }
           else if(ch == 'W') {
             runStep("When " + line.substring(4), whenMethods, runNotifier);
           }
           else if(ch == 'T') {
             runStep("Then " + line.substring(4), thenMethods, runNotifier);
           }
         }
         else {
           System.out.println(line);
           ch = '?';
           if (line.startsWith("Scenario:")) {
             newStepDefinition();
           }
           else if(line.startsWith("Scenario Template:")) {
             scenarioTemplate(scanner, runNotifier);
           }
         }
       }
       scanner.close();
     }
     catch (FileNotFoundException fileNotFound) {
       fileNotFound.printStackTrace();
     }
   }
 
   private void scenarioTemplate(Scanner scanner, RunNotifier runNotifier) {
     // read lines in Scenario Template
     String line;
     List<String> scenarioTemplate = new ArrayList<String>();
 
     while (scanner.hasNextLine()) {
       line = scanner.nextLine().trim();
       if (line.equals("Content Table:")) {
         break; // from while loop
       }
       scenarioTemplate.add(line);
     }
 
     // read Content Table's "header"
     line = scanner.nextLine().trim();
     List<String> keys = parseRow(line);
     Map<String, List<String>> contentTable = new HashMap<String, List<String>>();
     for(String key : keys) {
       contentTable.put(key, new ArrayList<String>());
     }
 
     // read Content Table's "body"
     String key;
     List<String> list;
     //int column;
     int numRows = 0;
 
     while (scanner.hasNextLine()) {
       line = scanner.nextLine().trim();
 
       if (!line.startsWith("|")) {
         break; // from while loop
       }
 
       numRows++;
       // column = 0;
       List<String> data = parseRow(line);
       for(int column = 0; column < data.size(); column++) {
         key = keys.get(column);
         list = contentTable.get(key);
         list.add(data.get(column));
       }
 
     } // end of while (scanner.hasNextLine()) {
 
     // run Scenario Template
     int row = 0;
 
     while (row < numRows) {
       for (String strLine : scenarioTemplate) {
         for (String strKey : contentTable.keySet()) {
           list = contentTable.get(strKey);
           strLine = strLine.replaceAll("<" + strKey + ">", list.get(row));
         }
 
         if (strLine.startsWith("Given")) {
           runStep(strLine, givenMethods, runNotifier);
         }
         else if (strLine.startsWith("When")) {
           runStep(strLine, whenMethods, runNotifier);
         }
         else if (strLine.startsWith("Then")) {
           runStep(strLine, thenMethods, runNotifier);
         }
       }
       System.out.println();
       row++;
     }
   }
 
   public static List<String> parseRow(String line) {
     List<String> list = new ArrayList<String>();
     StringBuilder strBuilder = null;
 
     for (char ch : line.toCharArray()) {
       if (ch == '|') {
         if (strBuilder != null) {
           list.add(strBuilder.toString().trim());
         }
         strBuilder = new StringBuilder();
       }
       else {
         strBuilder.append(ch);
       }
     }
 
     return list;
   }
 
   @Deprecated
   @Override
   protected void validateInstanceMethods(List<Throwable> errors) {
     // This method is called by collectInitializationErrors(List<Throwable> errors),
     // which is called by validate() method,
     // which is called inside ParentRunner's ParentRunner(Class<?> testClass) constructor.
 
     // For some strange reason the given-when-then maps aren't initialized when this method is called.
     // The JUnit API says this method will go away in the future. So I think it's safe to comment out.
   }
 
   private List<GivenFrameworkMethod> givenMethods = new ArrayList<GivenFrameworkMethod>();
   private List<WhenFrameworkMethod> whenMethods = new ArrayList<WhenFrameworkMethod>();
   private List<ThenFrameworkMethod> thenMethods = new ArrayList<ThenFrameworkMethod>();
 
   @Override
   protected List<FrameworkMethod> getChildren() {
     List<FrameworkMethod> list = new ArrayList<FrameworkMethod>();
 
     for(Method method : klass.getMethods()) {
       if(method.getAnnotation(Given.class) != null) {
         GivenFrameworkMethod givenMethod = new GivenFrameworkMethod(method);
         givenMethods.add(givenMethod);
         list.add(givenMethod);
       }
       else if(method.getAnnotation(When.class) != null) {
         WhenFrameworkMethod whenMethod = new WhenFrameworkMethod(method);
         whenMethods.add(whenMethod);
         list.add(whenMethod);
       }
       if(method.getAnnotation(Then.class) != null) {
         ThenFrameworkMethod thenMethod = new ThenFrameworkMethod(method);
         thenMethods.add(thenMethod);
         list.add(thenMethod);
       }
     }
 
     return list;
   }
 
   List<Object> params = new ArrayList<Object>();
 
   // Invokes a Step method
   @Override
   protected Statement methodBlock(final FrameworkMethod method) {
 
     return new Statement() {
       @Override
       public void evaluate() throws Throwable {
         // FrameworkMethod - Object invokeExplosively(Object target, Object... params)
         method.invokeExplosively(stepDef, params.toArray());
       }
     };
   }
 
   @Override
   protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
     Description description = describeChild(method);
 
     // if (method.getAnnotation(Ignore.class) != null) {   // <- BlockJUnit4ClassRunner's version
     if (method.getAnnotation(Pending.class) != null) {
       notifier.fireTestIgnored(description);
     }
     else {
       runLeaf(methodBlock(method), description, notifier);
     }
   }
 }
