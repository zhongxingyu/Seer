 package com.cafeform.iumfs;
 
 import com.cafeform.iumfs.handler.RequestHandler;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.channels.FileChannel;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import static com.cafeform.iumfs.handler.AbstractRequestHandler.*;
 
 /**
  * Handler which handles request from IUMFS files sytem.
  */
 public class RequestListener implements Runnable 
 {
     private static final Logger logger = Logger.getLogger(RequestListener.class.getName());
     private static final String CONTROLL_DEVICE_NAME = "/dev/iumfscntl";
     private final FileFactory fileFactory;
     
     public RequestListener (FileFactory fileFactory)
     {
         this.fileFactory = fileFactory;
     }
 
     @Override
     public void run() 
     {
         ByteBuffer buffer = ByteBuffer.allocate(RequestImpl.DEVICE_BUFFER_SIZE);
         buffer.order(ByteOrder.nativeOrder());
         int len;
         Request request = null;
         FileChannel fileChannel = openControllDevice();
         RequestHandlerFactory handlerFactory = new RequestHandlerFactory();
 
         logger.fine("Successfully open device.");
         logger.fine("Started");
 
         while (true) 
         {
             RequestHandler handler;
             Response response = null;
             try 
             {
                 // Read request data from iumfs device
                 buffer.clear();
                 if ((len = fileChannel.read(buffer)) < 0) 
                 {
                     logger.severe("failed to read data from device");
                     System.exit(1);
                 }
                 logger.log(Level.FINER, "device returns " + len + " bytes ");
 
                 // Create request object
                 request = RequestFactory.createRequest(buffer);
                 
                 if (null == request) 
                 {
                     logger.severe("Request object is null");
                     System.exit(1);
                 }
                 logger.log(Level.FINE, request.getType() + " recieved.");                
                 
                 IumfsFile file = fileFactory.getFile(request);
                 handler = handlerFactory.getHandler(request.getType());
                 response = handler.getResponse(request, file);
                logger.log(Level.FINE, request.getType() + " returned with " +
                         response.getResult() + ".");                                
             }
             catch (RuntimeException ex) 
             {
                 /*
                  * It is important to return an error to driver, even
                  * if exception happend.
                  * Convert RuntimeException to EIO error.
                  */
                 if (null != request)
                 {
                     logger.log(Level.SEVERE, "Failed to handle " +
                             request.getType(), ex);
                     response = new ResponseImpl(request.getType(), EIO);                    
                 }
                 else 
                 {
                     logger.log(Level.SEVERE, "Cannot read request", ex);
                     System.exit(1);                
                 }
             } 
             catch (IOException ex)
             {
                 logger.log(Level.SEVERE, "Cannot read request", ex);
                 System.exit(1);
             }
             
             if (null == response)
             {
                 response = new ResponseImpl(request.getType(), EIO);
             }
             
             // Write response to driver.
             try
             {
                 fileChannel.write(response.getBuffer());
             }
             catch (IOException ex) 
             {
                 logger.log(Level.SEVERE, "Cannot write response", ex);
                 System.exit(1);
             }
             logger.finer("request for " + request.getType() + " finished.");
         }
     }
     
     private FileChannel openControllDevice () 
     {
 
         RandomAccessFile controllDeviceFile = null;
         FileChannel ch = null;        
 
         try 
         {
             controllDeviceFile = new RandomAccessFile(CONTROLL_DEVICE_NAME, "rw");
         }
         catch (FileNotFoundException ex) 
         {
             logger.log(Level.SEVERE, "Cannot open " + CONTROLL_DEVICE_NAME, ex);            
             System.exit(1);
         }
         
         if(null == controllDeviceFile)
         {
             logger.severe("Cannot open " + CONTROLL_DEVICE_NAME);
             System.exit(1);
         } 
 
         return controllDeviceFile.getChannel();
     }
 }
