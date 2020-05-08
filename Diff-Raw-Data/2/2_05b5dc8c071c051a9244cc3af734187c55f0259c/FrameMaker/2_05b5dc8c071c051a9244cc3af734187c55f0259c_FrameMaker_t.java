 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.vis.html;
 
 import java.io.File;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.velocity.VelocityContext;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
 import org.seasr.meandre.support.generic.html.VelocityTemplateService;
 import org.seasr.meandre.support.generic.io.FileUtils;
 import org.seasr.meandre.support.generic.io.IOUtils;
 
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "Arranges into frames multiple HTML documents that are part of a stream",
         name = "Frame Maker",
         tags = "html, frames",
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = { "velocity-1.7-dep.jar", "protobuf-java-2.2.0.jar" },
         resources = { "FrameMaker.vm" }
 )
 public class FrameMaker extends AbstractStreamingExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 	        name = Names.PORT_HTML,
 	        description = "The HTML data" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
 	)
     protected static final String IN_HTML = Names.PORT_HTML;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_HTML,
             description = "The output HTML containing the frames" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_HTML = Names.PORT_HTML;
 
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             description = "The template name",
             name = Names.PROP_TEMPLATE,
             defaultValue = "org/seasr/meandre/components/vis/html/FrameMaker.vm"
     )
     protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;
 
     @ComponentProperty(
             description = "Number of columns (rows calculated automatically)",
             name = "columns",
             defaultValue = ""
     )
     protected static final String PROP_COLUMNS = "columns";
 
     //--------------------------------------------------------------------------------------------
 
     protected static final String FRAME_MAKER_HOME = "frame_maker";
     protected static final String FRAME_MAKER_URL = "/public/resources/" + FRAME_MAKER_HOME;
 
     protected String _template;
     protected File _parent;
     protected List<String> _htmlDocs = new ArrayList<String>();
     protected List<File> _tmpFiles = new ArrayList<File>();
     protected int _columns;
     protected boolean _isStreaming;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         super.initializeCallBack(ccp);
 
         _template = getPropertyOrDieTrying(PROP_TEMPLATE, ccp);
         _columns = Integer.parseInt(getPropertyOrDieTrying(PROP_COLUMNS, ccp));
 
 		_parent = new File(ccp.getPublicResourcesDirectory(), FRAME_MAKER_HOME);
         _parent.mkdirs();
 
         _isStreaming = false;
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
     	Object input = cc.getDataComponentFromInput(IN_HTML);
 		String html = DataTypeParser.parseAsString(input)[0];
 
     	if (_isStreaming) {
     		File tmpFile = File.createTempFile("frame_", ".html", _parent);
     		Writer writer = IOUtils.getWriterForResource(tmpFile.toURI());
 			writer.write(html);
 			writer.close();
 
     		_tmpFiles.add(tmpFile);
     		_htmlDocs.add(String.format("%s/%s", FRAME_MAKER_URL, tmpFile.getName()));
     	} else {
     		console.warning("No stream detected - forwarding input unmodified");
     		cc.pushDataComponentToOutput(OUT_HTML, input);
     	}
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     	for (File tmpFile : _tmpFiles)
     		FileUtils.deleteFileOrDirectory(tmpFile);
 
     	_tmpFiles.clear();
     	_tmpFiles = null;
     	_htmlDocs.clear();
     	_htmlDocs = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
 	public boolean isAccumulator() {
 		return true;
 	}
 
 	@Override
 	public void startStream() throws Exception {
 		_htmlDocs.clear();
 		_isStreaming = true;
 	}
 
 	@Override
 	public void endStream() throws Exception {
         VelocityTemplateService velocity = VelocityTemplateService.getInstance();
         VelocityContext context = velocity.getNewContext();
 
         context.put("htmlDocs", _htmlDocs);
        context.put("rows", (int)Math.ceil(_htmlDocs.size() / (double)_columns));
 		context.put("columns", Math.min(_columns, _htmlDocs.size()));
 
         String html = velocity.generateOutput(context, _template);
         componentContext.pushDataComponentToOutput(OUT_HTML, html);
 
 		_isStreaming = false;
 		_htmlDocs.clear();
 	}
 }
