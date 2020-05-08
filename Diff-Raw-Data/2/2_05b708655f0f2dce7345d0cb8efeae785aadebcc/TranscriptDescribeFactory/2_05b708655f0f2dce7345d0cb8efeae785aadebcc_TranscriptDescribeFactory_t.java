 package net.qldarch.ingest.transcript;
 
 import net.qldarch.ingest.Configuration;
 import net.qldarch.ingest.IngestStage;
 import net.qldarch.ingest.IngestStageFactory;
 
 public class TranscriptDescribeFactory implements IngestStageFactory<TranscriptDescribe> {
     public String getStageName() {
        return "describe";
     }
 
     public TranscriptDescribe createIngestStage(Configuration configuration) {
         return new TranscriptDescribe(configuration);
     }
 }
