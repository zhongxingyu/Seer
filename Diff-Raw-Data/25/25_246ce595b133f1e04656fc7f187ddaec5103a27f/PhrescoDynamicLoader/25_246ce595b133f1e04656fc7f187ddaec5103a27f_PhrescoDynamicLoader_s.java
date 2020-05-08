 package com.photon.phresco.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang.StringUtils;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.api.ApplicationProcessor;
 import com.photon.phresco.api.DynamicPageParameter;
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 
 public class PhrescoDynamicLoader {
 	RepoInfo repoInfo;
 	List<ArtifactGroup> plugins;
 	
 	public PhrescoDynamicLoader(RepoInfo repoInfo, List<ArtifactGroup> plugins) {
 		this.repoInfo = repoInfo;
 		this.plugins = plugins;
 	}
 	
 	public DynamicParameter getDynamicParameter(String className) throws PhrescoException {
 		DynamicParameter dynamicParameter;
 		try {
 			Class<DynamicParameter> apiClass = (Class<DynamicParameter>) Class
 					.forName(className, true, getURLClassLoader());
 			dynamicParameter = (DynamicParameter) apiClass.newInstance();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 		return dynamicParameter;
 	}
 	
 	public DynamicPageParameter getDynamicPageParameter(String className) throws PhrescoException {
 		DynamicPageParameter dynamicPageParameter;
 		try {
 			Class<DynamicPageParameter> apiClass = (Class<DynamicPageParameter>) Class
 					.forName(className, true, getURLClassLoader());
 			dynamicPageParameter = (DynamicPageParameter) apiClass.newInstance();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 		return dynamicPageParameter;
 	}
 	
 	public ApplicationProcessor getApplicationProcessor(String className) throws PhrescoException {
 		ApplicationProcessor applicationProcessor = null;
 		try {
 			Class<ApplicationProcessor> apiClass = (Class<ApplicationProcessor>) Class
 					.forName(className, true, getURLClassLoader());
 			ApplicationProcessor newInstance = apiClass.newInstance();
 			applicationProcessor = (ApplicationProcessor) newInstance;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 		return applicationProcessor;
 	}
 	
 	public InputStream getResourceAsStream(String fileName) throws PhrescoException {
 		List<Artifact> artifacts = new ArrayList<Artifact>();
 		String destFile = "";
 		JarFile jarfile = null; 
 		for (ArtifactGroup plugin : plugins) {
 			List<ArtifactInfo> versions = plugin.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
				Artifact artifact = new DefaultArtifact(plugin.getGroupId(), plugin.getArtifactId(), "jar", artifactInfo.getVersion());
				artifacts.add(artifact);
 			}
 		}
 		try {
 			URL[] artifactURLs = MavenArtifactResolver.resolve(repoInfo.getGroupRepoURL(), repoInfo.getRepoUserName(),
 					repoInfo.getRepoPassword(), artifacts);
 			for (URL url : artifactURLs) {
 				File jarFile = new File(url.toURI());
				jarfile = new JarFile(jarFile);
	            for (Enumeration<JarEntry> em = jarfile.entries(); em
	                    .hasMoreElements();) {
	                JarEntry jarEntry = em.nextElement();
	                if (jarEntry.getName().endsWith(fileName)) {
	                	destFile = jarEntry.getName();
	                }
	            }
 			}
 			if(StringUtils.isNotEmpty(destFile)) {
 				ZipEntry entry = jarfile.getEntry(destFile);
 				return jarfile.getInputStream(entry);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return null;
 	}
 	
 	private URLClassLoader getURLClassLoader() throws PhrescoException {
 		List<Artifact> artifacts = new ArrayList<Artifact>();
 		for (ArtifactGroup plugin : plugins) {
 			List<ArtifactInfo> versions = plugin.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				Artifact artifact = new DefaultArtifact(plugin.getGroupId(), plugin.getArtifactId(), "jar", artifactInfo.getVersion());
 				artifacts.add(artifact);
 			}
 		}
 		
 		URL[] artifactURLs;
 		try {
 			artifactURLs = MavenArtifactResolver.resolve(repoInfo.getGroupRepoURL(), repoInfo.getRepoUserName(),
 					repoInfo.getRepoPassword(), artifacts);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 		ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
 		if (clsLoader == null) {
 		    clsLoader = this.getClass().getClassLoader();
 		}
 		URLClassLoader classLoader = new URLClassLoader(artifactURLs, clsLoader);
 		return classLoader;
 	}
 	
 	
 	public static void main(String[] args) throws PhrescoException, IOException, ParserConfigurationException, SAXException, ConfigurationException {
 		RepoInfo repoinfo = new RepoInfo();
 		repoinfo.setRepoName("releases");
 		repoinfo.setRepoUserName("admin");
 //		repoinfo.setRepoPassword("ZGV2cmVwbzI=");
 		repoinfo.setRepoPassword("devrepo2");
 		repoinfo.setGroupRepoURL("http://172.16.17.226:8080/repository/content/groups/public/");
 		ArtifactGroup group = new ArtifactGroup();
 		group.setGroupId("com.photon.sample.appprocessor");
 		group.setArtifactId("testappprocessor");
 		group.setPackaging("jar");
 		ArtifactInfo info = new ArtifactInfo();
 		info.setVersion("1.0.0");
 		group.setVersions(Arrays.asList(info));
 		PhrescoDynamicLoader loader = new PhrescoDynamicLoader(repoinfo, Arrays.asList(group) );
 		ApplicationProcessor applicationProcessor = loader.getApplicationProcessor("com.photon.sample.appprocessor.testappprocessor.TestApplicationProcessor");
 		applicationProcessor.preCreate(null);
 	}
 	
 }
