 package org.bitrepository.commandline.eventhandler;
 
 import org.bitrepository.client.eventhandler.OperationEvent;
 import org.bitrepository.commandline.output.OutputHandler;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.modify.replacefile.conversation.ReplaceFileCompletePillarEvent;
 
 /**
  * Complete event awaiter for Getfile.
  * Prints out checksum results, if any.
  */
 public class ReplaceFileEventHandler extends CompleteEventAwaiter {
 
     /**
      * Constructor.
      * @param settings The settings.
      * @param outputHandler The output handler.
      */
     public ReplaceFileEventHandler(Settings settings, OutputHandler outputHandler) {
         super(settings, outputHandler);
     }
 
     @Override
     public void handleComponentComplete(OperationEvent event) {
         if(!(event instanceof ReplaceFileCompletePillarEvent)) {
             output.warn("ReplaceFileEventHandler received a component complete, which is not a "
                     + ReplaceFileCompletePillarEvent.class.getName());
         }
         
         ReplaceFileCompletePillarEvent pillarEvent = (ReplaceFileCompletePillarEvent) event;
         StringBuilder componentText = new StringBuilder();
        if(pillarEvent.getChecksumForDeletedFile() != null) {
            componentText.append("Checksum for delete: " 
                    + Base16Utils.decodeBase16(pillarEvent.getChecksumForDeletedFile().getChecksumValue()) + "\t");
         }
         
         if(pillarEvent.getChecksumForNewFile() != null) {
             componentText.append("Checksum for new file: " 
                     + Base16Utils.decodeBase16(pillarEvent.getChecksumForNewFile().getChecksumValue()) + "\t");
         }
         
         if(componentText.length() != 0) {
             output.resultLine(pillarEvent.getContributorID() + " : \t" + componentText.toString());
         }
     }
 }
