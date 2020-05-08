 package edu.cmu.solarflare;
 
 public class SerialBuffer {
     private int bufferCount;
     private String[] buffers;
     private int currentIndex;
     private String msgInProgress;
     
     SerialBuffer(int bufferCount) {
         this.bufferCount = bufferCount;
         buffers = new String[bufferCount];      // circular list of byte arrays 
         currentIndex = 0;
         msgInProgress = "";
     }
     
     public synchronized void put(String msgPiece) {
         if (msgInProgress.length() < 4 && onlyEscapeChars(msgPiece)) {
             return;
         }
         
        System.out.println("Message piece:");
        for (int i = 0; i < msgPiece.length(); i++) {
            System.out.println(msgPiece.charAt(i) + " (" + (byte) msgPiece.charAt(i) + ")");
        }
         
         int newLineIndex = msgPiece.indexOf('\n');
         if (newLineIndex != -1) {  
             buffers[currentIndex] = msgInProgress + msgPiece.substring(0, newLineIndex + 1);
             msgInProgress = msgPiece.substring(newLineIndex + 1);
            //if (msgInProgress.length() < 4) {
             if (msgInProgress.length() > 0 && onlyEscapeChars(msgInProgress)) {
                 msgInProgress = "";
             }
             currentIndex = (currentIndex + 1) % bufferCount;
             
            System.out.println("Keeping:");
            for (int i = 0; i < msgInProgress.length(); i++) {
                System.out.println(msgInProgress.charAt(i) + " (" + (byte) msgInProgress.charAt(i) + ")");
            }
             
             notify();
         } else {
             msgInProgress += msgPiece;
         }
     }
  
     public synchronized String get() throws InterruptedException {
         if (buffers[(bufferCount + currentIndex - 1) % bufferCount] == null) {
             wait();
         }
         
         String result = "";
         int byteBuffersIndex;
         for (int i = bufferCount - 1; i > 0; i--) {
             byteBuffersIndex = (bufferCount + currentIndex - i) % bufferCount;
             if (buffers[byteBuffersIndex] != null) {
                 result += buffers[byteBuffersIndex];
                 buffers[byteBuffersIndex] = null;
             }
         }
         
         return result;
     }
     
     private boolean onlyEscapeChars(String msgPiece) {
         // check if the msgPiece only contains <Esc>, E, or 0 characters
         if (msgPiece.replace((char) 27, ' ').replace((char) 69, ' ').replace((char) 79, ' ').trim().equals("")) {
         //char lastChar = msgPiece.charAt(msgPiece.length() - 1);
         //if (lastChar == (char) 27 || lastChar == (char) 69 || lastChar == (char) 79) {
             return true;
         } else {
             return false;
         }
     }
 }
