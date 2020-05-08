 /* File:        $Id$
  * Revision:    $Revision$
  * Author:      $Author$
  * Date:        $Date$
  *
  * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 package dk.statsbiblioteket.doms.radiotv.extractor;
 
 import com.sun.grizzly.tcp.Processor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashEstimatorProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ShardAnalyserProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ShardAnalysisOutputProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ShardEnricherProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ShardFixerProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer.IdentifyLongestClipProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer.PreviewGeneratorDispatcherProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter.SnapshotGeneratorDispatcherProcessor;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter.SnapshotPositionFinderProcessor;
 import dk.statsbiblioteket.util.Files;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.xml.bind.JAXBException;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 
 import org.apache.log4j.Logger;
 
 public class ExtractorApplication {
 
     private static Logger log = Logger.getLogger(ExtractorApplication.class);
      public static ServletConfig config =null;
 
 
 
     /**
      * Command line argument to test extraction of programs
      * @param  args [0] = e (Extract), p (Preview), or t (Thumbnail)
      * args[1...n] a list of uuid's of programs to fetch or .xml files containing shard data
      *
      */
     public static void main(String[] args) throws IOException, ProcessorException, JAXBException {
         String configFile = System.getProperty("config");
         log.info("Reading config from " + configFile);
         InputStream is = new FileInputStream(configFile);
         config = new FileBasedServletConfig(is);
         log.info("Starting extraction");
         DomsClient.getDOMSApiInstance(config);
         ServiceTypeEnum service = null;
         for (String arg: args) {
             arg = arg.trim();
             log.info("Processing argument '" + arg + "'");
             if (arg.length() == 1) {
                 if (arg.equals("e")) {
                     service = ServiceTypeEnum.BROADCAST_EXTRACTION;
                 } else if (arg.equals("p")) {
                     service = ServiceTypeEnum.PREVIEW_GENERATION;
                 } else if (arg.equals("t")) {
                     service = ServiceTypeEnum.THUMBNAIL_GENERATION;
                 } else if (arg.equals("a")) {
                     service = ServiceTypeEnum.SHARD_ANALYSIS;
                 } else if (arg.equals("w")) {
                     service = ServiceTypeEnum.SHARD_ANALYSIS_WRITE;
                 }
             } else {
                 log.info("Starting process for '" + arg + "'");
                 if ((new File(arg).exists())) {
                     log.info(arg + " is assummed to be a file");
                     File inputFile = new File(arg);
                     BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                     String line;
                     while ( (line = reader.readLine()) != null) {
                         queueProcessing(service, line);
                     }
                 } else {
                     log.info(arg + " is not a file, assumed to be a pid");
                     queueProcessing(service, arg);
                 }
 
             }
         }
     }
 
     private static void queueProcessing(ServiceTypeEnum service, String arg) throws IOException, ProcessorException {
         TranscodeRequest request = new TranscodeRequest(arg);
         request.setServiceType(service);
         switch(service) {
             case BROADCAST_EXTRACTION:
                 queueExtraction(arg, request);
                 break;
             case PREVIEW_GENERATION:
                 queuePreview(arg, request);
                 break;
             case THUMBNAIL_GENERATION:
                 queueThumbnails(arg, request);
                 break;
             case SHARD_ANALYSIS:
                 queueAnalysis(arg, request, false);
                 break;
             case SHARD_ANALYSIS_WRITE:
                 queueAnalysis(arg,request, true);
         }
     }
 
     private static void queueThumbnails(String arg, TranscodeRequest request) throws IOException, ProcessorException {
         ProcessorChainElement fetcher = new ShardFetcherProcessor();
         ProcessorChainElement parser = new ShardParserProcessor();
         ProcessorChainElement snapshotFinder = new SnapshotPositionFinderProcessor();
         ProcessorChainElement dispatcher = new SnapshotGeneratorDispatcherProcessor();
         fetcher.setChildElement(parser);
         parser.setChildElement(snapshotFinder);
         snapshotFinder.setChildElement(dispatcher);
         ProcessorChainThread thread;
         if (arg.endsWith(".xml")) {
             File file = new File(arg);
             request.setShard(Files.loadString(file));
             request.setPid(file.getName().replace(".xml",""));
             request.setServiceType(ServiceTypeEnum.THUMBNAIL_GENERATION);
             OutputFileUtil.getAndCreateOutputDir(request, config);
             log.debug("Set content: '" + request.getShard() + "'");
             thread = ProcessorChainThread.getIterativeProcessorChainThread(parser, request, config);
         } else {
             thread = ProcessorChainThread.getIterativeProcessorChainThread(fetcher, request, config);
         }
         ProcessorChainThreadPool.addProcessorChainThread(thread);
     }
 
     private static void queuePreview(String arg, TranscodeRequest request) throws IOException, ProcessorException {
         ProcessorChainElement fetcher = new ShardFetcherProcessor();
         ProcessorChainElement parser = new ShardParserProcessor();
         ProcessorChainElement aspecter = new AspectRatioDetectorProcessor();
         ProcessorChainElement pider = new PidExtractorProcessor();
         ProcessorChainElement longer = new IdentifyLongestClipProcessor();
         ProcessorChainElement dispatcher = new PreviewGeneratorDispatcherProcessor();
         fetcher.setChildElement(parser);
         parser.setChildElement(aspecter);
         aspecter.setChildElement(pider);
         pider.setChildElement(longer);
         longer.setChildElement(dispatcher);
         ProcessorChainThread thread;
         if (arg.endsWith(".xml")) {
             File file = new File(arg);
             request.setShard(Files.loadString(file));
             request.setPid(file.getName().replace(".xml",""));
             request.setServiceType(ServiceTypeEnum.PREVIEW_GENERATION);
             OutputFileUtil.getAndCreateOutputDir(request, config);
             log.debug("Set content: '" + request.getShard() + "'");
             thread = ProcessorChainThread.getIterativeProcessorChainThread(parser, request, config);
         } else {
             thread = ProcessorChainThread.getIterativeProcessorChainThread(fetcher, request, config);
         }
         ProcessorChainThreadPool.addProcessorChainThread(thread);
     }
 
     private static void queueExtraction(String arg, TranscodeRequest request) throws IOException {
         ProcessorChainElement transcoder = new FlashTranscoderProcessor();
         ProcessorChainElement estimator = new FlashEstimatorProcessor();
         ProcessorChainElement parser = new ShardParserProcessor();
         ProcessorChainElement aspecter = new AspectRatioDetectorProcessor();
         ProcessorChainElement pider = new PidExtractorProcessor();
         transcoder.setParentElement(estimator);
         estimator.setParentElement(pider);
         pider.setParentElement(aspecter);
         aspecter.setParentElement(parser);
         if (arg.endsWith(".xml")) {
             File file = new File(arg);
             request.setShard(Files.loadString(file));
             request.setPid(file.getName().replace(".xml",""));
             request.setServiceType(ServiceTypeEnum.BROADCAST_EXTRACTION);
             OutputFileUtil.getAndCreateOutputDir(request, config);
             log.debug("Set content: '" + request.getShard() + "'");
         } else {
             ProcessorChainElement fetcher = new ShardFetcherProcessor();
             parser.setParentElement(fetcher);
             request.setPid(arg);
         }
         ProcessorChainThread thread = ProcessorChainThread.getRecursiveProcessorChainThread(transcoder, request, config);
         ProcessorChainThreadPool.addProcessorChainThread(thread);
     }
 
     private static void queueAnalysis(String arg, TranscodeRequest request, boolean writeback) throws IOException {
         ProcessorChainElement parser = new ShardParserProcessor();
         ProcessorChainElement pbcorer = new PBCoreParserProcessor();
         ProcessorChainElement analyser = new ShardAnalyserProcessor();
         ProcessorChainElement outputter = new ShardAnalysisOutputProcessor();
         ProcessorChainElement fixer = new ShardFixerProcessor();
         ProcessorChainElement enricher = new ShardEnricherProcessor();
         parser.setChildElement(pbcorer);
         pbcorer.setChildElement(analyser);
         analyser.setChildElement(outputter);
         outputter.setChildElement(fixer);
         if (writeback) {
             fixer.setChildElement(enricher);
         }
         ProcessorChainThread thread;
         if (arg.endsWith(".xml")) {
             File file = new File(arg);
             request.setShard(Files.loadString(file));
             request.setPid(file.getName().replace(".xml",""));
            request.setServiceType(ServiceTypeEnum.BROADCAST_EXTRACTION);
             OutputFileUtil.getAndCreateOutputDir(request, config);
             log.debug("Set content: '" + request.getShard() + "'");
             thread = ProcessorChainThread.getIterativeProcessorChainThread(parser, request, config);
         } else {
             ProcessorChainElement fetcher = new ShardFetcherProcessor();
             fetcher.setChildElement(parser);
             request.setPid(arg.replaceAll("uuid:",""));
             thread = ProcessorChainThread.getIterativeProcessorChainThread(fetcher, request, config);
         }
         ProcessorChainThreadPool.addProcessorChainThread(thread);
     }
 
 }
