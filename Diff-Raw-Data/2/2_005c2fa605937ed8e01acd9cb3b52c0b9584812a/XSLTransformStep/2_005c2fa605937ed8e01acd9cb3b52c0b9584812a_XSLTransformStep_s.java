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
 
 package net.sf.okapi.steps.xsltransform;
 
 import java.io.File;
 import java.net.URI;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import net.sf.okapi.common.ConfigurationString;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
 import net.sf.okapi.common.pipeline.annotations.StepParameterType;
 import net.sf.okapi.common.resource.RawDocument;
 
 @UsingParameters(Parameters.class)
 public class XSLTransformStep extends BasePipelineStep {
 
 	private static final String FACTORY_PROP = "javax.xml.transform.TransformerFactory";
 	private static final String VAR_SRCLANG = "${srcLang}"; 
 	private static final String VAR_TRGLANG = "${trgLang}"; 
 	private static final String VAR_INPUTPATH = "${inputPath}"; 
 	private static final String VAR_INPUTURI = "${inputURI}"; 
 	private static final String VAR_OUTPUTPATH = "${outputPath}"; 
 	private static final String VAR_INPUTPATH1 = "${inputPath1}"; 
 	private static final String VAR_INPUTURI1 = "${inputURI1}"; 
 	private static final String VAR_OUTPUTPATH1 = "${outputPath1}"; 
 	private static final String VAR_INPUTPATH2 = "${inputPath2}"; 
 	private static final String VAR_INPUTURI2 = "${inputURI2}"; 
 	private static final String VAR_INPUTPATH3 = "${inputPath3}"; 
 	private static final String VAR_INPUTURI3 = "${inputURI3}"; 
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private Parameters params;
 	private Source xsltInput;
 	private Map<String, String> paramList;
 	private Transformer trans;
 	private boolean isDone;
 	private URI outputURI;
 	private RawDocument input1;
 	private RawDocument input2;
 	private RawDocument input3;
 	private String originalProcessor;
 	
 	public XSLTransformStep () {
 		params = new Parameters();
 		trans = null;
 		originalProcessor = System.getProperty(FACTORY_PROP);
 	}
 	
 	@Override
 	public void destroy () {
 		// Make available to GC
 		trans = null;
 		xsltInput = null;
 	}
 
 	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
 	public void setOutputURI (URI outputURI) {
 		this.outputURI = outputURI;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.INPUT_RAWDOC)
 	public void setInput (RawDocument input) {
 		input1 = input;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
 	public void setSecondInput (RawDocument secondInput) {
 		input2 = secondInput;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.THIRD_INPUT_RAWDOC)
 	public void setThirdInput (RawDocument thridInput) {
 		input3 = thridInput;
 	}
 	
 	public String getDescription () {
 		return "Apply an XSLT template to an XML document."
			+ " Expects: raw XML document. Sends back: raw XML document.";
 	}
 
 	public String getName () {
 		return "XSLT Transformation";
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 	
 	@Override
 	public boolean isDone () {
 		return isDone;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
  
 	@Override
 	protected Event handleStartBatch (Event event) {
 		try {
 			// Create the parameters map
 			ConfigurationString cfgString = new ConfigurationString(params.paramList);
 			paramList = cfgString.toMap();
 			
 			// Create the source for the XSLT
 			xsltInput = new javax.xml.transform.stream.StreamSource(
 				new File(params.xsltPath)); //TODO: .replace(VAR_PROJDIR, projectDir)));
 			
 			// Create an instance of TransformerFactory
 			if ( params.useCustomTransformer ) {
 				System.setProperty(FACTORY_PROP, params.factoryClass);
 			}
 			javax.xml.transform.TransformerFactory fact
 				= javax.xml.transform.TransformerFactory.newInstance();
 
 			trans = fact.newTransformer(xsltInput);
 			logger.info("Factory used: " + fact.getClass().getCanonicalName());
 			logger.info("Transformer used: " + trans.getClass().getCanonicalName());
 			isDone = true;
 		}
 		catch ( TransformerConfigurationException e ) {
 			throw new OkapiIOException("Error in XSLT input.\n" + e.getMessage(), e);
 		}
 		finally {
 			// Make sure to reset the original property
 			if ( params.useCustomTransformer ) {
 				System.setProperty(FACTORY_PROP, originalProcessor);
 			}
 		}
 		
 		return event;
 	}
 	
 	@Override
 	protected Event handleStartBatchItem (Event event) {
 		isDone = false;
 		return event;
 	}
 
 	@Override
 	protected Event handleRawDocument (Event event) {
 		try {
 			RawDocument rawDoc = (RawDocument)event.getResource();
 			trans.reset();
 			fillParameters();
 			
 			Properties props = trans.getOutputProperties();
 			for ( Object obj: props.keySet() ) {
 				String key = (String)obj;
 				String value = props.getProperty(key);
 				value = value+"";
 			}
 			
 			// Create the input source
 			// Use the stream, so the encoding auto-detection can be done
 			Source xmlInput = new javax.xml.transform.stream.StreamSource(rawDoc.getStream());
 			
 			// Create the output
 			File outFile;
 			if ( isLastOutputStep() ) {
 				outFile = new File(outputURI);
 				Util.createDirectories(outFile.getAbsolutePath());
 			}
 			else {
 				try {
 					outFile = File.createTempFile("okp-xslt_", ".tmp");
 				}
 				catch ( Throwable e ) {
 					throw new OkapiIOException("Cannot create temporary output.", e);
 				}
 				outFile.deleteOnExit();
 			}
 			
 			Result result = new javax.xml.transform.stream.StreamResult(outFile);
 			
 			// Apply the template
 			trans.transform(xmlInput, result);
 			
 			// Create the new raw-document resource
 			event.setResource(new RawDocument(outFile.toURI(), "UTF-8", 
 				rawDoc.getSourceLocale(), rawDoc.getTargetLocale()));
 		}
 		catch ( TransformerException e ) {
 			throw new OkapiIOException("Transformation error.\n" + e.getMessage(), e);
 		}
 		finally {
 			isDone = true;
 		}
 		
 		return event;
 	}
 
 	private void fillParameters () {
 		trans.clearParameters();
 		String value = null;
 		try {
 			for ( String key : paramList.keySet() ) {
 				// Try to find the replacement(s)
 				value = paramList.get(key).replace(VAR_SRCLANG, input1.getSourceLocale().getLanguage());
 				if ( value.indexOf(VAR_TRGLANG) > -1 ) {
 					value = value.replace(VAR_TRGLANG, input1.getTargetLocale().getLanguage());
 				}
 
 				if ( value.indexOf(VAR_INPUTPATH) > -1 ) {
 					value = value.replace(VAR_INPUTPATH, input1.getInputURI().getPath());
 				}
 				if ( value.indexOf(VAR_INPUTURI) > -1 ) {
 					value = value.replace(VAR_INPUTURI, input1.getInputURI().toString());
 				}
 				if ( value.indexOf(VAR_OUTPUTPATH) > -1 ) {
 					value = value.replace(VAR_OUTPUTPATH, outputURI.getPath());
 				}
 				
 				if ( value.indexOf(VAR_INPUTPATH1) > -1 ) { // Same as VAR_INPUTPATH
 					value = value.replace(VAR_INPUTPATH1, input1.getInputURI().getPath());
 				}
 				if ( value.indexOf(VAR_INPUTURI1) > -1 ) {
 					value = value.replace(VAR_INPUTURI1, input1.getInputURI().toString());
 				}
 				if ( value.indexOf(VAR_OUTPUTPATH1) > -1 ) { // Same as VAR_OUTPUTPATH
 					value = value.replace(VAR_OUTPUTPATH1, outputURI.getPath());
 				}
 				
 				if ( value.indexOf(VAR_INPUTPATH2) > -1 ) {
 					if ( input2 == null ) {
 						value = value.replace(VAR_INPUTPATH2, "null");
 					}
 					else {
 						value = value.replace(VAR_INPUTPATH2, input2.getInputURI().getPath());
 					}
 				}
 				if ( value.indexOf(VAR_INPUTURI2) > -1 ) {
 					if ( input2 == null ) {
 						value = value.replace(VAR_INPUTURI2, "null");
 					}
 					else {
 						value = value.replace(VAR_INPUTURI2, input2.getInputURI().toString());
 					}
 				}
 				
 				if ( value.indexOf(VAR_INPUTPATH3) > -1 ) {
 					if ( input3 == null ) {
 						value = value.replace(VAR_INPUTPATH3, "null");
 					}
 					else {
 						value = value.replace(VAR_INPUTPATH3, input3.getInputURI().getPath());
 					}
 				}
 				if ( value.indexOf(VAR_INPUTURI3) > -1 ) {
 					if ( input3 == null ) {
 						value = value.replace(VAR_INPUTURI3, "null");
 					}
 					else {
 						value = value.replace(VAR_INPUTURI3, input3.getInputURI().toString());
 					}
 				}
 				
 				// Assign the variable
 				trans.setParameter(key, value);
 			}
 		}
 		catch ( Throwable e ) {
 			logger.severe(String.format("Error when trying to substitute variables in the parameter value '%s'", value));
 		}
 	}
 
 }
