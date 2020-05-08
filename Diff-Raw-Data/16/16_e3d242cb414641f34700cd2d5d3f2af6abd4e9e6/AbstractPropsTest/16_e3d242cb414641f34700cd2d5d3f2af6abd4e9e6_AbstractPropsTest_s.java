 /**
  * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * - Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the
  *   distribution.
  *
  * - Neither the name of Anchor Intelligence nor the names of its
  *   contributors may be used to endorse or promote products derived
  *   from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
  */
 package com.fraudwall.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Date;
 
 import com.fraudwall.util.date.AnchorDateFormat;
 import com.fraudwall.util.io.IOUtils;
 
 /**
  * Abstract base class for all Anchor Intelligence unit tests that
  * require the initialization of {@link FWProps} and/or reference
  * particular files in the file system.
  * <p>
  * This class defines a {@link #ROOTDIR} constant that can
  * be used as the logical root of the file system for all tests.
  * Note that for subclasses of this class to work correctly, they must
  * be run from "&lt;svn&gt;/code/java" or a descendant.
  *
  * @author Allan Heydon
  * @author Ryan Hamilton
  */
 public abstract class AbstractPropsTest extends AbstractAnchorTest {
 	// Property name symbolic constants
 	private static final String DELETE_TEST_OUTPUT_PROP_NAME = "test.deleteOutput";
 
 	/**
 	 * The root directory used for all tests, namely
 	 * "&lt;svn&gt;/trunk/java".
 	 */
 	protected static final String ROOTDIR;
 
 	protected static final String ROOT_SQL;
 	protected static final String ROOT_SEED_DATA;
 
 	/*
 	 * Locate ROOTDIR by searching for directories up the tree
 	 * that end with code/java
 	 */
 	static {
 		String javaDir = IOUtils.locateJavaDirectory();
 		if (javaDir == null) {
 			throw new IllegalStateException("Unable to locate java source directory");
 		}
 		ROOTDIR = javaDir + File.separatorChar;
 		ROOT_SQL = ROOTDIR + File.separatorChar + "generated" + File.separatorChar + "sql";
 		ROOT_SEED_DATA = ROOTDIR + File.separatorChar + "seeddata";
 	}
 
 	/**
 	 * File containing definitions of adapters used to initially populate some
 	 * of the database tables for testing purposes.
 	 */
 	protected static final String ADBRITE_TEST_DEFS_FILE = "AdGuys/AdGuys-test.xml";
 	protected static final String ASK_TEST_DEFS_FILE = "Ask/Ask-test.xml";
 	protected static final String LOGPARSE_TEST_DEFS_FILE = "logparse.xml";
 	protected static final String TYPES_TEST_DEFS_FILE = "types.xml";
 	protected static final String MOOLA_TEST_DEFS_FILE = "moola-test.xml";
 
 	/**
 	 * Construct a test/user specific directory name for
 	 * test to write output into
 	 */
 	private String outDir;
 
 	/**
 	 * Returns the path of a temporary customer-specific output directory for this test.
 	 * The directory name includes the given <code>customerName</code> as well as a time
 	 * stamp to millisecond granularity.
 	 *
 	 * @see #getCreatedOutputDir()
 	 */
 	protected String outputDir() {
 		if (outDir == null) {
 			DateFormat dateFormat = new AnchorDateFormat("yyyyMMdd'T'HHmmssSSS");
 			outDir = IOUtils.getGlobalTmpDir().getPath() + File.separator
 					+ Utilities.getCurrentUser() + "-test" + File.separator
 					+ Utilities.ifNull(FWProps.getCustomerName(), "UNKNOWN") + "Test-" + dateFormat.format(new Date());
 		}
 		return outDir;
 	}
 
 	/**
 	 * Returns a {@link File} representation of the temporary output directory name
 	 * returned by {@link #outputDir()}. The directory is created in the file system
 	 * if it did not already exist.
 	 *
 	 * @see #outputDir()
 	 */
 	protected File getCreatedOutputDir() throws IOException {
 		File outputDir = new File(outputDir());
 		IOUtils.createDirectoryIfNeeded(outputDir);
 		return outputDir;
 	}
 
 	/**
 	 * Creates a root tmp directory if needed ($ROOT/tmp) and sets the
 	 * permissions on it to 777.
 	 */
 	protected void createTmpDirectory() throws IOException {
 		File tmpDir = IOUtils.getRootTmpDir();
 		IOUtils.createDirectoryIfNeeded(tmpDir);
 		ShellCommand.exec("chmod 777 " + tmpDir.getPath());
 	}
 
 	/**
 	 * Overridden to initialize {@link FWProps} with a null application
 	 * name and a null customer name. Call the two-argument variant to
 	 * override these defaults.
 	 *
 	 * @see #setUp(String, String)
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		this.setUp(null, TEST_CUSTOMER);
 	}
 
 	/**
 	 * Overridden to drop and re-create the test database.
 	 *
 	 * @param appName
 	 *            Name of the product to use to locate the correct property
 	 *            file(s) to read. May be <code>null</code>. When non-null,
	 *            this is typically one of the symbolic constants
	 *            {@link #LOGANALYZER}, {@link #DEFAULT}, or
	 *            {@link #DEFAULT}.
 	 * @param custName
 	 *            Name of the customer to use to locate the correct property
 	 *            file(s) to read. May be <code>null</code>. Should
 	 *            <em>not</em> be the name of a real customer, since we do not
 	 *            want any tests depending on production files.
 	 *
 	 * @see #setUp()
 	 * @see FWProps#initialize(String, String)
 	 */
 	protected void setUp(String appName, String custName) throws Exception {
 		super.setUp();
 		initializeFWPropsForTest(appName, custName);
 	}
 
 	protected void initializeFWPropsForTest(String appName, String custName) {
 		FWProps.initialize(appName, custName);
 		FWPropsTest.setProperty("ROOT", outputDir());
 
 		// configure property for accessing PositiveRule seed data
 		// The property name must agree with the value of PositiveRule.CSV_DIR_PROP_NAME; this code
 		// cannot reference that symbolic constant because it would introduce an upward dependency.
 		FWPropsTest.setProperty("PositiveRule.csvDir", new File(ROOT_SEED_DATA, "rules").getPath());
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		if (outDir != null) {
 			if (FWProps.getBooleanProperty(DELETE_TEST_OUTPUT_PROP_NAME) && new File(outDir).exists()) {
 				IOUtils.deleteDirectoryTree(new File(outDir));
 			}
 			outDir = null;
 		}
 		super.tearDown();
 	}
 
 
 	/**
 	 * Creates a file named <code>fileName</code> in the default output directory
 	 * with the contents of <code>lines</code>.
 	 */
 	protected File createFileFromLines(String fileName, String ... lines) throws IOException {
 		return createFileInDirFromLines(getCreatedOutputDir(), fileName, lines);
 	}
 
 	/**
 	 * Creates a file named <code>fileName</code> in the directory <code>dir</code>
 	 * with the contents of <code>lines</code>.
 	 */
 	protected File createFileInDirFromLines(File dir, String fileName, String ... lines) throws IOException {
 		File file = new File(dir, fileName);
 		BufferedWriter writer = IOUtils.getBufferedWriter(file, fileName.endsWith(".gz"));
 		try {
 			for (String line : lines) {
 				writer.write(line);
 				writer.newLine();
 			}
 			return file;
 		} finally {
 			writer.close();
 		}
 	}
 }
