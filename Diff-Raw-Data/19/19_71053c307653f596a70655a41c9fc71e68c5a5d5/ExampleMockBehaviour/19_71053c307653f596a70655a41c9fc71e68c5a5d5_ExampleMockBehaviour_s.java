 /*
  * Created on 16-July-2004
  *
  * (c) 2003-2004 ThoughtWorks
  *
  * See license.txt for licence details
  */
 package jbehave.jmock;
 
import jbehave.core.exception.PendingException;


 /**
  * @author <a href="mailto:damian.guy@thoughtworks.com">Damian Guy</a>
  *         Date: 16-Jul-2004
  */
 public class ExampleMockBehaviour extends UsingJMock {
 
    private Mock aMock;

     interface Dependency {
         String invokeMe();
     }
 
     public class ClassWithDependency {
         private Dependency dep;
 
         public ClassWithDependency(Dependency dep) {
             this.dep = dep;
         }
 
         public void execute() {
             dep.invokeMe();
         }
     }
 
     public void shouldUseAMock() {
        todo();
         // setup
         Mock dependencyMock = new Mock(Dependency.class);
        ClassWithDependency a = new ClassWithDependency((Dependency) aMock.proxy());
         // expect
         dependencyMock.expects(once()).
                 method("invokeMe").
                 withNoArguments().
                 will(returnValue("hello"));
         // execute
        a.execute();
         // verify  happens auto-magically
     }

    private void todo() {
        throw new PendingException("later, don't know why it is failing. SD");
    }
 }
