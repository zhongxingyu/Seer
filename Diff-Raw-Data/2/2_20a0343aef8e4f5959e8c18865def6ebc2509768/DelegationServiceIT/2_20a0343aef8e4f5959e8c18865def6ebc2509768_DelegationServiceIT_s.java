 package org.polyforms.delegation.integration;
 
 import java.util.Locale;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.polyforms.delegation.DelegationNotFoundException;
 import org.polyforms.delegation.DelegationService;
 import org.polyforms.delegation.annotation.DelegateTo;
 import org.polyforms.delegation.annotation.DelegatedBy;
 import org.polyforms.delegation.builder.DelegationBuilder;
 import org.polyforms.delegation.builder.DelegationRegistrationException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.stereotype.Component;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @ContextConfiguration("ComponentScannerIT-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 public class DelegationServiceIT {
     @Autowired
     private DelegationService delegationService;
 
     @Autowired
     private Delegator delegator;
 
     @Autowired
     private AbstractDelegator abstractDelegator;
 
     @Autowired
     private AnnotationDelegator annotationDelegator;
 
     @Test
     public void delegateAbstractClass() {
         Assert.assertEquals("1", abstractDelegator.echo("1"));
     }
 
     @Test
     public void beanDelegation() {
         Assert.assertEquals("1", delegator.echo("1"));
     }
 
     @Test
     public void domainDelegation() {
         Assert.assertEquals(4, delegator.length("test"));
     }
 
     @Test
     public void delegationWithMoreParameters() {
         Assert.assertEquals("CN", delegator.getCountry("zh_CN", 1));
     }
 
     @Test
     public void delegateToVoidMethod() {
         delegator.voidMethod("Test");
     }
 
     @Test
     public void delegateByVoidMethod() {
         Assert.assertEquals(0, delegator.length());
     }
 
     @Test
     public void delegatorBy() {
         Assert.assertEquals("1", annotationDelegator.echo("1"));
     }
 
     @Test
     public void delegateTo() {
         Assert.assertEquals(4, annotationDelegator.getLength("test"));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void delegationWithLessParameters() {
         delegator.hello();
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void domainDelegationWithNullArgument() {
         delegator.length(null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void domainDelegationWithoutParameters() {
         delegator.name();
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void cannotDelegateNullMethod() {
         delegationService.canDelegate(null);
     }
 
     @Test(expected = DelegationNotFoundException.class)
     public void delegationNotFound() throws Throwable {
         delegationService.delegate(null, Delegatee.class.getMethod("hello", String.class));
     }
 
     @Test(expected = DelegateException.class)
     public void testException() {
         delegator.exception();
     }
 
     @Test(expected = DelegateException.class)
     public void testExceptionWithName() {
         delegator.exceptionWithName(true);
     }
 
     @Test(expected = RuntimeException.class)
     public void testExceptionWithoutException() {
         delegator.exceptionWithName(false);
     }
 
     @Test(expected = DelegationRegistrationException.class)
     public void registerInexistentDelegator() {
         new DelegationBuilder() {
             @Override
             public void registerDelegations() {
                 delegate(Delegator.class, "inexistentMethod");
             }
         }.registerDelegations();
     }
 
     @Test(expected = DelegationRegistrationException.class)
     public void registerInexistentDelegatee() {
         new DelegationBuilder() {
             @Override
             public void registerDelegations() {
                 delegate(Delegator.class, "length").to(String.class, "inexistentMethod");
             }
         }.registerDelegations();
     }
 
     @Test(expected = DelegationRegistrationException.class)
     public void registerClassDelegator() {
         new DelegationBuilder() {
             @Override
             public void registerDelegations() {
                 delegate(Delegatee.class);
             }
         }.registerDelegations();
     }
 
     @Test(expected = DelegationRegistrationException.class)
     public void withNameBeforeTo() {
         new DelegationBuilder() {
             @Override
             public void registerDelegations() {
                 delegate(Delegator.class).withName("bean");
             }
         }.registerDelegations();
     }
 
     @Component
     public static class TestDelegationBuilder extends DelegationBuilder {
         @Override
         public void registerDelegations() {
             delegate(AbstractDelegator.class).to(Delegatee.class);
             delegate(Delegator.class, "length", String.class).to(String.class);
            delegate(Delegator.class).to(Delegatee.class).withName("delegationServiceTest.Delegatee");
             delegate(Delegator.class, "name").to(String.class, "toString");
             delegate(Delegator.class, "name").to(String.class, "length");
             delegate(Delegator.class, "getCountry").to(Locale.class);
             delegate(Delegator.class, "length").to(Delegatee.class, "voidMethod");
             delegate(Delegator.class, "voidMethod").to(String.class, "length");
         }
     }
 
     public static interface Delegator {
         String echo(String string);
 
         int length(String string);
 
         String name();
 
         String getCountry(String locale, int start);
 
         String hello();
 
         void voidMethod(String string);
 
         int length();
 
         void exception() throws IllegalArgumentException, DelegateException;
 
         void exceptionWithName(boolean exception) throws DelegateException;
     }
 
     @Component
     public static abstract interface AbstractDelegator extends AbstractInterface<String> {
     }
 
     public static interface AbstractInterface<T> {
         T echo(T String);
     }
 
     @Component
     public static interface AnnotationDelegator {
         String echo(String string);
 
         @DelegateTo(value = String.class, methodName = "length")
         int getLength(String string);
     }
 
     @Component
     public static class AnnotationDelegatee {
         @DelegatedBy(AnnotationDelegator.class)
         public Integer echo(final Integer number) {
             return number;
         }
     }
 
     public static interface GenericDelegatee<T extends Number> {
         T echo(T object);
     }
 
     public static class GenericDelegateeImpl<T extends Number> implements GenericDelegatee<T> {
         public T echo(final T object) {
             if (object instanceof Integer) {
                 return object;
             }
             return null;
         }
     }
 
     @Component
     public static class Delegatee extends GenericDelegateeImpl<Integer> {
         public String hello(final String name) {
             return "hello " + name;
         }
 
         public void voidMethod() {
         }
 
         public void exception() throws IllegalArgumentException, MockException {
             throw new MockException();
         }
 
         public void exceptionWithName(final boolean exception) {
             if (exception) {
                 throw new DelegateException();
             } else {
                 throw new RuntimeException();
             }
         }
 
         @SuppressWarnings("serial")
         private class MockException extends RuntimeException {
         }
 
         @SuppressWarnings("serial")
         private static class DelegateException extends RuntimeException {
         }
     }
 
     @SuppressWarnings("serial")
     public static class DelegateException extends RuntimeException {
     }
 }
 
 @Configuration
 class Config {
     @Bean
     public Object configuredObject() {
         return new Object();
     }
 }
