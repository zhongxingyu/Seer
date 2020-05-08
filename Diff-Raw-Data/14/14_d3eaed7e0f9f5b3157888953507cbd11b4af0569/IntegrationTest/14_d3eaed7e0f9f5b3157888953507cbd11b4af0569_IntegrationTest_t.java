 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.integration;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.Result;
 import org.junit.runner.notification.Failure;
 
 import de.fzi.cjunit.ConcurrentError;
 import de.fzi.cjunit.ConcurrentJUnit;
 import de.fzi.cjunit.DeadlockError;
 import de.fzi.cjunit.integration.testclasses.ConcurrentTestWithConcurrencyBug;
 import de.fzi.cjunit.integration.testclasses.ConcurrentTestWithDeadlock;
 import de.fzi.cjunit.integration.testclasses.ConcurrentTestWithSequentialBug;
 import de.fzi.cjunit.integration.testclasses.SequentialTestWithFailure;
 import de.fzi.cjunit.integration.testclasses.SuccessfulTests;
 import de.fzi.cjunit.testutils.TestException;
 
 
 public class IntegrationTest {
 
 	PrintStream originalSystemOut;
 
 	@Before
 	public void disableSystemOut() {
 		originalSystemOut = System.out;
 		System.setOut(new PrintStream(new ByteArrayOutputStream()));
 	}
 
 	@After
 	public void enableSystemOut() {
 		System.setOut(originalSystemOut);
 	}
 
 	@Test
 	public void successfulTests() {
 		Class<?>[] classes = new Class<?>[] { SuccessfulTests.class };
 		Result result = ConcurrentJUnit.runClasses(classes);
 
 		assertThat("test method was invoked", SuccessfulTests.invoked,
 				is(true));
 		assertThat("number of tests", result.getRunCount(), equalTo(7));
		assertThat("number of failures", result.getFailureCount(),
 				equalTo(0));
 	}
 
 	@Test
 	public void failingTest() {
 		Class<?>[] classes = new Class<?>[] {
 				SequentialTestWithFailure.class };
 		Result result = ConcurrentJUnit.runClasses(classes);
 
 		assertThat("test method was invoked",
 				SequentialTestWithFailure.invoked, is(true));
 		assertThat("number of tests", result.getRunCount(), equalTo(1));
		assertThat("number of failures", result.getFailureCount(),
 				equalTo(1));
 		Failure failure = result.getFailures().get(0);
 		assertThat("exception's type", failure.getException(),
 				instanceOf(TestException.class));
 	}
 
 	@Test
 	public void concurrentTestWithSequentialBug() {
 		Class<?>[] classes = new Class<?>[] {
 				ConcurrentTestWithSequentialBug.class };
 		Result result = ConcurrentJUnit.runClasses(classes);
 
 		assertThat("number of tests", result.getRunCount(), equalTo(1));
		assertThat("number of failures", result.getFailureCount(),
 				equalTo(1));
 		Failure failure = result.getFailures().get(0);
 		assertThat("exception's type", failure.getException(),
 				instanceOf(AssertionError.class));
 	}
 
 	@Test
 	public void concurrentTestWithConcurrencyBug() {
 		Class<?>[] classes = new Class<?>[] {
 				ConcurrentTestWithConcurrencyBug.class };
 		Result result = ConcurrentJUnit.runClasses(classes);
 
 		assertThat("number of tests", result.getRunCount(), equalTo(1));
		assertThat("number of failures", result.getFailureCount(),
 				equalTo(1));
 		Failure failure = result.getFailures().get(0);
 		assertThat("exception's type", failure.getException(),
 				instanceOf(ConcurrentError.class));
 		assertThat("causing exception's type",
 				failure.getException().getCause(),
 				instanceOf(AssertionError.class));
 	}
 
 	@Test
 	public void concurrentTestWithDeadlock() {
 		Class<?>[] classes = new Class<?>[] {
 				ConcurrentTestWithDeadlock.class };
 		Result result = ConcurrentJUnit.runClasses(classes);
 
 		assertThat("number of tests", result.getRunCount(), equalTo(1));
		assertThat("number of failures", result.getFailureCount(),
 				equalTo(1));
 		Failure failure = result.getFailures().get(0);
 		assertThat("exception's type", failure.getException(),
 				instanceOf(DeadlockError.class));
 	}
 }
