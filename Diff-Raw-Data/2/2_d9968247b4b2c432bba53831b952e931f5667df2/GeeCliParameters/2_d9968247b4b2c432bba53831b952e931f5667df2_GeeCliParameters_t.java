 package grisu.frontend;
 
 import grisu.frontend.view.cli.GrisuCliParameters;
 
 import com.beust.jcommander.Parameter;
 
 public class GeeCliParameters extends GrisuCliParameters {
 
	@Parameter(names = { "-f", "--applications-folder" }, description = "folder containing the test")
 	private String folder;
 	
 	@Parameter(names = {"--reset" }, description = "move all failed job log fails into the archive folder")
 	private boolean reset;
 	
 	@Parameter(names = {"--create-test-stub" }, description = "create a new test client stub")
 	private boolean create_test_stub;
 	
 	@Parameter(names = {"--application", "-a" }, description = "the application name")
 	private String app;
 	
 	@Parameter(names = {"--testname", "-t" }, description = "the test name")
 	private String testname;
 
 	public boolean isReset() {
 		return reset;
 	}
 
 	public boolean isCreate_test_stub() {
 		return create_test_stub;
 	}
 
 	public String getApp() {
 		return app;
 	}
 
 	public String getFolder() {
 		return folder;
 	}
 	
 	public String getTestName() {
 		return testname;
 	}
 
 }
