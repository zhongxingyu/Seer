 /**
  * @author      Terrence Howard <chemisus@gmail.com>
  * @copyright   Copyright (c) 2012, Terrence Howard
  * @package     chemisus.socket;
  */
 package chemisus.socket;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * <b>Socket</b> accepts or creates a network connection. This socket can then
  * read and write <b>Packet</b>s.
  * 
  * @author      Terrence Howard <chemisus@gmail.com>
  * @version     0.1
  * @since       0.1
  */
 public class Socket
 {
     /*\**********************************************************************\*/
     /*\                             Constants                                \*/
     /*\**********************************************************************\*/
     private static final int COUNT = 0;
 
     private static final int TRANSFER = 1;
 
     /*\**********************************************************************\*/
     /*\                             Static Fields                            \*/
     /*\**********************************************************************\*/
 
 
     /*\**********************************************************************\*/
     /*\                             Static Methods                           \*/
     /*\**********************************************************************\*/
 
 
     /*\**********************************************************************\*/
     /*\                             Fields                                   \*/
     /*\**********************************************************************\*/
     private final java.net.Socket socket;
     
     private final ConcurrentHashMap<Integer, Class> packets = new ConcurrentHashMap<Integer, Class>();
 
     private final Object write = new Object();
     
     private final Object read = new Object();
     
     private final SocketOutputStream output;
     
     private final SocketInputStream input;
     
     private final long[] in = {0, 0};
     
     private final long[] out = {0, 0};
 
     private final byte[] heads = {
         (byte)255,
         (byte)254,
         (byte)253,
         (byte)252
     };
 
     private final byte[] tails = {
         (byte)155,
         (byte)154,
         (byte)153,
         (byte)152
     };
             
     /*\**********************************************************************\*/
     /*\                             Properties                               \*/
     /*\**********************************************************************\*/
     /**
      * Get the number of packets that have been received by the socket.
      * 
      * @return <b>int</b> the number of packets received by the socket.
      */
     public long getInCount()
     {
         return in[COUNT];
     }
 
     /**
      * Get the number of packets that have been sent by the socket.
      * 
      * @return <b>int</b> the number of packets that have been sent.
      */
     public long getOutCount()
     {
         return out[COUNT];
     }
 
     /*\**********************************************************************\*/
     /*\                             Constructors                             \*/
     /*\**********************************************************************\*/
     /**
      * Create a new socket with an established connection.
      * 
      * @param socket The connection to create a new socket from.
      * @throws IOException 
      */
     public Socket(java.net.Socket socket)
         throws IOException
     {
         this.socket = socket;
         
         output = new SocketOutputStream(
             new BufferedOutputStream(socket.getOutputStream())
         );
         
         input = new SocketInputStream(socket.getInputStream());
     }
 
     /**
      * Connect to a server.
      * 
      * @param host The host name of the server to connect to
      * @param port The port to connect on
      * @throws UnknownHostException
      * @throws IOException
      * @throws ConnectException 
      */
     public Socket(String host, int port)
         throws UnknownHostException, IOException, ConnectException
     {
         this(new java.net.Socket(host, port));
     }
 
     /*\**********************************************************************\*/
     /*\                             Private Methods                          \*/
     /*\**********************************************************************\*/
 
 
     /*\**********************************************************************\*/
     /*\                             Protected Methods                        \*/
     /*\**********************************************************************\*/
     /**
      * Writes the head of the packet.
      * 
      * @throws IOException 
      */
     protected void writeHead()
         throws IOException
     {
         for (byte head : heads)
         {
             output.writeByte(head);
         }
     }
 
     /**
      * Writes the index of the packet.
      * 
      * @throws IOException 
      */
     protected void writeIndex()
         throws IOException
     {
         output.writeLong(out[COUNT]++);
     }
 
     /**
      * Writes the packet index and the actual packet.
      * 
      * @param packet The packet to write.
      * @throws IOException 
      */
     protected void writePacket(Packet packet)
         throws IOException
     {
         output.writeInt(packet.getPacketIndex());
         
         packet.write(output);
     }
 
     /**
      * Writes the tail of the packet.
      * 
      * @throws IOException 
      */
     protected void writeTail()
         throws IOException
     {
         for (byte tail : tails)
         {
             output.writeByte(tail);
         }
     }
 
     /**
      * Reads the head of the packet. If the head of the packet is not present
      * immediately, then it means that the previous packet was most likely
      * corrupt.
      * 
      * @return <b>boolean</b>
      * <table>
      *  <tr>
      *      <td><i>true</i></td>
      *      <td>The head was found with no problems.</td>
      *  </tr>
      *  <tr>
      *      <td><i>false</i></td>
      *      <td>There was a problem finding the head.</td>
      *  </tr>
      * </table>
      * @throws IOException 
      */
     protected boolean readHead()
         throws IOException
     {
         boolean correct = true;
         
         boolean fixed;
 
         do
         {
             fixed = true;
 
             for (byte head : heads)
             {
                 if (head != input.readByte())
                 {
                     fixed = false;
                     
                     correct = false;
 
                     break;
                 }
             }
         } while (!fixed);
         
         return correct;
     }
     
     /**
      * Reads the index of the packet and compares to the number of packets that
      * this socket has received so far.
      * 
      * @return <b>boolean</b>
      * <table>
      *  <tr>
      *      <td><i>true</i></td>
      *      <td>The index of the packet matched.</td>
      *  </tr>
      *  <tr>
      *      <td><i>false</i></td>
      *      <td>The index of the packet did not match.</td>
      *  </tr>
      * </table>
      * @throws IOException 
      */
     protected boolean readIndex()
         throws IOException
     {
         return input.readLong() == in[COUNT]++;
     }
 
     /**
      * Attempts to read the tail. If the tail did not match up, then there was
      * a problem somewhere.
      * 
      * @return <b>boolean</b>
      * <table>
      *  <tr>
      *      <td><i>true</i></td>
      *      <td>The tail was found with no problems.</td>
      *  </tr>
      *  <tr>
      *      <td><i>false</i></td>
      *      <td>There was a problem finding the tail.</td>
      *  </tr>
      * </table>
      * @throws IOException 
      */
     protected boolean readTail()
         throws IOException
     {
         boolean correct = true;
 
         for (byte tail : tails)
         {
             if ((byte)input.read() != tail)
             {
                 correct = false;
                 
                 break;
             }
         }
 
         return correct;
     }
     
     /**
      * Reads a packet from the socket.
      * 
      * @return <b>Packet</b> the packet that was read.
      * @throws IOException 
      */
     protected Packet readPacket()
         throws IOException
     {
         int index = input.readInt();
         
         Packet packet = instancePacket(index);
         
         if (packet == null)
         {
            packet = Packet.Instance(index);
 
             if (packet == null)
             {
                 Logger.getLogger(Packet.class.getName()).log(
                     Level.SEVERE, "Packet {0} does not exist", index);
             }
         }
 
         packet.read(input);
         
         return packet;
     }
 
     /**
      * Creates an instance of a packet based on the index supplied.
      * 
      * @param index The index representing which packet to create.
      * @return <b>Packet</b> the packet that was created.
      */
     protected Packet instancePacket(int index)
     {
         try
         {
             if (packets.containsKey(index))
             {
                 return (Packet)packets.get(index).newInstance();
             }
         }
         catch (NullPointerException ex)
         {
             Logger.getLogger(Packet.class.getName()).log(
                 Level.SEVERE, "The index was: " + index, ex
             );
         }
         catch (InstantiationException ex)
         {
             Logger.getLogger(Packet.class.getName()).log(
                 Level.SEVERE, null, ex
             );
         }
         catch (IllegalAccessException ex)
         {
             Logger.getLogger(Packet.class.getName()).log(
                 Level.SEVERE, null, ex
             );
         }
         
         return null;
     }
     
     /*\**********************************************************************\*/
     /*\                             Public Methods                           \*/
     /*\**********************************************************************\*/
     /**
      * Register a Packet class with this socket.
      * @param c The class of the packet to register.
      * @return <b>boolean</b>
      * <table>
      *  <tr>
      *      <td><i>true</i></td>
      *      <td>The class was successfully added.</td>
      *  </tr>
      *  <tr>
      *      <td><i>false</i></td>
      *      <td>The class was not successfully added.</td>
      *  </tr>
      * </table>
      */
     public boolean registerPacket(Class c)
     {
         try
         {
             packets.put(((Packet)c.newInstance()).getPacketIndex(), c);
             
             return true;
         }
         catch (InstantiationException | IllegalAccessException ex)
         {
             Logger.getLogger(Socket.class.getName()).log(
                 Level.SEVERE, null, ex
             );
         }
         
         return false;
     }
 
     /**
      * Write a <b>Packet</b> to the socket.
      * 
      * @param packet the packet to write.
      * @throws IOException 
      */
     public void write(Packet packet) throws IOException
     {
         synchronized(write)
         {
             writeHead();
             
             writeIndex();
 
             writePacket(packet);
             
             writeTail();
             
             output.flush();
         }
     }
 
     /**
      * Read a <b>Packet</b> from the socket.
      * 
      * @return <b>Packet</b> the packet that was read.
      * @throws IOException 
      */
     public Packet read() throws IOException
     {
         synchronized(read)
         {
             if (!readHead())
             {
                 Logger.getLogger(Socket.class.getName()).log(
                     Level.SEVERE, "readHead() failed."
                 );
             }
 
             if (!readIndex())
             {
                 Logger.getLogger(Socket.class.getName()).log(
                     Level.SEVERE, "readIndex() failed."
                 );
             }
 
             Packet packet = readPacket();
 
             if (!readTail())
             {
                 Logger.getLogger(Socket.class.getName()).log(
                     Level.SEVERE, "readTail() failed."
                 );
             }
             
             return packet;
         }
     }
 
     /**
      * Close the socket.
      * 
      * @throws IOException 
      */
     public void close() throws IOException
     {
         socket.close();
     }
 
     /*\**********************************************************************\*/
     /*\                             Event Triggers                           \*/
     /*\**********************************************************************\*/
 
 
     /*\**********************************************************************\*/
     /*\                             Event Handlers                           \*/
     /*\**********************************************************************\*/
 
 
     /*\**********************************************************************\*/
     /*\                             Classes                                  \*/
     /*\**********************************************************************\*/
     
     
 }
