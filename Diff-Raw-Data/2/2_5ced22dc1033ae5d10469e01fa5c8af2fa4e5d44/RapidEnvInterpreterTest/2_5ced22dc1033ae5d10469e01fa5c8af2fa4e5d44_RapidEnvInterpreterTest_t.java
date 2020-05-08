 /*
  * RapidEnv: RapidEnvInterpreterTest.java
  *
  * Copyright (C) 2010 Martin Bluemel
  *
  * Creation Date: 06/02/2010
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the
  * GNU Lesser General Public License as published by the Free Software Foundation;
  * either version 3 of the License, or (at your option) any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  * You should have received a copies of the GNU Lesser General Public License and the
  * GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.rapidbeans.rapidenv;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.SequenceInputStream;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.logging.Level;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.rapidbeans.core.type.TypePropertyCollection;
 import org.rapidbeans.core.util.FileHelper;
 import org.rapidbeans.core.util.OperatingSystem;
 import org.rapidbeans.core.util.PlatformHelper;
 import org.rapidbeans.rapidenv.cmd.CmdRenv;
 import org.rapidbeans.rapidenv.config.Installunit;
 import org.rapidbeans.rapidenv.config.Project;
 import org.rapidbeans.rapidenv.config.RapidEnvConfigurationException;
 import org.rapidbeans.rapidenv.config.cmd.ShellLinkIcon;
 import org.rapidbeans.rapidenv.config.cmd.ShellLinkWindows;
 
 /**
  * Unit tests for the RapidEnv command interpreter.
  * 
  * @author Martin Bluemel
  */
 public class RapidEnvInterpreterTest {
 
 	final static char DRIVE_LETTER = new File(".").getAbsolutePath().charAt(0);
 
 	final static String PROJECT_DIR = new File(".").getAbsolutePath().substring(0,
 			new File(".").getAbsolutePath().length() - 2);
 
 	@BeforeClass
 	public static void setUpClass() {
 		if (!new File("profile").exists()) {
 			new File("profile").mkdir();
 		}
 		TypePropertyCollection.setDefaultCharSeparator(',');
 		FileHelper.copyFile(new File("env.dtd"), new File("../../env.dtd"));
 		new File("testdata/testinstall").mkdir();
 	}
 
 	@AfterClass
 	public static void tearDownClass() {
 		FileHelper.deleteDeep(new File("profile"));
 		FileHelper.deleteDeep(new File("../../env.dtd"));
 		FileHelper.deleteDeep(new File("testdata/testinstall"));
 	}
 
 	@After
 	public void tearDown() {
 		RapidEnvInterpreter.clearInstance();
 	}
 
 	@Test
 	public void testCreateConfigurationWindowsSimple() {
 		if (PlatformHelper.getOs() != OperatingSystem.windows) {
 			return;
 		}
 		final File testinstall = new File("testdata/testinstall");
 		final File mysqlbin = new File("testdata/testinstall/mysql/5.5.15/bin");
 		if (!mysqlbin.exists()) {
 			assertTrue(mysqlbin.mkdirs());
 		}
 		FileHelper.copyFile(new File("testdata/conf/mysql/mysql.ico"), new File(
 				"testdata/testinstall/mysql/5.5.15/bin/mysql.ico"), true);
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envIcon01.xml", "c" }));
 		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 		PrintStream sout = new PrintStream(bStream);
 		env.setOut(sout);
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.config);
 		Project project = env.getProject();
 		final Installunit unitMysql = project.findInstallunitConfiguration("mysql");
 		ShellLinkIcon icon = (ShellLinkIcon) unitMysql.getConfigurations().get(0);
 		File shortcutFile = new File(System.getenv("USERPROFILE") + "\\Desktop\\" + icon.getTitle() + ".lnk");
 		try {
 			assertEquals("Start TestMySQL", icon.getTitle());
 			if (PlatformHelper.getOs() == OperatingSystem.windows) {
 				assertEquals(System.getenv("SystemRoot") + "/system32/cmd.exe", icon.getExecutable());
 			}
 			assertEquals(env.getProject().findInstallunitConfiguration("mysql").getHomedirAsFile().getAbsolutePath()
 					+ "/bin", icon.getExecutein());
 			assertEquals(env.getProject().findInstallunitConfiguration("mysql").getHomedirAsFile().getAbsolutePath()
 					+ "/bin/mysql.ico", icon.getIconfile());
 			assertEquals(2, icon.getArguments().size());
 			assertEquals("/C", icon.getArguments().get(0).getValue());
 			assertEquals("mysqld_start.cmd", icon.getArguments().get(1).getValue());
 
 			assertFalse(icon.check(false));
 			assertTrue(icon.check(true));
 			assertTrue(icon.check(false));
 
 			ShellLinkWindows shellLinkWinRead = new ShellLinkWindows(shortcutFile);
 			shellLinkWinRead.load();
 			assertEquals("Start TestMySQL.lnk", shellLinkWinRead.getFile().getName());
 			assertNotNull(shellLinkWinRead.getTargetPath());
 			assertEquals(new File(System.getenv("SystemRoot") + "/system32/cmd.exe").getAbsolutePath(),
 					shellLinkWinRead.getTargetPath().getAbsolutePath());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin").getAbsolutePath(), shellLinkWinRead
 					.getWorkingDirectory().getAbsolutePath());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin/mysql.ico").getAbsolutePath(), shellLinkWinRead
 					.getIconFile().getAbsolutePath());
 			assertEquals(0, shellLinkWinRead.getIconNumber());
 			assertEquals(2, shellLinkWinRead.getArguments().size());
 			assertEquals("/C", shellLinkWinRead.getArguments().get(0).getValue());
 			assertEquals("mysqld_start.cmd", shellLinkWinRead.getArguments().get(1).getValue());
 		} finally {
 			if (testinstall.exists()) {
 				FileHelper.deleteDeep(testinstall, true);
 			}
 			if (shortcutFile.exists()) {
 				assertTrue(shortcutFile.delete());
 			}
 		}
 	}
 
 	@Test
 	public void testCreateConfigurationWindowsCmd() {
 		if (PlatformHelper.getOs() != OperatingSystem.windows) {
 			return;
 		}
 		final File testinstall = new File("testdata/testinstall");
 		final File mysqlbin = new File("testdata/testinstall/mysql/5.5.15/bin");
 		if (!mysqlbin.exists()) {
 			assertTrue(mysqlbin.mkdirs());
 		}
 		FileHelper.copyFile(new File("testdata/conf/mysql/mysql.ico"), new File(
 				"testdata/testinstall/mysql/5.5.15/bin/mysql.ico"), true);
 		FileHelper.copyFile(new File("testdata/conf/mysql/mysqld_start.cmd"), new File(
 				"testdata/testinstall/mysql/5.5.15/bin/mysqld_start.cmd"), true);
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envIcon01.xml", "c" }));
 		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 		PrintStream sout = new PrintStream(bStream);
 		env.setOut(sout);
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.config);
 		Project project = env.getProject();
 		final Installunit unitMysql = project.findInstallunitConfiguration("mysql");
 		ShellLinkIcon icon = (ShellLinkIcon) unitMysql.getConfigurations().get(1);
 		File shortcutFile = new File(System.getenv("USERPROFILE") + "\\Desktop\\" + icon.getTitle() + ".lnk");
 		try {
 			assertEquals("Start TestMySQL", icon.getTitle());
 			assertEquals("mysqld_start.cmd", icon.getExecutable());
 			assertEquals(env.getProject().findInstallunitConfiguration("mysql").getHomedirAsFile().getAbsolutePath()
 					+ "/bin", icon.getExecutein());
 			assertEquals(env.getProject().findInstallunitConfiguration("mysql").getHomedirAsFile().getAbsolutePath()
 					+ "/bin/mysql.ico", icon.getIconfile());
 			assertEquals(2, icon.getArguments().size());
 			assertEquals("argval1", icon.getArguments().get(0).getValue());
 			assertEquals(false, icon.getArguments().get(0).getQuoted());
 			assertEquals("argval2 argval3", icon.getArguments().get(1).getValue());
 			assertEquals(true, icon.getArguments().get(1).getQuoted());
 
 			assertFalse(icon.check(false));
 			assertTrue(icon.check(true));
 			assertTrue(icon.check(false));
 
 			ShellLinkWindows shellLinkWinRead = new ShellLinkWindows(shortcutFile);
 			shellLinkWinRead.load();
 			assertEquals("Start TestMySQL.lnk", shellLinkWinRead.getFile().getName());
 			assertNotNull(shellLinkWinRead.getTargetPath());
 			assertEquals(new File(System.getenv("SystemRoot") + "/system32/cmd.exe").getAbsolutePath(),
 					shellLinkWinRead.getTargetPath().getAbsolutePath());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin").getAbsolutePath(), shellLinkWinRead
 					.getWorkingDirectory().getAbsolutePath());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin/mysql.ico").getAbsolutePath(), shellLinkWinRead
 					.getIconFile().getAbsolutePath());
 			assertEquals(0, shellLinkWinRead.getIconNumber());
 			assertEquals(5, shellLinkWinRead.getArguments().size());
 			assertEquals("/C", shellLinkWinRead.getArguments().get(0).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(0).getQuoted());
 			assertEquals("call", shellLinkWinRead.getArguments().get(1).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(1).getQuoted());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin/mysqld_start.cmd").getAbsolutePath(), shellLinkWinRead
 					.getArguments().get(2).getValue());
 			assertEquals(true, shellLinkWinRead.getArguments().get(2).getQuoted());
 			assertEquals("argval1", shellLinkWinRead.getArguments().get(3).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(3).getQuoted());
 			assertEquals("argval2 argval3", shellLinkWinRead.getArguments().get(4).getValue());
 			assertEquals(true, shellLinkWinRead.getArguments().get(4).getQuoted());
 		} finally {
 			if (testinstall.exists()) {
 				FileHelper.deleteDeep(testinstall, true);
 			}
 			if (shortcutFile.exists()) {
 				assertTrue(shortcutFile.delete());
 			}
 		}
 	}
 
 	@Test
 	public void testCreateConfigurationWindowsCmdenv() {
 		if (PlatformHelper.getOs() != OperatingSystem.windows) {
 			return;
 		}
 		final File testinstall = new File("testdata/testinstall");
 		final File mysqlbin = new File("testdata/testinstall/mysql/5.5.15/bin");
 		if (!mysqlbin.exists()) {
 			assertTrue(mysqlbin.mkdirs());
 		}
 		FileHelper.copyFile(new File("testdata/conf/mysql/mysql.ico"), new File(
 				"testdata/testinstall/mysql/5.5.15/bin/mysql.ico"), true);
 		FileHelper.copyFile(new File("testdata/conf/mysql/mysqld_start.cmd"), new File(
 				"testdata/testinstall/mysql/5.5.15/bin/mysqld_start.cmd"), true);
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envIcon01.xml", "c" }));
 		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 		PrintStream sout = new PrintStream(bStream);
 		env.setOut(sout);
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.config);
 		Project project = env.getProject();
 		final Installunit unitMysql = project.findInstallunitConfiguration("mysql");
 		ShellLinkIcon icon = (ShellLinkIcon) unitMysql.getConfigurations().get(2);
 		File shortcutFile = new File(System.getenv("USERPROFILE") + "\\Desktop\\" + icon.getTitle() + ".lnk");
 		try {
 			assertEquals("Start TestMySQL", icon.getTitle());
 			assertEquals("mysqld_start.cmd", icon.getExecutable());
 			assertEquals(env.getProject().findInstallunitConfiguration("mysql").getHomedirAsFile().getAbsolutePath()
 					+ "/bin", icon.getExecutein());
 			assertEquals(env.getProject().findInstallunitConfiguration("mysql").getHomedirAsFile().getAbsolutePath()
 					+ "/bin/mysql.ico", icon.getIconfile());
 			assertEquals(2, icon.getArguments().size());
 			assertEquals("argval1", icon.getArguments().get(0).getValue());
 			assertEquals(false, icon.getArguments().get(0).getQuoted());
 			assertEquals("argval2 argval3", icon.getArguments().get(1).getValue());
 			assertEquals(true, icon.getArguments().get(1).getQuoted());
 
 			assertFalse(icon.check(false));
 			assertTrue(icon.check(true));
 			assertTrue(icon.check(false));
 
 			ShellLinkWindows shellLinkWinRead = new ShellLinkWindows(shortcutFile);
 			shellLinkWinRead.load();
 			assertEquals("Start TestMySQL.lnk", shellLinkWinRead.getFile().getName());
 			assertNotNull(shellLinkWinRead.getTargetPath());
 			assertEquals(new File(System.getenv("SystemRoot") + "/system32/cmd.exe").getAbsolutePath(),
 					shellLinkWinRead.getTargetPath().getAbsolutePath());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin").getAbsolutePath(), shellLinkWinRead
 					.getWorkingDirectory().getAbsolutePath());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin/mysql.ico").getAbsolutePath(), shellLinkWinRead
 					.getIconFile().getAbsolutePath());
 			assertEquals(0, shellLinkWinRead.getIconNumber());
 			assertEquals(8, shellLinkWinRead.getArguments().size());
 			assertEquals("/C", shellLinkWinRead.getArguments().get(0).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(0).getQuoted());
 			assertEquals("call", shellLinkWinRead.getArguments().get(1).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(1).getQuoted());
 			assertEquals(env.getProfileCmd().getAbsolutePath(), shellLinkWinRead.getArguments().get(2).getValue());
 			assertEquals(true, shellLinkWinRead.getArguments().get(2).getQuoted());
 			assertEquals("&", shellLinkWinRead.getArguments().get(3).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(3).getQuoted());
 			assertEquals("call", shellLinkWinRead.getArguments().get(4).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(4).getQuoted());
 			assertEquals(new File(unitMysql.getHomedir() + "/bin/mysqld_start.cmd").getAbsolutePath(), shellLinkWinRead
 					.getArguments().get(5).getValue());
 			assertEquals(true, shellLinkWinRead.getArguments().get(5).getQuoted());
 			assertEquals("argval1", shellLinkWinRead.getArguments().get(6).getValue());
 			assertEquals(false, shellLinkWinRead.getArguments().get(6).getQuoted());
 			assertEquals("argval2 argval3", shellLinkWinRead.getArguments().get(7).getValue());
 			assertEquals(true, shellLinkWinRead.getArguments().get(7).getQuoted());
 		} finally {
 			if (testinstall.exists()) {
 				FileHelper.deleteDeep(testinstall, true);
 			}
 			if (shortcutFile.exists()) {
 				assertTrue(shortcutFile.delete());
 			}
 		}
 	}
 
 	@Test
 	public void testParsePropertyConfigurationsSimple() {
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"b" }));
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.boot);
 		assertEquals(4, env.getPropertiesToProcess().size());
 	}
 
 	/**
 	 * Test the status command for common and personal properties.
 	 * 
 	 * @throws IOException
 	 *             in case of IO problem
 	 */
 	@Test
 	public void testParsePropertyConfigurationsToolSpecific() throws IOException {
 
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envPropsToolSpecific.xml", "s" }));
 		RapidEnvInterpreter.clearInstance();
 
 		try {
 
 			// boot that thing
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"b" }));
 			SequenceInputStream sin = new SequenceInputStream(new InputStreamLines(new String[] { "xyz", "n" // do
 																												// not
 																												// create
 																												// the
 																												// "command prompt here"
 			// explorer menu entry
 					}));
 			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 			PrintStream sout = new PrintStream(bStream);
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificBootWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootWin.properties"),
 						env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootWin.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificBootLinux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(
 						new File("testdata/out/outPropertyToolSpecificBootLinux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootLinux.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// try a stat
 			sin = new SequenceInputStream(new InputStreamLines(new String[] {}));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"s" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificStatWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootWin.properties"),
 						env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootWin.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificStatLinux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(
 						new File("testdata/out/outPropertyToolSpecificBootLinux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootLinux.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// try an installation (the optional tool won't be installed)
 			sin = new SequenceInputStream(new InputStreamLines(new String[] {}));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"i" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificInstWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstWin.properties"),
 						env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstWin.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificInstLinux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(
 						new File("testdata/out/outPropertyToolSpecificInstLinux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstLinux.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// try a specific installation for the optional tool
 			sin = new SequenceInputStream(new InputStreamLines(new String[] { "", "Y" }));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"i", "otherapp" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificInstSpecWin.txt"),
 						bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstWinSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstWinSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificInstSpecLinux.txt"),
 						bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstLinuxSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstLinuxSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// try a 2nd stat
 			sin = new SequenceInputStream(new InputStreamLines(new String[] {}));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"s" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificStat2Win.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstWinSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstWinSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper
 						.assertOutput(new File("testdata/out/outPropertyToolSepcificStat2Linux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstLinuxSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstLinuxSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// configure all
 			sin = new SequenceInputStream(new InputStreamLines(new String[] {}));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"c" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificConfWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstWinSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstWinSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificConfLinux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstLinuxSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstLinuxSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// configure common property (nothing should happen)
 			sin = new SequenceInputStream(new InputStreamLines(new String[] {}));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"c", "myapp.home" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificConfcWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstWinSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstWinSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper
 						.assertOutput(new File("testdata/out/outPropertyToolSepcificConfcLinux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificInstLinuxSpec.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstLinuxSpec.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// configure personal property
 			sin = new SequenceInputStream(new InputStreamLines(new String[] { "" }));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"c", "otherapp.data" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificConfpWin.txt"), bStream);
 				break;
 			case linux:
 				RapidEnvTestHelper
 						.assertOutput(new File("testdata/out/outPropertyToolSepcificConfpLinux.txt"), bStream);
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// deinstall both installunits with specific properties
 			sin = new SequenceInputStream(new InputStreamLines(new String[] {}));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific.xml",
 					"d", "myapp", "otherapp" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificDeinstWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(
 						new File("testdata/out/outPropertyToolSpecificDeinstWin.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificDeinstWin.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificDeinstLinux.txt"),
 						bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificDeinstLinux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificDeinstLinux.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 
 		} finally {
 			RapidEnvTestHelper.tearDownProfile(env);
 		}
 	}
 
 	/**
 	 * Test the change of tool specific properties.
 	 * 
 	 * @throws IOException
 	 *             in case of IO problem
 	 */
 	@Test
 	public void testParsePropertyConfigurationsToolSpecificPropvalchange() throws IOException {
 
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envPropsToolSpecific.xml", "s" }));
 		RapidEnvInterpreter.clearInstance();
 
 		try {
 
 			// boot that thing
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific02.xml",
 					"b" }));
 			// do not create the "command prompt here" explorer menu entry
 			SequenceInputStream sin = new SequenceInputStream(new InputStreamLines(new String[] { "xyz", "n" }));
 			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 			PrintStream sout = new PrintStream(bStream);
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificBootWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootWin.properties"),
 						env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootWin.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificBootLinux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(
 						new File("testdata/out/outPropertyToolSpecificBootLinux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificBootLinux.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// install that stuff
			sin = new SequenceInputStream(new InputStreamLines(new String[] { "/a/b/c", "" }));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsToolSpecific02.xml",
 					"i" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificInst02Win.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(
 						new File("testdata/out/outPropertyToolSpecificInst02Win.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInst02Win.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificInstLinux.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(
 						new File("testdata/out/outPropertyToolSpecificInstLinux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificInstLinux.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 			RapidEnvInterpreter.clearInstance();
 
 			// update otherapp selectively
 			sin = new SequenceInputStream(new InputStreamLines(new String[] { "/a/b/c" }));
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 					"testdata/env/envPropsToolSpecificOtherPropvalCommon.xml", "u", "otherapp" }));
 			env.execute(sin, sout);
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificUpdate02Win.txt"),
 						bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificUpdate02Win.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificUpdate02Win.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyToolSepcificUpdate02Linux.txt"),
 						bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyToolSpecificUpdate02Linux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyToolSpecificUpdate02Linux.cmd"),
 						env.getProfileCmd());
 				break;
 			default:
 				fail("Platform \"" + PlatformHelper.getOs().name() + "\" not yet tested");
 			}
 		} finally {
 			RapidEnvTestHelper.tearDownProfile(env);
 		}
 	}
 
 	@Test
 	public void testParsePropertyConfigurationsWithPathextensions() {
 		CmdRenv cmd = new CmdRenv(new String[] { "-env", "testdata/env/envWithPathext.xml", "b" });
 		RapidEnvInterpreter env = new RapidEnvInterpreter(cmd);
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.boot);
 		assertEquals(3, env.getPropertiesToProcess().size());
 		env.initProperties(CmdRenvCommand.boot);
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			assertTrue("cmd.path = \"" + env.getPropertyValue("cmd.path") + "\"", env.getPropertyValue("cmd.path")
 					.startsWith(DRIVE_LETTER + ":\\h\\opt\\maven\\bin;"));
 			break;
 		default:
 			assertTrue("cmd.path = \"" + env.getPropertyValue("cmd.path") + "\"", env.getPropertyValue("cmd.path")
 					.startsWith("/h/opt/maven/bin:"));
 			break;
 		}
 	}
 
 	@Test
 	public void testParsePropertyConfigurationsWithPathextensionsWithBraces() {
 		CmdRenv cmd = new CmdRenv(new String[] { "-env", "testdata/env/envWithPathextBraces.xml", "b" });
 		RapidEnvInterpreter env = new RapidEnvInterpreter(cmd);
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.boot);
 		assertEquals(3, env.getPropertiesToProcess().size());
 		env.initProperties(CmdRenvCommand.boot);
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			assertTrue("cmd.path = \"" + env.getPropertyValue("cmd.path") + "\"", env.getPropertyValue("cmd.path")
 					.startsWith(DRIVE_LETTER + ":\\h\\opt\\maven\\bin;"));
 			break;
 		default:
 			assertTrue("cmd.path = \"" + env.getPropertyValue("cmd.path") + "\"", env.getPropertyValue("cmd.path")
 					.startsWith("/h/opt/maven/bin:"));
 			break;
 		}
 	}
 
 	@Test
 	public void testParsePropertyConfigurationsWithPathextensionsWithBraces2() {
 		CmdRenv cmd = new CmdRenv(new String[] { "-env", "testdata/env/envWithPathext.xml", "b" });
 		RapidEnvInterpreter env = new RapidEnvInterpreter(cmd, new AntGateway(new File(
 				"testdata/ant/ant02_win.properties")));
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.boot);
 		assertEquals(3, env.getPropertiesToProcess().size());
 		env.initProperties(CmdRenvCommand.boot);
 	}
 
 	@Test
 	public void testParsePropertyConfigurationsWithPathextensionsWithBracesNoInterpret() {
 		CmdRenv cmd = new CmdRenv(new String[] { "-env", "testdata/env/envWithPathextBracesNoInterpret.xml", "b" });
 		RapidEnvInterpreter env = new RapidEnvInterpreter(cmd);
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.boot);
 		assertEquals(3, env.getPropertiesToProcess().size());
 		env.initProperties(CmdRenvCommand.boot);
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			assertTrue("cmd.path = \"" + env.getPropertyValue("cmd.path") + "\"", env.getPropertyValue("cmd.path")
 					.startsWith(DRIVE_LETTER + ":\\h\\opt\\maven\\bin;"));
 			break;
 		default:
 			assertTrue("cmd.path = \"" + env.getPropertyValue("cmd.path") + "\"", env.getPropertyValue("cmd.path")
 					.startsWith("/h/opt/maven/bin:"));
 			break;
 		}
 	}
 
 	@Test
 	public void testInstallFromSourceurlFile() {
 		if (new File("testdata/testinstall/myapp").exists()) {
 			FileHelper.deleteDeep(new File("testdata/testinstall/myapp"));
 		}
 		assertFalse(new File("testdata/testinstall/myapp/1.0.2/readme.txt").exists());
 
 		try {
 			RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 					"testdata/env/envTestInstall.xml", "i", "myapp" }));
 			final ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 			final PrintStream sout = new PrintStream(bStream);
 			env.execute(System.in, sout);
 			assertTrue(new File("testdata/testinstall/myapp/1.0.2/readme.txt").exists());
 		} finally {
 			if (new File("testdata/testinstall/myapp").exists()) {
 				FileHelper.deleteDeep(new File("testdata/testinstall/myapp"));
 			}
 		}
 	}
 
 	/**
 	 * Specify the install units to process specifically with the command. If an
 	 * install unit is specified with its simple tool name it's found as long as
 	 * the tool name is not ambigouus.
 	 */
 	@Test
 	public void testInstallUnitsToProcessSimple() {
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"s", "maven", "jdk" }));
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.stat);
 		assertEquals(2, env.getInstallunitsToProcess().size());
 		assertEquals("org.apache.maven", env.getInstallunitsToProcess().get(0).getFullyQualifiedName());
 		assertEquals("jdk", env.getInstallunitsToProcess().get(1).getFullyQualifiedName());
 	}
 
 	/**
 	 * Specify the all install units to process by specifying no install unit
 	 * with the command.
 	 */
 	@Test
 	public void testInstallUnitsToProcessAll() {
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"s" }));
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.stat);
 		assertEquals(6, env.getInstallunitsToProcess().size());
 		assertEquals("jdk", env.getInstallunitsToProcess().get(0).getFullyQualifiedName());
 		assertEquals("org.apache.ant", env.getInstallunitsToProcess().get(1).getFullyQualifiedName());
 		assertEquals("org.apache.ant/xalan.serializer", env.getInstallunitsToProcess().get(2).getFullyQualifiedName());
 		assertEquals("org.apache.maven", env.getInstallunitsToProcess().get(3).getFullyQualifiedName());
 		assertEquals("org.rapidbeans.ambitool", env.getInstallunitsToProcess().get(4).getFullyQualifiedName());
 		assertEquals("org.rapidbeans.alt.ambitool", env.getInstallunitsToProcess().get(5).getFullyQualifiedName());
 	}
 
 	/**
 	 * Specify the install units to process specifically with the command. If an
 	 * install unit is specified with its simple tool name but it is ambigouus
 	 * we will get an exception.
 	 */
 	@Test(expected = RapidEnvCmdException.class)
 	public void testInstallUnitsToProcessAmbigouus() {
 		try {
 			RapidEnvInterpreter renv = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 					"testdata/env/env.xml", "s", "ant", "ambitool" }));
 			renv.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.stat);
 		} catch (RapidEnvCmdException e) {
 			assertEquals("Ambigouus tool name \"ambitool\"" + " has been specified with the command", e.getMessage());
 			throw e;
 		}
 	}
 
 	/**
 	 * Specify the install units to process specifically with the command.
 	 * Install units defined more than once only will produce a warning in the
 	 * log.
 	 */
 	@Test
 	public void testInstallUnitsToProcessRepeat() {
 		LoggerMock loggerMock = LoggerMock.getLogger(RapidEnvInterpreter.class.getName());
 		RapidEnvInterpreter renv = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"s", "ant", "ant" }), loggerMock);
 		renv.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.stat);
 		assertEquals(1, loggerMock.getLogRecords().size());
 		assertEquals("Install unit \"org.apache.ant\"" + " has been specified for one command multiple times",
 				loggerMock.getLogRecords().get(0).getMessage());
 		assertEquals(Level.WARNING, loggerMock.getLogRecords().get(0).getLevel());
 	}
 
 	/**
 	 * Specify the install units to process specifically with the command as
 	 * fully qualified names. If an install unit is specified with its fully
 	 * qualified tool name it's always found.
 	 */
 	@Test
 	public void testInstallUnitsToProcessFully() {
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"s", "org.apache.maven", "jdk", "org.rapidbeans.alt.ambitool" }));
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.stat);
 		assertEquals(3, env.getInstallunitsToProcess().size());
 		assertEquals("org.apache.maven", env.getInstallunitsToProcess().get(0).getFullyQualifiedName());
 		assertEquals("jdk", env.getInstallunitsToProcess().get(1).getFullyQualifiedName());
 		assertEquals("org.rapidbeans.alt.ambitool", env.getInstallunitsToProcess().get(2).getFullyQualifiedName());
 	}
 
 	/**
 	 * Specify an invalid install unit not defined in the configuration file.
 	 */
 	@Test(expected = RapidEnvCmdException.class)
 	public void testSpecInstallUnitInvalid() {
 		try {
 			RapidEnvInterpreter renv = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 					"testdata/env/env.xml", "b", "jdk", "ant", "xxx" }));
 			renv.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.boot);
 		} catch (RapidEnvCmdException e) {
 			assertTrue(e.getMessage().startsWith(
 					"No install unit or property \"xxx\"\n"
 							+ "  is defined in RapidEnv environment configuration file\n  \""
 							+ new File("testdata/env/env.xml").getAbsolutePath() + "\""));
 			throw e;
 		}
 	}
 
 	/**
 	 * Specify a subunit to process specifically with the command as name
 	 * without the parent unit path.
 	 */
 	@Test
 	public void testInstallUnitsToProcessSubunitWithoutParentsName() {
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"s", "serializer" }));
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.stat);
 		assertEquals(1, env.getInstallunitsToProcess().size());
 		assertEquals("org.apache.ant/xalan.serializer", env.getInstallunitsToProcess().get(0).getFullyQualifiedName());
 	}
 
 	/**
 	 * Specify a subunit to process specifically with the command as fully
 	 * qualified name without the parent unit path.
 	 */
 	@Test
 	public void testInstallUnitsToProcessSubunitWithoutParentsNameFully() {
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"s", "xalan.serializer" }));
 		env.initPropertiesAndInstallunitsToProcess(CmdRenvCommand.stat);
 		assertEquals(1, env.getInstallunitsToProcess().size());
 		assertEquals("org.apache.ant/xalan.serializer", env.getInstallunitsToProcess().get(0).getFullyQualifiedName());
 	}
 
 	/**
 	 * Test specifying a non existing configuration file.
 	 */
 	@Test
 	// (expected=RapidEnvConfigurationException.class)
 	// better without expected in order to have the assertion
 	// error presented in a better way.
 	public void testConfugrationInvalidFileNotFound() {
 		try {
 			new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envXXX.xml", "s" }));
 		} catch (RapidEnvConfigurationException e) {
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				assertTrue(e.getMessage().matches(
 						"RapidEnv configuration file \""
 								+ ".*\\\\org.rapidbeans.rapidenv\\\\testdata\\\\env\\\\envXXX.xml\" " + "not found"));
 				break;
 			default:
 				assertTrue(e.getMessage().matches(
 						"RapidEnv configuration file \"" + ".*/org.rapidbeans.rapidenv/testdata/env/envXXX.xml\" "
 								+ "not found"));
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Test defining one and the same tool (same name space and name) twice.
 	 */
 	@Test
 	// (expected=RapidEnvConfigurationException.class)
 	// better without expected in order to have the assertion
 	// error presented in a better way.
 	public void testConfugrationInvalidToolTwice() {
 		try {
 			new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envInvalidToolTwice.xml", "s" }));
 		} catch (RapidEnvConfigurationException e) {
 			assertEquals("Tool \"xxx.yyy.jdk\" specified twice", e.getMessage());
 		}
 	}
 
 	/**
 	 * Test defining one and the same property (same name) twice.
 	 */
 	@Test
 	// (expected=RapidEnvConfigurationException.class)
 	// better without expected in order to have the assertion
 	// error presented in a better way.
 	public void testConfugrationInvalidPropTwice() {
 		try {
 			new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envInvalidPropTwice.xml", "s" }));
 		} catch (RapidEnvConfigurationException e) {
 			assertEquals("Property \"java_home\" specified twice", e.getMessage());
 		}
 	}
 
 	/**
 	 * Test the status command for common and personal properties.
 	 * 
 	 * @throws IOException
 	 *             in case of IO problem
 	 */
 	@Test
 	public void testPropertiesStat() throws IOException {
 
 		// set up the profile
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envProps01.xml", "s" }));
 		env.setPropertyValue("testprop02", "oldvalue");
 		File jdkHomdir = new File(env.getProject().findInstallunitConfiguration("jdk").getHomedir());
 		String jdkHome = jdkHomdir.getAbsolutePath();
 		env.setPropertyValue("testprop03", jdkHome);
 		env.setPropertyValue("testprop05", "newvalue");
 		env.setPropertyValue("testprop06", "oldvalue");
 		env.writeProfile();
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStatWin.properties"),
 					env.getProfileProps());
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStat.cmd"),
 					env.getProfileCmd());
 			break;
 		case linux:
 			break;
 		default:
 			fail("Platform \"" + PlatformHelper.getOs().name() + "\" not supported.");
 		}
 		RapidEnvInterpreter.clearInstance();
 
 		env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envProps01.xml", "s" }));
 		assertNull(env.getPropertyValue("testprop01"));
 		final ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 		final PrintStream sout = new PrintStream(bStream);
 		env.execute(System.in, sout);
 		assertNull(env.getPropertyValue("testprop01"));
 		assertEquals("oldvalue", env.getPropertyValue("testprop02"));
 		assertEquals(jdkHome, env.getPropertyValue("testprop03"));
 		assertNull(env.getPropertyValue("testprop04"));
 		assertEquals("newvalue", env.getPropertyValue("testprop05"));
 		assertEquals("oldvalue", env.getPropertyValue("testprop06"));
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyCommonChangedStatWin.txt"), bStream);
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStatWin.properties"),
 					env.getProfileProps());
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStat.cmd"),
 					env.getProfileCmd());
 			break;
 		case linux:
 			RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyCommonChangedStatUnix.txt"), bStream);
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStatUnix.properties"),
 					env.getProfileProps());
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStat.sh"),
 					env.getProfileCmd());
 			break;
 		}
 
 		RapidEnvTestHelper.tearDownProfile(env);
 	}
 
 	/**
 	 * Test the configure command for common and personal properties.
 	 * 
 	 * @throws IOException
 	 *             in case of IO problem
 	 */
 	@Test
 	public void testPropertiesConfigure() throws IOException {
 
 		// set up the profile
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envProps01.xml", "s" }));
 
 		env.setPropertyValue("testprop02", "oldvalue");
 		File jdkHomdir = new File(env.getProject().findInstallunitConfiguration("jdk").getHomedir());
 		String jdkHome = jdkHomdir.getAbsolutePath();
 		env.setPropertyValue("testprop03", jdkHome);
 		env.setPropertyValue("testprop05", "newvalue");
 		env.setPropertyValue("testprop06", "oldvalue");
 		env.writeProfile();
 		switch (PlatformHelper.getOs()) {
 		case windows:
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStatWin.properties"),
 					env.getProfileProps());
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStat.cmd"),
 					env.getProfileCmd());
 			break;
 		case linux:
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStatUnix.properties"),
 					env.getProfileProps());
 			RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedStat.sh"),
 					env.getProfileCmd());
 			break;
 		}
 		RapidEnvInterpreter.clearInstance();
 
 		env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envProps01.xml", "c" }));
 		try {
 			assertNull(env.getPropertyValue("testprop01"));
 			final ByteArrayInputStream sin = new ByteArrayInputStream("".getBytes());
 			final ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 			final PrintStream sout = new PrintStream(bStream);
 			env.execute(sin, sout);
 
 			// new common property testprop1 should have been introduced
 			assertEquals("D:" + File.separator + "unknown", env.getPropertyValue("testprop01"));
 
 			// common property testprop02 should be reconfigured
 			// from "oldvalue" to "newvalue"
 			assertEquals("newvalue", env.getPropertyValue("testprop02"));
 
 			// common property testprop03 stays the same as before
 			assertEquals(jdkHome, env.getPropertyValue("testprop03"));
 
 			// new personal has been introduced asked interactively and
 			// initialized
 			// to given default value since the user only typed return
 			assertEquals("val4", env.getPropertyValue("testprop04"));
 
 			// old personal property has kept the changed value it had before
 			// in the properties file
 			assertEquals("newvalue", env.getPropertyValue("testprop05"));
 
 			// old personal property has kept the unchanged value it had before
 			// in the properties file
 			assertEquals("oldvalue", env.getPropertyValue("testprop06"));
 
 			switch (PlatformHelper.getOs()) {
 			case windows:
 				RapidEnvTestHelper
 						.assertOutput(new File("testdata/out/outPropertyCommonChangedConfigWin.txt"), bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyCommonChangedConfigWin.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedConfig.cmd"),
 						env.getProfileCmd());
 				break;
 			case linux:
 				RapidEnvTestHelper.assertOutput(new File("testdata/out/outPropertyCommonChangedConfigLinux.txt"),
 						bStream);
 				RapidEnvTestHelper.assertFilesEqual(new File(
 						"testdata/out/outPropertyCommonChangedConfigLinux.properties"), env.getProfileProps());
 				RapidEnvTestHelper.assertFilesEqual(new File("testdata/out/outPropertyCommonChangedConfig.sh"),
 						env.getProfileCmd());
 				break;
 			}
 		} finally {
 			RapidEnvTestHelper.tearDownProfile(env);
 		}
 	}
 
 	@Test
 	public void testPathWithExtensionsSimple() throws IOException {
 
 		// set up the profile
 		// cmd.path=/x1:/x2:/x3 (Linux) or \x1;x2\x3 (win)
 		RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 				"testdata/env/envPropsPath01.xml", "s" }));
 		String originalPath = File.separator + "x1" + File.pathSeparator + File.separator + "x2" + File.pathSeparator
 				+ File.separator + "x3";
 		env.setPropertyValue("cmd.path", originalPath);
 		env.writeProfile();
 		RapidEnvInterpreter.clearInstance();
 
 		// test booting the path property
 		env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsPath01.xml", "b" }));
 		ByteArrayInputStream sin = new ByteArrayInputStream("n\n".getBytes());
 		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 		PrintStream sout = new PrintStream(bStream);
 		env.execute(sin, sout);
 		// assert cmd.path extension
 		// cmd.path=/p1/:/p2:/p3:/x1:/x3/a1:/a2 (x2 has been removed)
 		assertEquals(File.separator + "p1" + File.pathSeparator + File.separator + "p2" + File.pathSeparator
 				+ File.separator + "p3" + File.pathSeparator + File.separator + "x1" + File.pathSeparator
 				+ File.separator + "x3" + File.pathSeparator + File.separator + "a1" + File.pathSeparator
 				+ File.separator + "a2", env.getPropertyValue("cmd.path"));
 		RapidEnvInterpreter.clearInstance();
 
 		env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsPath02.xml", "u" }));
 		// sin = new ByteArrayInputStream("n\n".getBytes());
 		bStream = new ByteArrayOutputStream();
 		sout = new PrintStream(bStream);
 		env.execute(null, sout);
 		// assert cmd.path extension
 		// cmd.path=/p0;/p1;/p2;/p2a;/p3;/x1;/x3;/a1;/a1a;/a2;/a3
 		assertEquals(File.separator + "p0" + File.pathSeparator + File.separator + "p1" + File.pathSeparator
 				+ File.separator + "p2" + File.pathSeparator + File.separator + "p2a" + File.pathSeparator
 				+ File.separator + "p3" + File.pathSeparator + File.separator + "x1" + File.pathSeparator
 				+ File.separator + "x3" + File.pathSeparator + File.separator + "a1" + File.pathSeparator
 				+ File.separator + "a1a" + File.pathSeparator + File.separator + "a2" + File.pathSeparator
 				+ File.separator + "a3", env.getPropertyValue("cmd.path"));
 		RapidEnvInterpreter.clearInstance();
 
 		env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsPath03.xml", "u" }));
 		sin = new ByteArrayInputStream("n\n".getBytes());
 		bStream = new ByteArrayOutputStream();
 		sout = new PrintStream(bStream);
 		env.execute(sin, sout);
 		assertEquals(File.separator + "p1" + File.pathSeparator + File.separator + "p2" + File.pathSeparator
 				+ File.separator + "p3" + File.pathSeparator + File.separator + "x1" + File.pathSeparator
 				+ File.separator + "x3" + File.pathSeparator + File.separator + "a1" + File.pathSeparator
 				+ File.separator + "a2", env.getPropertyValue("cmd.path"));
 
 		RapidEnvTestHelper.tearDownProfile(env);
 	}
 
 	@Test
 	public void testPathWithExtesionsInstallunitHomedir() throws IOException {
 
 		try {
 
 			// set up the profile
 			// cmd.path=/x1:/x2:/x3 (linux) or \x1;x2\x3 (win)
 			RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 					"testdata/env/envPropsPath04.xml", "s" }));
 			String originalPath = File.separator + "x1" + File.pathSeparator + File.separator + "x2"
 					+ File.pathSeparator + File.separator + "x3";
 			env.setPropertyValue("cmd.path", originalPath);
 			env.writeProfile();
 			RapidEnvInterpreter.clearInstance();
 
 			// test booting the path property
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsPath04.xml", "b" }));
 			ByteArrayInputStream sin = new ByteArrayInputStream("n\n".getBytes());
 			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 			PrintStream sout = new PrintStream(bStream);
 			env.execute(sin, sout);
 			// assert cmd.path extension
 			// cmd.path=/p1/:/p2:/p3:/a1:/a2
 			assertEquals(
 					File.separator + "p1" + File.pathSeparator + File.separator + "p2" + File.pathSeparator
 							+ File.separator + "p3" + File.pathSeparator + File.separator + "a1" + File.pathSeparator
 							+ File.separator + "a2" + File.pathSeparator
 							+ new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.pathSeparator
 							+ new File("testdata/testinstall/otherapp/1.0").getAbsolutePath(),
 					env.getPropertyValue("cmd.path"));
 			RapidEnvInterpreter.clearInstance();
 
 			// test installing units myapp 1.0.2 and otherapp 1.0
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsPath04.xml", "i" }));
 			sin = new ByteArrayInputStream("".getBytes());
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env.execute(sin, sout);
 			// assert cmd.path extension
 			// cmd.path=/p1/:/p2:/p3:/a1:/a2
 			assertEquals(
 					File.separator + "p1" + File.pathSeparator + File.separator + "p2" + File.pathSeparator
 							+ File.separator + "p3" + File.pathSeparator + File.separator + "a1" + File.pathSeparator
 							+ File.separator + "a2" + File.pathSeparator
 							+ new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.pathSeparator
 							+ new File("testdata/testinstall/otherapp/1.0").getAbsolutePath(),
 					env.getPropertyValue("cmd.path"));
 			RapidEnvInterpreter.clearInstance();
 
 			// test upgrading unit otherapp from version 1.0 to 2.0
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsPath05.xml", "u" }));
 			sin = new ByteArrayInputStream("".getBytes());
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env.execute(sin, sout);
 			// assert cmd.path extension
 			// cmd.path=/p1/:/p2:/p3:/a1:/a2
 			assertEquals(
 					File.separator + "p1" + File.pathSeparator + File.separator + "p2" + File.pathSeparator
 							+ File.separator + "p3" + File.pathSeparator + File.separator + "a1" + File.pathSeparator
 							+ File.separator + "a2" + File.pathSeparator
 							+ new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.pathSeparator
 							+ new File("testdata/testinstall/otherapp/2.0").getAbsolutePath(),
 					env.getPropertyValue("cmd.path"));
 			RapidEnvInterpreter.clearInstance();
 
 			// test downgrading unit otherapp from version 2.0 to 1.0
 			env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/envPropsPath04.xml", "u" }));
 			sin = new ByteArrayInputStream("".getBytes());
 			bStream = new ByteArrayOutputStream();
 			sout = new PrintStream(bStream);
 			env.execute(sin, sout);
 			// assert cmd.path extension
 			// cmd.path=/p1/:/p2:/p3:/a1:/a2
 			assertEquals(
 					File.separator + "p1" + File.pathSeparator + File.separator + "p2" + File.pathSeparator
 							+ File.separator + "p3" + File.pathSeparator + File.separator + "a1" + File.pathSeparator
 							+ File.separator + "a2" + File.pathSeparator
 							+ new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.pathSeparator
 							+ new File("testdata/testinstall/otherapp/1.0").getAbsolutePath(),
 					env.getPropertyValue("cmd.path"));
 			RapidEnvTestHelper.tearDownProfile(env);
 		} finally {
 			if (new File("testdata/testinstall/myapp").exists()) {
 				FileHelper.deleteDeep(new File("testdata/testinstall/myapp"));
 			}
 			if (new File("testdata/testinstall/otherapp").exists()) {
 				FileHelper.deleteDeep(new File("testdata/testinstall/otherapp"));
 			}
 		}
 	}
 
 	@Test
 	public void testPathWithExtesionsInstallunitHomedirAndPersonal() throws IOException {
 
 		try {
 
 			// test booting with properties cmd.path.system (personal)
 			// and cmd.path contains cmd.pat.system (profile is empty)
 			RapidEnvInterpreter env = new RapidEnvInterpreter(new CmdRenv(new String[] { "-env",
 					"testdata/env/envPropsPath06.xml", "b" }));
 			try {
 				// do not create the command prompt here explorer entry
 				SequenceInputStream sin = new SequenceInputStream(new InputStreamLines(new String[] {
 						"pathComponent1" + File.pathSeparator + "pathComponent2", "N" }));
 				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 				PrintStream sout = new PrintStream(bStream);
 				env.execute(sin, sout);
 				assertEquals("pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path.system"));
 				assertEquals(new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.separator + "bin"
 						+ File.pathSeparator + new File("testdata/testinstall/otherapp/1.0").getAbsolutePath()
 						+ File.separator + "bin" + File.pathSeparator + File.separator + "fixed" + File.separator
 						+ "extension" + File.pathSeparator + "pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path"));
 				RapidEnvInterpreter.clearInstance();
 
 				// test installing units myapp 1.0.2 and otherapp 1.0
 				env = new RapidEnvInterpreter(new CmdRenv(
 						new String[] { "-env", "testdata/env/envPropsPath06.xml", "i" }));
 				sin = new SequenceInputStream(new InputStreamLines());
 				bStream = new ByteArrayOutputStream();
 				sout = new PrintStream(bStream);
 				env.execute(sin, sout);
 				assertEquals("pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path.system"));
 				assertEquals(new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.separator + "bin"
 						+ File.pathSeparator + new File("testdata/testinstall/otherapp/1.0").getAbsolutePath()
 						+ File.separator + "bin" + File.pathSeparator + File.separator + "fixed" + File.separator
 						+ "extension" + File.pathSeparator + "pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path"));
 				RapidEnvInterpreter.clearInstance();
 
 				// test status after introducing upgrade of unit otherapp from
 				// version 1.0 to 2.0
 				env = new RapidEnvInterpreter(new CmdRenv(
 						new String[] { "-env", "testdata/env/envPropsPath07.xml", "s" }));
 				sin = new SequenceInputStream(new InputStreamLines());
 				bStream = new ByteArrayOutputStream();
 				sout = new PrintStream(bStream);
 				env.execute(sin, sout);
 				assertTrue(bStream.toString().contains("toolhome.myapp = \""));
 				assertTrue(bStream.toString().contains("toolhome.otherapp: value of common property should be changed"));
 				assertTrue(bStream.toString().contains(
 						"cmd.path.system = \"pathComponent1" + File.pathSeparator + "pathComponent2\""));
 				assertTrue(bStream.toString().contains("cmd.path: value of common property should be changed"));
 				RapidEnvInterpreter.clearInstance();
 
 				// test 2nd status after introducing upgrade of unit otherapp
 				// from version 1.0 to 2.0
 				// same result expected
 				env = new RapidEnvInterpreter(new CmdRenv(
 						new String[] { "-env", "testdata/env/envPropsPath07.xml", "s" }));
 				sin = new SequenceInputStream(new InputStreamLines());
 				bStream = new ByteArrayOutputStream();
 				sout = new PrintStream(bStream);
 				env.execute(sin, sout);
 				assertTrue(bStream.toString().contains("toolhome.myapp = \""));
 				assertTrue(bStream.toString().contains("toolhome.otherapp: value of common property should be changed"));
 				assertTrue(bStream.toString().contains(
 						"cmd.path.system = \"pathComponent1" + File.pathSeparator + "pathComponent2\""));
 				assertTrue(bStream.toString().contains("cmd.path: value of common property should be changed"));
 				RapidEnvInterpreter.clearInstance();
 
 				// test upgrading otherapp to version 2.0
 				env = new RapidEnvInterpreter(new CmdRenv(
 						new String[] { "-env", "testdata/env/envPropsPath07.xml", "u" }));
 				sin = new SequenceInputStream(new InputStreamLines());
 				bStream = new ByteArrayOutputStream();
 				sout = new PrintStream(bStream);
 				env.execute(sin, sout);
 				assertEquals(new File("testdata/testinstall/otherapp/2.0").getAbsolutePath(),
 						env.getPropertyValue("toolhome.otherapp"));
 				assertEquals("pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path.system"));
 				assertEquals(new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.separator + "bin"
 						+ File.pathSeparator + new File("testdata/testinstall/otherapp/2.0").getAbsolutePath()
 						+ File.separator + "bin" + File.pathSeparator + File.separator + "fixed" + File.separator
 						+ "extension" + File.pathSeparator + "pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path"));
 				RapidEnvInterpreter.clearInstance();
 
 				// test downgrading otherapp to version 1.0 again
 				env = new RapidEnvInterpreter(new CmdRenv(
 						new String[] { "-env", "testdata/env/envPropsPath06.xml", "u" }));
 				sin = new SequenceInputStream(new InputStreamLines());
 				bStream = new ByteArrayOutputStream();
 				sout = new PrintStream(bStream);
 				env.execute(sin, sout);
 				assertEquals("pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path.system"));
 				assertEquals(new File("testdata/testinstall/otherapp/1.0").getAbsolutePath(),
 						env.getPropertyValue("toolhome.otherapp"));
 				assertEquals(new File("testdata/testinstall/myapp/1.0.2").getAbsolutePath() + File.separator + "bin"
 						+ File.pathSeparator + new File("testdata/testinstall/otherapp/1.0").getAbsolutePath()
 						+ File.separator + "bin" + File.pathSeparator + File.separator + "fixed" + File.separator
 						+ "extension" + File.pathSeparator + "pathComponent1" + File.pathSeparator + "pathComponent2",
 						env.getPropertyValue("cmd.path"));
 			} finally {
 				RapidEnvTestHelper.tearDownProfile(env);
 			}
 		} finally {
 			if (new File("testdata/testinstall/myapp").exists()) {
 				FileHelper.deleteDeep(new File("testdata/testinstall/myapp"));
 			}
 			if (new File("testdata/testinstall/otherapp").exists()) {
 				FileHelper.deleteDeep(new File("testdata/testinstall/otherapp"));
 			}
 		}
 	}
 
 	/**
 	 * default log level INFO.
 	 */
 	@Test
 	public void testLogLevelDefault() {
 		assertEquals(Level.INFO, new RapidEnvInterpreter(new CmdRenv(
 				new String[] { "-env", "testdata/env/env.xml", "s" })).getLogLevel());
 	}
 
 	/**
 	 * option "-verbose" specifies log level FINE.
 	 */
 	@Test
 	public void testLogLevelVerbose() {
 		assertEquals(Level.FINE, new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"-verbose", "s" })).getLogLevel());
 	}
 
 	/**
 	 * option "-debug" specifies log level FINER.
 	 */
 	@Test
 	public void testLogLevelDebug() {
 		assertEquals(Level.FINER, new RapidEnvInterpreter(new CmdRenv(new String[] { "-env", "testdata/env/env.xml",
 				"-debug", "s" })).getLogLevel());
 	}
 
 	/**
 	 * Test helper class to simulate multiple input lines.
 	 * 
 	 * @author Raquel Silva, Martin Bluemel
 	 */
 	private static class InputStreamLines implements Enumeration<InputStream> {
 
 		private final LinkedList<InputStream> lines = new LinkedList<InputStream>();
 
 		/**
 		 * Constructor with given input lines.
 		 * 
 		 * @param lineArray
 		 *            a string array with lines to enter
 		 */
 		public InputStreamLines(String[] lineArray) {
 			for (final String s : lineArray) {
 				this.lines.add(new ByteArrayInputStream((s + '\n').getBytes()));
 			}
 		}
 
 		/**
 		 * Constructor with no input lines.
 		 */
 		public InputStreamLines() {
 			this(new String[] {});
 		}
 
 		@Override
 		public boolean hasMoreElements() {
 			return !this.lines.isEmpty();
 		}
 
 		@Override
 		public InputStream nextElement() {
 			return this.lines.pop();
 		}
 	}
 }
