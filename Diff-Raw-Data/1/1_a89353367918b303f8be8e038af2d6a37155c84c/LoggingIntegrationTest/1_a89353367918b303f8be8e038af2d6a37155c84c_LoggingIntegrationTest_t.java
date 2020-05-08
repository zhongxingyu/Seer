 package com.bluespot.logging.tests;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.bluespot.logging.CallStackHandler;
 import com.bluespot.logging.Logging;
 import com.bluespot.tree.PrintVisitor;
 import com.bluespot.tree.Tree;
 
 public class LoggingIntegrationTest {
 
 	public static class Bar {
 		private String name;
 
 		public Bar(final String name) {
 			Logging.log("Creating bar!");
 			this.setName(name);
 		}
 
 		public String getName() {
 			return this.name;
 		}
 
 		public void setName(final String name) {
 			Logging.log("Setting bar name: " + name);
 			this.name = name;
 		}
 	}
 
 	public static class Foo {
 		List<Bar> bars = new ArrayList<Bar>();
 
 		public Foo() {
 			Logging.log("Created Foo");
 		}
 
 		public void addBar(final String name) {
 			Logging.logEntry();
 			Logging.log("Adding bar with name: %s", name);
 			this.bars.add(new Bar(name));
 			Logging.logExit();
 		}
 
 		public void addBars(final String... names) {
 			Logging.log("Adding lots of bars: " + names.length);
 			for (final String name : names) {
 				Logging.log("Adding this name: " + name);
 				this.addBar(name);
 			}
 		}
 	}
 
 	private CallStackHandler handler;
 	private final Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
 	private Tree<LogRecord> tree;
 
 	@After
 	public void tearDown() {
 		this.logger.removeHandler(this.handler);
 	}
 
 	@Before
 	public void setUp() {
 		this.tree = new Tree<LogRecord>(null);
 		this.handler = new CallStackHandler(this.tree.walker());
 		this.logger.addHandler(this.handler);
 	}
 
	@Test
 	public void testLoggingStuff() {
 		LoggingIntegrationTest.runSimplestOperation();
 		Assert.assertTrue("Tree not empty", this.tree.size() > 0);
 		this.tree.visit(new PrintVisitor<LogRecord>() {
 
 			@Override
 			public String toString(final LogRecord record) {
 				return record != null ? record.getSourceMethodName() + ": " + record.getMessage() : "null";
 			}
 		});
 	}
 
 	public static void runRandomOperations() {
 		Logging.logEntry();
 		Logging.log("Running random operations");
 		final Foo foo = new Foo();
 		foo.addBar("No time");
 		foo.addBars("A", "B", "C", "D");
 		final Foo anotherFoo = new Foo();
 		anotherFoo.addBar("Cheese!");
 		new Bar("Crumpet.");
 		Logging.logExit();
 	}
 
 	public static void runSimplestOperation() {
 		final Foo foo = new Foo();
 		foo.addBars("A", "B", "C", "D");
 	}
 }
