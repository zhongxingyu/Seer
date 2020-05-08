 /*
  * RapidEnv: ExceptionMap.java
  *
  * Copyright (C) 2011 Martin Bluemel
  *
  * Creation Date: 12/24/2011
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
 
 package org.rapidbeans.rapidenv.config.cmd;
 
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ExceptionMapTest {
 
 	@BeforeClass
 	public static void setUpClass() {
 	}
 
 	@AfterClass
 	public static void tearDownClass() {
 	}
 
 	/**
 	 * Simply test loading the map from resource file.
 	 */
 	@Test
 	public void testLoadMap() {
 		ExceptionMap map = ExceptionMap.load();
 		Assert.assertTrue(map.getExceptions().size() > 0);
 	}
 
 	/**
 	 * Test the mapping.
 	 * 
 	 * @throws MalformedURLException
 	 *             not expected here
 	 */
 	// @Test
 	// public void testMapException() throws MalformedURLException {
 	// ExceptionMap map = ExceptionMap.load();
 	// try {
 	// HttpDownload.download(new URL("http://ajshdbebfhdzrbsmkvlpo"), new
 	// File("xxx"), new ArrayList<Filecheck>());
 	// // We can not expect a RuntimeException in every case since
 	// // sometimes we simply get back a standard HTML page saying
 	// // that the address requested has not been found
 	// // Assert.fail("Expected a RuntimeException to be thrown");
 	// if (new File("xxx").exists()) {
 	// Assert.assertTrue(new File("xxx").delete());
 	// }
 	// } catch (RapidEnvException e) {
 	// // if you are off line
 	// ExceptionMapping mapping = map.map(e);
 	// Assert.assertSame(e, mapping.getMappedException());
 	// Assert.assertEquals(ExceptionMap.ERRORCODE_HTTP_DOWNLOAD,
 	// mapping.getErrorcodeAsInteger());
 	// Assert.assertEquals("Download failed from unknown host " +
 	// "\"ajshdbebfhdzrbsmkvlpo\"\n"
 	// + "Please check if you are connected to the LAN or Internet.",
 	// mapping.getMessage(Locale.ENGLISH));
 	// } catch (RuntimeException e) {
 	// // if you are online
 	// e.printStackTrace();
 	// }
 	// }
 }
