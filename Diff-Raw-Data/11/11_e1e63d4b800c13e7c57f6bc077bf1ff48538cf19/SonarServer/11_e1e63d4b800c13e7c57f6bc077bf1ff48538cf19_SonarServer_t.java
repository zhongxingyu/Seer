 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.sas.sonar;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.jabox.apis.embedded.AbstractEmbeddedServer;
 import org.jabox.environment.Environment;
 import org.jabox.utils.DownloadHelper;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 /**
  * Hello world!
  * 
  */
 public class SonarServer extends AbstractEmbeddedServer {
 	private static final String URL = "http://dist.sonar.codehaus.org/sonar-2.6.zip";
 
 	public static void main(final String[] args) throws Exception {
 		new SonarServer().startServerAndWait();
 	}
 
 	@Override
 	public String getServerName() {
 		return "sonar";
 	}
 
 	@Override
 	public String getWarPath() {
 		File baseDir = Environment.getBaseDirFile();
 
 		// Download the sonar.zip
 		File zipFile = new File(baseDir, "tmp/sonar.zip");
 		if (!zipFile.exists()) {
 			DownloadHelper.downloadFile(URL, zipFile);
 		}
 		try {
 			Unzip.unzip(zipFile.getAbsolutePath(), baseDir.getAbsolutePath());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		File sonarBaseDir = new File(baseDir, "sonar-2.6");
		File sonarWar = execBuildWar(sonarBaseDir);
		return sonarWar.getAbsolutePath();
 	}
 
 	/**
 	 * Executes the Build War script.
 	 */
 	private File execBuildWar(File sonarBaseDir) {
 		File script = new File(sonarBaseDir, "war/build-war.bat");
 		try {
 			Process p = Runtime.getRuntime().exec(script.getAbsolutePath(),
 					null, new File(sonarBaseDir, "war"));
 			InputStream is = p.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			String line;
 
 			while ((line = br.readLine()) != null) {
 				System.out.println(line);
 			}
 
 			p.waitFor();
 			System.out.println(p.exitValue());
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return new File(sonarBaseDir, "war/sonar.war");
 	}
 
 	public static void doUnzip(String inputZip, String destinationDirectory)
 			throws IOException {
 		int BUFFER = 2048;
 		List<String> zipFiles = new ArrayList<String>();
 		File sourceZipFile = new File(inputZip);
 		File unzipDestinationDirectory = new File(destinationDirectory);
 		unzipDestinationDirectory.mkdir();
 
 		ZipFile zipFile;
 		// Open Zip file for reading
 		zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
 
 		// Create an enumeration of the entries in the zip file
 		Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
 
 		// Process each entry
 		while (zipFileEntries.hasMoreElements()) {
 			// grab a zip file entry
 			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
 
 			String currentEntry = entry.getName();
 
 			File destFile = new File(unzipDestinationDirectory, currentEntry);
 			destFile = new File(unzipDestinationDirectory, destFile.getName());
 
 			if (currentEntry.endsWith(".zip")) {
 				zipFiles.add(destFile.getAbsolutePath());
 			}
 
 			// grab file's parent directory structure
 			File destinationParent = destFile.getParentFile();
 
 			// create the parent directory structure if needed
 			destinationParent.mkdirs();
 
 			try {
 				// extract file if not a directory
 				if (!entry.isDirectory()) {
 					BufferedInputStream is = new BufferedInputStream(zipFile
 							.getInputStream(entry));
 					int currentByte;
 					// establish buffer for writing file
 					byte data[] = new byte[BUFFER];
 
 					// write the current file to disk
 					FileOutputStream fos = new FileOutputStream(destFile);
 					BufferedOutputStream dest = new BufferedOutputStream(fos,
 							BUFFER);
 
 					// read and write until last byte is encountered
 					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
 						dest.write(data, 0, currentByte);
 					}
 					dest.flush();
 					dest.close();
 					is.close();
 				}
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 
 			for (Iterator<String> iter = zipFiles.iterator(); iter.hasNext();) {
 				String zipName = iter.next();
 				doUnzip(zipName, destinationDirectory + File.separatorChar
 						+ zipName.substring(0, zipName.lastIndexOf(".zip")));
 			}
 
 		}
 	}
 
 	public static void main2(final String[] args) throws Exception {
 		Server server = new Server();
 		SocketConnector connector = new SocketConnector();
 		// Set some timeout options to make debugging easier.
 		connector.setMaxIdleTime(1000 * 60 * 60);
 		connector.setSoLingerTime(-1);
 		connector.setPort(9092);
 		server.setConnectors(new Connector[] { connector });
 
 		WebAppContext bb = new WebAppContext();
 		bb.setServer(server);
 		bb.setContextPath("/");
 		bb
 				.setWar("E:/Documents and Settings/Administrator/.m2/repository/org/codehaus/sonar/sonar-web/1.4.2/sonar-web-1.4.2.war");
 		bb.setParentLoaderPriority(true);
 		server.addHandler(bb);
 
 		try {
 			System.out
 					.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
 			server.start();
 			while (System.in.available() == 0) {
 				Thread.sleep(5000);
 			}
 			server.stop();
 			server.join();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(100);
 		}
 	}
 }
