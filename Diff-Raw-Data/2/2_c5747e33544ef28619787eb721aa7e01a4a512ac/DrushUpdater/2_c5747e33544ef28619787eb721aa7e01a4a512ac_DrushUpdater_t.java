 /*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package es.uah.cc.ie.utils;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Arrays;
 import java.io.*;
 
 // base64
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.StringUtils;
 // GSON
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 // JUnit
 import static org.junit.Assert.*;
 
 // import es.uah.cc.ie.portalupdater.*;
 // import es.uah.cc.ie.utils.Test;
 import org.ontspace.agrisap.translator.Agrisap;
 import org.ontspace.dc.translator.DublinCore;
 
 /**
  // Maven repositories. Base64, GSON, JUnit
  <dependency>
  <groupId>commons-codec</groupId>
  <artifactId>commons-codec</artifactId>
  <version>1.8</version>
  </dependency>
  <dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.2.4</version>
  </dependency>
  <dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>4.11</version>
  </dependency>
  **/
 
 /**
  * @version 0.x
  * @author: Julian R.
  */
 public class DrushUpdater {
 
	private static final String DRUPAL_ROOT_DIR = "/www/newvoa3r";
 	// Tests
 	private final boolean RUN_TESTS = false;
 	private final int DUBLIN_CORE = 0;
 	private final int AGRISAP = 1;
 	private int test_passed;
 	private int test_errors;
 
 	/**
 	 * @param agrisap
 	 */
 	public DrushUpdater(Agrisap agrisap) {
 
 		// final Test test = new Test();
 		final Agrisap agris = new Agrisap(agrisap);
 
 		packObjectHelper(agris, AGRISAP);
 
 	}// end of Agisap constructor
 
 	/**
 	 *
 	 * @param dublinCore
 	 */
 	public DrushUpdater(DublinCore dublinCore) {
 
 		// final Test test = new Test();
 		final DublinCore dc = new DublinCore(dublinCore);
 
 		packObjectHelper(dc, DUBLIN_CORE);
 	}// end of Dublincore constructor
 
 	/**
 	 * @param args
 	 *            the command line arguments
 	 */
 	public static void main(String[] args) {
 	}// end of main method
 
 	/**
 	 * @param
 	 */
 	public static void cleanCache() {
 		try {
 			Process p = null;
 			p = Runtime.getRuntime().exec("drush vreset --root="+DRUPAL_ROOT_DIR);
 		} catch (Exception e) {
 		    System.out.println("There was an error cleaning cache. process may fail");
 
 		}
 	}
 
 	/**
 	 * DRY method that converts an AGRISAP / DC object to a serialized Base64
 	 * JSON
 	 *
 	 * @param object
 	 */
 	private void packObjectHelper(Object object, int objectType) {
 
 		final GsonBuilder builder = new GsonBuilder();
 		final Gson gson = builder.create();
 		/*
 		 * test.setWidth(10); test.setHeight(20); test.setDepth(30);
 		 */
 		String json = null;
 
 		try {
 			json = gson.toJson(object);
 			this.test_passed++;
 		} catch (Exception e) {
 			test_errors++;
 			System.out.println("oops!" + e.getCause());
 			// log4j
 			// log.error("oops!", e.getCause());
 		}
 
 		// final String json = gson.toJson(test);
 		// System.out.printf("Serialised: %s%n", json);
 
 		if (this.RUN_TESTS) {
 			try {
 				assertNotNull("Failed creating serialized JSON", json);
 				this.test_passed++;
 			} catch (Exception e) {
 				test_errors++;
 				System.out.println("oops!" + e.getCause());
 				// log4j
 				// log.error("oops!", e.getCause());
 			}
 		}
 
 		/*
 		 * final Test otherBox = gson.fromJson(json, Test.class);
 		 * System.out.printf("Same test: %s%n", test.equals(otherBox));
 		 */
 		String encoded = null;
 		try {
 			encoded = encode(json);
 			this.test_passed++;
 		} catch (Exception e) {
 			test_errors++;
 			System.out.println("oops!" + e.getCause());
 			// log4j
 			// log.error("oops!", e.getCause());
 		}
 
 		if (this.RUN_TESTS) {
 			try {
 				assertNotNull("Failed Base64 encoding", encoded);
 				this.test_passed++;
 			} catch (Exception e) {
 				test_errors++;
 				System.out.println("oops!" + e.getCause());
 				// log4j
 				// log.error("oops!", e.getCause());
 			}
 		}
 		String decoded = null;
 		if (this.RUN_TESTS) {
 			try {
 				decoded = decode(encoded);
 				this.test_passed++;
 			} catch (Exception e) {
 				test_errors++;
 				System.out.println("oops!" + e.getCause());
 				// log4j
 				// log.error("oops!", e.getCause());
 			}
 		}
 		if (this.RUN_TESTS) {
 			try {
 				assertNotNull("Failed Base64 decoding", decoded);
 				this.test_passed++;
 			} catch (Exception e) {
 				test_errors++;
 				System.out.println("oops!" + e.getCause());
 				// log4j
 				// log.error("oops!", e.getCause());
 			}
 		}
 		if (this.RUN_TESTS) {
 			try {
 				assertEquals("Failed Base64 encoding<->decoding", json, decoded);
 				this.test_passed++;
 			} catch (Exception e) {
 				test_errors++;
 				System.out.println("oops!" + e.getCause());
 				// log4j
 				// log.error("oops!", e.getCause());
 
 			}
 		}
 
 		// System.out.println("encoded: " + encoded);
 		// System.out.println("decoded: " + decoded);
 
 		boolean execution_error = false;
 		try {
 			// .exec() for GUN/Linux
 			// TODO: get --root param from opts.xml
 			Process p = null;
 			if (objectType == AGRISAP) {
 				p = Runtime.getRuntime().exec(
 				        "drush agup --root=" + DRUPAL_ROOT_DIR + " --data="
 				                + encoded);
 			} else {
 				p = Runtime.getRuntime().exec(
 				        "drush dcup --root=" + DRUPAL_ROOT_DIR + " --data="
 				                + encoded);
 			}
 			// comment out for greater performance
 			// get stream
 			InputStream is = p.getInputStream();
 
 			// comment out for greater performance
 			// prepare bufferedReader to parse result
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
 			// comment out for greater performance
 			// get line
 			String aux = br.readLine();
 
 			// comment out for greater performance
 			while (aux != null) {
 				// printf
 				
 				// FIELD VALUE ************
 				// System.out.println(aux);
 
 				// loop
 				aux = br.readLine();
 			}
 		} catch (Exception e) {
 			// catch exceptions
 			// replace for System.out.print/logging for greater performance
 			// e.printStackTrace();
 			// TODO: do some logging
 			execution_error = true;
 			System.out.println("oops!" + e.getCause());
 			// log4j
 			// log.error("oops!", e.getCause());
 
 		}
 		if (this.RUN_TESTS) {
 			try {
 				assertFalse("Failed execution", execution_error);
 				this.test_passed++;
 			} catch (Exception e) {
 				test_errors++;
 				System.out.println("oops!" + e.getCause());
 				// log4j
 				// log.error("oops!", e.getCause());
 			}
 		}
 		testReport();
 	}
 
 	/**
 	 * @param String
 	 */
 	private String decode(String s) {
 		return StringUtils.newStringUtf8(Base64.decodeBase64(s));
 	}// end of decode
 
 	/**
 	 * @param String
 	 */
 	private String encode(String s) {
 		return Base64.encodeBase64String(StringUtils.getBytesUtf8(s));
 	}// end of encode
 
 	/**
 	 * @param void
 	 */
 	private void testReport() {
 		// TODO: send this to log
 		if (this.RUN_TESTS) {
 			int total = test_passed + test_errors;
 			System.out.println(" tests passed: " + this.test_passed
 			        + " | errors: " + this.test_errors + " | total tests: "
 			        + total);
 		}
 	}// end of testReport
 }
