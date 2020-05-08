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
 import org.apache.tools.ant.Task;
 
 public class I18nMergeTask extends Task {
 	private File i18nFile = null;
 	private File templateFile = null;
 	private File outDir = null;
 	private int startAtCol = 1;
 	private int startAtRow = 1;
 	private String languageListProperty = null;
 
 	public void setI18nFile (File f){
 		i18nFile = f;
 	}
 
 	public void setTemplateFile (File f){
 		templateFile = f;
 	}
 
 	public void setOutDir (File f){
 		outDir = f;
 	}
 
 	public void setStartAtCol (int s){
 		startAtCol = s - 1;
 	}
 
 	public void setStartAtRow (int s){
 		startAtRow = s - 1;
 	}
 
 	public void setLanguageListProperty (String p){
 		languageListProperty = p;
 	}
 
 	@Override
 	public void execute() throws BuildException {
 		if (outDir == null){
 			outDir = this.getProject().getBaseDir();
 		}
 
 		validateAttributes();
 
 		CSVReader iReader = null;
 		FileReader tReader = null;
 		BufferedWriter writer = null;
 		List<String> languagelist = new ArrayList<String>();
 
 		try {
 			iReader = new CSVReader(new FileReader(i18nFile));
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
 					output = output.replace("@" + r[startAtCol] + "@", r[l]);
 				}
 
 				String name = templateFile.getName();
 				try {
					if (!outDir.exists()) {
						outDir.mkdirs();
					}
 					writer = new BufferedWriter(new FileWriter(outDir.getAbsolutePath() + File.separator + language + name.substring(name.lastIndexOf('.'))));
 					writer.write(output);
 				} finally {
 					writer.close();
 				}
 			}
 
 			if (languageListProperty != null){
 				getProject().setProperty(languageListProperty, languagelist.toString().replace("[", "").replace("]", ""));
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
 				if (tReader != null){
 					tReader.close();
 				}
 			} catch (IOException e){
 
 			}
 		}
 	}
 
 	protected void validateAttributes() throws BuildException {
 		if (i18nFile == null){
 			throw new BuildException ("Specify the i18n file");
 		}
 
 		if (!i18nFile.isFile()){
 			throw new BuildException ("Parameter 'i18nfile' must be a file");
 		}
 
 		if (templateFile == null){
 			throw new BuildException ("Specify the template file");
 		}
 
 		if (!templateFile.isFile()){
 			throw new BuildException ("Parameter 'templateFile' must be a file");
 		}
 
 		if (startAtCol < 0){
 			throw new BuildException ("Parameter 'startAtCol' must be a positive integer");
 		}
 
 		if (startAtRow < 0){
 			throw new BuildException ("Parameter 'startAtRow' must be a positive integer");
 		}
 	}
 }
