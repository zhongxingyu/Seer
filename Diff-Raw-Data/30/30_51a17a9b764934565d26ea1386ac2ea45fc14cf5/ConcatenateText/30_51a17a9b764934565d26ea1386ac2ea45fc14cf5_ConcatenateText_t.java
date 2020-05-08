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
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 
 /**
  * Concatenates text from multiple inputs
  *
  * @author Boris Capitanu
  *
  */
 
 @Component(
         name = "Concatenate Text",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/tools/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "io, string, concatenate",
         description = "Concatenates text from multiple inputs",
         dependency = {"protobuf-java-2.0.3.jar"}
 )
 public class ConcatenateText extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "First text"
     )
     protected static final String IN_TEXT1 = Names.PORT_TEXT;
 
     @ComponentInput(
             name = Names.PORT_TEXT_2,
             description = "Second text"
     )
     protected static final String IN_TEXT2 = Names.PORT_TEXT_2;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TEXT,
             description = "The concatenated text"
     )
     protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_SEPARATOR,
             description = "The text to use as a separator when concatenating the texts",
             defaultValue = ""
     )
     protected static final String PROP_SEPARATOR = Names.PROP_SEPARATOR;
 
     //--------------------------------------------------------------------------------------------
 
 
     private String separator;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         separator = ccp.getProperty(PROP_SEPARATOR);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         String[] inputs1 = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT1));
         String[] inputs2 = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT2));
 
         if (inputs1.length != inputs2.length)
             throw new Exception("Cannot process inputs of varying length!");
 
         for (int i = 0; i < inputs1.length; i++)
             cc.pushDataComponentToOutput(OUT_TEXT,
                     BasicDataTypesTools.stringToStrings(inputs1[i] + separator + inputs2[i]));
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (inputPortsWithInitiators.contains(IN_TEXT1) &&
            inputPortsWithInitiators.contains(IN_TEXT2))
            componentContext.pushDataComponentToOutput(OUT_TEXT, new StreamInitiator());
        else
            throw new Exception("Unbalanced or unexpected StreamInitiator received");
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (inputPortsWithTerminators.contains(IN_TEXT1) &&
            inputPortsWithTerminators.contains(IN_TEXT2))
            componentContext.pushDataComponentToOutput(OUT_TEXT, new StreamTerminator());
        else
            throw new Exception("Unbalanced or unexpected StreamTerminator received");
    }
 }
