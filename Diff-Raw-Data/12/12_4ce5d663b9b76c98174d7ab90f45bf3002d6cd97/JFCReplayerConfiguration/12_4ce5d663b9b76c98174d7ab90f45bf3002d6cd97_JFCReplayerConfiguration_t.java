 /*	
  *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
  *  be obtained by sending an e-mail to atif@cs.umd.edu
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
  *  documentation files (the "Software"), to deal in the Software without restriction, including without 
  *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
  *	conditions:
  * 
  *	The above copyright notice and this permission notice shall be included in all copies or substantial 
  *	portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
  *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
  *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 package edu.umd.cs.guitar.replayer;
 
 import org.kohsuke.args4j.Option;
 
 import edu.umd.cs.guitar.util.Util;
 
 ;
 
 /**
  * Class contains the runtime configurations of JFC GUITAR Replayer
  * 
  * <p>
  * 
  * @author <a href="mailto:baonn@cs.umd.edu"> Bao Nguyen </a>
  */
 public class JFCReplayerConfiguration extends GReplayerConfiguration {
 
 	@Option(name = "-cf", usage = "Configure file for the gui recorder to recognize the terminal widgets", aliases = "--configure-file")
 	public static String CONFIG_FILE = // "resources" + File.separator +
 	// "config"
 	// + File.separator +
 	"configuration.xml";
 
 	// GUITAR runtime parameters
 	@Option(name = "-g", usage = "<REQUIRED> GUI file path", aliases = "--gui-file")
 	static String GUI_FILE = null;
 
 	@Option(name = "-e", usage = "<REQUIRED> EFG file path", aliases = "--efg-file")
 	static String EFG_FILE = null;
 
 	@Option(name = "-t", usage = "<REQUIRED> testcase file path", aliases = "--testcase-file")
 	static String TESTCASE = null;
 
 	@Option(name = "-gs", usage = "gui state file path", aliases = "--gui-state")
 	static String GUI_STATE_FILE = "GUITAR-Default.STA";
 
 	@Option(name = "-l", usage = "log file name ", aliases = "--log-file")
 	static String LOG_FILE = Util.getTimeStamp() + ".log";;
 
 	@Option(name = "-i", usage = "initial waiting time for the application to get stablized before being ripped", aliases = "--wait-time")
 	static int INITIAL_WAITING_TIME = 0;
 
 	@Option(name = "-d", usage = "step delay time", aliases = "--delay")
 	static int DELAY = 0;
 
 	@Option(name = "-to", usage = "testcase timeout", aliases = "--testcase-timeout")
 	static int TESTCASE_TIMEOUT = 30000;
 
 	@Option(name = "-so", usage = "test steptimeout", aliases = "--teststep-timeout")
 	static int TESTSTEP_TIMEOUT = 4000;
 
 	// Application Under Test
 	@Option(name = "-c", usage = "<REQUIRED> main class name for the Application Under Test ", aliases = "--main-class")
 	static String MAIN_CLASS = null;
 
	@Option(name = "-a", usage = "arguments for the Application Under Test, separated by ':' ", aliases = "--arguments")
 	static String ARGUMENT_LIST;
 
	@Option(name = "-u", usage = "URLs for the Application Under Test, separated by ':' ", aliases = "--urls")
 	static public String URL_LIST;
 
 	@Option(name = "-j", usage = "Java Virtual Machine options for the Application Under Test", aliases = "--jvm-options")
 	static String JVM_OPTIONS;
 
 	@Option(name = "-p", usage = "Pause after each step", aliases = "--pause")
 	static boolean PAUSE = false;
 	
 	@Option(name = "-r", usage = "Compare string using regular expression", aliases = "--regular-expression")
 	static boolean REG_USED= false;
 
 	// Cobertura Coverage collection
 	@Option(name = "-cd", usage = "Cobertura coverage output dir", aliases = "--coverage-dir")
 	static String COVERAGE_DIR = null;
 
 	// @Option(name = "-cm", usage = "Cobertura coverage main file ", aliases =
 	// "--coverage-main")
 	// static String COVERAGE_MAIN_FILE = null;
 
 	@Option(name = "-cc", usage = "Cobertura coverage clean file ", aliases = "--coverage-clean")
 	static String COVERAGE_CLEAN_FILE = null;
 
 }
