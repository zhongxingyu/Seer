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
 package org.trancecode.xproc.cli;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.URIResolver;
 
 import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.XdmNode;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.trancecode.xproc.Configuration;
 import org.trancecode.xproc.Pipeline;
 import org.trancecode.xproc.PipelineProcessor;
 import org.trancecode.xproc.PipelineResult;
 import org.trancecode.xproc.RunnablePipeline;
 import org.trancecode.xproc.Tubular;
 
 /**
  * @author Herve Quiroz
  * @author Torsten Knodt
  */
 public final class CommandLineExecutor
 {
     private final Options options;
     private final Option helpOption;
     private final Option librariesOption;
     private final Option optionOption;
     private final Option paramOption;
     private final Option portBindingOption;
     private final Option primaryInputPortOption;
     private final Option primaryOutputPortOption;
     private final Option verboseOption;
     private final Option versionOption;
     private final Option xplOption;
 
     protected CommandLineExecutor()
     {
         options = new Options();
 
         helpOption = new Option("h", "help", false, "Print help");
         options.addOption(helpOption);
 
         librariesOption = new Option("l", "library", true, "XProc pipeline library to load");
         librariesOption.setArgName("uri");
         librariesOption.setArgs(Option.UNLIMITED_VALUES);
         librariesOption.setType(URL.class);
         options.addOption(librariesOption);
 
         optionOption = new Option("o", "option", true, "Passes a option to the given XProc pipeline");
         optionOption.setArgName("name=value");
         optionOption.setArgs(2);
         optionOption.setValueSeparator('=');
         options.addOption(optionOption);
 
         paramOption = new Option("p", "param", true, "Passes a parameter to the given XProc pipeline");
         paramOption.setArgName("name=value");
         paramOption.setArgs(2);
         paramOption.setValueSeparator('=');
         options.addOption(paramOption);
 
         portBindingOption = new Option("b", "port-binding", true, "Passes a port binding to the given XProc pipeline");
         portBindingOption.setArgName("name=uri");
         portBindingOption.setArgs(2);
         portBindingOption.setValueSeparator('=');
         options.addOption(portBindingOption);
 
         primaryInputPortOption = new Option("i", "input-port", true, "Passes the primary input port");
         primaryInputPortOption.setArgName("name=uri");
         primaryInputPortOption.setArgs(2);
         primaryInputPortOption.setValueSeparator('=');
         options.addOption(primaryInputPortOption);
 
         primaryOutputPortOption = new Option("r", "output-port", true, "Passes the primary output port");
         primaryOutputPortOption.setArgName("name=uri");
         primaryOutputPortOption.setArgs(2);
         primaryOutputPortOption.setValueSeparator('=');
         options.addOption(primaryOutputPortOption);
 
         verboseOption = new Option("v", "verbose", false, "Display more information");
         options.addOption(verboseOption);
 
         versionOption = new Option("V", "version", false, "Print version and exit");
         options.addOption(versionOption);
 
         xplOption = new Option("x", "xpl", true, "XProc pipeline to load and run");
         xplOption.setArgName("uri");
         xplOption.setRequired(true);
         xplOption.setType(URL.class);
         options.addOption(xplOption);
     }
 
     protected int execute(final String[] args, final InputStream stdin, final PrintStream stdout,
             final PrintStream stderr)
     {
         final GnuParser parser = new GnuParser();
 
         try
         {
             final CommandLine commandLine = parser.parse(options, args);
 
             if (commandLine.hasOption(helpOption.getOpt()))
             {
                 printHelp(stderr);
                 return 0;
             }
 
             if (commandLine.hasOption(versionOption.getOpt()))
             {
                 stderr.println(Tubular.productInformation());
                 return 0;
             }
 
             if (commandLine.hasOption(verboseOption.getOpt()))
             {
                 Logger.getRootLogger().setLevel(Level.DEBUG);
             }
 
             final Configuration configurationPipelineContext = new Configuration();
             final URIResolver uriResolver = configurationPipelineContext.getUriResolver();
             final PipelineProcessor pipelineProcessor = new PipelineProcessor(configurationPipelineContext);
             final String[] libraries = commandLine.getOptionValues(librariesOption.getOpt());
 
             if (libraries != null)
             {
                 for (final String library : libraries)
                 {
                     // FIXME this will not really have any effect has the parsed
                     // library is returned and the pipeline processor stays
                     // unchanged
                     pipelineProcessor.buildPipelineLibrary(newSource(uriResolver, library,
                             "Cannot read library from %s", library));
                 }
             }
 
             // configurationPipelineContext.registerStepProcessor(null);
             final String xplValue = commandLine.getOptionValue(xplOption.getOpt());
 
             if (xplValue == null)
             {
                 stderr.println("Required pipeline given using the --" + xplOption.getLongOpt() + " option.");
                 printHelp(stderr);
                 return 2;
             }
             else
             {
                 final Source xplSource = newSource(uriResolver, xplValue, "Cannot read pipeline from %s", xplValue);
 
                 if (xplSource != null)
                 {
                     final Pipeline buildPipeline = pipelineProcessor.buildPipeline(xplSource);
                     final RunnablePipeline runnablePipeline = buildPipeline.load();
 
                     final Properties portBindingProperties = commandLine.getOptionProperties(optionOption.getOpt());
                     for (final String portBindingName : portBindingProperties.stringPropertyNames())
                     {
                         final String portBindingValue = portBindingProperties.getProperty(portBindingName);
                         runnablePipeline.setPortBinding(
                                 portBindingName,
                                 newSource(uriResolver, portBindingValue, "Cannot bind port to resource from %s",
                                         portBindingValue));
                     }
 
                     final String primaryInputPortValue = commandLine.getOptionValue(primaryInputPortOption.getOpt());
                     if (primaryInputPortValue != null)
                     {
                         final Source primaryInputSource = newSource(uriResolver, primaryInputPortValue,
                                 "Cannot bind port to resource from %s", primaryInputPortValue);
                         if (primaryInputSource != null)
                         {
                             runnablePipeline.setPortBinding(runnablePipeline.getPipeline().getPrimaryInputPort()
                                     .getPortName(), primaryInputSource);
                         }
                     }
 
                     final String primaryOutputPortValue = commandLine.getOptionValue(primaryOutputPortOption.getOpt());
                     /*
                      * @todo TK: final Result resolve =
                      * configurationPipelineContext
                      * .getProcessor().getUnderlyingConfiguration
                      * ().getOutputURIResolver().resolve(primaryOutputPortValue,
                      * null);
                      */
 
                     final Properties optionProperties = commandLine.getOptionProperties(optionOption.getOpt());
                     for (final String optionName : optionProperties.stringPropertyNames())
                     {
                         final String optionValue = optionProperties.getProperty(optionName);
                         runnablePipeline.withOption(new QName(optionName), optionValue);
                     }
 
                     final Properties paramProperties = commandLine.getOptionProperties(paramOption.getOpt());
                     for (final String paramName : paramProperties.stringPropertyNames())
                     {
                         final String paramValue = paramProperties.getProperty(paramName);
                         runnablePipeline.withParam(new QName(paramName), paramValue);
                     }
 
                     final PipelineResult pipelineResult = runnablePipeline.run();
                    final Serializer serializer = new Serializer();
                    serializer.setOutputStream(stdout); // TODO Use primary
                                                        // output port
                                                        // binding option
                    for (final XdmNode xdmNode : pipelineResult.readNodes(pipelineResult.getPipeline()
                            .getPrimaryOutputPort().getPortName()))
                     {
                        try
                        {
                            configurationPipelineContext.getProcessor().writeXdmValue(xdmNode, serializer);
                        }
                        catch (final SaxonApiException ex)
                         {
                            throw new IllegalStateException("error serializing node from output port", ex);
                         }
                     }
                    stdout.println();
                 }
                 else
                 {
                     stderr.println("Argument given to option --xpl is neither a URL nor or a file.");
                     printHelp(stderr);
                     return 3;
                 }
             }
         }
         catch (final ParseException ex)
         {
             printHelp(stderr);
             return 1;
         }
         return 0;
     }
 
     private static Source newSource(final URIResolver uriResolver, final String uri, final String errorMessage,
             final Object... args)
     {
         try
         {
             return uriResolver.resolve(uri, "");
         }
         catch (final TransformerException resolverError)
         {
             try
             {
                 return uriResolver.resolve(new File(uri).toURI().toString(), "");
             }
             catch (final TransformerException fileError)
             {
                 throw new IllegalArgumentException(String.format(errorMessage, args), resolverError);
             }
         }
     }
 
     private void printHelp(final PrintStream destination)
     {
         final HelpFormatter helpFormatter = new HelpFormatter();
         final PrintWriter printWriter = new PrintWriter(destination);
         try
         {
             helpFormatter.printHelp(printWriter, helpFormatter.getWidth(), "java -jar tubular-cli.jar", null, options,
                     helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(), null, true);
         }
         finally
         {
             printWriter.flush();
             printWriter.close();
         }
     }
 
     public static void main(final String[] args)
     {
         Logger.getRootLogger().removeAllAppenders();
         Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
         Logger.getRootLogger().setLevel(Level.INFO);
 
         System.exit(new CommandLineExecutor().execute(args, System.in, System.out, System.err));
     }
 }
