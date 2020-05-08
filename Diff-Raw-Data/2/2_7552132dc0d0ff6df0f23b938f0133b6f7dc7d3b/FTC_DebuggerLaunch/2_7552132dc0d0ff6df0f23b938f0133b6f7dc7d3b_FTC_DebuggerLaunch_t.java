 package org.rubypeople.rdt.debug.core.tests;
 
 import java.io.ByteArrayInputStream;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.model.IProcess;
 import org.rubypeople.eclipse.testutils.ResourceTools;
 import org.rubypeople.rdt.internal.debug.core.RubyLineBreakpoint;
 import org.rubypeople.rdt.internal.launching.RubyInterpreter;
 import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;
 import org.rubypeople.rdt.internal.launching.RubyRuntime;
 
 /*
  * 
  */
 public class FTC_DebuggerLaunch extends TestCase {
 
 
 	public void setUp() {
 		this.createInterpreter() ;
 	}
 	
 	protected void createInterpreter() {
		// FIXME We rely on the RUBY_INTERPRETER to be a full path to a valid ruby executable, and it's not getting set properly so this test ends up failing (on the nightly build)!
 
 		RubyInterpreter rubyInterpreter = new RubyInterpreter("RubyInterpreter", new Path(FTC_DebuggerCommunicationTest.RUBY_INTERPRETER));
 		RubyRuntime.getDefault().addInstalledInterpreter(rubyInterpreter) ;
 	
 	}
 	
 	protected void log(String label, ILaunch launch) throws Exception {
 		System.out.println("Infos about " + label + ":");
 		IProcess process = launch.getProcesses()[0];
 		if (process.isTerminated()) {
 			System.out.println("Process has finished with exit-value: " + process.getExitValue());
 		} else {
 			System.out.println("Process still running.");
 		}
 		String error = process.getStreamsProxy().getErrorStreamMonitor().getContents();
 		if (error != null && error.length() > 0) {
 			System.out.println("Process stderr: " + error);
 		}	
 		String stdout = process.getStreamsProxy().getOutputStreamMonitor().getContents();
 		if (stdout != null && stdout.length() > 0) {
 			System.out.println("Process stdout: " + stdout);
 		}		
 	}
 
 	
 	public void testTwoSessions() throws Exception {
 		ILaunchConfigurationType lcT = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(RubyLaunchConfigurationAttribute.RUBY_LAUNCH_CONFIGURATION_TYPE) ;
 		
 		ILaunchConfigurationWorkingCopy wc = lcT.newInstance(null, "TestLaunchConfiguration") ;
 		IProject project = ResourceTools.createProject("FTCDebuggerLaunchMultipleSessions") ;
 		IFile rubyFile = project.getFile("run.rb");
 		
 		rubyFile.create(new ByteArrayInputStream("puts 'a'\nputs 'b'".getBytes()), true, new NullProgressMonitor()) ;
 		wc.setAttribute(RubyLaunchConfigurationAttribute.PROJECT_NAME, rubyFile.getProject().getName());
 		wc.setAttribute(RubyLaunchConfigurationAttribute.FILE_NAME, rubyFile.getProjectRelativePath().toString());
 		//wc.setAttribute(RubyLaunchConfigurationAttribute.WORKING_DIRECTORY, RubyApplicationShortcut.getDefaultWorkingDirectory(rubyFile.getProject()));
 		wc.setAttribute(RubyLaunchConfigurationAttribute.SELECTED_INTERPRETER, RubyRuntime.getDefault().getSelectedInterpreter().getName());
 		ILaunchConfiguration lc = wc.doSave() ;
 		
 		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(new RubyLineBreakpoint(rubyFile, 1)) ;
 		
 		ILaunch launch = lc.launch("debug", new NullProgressMonitor()) ;
 		Thread.sleep(5000)  ;
 		this.log("1. launch", launch) ;
 		// getDebugTarget returns null if connection between ruby debugger and RubyDebuggerProxy (RubyLoop) could not
 		// be established
 		Assert.assertNotNull(launch.getDebugTarget()) ;
 		Assert.assertTrue(launch.getDebugTarget().getThreads()[0].isSuspended()) ;
 		
 		// the breakpoint we have set for the first launch has disappeard at this point through
 		// a ResourceChanged Event
 		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(new RubyLineBreakpoint(rubyFile, 1)) ;
 		ILaunch secondlaunch = lc.launch("debug", new NullProgressMonitor()) ;
 		Thread.sleep(5000)  ;
 		this.log("2. launch", secondlaunch) ;
 		Assert.assertNotNull(secondlaunch.getDebugTarget()) ;
 		Assert.assertFalse(secondlaunch.getProcesses()[0].isTerminated()) ;
 		Assert.assertTrue(secondlaunch.getDebugTarget().getThreads()[0].isSuspended()) ;
 	}
 }
