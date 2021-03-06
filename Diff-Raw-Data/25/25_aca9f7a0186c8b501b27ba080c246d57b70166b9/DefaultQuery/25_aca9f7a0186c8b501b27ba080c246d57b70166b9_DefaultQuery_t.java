 package enginuity.logger.query;
 
 import enginuity.logger.comms.SerialReader;
 import enginuity.logger.comms.SerialWriter;
 import enginuity.logger.exception.SerialCommunicationException;
 import static enginuity.util.ParamChecker.checkNotNull;
 
// TODO: Add a read timeout so doesn't wait forever?

 public final class DefaultQuery implements Query {
     private byte[] request;
 
     public DefaultQuery(byte[] request) {
         this.request = request;
     }
 
     public byte[] execute(SerialWriter writer, SerialReader reader) {
         checkNotNull(writer, reader);
         writer.write(request);
         byte[] response;
         while ((response = reader.read()).length == 0) {
            waitForResponse();
         }
         return response;
     }
 
    private void waitForResponse() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            throw new SerialCommunicationException(e);
        }
    }

 }
