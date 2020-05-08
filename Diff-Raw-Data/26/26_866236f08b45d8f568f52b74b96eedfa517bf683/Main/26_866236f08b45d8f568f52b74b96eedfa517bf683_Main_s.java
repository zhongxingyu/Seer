 /*******************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.test.performance.ui;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.core.runtime.IPlatformRunnable;
 import org.eclipse.core.runtime.Platform;
 
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.Variations;
 import org.eclipse.test.performance.ui.Utils.ConfigDescriptor;
 
 public class Main implements IPlatformRunnable{
 
 	private String baseline;
 	private String baselinePrefix=null;
 	private String output;
 	private String[] configNames;
 	private Hashtable configDescriptors;
 	private String currentBuildId;
 	private ArrayList currentBuildStreamIdPrefixes;
 	private ArrayList pointsOfInterest;
 	private Variations variations;
 	private Scenario[] scenarios;
 	private String jvm;
 	private String scenarioFilter;
 	private boolean genFingerPrints = false;
 	private boolean genScenarioSummaries =false;
 	private boolean genAll = true;
 	private Hashtable fingerPrints = new Hashtable();
 	private Hashtable scenarioComments=new Hashtable();
 	private Hashtable rawDataTables=new Hashtable();
 	
 	public Object run(Object args) throws Exception {
 		parse(args);
 		
 		Enumeration configIds=configDescriptors.keys();
 		
 		while (configIds.hasMoreElements()){
 			generate((ConfigDescriptor)configDescriptors.get(configIds.nextElement()));
 		}
 
 		Utils.printVariabilityTable(rawDataTables,output+"/cvsummary.html",configDescriptors);
 		
 		Enumeration components = fingerPrints.keys();
 		Bundle bundle= UiPlugin.getDefault().getBundle();
 		URL images=bundle.getEntry("images");
 		URL scripts=bundle.getEntry("scripts");
 
 		if (images!=null) {
 			images= Platform.resolve(images);
 			Utils.copyFile(new File(images.getPath(), "FAIL.gif"), output + "/FAIL.gif");
 			Utils.copyFile(new File(images.getPath(), "FAIL_greyed.gif"), output+"/FAIL_greyed.gif");
 			Utils.copyFile(new File(images.getPath(), "OK.gif"),output + "/OK.gif");
 			Utils.copyFile(new File(images.getPath(), "OK_greyed.gif"),output + "/OK_greyed.gif");
 		}
 		if (scripts!=null){
 			scripts= Platform.resolve(scripts);
 			Utils.copyFile(new File(scripts.getPath(), "ToolTip.css"),output+"/ToolTip.css");
 			Utils.copyFile(new File(scripts.getPath(), "ToolTip.js"),output+"/ToolTip.js");
 		}
 		
 		// print fingerprint/scenario status pages
 		while (components.hasMoreElements()) {
 			String component = components.nextElement().toString();
 			try {		
 				File outputFile = new File(output, component + ".php");
 				outputFile.getParentFile().mkdirs();
 				PrintStream os = new PrintStream(new FileOutputStream(outputFile));
 				os.println(Utils.HTML_OPEN);
 				os.println("<link href=\"ToolTip.css\" rel=\"stylesheet\" type=\"text/css\">"+
 				"<script src=\"ToolTip.js\"></script>");
 				os.println(Utils.HTML_DEFAULT_CSS);
 				os.println("<body>");
 				Hashtable fps = (Hashtable) fingerPrints.get(component);
 				Enumeration configs = fps.keys();
 				
 				int baselineUnderScoreIndex=baseline.indexOf("_");
 				int currentUnderScoreIndex=currentBuildId.indexOf("_");
 
 				String baselineName=(baselineUnderScoreIndex!=-1)?baseline.substring(0, baseline.indexOf("_")):baseline;
 				String currentName=(currentUnderScoreIndex!=-1)?currentBuildId.substring(0, currentBuildId.indexOf("_")):currentBuildId;
 				String title = "<h3>Performance of " + component + ": "
 						+ currentName + " relative to "
 						+ baselineName
 						+ "</h3>";
 				if (component.equals("global"))
 					title = "<h3>Performance of " + currentName
 							+ " relative to "
 							+ baselineName
 							+ "</h3>";
 				os.println(title);
 				
 				//print the html representation of fingerprint for each config 
 				while (configs.hasMoreElements()) {
 					String config = configs.nextElement().toString();
 					FingerPrint fp = (FingerPrint) fps.get(config);
 					os.println(Utils.getImageMap(fp));
 				}
 				if (component != "") {
 				//print the component scenario status table beneath the fingerprint
 					variations.put("config", "%");
 					ScenarioStatusTable sst = new ScenarioStatusTable(
 							variations, component + "%", configDescriptors,scenarioComments, baseline);
 					os.println(sst.toString());
 				}
 
 				os.println(Utils.HTML_CLOSE);
 				os.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return null;
 	}
 	
 
 	private void generate(Utils.ConfigDescriptor cd) {
 			//String config=cd.getName();
 			String dbloc_property= System.getProperty("eclipse.perf.dbloc");
 			if (dbloc_property == null || dbloc_property.equals(""))
 				System.out.println("WARNING:  eclipse.perf.dbloc value set to null");
 			scenarios = Utils.getScenarios("%", scenarioFilter, cd.name, jvm);
 			variations = Utils.getVariations("%", cd.name, jvm);
 
 			//creates and stores fingerprint objects
 			if (genFingerPrints || genAll) {
 				System.out.print(cd.name + ": generating fingerprints and scenario status tables...");
 				
 				//global
 				FingerPrint global = new FingerPrint(null, cd, baseline,
 						currentBuildId, variations, output);
 				
 				//store mappings of fingerprints per config for each component
 				Hashtable t;
 				if (fingerPrints.get("global") != null)
 					t = (Hashtable) fingerPrints.get("global");
 				else
 					t = new Hashtable();
 
 				t.put(cd.name, global);
 				fingerPrints.put("global", t);
 
 				//get unique component names from scenario names
 				ArrayList components = Utils.getComponentNames(scenarios);
 		
 				//store fingerprints for config for each component
 				for (int i = 0; i < components.size(); i++) {
 					String component = components.get(i).toString();
 					variations.put("config", cd.name);
 					FingerPrint componentFp = new FingerPrint(component,
 							cd, baseline, currentBuildId, variations,
 							output);
 					if (fingerPrints.get(component) != null)
 						t = (Hashtable) fingerPrints.get(component);
 					else
 						t = new Hashtable();
 					t.put(cd.name, componentFp);
 					fingerPrints.put(component, t);
 					if (componentFp.scenarioComments!=null)
 						scenarioComments.putAll(componentFp.scenarioComments);
 				}
 				System.out.println("done.");
 			}
 			
 			//generates scenario result pages and line graphs
 			if (genScenarioSummaries || genAll) {
 				System.out.print(cd.name
 						+ ": generating scenario results...");
 				new ScenarioResults(cd,scenarios, baseline,baselinePrefix,currentBuildId,pointsOfInterest,scenarioComments,currentBuildStreamIdPrefixes,rawDataTables,output);
 				System.out.println("done.");
 			}
 		}
 	
 
 	private void parse(Object argsObject) {
 		String []args=(String[])argsObject;
 		int i = 0;
 		if (args.length == 0) {
 			printUsage();
 		}
 
 		while (i < args.length) {
 			String arg = args[i];
 			if (!arg.startsWith("-")){
 				i++;
 				continue;
 			}
 			if (args.length==i+1&&i!=args.length-1){
 				System.out.println("Missing value for last parameter");
 				printUsage();
 			}
 			if (arg.equals("-baseline")) {
 				baseline = args[i + 1];
 				if (baseline.startsWith("-")) {
 					System.out.println("Missing value for -baseline parameter");
 					printUsage();
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-baseline.prefix")) {
 				baselinePrefix = args[i + 1];
 				if (baselinePrefix.startsWith("-")) {
 					System.out.println("Missing value for -baseline.prefix parameter");
 					printUsage();
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-current.prefix")) {
 				String idPrefixList=args[i + 1];
 				if (idPrefixList.startsWith("-")) {
 					System.out.println("Missing value for -current.prefix parameter");
 					printUsage();
 				}
 				String []ids=idPrefixList.split(",");
 				currentBuildStreamIdPrefixes=new ArrayList();
 				for (int j=0;j<ids.length;j++){
 					currentBuildStreamIdPrefixes.add(ids[j]);
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-highlight")||arg.equals("-highlight.latest")) {
 				if (args[i + 1].startsWith("-")) {
 					System.out.println("Missing value for -highlight parameter");
 					printUsage();
 				}
 				String []ids=args[i + 1].split(",");
 				pointsOfInterest=new ArrayList();
 				for (int j=0;j<ids.length;j++){
 					pointsOfInterest.add(ids[j]);
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-current")) {
 				currentBuildId = args[i + 1];
 				if (currentBuildId.startsWith("-")) {
 					System.out.println("Missing value for -current parameter");
 					printUsage();
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-jvm")) {
 				jvm = args[i + 1];
 				if (jvm.startsWith("-")) {
 					System.out.println("Missing value for -jvm parameter");
 					printUsage();
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-output")) {
 				output = args[i + 1];
 				if (output.startsWith("-")) {
 					System.out.println("Missing value for -output parameter");
 					printUsage();
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-config")) {
 				String configs = args[i + 1];
 				if (configs.startsWith("-")) {
 					System.out.println("Missing value for -config parameter");
 					printUsage();
 				}
 				configNames = configs.split(",");
 				Arrays.sort(configNames);
 				i++;
 				continue;
 			}
 			if (arg.equals("-config.properties")) {
 				String configProperties = args[i + 1];
 				if (configProperties.startsWith("-")) {
 					System.out.println("Missing value for -config.properties parameter");
 					printUsage();
 				}
 				configDescriptors = Utils
 						.getConfigDescriptors(configProperties);
 				i++;
 				continue;
 			}
 			if (arg.equals("-scenario.filter")||arg.equals("-scenario.pattern")) {
 				scenarioFilter = args[i + 1];
 				if (scenarioFilter.startsWith("-")) {
 					System.out.println("Missing value for -baseline parameter");
 					printUsage();
 				}
 				i++;
 				continue;
 			}
 			if (arg.equals("-fingerprints")) {
 				genFingerPrints = true;
 				genAll = false;
 				i++;
 				continue;
 			}
 			if (arg.equals("-scenarioresults")) {
 				genScenarioSummaries = true;
 				genAll = false;
 				i++;
 				continue;
 			}
 
 			i++;
 		}
 		if (baseline == null || output == null || configNames == null
 				|| jvm == null
 				|| currentBuildId == null)
 			printUsage();
 		
 		if (currentBuildStreamIdPrefixes==null){
 			currentBuildStreamIdPrefixes=new ArrayList();
 			currentBuildStreamIdPrefixes.add("N");
 			currentBuildStreamIdPrefixes.add("I");
 		}
 		if (configDescriptors==null){
 			configDescriptors=new Hashtable();
 			for (int j=0;j<configNames.length;j++){
 				String configName=configNames[j];
 				configDescriptors.put(configName,new Utils.ConfigDescriptor(configName,configName));
 		
 			}
 		}
 	}
 	
 	private void printUsage() {
 		System.out
 				.println("Usage:\n"
 						
 						+ "-baseline"
 								+"\n\tBuild id against which to compare results."
 								+"\n\tSame as value specified for the \"build\" key in the eclipse.perf.config system property.\n\n"
 
 						+ "[-baseline.prefix]"
 								+"\n\tBuild id prefix used in baseline test builds and reruns.  Used to plot baseline historical data."
 								+"\n\tA common prefix used for the value of the \"build\" key in the eclipse.perf.config system property when rerunning baseline tests.\n\n"
 								
 						+ "-current" 
 								+"\n\tbuild id for which to generate results.  Compared to build id specified in -baseline parameter above."
 								+"\n\tSame as value specified for the \"build\" key in the eclipse.perf.config system property. \n\n"
 
 						+ "[-current.prefix]" 
 							+"\n\tComma separated list of build id prefixes used in current build stream." 
 							+"\n\tUsed to plot current build stream historical data.  Defaults to \"N,I\"."
 							+"\n\tPrefixes for values specified for the \"build\" key in the eclipse.perf.config system property. \n\n"
 			
 						+ "-jvm"
 								+"\n\tValue specified in \"jvm\" key in eclipse.perf.config system property for current build.\n\n"
 						
 						+ "-config" 
 								+"\n\tComma separated list of config names for which to generate results."
 								+"\n\tSame as values specified in \"config\" key in eclipse.perf.config system property.\n\n"
 
 						+ "-output"
 								+" \n\tPath to default output directory.\n\n"
 
 						+ "[-config.properties]"
 								+"\n\tOptional.  Used by scenario status table to provide the following:"
 								+"\n\t\talternate descriptions of config values to use in columns."
 								+"\n\tThe value should be specified in the following format:"
 								+"\n\tname1,description1;name2,description2;etc..\n\n"
 
 						+ "[-highlight]"
 								+"\n\tOptional.  Comma-separated list of build Id prefixes used to find most recent matching for each entry." +
 										"\n\tResult used to highlight points in line graphs.\n\n"
 
 						+ "[-scenario.pattern]"
 								+"\n\tOptional.  Scenario prefix pattern to query database.  If not specified,"
 								+"\n\tdefault of % used in query.\n\n"
 							
 						+ "[-fingerprints]" 
 								+"\n\tOptional.  Use to generate fingerprints only.\n\n"
 									
 						+ "[-scenarioresults]"
 								+"\n\tGenerates table of scenario reference and current data with line graphs.\n\n");
 										
 		System.exit(1);
 
 	}
 
 
 
 }
