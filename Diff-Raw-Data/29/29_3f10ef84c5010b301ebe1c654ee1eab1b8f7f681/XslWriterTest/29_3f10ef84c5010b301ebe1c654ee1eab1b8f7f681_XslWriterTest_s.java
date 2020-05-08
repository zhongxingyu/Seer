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
 
 import java.nio.charset.Charset;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 
 import org.junit.Test;
 import org.apache.fop.apps.FopFactory;
 import org.joda.time.ReadableDateTime;
 import org.joda.time.DateTime;
 import novelang.common.metadata.DocumentMetadata;
 import novelang.parser.Encoding;
 import novelang.configuration.RenderingConfiguration;
 import novelang.configuration.FopFontStatus;
 import novelang.loader.ResourceLoader;
 import novelang.loader.ClasspathResourceLoader;
 import novelang.loader.ResourceName;
 import novelang.TestResources;
 import novelang.rendering.xslt.validate.BadExpandedNamesException;
 
 /**
  * Tests for {@link novelang.rendering.XslWriter}.
  *
  * @author Laurent Caillette
  */
 public class XslWriterTest {
 
   @Test( expected = BadExpandedNamesException.class )
   public void brokenXpathInStylesheet() throws Exception {
     final XslWriter xslWriter = createXslWriter( TestResources.XSL_BAD_XPATH_1 ) ;
     run( xslWriter ) ;
   }
 
   @Test( expected = BadExpandedNamesException.class )
   public void brokenXpathInStylesheetImport() throws Exception {
     final XslWriter xslWriter = createXslWriter( TestResources.XSL_BAD_XPATH_2 ) ;
     run( xslWriter ) ;
   }
 
 // =======
 // Fixture
 // =======
 
   private static final XslWriter createXslWriter( ResourceName stylesheet ) {
     final RenderingConfiguration renderingConfiguration = new CustomRenderingConfiguration(
         new ClasspathResourceLoader( TestResources.STYLE_RESOURCE_DIR ),
         null,
         null
     ) ;
     return new XslWriter( renderingConfiguration, stylesheet ) ;
   }
 
   private static final void run( XslWriter xslWriter ) throws Exception {
     final OutputStream sinkOutputStream = new ByteArrayOutputStream() ;
     xslWriter.startWriting( sinkOutputStream, new CustomDocumentMetadata(), Encoding.DEFAULT ) ;
   }
 
   private static class CustomDocumentMetadata implements DocumentMetadata {
 
     final ReadableDateTime timestamp = new DateTime() ;
 
     public ReadableDateTime getCreationTimestamp() {
       return timestamp ;
     }
 
     public Charset getEncoding() {
       return Encoding.DEFAULT ;
     }
   }
 
   private static class CustomRenderingConfiguration implements RenderingConfiguration {
 
     private final ResourceLoader resourceLoader ;
     private final FopFactory fopFactory ;
     private final FopFontStatus fopFontStatus ;
 
     public CustomRenderingConfiguration(
         ResourceLoader resourceLoader,
         FopFactory fopFactory,
         FopFontStatus fopFontStatus
     ) {
       this.resourceLoader = resourceLoader ;
       this.fopFactory = fopFactory ;
       this.fopFontStatus = fopFontStatus ;
     }
 
     public ResourceLoader getResourceLoader() {
       return resourceLoader ;
     }
 
     public FopFactory getFopFactory() {
       return fopFactory ;
     }
 
     public FopFontStatus getCurrentFopFontStatus() {
       return fopFontStatus ;
     }
   }
 }
