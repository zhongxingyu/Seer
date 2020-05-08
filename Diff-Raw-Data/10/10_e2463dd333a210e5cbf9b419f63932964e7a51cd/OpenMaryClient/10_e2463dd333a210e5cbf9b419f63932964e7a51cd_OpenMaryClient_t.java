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
 
 package org.seasr.meandre.components.nlp.openmary;
 
 import java.io.ByteArrayOutputStream;
 
 import marytts.client.MaryClient;
 import marytts.client.http.Address;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 
 /**
  *
  * @author Boris Capitanu
  *
  */
 
 @Component(
        creator = "Boris Capitanu",
        description = "OpenMary client",
        name = "OpenMary Client",
        tags = "openmary, speech, audio",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class OpenMaryClient extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The input data" +
                           "<br>TYPE: java.lang.String" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                           "<br>TYPE: byte[]" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "bytes",
             description = "The output data" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
     )
     protected static final String OUT_BYTES = "bytes";
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = "server_hostname",
             description = "The hostname for the OpenMary server",
             defaultValue = "localhost"
     )
     protected static final String PROP_SERVER = "server_hostname";
 
     @ComponentProperty(
             name = "server_port",
             description = "The port number for the OpenMary server",
             defaultValue = "59125"
     )
     protected static final String PROP_PORT = "server_port";
 
     @ComponentProperty(
             name = "locale",
             description = "Specifies the language of the domain",
             defaultValue = "en-US"
     )
     protected static final String PROP_LOCALE = "locale";
 
     @ComponentProperty(
             name = "input_type",
             description = "The input type. Can be one of:<br>" +
             		"TEXT<br>SIMPLEPHONEMES<br>APML<br>SSML<br>SABLE<br>PARTSOFSPEECH<br>PHONEMES<br>" +
             		"INTONATION<br>ALLOPHONES<br>PRAAT_TEXTGRID<br>REALISED_DURATIONS<br>REALISED_ACOUSTPARAMS<br>" +
             		"RAWMARYXML<br>TOKENS<br>WORDS<br>ACOUSTPARAMS",
             defaultValue = "TEXT"
     )
     protected static final String PROP_INPUT_TYPE = "input_type";
 
     @ComponentProperty(
             name = "output_type",
             description = "The output type. Can be one of:<br>" +
             		"PARTSOFSPEECH<br>PHONEMES<br>INTONATION<br>ALLOPHONES<br>PRAAT_TEXTGRID<br>" +
             		"REALISED_DURATIONS<br>REALISED_ACOUSTPARAMS<br>RAWMARYXML<br>TOKENS<br>WORDS<br>" +
             		"ACOUSTPARAMS<br>HALFPHONE_TARGETFEATURES<br>TARGETFEATURES<br>AUDIO",
             defaultValue = "AUDIO"
     )
     protected static final String PROP_OUTPUT_TYPE = "output_type";
 
     @ComponentProperty(
             name = "audio_type",
             description = "The audio type. Can be one of:<br>" +
             		"WAVE<br>AU<br>AIFF",
             defaultValue = "WAVE"
     )
     protected static final String PROP_AUDIO_TYPE = "audio_type";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected MaryClient _openMary = null;
 
     protected String _serverHost;
     protected int _serverPort;
     protected String _locale;
     protected String _inputType;
     protected String _outputType;
     protected String _audioType;
 
     protected String _defaultVoiceName = null;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _serverHost = getPropertyOrDieTrying(PROP_SERVER, ccp);
         _serverPort = Integer.parseInt(getPropertyOrDieTrying(PROP_PORT, ccp));
         _locale = getPropertyOrDieTrying(PROP_LOCALE, ccp);
         _inputType = getPropertyOrDieTrying(PROP_INPUT_TYPE, ccp);
         _outputType = getPropertyOrDieTrying(PROP_OUTPUT_TYPE, ccp);
         _audioType = getPropertyOrDieTrying(PROP_AUDIO_TYPE, ccp);
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         // Delay initialization until execute() so that if we have an OpenMary server
         // component, it has time to start during initialize() before we attempt
         // to connect to it
         if (_openMary == null)
             _openMary = MaryClient.getMaryClient(new Address(_serverHost, _serverPort));
 
         for (String text : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))) {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                _openMary.process(text, _inputType, _outputType, _locale, _audioType, _defaultVoiceName, baos);
            }
            catch (Exception e) {
                console.severe(e.getMessage());
                console.severe("The failing text follows:");
                console.severe(text);
                throw e;
            }
 
             cc.pushDataComponentToOutput(OUT_BYTES, BasicDataTypesTools.byteArrayToBytes(baos.toByteArray()));
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _openMary = null;
     }
 }
