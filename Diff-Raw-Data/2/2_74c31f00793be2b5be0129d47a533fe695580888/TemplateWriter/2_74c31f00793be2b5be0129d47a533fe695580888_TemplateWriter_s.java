 package org.zend.sdklib.internal.project;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Writes files from template
  *
  */
 public class TemplateWriter {
 
 	private static final String TEMPLATES_DIR = "templates";
 	private static final String SCRIPTS_DIR = "scripts";
 	public static final String DESCRIPTOR = "descriptor.xml";
 	
 	/**
 	 * 
 	 * @param name - name of the project
 	 * @param withContent - whether to write other contents than scripts and descriptor
 	 * @param withScripts - whether to write scripts
 	 * @param destination - destination directory
 	 * @throws IOException
 	 */
 	public void writeTemplate(String name, boolean withContent, boolean withScripts, File destination) throws IOException {
 		File descrFile = new File(destination, DESCRIPTOR);
 		if (! descrFile.getParentFile().exists()) {
 			descrFile.getParentFile().mkdirs();
 		}
 		
 		if (! descrFile.exists()) {
 			writeDescriptor(name, withScripts, new FileWriter(descrFile));
 		}
 		
 		String[] resources = getTemplateResources();
 		if (resources == null) {
 			return;
 		}
 		
 		for (int i = 0; i < resources.length; i++) {
 			if (withScripts || (withContent && (!isScript(resources[i])))) {
 				writeStaticResource(resources[i], destination);
 			}
 		}
 	}
 	
 	private boolean isScript(String path) {
 		return path.startsWith(SCRIPTS_DIR);
 	}
 
 	private void writeDescriptor(String name, boolean withScripts, Writer out) throws IOException {
 		if (name == null) {
			throw new IllegalArgumentException("Name parameter is required in order to create descriptor");
 		}
 		out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
 		out.append("<package version=\"1.4.11\" xmlns=\"http://www.zend.com/server/deployment-descriptor/1.0\" xmlns:xsi=\">http://www.w3.org/2001/XMLSchema-instance\">\n");
 		out.append(" <name>").append(xmlEscape(name)).append("</name>\n");
 		out.append(" <summary>short description</summary>\n");
 		out.append(" <description>long description</description>\n");
 		out.append(" <version>\n");
 		out.append("   <release>1.0.0.0/release>\n");
 		out.append(" </version>\n");
 		out.append(" <icon></icon>\n");
 		out.append(" <eula></eula>\n");
 		out.append(" <docroot></docroot>\n");
 		if (withScripts) {
 			out.append(" <scriptsdir>scripts</scriptsdir>\n");
 		}
 		out.append(" <dependencies></dependencies>\n");
 		out.append(" <parameters></parameters>\n");
 		out.append("</package>\n");
 		out.close();
 	}
 	
 	private CharSequence xmlEscape(String name) {
 		for (int i = 0; i < name.length(); i++) {
 			char c = name.charAt(i);
 			if (c == '&' || c== '<' || c == '>') {
 				return "<![CDATA[" + name.replaceAll("]]>", "]]>]]><![CDATA[") + "]]>";
 			}
 		}
 		
 		return name;
 	}
 
 	private void writeStaticResource(String resourceName, File destination) throws IOException {
 		URL url = getTemplateResource(resourceName);
 		
 		File destFile = new File(destination, resourceName);
 		File dir = destFile.getParentFile();
 		if (! dir.exists()) {
 			dir.mkdirs();
 		}
 		
 		if (destFile.exists()) { // don't overwrite already existing files
 			return;
 		}
 		
 		if (!destFile.getParentFile().canWrite()) { // skip if parent directory is not writeable
 			return;
 		}
 		
 		FileOutputStream out = new FileOutputStream(destFile);
 		
 		InputStream is = null;
 		try {
 			is = url.openStream();
 			byte[] buf = new byte[4098];
 			int c;
 			while ((c = is.read(buf)) > 0) {
 				out.write(buf, 0, c);
 			}
 		} finally {
 			if (is != null) {
 				is.close();
 			}
 			if (out != null) {
 				out.close();
 			}
 		}
 	}
 
 	private File getTemplatesRoot() {
 		// <somewhere>/org.zend.sdk/lib/zend_sdk.jar
 		File zendSDKJarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
 		
 		// <somewhere>/org.zend.sdk
 		File zendSDKroot = zendSDKJarFile.getParentFile().getParentFile();
 		File templates = new File(zendSDKroot, TEMPLATES_DIR);
 		
 		// in development-time scenario, classes are in "sdklib", instead of "lib/zend_sdk.jar" 
 		if (!templates.exists()) {
 			zendSDKroot = zendSDKJarFile.getParentFile();
 			templates = new File(zendSDKroot, TEMPLATES_DIR);
 		}
 		
 		return templates.exists() ? templates : null;
 	}
 	
 	private URL getTemplateResource(String resourceName) {
 		File root = getTemplatesRoot();
 		if (root == null) {
 			return null;
 		}
 		
 		File resource = new File(root, resourceName);
 		//System.out.println(resource);
 		try {
 			return resource.toURI().toURL();
 		} catch (MalformedURLException e) {
 			return null;
 		}
 	}
 
 	private void recursiveFindFiles(File dir, File root, List<String> out) {
 		File[] files = dir.listFiles();
 		if (files == null) {
 			return;
 		}
 		
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isFile()) {
 				String relativePath = getRelativePath(files[i], root);
 				out.add(relativePath);
 			} else if (files[i].isDirectory()) {
 				//String fileName = files[i].getName();
 				//if (! fileName.startsWith(".")) { // ignore hidden files
 					recursiveFindFiles(files[i], root, out);
 				//}
 			}
 		}
 	}
 
 	private String getRelativePath(File file, File root) {
 		String filePath = file.getAbsolutePath();
 		String rootPath = root.getAbsolutePath();
 		
 		if (filePath.startsWith(rootPath)) {
 			return filePath.substring(rootPath.length() + 1);
 		}
 		
 		return filePath;
 	}
 
 	private String[] getTemplateResources() {
 		File root = getTemplatesRoot();
 		if (root == null) {
 			return null;
 		}
 		
 		List<String> files = new ArrayList<String>();
 		
 		recursiveFindFiles(root, root, files);
 		
 		return files.toArray(new String[files.size()]);
 	}
 
 }
