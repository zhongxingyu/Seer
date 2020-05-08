 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.tikal;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Locale;
 
 import net.sf.okapi.common.BaseContext;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.ui.filters.FilterConfigurationsDialog;
 import net.sf.okapi.lib.translation.IQuery;
 import net.sf.okapi.lib.translation.QueryResult;
 import net.sf.okapi.mt.google.GoogleMTConnector;
 import net.sf.okapi.tm.opentran.OpenTranTMConnector;
 import net.sf.okapi.tm.translatetoolkit.Parameters;
 import net.sf.okapi.tm.translatetoolkit.TranslateToolkitTMConnector;
 
 public class Main {
 	
 	protected final static int CMD_EXTRACT = 0;
 	protected final static int CMD_MERGE = 1;
 	protected final static int CMD_EDITCONFIG = 2;
 	protected final static int CMD_QUERYTRANS = 3;
 	
 	protected ArrayList<String> inputs;
 	protected String skeleton;
 	protected String output;
 	protected String specifiedConfigId;
 	protected String configId;
 	protected String inputEncoding;
 	protected String outputEncoding;
 	protected String srcLang = Locale.getDefault().getLanguage();
 	protected String trgLang = "fr";
 	protected int command = -1;
 	protected String query;
 	protected boolean useGoogle;
 	protected boolean useOpenTran;
 	protected boolean useTT;
 	protected String ttParams;
 	
 	private FilterConfigurationMapper fcMapper;
 	private Hashtable<String, String> extensionsMap;
 	private Hashtable<String, String> filtersMap;
 	
 	public static void main (String[] args) {
 		try {
 			Main prog = new Main();
 			prog.printBanner();
 			if ( args.length == 0 ) {
 				prog.printUsage();
 				return;
 			}
 			
 			for ( int i=0; i<args.length; i++ ) {
 				String arg = args[i];
 				arg = arg.replace('/', '-'); // To allow /x syntax
 				if ( arg.equals("-?") ) {
 					prog.printUsage();
 					return;
 				}
 				else if ( arg.equals("-h") ) {
 					prog.showHelp();
 					return;
 				}
 				else if ( arg.equals("-fc") ) {
 					prog.specifiedConfigId = getArgument(args, ++i);
 				}
 				else if ( arg.equals("-sl") ) {
 					prog.srcLang = getArgument(args, ++i);
 				}
 				else if ( arg.equals("-tl") ) {
 					prog.trgLang = getArgument(args, ++i);
 				}
 				else if ( arg.equals("-ie") ) {
 					prog.inputEncoding = getArgument(args, ++i);
 				}
 				else if ( arg.equals("-oe") ) {
 					prog.outputEncoding = getArgument(args, ++i);
 				}
 				else if ( arg.equals("-x") ) {
 					prog.command = CMD_EXTRACT;
 				}
 				else if ( arg.equals("-m") ) {
 					prog.command = CMD_MERGE;
 				}
 				else if ( arg.equals("-e") ) {
 					prog.command = CMD_EDITCONFIG;
 					if ( args.length > i+1 ) {
 						if ( !args[i+1].startsWith("-") ) {
							++i; // Avoid empty argument
							if ( args[i].length() > 0 ) {
								prog.specifiedConfigId = args[i];
							}
 						}
 					}
 				}
 				else if ( arg.equals("-q") ) {
 					prog.command = CMD_QUERYTRANS;
 					prog.query = getArgument(args, ++i);
 				}
 				else if ( arg.equals("-google") ) {
 					prog.useGoogle = true;
 				}
 				else if ( arg.equals("-opentran") ) {
 					prog.useOpenTran = true;
 				}
 				else if ( arg.equals("-tt") ) {
 					prog.useTT = true;
 					prog.ttParams = getArgument(args, ++i);
 				}
 				else if ( arg.equals("-listconf") ) {
 					prog.showAllConfigurations();
 					return;
 				}
 				else if ( !arg.startsWith("-") ) {
 					prog.inputs.add(args[i]);
 				}
 				else {
 					throw new InvalidParameterException(
 						String.format("Invalid command-line argument '%s'.", args[i]));
 				}
 			}
 			
 			// Check inputs and command
 			if ( prog.command == -1 ) {
 				System.out.println("No command specified. Please use one of the command described below:");
 				prog.printUsage();
 				return;
 			}
 			if ( prog.command == CMD_EDITCONFIG ) {
 				if ( prog.specifiedConfigId == null ) {
 					prog.editAllConfigurations();
 				}
 				else {
 					prog.editConfiguration();
 				}
 				return;
 			}
 			if ( prog.command == CMD_QUERYTRANS ) {
 				prog.processQuery();
 				return;
 			}
 			if ( prog.inputs.size() == 0 ) {
 				throw new RuntimeException("No input document specified.");
 			}
 			
 			// Process all input files
 			for ( int i=0; i<prog.inputs.size(); i++ ) {
 				if ( i > 0 ) {
 					System.out.println("------------------------------------------------------------"); //$NON-NLS-1$
 				}
 				prog.process(prog.inputs.get(i));
 			}
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 		}
 	}
 
 	private static String getArgument (String[] args, int index) {
 		if ( index >= args.length ) {
 			throw new RuntimeException(String.format(
 				"Missing parameter after '%s'", args[index-1]));
 		}
 		return args[index];
 	}
 	
 	public Main () {
 		inputs = new ArrayList<String>();
 	}
 	
 	private void initialize () {
 		fcMapper = new FilterConfigurationMapper();
 		extensionsMap = new Hashtable<String, String>();
 		filtersMap = new Hashtable<String, String>();
 		
 		extensionsMap.put(".docx", "okf_openxml");
 		extensionsMap.put(".pptx", "okf_openxml");
 		extensionsMap.put(".xlsx", "okf_openxml");
 		filtersMap.put("okf_openxml", "net.sf.okapi.filters.openxml.OpenXMLFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.openxml.ui.Editor",
 			"net.sf.okapi.filters.openxml.ConditionalParameters");
 
 		extensionsMap.put(".odt", "okf_openoffice");
 		extensionsMap.put(".odp", "okf_openoffice");
 		extensionsMap.put(".ods", "okf_openoffice");
 		filtersMap.put("okf_openoffice", "net.sf.okapi.filters.openoffice.OpenOfficeFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.openoffice.ui.Editor",
 			"net.sf.okapi.filters.openoffice.Parameters");
 
 		extensionsMap.put(".htm", "okf_html");
 		extensionsMap.put(".html", "okf_html");
 		filtersMap.put("okf_html", "net.sf.okapi.filters.html.HtmlFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.html.ui.Editor",
 			"net.sf.okapi.filters.html.HtmlFilter.Parameters");
 		
 		extensionsMap.put(".xlf", "okf_xliff");
 		extensionsMap.put(".xlif", "okf_xliff");
 		extensionsMap.put(".xliff", "okf_xliff");
 		filtersMap.put("okf_xliff", "net.sf.okapi.filters.xliff.XLIFFFilter");
 		fcMapper.addDescriptionProvider("net.sf.okapi.filters.xliff.ParametersUI",
 			"net.sf.okapi.filters.xliff.Parameters");
 		
 		extensionsMap.put(".tmx", "okf_tmx");
 		filtersMap.put("okf_tmx", "net.sf.okapi.filters.tmx.TmxFilter");
 		
 		extensionsMap.put(".properties", "okf_properties");
 		filtersMap.put("okf_properties", "net.sf.okapi.filters.properties.PropertiesFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.properties.ui.Editor",
 			"net.sf.okapi.filters.properties.Parameters");
 		
 		extensionsMap.put(".po", "okf_po");
 		filtersMap.put("okf_po", "net.sf.okapi.filters.po.POFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.po.ui.Editor",
 			"net.sf.okapi.filters.po.Parameters");
 		
 		extensionsMap.put(".xml", "okf_xml");
 		extensionsMap.put(".resx", "okf_xml-resx");
 		filtersMap.put("okf_xml", "net.sf.okapi.filters.xml.XMLFilter");
 		
 		extensionsMap.put(".srt", "okf_regex-srt");
 		filtersMap.put("okf_regex", "net.sf.okapi.filters.regex.RegexFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.regex.ui.Editor",
 			"net.sf.okapi.filters.regex.Parameters");
 		
 		extensionsMap.put(".dtd", "okf_dtd");
 		extensionsMap.put(".ent", "okf_dtd");
 		filtersMap.put("okf_dtd", "net.sf.okapi.filters.dtd.DTDFilter");
 		
 		extensionsMap.put(".ts", "okf_ts");
 		filtersMap.put("okf_ts", "net.sf.okapi.filters.ts.TsFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.ts.ui.Editor",
 			"net.sf.okapi.filters.ts.Parameters");
 		
 		extensionsMap.put(".txt", "okf_plaintext");
 		filtersMap.put("okf_plaintext", "net.sf.okapi.filters.plaintext.PlainTextFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.plaintext.ui.Editor",
 			"net.sf.okapi.filters.plaintext.Parameters");
 
 		extensionsMap.put(".csv", "okf_table_csv");
 		filtersMap.put("okf_table", "net.sf.okapi.filters.table.TableFilter");
 		fcMapper.addEditor("net.sf.okapi.filters.table.ui.Editor",
 			"net.sf.okapi.filters.table.Parameters");
 	}
 	
 	private String getConfigurationId (String ext) {
 		// Get the configuration for the extension
 		String id = extensionsMap.get(ext);
 		if ( id == null ) {
 			throw new RuntimeException(String.format(
 				"Could not guess the configuration for the extension '%s'", ext));
 		}
 		return id;
 	}
 	
 	private void editAllConfigurations () {
 		initialize();
 		// Add all the pre-defined configurations
 		for ( String className : filtersMap.values() ) {
 			fcMapper.addConfigurations(className);
 		}
 		// Add the custom configurations
 		fcMapper.updateCustomConfigurations();
 		
 		// Edit
 		FilterConfigurationsDialog dlg = new FilterConfigurationsDialog(null, false, fcMapper, null);
 		dlg.showDialog(specifiedConfigId);
 	}
 	
 	private void editConfiguration () {
 		initialize();
 		
 		if ( specifiedConfigId == null ) {
 			throw new RuntimeException("You must specified the configuration to edit.");
 		}
 		configId = specifiedConfigId;
 		if ( !prepareFilter(configId) ) return; // Next input
 
 		FilterConfiguration config = fcMapper.getConfiguration(configId);
 		if ( config == null ) {
 			throw new RuntimeException(String.format(
 				"Cannot find the configuration for '%s'.", configId));
 		}
 		IParameters params = fcMapper.getParameters(config);
 		if ( params == null ) {
 			throw new RuntimeException(String.format(
 				"Cannot load parameters for '%s'.", config.configId));
 		}
 		
 		IParametersEditor editor = fcMapper.createConfigurationEditor(configId);
 		if ( editor != null ) {
 			editor.edit(params, !config.custom, new BaseContext());
 		}
 		else {
 			throw new RuntimeException(String.format(
 				"Cannot create the parameters editor for '%s'.", configId));
 		}
 	}
 	
 	private void showAllConfigurations () {
 		initialize();
 		for ( String className : filtersMap.values() ) {
 			fcMapper.addConfigurations(className);
 		}
 
 		System.out.println("List of all filter configurations available:");
 		Iterator<FilterConfiguration> iter = fcMapper.getAllConfigurations();
 		FilterConfiguration config;
 		while ( iter.hasNext() ) {
 			config = iter.next();
 			System.out.println(String.format(" - %s = %s",
 				config.configId, config.description));
 		}
 	}
 	
 	private boolean prepareFilter (String configId) {
 		// Is it a default configuration?
 		if ( filtersMap.containsKey(configId) ) {
 			// Configuration ID is a default one:
 			// Add its filter to the configuration mapper
 			fcMapper.addConfigurations(filtersMap.get(configId));
 			return true;
 		}
 		
 		// Else: Try to find the filter for that configuration
 		for ( String tmp : filtersMap.keySet() ) {
 			if ( configId.startsWith(tmp) ) {
 				fcMapper.addConfigurations(filtersMap.get(tmp));
 				fcMapper.addCustomConfiguration(configId);
 				return true;
 			}
 		}
 		
 		// Could not guess
 		System.out.println(String.format(
 			"ERROR: Could not guess the filter for the configuration '%s'", configId));
 		return false;
 	}
 
 	private void guessMissingParameters (String inputOfConfig) {
 		if ( specifiedConfigId == null ) {
 			String ext = Util.getExtension(inputOfConfig);
 			if ( Util.isEmpty(ext) ) {
 				throw new RuntimeException(String.format(
 					"The input file '%s' has no extension to guess the filter from.", inputOfConfig));
 			}
 			configId = getConfigurationId(ext.toLowerCase());
 		}
 		else {
 			configId = specifiedConfigId;
 		}
 		
 		if ( outputEncoding == null ) {
 			if ( inputEncoding != null ) outputEncoding = inputEncoding;
 			else outputEncoding = Charset.defaultCharset().name();
 		}
 		if ( inputEncoding == null ) {
 			inputEncoding = Charset.defaultCharset().name();
 		}
 	}
 	
 	private void guessMergingArguments (String input) {
 		
 		String ext = Util.getExtension(input);
 		if ( !ext.equals(".xlf") ) {
 			throw new RuntimeException(String.format(
 				"The input file '%s' does not have the expected .xlf extension.", input));
 		}
 		
 		int n = input.lastIndexOf('.');
 		skeleton = input.substring(0, n);
 		
 		ext = Util.getExtension(skeleton);
 		n = skeleton.lastIndexOf('.');
 		output = skeleton.substring(0, n) + ".out" + ext;
 	}
 	
 	protected void process (String input) throws URISyntaxException {
 		initialize();
 
 		if ( command == CMD_EXTRACT ) {
 			guessMissingParameters(input);
 			if ( !prepareFilter(configId) ) return; // Next input
 			XLIFFExtractionStep step = new XLIFFExtractionStep(fcMapper);
 			
 			File file = new File(input);
 			RawDocument rd = new RawDocument(file.toURI(), inputEncoding, srcLang, trgLang);
 			rd.setFilterConfigId(configId);
 			
 			System.out.println("Source language: "+srcLang);
 			System.out.print("Target language: ");
 			System.out.println(trgLang);
 			System.out.println(" Input encoding: "+inputEncoding);
 			System.out.println("  Configuration: "+configId);
 			System.out.println(" Input document: "+input);
 			System.out.print("Output document: ");
 			if ( output == null ) System.out.println("<auto-defined>");
 			else System.out.println(output);
 			System.out.print("Extaction...");
 			
 			step.handleRawDocument(rd);
 			System.out.println(" Done");
 		}
 		else if ( command == CMD_MERGE ) {
 			guessMergingArguments(input);
 			guessMissingParameters(skeleton);
 			if ( !prepareFilter(configId) ) return; // Next input
 			XLIFFMergingStep step = new XLIFFMergingStep(fcMapper);
 
 			File file = new File(skeleton);
 			RawDocument skelRawDoc = new RawDocument(file.toURI(), inputEncoding, srcLang, trgLang);
 			skelRawDoc.setFilterConfigId(configId);
 			step.setXliffPath(input);
 			step.setOutputPath(output);
 			step.setOutputEncoding(outputEncoding);
 			
 			System.out.println("Source language: "+srcLang);
 			System.out.print("Target language: ");
 			System.out.println(trgLang);
 			System.out.println(" Input encoding: "+inputEncoding);
 			System.out.println("Output encoding: "+outputEncoding);
 			System.out.println("  Configuration: "+configId);
 			System.out.println(" XLIFF document: "+input);
 			System.out.print("Output document: ");
 			if ( output == null ) System.out.println("<auto-defined>");
 			else System.out.println(output);
 			System.out.print("Merging...");
 
 			step.handleRawDocument(skelRawDoc);
 			System.out.println(" Done");
 		}
 	}
 	
 	private void printBanner () {
 		System.out.println("-------------------------------------------------------------------------------"); //$NON-NLS-1$
 		System.out.println("Okapi Tikal - Localization Toolset");
 		// This will null for version until compiled as jar, which is ok.
 		System.out.println(String.format("Version: %s", getClass().getPackage().getImplementationVersion()));
 		System.out.println("-------------------------------------------------------------------------------"); //$NON-NLS-1$
 	}
 	
 	private void showHelp () {
 		// Get the path/URL of the help file 
 		URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
 		String path = Util.getDirectoryName(Util.getDirectoryName(url.getPath()));
 		path += "/help/applications/tikal/index.html"; //$NON-NLS-1$
 		// Opens the file
 		try {
 			Util.openURL((new File(path)).toURL().toString());
 		}
 		catch ( MalformedURLException e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void printUsage () {
 		System.out.println("Show this help:");
 		System.out.println("   -?");
 		System.out.println("Open the user guide page:");
 		System.out.println("   -h");
 		System.out.println("List all available filter configurations:");
 		System.out.println("   -listconf");
 		System.out.println("Edit or view filter configurations (UI-dependent command):");
 		System.out.println("   -e [[-fc] configId]");
 		System.out.println("Extract a file to XLIFF:");
 		System.out.println("   -x inputFile [inputFile2...] [-fc configId] [-ie encoding]");
 		System.out.println("      [-sl srcLang] [-tl trgLang]");
 		System.out.println("Merge an XLIFF document back to its original format:");
 		System.out.println("   -m xliffFile [xliffFile2...] [-fc configId] [-ie encoding]");
 		System.out.println("      [-oe encoding] [-sl srcLang] [-tl trgLang]");
 		System.out.println("Query translation resources:");
 		System.out.println("   -q \"source text\" [-sl srcLang] [-tl trgLang] [-opentran]");
 		System.out.println("      [-google] [-tt hostname[:port]]");
 	}
 
 	private void displayQuery (IQuery conn) {
 		if ( conn.query(query) > 0 ) {
 			QueryResult qr;
 			while ( conn.hasNext() ) {
 				qr = conn.next();
 				System.out.println(String.format("Result: From %s (%s->%s, score: %d)", conn.getName(),
 					conn.getSourceLanguage(), conn.getTargetLanguage(), qr.score));
 				System.out.println(String.format("  Source: \"%s\"", qr.source.toString()));
 				System.out.println(String.format("  Target: \"%s\"", qr.target.toString()));
 			}
 		}
 		else {
 			System.out.println(String.format("Result: From %s (%s->%s)", conn.getName(),
 				conn.getSourceLanguage(), conn.getTargetLanguage()));
 			System.out.println(String.format("  Source: \"%s\"", query));
 			System.out.println("  <Not translation has been found>");
 		}	
 	}
 	
 	private void processQuery () {
 		if ( !useGoogle && !useOpenTran && !useTT ) {
 			useGoogle = true; // Default if none is specified
 		}
 		
 		IQuery conn;
 		if ( useGoogle ) {
 			conn = new GoogleMTConnector();
 			conn.setLanguages(srcLang, trgLang);
 			conn.open();
 			displayQuery(conn);
 			conn.close();
 		}
 		if ( useTT ) {
 			// Parse the parameters hostname:port
 			int n = ttParams.lastIndexOf(':');
 			net.sf.okapi.tm.translatetoolkit.Parameters params = new Parameters();
 			if ( n == -1 ) {
 				params.setHost(ttParams);
 			}
 			else {
 				params.setPort(Integer.valueOf(ttParams.substring(n+1)));
 				params.setHost(ttParams.substring(0, n));
 			}
 			conn = new TranslateToolkitTMConnector();
 			conn.setParameters(params);
 			conn.setLanguages(srcLang, trgLang);
 			conn.open();
 			displayQuery(conn);
 			conn.close();
 		}
 		if ( useOpenTran ) {
 			conn = new OpenTranTMConnector();
 			conn.setLanguages(srcLang, trgLang);
 			conn.open();
 			displayQuery(conn);
 			conn.close();
 		}
 	}
 
 }
