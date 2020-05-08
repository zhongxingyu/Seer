 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.rendering;
 
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 
 import org.xml.sax.ContentHandler;
 import novelang.configuration.RenderingConfiguration;
 import novelang.common.metadata.TreeMetadata;
 import novelang.loader.ResourceName;
 
 /**
  * @author Laurent Caillette
  */
 public class NlpWriter extends EscapingWriter {
 
   protected static final ResourceName DEFAULT_NLP_STYLESHEET = new ResourceName( "nlp.xsl" ) ;
 
   public NlpWriter( RenderingConfiguration configuration, ResourceName stylesheet ) {
     super(
         configuration,
         null == stylesheet ? DEFAULT_NLP_STYLESHEET : stylesheet, 
         RenditionMimeType.NLP ) ;
   }
 
   protected final ContentHandler createSinkContentHandler(
       final OutputStream outputStream,
       TreeMetadata treeMetadata,
       final Charset encoding
   )
       throws Exception
   {
 
     // dom4j's XML writer does some clever entity-escaping not good for us.
    final ContentHandler sink = new TextSink( outputStream );

    return sink ;
   }
 
 
 }
