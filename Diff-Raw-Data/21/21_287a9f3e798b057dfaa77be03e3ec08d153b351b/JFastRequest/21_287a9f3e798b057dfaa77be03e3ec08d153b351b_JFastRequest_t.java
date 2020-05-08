 
 package org.bumblescript.jfast;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 
 
 public class JFastRequest
 {
     private final static int OUTPUT_BUFFER_SIZE = 512;
 
     private Socket socket;
     private InputStream input;
     private OutputStream output;
         
     private int requestId;
     private int role;
     private int flags;
     
     public PrintStream out;
     public PrintStream error;
     public Properties properties;
     public byte data[];
 
     public JFastRequest(Socket socket)
         throws IOException, JFastException
     {
         this.socket = socket;
         properties = new Properties();
         data = new byte[0];
         input = socket.getInputStream();
         output = socket.getOutputStream();
         
         JFastMessage message = null;        
         do
         {               
             message = new JFastMessage(input);
             switch(message.type)
             {                    
                 case JFast.BEGIN_REQUEST:
                     
                     if(this.requestId != 0)
                     {
                         // tell the webserver that we won't handle 
                         // multiplexed connections
                         JFastMessage cancel = new JFastMessage(JFast.VERSION,
                             JFast.END_REQUEST,message.requestId);
                         cancel.setContent(createEndRequestBody(0,
                             JFast.NO_MULTIPLEX_CONNECTION));
                         cancel.write(output);
                     }
                     else
                     {
                         this.requestId = message.requestId;
                         this.flags = message.content[2] & 0xff;
                         this.role = ((message.content[0] & 0xff) << 8) | 
                                      (message.content[1] & 0xff);                        
                     }
                     
                     continue;
                     
                 case JFast.STDIN:
                     appendData(message.content);
                     continue;
                     
                 case JFast.PARAMETERS:
                     collectParameters(message.content);
                     continue;                         
             }                        
         }
         // an empty STDIN means it's our turn
         while(message.type != JFast.STDIN || message.content.length != 0);
         
         out = new PrintStream(new BufferedOutputStream(
             new JFastOutput(output,requestId),OUTPUT_BUFFER_SIZE));
         error = new PrintStream(new BufferedOutputStream(
             new JFastErrorOutput(output,requestId),OUTPUT_BUFFER_SIZE));
     }
         
     public void end()
         throws IOException
     {
         // flush any remaining output
         error.flush();
         out.flush();
         
         // send empty STDERR message before signalling completion
         JFastMessage message = new JFastMessage(JFast.VERSION,
             JFast.STDERR,requestId);
         message.write(output);
         
         // send empty STDOUT message before signalling completion
         message = new JFastMessage(JFast.VERSION,JFast.STDOUT,requestId);
         message.write(output);
     
         // create and send an END_REQUEST message    
         message = new JFastMessage(JFast.VERSION,JFast.END_REQUEST,requestId);
         message.setContent(createEndRequestBody(0,JFast.REQUEST_COMPLETE));        
         message.write(output);

	// Close streams and socket
	try { output.close(); } catch (IOException ex) {}
	try { input.close();  } catch (IOException ex) {}
	try { socket.close(); } catch (IOException ex) {}
     }
     
     private byte[] createEndRequestBody(int applicationStatus,
         int protocolStatus)
     {
         byte content[] = new byte[JFast.END_REQUEST_BODY_LENGTH];        
         for(int i=0;i<content.length;i++)
             content[i] = 0;
             
         content[0] = (byte) ((applicationStatus >> 24) & 0xff);
         content[1] = (byte) ((applicationStatus >> 16) & 0xff);
         content[2] = (byte) ((applicationStatus >> 8) & 0xff);
         content[3] = (byte) (applicationStatus & 0xff);
         content[4] = (byte) protocolStatus;
         
         return content;
     }
         
     private void appendData(byte content[])
     {
         if(content.length > 0)
         {
             if(data == null)
             {
                 data = content;
             }
             else
             {
                 byte replacement[] = new byte[data.length + content.length];
                 System.arraycopy(data,0,replacement,0,data.length);
                 System.arraycopy(content,0,replacement,data.length,
                     content.length);
                     
                 data = replacement;
             }
         }
     }
     
     private void collectParameters(byte content[])
     {
         int length[] = new int[2];
         int offset = 0;
         
         while(offset < content.length)
         {
             for(int i=0;i<2;i++)
             {        
                 // lengths are either 8- or 32-bit
                 length[i]= content[offset++];
                 if((length[i] & 0x80) != 0)
                 {
                     length[i] = ((length[i] & 0x7f) << 24) |
                         ((content[offset++] & 0xff) << 16) |
                         ((content[offset++] & 0xff) << 8) |
                          (content[offset++] & 0xff);
                 }               
             }
             
             String name = new String(content,offset,length[0]);
             String value = new String(content,offset+length[0],length[1]);
             properties.setProperty(name,value);
             
             offset += (length[0] + length[1]);
         }              
     }
 }
