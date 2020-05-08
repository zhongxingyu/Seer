 package com.alexrnl.commons.arguments;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import java.io.PrintStream;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test suite for the {@link Arguments} class.
  * @author Alex
  */
 public class ArgumentsTest {
 	/** The arguments to test */
 	private Arguments	arguments;
 	/** The target for the parameters */
 	private Target		target;
 	
 	/**
 	 * A class representing the target for the argument parsing.
 	 * @author Alex
 	 */
 	private static class Target {
 		/** If the feature is used */
		@Param(names = { "-u", "--used" }, description = "if the feature should be used", required = false)
		private boolean	used;
 		/** The name to use */
		@Param(names = { "-n" }, description = "the name of the object", required = true)
 		private String	name;

 		/**
 		 * Constructor #1.<br />
 		 * Default constructor.
 		 */
 		private Target () {
 			super();
 		}
 		
 		/**
 		 * Return the attribute used.
 		 * @return the attribute used.
 		 */
 		public boolean isUsed () {
 			return used;
 		}
 		
 		/**
 		 * Return the attribute name.
 		 * @return the attribute name.
 		 */
 		public String getName () {
 			return name;
 		}
 	}
 	
 	/**
 	 * Set up test attributes.
 	 */
 	@Before
 	public void setUp () {
 		target = new Target();
 		arguments = new Arguments("manLau", target);
 	}
 	
 	/**
 	 * Test method for {@link Arguments#parse(String[])}.
 	 */
 	@Test
 	public void testParseStringArray () {
 		arguments.parse("-u", "-n", "alex");
 		assertTrue(target.isUsed());
 		assertEquals("alex", target.getName());
 	}
 	
 	/**
 	 * Test method for {@link Arguments#parse(Iterable)}.
 	 */
 	@Test
 	public void testParseIterableOfString () {
 		arguments.parse("--used", "-n", "alex");
 		assertTrue(target.isUsed());
 		assertEquals("alex", target.getName());
 	}
 	
 	// TODO add tests for error cases
 	
 	/**
 	 * Test method for {@link Arguments#usage(PrintStream)}.
 	 */
 	@Test
 	public void testUsagePrintStream () {
 		final PrintStream out = mock(PrintStream.class);
 		arguments.usage(out);
 		verify(out).println(arguments);
 	}
 	
 	/**
 	 * Test method for {@link Arguments#usage()}.
 	 */
 	@Test
 	public void testUsage () {
 		// Nothing can really be done to check that the usage has been printed on stdout
 		arguments.usage();
 	}
 	
 	/**
 	 * Test method for {@link Arguments#toString()}.
 	 */
 	@Test
 	public void testToString () {
 		assertEquals("manLau usage as follow:\n" +
 				"\t   -n\t\t\t\tthe name of the object\n" +
 				"\t[  -u, --used\t\t\t\tif the feature should be used  ]",
 				arguments.toString());
 	}
 }
