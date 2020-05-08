 /*
  * Created on May 12, 2006
  */
 package ecologylab.services.nio;
 
 import java.io.IOException;
 import java.net.BindException;
 import java.net.InetSocketAddress;
 import java.net.PortUnreachableException;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.util.Iterator;
 
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.generic.StartAndStoppable;
 import ecologylab.services.ServerConstants;
 import ecologylab.services.ServicesClientBase;
 import ecologylab.services.messages.RequestMessage;
 import ecologylab.services.messages.ResponseMessage;
 import ecologylab.xml.NameSpace;
 import ecologylab.xml.XmlTranslationException;
 
 /**
  * Services Client using NIO; a major difference with the NIO version is state
  * tracking. Since the sending methods do not wait for the server to return.
  * 
  * This object will listen for incoming messages from the server, and will send
  * any messages that it recieves on its end.
  * 
  * Since the underlying implementation is TCP/IP, messages sent should be sent
  * in order, and the responses should match that order.
  * 
  * Another major difference between this and the non-NIO version of
  * ServicesClient is that it is StartAndStoppable.
  * 
  * @author Zach Toups (toupsz@gmail.com)
  */
 public class ServicesClientNIO extends ServicesClientBase implements
         StartAndStoppable, Runnable, ServerConstants
 {
     private Selector        selector;
 
     private boolean         running          = false;
 
     private SocketChannel   channel;
 
     private Thread          thread;
 
     private SelectionKey    key;
 
     private ByteBuffer      incomingRawBytes = ByteBuffer
                                                      .allocate(MAX_PACKET_SIZE);
 
     private CharBuffer      outgoingChars    = CharBuffer
                                                      .allocate(MAX_PACKET_SIZE);
 
     private StringBuffer    accumulator      = new StringBuffer(MAX_PACKET_SIZE);
 
     private CharsetDecoder  decoder          = Charset.forName("ASCII")
                                                      .newDecoder();
 
     private CharsetEncoder  encoder          = Charset.forName("ASCII")
                                                      .newEncoder();
 
     private ResponseMessage responseMessage  = null;
     
     private Iterator incoming;
 
     public ServicesClientNIO(String server, int port, NameSpace messageSpace,
             ObjectRegistry objectRegistry)
     {
         super(server, port, messageSpace, objectRegistry);
     }
 
     public void disconnect()
     {
         try
         {
             if (connected())
             {
                 channel.close();
             }
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     public boolean connected()
     {
         if (channel != null)
         {
             return channel.isConnected();
         } else
         {
             return false;
         }
     }
 
    /**
     * Side effect of calling start().
     */
     protected boolean createConnection()
     {
         try
         {
             // get the selector
             selector = Selector.open();
 
             // create the channel and connect it to the server
             channel = SocketChannel.open(new InetSocketAddress(server, port));
 
             // disable blocking
             channel.configureBlocking(false);
 
             // link in the socket from the channel
             socket = channel.socket();
 
             if (connected())
             {
                 // register the channel for read operations, now that it is
                 // connected
                 channel.register(selector, SelectionKey.OP_READ);
             }
         } catch (BindException e)
         {
             debug("Couldnt create socket connection to server '" + server
                     + "': " + e);
             // e.printStackTrace();
             socket = null;
         } catch (PortUnreachableException e)
         {
             debug("Server is alive, but has no daemon on port " + port + ": "
                     + e);
             // e.printStackTrace();
             socket = null;
         } catch (SocketException e)
         {
             debug("Server '" + server + "' unreachable: " + e);
         } catch (IOException e)
         {
             debug("Bad response from server: " + e);
             // e.printStackTrace();
             socket = null;
         }
 
        if (connected())
        {
            start();
        }
        
         return connected();
     }
 
     public void sendMessage(RequestMessage request)
     {
         // translate the response and store it, then
         // encode it and write it
         if (connected())
         {
             try
             {
 
                 outgoingChars.clear();
                 outgoingChars.put(request.translateToXML(false)).put('\n');
                 outgoingChars.flip();
 
                 channel.write(encoder.encode(outgoingChars));
             } catch (NullPointerException e)
             {
                 e.printStackTrace();
                 System.out.println("recovering.");
             } catch (XmlTranslationException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (CharacterCodingException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         } else
         {
             debug("Attempted to send message, but not connected.");
         }
     }
 
     public void start()
     {
         running = true;
 
         if (thread == null)
         {
             thread = new Thread(this, "Client thread.");
             thread.start();
         }
     }
 
     public void stop()
     {
         System.err.println("shutting down client listening thread.");
 
         running = false;
        
        // dispose of thread
        thread = null;
     }
 
     public void run()
     {
         while (running)
         {
             try
             {
                 if (connected())
                 {
                     if (selector.select() > 0)
 
                     { // there is something to read; only register one
                         // channel,
                         // so...
                         incoming = selector.selectedKeys().iterator();
                         
                         key = (SelectionKey) incoming
                                 .next();
                         
                         incoming.remove();
 
                         if (key.isReadable())
                         {
                             try
                             {
                                 while (channel.read(incomingRawBytes) > 0)
                                 {
                                     incomingRawBytes.flip();
 
                                     accumulator.append(decoder
                                             .decode(incomingRawBytes));
 
                                     incomingRawBytes.clear();
 
                                     if (accumulator.length() > 0)
                                     {
                                         if ((accumulator.charAt(accumulator
                                                 .length() - 1) == '\n')
                                                 || (accumulator
                                                         .charAt(accumulator
                                                                 .length() - 1) == '\r'))
                                         { // when we have accumulated an
                                             // entire
                                             // message,
                                             // process it
 
                                             // in case we have several messages
                                             // that are split by returns
                                             while (accumulator.length() > 0)
                                             {
                                                 // transform the message into a
                                                 // request and
                                                 // perform the service
                                                 // long time =
                                                 // System.currentTimeMillis();
 
                                                 processString(accumulator
                                                         .substring(
                                                                 0,
                                                                 accumulator
                                                                         .indexOf("\n")));
 
                                                 // System.out.println("time:
                                                 // "+(System.currentTimeMillis()-time));
 
                                                 // erase the message from the
                                                 // accumulator
                                                 accumulator
                                                         .delete(
                                                                 0,
                                                                 accumulator
                                                                         .indexOf("\n") + 1);
                                             }
                                         }
                                     }
                                 }
                             } catch (CharacterCodingException e1)
                             {
                                 // TODO Auto-generated catch block
                                 e1.printStackTrace();
                             } catch (IOException e1)
                             {
                                 // TODO Auto-generated catch block
                                 e1.printStackTrace();
                             }
                         } else
                         {
                             debug("Key is selected and not readable!");
                         }
                     }
                 }
             } catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
 
         System.out.println("Thread shutting down.");
 
     }
 
     private void processString(String incomingMessage)
     {
 //        System.out.println("client got the following: " + incomingMessage);
 
         try
         {
             responseMessage = translateXMLStringToResponseMessage(incomingMessage);
 
         } catch (XmlTranslationException e)
         {
             e.printStackTrace();
         }
 
         if (responseMessage == null)
         {
             debug("ERROR: translation failed: ");
 
         } else
         {
             // perform the service being requested
             processResponse(responseMessage);
         }
     }
 }
