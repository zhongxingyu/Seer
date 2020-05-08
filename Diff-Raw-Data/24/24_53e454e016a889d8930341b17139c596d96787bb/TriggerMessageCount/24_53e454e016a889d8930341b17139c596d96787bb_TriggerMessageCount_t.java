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
 
 package org.seasr.meandre.components.tools.control;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Trigger Message Count",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.any,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "message, trigger",
         description = "This component will receive a message, and a count."+
                       "The message is sent out as many times as indicated by the count.",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TriggerMessageCount extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_OBJECT,
             description = "Object that is saved and forwarded when trigger is received." +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_OBJECT = Names.PORT_OBJECT;
 
     @ComponentInput(
             name = "count",
             description = "The count" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_COUNT = "count";
 
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_OBJECT,
             description = "The Object that has been saved." +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String OUT_OBJECT = Names.PORT_OBJECT;
 
     //--------------------------------------------------------------------------------------------
 
 
    protected boolean _isStreaming = false;

 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         componentInputCache.storeIfAvailable(cc, IN_COUNT);
         componentInputCache.storeIfAvailable(cc, IN_OBJECT);
 
         while (componentInputCache.hasDataAll(new String[] { IN_COUNT, IN_OBJECT })) {
             Object in_count = componentInputCache.peek(IN_COUNT);
             Object in_data = componentInputCache.peek(IN_OBJECT);
 
             if (in_count instanceof StreamDelimiter) {
                 console.warning(String.format("Stream markers should not arrive on the '%s' port - ignoring them...", IN_COUNT));
                 componentInputCache.retrieveNext(IN_COUNT);  // remove it from the cache
                 continue;
             }
 
            if (in_data instanceof StreamInitiator) {
                 // Forward it
                 cc.pushDataComponentToOutput(OUT_OBJECT, in_data);
                 componentInputCache.retrieveNext(IN_OBJECT);  // remove it from the cache
                _isStreaming = true;
                continue;
            }

            if (in_data instanceof StreamTerminator) {
                // Forward it
                cc.pushDataComponentToOutput(OUT_OBJECT, in_data);
                componentInputCache.retrieveNext(IN_OBJECT);  // remove it from the cache
                _isStreaming = false;
                 continue;
             }
 
             int count = Integer.parseInt(DataTypeParser.parseAsString(componentInputCache.retrieveNext(IN_COUNT))[0]);
             Object data = componentInputCache.retrieveNext(IN_OBJECT);
 
             for (int i = 0; i < count; i++)
                 cc.pushDataComponentToOutput(OUT_OBJECT, data);
         }

        if (_isStreaming && componentInputCache.hasData(IN_OBJECT) && componentInputCache.peek(IN_OBJECT) instanceof StreamTerminator) {
            cc.pushDataComponentToOutput(OUT_OBJECT, componentInputCache.retrieveNext(IN_OBJECT));
            _isStreaming = false;
        }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void handleStreamInitiators() throws Exception {
         executeCallBack(componentContext);
     }
 
     @Override
     public void handleStreamTerminators() throws Exception {
         executeCallBack(componentContext);
     }
 }
