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
 
 package org.seasr.meandre.components.vis.text;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 import org.seasr.meandre.support.text.TagCloudImage;
 
 /**
  * @author Lily Dong
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Lily Dong",
         description = "Creates a tag cloud image from a word count table. " +
                       "If there are many tags to be displayed, " +
                       "reduce the maximum size of the font or increase the size of the canvas " +
                       "to accommodate all of tags.",
         name = "Tag Cloud Image Maker",
         rights = Licenses.UofINCSA,
         tags = "tag cloud, visualization",
         baseURL="meandre://seasr.org/components/",
         dependency = {"protobuf-java-2.0.3.jar"}
 )
 public class TagCloudImageMaker extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             description = "Tags to be analyzed." +
                           "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>, IntegersMap",
             name = Names.PORT_TOKEN_COUNTS
     )
     protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             description = "The image." +
                           "<br>TYPE: Bytes",
             name = Names.PORT_RAW_DATA
     )
     protected final static String OUT_IMAGE_RAW = Names.PORT_RAW_DATA;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
 	@ComponentProperty(
 	        defaultValue = "1000",
             description = "This property sets the width of the canvas.",
             name = Names.PROP_WIDTH
 	)
     protected static final String PROP_CANVAS_WIDTH = Names.PROP_WIDTH;
 
 	@ComponentProperty(
 	        defaultValue = "1000",
   		   	description = "This property sets the height of the canvas.",
   		   	name = Names.PROP_HEIGHT
 	)
     protected static final String PROP_CANVAS_HEIGHT = Names.PROP_HEIGHT;
 
 	@ComponentProperty(
 	        defaultValue = "Courier",
 	        description = "This property sets the name of the font to be used for the words in the tag cloud.",
 	        name = Names.PROP_FONT_NAME
 	)
     protected static final String PROP_FONT_NAME = Names.PROP_FONT_NAME;
 
 	@ComponentProperty(
 	        defaultValue = "150",
   			description = "This property sets the maximum size of the font.",
   			name = Names.PROP_MAX_SIZE
 	)
     protected static final String PROP_FONT_MAX_SIZE = Names.PROP_MAX_SIZE;
 
 	@ComponentProperty(
 	        defaultValue = "20",
 	        description = "This property sets the minimum size of the font to be used for the words in the tag cloud.",
 	        name = Names.PROP_MIN_SIZE
 	)
     protected static final String PROP_FONT_MIN_SIZE = Names.PROP_MIN_SIZE;
 
 	@ComponentProperty(
 	        defaultValue = "false",
     		description = "Set to 'true' to show the count for each tag. " +
     		              "Ensure there is enough space for showing the count. In doing so, " +
     		              "you could increase the canvas width or height or both, or reduce the font size.",
     		name = Names.PROP_SHOW_COUNT
 	)
     protected static final String PROP_SHOW_COUNT = Names.PROP_SHOW_COUNT;
 
     //--------------------------------------------------------------------------------------------
 
 
 	private org.seasr.meandre.support.text.TagCloudImageMaker _tagCloudImageMaker;
 
 
     //--------------------------------------------------------------------------------------------
 
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         int canvasWidth = Integer.parseInt(ccp.getProperty(PROP_CANVAS_WIDTH));
         int canvasHeight = Integer.parseInt(ccp.getProperty(PROP_CANVAS_HEIGHT));
 
         String fontName = ccp.getProperty(PROP_FONT_NAME);
         if (fontName.trim().length() == 0)
             fontName = null;
 
        float fontSizeMin = Float.parseFloat(ccp.getProperty(PROP_FONT_MIN_SIZE));
        float fontSizeMax = Float.parseFloat(ccp.getProperty(PROP_FONT_MAX_SIZE));
 
         boolean showCounts = Boolean.parseBoolean(ccp.getProperty(PROP_SHOW_COUNT));
 
         _tagCloudImageMaker = new org.seasr.meandre.support.text.TagCloudImageMaker(
                 canvasWidth, canvasHeight, fontName, fontSizeMin, fontSizeMax, showCounts);
     }
 
     public void executeCallBack(ComponentContext cc) throws Exception {
         Map<String, Integer> table = DataTypeParser.parseAsStringIntegerMap(
                 cc.getDataComponentFromInput(IN_TOKEN_COUNTS));
 
         console.fine("Creating the tag cloud image");
         TagCloudImage image = _tagCloudImageMaker.createTagCloudImage(table);
         console.fine("Tag cloud image created");
 
         if (!image.hasAllWords()) {
             StringBuffer buf = new StringBuffer();
             buf.append("Only " + image.getShownWords() + " of " + image.getTotalWords() + " words displayed due to limited space.\n");
             buf.append("For viewing all of the words, the alternatives you can choose are\n");
             buf.append("1) Increase the width or height of canvas\n");
             buf.append("2) Decrease the number of words to be displayed.\n");
             buf.append("3) Decrease the minimum font size.\n");
             buf.append("4) Decrease the maximum font size.\n");
             console.warning(buf.toString());
         }
 
 	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
 	    ImageIO.write(image, "png", baos);
 	    baos.flush();
 
  		cc.pushDataComponentToOutput(OUT_IMAGE_RAW, baos.toByteArray());
     }
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
