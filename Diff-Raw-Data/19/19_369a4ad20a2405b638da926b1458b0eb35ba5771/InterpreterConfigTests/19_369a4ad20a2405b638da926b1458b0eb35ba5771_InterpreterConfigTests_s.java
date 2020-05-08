 package org.eclipse.dltk.core.tests.launching;
 
 import java.io.File;
 import java.util.List;
 
 import junit.framework.Test;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.tests.model.AbstractModelTests;
 import org.eclipse.dltk.launching.InterpreterConfig;
 
 public class InterpreterConfigTests extends AbstractModelTests {
 	private static final String PROJECT_NAME = "launching";
 
 	private IScriptProject scriptProject;
 
 	public InterpreterConfigTests(String name) {
 		super("org.eclipse.dltk.core.tests", name);
 	}
 
 	// Configuration
 	public void setUpSuite() throws Exception {
 		super.setUpSuite();
 		scriptProject = setUpScriptProject(PROJECT_NAME);
 	}
 
 	public void tearDownSuite() throws Exception {
 		deleteProject(PROJECT_NAME);
 		super.tearDownSuite();
 	}
 
 	public static Test suite() {
 		return new Suite(InterpreterConfigTests.class);
 	}
 
 	protected InterpreterConfig createInterperterConfig() {
 		IProject project = scriptProject.getProject();
 		IResource member = project.findMember("src/script.xxx");
 		IPath scriptPath = member.getLocation();
 		File file = new File( scriptPath.toOSString() );
 		return new InterpreterConfig(file);
 	}
 
 	public void testInterpreterConfig() {
 		IProject project = scriptProject.getProject();
 		IResource member = project.findMember("src/script.xxx");
 		IPath scriptPath = member.getLocation();
 
 		File file = new File( scriptPath.toOSString() );
 		InterpreterConfig config = new InterpreterConfig(file);
 
 		// Creation
 		assertNotNull(config.getScriptFile());
 		assertNotNull(config.getWorkingDirectory());
 
		assertEquals(scriptPath, config.getScriptFile());
		assertEquals(scriptPath.removeLastSegments(1), config
				.getWorkingDirectory());
 
 		// Null as script file
 		try {
 			config.setScriptFile((File)null);
 			fail("Should raise an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 
 		}
 
 //		try {
 //			config.setScriptFile((File) null);
 //			fail("Should raise an IllegalArgumentException");
 //		} catch (IllegalArgumentException e) {
 //
 //		}
 
 		// Null as working directory
 		try {
 			config.setWorkingDirectory((File) null);
 			fail("Should raise an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 
 		}
 
 //		try {
 //			config.setWorkingDirectory((File) null);
 //			fail("Should raise an IllegalArgumentException");
 //		} catch (IllegalArgumentException e) {
 //
 //		}
 	}
 
 	public void testInterpreterConfigEnvArgs() {
 		InterpreterConfig config = createInterperterConfig();
 
 		// Environment
 		assertEquals(0, config.getEnvVars().size());
 
 		// Not existent variable
 		assertFalse(config.hasEnvVar("DIMAN"));
 		assertNull(config.getEnvVar("KDS"));
 
 		// Add variable
 		config.addEnvVar("KDS", "DIMAN");
 		assertTrue(config.hasEnvVar("KDS"));
 		assertNotNull(config.getEnvVar("KDS"));
 		assertEquals("DIMAN", config.getEnvVar("KDS"));
 
 		// Remove
 		config.removeEnvVar("KDS");
 		assertFalse(config.hasEnvVar("DIMAN"));
 		assertNull(config.getEnvVar("KDS"));
 
 		assertEquals(0, config.getEnvVars().size());
 
 		// Adding null name
 		try {
 			config.addEnvVar("KDS", null);
 			fail("Should raise an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 		}
 
 		// Adding null value
 		try {
 			config.addEnvVar(null, "DIMAN");
 			fail("Should raise an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 		}
 	}
 
 	public void testInterperterConfigScriptArgs() {
 		InterpreterConfig config = createInterperterConfig();
 
 		// Script arguments
 		assertEquals(0, config.getScriptArgs().size());
 		assertFalse(config.hasScriptArg("-gXXX"));
 
 		// Adding first argument
 		config.addScriptArg("-gXXX");
 		assertTrue(config.hasScriptArg("-gXXX"));
 
 		// Adding second argument
 		config.addScriptArg("-pXXX");
 		assertTrue(config.hasScriptArg("-pXXX"));
 
 		final List args = config.getScriptArgs();
 		assertEquals(2, args.size());
 		assertEquals("-gXXX", args.get(0));
 		assertEquals("-pXXX", args.get(1));
 
 		// Adding null argument
 		try {
 			config.addScriptArg(null);
 			fail("Should raise an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 		}
 	}
 
 	public void testInterperterConfigInterpreterArgs() {
 		InterpreterConfig config = createInterperterConfig();
 
 		// Script arguments
 		assertEquals(0, config.getInterpreterArgs().size());
 		assertFalse(config.hasInterpreterArg("arg1"));
 
 		// Adding first argument
 		config.addInterpreterArg("arg1");
 		assertTrue(config.hasInterpreterArg("arg1"));
 
 		// Adding second argument
 		config.addInterpreterArg("arg2");
 		assertTrue(config.hasInterpreterArg("arg2"));
 
 		final List args = config.getInterpreterArgs();
 		assertEquals(2, args.size());
 		assertEquals("arg1", args.get(0));
 		assertEquals("arg2", args.get(1));
 
 		// Adding null argument
 		try {
 			config.addInterpreterArg(null);
 			fail("Should raise an IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 		}
 	}
 }
