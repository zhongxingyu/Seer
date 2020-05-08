 package testutils;
 
 import AST.*;
 import java.io.File;
 import java.util.LinkedList;
 
 public class Tester extends Frontend {
 
     public static boolean compile(String fileName) {
         String[] args = {fileName};
         boolean res =  false;
         try {
            res = new Tester().process(
                 args,
                 new BytecodeParser(),
                 new JavaParser() {
 
                     public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                         return new parser.JavaParser().parse(is, fileName);
                     }
                 });
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
         return res;
 
     }
 
     protected void processErrors(java.util.Collection errors, CompilationUnit unit) {
         //System.out.println(unit.dumpTreeNoRewrite());
         //System.out.println(unit.toString());
         if (Tester.shouldBeOk || Tester.isSingle) super.processErrors(errors, unit);
     }
 
     protected void processWarnings(java.util.Collection warnings, CompilationUnit unit) {
         if (Tester.shouldBeOk || Tester.isSingle) super.processWarnings(warnings, unit);
     }
 
     protected void processNoErrors(CompilationUnit unit) {
         //System.out.println(unit.dumpTreeNoRewrite());
         //System.out.println(unit.toString());
     }
 
     /*Tester() {
 
     }*/
 
     public static boolean stopFirst = false;
     public static boolean isSingle = false;
     public static int total = 0;
     public static int totalFail = 0;
     public static int totalOK = 0;
     public static boolean shouldBeOk;
     public static LinkedList<String> notPassed = new LinkedList<String>();
 
     public static void doFile(String fileName) {
         if (stopFirst && totalFail>0) return;
         total+=1;
         shouldBeOk = !fileName.contains("_fail");
         boolean ok = Tester.compile(fileName);
         if (shouldBeOk != ok) {
             totalFail+=1;
             String desiredResult = shouldBeOk==true ? "passed" : "failed";
             String result  = shouldBeOk!=true ? "passed" : "failed";
             System.out.println("Test for filename '"+fileName+"' ("+total+") should have " + desiredResult + ", but " + result);
             notPassed.add(fileName);
         }
         else { totalOK +=1; }
     }
 
     public static void doDir(String dir) {
         File f = new File(dir);
         if (f.isDirectory()) {
             File[] files = f.listFiles(new FileEndingFilter("java"));
             for (int i = 0; i < files.length; i++) {
                 String fileName = files[i].getName();
                 doFile(dir + fileName);
             }
         } else if (f.isFile()) {
             isSingle = true;
             doFile(dir);
         }
     }
 
     public static void main(String[] args) {
         for (String f : args) {
             if (f.equals("--stopfirst")) {
                 stopFirst = true;
                 continue;
             }
             doDir(f);
         }
         if (totalOK == total) { System.out.println("*** All "+total+" tests passed."); }
         else {
             System.out.println(String.format("\n*** %d of %d tests passed, %d failed.", totalOK, total, totalFail));
             System.out.println("The following files did not pass as expected:");
             for (String f : notPassed) {
                 System.out.println("    ant testsingle -Dname=" + f);
             }
         }
     }
 }
