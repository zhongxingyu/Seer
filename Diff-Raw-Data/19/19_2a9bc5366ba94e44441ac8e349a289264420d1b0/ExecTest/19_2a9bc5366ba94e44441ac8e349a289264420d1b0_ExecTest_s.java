 package gotchas;
 
 import junit.framework.TestCase;
 import gotchas.Exec;
 
 
 public class ExecTest extends TestCase {
 
    public final void test_runWEcho() {
         SystemCommand echo = new EchoCommand();
         Exec exec = new Exec();
         exec.execute(echo);
         assertEquals("does it work", exec.stdout());
     }
 }
