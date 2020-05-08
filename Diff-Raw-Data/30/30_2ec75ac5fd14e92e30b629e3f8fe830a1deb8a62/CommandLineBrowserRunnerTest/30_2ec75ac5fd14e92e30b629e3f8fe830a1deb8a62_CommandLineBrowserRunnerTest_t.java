 package com.google.jstestdriver.browser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.google.common.collect.Lists;
 import com.google.jstestdriver.ProcessFactory;
 
 public class CommandLineBrowserRunnerTest extends TestCase {
   public void testStartAndStop() throws Exception {
     final String browserPath = "/foo/bar";
     final String browserArgs = "--helloWorld";
     final String url = "htpp://localhost:42242";
 
     FakeProcessFactory processFactory = new FakeProcessFactory();
     CommandLineBrowserRunner runner =
        new CommandLineBrowserRunner(browserPath, browserArgs, processFactory, "not os x");
 
     runner.startBrowser(url);
     assertEquals(1, processFactory.processStubs.size());
     assertEquals(browserPath,
         processFactory.processStubs.get(0).commands[0]);
     assertEquals(Lists.newArrayList(new String[]{browserPath, browserArgs, url}),
         Lists.newArrayList(processFactory.processStubs.get(0).commands));
   }
 
   public void testNoArgs() throws Exception {
     final String browserPath = "/foo/bar";
     final String browserArgs = "";
     final String url = "htpp://localhost:42242";
 
     FakeProcessFactory processFactory = new FakeProcessFactory();
     CommandLineBrowserRunner runner =
        new CommandLineBrowserRunner(browserPath, browserArgs, processFactory, "not os x");
 
     runner.startBrowser(url);
     assertEquals(1, processFactory.processStubs.size());
     assertEquals(browserPath,
         processFactory.processStubs.get(0).commands[0]);
     assertEquals(url,
         processFactory.processStubs.get(0).commands[1]);
   }
 
   public void testUrlParam() throws Exception {
     final String browserPath = "/foo/bar";
     final String browserArgs = "--app=%s";
     final String url = "htpp://localhost:42242";
 
     FakeProcessFactory processFactory = new FakeProcessFactory();
     CommandLineBrowserRunner runner =
        new CommandLineBrowserRunner(browserPath, browserArgs, processFactory, "not os x");
 
     runner.startBrowser(url);
     assertEquals(1, processFactory.processStubs.size());
     assertEquals(browserPath,
         processFactory.processStubs.get(0).commands[0]);
     assertEquals("--app=" + url,
         processFactory.processStubs.get(0).commands[1]);
   }
 
   private final class FakeProcessFactory implements ProcessFactory {
     public List<ProcessStub> processStubs = Lists.newLinkedList();
 
     @SuppressWarnings("unused")
     public Process start(String... commands) throws IOException {
       ProcessStub processStub = new ProcessStub(commands);
       processStubs.add(processStub);
       return processStub;
     }
   }
 
   private final static class ProcessStub extends Process {
 
     public final String[] commands;
 
     public ProcessStub(String[] commands) {
       this.commands = commands;
     }
     @Override
     public void destroy() {
     }
 
     @Override
     public int exitValue() {
       return 0;
     }
 
     @Override
     public InputStream getErrorStream() {
       return null;
     }
 
     @Override
     public InputStream getInputStream() {
       return null;
     }
 
     @Override
     public OutputStream getOutputStream() {
       return null;
     }
 
     @Override
     public int waitFor() {
       return 0;
     }
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + Arrays.hashCode(commands);
       return result;
     }
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null)
         return false;
       if (getClass() != obj.getClass())
         return false;
       ProcessStub other = (ProcessStub) obj;
       if (!Arrays.equals(commands, other.commands))
         return false;
       return true;
     }
     @Override
     public String toString() {
       return String.format("%s(%s)", getClass().getSimpleName(), Arrays.toString(commands));
     }
   }
 }
