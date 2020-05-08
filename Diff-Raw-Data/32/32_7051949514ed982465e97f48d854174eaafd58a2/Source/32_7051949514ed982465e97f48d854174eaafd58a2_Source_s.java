 package de.weltraumschaf.caythe.frontend;
 
 import de.weltraumschaf.caythe.message.Message;
 import de.weltraumschaf.caythe.message.MessageHandler;
 import de.weltraumschaf.caythe.message.MessageListener;
 import de.weltraumschaf.caythe.message.MessageProducer;
import static de.weltraumschaf.caythe.message.MessageType.SOURCE_LINE;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class Source implements MessageProducer {
     public static final char EOL = '\n';
     public static final char EOF = (char) 0;
 
     protected MessageHandler messageHandler;
     private BufferedReader reader;
     private String line;
     private int lineNumber;
     private int currentPos;
 
     public Source(BufferedReader reader) throws IOException {
         this.reader    = reader;
         lineNumber     = 0;
         currentPos     = -2;
         messageHandler = new MessageHandler();
     }
 
     public int getCurrentPos() {
         return currentPos;
     }
 
     public int getLineNumber() {
         return lineNumber;
     }
 
    public char currentChar() throws Exception {
         // First time?
         if (-2 == currentPos) {
             readLine();
             return nextChar();
         }
         // At end of file?
         else if (null == line) {
             return EOF;
         }
         // At end of line?
         else if ( (-1 == currentPos) || (line.length() == currentPos) ){
             return EOL;
         }
         // Need to read the next line?
         else if (currentPos > line.length()) {
             readLine();
             return nextChar();
         }
         // REturn th character at the current position.
         else {
             return line.charAt(currentPos);
         }
     }
 
    public char nextChar() throws Exception {
         ++currentPos;
         return currentChar();
     }
 
    public char peekChar() throws Exception {
         currentChar();
 
         if (null == line) {
             return EOF;
         }
 
         int nextPos = currentPos + 1;
         return nextPos < line.length() ? line.charAt(nextPos) : EOL;
     }
 
     private void readLine() throws IOException {
         line       = reader.readLine(); // Null when at end of the source.
         currentPos = -1;
 
 	if (line != null) {
             ++lineNumber;
         }
 
         if (null != line) {
            sendMessage(new Message(SOURCE_LINE, new Object[] {lineNumber, line}));
         }
     }
 
    public void close() throws Exception {
         if (null != reader) {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                throw ex;
            }
         }
     }
 
     @Override
     public void addMessageListener(MessageListener listener) {
         messageHandler.addListener(listener);
     }
 
     @Override
     public void removeMessageListener(MessageListener listener) {
         messageHandler.removeListener(listener);
     }
 
     @Override
     public void sendMessage(Message message) {
         messageHandler.sendMessage(message);
     }
 

 }
