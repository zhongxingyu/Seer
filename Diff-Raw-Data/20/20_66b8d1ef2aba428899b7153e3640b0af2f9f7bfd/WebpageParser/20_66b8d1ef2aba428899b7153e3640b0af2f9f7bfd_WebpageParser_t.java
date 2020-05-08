 package com.rudyrigot.staticwebsitecompiler.parsers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import com.rudyrigot.staticwebsitecompiler.websiteversion.WebsiteVersion;
 
 public class WebpageParser {
 	
 	public static int FILETYPE_ROOTPAGE = 1;
 	public static int FILETYPE_MODULE = 2;
 
 	private String websiteRoot;
 	private WebsiteVersion version;
 	private CompilingActions compilingActions;
 	private String fileName;
 	private int fileType;
 	private StringBuffer output;
 	
 	public WebpageParser(String websiteRoot, WebsiteVersion version, CompilingActions compilingActions, String fileName, int fileType) {
 		this.websiteRoot = websiteRoot;
 		this.version = version;
 		this.compilingActions = compilingActions;
 		this.fileName = fileName;
 		this.fileType = fileType;
 		this.output = new StringBuffer();
 	}
 
 	public String parse() {
 		try {
 			String line = null;
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(websiteRoot+"/sources/rootpages/"+fileName))));
 			while ((line = br.readLine())!=null) {
 				line = compilingActions.executeCompilingActions(version, line, fileName);
 				if (line.length()>0) output.append(line+" ");
 			}
 			br.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (fileType==FILETYPE_ROOTPAGE) {
 			String outputFileName = websiteRoot+"/WebContent/"+version.getSubfolderName()+"/"+version.getFilenamePrefix()+fileName.replaceAll(".html$", "")+version.getFilenameSuffixe()+".html";
 			try {
 				PrintWriter pw = new PrintWriter(new File(outputFileName));
 				pw.append(output.toString());
 				pw.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
			System.out.println("File treated : "+fileName+" (version "+version.getVersionNumber()+")");
 		}
 		return output.toString();
 	}
 
 }
