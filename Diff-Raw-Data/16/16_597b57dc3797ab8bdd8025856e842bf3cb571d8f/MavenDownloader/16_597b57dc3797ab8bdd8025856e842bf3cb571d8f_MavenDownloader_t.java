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
 package org.jabox.maven.helper;
 
 import java.io.File;
 
import org.jabox.environment.Environment;
 import org.jabox.maven.helper.aether.aether.Aether;
 import org.sonatype.aether.collection.DependencyCollectionException;
 import org.sonatype.aether.resolution.ArtifactResolutionException;
 import org.sonatype.aether.transfer.ArtifactNotFoundException;
 
 public class MavenDownloader {
 
 	private static final String MAVEN_REPO = "http://repo1.maven.org/maven2/";
	private static final String LOCAL_REPO = Environment
			.getCustomMavenHomeDir().getAbsolutePath()
			+ "/repository/";
 
 	/**
 	 * @param groupId
 	 * @param artifactId
 	 * @param version
 	 * @param extension
 	 */
 	public static File downloadArtifact(final String groupId,
 			final String artifactId, final String version,
 			final String extension) {
 		System.out.println("Downloading: " + groupId + ":" + artifactId + ":"
 				+ version + ":" + extension);
 		try {
 			return retrieveArtifact(groupId, artifactId, version, extension);
 		} catch (ArtifactResolutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ArtifactNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DependencyCollectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public static File getArtifactFile(final String groupId,
 			final String artifactId, final String version, final String type) {
		String m2Home = Environment.getCustomMavenHomeDir().getAbsolutePath();
		assert m2Home != null;
		StringBuffer sb = new StringBuffer(m2Home);
 		sb.append(File.separator);
 		sb.append("repository");
 		sb.append(File.separator);
 		sb.append(groupId.replace('.', File.separatorChar));
 		sb.append(File.separator);
 		sb.append(artifactId);
 		sb.append(File.separator);
 		sb.append(version);
 		sb.append(File.separator);
 		sb.append(artifactId);
 		sb.append("-");
 		sb.append(version);
 		sb.append(".");
 		sb.append(type);
 		File file = new File(sb.toString());
 		assert file.exists();
 		return file;
 	}
 
 	private static File retrieveArtifact(final String groupId,
 			final String artifactId, final String version,
 			final String extension) throws ArtifactResolutionException,
 			ArtifactNotFoundException, DependencyCollectionException {
 		Aether aether = new Aether(MAVEN_REPO, LOCAL_REPO);
 
 		File result = aether.resolveArtifact(groupId, artifactId, version,
 				extension);
 
 		return result;
 	}
 }
