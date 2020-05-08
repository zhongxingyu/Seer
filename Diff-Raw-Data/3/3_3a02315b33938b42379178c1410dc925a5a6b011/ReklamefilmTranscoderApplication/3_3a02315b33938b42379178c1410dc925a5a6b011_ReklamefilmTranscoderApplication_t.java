 package dk.statsbiblioteket.broadcasttranscoder;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
 import dk.statsbiblioteket.broadcasttranscoder.cli.OptionParseException;
 import dk.statsbiblioteket.broadcasttranscoder.cli.OptionsParser;
 import dk.statsbiblioteket.broadcasttranscoder.processors.*;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.FfprobeFetcherProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.GoNoGoProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.ReklamefilmFileResolverProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.ReklamefilmPersistentRecordEnricherProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.ReklamefilmFileResolverImpl;
 import dk.statsbiblioteket.broadcasttranscoder.util.FileUtils;
 import dk.statsbiblioteket.broadcasttranscoder.util.persistence.HibernateUtil;
 import dk.statsbiblioteket.broadcasttranscoder.util.persistence.ReklamefilmTranscodingRecordDAO;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  *
  */
 public class ReklamefilmTranscoderApplication {
 
     private static Logger logger = LoggerFactory.getLogger(ReklamefilmTranscoderApplication.class);
 
     public static void main(String[] args) throws Exception {
         logger.debug("Entered main method.");
         Context context = new OptionsParser().parseOptions(args);
         HibernateUtil util = HibernateUtil.getInstance(context.getHibernateConfigFile().getAbsolutePath());
         context.setTimestampPersister(new ReklamefilmTranscodingRecordDAO(util));
         context.setReklamefilmFileResolver(new ReklamefilmFileResolverImpl(context));
         TranscodeRequest request = new TranscodeRequest();
         File lockFile = FileUtils.getLockFile(request, context);
         if (lockFile.exists()) {
             logger.warn("Lockfile " + lockFile.getAbsolutePath() + " already exists. Exiting.");
             System.exit(2);
         }
         try {
             boolean created = lockFile.createNewFile();
             if (!created) {
                 logger.warn("Could not create lockfile: " + lockFile.getAbsolutePath() + ". Exiting.");
                 System.exit(3);
             }
         } catch (IOException e) {
             logger.warn("Could not create lockfile: " + lockFile.getAbsolutePath() + ". Exiting.");
             System.exit(3);
         }
         try {
             request.setGoForTranscoding(true);
             ProcessorChainElement gonogoer = new GoNoGoProcessor();
             ProcessorChainElement firstChain = ProcessorChainElement.makeChain(gonogoer);
             firstChain.processIteratively(request, context);
             if (request.isGoForTranscoding()) {
                 context.getTimestampPersister().setTimestamp(context.getProgrampid(), context.getTranscodingTimestamp());
 
                 ProcessorChainElement resolver = new ReklamefilmFileResolverProcessor();
                 ProcessorChainElement aspecter = new PidAndAsepctRatioExtractorProcessor();
                 ProcessorChainElement transcoder = new UnistreamVideoTranscoderProcessor();
                ProcessorChainElement renamer = new FinalMediaFileRenamerProcessor();
                 ProcessorChainElement zeroChecker = new ZeroLengthCheckerProcessor();
                 ProcessorChainElement ffprober = new FfprobeFetcherProcessor();
                 ProcessorChainElement snapshotter = new SnapshotExtractorProcessor();
                 ProcessorChainElement reklamePersistenceEnricher = new ReklamefilmPersistentRecordEnricherProcessor();
                 ProcessorChainElement secondChain = ProcessorChainElement.makeChain(
                         resolver,
                         aspecter,
                         transcoder,
                        renamer,
                         zeroChecker,
                         ffprober,
                         snapshotter,
                         reklamePersistenceEnricher
                         );
                 secondChain.processIteratively(request, context);
             }
         } catch (Exception e) {
             //Fault barrier. This is necessary because an uncaught RuntimeException will otherwise not log the pid it
             //failed on.
             logger.error("Error processing " + context.getProgrampid(), e);
             throw(e);
         } finally {
             boolean deleted = lockFile.delete();
             if (!deleted) {
                 logger.error("Could not delete lockfile: " + lockFile.getAbsolutePath());
                 System.exit(4);
             }
         }
 
 
     }
 
 }
