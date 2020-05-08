 package com.bigvisible.kanbansimulator;
 import java.io.ByteArrayOutputStream;
 
 import org.junit.Test;
 
 import com.bigvisible.kanbansimulator.CommandLine;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 
 public class CommandLineTest {
 
 	@Test
 	public void when_passed_proper_set_of_parameters_then_simulator_runs_successfully() {
		String args[] = new String[] { "4", "88", "13", "12", "12", "10", "11" };
 
 		ByteArrayOutputStream rawOuput = new ByteArrayOutputStream();
 
 		CommandLine.redirectOutputTo(rawOuput);
 
 		CommandLine.main(args);
 
 		String output = rawOuput.toString();
 
 		assertThat(output, containsString("1, 11, 13, 11, 0, 12, 11, 0, 12, 11, 0, 10, 10, 1, 10"));
 	}
 
 	@Test
 	public void when_not_given_any_parameters_displays_a_help_message()
 			throws Exception {
 		String args[] = new String[] {};
 
 		ByteArrayOutputStream rawOuput = new ByteArrayOutputStream();
 
 		CommandLine.redirectOutputTo(rawOuput);
 
 		CommandLine.main(args);
 
 		String output = rawOuput.toString();
 
 		assertTrue(output.contains("Usage:"));
 	}
 
 }
