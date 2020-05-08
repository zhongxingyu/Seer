 package com.dsp.ass1;
 
 import java.net.URL;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.concurrent.TimeUnit;
 
 import java.awt.image.BufferedImage;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.util.PDFTextStripper;
 import org.apache.pdfbox.util.PDFText2HTML;
 
 import org.apache.commons.io.FilenameUtils;
 
 import javax.imageio.ImageIO;
 
 import com.amazonaws.AmazonClientException;
 
 import com.amazonaws.auth.AWSCredentials;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.CannedAccessControlList;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 
 import com.amazonaws.services.sqs.AmazonSQS;
 import com.amazonaws.services.sqs.AmazonSQSClient;
 import com.amazonaws.services.sqs.model.DeleteMessageRequest;
 import com.amazonaws.services.sqs.model.Message;
 import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
 
 
 public class Worker {
     // TODO skip INFO messages on production.
     private static final Logger logger = Logger.getLogger(Worker.class.getName());
 
 
     // Returns a PDDocument from a string URL or null on error.
     private static PDDocument getDocument(String url) {
         PDDocument doc;
 
         try {
             doc = PDDocument.load(new URL(url));
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         return doc;
     }
 
 
     // Converts PDF file to PNG image and uploads it to S3.
     private static String toImage(PDPage page, String base, String missionNumber, AmazonS3 s3) {
         String fileName = missionNumber + "_" + System.currentTimeMillis() + "_"+ base + ".png";
         BufferedImage img;
 
         logger.info("Image: " + fileName);
 
         try {
             img = page.convertToImage();  // PDF to PNG image.
             return uploadImageToS3(s3, fileName, img);
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
     }
 
 
     // Converts PDF to HTML and uploads it to S3.
     private static String toHTML(PDDocument doc, String base, String missionNumber, AmazonS3 s3) {
         String fileName = missionNumber + "_" + System.currentTimeMillis() + "_" + base + ".html";
         logger.info("HTML: " + fileName);
 
         String pageText;
         PDFTextStripper stripper;
 
         try {
             stripper = new PDFText2HTML("utf-8");  // encoding: utf-8
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         try {
             pageText = stripper.getText(doc);
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         return Utils.uploadFileToS3(s3, fileName, pageText);
     }
 
 
     // Converts PDF to clear text file and uploads it to S3.
     private static String toText(PDDocument doc, String base, String missionNumber, AmazonS3 s3) {
         String fileName = missionNumber + "_" + System.currentTimeMillis() + "_" + base + ".txt"; // TODO choose name for the file
         logger.info("Text: " + fileName);
 
         PDFTextStripper stripper;
         String pageText;
 
         try {
             stripper = new PDFTextStripper();
             stripper.setStartPage(1);
             stripper.setEndPage(1);
             pageText = stripper.getText(doc);
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         return Utils.uploadFileToS3(s3, fileName, pageText);
     }
 
 
     // Receives an action, PDF file url, and S3 connection and process action
     // on PDF url. Afterwards uploads result to S3.
     private static String handleDocument(String action, String url, String missionNumber ,AmazonS3 s3) {
         logger.info("Handling: " + action + "\t" + url + "\t" + missionNumber);
 
         String base = FilenameUtils.getBaseName(url),
                 result = null;
         PDDocument doc = getDocument(url);
 
         if (doc == null) {
             return null;
         } else {
            if (action.equals("ToImage") && (doc.getDocumentCatalog().getAllPages()).size()>0) {
                 PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
                 result = toImage(page, base, missionNumber, s3);
             }
             else if (action.equals("ToText")) {
                 result = toText(doc, base, missionNumber,s3);
             }
             else if (action.equals("ToHTML")) {
                 result = toHTML(doc, base, missionNumber, s3);
             }
         }
 
         logger.info("Finished handling: " + action + "\t" + url + "\t" + missionNumber);
 
         try {
             doc.close();
         } catch (IOException e) {
             logger.severe(e.getMessage());
         }
 
         return result;
     }
 
 
     // Deletes a 'new PDF task ..' message.
     private static void deleteTaskMessage(Message msg, String sqsUrl, AmazonSQS sqs) {
         String handle = msg.getReceiptHandle();
 
         try {
             sqs.deleteMessage(new DeleteMessageRequest(sqsUrl, handle));
             logger.info("Deleted: " + msg.getBody());
         } catch (AmazonClientException e){
             logger.severe(e.getMessage());
         }
     }
 
 
     // Sends a 'done PDF task ..' message to the queue.
     private static void sendFinishedMessage(Message msg, String pos, String sqsUrl, AmazonSQS sqs) {
         // TODO make this message more verbose i.e. send instance id etc.
         // action = split[1] , input = split[2], output = split[3], missionNumber = split[4];
         String[] splitter = msg.getBody().split("\t");
         // TODO need to add s3 location of result, according to instructions.
         // TODO msg might not spliter to 3 parts, thus splitter[1] or [2] will throw an exception.
        String reply = "done PDF task\t" + splitter[1] + "\t" + splitter[2] + "\t" + pos + "\t" + splitter[3];
        Utils.sendMessage(sqs, sqsUrl, reply);
    }


    // Sends a 'Failed PDF task ..' message to the queue.
    private static void sendFailedMessage(Message msg, String sqsUrl, AmazonSQS sqs) {
        // action = split[1] , input = split[2], output = split[3], missionNumber = split[4];
        String[] splitter = msg.getBody().split("\t");
        String reply = "Failed PDF task\t" + splitter[1] + "\t" + splitter[2] + "\t" + "null" + "\t" + splitter[3];
        Utils.sendMessage(sqs, sqsUrl, reply);
     }
 
 
     private static String uploadImageToS3(AmazonS3 s3, String fileName, BufferedImage img) {
         // Get S3 file address (file doesn't exist yet, we're going to save the data to this address.)
         // TODO we don't need to use getS3FileAddress() ! it creates a file which we don't need!
         String address = Utils.getS3FileAddress(s3, fileName);
 
         if (address != null) {
             ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 
             // Write image to file.
             try {
                 ImageIO.write(img, "png", outStream);
             } catch (IOException e) {
                 logger.severe(e.getMessage());
                 return null;
             }
 
             // Upload saved image to S3.
             byte[] buffer = outStream.toByteArray();
             ObjectMetadata meta = new ObjectMetadata();
             meta.setContentLength(buffer.length);
             InputStream inStream = new ByteArrayInputStream(buffer);
             PutObjectRequest request = new PutObjectRequest(Utils.bucket, Utils.path + fileName, inStream, meta);
 
             try {
                 s3.putObject(request.withCannedAcl(CannedAccessControlList.PublicRead));
             } catch (AmazonClientException e) {
                 logger.severe(e.getMessage());
                 return null;
             }
         }
 
         return address;
     }
 
 
     // Processes (or ignores) messages.
     private static String handleTaskMessage(Message msg, AmazonS3 s3) {
         String body = msg.getBody();
         logger.info("Task queue received: " + body);
 
         String[] parts = msg.getBody().split("\t");
 
        if (parts[0].equals("new PDF task") && parts.length >= 4) {
             return handleDocument(parts[1], parts[2], parts[3], s3); // parts[1] = action , parts [2] = link , parts[3] = mission counter
             // TODO Shutdown message should include a worker name (or tag name?)
             // so each worker will know the message if the message is intended
             // to him or noted to him or not. Something like this:
             // } else if (parts[0].equals("shutdown") && parts.length >= 2 && parts[1].equals(ami-identifier) {
         } else  {
             logger.info("Ignoring: " + body);
             return null;
         }
     }
 
 
     private static String handleShutdownMessage(Message msg, AmazonS3 s3) {
         String body = msg.getBody();
         logger.info("shutdown queue received : " + body);
 
         String[] parts = msg.getBody().split("\t");
 
         if (parts[0].equals("shutdown")) {
            logger.info("got shutting down message - starting to shut down.");
             return "shutdown";
         } else  {
             logger.info("Ignoring: " + body);
             return null;
         }
     }
 
 
     public static void main(String[] args) {
         // TODO split main() to multiple functions.
 
         Utils.setLogger(logger);
         logger.info("Starting.");
 
         String result;
 
         AWSCredentials creds = Utils.loadCredentials();
         if(creds == null) {
             logger.severe("Can't find  the credentials file : closing...");
             // TODO close myself
             return;
         }
 
         AmazonSQS sqs = new AmazonSQSClient(creds);
 
         AmazonS3 s3 = new AmazonS3Client(creds);
 
         ReceiveMessageRequest taskReq = new ReceiveMessageRequest(Utils.tasksUrl),
                 shutdownReq = new ReceiveMessageRequest(Utils.shutdownUrl);
 
         List<Message> taskMsgs , shutdownMsgs;
         Message msg;
 
         // Process messages.
         taskMsgs = Utils.getMessages(taskReq, sqs);
         shutdownMsgs = Utils.getMessages(shutdownReq, sqs);
         while (true) {
             // Sleep if no messages arrived, and retry re-fetch new ones afterwards.
             if (Utils.isEmpty(taskMsgs) && Utils.isEmpty(shutdownMsgs)) {
                 logger.info("No messages, sleeping.");
 
                 try {
                     TimeUnit.SECONDS.sleep(5);
                 } catch(InterruptedException e) {
                     logger.severe(e.getMessage());
                     result = "shutdown";
                 }
 
             }
             else {
                 if (!Utils.isEmpty(taskMsgs)){
                     msg = taskMsgs.get(0);
                     result = handleTaskMessage(msg, s3);
                     if (result != null) {
                         sendFinishedMessage(msg, result, Utils.finishedUrl, sqs);
                     }
                    else {
                        sendFailedMessage(msg, Utils.finishedUrl, sqs);
                    }
                    deleteTaskMessage(msg, Utils.tasksUrl, sqs);
                 }
                 if (!Utils.isEmpty(shutdownMsgs)) {
                     msg = shutdownMsgs.get(0);
                     result = handleShutdownMessage(msg, s3);
                     if (result != null && result.equals("shutdown")) {
                         break;
                     }
                 }
             }
             taskMsgs = Utils.getMessages(taskReq, sqs);
             shutdownMsgs = Utils.getMessages(shutdownReq, sqs);
         }
        // TODO close myself
     }
 }
