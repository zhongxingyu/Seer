 package com.despegar.framework.eclipse.maven;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 import com.despegar.framework.hash.HashHelper;
 
 /**
 * @phase initialize
  * @goal configure
  * 
  * @author gkondolf@despegar.com
  */
 public class MavenM2ECodeStyleMojo extends AbstractMojo {
 
 	private static final String ECLIPSE_SETTINGS_FOLDER = ".settings";
 	// private static final String MAVEN_CODE_STYLE_BASE_URL_PROPERTY =
 	// "m2e.codestyle.base.url";
 	private static final String CORE_PREFS_FILE = "org.eclipse.core.resources.prefs";
 	private static final String JDT_CORE_PREFS_FILE = "org.eclipse.jdt.core.prefs";
 	private static final String JDT_UI_PREFS_FILE = "org.eclipse.jdt.ui.prefs";
 
 	/**
 	 * @component
 	 */
 	private BuildContext buildContext;
 
 	/**
 	 * Code style base url.
 	 * 
 	 * @parameter expression="${codeStyleBaseUrl}"
 	 * @required
 	 */
 	private String codeStyleBaseUrl;
 
 	/**
 	 * Base Directory.
 	 * 
 	 * @parameter expression="${project.basedir}"
 	 * @required
 	 */
 	private File baseDir;
 
 	private HttpClient httpClient = new DefaultHttpClient();
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		File sttDir = new File(baseDir, ECLIPSE_SETTINGS_FOLDER);
 		if (!sttDir.exists()) {
 			getLog().info("Created [" + ECLIPSE_SETTINGS_FOLDER + "] folder!");
 			sttDir.mkdir();
 		}
 		
 		getLog().info("Using code-style base-url [" + codeStyleBaseUrl + "]...");
 
 		configureJDTUIPrefs(sttDir, codeStyleBaseUrl);
 		configureJDTCorePrefs(sttDir, codeStyleBaseUrl);
 		configureCorePrefs(sttDir, codeStyleBaseUrl);
 
 		getLog().info("Refreshing newly created settings...");
 		buildContext.refresh(sttDir);
 	}
 
 	private void configureJDTUIPrefs(File dir, String baseUrl) {
 		downloadAndWriteFile(dir, baseUrl, JDT_UI_PREFS_FILE);
 	}
 
 	private void configureJDTCorePrefs(File dir, String baseUrl) {
 		downloadAndWriteFile(dir, baseUrl, JDT_CORE_PREFS_FILE);
 	}
 
 	private void configureCorePrefs(File dir, String baseUrl) {
 		downloadAndWriteFile(dir, baseUrl, CORE_PREFS_FILE);
 	}
 
 	private void downloadAndWriteFile(File dir, String baseUrl, String fileName) {
 
 		getLog().info("Checking new version of [" + fileName + "]...");
 		String content = getUrlContentAsString(baseUrl + "/" + fileName);
 
 		if (content != null) {
 			String md5New = HashHelper.md5(cleanUpString(content));
 			getLog().info("Fingerprint for [" + fileName + "] = {" + md5New + "}");
 
 			File file = new File(dir, fileName);
 			String md5Old = HashHelper.md5(cleanUpString(readTextFile(file)));
 			getLog().info("Fingerprint for [" + fileName + "] = {" + md5Old + "}");
 
 			if (!md5New.equals(md5Old)) {
 				writeTextFile(file, content);
 				getLog().info("File [" + fileName + "] written.");
 			} else {
 				getLog().info("File [" + fileName + "] didn't change!");
 			}
 		}
 	}
 
 	private String readTextFile(File file) {
 		FileReader inFile = null;
 		StringBuilder sb = new StringBuilder();
 		try {
 			inFile = new FileReader(file);
 			BufferedReader in = new BufferedReader(inFile);
 			String text = null;
 			while ((text = in.readLine()) != null) {
 				sb.append(text).append(System.getProperty("line.separator"));
 			}
 		} catch (IOException e) {
 			getLog().warn("Exception reading file [" + file.getName() + "]", e);
 		} finally {
 			if (inFile != null) {
 				try {
 					inFile.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	private void writeTextFile(File file, String content) {
 		FileWriter outFile = null;
 		try {
 			outFile = new FileWriter(file);
 			PrintWriter out = new PrintWriter(outFile);
 			out.write(content);
 		} catch (IOException e) {
 			getLog().warn("Exception writing file [" + file.getName() + "]", e);
 		} finally {
 			if (outFile != null) {
 				try {
 					outFile.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 	}
 
 	private String cleanUpString(String content) {
 		BufferedReader br = new BufferedReader(new StringReader(content));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		try {
 			while((line = br.readLine()) != null) {
 				sb.append(line).append("\n");
 			}
 		} catch (IOException e) {
 		}
 		
 		return sb.toString();
 	}
 	
 	private String getUrlContentAsString(String url) {
 
 		HttpResponse httpResponse = null;
 		try {
 			httpResponse = httpClient.execute(new HttpGet(url));
 		} catch (ClientProtocolException e1) {
 			throw new RuntimeException(e1);
 		} catch (IOException e1) {
 			throw new RuntimeException(e1);
 		}
 		if (httpResponse.getStatusLine().getStatusCode() == 404)
 			return null;
 
 		String response = null;
 		try {
 			response = IOUtils.toString(httpResponse.getEntity().getContent(),
 					"UTF-8");
 		} catch (IllegalStateException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return response;
 	}
 
 }
