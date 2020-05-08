 package net.sf.okapi.applications.rainbow.utilities.xsltransform;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import net.sf.okapi.applications.rainbow.utilities.ISimpleUtility;
 import net.sf.okapi.common.ConfigurationString;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 
 public class Utility implements ISimpleUtility {
 
 	private String                     srcLang;
 	private String                     trgLang;
 	private Source                     xsltInput;
 	private ArrayList<String>          inputPaths;
 	private ArrayList<String>          outputEncodings;
 	private ArrayList<String>          outputPaths;
 	private Hashtable<String, String>  paramList;
 	private Transformer                trans;
 	private IParameters                params;
 	private String                     commonFolder;
 
 	
 	public Utility () {
 		params = new Parameters();
 	}
 	
 	public void resetLists () {
 		inputPaths = new ArrayList<String>();
 		outputEncodings = new ArrayList<String>();
 		outputPaths = new ArrayList<String>();
 	}
 	
 	public String getID () {
 		return "oku_xsltransform";
 	}
 	
 	public void processInput () {
 		try {
 			trans.reset();
 			fillParameters();
 			// Create the source for the XML input
 			Source xmlInput = new javax.xml.transform.stream.StreamSource(
 				new File(inputPaths.get(0)));
 			// Create the output
 			Result result = new javax.xml.transform.stream.StreamResult(
 				new File(outputPaths.get(0)));
 			trans.transform(xmlInput, result);
 		}
 		catch ( TransformerException e ) {
 			throw new RuntimeException("Error when transforming.", e);
 		}
 	}
 
 	public void doEpilog () {
 	}
 
 	public void doProlog (String sourceLanguage,
 		String targetLanguage)
 	{
 		try {
 			commonFolder = null; // Reset
 			// Store the languages
 			srcLang = sourceLanguage;
 			trgLang = targetLanguage;
 			
 			ConfigurationString cfgString = new ConfigurationString(
 				params.getParameter("paramList"));
 			paramList = cfgString.toHashtable();
 			
 			// Create the source for the XSLT
 			xsltInput = new javax.xml.transform.stream.StreamSource(
 				new File(params.getParameter("xsltPath")));
 			
 			// Create an instance of TransformerFactory
 			javax.xml.transform.TransformerFactory fact =
 				javax.xml.transform.TransformerFactory.newInstance();
 			trans = fact.newTransformer(xsltInput);
 		}
 		catch ( TransformerConfigurationException e ) {
 			throw new RuntimeException("Error in the XSLT input.", e);
 		}
 	}
 
 	public String getInputRoot () {
 		return null;
 	}
 
 	public String getOutputRoot () {
 		return null;
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasParameters () {
 		return true;
 	}
 
 	public boolean isFilterDriven () {
 		return false;
 	}
 
 	public boolean needsRoots () {
 		return false;
 	}
 
 	public void addInputData (String path,
 		String encoding,
 		String filterSettings)
 	{
 		// Add the info to the list
 		inputPaths.add(path);
 	}
 
 	public void addOutputData (String path,
 		String encoding)
 	{
 		// Compute the longest common folder
		commonFolder = Util.longestCommonDir(commonFolder,
			Util.getDirectoryName(path), !Util.isOSCaseSensitive());
 		// Add the info to the list
 		outputPaths.add(path);
 		outputEncodings.add(encoding);
 	}
 
 	public void setParameters (IParameters paramsObject) {
 		params = paramsObject;
 	}
 
 	public void setRoots (String inputRoot,
 		String outputRoot)
 	{
 		// Not used in this utility.
 	}
 
 	private void fillParameters () {
 		trans.clearParameters();
 		String value;
 		for ( String key : paramList.keySet() ) {
 			value = paramList.get(key).replace("<$SrcLang>", srcLang);
 			value = value.replace("<$TrgLang>", trgLang);
 			value = value.replace("<$Input1>", inputPaths.get(0));
 			value = value.replace("<$Output1>", outputPaths.get(0));
 			if ( inputPaths.get(1) != null ) {
 				value = value.replace("<$Input2>", inputPaths.get(1));
 				value = value.replace("<$Output2>", outputPaths.get(1));
 			}
 			if ( inputPaths.get(2) != null ) {
 				value = value.replace("<$Input3>", inputPaths.get(2));
 				value = value.replace("<$Output3>", outputPaths.get(2));
 			}
 			trans.setParameter(key, value);
 		}
 	}
 
 	public int getInputCount () {
 		return 3; // between 1 and 3, depending on the template
 	}
 
 	public String getFolderAfterProcess () {
 		return commonFolder;
 	}
 }
