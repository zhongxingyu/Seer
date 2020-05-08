 package dk.statsbiblioteket.broadcasttranscoder;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import dk.statsbiblioteket.broadcasttranscoder.cli.parsers.SingleTranscodingOptionsParser;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.ReklamefilmTranscodingRecord;
 import dk.statsbiblioteket.broadcasttranscoder.processors.*;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.FfprobeFetcherProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.GoNoGoProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.ReklamefilmFileResolverProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.ReklamefilmPersistentRecordEnricherProcessor;
 import dk.statsbiblioteket.broadcasttranscoder.reklamefilm.ReklamefilmFileResolverImpl;
 import dk.statsbiblioteket.broadcasttranscoder.util.FileUtils;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.HibernateUtil;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.ReklamefilmTranscodingRecordDAO;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  *
  */
 public class ReklamefilmTranscoderApplication extends TranscoderApplication {
 
     private static Logger logger = LoggerFactory.getLogger(ReklamefilmTranscoderApplication.class);
 
     public static void main(String[] args) throws Exception {
         logger.debug("Entered main method.");
         SingleTranscodingContext<ReklamefilmTranscodingRecord> context = new SingleTranscodingOptionsParser<ReklamefilmTranscodingRecord>().parseOptions(args);
         HibernateUtil util = HibernateUtil.getInstance(context.getHibernateConfigFile().getAbsolutePath());
         context.setTranscodingProcessInterface(new ReklamefilmTranscodingRecordDAO(util));
         context.setReklamefilmFileResolver(new ReklamefilmFileResolverImpl(context));
         TranscodeRequest request = new TranscodeRequest();
        request.setObjectPid(context.getProgrampid());
 
         try {
             request.setGoForTranscoding(true);
             ProcessorChainElement gonogoer = new GoNoGoProcessor();
             ProcessorChainElement firstChain = ProcessorChainElement.makeChain(gonogoer);
             firstChain.processIteratively(request, context);
             if (request.isGoForTranscoding()) {
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
             transcodingFailed(request,context,e);
             //Fault barrier. This is necessary because an uncaught RuntimeException will otherwise not log the pid it
             //failed on.
             logger.error("Error processing " + request.getObjectPid(), e);
 
             throw(e);
         }
         transcodingComplete(request,context);
 
 
     }
 
 }
