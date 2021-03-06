 /*
  * The Apache Software License, Version 1.1
  *
  *
  * Copyright (c) 1999 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/)."
  *    Alternately, this acknowledgment may appear in the software itself,
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Xalan" and "Apache Software Foundation" must
  *    not be used to endorse or promote products derived from this
  *    software without prior written permission. For written
  *    permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    nor may "Apache" appear in their name, without prior written
  *    permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Apache Software Foundation and was
  * originally based on software copyright (c) 1999, Lotus
  * Development Corporation., http://www.lotus.com.  For more
  * information on the Apache Software Foundation, please see
  * <http://www.apache.org/>.
  */
 package org.apache.xalan.xslt;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.BufferedInputStream;
 import java.io.ObjectOutputStream;
 import java.io.ObjectInputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringReader;
 
 import java.lang.reflect.Constructor;
 
 import java.util.TooManyListenersException;
 import java.util.Vector;
 import java.util.Properties;
 import java.util.Enumeration;
 
 import java.util.Date;
 
 // Needed Xalan classes
 import org.apache.xalan.res.XSLMessages;
 
 import org.apache.xalan.processor.XSLProcessorVersion;
 
 import org.apache.xalan.res.XSLTErrorResources;
 
 import org.apache.xalan.templates.Constants;
 import org.apache.xalan.templates.ElemTemplateElement;
 import org.apache.xalan.templates.StylesheetRoot;
 
 import org.apache.xalan.transformer.TransformerImpl;
 import org.apache.xalan.processor.StylesheetProcessor;
 
 import org.apache.xalan.trace.PrintTraceListener;
 import org.apache.xalan.trace.TraceListener;
 import org.apache.xalan.trace.TraceManager;
 
 // Needed TRaX classes
 import trax.Result;
 import trax.Processor;
 import trax.ProcessorFactoryException;
 import trax.Transformer;
 import trax.Templates;
 
 // Needed SAX classes
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.ParserAdapter;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 // Needed DOM classes
 import org.w3c.dom.Node;
 import org.w3c.dom.Document;
 
 // Needed Serializer classes
 import serialize.OutputFormat;
 import serialize.Serializer;
 import serialize.SerializerFactory;
 
 
 /**
  * <meta name="usage" content="general"/>
  * The main() method handles the Xalan command-line interface.
  */
 public class Process
 {
   /**
    * Prints argument options.
    */
   protected static void printArgOptions(XSLTErrorResources resbundle)
   {
     System.out.println(resbundle.getString("xslProc_option")); //"xslproc options: ");
     System.out.println(resbundle.getString("optionIN")); //"    -IN inputXMLURL");
     System.out.println(resbundle.getString("optionXSL")); //"   [-XSL XSLTransformationURL]");
     System.out.println(resbundle.getString("optionOUT")); //"   [-OUT outputFileName]");
     // System.out.println(resbundle.getString("optionE")); //"   [-E (Do not expand entity refs)]");
     System.out.println(resbundle.getString("optionV")); //"   [-V (Version info)]");
     System.out.println(resbundle.getString("optionQC")); //"   [-QC (Quiet Pattern Conflicts Warnings)]");
     System.out.println(resbundle.getString("optionQ")); //"   [-Q  (Quiet Mode)]");
     System.out.println(resbundle.getString("optionTT")); //"   [-TT (Trace the templates as they are being called.)]");
     System.out.println(resbundle.getString("optionTG")); //"   [-TG (Trace each generation event.)]");
     System.out.println(resbundle.getString("optionTS")); //"   [-TS (Trace each selection event.)]");
     System.out.println(resbundle.getString("optionTTC")); //"   [-TTC (Trace the template children as they are being processed.)]");
     System.out.println(resbundle.getString("optionTCLASS")); //"   [-TCLASS (TraceListener class for trace extensions.)]");
     System.out.println(resbundle.getString("optionVALIDATE")); //"   [-VALIDATE (Set whether validation occurs.  Validation is off by default.)]");
     System.out.println(resbundle.getString("optionEDUMP")); //"   [-EDUMP {optional filename} (Do stackdump on error.)]");
     System.out.println(resbundle.getString("optionXML")); //"   [-XML (Use XML formatter and add XML header.)]");
     System.out.println(resbundle.getString("optionTEXT")); //"   [-TEXT (Use simple Text formatter.)]");
     System.out.println(resbundle.getString("optionHTML")); //"   [-HTML (Use HTML formatter.)]");
     System.out.println(resbundle.getString("optionPARAM")); //"   [-PARAM name expression (Set a stylesheet parameter)]");
     System.out.println("[-MEDIA use media attribute to find stylesheet associated with a document.]"); //"   [-PARAM name expression (Set a stylesheet parameter)]");
   }
   
   static String XSLT_PROPERTIES = "/org/apache/xalan/res/XSLTInfo.properties";
 
   /**
    * Command line interfact to transform the XML according to
    * the instructions found in the XSL stylesheet.
    *  <p>To set stylesheet parameters from the command line, use -PARAM name expression. If
    *  you want to set the parameter to a string value, enclose the string in single quotes (') to
    */
   public static void main( String argv[] )
   {
     Runtime.getRuntime().traceMethodCalls(false); // turns Java tracing off
     boolean doStackDumpOnError = false;
     boolean setQuietMode = false;
     boolean doDiag = false;
 
     // Runtime.getRuntime().traceMethodCalls(false);
     // Runtime.getRuntime().traceInstructions(false);
     /**
     * The default diagnostic writer...
     */
     java.io.PrintWriter diagnosticsWriter = new PrintWriter(System.err, true);
     java.io.PrintWriter dumpWriter = diagnosticsWriter;
 
     XSLTErrorResources resbundle = (XSLTErrorResources)(XSLMessages.loadResourceBundle(Constants.ERROR_RESOURCES));
     // loadPropertyFileToSystem(XSLT_PROPERTIES);
 
     if(argv.length < 1)
     {
       printArgOptions(resbundle);
     }
     else
     {
       Processor processor;
       try
       {
         processor = Processor.newInstance("xslt");
       }
       catch(ProcessorFactoryException pfe)
       {
         if(doStackDumpOnError)
           pfe.printStackTrace(dumpWriter);
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         processor = null; // shut up compiler
         System.exit(-1);
       }
       boolean formatOutput = false;
       String inFileName = null;
       String outFileName = null;
       String dumpFileName = null;
       String xslFileName = null;
       String compiledStylesheetFileNameOut = null;
       String compiledStylesheetFileNameIn = null;
       String treedumpFileName = null;
       PrintTraceListener tracer = null;
       String outputType = null;
       String media = null;
       Vector params = new Vector();
       boolean quietConflictWarnings = false;
 
       for (int i = 0;  i < argv.length;  i ++)
       {
         if("-TT".equalsIgnoreCase(argv[i]))
         {
           if(null == tracer)
             tracer = new PrintTraceListener(diagnosticsWriter);
           tracer.m_traceTemplates = true;
           // processor.setTraceTemplates(true);
         }
         else if("-TG".equalsIgnoreCase(argv[i]))
         {
           if(null == tracer)
             tracer = new PrintTraceListener(diagnosticsWriter);
           tracer.m_traceGeneration = true;
           // processor.setTraceSelect(true);
         }
         else if("-TS".equalsIgnoreCase(argv[i]))
         {
           if(null == tracer)
             tracer = new PrintTraceListener(diagnosticsWriter);
           tracer.m_traceSelection = true;
           // processor.setTraceTemplates(true);
         }
         else if("-TTC".equalsIgnoreCase(argv[i]))
         {
           if(null == tracer)
             tracer = new PrintTraceListener(diagnosticsWriter);
           tracer.m_traceElements = true;
           // processor.setTraceTemplateChildren(true);
         }
         else if ("-INDENT".equalsIgnoreCase(argv[i]))
         {
           int indentAmount;
           if(((i+1) < argv.length) && (argv[i+1].charAt(0) != '-'))
           {
             indentAmount = Integer.parseInt( argv[++i] );
           }
           else
           {
             indentAmount = 0;
           }
           // TBD:
           // xmlProcessorLiaison.setIndent(indentAmount);
         }
         else if ("-IN".equalsIgnoreCase(argv[i]))
         {
           if ( i+1 < argv.length)
             inFileName = argv[++i];
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-IN"})); //"Missing argument for);
 
         }
         else if ("-MEDIA".equalsIgnoreCase(argv[i]))
         {
           if ( i+1 < argv.length)
             media = argv[++i];
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-MEDIA"})); //"Missing argument for);
 
         }
 
         else if ("-OUT".equalsIgnoreCase(argv[i]))
         {
           if ( i+1 < argv.length)
             outFileName = argv[++i];
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-OUT"})); //"Missing argument for);
 
         }
         else if ("-XSL".equalsIgnoreCase(argv[i]))
         {
           if ( i+1 < argv.length)
             xslFileName = argv[++i];
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-XSL"})); //"Missing argument for);
 
         }
         else if("-LXCIN".equalsIgnoreCase(argv[i]))
         {
           if ( i+1 < argv.length)
             compiledStylesheetFileNameIn = argv[++i];
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-LXCIN"})); //"Missing argument for);
 
         }
         else if("-LXCOUT".equalsIgnoreCase(argv[i]))
         {
           if ( i+1 < argv.length)
             compiledStylesheetFileNameOut = argv[++i];
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-LXCOUT"})); //"Missing argument for);
 
         }
         else if ("-PARAM".equalsIgnoreCase(argv[i]))
         {
           if ( i+2 < argv.length)
           {
             String name = argv[++i];
             params.addElement(name);
             String expression = argv[++i];
             params.addElement(expression);
           }
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-PARAM"})); //"Missing argument for);
 
         }
         else if ("-treedump".equalsIgnoreCase(argv[i]))
         {
           if ( i+1 < argv.length)
             treedumpFileName = argv[++i];
           else
             System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_MISSING_ARG_FOR_OPTION, new Object[] {"-treedump"})); //"Missing argument for);
 
         }
         else if("-F".equalsIgnoreCase(argv[i]))
         {
           formatOutput = true;
         }
         else if("-E".equalsIgnoreCase(argv[i]))
         {
           // TBD:
           // xmlProcessorLiaison.setShouldExpandEntityRefs(false);
         }
         else if("-V".equalsIgnoreCase(argv[i]))
         {
           diagnosticsWriter.println(resbundle.getString("version") //">>>>>>> Xalan Version "
                                     +XSLProcessorVersion.S_VERSION+", "+
                                     /* xmlProcessorLiaison.getParserDescription()+ */
                                     resbundle.getString("version2"));// "<<<<<<<");
         }
         else if("-QC".equalsIgnoreCase(argv[i]))
         {
           quietConflictWarnings = true;
         }
         else if("-Q".equalsIgnoreCase(argv[i]))
         {
           setQuietMode = true;
         }
         else if("-VALIDATE".equalsIgnoreCase(argv[i]))
         {
           String shouldValidate;
           if(((i+1) < argv.length) && (argv[i+1].charAt(0) != '-'))
           {
             shouldValidate = argv[++i];
           }
           else
           {
             shouldValidate = "yes";
           }
 
           // xmlProcessorLiaison.setUseValidation(shouldValidate.equalsIgnoreCase("yes"));
         }
         else if("-DIAG".equalsIgnoreCase(argv[i]))
         {
           doDiag = true;
         }
         else if("-XML".equalsIgnoreCase(argv[i]))
         {
           outputType = "xml";
         }
         else if("-TEXT".equalsIgnoreCase(argv[i]))
         {
           outputType = "text";
         }
         else if("-HTML".equalsIgnoreCase(argv[i]))
         {
           outputType = "html";
         }
         else if("-EDUMP".equalsIgnoreCase(argv[i]))
         {
           doStackDumpOnError = true;
           if(((i+1) < argv.length) && (argv[i+1].charAt(0) != '-'))
           {
             dumpFileName = argv[++i];
           }
         }
         else
           System.err.println(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_OPTION, new Object[] {argv[i]})); //"Invalid argument:);
 
       }
 
       // The main XSL transformation occurs here!
       try
       {
         long start = System.currentTimeMillis();
         if(null != dumpFileName)
         {
           dumpWriter = new PrintWriter( new FileWriter(dumpFileName) );
         }
 
         Templates stylesheet = null;
 
         if(null != compiledStylesheetFileNameIn)
         {
           try
           {
             FileInputStream fileInputStream
               = new FileInputStream(compiledStylesheetFileNameIn);
             ObjectInputStream objectInput
               = new ObjectInputStream(fileInputStream);
             stylesheet = (Templates)objectInput.readObject();
             objectInput.close();
           }
           catch(java.io.UnsupportedEncodingException uee)
           {
             stylesheet = null;
             // diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_ENCODING_NOT_SUPPORTED, new Object[] {stylesheet.getOutputEncoding()})); //"Encoding not supported: "+stylesheet.m_encoding);
             // throw new XSLProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_ENCODING_NOT_SUPPORTED, new Object[] {stylesheet.getOutputEncoding()})); //"Encoding not supported: "+stylesheet.m_encoding);
           }
         }
         else if(null != xslFileName)
         {
           stylesheet = processor.process(new InputSource(xslFileName));
         }
 
         PrintWriter resultWriter;
 
         OutputStream outputStream = (null != outFileName) ? 
                                     new FileOutputStream(outFileName) :
                                     (OutputStream)System.out;
 
         // Did they pass in a stylesheet, or should we get it from the 
         // document?
         if(null == stylesheet)
         {
           InputSource[] sources = processor.getAssociatedStylesheets(new InputSource(inFileName),
                                                                      media, null, null);
           if(null != sources)
             stylesheet = processor.processMultiple(sources);
           else
             throw new SAXException("No stylesheet found for media: "+media);
         }
 
         if(null != stylesheet)
         {
           if(null != compiledStylesheetFileNameOut)
           {
             FileOutputStream compiledStylesheetOutputStream
               = new FileOutputStream(compiledStylesheetFileNameOut);
             ObjectOutputStream compiledStylesheetOutput
               = new ObjectOutputStream(compiledStylesheetOutputStream);
 
             compiledStylesheetOutput.writeObject(stylesheet);
           }
 
           Transformer transformer = stylesheet.newTransformer();
           
           // Override the output format?
           if(null != outputType)
           {
             OutputFormat of = stylesheet.getOutputFormat();
             of.setMethod(outputType);
             transformer.setOutputFormat(of);
           }
           
           if(transformer instanceof TransformerImpl)
           {
             TransformerImpl impl = ((TransformerImpl)transformer);
             TraceManager tm = impl.getTraceManager();
             
             if(null != tracer)
               tm.addTraceListener(tracer);
             
             impl.setQuietConflictWarnings(quietConflictWarnings);
             // impl.setDiagnosticsOutput( setQuietMode ? null : diagnosticsWriter );
 
           }
           int nParams = params.size();
           for(int i = 0; i < nParams; i+=2)
             transformer.setParameter((String)params.elementAt(i), null,
                                      (String)params.elementAt(i+1));
           
           if(null != inFileName)
           {
             transformer.transform(new InputSource(inFileName), new Result(outputStream));
           }
           else
           {
             StringReader reader = new StringReader("<?xml version=\"1.0\"?> <doc/>");
             transformer.transform(new InputSource(reader), new Result(outputStream));
           }
         }
         else
         {
           diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
           System.exit(-1);
         }
         long stop = System.currentTimeMillis();
         long millisecondsDuration = stop - start;
         if(doDiag)
           diagnosticsWriter.println("\n\n========\nTransform of "+inFileName+" via "+xslFileName+" took "+millisecondsDuration+" ms");
       }
       catch(SAXParseException spe)
       {
         Exception containedException = spe.getException();
         if(null != containedException)
         {
           if(doStackDumpOnError)
           {
             containedException = spe;
             while(containedException instanceof SAXException)
             {
               containedException.printStackTrace(dumpWriter);
               containedException = ((SAXException)containedException).getException();
               dumpWriter.println("====================");
             }
             if(null != containedException)
             {
               containedException.printStackTrace(dumpWriter);
               dumpWriter.println("====================");
            }
           }
           // else
           //  System.out.println("Error! "+se.getMessage());
           diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         }
         else
         {
           if(doStackDumpOnError)
             spe.printStackTrace(dumpWriter);
           // else
           //  System.out.println("Error! "+se.getMessage());
           diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null) ); //"XSL Process was not successful.");
         }
         diagnosticsWriter.println(" ID: "+spe.getSystemId()
                                   +" Line #"+spe.getLineNumber()
                                   +" Column #"+spe.getColumnNumber());
         System.exit(-1);
       }      
       catch(TooManyListenersException tmle)
       {
         if(doStackDumpOnError)
           tmle.printStackTrace(dumpWriter);
         // else
         //  System.out.println("Error! "+se.getMessage());
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         System.exit(-1);
       }
 
       catch(SAXException se)
       {
         Exception containedException = se.getException();
         if(null != containedException)
         {
           if(doStackDumpOnError)
             containedException.printStackTrace(dumpWriter);
           // else
           //  System.out.println("Error! "+se.getMessage());
           diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         }
         else
         {
           if(doStackDumpOnError)
             se.printStackTrace(dumpWriter);
           // else
           //  System.out.println("Error! "+se.getMessage());
           diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null) ); //"XSL Process was not successful.");
         }
         System.exit(-1);
       }
       catch(ClassNotFoundException cnfe)
       {
         if(doStackDumpOnError)
           cnfe.printStackTrace(dumpWriter);
         else
           System.out.println("Error! "+cnfe.getMessage());
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         System.exit(-1);
       }
       catch(IOException ioe)
       {
         if(doStackDumpOnError)
           ioe.printStackTrace(dumpWriter);
         else
           System.out.println("Error! "+ioe.getMessage());
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         System.exit(-1);
       }
       catch(RuntimeException rte)
       {
         if(doStackDumpOnError)
           rte.printStackTrace(dumpWriter);
         else
           System.out.println("Error! "+rte.getMessage());
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         System.exit(-1);
       }
       catch(Exception e)
       {
         if(doStackDumpOnError)
           e.printStackTrace(dumpWriter);
         else
           System.out.println("Error! "+e.getMessage());
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         System.exit(-1);
       }
       catch(Error err)
       {
         if(doStackDumpOnError)
           err.printStackTrace(dumpWriter);
         else
           System.out.println("Error! "+err.getMessage());
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         System.exit(-1);
       }
       catch(Throwable thr)
       {
         if(doStackDumpOnError)
           thr.printStackTrace(dumpWriter);
        else
           System.out.println("Error! "+thr.getMessage());
         diagnosticsWriter.println(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUCCESSFUL, null)); //"XSL Process was not successful.");
         System.exit(-1);
       }
 
       if(null != dumpFileName)
       {
         dumpWriter.close();
       }
       if(null != diagnosticsWriter)
       {
         // diagnosticsWriter.close();
       }
       // if(!setQuietMode)
       //  diagnosticsWriter.println(resbundle.getString("xsldone")); //"Xalan: done");
       // else
         diagnosticsWriter.println(""); //"Xalan: done");
     }
   }
 }
