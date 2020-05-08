 /*
  * LABPipe - Natural Language Processing Pipeline for Bulgarian
  * Copyright (C) 2011  Aleksandar Savkov
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bultreebank.labpipe.main;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.ParserConfigurationException;
 import org.bultreebank.labpipe.converters.Converter;
 import org.bultreebank.labpipe.exceptions.ClarkConfigurationException;
 import org.bultreebank.labpipe.exceptions.IncorrectInputException;
 import org.bultreebank.labpipe.exceptions.IncorrectOutputException;
 import org.bultreebank.labpipe.exceptions.IncorrectParameterValueException;
 import org.bultreebank.labpipe.exceptions.MissingContentException;
 import org.bultreebank.labpipe.exceptions.SVMTConnectionExceptoin;
 import org.bultreebank.labpipe.tools.ProcessingLine;
 import org.bultreebank.labpipe.utils.CommandLineUtils;
 import org.bultreebank.labpipe.utils.Configuration;
 import org.bultreebank.labpipe.utils.Misc;
 import org.bultreebank.labpipe.utils.ServiceConstants;
 import org.maltparser.core.exception.MaltChainedException;
 import org.xml.sax.SAXException;
 
 /**
  *
  ** @author Aleksandar Savkov
  */
 public class Main {
 
     private static final Logger logger = Logger.getLogger(Main.class.getName());
 
     public static void main(String[] args) throws IOException, JAXBException, ParserConfigurationException, SAXException, MaltChainedException, InterruptedException {
         
         System.out.println(CommandLineUtils.COPYRIGHT_MESSEGE);
         
         if (!CommandLineUtils.validateParameters(args)) {
             return;
         }
 
         HashMap<String, String> parameterMap = CommandLineUtils.makeParameterMap(args);
         
         if (parameterMap == null) {
             return;
         }
         
         int inputType = CommandLineUtils.parseDataType(parameterMap.get(CommandLineUtils.INPUT_TYPE_PAR));
         int outputType = CommandLineUtils.parseDataType(parameterMap.get(CommandLineUtils.OUTPUT_TYPE_PAR));
         String optionsPath = (parameterMap.containsKey(CommandLineUtils.OPTIONS_PATH_PAR)) ? parameterMap.get(CommandLineUtils.OPTIONS_PATH_PAR) : new File(".").getCanonicalPath() + "/conf/conf.xml";
 
         Configuration conf = new Configuration();
         conf.loadConfigFileFromFS(optionsPath, Configuration.XML);
 
         List<Integer> commandsList = (parameterMap.containsKey(CommandLineUtils.PIPE_COMMANDS_PAR)) ? CommandLineUtils.makeCommandsList(parameterMap.get(CommandLineUtils.PIPE_COMMANDS_PAR)) : CommandLineUtils.makeCommandsList(conf.getProperty(Configuration.DEFAULT_PIPE));
        String workingDir = (parameterMap.containsKey(CommandLineUtils.WORKING_DIR_PAR)) ? parameterMap.get(CommandLineUtils.WORKING_DIR_PAR) : new File(".").getCanonicalPath();
         String outputDir = (parameterMap.containsKey(CommandLineUtils.OUTPUT_DIR_PAR)) ? parameterMap.get(CommandLineUtils.OUTPUT_DIR_PAR) : null;
 
         ArrayList<Integer> pipeCommands = new ArrayList();
         for (String p : (parameterMap.containsKey(CommandLineUtils.PIPE_COMMANDS_PAR))
                 ? parameterMap.get(CommandLineUtils.PIPE_COMMANDS_PAR).split(";")
                 : conf.getProperty(Configuration.DEFAULT_PIPE).split(";")) {
             pipeCommands.add(CommandLineUtils.parseProcessCommand(p));
         }
 
         String[] inputFiles = (parameterMap.containsKey(CommandLineUtils.INPUT_PAR))
                 ? parameterMap.get(CommandLineUtils.INPUT_PAR).split(";")
                 : CommandLineUtils.getFilesInDir(workingDir);
         String[] outputFiles = (parameterMap.containsKey(CommandLineUtils.OUTPUT_PAR))
                 ? parameterMap.get(CommandLineUtils.OUTPUT_PAR).split(";")
                 : CommandLineUtils.generateOutputFileNames(inputFiles, outputDir, CommandLineUtils.generateOutputExtension(outputType));
 
 
         if (inputFiles.length != outputFiles.length) {
             CommandLineUtils.throwCommandLineError(CommandLineUtils.ERROR_INVALID_PARAMETER_MAP);
             return;
         }
 
         ProcessingLine pl = null;
         try {
             pl = new ProcessingLine(conf);
         } catch (IncorrectInputException ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
         } catch (IncorrectOutputException ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
         } catch (MaltChainedException ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
         } catch (ClarkConfigurationException ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
         Converter converter = new Converter(conf);
 
         for (int i = 0; i < inputFiles.length; i++) {
 
             File input = new File(inputFiles[i]);
             File output = new File(outputFiles[i]);
 
             StringBuilder sb = new StringBuilder();
             sb.append("Processing ");
             sb.append(input.getAbsolutePath());
             sb.append(" ");
             sb.append(String.valueOf(i+1));
             sb.append(" of ");
             sb.append(String.valueOf(inputFiles.length));
             
             System.out.println(sb.toString());
             
             String inputPath = (inputFiles[i].contains(ServiceConstants.SYSTEM_SEPARATOR))
                     ? input.getAbsolutePath() : workingDir + input.getName();
             String outputPath = (outputFiles[i].contains(ServiceConstants.SYSTEM_SEPARATOR))
                     ? output.getAbsolutePath() : workingDir + output.getName();
 
             InputStream is = null;
             OutputStream os = null;
             try {
                 is = new FileInputStream(inputPath);
                 os = new FileOutputStream(outputPath);
             } catch (FileNotFoundException ex) {
                 CommandLineUtils.throwCommandLineError(CommandLineUtils.ERROR_FILE_NOT_FOUND, ex.getMessage());
                 return;
             }
             
             if (parameterMap.containsKey(CommandLineUtils.CONVERTER_PAR)) {
                 System.out.println("Converting from " +
                         parameterMap.get(CommandLineUtils.INPUT_TYPE_PAR) + " to " 
                         + parameterMap.get(CommandLineUtils.OUTPUT_TYPE_PAR));
                 try {
                     converter.convert(is, os, inputType, outputType);
                 } catch (MissingContentException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 } catch (IncorrectInputException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 } catch (IncorrectOutputException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 }
             } else {
                 try {
                     pl.importInput(Misc.readFileInputStream(is), inputType);
                     pl.run(commandsList);
                     pl.exportOutput(os, outputType);
                     os.close();
                 } catch (IncorrectParameterValueException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 } catch (SVMTConnectionExceptoin ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 } catch (IncorrectOutputException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 } catch (ClarkConfigurationException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 } catch (IncorrectInputException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 } catch (MissingContentException ex) {
                     logger.log(Level.SEVERE, ex.getMessage(), ex);
                 }
             }
             
             pl.clear();
             System.out.println("-------------------------------------------------------");
             System.out.println(inputPath.concat(" ...done."));
             System.out.println("Output:".concat(outputPath));
             System.out.println("-------------------------------------------------------");
 
         }
 
         System.out.println("-------------------------------------------------------");
         System.out.println("Done!");
 
     }
 }
