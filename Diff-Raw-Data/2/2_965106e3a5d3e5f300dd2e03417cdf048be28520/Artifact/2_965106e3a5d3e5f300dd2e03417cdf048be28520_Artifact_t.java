 package com.github.swt_release_fetcher;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.Maven;
 import org.apache.maven.cli.MavenLoggerManager;
 import org.apache.maven.cli.PrintStreamLogger;
 import org.apache.maven.execution.DefaultMavenExecutionRequest;
 import org.apache.maven.execution.MavenExecutionRequest;
 import org.apache.maven.execution.MavenExecutionResult;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.codehaus.plexus.ContainerConfiguration;
 import org.codehaus.plexus.DefaultContainerConfiguration;
 import org.codehaus.plexus.DefaultPlexusContainer;
 import org.codehaus.plexus.PlexusContainerException;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 
 public class Artifact {
 	private final File file;
 	private boolean newDownload;
 
 	public Artifact(File file) {
 		super();
 		this.file = file;
 	}
 
 	private boolean validateFileMd5(File file, String expected) throws IOException {
 		InputStream downloadedFile = new FileInputStream(file);
 		try {
 			String calculatedMd5 = DigestUtils.md5Hex(downloadedFile);
 			return calculatedMd5.equals(expected);
 		} finally {
 			downloadedFile.close();
 		}
 	}
 
 	public void downloadAndValidate(URL downloadUrl, URL checksumUrl) throws IOException {
 		String md5FileContent = IOUtils.toString(checksumUrl.openStream());
 		String md5Hash = md5FileContent.split(" ")[0];
 		if (file.exists() && validateFileMd5(file, md5Hash)) {
 			System.out.println("SKIP");
 			return;
 		}
 
 		OutputStream outputFile = new FileOutputStream(file);
 		try {
 			IOUtils.copy(downloadUrl.openStream(), outputFile);
 		} finally {
 			outputFile.close();
 		}
 		if (!validateFileMd5(file, md5Hash)) {
 			throw new IOException("MD5 Validation Failed! Expected hash: " + md5Hash);
 		}
 		newDownload = true;
 		System.out.println("DONE");
 	}
 
 	public void generatePom(File pomFile) throws IOException {
 		Velocity.addProperty("resource.loader", "class");
 		Velocity.addProperty("class.resource.loader.class",
 				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
 		Velocity.init();
 		VelocityContext context = new VelocityContext();
 
 		// TODO fill those correctly
 		context.put("version", "3.7.2");
 		context.put("artifactId", "foo.bar");
 
 		Writer writer = new FileWriter(pomFile);
 		try {
 			Velocity.mergeTemplate("pom.vm", "UTF-8", context, writer);
 		} finally {
 			writer.close();
 		}
 	}
 
 	public void deploy() throws IOException {
 		File jarFile = null;
 		File sourcesFile = null;
 		File pomFile = null;
 		ZipFile zipFile = null;
 
 		try {
 			jarFile = File.createTempFile("deploy", ".jar");
 			sourcesFile = File.createTempFile("deploy", "-sources.jar");
 			pomFile = File.createTempFile("pom", ".xml");
 
 			zipFile = new ZipFile(file);
 			extractFromZip(zipFile, "swt.jar", jarFile);
 			extractFromZip(zipFile, "src.zip", sourcesFile);
 			generatePom(pomFile);
 
 			runMavenDeploy(pomFile, jarFile, sourcesFile);
 
 		} finally {
 			if (zipFile != null) {
 				zipFile.close();
 			}
 			FileUtils.deleteQuietly(jarFile);
 			FileUtils.deleteQuietly(sourcesFile);
 			FileUtils.deleteQuietly(pomFile);
 		}
 	}
 
 	public Maven initMaven() {
 		ContainerConfiguration cc = new DefaultContainerConfiguration().setName("maven");
 		DefaultPlexusContainer container;
 		try {
 			container = new DefaultPlexusContainer(cc);
 			PrintStreamLogger logger = new PrintStreamLogger(System.out);
 			container.setLoggerManager(new MavenLoggerManager(logger));
 			return container.lookup(Maven.class);
 		} catch (PlexusContainerException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} catch (ComponentLookupException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void runMavenDeploy(File pomFile, File jarFile, File sourcesFile) {
 		// TODO
 		// mvn deploy:deploy-file -f %1.pom -DpomFile=%1.pom -Dfile=%1.jar
 		// -Durl=svn:https://swt-repo.googlecode.com/svn/repo
 		// -Dsources=%1-sources.jar
 		Properties properties = new Properties();
 		properties.put("pomFile", pomFile.getAbsolutePath());
 		properties.put("file", jarFile.getAbsolutePath());
 		properties.put("sources", sourcesFile.getAbsolutePath());
 		properties.put("url", "svn:https://swt-repo.googlecode.com/svn/repo");
 
 		MavenExecutionRequest request = new DefaultMavenExecutionRequest();
 		request.setPom(pomFile);
		request.setGoals(Arrays.asList(new String[] { "deploy:deploy-file" }));
 		request.setSystemProperties(properties);
 
 		Maven maven = initMaven();
 		MavenExecutionResult result = maven.execute(request);
 
 		if (result.hasExceptions()) {
 			System.out.println("Maven deploy failed!");
 			System.out.println(result.getExceptions());
 			throw new RuntimeException("Maven deploy failed!", result.getExceptions().get(0));
 		} else {
 			System.out.println("Maven deploy succeeded!");
 		}
 	}
 
 	private void extractFromZip(ZipFile zipFile, String fileNameToExtract, File targetFile) throws IOException {
 		ZipEntry entry = zipFile.getEntry(fileNameToExtract);
 		InputStream inputStream = null;
 		OutputStream outputStream = null;
 		try {
 			inputStream = zipFile.getInputStream(entry);
 			outputStream = new FileOutputStream(targetFile);
 			IOUtils.copy(inputStream, outputStream);
 		} finally {
 			IOUtils.closeQuietly(inputStream);
 			IOUtils.closeQuietly(outputStream);
 		}
 	}
 
 	public File getFile() {
 		return file;
 	}
 
 	public boolean isNewDownload() {
 		return newDownload;
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		Artifact art = new Artifact(new File("downloads/win32-win32-x86_64.zip"));
 		art.deploy();
 	}
 }
