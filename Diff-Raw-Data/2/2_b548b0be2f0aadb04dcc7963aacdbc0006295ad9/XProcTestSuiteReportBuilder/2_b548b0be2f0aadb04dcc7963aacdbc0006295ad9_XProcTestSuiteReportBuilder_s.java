 /*
  * Copyright (C) 2010 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.xproc;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.io.Closeables;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.Serializer.Property;
 import org.trancecode.io.Files;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xproc.XProcTestReportXmlModel.Attributes;
 import org.trancecode.xproc.XProcTestReportXmlModel.Elements;
 
 /**
  * @author Herve Quiroz
  */
 public final class XProcTestSuiteReportBuilder
 {
     private final Multimap<String, TestResult> results = ArrayListMultimap.create();
 
     private static final class TestResult
     {
         private final XProcTestCase test;
         private final QName actualError;
         private final String message;
 
         public TestResult(final XProcTestCase test, final QName actualError, final String message)
         {
             this.test = Preconditions.checkNotNull(test);
             this.actualError = actualError;
             this.message = message;
         }
 
         public boolean failed()
         {
             return actualError != null && !actualError.equals(test.getError());
         }
     }
 
     private static void writeProcessorInformation(final SaxonBuilder builder)
     {
         builder.startElement(Elements.PROCESSOR);
 
         builder.startElement(Elements.NAME);
         builder.text("Tubular");
         builder.endElement();
 
         builder.startElement(Elements.VENDOR);
         builder.text("TranceCode");
         builder.endElement();
 
         builder.startElement(Elements.VENDOR_URI);
         builder.text("http://code.google.com/p/tubular/");
         builder.endElement();
 
         builder.startElement(Elements.VERSION);
         builder.text(Tubular.version());
         builder.endElement();
 
         builder.startElement(Elements.LANGUAGE);
         builder.text("en_US");
         builder.endElement();
 
         builder.startElement(Elements.XPROC_VERSION);
         builder.text(Tubular.xprocVersion());
         builder.endElement();
 
         builder.startElement(Elements.XPATH_VERSION);
         builder.text(Tubular.xpathVersion());
         builder.endElement();
 
         builder.startElement(Elements.PSVI_SUPPORTED);
         builder.text("false");
         builder.endElement();
 
         builder.endElement();
     }
 
     public void pass(final XProcTestCase test, final String message)
     {
         if (test.testSuite() != null)
         {
             results.put(test.testSuite(), new TestResult(test, null, message));
         }
     }
 
     public void fail(final XProcTestCase test, final QName actualError, final String message)
     {
         if (test.testSuite() != null)
         {
             results.put(test.testSuite(), new TestResult(test, actualError, message));
         }
     }
 
     public void write(final File file)
     {
         final Processor processor = new Processor(false);
         final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
         builder.startDocument();
         builder.startElement(Elements.TEST_REPORT);
 
         builder.startElement(Elements.TITLE);
         builder.text("XProc Test Results for Tubular");
         builder.endElement();
 
         builder.startElement(Elements.DATE);
        builder.text(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
         builder.endElement();
 
         writeProcessorInformation(builder);
 
         for (final String testSuite : results.keySet())
         {
             builder.startElement(Elements.TEST_SUITE);
             builder.startElement(Elements.TITLE);
             builder.text(testSuite);
             builder.endElement();
 
             for (final TestResult result : results.get(testSuite))
             {
                 assert result.test.testSuite().equals(testSuite);
                 if (result.failed())
                 {
                     builder.startElement(Elements.FAIL);
                 }
                 else
                 {
                     builder.startElement(Elements.PASS);
                 }
 
                 builder.attribute(Attributes.URI, result.test.url().toString());
                 builder.startElement(Elements.TITLE);
                 builder.text(result.test.getTitle());
                 builder.endElement();
 
                 if (result.actualError != null)
                 {
                     builder.startElement(Elements.ERROR);
                     builder.text(result.actualError.toString());
                     builder.endElement();
                 }
 
                 if (result.message != null)
                 {
                     builder.startElement(Elements.MESSAGE);
                     builder.text(result.message);
                     builder.endElement();
                 }
 
                 if (result.test.getError() != null)
                 {
                     builder.startElement(Elements.EXPECTED);
                     builder.text(result.test.getError().toString());
                     builder.endElement();
 
                     if (result.actualError != null)
                     {
                         builder.startElement(Elements.ACTUAL);
                         builder.text(result.actualError.toString());
                         builder.endElement();
                     }
                 }
 
                 builder.endElement();
             }
             builder.endElement();
         }
 
         builder.endElement();
         builder.endDocument();
 
         // Write report to file
         final OutputStream reportOut = Files.newFileOutputStream(file);
 
         final Serializer serializer = new Serializer();
         serializer.setOutputStream(reportOut);
         serializer.setOutputProperty(Property.INDENT, "yes");
 
         try
         {
             processor.writeXdmValue(builder.getNode(), serializer);
         }
         catch (final SaxonApiException e)
         {
             throw new IllegalStateException(e);
         }
         finally
         {
             Closeables.closeQuietly(reportOut);
         }
     }
 }
