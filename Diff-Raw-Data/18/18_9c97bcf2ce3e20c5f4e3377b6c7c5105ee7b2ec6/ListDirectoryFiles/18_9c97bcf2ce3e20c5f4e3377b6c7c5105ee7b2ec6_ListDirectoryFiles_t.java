 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.tools.basic;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
 
 /**
  * Given a directory, this component pushes all the file names available in the
  * directory that match a certain regular expression given in the properties
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  */
 
 @Component(
 		name = "List Directory Files",
 		creator = "Xavier Llora",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "#INPUT, file, local",
 		description = "Given a [server-relative] directory, this component pushes all the file names available that " +
 				      "match a certain regular expression given in the properties.",
 		dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class ListDirectoryFiles extends AbstractStreamingExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_LOCATION,
 			description = "The location of the directory to push" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
 	)
 	protected static final String IN_LOCATION = Names.PORT_LOCATION;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_LOCATION,
 			description = "The location being pushed" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_LOCATION = Names.PORT_LOCATION;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_EXPRESSION,
             description = "The regular expression to use as a filter.",
             defaultValue = ".*"
     )
     protected static final String PROP_EXPRESSION = Names.PROP_EXPRESSION;
 
     @ComponentProperty(
             name = Names.PROP_RECURSIVE,
             description = "Should the directory be processed recursively?",
             defaultValue = "true"
     )
     protected static final String PROP_RECURSIVE = Names.PROP_RECURSIVE;
 
     @ComponentProperty(
             name = Names.PROP_WRAP_STREAM,
             description = "Should the output be wrapped as a stream?",
             defaultValue = "true"
     )
     protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;
 
     //--------------------------------------------------------------------------------------------
 
 
 	/** The regular expression */
 	private String sExpression;
 
 	/** Should recurse? */
 	private boolean bRecursive;
 
 	private boolean bWrapStream;
 
 
 	//--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    super.initializeCallBack(ccp);
 
 		sExpression = getPropertyOrDieTrying(PROP_EXPRESSION, false, true, ccp);
 		bRecursive = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_RECURSIVE, ccp));
 		bWrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		for (String sLoc : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION))) {
 		    File dir = (sLoc.trim().toLowerCase().startsWith("file:/")) ?
 		        new File(DataTypeParser.parseAsURI(sLoc)) : new File(sLoc);
 
 		    console.fine("Processing " + dir);
 
 		    if (bWrapStream) {
 		        console.fine("Starting stream: " + streamId);
 		        cc.pushDataComponentToOutput(OUT_LOCATION, new StreamInitiator(streamId));
 		    }
 
     		pushLocations(dir);
 
     		if (bWrapStream) {
     		    console.fine("Ending stream: " + streamId);
     		    cc.pushDataComponentToOutput(OUT_LOCATION, new StreamTerminator(streamId));
     		}
 		}
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         sExpression = null;
         bRecursive = false;
     }
 
 	//-----------------------------------------------------------------------------------
 
     @Override
     public boolean isAccumulator() {
         return false;
     }
 
     //-----------------------------------------------------------------------------------
 
 	private void pushLocation(File file) throws Exception {
 	    String sLoc = file.toURI().toString();
 
         if (sLoc.matches(sExpression)) {
             console.fine(String.format("Pushing filename %s", sLoc));
             componentContext.pushDataComponentToOutput(OUT_LOCATION,
                     BasicDataTypesTools.stringToStrings(sLoc));
         }
 	}
 
 	/**
 	 * Pushes the locations that match the given expression.
 	 *
 	 * @param fileLoc The location being processed
 	 * @throws Exception Failed to push the the file name to the output
 	 */
 	private void pushLocations(File fileLoc) throws Exception {
 	    if (fileLoc.isDirectory()) {
	        File[] files = fileLoc.listFiles();
	        if (files != null)
				for (File file : files) {
		            if (file.isDirectory() && bRecursive)
		                pushLocations(file);
		            else {
		                if (file.isFile())
		                    pushLocation(file);
		            }
		        }
 	    } else {
 	        if (fileLoc.exists())
 	            pushLocation(fileLoc);
 	        else
 	            throw new FileNotFoundException(fileLoc.toString());
 	    }
 	}
 }
