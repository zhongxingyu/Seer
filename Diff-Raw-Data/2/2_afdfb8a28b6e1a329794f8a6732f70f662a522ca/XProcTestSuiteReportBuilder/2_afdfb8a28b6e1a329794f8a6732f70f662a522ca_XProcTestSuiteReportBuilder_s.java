 /*
  * Copyright (C) 2010 Herve Quiroz
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
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.Serializer.Property;
 import org.trancecode.TcAssert.XdmNodeCompareAssertionError;
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
 
     public static final class TestResult
     {
         private final XProcTestCase test;
         private final Throwable error;
 
         public TestResult(final XProcTestCase test, final Throwable error)
         {
             this.test = Preconditions.checkNotNull(test);
             this.error = error;
         }
 
         public boolean failed()
         {
             return (error != null
                     && !(error instanceof XProcException && ((XProcException) error).getName().equals(test.getError()))) ||
                    (error == null && test.getError() != null);
         }
     }
 
     private static void writeProcessorInformation(final SaxonBuilder builder)
     {
         builder.startElement(Elements.PROCESSOR);
 
         builder.startElement(Elements.NAME);
         builder.text(Tubular.productName());
         builder.endElement();
 
         builder.startElement(Elements.VENDOR);
         builder.text(Tubular.vendor());
         builder.endElement();
 
         builder.startElement(Elements.VENDOR_URI);
         builder.text(Tubular.vendorUri());
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
 
     public TestResult result(final XProcTestCase test, final Throwable error)
     {
         final TestResult result = new TestResult(test, error);
         if (test.testSuite() != null)
         {
             results.put(test.testSuite(), result);
         }
 
         return result;
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
         builder.text(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
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
 
                 if (result.error != null)
                 {
                     builder.startElement(Elements.ERROR);
 
                     if (result.test.getError() != null)
                     {
                         builder.attribute(Attributes.EXPECTED, result.test.getError().toString());
                     }
 
                     if (result.error instanceof XProcException)
                     {
                        builder.text(((XProcException) result.error).getName().getClarkName());
                     }
                     else
                     {
                         builder.text(result.error.getClass().getSimpleName());
                     }
                     builder.endElement();
 
                     if (result.error instanceof XdmNodeCompareAssertionError)
                     {
                         final XdmNodeCompareAssertionError comparisonError = (XdmNodeCompareAssertionError) result.error;
                         builder.startElement(Elements.EXPECTED);
                         builder.text(comparisonError.expected().toString());
                         builder.endElement();
                         builder.startElement(Elements.ACTUAL);
                         builder.text(comparisonError.actual().toString());
                         builder.endElement();
                     }
                 }
 
                 if (result.error != null && !(result.error instanceof XdmNodeCompareAssertionError))
                 {
                     builder.startElement(Elements.MESSAGE);
                     if (result.error.getMessage() != null)
                     {
                         builder.text(result.error.getMessage());
                     }
                     else
                     {
                         builder.text(result.error.getClass().getName());
                     }
                     builder.endElement();
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
