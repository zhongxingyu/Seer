
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
 
    public HTTPResponse(byte[] b) throws Exception {
         totalBinaryData = b;
         boolean responseDataAvailable = true;
 
         for (int i = 0; i < b.length; i++) {
            /* check for \r\n\r\n - this signified end of headers and start of response body */
             if (b[i] == 13 && b[i + 1] == 10 && b[i + 2] == 13 && b[i + 3] == 10) {
                 System.out.println("found end of headers");
                 binaryDataStartPosition = i + 4;
                break;
             }
         }
 
        if (binaryDataStartPosition == b.length) {
            responseDataAvailable = false;
        } else if (binaryDataStartPosition > b.length) {
            System.err.println("error encountered while parsing bytes from response");
            throw new Exception();
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
