 package dk.statsbiblioteket.doms.transformers.fileenricher;
 
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.transformers.common.DomsConfig;
 import dk.statsbiblioteket.doms.transformers.common.ObjectHandler;
 
 /**
  * Use DOMS to enrich file metadata.
  */
 public class DomsFileEnricherObjectHandler implements ObjectHandler {
 
     private final DomsConfig config;
     private final CentralWebservice webservice;
 
     /**
      * Initialise object handler.
      * @param config Configuration.
      * @param webservice The DOMS WebService.
      */
     public DomsFileEnricherObjectHandler(DomsConfig config, CentralWebservice webservice) {
         this.config = config;
         this.webservice = webservice;
     }
 
     @Override
     public void transform(String uuid) throws Exception {
         //TODO Use metadata in file name and from channel mapping database to update file object metadata.
         throw new UnsupportedOperationException("Not implemented yet");
     }
 
 }
