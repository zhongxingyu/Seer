 package org.polyforms.delegation.integration;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.polyforms.delegation.DelegateTo;
 import org.polyforms.delegation.DelegationService;
 import org.polyforms.delegation.DelegatorRegister;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.stereotype.Component;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @ContextConfiguration("ComponentScannerIT-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 public class DelegationServiceIT {
     private final StringWrapper string = new StringWrapper("test");
     @Autowired
     private DelegationService delegationService;
     @Autowired
     private Delegator delegator;
     @Autowired
     private AbstractInterface<String> abstractInterface;
     @Autowired
     private AnnotationDelegator annotationDelegator;
 
     @Test
     public void delegateAbstractClass() {
         Assert.assertEquals("1", abstractInterface.echo("1"));
     }
 
     @Test
     public void beanDelegation() {
         Assert.assertEquals("test", delegator.echo(string));
     }
 
     @Test
     public void collectionParameters() {
         final Set<Long> numbers = new HashSet<Long>();
         numbers.add(1L);
         numbers.add(2L);
         final List<String> strings = delegator.collection(numbers);
         Assert.assertEquals(2, strings.size());
        Assert.assertEquals("1", strings.get(0));
        Assert.assertEquals("2", strings.get(1));
     }
 
     @Test
     public void domainDelegation() {
         Assert.assertEquals(4, delegator.length(string));
     }
 
     @Test
     public void domainDelegationWithParameter() {
         Assert.assertEquals("4", delegator.echo(string, "4"));
     }
 
     @Test
     public void delegationWithMoreParameters() {
         Assert.assertEquals("CN", delegator.getCountry(Locale.CHINA, 1));
     }
 
     @Test
     public void delegateToVoidMethod() {
         delegator.voidMethod(string);
     }
 
     @Test
     public void delegateByVoidMethod() {
         Assert.assertEquals(0, delegator.length());
     }
 
     @Test
     public void delegateTo() {
         Assert.assertEquals(4, annotationDelegator.length(string));
     }
 
     @Test
     public void parameterByType() {
         Assert.assertEquals("test2", delegator.byType(2, "test"));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void delegationWithLessParameters() {
         delegator.hello();
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void domainDelegationWithNullArgument() {
         delegator.length(null);
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void domainDelegationWithoutParameters() {
         delegator.name();
     }
 
     @Test
     public void cannotDelegateNullClass() {
         Assert.assertFalse(delegationService.supports(null, null));
     }
 
     @Test
     public void cannotDelegateNullMethod() {
         Assert.assertFalse(delegationService.supports(Delegator.class, null));
     }
 
     @Test(expected = DelegateException.class)
     public void testMappedException() {
         delegator.exception();
     }
 
     @Test(expected = DelegateException.class)
     public void testExceptionWithName() {
         delegator.exceptionWithName(true);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testExceptionWithoutMapping() {
         delegator.exceptionWithName(false);
     }
 
     @Component
     public static class TestDelegationBuilder extends DelegatorRegister<AbstractDelegator> {
         @Override
         public void register(final AbstractDelegator source) {
             with(new DelegateeRegister<Delegatee>() {
                 @Override
                 public void register(final AbstractDelegator delegator) {
                     delegate(delegator.echo(null)).echo(at(Integer.class, 0));
                 }
             });
         }
     }
 
     @Component
     public static class DelegateeDelegationBuilder extends DelegatorRegister<Delegator> {
         @Override
         public void register(final Delegator source) {
             delegate();
             this.<StringWrapper> delegate(source.echo(null)).toString();
             this.<StringWrapper> delegate(source.echo(null, null)).toString(0);
             source.voidMethod(null);
             this.<StringWrapper> delegate().length();
 
             with(new DelegateeRegister<Delegatee>("delegationServiceIT.Delegatee") {
                 @Override
                 public void register(final Delegator delegator) {
                     delegator.exceptionWithName(false);
                     delegate();
                     delegator.exception();
                     delegate();
                     map(DelegateException.class, MockException.class);
                     delegate(delegator.length()).voidMethod();
                     delegate(delegator.hello());
                     delegate(delegator.byType(0, null)).join(null, 0);
                     delegate(delegator.collection(null));
                 }
             });
         }
     }
 
     public static interface Delegator {
         String echo(StringWrapper string);
 
         List<String> collection(Set<Long> numbers);
 
         int length(StringWrapper string);
 
         String echo(StringWrapper string, String number);
 
         String name();
 
         String getCountry(Locale locale, int start);
 
         String hello();
 
         void voidMethod(StringWrapper string);
 
         int length();
 
         String byType(int order, String name);
 
         void exception() throws DelegateException;
 
         void exceptionWithName(boolean exception) throws DelegateException;
     }
 
     public static class StringWrapper {
         private String string;
 
         protected StringWrapper() {
         }
 
         public StringWrapper(final String string) {
             this.string = string;
         }
 
         public int length() {
             return string.length();
         }
 
         public String toString(final Integer number) {
             return number.toString();
         }
 
         @Override
         public String toString() {
             return string.toString();
         }
     }
 
     @Component
     public static abstract class AbstractDelegator implements AbstractInterface<String> {
     }
 
     public interface AbstractInterface<T> {
         T echo(T String);
     }
 
     @Component
     public static interface AnnotationDelegator {
         String echo(String string);
 
         @DelegateTo
         int length(StringWrapper string);
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
 
        public List<Integer> collection(List<Integer> numbers) {
             return numbers;
         }
 
         public void voidMethod() {
         }
 
         public String join(final String name, final Integer number) {
             return name + number;
         }
 
         public void exception() throws IllegalArgumentException, MockException {
             throw new MockException();
         }
 
         public void exceptionWithName(final boolean exception) {
             if (exception) {
                 throw new DelegateException();
             } else {
                 throw new IllegalArgumentException();
             }
         }
 
         @SuppressWarnings("serial")
         private static class DelegateException extends RuntimeException {
         }
     }
 
     @SuppressWarnings("serial")
     public static class DelegateException extends RuntimeException {
     }
 
     @SuppressWarnings("serial")
     public static class MockException extends RuntimeException {
     }
 }
 
 @Configuration
 class Config {
     @Bean
     public Object configuredObject() {
         return new Object();
     }
 }
