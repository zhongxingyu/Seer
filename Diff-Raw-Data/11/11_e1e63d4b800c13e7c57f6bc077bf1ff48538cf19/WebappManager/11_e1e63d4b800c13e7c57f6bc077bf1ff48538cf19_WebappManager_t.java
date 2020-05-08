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
 package org.jabox.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.jabox.environment.Environment;
 
 public class WebappManager {
 
 	public static List<String> getWebapps() throws IOException {
 		File baseDir = Environment.getBaseDirFile();
 		File file = new File(baseDir, "servers.csv");
 		if (!file.exists()) {
 			baseDir.mkdirs();
 			createDefaultServersFile(file);
 		}
 		List<String> servers = readData(file);
 		return servers;
 	}
 
 	/**
 	 * Reads the data from the file and converts them to List.
 	 * 
 	 * @param file
 	 * @return a List of the data of the file.
 	 * @throws FileNotFoundException
 	 */
 	private static List<String> readData(final File file)
 			throws FileNotFoundException {
 		List<String> list = new ArrayList<String>();
 		Scanner scanner = new Scanner(file);
 		while (scanner.hasNextLine()) {
 			list.add(scanner.nextLine());
 		}
 		return list;
 	}
 
 	/**
 	 * Writes the data from the List to the file.
 	 * 
 	 * @param file
 	 * @param list
 	 * @throws IOException
 	 */
 	private static void writeData(final List<String> list, final File file)
 			throws IOException {
 		FileWriter fileWriter = new FileWriter(file);
 		for (String str : list) {
 			fileWriter.write(str + "\n");
 		}
 		fileWriter.flush();
 		fileWriter.close();
 	}
 
 	private static void createDefaultServersFile(final File file)
 			throws IOException {
 		List<String> list = new ArrayList<String>();
 		list.add("org.jabox.cis.hudson.HudsonServer");
 		// list.add("org.jabox.mrm.nexus.NexusServer");
 		// list.add("org.jabox.ide.eclipse.EclipseJNLPServer");
 		list.add("org.jabox.mrm.artifactory.ArtifactoryServer");
		list.add("org.jabox.sas.sonar.SonarServer");
 		writeData(list, file);
 	}
 }
