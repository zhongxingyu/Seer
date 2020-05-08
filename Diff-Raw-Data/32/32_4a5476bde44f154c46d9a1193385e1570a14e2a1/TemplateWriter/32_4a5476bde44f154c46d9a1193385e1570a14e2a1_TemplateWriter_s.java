 package org.zend.sdklib.internal.project.template;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.URL;
 
 public class TemplateWriter {
 
 	public static final String DESCRIPTOR = "descriptor.xml";
 	
 	public static String[] resources = {
 		"public/index.html",
 	};
 	
 	public static String[] scripts = {
 		"scripts/post_activate.php", 
 		"scripts/post_deactivate.php", 
 		"scripts/post_stage.php", 
 		"scripts/post_unstage.php", 
 		"scripts/pre_activate.php", 
 		"scripts/pre_deactivate.php",
 		"scripts/pre_stage.php", 
 		"scripts/pre_unstage.php" };
 	
 	public void writeTemplate(String name, boolean withScripts, File destination) throws IOException {
 		writeDescriptor(name, withScripts, new FileWriter(new File(destination, DESCRIPTOR)));
 		for (int i = 0; i < resources.length; i++) {
 			writeStaticResource(resources[i], destination);
 		}
 		
 		if (withScripts) {
 			for (int i = 0; i < scripts.length; i++) {
 				writeStaticResource(scripts[i], destination);
 			}
 		}
 	}
 	
 	private void writeDescriptor(String name, boolean withScripts, Writer out) throws IOException {
 		out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
 		out.append("<package version=\"1.4.11\" xmlns=\"http://www.zend.com/server/deployment-descriptor/1.0\" xmlns:xsi=\">http://www.w3.org/2001/XMLSchema-instance\">\n");
 		out.append(" <name>").append(xmlEscape(name)).append("</name>\n");
 		out.append(" <summary>short description</summary>\n");
 		out.append(" <description>long description</description>\n");
 		out.append(" <version>\n");
 		out.append("   <release>1.0.0.0/release>\n");
 		out.append(" </version>\n");
 		out.append(" <eula></eula>\n");
 		out.append(" <docroot></docroot>\n");
 		if (withScripts) {
 			out.append(" <scriptsdir>scripts</scriptsdir>\n");
 		}
 		out.append("</package>\n");
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
 		URL url = getClass().getResource(resourceName);
 		
 		File destFile = new File(destination, resourceName);
 		File dir = destFile.getParentFile();
 		if (! dir.exists()) {
 			dir.mkdir();
 		}
 		FileOutputStream out = new FileOutputStream(destFile);
 		
		InputStream is = url.openStream();
		byte[] buf = new byte[4098];
		int c;
		while ((c = is.read(buf)) > 0) {
			out.write(buf, 0, c);
 		}
 	}
 	
 	public static void main(String[] args) throws IOException {
 		TemplateWriter tw = new TemplateWriter();
 		tw.writeStaticResource(resources[0], new File("."));
 	}
 }
