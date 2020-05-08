 package dk.statsbiblioteket.broadcasttranscoder;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.OptionParseException;
 import dk.statsbiblioteket.broadcasttranscoder.cli.MarkAsCompleteContext;
 import dk.statsbiblioteket.broadcasttranscoder.cli.parsers.MarkAsCompleteOptionsParser;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.TranscodingStateEnum;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.BroadcastTranscodingRecordDAO;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.HibernateUtil;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.BroadcastTranscodingRecord;
 import dk.statsbiblioteket.broadcasttranscoder.processors.*;
 import dk.statsbiblioteket.util.Pair;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This program will read a list of pid/timestamps and add these to the database, so they will not
  * be transcoded automatically.
  */
 public class MarkAsComplete{
 
     private static Logger logger = LoggerFactory.getLogger(ProgramAnalyzer.class);
 
     public static void main(String[] args) throws Exception {
         logger.debug("Entered main method.");
         MarkAsCompleteContext<BroadcastTranscodingRecord> context = null;
         try {
             context = new MarkAsCompleteOptionsParser<BroadcastTranscodingRecord>().parseOptions(args);
             HibernateUtil util = HibernateUtil.getInstance(context.getHibernateConfigFile().getAbsolutePath());
             context.setTranscodingProcessInterface(new BroadcastTranscodingRecordDAO(util));
         } catch (Exception e) {
             logger.error("Error in initial environment", e);
             throw new OptionParseException("Error in initial environment", e);
         }
         for (Pair<String, Long> entry : context.getRecords()) {
             String pid = entry.getLeft();
             Long timestamp = entry.getRight();
 
             TranscodeRequest request;
             request = new TranscodeRequest();
             request.setObjectPid(pid);
 
             BroadcastTranscodingRecord record = context.getTranscodingProcessInterface().read(request.getObjectPid());
             if (record == null){
                 record = new BroadcastTranscodingRecord();
                 record.setID(request.getObjectPid());
                 record.setDomsLatestTimestamp(timestamp);
                 record.setTranscodingState(TranscodingStateEnum.COMPLETE);
                 record.setLastTranscodedTimestamp(timestamp);
                 context.getTranscodingProcessInterface().create(record);
             } else {
                 if (timestamp >= record.getDomsLatestTimestamp()){
                     record.setTranscodingState(TranscodingStateEnum.COMPLETE);
                     record.setLastTranscodedTimestamp(timestamp);
                     record.setDomsLatestTimestamp(timestamp);
                     context.getTranscodingProcessInterface().update(record);
                 }
             }
 
         }
 
     }
 
 }
h
