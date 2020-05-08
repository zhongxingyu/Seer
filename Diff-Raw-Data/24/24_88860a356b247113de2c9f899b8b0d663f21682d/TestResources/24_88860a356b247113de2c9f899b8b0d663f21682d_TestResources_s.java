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
 
 package novelang;
 
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 import org.apache.fop.apps.FopFactory;
 import org.apache.fop.fonts.EmbedFontInfo;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import novelang.configuration.ConfigurationTools;
 import novelang.configuration.ContentConfiguration;
 import novelang.configuration.DaemonConfiguration;
 import novelang.configuration.FopFontStatus;
 import novelang.configuration.ProducerConfiguration;
 import novelang.configuration.RenderingConfiguration;
 import novelang.loader.ClasspathResourceLoader;
 import novelang.loader.ResourceLoader;
 import novelang.loader.ResourceLoaderTools;
 import novelang.loader.ResourceName;
 import novelang.system.DefaultCharset;
 
 /**
  * The only place where constants referencing test-dedicated resources can be defined.
  *
  * @author Laurent Caillette
  */
 public class TestResources {
 
   private TestResources() { }
 
   private static final String ONE_WORD_RELATIVEFILENAME = "one-word.nlp";
   public static final String ONE_WORD_ABSOLUTEFILENAME = "/" + ONE_WORD_RELATIVEFILENAME ;
   public static final ResourceName ONE_WORD_RESOURCENAME = new ResourceName( ONE_WORD_RELATIVEFILENAME ) ;
 
   public static final String PARTS_DIR = "/parts" ;
   public static final String JUST_SECTIONS = PARTS_DIR + "/just-sections.nlp";
   public static final String MESSY_IDENTIFIERS = PARTS_DIR + "/messy-identifiers.nlp";
   public static final String NO_CHAPTER = PARTS_DIR + "/no-chapter.nlp";
   public static final String SIMPLE_STRUCTURE = PARTS_DIR + "/simple-structure.nlp";
   public static final String BROKEN_CANNOTPARSE = PARTS_DIR + "/broken-cannotparse.nlp";
 
 
   public static final String SCANNED_DIR = "/scanned" ;
   public static final String SCANNED_BOOK_NOSTYLE = SCANNED_DIR + "/book.nlb" ;
   public static final String SCANNED_BOOK_WITHSTYLE = SCANNED_DIR + "/book-withstyle.nlb" ;
   public static final String SCANNED_FILE1 = SCANNED_DIR + "/file1.nlp" ;
   public static final String SCANNED_FILE2 = SCANNED_DIR + "/file2.nlp" ;
   public static final String SCANNED_SUBDIR = SCANNED_DIR + "/sub" ;
   public static final String SCANNED_FILE3 = SCANNED_SUBDIR + "/file3.nlp" ;
 
 
   
 
   public static final String FONT_STRUCTURE_DIR = "/fonts-structure" ;
 
   public static final String DEFAULT_FONTS_DIR = FONT_STRUCTURE_DIR + "/fonts" ;
   public static final String FONT_FILE_DEFAULT_1 = DEFAULT_FONTS_DIR + "/Bitstream-Vera-Sans-Mono.ttf" ;
   public static final String FONT_FILE_DEFAULT_2 = DEFAULT_FONTS_DIR + "/Bitstream-Vera-Sans-Mono-Bold.ttf" ;
 
   public static final String ALTERNATE_FONTS_DIR_NAME = "alternate" ;
   public static final String ALTERNATE_FONTS_DIR =
       FONT_STRUCTURE_DIR + "/" + ALTERNATE_FONTS_DIR_NAME ;
   public static final String FONT_FILE_ALTERNATE =
       ALTERNATE_FONTS_DIR + "/Bitstream-Vera-Sans-Mono-Bold-Oblique.ttf" ;
 
   public static final String FONT_FILE_PARENT_CHILD =
       FONT_STRUCTURE_DIR + "/parent/child/Bitstream-Vera-Sans-Mono-Oblique.ttf" ;
   public static final String FONT_FILE_PARENT_CHILD_BAD =
       FONT_STRUCTURE_DIR + "/parent/child/Bad.ttf" ;
 
 
 
   public static final String STYLE_RESOURCE_DIR = "/style" ;
 
   public static final ResourceName XSL_BAD_XPATH_1 = new ResourceName( "bad-xpath-1.xsl" ) ;
   public static final ResourceName XSL_BAD_XPATH_2 = new ResourceName( "bad-xpath-2.xsl" ) ;
 
   public static final String NODESET_DIR = "/format-in-xsl" ;
   public static final ResourceName NODESET_XSL = new ResourceName( "format.xsl" ) ;
   public static final String NODESET_SOMECHAPTERS_DOCUMENTNAME = "some-chapters" ;
   public static final String NODESET_SOMECHAPTERS =
       NODESET_DIR + "/" + NODESET_SOMECHAPTERS_DOCUMENTNAME + ".nlp" ;
 
   public static final ResourceName SHOWCASE = new ResourceName( "showcase/showcase.nlp" ) ;
 
 
   public static final String SERVED_DIRECTORY_NAME = "served" ;
   public static final String SERVED_GOOD_RADIX = "good";
   public static final String
       SERVED_PART_GOOD_NOEXTENSION = "/" + SERVED_DIRECTORY_NAME + "/" + SERVED_GOOD_RADIX;
   public static final String SERVED_PARTSOURCE_GOOD = SERVED_PART_GOOD_NOEXTENSION + ".nlp" ;
   public static final String SERVED_HTMLDOCUMENT_GOOD = SERVED_PART_GOOD_NOEXTENSION + ".html" ;
 
   public static final ResourceName SERVED_VOIDSTYLESHEET =
       new ResourceName( SERVED_DIRECTORY_NAME + "void.xsl" ) ;
 
   public static final String SERVED_PART_BROKEN_NOEXTENSION =
       "/" + SERVED_DIRECTORY_NAME + "/broken" ;
 
   public static final String SERVED_PARTSOURCE_BROKEN = SERVED_PART_BROKEN_NOEXTENSION + ".nlp" ;
 
   public static final String SERVED_BOOK_WITHALTERNATESTYLESHEET_NOEXTENSION =
       "/" + SERVED_DIRECTORY_NAME + "/book-alternatexsl" ;
 
   public static final String SERVED_BOOK_WITHALTERNATESTYLESHEET =
       SERVED_BOOK_WITHALTERNATESTYLESHEET_NOEXTENSION + ".nlb" ;
 
   public static final String SERVED_BOOK_BADSCANNEDPART_NOEXTENSION =
       "/" + SERVED_DIRECTORY_NAME + "/book-bad-scanned-part" ;
 
   public static final String SERVED_BOOK_BADSCANNEDPART =
       SERVED_BOOK_BADSCANNEDPART_NOEXTENSION + ".nlb" ;
 
 
   public static final String IMAGES_DIRECTORY_NAME = "images" ;
 
   public static final String BLUE_128x64_GIF_NAME = "Blue-128x64.gif" ;
   public static final String GREEN_128x64_JPG_NAME = "Green-128x64.jpg" ;
   public static final String RED_128x64_PNG_NAME = "Red-128x64.png" ;
   public static final String YELLOW_128x64_SVG_NAME = "Yellow-128x64.svg" ;
 
   public static final ResourceName IMAGE_BLUE_128x64_GIF =
      new ResourceName( "images/" + BLUE_128x64_GIF_NAME ) ;
   public static final ResourceName IMAGE_GREEN_128x64_JPG =
      new ResourceName( "images/" + GREEN_128x64_JPG_NAME ) ;
   public static final ResourceName IMAGE_RED_128x64_PNG =
      new ResourceName( "images/" + RED_128x64_PNG_NAME ) ;
   public static final ResourceName IMAGE_YELLOW_128x64_SVG =
      new ResourceName( "images/" + YELLOW_128x64_SVG_NAME ) ;
 
 
   public static void copyServedResources( File contentDirectory ) {
     TestResourceTools.copyResourceToDirectory(
         TestResources.class, SERVED_PARTSOURCE_GOOD, contentDirectory ) ;
 
     TestResourceTools.copyResourceToDirectory(
         TestResources.class, SERVED_PARTSOURCE_BROKEN, contentDirectory ) ;
 
     TestResourceTools.copyResourceToDirectory(
         TestResources.class, SERVED_BOOK_WITHALTERNATESTYLESHEET, contentDirectory ) ;
 
   }
 
   public static ProducerConfiguration createProducerConfiguration(
       final File contentDirectory,
       final String styleDirectoryName,
       final boolean shouldAddClasspathResourceLoader,
       final Charset renderingCharset
   ) {
     final ResourceLoader resourceLoader ;
     final ClasspathResourceLoader customResourceLoader =
         new ClasspathResourceLoader( styleDirectoryName ) ;
     if( shouldAddClasspathResourceLoader ) {
       resourceLoader = ResourceLoaderTools.compose(
           customResourceLoader,
           new ClasspathResourceLoader( ConfigurationTools.BUNDLED_STYLE_DIR )
       ) ;
     } else {
       resourceLoader = customResourceLoader ;
     }
 
     return new ProducerConfiguration() {
 
       public RenderingConfiguration getRenderingConfiguration() {
         return new RenderingConfiguration() {
           public ResourceLoader getResourceLoader() {
             return resourceLoader ;
           }
           public FopFactory getFopFactory() {
             return FopFactory.newInstance() ;
           }
 
           public FopFontStatus getCurrentFopFontStatus() {
             final Iterable< EmbedFontInfo > fontInfo = Iterables.emptyIterable() ;
             final Map< String, EmbedFontInfo > failedFonts = ImmutableMap.of() ;
             return new FopFontStatus( 
                 fontInfo, 
                 failedFonts
             ) ;
           }
           public Charset getDefaultCharset() {
             return renderingCharset ;
           }
         } ;
       }
 
       public ContentConfiguration getContentConfiguration() {
         return new ContentConfiguration() {
           public File getContentRoot() {
             return contentDirectory;
           }
           public Charset getSourceCharset() {
             return DefaultCharset.SOURCE ;
           }
         } ;
       }
     } ;
 
   }
 
 
   public static DaemonConfiguration createDaemonConfiguration(
       final int httpDaemonPort,
       final File contentDirectory,
       final String styleDirectoryName,
       final Charset renderingCharset
   ) {
     final ProducerConfiguration producerConfiguration = createProducerConfiguration(
         contentDirectory,
         styleDirectoryName,
         false,
         renderingCharset
     ) ;
 
     return new DaemonConfiguration() {
       public int getPort() {
         return httpDaemonPort ;
       }
       public ProducerConfiguration getProducerConfiguration() {
         return producerConfiguration ;
       }
     } ;
 
   }
 
 }
