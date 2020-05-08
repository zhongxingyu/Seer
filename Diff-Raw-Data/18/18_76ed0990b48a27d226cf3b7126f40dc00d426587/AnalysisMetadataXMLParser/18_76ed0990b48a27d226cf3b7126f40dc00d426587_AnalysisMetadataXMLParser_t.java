 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2012 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the Software), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.meta;
 
 import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.LegacyParameterXMLParser;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType;
 
 
 /**
 * A parser for new-format XML analysis metadata files.
 * <p>
 * If an old-format XML parameter file is passed in instead, it will be passed
 * off to a {@link edu.stanford.cfuller.imageanalysistools.meta.parameters.LegacyParameterXMLParser LegacyParameterXMLParser}.
 * 
 * @author Colin J. Fuller
 * 
 */
 public class AnalysisMetadataXMLParser extends AnalysisMetadataParser {
 	
 	//XML tags
 	
 	public static final String TAG_ANALYSIS_METADATA = "analysis_metadata";
 	public static final String TAG_INPUT_STATE = "input_state";
 	public static final String TAG_PARAMETERS =  "parameters";
 	public static final String TAG_PARAMETER = "parameter";
 	public static final String TAG_IMAGES = "images";
 	public static final String TAG_IMAGE = "image";
 	public static final String TAG_ANALYSIS = "analysis";
 	public static final String TAG_LIBRARY = "library";
 	public static final String TAG_TIMESTAMP = "timestamp";
 	public static final String TAG_METHOD = "method";
 	public static final String TAG_SCRIPT = "script";
 	public static final String TAG_OUTPUT_STATE = "output_state";
 	public static final String TAG_QUANT = "quantification";
 	public static final String TAG_DATAFILE = "datafile";
 	public static final String TAG_DESCRIPTION = "description";
 	
 	//tag attributes
 	
 	public static final String ATTR_TIME = "time";
 	public static final String ATTR_VERSION = "version";
 	public static final String ATTR_COMMIT = "commit";
 	public static final String ATTR_FILENAME = "filename";
 	public static final String ATTR_HASH_ALG = "hash_algorithm";
 	public static final String ATTR_HASH = "hash";
 	public static final String ATTR_NAME = "name";
 	public static final String ATTR_DISPLAY_NAME = "display_name";
 	public static final String ATTR_VALUE = "value";
 	public static final String ATTR_TYPE = "type";
 	
 	//library version info
 	
 	protected final static Pattern versionGrabber = Pattern.compile(ATTR_VERSION + "=\"(\\S*)\"");
 	protected final static Pattern commitGrabber = Pattern.compile(ATTR_COMMIT + "=\"(\\S*)\"");
 	
 	/**
      * Parses a parameter file to an AnalysisMetadata object.
      * @param filename  The XML-formatted file to parse.
      * @return          An AnalysisMetadata containing the information described in the file.
      */
 	@Override
 	public AnalysisMetadata parseFileToAnalysisMetadata(String filename) {
 		
 		Document metaDoc = this.getDocumentForFilename(filename);
 		
 		AnalysisMetadata meta = new AnalysisMetadata();
 		
 		ParameterDictionary inputParameters = this.getInputParametersFromDocument(metaDoc);
 		
 		meta.setInputParameters(inputParameters);
 		
 		boolean hasOutput = false;
 		
 		if (this.hasOutputSection(metaDoc) && this.hasAnalysisSection(metaDoc)) {
						
 			hasOutput = true;
 			
 			loadInputImageInformation(metaDoc, meta);
 			
 			loadPreviousAnalysisData(metaDoc, meta);
 			
 			ParameterDictionary outputParameters = this.getOutputParametersFromDocument(metaDoc);
 			
 			meta.setOutputParameters(outputParameters);
 			
 		}
 		
 		meta.setHasPreviousOutput(hasOutput);
 
 		return meta;
 		
 	}
 	
 	protected Document getDocumentForFilename(String filename) {
 		Document metaDoc = null;
 
         try {
             metaDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename);
         } catch (SAXException e) {
             LoggingUtilities.getLogger().severe("Encountered exception while parsing tasks xml file.");
             e.printStackTrace();
             return null;
         } catch (ParserConfigurationException e) {
             LoggingUtilities.getLogger().severe("Incorrectly configured xml parser.");
             e.printStackTrace();
             return null;
         } catch (java.io.IOException e) {
             LoggingUtilities.getLogger().severe("Exception while reading xml file.");
             e.printStackTrace();
             return null;
         }
 		return metaDoc;
 	}
 	
 	private ParameterDictionary getParametersFromDocument(Document analysisMetadataDocument, String tag) {
 		
 		NodeList inputStateList = analysisMetadataDocument.getElementsByTagName(tag);
 
 		if (inputStateList.getLength() > 0) {
 			
 			Node inputState = inputStateList.item(0); // only one input state allowed
 			
 			NodeList children = inputState.getChildNodes();
 			
 			Node parametersNode = null;
 			
 			for (int i = 0; i < children.getLength(); i++) {
 				Node child = children.item(i);
 				if (child.getNodeName().equals(TAG_PARAMETERS)) {
 					parametersNode = child;
 					break;
 				}
 			}
 			
 			if (parametersNode != null) {
 				return parseParametersNodeToParameterDictionary(parametersNode);
 			}
 			
 		}
 		
 		return (new LegacyParameterXMLParser()).parseDocumentToParameterDictionary(analysisMetadataDocument);
 	}
 	
 	private ParameterDictionary getInputParametersFromDocument(Document analysisMetadataDocument) {
 		
 		return this.getParametersFromDocument(analysisMetadataDocument, TAG_INPUT_STATE);
 		
 	}
 	
 	private ParameterDictionary getOutputParametersFromDocument(Document analysisMetadataDocument) {
 		
 		return this.getParametersFromDocument(analysisMetadataDocument, TAG_OUTPUT_STATE);
 		
 	}
 	
 	private ParameterDictionary parseParametersNodeToParameterDictionary(Node parameters) {
 		
 		NodeList tasks = parameters.getChildNodes();
 		
 		ParameterDictionary output = ParameterDictionary.emptyDictionary();
 	
 		NodeList parameterNodes = parameters.getChildNodes();
 		
 		 for (int i = 0; i < parameterNodes.getLength(); i++) {
 
             Node n = parameterNodes.item(i);
 
 			if (! n.getNodeName().equals(TAG_PARAMETER)) continue;
 
             Parameter p = this.parameterWithXMLNode(n);
 
             output.addParameter(p);
 
         }
         
         return output;
 
 	}
 	
 	protected boolean hasOutputSection(Document metaDoc) {
 		NodeList outputStateList = metaDoc.getElementsByTagName(TAG_OUTPUT_STATE);
 		return (outputStateList.getLength() > 0);
 	}
 	
 	protected boolean hasAnalysisSection(Document metaDoc) {
 		NodeList analysisList = metaDoc.getElementsByTagName(TAG_ANALYSIS);
 		return (analysisList.getLength() > 0);
 	}
 	
 	private void loadInputImageInformation(Document metaDoc, AnalysisMetadata meta) {
 		
 		NodeList inputStateList = metaDoc.getElementsByTagName(TAG_INPUT_STATE);
 
 		if (inputStateList.getLength() > 0) {
 			
 			Node inputState = inputStateList.item(0); // only one input state allowed
 			
 			NodeList children = inputState.getChildNodes();
 			
 			Node imagesNode = null;
 			
 			for (int i = 0; i < children.getLength(); i++) {
 				Node child = children.item(i);
 				if (child.getNodeName().equals(TAG_IMAGES)) {
 					imagesNode = child;
 					break;
 				}
 			}
 			
 			if (imagesNode != null) {
 				
 				NodeList allImages = imagesNode.getChildNodes();
 				
 				for (int i = 0; i < allImages.getLength(); i++) {
 					
 					Node im = allImages.item(i);
 					
 					if (! im.getNodeName().equals(TAG_IMAGE)) continue;
 					
 					NamedNodeMap nnm = im.getAttributes();
 			        
 					String filename = nnm.getNamedItem(ATTR_FILENAME).getNodeValue();
 					String hashAlgorithm = nnm.getNamedItem(ATTR_HASH_ALG).getNodeValue();
 					String hash = nnm.getNamedItem(ATTR_HASH).getNodeValue();
 					
 					meta.setInputImageHash(filename, hashAlgorithm, hash);
 					
 				}	
 			}
 			
 		}
 				
 	}
 	
 	private void loadPreviousAnalysisData(Document metaDoc, AnalysisMetadata meta) {
 		
 		NodeList analysisList = metaDoc.getElementsByTagName(TAG_ANALYSIS);
 
 		if (analysisList.getLength() > 0) {
 			
 			Node analysis = analysisList.item(0); // only one analysis allowed
 			
 			NodeList children = analysis.getChildNodes();
									
 			for (int i = 0; i < children.getLength(); i++) {

 				Node child = children.item(i);
				
 				if (child.getNodeName().equals(TAG_LIBRARY)) {
 					this.processLibraryNode(child, meta);
 				} else if (child.getNodeName().equals(TAG_SCRIPT)) {
 					this.processScriptNode(child, meta);
 				}
 			}
 		
 			
 		}
 		
 	}
 	
 	private void processLibraryNode(Node library, AnalysisMetadata meta) {
 		
 		NamedNodeMap nnm = library.getAttributes();
 		
 		String xmlVersionString =  AnalysisMetadata.getLibraryVersionXMLString();
 		Matcher versionMatcher = AnalysisMetadataXMLParser.versionGrabber.matcher(xmlVersionString);
 		Matcher commitMatcher = AnalysisMetadataXMLParser.commitGrabber.matcher(xmlVersionString);
 		versionMatcher.find();
 		commitMatcher.find();
 		String currentVersion = versionMatcher.group(1);
 		String currentCommit = commitMatcher.group(1);
 		
 		String lastVersion = nnm.getNamedItem(ATTR_VERSION).getNodeValue();
 		String lastCommit = nnm.getNamedItem(ATTR_COMMIT).getNodeValue();
 		
 		if (!(currentVersion.equals(lastVersion) && currentCommit.equals(lastCommit))) {
 			edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.getLogger().warning("library version does not match that used for previous analysis.\nPrevious version info: " + lastVersion + " " + lastCommit  +"\nCurrent version info: " + currentVersion + " " + currentCommit);
 		}
 		
 	}
 	
 	private void processScriptNode(Node script, AnalysisMetadata meta) {
 		Node scriptCData = script.getChildNodes().item(0);
 		String scriptString = scriptCData.getNodeValue();
 		NamedNodeMap nnm = script.getAttributes();
 		String name = nnm.getNamedItem(ATTR_FILENAME).getNodeValue();
 		meta.setScript(new RubyScript(scriptString, name));
 	}
 	
 	
 	/**
      * Parses an XML node from an XML parameter file to a Parameter object.
      * @param node  The node to parse.
      * @return      The Parameter described by the supplied node.
      */
     public Parameter parameterWithXMLNode(Node node) {
 	
         NamedNodeMap nnm = node.getAttributes();
 
         String typeString = null;
         String name = "";
         String displayName = "";
         ParameterType type = ParameterType.STRING_T;
         String descriptionString = "";
         Object value = null;
         Object defaultValue = null;
 
         if (nnm.getNamedItem(ATTR_TYPE) != null) {
             typeString = nnm.getNamedItem(ATTR_TYPE).getNodeValue();
         }
 
         if (typeString != null) {
             if (typeString.equals(ParameterType.BOOLEAN_T.toString())) type = ParameterType.BOOLEAN_T;
             else if(typeString.equals(ParameterType.INTEGER_T.toString())) type = ParameterType.INTEGER_T;
             else if (typeString.equals(ParameterType.FLOATING_T.toString())) type = ParameterType.FLOATING_T;
             else type = ParameterType.STRING_T;
 
         }
 
         if (nnm.getNamedItem(ATTR_NAME) == null && nnm.getNamedItem(ATTR_DISPLAY_NAME) == null) {
             LoggingUtilities.getLogger().severe("parameter specified without name or display name");
             throw new IllegalArgumentException("parameter specified without name or display name");
         }
 
         if (nnm.getNamedItem(ATTR_NAME) != null) {
             name = nnm.getNamedItem(ATTR_NAME).getNodeValue();
         } else {
             name = nnm.getNamedItem(ATTR_DISPLAY_NAME).getNodeValue();
         }
 
         if (nnm.getNamedItem(ATTR_DISPLAY_NAME) != null ) {
             displayName = nnm.getNamedItem(ATTR_DISPLAY_NAME).getNodeValue();
         } else {
             displayName = nnm.getNamedItem(ATTR_NAME).getNodeValue();
         }
 
         if (node.getChildNodes().getLength() > 0) {
             NodeList children = node.getChildNodes();
             for (int i = 0; i < children.getLength(); i++) {
                 Node n = children.item(i);
 
                 if (n.getNodeName() == TAG_DESCRIPTION) {
                     descriptionString += n.getTextContent();
                 }
             }
         }
         
         if (nnm.getNamedItem(ATTR_VALUE) != null) {
             String valueString = nnm.getNamedItem(ATTR_VALUE).getNodeValue();
 
             try {
 
                 switch(type) {
                     case BOOLEAN_T:
 
                         value = Boolean.valueOf(valueString);
 
                         break;
 
                     case INTEGER_T:
 
                         value = Integer.valueOf(valueString);
 
                         break;
 
                     case FLOATING_T:
 
                         value = Double.valueOf(valueString);
 
                         break;
 
                     case STRING_T:
 
                         value = valueString;
 
                         break;
 
                 }
                 
             } catch (NumberFormatException e) {
                 LoggingUtilities.getLogger().warning("Exception encountered while parsing value for parameter named: " + name);
                 value = null;
             }
         }
 
         Parameter p = new Parameter(name, displayName, type, value, descriptionString);
 
         return p;
 
     }
 	
 	
 	
 
 }
