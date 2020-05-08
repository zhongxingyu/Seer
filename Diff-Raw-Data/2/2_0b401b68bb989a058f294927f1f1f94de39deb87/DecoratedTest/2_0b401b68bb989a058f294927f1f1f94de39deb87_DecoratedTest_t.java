 package org.jboss.weld.compliance.impl.scenarios.decorator.tests;
 
 import javax.inject.Inject;
 import org.jboss.weld.compliance.exception.ComplianceException;
 import org.jboss.weld.compliance.impl.AbstractTest;
 import org.jboss.weld.compliance.impl.scenarios.decorator.util.DecoratedClass;
 
 /**
  *
  * @author Matthieu Clochard
  */
 public class DecoratedTest extends AbstractTest {
 
     @Inject
     private DecoratedClass fieldProduced;
 
     @Override
     public void run() throws ComplianceException {
         if(fieldProduced == null) {
             throw new ComplianceException("the injected value was null (not produced)");
         }
         if(!fieldProduced.getName().equals("DecoratedClass")) {
             throw new ComplianceException("the injected value was wrong (produced elsewhere)");
         }
         fieldProduced.decorate();
         if(!fieldProduced.getName().equals("DecoratedClassAfter")) {
             throw new ComplianceException("the decoration went wrong (decorated method never called or too soon)");
         }
        if(!fieldProduced.getDecoratorName().equals("DecoratorClass")) {
             throw new ComplianceException("the decoration went wrong (decorator never called)");
         }
     }
 
 }
