 /*!
  * Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
  * wet-boew.github.com/wet-boew/License-eng.txt / wet-boew.github.com/wet-boew/Licence-fra.txt
  */
 
 package org.wet_boew.wet_boew.ant;
 
 import au.com.bytecode.opencsv.CSVReader;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import org.apache.tools.ant.BuildException;
 
 public class I18nMergeTask extends I18nBaseTask {
 	private File templateFile = null;
 	protected File outDir = null;
 	private String languageListProperty = null;
 	private String languageListSeparator= ",";
 	private String[] escapeCharacters = new String[]{"'"};
 
 	public void setTemplateFile (File f){
 		templateFile = f;
 	}
 
 	public void setOutDir (File f){
 		outDir = f;
 	}
 
 	public void setLanguageListProperty (String p){
 		languageListProperty = p;
 	}
 
 	public void setLanguageListSeparator(String s){
 		languageListSeparator = s;
 	}
 
 	@Override
 	public void execute() throws BuildException {
 		if (outDir == null){
 			outDir = this.getProject().getBaseDir();
 		}
 
 		validateAttributes();
 
 		CSVReader iReader = null;
 		OutputStreamWriter writer = null;
 		List<String> languagelist = new ArrayList<String>();
 
 		try {
 			FileInputStream fis = new FileInputStream(i18nFile);
 			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
 			iReader = new CSVReader(in);
 			List<String []> i18n = iReader.readAll();
			String template = new Scanner(templateFile).useDelimiter("\\Z").next();
 
 			for (int l = startAtCol + 1; l < i18n.get(startAtRow).length; l++){
 				String output = template;
 				String language = "";
 
 				for (int s = startAtRow; s < i18n.size(); s++){
 					String[] r = i18n.get(s);
 					if (s == startAtRow) {
 						language = r[l];
 						languagelist.add(language);
 					}
 
 					String value = "";
 					if (r.length <= l){
 						value = r[startAtCol + 1]; //defaults to the default language when string is missing
 					}else if (r[l].isEmpty()) {
 						value = r[startAtCol + 1]; //defaults to the default language when a string is empty
 					} else {
 						value = r[l];
 					}
 
 					for(String e : escapeCharacters){
 						value = value.replace(e, "\\" + e);
 					}
 					output = output.replace("@" + r[startAtCol] + "@", value);
 				}
 
 				String name = templateFile.getName();
 				try {
 					if (!outDir.exists()) {
 						outDir.mkdirs();
 					}
 					writer = new OutputStreamWriter(new FileOutputStream(outDir.getAbsolutePath() + File.separator + language + name.substring(name.lastIndexOf('.'))), "UTF-8");
 					writer.write(output);
 				} finally {
 					writer.close();
 				}
 			}
 
 			if (languageListProperty != null){
 				getProject().setProperty(languageListProperty, languagelist.toString().replace("[", "").replace("]", "").replace(", ", languageListSeparator));
 			}
 
 		} catch (FileNotFoundException e) {
 			throw new BuildException (e.getMessage());
 		} catch (IOException e) {
 			throw new BuildException (e.getMessage());
 		} finally {
 			try{
 				if (iReader != null){
 					iReader.close();
 				}
 			} catch (IOException e){
 
 			}
 		}
 	}
 
 	@Override
 	protected void validateAttributes() throws BuildException {
 		super.validateAttributes();
 		if (outDir == null){
 			throw new BuildException ("Specify the output directory");
 		}
 
 		if (!outDir.isDirectory()){
 			throw new BuildException ("Parameter 'outdir' must be a directory");
 		}
 
 		if (templateFile == null){
 			throw new BuildException ("Specify the template file");
 		}
 
 		if (!templateFile.isFile()){
 			throw new BuildException ("Parameter 'templateFile' must be a file");
 		}
 	}
 }
