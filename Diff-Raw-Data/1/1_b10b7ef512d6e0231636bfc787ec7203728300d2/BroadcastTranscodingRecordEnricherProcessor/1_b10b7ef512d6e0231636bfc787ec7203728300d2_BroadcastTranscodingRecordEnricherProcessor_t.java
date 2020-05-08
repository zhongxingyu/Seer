 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
 import dk.statsbiblioteket.broadcasttranscoder.util.MetadataUtils;
 import dk.statsbiblioteket.broadcasttranscoder.util.persistence.BroadcastTranscodingRecord;
 import dk.statsbiblioteket.broadcasttranscoder.util.persistence.BroadcastTranscodingRecordDAO;
 import dk.statsbiblioteket.broadcasttranscoder.util.persistence.HibernateUtil;
 
 /**
  * Created with IntelliJ IDEA.
  * User: csr
  * Date: 12/4/12
  * Time: 2:43 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BroadcastTranscodingRecordEnricherProcessor extends ProcessorChainElement {
     @Override
     protected void processThis(TranscodeRequest request, Context context) throws ProcessorException {
         HibernateUtil util = HibernateUtil.getInstance(context.getHibernateConfigFile().getAbsolutePath());
         BroadcastTranscodingRecordDAO dao = new BroadcastTranscodingRecordDAO(util);
         BroadcastTranscodingRecord record = dao.read(context.getProgrampid());
         if (record == null) {
             throw new ProcessorException("Attempted to enrich metadata for a non-existent database " +
                     "record for pid " + context.getProgrampid());
         }
         record.setTvmeter(request.isTvmeter());
         record.setBroadtcastStartTime(MetadataUtils.getProgramStart(request));
         record.setBroadcastEndTime(MetadataUtils.getProgramEnd(request));
         record.setChannel(request.getProgramBroadcast().getChannelId());
         record.setEndOffset(request.getEndOffsetUsed());
         record.setStartOffset(request.getStartOffsetUsed());
         record.setTitle(request.getTitle());
        record.setTranscodingCommand(request.getTranscoderCommand());
         dao.update(record);
     }
 }
