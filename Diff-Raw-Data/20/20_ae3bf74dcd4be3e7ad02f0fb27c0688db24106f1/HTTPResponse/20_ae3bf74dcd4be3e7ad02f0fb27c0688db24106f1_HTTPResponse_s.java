 /**
  *
  * @author Devasia
  */
 import java.util.*;
 
 public class HTTPResponse {
 
     private HTTPParser parser;
     
     private byte[] headers;
     private byte[] data;
     private byte[] totalBinaryData;
     private int binaryDataStartPosition;
 
    public HTTPResponse(byte[] b) {
         totalBinaryData = b;
         boolean responseDataAvailable = true;
 
         for (int i = 0; i < b.length; i++) {
             if (b[i] == 13 && b[i + 1] == 10 && b[i + 2] == 13 && b[i + 3] == 10) {
                 System.out.println("found end of headers");
                 binaryDataStartPosition = i + 4;

                if (binaryDataStartPosition >= b.length - 1) {
                    responseDataAvailable = false;
                }
             }
         }
 
         if (responseDataAvailable) {
             headers = Arrays.copyOfRange(b, 0, binaryDataStartPosition - 1);
             data = Arrays.copyOfRange(b, binaryDataStartPosition, b.length);
 
             parser = new HTTPParser(new String(headers));
         }
 
     }
 
     public HTTPParser returnParser() {
         return parser;
     }
 
     public byte[] toHTTPBytes() {
         return totalBinaryData;
     }
 }
