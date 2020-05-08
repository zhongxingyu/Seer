 package com.natpryce.jnirn.proguard;
 
 import com.beust.jcommander.ParameterException;
 import com.natpryce.approvals.IO;
 import com.natpryce.approvals.junit.ApprovalRule;
 import com.natpryce.jnirn.JNIRN;
 import com.natpryce.jnirn.examples.NativeCallback;
 import org.junit.Assume;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
 import org.junit.rules.TestName;
 
 import java.io.File;
 import java.io.IOException;
 
 public class JNIRegisterNativesProguardTest {
     @Rule
     public TestName testName = new TestName();
 
     @Rule
     public StandardOutputStreamLog stdout = new StandardOutputStreamLog();
 
     @Rule
     public ApprovalRule approval = new ApprovalRule("test");
 
     File proguardMapFile = new File("test-input/jnirn.map.txt");
 
     @Test
     public void generatesCCodeToRegisterNativeMethods() throws IOException {
         File cFile = fileNameForTest(".c");
         JNIRN.main("out/test/jnirn/", "-o", cFile.toString(), "-m", proguardMapFile.getAbsolutePath());
         approval.check(IO.readContents(cFile));
     }
 
     @Test
     public void canGenerateHeaderDeclaringFunctionToRegisterNativeMethods() throws IOException {
         File cFile = fileNameForTest(".c");
         File headerFile = fileNameForTest(".h");
        JNIRN.main("out/test/jnirn", "-o", cFile.toString(), "-H", headerFile.toString());
 
         approval.check("Header " + headerFile + ":\n\n" + IO.readContents(headerFile) +
                 "\n\nC Source " + cFile + ":\n\n" + IO.readContents(cFile));
     }
 
     @Test
     public void generatesGlobalVariablesThatReferToMethodsInvokedFromNativeCode() throws IOException {
         File cFile = fileNameForTest(".c");
 
        JNIRN.main("out/test/jnirn", "-o", cFile.toString(), "-C", NativeCallback.class.getName());
 
         approval.check(IO.readContents(cFile));
     }
 
     @Test
     public void canSpecifyNameOfFunctionThatRegistersNativeMethods() throws IOException {
         File cFile = fileNameForTest(".c");
        JNIRN.main("out/test/jnirn", "-o", cFile.toString(), "-p", "Bobbly");
         approval.check(IO.readContents(cFile));
     }
 
     @Test
     public void generatesDependencyRules() throws IOException {
         File mkFile = fileNameForTest(".mk");
         File cFile = fileNameForTest(".c");
        JNIRN.main("out/test/jnirn", "-o", cFile.toString(), "-M", mkFile.toString());
 
         approval.check(IO.readContents(mkFile));
     }
 
     @Test(expected = ParameterException.class)
     public void cannotGenerateMakefileDependenciesIfOutputFileNameNotSpecified() throws IOException {
         File mkFile = fileNameForTest(".mk");
         JNIRN.main("out/test/jnirn", "-M", mkFile.toString());
     }
 
     @Test
     public void reportsUsage() throws IOException {
         // Ant interferes with System.setOut and so makes this test fail.
         Assume.assumeFalse(Boolean.getBoolean("jnirn.run-by-ant"));
 
         JNIRN.main("-?");
         approval.check(stdout.getLog());
     }
 
     public File fileNameForTest(String ext) {
         File file = new File("out/test/noproguard/" + testName.getMethodName() + ext);
         file.getParentFile().mkdirs();
         return file;
     }
 }
