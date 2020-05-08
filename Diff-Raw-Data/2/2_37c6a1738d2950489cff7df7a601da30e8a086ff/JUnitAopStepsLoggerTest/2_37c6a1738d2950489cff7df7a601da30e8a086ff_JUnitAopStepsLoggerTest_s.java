 /*
  * Copyright 2012 CoreMedia AG
  *
  * This file is part of Joala.
  *
  * Joala is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Joala is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.joala.bdd.aop;
 
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.Matchers;
 import org.hamcrest.SelfDescribing;
 import org.junit.After;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.internal.AssumptionViolatedException;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 import java.util.Collection;
 
 import static org.hamcrest.core.AllOf.allOf;
 import static org.hamcrest.core.StringContains.containsString;
 import static org.hamcrest.number.OrderingComparison.greaterThan;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import static org.junit.Assume.assumeThat;
 
 /**
  * @since 6/1/12
  */
 @SuppressWarnings("ProhibitedExceptionDeclared")
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class JUnitAopStepsLoggerTest {
   @Inject
   @Singleton
   private Steps _;
 
   @Before
   public void setUp() throws Exception {
     JUnitAopStepsLoggerTestAppender.clearEvents();
   }
 
   @After
   public void tearDown() throws Exception {
     JUnitAopStepsLoggerTestAppender.clearEvents();
   }
 
   public void assertMessagesContains(final String expectedString) {
     assertThatMessages(Matchers.containsString(expectedString));
   }
 
   public void assertMessagesDontContain(final String expectedString) {
     assertThatMessages(Matchers.not(Matchers.containsString(expectedString)));
   }
 
  private void assertThatMessages(final Matcher<String> matcher) {
     final Collection<ILoggingEvent> events = JUnitAopStepsLoggerTestAppender.getEvents();
     assertThat("Should have recorded some logging events. Please verify logback configuration.", events.size(), greaterThan(0));
     boolean passedAtLeastOnce = false;
     AssertionError lastError = null;
     for (final ILoggingEvent event : events) {
       try {
         assertThat(event.getFormattedMessage(), matcher);
         passedAtLeastOnce = true;
       } catch (AssertionError e) {
         lastError = e;
       }
     }
     if (!passedAtLeastOnce) {
       throw lastError;
     }
   }
 
   @Test
   public void testGivenPointcut() throws Exception {
     assumeThat(JUnitAopStepsLoggerTestAppender.getEvents().size(), Matchers.equalTo(0));
     _.given_this_is_a_test();
     assertMessagesContains("given this is a test");
   }
 
   @Test
   public void testSingleArgumentLogged() throws Exception {
     final String TEST_VALUE = "cxyvhkah";
     final SelfDescribing pseudoRef = new TestSelfDescribing(TEST_VALUE);
     _.given_this_is_a_step_with_a_logged_argument(pseudoRef);
     assertMessagesContains(TEST_VALUE);
   }
 
   @Test
   public void testSingleArgumentNotLogged() throws Exception {
     final String TEST_VALUE = "cxyvhkah";
     _.given_this_is_a_step_with_a_not_logged_argument(TEST_VALUE);
     assertMessagesDontContain(TEST_VALUE);
   }
 
   @Test
   public void testMultipleLoggedArgumentsAreLogged() throws Exception {
     final String TEST_VALUE_1 = "cxyvhkah";
     final String TEST_VALUE_2 = "sdfahvhs";
     final SelfDescribing pseudoRef1 = new TestSelfDescribing(TEST_VALUE_1);
     final SelfDescribing pseudoRef2 = new TestSelfDescribing(TEST_VALUE_2);
     _.given_this_is_a_step_with_two_logged_arguments(pseudoRef1, pseudoRef2);
     assertThatMessages(allOf(
             containsString(TEST_VALUE_1),
             containsString(TEST_VALUE_2)
     ));
   }
 
   @Test
   public void testMultipleLoggedVarargArgumentsAreLogged() throws Exception {
     final String TEST_VALUE_1 = "cxyvhkah";
     final String TEST_VALUE_2 = "sdfahvhs";
     final SelfDescribing pseudoRef1 = new TestSelfDescribing(TEST_VALUE_1);
     final SelfDescribing pseudoRef2 = new TestSelfDescribing(TEST_VALUE_2);
     _.given_this_is_a_step_with_two_logged_vararg_arguments(pseudoRef1, pseudoRef2);
     assertThatMessages(allOf(
             containsString(TEST_VALUE_1),
             containsString(TEST_VALUE_2)
     ));
   }
 
   @Test
   public void testWhenPointcut() throws Exception {
     assumeThat(JUnitAopStepsLoggerTestAppender.getEvents().size(), Matchers.equalTo(0));
     _.when_this_is_a_test();
     assertMessagesContains("when this is a test");
   }
 
   @Test
   public void testFailingWhenPointcut() throws Exception {
     assumeThat(JUnitAopStepsLoggerTestAppender.getEvents().size(), Matchers.equalTo(0));
     try {
       _.when_assumption_fails();
     } catch (AssumptionViolatedException ignored) {
     }
     assertMessagesContains("when assumption fails");
     assertMessagesContains("FAILED");
   }
 
   @Test
   public void testThenPointcut() throws Exception {
     assumeThat(JUnitAopStepsLoggerTestAppender.getEvents().size(), Matchers.equalTo(0));
     _.then_this_is_a_test();
     assertMessagesContains("then this is a test");
   }
 
   @Test
   public void testFailingThenPointcut() throws Exception {
     assumeThat(JUnitAopStepsLoggerTestAppender.getEvents().size(), Matchers.equalTo(0));
     try {
       _.then_I_fail();
     } catch (AssertionError ignored) {
     }
     assertMessagesContains("then I fail");
     assertMessagesContains("FAILED");
   }
 
   @Named
   @Singleton
   public static class Steps {
     public void given_this_is_a_test() {
     }
 
     public void when_this_is_a_test() {
     }
 
     public void when_assumption_fails() {
       Assume.assumeTrue(false);
     }
 
     public void then_this_is_a_test() {
     }
 
     public void then_I_fail() {
       fail("I will fail.");
     }
 
     @SuppressWarnings("UnusedParameters")
     public void given_this_is_a_step_with_a_logged_argument(final SelfDescribing pseudoRef) {
     }
 
     @SuppressWarnings("UnusedParameters")
     public void given_this_is_a_step_with_a_not_logged_argument(final String val) {
     }
 
     @SuppressWarnings("UnusedParameters")
     public void given_this_is_a_step_with_two_logged_arguments(final SelfDescribing pseudoRef1, final SelfDescribing pseudoRef2) {
     }
 
     @SuppressWarnings("UnusedParameters")
     public void given_this_is_a_step_with_two_logged_vararg_arguments(final SelfDescribing... pseudoRef1) {
     }
   }
 
   private static class TestSelfDescribing implements SelfDescribing {
     private final String value;
 
     private TestSelfDescribing(final String value) {
       this.value = value;
     }
 
     @Override
     public void describeTo(final Description description) {
       description.appendText(value);
     }
   }
 }
