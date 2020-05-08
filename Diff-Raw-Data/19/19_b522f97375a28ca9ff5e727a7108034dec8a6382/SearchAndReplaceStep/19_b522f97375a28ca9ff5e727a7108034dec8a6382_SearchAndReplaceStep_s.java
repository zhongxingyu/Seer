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
 
 package net.sf.okapi.steps.searchandreplace;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 
 public class SearchAndReplaceStep extends BasePipelineStep {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private Parameters params;
 	private String trgLang;
 	private boolean isDone;
 	
 	private Matcher matcher;
 	private Pattern patterns[];
 	
 	@Override
 	public void destroy () {
 		// Nothing to do
 	}
 
 	public SearchAndReplaceStep() {
 		params = new Parameters();
 	}
 
 	
 	public String getDescription () {
 		return "Performs search and replace on the entire file or the text units.";
 	}
 
 	public String getName () {
 		return "Search and Replace";
 	}
 
 	@Override
 	public boolean isDone () {
		if ( !params.plainText ) {
			return true;
		}
		else { // Expects RawDocument
 			return isDone;
 		}
 	}
 	
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		params = (Parameters)params;
 	}
  
 	@Override
 	public boolean needsOutput (int inputIndex) {
		return pipeline.isLastStep(this);
 	}
 
 	
 	@Override
 	protected void handleStartBatch (Event event) {
 		if ( params.regEx ){
 			
 			int flags = 0;
 			patterns = new Pattern[params.rules.size()];
 			
 			//--initialize patterns--
 			if ( params.dotAll ) flags |=  Pattern.DOTALL;
         	if ( params.ignoreCase ) flags |= Pattern.CASE_INSENSITIVE;
         	if ( params.multiLine ) flags |= Pattern.MULTILINE;
 			
         	for(int i=0; i<params.rules.size();i++){
         		String s[] = params.rules.get(i);
  	        	//if ( s[0].equals("true") ) {
 	        		if ( params.regEx ){
 	        			patterns[i]= Pattern.compile(s[1], flags);
  		        	}
  	        	//}        	
         	 }
 		}
 	}	
 		
 	
 	@Override
 	protected void handleStartBatchItem (Event event) {
 		if ( !params.plainText ) { // RawDocument mode
 			isDone = false;
 		}		
 		trgLang = getContext().getTargetLanguage(0);
 	}	
 	
 	@Override
 	protected void handleRawDocument (Event event) {
 		if ( !params.plainText ) {
 			return; // Options set to use on text units only, so we just skip this event
 		}
 		RawDocument rawDoc;
 		String encoding = null;
 		BufferedReader reader = null;
 		BufferedWriter writer = null;
 
 		String result = null;
 		StringBuilder assembled = new StringBuilder();
 		
 		try {
 			rawDoc = (RawDocument)event.getResource();
 			encoding = rawDoc.getEncoding();
 			reader = new BufferedReader(rawDoc.getReader());
 			
 	        char[] buf = new char[1024];
 	        int numRead=0;
 	        while((numRead=reader.read(buf)) != -1){
 	            assembled.append(buf, 0, numRead);
 	        }
 	        reader.close();
 	        result = assembled.toString();
 	        assembled = null;
 	        
 			// Open the output
 			File outFile;
 			if ( pipeline.isLastStep(this) ) {
 				outFile = new File(getContext().getOutputURI(0));
 				Util.createDirectories(outFile.getAbsolutePath());
 			}
 			else {
 				try {
 					outFile = File.createTempFile("okptmp_", ".snr");
 				}
 				catch ( Throwable e ) {
 					throw new OkapiIOException("Cannot create temporary output.", e);
 				}
 				outFile.deleteOnExit();
 			}
 
         	if ( params.regEx ){
         		for(int i=0; i<params.rules.size();i++){
             		String s[] = params.rules.get(i);
      	           	if ( s[0].equals("true") ) {
 		        		
      	           		matcher = patterns[i].matcher(result);
 		        		result = matcher.replaceAll(s[2]);
     	        	}
     	        }
         	}else{
     	        for ( String[] s : params.rules ) {
     	        	if ( s[0].equals("true") ) {
    		        		result = result.replace(s[1],s[2]);
     	        	}
     	        }
         	}
 			
 			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), encoding));
 			Util.writeBOMIfNeeded(writer, true, encoding);
 			writer.write(result);
 			
 			writer.close();
 			reader.close();
 			
 			event.setResource(new RawDocument(outFile.toURI(),rawDoc.getEncoding(),rawDoc.getSourceLanguage(),rawDoc.getTargetLanguage()));
 		}
 		catch ( FileNotFoundException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 		finally {
 			isDone = true;
 			try {
 				if ( writer != null ) {
 					writer.close();
 					writer = null;
 				}
 				if ( reader != null ) {
 					reader.close();
 					reader = null;
 				}
 			}
 			catch ( IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	
 	@Override
 	protected void handleTextUnit (Event event) {
 
 		//--Limit the textunit handler to running in filter-mode--
 		if ( params.plainText ) {
 			throw new OkapiBadStepInputException("Search and Replace cannot be performed on the entire file (non-filter mode) in the current pipeline configuration. \nPlease re-configure the pipeline or modify Search and Replace to use the filter-mode option. ");
 		}
 		
 		TextUnit tu = (TextUnit)event.getResource();
 		// Skip non-translatable
 		if ( !tu.isTranslatable() ) return;
 		
 		String tmp = null;
 		try {
 			// Else: do the requested modifications
 			// Make sure we have a target where to set data
 			tu.createTarget(trgLang, false, IResource.COPY_ALL);
 
 			String result = tu.getTargetContent(trgLang).getCodedText();
 
         	if ( params.regEx ){
         		for(int i=0; i<params.rules.size();i++){
             		String s[] = params.rules.get(i);
      	           	if ( s[0].equals("true") ) {
 		        		
      	           		matcher = patterns[i].matcher(result);
 		        		result = matcher.replaceAll(s[2]);
     	        	}
     	        }
         	}else{
     	        for ( String[] s : params.rules ) {
     	        	if ( s[0].equals("true") ) {
    		        		result = result.replace(s[1],s[2]);
     	        	}
     	        }
         	}
 			
 			TextContainer cnt = tu.getTarget(trgLang); 
 			cnt.setCodedText(result);
 		}
 		catch ( Exception e ) {
 			logger.log(Level.WARNING, "Error when updating content: '"+tmp+"'", e);
 		}		
 	}
 }
