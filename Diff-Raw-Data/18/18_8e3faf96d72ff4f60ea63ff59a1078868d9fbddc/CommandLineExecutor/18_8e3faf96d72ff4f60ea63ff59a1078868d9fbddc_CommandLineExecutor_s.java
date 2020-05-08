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
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import net.sf.saxon.s9api.QName;
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
 
 /**
  * @author Herve Quiroz
  * @author Torsten Knodt
  */
 public final class CommandLineExecutor
 {
     public static void main(final String[] args)
     {
         Logger.getRootLogger().removeAllAppenders();
         Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
         Logger.getRootLogger().setLevel(Level.INFO);
 
         final Options options = new Options();
         final Option portBindingOption = new Option("b", "port-binding", true,
                 "Passes a port binding to the given XProc pipeline");
         final Option optionOption = new Option("o", "option", true, "Passes a option to the given XProc pipeline");
         final Option paramOption = new Option("p", "param", true, "Passes a parameter to the given XProc pipeline");
         final Option librariesOption = new Option("l", "library", true, "XProc pipeline library to load");
         final Option xplOption = new Option("x", "xpl", true, "XProc pipeline to load and run");
         final Option helpOption = new Option("h", "help", false, "Print help");
 
         xplOption.setArgName("uri");
         xplOption.setRequired(true);
         xplOption.setType(URL.class);
         portBindingOption.setArgName("name=uri");
         portBindingOption.setArgs(2);
         portBindingOption.setValueSeparator('=');
         optionOption.setArgName("name=value");
         optionOption.setArgs(2);
         optionOption.setValueSeparator('=');
         paramOption.setArgName("name=value");
         paramOption.setArgs(2);
         paramOption.setValueSeparator('=');
         librariesOption.setArgName("uri");
         librariesOption.setArgs(Option.UNLIMITED_VALUES);
         librariesOption.setType(URL.class);
         options.addOption(portBindingOption);
         options.addOption(optionOption);
         options.addOption(paramOption);
         options.addOption(xplOption);
         options.addOption(librariesOption);
         options.addOption(helpOption);
 
         final GnuParser parser = new GnuParser();
 
         try
         {
             final CommandLine commandLine = parser.parse(options, args);
 
             if (commandLine.hasOption(helpOption.getOpt()))
             {
                 printHelp(options);
                 System.exit(0);
             }
 
             final Configuration configurationPipelineContext = new Configuration();
             final PipelineProcessor pipelineProcessor = new PipelineProcessor(configurationPipelineContext);
             final String[] libraries = commandLine.getOptionValues(librariesOption.getOpt());
 
             if (libraries != null)
             {
                 for (final String library : libraries)
                 {
                     pipelineProcessor.buildPipelineLibrary(new StreamSource(library));
                 }
             }
 
             // configurationPipelineContext.registerStepProcessor(null);
             final String xplValue = commandLine.getOptionValue(xplOption.getOpt());
 
             if (xplValue == null)
             {
                 System.err.println("Required pipeline given using the --" + xplOption.getLongOpt() + " option.");
                 printHelp(options);
                 System.exit(2);
             }
 
             URL xplURL;
 
             try
             {
                 xplURL = new URL(xplValue);
             }
             catch (final MalformedURLException ex)
             {
                 xplURL = null;
             }
 
             final File xplFile = new File(xplValue);
 
             if (xplURL == null)
             {
                 try
                 {
                     xplURL = xplFile.toURI().toURL();
                 }
                 catch (final MalformedURLException ex)
                 {
                 }
             }
 
             Source xplSource = null;
 
             if (xplURL != null)
             {
                 xplSource = new StreamSource(xplURL.toExternalForm());
             }
             else if (xplFile != null)
             {
                 xplSource = new StreamSource(xplFile);
             }
 
             if (xplSource != null)
             {
                 final Pipeline buildPipeline = pipelineProcessor.buildPipeline(xplSource);
                 final RunnablePipeline runnablePipeline = buildPipeline.load();
                 final Properties portBindingProperties = commandLine.getOptionProperties(optionOption.getOpt());
 
                 for (final String portBindingName : portBindingProperties.stringPropertyNames())
                 {
                     final String portBindingValue = portBindingProperties.getProperty(portBindingName);
                     runnablePipeline.setPortBinding(portBindingName, new StreamSource(portBindingValue));
                 }
 
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
                     runnablePipeline.withOption(new QName(paramName), paramValue);
                 }
 
                 final PipelineResult run = runnablePipeline.run();
             }
             else
             {
                 System.err.println("Argument given to option --xpl is neither a URL nor or a file.");
                 printHelp(options);
                 System.exit(3);
             }
         }
         catch (final ParseException ex)
         {
             printHelp(options);
             System.exit(1);
         }
     }
 
     private static void printHelp(final Options options)
     {
         final HelpFormatter helpFormatter = new HelpFormatter();
         final PrintWriter printWriter = new PrintWriter(System.err);
         helpFormatter.printHelp(printWriter, helpFormatter.getWidth(), "java -jar tubular-cli.jar", null, options,
                 helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(), null, true);
         printWriter.flush();
         printWriter.close();
     }

    /**
     * Same as {@link Option#getValues()} but returns an empty array rather than
     * {@code null}.
     * 
     * @see Option#getValues()
     */
    private static String[] getValues(final Option option)
    {
        final String[] values = option.getValues();

        if (values != null)
        {
            return values;
        }

        return new String[0];
    }
 }
