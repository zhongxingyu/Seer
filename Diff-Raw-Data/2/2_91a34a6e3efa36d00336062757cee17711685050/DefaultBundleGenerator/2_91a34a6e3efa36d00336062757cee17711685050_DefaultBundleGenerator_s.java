 /**
  * Copyright 2013 Ben Navetta <ben.navetta@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.bennavetta.util.tycho.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.apache.maven.model.Model;
 import org.apache.maven.model.building.ModelBuildingException;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.sonatype.aether.artifact.Artifact;
 
 import com.bennavetta.util.tycho.BundleGenerator;
 import com.bennavetta.util.tycho.maven.Maven;
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.net.InternetDomainName;
 import com.google.common.primitives.Ints;
 
 /**
  * Default bundle metadata generator
  * @author ben
  * @see <a href="http://fusesource.com/docs/esb/4.4.1/esb_deploy_osgi/ESBMavenOSGiConfig.html">FuseSource Maven OSGi Config</a>
  */
 public class DefaultBundleGenerator implements BundleGenerator
 {	
 	private static final Ordering<String> PATH_COMPONENTS = new Ordering<String>() {
 		@Override
 		public int compare(String a, String b)
 		{
 			// favor something like org.junit over junit.assert
 			boolean aHasTld = hasTld(a);
 			boolean bHasTld = hasTld(b);
 			if(aHasTld && ! bHasTld)
 				return 1;
 			if(!aHasTld && bHasTld)
 				return -1;
 			
 			return Ints.compare(
 				Iterables.size(Splitter.on('/').split(a)),
 				Iterables.size(Splitter.on('/').split(b)));
 		}
 		
 		private boolean hasTld(String path)
 		{
 			String domain = Joiner.on('.').join(Lists.reverse(Lists.newArrayList(Splitter.on('/').split(path))));
 			return InternetDomainName.isValid(domain) && InternetDomainName.from(domain).hasPublicSuffix();
 		}
 	};
 	
 	private static final CharMatcher PUNCTUATION = CharMatcher.ASCII
 			.and(CharMatcher.is('-').or(CharMatcher.is('.'))); // what we are going to get in an artifact id
 	
 	@Override
 	public String getSymbolicName(Artifact artifact)
 	{
 		if(artifact.getGroupId().indexOf('.') == -1)
 		{
 			// Find the first package with classes in it
 			try(JarFile jar = new JarFile(artifact.getFile())) // commons-logging:commons-logging -> org.apache.commons.logging
 			{
 				List<String> contents = new ArrayList<>();
 				Enumeration<JarEntry> entries = jar.entries();
 				while(entries.hasMoreElements())
 				{
 					JarEntry entry = entries.nextElement();
 					contents.add(entry.getName());
 				}
 				// sort by number of slashes
 				Collections.sort(contents, PATH_COMPONENTS);
 				for(String path : contents)
 				{
 					if(path.endsWith(".class"))
 					{
 						path = path.substring(0, path.lastIndexOf('/')).replace('/', '.');
 						if(path.startsWith("/"))
 						{
 							path = path.substring(1);
 						}
 						return path;
 					}
 				}
 			}
 			catch (IOException e)
 			{
 				return null;
 			}
 		}
 		else if(Iterables.getLast(Splitter.on('.').split(artifact.getGroupId())).equals(artifact.getArtifactId()))
 		{
 			return artifact.getGroupId(); // org.apache.maven:maven -> org.apache.maven
 		}
 		else
 		{
 			String gidEnd = Iterables.getLast(Splitter.on('.').split(artifact.getGroupId()));
			if(artifact.getArtifactId().startsWith(gidEnd))
 			{
 				// org.apache.maven:maven-core -> org.apache.maven.core
 				return artifact.getGroupId() + "." + PUNCTUATION.trimFrom(artifact.getArtifactId().substring(gidEnd.length()));
 			}
 			else
 			{
 				return artifact.getGroupId() + "." + artifact.getArtifactId(); // groupId + "." + artifactId
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getVersion(Artifact artifact)
 	{
 		return artifact.getVersion().replace('-', '.');
 	}
 
 	@Override
 	public String getBundleName(Artifact artifact)
 	{
 		//return artifact.getGroupId() + " " +  artifact.getArtifactId(); // don't have access to the name
 		try
 		{
 			Model pom = Maven.getModel(artifact);
 			String name = pom.getName();
 			System.out.println(pom);
 			if(Strings.isNullOrEmpty(name))
 				return artifact.getArtifactId();
 			return name;
 		}
 		catch(ModelBuildingException | ComponentLookupException e)
 		{
 			System.err.println("Exception: " + e);
 			return artifact.getArtifactId(); // don't have access to the name
 		}
 	}
 
 }
