 package ua.in.dmitry404.writers;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 /**
  * Class that provides an ability of write information (responses) to OutputStream implementation
  *
  * @author Dmitriy Butakov
  */
 public class OutputStreamWriter implements OutputWriter {
    public static final String SUCCESS_MESSAGE = "OK\n";
    public static final String ERROR_MESSAGE = "ERROR\n";
 
     private OutputStream stream;
 
     /**
      * Construct object with specified OutputStream implementation
      *
      * @param stream specified OutputStream implementation
      */
     public OutputStreamWriter(OutputStream stream) {
         this.stream = stream;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void success() throws WriterException {
         try {
             stream.write(SUCCESS_MESSAGE.getBytes());
         } catch (IOException e) {
             throw new WriterException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void error() throws WriterException {
         try {
             stream.write(ERROR_MESSAGE.getBytes());
         } catch (IOException e) {
             throw new WriterException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void info(String message) throws WriterException {
         try {
             stream.write(message.getBytes());
         } catch (IOException e) {
             throw new WriterException(e);
         }
     }
 }
