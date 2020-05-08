 package com.github.enr.clap.test;
 
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import org.testng.annotations.Test;
 
 import com.github.enr.clap.api.Reporter.Level;
 import com.github.enr.clap.impl.DefaultOutputRetainingReporter;
 
 
@Test(suiteName = "Core components")
 public class DefaultOutputRetainingTest {
 
 	@Test
 	public void shouldRetainAllTheOutput() {
 		DefaultOutputRetainingReporter reporter = new DefaultOutputRetainingReporter();
 		reporter.setLevel(Level.DEBUG);
 		reporter.debug("_debug_");
 		reporter.info("_info_");
 		reporter.warn("_warn_");
 		reporter.out("_out_");
 		String output = reporter.getOutput();
 		assertThat(output).as("reporter output").contains("_debug_").contains("_info_").contains("_warn_").contains("_out_");
 	}
 
 	@Test
 	public void shouldFilterOutput() {
 		DefaultOutputRetainingReporter reporter = new DefaultOutputRetainingReporter();
 		reporter.setLevel(Level.WARN);
 		reporter.debug("_debug_");
 		reporter.info("_info_");
 		reporter.warn("_warn_");
 		reporter.out("_out_");
 		String output = reporter.getOutput();
 		assertThat(output).as("reporter output").doesNotContain("debug").doesNotContain("info").contains("_warn_").contains("_out_");
 	}
 }
