 package org.rubypeople.rdt.testunit.launcher;
 
 import java.io.File;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.SocketUtil;
 import org.rubypeople.rdt.internal.launching.InterpreterRunnerConfiguration;
 import org.rubypeople.rdt.testunit.TestunitPlugin;
 
 public class TestUnitRunnerConfiguration extends InterpreterRunnerConfiguration {
 	private int port = -1 ;
 	
 	public TestUnitRunnerConfiguration(ILaunchConfiguration aConfiguration) {
 		super(aConfiguration);
 	}
 
 	public String getAbsoluteFileName() {
 		return new File(getFileName()).getAbsolutePath();
 	}
 	
 	public String getFileName() {
 		return getTestRunnerPath();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.rubypeople.rdt.internal.launching.InterpreterRunnerConfiguration#getAbsoluteFileDirectory()
 	 */
 	public String getAbsoluteFileDirectory() {
 		IPath path = new Path(this.getFileName());
 		path = path.removeLastSegments(1);
 		return path.toOSString();
 	}
 
 	public String getAbsoluteTestFileName() {
 		String fileName = "";
 		try {
 			fileName = configuration.getAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, "");
 		} catch (CoreException e) {}
 
 		IPath path = new Path(fileName);
 		path = path.removeLastSegments(1);
 		return path.toOSString();
 	}
 
 	public int getPort() {
 		// the port is needed render the command line for the ruby interpreter call
 		// and in TestUnitPlugin::launchChanged in order to start the server on
 		// the java side
 		if (port == -1) {
 			port = SocketUtil.findFreePort();
 		}
 		return port ;
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.rubypeople.rdt.internal.launching.InterpreterRunnerConfiguration#getProgramArguments()
 	 */
 	public String getProgramArguments() {		
 		String fileName = "";
 		String testClass = "";
 		String testMethod = "";
 		// FIXME Remove keepAlive on this end and remove looking for it on
 		// RemoteTestRunner.rb
 		boolean keepAlive = false;
 		try {
 			// Pull out the port and other unit testing variables
 			// and convert them into command line args			
 			fileName = configuration.getAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, "");
 			testClass = configuration.getAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, "");
 			testMethod = configuration.getAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, "");
 		} catch (CoreException e) {
 			TestunitPlugin.log(e);
 			throw new RuntimeException("Could not get necessary attributes from the launch configuration.") ;
 		}
 
		return fileName + " " + this.getPort() + " " + keepAlive + " " + testClass + " " + testMethod;
 	}
 
 	public List renderLoadPath() {
 		List loadPath = super.renderLoadPath();
 		
 		String absoluteTestFileName = this.getAbsoluteTestFileName();
 		if (absoluteTestFileName.length() != 0) {
 			loadPath.add("-I");
 			loadPath.add(absoluteTestFileName);
 		}
 		return loadPath;
 	}
 
 	public static String getTestRunnerPath() {
 		String directory = RubyCore.getOSDirectory(TestunitPlugin.getDefault());
 		File pluginDirFile  = new File(directory, "ruby");
 		
 		if (!pluginDirFile.exists()) 
 			throw new RuntimeException("Expected directory of RemoteTestRunner.rb does not exist: " + pluginDirFile.getAbsolutePath()); 
 	
 		return pluginDirFile.getAbsolutePath() + File.separator + TestUnitLaunchShortcut.TEST_RUNNER_FILE;
 	}
 }
