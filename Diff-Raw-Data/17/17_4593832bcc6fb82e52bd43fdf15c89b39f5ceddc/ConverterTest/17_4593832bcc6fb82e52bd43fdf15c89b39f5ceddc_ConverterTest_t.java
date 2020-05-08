 package org.apache.maven.doxia;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.OutputStream;
 import java.io.StringWriter;
 
 import junitx.util.PrivateAccessor;
 
 import org.apache.maven.doxia.wrapper.InputFileWrapper;
 import org.apache.maven.doxia.wrapper.InputReaderWrapper;
 import org.apache.maven.doxia.wrapper.OutputFileWrapper;
 import org.apache.maven.doxia.wrapper.OutputStreamWrapper;
 import org.codehaus.plexus.PlexusTestCase;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.codehaus.plexus.util.WriterFactory;
 
 /**
  * Tests Doxia converter.
  *
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  */
 public class ConverterTest
     extends PlexusTestCase
 {
     private Converter converter;
 
     private boolean formatOutput;
 
     /** {@inheritDoc} */
     protected void setUp()
         throws Exception
     {
         super.setUp();
 
         converter = new DefaultConverter();
 
         formatOutput = Boolean.valueOf( System.getProperty( "format", "false" ) ).booleanValue();
     }
 
     /** {@inheritDoc} */
     protected void tearDown()
         throws Exception
     {
         super.tearDown();
 
         converter = null;
     }
 
     /**
      * @see Converter#getInputFormats()
      */
     public void testGetInputFormats()
     {
         assertNotNull( converter.getInputFormats() );
     }
 
     /**
      * @see Converter#getOutputFormats()
      */
     public void testGetOutputFormats()
     {
         assertNotNull( converter.getOutputFormats() );
     }
 
     /**
      * Input file / output dir
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testFileConverterWithInputFileOutputDir()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/Doxia.htm";
         String from = "xhtml";
         String out = getBasedir() + "/target/unit/";
         String to = "apt";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out, "Doxia.htm.apt" ).exists() );
         assertTrue( new File( out, "Doxia.htm.apt" ).length() != 0 );
 
         FileUtils.deleteDirectory( new File( getBasedir() + "/target/unit/" ) );
     }
 
     /**
      * Input file / output file
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testFileConverterWithInputFileOutputFile()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/Doxia.htm";
         String from = "xhtml";
         String out = getBasedir() + "/target/unit/Doxia.apt";
         String to = "apt";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
         FileUtils.deleteDirectory( new File( getBasedir() + "/target/unit/" ) );
     }
 
     /**
      * Input apt file / output file
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testAptFileConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/apt/test.apt";
         String from = "apt";
         String out = getBasedir() + "/target/unit/file/apt/test.apt.xhtml";
         String to = "xhtml";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
         in = getBasedir() + "/target/unit/file/apt/test.apt.xhtml";
         from = "xhtml";
         out = getBasedir() + "/target/unit/file/apt/test.apt";
         to = "apt";
 
         input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
     }
 
     /**
      * Input confluence file / output file
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testConfluenceFileConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/confluence/test.confluence";
         String from = "confluence";
         String out = getBasedir() + "/target/unit/file/confluence/test.confluence.xhtml";
         String to = "xhtml";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
         in = getBasedir() + "/target/unit/file/confluence/test.confluence.xhtml";
         from = "xhtml";
         out = getBasedir() + "/target/unit/file/confluence/test.confluence";
         to = "confluence";
 
         input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
     }
 
     /**
      * Input docbook file / output file
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testDocbookFileConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/docbook/test.xml";
         String from = "docbook";
         String out = getBasedir() + "/target/unit/file/docbook/test.docbook.xhtml";
         String to = "xhtml";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
          in = getBasedir() + "/target/unit/file/docbook/test.docbook.xhtml";
          from = "xhtml";
          out = getBasedir() + "/target/unit/file/docbook/test.docbook";
          to = "docbook";
 
          input = InputFileWrapper.valueOf( in, from, converter.getInputFormats() );
          output = OutputFileWrapper.valueOf( out, to, converter.getOutputFormats() );
 
          converter.setFormatOutput( formatOutput );
          converter.convert( input, output );
          assertTrue( new File( out ).exists() );
     }
 
     /**
      * Input fml dir / output dir
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testFmlFileConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/fml/test.fml";
         String from = "fml";
         String out = getBasedir() + "/target/unit/file/fml/test.fml.xhtml";
         String to = "xhtml";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
         in = getBasedir() + "/target/unit/file/fml/test.fml.xhtml";
         from = "xhtml";
         out = getBasedir() + "/target/unit/file/fml/test.fml";
         to = "fml";
 
         try
         {
             input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
             output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
             converter.setFormatOutput( formatOutput );
             converter.convert( input, output );
 
             assertFalse( true );
         }
         catch ( UnsupportedFormatException e )
         {
             assertTrue( true );
         }
     }
 
     /**
      * Input twiki file / output file
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testTwikiFileConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/twiki/test.twiki";
         String from = "twiki";
         String out = getBasedir() + "/target/unit/file/twiki/test.twiki.xhtml";
         String to = "xhtml";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
         in = getBasedir() + "/target/unit/file/twiki/test.twiki.xhtml";
         from = "xhtml";
         out = getBasedir() + "/target/unit/file/twiki/test.twiki";
         to = "twiki";
 
         input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         try
         {
             converter.convert( input, output );
         }
         catch ( ConverterException e )
         {
             // The TWiki parser is wrong for *  <pre>some text</pre>
             if ( e.getMessage().indexOf( "Error validating the model" ) == -1 )
             {
                 throw e;
             }
         }
     }
 
     /**
      * Input xdoc file / output dir
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testXdocFileConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/xdoc/test.xml";
         String from = "xdoc";
         String out = getBasedir() + "/target/unit/file/xdoc/test.xdoc.xhtml";
         String to = "xhtml";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
         in = getBasedir() + "/target/unit/file/xdoc/test.xdoc.xhtml";
         from = "xhtml";
         out = getBasedir() + "/target/unit/file/xdoc/test.xdoc";
         to = "xdoc";
 
         input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
     }
 
     /**
      * Input xhtml file / output dir
      *
      * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
      * @throws Exception if any
      */
     public void testXhtmlFileConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/xhtml/test.xhtml";
         String from = "xhtml";
         String out = getBasedir() + "/target/unit/file/xhtml/test.xhtml.xhtml";
         String to = "xhtml";
 
         InputFileWrapper input =
             InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         OutputFileWrapper output =
             OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
 
         in = getBasedir() + "/target/unit/file/xhtml/test.xhtml.xhtml";
         from = "xhtml";
         out = getBasedir() + "/target/unit/file/xhtml/test.xhtml";
         to = "xhtml";
 
         input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
         output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );
 
         converter.setFormatOutput( formatOutput );
         converter.convert( input, output );
         assertTrue( new File( out ).exists() );
         assertTrue( new File( out ).length() != 0 );
     }
 
     /**
      * Input apt reader / output writer
      *
      * @see Converter#convert(InputReaderWrapper, OutputStreamWrapper)
      * @throws Exception if any
      */
     public void testAptWriterConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/apt/test.apt";
         String from = "apt";
         String out = getBasedir() + "/target/unit/writer/apt/test.apt.xhtml";
         String to = "xhtml";
 
         File inFile = new File( in );
         File outFile = new File( out );
         outFile.getParentFile().mkdirs();
 
         OutputStream fo = null;
         try
         {
             fo = new FileOutputStream( outFile );
 
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 
             InputReaderWrapper input =
                 InputReaderWrapper.valueOf( new FileReader( inFile ), from, converter.getInputFormats() );
             OutputStreamWrapper output = OutputStreamWrapper.valueOf( outputStream, to, "UTF-8", converter.getOutputFormats() );
 
             converter.setFormatOutput( formatOutput );
             converter.convert( input, output );
 
             IOUtil.copy( outputStream.toByteArray(), fo );
         }
         finally
         {
             IOUtil.close( fo );
         }
 
         assertTrue( outFile.exists() );
         assertTrue( outFile.length() != 0 );
     }
 
     /**
      * Input confluence reader / output writer
      *
      * @see Converter#convert(InputReaderWrapper, OutputStreamWrapper)
      * @throws Exception if any
      */
     public void testConfluenceWriterConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/confluence/test.confluence";
         String from = "confluence";
         String out = getBasedir() + "/target/unit/writer/confluence/test.confluence.xhtml";
         String to = "xhtml";
 
         File inFile = new File( in );
         File outFile = new File( out );
         outFile.getParentFile().mkdirs();
 
         OutputStream fo = null;
         try
         {
             fo = new FileOutputStream( outFile );
 
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 
             InputReaderWrapper input =
                 InputReaderWrapper.valueOf( new FileReader( inFile ), from, converter.getInputFormats() );
             OutputStreamWrapper output =
                 OutputStreamWrapper.valueOf( outputStream, to, "UTF-8", converter.getOutputFormats() );
 
             converter.setFormatOutput( formatOutput );
             converter.convert( input, output );
 
             IOUtil.copy( outputStream.toByteArray(), fo );
         }
         finally
         {
             IOUtil.close( fo );
         }
 
         assertTrue( outFile.exists() );
         assertTrue( outFile.length() != 0 );
     }
 
     /**
      * Input xdoc (autodetect) reader / output writer
      *
      * @see Converter#convert(InputReaderWrapper, OutputStreamWrapper)
      * @throws Exception if any
      */
     public void testAutoDetectConverter()
         throws Exception
     {
         String in = getBasedir() + "/src/test/resources/unit/xdoc/test.xml";
         String from = null;
         String out = getBasedir() + "/target/unit/writer/apt/test.xdoc.apt";
         String to = "xhtml";
 
         File inFile = new File( in );
         File outFile = new File( out );
         outFile.getParentFile().mkdirs();
 
         FileWriter fw = null;
         try
         {
             fw = new FileWriter( outFile );
 
             StringWriter writer = new StringWriter();
 
             InputFileWrapper input =
                 InputFileWrapper.valueOf( inFile.getAbsolutePath(), from, converter.getInputFormats() );
             OutputFileWrapper output =
                 OutputFileWrapper.valueOf( outFile.getAbsolutePath(), to, converter.getOutputFormats() );
 
             converter.setFormatOutput( formatOutput );
             converter.convert( input, output );
 
             IOUtil.copy( writer.toString(), fw );
 
             assertTrue( outFile.exists() );
             assertTrue( outFile.length() != 0 );
         }
         finally
         {
             IOUtil.close( fw );
         }
 
         in = getBasedir() + "/src/test/resources/unit/apt/test.apt";
         from = null;
         out = getBasedir() + "/target/unit/writer/apt/test.apt.xhtml";
         to = "xhtml";
 
         inFile = new File( in );
         outFile = new File( out );
         outFile.getParentFile().mkdirs();
 
         try
         {
             fw = new FileWriter( outFile );
 
             StringWriter writer = new StringWriter();
 
             InputFileWrapper input =
                 InputFileWrapper.valueOf( inFile.getAbsolutePath(), from, converter.getInputFormats() );
             OutputFileWrapper output =
                 OutputFileWrapper.valueOf( outFile.getAbsolutePath(), to, converter.getOutputFormats() );
 
             converter.setFormatOutput( formatOutput );
             converter.convert( input, output );
 
             IOUtil.copy( writer.toString(), fw );
 
             assertTrue( outFile.exists() );
             assertTrue( outFile.length() != 0 );
         }
         finally
         {
             IOUtil.close( fw );
         }
 
         in = getBasedir() + "/src/test/resources/unit/apt/test.unknown";
         from = null;
         out = getBasedir() + "/target/unit/writer/apt/test.apt.xhtml";
         to = "xhtml";
 
         inFile = new File( in );
         outFile = new File( out );
         outFile.getParentFile().mkdirs();
 
         try
         {
             fw = new FileWriter( outFile );
 
             InputFileWrapper input =
                 InputFileWrapper.valueOf( inFile.getAbsolutePath(), from, converter.getInputFormats() );
             OutputFileWrapper output =
                 OutputFileWrapper.valueOf( outFile.getAbsolutePath(), to, converter.getOutputFormats() );
 
             converter.setFormatOutput( formatOutput );
             converter.convert( input, output );
 
             assertFalse( true );
         }
         catch ( UnsupportedOperationException e )
         {
             assertTrue( true );
         }
         finally
         {
             IOUtil.close( fw );
         }
     }
 
     private String autoDetectEncoding( File f )
         throws Throwable
     {
         return (String) PrivateAccessor.invoke( DefaultConverter.class, "autoDetectEncoding",
                                          new Class[] { File.class }, new Object[] { f } );
     }
 
     private String autoDetectEncoding( String filename )
         throws Throwable
     {
         return autoDetectEncoding( new File( getBasedir() + "/src/test/resources/unit/" + filename ) );
     }
 
     /**
     * Test {@link DefaultConverter#autoDetectEncoding(File)}
      *
      * @throws Throwable
      */
     public void testAutodetectEncoding()
         throws Throwable
     {
         assertEquals( autoDetectEncoding( "apt/test.apt" ), "ISO-8859-1" );
         assertEquals( autoDetectEncoding( "confluence/test.confluence" ), "ISO-8859-1" );
         assertEquals( autoDetectEncoding( "docbook/test.xml" ), "UTF-8" );
         assertEquals( autoDetectEncoding( "fml/test.fml" ), "ISO-8859-1" );
         assertEquals( autoDetectEncoding( "twiki/test.twiki" ), "ISO-8859-1" );
         assertEquals( autoDetectEncoding( "xhtml/test.xhtml" ), "UTF-8" );
     }
 
     private String autoDetectFormat( File f, String encoding )
         throws Throwable
     {
         return (String) PrivateAccessor.invoke( DefaultConverter.class, "autoDetectFormat", new Class[] { File.class,
                                                 String.class }, new Object[] { f, encoding } );
     }
 
     private String autoDetectFormat( String filename, String encoding )
         throws Throwable
     {
         return autoDetectFormat( new File( getBasedir() + "/src/test/resources/unit/" + filename ), encoding );
     }
 
     /**
     * Test {@link DefaultConverter#autoDetectFormat(File,String)}
      *
      * @throws Throwable
      */
     public void testAutodetectFormat()
         throws Throwable
     {
         assertEquals( autoDetectFormat( "apt/test.apt", "UTF-8" ), "apt" );
 
         try
         {
             autoDetectFormat( "apt/test.unknown", "UTF-8" );
             fail();
         }
         catch ( UnsupportedOperationException e )
         {
             assertTrue( true );
         }
 
         assertEquals( autoDetectFormat( "confluence/test.confluence", "UTF-8" ), "confluence" );
         assertEquals( autoDetectFormat( "docbook/test.xml", "UTF-8" ), "docbook" );
         assertEquals( autoDetectFormat( "fml/test.fml", "UTF-8" ), "fml" );
         assertEquals( autoDetectFormat( "twiki/test.twiki", "UTF-8" ), "twiki" );
         assertEquals( autoDetectFormat( "xhtml/test.xhtml", "UTF-8" ), "xhtml" );
     }
 }
