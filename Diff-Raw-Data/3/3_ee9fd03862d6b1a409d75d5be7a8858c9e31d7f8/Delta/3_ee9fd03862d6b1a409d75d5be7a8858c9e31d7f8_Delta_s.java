 /*******************************************************************************
  * Copyright (c) Dec 7, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.update.parser;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.zend.sdkcli.monitor.ProgressMonitor;
 import org.zend.sdkcli.monitor.TextProgressMonitor;
 import org.zend.sdkcli.update.UpdateException;
 import org.zend.sdkcli.update.monitor.UpdateOutputStream;
 
 /**
  * 
  * Represents group of actions which should be performed to update Zend SDK. It
  * is responsible for downloading and extracting update package and execution of
  * all entries defined in it.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class Delta {
 
 	private static final String REMOVE = "remove";
 	private static final String ADD = "add";
 
 	private static final int BUFFER = 4096;
 
 	private List<AbstractDeltaEntry> entries;
 	private File temp;
 	private String zipLocation;
 	private int size;
 	private ProgressMonitor monitor;
 
 	public Delta(Document doc, String zipLocation, int size) {
 		this.zipLocation = zipLocation;
 		this.size = size;
 		this.entries = new ArrayList<AbstractDeltaEntry>();
 		this.temp = getTempFile();
 		this.monitor = new TextProgressMonitor(new PrintWriter(System.out));
 		parse(doc);
 	}
 
 	/**
 	 * @return temporary file for all files used during update
 	 */
 	public File getTemp() {
 		return temp;
 	}
 
 	/**
 	 * @return location of update package defined in delta.xml
 	 */
 	public String getZipLocation() {
 		return zipLocation;
 	}
 
 	/**
 	 * Executes update process based on delta defined in delta.xml file.
 	 * 
 	 * @param root
 	 *            - root file of Zend SDK
 	 * @return true if execution was performed successfully; otherwise returns
 	 *         false
 	 * @throws UpdateException
 	 */
 	public boolean execute(File root) throws UpdateException {
 		extractZip(getZipFile(getZipLocation()));
 		for (AbstractDeltaEntry entry : entries) {
 			if (!entry.execute(root)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private void parse(Document doc) {
 		NodeList removes = doc.getElementsByTagName(REMOVE);
 		for (int i = 0; i < removes.getLength(); i++) {
 			entries.add(new RemoveEntry(removes.item(i)));
 		}
 		NodeList adds = doc.getElementsByTagName(ADD);
 		for (int i = 0; i < adds.getLength(); i++) {
 			entries.add(new AddEntry(adds.item(i), getTemp()));
 		}
 	}
 
 	private File getZipFile(String location) throws UpdateException {
 		monitor.beginTask("Downloading update package", size);
		String fileName = location.substring(location.lastIndexOf('/') + 1);
 		File result = new File(getTemp(), fileName);
 		BufferedInputStream in = null;
 		FileOutputStream out = null;
 		try {
 			URL url = new URL(location);
 			try {
 				in = new BufferedInputStream(url.openStream());
 				out = new UpdateOutputStream(result, monitor, size);
 				byte data[] = new byte[BUFFER];
 				int count;
 				while ((count = in.read(data, 0, BUFFER)) != -1) {
 					out.write(data, 0, count);
 				}
 			} finally {
 				closeStream(in);
 				closeStream(out);
 			}
 		} catch (MalformedURLException e) {
 			throw new UpdateException(e);
 		} catch (IOException e) {
 			throw new UpdateException(e);
 		}
 		return result;
 	}
 
 	private void extractZip(File zipFile) throws UpdateException {
 		try {
 			monitor.beginTask("Extracting update package",
 					new ZipFile(zipFile).size());
 			BufferedOutputStream dest = null;
 			FileInputStream in = new FileInputStream(zipFile);
 			File parent = zipFile.getParentFile();
 			ZipInputStream zipStream = new ZipInputStream(
 					new BufferedInputStream(in));
 			ZipEntry entry;
 			while ((entry = zipStream.getNextEntry()) != null) {
 				int count;
 				byte data[] = new byte[BUFFER];
 				File file = new File(parent, entry.getName());
 				createParents(file.getParentFile(), parent);
 				if (entry.getName().endsWith("/")) {
 					file.mkdir();
 				} else {
 					file.getParentFile().mkdirs();
 					file.createNewFile();
 					FileOutputStream out = new FileOutputStream(file);
 					dest = new BufferedOutputStream(out, BUFFER);
 					while ((count = zipStream.read(data, 0, BUFFER)) != -1) {
 						dest.write(data, 0, count);
 					}
 					dest.flush();
 					closeStream(dest);
 				}
 				monitor.update(1);
 			}
 			closeStream(zipStream);
 		} catch (Exception e) {
 			throw new UpdateException(e);
 		}
 		monitor.endTask();
 	}
 
 	private File getTempFile() {
 		String tempDir = System.getProperty("java.io.tmpdir");
 		File file = new File(tempDir + File.separator + new Random().nextInt());
 		file.mkdir();
 		return file;
 	}
 
 	private boolean createParents(File file, File root) {
 		File parent = file.getParentFile();
 		if (parent.equals(root)) {
 			file.mkdir();
 			return true;
 		} else {
 			if (!parent.exists()) {
 				if (createParents(parent, root)) {
 					file.mkdir();
 					return true;
 				}
 			}
 		}
 		return true;
 	}
 
 	private void closeStream(Closeable stream) throws IOException {
 		if (stream != null) {
 			stream.close();
 		}
 	}
 
 }
