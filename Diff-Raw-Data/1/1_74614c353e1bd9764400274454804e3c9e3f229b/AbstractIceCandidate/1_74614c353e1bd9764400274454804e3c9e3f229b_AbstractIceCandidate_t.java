 package org.lastbamboo.common.ice;
 
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import org.apache.commons.id.uuid.UUID;
 
 /**
  * Class that abstracts out general attributes of all ICE session candidates.
  */
 public abstract class AbstractIceCandidate implements IceCandidate
     {
     private final int m_candidateId;
     private final UUID m_transportId;
     private final int m_priority;
     private final InetSocketAddress m_address;
     
     private Socket m_socket;
 
     /**
      * Creates a new ICE candidate.
      * 
      * @param candidateId The ID of the candidate.
      * @param transportId The unique ID of the transport.
      * @param priority The priority the candidate is to be used in.
      * @param socketAddress The socket address of the candidate.
      */
     protected AbstractIceCandidate(final int candidateId, 
         final UUID transportId, final int priority, 
         final InetSocketAddress socketAddress)
         {
         this.m_candidateId = candidateId;
         this.m_transportId = transportId;
         this.m_priority = priority;
         this.m_address = socketAddress;
         }
 
     public final InetSocketAddress getSocketAddress()
         {
         return m_address;
         }
 
     public final int getCandidateId()
         {
         return m_candidateId;
         }
 
 
     public final int getPriority()
         {
         return m_priority;
         }
 
     public final UUID getTransportId()
         {
         return m_transportId;
         }
 
     public Socket getSocket()
         {
         return m_socket;
         }
 
     public void setSocket(Socket socket)
         {
         m_socket = socket;
         }
 
     }
