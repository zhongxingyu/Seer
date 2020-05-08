 package dk.statsbiblioteket.doms.transformers.fileenricher;
 
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.transformers.common.DomsWebserviceFactory;
 import dk.statsbiblioteket.doms.transformers.common.FFProbeLocationDomsConfig;
 import dk.statsbiblioteket.doms.transformers.common.FileRecordingObjectListHandler;
 import dk.statsbiblioteket.doms.transformers.common.ObjectHandler;
 import dk.statsbiblioteket.doms.transformers.common.ObjectListHandler;
 import dk.statsbiblioteket.doms.transformers.common.callbacks.ExceptionLoggerCallback;
 import dk.statsbiblioteket.doms.transformers.common.callbacks.OutputWriterCallback;
 import dk.statsbiblioteket.doms.transformers.common.callbacks.exceptions.OutputWritingFailedException;
 import dk.statsbiblioteket.doms.transformers.common.TrivialUuidFileReader;
 import dk.statsbiblioteket.doms.transformers.common.UuidFileReader;
 import dk.statsbiblioteket.doms.transformers.common.callbacks.StdoutDisplayCallback;
 import dk.statsbiblioteket.doms.transformers.common.callbacks.exceptions.StopExecutionException;
 import dk.statsbiblioteket.doms.transformers.fileobjectcreator.FFProbeLocationPropertyBasedDomsConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.xml.bind.JAXBException;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 import java.text.ParseException;
 import java.util.List;
 
 /**
  * Tool for enriching Radio/TV file metadata.
  * Takes as input a file with program uuids (one per line). For each file, enriches metadata.
  */
 public class FileEnricher {
     private static final Logger log = LoggerFactory.getLogger(FileEnricher.class);
 
     public static void main(String[] args) throws IOException, StopExecutionException, JAXBException, URISyntaxException, ParseException {
         //TODO: Setup apache CLI
 
         File configFile;
         ChecksumParser checksumParser;
         List<String> uuids;
         UuidFileReader uuidFileReader = new TrivialUuidFileReader();
 
         switch (args.length) {
             case 2:
                 configFile = new File(args[0]);
                 checksumParser = new ChecksumParser(new BufferedReader(new FileReader(new File(args[1]))));
                 System.out.println("Reading uuids from stdin..");
                 uuids = uuidFileReader.readUuids(new BufferedReader(new InputStreamReader(System.in)));
                 run(configFile, checksumParser, uuids);
                 break;
             case 3:
                 configFile = new File(args[0]);
                 checksumParser = new ChecksumParser(new BufferedReader(new FileReader(new File(args[1]))));
                System.out.println("Reading uuids from " + args[1]);
                 File uuidfile = new File(args[2]);
                 uuids = uuidFileReader.readUuids(uuidfile);
                 run(configFile, checksumParser, uuids);
                 break;
             default:
                 System.out.println("Usage: bin/fileenricher.sh config-file checksum-file [uuid-file]");
                 System.exit(1);
         }
     }
 
     private static void run(File configFile, ChecksumParser checksumParser, List<String> uuids) throws IOException, StopExecutionException, JAXBException, URISyntaxException, ParseException {
         FFProbeLocationDomsConfig config = new FFProbeLocationPropertyBasedDomsConfig(configFile);
         CentralWebservice webservice = new DomsWebserviceFactory(config).getWebservice();
 
         ObjectHandler objectHandler = new DomsFileEnricherObjectHandler(config, webservice, checksumParser.getNameChecksumsMap());
         ObjectListHandler objectListHandler = new FileRecordingObjectListHandler(config, objectHandler);
         objectListHandler.addCallback(new OutputWriterCallback(config, objectHandler), OutputWritingFailedException.class);
         objectListHandler.addCallback(new StdoutDisplayCallback());
         objectListHandler.addCallback(new ExceptionLoggerCallback(log));
         try {
             objectListHandler.transform(uuids);
         } catch (OutputWritingFailedException e) {
             String msg = "Failed writing to output-file.";
             log.error(msg, e);
             System.err.println(msg);
         }
     }
 }
