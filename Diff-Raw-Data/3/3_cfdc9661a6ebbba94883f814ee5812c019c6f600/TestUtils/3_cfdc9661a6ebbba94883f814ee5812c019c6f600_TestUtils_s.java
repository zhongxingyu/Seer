 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.iqchartimport;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.iqchartimport.iq.IQChartDatabase;
 
 /**
  * Utility methods for testing
  */
 public class TestUtils {
 	
 	private static IQChartDatabase database = null;
 	private static File tempZipFile, tempMdbFile;
 	
 	/**
 	 * Creates a date object
 	 * @param year the year
 	 * @param month the month (1..12)
 	 * @param day the date (1..31)
 	 * @return
 	 */
 	public static Date date(int year, int month, int day) {
 		return new GregorianCalendar(year, month - 1, day).getTime();
 	}
 	
 	/**
 	 * Creates and saves a global property
 	 * @param name the property name
 	 * @param value the property value
 	 * @return the property object
 	 */
 	public static GlobalProperty setGlobalProperty(String name, Object value) {
 		GlobalProperty property = Context.getAdministrationService().getGlobalPropertyObject(name);
 		String val = value != null ? value.toString() : null;
 				
 		if (property == null)
 			property = new GlobalProperty(name, val);
 		else
 			property.setPropertyValue(val);
 		
 		return Context.getAdministrationService().saveGlobalProperty(property);
 	}
 	
 	/**
 	 * Outputs a basic progress bar to the console
 	 * @param progress the progress (0...100)
 	 * @param existing the existing ticks as returned by the last call to this function
 	 * @return the existing ticks
 	 */
 	public static int progressBar(int progress, int existing) {
 		final int TOTAL_TICKS = 50;
 		final int progressTicks = (progress * TOTAL_TICKS) / 100;
 		final int newTicks = progressTicks - existing;
 		
 		for (int i = 0; i < newTicks; ++i)
 			System.out.print(".");
 		
 		if (progressTicks == TOTAL_TICKS && newTicks > 0)
 			System.out.println();
 			
 		return progressTicks;
 	}
 	
 	/**
 	 * Gets the embedded IQChart database for testing
 	 * @return the database
 	 * @throws IOException
 	 */
 	public static IQChartDatabase getDatabase() throws IOException {
 		if (database == null) {
 			// Extract embedded test database
 			tempZipFile = copyResource("/HIVData.mdb.zip");
 			tempMdbFile = extractZipEntry(tempZipFile, "HIVData.mdb");	
 			database = new IQChartDatabase("HIVData.mdb", tempMdbFile.getAbsolutePath());
 		}
 		
 		return database;
 	}
 	
 	/**
 	 * Extracts a resource to a temporary file for testing
 	 * @param path the database resource path
 	 * @return the temporary file
 	 * @throws Exception
 	 */
 	private static File copyResource(String path) throws IOException {
 		InputStream in = TestUtils.class.getResourceAsStream(path);
 		if (in == null)
 			throw new IOException("Unable to open resource: " + path);
 
 		File tempFile = File.createTempFile("temp", "." + getExtension(path));
 		
 		copyStream(in, new FileOutputStream(tempFile));
 		
 		in.close();
 		return tempFile;
 	}
 	
 	/**
 	 * Extracts an item from a zip file into a temporary file
 	 * @param zipFile the zip file
 	 * @param entryName the name of the item to extract
 	 * @return the item temp file
 	 * @throws ZipException
 	 * @throws IOException
 	 */
 	private static File extractZipEntry(File zipFile, String entryName) throws ZipException, IOException {
 		ZipFile zip = new ZipFile(zipFile);
 		ZipEntry entry = zip.getEntry(entryName);
 		InputStream in = zip.getInputStream(entry);
 		
 		File tempFile = File.createTempFile("temp", "." + getExtension(entryName));
 		
 		copyStream(in, new FileOutputStream(tempFile));
 		
 		in.close();
 		return tempFile;
 	}
 	
 	/**
 	 * Gets the extension of a file path
 	 * @param path the file path
 	 * @return the extension
 	 */
 	private static String getExtension(String path) {
 		final int index = path.lastIndexOf('.');
 		return index >= 0 ? path.substring(index + 1) : null;
 	}
 	
 	/**
 	 * Copies all data from one stream to another
 	 * @param in the input stream
 	 * @param out the output stream
 	 * @throws IOException
 	 */
 	private static void copyStream(InputStream in, OutputStream out) throws IOException {
 		final byte[] buf = new byte[256];
 		int len;
 		while ((len = in.read(buf)) >= 0)
 			out.write(buf, 0, len);
 		out.close();
 	}
 }
