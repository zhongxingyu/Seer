 /*
  * Copyright 2010 jccastrejon
  *  
  * This file is part of MexADL.
  *
  * MexADL is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MexADL is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with MexADL.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mx.itesm.mexadl;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import mx.itesm.mexadl.util.Util;
 
 import org.apache.commons.io.FileUtils;
 import org.jdom.Document;
 import org.jdom.input.SAXBuilder;
 
 /**
  * The MexAdlAnalyzer class generates MexADL artifacts based on xADL
  * architecture definitions.
  * 
  * @author jccastrejon
  * 
  */
 public class MexAdlAnalyzer {
 
     /**
      * Class logger.
      */
     private static Logger logger = Logger.getLogger(MexAdlAnalyzer.class.getName());
 
     /**
      * JDOM builder to parse an architecture definition document.
      */
     private static SAXBuilder saxBuilder = new SAXBuilder();
 
     /**
      * List of MexAdlProcessors that will be used to analyze xADL architectures.
      */
     private static List<MexAdlProcessor> processors;
 
     static {
         String configProcessors;
         String[] configProcessorsList;
 
         // Load the MexAdlProcessors from the configuration file
         configProcessors = Util.getConfigurationProperty(MexAdlAnalyzer.class, "processors");
         if (configProcessors != null) {
             configProcessorsList = configProcessors.split(",");
             MexAdlAnalyzer.processors = new ArrayList<MexAdlProcessor>(configProcessorsList.length);
             for (String processor : configProcessorsList) {
                 try {
                     MexAdlAnalyzer.processors.add((MexAdlProcessor) Class.forName(processor.trim()).newInstance());
                 } catch (Exception e) {
                     MexAdlAnalyzer.logger.log(Level.WARNING, "Unable to register " + processor + " as MexAdlProcessor",
                             e);
                 }
             }
         }
 
         // Logging configuration
         try {
             LogManager.getLogManager().readConfiguration(
                     MexAdlAnalyzer.class.getClassLoader().getResourceAsStream("mx/itesm/mexadl/logging.properties"));
         } catch (Exception e) {
             System.out.println("Unable to register logging configuration: " + e);
         }
     }
 
     /**
      * Analyze an xADL architecture in order to generate the following MexADL
      * artifacts:
      * <ul>
      * <li>AspectJ architecture description aspect (XPI)</li>
      * <li>AspectJ metrics data aspect</li>
      * </ul>
      * 
      * @param xArch
      *            xADL architecture definition.
      * @param xArchFilePath
      *            Path to the file containing the xADL architecture definition.
      * @throws Exception
      */
     public static void analyzeXArch(final String xArch, final String xArchFilePath) throws Exception {
         Document document;
 
         try {
             // Clean output
             FileUtils.deleteQuietly(Util.getOutputDir(xArchFilePath));
 
             // If there are any processors configured, execute them with the
             // current xADL architecture
             if ((MexAdlAnalyzer.processors != null) && (!MexAdlAnalyzer.processors.isEmpty())) {
                 document = MexAdlAnalyzer.saxBuilder.build(new ByteArrayInputStream(xArch.getBytes()));
                 for (MexAdlProcessor processor : processors) {
                     try {
                         processor.processDocument(document, xArchFilePath);
                     } catch (Exception e) {
                         MexAdlAnalyzer.logger.log(Level.WARNING, "An error ocurred while executing " + processor, e);
                        throw e;
                     }
                 }
             } else {
                 MexAdlAnalyzer.logger.log(Level.WARNING, "No MexAdlProcessors found to analyze the xADL architecture");
             }
         } catch (Exception e) {
             MexAdlAnalyzer.logger.log(Level.WARNING, "Error analyzing xArch: ", e);
             throw e;
         }
     }
 }
