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
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
 		name = "Flow Parameter",
 		creator = "Boris Capitanu",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "#INPUT, io, string, parameter",
 		description = "Pushes the value of a flow parameter to the output.",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class FlowParam extends AbstractStreamingExecutableComponent {
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TEXT,
 			description = "The parameter value" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = "param_name",
             description = "The parameter name whose value to push",
             defaultValue = ""
     )
     protected static final String PROP_PARAM_NAME = "param_name";
 
     @ComponentProperty(
             name = "default_value",
             description = "The parameter value to use if the flow parameter specified is not set",
             defaultValue = ""
     )
     protected static final String PROP_DEFAULT_VALUE = "default_value";
 
     @ComponentProperty(
             name = Names.PROP_WRAP_STREAM,
             description = "Should the output be wrapped as a stream?",
             defaultValue = "false"
     )
     protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;
 
 	//--------------------------------------------------------------------------------------------
 
 
     protected String _paramName;
     protected String _defaultValue;
     protected boolean _wrapStream;
 
 
 	//--------------------------------------------------------------------------------------------
 
 	@Override
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		_paramName = getPropertyOrDieTrying(PROP_PARAM_NAME, ccp);
 		_defaultValue = getPropertyOrDieTrying(PROP_DEFAULT_VALUE, false, false, ccp);
 		_wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
 	}
 
 	@Override
 	public void executeCallBack(ComponentContext cc) throws Exception {
 		if (_wrapStream)
 			cc.pushDataComponentToOutput(OUT_TEXT, new StreamInitiator(streamId));
 
 		String paramValue = cc.getFlowProperties().getProperty(_paramName, _defaultValue);
 		cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(paramValue));
 
 		if (_wrapStream)
 			cc.pushDataComponentToOutput(OUT_TEXT, new StreamTerminator(streamId));
 	}
 
 	@Override
 	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 
 	//--------------------------------------------------------------------------------------------
 
 	@Override
 	public boolean isAccumulator() {
 		return false;
 	}
 }
